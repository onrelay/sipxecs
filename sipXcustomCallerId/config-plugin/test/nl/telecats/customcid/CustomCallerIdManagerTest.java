/*
 * Copyright (c) 2011 Telecats B.V. All rights reserved. Contributed to
 * SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 * This library or application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public
 * License (AGPL) as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library or application is distributed in the hope that it will be
 * useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License (AGPL) for more details.
 *
 * $
 */
package nl.telecats.customcid;

import static org.junit.Assert.assertEquals;
import static org.sipfoundry.sipxconfig.commserver.imdb.MongoTestCaseHelper.insertJson;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


public class CustomCallerIdManagerTest {
    private MongoTemplate m_db;
    private CustomCallerIdManagerImpl m_ccim;

    @Before
    public void onSetUp() throws UnknownHostException {
        Mongo m = new Mongo("127.0.0.1");
        m_db = new MongoTemplate(m, "test");
        m_db.getDb().dropDatabase();
        m_ccim = new CustomCallerIdManagerImpl();
        m_ccim.setDb(m_db);
    }

    @Test
    public void testDdiRead() {
        insertJson(getDdiCollection(), "{'from' : 'a', 'to' : 'b'}");
        Collection<CustomCallerAlias> actual = m_ccim.getDdiRewrites();
        Collection<CustomCallerAlias> expected = Arrays.asList(new CustomCallerAlias("a", "b"));
        assertEquals(expected, actual);
    }

    @Test
    public void testDdiWrite() {
        Collection<CustomCallerAlias> seed = Arrays.asList(new CustomCallerAlias("x", "y"));
        m_ccim.setDdiRewrites(seed);
        DBCursor csr = getDdiCollection().find();
        assertEquals(1, csr.count());
        DBObject actual = (DBObject) csr.next();
        assertEquals("x", actual.get("from"));
        assertEquals("y", actual.get("to"));
    }

    @Test
    public void testDbReadWrite() {
        insertJson(getDnisCollection(), "{'from' : 'c', 'to' : 'd'}");
        Collection<CustomCallerAlias> actual = m_ccim.getDdiRewrites();
        Collection<CustomCallerAlias> expected = Arrays.asList(new CustomCallerAlias("c", "d"));
        assertEquals(expected, actual);
    }

    private DBCollection getDdiCollection() {
        return m_db.getDb().getCollection(CustomCallerIdManagerImpl.DDI_COL);
    }

    private DBCollection getDnisCollection() {
        return m_db.getDb().getCollection(CustomCallerIdManagerImpl.DNIS_COL);
    }
}
