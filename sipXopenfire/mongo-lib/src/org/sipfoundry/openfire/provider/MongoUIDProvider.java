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

import org.jivesoftware.openfire.provider.UIDProvider;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

public class MongoUIDProvider extends BaseMongoProvider implements UIDProvider {
    private static final String COLLECTION_NAME = "ofId";

    public MongoUIDProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> idCollection = getDefaultCollection();

        // Create an index on the "idType" field ascending
        idCollection.createIndex(Indexes.ascending("idType"));
    }

    @Override
    public long[] getNextBlock(int type, int blockSize) {
        long[] result = new long[2]; // we just return the min and max ids
        MongoCollection<Document> idCollection = getDefaultCollection();

        Document query = new Document("idType", type);

        Document existing = idCollection.find(query).first();

        if (existing != null && existing.get("id") != null) {
            result[0] = ((Number) existing.get("id")).longValue();
        } else {
            result[0] = 1L;
        }
        result[1] = result[0] + blockSize;

        Document update = new Document("$set", new Document("idType", type).append("id", result[1]));

        idCollection.updateOne(query, update, new UpdateOptions().upsert(true));

        return result;
    }

}
