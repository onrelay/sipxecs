package org.sipfoundry.sipxconfig.commserver.imdb;

import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.PERMISSIONS;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sipfoundry.sipxconfig.common.Closure;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.speeddial.Button;
import org.sipfoundry.sipxconfig.speeddial.SpeedDialGroup;
import org.sipfoundry.sipxconfig.speeddial.SpeedDialManager;
import org.sipfoundry.sipxconfig.test.ImdbTestCase;
import org.sipfoundry.sipxconfig.test.TestHelper;

import org.bson.Document;

public class SpeedDialsTestIntegration extends ImdbTestCase {
    private SpeedDials m_speeddialDataSet;
    private SettingDao m_settingDao;
    private SpeedDialManager m_speedDialManager;

    public void testGenerateResourceLists() throws Exception {
        TestHelper.cleanInsert("ClearDb.xml");
        loadDataSetXml("commserver/seedLocations.xml");
        loadDataSetXml("domain/DomainSeed.xml");
        //loadDataSet("commserver/imdb/speeddials.db.xml");
        sql("commserver/imdb/speeddials.sql");

        /* TODO: enable test for speed dial groups;
         * i don't understand why a group with 2 members when it gets to:
         *  DaoUtils.forAllGroupMembersDo(CoreContext coreContext, Group group,
            Closure<User> closure, int start, int pageSize)
            the return of  coreContext.getGroupMembersCount(group.getId()) is 0
        SpeedDialGroup spdlGroup = m_speedDialManager.getSpeedDialForGroupId(1001);
        m_speedDialManager.saveSpeedDialGroup(spdlGroup);*/


        //just to trigger the replication
        User userA = getCoreContext().loadUserByUserName("user_a");
        User userB = getCoreContext().loadUserByUserName("user_b");
        User userC = getCoreContext().loadUserByUserName("user_c");
        User userD = getCoreContext().loadUserByUserName("user_d");
        getCoreContext().saveUser(userA);
        getCoreContext().saveUser(userB);
        getCoreContext().saveUser(userC);
        getCoreContext().saveUser(userD);

        Document user1 = new Document().append(ID, "User9991").append(UID, "user_a");
        Document speeddial1 = new Document("usr", "~~rl~F~user_a")
            .append("usrcns", "~~rl~C~user_a");
        List<Document> btns1 = new ArrayList<Document>();
        btns1.add(new Document("uri", "sip:102@example.org").append("name", "beta"));
        btns1.add(new Document("uri", "sip:104@sipfoundry.org").append("name", "gamma"));
        speeddial1.append("btn", btns1);
        user1.put("spdl", speeddial1);

        Document user2 = new Document().append(ID, "User9992").append(UID, "user_b");
        Document speeddial2 = new Document("usr", "~~rl~F~user_b")
            .append("usrcns", "~~rl~C~user_b");
        List<Document> btns2 = new ArrayList<Document>();
        btns2.add(new Document("uri", "sip:404@example.org").append("name", "beta1"));
        speeddial2.append("btn", btns2);
        user2.put("spdl", speeddial2);


        Document user3 = new Document().append(ID, "User9993").append(UID, "user_c");
        Document speeddial3 = new Document();
        user3.put("spdl", speeddial3);

        Document user4 = new Document().append(ID, "User9994").append(UID, "user_d");
        Document speeddial4 = new Document("usr", "~~rl~F~user_d")
            .append("usrcns", "~~rl~C~user_d");
        List<Document> btns4 = new ArrayList<Document>();
        btns4.add(new Document("uri", "sip:101@example.org").append("name", "alpha"));
        speeddial4.append("btn", btns4);
        user4.put("spdl", speeddial4);

        MongoTestCaseHelper.assertObjectPresent(getEntityCollection(), user1);
        MongoTestCaseHelper.assertObjectPresent(getEntityCollection(), user2);
        MongoTestCaseHelper.assertObjectWithIdFieldValuePresent(getEntityCollection(), "User9993", "spdl", null);

        MongoTestCaseHelper.assertObjectPresent(getEntityCollection(), user4);
        m_speedDialManager.deleteSpeedDialsForUser(9994);
        getCoreContext().saveUser(userD);
        MongoTestCaseHelper.assertObjectWithIdFieldValuePresent(getEntityCollection(), "User9994", "spdl", null);
    }

    public void testSpeedDials() {

    }

    public void setSpeeddialDataSet(SpeedDials speedDials) {
        m_speeddialDataSet = speedDials;
    }

    public void setSettingDao(SettingDao settingDao) {
        m_settingDao = settingDao;
    }

    public void setSpeedDialManager(SpeedDialManager speedDialManager) {
        m_speedDialManager = speedDialManager;
    }
}
