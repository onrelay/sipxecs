/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.commons.userdb;

import static org.sipfoundry.commons.mongo.MongoConstants.ACTIVEGREETING;
import static org.sipfoundry.commons.mongo.MongoConstants.ALIAS;
import static org.sipfoundry.commons.mongo.MongoConstants.ALIASES;
import static org.sipfoundry.commons.mongo.MongoConstants.ALIAS_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_ATTACH_AUDIO;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_NOTIFICATION;
import static org.sipfoundry.commons.mongo.MongoConstants.ATTACH_AUDIO;
import static org.sipfoundry.commons.mongo.MongoConstants.AUTO_ENTER_PIN_EXTENSION;
import static org.sipfoundry.commons.mongo.MongoConstants.AUTO_ENTER_PIN_EXTERNAL;
import static org.sipfoundry.commons.mongo.MongoConstants.AVATAR;
import static org.sipfoundry.commons.mongo.MongoConstants.BUTTONS;
import static org.sipfoundry.commons.mongo.MongoConstants.CALL_FROM_ANY_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CALL_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CELL_PHONE_NUMBER;
import static org.sipfoundry.commons.mongo.MongoConstants.COMPANY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_ENTRY_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_EXIT_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_EXT;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_OWNER;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_PIN;
import static org.sipfoundry.commons.mongo.MongoConstants.CONTACT;
import static org.sipfoundry.commons.mongo.MongoConstants.DAYS_TO_KEEP_VM;
import static org.sipfoundry.commons.mongo.MongoConstants.DESCR;
import static org.sipfoundry.commons.mongo.MongoConstants.DIALPAD;
import static org.sipfoundry.commons.mongo.MongoConstants.DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.DISTRIB_LISTS;
import static org.sipfoundry.commons.mongo.MongoConstants.EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.ENTITY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.FAX_NUMBER;
import static org.sipfoundry.commons.mongo.MongoConstants.FORCE_PIN_CHANGE;
import static org.sipfoundry.commons.mongo.MongoConstants.FORWARD_DELETE_VOICEMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.GROUPS;
import static org.sipfoundry.commons.mongo.MongoConstants.HASHED_PASSTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_CITY;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_COUNTRY;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_PHONE_NUMBER;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_STATE;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_STREET;
import static org.sipfoundry.commons.mongo.MongoConstants.HOME_ZIP;
import static org.sipfoundry.commons.mongo.MongoConstants.HOTELING;
import static org.sipfoundry.commons.mongo.MongoConstants.ID;
import static org.sipfoundry.commons.mongo.MongoConstants.IDENTITY;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ADVERTISE_ON_CALL_STATUS;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_GROUP;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ON_THE_PHONE_MESSAGE;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_SHOW_ON_CALL_DETAILS;
import static org.sipfoundry.commons.mongo.MongoConstants.ITEM;
import static org.sipfoundry.commons.mongo.MongoConstants.JOB_DEPT;
import static org.sipfoundry.commons.mongo.MongoConstants.JOB_TITLE;
import static org.sipfoundry.commons.mongo.MongoConstants.LANGUAGE;
import static org.sipfoundry.commons.mongo.MongoConstants.LEAVE_MESSAGE_BEGIN_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.LEAVE_MESSAGE_END_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.MOH;
import static org.sipfoundry.commons.mongo.MongoConstants.MY_BUDDY_GROUP;
import static org.sipfoundry.commons.mongo.MongoConstants.NOTIFICATION;
import static org.sipfoundry.commons.mongo.MongoConstants.OFFICE_CITY;
import static org.sipfoundry.commons.mongo.MongoConstants.OFFICE_COUNTRY;
import static org.sipfoundry.commons.mongo.MongoConstants.OFFICE_STATE;
import static org.sipfoundry.commons.mongo.MongoConstants.OFFICE_STREET;
import static org.sipfoundry.commons.mongo.MongoConstants.OFFICE_ZIP;
import static org.sipfoundry.commons.mongo.MongoConstants.OPERATOR;
import static org.sipfoundry.commons.mongo.MongoConstants.PASSTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.PERMISSIONS;
import static org.sipfoundry.commons.mongo.MongoConstants.PERSONAL_ATT;
import static org.sipfoundry.commons.mongo.MongoConstants.PINTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.PLAY_DEFAULT_VM;
import static org.sipfoundry.commons.mongo.MongoConstants.RELATION;
import static org.sipfoundry.commons.mongo.MongoConstants.SPEEDDIAL;
import static org.sipfoundry.commons.mongo.MongoConstants.TIMEZONE;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;
import static org.sipfoundry.commons.mongo.MongoConstants.UNIFIED_MESSAGING_LANGUAGE;
import static org.sipfoundry.commons.mongo.MongoConstants.USERBUSYPROMPT;
import static org.sipfoundry.commons.mongo.MongoConstants.USER_LOCATION;
import static org.sipfoundry.commons.mongo.MongoConstants.VALID_USER;
import static org.sipfoundry.commons.mongo.MongoConstants.VOICEMAILTUI;
import static org.sipfoundry.commons.mongo.MongoConstants.VOICEMAIL_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.VOICEMAIL_PINTOKEN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;

