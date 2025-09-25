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

import java.util.*;
import java.util.regex.Pattern;

import org.jivesoftware.openfire.group.DefaultGroupPropertyMap;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.util.PersistableMap;

import org.bson.Document;
import com.mongodb.client.MongoCollection;

public class MongoGroupPropertiesProvider extends BaseMongoProvider  {
    private static final String COLLECTION_NAME = "ofGroupProp";

    private static final String GPN_DISPLAY_NAME = "sharedRoster.displayName";
    private static final String GPN_SHOW_IN_ROSTER = "sharedRoster.showInRoster";

    private final GroupManager m_groupManager;

    public MongoGroupPropertiesProvider(GroupManager groupManager) {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> grpPropsCollection = getDefaultCollection();

        Document index = new Document()
            .append("groupname", 1)
            .append("name", 1);
        grpPropsCollection.createIndex(index);

        m_groupManager = groupManager;
    }

    public PersistableMap<String, String> loadProperties(Group group) {
        PersistableMap<String, String> grpProps = new DefaultGroupPropertyMap<>(group);
        MongoCollection<Document> grpPropsCollection = getDefaultCollection();

        Document query = new Document("groupname", group.getName());
        for (Document grpPropsObj : grpPropsCollection.find(query)) {
            String propName = (String) grpPropsObj.get("name");
            String propValue = (String) grpPropsObj.get("propValue");
            grpProps.put(propName, propValue, false);
        }

        if (grpProps.get(GPN_DISPLAY_NAME) == null) {
            grpProps.put(GPN_DISPLAY_NAME, group.getName(), false);
            insertProperty(group.getName(), GPN_DISPLAY_NAME, group.getName());
        }
        if (grpProps.get(GPN_SHOW_IN_ROSTER) == null) {
            grpProps.put(GPN_SHOW_IN_ROSTER, "onlyGroup", false);
            insertProperty(group.getName(), GPN_SHOW_IN_ROSTER, "onlyGroup");
        }

        return grpProps;
    }

    public void insertProperty(String groupName, String propName, String propValue) {
        MongoCollection<Document> grpPropsCollection = getDefaultCollection();
        Document toInsert = new Document()
            .append("groupname", groupName)
            .append("name", propName)
            .append("propValue", propValue);
        grpPropsCollection.insertOne(toInsert);
    }

    public void updateProperty(String groupName, String propName, String propValue) {
        // noop for now
    }

    public void deleteProperty(String groupName, String propName) {
        // noop for now
    }

    public boolean deleteGroupProperties(String groupName) {
        return true;
    }

    public Collection<String> getPublicSharedGroupNames() {
        return search(GPN_SHOW_IN_ROSTER, "everybody");
    }

    public Collection<String> getVisibleGroupNames(String userGroup) {
        MongoCollection<Document> grpPropsCollection = getDefaultCollection();

        Document query = new Document()
            .append("name", "sharedRoster.groupList")
            .append("propValue", Pattern.compile("\\.*" + userGroup + "\\.*"));

        Set<String> names = new HashSet<>();
        for (Document propObj : grpPropsCollection.find(query)) {
            names.add((String) propObj.get("groupName"));
        }
        return names;
    }

    public boolean setName(String oldName, String newName) throws GroupAlreadyExistsException {
        return true;
    }

    public Collection<String> search(String key, String value) {
        MongoCollection<Document> grpPropsCollection = getDefaultCollection();
        Document query = new Document()
            .append("name", key)
            .append("propValue", value);

        Set<String> names = new HashSet<>();
        for (Document propObj : grpPropsCollection.find(query)) {
            names.add((String) propObj.get("groupName"));
        }
        return names;
    }
}