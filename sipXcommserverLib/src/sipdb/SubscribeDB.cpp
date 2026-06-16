/*
 * Copyright (c) 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */



#include <mongocxx/client.hpp>
#include <mongocxx/uri.hpp>
#include <mongocxx/collection.hpp>
#include <mongocxx/options/find.hpp>
#include <mongocxx/exception/exception.hpp>

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/types.hpp>

#include "sipdb/SubscribeDB.h"
#include "sipdb/SubscribeExpireThread.h"

#include "os/OsLogger.h"
#include "os/OsDateTime.h"


#include <utl/UtlString.h>


using namespace std;

const string SubscribeDB::NS("node.subscription");

SubscribeDB* SubscribeDB::CreateInstance(bool ensureIndexes)
{
  SubscribeDB* ldb = NULL;

  MongoDB::ConnectionInfo local = MongoDB::ConnectionInfo::localInfo();
  if (!local.isEmpty())
  {
    ldb = new SubscribeDB(local);

    // ensure the indexes
    if (ensureIndexes)
    {
      ldb->ensureIndexes();
    }
  }

  MongoDB::ConnectionInfo global = MongoDB::ConnectionInfo::globalInfo();
  SubscribeDB* db = new SubscribeDB(global, ldb);

  // ensure the indexes
  if (ensureIndexes)
  {
    db->ensureIndexes();
  }

  return db;
}

void SubscribeDB::getAll(Subscriptions& subscriptions, bool preferPrimary)
{
    MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

    try
    {
        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        bsoncxx::builder::basic::document queryBuilder;

        if (_local)
        {
            preferPrimary = false;
            _local->getAll(subscriptions, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()), 
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp(std::string("$ne"), getShardId()))));
        }

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for(const bsoncxx::document::view& doc : cursor)
        {
            subscriptions.push_back(doc);
        }

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::getAll - Retrieved " 
            << subscriptions.size() << " subscriptions from namespace " << _ns);
    }
    catch (const mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::getAll - Error querying MongoDB: " << e.what());
        throw; // Re-throw the exception for higher-level handling
    }
}