import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;

/**
 * Holds the valid user data needed for the AutoAttendant, parsing from mongo db imdb
 *
 */
public class ValidUsers {
    public static String IM_USERNAME_FILTER = "Username";
    public static String IM_NAME_FILTER = "Name";
    public static String IM_EMAIL_FILTER = "Email";
    // Mapping of letters to DTMF numbers.
    // Position of letter in letters maps to corresponding position in numbers
    private static String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static String NUMBERS = "22233344455566677778889999";
    private static final String IMDB_PERM_AA = "AutoAttendant";
    private static final String IMDB_PERM_VOICEMAIL = "Voicemail";
    private static final String IMDB_PERM_RECPROMPTS = "RecordSystemPrompts";
    private static final String IMDB_PERM_TUICHANGEPIN = "tui-change-pin";
    private static final String IMDB_PERM_ADMIN = "superadmin";
    private static final String ENTITY_NAME_USER = "user";
    private static final String ENTITY_NAME_GROUP = "group";
    private static final String ENTITY_NAME_IMBOTSETTINGS = "imbotsettings";

    private MongoDatabase m_imdb;

    /**
     * Loading all users into memory is an extremely expensive call for large systems (10K-50K
     * user system). Consider refactoring your code to not call this method.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<User> getValidUsers() {
        List<User> users = new ArrayList<User>();
        try {
            FindIterable<Document> validUsers = getEntityCollection().find(Filters.eq(VALID_USER, true));
            for (Document validUser : validUsers ) {
                if (!validUser.get(ID).toString().startsWith("User")) {
                    List<Document> aliasesObj = (List<Document>) validUser.get(ALIASES);
                    if (aliasesObj != null) {
                        for (int i = 0; i < aliasesObj.size(); i++) {
                            Document aliasObj = (Document) aliasesObj.get(i);
                            users.add(extractValidUserFromAlias(aliasObj));
                        }
                    }
                } else {
                    users.add(extractValidUser(validUser));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Loading all users into memory is an extremely expensive call for large systems (10K-50K
     * user system). Consider refactoring your code to not call this method.
     *
     * @return
     */
    public FindIterable<Document> getUsers() {
        Document query = new Document(ENTITY_NAME, ENTITY_NAME_USER);
        FindIterable<Document> cursor = getEntityCollection().find(query);
        return cursor;
    }

    /**
     * Find a list of users Documents based on a search query. The Document will have only
     * the fields specified by projection.
     * @param query
     * @param projection
     * @return
     */
    public List<Document> getUsers(Document query, Document projection) {
        return getEntityCollection().find(query).projection(projection).into(new ArrayList<>());
    }

    public FindIterable<Document> getUsersWithSpeedDial() {
        return getEntityCollection().find(
            Filters.and(
                Filters.eq(ENTITY_NAME, ENTITY_NAME_USER),
                Filters.or(Filters.exists(SPEEDDIAL, true), Filters.eq(IM_ENABLED, true))
            )
        );
    }

    /**
     * Use this method if you need to remove a field from users completely. This may be achieved
     * by regenerating the entire collection, or a DataSet, but this would be much faster.
     *
     * @param field
     */
    public void removeFieldFromUsers(String field) {
        getEntityCollection().updateMany(
            new Document(), 
            Updates.unset(field)
        );
    }

    /**
     * Loading all users into memory is an extremely expensive call for large systems (10K-50K
     * user system). Consider refactoring your code to not call this method.
     *
     * @return
     */
    public List<User> getUsersWithImEnabled() {

        List<User> users = new ArrayList<>();
    
        FindIterable<Document> userDocs = getEntityCollection().find(Filters.eq(IM_ENABLED, true));
    
        for (Document userDoc : userDocs ) {
            users.add(extractUser(userDoc));
        }
    
        return users;
    }

    /**
     * Returns a list of all im ids (of users with im enabled)
     *
     * @return
     */
    public List<String> getAllImIdsInGroup(String group) {

        List<String> imIds = new ArrayList<>();
    
        FindIterable<Document> userDocs = getEntityCollection().find(
            Filters.and(Filters.eq(GROUPS, group), Filters.eq(IM_ENABLED, true))
        );
    
        for (Document userDoc : userDocs) {
            Object imId = userDoc.get(IM_ID);
            if (imId != null) {
                imIds.add(imId.toString());
            }
        }
        return imIds;
    }

