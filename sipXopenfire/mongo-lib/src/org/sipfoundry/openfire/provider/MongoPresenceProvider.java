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

import java.util.Date;

import org.jivesoftware.openfire.provider.PresenceProvider;
import org.jivesoftware.util.StringUtils;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.ReplaceOptions;


public class MongoPresenceProvider extends BaseMongoProvider implements PresenceProvider {
    private static final String COLLECTION_NAME = "ofPresence";

    public MongoPresenceProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> presenceCollection = getDefaultCollection();

        Document index = new Document("username", 1);
        presenceCollection.createIndex(index);
    }

    @Override
    public void deleteOfflinePresenceFromDB(String username) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Document toRemove = new Document("username", username);

        presenceCollection.deleteMany(toRemove);
    }

    @Override
    public void insertOfflinePresenceIntoDB(String username, String offlinePresence, Date offlinePresenceDate) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Document query = new Document("username", username);

        Document updateDoc = new Document()
            .append("username", username)
            .append("offlinePresence", offlinePresence)
            .append("offlinePresenceDate", StringUtils.dateToMillis(offlinePresenceDate));

        ReplaceOptions options = new ReplaceOptions().upsert(true);

        presenceCollection.replaceOne(query, updateDoc, options);
    }

    @Override
    public TimePresence loadOfflinePresence(String username) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Document toFind = new Document("username", username);

        Document entry = presenceCollection.find(toFind).first();
        TimePresence tp;

        if (entry != null) {
            String lastActivity = (String) entry.get("offlinePresenceDate");
            String presence = (String) entry.get("offlinePresence");

            tp = new TimePresence(Long.valueOf(lastActivity), presence);
        } else {
            tp = new TimePresence(NULL_LONG, "NULL");
        }

        return tp;
    }

}
