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

import org.jivesoftware.openfire.server.RemoteServerConfiguration;
import org.jivesoftware.openfire.server.RemoteServerConfiguration.Permission;

import org.bson.Document;
import com.mongodb.client.MongoCollection;

public class MongoRemoteServerProvider extends BaseMongoProvider {
    private static final String COLLECTION_NAME = "ofRemoteServerConf";

    public MongoRemoteServerProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> rSrvCollection = getDefaultCollection();

        // Ensure an index exists on xmppDomain for faster lookups
        Document index = new Document("xmppDomain", 1);
        rSrvCollection.createIndex(index);
    }

    public void addConfiguration(RemoteServerConfiguration configuration) {
        MongoCollection<Document> rSrvCollection = getDefaultCollection();

        Document toInsert = new Document()
            .append("xmppDomain", configuration.getDomain())
            .append("remotePort", configuration.getRemotePort())
            .append("permission", configuration.getPermission().name());

        rSrvCollection.insertOne(toInsert);
    }

    public RemoteServerConfiguration getConfiguration(String domain) {
        MongoCollection<Document> rSrvCollection = getDefaultCollection();

        Document query = new Document("xmppDomain", domain);
        Document confObj = rSrvCollection.find(query).first();

        if (confObj != null) {
            Integer remote = confObj.getInteger("remotePort");
            String permission = confObj.getString("permission");

            RemoteServerConfiguration conf = new RemoteServerConfiguration(domain);
            conf.setPermission(Permission.valueOf(permission));
            conf.setRemotePort(remote);

            return conf;
        }

        return null;
    }

    public Collection<RemoteServerConfiguration> getConfigurations(Permission permission) {
        Collection<RemoteServerConfiguration> confs = new ArrayList<>();
        MongoCollection<Document> rSrvCollection = getDefaultCollection();

        Document query = new Document("permission", permission.name());

        for (Document confObj : rSrvCollection.find(query)) {
            String domain = confObj.getString("xmppDomain");
            Integer remote = confObj.getInteger("remotePort");

            RemoteServerConfiguration conf = new RemoteServerConfiguration(domain);
            conf.setPermission(permission);
            conf.setRemotePort(remote);
            confs.add(conf);
        }

        return confs;
    }

    public void deleteConfiguration(String domain) {
        MongoCollection<Document> rSrvCollection = getDefaultCollection();

        Document toDelete = new Document("xmppDomain", domain);
        rSrvCollection.deleteOne(toDelete);
    }
}