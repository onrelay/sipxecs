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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class CustomCallerIdManagerImpl implements CustomCallerIdManager {
    public static final String DNIS_COL = "cci_dnis";
    public static final String DDI_COL = "cci_ddi";
    public static final String FROM_ATTR = "from";
    public static final String TO_ATTR = "to";
    private MongoTemplate m_db;
    private BeanWithSettingsDao<CustomCallerIdSettings> m_settingsDao;

    @Override
    public CustomCallerIdSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(CustomCallerIdSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<CustomCallerAlias> getDnisRewrites() {
        return getRewrites(DNIS_COL);
    }

    @Override
    public Collection<CustomCallerAlias> getDdiRewrites() {
        return getRewrites(DDI_COL);
    }

    @Override
    public void setDdiRewrites(Collection<CustomCallerAlias> rewrites) {
        setRewrites(DDI_COL, rewrites);
    }

    private void setRewrites(String collection, Collection<CustomCallerAlias> rewrites) {
        DBCollection col = m_db.getDb().getCollection(collection);
        BasicDBObject[] dbo = new BasicDBObject[rewrites.size()];
        Iterator<CustomCallerAlias> iRewrites = rewrites.iterator();
        for (int i = 0; i < rewrites.size(); i++) {
            dbo[i] = new BasicDBObject();
            CustomCallerAlias cca = iRewrites.next();
            dbo[i].put(FROM_ATTR, cca.getFrom());
            dbo[i].put(TO_ATTR, cca.getTo());
        }
        col.drop();
        col.insert(dbo);
    }

    private Collection<CustomCallerAlias> getRewrites(String collection) {
        DBCursor csr = m_db.getDb().getCollection(collection).find();
        List<CustomCallerAlias> rw = new ArrayList<CustomCallerAlias>();
        while (csr.hasNext()) {
            DBObject o = csr.next();
            rw.add(new CustomCallerAlias(o.get(FROM_ATTR).toString(), o.get(TO_ATTR).toString()));
        }

        return rw;
    }

    public MongoTemplate getDb() {
        return m_db;
    }

    public void setDb(MongoTemplate db) {
        m_db = db;
    }

    @Override
    public void setDnisRewrites(Collection<CustomCallerAlias> rewrites) {
        setRewrites(DNIS_COL, rewrites);
    }

    public void setSettingsDao(BeanWithSettingsDao<CustomCallerIdSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
}