void SubscribeDB::upsert(
    const UtlString& component,
    const UtlString& uri,
    const UtlString& callId,
    const UtlString& contact,
    unsigned int expires,
    unsigned int subscribeCseq,
    const UtlString& eventTypeKey,
    const UtlString& eventType,
    const UtlString& id,
    const UtlString& toUri,
    const UtlString& fromUri,
    const UtlString& key,
    const UtlString& recordRoute,
    unsigned int notifyCseq,
    const UtlString& accept,
    unsigned int version)
{
    if (_local)
    {
        _local->upsert(
            component, 
            uri, 
            callId, 
            contact,
            expires, 
            subscribeCseq,
            eventTypeKey, 
            eventType, 
            id, 
            toUri, 
            fromUri, 
            key, 
            recordRoute, 
            notifyCseq, 
            accept, 
            version);
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));

    // Construct query
    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(
        bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), toUri.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), fromUri.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callId.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::eventTypeKey_fld()), eventTypeKey.str())
    );

    // Construct update document
    bsoncxx::builder::basic::document updateBuilder;
    updateBuilder.append(
        bsoncxx::builder::basic::kvp(std::string(Subscription::component_fld()), component.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::uri_fld()), uri.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callId.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::contact_fld()), contact.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::expires_fld()), BaseDB::dateFromSecsSinceEpoch(expires)),
        bsoncxx::builder::basic::kvp(std::string(Subscription::subscribeCseq_fld()), static_cast<std::int32_t>(subscribeCseq)),
        bsoncxx::builder::basic::kvp(std::string(Subscription::eventTypeKey_fld()), eventTypeKey.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::eventType_fld()), eventType.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::id_fld()), id.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), toUri.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), fromUri.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::key_fld()), key.str()),
        bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()), getShardId())
    );

    // Add optional fields
    if (!recordRoute.isNull())
    {
        updateBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::recordRoute_fld()), recordRoute.str()));
    }

    if (notifyCseq)
    {
        updateBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::notifyCseq_fld()), static_cast<std::int32_t>(notifyCseq)));
    }

    updateBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::accept_fld()), accept.str()));
    updateBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::version_fld()), static_cast<std::int32_t>(version)));

    // Wrap update in `$set` operation
    bsoncxx::builder::basic::document setOperation;
    setOperation.append(bsoncxx::builder::basic::kvp(std::string("$set"), updateBuilder.view()));

    try
    {
        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the connection as OK
        updateTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the upsert
        mongocxx::options::update options;
        options.upsert(true);

        collection.update_one(queryBuilder.view(), setOperation.view(), options);

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::upsert - Successfully upserted subscription.");
    }
    catch (const mongocxx::exception& e)
    {
        updateTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::upsert - Error upserting subscription: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::ensureIndexes()
{
    OS_LOG_INFO(FAC_SIP, "SubscribeDB::ensureIndexes - isFirstEnsureIndexes: " << _isFirstEnsureIndexes);

    try
    {
        // Establish a MongoDB connection
        MongoDB::MongoConnection connection(_info);
        mongocxx::collection collection = connection.collection(_ns);

        // Create indexes
        collection.create_index(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::key_fld()), 1)));
        
        collection.create_index(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()), 1)));
        
        collection.create_index(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), 1),
            bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), 1),
            bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), 1),
            bsoncxx::builder::basic::kvp(std::string(Subscription::eventTypeKey_fld()), 1)));

        // Drop the 'expires' index if this is the first ensureIndexes call
        if (_isFirstEnsureIndexes)
        {
            safeDropIndex(collection, Subscription::expires_fld());
            _isFirstEnsureIndexes = false;
        }

        // Recreate the 'expires' index with TTL
        safeEnsureTTLIndex(collection, Subscription::expires_fld(), MONGODB_EXPIRES_AFTER_SECONDS_MINIMUM_SECS);

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::ensureIndexes - Indexes ensured successfully.");
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::ensureIndexes - Error ensuring indexes: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::remove(
    const UtlString& component,
    const UtlString& to,
    const UtlString& from,
    const UtlString& callid,
    const int& subscribeCseq)
{
    if (_local)
    {
        _local->remove(component, to, from, callid, subscribeCseq);
        return;
    }

    try
    {
        MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));

        // Build the query document
        auto query = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), to.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), from.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callid.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::subscribeCseq_fld()),
                                         bsoncxx::builder::basic::make_document(
                                             bsoncxx::builder::basic::kvp(std::string("$lt"), subscribeCseq))));

        // Establish a connection and get the collection
        MongoDB::MongoConnection connection(_info);
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the removal operation
        auto result = collection.delete_many(query.view());
        if (result)
        {
            OS_LOG_INFO(FAC_SIP, "SubscribeDB::remove - Removed " << result->deleted_count() << " documents.");
        }
        else
        {
            OS_LOG_WARNING(FAC_SIP, "SubscribeDB::remove - No documents matched the query for removal.");
        }

        updateTimer.setDBConnOK(true);
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::remove - Error removing documents: " << e.what());
        throw; // Re-throw the exception for higher-level handling
    }
}

void SubscribeDB::removeError(
    const UtlString& component,
    const UtlString& to,
    const UtlString& from,
    const UtlString& callid)
{
    if (_local)
    {
        _local->removeError(component, to, from, callid);
        return;
    }

    try
    {
        MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));

        // Build the query document
        auto query = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), to.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), from.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callid.str()));

        // Establish a connection and get the collection
        MongoDB::MongoConnection connection(_info);
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the removal operation
        auto result = collection.delete_many(query.view());
        if (result)
        {
            OS_LOG_INFO(FAC_SIP, "SubscribeDB::removeError - Removed " << result->deleted_count() << " documents.");
        }
        else
        {
            OS_LOG_WARNING (FAC_SIP, "SubscribeDB::removeError - No documents matched the query for removal.");
        }

        updateTimer.setDBConnOK(true);
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::removeError - Error removing documents: " << e.what());
        throw; // Re-throw the exception for higher-level handling
    }
}

