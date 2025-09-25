/**
 *
 *
 * Copyright (c) 2015 sipXcom, Inc. All rights reserved.
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
package org.sipfoundry.sipxcallback.common;

import static org.sipfoundry.commons.mongo.MongoConstants.CALLBACK_LIST;
import static org.sipfoundry.commons.mongo.MongoConstants.ENTITY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.collection.IQueue;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.Cluster;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

public class CallbackServiceImpl implements CallbackService {

    private static final String HAZELCAST_CALLBACK_QUEUE = "hazelcast_callback_queue";
    private static final String HAZELCAST_CALLBACK_QUEUE_INITIATED = "callback_queue_initiated";
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");

    private MongoTemplate m_imdbTemplate;
    private int m_expires;
    private HazelcastInstance m_hazelcastInstance;

    @Override
    public void updateCallbackInfoToMongo(CallbackLegs callbackLegs, boolean insertNewRequest)
            throws CallbackException {
        String callerChannelName = callbackLegs.getCallerUID();
        if (callerChannelName.contains(".")) {
            callerChannelName = callerChannelName.replace(".", ";");
        }
        MongoCollection<Document> entityCollection = m_imdbTemplate.getCollection("entity");
        Document user = findUserByName(callbackLegs.getCalleeName(), entityCollection);
        if (user == null) {
            // callback user was not found
            throw new CallbackException("Callback user: " + callbackLegs.getCalleeName() + " not found !");
        }
        List<Document> callbackList = null;
        if (user.keySet().contains(MongoConstants.CALLBACK_LIST)) {
            callbackList = (List<Document>) user.get(MongoConstants.CALLBACK_LIST);
        } else {
            callbackList = new ArrayList<Document>();
        }
        for (Document callerDbObject: callbackList ) {

            if( callerDbObject.containsKey(callerChannelName) ) {
                callbackList.remove(callerDbObject);
            }
        }
        if (insertNewRequest) {
            insertNewObject(callbackLegs, callbackList);
        }
        updateCallbackList(entityCollection, callbackList, user, null);
    }

    private void insertNewObject(CallbackLegs callbackLegs, List<Document> callbackList) {
        Document callback = new Document(callbackLegs.getCallerUID(), getCurrentTimestamp());
        callbackList.add(callback);
        if (callbackLegs != null) {
            // add the request also in the hazelcast call queue
            IQueue<CallbackLegs> callbackQueue = getCallbackQueue();
            boolean isUpdated = false;
            for (CallbackLegs oldCallbackLegs : callbackQueue) {
                if (oldCallbackLegs.equals(callbackLegs)) {
                    oldCallbackLegs.setDate(callbackLegs.getDate());
                    isUpdated = true;
                    break;
                }
            }
            if (!isUpdated) {
                getCallbackQueue().add(callbackLegs);
            }
        }
    }

    private Set<CallbackLegs> setupCallbackRequest() {
        MongoCollection<Document> entityCollection = m_imdbTemplate.getCollection("entity");
        // get all users which have callback on busy set
        FindIterable<Document> users = getCallbackUsers(entityCollection);

        // iterate over users
        Set<CallbackLegs> callsMap = new HashSet<CallbackLegs>();
        for (Document user : users) {
            List<Document> callbackList = (List<Document>) user.get(CALLBACK_LIST);
            String calleeName = ValidUsers.getStringValue(user, UID);
            // iterate over callback requests for each user
            // keep tabs if any callback flag has expired and then update the callback list
            List<Document> objectsToBeRemoved = new ArrayList<Document>();
            for (Object callerDbObject : callbackList) {
                Set<CallbackLegs> callbackRequests = handleCallbackAction(calleeName, (Document) callerDbObject,
                        callbackList, objectsToBeRemoved);
                callsMap.addAll(callbackRequests);
            }
            updateCallbackList(entityCollection, callbackList, user, objectsToBeRemoved);
        }
        return callsMap;
    }

    public Document findUserByName(String userName, MongoCollection<Document> entityCollection) {
        return entityCollection.find(
                Filters.and(
                        Filters.eq(ENTITY_NAME, "user"),
                        Filters.eq(UID, userName)
                )
        ).first();
    }
    
    /**
     * Retrieves the current date in UTC timezone
     */
    public static long getCurrentTimestamp() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return date.getTime();
    }

    private void updateCallbackList(MongoCollection<Document> entityCollection,
        List<Document> callbackList, Document user, List<Document> objectsToBeRemoved) {

        if (objectsToBeRemoved != null && !objectsToBeRemoved.isEmpty()) {
            callbackList.removeAll(objectsToBeRemoved);
        }
        if (callbackList.isEmpty()) {
            user.remove(MongoConstants.CALLBACK_LIST);
        } else {
            user.put(MongoConstants.CALLBACK_LIST, callbackList);
        }

        entityCollection.replaceOne(Filters.eq("_id", user.getObjectId("_id")), user, new ReplaceOptions().upsert(true));
    }

    /**
     * Returns a list with objects to be removed because their callback duration has expired
     */
    private Set<CallbackLegs> handleCallbackAction(String calleeName,Document callbackObject, List<Document> callbackList,
            List<Document> objectsToBeRemoved) {
        Set<CallbackLegs> callSet = new HashSet<CallbackLegs>();
        for (String callerName : callbackObject.keySet()) {
            long callerDate = (long) callbackObject.get(callerName);
            long currentDate = getCurrentTimestamp();
            long timeDiff = currentDate - callerDate;
            // check if the flag for callback has expired
            if (timeDiff < m_expires * 60000) {
                CallbackLegs callbackLegs = new CallbackLegs(calleeName, callerName , callerDate);
                callSet.add(callbackLegs);
            } else {
                objectsToBeRemoved.add(callbackObject);
            }
        }
        return callSet;
    }

    private FindIterable<Document> getCallbackUsers(MongoCollection<Document> entityCollection) {
        return entityCollection.find(
                Filters.and(
                        Filters.eq(ENTITY_NAME, "user"),  // ENTITY_NAME == "user"
                        Filters.exists(CALLBACK_LIST)  // CALLBACK_LIST field exists
                )
        );
    }

    
    public void setImdbTemplate(MongoTemplate imdbTemplate) {
        m_imdbTemplate = imdbTemplate;
    }

    
    public void setExpires(int expires) {
        m_expires = expires;
    }

    
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        m_hazelcastInstance = hazelcastInstance;
    }

    @Override
    public IQueue<CallbackLegs> getCallbackQueue() {
        return m_hazelcastInstance.getQueue(HAZELCAST_CALLBACK_QUEUE);
    }

    @Override
    public void initiateCallbackQueue() {
        Cluster hazelcastCluster = m_hazelcastInstance.getCluster();
        Set<Member> hazelcastMembers = hazelcastCluster.getMembers();

        // initiate on "primary" hazelcast instance only
        if ((hazelcastCluster.getLocalMember().equals(hazelcastMembers.iterator().next()))) {
            IAtomicReference<Boolean> initiated = getAtomicReference(HAZELCAST_CALLBACK_QUEUE_INITIATED);
            // initiate the queue if needed
            if (initiated.get() == null) {
                LOG.debug("Setting up Hazelcast callback queue.");
                initiated.set(Boolean.valueOf(true));
                Set<CallbackLegs> calls = setupCallbackRequest();
                getCallbackQueue().clear();
                getCallbackQueue().addAll(calls);
            }
        }
    }

    @Override
    public IAtomicReference<Boolean> getAtomicReference(String key) {
        return m_hazelcastInstance.getCPSubsystem().getAtomicReference(key);
    }

    @Override
    public boolean isCallbackLegsFreeToProcess(CallbackLegs callbackLegs) {
        IAtomicReference<Boolean> calleeReference = getAtomicReference(callbackLegs.getCalleeName());
        Boolean calleeIsProcessing = calleeReference.get();
        IAtomicReference<Boolean> callerReference = getAtomicReference(callbackLegs.getCallerName());
        Boolean callerIsProcessing = callerReference.get();
        // process this request ONLY if this callee is not currently beeing processed by another callback thread
        return ((calleeReference.isNull() || calleeIsProcessing.equals(false))
                && (callerReference.isNull() || callerIsProcessing.equals(false)));
    }

}
