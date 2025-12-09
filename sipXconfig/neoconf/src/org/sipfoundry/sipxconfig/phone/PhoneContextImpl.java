/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.phone;

import static org.apache.commons.collections.CollectionUtils.select;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import org.sipfoundry.commons.util.ShortHash;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.DaoUtils;
import org.sipfoundry.sipxconfig.common.DataCollectionUtil;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.SpecialUser.SpecialUserType;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.device.ProfileManager;
import org.sipfoundry.sipxconfig.intercom.Intercom;
import org.sipfoundry.sipxconfig.intercom.IntercomManager;
import org.sipfoundry.sipxconfig.phonebook.GooglePhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
import org.sipfoundry.sipxconfig.speeddial.SpeedDialManager;

/**
 * Context for entire sipXconfig framework. Holder for service layer bean factories.
 */
public class PhoneContextImpl extends SipxHibernateDaoSupport<Phone> implements BeanFactoryAware, PhoneContext,
        ApplicationListener<ApplicationEvent>, AlarmProvider, DaoEventListener {
    private static final String SERIAL_NUMBER = "serial_number";
    private static final String MODEL_ID = "model_id";
    private static final String PHONE_ID = "phone_id";
    private static final String VALUE_STORAGE_ID = "value_storage_id";
    private static final Log LOG = LogFactory.getLog(PhoneContextImpl.class);
    private static final String QUERY_PHONE_ID_BY_SERIAL_NUMBER = "phoneIdsWithSerialNumber";
    private static final String QUERY_PHONE_BY_SERIAL_NUMBER = "phoneWithSerialNumber";
    private static final String ALARM_PHONE_ADDED = "ALARM_PHONE_ADDED Phone with serial %s was added to the system.";
    private static final String ALARM_PHONE_CHANGED = "ALARM_PHONE_CHANGED Phone with id %d serial %s was changed.";
    private static final String ALARM_PHONE_DELETED = "ALARM_PHONE_DELETED Phone with id %d serial %s was deleted.";

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String VALUE = "value";
    private static final String SQL_SELECT_GROUP = "select p.phone_id,p.serial_number "
            + "from phone p join phone_group pg on pg.phone_id = p.phone_id where pg.group_id=%d";
    private static final String SQL_SELECT_GROUP_RESTRICT_BY_BEAN_ID = "select p.phone_id,p.model_id,p.serial_number, "
            + "p.value_storage_id from phone p join phone_group pg on pg.phone_id = p.phone_id "
            + "where p.bean_id = '%s' and p.model_id = '%s' and pg.group_id=%d and p.device_version_id != '%s'";
    private static final String SQL_UPDATE = "update phone set device_version_id='%s' where phone_id=%d";
    private static final String SQL_PHONE_GROUP_WEIGHT = "SELECT MAX(gs.weight) FROM phone_group ph "
        + "JOIN group_storage gs ON ph.group_id = gs.group_id WHERE ph.phone_id=%d";
    private static final String SQL_GROUP_WEIGHT = "SELECT gs.weight FROM group_storage gs WHERE gs.group_id = %d";
    private static final String SLASH = "/";
    private static final String LINES = "lines";
    private static final String PERCENT = "%";
    private static final String FIELD_USER = "user";

    private CoreContext m_coreContext;

    private SettingDao m_settingDao;

    private BeanFactory m_beanFactory;

    private String m_systemDirectory;

    private DeviceDefaults m_deviceDefaults;

    private IntercomManager m_intercomManager;

    private PhonebookManager m_phonebookManager;

    private SpeedDialManager m_speedDialManager;

    private JdbcTemplate m_jdbcTemplate;

    private ProfileManager m_profileManager;

    
    public void setPhonebookManager(PhonebookManager phonebookManager) {
        m_phonebookManager = phonebookManager;
    }

    
    public void setSettingDao(SettingDao settingDao) {
        m_settingDao = settingDao;
    }

    
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    
    public void setIntercomManager(IntercomManager intercomManager) {
        m_intercomManager = intercomManager;
    }

    
    public void setSpeedDialManager(SpeedDialManager speedDialManager) {
        m_speedDialManager = speedDialManager;
    }

    public void setConfigJdbcTemplate(JdbcTemplate template) {
        m_jdbcTemplate = template;
    }

    
    public void setProfileManager(ProfileManager profileManager) {
        m_profileManager = profileManager;
    }

    /**
     * Callback that supplies the owning factory to a bean instance.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }


    @Override
    public void storePhone(Phone phone) {

        boolean isNew;
        String serialNumber = phone.getSerialNumber();
        if (!phone.getModel().isSerialNumberValid(serialNumber)) {
            throw new InvalidSerialNumberException(serialNumber, phone.getModel().getSerialNumberPattern());
        }

        super.getSessionFactory().inTransaction( session -> {

            DaoUtils.checkDuplicatesByNamedQuery(session, phone, QUERY_PHONE_ID_BY_SERIAL_NUMBER, serialNumber,
                    new DuplicateSerialNumberException(serialNumber));
        });

        phone.setValueStorage(clearUnsavedValueStorage(phone.getValueStorage()));
        isNew = phone.isNew();
        if (isNew) {
            LOG.error(String.format(ALARM_PHONE_ADDED, phone.getSerialNumber()));
        } 
        super.saveEntity(phone);
        super.flush();
        getDaoEventPublisher().publishSave(phone); 
    }

    @Override
    public void deletePhone(Phone phone) {
        ProfileLocation location = phone.getModel().getDefaultProfileLocation();
        phone.removeProfiles(location);
        phone.setValueStorage(clearUnsavedValueStorage(phone.getValueStorage()));
        for (Line line : phone.getLines()) {
            line.setValueStorage(clearUnsavedValueStorage(line.getValueStorage()));
        }
        super.removeEntity(phone);
        LOG.error(String.format(ALARM_PHONE_DELETED, phone.getId(), phone.getSerialNumber()));
    }

    @Override
    public void storeLine(Line line) {
        line.setValueStorage(clearUnsavedValueStorage(line.getValueStorage()));
        
        super.saveEntity(line);

        getDaoEventPublisher().publishSave(line);
    }

    @Override
    public void deleteLine(Line line) {
        line.setValueStorage(clearUnsavedValueStorage(line.getValueStorage()));
        super.removeEntity(line);
    }

    @Override
    public Line loadLine(Integer id) {
        Line line = super.loadEntity(Line.class, id);
        return line;
    }

    @Override
    public int getPhonesCount() {
        return getPhonesInGroupCount(null);
    }

    @Override
    public int getPhonesInGroupCount(Integer groupId) {
        return getBeansInGroupCount(Phone.class, groupId);
    }

    @Override
    public List<Phone> loadPhonesByPage(Integer groupId, int firstRow, int pageSize, String[] orderBy,
            boolean orderAscending) {
        return loadBeansByPage(Phone.class, groupId, firstRow, pageSize, orderBy, orderAscending);
    }

    @Override
    public List<Phone> loadPhonesByPage(int firstRow, int pageSize) {
        return loadBeansByPage(Phone.class, firstRow, pageSize);
    }

    @Override
    public List<Phone> loadPhones() {
        return super.loadAllEntities(Phone.class);
    }

    @Override
    public List<Integer> getAllPhoneIds() {
        return (List<Integer>)super.findByNamedQuery("phoneIds", Integer.class);
    }

    @Override
    public Phone loadPhone(Integer id) {
        Phone phone = super.loadEntity(Phone.class, id);
        return phone;
    }

    @Override
    public Phone load(Class c, Object id) {
        return loadPhone( (Integer)id );
    }

    @Override
    public Integer getPhoneIdBySerialNumber(String serialNumber) {
        List<Integer> objs = (List<Integer>)super.findByNamedQueryAndNamedParam(
            QUERY_PHONE_ID_BY_SERIAL_NUMBER, 
            VALUE,
            serialNumber,
            Integer.class );
        return (Integer) DaoUtils.requireOneOrZero(objs, QUERY_PHONE_ID_BY_SERIAL_NUMBER);
    }

    @Override
    public Phone getPhoneBySerialNumber(String serialNumber) {
        List<Phone> objs = (List<Phone>)super.findByNamedQueryAndNamedParam(
            QUERY_PHONE_BY_SERIAL_NUMBER, 
            VALUE,
            serialNumber,
            Phone.class);
        return DaoUtils.requireOneOrZero(objs, QUERY_PHONE_BY_SERIAL_NUMBER);
    }

    @Override
    public Phone newPhone(PhoneModel model) {
        Phone phone = (Phone) m_beanFactory.getBean(model.getBeanId());
        phone.setModel(model);
        return phone;
    }

    @Override
    public List<Group> getGroups() {
        return m_settingDao.getGroups(GROUP_RESOURCE_ID);
    }

    @Override
    public void storeGroup(Group group) {
        m_settingDao.saveGroup(group);
    }

    @Override
    public boolean deleteGroups(Collection<Integer> groupIds) {
        return m_settingDao.deleteGroups(groupIds);
    }

    @Override
    public Group getGroupByName(String phoneGroupName, boolean createIfNotFound) {
        if (createIfNotFound) {
            return m_settingDao.getGroupCreateIfNotFound(GROUP_RESOURCE_ID, phoneGroupName);
        }
        return m_settingDao.getGroupByName(GROUP_RESOURCE_ID, phoneGroupName);
    }

    /** unittesting only */
    @Override
    public void clear() {
        // ordered bottom-up, e.g. traverse foreign keys so as to
        // not leave hanging references. DB will reject otherwise
        deleteAll("from Phone");
        deleteAll("from Group where resource = 'phone'");
    }

    private void deleteAll(String query) {
        Collection<Object> c = (Collection<Object>)super.find(query, Object.class);
        super.removeAllEntities(c);
    }

    @Override
    public String getSystemDirectory() {
        return m_systemDirectory;
    }

    public void setSystemDirectory(String systemDirectory) {
        m_systemDirectory = systemDirectory;
    }

    private static class DuplicateSerialNumberException extends UserException {
        public DuplicateSerialNumberException(String serialNumber) {
            super("&error.duplicateSerialNumberException", serialNumber);
        }
    }

    private static class InvalidSerialNumberException extends UserException {
        private static final String ERROR = "&error.invalidSerialNumberException";

        public InvalidSerialNumberException(String serialNumber, String pattern) {
            super(ERROR, serialNumber, pattern);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event_) {
        // no init tasks defined yet
    }

    @Override
    public DeviceDefaults getPhoneDefaults() {
        return m_deviceDefaults;
    }

    public void setPhoneDefaults(DeviceDefaults deviceDefaults) {
        m_deviceDefaults = deviceDefaults;
    }

    @Override
    public Collection<Phone> getPhonesByGroupId(Integer groupId) {
        Collection<Phone> phones = (Collection<Phone>)super.findByNamedQueryAndNamedParam(
            "phonesByGroupId",
            "groupId", 
            groupId,
            Phone.class);
        return phones;
    }

    @Override
    public Collection<Phone> getPhonesByGroupName(String groupName) {
        Collection<Phone> phones = (Collection<Phone>)super.findByNamedQueryAndNamedParam(
            "phonesByGroupName",
            "groupName", 
            groupName,
            Phone.class);
        return phones;
    }

    @Override
    public void onDelete(Object entity) {
        Class c = entity.getClass();
        if (User.class.equals(c)) {
            User user = (User) entity;
            Collection<Phone> phones = getPhonesByUserId(user.getId());
            List<Integer> phoneIds = new ArrayList<Integer>();
            for (Phone phone : phones) {
                phoneIds.add(phone.getId());
                List<Integer> ids = new ArrayList<Integer>();
                Collection<Line> lines = phone.getLines();
                for (Line line : lines) {
                    User lineUser = line.getUser();
                    if (lineUser != null && lineUser.getId().equals(user.getId())) {
                        ids.add(line.getId());
                    }
                }
                DataCollectionUtil.removeByPrimaryKey(lines, ids.toArray());
                storePhone(phone);
            }
            m_profileManager.generateProfiles(phoneIds, true, null);
        }
    }

    private void raiseGroupAlarm(int groupId) {
        m_jdbcTemplate.query(String.format(SQL_SELECT_GROUP, groupId), new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                LOG.error(String.format(ALARM_PHONE_CHANGED, rs.getInt(PHONE_ID), rs.getString(SERIAL_NUMBER)));
            }
        });
    }

    @Override
    public void onSave(Object entity) {
        if (entity instanceof Phone) {
            Phone phone = (Phone) entity;
            LOG.error(String.format(ALARM_PHONE_CHANGED, phone.getId(), phone.getSerialNumber()));
        } else if (entity instanceof Group) {
            Group g = (Group) entity;
            if (g.getResource().equals(Phone.GROUP_RESOURCE_ID)) {
                raiseGroupAlarm(g.getId());
            }
        }
    }

    @Override
    public void applyGroupFirmwareVersion(Group group, DeviceVersion v, String modelId) {
        LOG.debug(String.format("Attempting to apply firmware version %s to group %s... ", v.getVersionId(),
                group.getName()));

        group.setSettingValue(Phone.GROUP_VERSION_FIRMWARE_VERSION + SLASH + modelId, v.toString());
        storeGroup(group);

        String versionId = v.toString();
        final List<Integer> ids = new LinkedList<Integer>();
        final List<String> models = new LinkedList<String>();
        final List<String> serials = new LinkedList<String>();
        final List<Integer> storageIds = new LinkedList<Integer>();
        m_jdbcTemplate.query(
                String.format(SQL_SELECT_GROUP_RESTRICT_BY_BEAN_ID, v.getVendorId(), modelId, group.getId(), versionId),
                new RowCallbackHandler() {

                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        ids.add(rs.getInt(PHONE_ID));
                        models.add(rs.getString(MODEL_ID));
                        serials.add(rs.getString(SERIAL_NUMBER));
                        storageIds.add(rs.getInt(VALUE_STORAGE_ID));
                    }
                });
        List<String> updates = new ArrayList<String>();
        for (int i = 0; i < ids.size(); i++) {
            PhoneModel model = m_beanFactory.getBean(models.get(i), PhoneModel.class);
            int id = ids.get(i);
            String serial = serials.get(i);
            Integer valueStorageId = storageIds.get(i);
            if (ArrayUtils.contains(model.getVersions(), v)) {
                LOG.info("Updating " + serial + " to " + versionId);
                if (valueStorageId != null && getPhoneGroupWeight(id) == getGroupWeight(group.getId())) {
                    updates.add(String.format(SQL_UPDATE, versionId, id));
                }
            } else {
                LOG.debug("Skipping " + serial + " as it doesn't support " + versionId);
            }
        }
        if (updates.size() > 0) {
            m_jdbcTemplate.batchUpdate(updates.toArray(new String[] {}));
        }
    }

    @Override
    public Collection<Phone> getPhonesByUserId(Integer userId) {
        return (Collection<Phone>)super.findByNamedQueryAndNamedParam(
            "phonesByUserId", 
            USER_ID, 
            userId,
            Phone.class);
    }

    @Override
    public Collection<Phone> getPhonesByUserName(String userName) {
        return (Collection<Phone>)super.findByNamedQueryAndNamedParam(
            "phonesByUserName", 
            USER_NAME, 
            userName,
            Phone.class);
    }

    @Override
    public Collection<Phone> getPhonesByUserIdAndPhoneModel(Integer userId, String modelId) {
        String[] paramsNames = {
            USER_ID, "modelId"
        };
        Object[] paramsValues = {
            userId, modelId
        };
        Collection<Phone> phones = (Collection<Phone>)super.findByNamedQueryAndNamedParam(
                "phonesByUserIdAndPhoneModel", 
                paramsNames, 
                paramsValues,
                Phone.class);
        return phones;
    }

    @Override
    public void addToGroup(Integer groupId, Collection<Integer> ids) {

        super.getSessionFactory().inTransaction( session -> {

            DaoUtils.addToGroup(session, getDaoEventPublisher(), groupId, Phone.class, ids);
        });
    }

    @Override
    public void removeFromGroup(Integer groupId, Collection<Integer> ids) {

        super.getSessionFactory().inTransaction( session -> {

            DaoUtils.removeFromGroup(session, getDaoEventPublisher(), groupId, Phone.class, ids);
        });
    }

    @Override
    public void addUsersToPhone(Integer phoneId, Collection<Integer> ids) {
        Phone phone = loadPhone(phoneId);
        for (Integer userId : ids) {
            User user = m_coreContext.loadUser(userId);
            Line line = phone.createLine();
            line.setUser(user);
            phone.addLine(line);
        }
        storePhone(phone);
    }

    @Override
    public Intercom getIntercomForPhone(Phone phone) {
        return m_intercomManager.getIntercomForPhone(phone);
    }

    @Override
    public Collection<PhonebookEntry> getPhonebookEntries(Phone phone) {
        User user = phone.getPrimaryUser();
        if (user != null) {
            Collection<Phonebook> books = filterPhonebooks(m_phonebookManager.getAllPhonebooksByUser(user));
            return filterPhonebookEntries(m_phonebookManager.getEntries(books, user));
        }
        return Collections.emptyList();
    }

    private Collection<PhonebookEntry> filterPhonebookEntries(Collection<PhonebookEntry> entries) {
        entries.removeIf(new InvalidGoogleEntrySearchPredicate());
        return entries;
    }

    private Collection<Phonebook> filterPhonebooks(Collection<Phonebook> books) {
        Collection<Phonebook> filteredPhonebooks = new ArrayList<Phonebook>();
        for (Phonebook book : books) {
            if (book.getShowOnPhone()) {
                filteredPhonebooks.add(book);
            }
        }
        return filteredPhonebooks;
    }

    @Override
    public SpeedDial getSpeedDial(Phone phone) {
        User user = phone.getPrimaryUser();
        if (user != null && !user.isNew()) {
            return m_speedDialManager.getSpeedDialForUserId(user.getId(), false);
        }
        return null;
    }

    @Override
    public Collection<Integer> getPhoneIdsByUserGroupId(int groupId) {
        final List<Integer> userIds = new LinkedList<Integer>();
        m_jdbcTemplate.query("SELECT u.user_id from users u inner join user_group g "
                + "on u.user_id = g.user_id WHERE group_id=" + groupId + " AND u.user_type='C' "
                + "ORDER BY u.user_id;", new RowCallbackHandler() {
                    //keep this indentation, otherwise checkstyle will fail
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        userIds.add(rs.getInt("user_id"));
                    }
                });
        Collection<Integer> ids = new ArrayList<Integer>();

        for (Integer userId : userIds) {
            Collection<Phone> phones = getPhonesByUserId(userId);
            ids.addAll(DataCollectionUtil.extractPrimaryKeys(phones));
        }
        return ids;
    }

    @Override
    public User createSpecialPhoneProvisionUser(String serialNumber) {
        User user = m_coreContext.getSpecialUser(SpecialUserType.PHONE_PROVISION);
        user.setFirstName("ID:");
        user.setLastName(ShortHash.get(serialNumber));

        return user;
    }

    @Override
    public List<Phone> loadPhonesWithNoLinesByPage(int firstRow, int pageSize, String[] orderBy, boolean orderAscending) {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
            Root<Phone> root = cq.from(Phone.class);

            // addByNoLinesCriteria equivalent:
            // lines collection is empty
            // JPA does not have direct isEmpty on collection join, so we do a subquery NOT EXISTS
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<Phone> subRoot = subquery.from(Phone.class);
            Join<Object, Object> linesJoin = subRoot.join(LINES, JoinType.LEFT);
            subquery.select(cb.count(linesJoin));
            subquery.where(cb.equal(subRoot, root));

            Predicate noLinesPredicate = cb.equal(subquery, 0L);
            cq.where(noLinesPredicate);

            // ordering
            if (orderBy != null && orderBy.length > 0) {
                List<Order> orders = new ArrayList<>();
                for (String prop : orderBy) {
                    orders.add(orderAscending ? cb.asc(root.get(prop)) : cb.desc(root.get(prop)));
                }
                cq.orderBy(orders);
            }

            Query<Phone> query = session.createQuery(cq);
            query.setFirstResult(firstRow);
            query.setMaxResults(pageSize);

            return query.getResultList();
        });
    }

    public int getPhonesWithNoLinesCount() {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Phone> root = cq.from(Phone.class);

            // no lines subquery same as above
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<Phone> subRoot = subquery.from(Phone.class);
            Join<Object, Object> linesJoin = subRoot.join(LINES, JoinType.LEFT);
            subquery.select(cb.count(linesJoin));
            subquery.where(cb.equal(subRoot, root));

            Predicate noLinesPredicate = cb.equal(subquery, 0L);

            cq.select(cb.count(root)).where(noLinesPredicate);

            Long count = session.createQuery(cq).getSingleResult();
            return count;
        }).intValue();
    }

    public List<Phone> getPhonesWithLinesLike(String value) {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();

            // First query: internal lines with user.userName like %value%
            CriteriaQuery<Phone> cqInternal = cb.createQuery(Phone.class);
            Root<Phone> rootInternal = cqInternal.from(Phone.class);

            Join<Object, Object> linesJoinInternal = rootInternal.join(LINES);
            Join<Object, Object> userJoin = linesJoinInternal.join(FIELD_USER);

            String pattern = "%" + value + "%";
            Predicate userNameLike = cb.like(userJoin.get("userName"), pattern);

            cqInternal.select(rootInternal).distinct(true).where(userNameLike);
            List<Phone> internalPhones = session.createQuery(cqInternal).getResultList();

            // Second query: phones with external lines (lines.user is null)
            CriteriaQuery<Phone> cqExternal = cb.createQuery(Phone.class);
            Root<Phone> rootExternal = cqExternal.from(Phone.class);
            Join<Object, Object> linesJoinExternal = rootExternal.join(LINES);

            Predicate userIsNull = cb.isNull(linesJoinExternal.get(FIELD_USER));
            cqExternal.select(rootExternal).distinct(true).where(userIsNull);

            List<Phone> externalPhones = session.createQuery(cqExternal).getResultList();

            // Filter externalPhones to keep only those with any line's displayLabel containing value
            List<Phone> filteredExternalPhones = new ArrayList<>();
            for (Phone phone : externalPhones) {
                boolean matches = phone.getLines().stream()
                    .anyMatch(line -> line.getDisplayLabel() != null && line.getDisplayLabel().contains(value));
                if (matches) {
                    filteredExternalPhones.add(phone);
                }
            }

            // Combine and return
            internalPhones.addAll(filteredExternalPhones);
            return internalPhones;
        });

    }

    public static Predicate addByExternalLinesPredicate(Root<Phone> root, CriteriaBuilder cb) {
        // Join the lines collection on Phone
        Join<Phone, Line> linesJoin = root.join(LINES);
        // Return the predicate lines.user IS NULL
        return cb.isNull(linesJoin.get(FIELD_USER));
    }

    static class InvalidGoogleEntrySearchPredicate implements java.util.function.Predicate<PhonebookEntry> {
        @Override
        public boolean test(PhonebookEntry entry) {
            if (entry instanceof GooglePhonebookEntry) {
                GooglePhonebookEntry gEntry = (GooglePhonebookEntry) entry;
                return (isEmpty(gEntry.getFirstName()) && isEmpty(gEntry.getLastName()))
                    || isEmpty(gEntry.getNumber());
            }
            return false;
        }
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        Collection<AlarmDefinition> alarms = Arrays.asList(new AlarmDefinition[] {
            PhoneContext.ALARM_PHONE_ADDED, PhoneContext.ALARM_PHONE_DELETED, PhoneContext.ALARM_PHONE_CHANGED
        });
        return alarms;
    }

    @Override
    public String getGroupFirmwareVersion(Phone phone, Group group) {
        //if null, device version is not applicable for phone
        if (group == null) {
            return phone.getDeviceVersion().toString();
        } else {
            return group.getSettingValue(Phone.GROUP_VERSION_FIRMWARE_VERSION + SLASH + phone.getModelId());
        }
    }

    private int getPhoneGroupWeight(int phoneId) {

        return super.getSessionFactory().fromTransaction( session -> {

            org.hibernate.query.NativeQuery<?> q = session.createNativeQuery(SQL_PHONE_GROUP_WEIGHT);
            q.setParameter("phoneId", phoneId);
            Number result = (Number) q.uniqueResult();
            return result != null ? result : 0;
        }).intValue();
    }

    private int getGroupWeight(int groupId) {

        return super.getSessionFactory().fromTransaction( session -> {

            org.hibernate.query.NativeQuery<?> q = session.createNativeQuery(SQL_GROUP_WEIGHT);
            q.setParameter("groupId", groupId);
            Number result = (Number) q.uniqueResult();
            return result != null ? result : 0;
        }).intValue();
    }
}
