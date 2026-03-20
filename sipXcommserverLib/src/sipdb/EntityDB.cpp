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


#include "os/OsLogger.h"

#include "sipdb/MongoDB.h"
#include "sipdb/EntityDB.h"

#include <vector>

#include <mongocxx/client.hpp>
#include <mongocxx/uri.hpp>
#include <mongocxx/collection.hpp>
#include <mongocxx/options/find.hpp>
#include <mongocxx/cursor.hpp>

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/types.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/json.hpp>             

using namespace std;

static const std::string ID_SIPX_PROVISION = "~~id~sipXprovision";

const std::string EntityDB::NS("imdb.entity");

static std::string validate_identity_string(const std::string& identity)
{
  //
  // There are special cases where we might want to rewrite certain identity.
  // A good example is if a set of users share a common credential like ~~id~sipXprovision
  //
  // Rewrite:  ~~id~sipXprovision~XXXYYY => ~~id~sipXprovision
  // Rewrite:  ~~id~sipXprovision~XXXYYY@somedomain.com  => ~~id~sipXprovision@somedomain.com
  //
  if (identity.find(ID_SIPX_PROVISION) == 0)
  {
    std::vector<std::string> tokens;
    boost::split(tokens, identity, boost::is_any_of("@"), boost::token_compress_on);

    if (tokens.size() <= 1)
    {
      return ID_SIPX_PROVISION;
    }
    else if (tokens.size() == 2)
    {
      return  ID_SIPX_PROVISION + std::string("@") + tokens[1];
    }
    else
    {
      OS_LOG_WARNING(FAC_ODBC, "validate_identity_string - " << identity << " appears to be malformed");
    }
  }

  return identity;
}

static bool wildcard_compare(const char* wild, const std::string& str)
{
  const char* string = str.c_str();

  const char *cp = NULL, *mp = NULL;

  while ((*string) && (*wild != '*')) {
    if ((*wild != *string) && (*wild != '?')) {
      return 0;
    }
    wild++;
    string++;
  }

  while (*string) {
    if (*wild == '*') {
      if (!*++wild) {
        return 1;
      }
      mp = wild;
      cp = string+1;
    } else if ((*wild == *string) || (*wild == '?')) {
      wild++;
      string++;
    } else {
      wild = mp;
      string = cp++;
    }
  }

  while (*wild == '*') {
    wild++;
  }
  return !*wild;
}

static bool cidr_compare(const std::string& cidr, const std::string& ip)
{
  std::vector<std::string> cidr_tokens;
  boost::split(cidr_tokens, cidr, boost::is_any_of("/-"), boost::token_compress_on);

  unsigned bits = 24;
  std::string start_ip;
  if (cidr_tokens.size() == 2)
  {
    bits = (unsigned)::atoi(cidr_tokens[1].c_str());
    start_ip = cidr_tokens[0];
  }
  else
  {
    start_ip = cidr;
  }

  boost::system::error_code ec;
  boost::asio::ip::address_v4 ipv4;
  ipv4 = boost::asio::ip::address_v4::from_string(ip, ec);
  if (!ec)
  {
    unsigned long ipv4ul = ipv4.to_ulong();
    boost::asio::ip::address_v4 start_ip_ipv4;
    start_ip_ipv4 = boost::asio::ip::address_v4::from_string(start_ip, ec);
    if (!ec)
    {
      long double numHosts = pow((long double)2, (int)(32-bits)) - 1;
      unsigned long start_ip_ipv4ul = start_ip_ipv4.to_ulong();
      unsigned long ceiling = start_ip_ipv4ul + numHosts;
      return ipv4ul >= start_ip_ipv4ul && ipv4ul <= ceiling;
    }
  }
  return false;
}

EntityDB::EntityDB(const MongoDB::ConnectionInfo& info, size_t cacheExpire) :
   BaseDB(info, NS), _cache(cacheExpire), _typeCache(cacheExpire)
{
  OS_LOG_INFO(FAC_ODBC, "EntityDB::EntityDB - cache expiration " << cacheExpire << " milliseconds");
  init();
}


EntityDB::EntityDB(const MongoDB::ConnectionInfo& info, const std::string& ns, size_t cacheExpire) :
   BaseDB(info, ns), _cache(cacheExpire), _typeCache(cacheExpire)
{
  OS_LOG_INFO(FAC_ODBC, "EntityDB::EntityDB - cache expiration " << cacheExpire << " milliseconds");
  init();
}

