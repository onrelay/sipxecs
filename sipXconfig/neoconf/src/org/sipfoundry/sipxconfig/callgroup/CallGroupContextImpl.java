/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.callgroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.hibernate.query.NativeQuery;
import org.hibernate.Session;


import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.DidInUseException;
import org.sipfoundry.sipxconfig.common.ExtensionInUseException;
import org.sipfoundry.sipxconfig.common.NameInUseException;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipxCollectionUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.forwarding.CallSequence;

/**
 * Hibernate implementation of the call group context
 */
public class CallGroupContextImpl extends SipxHibernateDaoSupport<CallGroup> implements CallGroupContext, DaoEventListener {
    private static final String VALUE = "value";

    private static final String QUERY_CALL_GROUP_IDS_WITH_NAME = "callGroupIdsWithName";
    private static final String QUERY_CALL_GROUP_IDS_WITH_ALIAS = "callGroupIdsWithAlias";
    private static final String SQL_CALL_GROUP_EXTENSION = "SELECT call_group_id from call_group where extension = :extension";

    private CoreContext m_coreContext;
    private SipxReplicationContext m_replicationContext;
    private AliasManager m_aliasManager;

    // trivial setters
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setReplicationContext(SipxReplicationContext replicationContext) {
        m_replicationContext = replicationContext;
    }

    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    @Override
    public CallGroup loadCallGroup(Integer id) {
        return super.loadEntity(CallGroup.class, id);
    }
    
    @Override
    public int getCallGroupId(String extension) {

        try( SessionTransaction sessionTransaction = super.getSessionTransaction() ) {

            Session session = sessionTransaction.getSession();

            NativeQuery<?> q = session.createNativeQuery(SQL_CALL_GROUP_EXTENSION);
            q.setParameter("extension", extension);
            Number result = (Number) q.uniqueResult();
            return result != null ? result.intValue() : 0; // handle null safely
        }
    }
    
    @Override
    public Integer getCallGroupIdByAlias(String alias) {
        List<Integer> ids = super.findByNamedQueryAndNamedParam(
                QUERY_CALL_GROUP_IDS_WITH_ALIAS, VALUE, alias, Integer.class);
        return ids.size() > 0 ? (Integer)ids.get(0) : null;
    }

    @Override
    public void saveCallGroup(CallGroup callGroup) {
        // Check for duplicate names or extensions before saving the call group
        String name = callGroup.getName();
        String extension = callGroup.getExtension();
        String did = callGroup.getDid();
        final String huntGroupTypeName = "&label.huntGroup";
        if (!m_aliasManager.canObjectUseAlias(callGroup, name)) {
            throw new NameInUseException(huntGroupTypeName, name);
        }
        if (!m_aliasManager.canObjectUseAlias(callGroup, extension)) {
            throw new ExtensionInUseException(huntGroupTypeName, extension);
        }
        if (!m_aliasManager.canObjectUseAlias(callGroup, did)) {
            throw new ExtensionInUseException(huntGroupTypeName, did);
        }
        if (StringUtils.isNotBlank(did) && did.equals(extension)) {
            throw new DidInUseException(huntGroupTypeName, did);
        }
        super.saveEntity(callGroup);
        // activate call groups every time the call group is saved
        m_replicationContext.generate(callGroup);
    }

    @Override
    public void removeCallGroups(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return;
        }
        Collection<CallGroup> cgs = new ArrayList<CallGroup>();
        for (Integer id : ids) {
            CallGroup cg = (CallGroup) load(CallGroup.class, id);
            cgs.add(cg);
        }
        super.removeAll(CallGroup.class, ids);

