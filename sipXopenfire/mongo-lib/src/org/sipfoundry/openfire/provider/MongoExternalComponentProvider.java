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

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.openfire.component.ExternalComponentConfiguration;
import org.jivesoftware.openfire.component.ExternalComponentConfiguration.Permission;
import org.jivesoftware.openfire.component.ExternalComponentManager;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MongoExternalComponentProvider extends BaseMongoProvider {
    private static final String COLLECTION_NAME = "ofExtComponentConf";

    private ExternalComponentManager m_externalComponentManager;

    public MongoExternalComponentProvider(ExternalComponentManager externalComponentManager) {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> extCompCollection = getDefaultCollection();

        Document index = new Document("subdomain", 1);
        extCompCollection.createIndex(index);

        m_externalComponentManager = externalComponentManager;
    }

    public ExternalComponentConfiguration getConfiguration(String subdomain, boolean useWildcard) {
        ExternalComponentConfiguration conf = null;
        MongoCollection<Document> extCompCollection = getDefaultCollection();

        Document query = new Document();
        query.put("subdomain", subdomain);
        query.put("wildcard", false);

        Document confObj = extCompCollection.find(query).first();

        if (confObj != null) {
            String secret = (String) confObj.get("secret");
            String permission = (String) confObj.get("permission");
            conf = new ExternalComponentConfiguration(subdomain, false, Permission.valueOf(permission), secret);
        } else if (useWildcard) {
            confObj = extCompCollection.find(
                Filters.and(
                    Filters.regex("subdomain", subdomain),
                    Filters.eq("wildcard", false)
                )
            ).first();

            if (confObj != null) {
                String secret = (String) confObj.get("secret");
                String permission = (String) confObj.get("permission");
                conf = new ExternalComponentConfiguration(subdomain, false, Permission.valueOf(permission), secret);
            }
        }
        return conf;
    }

    public void addConfiguration(ExternalComponentConfiguration configuration) {
        MongoCollection<Document> extCompCollection = getDefaultCollection();
        Document toInsert = new Document();
        toInsert.put("subdomain", configuration.getSubdomain());
        toInsert.put("wildcard", configuration.isWildcard());
        toInsert.put("permission", configuration.getPermission().toString());
        toInsert.put("secret", configuration.getSecret());
        extCompCollection.insertOne(toInsert);
    }

    public Collection<ExternalComponentConfiguration> getConfigurations(Permission permission) {
        Collection<ExternalComponentConfiguration> confs = new ArrayList<>();
        MongoCollection<Document> extCompCollection = getDefaultCollection();

        Document query = new Document();
        query.put("permission", permission != null ? permission.toString() : null);

        for (Document confObj : extCompCollection.find(query)) {
            String subdomain = (String) confObj.get("subdomain");
            String secret = (String) confObj.get("secret");
            Boolean wildcard = (Boolean) confObj.get("wildcard");
            confs.add(new ExternalComponentConfiguration(subdomain, wildcard, permission, secret));
        }
        return confs;
    }

    public void deleteConfigurationFromDB(ExternalComponentConfiguration configuration) {
        MongoCollection<Document> extCompCollection = getDefaultCollection();
        Document toDelete = new Document();
        toDelete.put("subdomain", configuration.getSubdomain());
        toDelete.put("wildcard", configuration.isWildcard());
        extCompCollection.deleteOne(toDelete);
    }
}