bool SubscribeDB::subscriptionExists(
    const UtlString& component,
    const UtlString& toUri,
    const UtlString& fromUri,
    const UtlString& callId,
    const std::int64_t timeNow,
    bool preferPrimary)
{
    try
    {
        MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), toUri.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::fromUri_fld()), fromUri.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callId.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callId.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(Subscription::expires_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        // Check for local subscription
        if (_local)
        {
            preferPrimary = false;
            if (_local->subscriptionExists(component, toUri, fromUri, callId, timeNow, preferPrimary))
            {
                return true;
            }
            queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()),
                                                     bsoncxx::builder::basic::make_document(
                                                         bsoncxx::builder::basic::kvp(std::string("$ne"), getShardId()))));
        }

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        // Perform the query
        std::optional<bsoncxx::document::value> result = collection.find_one(queryBuilder.view(), findOptions);

        // Return true if a document is found
        return static_cast<bool>(result);  
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::subscriptionExists - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::removeExpired(const UtlString& component, const std::int64_t timeNow)
{
    try
    {
        // Handle local database case
        if (_local)
        {
            _local->removeExpired(component, timeNow);
            return;
        }

        // Initialize the MongoDB connection
        MongoDB::MongoConnection connection(_info);

        // Build the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::component_fld()), component.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(Subscription::expires_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));


        // Retrieve the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Remove all expired entries matching the query
        collection.delete_many(queryBuilder.view());

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::removeExpired - Removed expired entries for component: " << component.str());
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::removeExpired - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::getUnexpiredSubscriptions(
    const UtlString& component,
    const UtlString& key,
    const UtlString& eventTypeKey,
    const unsigned long& timeNow,
    Subscriptions& subscriptions,
    bool preferPrimary)
{
    try
    {
        // Handle local database case
        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredSubscriptions(component, key, eventTypeKey, timeNow, subscriptions, preferPrimary);
            return;
        }

        MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::key_fld()), key.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::eventTypeKey_fld()), eventTypeKey.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()), getShardId()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(Subscription::expires_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for(const bsoncxx::document::view& doc : cursor)
        {
            subscriptions.push_back(doc);
        }

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::getUnexpiredSubscriptions - Retrieved subscriptions for key: " << key.str());
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::getUnexpiredSubscriptions - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::getUnexpiredContactsFieldsContaining(
    UtlString& substringToMatch,
    const unsigned long& timeNow,
    std::vector<std::string>& matchingContactFields,
    bool preferPrimary) const
{
    try
    {
        // Handle local database case
        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredContactsFieldsContaining(substringToMatch, timeNow, matchingContactFields, preferPrimary);
            return;
        }

        MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(Subscription::expires_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for(const bsoncxx::document::view& doc : cursor)
        {
            auto contactElement = doc[Subscription::contact_fld()];
            if (contactElement && contactElement.type() == bsoncxx::type::k_string)
            {
                std::string contact = std::string(contactElement.get_string().value);
                if (contact.find(substringToMatch.str()) != std::string::npos)
                {
                    matchingContactFields.push_back(contact);
                }
            }
        }
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::getUnexpiredContactsFieldsContaining - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

void SubscribeDB::updateNotifyUnexpiredSubscription(
    const UtlString& component,
    const UtlString& to,
    const UtlString& from,
    const UtlString& callid,
    const UtlString& eventTypeKey,
    const UtlString& id,
    std::int64_t timeNow,
    int updatedNotifyCseq,
    int version)
{
    try
    {
        // Handle local database case
        if (_local)
        {
            _local->updateNotifyUnexpiredSubscription(
                component,
                to,
                from,
                callid,
                eventTypeKey,
                id,
                timeNow,
                updatedNotifyCseq,
                version);
            return;
        }

        // Initialize the MongoDB connection
        MongoDB::MongoConnection connection(_info);

        // Build the query document
        auto query = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), to.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callid.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::eventTypeKey_fld()), eventTypeKey.str()),
            bsoncxx::builder::basic::kvp(std::string(Subscription::id_fld()), id.str()));

        // Build the update document
        auto update = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string("$set"),
                                         bsoncxx::builder::basic::make_document(
                                             bsoncxx::builder::basic::kvp(std::string(Subscription::notifyCseq_fld()), updatedNotifyCseq),
                                             bsoncxx::builder::basic::kvp(std::string(Subscription::version_fld()), version))));

        // Retrieve the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Execute the update operation
        auto result = collection.update_one(query.view(), update.view());

        // Log the result of the update
        if (result && result->modified_count() > 0)
        {
            OS_LOG_INFO(FAC_SIP, "SubscribeDB::updateNotifyUnexpiredSubscription - Successfully updated subscription.");
        }
        else
        {
            OS_LOG_WARNING(FAC_SIP, "SubscribeDB::updateNotifyUnexpiredSubscription - No matching document found to update.");
        }
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::updateNotifyUnexpiredSubscription - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}