void EntityDB::init()
{
  bsoncxx::document::value minKeyDoc = bsoncxx::builder::basic::make_document(
      bsoncxx::builder::basic::kvp("_id", bsoncxx::types::b_minkey{}));

  bsoncxx::document::view minKeyView = minKeyDoc.view();
  _lastTailId = minKeyView["_id"];
}


bool EntityDB::findByIdentity(const string& ident, EntityRecord& entity) const
{
    MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this)); // Start the read timer

    std::string identity = validate_identity_string(ident);

    OS_LOG_INFO(FAC_ODBC, "EntityDB::findByIdentity - Finding entity record for " << identity << " from namespace " << _ns);

    // Check if we have it in cache
    ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(identity);
    if (pCacheObj)
    {
        OS_LOG_DEBUG(FAC_ODBC, identity << " is present in namespace " << _ns << " (CACHED)");
        entity = *pCacheObj;
        return true;
    }

    // Try to connect to MongoDB and perform the query
    try
    {
        // Create the MongoDB connection
        MongoDB::MongoConnection connection(_info);

        readTimer.setDBConnOK(connection.ok());

        // Access the collection directly
        mongocxx::collection collection = connection.collection(_ns);

        // Build the BSON query
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(EntityRecord::identity_fld()), identity));

        // Execute the query with a limit of 1 document
        mongocxx::cursor cursor = collection.find(queryBuilder.view(), mongocxx::options::find{}.limit(1));

        // Check if any documents were returned
        auto it = cursor.begin();
        if (it == cursor.end())
        {
            OS_LOG_DEBUG(FAC_ODBC, identity << " is NOT present in namespace " << _ns);
            OS_LOG_INFO(FAC_ODBC, "EntityDB::findByIdentity - Unable to find entity record for " << identity << " from namespace " << _ns);
            return false;
        }

        // Get the first document from the cursor
        bsoncxx::document::view doc = *it;
        OS_LOG_DEBUG(FAC_ODBC, identity << " is present in namespace " << _ns);

        // Deserialize the document into an EntityRecord
        entity = doc;

        // Cache the entity
        const_cast<ExpireCache&>(_cache).add(identity, ExpireCacheable(new EntityRecord(entity)));

        return true;
    }
    catch (mongocxx::exception& e)
    {
        // Mark the DB connection as failed if an exception occurs
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_ODBC, "EntityDB::findByIdentity - Error querying MongoDB: " << e.what());
    }
    return false;
}

void EntityDB::getEntitiesByType(const std::string& entityType, Entities& entities, bool nocache)
{
    MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
    OS_LOG_INFO(FAC_ODBC, "EntityDB::getEntitiesByType - Finding entity records for type " 
                          << entityType << " from namespace " << _ns);

    //
    // Check if we have it in cache
    //
    if (!nocache)
    {
        EntityTypeCacheable pCacheObj = const_cast<EntityTypeCache&>(_typeCache).get(entityType);
        if (pCacheObj)
        {
            OS_LOG_DEBUG(FAC_ODBC, "EntityDB::getEntitiesByType - " << entityType 
                               << " is present in namespace " << _ns << " (CACHED)");
            entities = *pCacheObj;
            return;
        }
    }

    // Build the query
    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(EntityRecord::entity_fld()), entityType));

    // Use MongoConnection to get a connection and database
    MongoDB::MongoConnection connection(_info.getConnectionUri());

    readTimer.setDBConnOK(connection.ok());

    mongocxx::collection collection = connection.collection(_ns);

    // Use a query limit of 1024
    mongocxx::options::find options;
    options.limit(1024);
    options.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

    // Perform the query
    auto cursor = collection.find(queryBuilder.view(), options);

    // Clear the existing entities
    entities.clear();

    // Process the results
    for (auto&& doc : cursor)
    {
        EntityRecord entity;
        entity = doc;
        entities.push_back(entity);
    }

    // Log and cache the results
    if (entities.empty())
    {
        OS_LOG_DEBUG(FAC_ODBC, entityType << " is NOT present in namespace " << _ns);
        OS_LOG_INFO(FAC_ODBC, "EntityDB::getEntitiesByType - Unable to find entity record for type " 
                              << entityType << " from namespace " << _ns);
    }
    else
    {
        const_cast<EntityTypeCache&>(_typeCache).add(entityType, EntityTypeCacheable(new Entities(entities)));
        OS_LOG_DEBUG(FAC_ODBC, "EntityDB::getEntitiesByType - " << entityType 
                           << " is present in namespace " << _ns);
    }
}

