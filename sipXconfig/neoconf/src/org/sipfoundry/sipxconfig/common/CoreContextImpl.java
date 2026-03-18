/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.common;

import static org.sipfoundry.commons.userdb.profile.UserProfileService.DISABLED;
import static org.sipfoundry.commons.userdb.profile.UserProfileService.ENABLED;
import static org.sipfoundry.commons.userdb.profile.UserProfileService.LDAP;
import static org.sipfoundry.commons.userdb.profile.UserProfileService.PHANTOM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.sipfoundry.commons.diddb.Did;
import org.sipfoundry.commons.diddb.DidService;
import org.sipfoundry.commons.userdb.profile.Address;
import org.sipfoundry.commons.userdb.profile.UserProfile;
import org.sipfoundry.commons.userdb.profile.UserProfileService;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.SpecialUser.SpecialUserType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.im.ImAccount;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;


public abstract class CoreContextImpl extends SipxHibernateDaoSupport<User> implements CoreContext, SetupListener {

    public static final String ADMIN_GROUP_NAME = "administrators";
    public static final String CONTEXT_BEAN_NAME = "coreContextImpl";
    private static final int SIP_PASSWORD_LEN = 12;
    private static final String USERNAME_PROP_NAME = "userName";
    private static final String VALUE = "value";
    /** nothing special about this name */
    private static final String QUERY_USER_BY_NAME_OR_ALIAS = "userByNameOrAlias";
    private static final String QUERY_IP_ADDRESS = "ipAddress";
    private static final String SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS = "select distinct u.user_id from users u "
            + "left outer join user_alias alias  " + "on u.user_id=alias.user_id  "
            + "left outer join value_storage vs on vs.value_storage_id=u.value_storage_id  "
            + "left outer join setting_value sv on sv.value_storage_id=vs.value_storage_id  "
            + "where u.user_name= :alias or alias.alias= :alias  "
            + "or (sv.path='voicemail/fax/did' and sv.value= :alias)  "
            + "or (sv.path='voicemail/fax/extension' and sv.value= :alias) ";
    private static final String SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS_EXCEPT_DID = "select distinct u.user_id from users u "
        + "left outer join user_alias alias  " + "on u.user_id=alias.user_id  "
        + "left outer join value_storage vs on vs.value_storage_id=u.value_storage_id  "
        + "left outer join setting_value sv on sv.value_storage_id=vs.value_storage_id  "
        + "where u.user_name= :alias or alias.alias= :alias  "       
        + "or (sv.path='voicemail/fax/extension' and sv.value= :alias) ";
    private static final String SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS_EXCEPT_THIS =
        "select distinct u.user_id from users "
        + "u left outer join user_alias alias  "
        + "on u.user_id=alias.user_id left "
        + "outer join value_storage vs on vs.value_storage_id=u.value_storage_id left "
        + "outer join setting_value sv on sv.value_storage_id=vs.value_storage_id  "
        + "where (u.user_name= :alias or alias.alias= :alias  "
        + "or (sv.path='voicemail/fax/did' and sv.value = :alias)  "
        + "or (sv.path='voicemail/fax/extension' and sv.value = :alias)) and u.user_name != :username";
    private static final String SQL_QUERY_PHANTOM_USERS =
        "select count(u.user_name) from users "
        + "u inner join setting_value v "
        + "on u.value_storage_id = v.value_storage_id and v.path='phantom/enabled'";
    private static final String SQL_QUERY_PHANTOM_USERS_WITHOUT_SUPERADMIN =
            SQL_QUERY_PHANTOM_USERS + " where u.user_type ='C' AND u.user_name != 'superadmin'";
    private static final String PHANTOM_USERS_QUERY = "select u from User u join u.valueStorage vs where "
        + "vs.databaseValues['phantom/enabled'] != null "
        + "and vs.databaseValues['phantom/enabled'] = '1'";
    private static final String ALIAS = "alias";
    private static final String QUERY_USER = "from AbstractUser";
    private static final String QUERY_PARAM_GROUP_ID = "groupId";
    private static final String QUERY_IM_ID = "imId";
    private static final String QUERY_USER_ID = "userId";
    private static final String SPECIAL_USER_BY_TYPE = "specialUserByType";
    private static final String SPECIAL_USER_TYPE = "specialUserType";
    private static final String USER_ADMIN = "userAdmin";
    private static final String FIRST = "first";
    private static final String PAGE_SIZE = "pageSize";
    private static final String USER_ID = "user_id";
    private static final String BRANCH_ID = "branchId";
    private static final String USERNAME = "username";

    private DomainManager m_domainManager;
    private SettingDao m_settingDao;
    private AliasManager m_aliasManager;
    private JdbcTemplate m_jdbcTemplate;
    private boolean m_debug;
    private boolean m_setup;
    private DidService m_didService;

