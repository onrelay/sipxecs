
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

#include <chrono>
#include <thread>
#include <string>
#include <vector>
#include <map>
#include <utility>
 
#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/document/value.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/json.hpp>
#include <bsoncxx/types.hpp>
#include <bsoncxx/types/bson_value/view.hpp>
#include <bsoncxx/types/bson_value/value.hpp>

#include <mongocxx/exception/exception.hpp>
#include <mongocxx/options/find.hpp>
#include <mongocxx/cursor.hpp>

#include <sipdb/MongoDB.h> 
#include <sipdb/MongoOpLog.h> 

#include <os/OsLogger.h>
#include <os/OsDateTime.h>

const std::string MongoOpLog::NS("local.oplog.rs");

const int MongoOpLog::MULTIPLIER = 1000;

const char* MongoOpLog::ts_fld(){static std::string name = "ts"; return name.c_str();}
const char* MongoOpLog::op_fld(){static std::string name = "op"; return name.c_str();}
void MongoOpLog::createOpLogDataMap(OpLogDataMap& opLogDataMap)
{
  opLogDataMap.insert(std::pair<std::string, OpLogType>("u", Update));
  opLogDataMap.insert(std::pair<std::string, OpLogType>("i", Insert));
  opLogDataMap.insert(std::pair<std::string, OpLogType>("d", Delete));
}


MongoOpLog::MongoOpLog(const MongoDB::ConnectionInfo& info,
                      const bsoncxx::builder::basic::document& customQuery,
                      const int querySleepTime,
                      const unsigned long startFromTimestamp)
                    : MongoDB::BaseDB(info, NS),
                    _isRunning(false),
                    _pThread(nullptr),
                    _querySleepTime(querySleepTime > 0 ? querySleepTime : 60),  // Default to 60 if invalid
                    _startFromTimestamp(startFromTimestamp),
                    _lastEntry(bsoncxx::builder::basic::make_document().view())
{ 
    // Copy the contents of customQuery into _customQuery
    _customQuery.append(bsoncxx::builder::basic::concatenate(customQuery.view()));

    OS_LOG_INFO(FAC_SIP, "MongoOpLog::MongoOpLog:"
                          << " entering"
                          << " querySleepTime=" << querySleepTime
                          << " startFromTimestamp=" << startFromTimestamp);

    createOpLogDataMap(_opLogDataMap);
}

MongoOpLog::~MongoOpLog()
{
  stop();
}

void MongoOpLog::registerCallback(OpLogType type, OpLogCallBack cb)
{
  if (type < Insert ||
      type > OpLogTypeNumber)
  {
    OS_LOG_ERROR(FAC_SIP, "MongoOpLog::registerCallback:" <<
                          " invalid callback type");

    return;
  }

  OS_LOG_INFO(FAC_SIP, "MongoOpLog::registerCallback:" <<
                        " entering" <<
                        " type=" << type);

  if (type < OpLogTypeNumber)
    _opLogCbVectors[type].push_back(cb);
}

bool MongoOpLog::run()
{
  OS_LOG_INFO(FAC_SIP, "MongoOpLog::run:" <<
                        " starting MongoOpLog thread");

  bool rc = prepareFirstEntry(_lastEntry);
  if (false == rc)
  {
    OS_LOG_ERROR(FAC_SIP, "MongoOpLog::run" <<
                 " exited with error");
    return rc;
  }

  _isRunning = true;
  _pThread = new boost::thread(boost::bind(&MongoOpLog::internal_run_esafe, this));

  return true;
}

void MongoOpLog::requestStop()
{
  OS_LOG_INFO(FAC_SIP, "MongoOpLog::requestStop notify MongoOpLog thread");
  _isRunning = false;
}

void MongoOpLog::stop()
{
  requestStop();

  OS_LOG_INFO(FAC_SIP, "MongoOpLog::stop:" <<
                       " stopping MongoOpLog thread");

  if (_pThread)
  {
    _pThread->join();

    delete _pThread;
    _pThread = 0;
  }

  OS_LOG_INFO(FAC_SIP, "MongoOpLog::stop:" <<
                       " exiting");
}

// The monitor thread stays in this function as long as the cursor is not dead
// If the cursor already processed lastEntryObj, it gets blocked in a while in
// cursor->more() for 1 or 2 seconds until new entries are added to this collection.
// After that the entries are processed and the thread gets blocked again in
// cursor->more function
bool  MongoOpLog::processQuery(mongocxx::cursor& cursor,
                              bsoncxx::document::value& lastEntry)
{
    OS_LOG_INFO(FAC_SIP, "MongoOpLog::processQuery: entering");

    // Check if the cursor is empty
    if (cursor.begin() == cursor.end()) {
        OS_LOG_ERROR(FAC_SIP, "MongoOpLog::processQuery - Cursor is empty or invalid");
        return false;
    }

    for (bsoncxx::document::view doc : cursor) {
        if (!_isRunning) {
            break;
        }

        // Store the last processed document
        lastEntry = bsoncxx::document::value(doc);
        runCallBacks(lastEntry);
    }

    return true;
}

void MongoOpLog::createQuery(bsoncxx::builder::basic::document& queryBuilder, const bsoncxx::document::view& lastEntry )
{
    OS_LOG_INFO(FAC_SIP, "MongoOpLog::createQuery: entering");

    // Add the timestamp condition
    bsoncxx::document::element tsElement = lastEntry[ts_fld()];
    if (tsElement) {
        queryBuilder.append(
            bsoncxx::builder::basic::kvp(
                std::string(ts_fld()), 
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp(std::string("$gt"), tsElement.get_value()))));
    }

    // Add custom query elements if they exist
    if (!_customQuery.view().empty()) {
        for (bsoncxx::document::element element : _customQuery.view()) {
            queryBuilder.append(bsoncxx::builder::basic::kvp(element.key(), element.get_value()));
        }
    }
}