    /**
     * See if a given user_name is valid (aka it can be dialed and reach a user)
     *
     * @param userNname
     *
     * @return user found or null
     */
    public User getUser(String userName) {
        if (userName == null) {
            return null;
        }
        
        // Primary query: Search by UID with VALID_USER = true
        Document result = getEntityCollection().find(
                Filters.and(Filters.eq(VALID_USER, true), Filters.eq(UID, userName))
        ).first();
    
        if (result != null) {
            return extractValidUser(result);
        }
    
        // Secondary query: Search in aliases
        Document aliasResult = getEntityCollection().find(
                Filters.and(Filters.eq(VALID_USER, true), Filters.elemMatch(ALIASES, Filters.eq(ALIAS_ID, userName)))
        ).first();
    
        if (aliasResult == null) {
            return null;
        }
    
        // If ID does not start with "User", check aliases explicitly
        if (!aliasResult.get(ID).toString().startsWith("User")) {
            List<Document> aliases = aliasResult.getList(ALIASES, Document.class);
            for (Document aliasObj : aliases) {
                if (userName.equals(getStringValue(aliasObj, ALIAS_ID))) {
                    return extractValidUserFromAlias(aliasObj);
                }
            }
        }
    
        return extractValidUser(aliasResult);
    }

    /**
     * Retrieve a specific user based on their Cell Phone or Home Phone (as defined on user's contact information)
     * which has the Auto Enter Pin from External Number permission set to true. <br>
     * If the method finds more than 1 user who share the same external number it will return null;
     */

    public User getUserWithAutoEnterPinByExternalNumber(String externalNumber, int matchLastDigits) {
        if (externalNumber == null) {
            return null;
        }
        
        // Get the last N digits of the external number
        externalNumber = getExternalNumberLastDigits(externalNumber, matchLastDigits);
    
        // Create regex pattern for partial matching
        Pattern cellPattern = Pattern.compile(".*" + externalNumber);
    
        // Build query using Filters
        Bson query = Filters.and(
                Filters.eq(VALID_USER, true),
                Filters.or(
                        Filters.regex(CELL_PHONE_NUMBER, cellPattern),
                        Filters.regex(HOME_PHONE_NUMBER, cellPattern)
                ),
                Filters.eq(AUTO_ENTER_PIN_EXTERNAL, "1")
        );
    
        Document result = getEntityCollection().find(query).first();
        return (result != null) ? extractValidUser(result) : null;
    }

    private String getExternalNumberLastDigits(String externalNumber, int matchLastDigits) {
        String externalNumberToMatch;
        if (matchLastDigits == 0 || externalNumber == null || externalNumber.length() < matchLastDigits) {
            externalNumberToMatch = externalNumber;
        } else {
            externalNumberToMatch = externalNumber.substring(externalNumber.length() - matchLastDigits);
        }
        return externalNumberToMatch;
    }

    public User getUserByConferenceName(String conferenceName) {
    
        // Build query using Filters
        Document conferenceResult = getEntityCollection().find(
            Filters.eq(CONF_NAME, conferenceName)).first();
    
        if (conferenceResult != null) {
            String owner = getStringValue(conferenceResult, CONF_OWNER);
            if (owner != null) {
                User user = getUser(owner);
                addConference(user, conferenceResult);
                return user;
            }
        }
        return null;
    }

    public User getUserByJid(String jid) {
        return getUserByJidObject(jid);
    }

    public User getUserByInsensitiveJid(String jid) {
        return getUserByJidObject(jid);
    }

    // We might rarely need to search through alternate id. "or" query proved to be pretty heavy
    // especially on systems with many users, so we might want to limit those if possible
    private User getUserByJidObject(Object jid) {
    
        // First attempt: Find by IM_ID
        Document jidResult = getEntityCollection().find(Filters.eq(IM_ID, jid)).first();
        User user = extractValidUser(jidResult);
    
        // If not found, attempt ALT_IM_ID
        if (user == null) {
            jidResult = getEntityCollection().find(Filters.eq(ALT_IM_ID, jid)).first();
            user = extractValidUser(jidResult);
        }
    
        // If user is found, check for associated conference
        if (user != null) {
            Document conferenceResult = getEntityCollection().find(Filters.eq(CONF_OWNER, user.getUserName())).first();
            if (conferenceResult != null) {
                addConference(user, conferenceResult);
            }
        }
    
        return user;
    }

    public List<User> getUsersUpdatedAfter(Long ms) {
    
        List<User> users = new ArrayList<>();

        FindIterable<Document> userDocs = getEntityCollection().find(
            Filters.and(
                Filters.eq(ENTITY_NAME, ENTITY_NAME_USER),
                Filters.gt(MongoConstants.TIMESTAMP, ms)
            )
        );
    
        for (Document userDoc : userDocs ) {
            users.add(extractUser(userDoc));
        }
    
        return users;
    }

    public FindIterable<Document> getEntitiesWithPermissions() {
        return getEntityCollection().find(Filters.exists(PERMISSIONS, true));
    }
    
    public FindIterable<Document> getEntitiesWithPermission(String name) {
        return getEntityCollection().find(
            Filters.and(
                Filters.exists(PERMISSIONS, true),
                Filters.eq(PERMISSIONS, name)
            )
        );
    }

