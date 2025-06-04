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
package org.sipfoundry.openfire.sync.listener;
import static org.sipfoundry.commons.mongo.MongoConstants.ID;
import org.sipfoundry.commons.mongo.MongoFactory;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.sipfoundry.openfire.sync.MongoOperation;
import org.sipfoundry.openfire.sync.job.AbstractJobFactory;
import org.sipfoundry.openfire.sync.job.Job;

import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

public abstract class MongoOplogListener<T extends AbstractJobFactory> implements Runnable {
    private static Logger logger = Logger.getLogger(MongoOplogListener.class);

    protected static final String RECORD = "o";
    protected static final String RECORD2 = "o2";
    protected static final String NAMESPACE = "ns";
    private static final String OPERATION = "op";
    private static final String TIMESTAMP = "ts";
    private static final String SET_OP = "$set";

    private T m_jobFactory;

    public void setJobFactory(T factory) {
        m_jobFactory = factory;
    }

    @Override
    public void run() {
        logger.debug("Running " + this.getClass());

        try {
            MongoClient client = MongoFactory.fromConnectionFile();

            MongoDatabase db = client.getDatabase("local");
            MongoCollection<Document> col = db.getCollection("oplog.rs");

            long seconds = System.currentTimeMillis() / 1000;

            Bson query = buildOpLogQuery();
            FindIterable<Document> cur = col.find(query).cursorType(CursorType.TailableAwait);

            for (Document object : cur) {
                try {
                    BSONTimestamp ts = object.get(TIMESTAMP, BSONTimestamp.class);
                    if (ts != null && ts.getTime() > seconds) {
                        logger.debug(String.format("Got operation: %s", object));
                        MongoOperation op = MongoOperation.fromString(object.getString(OPERATION));

                        if (getWatchedOperations().contains(op)) {
                            Document record = object.get(RECORD, Document.class);
                            Object id = record.get(ID);

                            if (id == null) {
                                // this is an update, the id is in a different place
                                Document otherRecord = object.get(RECORD2, Document.class);
                                id = otherRecord.get(ID);

                                Document partialUpdate = record.get(SET_OP, Document.class);
                                if (partialUpdate != null) {
                                    record = partialUpdate;
                                }
                            }

                            Job j = m_jobFactory.createJob(op, record, id);
                            if (j != null) {
                                j.process();
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Error processing change: " + object, ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error running mongo oplog listener", ex);
        }
    }

    protected abstract Bson buildOpLogQuery();

    protected abstract Collection<MongoOperation> getWatchedOperations();
}