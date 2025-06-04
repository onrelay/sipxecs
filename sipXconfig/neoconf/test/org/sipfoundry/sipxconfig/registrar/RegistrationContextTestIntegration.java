/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.registrar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.RegistrationItem;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.test.ImdbTestCase;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.FindIterable;

public class RegistrationContextTestIntegration extends ImdbTestCase {
    private RegistrationContextImpl m_builder;
    private MongoTemplate m_nodeDb;
    private DomainManager m_mgr;
    private Calendar calendar = new GregorianCalendar(2015,6,2,13,24,30);

    private final Object[][] DATA = {
        {
            "063b4c2f5e11bf66a232762a7cf9e73a", "2395", "sip:3000@example.org",
            "\"John Doe\"<sip:john.doe@example.org;LINEID=f57f2117d5997f8d03d8395732f463f3>", true,
            "3000@example.org", calendar.getTime(), 1299762667, "0004f22aa38a", "1",
            "3f404b64-fc8490c3-6b14ac9a@192.168.2.21"
        },
        {
            "063b4c2f5e11bf66a232762a7cf9e73b", "2399", "sip:3001@example.org",
            "\"John Doe\"<sip:jane.doe@example.org>", false, "3001@example.org", calendar.getTime(), 1299762668,
            "0004f2a9b633", "2", "3f404b64-fc8490c3-6b14ac9a@192.168.2.19"
        }
    };

    private MongoCollection<Document> getRegistrarCollection() {
        return m_nodeDb.getDb().getCollection("registrar");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        Document reg1 = new Document();
        reg1.put("contact", DATA[0][3]);
        reg1.put("expirationTime", DATA[0][6]);
        reg1.put("uri", DATA[0][2]);
        reg1.put("instrument", DATA[0][8]);
        reg1.put("expired", DATA[0][4]);
        reg1.put("identity", DATA[0][5]);
        reg1.put("_id", DATA[0][9]);
        reg1.put("callId", DATA[0][10]);
        Document reg2 = new Document();
        reg2.put("contact", DATA[1][3]);
        reg2.put("expirationTime", DATA[1][6]);
        reg2.put("uri", DATA[1][2]);
        reg2.put("instrument", DATA[1][8]);
        reg2.put("expired", DATA[1][4]);
        reg2.put("identity", DATA[1][5]);
        reg2.put("_id", DATA[1][9]);
        reg2.put("callId", DATA[1][10]);

        m_nodeDb.getDb().drop();
        getRegistrarCollection().insertMany( Arrays.asList( reg1, reg2 ) );

        m_builder = new RegistrationContextImpl();
        m_builder.setNodedb(m_nodeDb);
        m_builder.setDomainManager(m_mgr);
    }

    public void testGetRegistrations() throws Exception {
        List<RegistrationItem> registrations = m_builder.getRegistrations();
        assertEquals(1, registrations.size());
        RegistrationItem ri = (RegistrationItem) registrations.get(0);
        assertEquals(calendar.getTime(), ri.getExpires());
        assertEquals("sip:3001@example.org", ri.getUri());
        assertTrue(ri.getContact().indexOf("Doe") > 0);
    }

    public void testGetRegistrationsByUser() throws Exception {
        List<RegistrationItem> registrations = m_builder.getRegistrations();
        User user = new User();
        user.setUserName("3001");
        registrations = m_builder.getRegistrationsByUser(user);
        assertEquals(1, registrations.size());
        RegistrationItem ri = registrations.get(0);
        assertEquals(calendar.getTime(), ri.getExpires());
        assertEquals("sip:3001@example.org", ri.getUri());
        assertTrue(ri.getContact().indexOf("Doe") > 0);
    }

    public void testGetRegistrationsByCallId() throws Exception {
        List<RegistrationItem> registrations = m_builder.getRegistrationsByCallId("3f404b64-fc8490c3-6b14ac9a@192.168.2.19");
        assertEquals(1, registrations.size());
        RegistrationItem ri = (RegistrationItem) registrations.get(0);
        assertEquals("0004f2a9b633", ri.getInstrument());
    }

    public void testGetRegistrationsByIp() throws Exception {
        List<RegistrationItem> registrations = m_builder.getRegistrationsByIp("192.168.2.19");
        assertEquals(1, registrations.size());
        RegistrationItem ri = (RegistrationItem) registrations.get(0);
        assertEquals("0004f2a9b633", ri.getInstrument());
    }

    public void testGetRegistrationsByMac() throws Exception {
        List<RegistrationItem> registrations = m_builder.getRegistrationsByMac("0004f2a9b633");
        assertEquals(1, registrations.size());
        RegistrationItem ri = (RegistrationItem) registrations.get(0);
        assertEquals("sip:3001@example.org", ri.getUri());
    }

    public void testGetCursorRegistrationsByMac() throws Exception {
        FindIterable<Document> registrations = m_builder.getMongoDbCursorRegistrationsByMac("0004f22aa38a");
        assertFalse(registrations.iterator().hasNext());

        registrations = m_builder.getMongoDbCursorRegistrationsByMac("0004f2a9b633");
        List<Document> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = registrations.iterator()) {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        }
        assertEquals(1, list.size());
    }

    public void testGetCursorRegistrationsByIp() throws Exception {
        FindIterable<Document> registrations = m_builder.getMongoDbCursorRegistrationsByIp("192.168.2.21");
        assertFalse(registrations.iterator().hasNext());

        registrations = m_builder.getMongoDbCursorRegistrationsByIp("192.168.2.19");
        List<Document> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = registrations.iterator()) {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        }
        assertEquals(1, list.size());
    }

    public void testGetCursorRegistrationsByUid() throws Exception {
        FindIterable<Document> registrations = m_builder.getMongoDbCursorRegistrationsByLineId("3000");
        assertFalse(registrations.iterator().hasNext());

        registrations = m_builder.getMongoDbCursorRegistrationsByLineId("3001");
        List<Document> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = registrations.iterator()) {
            while (cursor.hasNext()) {
                list.add(cursor.next());
            }
        }
        assertEquals(1, list.size());
    }

    public void setNodeDb(MongoTemplate nodeDb) {
        m_nodeDb = nodeDb;
    }

    @Override
    public void setDomainManager(DomainManager mgr) {
        m_mgr = mgr;
    }

}