void SubscribeDB::updateToTag(
    const UtlString& callid,
    const UtlString& fromtag,
    const UtlString& totag)
{
    try
    {
        // Handle local database case
        if (_local)
        {
            _local->updateToTag(callid, fromtag, totag);
            return;
        }

        // Initialize the MongoDB connection
        MongoDB::MongoConnection connection(_info);

        // Build the query document to find records matching the `callid`
        auto query = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(Subscription::callId_fld()), callid.str()));

        // Retrieve the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Execute the query
        auto cursor = collection.find(query.view());
        for (const auto& bsonObj : cursor)
        {
            // Check for `fromUri` field
            auto fromUriElem = bsonObj[Subscription::fromUri_fld()];
            if (fromUriElem && fromUriElem.type() == bsoncxx::type::k_string)
            {
                std::string fromUri = std::string(fromUriElem.get_string().value);
                Url from_uri(fromUri.c_str(), FALSE);

                // Check for matching `fromtag`
                UtlString seen_tag;
                if (from_uri.getFieldParameter("tag", seen_tag) && seen_tag.compareTo(fromtag) == 0)
                {
                    // Check for `toUri` field
                    auto toUriElem = bsonObj[Subscription::toUri_fld()];
                    if (toUriElem && toUriElem.type() == bsoncxx::type::k_string)
                    {
                        std::string toUri = std::string(toUriElem.get_string().value); 
                        Url to_uri(toUri.c_str(), FALSE);
                        UtlString dummy;

                        // If `toUri` does not already have a tag, add the new tag
                        if (!to_uri.getFieldParameter("tag", dummy))
                        {
                            to_uri.setFieldParameter("tag", totag);
                            to_uri.toString(dummy); // Convert back to string

                            // Get the `_id` field
                            auto idElem = bsonObj["_id"];
                            if (idElem && idElem.type() == bsoncxx::type::k_oid)
                            {
                                bsoncxx::oid oid = idElem.get_oid().value;

                                // Create query and update documents
                                auto updateQuery = bsoncxx::builder::basic::make_document(
                                    bsoncxx::builder::basic::kvp(std::string("_id"), oid));
                                auto updateDoc = bsoncxx::builder::basic::make_document(
                                    bsoncxx::builder::basic::kvp(std::string("$set"),
                                                                 bsoncxx::builder::basic::make_document(
                                                                     bsoncxx::builder::basic::kvp(std::string(Subscription::toUri_fld()), dummy.data()))));

                                // Perform the update
                                collection.update_one(updateQuery.view(), updateDoc.view());
                            }
                            else
                            {
                                // Log if `_id` is not retrievable
                                OS_LOG_INFO(FAC_ODBC, "SubscribeDB::updateToTag - Failed to retrieve object ID.");
                            }
                        }
                    }
                }
            }
        }
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_ODBC, "SubscribeDB::updateToTag - MongoDB exception: " << e.what());
        throw; // Re-throw for higher-level handling
    }
}