void EntityDB::getCallerLocation(CallerLocations& locations, std::string& fallbackLocation, const std::string& identity, const std::string& host, const std::string& address)
{
  Entities branches;
  getEntitiesByType(EntityRecord::entity_branch_str(), branches);

  OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation( identity=" << identity << ", host=" << host << ", address=" << address << ")");

  EntityRecord userEntity;
  if (findByIdentity(identity, userEntity) && !userEntity.allowedLocations().empty())
  {
    //
    // Now that we have the locations for this user, check for branch associated locations
    //
    for (std::set<std::string>::iterator locations_iter = userEntity.allowedLocations().begin(); locations_iter != userEntity.allowedLocations().end(); locations_iter++)
    {
      for (Entities::iterator branch_iter = branches.begin(); branch_iter != branches.end(); branch_iter++)
      {
        if (branch_iter->location() == *locations_iter)
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Setting associated locations based  on identity " << identity << " branch " << branch_iter->location());
          locations = branch_iter->associatedLocations();
          fallbackLocation = branch_iter->associatedLocationFallback();
          return;
        }
      }
    }

    return;
  }

  //
  // Get locations by domain or subnet
  //
  for (Entities::iterator host_iter = branches.begin(); host_iter != branches.end(); host_iter++) // this loop iterates the branch record we have queried
  {
    if (!host.empty() && !host_iter->loc_restr_dom().empty() && !host_iter->inboundAssociatedLocations().empty()) // only if locations and wild card matching is specified by the branch
    {
      for (EntityRecord::LocationDomain::iterator domainIter = host_iter->loc_restr_dom().begin(); domainIter != host_iter->loc_restr_dom().end(); domainIter++)
      {
        if (wildcard_compare(domainIter->c_str(), host))
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Inserting location based  on " << *domainIter << " wildcard match for domain " << host);
          locations = host_iter->inboundAssociatedLocations();
          fallbackLocation = host_iter->associatedLocationFallback();
          return;
        }
        else
        {
          OS_LOG_DEBUG(FAC_ODBC,"EntityDB::getCallerLocation - " << host_iter->location() << "/" << *domainIter << " does not own domain " << host);
        }
      }
    }

    if (!address.empty() && !host_iter->loc_restr_sbnet().empty() && !host_iter->inboundAssociatedLocations().empty())
    {
      for (EntityRecord::LocationSubnet::iterator subnetIter = host_iter->loc_restr_sbnet().begin(); subnetIter != host_iter->loc_restr_sbnet().end(); subnetIter++)
      {
        if (cidr_compare(*subnetIter, address))
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Inserting location based  on " << *subnetIter << " CIDR match for address " << address);
          locations = host_iter->inboundAssociatedLocations();
          fallbackLocation = host_iter->associatedLocationFallback();
          return;
        }
        else
        {
          OS_LOG_DEBUG(FAC_ODBC,"EntityDB::getCallerLocation - " << host_iter->location() << "/" << *subnetIter << " does not own address " << address);
        }
      }
    }
  }
}

bool EntityDB::findByUserId(const std::string& uid, EntityRecord& entity) const
{
    MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));

    std::string userId = validate_identity_string(uid);

    OS_LOG_INFO(FAC_ODBC, "EntityDB::findByUserId - Finding entity record for " << userId << " from namespace " << _ns);

    // Check if the entity is already cached
    ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(userId);
    if (pCacheObj) {
        OS_LOG_DEBUG(FAC_ODBC, userId << " is present in namespace " << _ns << " (CACHED)");
        entity = *pCacheObj;
        return true;
    }
    try {

        // Create a MongoConnection instance for the current operation
        MongoDB::MongoConnection connection(_info);

        readTimer.setDBConnOK(connection.ok());

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Build the query to search for the entity by userId
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(EntityRecord::userId_fld()), userId));

        mongocxx::options::find findOptions;
        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        // Execute the query
        std::optional<bsoncxx::document::value> maybeResult = collection.find_one(queryBuilder.view(), findOptions);

        if (maybeResult) {
            bsoncxx::document::view result = maybeResult->view();
            entity = result;  // Convert BSON document to EntityRecord
            //
            // Cache the entity
            //
            const_cast<ExpireCache&>(_cache).add(userId, ExpireCacheable(new EntityRecord(entity)));

            OS_LOG_DEBUG(FAC_ODBC, "EntityDB::findByUserId - Found entity record for " << userId << " from namespace " << _ns);
            return true;
        }
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "EntityDB::findByUserId - Error while querying MongoDB: " << e.what());
    }

    OS_LOG_INFO(FAC_ODBC, "EntityDB::findByUserId - Unable to find entity record for " << userId << " from namespace " << _ns);
    return false;
}

