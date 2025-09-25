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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.OfflineMessageStore;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.jivesoftware.openfire.OfflineMessage;
import org.jivesoftware.util.FastDateFormat;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.XMPPDateTimeFormat;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;


public class MongoOfflineMessageProvider extends BaseMongoProvider {
    private static final String COLLECTION_NAME = "ofOffline";

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(
            XMPPDateTimeFormat.XMPP_DATETIME_FORMAT, TimeZone.getTimeZone("UTC"));
    private static final FastDateFormat OLD_DATE_FORMAT = FastDateFormat.getInstance(
            XMPPDateTimeFormat.XMPP_DELAY_DATETIME_FORMAT, TimeZone.getTimeZone("UTC"));
    private static final Logger log = Logger.getLogger(MongoOfflineMessageProvider.class);

    private final OfflineMessageStore m_offlineMessageStore;

    /**
     * Pattern to use for detecting invalid XML characters. Invalid XML characters will be removed
     * from the stored offline messages.
     */
    private static final Pattern PATTERN = Pattern.compile("&\\#[\\d]+;");

    public MongoOfflineMessageProvider( OfflineMessageStore offlineMessageStore ) {

        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        offlineCollection.createIndex(new Document("username", 1));

        Document index = new Document();
        index.put("username", 1);
        index.put("messageID", 1);
        offlineCollection.createIndex(index);

        m_offlineMessageStore = offlineMessageStore;
    }

    public void addMessage(JID recipient, Message message) throws UserNotFoundException {
        final String username = recipient.getNode();
        if (username == null) {
            throw new UserNotFoundException("Recipient has no node: " + recipient.toString());
        }

        String msgXML = message.toXML();
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document toInsert = new Document();
        toInsert.put("username", username);
        toInsert.put("messageID", message.getID());
        toInsert.put("creationDate", System.currentTimeMillis());
        toInsert.put("messageSize", msgXML.length());
        toInsert.put("stanza", msgXML);

        offlineCollection.insertOne(toInsert);
    }

    public Collection<OfflineMessage> getMessages(String username, boolean delete) {
        List<OfflineMessage> messages = new ArrayList<>();
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document query = new Document("username", username);
        Document projection = new Document("stanza", 1).append("creationDate", 1);

        for (Document dbObj : offlineCollection.find(query).projection(projection)) {
            String msgXml = dbObj.getString("stanza");
            long creationMillis = dbObj.getLong("creationDate");
            Date creationDate = new Date(creationMillis);
            messages.add(fromString(msgXml, creationDate, new SAXReader()));
        }

        if (delete && !messages.isEmpty()) {
            log.debug("Deleting offline messages for user " + username);
            offlineCollection.deleteMany(query);
        }

        return messages;
    }

    public void deleteMessages(String username) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();
        offlineCollection.deleteMany(new Document("username", username));
    }

    public int getSize(String username) {
        Document query = new Document("username", username);
        return getSize(query);
    }

    public int getSize() {
        return getSize(new Document());
    }

    private int getSize(Document toFind) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        int totalSize = 0;
        Document projection = new Document("messageSize", 1);

        for (Document dbObj : offlineCollection.find(toFind).projection(projection)) {
            Integer messageSize = dbObj.getInteger("messageSize", 0);
            totalSize += messageSize;
        }

        return totalSize;
    }

    private static OfflineMessage fromString(String msgXml, Date creationDate, SAXReader xmlReader) {
        OfflineMessage message = null;
        try {
            try {
                message = new OfflineMessage(creationDate, xmlReader.read(new StringReader(msgXml)).getRootElement());
            } catch (DocumentException e) {
                Matcher matcher = PATTERN.matcher(msgXml);
                if (matcher.find()) {
                    String invalidRemoved = matcher.replaceAll("");
                    org.dom4j.Document doc = xmlReader.read(new StringReader(invalidRemoved));
                    message = new OfflineMessage(creationDate, doc.getRootElement());
                }
            }

            if (message != null) {
                Element delay = message.addChildElement("delay", "urn:xmpp:delay");
                delay.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
                delay.addAttribute("stamp", DATE_FORMAT.format(creationDate));

                delay = message.addChildElement("x", "jabber:x:delay");
                delay.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
                delay.addAttribute("stamp", OLD_DATE_FORMAT.format(creationDate));
            }
        } catch (Exception e) {
            log.error("Error parsing offline message XML", e);
        }
        return message;
    }
}