    public FindIterable<Document> getUsersInBranch(String name) {
        return getEntityCollection().find(Filters.eq(USER_LOCATION, name));
    }
    
    public FindIterable<Document> getUsersInGroup(String name) {
        return getEntityCollection().find(
            Filters.and(
                Filters.eq(ENTITY_NAME, ENTITY_NAME_USER),
                Filters.eq(GROUPS, name)
            )
        );
    }

    private static void addConference(User user, Document conference) {
        user.setConfName(getStringValue(conference, CONF_NAME));
        user.setConfNum(getStringValue(conference, CONF_EXT));
        user.setConfPin(getStringValue(conference, CONF_PIN));
    }

    /**
     * Given a bunch of DTMF digits, return the list of users that matches
     *
     * @param digits DTMF digits to match against user directory
     * @param onlyVoicemailUsers limit match to users in directory who have voicemail
     * @return a Vector of users that match
     */

     public List<User> lookupDTMF(String digits, boolean onlyVoicemailUsers, String groups) {
        List<User> matches = new ArrayList<>();
    
        // Permissions filter (corrected)
        List<Object> permList = Collections.singletonList(IMDB_PERM_AA);
        Bson inDirectory = Filters.all(PERMISSIONS, permList);
        Bson hasDisplayName = Filters.exists(DISPLAY_NAME, true);
        Bson validUserFilter = Filters.eq(VALID_USER, Boolean.TRUE);
    
        // Base query
        List<Bson> filters = new ArrayList<>();
        filters.add(inDirectory);
        filters.add(validUserFilter);
        filters.add(hasDisplayName);
    
        // Group filter (if applicable)
        if (!StringUtils.isBlank(groups)) {
            String[] searchGroups = StringUtils.split(groups, " ");
            Bson groupFilter = Filters.in(GROUPS, (Object[]) searchGroups);
            filters.add(groupFilter);
        }
    
        // Query execution
        FindIterable<Document> aliasResult = getEntityCollection().find(Filters.and(filters));
    
        // Processing results
        for (Document doc : aliasResult) {
            User user = extractValidUser(doc);
            if (user.getDialPatterns() != null) {
                for (String dialPattern : user.getDialPatterns()) {
                    if (dialPattern.startsWith(digits)) {
                        if (!onlyVoicemailUsers || user.hasVoicemail()) {
                            matches.add(user);
                            break;
                        }
                    }
                }
            }
        }
        return matches;
    }


    public long getImUsersCount() {
        return getEntityCollection().countDocuments(Filters.eq(IM_ENABLED, true));
    }

    public List<User> getImUsersByFilter(Set<String> fields, String query, int startIndex, int numResults) {
        List<Bson> orFilters = new ArrayList<>();
    
        if (fields.contains(IM_USERNAME_FILTER)) {
            orFilters.add(Filters.eq(IM_ID, query));
            orFilters.add(Filters.eq(ALT_IM_ID, query));
        }
        if (fields.contains(IM_NAME_FILTER)) {
            orFilters.add(Filters.eq(IM_DISPLAY_NAME, query));
        }
        if (fields.contains(IM_EMAIL_FILTER)) {
            orFilters.add(Filters.eq(EMAIL, query));
        }
    
        Bson finalFilter = orFilters.isEmpty() ? new Document() : Filters.or(orFilters);
    
        FindIterable<Document> cursor = getEntityCollection()
            .find(finalFilter)
            .skip(startIndex)
            .limit(numResults);
    
        List<User> users = new ArrayList<>();
        for (Document userDoc : cursor) {
            User imUser = extractUser(userDoc);
            if (imUser.isImEnabled()) {
                users.add(imUser);
            }
        }
    
        return users;
    }


    public Collection<String> getImUsernames(int startIndex, int numResults) {
        Bson filter = Filters.eq(IM_ENABLED, true);
        Bson projection = Projections.include(IM_ID);
    
        List<String> userNames = new ArrayList<>();
        FindIterable<Document> cursor = getEntityCollection()
            .find(filter)
            .projection(projection)
            .skip(startIndex)
            .limit(numResults);
    
        for (Document user : cursor) {
            String imUsername = getStringValue(user, IM_ID);
            if (StringUtils.isNotBlank(imUsername)) {
                userNames.add(imUsername);
            }
        }
    
        return userNames;
    }

    public UserGroup getImGroup(String name) {
        Bson filter = Filters.and(
            Filters.eq(IM_GROUP, "1"),
            Filters.eq(UID, name)
        );
    
        Document groupResult = getEntityCollection().find(filter).first();
        if (groupResult != null) {
            return convertUserGroup(groupResult);
        }
        return null;
    }

    public Collection<UserGroup> getImGroups() {
        Collection<UserGroup> groups = new ArrayList<>();
        Bson filter = Filters.eq(IM_GROUP, "1");
    
        FindIterable<Document> cursor = getEntityCollection().find(filter);
        for (Document groupResult : cursor) {
            if (groupResult != null) {
                groups.add(convertUserGroup(groupResult));
            }
        }
    
        return groups;
    }

