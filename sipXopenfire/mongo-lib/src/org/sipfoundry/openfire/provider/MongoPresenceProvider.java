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

import org.bson.Document;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Presence;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.IndexOptions;

/**
 * MongoDB-backed storage for offline presence.
 * Automatically persists offline presence when users go offline.
 */
public class MongoPresenceProvider extends BaseMongoProvider {

    private static final String COLLECTION_NAME = "ofPresence";

    public static final class TimePresence {
        private final long lastActivity;
        private final String presence;

        public TimePresence(long lastActivity, String presence) {
            this.lastActivity = lastActivity;
            this.presence = presence;
        }

        public long getLastActivity() { return lastActivity; }
        public String getPresence() { return presence; }

        @Override
        public String toString() {
            return "TimePresence{lastActivity=" + lastActivity + ", presence='" + presence + "'}";
        }
    }

    public MongoPresenceProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> presenceCollection = getDefaultCollection();

        // Ensure index on username (unique per user)
        presenceCollection.createIndex(new Document("username", 1), new IndexOptions().unique(true));

    }

    public void deleteOfflinePresenceFromDB(String username) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Bson query = new Document("username", username);
        presenceCollection.deleteMany(query);
    }

    public void insertOfflinePresenceIntoDB(String username, String offlinePresence, Date offlinePresenceDate) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Document query = new Document("username", username);

        Document updateDoc = new Document()
            .append("username", username)
            .append("offlinePresence", offlinePresence)
            .append("offlinePresenceDate", offlinePresenceDate.getTime());

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        presenceCollection.replaceOne(query, updateDoc, options);
    }

    public TimePresence loadOfflinePresence(String username) {
        MongoCollection<Document> presenceCollection = getDefaultCollection();
        Document query = new Document("username", username);

        Document entry = presenceCollection.find(query).first();
        if (entry != null) {
            Object lastActivityObj = entry.get("offlinePresenceDate");
            long lastActivity = 0L;
            if (lastActivityObj instanceof Number) {
                lastActivity = ((Number) lastActivityObj).longValue();
            } else if (lastActivityObj != null) {
                lastActivity = Long.parseLong(lastActivityObj.toString());
            }

            String presence = entry.getString("offlinePresence");
            return new TimePresence(lastActivity, presence);
        } else {
            return null;
        }
    }
}