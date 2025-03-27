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

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/types.hpp>

#include <os/OsDateTime.h>
#include <os/OsLogger.h>

#include "sipdb/RegDB.h"
#include "sipdb/RegExpireThread.h"


using namespace std;

const string RegDB::NS("node.registrar");

RegDB* RegDB::CreateInstance(bool ensureIndexes, int gracePeriod) {
   RegDB* lRegDb = NULL;

   MongoDB::ConnectionInfo local = MongoDB::ConnectionInfo::localInfo();
   if (!local.isEmpty()) {
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, "Regional database defined");
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, local.getConnectionUri().to_string().c_str());
     lRegDb = new RegDB(local);
     lRegDb->setExpireGracePeriod(gracePeriod);

     // ensure indexes, if requested
     if (ensureIndexes)
     {
       lRegDb->ensureIndexes();
     }
   } else {
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, "No regional database found");
   }

   MongoDB::ConnectionInfo global = MongoDB::ConnectionInfo::globalInfo();
   RegDB* regDb = new RegDB(global, lRegDb);
   regDb->setExpireGracePeriod(gracePeriod);

   // ensure indexes, if requested
   if (ensureIndexes)
   {
     regDb->ensureIndexes();
   }
   return regDb;
}

//
// Creates/updates the indexes needed for RegDB
//
void RegDB::ensureIndexes()
{
    OS_LOG_INFO(FAC_SIP, "RegDB::ensureIndexes "
                          << "expireGracePeriod: " << _expireGracePeriod
                          << ", expirationTimeIndexTTL: " << _expirationTimeIndexTTL);

    // Use MongoConnection to manage the database connection
    MongoDB::MongoConnection connection(_info);

    // Access the target collection
    mongocxx::collection collection = connection.collection(_ns);

    // Create the indexes
    collection.create_index(bsoncxx::builder::basic::make_document(
        bsoncxx::builder::basic::kvp(std::string(RegBinding::instrument_fld()), 1)));

    collection.create_index(bsoncxx::builder::basic::make_document(
        bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), 1),
        bsoncxx::builder::basic::kvp(std::string(RegBinding::contact_fld()), 1),
        bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), 1)
    ));

    // Calculate the new expiration time index TTL
    int newExpirationTimeIndexTTL = std::max(
        MONGODB_EXPIRES_AFTER_SECONDS_MINIMUM_SECS,
        static_cast<int>(_expireGracePeriod)
    );

    // Check and update the TTL index if necessary
    if (newExpirationTimeIndexTTL != _expirationTimeIndexTTL)
    {
        _expirationTimeIndexTTL = newExpirationTimeIndexTTL;
        safeDropIndex(collection, RegBinding::expirationTime_fld());
    }

    safeEnsureTTLIndex(collection, RegBinding::expirationTime_fld(), _expirationTimeIndexTTL);
}

void RegDB::updateBinding(const RegBinding::Ptr& pBinding)
{
	updateBinding(*(pBinding.get()));
}
 
void RegDB::updateBinding(RegBinding& binding)
{
    if (_local != nullptr)
    {
        _local->updateBinding(binding);
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    try
    {
        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        updateTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        if (binding.getTimestamp() == 0)
        {
            binding.setTimestamp(OsDateTime::getSecsSinceEpoch());
        }

        if (binding.getLocalAddress().empty())
        {
            string serverId = _localAddress;
            binding.setLocalAddress(serverId);
        }

        if (binding.getBinding().empty())
        {
            Url curl(binding.getContact().c_str());
            UtlString hostPort;
            UtlString user;
            curl.getHostWithPort(hostPort);
            curl.getUserId(user);

            std::ostringstream strm;
            strm << "sip:";
            if (!user.isNull())
            {
                strm << user.data() << "@";
            }
            strm << hostPort.data();

            binding.setBinding(strm.str());
        }

        binding.setShardId(getShardId());

        // Create the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(
            bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), binding.getIdentity()),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::contact_fld()), binding.getContact()),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), binding.getShardId()));

        bool isExpired = (binding.getExpirationTime() == 0);
        binding.setExpired(isExpired);

        // Create the update document
        bsoncxx::document::value updateDoc = binding.toBSONObj();
        bsoncxx::builder::basic::document updateOpBuilder;
        updateOpBuilder.append(
            bsoncxx::builder::basic::kvp("$set", updateDoc.view()));

        mongocxx::options::update options;
        options.upsert(true); // Enable upsert

        auto result = collection.update_one(queryBuilder.view(), updateOpBuilder.view(), options);

        if (result && result->modified_count() > 0)
        {
            OS_LOG_DEBUG(FAC_SIP, "Save reg ok");
        }
        else if (result && result->upserted_id())
        {
            OS_LOG_DEBUG(FAC_SIP, "Document was upserted");
        }
        else
        {
            OS_LOG_WARNING(FAC_SIP, "No document modified or upserted");
        }
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_SIP, "Failed to update bindings: " << e.what());
    } catch (...) {
        OS_LOG_ERROR(FAC_SIP, "Unknown error occurred while updating bindings");
    }
}

