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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.user.UserProvider;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.xmpp.packet.JID;

import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

public class MongoUserProvider extends BaseMongoProvider implements UserProvider {
    public static final String SIP_UID = "sipUid";
    private static final Logger log = Logger.getLogger(MongoUserProvider.class);

    private static final String COLLECTION_NAME = "entity";

    public MongoUserProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
    }

    @Override
    public User loadUser(String username) throws UserNotFoundException {
        log.debug("load user: " + username);
        String actualUsername = username;
        if (username.contains("@")) {
            if (!XMPPServer.getInstance().isLocal(new JID(username))) {
                throw new UserNotFoundException("Cannot load user of remote server: " + username);
            }
            actualUsername = username.substring(0, username.lastIndexOf("@"));
        }

        MongoCollection<Document> userCollection = getDefaultCollection();
        Document query = new Document();

        query.put("ent", "user");
        query.put("imenbld", true);
        query.put("imid", actualUsername);

        Document userObj = userCollection.find(query).first();

        if (userObj == null) {
            // maybe it was looking for imbot
            query.put("ent", "imbotsettings");
            userObj = userCollection.find(query).first();
            log.debug("ImBot query: " + query);

            if (userObj == null) {
                throw new UserNotFoundException(username);
            }
        }
        String id = (String) userObj.get("_id");       
        //initialize cache with usernames found in db - this method is called when openfire starts
        if (CacheHolder.getUserName(id) == null) {
            CacheHolder.putUser(id, actualUsername);         
        }
        if (CacheHolder.getUserGroups(id) == null) {
            CacheHolder.putUserGroups(id, 
                GroupManager.getInstance().getProvider().getGroupNames(new JID(appendDomain(username))));
        }
        
        return fromDocument(userObj);
    }

    @Override
    public User createUser(String username, String password, String name, String email) {
        User u = null;

        try {
            u = loadUser(username);
        } catch (UserNotFoundException e) {
            log.error("User not find while trying to simulate a create [" + username + "]");
        }

        return u;
    }

    @Override
    public void deleteUser(String username) {
        CacheHolder.removeUserByName(username);
        // users are read-only, no further action needed
    }

    @Override
    public int getUserCount() {
        MongoCollection<Document> userCollection = getDefaultCollection();
        Document query = new Document();

        query.put("ent", "user");
        query.put("imenbld", true);

        return (int) userCollection.countDocuments(query);
    }

    @Override
    public Collection<User> getUsers() {
        return getUsers(0, Integer.MAX_VALUE);
    }

    @Override
    public Collection<String> getUsernames() {
        List<String> usernames = new ArrayList<String>();

        for (User u : getUsers()) {
            usernames.add(u.getName());
        }

        return usernames;
    }

    @Override
    public Collection<User> getUsers(int startIndex, int numResults) {
        List<User> users = new ArrayList<User>();
        MongoCollection<Document> userCollection = getDefaultCollection();
        Document query = new Document();

        query.put("ent", "user");
        query.put("imenbld", true);

        for (Document userObj : userCollection.find(query).skip(startIndex).limit(numResults)) {
            users.add(fromDocument(userObj));
        }

        return users;
    }

    @Override
    public void setName(String username, String name) throws UserNotFoundException {
        // user is read-only
    }

    @Override
    public void setEmail(String username, String email) throws UserNotFoundException {
        // user is read-only
    }

    @Override
    public void setCreationDate(String username, Date creationDate) throws UserNotFoundException {
        // user is read-only
    }

    @Override
    public void setModificationDate(String username, Date modificationDate) throws UserNotFoundException {
        // user is read-only
    }

    @Override
    public Set<String> getSearchFields() {
        return new LinkedHashSet<String>(Arrays.asList(ValidUsers.IM_USERNAME_FILTER, ValidUsers.IM_NAME_FILTER,
                ValidUsers.IM_EMAIL_FILTER));
    }

    @Override
    public Collection<User> findUsers(Set<String> fields, String query) {
        return findUsers(fields, query, 0, Integer.MAX_VALUE);
    }

    @Override
    public Collection<User> findUsers(Set<String> fields, String query, int startIndex, int numResults) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (!getSearchFields().containsAll(fields)) {
            throw new IllegalArgumentException("Search fields " + fields + " are not valid.");
        }

        String trimmedQuery = query.trim();

        List<Bson> orConditions = new ArrayList<>();

        if (fields.contains("Username")) {
            orConditions.add(Filters.eq(IM_ID, trimmedQuery));
            orConditions.add(Filters.eq(ALT_IM_ID, trimmedQuery));
        }
        if (fields.contains("Name")) {
            orConditions.add(Filters.eq(IM_DISPLAY_NAME, trimmedQuery));
        }
        if (fields.contains("Email")) {
            orConditions.add(Filters.eq(EMAIL, trimmedQuery));
        }

        if (orConditions.isEmpty()) {
            return Collections.emptyList();
        }

        Bson mongoQuery = Filters.or(orConditions);

        List<User> users = new ArrayList<>();
        MongoCollection<Document> userCollection = getDefaultCollection();

        for (Document userObj : userCollection.find(mongoQuery).skip(startIndex).limit(numResults)) {
            users.add(fromDocument(userObj));
        }

        return users;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isNameRequired() {
        return false;
    }

    @Override
    public boolean isEmailRequired() {
        return false;
    }

    private static User fromDocument(Document userObj) {
        User u = null;

        if (userObj != null) {
            String username = (String) userObj.get("imid");
            String sipId = (String) userObj.get(UID);
            String name = (String) userObj.get("imdn");
            String email = (String) userObj.get("email");
            Date creationDate = new Date();
            if (userObj.containsKey("creationDate")) {
                creationDate = new Date((Long) userObj.get("creationDate"));
            }
            Date modificationDate = new Date((Long) userObj.get("lastUpdated"));

            u = new User(username, name, email, creationDate, modificationDate);
            u.setNameVisible(true);
            u.getProperties().put(UID, defaultIfEmpty(sipId, ""));
            u.getProperties().put(SIP_UID, sipId);
        }
        log.debug(String.format("Found in mongo: %s %s %s", u.getUsername(), u.getName(), u.getUID()));
        return u;
    }

    
    private static String appendDomain(String userName) {
        if (userName.indexOf("@") == -1) {
            // No @ in the domain so assume this is our domain.
            return userName + "@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        }

        return userName;
    }    
}