    private static UserGroup convertUserGroup(Document groupResult) {
        UserGroup group = new UserGroup();
        group.setGroupName(getStringValue(groupResult, UID));
        group.setDescription(getStringValue(groupResult, DESCR));
        group.setSysId(getStringValue(groupResult, ID));
        String imBotEnabled = getStringValue(groupResult, MY_BUDDY_GROUP);
        if (StringUtils.equals(imBotEnabled, "1")) {
            group.setImBotEnabled(true);
        }
        return group;
    }

    public String getImBotName() {
        User imbotUser = getImbotUser();
        if (imbotUser != null) {
            return imbotUser.getUserName();
        }
        return null;
    }

    public User getImbotUser() {
        Bson filter = Filters.and(
            Filters.eq(ENTITY_NAME, ENTITY_NAME_IMBOTSETTINGS),
            Filters.eq(IM_ENABLED, true)
        );
    
        Document imbotResult = getEntityCollection().find(filter).first();
        if (imbotResult != null) {
            User imbotUser = new User();
            imbotUser.setUserName(getStringValue(imbotResult, IM_ID));
            imbotUser.setPintoken(getStringValue(imbotResult, PINTOKEN));
            return imbotUser;
        }
    
        return null;
    }

    public long getImGroupCount() {
        Bson filter = Filters.and(
            Filters.eq(ENTITY_NAME, ENTITY_NAME_GROUP),
            Filters.eq(IM_GROUP, "1")
        );

        return getEntityCollection().countDocuments(filter);
    }

    public Collection<String> getImGroupNames(int startIndex, int numResults) {
        Bson query = Filters.eq(IM_GROUP, "1");
        Bson projection = new Document(UID, 1); // Projection to retrieve only UID
    
        List<String> groupNames = new ArrayList<>();
        FindIterable<Document> cursor = getEntityCollection()
            .find(query)
            .projection(projection)
            .skip(startIndex)
            .limit(numResults);
    
        for (Document document : cursor) {
            
            String groupName = getStringValue(document, UID);
            if (StringUtils.isNotBlank(groupName)) {
                groupNames.add(groupName);
            }
        }
    
        return groupNames;
    }

    public List<String> getImGroupNameByQuery(String query, int startIndex, int numResults) {
        Pattern insensitiveQuery = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        Bson mongoQuery = Filters.and(
            Filters.eq(ENTITY_NAME, ENTITY_NAME_GROUP),
            Filters.regex(UID, insensitiveQuery)
        );
    
        List<String> groups = new ArrayList<>();
        FindIterable<Document> cursor = getEntityCollection()
            .find(mongoQuery)
            .skip(startIndex)
            .limit(numResults);
    
        for (Document document : cursor) {
            groups.add(getStringValue(document, UID));
        }
    
        return groups;
    }

    @SuppressWarnings("unchecked")
    public List<String> getImGroupnamesForUser(String jid) {
        List<String> names = new ArrayList<>();
        Collection<UserGroup> imGroups = getImGroups();
    
        // For imbot user, there is a different algorithm to return all groups where imbot is a member
        if (StringUtils.equals(jid, getImBotName())) {
            for (UserGroup group : imGroups) {
                if (group.isImbotEnabled()) {
                    names.add(group.getGroupName());
                }
            }
            return names;
        }
    
        Bson query = Filters.and(
            Filters.eq(IM_ENABLED, true),
            Filters.eq(IM_ID, jid)
        );
    
        Document user = getEntityCollection().find(query).first();
        if (user != null) {
            List<String> groupList = (List<String>) user.get(GROUPS);
            names.addAll(groupList);
        }
    
        List<String> imNames = new ArrayList<>();
        for (UserGroup group : imGroups) {
            imNames.add(group.getGroupName());
        }
    
        return new ArrayList<>(CollectionUtils.intersection(names, imNames));
    }

    public List<String> getImUsernamesInGroup(String groupName) {
        Bson query = Filters.and(
            Filters.eq(IM_ENABLED, true),
            Filters.in(GROUPS, groupName)
        );
    
        Bson projection = Projections.include(IM_ID);
    
        List<String> userNames = new ArrayList<>();
        FindIterable<Document> cursor = getEntityCollection().find(query).projection(projection);
    
        for (Document user : cursor) {
            String imUsername = getStringValue(user, IM_ID);
            if (StringUtils.isNotBlank(imUsername)) {
                userNames.add(imUsername);
            }
        }
    
        return userNames;
    }

    private MongoCollection<Document> getEntityCollection() {
        return getImdb().getCollection( MongoConstants.ENTITY_COLLECTION );
    }

    private static User extractValidUserFromAlias(Document aliasObj) {
        if (aliasObj == null) {
            return null;
        }
        User user = new User();
        String id = getStringValue(aliasObj, ALIAS_ID);
        user.setIdentity(id);
        user.setUserName(id);
        user.setUri(getStringValue(aliasObj, CONTACT));
        user.setInDirectory(false);
        return user;
    }