void RegDB::expireOldBindings(const std::string& identity, const std::string& callId, unsigned int cseq, std::int64_t timeNow)
{
    if (_local != nullptr) {
        _local->expireOldBindings(identity, callId, cseq, timeNow);
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    try {
        // Build the query using bsoncxx::builder
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(
            bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), identity),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::callId_fld()), callId),
            bsoncxx::builder::basic::kvp(
                std::string(RegBinding::cseq_fld()),
                [cseq](bsoncxx::builder::basic::sub_document subDoc) {
                    subDoc.append(bsoncxx::builder::basic::kvp(std::string("$lt"), static_cast<int32_t>(cseq)));
                }),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), getShardId()));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        updateTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the remove operation
        collection.delete_many(queryBuilder.view());

        // Logging
        OS_LOG_INFO(FAC_SIP, "Expired old bindings for identity=" << identity
                                                                  << ", callId=" << callId
                                                                  << ", cseq < " << cseq);
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_SIP, "Failed to expire old bindings: " << e.what());
    } catch (...) {
        OS_LOG_ERROR(FAC_SIP, "Unknown error occurred while expiring old bindings");
    }
}

void RegDB::expireAllBindings(const std::string& identity, const std::string& callId, unsigned int cseq, std::int64_t timeNow)
{
    if (_local != nullptr) {
        _local->expireAllBindings(identity, callId, cseq, timeNow);
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    try {
        // Build the query using bsoncxx::builder
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(
            bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), getShardId()),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), identity));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        updateTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the remove operation
        collection.delete_many(queryBuilder.view());

        // Logging
        OS_LOG_INFO(FAC_SIP, "Expired all bindings for identity=" << identity
                                                                << ", callId=" << callId);
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_SIP, "Failed to expire all bindings: " << e.what());
    } catch (...) {
        OS_LOG_ERROR(FAC_SIP, "Unknown error occurred while expiring all bindings");
    }
}

void RegDB::removeAllExpired()
{
    if (_local != nullptr) {
        _local->removeAllExpired();
        return;
    }

    std::int64_t timeNow = OsDateTime::getSecsSinceEpoch() - _expireGracePeriod;

    OS_LOG_INFO(FAC_SIP, "RegDB::removeAllExpired INVOKED for shard == " << getShardId()
                        << " and expireTime <= " << timeNow
                        << " gracePeriod: " << _expireGracePeriod << " sec");

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    try {
        // Build the query using bsoncxx::builder
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(
            bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), getShardId()),
            bsoncxx::builder::basic::kvp(std::string(RegBinding::expirationTime_fld()), bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string("$lte"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        updateTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the remove operation
        collection.delete_many(queryBuilder.view());

        // Logging the result
        OS_LOG_INFO(FAC_SIP, "Expired all bindings for shard " << getShardId() << " successfully.");
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_SIP, "Failed to remove all expired bindings: " << e.what());
    } catch (...) {
        OS_LOG_ERROR(FAC_SIP, "Unknown error occurred while removing all expired bindings");
    }
}

