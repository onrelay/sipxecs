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

import static org.sipfoundry.commons.mongo.MongoConstants.DESCR;
import static org.sipfoundry.commons.mongo.MongoConstants.GROUPS;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_GROUP;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.AbstractGroupProvider;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.xmpp.packet.JID;

import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

public class MongoGroupProvider extends AbstractGroupProvider {
    private static final Logger log = Logger.getLogger(MongoGroupProvider.class);

    private static final String COLLECTION_NAME = "entity";

    @Override
    public Group createGroup(String name) {
        log.debug("Creating group: " + name);
        Group g = null;

        try {
            g = getGroup(name);
        } catch (GroupNotFoundException e) {
            log.error("Group not find while trying to simulate a create [" + name + "]");
        }

        return g;
    }

    @Override
    public void deleteGroup(String name) {
        // groups are read-only, no further action needed
    }

    @Override
    public Group getGroup(String name) throws GroupNotFoundException {
        log.debug("Getting group: " + name);

        MongoCollection<Document> groupsCollection = getCollection();

        Document query = new Document()
            .append("ent", "group")
            .append("uid", name)
            .append("imgrp", "1");

        Document groupObj = groupsCollection.find(query).first();

        if (groupObj == null) {
            throw new GroupNotFoundException(name);
        }
        log.debug("Found group: " + groupObj);

        String id = (String) groupObj.get("_id");
        // initialize cache with the actual value of group - this method is called when openfire starts
        if (CacheHolder.getGroupName(id) == null) {
            CacheHolder.putGroup(id, name);
        }

        return fromDocument(groupObj);
    }

    @Override
    public void setName(String oldName, String newName) throws GroupAlreadyExistsException {
        // groups are read-only
    }

    @Override
    public void setDescription(String name, String description) throws GroupNotFoundException {
        // groups are read-only
    }

    @Override
    public int getGroupCount() {
        MongoCollection<Document> groupsCollection = getCollection();

        Document query = new Document()
            .append("ent", "group")
            .append("imgrp", "1");

        long count = groupsCollection.countDocuments(query);

        return (int) count;
    }

    @Override
    public Collection<String> getGroupNames() {
        return getGroupNames(0, Integer.MAX_VALUE);
    }

    @Override
    public Collection<String> getGroupNames(int startIndex, int numResults) {
        List<String> groupNames = new ArrayList<String>();
        MongoCollection<Document> groupsCollection = getCollection();

        Document query = new Document();

        query.put("ent", "group");
        query.put("imgrp", "1");

        for (Document groupObj : groupsCollection.find(query).skip(startIndex).limit(numResults)) {
            groupNames.add((String) groupObj.get("uid"));
        }

        return groupNames;
    }

    @Override
    public Collection<String> getGroupNames(JID user) {
        String jid = user.toBareJID();
        jid = jid.substring(0, jid.lastIndexOf("@"));
        List<String> names = UnfortunateLackOfSpringSupportFactory.getValidUsers().getImGroupnamesForUser(jid);
        return names;
    }

    @Override
    public void addMember(String groupName, JID user, boolean administrator) {
        // groups are read-only
    }

    @Override
    public void updateMember(String groupName, JID user, boolean administrator) {
        // groups are read-only
    }

    @Override
    public void deleteMember(String groupName, JID user) {
        // groups are read-only
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Collection<String> search(String query) {
        return search(query, 0, Integer.MAX_VALUE);
    }

    @Override
    public Collection<String> search(String query, int startIndex, int numResults) {
        Set<String> groupNames = new HashSet<String>();
        MongoCollection<Document> groupsCollection = getCollection();

        String asPattern = query.replaceAll("\\*", "\\.*");
        Pattern p = Pattern.compile(asPattern);
        Document queryObj = new Document();

        queryObj.put("ent", "group");
        queryObj.put("uid", p);

        for (Document groupObj : groupsCollection.find(queryObj).skip(startIndex).limit(numResults)) {
            groupNames.add((String) groupObj.get("uid"));
        }

        return groupNames;
    }

    @Override
    public Collection<String> search(String key, String value) {
        Set<String> groupNames = new HashSet<String>();
        MongoCollection<Document> groupsCollection = getCollection();

        Document queryObj = new Document();

        queryObj.put("ent", "group");
        queryObj.put(key, value);

        for (Document groupObj : groupsCollection.find(queryObj)) {
            groupNames.add((String) groupObj.get("uid"));
        }

        return groupNames;
    }

    @Override
    public boolean isSearchSupported() {
        return true;
    }

    @Override
    public boolean isSharingSupported() {
        return true;
    }

    private static Group fromDocument(Document groupObj) {
        Group g = null;

        if (groupObj != null) {
            String name = (String) groupObj.get("uid");
            String description = (String) groupObj.get(DESCR);
            boolean imBotEnabled = "1".equals(groupObj.get("imbot"));
            Collection<JID> members = getMembers(name);
            Collection<JID> administrators = Collections.emptyList();

            if (imBotEnabled) {
                Document imBotObj = getImBot(null);
                if (imBotObj != null) {
                    String imBotName = (String) imBotObj.get(IM_ID);
                    String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
                    JID imBotJid = new JID(imBotName, xmppDomain, null, false);
                    log.debug(String.format("ImBot JID: %s, domain: ", imBotJid.toBareJID(), imBotJid.getDomain()));
                    members.add(imBotJid);
                }
            }

            log.debug("fromDocument: for group " + name + " with members " + members);
            g = new Group(name, description, members, administrators);
        }

        return g;
    }

    private static Collection<JID> getMembers(String groupName) {
        Collection<JID> members = new ArrayList<JID>();
        MongoCollection<Document> usersCollection = getCollection();

        Document query = new Document();
        query.put("ent", "user");
        query.put(IM_ENABLED, true);
        query.put("gr", groupName);
        String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

        for (Document userObj : usersCollection.find(query)) {
            members.add(new JID((String) userObj.get(IM_ID) + "@" + domain));
        }

        return members;
    }

    private static Document getImBot(String name) {
        MongoCollection<Document> usersCollection = getUsersCollection();
        Document query = new Document()
            .append("ent", "imbotsettings")
            .append(IM_ENABLED, true);

        if (name != null) {
            query.put(IM_ID, name);
        }

        return usersCollection.find(query).first();
    }

    private static boolean isImBot(String name) {
        return getImBot(name) != null;
    }

    private static MongoCollection<Document> getUsersCollection() {
        // both users and groups are stored together
        return getCollection();
    }

    private static MongoCollection<Document> getCollection() {
        MongoDatabase db = UnfortunateLackOfSpringSupportFactory.getImdb();

        return db.getCollection(COLLECTION_NAME);
    }
}