bool SubscribeDB::findFromAndTo(
    const UtlString& callid,
    const UtlString& fromtag,
    const UtlString& totag,
    UtlString& from,
    UtlString& to,
    bool preferPrimary) const
{
    try
    {
        MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query document for `callid`
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(Subscription::callId_fld()), callid.str()));

        // Handle local database logic
        if (_local)
        {
            preferPrimary = false;  // Local connections don't use primary preference
            if (_local->findFromAndTo(callid, fromtag, totag, from, to, preferPrimary))
            {
                return true;
            }
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(Subscription::shardId_fld()),
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp("$ne", getShardId())
                )
            ));
        }

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for(const bsoncxx::document::view& doc : cursor)
        {
            // Extract the `fromUri` field
            bsoncxx::document::element fromUriElem = doc[Subscription::fromUri_fld()];
            if (fromUriElem && fromUriElem.type() == bsoncxx::type::k_string)
            {
                std::string fromUri = std::string(fromUriElem.get_string().value);
                Url from_uri(fromUri.c_str(), FALSE);

                // Check for matching `fromtag`
                UtlString seen_tag;
                if (from_uri.getFieldParameter("tag", seen_tag) && seen_tag.compareTo(fromtag) == 0)
                {
                    // Extract the `toUri` field
                    bsoncxx::document::element toUriElem = doc[Subscription::toUri_fld()];
                    if (toUriElem && toUriElem.type() == bsoncxx::type::k_string)
                    {
                        std::string toUri = std::string(toUriElem.get_string().value);
                        Url to_uri(toUri.c_str(), FALSE);

                        // Check for matching `totag`
                        if (to_uri.getFieldParameter("tag", seen_tag) && seen_tag.compareTo(totag) == 0)
                        {
                            // Found a match; record the full URIs
                            from = fromUri.c_str();
                            to = toUri.c_str();
                            return true;
                        }
                    }
                }
            }
        }
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_ODBC, "SubscribeDB::findFromAndTo - MongoDB exception: " << e.what());
        throw;  // Re-throw for higher-level handling
    }

    return false;  // No match found
}

int SubscribeDB::getMaxVersion(const UtlString& uri, bool preferPrimary) const
{
    try
    {
        MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));

        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        readTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query document for `uri`
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::uri_fld()), uri.str()));

        unsigned int maxValue = 0;

        // Handle local database logic
        if (_local)
        { 
            preferPrimary = false;  // Local connections don't use primary preference
            maxValue = _local->getMaxVersion(uri, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(Subscription::shardId_fld()),
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp("$ne", getShardId())
                )
            ));
        }

        mongocxx::options::find findOptions;
        if (preferPrimary) 
        {
            BaseDB::primaryPreferred(findOptions);
        } 
        else 
        {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for(const bsoncxx::document::view& doc : cursor)
        {
            Subscription subscription = doc;
            maxValue = std::max(maxValue, subscription.version());
        }

        return maxValue;
    }
    catch (const mongocxx::exception& e)
    {
        // Log and re-throw exception for higher-level handling
        OS_LOG_ERROR(FAC_ODBC, "SubscribeDB::getMaxVersion - MongoDB exception: " << e.what());
        throw MongoDB::MongoException("Failed to retrieve max version for URI " + uri.str() + ": " + e.what());
    }
}

void SubscribeDB::removeAllExpired()
{
    try
    {
        // Handle local database logic
        if (_local)
        {
            _local->removeAllExpired();
            return;
        }

        // Get current time in seconds since epoch
        std::int64_t timeNow = OsDateTime::getSecsSinceEpoch();

        OS_LOG_INFO(FAC_SIP, "SubscribeDB::removeAllExpired INVOKED for shard == " << getShardId() << " and expireTime <= " << timeNow);

        // Initialize MongoDB connection
        MongoDB::MongoConnection connection(_info);

        // Construct the query for expired subscriptions
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::shardId_fld()), getShardId()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(Subscription::expires_fld()),
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string("$lte"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        // Retrieve the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Execute the remove operation
        collection.delete_many(queryBuilder.view());

    }
    catch (const mongocxx::exception& e)
    {
        // Log and re-throw exception for higher-level handling
        OS_LOG_ERROR(FAC_SIP, "SubscribeDB::removeAllExpired - MongoDB exception: " << e.what());
        throw MongoDB::MongoException("Failed to remove expired subscriptions: " + std::string(e.what()));
    }
}

