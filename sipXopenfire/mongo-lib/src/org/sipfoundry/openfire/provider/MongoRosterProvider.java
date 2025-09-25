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
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.roster.RosterItemProvider;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveConstants;
import org.xmpp.packet.JID;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

public class MongoRosterProvider extends BaseMongoProvider implements RosterItemProvider {
    private static final String COLLECTION_NAME = "ofRoster";

    public MongoRosterProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> rosterCollection = getDefaultCollection();

        // Create indexes on the specified fields
        rosterCollection.createIndex(Indexes.ascending("rosterID"));
        rosterCollection.createIndex(Indexes.ascending("jid"));
        rosterCollection.createIndex(Indexes.ascending("username"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RosterItem createItem(String username, RosterItem item) throws UserAlreadyExistsException {
        MongoCollection<Document> rosterCollection = getDefaultCollection();
        Document toInsert = new Document();

        toInsert.put("rosterID", SequenceManager.nextID(JiveConstants.ROSTER));
        toInsert.put("username", username);
        toInsert.put("jid", item.getJid().toBareJID());
        toInsert.put("sub", item.getSubStatus().getValue());
        toInsert.put("ask", item.getAskStatus().getValue());
        toInsert.put("recv", item.getRecvStatus().getValue());
        toInsert.put("nick", item.getNickname());
        toInsert.put("groups", item.getGroups());

        rosterCollection.insertOne(toInsert);
        item.setID(toInsert.getLong("rosterID"));

        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteItem(String username, long rosterItemID) {
        MongoCollection<Document> rosterGrpCollection = getCollection("ofRosterGroups");
        Document grpToRemove = new Document("rosterID", rosterItemID);

        rosterGrpCollection.deleteOne(grpToRemove);

        MongoCollection<Document> rosterCollection = getDefaultCollection();
        Document toRemove = new Document("rosterID", rosterItemID);

        rosterCollection.deleteOne(toRemove);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount(String username) {
        MongoCollection<Document> rosterCollection = getDefaultCollection();
        Document toQuery = new Document("username", username);

        long count = rosterCollection.countDocuments(toQuery);
        return (int) count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<RosterItem> getItems(String username) {
        List<RosterItem> items = new ArrayList<RosterItem>();

        MongoCollection<Document> rosterCollection = getDefaultCollection();
        Document toQuery = new Document();
        toQuery.put("username", username);

        for (Document row : rosterCollection.find(toQuery)) {
            long id = (Long) row.get("rosterID");
            JID jid = new JID((String) row.get("jid"));
            int subType = (Integer) row.get("sub");
            int askType = (Integer) row.get("ask");
            int recvType = (Integer) row.get("recv");
            String nick = (String) row.get("nick");

            RosterItem item = new RosterItem(id, jid, RosterItem.SubType.getTypeFromInt(subType),
                    RosterItem.AskType.getTypeFromInt(askType), RosterItem.RecvType.getTypeFromInt(recvType), nick,
                    null);

            items.add(item);
        }

        return items.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getUsernames(String jid) {
        List<String> names = new ArrayList<String>();

        MongoCollection<Document> rosterCollection = getDefaultCollection();
        Document toQuery = new Document();
        toQuery.put("jid", jid);

        for (Document row : rosterCollection.find(toQuery)) {
            names.add((String) row.get("username"));
        }

        return names.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(String username, RosterItem item) throws UserNotFoundException {
        MongoCollection<Document> rosterCollection = getDefaultCollection();

        Document query = new Document("rosterID", item.getID());

        Document update = new Document()
            .append("sub", item.getSubStatus().getValue())
            .append("ask", item.getAskStatus().getValue())
            .append("recv", item.getRecvStatus().getValue())
            .append("nick", item.getNickname())
            .append("groups", item.getGroups());

        Document updateOperation = new Document("$set", update);

        Document updatedDoc = rosterCollection.findOneAndUpdate(query, updateOperation);

        if (updatedDoc == null) {
            throw new UserNotFoundException("Roster item not found with ID: " + item.getID());
        }
    }
}
