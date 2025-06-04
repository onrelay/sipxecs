/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.openfire.provider;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.privacy.PrivacyList;
import org.jivesoftware.openfire.provider.PrivacyListProvider;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.FindIterable;


public class MongoPrivacyListProvider extends BaseMongoProvider implements PrivacyListProvider {
    private static final Logger log = Logger.getLogger(MongoPrivacyListProvider.class);
    private static final String COLLECTION_NAME = "ofPrivacyList";

    private static final int POOL_SIZE = 50;
    private static final long POOL_TIMEOUT_SECONDS = 30;

    /**
     * Pool of SAX Readers. SAXReader is not thread safe so we need to have a pool of readers.
     */
    private final BlockingQueue<SAXReader> m_xmlReaders = new LinkedBlockingQueue<SAXReader>(POOL_SIZE);

    public MongoPrivacyListProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        // Modern index creation using Indexes helper
        prvListCollection.createIndex(Indexes.ascending("username"));

        for (int i = 0; i < POOL_SIZE; i++) {
            SAXReader xmlReader = new SAXReader();
            xmlReader.setEncoding("UTF-8");
            m_xmlReaders.add(xmlReader);
        }
    }

    @Override
    public Map<String, Boolean> getPrivacyLists(String username) {
        Map<String, Boolean> privacyLists = new HashMap<>();
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Bson query = Filters.eq("username", username);
        Bson projection = Projections.include("name", "isDefault");

        FindIterable<Document> results = prvListCollection.find(query).projection(projection);
        for (Document dbObj : results) {
            String name = dbObj.getString("name");
            Boolean isDefault = dbObj.getBoolean("isDefault");

            privacyLists.put(name, isDefault);
        }

        return privacyLists;
    }

    @Override
    public PrivacyList loadPrivacyList(String username, String listName) {
        PrivacyList privacyList = null;
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Bson query = Filters.and(
            Filters.eq("username", username),
            Filters.eq("name", listName)
        );
        Bson projection = Projections.include("list", "isDefault");

        Document grpPropsObj = prvListCollection.find(query).projection(projection).first();

        if (grpPropsObj != null) {
            privacyList = buildPrivacyList(username, listName, grpPropsObj);
        }

        return privacyList;
    }

    @Override
    public PrivacyList loadDefaultPrivacyList(String username) {
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Bson query = Filters.and(
            Filters.eq("username", username),
            Filters.eq("isDefault", true)
        );
        Bson projection = Projections.include("list", "name");

        Document grpPropsObj = prvListCollection.find(query)
                                                .projection(projection)
                                                .first();

        PrivacyList privacyList = null;

        if (grpPropsObj != null) {
            privacyList = buildPrivacyList(username, null, grpPropsObj);
        }

        return privacyList;
    }

    @Override
    public void createPrivacyList(String username, PrivacyList list) {
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Document toInsert = new Document()
            .append("username", username)
            .append("name", list.getName())
            .append("isDefault", list.isDefault())
            .append("list", list.asElement().asXML());

        prvListCollection.insertOne(toInsert);
    }

    @Override
    public void updatePrivacyList(String username, PrivacyList list) {
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Document query = new Document()
            .append("username", username)
            .append("name", list.getName());

        Document update = new Document("$set", new Document()
            .append("isDefault", list.isDefault())
            .append("list", list.asElement().asXML()));

        prvListCollection.updateOne(query, update);
    }

    @Override
    public void deletePrivacyList(String username, String listName) {
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Document query = new Document()
            .append("username", username)
            .append("name", listName);

        prvListCollection.deleteOne(query);
    }

    @Override
    public void deletePrivacyLists(String username) {
        MongoCollection<Document> prvListCollection = getDefaultCollection();

        Document query = new Document("username", username);

        prvListCollection.deleteMany(query);
    }

    private PrivacyList buildPrivacyList(String username, String listName, Document dbObj) {
        PrivacyList privacyList = null;
        String list = (String) dbObj.get("list");
        Boolean isDefault = (Boolean) dbObj.get("isDefault");
        if (isDefault == null) {
            isDefault = false;
        }

        SAXReader xmlReader = null;
        try {
            // Get a sax reader from the pool
            xmlReader = m_xmlReaders.poll(POOL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Element listElement = xmlReader.read(new StringReader(list)).getRootElement();
            String actualListName = listName != null ? listName : (String) dbObj.get("name");
            log.debug("Creating privacy list for username=" + username + "; actualListName=" + actualListName + "; isDefault=" + isDefault + "; listElement=" + listElement);
            privacyList = new PrivacyList(username, actualListName, isDefault, listElement);
        } catch (Exception e) {
            log.error("Error reading privacy list", e);
        } finally {
            // Return the sax reader to the pool
            if (xmlReader != null) {
                m_xmlReaders.add(xmlReader);
            }
        }
        return privacyList;
    }
}