bool EntityDB::findByIdentityOrAlias(const Url& uri, EntityRecord& entity) const
{
	UtlString identity;
	UtlString userId;
	uri.getIdentity(identity);
	uri.getUserId(userId);
	return findByIdentityOrAlias(identity.str(), userId.str(), entity);
}

bool EntityDB::findByIdentityOrAlias(const string& identity, const string& alias,
		EntityRecord& entity) const
{
	bool found = false;
	if (!identity.empty())
		found = findByIdentity(identity, entity);

	if (!found && !alias.empty())
		found = findByAliasUserId(alias, entity);

	return found;
}

bool EntityDB::findByAliasUserId(const std::string& alias, EntityRecord& entity) const
{
    MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));

    ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(alias);
    if (pCacheObj)
    {
        OS_LOG_DEBUG(FAC_ODBC,
            "EntityDB::findByAliasUserId - " << alias <<
            " is present in namespace " << _ns << " (CACHED)");
        entity = *pCacheObj;
        return true;
    }

    try
    {
        bsoncxx::builder::basic::document queryBuilder;

        queryBuilder.append(
            bsoncxx::builder::basic::kvp(
                std::string(EntityRecord::aliases_fld()),
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp(
                        std::string("$elemMatch"),
                        bsoncxx::builder::basic::make_document(
                            bsoncxx::builder::basic::kvp(
                                std::string(EntityRecord::aliasesId_fld()),
                                alias
                            )
                        )
                    )
                )
            )
        );

        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        mongocxx::options::find findOptions;
        findOptions.max_time(
            std::chrono::milliseconds(_info.getReadQueryTimeoutMs())
        );

        std::optional<bsoncxx::document::value> maybeResult =
            collection.find_one(queryBuilder.view(), findOptions);

        if (maybeResult)
        {
            bsoncxx::document::view result = maybeResult->view();

            entity = result;

            // ---- Cache result ----
            const_cast<ExpireCache&>(_cache).add(
                alias,
                ExpireCacheable(new EntityRecord(entity))
            );

            OS_LOG_DEBUG(FAC_ODBC,
                "EntityDB::findByAliasUserId - Found entity record for alias "
                << alias << " from namespace " << _ns);

            return true;
        }
    }
    catch (const std::exception& e)
    {
        OS_LOG_ERROR(FAC_ODBC,
            "EntityDB::findByAliasUserId - Error while querying MongoDB: "
            << e.what());
    }

    OS_LOG_INFO(FAC_ODBC,
        "EntityDB::findByAliasUserId - Unable to find entity record for alias "
        << alias << " from namespace " << _ns);

    return false;
}

bool EntityDB::findByAliasIdentity(const std::string& identity, EntityRecord& entity) const
{
  std::vector<std::string> tokens;
  boost::split(tokens, identity, boost::is_any_of("@"), boost::token_compress_on);
  if (tokens.size() != 2)
    return false;
  std::string userId = tokens[0];
  std::string host = tokens[1];
  if (!findByAliasUserId(userId, entity))
    return false;
  size_t i = entity.identity().rfind(host);
  return (i != std::string::npos) && (i == (entity.identity().length() - host.length()));
}

/// Retrieve the SIP credential check values for a given identity and realm
bool EntityDB::getCredential(const Url& uri, const UtlString& realm, UtlString& userid, UtlString& passtoken,
		UtlString& authType) const
{
	UtlString identity;
	uri.getIdentity(identity);

	EntityRecord entity;
	if (!findByIdentity(identity.str(), entity))
		return false;

	if (entity.realm() != realm.str())
		return false;

	userid = entity.userId();
	passtoken = entity.password();
	authType = entity.authType();

	return true;
}

/// Retrieve the SIP credential check values for a given userid and realm
bool EntityDB::getCredential(const UtlString& userid, const UtlString& realm, Url& uri, UtlString& passtoken,
		UtlString& authType) const
{
	EntityRecord entity;
	if (!findByUserId(userid.str(), entity))
		return false;

	if (entity.realm() != realm.str())
		return false;

	uri = entity.identity().c_str();
	passtoken = entity.password();
	authType = entity.authType();

	return true;
}