bool MongoOpLog::prepareFirstEntry(bsoncxx::document::value& lastEntry)
{
    OS_LOG_INFO(FAC_SIP, "MongoOpLog::prepareFirstEntry: entering");

    if (_startFromTimestamp == 0) 
    {
        // Use MongoDB's MinKey equivalent
        lastEntry = bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string(ts_fld()), bsoncxx::types::b_minkey{})
        );
    } 
    else 
    {
        bsoncxx::builder::basic::document builder;

        // Extract the seconds part
        uint32_t seconds = static_cast<uint32_t>(_startFromTimestamp);

        // Append the timestamp with correct struct
        builder.append(bsoncxx::builder::basic::kvp(
            std::string(ts_fld()),
            bsoncxx::types::b_timestamp{0, seconds} 
        ));

        lastEntry = builder.extract();

        // Verify the created BSONElement has the correct timestamp
        bsoncxx::types::b_timestamp ts = lastEntry.view()[ts_fld()].get_timestamp();
        if (_startFromTimestamp * MULTIPLIER != ts.timestamp) 
        {
            OS_LOG_ERROR(FAC_SIP, "MongoOpLog::prepareFirstEntry: time stamps are different " <<
                                      (_startFromTimestamp * MULTIPLIER) << " != " << ts.timestamp);
            return false;
        }
    }

    return true;
}

void MongoOpLog::internal_run_esafe()
{
  OS_LOG_INFO(FAC_SIP, "MongoOpLog::internal_run_esafe:"
              << " entering");

  while (_isRunning)
  {
    try
    {
      internal_run();
    }
    #ifdef MONGO_assert
    catch (mongocxx::exception& e)
    {
      OS_LOG_ERROR( FAC_SIP, "MongoOpLog::internal_run_esafe Mongo DB Exception: "
          << e.what());
    }
    #endif //MONGO_assert
    catch (boost::exception& e)
    {
      OS_LOG_ERROR( FAC_SIP, "MongoOpLog::internal_run_esafe Boost Library Exception: "
          << boost::diagnostic_information(e));
    }
    catch (std::exception& e)
    {
      OS_LOG_ERROR( FAC_SIP, "MongoOpLog::internal_run_esafe Standard Library Exception: "
          << e.what());
    }
    catch (...)
    {
      OS_LOG_ERROR( FAC_SIP, "MongoOpLog::internal_run_esafe Exception: Unknown exception");
    }

    if (_isRunning)
    {
      // reset timestamp to check for entries right after the exception was triggered
      _startFromTimestamp = OsDateTime::getSecsSinceEpoch();
      // sleep for some time to give mongo time to recover
      sleep(EXCEPTION_RECOVER_TIME_SEC);
    }
  }

  OS_LOG_INFO(FAC_SIP, "MongoOpLog::internal_run_esafe:"
              << " exiting");
}

void MongoOpLog::internal_run()
{
    OS_LOG_INFO(FAC_SIP, "MongoOpLog::internal_run: entering");

    bsoncxx::builder::basic::document queryBuilder;
    createQuery(queryBuilder, _lastEntry);

    // Initialize MongoConnection
    MongoDB::MongoConnection mongoConnection(_info.getConnectionUri());

    if (!mongoConnection.ok())
    {
        OS_LOG_ERROR(FAC_SIP, "MongoOpLog::internal_run: Failed to connect to MongoDB");
        return;
    }

    auto collection = mongoConnection.collection(_ns);

    while (_isRunning)
    {
        try
        {
            // Set options for a tailable cursor
            auto options = mongocxx::options::find{};
            options.cursor_type(mongocxx::cursor::type::k_tailable_await);

            // Execute the query
            auto cursor = collection.find(queryBuilder.view(), options);

            bool rc = processQuery(cursor, _lastEntry);
            if (!rc)
            {
                break;
            }

            // Recreate the query with the updated `_lastEntry`
            queryBuilder.clear();
            createQuery(queryBuilder,_lastEntry);
        }
        catch (const mongocxx::exception& e)
        {
            OS_LOG_ERROR(FAC_SIP, "MongoOpLog::internal_run: MongoDB query failed: " + std::string(e.what()));
            break;
        }
    }

    OS_LOG_INFO(FAC_SIP, "MongoOpLog::internal_run: exiting");
}

bool MongoOpLog::getOpLogType(const std::string& operationType,
                              OpLogType& opLogType)
{
  OpLogDataMap::iterator it;

  it = _opLogDataMap.find(operationType);
  if (_opLogDataMap.end() != it)
  {
    opLogType = it->second;

    return true;
  }

  return false;
}

void MongoOpLog::runCallBacks(const bsoncxx::document::value& bSONObj)
{
    std::string type = std::string(bSONObj[op_fld()].get_string());
    std::string opLog = bsoncxx::to_json(bSONObj.view());

    OS_LOG_DEBUG(FAC_SIP, "MongoOpLog::notifyCallBacks:" <<
                  " type=" << type <<
                  " opLog=" << opLog);

    if (type.empty())
    {
        OS_LOG_WARNING(FAC_SIP, "MongoOpLog::notifyCallBacks: type is empty");
        return;
    }

    // Notify all subscribers
    for (auto& callback : _opLogCbVectors[All])
    {
        callback(bSONObj);
    }

    OpLogType opLogType;
    bool found = getOpLogType(type, opLogType);
    if (!found)
    {
        return;
    }

    // Notify specific subscribers
    for (auto& callback : _opLogCbVectors[opLogType])
    {
        callback(bSONObj);
    }
}

