/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.bulk.ldap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.createMock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.sipfoundry.commons.userdb.profile.UserProfile;
import org.sipfoundry.sipxconfig.bulk.RowInserter.RowStatus;
import org.sipfoundry.sipxconfig.bulk.csv.Index;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.permission.PermissionManager;
import org.sipfoundry.sipxconfig.setting.AbstractSetting;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.setting.SettingImpl;
import org.sipfoundry.sipxconfig.setting.SettingSet;
import org.sipfoundry.sipxconfig.setting.type.IntegerSetting;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.sipfoundry.sipxconfig.vm.MailboxManager;

public class LdapRowInserterTest extends TestCase {
    protected static final String SALES = "sales";
    private static final String LDAP = "ldap";
    private static final String NO_LDAP = "noldap";
    private static final String JOE = "joe";

    protected String m_joe;
    protected LdapRowInserter m_rowInserter;
    protected LdapConnectionParams m_connParams;
    protected SettingDao m_settingDao;

    @Override
    protected void setUp() {
        m_joe = "joe";
        m_rowInserter = new LdapRowInserter();
        m_connParams = new LdapConnectionParams();
        SettingSet root = new SettingSet();
        AbstractSetting ldapManagement = (AbstractSetting) root.addSetting(new SettingSet("ldap-management"));
        AbstractSetting newGroupNamePrefix = (AbstractSetting) ldapManagement.addSetting(new SettingImpl("newUserGroupPrefix"));
        newGroupNamePrefix.setValue("grPrefix_");
        AbstractSetting stripUsername = (AbstractSetting) ldapManagement.addSetting(new SettingImpl("stripUserName"));
        stripUsername.setType(new IntegerSetting());
        stripUsername.setTypedValue(Integer.valueOf(0));
        AbstractSetting regex = (AbstractSetting) ldapManagement.addSetting(new SettingImpl("regex"));
        regex.setTypedValue("");
        AbstractSetting prefix = (AbstractSetting) ldapManagement.addSetting(new SettingImpl("prefix"));
        prefix.setTypedValue("");
        AbstractSetting suffix = (AbstractSetting) ldapManagement.addSetting(new SettingImpl("suffix"));
        suffix.setTypedValue("");        

        m_connParams.setSettings(root);
        
        Group newGroup = new Group();
        newGroup.setName("grPrefix_example.com");
        newGroup.setResource(CoreContext.USER_GROUP_RESOURCE_ID);
        m_settingDao = createMock(SettingDao.class);
        m_settingDao.getGroupCreateIfNotFound(CoreContext.USER_GROUP_RESOURCE_ID, "grPrefix_example.com");
        expectLastCall().andReturn(newGroup);
        replay(m_settingDao);
        AttrMap attrMap = new AttrMap();
        attrMap.setDefaultGroupName("test-import");
        attrMap.setAttribute(Index.USERNAME.getName(), "identity");
        m_rowInserter.setAttrMap(attrMap);
        m_rowInserter.setLdapConnectionParams(m_connParams);
        m_rowInserter.setSettingDao(m_settingDao);
    }

    private User insertRow(boolean existingUser, boolean ldapManaged) throws Exception {
        IMocksControl control = EasyMock.createNiceControl();
        UserMapper userMapper = control.createMock(UserMapper.class);
        SearchResult searchResult = control.createMock(SearchResult.class);

        Attributes attributes = control.createMock(Attributes.class);
        Attribute attribute = control.createMock(Attribute.class);

        EasyMock.expect(searchResult.getAttributes()).andReturn(attributes);
        EasyMock.expect(attributes.get("identity")).andReturn(attribute);
        EasyMock.expect(userMapper.getUserName(attributes)).andReturn(m_joe).times(2);
        EasyMock.expect(userMapper.getGroupNames(searchResult)).andReturn(Collections.singleton(SALES));
        control.replay();

        User joe = new User();

        PermissionManager pManager = EasyMock.createMock(PermissionManager.class);
        EasyMock.expect(pManager.getPermissionModel())
                .andReturn(TestHelper.loadSettings("commserver/user-settings.xml"))
                .anyTimes();
        EasyMock.replay(pManager);

        joe.setPermissionManager(pManager);
        joe.setUserProfile(new UserProfile());

        Group salesGroup = new Group();
        salesGroup.setName(SALES);
        salesGroup.setUniqueId();

        Group importGroup = new Group();
        importGroup.setName("import");
        importGroup.setUniqueId();

        AttrMap map = new AttrMap();
        map.setAttribute(Index.USERNAME.getName(), "identity");
        map.setObjectClass("person");
        map.setDefaultGroupName("test-import");

        IMocksControl coreContextControl = EasyMock.createControl();
        CoreContext coreContext = coreContextControl.createMock(CoreContext.class);
        LdapManager ldapManager = coreContextControl.createMock(LdapManager.class);

        EasyMock.expect(coreContext.loadUserByUserName(JOE)).andReturn(existingUser ? joe : null);

        if (!existingUser) {
            EasyMock.expect(coreContext.newUser()).andReturn(joe);
        } else {
            Group ldapGroup = new Group();
            ldapGroup.setName(LDAP);
            ldapGroup.setUniqueId();
            ldapGroup.setSettingValue(LdapRowInserter.LDAP_SETTING, "true");

            Group noLdapGroup = new Group();
            noLdapGroup.setUniqueId();
            noLdapGroup.setName(NO_LDAP);

            Set<Group> groups = new HashSet<>();
            groups.add(salesGroup);
            groups.add(ldapGroup);
            groups.add(noLdapGroup);

            joe.replaceGroups(groups);
            joe.setLdapManaged(ldapManaged);
        }

        EasyMock.expect(coreContext.getGroupByName(SALES, true)).andReturn(salesGroup);
        coreContext.saveUser(joe);
        EasyMock.expect(ldapManager.retriveOverwritePin()).andReturn(new OverwritePinBean(100, true)).anyTimes();
        coreContextControl.replay();

        IMocksControl mailboxManagerControl = EasyMock.createControl();
        MailboxManager mailboxManager = mailboxManagerControl.createMock(MailboxManager.class);
        EasyMock.expect(mailboxManager.isEnabled()).andReturn(true);
        mailboxManager.deleteMailbox(JOE);
        mailboxManagerControl.replay();

        UserMapper rowInserterUserMapper = new UserMapper();
        m_rowInserter.setUserMapper(rowInserterUserMapper);
        m_rowInserter.setCoreContext(coreContext);
        m_rowInserter.setUserMapper(userMapper);
        m_rowInserter.setMailboxManager(mailboxManager);
        m_rowInserter.setAttrMap(map);
        m_rowInserter.setDomain("example.com");
        m_rowInserter.setPermissionManager(pManager);
        m_rowInserter.beforeInserting((Object[]) null);
        m_rowInserter.checkRowData(searchResult);
        m_rowInserter.insertRow(searchResult, attributes);
        m_rowInserter.afterInserting();

        coreContextControl.verify();
        control.verify();
        return joe;
    }