bool RegDB::isOutOfSequence(const string& identity, const string& callId, unsigned int cseq) const
{
    // Remove this method altogether?!?!? -- Conversation between douglas and joegen on 6/18/13
	return false;
}

bool RegDB::isRegisteredBinding(const Url& curl, bool preferPrimary)
{
  bool isRegistered = false;

  try {
        UtlString hostPort;
        UtlString user;
        curl.getHostWithPort(hostPort);
        curl.getUserId(user);

        std::ostringstream binding;
        binding << "sip:";
        if (!user.isNull())
            binding << user.data() << "@";
        binding << hostPort.data();

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::binding_fld()), binding.str()));

        if (_local)
        {
            preferPrimary = false;
            _local->isRegisteredBinding(curl, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::shardId_fld()), bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string("$ne"), _local->getShardId()))));
        }

        MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        readTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        mongocxx::options::find findOptions;
        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        auto cursor = collection.find(queryBuilder.view(), findOptions);

        isRegistered = cursor.begin() != cursor.end(); // Check if there are any results

        OS_LOG_INFO(FAC_SIP, "RegDB::isRegisteredBinding returning " << (isRegistered ? "TRUE" : "FALSE") << " for binding " << binding.str());
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_SIP, "Failed to check if binding is registered: " << e.what());
    } catch (...) {
        OS_LOG_ERROR(FAC_SIP, "Unknown error occurred while checking if binding is registered");
    }

    return isRegistered;
}

void RegDB::pushOrReplaceBinding(RegDB::Bindings& bindings, const RegBinding& binding)
{
  //
  // Check if the call-id or contact of this binding has been previously pushed
  //
  for (RegDB::Bindings::iterator iter = bindings.begin(); iter != bindings.end(); iter++)
  {
    if (iter->getCallId() == binding.getCallId() || iter->getContact() == binding.getContact())
    {
      //
      // This is already inserted previously most probably by local shard.
      // Check which one of them has the larger timestamp, and therefore
      // the most up-to-date
      //

      if (binding.getTimestamp() > iter->getTimestamp())
      {
        //
        // The new binding has a more recent timestamp value.  replace the old one
        //
        OS_LOG_INFO(FAC_SIP, "RegDB::findAndReplaceOlderBindings - replacing duplicate binding for " << binding.getUri());
        *iter = binding;
      }
      else
      {
        //
        // Simply ignore this binding.  It is older (or equal) than what was previously pushed
        //
        OS_LOG_INFO(FAC_SIP, "RegDB::findAndReplaceOlderBindings - dropping older binding for " << binding.getUri());
      }
      return;
    }
  }
  //
  // We haven't found any older duplicate
  //
  bindings.push_back(binding);
}

bool RegDB::getUnexpiredRegisteredBinding(const Url &registeredBinding,
                                          Bindings &bindings,
                                          bool preferPrimary) 
{
    bool isRegistered = false;

    UtlString hostPort;
    UtlString user;
    registeredBinding.getHostWithPort(hostPort);
    registeredBinding.getUserId(user);

    std::ostringstream binding;
    binding << "sip:";
    if (!user.isNull()) {
    binding << user.data() << "@";
    }
    binding << hostPort.data();
 
    try {
        MongoDB::ReadTimer readTimer(const_cast<RegDB &>(*this));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        readTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        std::int64_t timeNow = OsDateTime::getSecsSinceEpoch();

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::binding_fld()), binding.str()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::expirationTime_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))));

        if (_local) {
            preferPrimary = false;
            _local->getUnexpiredRegisteredBinding(registeredBinding, bindings,
                                                    preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(RegBinding::shardId_fld()),
                bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(
                    std::string("$ne"), _local->getShardId()))));
        }

        mongocxx::options::find findOptions;

        if (preferPrimary) {
            BaseDB::primaryPreferred(findOptions);
        } 
        else {
            BaseDB::nearest(findOptions);
        }

        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), findOptions);

        for (const bsoncxx::document::view &doc : cursor) 
        {
            isRegistered = true;

            RegBinding binding(doc);

            if (binding.getExpirationTime() > timeNow) {
                OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding "
                                        << " Identity: " << binding.getIdentity()
                                        << " Contact: " << binding.getContact()
                                        << " Expires: "
                                        << binding.getExpirationTime() -
                                                OsDateTime::getSecsSinceEpoch()
                                        << " sec"
                                        << " Call-Id: " << binding.getCallId());

                pushOrReplaceBinding(bindings, binding);
            } 
            else {
                OS_LOG_WARNING(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding returned "
                                        "an expired record?!?!"
                                            << " Identity: " << binding.getIdentity()
                                            << " Contact: " << binding.getContact()
                                            << " Call-Id: " << binding.getCallId()
                                            << " Expires: "
                                            << binding.getExpirationTime() << " epoch"
                                            << " TimeNow: " << timeNow << " epoch");
            }
        }
        
    } catch(mongocxx::exception &e) {
        OS_LOG_ERROR(
        FAC_SIP,
        "RegDB::getUnexpiredRegisteredBinding caught DBException: " << e.what());
    } 

    OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding returning "
                            << (isRegistered ? "TRUE" : "FALSE") << " for binding "
                            << binding.str());

    return isRegistered;
}