    /** limit number of users */
    private int m_maxUserCount = -1;

    public CoreContextImpl() {
        super();
    }

    /**
     * Implemented by Spring lookup-method injection
     */
    @Override
    public abstract User newUser();

    /**
     * Implemented by Spring lookup-method injection
     */
    @Override
    public abstract InternalUser newInternalUser();

    public abstract UserProfileService getUserProfileService();

    @Override
    public boolean getDebug() {
        return m_debug;
    }

    public void setDebug(boolean debug) {
        m_debug = debug;
    }

    @Override
    public String getAuthorizationRealm() {
        return m_domainManager.getAuthorizationRealm();
    }

    public void setMaxUserCount(int maxUserCount) {
        m_maxUserCount = maxUserCount;
    }

    @Override
    public String getDomainName() {
        return m_domainManager.getDomain().getName();
    }

    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    @Override
    public void saveUser(User user) {
        DuplicateEntity dup = null;
        try {
            dup = checkForDuplicateNameOrAlias(user);
        } catch (Exception ex) {
            throw new UserException("&err.msg.checkUserIdAliasFailed", ex);
        }
        if (dup != null) {
            throw new NameInUseException(dup);
        }

        checkMaxUsers(user, m_maxUserCount);
        checkBranch(user);
        String origUserName = null;
        if (!user.isNew()) {
            origUserName = (String) getOriginalValue(user, USERNAME_PROP_NAME);
            if (!origUserName.equals(user.getUserName())) {
                if (origUserName.equals(User.SUPERADMIN)) {
                    throw new UserException("&msg.error.renameAdminUser");
                }
                String origPintoken = (String) getOriginalValue(user, "pintoken");
                if (origPintoken.equals(user.getPintoken())) {
                    throw new ChangePintokenRequiredException("&error.changePintokenRequiredException");
                }
            }
        } else {
            user.getUserProfile().setUseBranchAddress(true);
        }

        if (user.getUserProfile().getUseBranchAddress() && user.getSite() != null) {
            Address branch = user.getUserProfile().getBranchAddress();
            if (branch == null) {
                branch = new Address();
                user.getUserProfile().setBranchAddress(branch);
            }
            branch.setCity(user.getSite().getAddress().getCity());
            branch.setCountry(user.getSite().getAddress().getCountry());
            branch.setOfficeDesignation(user.getSite().getAddress().getOfficeDesignation());
            branch.setState(user.getSite().getAddress().getState());
            branch.setStreet(user.getSite().getAddress().getStreet());
            branch.setZip(user.getSite().getAddress().getZip());
            user.getUserProfile().setBranchName(user.getSite().getName());
        }
        if (!user.isNew() && user.getSite() == null) {
            user.getUserProfile().setUseBranchAddress(false);
            user.getUserProfile().setBranchAddress(new Address());
        }

        super.saveEntity( user );
    }

    @Override
    public String getOriginalUserName(User user) {
        return (String) getOriginalValue(user, USERNAME_PROP_NAME);
    }

    /**
     * Check that the system has been restricted to a certain number of users
     *
     * @param maxUserCount -1 or represent infinite number
     */
    public void checkMaxUsers(User user, int maxUserCount) {
        // allow edits to the Nth (or beyond) user
        if (!user.isNew()) {
            return;
        }

        if (maxUserCount < 0) {
            return;
        }

        int count = getUsersCount();
        if (count >= maxUserCount) {
            throw new MaxUsersException(m_maxUserCount);
        }
    }

    static class MaxUsersException extends UserException {
        MaxUsersException(int maxCount) {
            super("You cannot exceed the maximum number of allowed users: " + maxCount);
        }
    }

    public static class ChangePintokenRequiredException extends UserException {
        public ChangePintokenRequiredException(String msg) {
            super(msg);
        }
    }

    @Override
    public void deleteUser(User user) {
        super.removeEntity(user);
    }

    @Override
    public boolean deleteUsers(Collection<Integer> userIds) {
        if (userIds.isEmpty()) {
            // no users to delete => nothing to do
            return false;
        }

        User admin = loadUserByUserName(User.SUPERADMIN);
        boolean affectAdmin = false;
        List<User> users = new ArrayList<User>(userIds.size());
        for (Integer id : userIds) {
            User user = loadUser(id);
            if (user != admin) {
                users.add(user);
            } else {
                affectAdmin = true;
            }
        }
        getDaoEventPublisher().publishDeleteCollection(users);
        super.removeAllEntities(users);
        getDaoEventPublisher().publishAfterDeleteCollection(users);
        return affectAdmin;
    }