        // activate call groups every time the call group is removed
        deactivateCallGroups(cgs);
    }

    public void removeCallGroupByAlias(String alias) {
        List<Integer> ids = super.findByNamedQueryAndNamedParam(
                QUERY_CALL_GROUP_IDS_WITH_ALIAS, VALUE, alias, Integer.class );
        removeCallGroups(ids);
    }

    @Override
    public void onDelete(Object entity) {
        if (entity instanceof User) {
            User user = (User) entity;
            super.mergeEntity(user);
            removeUser(user.getId());
        }
    }

    @Override
    public void onSave(Object entity) {
        if (entity instanceof CallSequence) {
            CallSequence seq = (CallSequence) entity;
            User user = seq.getUser();
            if (user != null) {
                updateUser(user.getId(), false);
            }
        }
    }

    /**
     * Removes all rings associated with a removed user. This is temporary. In the long term we
     * will introduce hibernate dependency between the users and rings table.
     *
     * Note: we cannot just blindly removed rings from database, there is a cascade relationship
     * between call groups and rings, hibernate will resave the ring if it's removed from database
     * but not from the call group
     *
     * This function is called from legacy notification service, there is no need to activate call
     * groups - they will be activated anyway because alias generation follows user deletion.
     *
     * @param userId id of the user that us being deleted
     *
     */
    @Override
    public void removeUser(Integer userId) {
        updateUser(userId, true);
    }

    private void updateUser(Integer userId, boolean delete) {
        Collection<UserRing> rings = 
            super.findByNamedQueryAndNamedParam("userRingsForUserId", "userId", userId, UserRing.class );
        for (Iterator i = rings.iterator(); i.hasNext();) {
            UserRing ring = (UserRing) i.next();
            CallGroup callGroup = ring.getCallGroup();
            if (delete) {
                callGroup.removeRing(ring);
            }
            super.saveEntity(callGroup);
            super.flush();
            m_replicationContext.generate(callGroup);
        }
    }

    @Override
    public List<CallGroup> getCallGroups() {
        return super.loadAllEntities(CallGroup.class);
    }

    @Override
    public void duplicateCallGroups(Collection ids) {
        for (Iterator i = ids.iterator(); i.hasNext();) {
            CallGroup group = loadCallGroup((Integer) i.next());

            // Create a copy of the call group with a unique name
            CallGroup groupDup = (CallGroup) duplicateBean(group, QUERY_CALL_GROUP_IDS_WITH_NAME);

            // Extensions should be unique, so don't copy the extension from the
            // source call group. The admin must fill it in explicitly.
            groupDup.setExtension(null);
            groupDup.setDid(null);

            groupDup.setEnabled(false);
            saveCallGroup(groupDup);
            getDaoEventPublisher().publishSave(groupDup);
        }
    }
    
    @Override
    public void duplicateCallGroup(Integer id, String extension) {
        CallGroup group = loadCallGroup(id);

        // Create a copy of the call group with a unique name
        CallGroup groupDup = (CallGroup) duplicateBean(group, QUERY_CALL_GROUP_IDS_WITH_NAME);
        
        groupDup.setExtension(extension);
        groupDup.setDid(null);

        groupDup.setEnabled(false);
        saveCallGroup(groupDup);
        getDaoEventPublisher().publishSave(groupDup);        
    }

    /**
     * Remove all call groups - mostly used for testing
     */
    @Override
    public void clear() {
        Collection<CallGroup> callGroups = super.loadAllEntities(CallGroup.class);
        getDaoEventPublisher().publishDelete(callGroups);
        super.removeAllEntities(callGroups);
        for (CallGroup cg : callGroups) {
            m_replicationContext.generate(cg);
        }
    }

    /**
     * Sends notification to profile generator to trigger alias generation
     */
    public void deactivateCallGroups(Collection<CallGroup> cgs) {
        for (CallGroup cg : cgs) {
            m_replicationContext.remove(cg);
        }
    }

    @Override
    public boolean isAliasInUse(String alias) {
        // Look for the ID of a call group with the specified alias as its name or extension.
        // If there is one, then the alias is in use.
        List<Integer> objs = super.findByNamedQueryAndNamedParam(
                QUERY_CALL_GROUP_IDS_WITH_ALIAS, 
                VALUE,
                alias,
                Integer.class);
        return SipxCollectionUtils.safeSize(objs) > 0;
    }

    @Override
    public Collection getBeanIdsOfObjectsWithAlias(String alias) {
        List<Integer> ids = super.findByNamedQueryAndNamedParam(
                QUERY_CALL_GROUP_IDS_WITH_ALIAS, 
                VALUE,
                alias,
                Integer.class);
        Collection bids = BeanId.createBeanIdCollection(ids, CallGroup.class);
        return bids;
    }

    @Override
    public void addUsersToCallGroup(Integer callGroupId, Collection ids) {
        CallGroup callGroup = loadCallGroup(callGroupId);
        for (Iterator i = ids.iterator(); i.hasNext();) {
            Integer userId = (Integer) i.next();
            User user = m_coreContext.loadUser(userId);
            callGroup.insertRingForUser(user);
        }
        saveCallGroup(callGroup);
    }

    @Override
    public void generateSipPasswords() {
        List<CallGroup> callGroups = getCallGroups();
        List<CallGroup> changed = new ArrayList<CallGroup>();
        for (CallGroup callGroup : callGroups) {
            if (callGroup.generateSipPassword()) {
                changed.add(callGroup);
            }
        }

        for (CallGroup callGroup : changed) {
            // no need to trigger replication - do not use storeCallGroup
            super.mergeEntity(callGroup);
        }
    }

    @Override
    public List<Replicable> getReplicables() {
        List<Replicable> replicables = new ArrayList<Replicable>();
        for (CallGroup cg : getCallGroups()) {
            replicables.add(cg);
        }
        return replicables;
    }
}