    public void testInsertRowExistingUser() throws Exception {
        User joe = insertRow(true, true);
        assertEquals("example.com", joe.getSettingValue(User.DOMAIN_SETTING));
        assertEquals(2, joe.getGroups().size());
        //existing ldap group have been deleted and replaced with new ldap group
        for (Group group : joe.getGroups()) {
            if (StringUtils.equals(group.getName(), SALES)) {
                assertTrue(Boolean.valueOf(group.getSettingValue(LdapRowInserter.LDAP_SETTING)));
            } else if (StringUtils.equals(group.getName(), NO_LDAP)) {
                assertFalse(Boolean.valueOf(group.getSettingValue(LdapRowInserter.LDAP_SETTING)));
            }
        }
    }

    public void testInsertRowExistingUserNonLdap() throws Exception {
        try {
            insertRow(true, false);
            Assert.fail("Did not fail on insert");
        } catch (AssertionError ex) {
            Assert.assertNotNull(ex.getMessage());
        }
    }

    public void testInsertRowNewUser() throws Exception {
        User joe = insertRow(false, false);
        assertEquals(2, joe.getGroups().size());
        //ldap group was saved
        for (Group group : joe.getGroups()) {
            if (StringUtils.equals(group.getName(), SALES)) {
                assertTrue(Boolean.valueOf(group.getSettingValue(LdapRowInserter.LDAP_SETTING)));
            }
        }
    }

    /**
     * Test applies in cases when username is valid no matter if is formatted or not @see LdapRowInserter.formatUserName
     */
    public void testCheckRowDataValid() throws Exception {
        IMocksControl control = org.easymock.EasyMock.createNiceControl();
        UserMapper userMapper = control.createMock(UserMapper.class);
        SearchResult searchResult = control.createMock(SearchResult.class);
        Attributes attributes = control.createMock(Attributes.class);
        Attribute attribute = control.createMock(Attribute.class);

        // First run: identity is null
        EasyMock.expect(searchResult.getAttributes()).andReturn(attributes);
        EasyMock.expect(attributes.get("identity")).andReturn(null);
        control.replay();

        m_rowInserter.setUserMapper(userMapper);
        m_rowInserter.beforeInserting((Object[]) null);
        assertEquals(RowStatus.FAILURE, m_rowInserter.checkRowData(searchResult).getRowStatus());

        // Second run: identity is found, username and group names retrieved
        control.reset();
        EasyMock.expect(searchResult.getAttributes()).andReturn(attributes);
        EasyMock.expect(attributes.get("identity")).andReturn(attribute);
        EasyMock.expect(userMapper.getUserName(attributes)).andReturn("McQueen");
        EasyMock.expect(userMapper.getGroupNames(searchResult)).andReturn(Collections.singleton(SALES));
        control.replay();

        m_rowInserter.setUserMapper(userMapper);
        assertEquals(RowStatus.SUCCESS, m_rowInserter.checkRowData(searchResult).getRowStatus());
    }
    
    /**
     * Test applies in cases when username is not valid when not formatted and valid otherwise not @see LdapRowInserter.formatUserName
     */    
    public void testCheckRowDataNotValid() throws Exception {
        IMocksControl control = org.easymock.EasyMock.createNiceControl();
        UserMapper userMapper = control.createMock(UserMapper.class);
        SearchResult searchResult = control.createMock(SearchResult.class);
        Attributes attributes = control.createMock(Attributes.class);
        Attribute attribute = control.createMock(Attribute.class);
        
        EasyMock.expect(searchResult.getAttributes()).andReturn(attributes);
        EasyMock.expect(attributes.get("identity")).andReturn(attribute);
        EasyMock.expect(userMapper.getUserName(attributes)).andReturn("@McQueen");
        EasyMock.expect(userMapper.getGroupNames(searchResult)).andReturn(Collections.singleton(SALES));
        
        control.replay();
        m_rowInserter.setUserMapper(userMapper);
        m_rowInserter.beforeInserting((Object[]) null);
        assertEquals(RowStatus.FAILURE, m_rowInserter.checkRowData(searchResult).getRowStatus());
    }  
}
