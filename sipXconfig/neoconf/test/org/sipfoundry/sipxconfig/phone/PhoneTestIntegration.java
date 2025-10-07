/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.MongoTestCaseHelper;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.device.ModelSource;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.setting.ValueStorage;
import org.sipfoundry.sipxconfig.test.MongoTestIntegration;
import org.sipfoundry.sipxconfig.test.ResultDataGrid;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.orm.ObjectRetrievalFailureException;

public class PhoneTestIntegration extends MongoTestIntegration {
    private PhoneContext context;
    private SettingDao settingDao;
    private CoreContext core;
    private ModelSource<PhoneModel> phoneModelSource;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
        getImdb().dropCollection("entity");
    }

    public void testSave() throws Exception {
        final Phone phone = context.newPhone(new TestPhoneModel());
        phone.setSerialNumber("999123456789");
        phone.setDescription("unittest-sample phone1");
        context.storePhone(phone);
        flush();
        final int[] rowCount = new int[] {0};
        db().query("select * from phone", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet actual) throws SQLException {
                rowCount[0]++;
                assertEquals(phone.getSerialNumber(), actual.getString("serial_number"));
                assertEquals(phone.getDescription(), actual.getString("description"));
                assertEquals("testPhoneModel", actual.getString("model_id"));
            }
        });
        assertEquals(1, rowCount[0]);

        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getImdb().getCollection("entity"), new String[] {
            "ent", "mac"
        }, new String[] {
            "phone", "999123456789"
        });
    }

    public void testLoadAndDelete() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");

        Phone p = context.loadPhone(Integer.valueOf(1000));
        assertEquals("999123456789", p.getSerialNumber());

        context.storePhone(p);

        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getImdb().getCollection("entity"), new String[] {
            "ent", "mac"
        }, new String[] {
            "phone", "999123456789"
        });

        Integer id = p.getId();
        context.deletePhone(p);
        try {
            context.loadPhone(id);
            fail();
        } catch (ObjectRetrievalFailureException x) {
            assertTrue(true);
        }
        flush();
        assertEquals(0, (int)db().queryForObject("select count(*) from phone", Integer.class));
        assertEquals(0, (int)db().queryForObject("select count(*) from line", Integer.class));

        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getImdb().getCollection("entity"), new String[] {
            "ent", "mac"
        }, new String[] {
            "phone", "999123456789"
        });
    }

    public void testUpdateSettings() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");

        Phone p = context.loadPhone(Integer.valueOf(1000));
        context.storePhone(p);
        context.flush();

        Phone reloadPhone = context.loadPhone(Integer.valueOf(1000));
        ValueStorage s = (ValueStorage) reloadPhone.getValueStorage();
        Map<String, Object> vs = db().queryForMap("select * from setting_value where value_storage_id = ?", s.getId());
        assertEquals("group.version/firmware.version", vs.get("path"));
    }

    public void testAddGroup() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        sql("phone/SeedPhoneGroup.sql");

        Phone p = context.loadPhone(Integer.valueOf(1000));
        List<Group> groups = context.getGroups();
        p.addGroup((Group) groups.get(0));
        context.storePhone(p);
        flush();
        db().queryForObject("select 1 from phone_group where group_id = ? and phone_id = ?", Integer.class, 1000, 1000);
    }

    public void testRemoveGroupThenAddBackThenAddAnotherGroup() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        sql("phone/SeedPhoneGroup.sql");
        Phone p = context.loadPhone(Integer.valueOf(1000));
        List<Group> groups = context.getGroups();
        p.addGroup((Group) groups.get(0));
        context.storePhone(p);
        p = null;

        Phone reloaded = context.loadPhone(Integer.valueOf(1000));
        reloaded.getGroups().clear();
        reloaded.addGroup((Group) groups.get(0));
        reloaded.addGroup((Group) groups.get(1));
        context.storePhone(reloaded);
        flush();
        Object[][] expected = new Object[][] {
                new Object[] { 1000, 1000 },
                new Object[] { 1001, 1000 }
        };

        ResultDataGrid actual = new ResultDataGrid();
        db().query("select group_id, phone_id from phone_group", actual);
        assertEquals(2, actual.getRowCount());
        assertArrayEquals(expected, actual.toArray());
    }

    public void testPhoneSubclassSave() throws Exception {
        PhoneModel model = new PhoneModel("acmePhone", "acmePhoneStandard");
        Phone subclass = context.newPhone(model);
        subclass.setSerialNumber("000000000000");
        context.storePhone(subclass);
        flush();
        db().queryForObject("select 1 from phone where serial_number = ? and bean_id = ? and model_id = ?",
                Integer.class,
                subclass.getSerialNumber(), 
                model.getBeanId(), 
                model.getModelId());
    }

    public void testPhoneSubclassDelete() throws Exception {
        sql("phone/PhoneSubclassSeed.sql");
        Phone subclass = context.loadPhone(Integer.valueOf(1000));
        subclass.setSerialNumber("000000000000");
        context.deletePhone(subclass);
        flush();
        assertEquals(0, (long)db().queryForObject("select count(*) from phone", Long.class ));
    }

    public void testClear() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        sql("phone/SeedPhoneGroup.sql");
        // tests foreign keys
        context.clear();
    }

    public void testDeletePhoneGroups() throws Exception {
        sql("phone/GroupMemberCountSeed.sql");
        settingDao.deleteGroups(Collections.singletonList(Integer.valueOf(1001)));
        flush();
        assertEquals(1, (long)db().queryForObject("select count(*) from phone_group", Long.class));
    }

    public void testPhonesByUserId() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        Collection<Phone> phones = context.getPhonesByUserId(Integer.valueOf(1000));
        assertEquals(1, phones.size());
        Phone p = (Phone) phones.iterator().next();
        assertEquals("unittest-sample phone1", p.getDescription());
    }

    public void testPhonesByUserIdAndPhoneModel() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        Collection<Phone> phones = context.getPhonesByUserIdAndPhoneModel(Integer.valueOf(1000), "testPhoneModel");
        assertEquals(1, phones.size());
        Phone p = (Phone) phones.iterator().next();
        assertEquals("unittest-sample phone1", p.getDescription());
        phones = context.getPhonesByUserIdAndPhoneModel(Integer.valueOf(1000), "unknownModel");
        assertEquals(0, phones.size());
    }

    public void testDeleteUserRemoveLines() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        User testUser = core.loadUser(Integer.valueOf(1000));
        core.deleteUser(testUser);
        assertEquals(0, TestHelper.getConnection().createDataSet().getTable("line").getRowCount());
    }

    public void testDeleteUserOnPhoneWithExternalLines() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/ExternalLineSeed.sql");
        User testUser = core.loadUser(Integer.valueOf(1000));
        core.deleteUser(testUser);
        flush();
        assertEquals(1, (long)db().queryForObject("select count(*) from line", Long.class));

        // no primary user after user is deleted
        Phone phone = context.loadPhone(1000);
        assertNull(phone.getPrimaryUser());
    }

    public void testDeviceVersion() throws Exception {
        sql("phone/PhoneVersionSeed.sql");
        Phone phone = context.loadPhone(1000);
        DeviceVersion deviceVersion = phone.getDeviceVersion();
        assertNotNull(deviceVersion);
        assertEquals("acmePhone", deviceVersion.getVendorId());
        assertEquals("1", deviceVersion.getVersionId());
        context.storePhone(phone);
    }

    public void testGetPrimaryUser() throws Exception {
        sql("common/TestUserSeed.sql");
        sql("phone/EndpointLineSeed.sql");
        Phone phone = context.loadPhone(1000);
        User user = phone.getPrimaryUser();
        assertNotNull(user);
        assertEquals(1000, user.getId().intValue());
    }

    public void testGetPrimaryUserNoUser() throws Exception {
        sql("phone/PhoneVersionSeed.sql");
        Phone phone = context.loadPhone(1000);
        assertNull(phone.getPrimaryUser());
    }

    public void testGetPrimaryUserLineError() throws IOException {
        sql("common/TestUserSeed.sql");
        loadDataSet("phone/ErroneousLineSeed.xml");
        Phone phone = context.loadPhone(1000);
        User user = phone.getPrimaryUser();
        assertNull(user);
    }

    public void testPopulatePhones() throws Exception {
        for (PhoneModel model : phoneModelSource.getModels()) {
            Phone phone = context.newPhone(model);
            phone.setSerialNumber(RandomStringUtils.randomNumeric(12));
            context.storePhone(phone);
        }
    }

    public void setPhoneContext(PhoneContext context) {
        this.context = context;
    }

    public void setSettingDao(SettingDao settingDao) {
        this.settingDao = settingDao;
    }

    public void setCoreContext(CoreContext core) {
        this.core = core;
    }

    public void setPhoneModelSource(ModelSource<PhoneModel> phoneModelSource) {
        this.phoneModelSource = phoneModelSource;
    }
}
