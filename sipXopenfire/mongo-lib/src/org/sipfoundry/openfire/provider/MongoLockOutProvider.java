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

import org.bson.BasicBSONObject;
import org.jivesoftware.openfire.lockout.LockOutFlag;
import org.jivesoftware.openfire.provider.LockOutProvider;
import org.jivesoftware.util.StringUtils;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Indexes;

public class MongoLockOutProvider extends BaseMongoProvider implements LockOutProvider {
    private static final String COLLECTION_NAME = "ofUserFlag";

    public MongoLockOutProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> lockoutCollection = getDefaultCollection();

        // Create a compound index on "username" and "name"
        lockoutCollection.createIndex(Indexes.compoundIndex(
            Indexes.ascending("username"),
            Indexes.ascending("name")
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LockOutFlag getDisabledStatus(String username) {
        LockOutFlag flag = null;
        MongoCollection<Document> lockoutCollection = getDefaultCollection();
        Document query = new Document()
            .append("username", username)
            .append("name", "lockout");

        Document line = lockoutCollection.find(query).first();

        if (line != null) {
            long start = line.getLong("startTime");
            long end = line.getLong("endTime");
            flag = new LockOutFlag(username, new Date(start), new Date(end));
        }

        return flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisabledStatus(LockOutFlag flag) {
        MongoCollection<Document> lockoutCollection = getDefaultCollection();
        Document toInsert = new Document();

        toInsert.put("username", flag.getUsername());
        toInsert.put("name", "lockout");
        toInsert.put("startTime", StringUtils.dateToMillis(flag.getStartTime()));
        toInsert.put("endTime", StringUtils.dateToMillis(flag.getEndTime()));

        lockoutCollection.insertOne(toInsert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsetDisabledStatus(String username) {
        MongoCollection<Document> lockoutCollection = getDefaultCollection();
        Document toDelete = new Document();

        toDelete.put("username", username);

        lockoutCollection.deleteOne(toDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDelayedStartSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTimeoutSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldNotBeCached() {
        return false;
    }
}