    private static User extractValidUser(Document obj) {
        if (obj == null) {
            return null;
        }
        if (!Boolean.valueOf(obj.get(VALID_USER).toString())) {
            return null;
        }
        return extractUser(obj);
    }

    @SuppressWarnings("unchecked")
    private static User extractUser(Document obj) {
        if (obj == null) {
            return null;
        }

        User user = new User();
        user.setSysId(getStringValue(obj, ID));
        user.setIdentity(getStringValue(obj, IDENTITY));
        user.setUserName(getStringValue(obj, UID));
        user.setDisplayName(getStringValue(obj, DISPLAY_NAME));
        user.setUri(getStringValue(obj, CONTACT));
        user.setPasstoken(getStringValue(obj, HASHED_PASSTOKEN));
        user.setSipPassword(getStringValue(obj, PASSTOKEN));
        user.setPintoken(getStringValue(obj, PINTOKEN));
        user.setVoicemailPintoken(getStringValue(obj, VOICEMAIL_PINTOKEN));
        user.setTimeZone(getStringValue(obj, TIMEZONE));
        String htl = getStringValue(obj, HOTELING);
        if (htl != null) {
            user.setHotelingEnabled(BooleanUtils.toBoolean(getStringValue(obj, HOTELING), "1", "0"));
        }

        List<String> permissions = (List<String>) obj.get(PERMISSIONS);
        if (permissions != null) {
            user.setInDirectory(permissions.contains(IMDB_PERM_AA));
            user.setHasVoicemail(permissions.contains(IMDB_PERM_VOICEMAIL));
            user.setCanRecordPrompts(permissions.contains(IMDB_PERM_RECPROMPTS));
            user.setCanTuiChangePin(permissions.contains(IMDB_PERM_TUICHANGEPIN));
            user.setAdmin(permissions.contains(IMDB_PERM_ADMIN));
        }

        user.setUserBusyPrompt(Boolean.valueOf(getStringValue(obj, USERBUSYPROMPT)));
        user.setMoh(getStringValue(obj, MOH));

        // highest weight group is always the last in the list
        List<Document> groups = (List<Document>) obj.get(GROUPS);
        if (groups != null) {
            Document lastGroup = groups.get(groups.size() - 1);
        	user.setHighestWeightGroup(getStringValue(lastGroup, UID));
        }

        user.setVoicemailTui(getStringValue(obj, VOICEMAILTUI));

        if (getStringValue(obj, FORWARD_DELETE_VOICEMAIL) != null) {
            user.setForwardDeleteVoicemail(getStringValue(obj, FORWARD_DELETE_VOICEMAIL));
        }

        user.setEmailAddress(getStringValue(obj, EMAIL));
        if (obj.keySet().contains(NOTIFICATION)) {
            user.setEmailFormat(getStringValue(obj, NOTIFICATION));
        }
        user.setAttachAudioToEmail(Boolean.valueOf(getStringValue(obj, ATTACH_AUDIO)));

        user.setAltEmailAddress(getStringValue(obj, ALT_EMAIL));
        if (obj.keySet().contains(ALT_NOTIFICATION)) {
            user.setAltEmailFormat(getStringValue(obj, ALT_NOTIFICATION));
        }
        user.setAltAttachAudioToEmail(Boolean.valueOf(getStringValue(obj, ALT_ATTACH_AUDIO)));

        String forcePinChange = getStringValue(obj, FORCE_PIN_CHANGE);
        if (forcePinChange != null) {
            user.setForcePinChange(forcePinChange);
        }

        String autoEnterPinExtension = getStringValue(obj, AUTO_ENTER_PIN_EXTENSION);
        if (autoEnterPinExtension != null) {
            user.setAutoEnterPinExtension(autoEnterPinExtension);
        }

        String autoEnterPinExternal = getStringValue(obj, AUTO_ENTER_PIN_EXTERNAL);
        if (autoEnterPinExternal != null) {
            user.setAutoEnterPinExternal(autoEnterPinExternal);
        }

        Integer daysToKeepVM = getIntegerValue(obj, DAYS_TO_KEEP_VM);
        if (daysToKeepVM != null) {
            user.setDaysToKeepVM(daysToKeepVM);
        }

        List<Document> aliasesObj = (List<Document>) obj.get(ALIASES);
        if (aliasesObj != null) {
            Vector<String> aliases = new Vector<String>();
            for (int i = 0; i < aliasesObj.size(); i++) {
                Document aliasObj = (Document) aliasesObj.get(i);
                if (aliasObj.get(RELATION).toString().equals(ALIAS)) {
                    aliases.add(aliasObj.get(ALIAS_ID).toString());
                }
            }
            user.setAliases(aliases);
        }

        // contact info related data
        user.setCellNum(getStringValue(obj, CELL_PHONE_NUMBER));
        user.setHomeNum(getStringValue(obj, HOME_PHONE_NUMBER));
        user.setConfEntryIM(getStringValue(obj, CONF_ENTRY_IM));
        user.setConfExitIM(getStringValue(obj, CONF_EXIT_IM));
        user.setVMEntryIM(getStringValue(obj, LEAVE_MESSAGE_BEGIN_IM));
        user.setVMExitIM(getStringValue(obj, LEAVE_MESSAGE_END_IM));
        user.setCallIM(getStringValue(obj, CALL_IM));
        user.setCallFromAnyIM(getStringValue(obj, CALL_FROM_ANY_IM));
        user.setImEnabled(Boolean.valueOf(getStringValue(obj, IM_ENABLED)));
        user.setJid(getStringValue(obj, IM_ID));
        user.setAltJid(getStringValue(obj, ALT_IM_ID));
        user.setImDisplayName(getStringValue(obj, IM_DISPLAY_NAME));
        user.setOnthePhoneMessage(getStringValue(obj, IM_ON_THE_PHONE_MESSAGE));
        user.setAdvertiseOnCallStatus(Boolean.valueOf(getStringValue(obj, IM_ADVERTISE_ON_CALL_STATUS)));
        user.setShowOnCallDetails(Boolean.valueOf(getStringValue(obj, IM_SHOW_ON_CALL_DETAILS)));
        user.setCompanyName(getStringValue(obj, COMPANY_NAME));
        user.setJobDepartment(getStringValue(obj, JOB_DEPT));
        user.setJobTitle(getStringValue(obj, JOB_TITLE));
        user.setFaxNumber(getStringValue(obj, FAX_NUMBER));

        // office details
        user.setOfficeStreet(getStringValue(obj, OFFICE_STREET));
        user.setOfficeCity(getStringValue(obj, OFFICE_CITY));
        user.setOfficeState(getStringValue(obj, OFFICE_STATE));
        user.setOfficeZip(getStringValue(obj, OFFICE_ZIP));
        user.setOfficeCountry(getStringValue(obj, OFFICE_COUNTRY));

        // home details
        user.setHomeCity(getStringValue(obj, HOME_CITY));
        user.setHomeState(getStringValue(obj, HOME_STATE));
        user.setHomeZip(getStringValue(obj, HOME_ZIP));
        user.setHomeCountry(getStringValue(obj, HOME_COUNTRY));
        user.setHomeStreet(getStringValue(obj, HOME_STREET));

        user.setAvatar(getStringValue(obj, AVATAR));

        // active greeting related data
        if (obj.keySet().contains(ACTIVEGREETING)) {
            user.setActiveGreeting(getStringValue(obj, ACTIVEGREETING));
        }

        user.setPlayDefaultVmOption(Boolean.valueOf(getStringValue(obj, PLAY_DEFAULT_VM)));
        user.setDepositVoicemail(Boolean.valueOf(getStringValue(obj, VOICEMAIL_ENABLED)));
        user.setVmLanguage(getStringValue(obj, UNIFIED_MESSAGING_LANGUAGE));

        // personal attendant related data
        if (obj.keySet().contains(PERSONAL_ATT)) {
            Document pao = (Document) obj.get(PERSONAL_ATT);
            String operator = getStringValue(pao, OPERATOR);
            String language = getStringValue(pao, LANGUAGE);
            Map<String, String> menu = new HashMap<String, String>();
            StringBuilder validDigits = new StringBuilder(10);
            List<Document> buttonsList = (List<Document>) pao.get(BUTTONS);
            if (buttonsList != null) {
                for (int i = 0; i < buttonsList.size(); i++) {
                    Document button = (Document) buttonsList.get(i);
                    if (button != null) {
                        menu.put(getStringValue(button, DIALPAD), getStringValue(button, ITEM));
                        validDigits.append(getStringValue(button, DIALPAD));
                    }
                }
            }
            user.setPersonalAttendant(new PersonalAttendant(language, operator, menu, validDigits.toString()));
        }

        // distribution lists
        if (obj.keySet().contains(DISTRIB_LISTS)) {
            Distributions distribs = new Distributions();
            List<Document> distribList = (List<Document>) obj.get(DISTRIB_LISTS);
            if (distribList != null) {
                for (int i = 0; i < distribList.size(); i++) {
                    Document distrib = (Document) distribList.get(i);
                    if (distrib != null) {
                        distribs.addList(getStringValue(distrib, DIALPAD),
                                StringUtils.split(getStringValue(distrib, ITEM), " "));
                    }
                }
            }
            user.setDistributions(distribs);
        }

        if (user.isInDirectory()) {
            buildDialPatterns(user);
        }

        return user;
    }

