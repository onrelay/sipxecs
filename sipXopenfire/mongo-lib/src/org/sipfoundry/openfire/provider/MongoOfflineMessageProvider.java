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
import org.jivesoftware.openfire.OfflineMessage;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.provider.OfflineMessageProvider;
import org.jivesoftware.util.FastDateFormat;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.XMPPDateTimeFormat;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;

public class MongoOfflineMessageProvider extends BaseMongoProvider implements OfflineMessageProvider {
    private static final String COLLECTION_NAME = "ofOffline";

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(
            XMPPDateTimeFormat.XMPP_DATETIME_FORMAT, TimeZone.getTimeZone("UTC"));
    private static final FastDateFormat OLD_DATE_FORMAT = FastDateFormat.getInstance(
            XMPPDateTimeFormat.XMPP_DELAY_DATETIME_FORMAT, TimeZone.getTimeZone("UTC"));
    private static Logger log = Logger.getLogger(MongoOfflineMessageProvider.class);

    /**
     * Pattern to use for detecting invalid XML characters. Invalid XML characters will be removed
     * from the stored offline messages.
     */
    private static final Pattern PATTERN = Pattern.compile("&\\#[\\d]+;");

    public MongoOfflineMessageProvider() {
        setDefaultCollectionName(COLLECTION_NAME);
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        offlineCollection.createIndex(new Document("username", 1));

        Document index = new Document();
        index.put("username", 1);
        index.put("messageID", 1);
        offlineCollection.createIndex(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(String username, long messageID, String msgXML) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document toInsert = new Document();

        toInsert.put("username", username);
        toInsert.put("messageID", messageID);
        toInsert.put("creationDate", StringUtils.dateToMillis(new java.util.Date()));
        toInsert.put("messageSize", msgXML.length());
        toInsert.put("stanza", msgXML);

        offlineCollection.insertOne(toInsert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessage(String username, Date creationDate) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document toDelete = new Document();
        toDelete.put("username", username);
        toDelete.put("creationDate", StringUtils.dateToMillis(creationDate));
        DeleteResult result = offlineCollection.deleteOne(toDelete);

        return result.getDeletedCount() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessages(String username) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document toDelete = new Document();
        toDelete.put("username", username);
        DeleteResult result = offlineCollection.deleteMany(toDelete);

        return result.getDeletedCount() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflineMessage getMessage(String username, Date creationDate, SAXReader xmlReader) {
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document query = new Document();
        query.put("username", username);
        query.put("creationDate", StringUtils.dateToMillis(creationDate));

        Document projection = new Document();
        projection.put("stanza", 1);

        Document dbObj = offlineCollection.find(query)
                                        .projection(projection)
                                        .first();

        if (dbObj == null) {
            return null; // or handle not found case as appropriate
        }

        String msgXml = (String) dbObj.get("stanza");

        return fromString(msgXml, creationDate, xmlReader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OfflineMessage> getMessages(String username, boolean delete, SAXReader xmlReader) {
        List<OfflineMessage> messages = new ArrayList<>();
        MongoCollection<Document> offlineCollection = getDefaultCollection();

        Document query = new Document("username", username);
        Document projection = new Document("stanza", 1).append("creationDate", 1);

        // Iterate over results with projection
        for (Document dbObj : offlineCollection.find(query).projection(projection)) {
            String msgXml = dbObj.getString("stanza");
            // Note: "creationDate" stored as string in old code, convert properly:
            String creationDateStr = dbObj.getString("creationDate");
            Date creationDate = new Date(Long.parseLong(creationDateStr));
            messages.add(fromString(msgXml, creationDate, xmlReader));
        }

        // Delete if requested
        if (delete && !messages.isEmpty()) {
            log.debug("deleting offline messages for user " + username);
            offlineCollection.deleteMany(query);
        }

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return getSize(new Document());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize(String username) {
        Document query = new Document();

        query.put("username", username);

        return getSize(query);
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
                // Try again after removing invalid XML chars (e.g. &#12;)
                Matcher matcher = PATTERN.matcher(msgXml);
                if (matcher.find()) {
                    String invalidRemoved = matcher.replaceAll("");
                    org.dom4j.Document doc = xmlReader.read(new StringReader(invalidRemoved));
                    message = new OfflineMessage(creationDate, doc.getRootElement());
                }
            }

            if (message != null) {
                // Add a delayed delivery (XEP-0203) element to the message.
                Element delay = message.addChildElement("delay", "urn:xmpp:delay");
                delay.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
                delay.addAttribute("stamp", DATE_FORMAT.format(creationDate));
                // Add a legacy delayed delivery (XEP-0091) element to the
                // message. XEP is obsolete and support should be dropped in
                // future.
                delay = message.addChildElement("x", "jabber:x:delay");
                delay.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
                delay.addAttribute("stamp", OLD_DATE_FORMAT.format(creationDate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }
}
