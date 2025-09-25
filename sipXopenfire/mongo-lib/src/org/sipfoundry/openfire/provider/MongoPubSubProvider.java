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
import java.util.stream.Collectors;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.types.Binary;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.pubsub.DefaultPubSubPersistenceProvider;
import org.jivesoftware.openfire.pubsub.CollectionNode;
import org.jivesoftware.openfire.pubsub.DefaultNodeConfiguration;
import org.jivesoftware.openfire.pubsub.LeafNode;
import org.jivesoftware.openfire.pubsub.Node;
import org.jivesoftware.openfire.pubsub.NodeAffiliate;
import org.jivesoftware.openfire.pubsub.NodeSubscription;
import org.jivesoftware.openfire.pubsub.PubSubService;
import org.jivesoftware.openfire.pubsub.PublishedItem;
import org.jivesoftware.openfire.pubsub.cluster.FlushTask;
import org.jivesoftware.openfire.pubsub.models.AccessModel;
import org.jivesoftware.openfire.pubsub.models.PublisherModel;
import org.jivesoftware.openfire.pep.PEPService;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LinkedListNode;
import org.xmpp.packet.JID;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;

import org.bson.conversions.Bson;

import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;

public class MongoPubSubProvider extends DefaultPubSubPersistenceProvider {
    private static final Logger log = Logger.getLogger(MongoPubSubProvider.class);

    private static final String COLLECTION_NAME = "ofPubsubNode";
    private static final String AFFILIATION_COLLECTION_NAME = "ofPubsubAffiliation";
    private static final String DEFAULT_CFG_COLLECTION_NAME = "ofPubsubDefaultConf";
    private static final String ITEM_COLLECTION_NAME = "ofPubsubItem";
    private static final String NODE_GROUP_COLLECTION_NAME = "ofPubsubNodeGroups";
    private static final String NODE_JID_COLLECTION_NAME = "ofPubsubNodeJIDs";
    private static final String SUBSCRIPTIONS_COLLECTION_NAME = "ofPubsubSubscription";

    public MongoPubSubProvider() {
        getDefaultCollection().createIndex(Indexes.ascending("serviceId", "nodeId"));
    }

    @Override
    public void createNode(Node node) {

        super.createNode( node );
        Document toInsert = nodeToDocument(node);

        getDefaultCollection().insertOne(toInsert);

        saveAssociatedElements(node);
    }

    @Override
    public void updateNode(Node node) {

        super.updateNode( node );

        Document query = new Document();
        query.put("serviceId", node.getService().getServiceID());
        query.put("nodeId", node.getNodeID());

        Document update = nodeToDocument(node);

        // Replace the entire document matching the query
        getDefaultCollection().replaceOne(query, update);

        Document toDelete = new Document();
        toDelete.put("serviceId", node.getService().getServiceID());
        toDelete.put("nodeId", node.getNodeID());

        // Delete all associated elements
        getCollection(NODE_JID_COLLECTION_NAME).deleteMany(toDelete);
        getCollection(NODE_GROUP_COLLECTION_NAME).deleteMany(toDelete);

        saveAssociatedElements(node);
    }

    @Override
    public void removeNode(Node node) {

        super.removeNode( node );

        Document toDelete = new Document();
        toDelete.put("serviceId", node.getService().getServiceID());
        toDelete.put("nodeId", node.getNodeID());

        // Remove main node
        getDefaultCollection().deleteOne(toDelete);

        // Remove all associated elements
        getCollection(NODE_JID_COLLECTION_NAME).deleteMany(toDelete);
        getCollection(NODE_GROUP_COLLECTION_NAME).deleteMany(toDelete);
        getCollection(ITEM_COLLECTION_NAME).deleteMany(toDelete);
        getCollection(AFFILIATION_COLLECTION_NAME).deleteMany(toDelete);
        getCollection(SUBSCRIPTIONS_COLLECTION_NAME).deleteMany(toDelete);
    }


    @Override
    public void createAffiliation(Node node, NodeAffiliate affiliate) {

        super.createAffiliation( node, affiliate );

        MongoCollection<Document> collection = getCollection(AFFILIATION_COLLECTION_NAME);

        Document doc = new Document();
        doc.put("serviceId", node.getService().getServiceID());
        doc.put("nodeId", node.getNodeID());
        doc.put("jid", affiliate.getJID().toString());
        doc.put("affiliation", affiliate.getAffiliation().name());

        collection.insertOne(doc);
    }