    public static String getStringValue(Document obj, String key) {
        if (obj.keySet().contains(key)) {
            if (obj.get(key) != null) {
                return obj.get(key).toString();
            }
        }
        return null;
    }

    public static Integer getIntegerValue(Document obj, String key) {
        if (obj.keySet().contains(key)) {
            if (obj.get(key) != null) {
                return Integer.parseInt(obj.get(key).toString());
            }
        }
        return null;
    }

    /**
     * Remove all non-letter characters, convert to upper case Remove diacritical marks if
     * possible
     *
     * @param orig
     */
    protected static String compress(String orig) {
        if (orig == null) {
            return "";
        }

        String normal = orig.toUpperCase();

        // Brute force conversion of diacriticals
        normal = normal.replaceAll("[\u00C0\u00C1\u00C2\u00C3\u00C4]", "A");
        normal = normal.replaceAll("[\u00C8\u00C9\u00CA\u00CB]", "E");
        normal = normal.replaceAll("[\u00CC\u00CD\u00CE\u00CF]", "I");
        normal = normal.replaceAll("[\u00D2\u00D3\u00D4\u00D5\u00D6]", "O");
        normal = normal.replaceAll("[\u00D9\u00DA\u00DB\u00DC]", "U");
        normal = normal.replaceAll("\u00C7", "C");
        normal = normal.replaceAll("\u00D1", "N");

        // Remove non letters
        normal = normal.replaceAll("[^A-Z]", "");

        return normal;
    }