    @Override
    public void deleteUsersByUserName(Collection<String> userNames) {
        if (userNames.isEmpty()) {
            // no users to delete => nothing to do
            return;
        }
        List<User> users = new ArrayList<>(userNames.size());
        for (String userName : userNames) {
            User user = loadUserByUserName(userName);
            users.add(user);
        }
        getDaoEventPublisher().publishDeleteCollection(users);
        super.removeAllEntities(users);
    }

    @Override
    public User loadUser(Integer id) {
        return super.load(User.class, id);
    }

    @Override
    public User getUser(Integer id) {
        return super.findEntity(User.class, id);
    }

    @Override
    public User loadUserByUserName(String userName) {
        return loadUserByNamedQueryAndNamedParam("userByUserName", VALUE, userName);
    }

    private User loadUserByUniqueProperty(String propName, String propValue) {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);

            Predicate predicate = cb.equal(root.get(propName), propValue);
            cq.where(predicate);

            List<User> users = session.createQuery(cq).getResultList();

            return DaoUtils.requireOneOrZero(users, predicate.toString());
        });
    }

    @Override
    public User loadUserByAlias(String alias) {
        return loadUserByNamedQueryAndNamedParam("userByAlias", VALUE, alias);
    }

    @Override
    public User loadUserByConfiguredImId(String imId) {
        Integer userId = getUserProfileService().getUserIdByImId(imId);
        if (userId != null) {
            return loadUser(userId);
        }
        return null;
    }

    @Override
    public User loadUserByUserNameOrAlias(String userNameOrAlias) {
        return loadUserByNamedQueryAndNamedParam(QUERY_USER_BY_NAME_OR_ALIAS, VALUE, userNameOrAlias);
    }

    @Override
    public List<User> loadUsersByAuthAccountName(String authAccountName) {
        List<User> users = new ArrayList<User>();
        List<Integer> userIds = getUserProfileService().getUserIdsByAuthAccountName(authAccountName);
        for (Integer userId : userIds) {
            users.add(loadUser(userId));
        }
        return users;
    }

    @Override
    public List<User> loadUsersByEmail(String email) {
        List<User> users = new ArrayList<User>();
        List<Integer> userIds = getUserProfileService().getUserIdsByEmail(email);
        for (Integer userId : userIds) {
            users.add(loadUser(userId));
        }
        return users;
    }

    @Override
    public List<User> loadUsersContainsEmail(String email) {
        List<User> users = new ArrayList<User>();
        List<Integer> userIds = getUserProfileService().getUserIdsContainsEmail(email);
        for (Integer userId : userIds) {
            users.add(loadUser(userId));
        }
        return users;
    }

    @Override
    public List<User> loadUserByAdmin() {
        return (List<User>)super.findByNamedQuery(USER_ADMIN,User.class);
    }

    /**
     * Checks if the inherited branch is the same with the actual branch when they are not null
     *
     * @param user
     */
    private void checkBranch(User user) {
        Branch inheritedBranch = user.getInheritedBranch();
        Branch branch = user.getBranch();
        if (inheritedBranch != null && branch != null
                && !StringUtils.equals(inheritedBranch.getName(), branch.getName())) {
            throw new UserException("&invalid.branch");
        }
    }

    @Override
    public Collection<User> getUsersForBranch(Branch branch) {
        Collection<User> users = 
            (Collection<User>)super.findByNamedQueryAndNamedParam(
                "usersForBranch", 
                "branch",
                branch,
                User.class );
        return users;
    }

    /**
     * Check whether the user has a username or alias or ImId that collides with an existing
     * username or alias. Check for internal collisions as well, for example, the user has an
     * alias that is the same as the username. (Duplication within the aliases is not possible
     * because the aliases are stored as a Set.) If there is a collision, then return the bad name
     * (username or alias). Otherwise return null. If there are multiple collisions, then it's
     * arbitrary which name is returned.
     *
     * @param user user to test
     * @return name that collides
     */
    @Override
    public DuplicateEntity checkForDuplicateNameOrAlias(User user) {
        String result = null;
        DuplicateEntity duplicateEntity = null;

        // Check for duplication within the user itself
        List names = new ArrayList(user.getAliases());
        String userName = user.getUserName();
        names.add(userName);
        String faxExtension = user.getFaxExtension();
        if (!faxExtension.isEmpty()) {
            names.add(faxExtension);
        }
        String faxDid = user.getFaxDid();
        if (!faxDid.isEmpty()) {
            names.add(faxDid);
        }
        String did = user.getDid();
        if (!StringUtils.isEmpty(did)) {
        	names.add(did);
        }
        result = checkForDuplicateString(names);
        duplicateEntity = result != null ? new DuplicateEntity(DuplicateType.USER_INTERNAL, result) : null;
        if (result == null) {
            names.remove(faxDid);
            List<Did> dids = m_didService.getDidsInUse(user.isSaveFaxDid() ? 
                user.getFaxExtension(): user.getExtension(true), names);
            result = dids.isEmpty() ? null : Arrays.toString(dids.toArray());
            duplicateEntity = result != null ? new DuplicateEntity(DuplicateType.USER_DID, result) : null;
        }
        if (result == null) {
            // Check whether the userName is a duplicate.
            if (!m_aliasManager.canObjectUseAlias(user, userName)) {
                result = userName;
                duplicateEntity = result != null ? new DuplicateEntity(DuplicateType.USER_NAME, result) : null;
            } else {
                // Check the aliases and return any duplicate as a bad name.
                for (String alias : user.getAliases()) {
                    if (!m_aliasManager.canObjectUseAlias(user, alias)) {
                        result = alias;
                        duplicateEntity = result != null
                            ? new DuplicateEntity(DuplicateType.USER_ALIAS, result) : null;
                        break;
                    }
                }
                // check if user ImId is unique in alias namespace
                ImAccount imAccount = new ImAccount(user);
                if (!m_aliasManager.canObjectUseAlias(user, imAccount.getImId())) {
                    result = imAccount.getImId();
                    duplicateEntity = result != null ? new DuplicateEntity(DuplicateType.USER_IM, result) : null;
                }

                // check if the user's fax extension and DID areunique in the alias namespace
                if (!faxExtension.isEmpty()) {
                    if (!m_aliasManager.canObjectUseAlias(user, faxExtension)) {
                        result = faxExtension;
                        duplicateEntity = result != null
                            ? new DuplicateEntity(DuplicateType.USER_FAX_EXTENSION, result) : null;
                    }
                }
                if (!faxDid.isEmpty()) {
                    if (!m_aliasManager.canObjectUseAlias(user, faxDid)) {
                        result = faxDid;
                        duplicateEntity = result != null
                            ? new DuplicateEntity(DuplicateType.USER_FAX_DID, result) : null;
                    }
                }
            }
        }

        return duplicateEntity;
    }

    /**
     * Given a collection of strings, look for duplicates. Return the first duplicate found, or
     * null if all strings are unique.
     */
    String checkForDuplicateString(Collection<String> strings) {
        Set<String> set = new TreeSet<String>();
        for (String str : strings) {
            if (!set.add(str)) {
                return str;
            }
        }
        return null;
    }

    private User loadUserByNamedQueryAndNamedParam(String queryName, String paramName, Object value) {
        List<User> usersColl = (List<User>)super.findByNamedQueryAndNamedParam(
                queryName, 
                paramName, 
                value,
                User.class);
        Set<User> users = new HashSet<>(usersColl); // eliminate duplicates
        if (users.size() > 1) {
            throw new IllegalStateException("The database has more than one user matching the query " + queryName
                    + ", paramName = " + paramName + ", value = " + value);
        }
        User user = null;
        if (users.size() > 0) {
            user = (User) users.iterator().next();
        }
        return user;
    }

    /**
     * Return all users matching the userTemplate example. Empty properties of userTemplate are
     * ignored in the search. The userName property matches either the userName or aliases
     * properties.
     */
    @Override
    public List<User> loadUserByTemplateUser(final User userTemplate) {

        return super.getSessionFactory().fromTransaction( session -> {

            UserLoader loader = new UserLoader(session);
            return loader.loadUsers(userTemplate);
        });
    }

    @Override
    public List<User> loadUsers() {
        return super.loadAllEntities(User.class);
    }

    @Override
    public int getUsersCount() {
        return getUsersInGroupCount(null);
    }

    // returns only the number of users created by admin
    @Override
    public int getAllUsersCount() {
        return getBeansInGroupCount(AbstractUser.class, null);
    }

    @Override
    public int getUsersInGroupCount(Integer groupId) {
        return getBeansInGroupCount(User.class, groupId);
    }

    @Override
    public int getUsersInGroupWithSearchCount(final Integer groupId, final String searchString) {
        int numUsers = 0;
        if (!StringUtils.isEmpty(searchString)) {

            numUsers = super.getSessionFactory().fromTransaction( session -> {

                UserLoader loader = new UserLoader(session);
                Integer count = (Integer) loader.countUsers(searchString, groupId);
                return count;
            });
        } 
        else {
            numUsers = getUsersInGroupCount(groupId);
        }
        return numUsers;
    }

    @Override
    public List<User> getSharedUsers() {
        List<User> sharedUsers = 
        (List<User>)super.findByNamedQueryAndNamedParam(
                "sharedUsers", 
                "isShared",
                true,
                User.class );
        return sharedUsers;
    }

    @Override
    public List<User> loadUsersByPage(final String search, final Integer groupId, final Integer branchId,
            final int firstRow, final int pageSize, final String orderBy, final boolean orderAscending) {
       
        if (StringUtils.equals(search, DISABLED) || StringUtils.equals(search, ENABLED)
                || StringUtils.equals(search, LDAP)) {
            return loadUsersByUserProfileAndPage(search, firstRow, pageSize);
        }

        if (StringUtils.equals(search, PHANTOM)) {

            return super.getSessionFactory().fromTransaction( session -> {

                Query query = session.createQuery(PHANTOM_USERS_QUERY);
                query.setFirstResult(firstRow);
                query.setMaxResults(pageSize);
                return query.list();
            });
        }
        
        return super.getSessionFactory().fromTransaction( session -> {

            UserLoader loader = new UserLoader(session);
            List<User> users = (List<User>)loader.loadUsersByPage(
                search, groupId, branchId, firstRow, pageSize, orderBy, orderAscending);;
            return users;
        });
    }

    private List<User> loadUsersByUserProfileAndPage(String search, int firstRow, int pageSize) {
        List<UserProfile> profiles = getUserProfileService().getUserProfilesByEnabledProperty(search,
            firstRow, pageSize);
        List<User> users = new ArrayList<User>();
        for (UserProfile profile : profiles) {
            users.add(loadUser(Integer.valueOf(profile.getUserId())));
        }
        return users;
    }

    @Override
    public List<User> loadUsersByPage(int first, int pageSize) {

        return super.getSessionFactory().fromTransaction( session -> {

            Query<User> q = session.createNativeQuery(
                "select * from users where user_type='C' order by user_id limit :" + PAGE_SIZE + " offset :" + FIRST,
                User.class);
            q.setParameter(FIRST, first);
            q.setParameter(PAGE_SIZE, pageSize);
            return q.getResultList();
        });
    }

    @Override
    public List<Integer> loadUserIdsByPage(int first, int pageSize) {

        return super.getSessionFactory().fromTransaction( session -> {

            Query<Integer> q = session.createNativeQuery(
                "select " + USER_ID + " from users where user_type='C' order by user_id limit :" + PAGE_SIZE + " offset :" + FIRST,
                Integer.class);
            q.setParameter(FIRST, first);
            q.setParameter(PAGE_SIZE, pageSize);
            return q.getResultList();
        });
    }

    @Override
    public List<InternalUser> loadInternalUsers() {
        return super.loadAllEntities(InternalUser.class);
    }

    @Override
    public void clear() {
        List<Object> c = (List<Object>)super.find(QUERY_USER, Object.class);
        super.removeAllEntities(c);
    }
    /**
     * Create a superadmin user with an empty pin. This is used to recover from the loss of all
     * users from the database.
     */
    @Override
    public void createAdminGroupAndInitialUserTask() {
        createAdminGroupAndInitialUser(null);
    }

    /**
     * Create a superadmin user with the specified pin.
     *
     * Map an empty pin to an empty pintoken as a special hack allowing the empty pin to be used
     * when an insecure, easy to remember pin is needed. Previously we used 'password' rather than
     * the empty string, relying on another hack that allowed the password and pintoken to be the
     * same. That hack is gone so setting the pintoken to 'password' would no longer work because
     * the password would then be the inverse hash of 'password' rather than 'password'.
     */
    @Override
    public void createAdminGroupAndInitialUser(String pin) {
        Group adminGroup = m_settingDao.getGroupByName(User.GROUP_RESOURCE_ID, ADMIN_GROUP_NAME);
        if (adminGroup == null) {
            adminGroup = new Group();
            adminGroup.setName(ADMIN_GROUP_NAME);
            adminGroup.setResource(User.GROUP_RESOURCE_ID);
            adminGroup.setDescription("Users with superadmin privileges");
            persistEntity(adminGroup);
        }

        User admin = loadUserByUserName(User.SUPERADMIN);

        boolean newUser = (admin == null);

        if (newUser) {
            admin = newUser();
            admin.setUserName(User.SUPERADMIN);
            // currently superadmin cannot invite to a conference without a valid sip password
            admin.setSipPassword(RandomStringUtils.randomAlphanumeric(SIP_PASSWORD_LEN));
            admin.setPermission(PermissionName.SUPERADMIN, true);
            admin.setPin(StringUtils.defaultString(pin));
        }

        PermissionName.SUPERADMIN.setEnabled(adminGroup, true);
        PermissionName.TUI_CHANGE_PIN.setEnabled(adminGroup, false);
        admin.addGroup(adminGroup);
        // enable IM for superadmin
        ImAccount imAccount = new ImAccount(admin);
        imAccount.setEnabled(true);
        if( newUser ) {
            persistEntity(admin);
        }
        else {
            mergeEntity(admin);
        }
    }

