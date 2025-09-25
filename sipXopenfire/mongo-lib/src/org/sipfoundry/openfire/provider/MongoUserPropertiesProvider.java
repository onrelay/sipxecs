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

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;

public class MongoUserPropertiesProvider extends BaseMongoProvider  {
    private static final String COLLECTION_NAME = "ofUserProp";

    public MongoUserPropertiesProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> usrPropsCollection = getDefaultCollection();

        usrPropsCollection.createIndex(Indexes.ascending("username", "name"));
    }

    public Map<String, String> loadProperties(String username) {
        Map<String, String> props = new HashMap<String, String>();
        MongoCollection<Document> usrPropsCollection = getDefaultCollection();
        Document query = new Document();

        query.put("username", username);

        for (Document usrPropObj : usrPropsCollection.find(query)) {
            String propName = (String) usrPropObj.get("name");
            String propValue = (String) usrPropObj.get("propValue");

            props.put(propName, propValue);
        }

        return props;
    }

    public void insertProperty(String username, String propName, String propValue) {
        // nothing to do
    }

    public String getPropertyValue(String username, String propName) {
        MongoCollection<Document> usrPropsCollection = getDefaultCollection();
        Document usrPropObj = getPropObject(usrPropsCollection, username, propName);
        String propValue = null;

        if (usrPropObj != null) {
            propValue = (String) usrPropObj.get("propValue");
        }

        return propValue;
    }

    public void updateProperty(String username, String propName, String propValue) {
        // nothing to do
    }

    public boolean deleteUserProperties(String username) {
        // nothing to do

        return true;
    }

    public void deleteProperty(String username, String propName) {
        // nothing to do
    }

    private static Document getPropObject(MongoCollection<Document> usrPropsCollection, String username, String propName) {
        return usrPropsCollection.find(
            Filters.and(
                Filters.eq("username", username),
                Filters.eq("name", propName)
            )
        ).first();
    }
}