    /**
     * Map from letters to DTMF numbers
     */
    protected static String mapDTMF(String orig) {
        if (orig == null) {
            return "";
        }

        StringBuilder output = new StringBuilder(orig.length());
        for (int i = 0; i < orig.length(); i++) {
            String c = orig.substring(i, i + 1);
            int pos = LETTERS.indexOf(c);
            if (pos >= 0) {
                // Output the corresponding position in numbers
                output.append(NUMBERS.charAt(pos));
            }
        }
        return output.toString();
    }

    public static String getDomainPart(String uri) {
        if (uri == null) {
            return null;
        }

        String domainName = uri;
        int atStart = domainName.indexOf("@");
        if (atStart <= 0) {
            return null;
        }

        domainName = domainName.substring(atStart + 1);
        return domainName.trim();
    }

    public static String getUserPart(String uri) {
        if (uri == null) {
            return null;
        }

        String userName = uri;
        int urlStart = userName.indexOf("sip:");

        if (urlStart >= 0) {
            userName = userName.substring(urlStart + 4);
        } else {
            int gtStart = userName.indexOf("<");
            if (gtStart >= 0) {
                userName = userName.substring(gtStart + 1);
            }
        }

        int atStart = userName.indexOf("@");
        if (atStart <= 0) {
            return null;
        }

        userName = userName.substring(0, atStart);
        return userName.trim();
    }

    public static String getDisplayPart(String uri) {
        if (uri == null) {
            return null;
        }

        String displayName = "";
        int urlStart = uri.indexOf("sip:");

        if (urlStart < 0) {
            return null;
        }

        displayName = uri.substring(0, urlStart);
        displayName = displayName.trim();
        int gtStart = displayName.indexOf("<");
        if (gtStart >= 0) {
            displayName = displayName.substring(0, gtStart);
        }
        int quoteStart = displayName.indexOf('"');
        if (quoteStart >= 0) {
            displayName = displayName.substring(quoteStart + 1);
            int quoteEnd = displayName.indexOf('"');
            if (quoteEnd >= 0) {
                displayName = displayName.substring(0, quoteEnd);
            }
        }
        return displayName.trim();
    }

    /**
     * Parse the Display name into a list of DTMF sequences
     *
     * Do one for Last name first And one for First name first
     *
     * @param u
     */
    protected static void buildDialPatterns(User u) {
        u.setDialPatterns(new Vector<String>());

        if (u.getDisplayName() == null) {
            return;
        }

        String[] names = u.getDisplayName().split("\\W");
        // Remove all non-character data, convert to upper case, convert to DTMF

        LinkedList<String> queue = new LinkedList<String>();
        for (String name : names) {
            String dtmf = mapDTMF(compress(name));
            if (dtmf.length() > 0) {
                queue.add(mapDTMF(compress(name)));
            }
        }

        // Given a b c d, generate:
        // a b c d
        // b c d a
        // c d a b
        // d a b c
        for (int i = 0; i < queue.size(); i++) {
            String mashup;
            String first = queue.poll(); // Pull first
            mashup = first;
            for (int j = 0; j < queue.size(); j++) {
                String next = queue.poll();
                mashup += next;
                queue.add(next);
            }
            queue.add(first); // Put first back (its now last)
            u.getDialPatterns().add(mashup);
        }
    }

    public boolean isValidIdentity(String uri) {
        if (uri == null) {
            return false;
        }
    
        Bson queryIdent = Filters.eq(IDENTITY, uri);
    
        long result = getEntityCollection().countDocuments(queryIdent);
        return result > 0;
    }

    public MongoDatabase getImdb() {
        return m_imdb;
    }

    public void setImdb(MongoDatabase imdb) {
        m_imdb = imdb;
    }
}