void EntityDB::getAliasContacts(const Url& aliasIdentity, Aliases& aliases, bool& isUserIdentity, UtlString& userIdentity) const
{
  UtlString alias;
	aliasIdentity.getUserId(alias);
	if (alias.isNull())
		return;

  UtlString identity;
	aliasIdentity.getIdentity(identity);
  if (identity.isNull())
		return;

	EntityRecord entity;
	if (findByAliasIdentity(identity.str(), entity))
	{
		Aliases result = entity.aliases();
		for (Aliases::iterator iter = result.begin(); iter != result.end(); iter++)
		{
			if (iter->id == alias.data())
				aliases.push_back(*iter);
		}
		isUserIdentity = !entity.realm().empty() && !entity.password().empty();
    userIdentity = entity.identity().c_str();
	}
}

void EntityDB::getAliasContacts(const Url& aliasIdentity, Aliases& aliases, bool& isUserIdentity) const
{
	UtlString identity;
  getAliasContacts(aliasIdentity, aliases, isUserIdentity, identity);
}

bool EntityDB::findByIdentity(const Url& uri, EntityRecord& entity) const
{
	UtlString identity;
	uri.getIdentity(identity);
	return findByIdentity(identity.str(), entity);
}


bool EntityDB::tail(std::vector<std::string>& opLogs) {
    // minKey is smaller than any other possible value
    static bool hasLastTailId = false;

    if (!hasLastTailId) {
        MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));

        // Create MongoConnection instance for the operation
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        // Build query to find entries greater than _lastTailId
        bsoncxx::builder::basic::document queryBuilder;
        bsoncxx::builder::basic::document idQuery;
        idQuery.append(bsoncxx::builder::basic::kvp(std::string("$gt"), _lastTailId.get_oid().value));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("_id"), idQuery.view()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("ns"), NS));

        // Create the query document
        bsoncxx::document::value queryDoc = queryBuilder.extract();
        bsoncxx::document::view query = queryDoc.view();

        try {

            // Access the target collection
            mongocxx::collection collection = connection.collection(_ns);

            mongocxx::options::find findOptions;
            findOptions.cursor_type(mongocxx::cursor::type::k_tailable);
            findOptions.max_time(std::chrono::seconds(10));

            mongocxx::cursor cursor = collection.find(query, findOptions);

            for (bsoncxx::document::view doc : cursor) {
                // Ensure _id exists
                bsoncxx::document::element id_element = doc["_id"];
                if (id_element.type() == bsoncxx::type::k_oid) {
                    _lastTailId = id_element;
                }
                 else {
                    OS_LOG_ERROR(FAC_ODBC, "Unexpected _id type in MongoDB document.");
                    return false;
                }
                hasLastTailId = true;
            }
        } catch (const std::exception& e) {
            OS_LOG_ERROR(FAC_ODBC, "Error while querying MongoDB: " << e.what());
            return false;
        }
    }

    // Re-query for logs after the last tail ID has been set
    bsoncxx::builder::basic::document queryBuilder2;
    queryBuilder2.append(bsoncxx::builder::basic::kvp(std::string("_id"), 
        bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), _lastTailId.get_oid().value))
    ));
    queryBuilder2.append(bsoncxx::builder::basic::kvp(std::string("ns"), NS));

    bsoncxx::document::value queryDoc2 = queryBuilder2.extract();
    bsoncxx::document::view query2 = queryDoc2.view();

    try {
        MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
        
        MongoDB::MongoConnection connection(_info);
        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection opLogCollection = connection.collection("local.oplog");

        mongocxx::options::find findOptions;
        findOptions.cursor_type(mongocxx::cursor::type::k_tailable);
        findOptions.max_time(std::chrono::seconds(10));

        mongocxx::cursor cursor = opLogCollection.find(query2, findOptions);

        for (bsoncxx::document::view doc : cursor) {
            bsoncxx::document::element id_element = doc["_id"];
            if (id_element.type() == bsoncxx::type::k_oid) {
                _lastTailId = id_element;
            }

            opLogs.push_back(bsoncxx::to_json(doc));
        }
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while querying MongoDB: " << e.what());
        return false;
    }

    return !opLogs.empty();
}