/*    @Override
    public void saveUserToAgentsGroup(User user) {
        createAgentsGroup();
        Group allAgentsGroup = m_settingDao.getGroupByName(User.GROUP_RESOURCE_ID, AGENT_GROUP_NAME);
        if (allAgentsGroup != null) {
            user.getGroups().add(allAgentsGroup);
            super.mergeEntity(user);
        }
    }

    @Override
    public void saveRemoveUserFromAgentGroup(User user) {
        Group agentGroup = m_settingDao.getGroupByName(User.GROUP_RESOURCE_ID, AGENT_GROUP_NAME);
        if (agentGroup != null) {
            user.removeGroup(agentGroup);
            saveUser(user);
        }
    }*/

    public void setSettingDao(SettingDao settingDao) {
        m_settingDao = settingDao;
    }

    @Override
    public List<Group> getGroups() {
        return m_settingDao.getGroups(USER_GROUP_RESOURCE_ID);
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
    public List<Group> getAvailableGroups(User user) {
        List<Group> allGroups = getGroups();
        List<Group> availableGroups = new ArrayList<Group>();
        for (Group group : allGroups) {
            if (user.isGroupAvailable(group)) {
                availableGroups.add(group);
            }
        }
        return availableGroups;
    }

    @Override
    public Group getGroupById(Integer groupId) {
        List<Group> groups = m_settingDao.getGroups(USER_GROUP_RESOURCE_ID);
        for (Group group : groups) {
            int id = group.getId();
            if (groupId == id) {
                return group;
            }
        }
        return null;
    }

    @Override
    public Group getGroupByName(String userGroupName, boolean createIfNotFound) {
        if (createIfNotFound) {
            return m_settingDao.getGroupCreateIfNotFound(USER_GROUP_RESOURCE_ID, userGroupName);
        }
        return m_settingDao.getGroupByName(USER_GROUP_RESOURCE_ID, userGroupName);
    }

    @Override
    public List<User> getGroupMembers(Group group) {
        List<User> users = (List<User>)super.findByNamedQueryAndNamedParam("userGroupMembers",
                QUERY_PARAM_GROUP_ID, group.getId(), User.class );
        return users;
    }

    @Override
    public Collection<Integer> getGroupMembersIds(Group group) {
        return m_jdbcTemplate.queryForList(
                "select users.user_id from users join user_group on user_group.user_id=users.user_id where "
                + "user_group.group_id=" + group.getId(), Integer.class);
    }

    @Override
    public Collection<Integer> getGroupMembersByPage(int gid, int first, int pageSize) {
        final List<Integer> ids = new LinkedList<Integer>();
        m_jdbcTemplate.query(String.format("select users.user_id from users join user_group on "
                + "user_group.user_id=users.user_id "
                + "where user_group.group_id=%d "
                + "order by users.user_id limit %d offset %d", gid, pageSize, first),
                new RowCallbackHandler() {

                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        ids.add(rs.getInt(USER_ID));
                    }
                });

        return ids;
    }

    @Override
    public List<String> getGroupMembersNames(Group group) {
        List<String> userNames = (List<String>)super.findByNamedQueryAndNamedParam("userNamesGroupMembers",
                QUERY_PARAM_GROUP_ID, group.getId(), String.class);
        return userNames;
    }

    @Override
    public int getGroupMembersCount(int groupId) {
        return m_jdbcTemplate.queryForObject(
                "select count(users.user_id) from users join user_group on user_group.user_id=users.user_id "
                + "where user_group.group_id=" + groupId,
                Integer.class);
    }

    @Override
    public int getBranchMembersCount(int branchId) {
        return m_jdbcTemplate.queryForObject(
                "select count (users.user_id) from users left outer join "
                + "user_group on users.user_id=user_group.user_id "
                + " left outer join group_storage on user_group.group_id=group_storage.group_id "
                + " where group_storage.branch_id=" + branchId + " or users.branch_id=" + branchId,
                Integer.class );
    }

    @Override
    public Collection<Integer> getBranchMembersByPage(int bid, int first, int pageSize) {

        return super.getSessionFactory().fromTransaction( session -> {

            String sql = """
                select users.user_id
                from users
                left outer join user_group on users.user_id = user_group.user_id
                left outer join group_storage on user_group.group_id = group_storage.group_id
                where group_storage.branch_id = :branchId or users.branch_id = :branchId
                order by users.user_id
                limit :pageSize offset :first
            """;

            NativeQuery<Integer> query = session.createNativeQuery(sql, Integer.class);
            query.setParameter(BRANCH_ID, bid);
            query.setParameter(FIRST, first);
            query.setParameter(PAGE_SIZE, pageSize);

            return query.getResultList();
        });
    }

    @Override 
    public boolean isAliasInUse(String alias) {

        boolean inUse = super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<Integer> query = session
                .createNativeQuery(SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS, Integer.class);
            query.setParameter(ALIAS, alias);

            List<Integer> userIds = query.getResultList();

            return SipxCollectionUtils.safeSize(userIds) > 0;
        });

        // Fallback check in user profile DB
        return inUse || getUserProfileService().isImIdInUse(alias);
    }
    
    @Override
    public boolean isAliasInUseExceptDid(String alias) {

        boolean inUse = super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<Integer> query = session
                .createNativeQuery(SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS_EXCEPT_DID, Integer.class);
            query.setParameter(ALIAS, alias);

            List<Integer> userIds = query.getResultList();

            return SipxCollectionUtils.safeSize(userIds) > 0;
        });

        // Fallback check in user profile DB
        return inUse || getUserProfileService().isImIdInUse(alias);
    }

    @Override
    public boolean isAliasInUseForOthers(String alias, String username) {

        boolean inUse = super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<Integer> query = session
                .createNativeQuery(SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS_EXCEPT_THIS, Integer.class);
            query.setParameter(ALIAS, alias);
            query.setParameter(USERNAME, username);

            List<Integer> userIds = query.getResultList();

            return SipxCollectionUtils.safeSize(userIds) > 0;
        });

        // Fallback check in user profile DB
        return inUse || getUserProfileService().isAliasInUse(alias, username);
    }

    @Override
    public Collection<BeanId> getBeanIdsOfObjectsWithAlias(String alias) {

        List<Integer> ids = super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<Integer> query = session
                .createNativeQuery(SQL_QUERY_USER_IDS_BY_NAME_OR_ALIAS, Integer.class);
            query.setParameter(ALIAS, alias);

            return query.getResultList();
        });

        String username = getUserProfileService().getUsernameByImId(alias);
        if (username != null) {
            User user = loadUserByUserName(username);
            Integer userId = user.getId();
            if (!ids.contains(userId)) {
                ids.add(userId);
            }
        }

        return BeanId.createBeanIdCollection(ids, User.class);
        
    }

    @Override
    public void addToGroup(Integer groupId, Collection<Integer> ids) {

        Group group = super.loadEntity(Group.class, groupId);
        for (Integer id : ids) {
            User user = loadUser(id);
            if (!user.isGroupAvailable(group)) {
                throw new UserException("&branch.validity.error", user.getUserName(), user.getSite().getName(),
                        group.getBranch().getName());
            }
        }
        
        super.getSessionFactory().inTransaction( session -> {

            DaoUtils.addToGroup(session, getDaoEventPublisher(), groupId, User.class, ids);
        });
    }

    @Override
    public void removeFromGroup(Integer groupId, Collection<Integer> ids) {

        super.getSessionFactory().inTransaction( session -> {

            DaoUtils.removeFromGroup(session, getDaoEventPublisher(), groupId, User.class, ids);
        });
    }

    @Override
    public List<User> getGroupSupervisors(Group group) {
        List<User> objs = (List<User>)super.findByNamedQueryAndNamedParam("groupSupervisors",
                QUERY_PARAM_GROUP_ID, group.getId(), User.class );
        return objs;
    }

    @Override
    public List<User> getUsersThatISupervise(User supervisor) {
        List<User> objs = (List<User>)super.findByNamedQueryAndNamedParam("usersThatISupervise",
                "supervisorId", supervisor.getId(), User.class );
        return objs;
    }

    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    /**
     * Given a collection of extensions, looks for invalid user or user without a specified
     * permission. Throw a exception if an invalid extension found.
     *
     * @param list of user aliases
     * @param permission permission to check
     * @throws ExtensionException if at least one of the aliases does not represent a valid user
     *         with permission enabled
     */
    @Override
    public void checkForValidExtensions(Collection<String> aliases, PermissionName permission) {
        Collection<String> invalidExtensions = new ArrayList<String>();
        for (String extension : aliases) {
            User user = loadUserByUserNameOrAlias(extension);
            if (user == null) {
                invalidExtensions.add(extension);
            } else if (!user.hasPermission(permission)) {
                invalidExtensions.add(extension);
            }
        }
        if (!invalidExtensions.isEmpty()) {
            throw new ExtensionException(permission, invalidExtensions);
        }
    }

    static class ExtensionException extends UserException {
        private static final String ERROR = "The following extensions do not exist or do not have {0} permission: {1}.";

        ExtensionException(PermissionName permission, Collection<String> invalidExtensions) {
            super(ERROR, permission.getName(), StringUtils.join(invalidExtensions, ", "));
        }
    }

    @Override
    public User getSpecialUser(SpecialUserType specialUserType) {
        List<SpecialUser> specialUsersOfType = (List<SpecialUser>)super.findByNamedQueryAndNamedParam(
                SPECIAL_USER_BY_TYPE, SPECIAL_USER_TYPE, specialUserType.name(), SpecialUser.class );
        SpecialUser specialUser = DataAccessUtils.singleResult(specialUsersOfType);
        if (specialUser == null) {
            return null;
        }

        User newUser = newUser();
        newUser.setUserName(specialUser.getUserName());
        newUser.setSipPassword(specialUser.getSipPassword());

        // if this is the provisioning user we don't want to have to much permissions
        // these are auto provisioned phones which better shouldn't have dialout
        // permissions at all
        if (specialUserType == SpecialUserType.PHONE_PROVISION) {
            // We remove all system permissions for provisioning user
            newUser.setSettingTypedValue(
                PermissionName.EXCHANGE_VOICEMAIL.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.FREESWITH_VOICEMAIL.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.INTERNATIONAL_DIALING.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.LOCAL_DIALING.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.LONG_DISTANCE_DIALING.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.MOBILE.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.MUSIC_ON_HOLD.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.NINEHUNDERED_DIALING.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.PERSONAL_AUTO_ATTENDANT.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.RECORD_SYSTEM_PROMPTS.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.SUBSCRIBE_TO_PRESENCE.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.SUPERADMIN.getPath(), false);

            // We could allow this theoretically but it would be better
            // to only allow real users to dial out
            newUser.setSettingTypedValue(
                PermissionName.TOLL_FREE_DIALING.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.TUI_CHANGE_PIN.getPath(), false);
            newUser.setSettingTypedValue(
                PermissionName.VOICEMAIL.getPath(), false);

            // Emergency dialing is the only allowed thing!
            // Does not matter, cause we can't disable it at all
        }
        return newUser;
    }

    @Override
    public SpecialUser getSpecialUserAsSpecialUser(SpecialUserType specialUserType) {
        List<SpecialUser> specialUsersOfType = (List<SpecialUser>)super.findByNamedQueryAndNamedParam(
                SPECIAL_USER_BY_TYPE, SPECIAL_USER_TYPE, specialUserType.name(), SpecialUser.class );
        SpecialUser specialUser = DataAccessUtils.singleResult(specialUsersOfType);
        if (specialUser == null) {
            return null;
        }
        return specialUser;
    }

    @Override
    public int getEnabledUsersCount() {
        return getUserProfileService().getEnabledUsersCount();
    }

    @Override
    public int getDisabledUsersCount() {
        return getUserProfileService().getDisabledUsersCount();
    }

    @Override
    public int getPhantomUsersCount() {

        return super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<?> query = session.createNativeQuery(SQL_QUERY_PHANTOM_USERS);
            Number result = (Number) query.getSingleResult();
            return result != null ? result : 0;
        }).intValue();
    }

    @Override
    public int getPhantomUsersWithoutSuperadminCount() {

        return super.getSessionFactory().fromTransaction( session -> {

            NativeQuery<?> query = session.createNativeQuery(SQL_QUERY_PHANTOM_USERS_WITHOUT_SUPERADMIN);
            Number result = (Number) query.getSingleResult();
            return result != null ? result : 0;
        }).intValue();
    }

    @Override
    public void initializeSpecialUsers() {
        for (SpecialUserType type : SpecialUserType.values()) {
            User specialUser = getSpecialUser(type);
            if (specialUser == null) {
                SpecialUser newSpecialUser = new SpecialUser(type);
                super.mergeEntity(newSpecialUser);
            }
        }
    }

    /*
     * Here take account only of the special users. In ReplicationManagerImpl.generateAll all
     * usersare replicated separately
     *
     * @see org.sipfoundry.sipxconfig.common.ReplicableProvider#getReplicables()
     */
    @Override
    public List<Replicable> getReplicables() {
        List<Replicable> replicables = new ArrayList<Replicable>();
        for (SpecialUserType specialUserType : SpecialUserType.values()) {
            SpecialUser user = getSpecialUserAsSpecialUser(specialUserType);
            replicables.add(user);
        }
        replicables.addAll(getGroups());
        return replicables;
    }

    public void setConfigJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }


    public void setDidService(DidService didService) {
        m_didService = didService;
    }

    @Override
    public boolean setup(SetupManager manager) {        
        if (manager.isFalse(AdminContext.FEATURE.getId())) {
            Location primary = manager.getConfigManager().getLocationManager().getPrimaryLocation();
            if (primary == null) {
                return false;
            }
            manager.getFeatureManager().enableLocationFeature(AdminContext.FEATURE, primary, true);
            manager.setTrue(AdminContext.FEATURE.getId());
        }
        if (!m_setup) {
            // this checks special users on every start-up as there seems to be no harm.
            initializeSpecialUsers();
            m_setup = true;
        }
        return true;
    }
}
