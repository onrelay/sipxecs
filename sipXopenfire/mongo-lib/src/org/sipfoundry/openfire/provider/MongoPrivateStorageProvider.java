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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

public class MongoPrivateStorageProvider extends BaseMongoProvider {
    private static final Logger log = Logger.getLogger(MongoPrivateStorageProvider.class);

    private static final String COLLECTION_NAME = "ofPrivate";

    public MongoPrivateStorageProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> prvStorageCollection = getDefaultCollection();

        // Modern index creation
        prvStorageCollection.createIndex(Indexes.ascending("username", "namespace", "name"));
    }

    public void add(String username, Element data) {
        log.debug(String.format("Writing private data for %s", username));
        try {
            StringWriter writer = new StringWriter();
            data.write(writer);
            log.debug(String.format("Writing private data %s", writer.toString()));

            MongoCollection<Document> prvStorageCollection = getDefaultCollection();
            Document query = new Document()
                .append("username", username)
                .append("namespace", data.getNamespaceURI());

            Document existing = prvStorageCollection.find(query).first();

            if (existing == null) {
                log.debug("new data");
                Document toInsert = new Document()
                    .append("username", username)
                    .append("namespace", data.getNamespaceURI())
                    .append("name", data.getName())
                    .append("privateData", writer.toString());
                prvStorageCollection.insertOne(toInsert);
            } else {
                log.debug("existing data");
                Document updated = new Document(existing)
                    .append("name", data.getName())
                    .append("privateData", writer.toString());
                prvStorageCollection.replaceOne(query, updated);
            }
        } catch (IOException e) {
            log.error("Error storing data: " + e.getMessage(), e);
        }
    }

    public Element get(String username, Element data, SAXReader reader) {
        MongoCollection<Document> prvStorageCollection = getDefaultCollection();
        Document query = new Document()
            .append("username", username)
            .append("namespace", data.getNamespaceURI());

        Element result = data;
        log.debug(String.format("Retrieving data for user %s and namespace %s", username, data.getNamespaceURI()));

        Document existing = prvStorageCollection.find(query).first();
        if (existing != null) {
            String prvData = ((String) existing.get("privateData")).trim();
            try {
                org.dom4j.Document doc = reader.read(new StringReader(prvData));
                result = doc.getRootElement();
            } catch (Exception e) {
                log.error("Error retrieving data: " + e.getMessage(), e);
            }
        }

        log.debug(String.format("Found data: %s", result.asXML()));
        return result;
    }

    public void userDeleting(String username) {
        MongoCollection<Document> prvStorageCollection = getDefaultCollection();
        Document toDelete = new Document().append("username", username);
        prvStorageCollection.deleteOne(toDelete);
    }

    public void userDeleting(org.jivesoftware.openfire.user.User user, Map<String, Object> params) {
        userDeleting(user.getUsername());
    }
}