bool RegDB::getUnexpiredContactsUser(const std::string& identity, std::int64_t timeNow, Bindings& bindings, bool preferPrimary) const
{
    MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

    try
    {
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        static std::string gruuPrefix = GRUU_PREFIX;

        bool isGruu = identity.substr(0, gruuPrefix.size()) == gruuPrefix;

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::expirationTime_fld()), 
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))
        ));

        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredContactsUser(identity, timeNow, bindings, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(RegBinding::shardId_fld()),
                bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$ne"), _local->getShardId()))
            ));
        }

        if (isGruu) {
            std::string searchString = identity + ";" + SIP_GRUU_URI_PARAM;
            queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::gruu_fld()), searchString));
        } else {
            queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), identity));
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
            RegBinding binding(doc);

            if (binding.getExpirationTime() > timeNow)
            {
                OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredContactsUser "
                    << " Identity: " << identity
                    << " Contact: " << binding.getContact()
                    << " Expires: " << binding.getExpirationTime() - timeNow << " sec"
                    << " Expired: " << binding.getExpired()
                    << " Call-Id: " << binding.getCallId());

                pushOrReplaceBinding(bindings, binding);
            }
            else
            {
                OS_LOG_WARNING(FAC_SIP, "RegDB::getUnexpiredContactsUser returned an expired record?!?!"
                    << " Identity: " << identity
                    << " Contact: " << binding.getContact()
                    << " Call-Id: " << binding.getCallId()
                    << " Expiration time: " << binding.getExpirationTime() << " sec since epoch"
                    << " Expires: " << binding.getExpirationTime() - timeNow << " sec"
                    << " Expired: " << binding.getExpired()
                    << " TimeNow: " << timeNow << " epoch");
            }
        }
    }
    catch (const mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::getUnexpiredContactsUser - Error querying MongoDB: " << e.what());
        return false;
    }

    if (bindings.empty())
    {
        OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredContactsUser returned empty recordset for identity " << identity);
    }

    return !bindings.empty();
}

bool RegDB::getUnexpiredContactsUserContaining(const std::string& matchIdentity, std::int64_t timeNow, Bindings& bindings, bool preferPrimary) const
{
    MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

    try
    {
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::expirationTime_fld()), 
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))
        ));

        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredContactsUserContaining(matchIdentity, timeNow, bindings, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(RegBinding::shardId_fld()),
                bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$ne"), _local->getShardId()))
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
            RegBinding binding(doc);

            // Check if the contact field contains the matchIdentity
            if (binding.getContact().find(matchIdentity) == std::string::npos)
            {
                continue;
            }

            pushOrReplaceBinding(bindings, binding);
        }

        // If we reach here, it means there were bindings added
        return !bindings.empty();
    }
    catch (const mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::getUnexpiredContactsUserContaining - Error querying MongoDB: " << e.what());
        return false;
    }
}

