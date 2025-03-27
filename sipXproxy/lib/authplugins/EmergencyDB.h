// Copyright (c) eZuce, Inc. All rights reserved.
// Contributed to SIPfoundry under a Contributor Agreement
//
// This software is free software; you can redistribute it and/or modify it under
// the terms of the Affero General Public License (AGPL) as published by the
// Free Software Foundation; either version 3 of the License, or (at your option)
// any later version.
//
// This software is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
// details.

#include <string>

#include "sipdb/MongoDB.h"
#include "sipdb/EntityDB.h"

#include "os/OsLogger.h"

#include <bsoncxx/document/view.hpp>


class EmergencyDB : public EntityDB
{
public:
  EmergencyDB(const MongoDB::ConnectionInfo& info) : EntityDB(info) {}

  bool findE911LineIdentifier(
    const std::string& userId,
    std::string& e911,
    std::string& address,
    std::string& location);

  bool findE911InstrumentIdentifier(
    const std::string& instrument,
    std::string& e911,
    std::string& address,
    std::string& location);

  bool findE911Location(
    MongoDB::MongoConnection& conn,
    const std::string& e911,
    std::string& address,
    std::string& location);
};

bool EmergencyDB::findE911Location(
    MongoDB::MongoConnection& conn,
    const std::string& e911,
    std::string& address,
    std::string& location)
{
    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("ent"), "e911location"));
    queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("elin"), e911));

    mongocxx::options::find findOptions;
    BaseDB::nearest(findOptions);

    std::optional<bsoncxx::document::value> result = conn.collection(ns()).find_one(queryBuilder.view(), findOptions);
    if (result) {
        bsoncxx::document::view doc = result->view();
        bsoncxx::document::element addrinfo = doc["addrinfo"];
        if (addrinfo && addrinfo.type() == bsoncxx::type::k_string) {
            address = std::string(addrinfo.get_string().value);
        }

        bsoncxx::document::element loctn = doc["loctn"];
        if (loctn && loctn.type() == bsoncxx::type::k_string) {
            location = std::string(loctn.get_string().value);
        }
    }
    return true;
}

bool EmergencyDB::findE911LineIdentifier(
  const std::string& userId,
  std::string& e911,
  std::string& address,
  std::string& location)
{
    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(bsoncxx::builder::basic::kvp(std::string(EntityRecord::identity_fld()), userId));

    MongoDB::MongoConnection conn(_info);

    mongocxx::options::find findOptions;
    BaseDB::nearest(findOptions);

    std::optional<bsoncxx::document::value> entityObj = conn.collection(ns()).find_one(queryBuilder.view(), findOptions);
    if (entityObj) {
        bsoncxx::document::view doc = entityObj->view();

        bsoncxx::document::element elin = doc["elin"];
        if (elin && elin.type() == bsoncxx::type::k_string) {
            e911 = std::string(elin.get_string().value);
            if (!e911.empty()) {
                findE911Location(conn, e911, address, location);
            }
            return !e911.empty();
        }
    }
    return false;
}

bool EmergencyDB::findE911InstrumentIdentifier(
    const std::string& instrument,
    std::string& e911,
    std::string& address,
    std::string& location)
{
    OS_LOG_INFO(FAC_SIP, "");

    bsoncxx::builder::basic::document queryBuilder;
    queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("mac"), instrument));

    MongoDB::MongoConnection conn(_info);

    mongocxx::options::find findOptions;
    BaseDB::nearest(findOptions);

    std::optional<bsoncxx::document::value> instrumentObj = conn.collection(ns()).find_one(queryBuilder.view(), findOptions);
    if (instrumentObj) {
        bsoncxx::document::view doc = instrumentObj->view();
        bsoncxx::document::element elin = doc["elin"];
        if (elin && elin.type() == bsoncxx::type::k_string) {
            e911 = std::string(elin.get_string().value);
            if (!e911.empty()) {
                findE911Location(conn, e911, address, location);
            }
            return !e911.empty();
        }
    }
    return false;
}
