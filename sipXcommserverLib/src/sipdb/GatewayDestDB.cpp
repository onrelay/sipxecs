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

#include <fstream>
#include <string>
#include <exception>
#include <os/OsDateTime.h>
#include <os/OsLogger.h>

#include "sipdb/GatewayDestDB.h"
#include "sipdb/RegExpireThread.h"
#include "sipdb/MongoDB.h"
#include "sipdb/GatewayDestRecord.h"  

#include <mongocxx/client.hpp>        
#include <mongocxx/collection.hpp>    
#include <mongocxx/database.hpp>     

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/json.hpp>

using namespace std;

const string GatewayDestDB::NS("node.gatewaydest");

void GatewayDestDB::updateRecord(const GatewayDestRecord& record, bool upsert)
{
    if (!upsert)
    {
        OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::updateRecord - adding record for "
            " callid " << record.getCallId() <<
            " toTag " << record.getToTag() <<
            " fromTag " << record.getFromTag());
    }
    else
    {
        OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::updateRecord - updating record for "
            " callid " << record.getCallId() <<
            " toTag " << record.getToTag() <<
            " fromTag " << record.getFromTag() <<
            " identity " << record.getIdentity() <<
            " lineId " << record.getLineId() <<
            " expirationTime " << record.getExpirationTime());
    }

    try {
        // Create the query document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::callIdField()), record.getCallId()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::toTagField()), record.getToTag()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::fromTagField()), record.getFromTag()));

        // Create the update document
        bsoncxx::builder::basic::document setBuilder;
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::callIdField()), record.getCallId()));
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::toTagField()), record.getToTag()));
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::fromTagField()), record.getFromTag()));
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::identityField()), record.getIdentity()));
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::lineIdField()), record.getLineId()));
        setBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::expirationTimeField()), record.getExpirationTime()));

        bsoncxx::builder::basic::document updateBuilder;
        updateBuilder.append(bsoncxx::builder::basic::kvp("$set", setBuilder.view()));

        // Use MongoConnection to handle the DB operations
        MongoDB::MongoConnection connection(_info);
        
        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        collection.update_one(queryBuilder.view(), updateBuilder.view(), 
                              mongocxx::options::update{}.upsert(upsert));
        
        // Ensure indexes
        collection.create_index(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::callIdField()), 1)));
        collection.create_index(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::expirationTimeField()), 1)));

        // Perform any clean-up logic
        removeAllExpired();

    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while updating GatewayDest record: " << e.what());
    }
}

void GatewayDestDB::removeRecord(const GatewayDestRecord& record)
{
    OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::removeRecord - "
        " callid " << record.getCallId() <<
        " toTag " << record.getToTag() <<
        " fromTag " << record.getFromTag());

    try {
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::callIdField()), record.getCallId()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::toTagField()), record.getToTag()));
        queryBuilder.append(bsoncxx::builder::basic::kvp(
            std::string(GatewayDestRecord::fromTagField()), record.getFromTag()));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);
        
        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Perform the remove operation
        collection.delete_one(queryBuilder.view());

        // Ensure indexes
        collection.create_index(
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::callIdField()), 1)
            )
        );

        collection.create_index(
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::expirationTimeField()), 1)
            )
        );

    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while removing GatewayDest record: " << e.what());
    }
}

void GatewayDestDB::removeAllRecords()
{
    OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::removeAllRecords");

    // Create an empty BSON document to match all records
    bsoncxx::builder::basic::document all;

    try {
        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Remove all records from the collection
        collection.delete_many(all.view());

    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while removing all GatewayDest records: " << e.what());
    }
}

void GatewayDestDB::removeAllExpired()
{
    std::int64_t timeNow = static_cast<std::int64_t>(OsDateTime::getSecsSinceEpoch());
    OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::removeAllExpired - timeNow " << timeNow);

    try {

        bsoncxx::builder::basic::document query;
        query.append(
            bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::expirationTimeField()), 
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$lte"), timeNow))));

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        // Remove records with expired expirationTime
        collection.delete_many(query.view());

        // Ensure indices
        collection.create_index(
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::callIdField()), 1)));
        collection.create_index(
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::expirationTimeField()), 1)));
        
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while removing expired GatewayDest records: " << e.what());
    }
}

bool GatewayDestDB::getUnexpiredRecord(GatewayDestRecord& record) const
{
    std::int64_t timeNow = OsDateTime::getSecsSinceEpoch();

    OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::getUnexpiredRecord for "
        " callid " << record.getCallId() <<
        " toTag " << record.getToTag() <<
        " fromTag " << record.getFromTag() <<
        " timeNow" << timeNow);

    try {

        bsoncxx::builder::basic::document query;
        query.append(bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::callIdField()), record.getCallId()));
        query.append(bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::toTagField()), record.getToTag()));
        query.append(bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::fromTagField()), record.getFromTag()));
        query.append(
            bsoncxx::builder::basic::kvp(std::string(GatewayDestRecord::expirationTimeField()), 
            bsoncxx::builder::basic::make_document(bsoncxx::builder::basic::kvp(std::string("$gt"), timeNow))));

        OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::getUnexpiredRecord - query constructed");

        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection connection(_info);

        // Access the target collection
        mongocxx::collection collection = connection.collection(_ns);

        auto cursor = collection.find(query.view());

        if (cursor.begin() != cursor.end())
        {
            auto recordObj = *cursor.begin();

            OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::getUnexpiredRecord - found record "
                " callid " << record.getCallId() <<
                " toTag " << record.getToTag() <<
                " fromTag " << record.getFromTag() <<
                " identity " << record.getIdentity() <<
                " lineId " << record.getLineId() <<
                " expirationTime " << record.getExpirationTime());

            record = recordObj;
            return true;
        }
    } catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error while getting unexpired GatewayDest record: " << e.what());
    }

    OS_LOG_DEBUG(FAC_ODBC, "GatewayDestDB::getUnexpiredRecord - NOT found record "
        " callid " << record.getCallId() <<
        " toTag " << record.getToTag() <<
        " fromTag " << record.getFromTag() <<
        " expirationTime " << timeNow);

    return false;
}
