/*
 * Copyright (C) 2026 OnRelay, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin.presence;

import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.sipfoundry.commons.mongo.MongoFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

/**
 * Persists unified presence into the shared imdb.unifiedPresence collection so
 * that other sipXecs components (most notably the registrar presence routing
 * plugin) can make decisions based on IM presence without calling into
 * openfire.
 *
 * Documents are keyed by the SIP identity (user@sipdomain). Openfire owns the
 * XMPP related fields of the document; other components may maintain
 * additional fields on the same document (for example sipState), which is why
 * updates only ever $set the openfire owned fields.
 *
 * The updated timestamp doubles as a freshness contract: PresenceUnifier
 * re-stamps every document it actively tracks at least every
 * PRESENCE_REFRESH_PERIOD_MILLIS, so readers must ignore documents with an
 * older timestamp - they are left-overs from a crashed or restarted openfire
 * and no longer reflect actual presence.
 */
public class MongoUnifiedPresenceWriter {

    public static final String COLLECTION_NAME = "unifiedPresence";
    public static final String SIP_STATE_UNDETERMINED = "UNDETERMINED";
    public static final long PRESENCE_REFRESH_PERIOD_MILLIS = 30000;

    private static final String DB_NAME = "imdb";
    private static final Logger log = Logger.getLogger(MongoUnifiedPresenceWriter.class);
    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    private final MongoCollection<Document> collection;

    public MongoUnifiedPresenceWriter() throws Exception {
        MongoClient mongoClient = MongoFactory.fromConnectionFile();
        collection = mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
    }

    public void write(String sipIdentity, String jid, UnifiedPresence unifiedPresence) {
        try {
            collection.updateOne(Filters.eq("_id", sipIdentity),
                    Updates.combine(
                            Updates.set("jid", jid),
                            Updates.set("xmppPresence", unifiedPresence.getXmppPresence().toString()),
                            Updates.set("xmppStatusMessage", unifiedPresence.getXmppStatusMessage()),
                            Updates.set("unifiedPresence", unifiedPresence.getUnifiedPresence()),
                            Updates.set("updated", new Date()),
                            Updates.setOnInsert("sipState", SIP_STATE_UNDETERMINED)),
                    UPSERT);
        } catch (Exception ex) {
            log.error("failed to persist unified presence for " + sipIdentity, ex);
        }
    }
}