    @Override
    public void updateAffiliation(Node node, NodeAffiliate affiliate) {

        super.updateAffiliation( node, affiliate );

        MongoCollection<Document> collection = getCollection(AFFILIATION_COLLECTION_NAME);

        Document filter = new Document();
        filter.put("serviceId", node.getService().getServiceID());
        filter.put("nodeId", node.getNodeID());
        filter.put("jid", affiliate.getJID().toString());

        Document update = new Document("$set",
            new Document("affiliation", affiliate.getAffiliation().name()));

        // Upsert ensures it will insert if it doesn't exist
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void removeAffiliation(Node node, NodeAffiliate affiliate) {

        super.removeAffiliation( node, affiliate );

        Document toDelete = new Document();
        toDelete.put("serviceId", node.getService().getServiceID());
        toDelete.put("nodeId", node.getNodeID());
        toDelete.put("jid", affiliate.getJID().toString());

        getCollection(AFFILIATION_COLLECTION_NAME).deleteOne(toDelete);
    }

    @Override
    public void createDefaultConfiguration(PubSubService.UniqueIdentifier serviceIdentifier,
            DefaultNodeConfiguration config) {

        super.createDefaultConfiguration( serviceIdentifier, config );

        Document toInsert = configToDocument(serviceIdentifier, config);
        getCollection(DEFAULT_CFG_COLLECTION_NAME).insertOne(toInsert);
    }

    @Override
    public void updateDefaultConfiguration(PubSubService.UniqueIdentifier serviceIdentifier, DefaultNodeConfiguration config) {

        super.updateDefaultConfiguration( serviceIdentifier, config );

        Document filter = new Document();
        filter.put("serviceId", serviceIdentifier.toString());
        filter.put("leaf", config.isLeaf());

        Document update = configToDocument(serviceIdentifier, config);

        getCollection(DEFAULT_CFG_COLLECTION_NAME)
            .updateOne(filter, new Document("$set", update));
    }

    @Override
    public void savePublishedItem(PublishedItem item) {

        super.savePublishedItem( item );

        MongoCollection<Document> itemCollection = getCollection(ITEM_COLLECTION_NAME);

        Document doc = new Document();
        doc.put("serviceId", item.getNode().getService().getServiceID());
        doc.put("nodeId", item.getNode().getNodeID());
        doc.put("id", item.getID());
        doc.put("jid", item.getPublisher().toString());
        doc.put("creationDate", item.getCreationDate().getTime());
        if (item.getPayloadXML() != null) {
            doc.put("payload", item.getPayloadXML());
        }

        itemCollection.insertOne(doc);
    }

    @Override
    public void removePublishedItem(PublishedItem item) {

        super.removePublishedItem( item );

        MongoCollection<Document> itemCollection = getCollection(ITEM_COLLECTION_NAME);

        Document filter = new Document();
        filter.put("serviceId", item.getNode().getService().getServiceID());
        filter.put("nodeId", item.getNode().getNodeID());
        filter.put("id", item.getID());

        itemCollection.deleteOne(filter);
    }

    @Override
    public void bulkPublishedItems(List<PublishedItem> itemsToAdd, List<PublishedItem> itemsToDelete) {

        super.bulkPublishedItems( itemsToAdd, itemsToDelete );

        if (itemsToDelete != null) {
            for (PublishedItem item : itemsToDelete) {
                removePublishedItem(item);
            }
        }

        if (itemsToAdd != null) {
            for (PublishedItem item : itemsToAdd) {
                savePublishedItem(item);
            }
        }
    }

    @Override
    public void purgeNode(LeafNode leafNode) {

        super.purgeNode( leafNode );

        MongoCollection<Document> itemCollection = getCollection(ITEM_COLLECTION_NAME);

        Document toDelete = new Document();
        toDelete.put("serviceId", leafNode.getService().getServiceID());
        toDelete.put("nodeId", leafNode.getNodeID());

        DeleteResult result = itemCollection.deleteMany(toDelete);
    }

    private static void saveAssociatedElements(Node node) {
        MongoCollection<Document> nodeJidCollection = getCollection(NODE_JID_COLLECTION_NAME);

        for (JID jid : node.getContacts()) {
            nodeJidCollection.insertOne(nodeJidToDocument(node, jid, "contacts"));
        }
        for (JID jid : node.getReplyRooms()) {
            nodeJidCollection.insertOne(nodeJidToDocument(node, jid, "replyRooms"));
        }
        for (JID jid : node.getReplyTo()) {
            nodeJidCollection.insertOne(nodeJidToDocument(node, jid, "replyTo"));
        }
        if (node.isCollectionNode()) {
            for (JID jid : ((CollectionNode) node).getAssociationTrusted()) {
                nodeJidCollection.insertOne(nodeJidToDocument(node, jid, "associationTrusted"));
            }
        }

        MongoCollection<Document> groupCollection = getCollection(NODE_GROUP_COLLECTION_NAME);
        for (String groupName : node.getRosterGroupsAllowed()) {
            Document doc = new Document()
                    .append("serviceId", node.getService().getServiceID())
                    .append("nodeId", node.getNodeID())
                    .append("rosterGroup", groupName);
            groupCollection.insertOne(doc);
        }
    }

    private static Document nodeJidToDocument(Node node, JID jid, String type) {
        return new Document()
                .append("serviceId", node.getService().getServiceID())
                .append("nodeId", node.getNodeID())
                .append("jid", jid.toString())
                .append("associationType", type);
    }

    private static Document nodeToDocument(Node node) {
        Document nodeObj = new Document();

        nodeObj.put("serviceId", node.getService().getServiceID());
        if (!StringUtils.isEmpty(node.getNodeID())) {
            nodeObj.put("nodeId", node.getNodeID());
        } else {
            nodeObj.put("nodeId", new ObjectId().toString());
        }
        if (node.isCollectionNode()) {
            nodeObj.put("leaf", false);
            nodeObj.put("maxPayloadSize", 0);
            nodeObj.put("persistItems", false);
            nodeObj.put("maxItems", 0);
        } else {
            nodeObj.put("leaf", true);
            nodeObj.put("maxPayloadSize", ((LeafNode) node).getMaxPayloadSize());
            nodeObj.put("persistItems", ((LeafNode) node).isPersistPublishedItems());
            nodeObj.put("maxItems", ((LeafNode) node).getMaxPublishedItems());
        }
        nodeObj.put("creationDate", node.getCreationDate());
        nodeObj.put("modificationDate", node.getModificationDate());
        nodeObj.put("parent", node.getParent() != null ? node.getParent().getNodeID() : null);
        nodeObj.put("deliverPayloads", node.isPayloadDelivered());
        nodeObj.put("notifyConfigChanges", node.isNotifiedOfConfigChanges());
        nodeObj.put("notifyDelete", node.isNotifiedOfDelete());
        nodeObj.put("notifyRetract", node.isNotifiedOfRetract());
        nodeObj.put("presenceBased", node.isPresenceBasedDelivery());
        nodeObj.put("sendItemSubscribe", node.isSendItemSubscribe());
        nodeObj.put("publisherModel", node.getPublisherModel().getName());
        nodeObj.put("subscriptionEnabled", node.isSubscriptionEnabled());
        nodeObj.put("configSubscription", node.isSubscriptionConfigurationRequired());
        nodeObj.put("accessModel", node.getAccessModel().getName());
        nodeObj.put("payloadType", node.getPayloadType());
        nodeObj.put("bodyXslt", node.getBodyXSLT());
        nodeObj.put("dataformXslt", node.getDataformXSLT());
        nodeObj.put("creator", node.getCreator().toString());
        nodeObj.put("description", node.getDescription());
        nodeObj.put("language", node.getLanguage());
        nodeObj.put("name", node.getName());
        nodeObj.put("replyPolicy", node.getReplyPolicy() != null ? node.getReplyPolicy().name() : null);
        if (node.isCollectionNode()) {
            nodeObj.put("associationPolicy", ((CollectionNode) node).getAssociationPolicy().name());
            nodeObj.put("maxLeafNodes", ((CollectionNode) node).getMaxLeafNodes());
        } else {
            nodeObj.put("associationPolicy", null);
            nodeObj.put("maxLeafNodes", 0);
        }

        return nodeObj;
    }
    private static Document configToDocument(PubSubService.UniqueIdentifier serviceIdentifier, 
            DefaultNodeConfiguration config) {
        Document dbObj = new Document();
        dbObj.put("serviceId", serviceIdentifier.toString());
        dbObj.put("leaf", config.isLeaf());
        dbObj.put("deliverPayloads", config.isDeliverPayloads());
        dbObj.put("maxPayloadSize", config.getMaxPayloadSize());
        dbObj.put("persistItems", config.isPersistPublishedItems());
        dbObj.put("maxItems", config.getMaxPublishedItems());
        dbObj.put("notifyConfigChanges", config.isNotifyConfigChanges());
        dbObj.put("notifyDelete", config.isNotifyDelete());
        dbObj.put("notifyRetract", config.isNotifyRetract());
        dbObj.put("presenceBased", config.isPresenceBasedDelivery());
        dbObj.put("sendItemSubscribe", config.isSendItemSubscribe());
        dbObj.put("publisherModel", config.getPublisherModel().getName());
        dbObj.put("subscriptionEnabled", config.isSubscriptionEnabled());
        dbObj.put("accessModel", config.getAccessModel().getName());
        dbObj.put("language", config.getLanguage());
        dbObj.put("replyPolicy", config.getReplyPolicy() != null ? config.getReplyPolicy().name() : null);
        dbObj.put("associationPolicy", config.getAssociationPolicy().name());
        dbObj.put("maxLeafNodes", config.getMaxLeafNodes());

        return dbObj;
    }


    @Override
    public void createSubscription(Node node, NodeSubscription subscription) {

        super.createSubscription( node, subscription );

        MongoCollection<Document> collection = getCollection(SUBSCRIPTIONS_COLLECTION_NAME);

        Document filter = new Document();
        filter.put("serviceId", node.getService().getServiceID());
        filter.put("nodeId", node.getNodeID());
        filter.put("id", subscription.getID());

        // Check if it already exists
        if (collection.find(filter).first() != null) {
            log.warn("Subscription already exists: " + subscription.getID() + " for node " + node.getNodeID());
            return;
        }

        Document toInsert = subscriptionToDocument(node, subscription);
        collection.insertOne(toInsert);
    }

    @Override
    public void updateSubscription(Node node, NodeSubscription subscription) {

        super.updateSubscription( node, subscription );

        MongoCollection<Document> collection = getCollection(SUBSCRIPTIONS_COLLECTION_NAME);

        if (NodeSubscription.State.none == subscription.getState()) {
            // Equivalent to delete
            removeSubscription(subscription);
        } else {
            Document filter = new Document();
            filter.put("serviceId", node.getService().getServiceID());
            filter.put("nodeId", node.getNodeID());
            filter.put("id", subscription.getID());

            Document update = subscriptionToDocument(node, subscription);

            // Try update first
            UpdateResult result = collection.updateOne(filter, new Document("$set", update));

            if (result.getMatchedCount() == 0) {
                collection.insertOne(update);
            }
        }
    }

    @Override
    public void removeSubscription(NodeSubscription subscription) {

        super.removeSubscription( subscription );

        Node node = subscription.getNode();
        Document toDelete = new Document();
       
        toDelete.put("serviceId", node.getService().getServiceID());
        toDelete.put("nodeId", node.getNodeID());
        toDelete.put("id", subscription.getID());

        getCollection(SUBSCRIPTIONS_COLLECTION_NAME).deleteOne(toDelete);
    }

    private static Document subscriptionToDocument(Node node, NodeSubscription subscription) {
        Document doc = new Document();
        doc.put("serviceId", node.getService().getServiceID());
        doc.put("nodeId", node.getNodeID());
        doc.put("id", subscription.getID());
        doc.put("owner", subscription.getOwner().toString());
        doc.put("jid", subscription.getJID().toString());
        doc.put("state", subscription.getState().name());
        doc.put("deliver", subscription.shouldDeliverNotifications());
        doc.put("digest", subscription.isUsingDigest());
        doc.put("digestFrequency", subscription.getDigestFrequency());
        doc.put("expire", subscription.getExpire() != null ? subscription.getExpire().getTime() : null);
        doc.put("includeBody", subscription.isIncludingBody());
        doc.put("showValues", encodeWithComma(subscription.getPresenceStates()));
        doc.put("subscriptionType", subscription.getType().name());
        doc.put("subscriptionDepth", subscription.getDepth());
        doc.put("keyword", subscription.getKeyword());

        return doc;
    }

    private static String encodeWithComma(Collection<String> strings) {
        return String.join(",", strings);
    }

    private static Collection<String> decodeWithComma(String strings) {
        if (strings == null || strings.isEmpty()) return new ArrayList<>();
        return Arrays.asList(strings.split("\\s*,\\s*"));
    }

    private static MongoCollection<Document> getDefaultCollection() {
        return getCollection(COLLECTION_NAME);
    }

    private static MongoCollection<Document> getCollection(String collectionName) {
        MongoDatabase db = UnfortunateLackOfSpringSupportFactory.getOpenfiredb();

        return db.getCollection(collectionName);
    }

}
