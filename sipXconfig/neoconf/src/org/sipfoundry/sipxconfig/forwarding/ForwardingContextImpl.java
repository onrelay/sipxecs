/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.forwarding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.SqlTypes;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.DSTChangeEvent;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.dialplan.AttendantRule;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.setting.Group;


/**
 * ForwardingContextImpl
 */
public class ForwardingContextImpl extends SipxHibernateDaoSupport<Object> implements ForwardingContext,
        ApplicationListener, DaoEventListener {
    private static final Log LOG = LogFactory.getLog(ForwardingContextImpl.class);
    private static final String PARAM_SCHEDULE_ID = "scheduleId";
    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_USER_GROUP_ID = "userGroupId";
    private static final String PARAM_FEATURE_ID = "featureId";
    private static final String PARAM_NAME = "name";
    private static final String SQL_CALLSEQUENCE_IDS = "select distinct u.user_id from users u";
    private CoreContext m_coreContext;
    private JdbcTemplate m_jdbcTemplate;
    private SipxReplicationContext m_sipxReplicationContext;
    private ConfigManager m_configManager;

    /**
     * Looks for a call sequence associated with a given user.
     *
     * This version just assumes that CallSequence id is the same as user id. More general
     * implementation would run a query. <code>
     *      String ringsForUser = "from CallSequence cs where cs.user = :user";
     *      super.findByNamedParam(ringsForUser, "user", user);
     * </code>
     *
     * @param user for which CallSequence object is retrieved
     */
    @Override
    public CallSequence getCallSequenceForUser(User user) {
        return getCallSequenceForUserId(user.getId());
    }

    public void notifyCommserver(Collection<CallSequence> callSequences) {
        // Load call sequences, because aliases have been changed
        // TODO: replicate only call seq with those aliases
        for (CallSequence callSequence : callSequences) {
            getDaoEventPublisher().publishSave(callSequence);
        }
    }

    @Override
    public void saveCallSequence(CallSequence callSequence) {
        super.mergeEntity(callSequence);
        m_coreContext.saveUser(callSequence.getUser());
    }

    @Override
    public CallSequence getCallSequenceForUserId(Integer userId) {
        return super.findEntity(CallSequence.class, userId);
    }

    private void removeCallSequenceForUserId(Integer userId) {
        CallSequence callSequence = getCallSequenceForUserId(userId);
        callSequence.clear();
        super.mergeEntity(callSequence);
        getDaoEventPublisher().publishDelete(callSequence);
        super.flush();
    }

    public void removeSchedulesForUserID(Integer userId) {
        List schedules = getPersonalSchedulesForUserId(userId);
        super.removeAllEntities(schedules);
    }

    @Override
    public Ring getRing(Integer id) {
        return super.loadEntity(Ring.class, id);
    }

    /**
     * Loads call sequences for all uses in current root organization
     *
     * @return list of CallSequence objects
     */
    private List<CallSequence> loadAllCallSequences() {
        
        return super.getSessionFactory().fromTransaction( session -> {

            List<CallSequence> callSequences = new ArrayList<>();

            // Native SQL query returning scalar (Integer) results
            List<Integer> ids = session.createNativeQuery(SQL_CALLSEQUENCE_IDS, Integer.class).getResultList();

            for (Integer id : ids) {
                CallSequence cs = getCallSequenceForUserId(id);
                if (CollectionUtils.isNotEmpty(cs.getRings())) {
                    callSequences.add(cs);
                }
            }

            return callSequences;
        });
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public Collection<CallSequence> getCallSequencesForGroup(Group group) {
        Collection<CallSequence> ids = new HashSet<CallSequence>();
        for (Integer id : m_coreContext.getGroupMembersIds(group)) {
            ids.add(getCallSequenceForUserId(id));
        }
        return ids;
    }

    @Override
    public List<Schedule> getPersonalSchedulesForUserId(Integer userId) {

        return (List<Schedule>)super.findByNamedQueryAndNamedParam("userSchedulesForUserId", 
            PARAM_USER_ID, 
            userId,
            Schedule.class);
    }

    public List<Ring> getRingsForScheduleId(Integer scheduleId) {

        return (List<Ring>)super.findByNamedQueryAndNamedParam("ringsForScheduleId", 
            PARAM_SCHEDULE_ID, 
            scheduleId,
            Ring.class );
    }

    private List<DialingRule> getDialingRulesForScheduleId(Integer scheduleId) {

        return (List<DialingRule>)super.findByNamedQueryAndNamedParam(
            "dialingRulesForScheduleId", 
            PARAM_SCHEDULE_ID, 
            scheduleId,
            DialingRule.class );
    }

    @Override
    public Schedule getScheduleById(Integer scheduleId) {
        return super.loadEntity(Schedule.class, scheduleId);
    }

    @Override
    public void saveSchedule(Schedule schedule) {
        if (schedule.isNew()) {
            // check if new object
            checkForDuplicateNames(schedule);
            super.saveEntity(schedule);
        } else {
            // on edit action - check if the name for this schedule was modified
            // if the name was changed then perform duplicate name checking
            if (isNameChanged(schedule)) {
                checkForDuplicateNames(schedule);
            }
            super.saveEntity(schedule);
            List<Ring> rings = getRingsForScheduleId(schedule.getId());
            Collection<CallSequence> css = new HashSet<CallSequence>();
            if (rings != null) {
                for (Ring ring : rings) {
                    css.add(ring.getCallSequence());
                }
            }
            notifyCommserver(css);
            getDaoEventPublisher().publishSave(schedule);
        }
    }

    private void checkForDuplicateNames(Schedule schedule) {
        if (isNameInUse(schedule)) {
            throw new UserException("A schedule with name {0} is already defined", schedule.getName());
        }
    }

    private boolean isNameInUse(Schedule schedule) {
        List<Schedule> count = null;
        if (schedule instanceof UserSchedule) {
            count = super.findByNamedQueryAndNamedParam("anotherUserScheduleWithTheSameName",
                    new String[] {
                        PARAM_USER_ID, PARAM_NAME
                    }, new Object[] {
                        schedule.getUser().getId(), schedule.getName()
                    },
                    Schedule.class);
        } else if (schedule instanceof UserGroupSchedule) {
            count = super.findByNamedQueryAndNamedParam("anotherUserGroupScheduleWithTheSameName",
                    new String[] {
                        PARAM_USER_GROUP_ID, PARAM_NAME
                    }, new Object[] {
                        schedule.getUserGroup().getId(), schedule.getName()
                    },
                    Schedule.class);
        } else if (schedule instanceof GeneralSchedule) {
            count = super.findByNamedQueryAndNamedParam("anotherGeneralScheduleWithTheSameName",
                    PARAM_NAME, 
                    schedule.getName(),
                    Schedule.class);
        } else if (schedule instanceof FeatureSchedule) {
            count = super.findByNamedQueryAndNamedParam("anotherFeatureScheduleWithTheSameName",
                PARAM_NAME, 
                schedule.getName(),
                Schedule.class);
        }

        return DataAccessUtils.intResult(count) > 0;
    }

    private boolean isNameChanged(Schedule schedule) {
        List<Object> count = (List<Object>)super.findByNamedQueryAndNamedParam(
            "countScheduleWithSameName", new String[] {
            PARAM_SCHEDULE_ID, PARAM_NAME
        }, new Object[] {
            schedule.getId(), schedule.getName()
        },
        Object.class);

        return DataAccessUtils.intResult(count) == 0;
    }

    @Override
    public void deleteSchedulesById(Collection<Integer> scheduleIds) {
        Collection<Schedule> schedules = new ArrayList<Schedule>(scheduleIds.size());
        for (Integer id : scheduleIds) {
            Schedule schedule = getScheduleById(id);
            schedules.add(schedule);
            getDaoEventPublisher().publishDelete(schedule);
        }
        super.removeAllEntities(schedules);
    }

    @Override
    public List<UserGroupSchedule> getAllUserGroupSchedules() {
        return super.loadAllEntities(UserGroupSchedule.class);
    }

    @Override
    public List<Schedule> getAllAvailableSchedulesForUser(User user) {
        List<Schedule> schedulesForUser = new ArrayList<Schedule>();
        schedulesForUser.addAll(getPersonalSchedulesForUserId(user.getId()));
        for (Group group : user.getGroups()) {
            schedulesForUser.addAll(getSchedulesForUserGroupId(group.getId()));
        }

        return schedulesForUser;
    }

    @Override
    public List<UserGroupSchedule> getSchedulesForUserGroupId(Integer userGroupId) {

        return (List<UserGroupSchedule>)super.findByNamedQueryAndNamedParam("userSchedulesForUserGroupId", 
            PARAM_USER_GROUP_ID,
            userGroupId,
            UserGroupSchedule.class);
    }

    @Override
    public List<GeneralSchedule> getAllGeneralSchedules() {
        return super.loadAllEntities(GeneralSchedule.class);
    }

    @Override
    public List<FeatureSchedule> getAllFeatureSchedules() {
        return super.loadAllEntities(FeatureSchedule.class);
    }

    @Override
    public List<FeatureSchedule> getSchedulesForFeatureId(String featureId) {
        return (List<FeatureSchedule>)super.findByNamedQueryAndNamedParam("schedulesForFeatureId", 
            PARAM_FEATURE_ID,
            featureId,
            FeatureSchedule.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof DSTChangeEvent) {
            LOG.info("DST change event caught. Triggering alias generation.");
            m_sipxReplicationContext.generateAll(DataSet.ALIAS);
            m_configManager.configureEverywhere(DialPlanContext.FEATURE);
        }
    }

    /**
     * Only used from WEB UI test code
     */
    @Override
    public void clear() {
        Collection<CallSequence> sequences = loadAllCallSequences();
        for (CallSequence sequence : sequences) {
            sequence.clear();
            saveCallSequence(sequence);
        }
    }

    @Override
    public void clearSchedules() {
        Collection<Schedule> schedules = super.loadAllEntities(Schedule.class);
        super.removeAllEntities(schedules);
    }

    @Override
    public boolean isCallSequenceReplicable(User user) {
        int any = m_jdbcTemplate.queryForObject("select count(*) from ring where user_id = ?", Integer.class, user.getId());
        return any > 0;
    }

    public void setConfigJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }

    public void setSipxReplicationContext(SipxReplicationContext sipxReplicationContext) {
        m_sipxReplicationContext = sipxReplicationContext;
    }

    
    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    @Override
    public void onDelete(Object entity) {
        if (entity instanceof Schedule) {
            Schedule schedule = (Schedule) entity;
            if (schedule instanceof GeneralSchedule) {
                // get all dialing rules and set schedule to Always
                List<DialingRule> rules = getDialingRulesForScheduleId(schedule.getId());
                if (rules != null) {
                    for (DialingRule rule : rules) {
                        rule.setSchedule(null);
                    }
                    for (DialingRule rule : rules) {
                        super.mergeEntity(rule);
                    }
                    for (DialingRule rule : rules) {
                        if (rule instanceof AttendantRule) {
                            AttendantRule aaRule = (AttendantRule) rule;
                            m_sipxReplicationContext.generate(aaRule);
                        } 
                    }
                }
            } else if (schedule instanceof UserSchedule || schedule instanceof UserGroupSchedule) {
                Collection<CallSequence> css = new HashSet<CallSequence>();
                // get all rings and set schedule to Always
                List<Ring> rings = getRingsForScheduleId(schedule.getId());
                if (rings != null) {
                    for (Ring ring : rings) {
                        ring.setSchedule(null);
                        css.add(ring.getCallSequence());
                    }
                    for (Ring ring : rings) {
                        super.mergeEntity(ring);
                    }
                }
                notifyCommserver(css);
            }
        } else if (entity instanceof User) {
            User user = (User) entity;
            removeCallSequenceForUserId(user.getId());
            removeSchedulesForUserID(user.getId());
        }
    }

    @Override
    public void onSave(Object entity) {
        if (entity instanceof CallSequence) {
            CallSequence seq = (CallSequence) entity;
            m_sipxReplicationContext.generate(seq.getUser());
        } else if (entity instanceof Schedule) {
            Schedule schedule = (Schedule) entity;
            if (schedule instanceof GeneralSchedule) {
                List<DialingRule> rules = getDialingRulesForScheduleId(schedule.getId());
                if (rules != null) {
                    for (DialingRule rule : rules) {
                        if (rule instanceof AttendantRule) {
                            AttendantRule aaRule = (AttendantRule) rule;
                            m_sipxReplicationContext.generate(aaRule);
                        } else {
                            getDaoEventPublisher().publishSave(rule);
                        }
                    }
                }
            }
        }
    }
}
