/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.commserver.imdb;

import java.util.ArrayList;
import java.util.List;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.test.ImdbTestCase;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;

public class UserStaticTestIntegration extends ImdbTestCase {
    private final String[][] USER_DATA = {
        {
            "0", "first1", "last1", "8809", "63948809"
        }, {
            "1", "first2", "last2", "8810", "63948810"
        }, {
            "2", "first3", "last3", "8811", "63948811"
        }, {
            "3", "first4", "last4", "8812", "test@mwi.com"
        },
    };

    private List<User> m_users;

    @Override
    public void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        m_users = new ArrayList<User>();
        for (String[] ud : USER_DATA) {
            User user = getCoreContext().newUser();

            user.setUniqueId(Integer.valueOf(ud[0]));
            user.setFirstName(ud[1]);
            user.setLastName(ud[2]);
            user.setUserName(ud[3]);
            user.setSettingValue("voicemail/mailbox/external-mwi", ud[4]);
            m_users.add(user);
        }
    }

    public void testGenerate() throws Exception {
        getReplicationManager().replicateEntity(m_users.get(0), DataSet.USER_STATIC);
        getReplicationManager().replicateEntity(m_users.get(1), DataSet.USER_STATIC);
        getReplicationManager().replicateEntity(m_users.get(2), DataSet.USER_STATIC);
        getReplicationManager().replicateEntity(m_users.get(3), DataSet.USER_STATIC);

        MongoCollection<Document> collection = getEntityCollection();

        assertEquals(4, collection.countDocuments());

        // Check specific fields for User0
        String contactUri = "sip:" + USER_DATA[0][4] + "@" + DOMAIN;
        String toUri = "sip:" + USER_DATA[0][3] + "@" + DOMAIN;
        Document docUser0 = collection.find(Filters.eq("_id", "User0")).first();
        assertNotNull(docUser0);
        assertEquals(contactUri, getNestedField(docUser0, MongoConstants.STATIC + "." + MongoConstants.CONTACT));
        assertEquals(toUri, getNestedField(docUser0, MongoConstants.STATIC + "." + MongoConstants.TO_URI));

        // Check specific fields for User1
        Document docUser1 = collection.find(Filters.eq("_id", "User1")).first();
        assertNotNull(docUser1);
        assertEquals("message-summary", getNestedField(docUser1, MongoConstants.STATIC + "." + MongoConstants.EVENT));

        // Check specific fields for User2
        Document docUser2 = collection.find(Filters.eq("_id", "User2")).first();
        assertNotNull(docUser2);
        assertEquals("sip:IVR@" + DOMAIN, getNestedField(docUser2, MongoConstants.STATIC + "." + MongoConstants.FROM_URI));

        // Check specific fields for User3
        Document docUser3 = collection.find(Filters.eq("_id", "User3")).first();
        assertNotNull(docUser3);
        assertEquals("sip:test@mwi.com", getNestedField(docUser3, MongoConstants.STATIC + "." + MongoConstants.CONTACT));
    }

    private Object getNestedField(Document doc, String dotNotationKey) {
        String[] keys = dotNotationKey.split("\\.");
        Object current = doc;
        for (String key : keys) {
            if (!(current instanceof Document)) return null;
            current = ((Document) current).get(key);
        }
        return current;
    }
}