bool RegDB::getUnexpiredContactsUserWithAddress(const string& identity, const std::string& address, std::int64_t timeNow, Bindings& bindings, bool preferPrimary) const
{
  Bindings regRecords;

  if (!getUnexpiredContactsUser(identity, timeNow, regRecords, preferPrimary))
  {
    return false;
  }

  bindings.clear();

  for (Bindings::const_iterator iter = regRecords.begin(); iter != regRecords.end(); iter++)
  {
    if (iter->getContact().find(address) != std::string::npos)
    {
      bindings.push_back(*iter);
    }
  }

  return !bindings.empty();
}


bool RegDB::getUnexpiredContactsUserInstrument(
    const std::string& identity,
    const std::string& instrument,
    std::int64_t timeNow,
    Bindings& bindings,
    bool preferPrimary) const
{
    MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

    try
    {
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::identity_fld()), identity));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::instrument_fld()), instrument));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::expirationTime_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))
        ));

        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredContactsUserInstrument(identity, instrument, timeNow, bindings, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(RegBinding::shardId_fld()),
                bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$ne"), _local->getShardId()))
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
            RegBinding binding(doc);
            pushOrReplaceBinding(bindings, binding);
        }

        // Return true if bindings are found
        return !bindings.empty();
    }
    catch (const mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::getUnexpiredContactsUserInstrument - Error querying MongoDB: " << e.what());
        return false;
    }
}

// TODO : Unclear how big this dataset would be, decide if this should be removed
bool RegDB::getUnexpiredContactsInstrument(
    const std::string& instrument,
    std::int64_t timeNow,
    Bindings& bindings,
    bool preferPrimary) const
{
    MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

    try
    {
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(RegBinding::instrument_fld()), instrument));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(RegBinding::expirationTime_fld()),
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), BaseDB::dateFromSecsSinceEpoch(timeNow)))
        ));

        if (_local)
        {
            preferPrimary = false;
            _local->getUnexpiredContactsInstrument(instrument, timeNow, bindings, preferPrimary);
            queryBuilder.append(bsoncxx::builder::basic::kvp(
                std::string(RegBinding::shardId_fld()),
                bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$ne"), _local->getShardId()))
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
            RegBinding binding(doc);
            pushOrReplaceBinding(bindings, binding);
        }

        // Return true if bindings are found
        return !bindings.empty();
    }
    catch (const mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::getUnexpiredContactsInstrument - Error querying MongoDB: " << e.what());
        return false;
    }
}

void RegDB::cleanAndPersist(int currentExpireTime)
{
    if (_local)
    {
        _local->cleanAndPersist(currentExpireTime);
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    // Build the query to find expired records
    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(bsoncxx::builder::basic::kvp(
        std::string(RegBinding::expirationTime_fld()),
        bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$lt"), BaseDB::dateFromSecsSinceEpoch(currentExpireTime)))
    ));

    try
    {
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        updateTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Remove expired records
        auto result = collection.delete_many(queryBuilder.view());

        OS_LOG_INFO(FAC_SIP, "RegDB::cleanAndPersist - Removed " 
            << (result ? result->deleted_count() : 0) 
            << " expired records from namespace " << _ns);
    }
    catch (const mongocxx::exception& e)
    {
        updateTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::cleanAndPersist - Error while cleaning expired records: " << e.what());
        throw; // Re-throw the exception for higher-level handling if necessary
    }
}

void RegDB::clearAllBindings()
{
    if (_local)
    {
        _local->clearAllBindings();
        return;
    }

    MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

    try
    {
        // Establish connection
        MongoDB::MongoConnection connection(_info);

        // Mark the database connection as OK
        updateTimer.setDBConnOK(connection.ok());

        // Get the collection
        mongocxx::collection collection = connection.collection(_ns);

        // Remove all documents
        auto result = collection.delete_many({}); // Empty filter matches all documents

        OS_LOG_INFO(FAC_SIP, "RegDB::clearAllBindings - Removed all bindings from namespace " 
            << _ns << ", count: " << (result ? result->deleted_count() : 0));
    }
    catch (const mongocxx::exception& e)
    {
        updateTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_SIP, "RegDB::clearAllBindings - Error while clearing all bindings: " << e.what());
        throw; // Re-throw the exception for higher-level handling if necessary
    }
}