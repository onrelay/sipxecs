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

#ifndef MONGODB_H
#define	MONGODB_H

#include <queue>
#include <vector>
#include <assert.h>
#include <exception>

#if !defined(BOOST_BIND_GLOBAL_PLACEHOLDERS)
  #define BOOST_BIND_GLOBAL_PLACEHOLDERS
#endif
#include <boost/format.hpp>
#include <boost/bind.hpp>
#include <boost/thread.hpp>
#include <boost/function.hpp>
#include <boost/asio.hpp>
#include <boost/format.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/shared_array.hpp>
#include <boost/circular_buffer.hpp>
#include <boost/program_options.hpp>
#include <boost/filesystem/operations.hpp>
#include <boost/exception/all.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/date_time/gregorian/greg_date.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/local_time_adjustor.hpp>
#include <boost/date_time/c_local_time_adjustor.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <boost/property_tree/detail/ptree_utils.hpp>


#include <os/OsTime.h>

#include <mongocxx/client.hpp>  
#include <mongocxx/database.hpp>  
#include <mongocxx/collection.hpp>  
#include <mongocxx/uri.hpp>  
#include <mongocxx/exception/exception.hpp>

#include <bsoncxx/document/view.hpp>

// cannot seem to redfine it so safer to undefine it
#undef VERSION

#define BSON_NOT_EQUAL(val) BSON("$ne"<< val)
#define BSON_LESS_THAN(val) BSON("$lt"<< val)
#define BSON_LESS_THAN_EQUAL(val) BSON("$lte"<< val)
#define BSON_GREATER_THAN(val) BSON("$gt" << val)
#define BSON_GREATER_THAN_EQUAL(val) BSON("$gte" << val)
#define BSON_ELEM_MATCH(val) BSON("$elemMatch" << val)
#define BSON_OR(val) BSON("$or" << val)

#define MONGODB_EXPIRES_AFTER_SECONDS_MINIMUM_SECS 1

typedef boost::error_info<struct tag_errmsg, std::string> errmsg_info;

namespace MongoDB
{




class ConfigError: public boost::exception, public std::exception {
public:
    ConfigError() {
    }
};

class ConnectionInfo
{
public:
  ConnectionInfo();

	ConnectionInfo(const ConnectionInfo& rhs);

  ConnectionInfo(const std::string& connectionUrl);

  ConnectionInfo(const mongocxx::uri& connectionUrl);

	ConnectionInfo(std::ifstream& configFile);

	~ConnectionInfo()
	{
	}

  ConnectionInfo& operator=(const ConnectionInfo& conn);

	static ConnectionInfo globalInfo();
	static ConnectionInfo localInfo();

	static bool	testConnection(const mongocxx::uri& connectionUrl, std::string& errmsg);

  const mongocxx::uri& getConnectionUri() const
	{
    return _connectionUrl;
	}
	

	const int getShardId() const
	{
		return _shard;
	}
	

	const bool useReadTags() const
	{
		return _useReadTags;
	}

	void enableReadTags(bool enable)
	{
		_useReadTags = enable;
	}

	const bool isEmpty() const
	{
		return _connectionUrl.to_string().empty();
	}

  const std::string& getClusterId() const
  {
    return _clusterId;
  }

  const std::int64_t getReadQueryTimeoutMs() const
  {
    return _readQueryTimeoutMs;
  }

  const std::int64_t getWriteQueryTimeoutMs() const
  {
    return _writeQueryTimeoutMs;
  }

  const double getReadQueryTimeout() const
  {
    double readQueryTimeout = _readQueryTimeoutMs;
    return readQueryTimeout/1000;
  }

  const double getWriteQueryTimeout() const
  {
    double writeQueryTimeout = _writeQueryTimeoutMs;
    return writeQueryTimeout/1000;
  }

private:

  mongocxx::uri _connectionUrl; 
  std::int32_t _shard;
  bool _useReadTags;
  std::string _clusterId;
  std::int64_t _readQueryTimeoutMs;
  std::int64_t _writeQueryTimeoutMs;
};

class MongoConnection {
  public:
    // Constructor to initialize the client with connection info
    MongoConnection(const ConnectionInfo& connectionInfo);

    MongoConnection(const std::string& connectionUrl);

    MongoConnection(const mongocxx::uri& connectionUrl);

    // Getter for the raw mongocxx::client object
    mongocxx::client& client();

    // Method to get a database from the client
    mongocxx::database database(const std::string& ns);

    // Method to get a collection from the client
    mongocxx::collection collection(const std::string& ns);

    static std::string databaseName(const std::string& ns);

    static std::string collectionName(const std::string& ns);

    bool ok();

  private:
    // The unique pointer that holds the mongocxx::client
    std::unique_ptr<mongocxx::client> _ptr;
};

class UpdateTimer;
class ReadTimer;

class BaseDB
{
public:
	BaseDB(const ConnectionInfo& info, const std::string& ns);

	virtual ~BaseDB()
	{
	}

	// This does something for each record. Efficient because it doesn't store each record into a collection
	// then pass back the collection for you to iterate over.
	//
	// NOTE: You can easily load _all_ objects which would prohibit your function from working on a large
	// production system depending on the circumstance.
	//
	// Example:
	//   include <boost/bind.hpp>
	//
	//   class X {
	//      void y(bsoncxx::document::view o) {
	//         println("%s\n", o.getStringField("a"));
	//      }
	//   };
	//
	//   BaseDB d(info);
	//   X z;
	//   bsoncxx::document::view all;
	//   d.forEach(all, bind(&X::y, &z, _1));   // _1 is required means a single argument
	//
	void forEach(const bsoncxx::document::view& query, const std::string& ns, boost::function<void(const bsoncxx::document::view&)> doSomething);

  void primaryPreferred(mongocxx::options::find& findOptions ) const;

  void nearest(mongocxx::options::find& findOptions ) const;

	void  setReadPreference(bsoncxx::builder::basic::document& builder, const bsoncxx::document::view& query, const char* readPreferrence) const;

	const int getShardId() const { return _info.getShardId(); };

	const bool useReadTags() const { return _info.useReadTags(); };

  const std::string& getClusterId() const { return _info.getClusterId(); }

  void registerTimer(const UpdateTimer* pTimer);

  void registerTimer(const ReadTimer* pTimer);

  std::int64_t getUpdateAverageSpeed() const;

  std::int64_t getLastUpdateSpeed() const;

  std::int64_t getReadAverageSpeed() const;

  std::int64_t getLastReadSpeed() const;

  const double getReadQueryTimeout() const { double readQueryTimeout = _info.getReadQueryTimeoutMs(); return readQueryTimeout/1000; }

  const double getWriteQueryTimeout() const { double writeQueryTimeout = _info.getWriteQueryTimeoutMs(); return writeQueryTimeout/1000; }

  //
  // Construct final read and write queries by setting the maximum
  // time (in milliseconds) the queries will be available in the server part
  //
  bsoncxx::document::value queryMaxTimeMS(const bsoncxx::document::view& obj, std::int64_t maxTimeMs) const;
  bsoncxx::document::value readQueryMaxTimeMS(const bsoncxx::document::view& obj) const;
  bsoncxx::document::value writeQueryMaxTimeMS(const bsoncxx::document::view& obj) const;

  //
  // Gets a bsoncxx::types::b_date object from the given epoch time in seconds
  //
  static bsoncxx::types::b_date dateFromSecsSinceEpoch(unsigned long timestamp);

  //
  // Drops/Removes the specified index keys in a safe way (i.e. catching mongo's possible exceptions)
  //
  bool safeDropIndex(mongocxx::collection& collection, const std::string& key) const;

  //
  // Ensures the creation of a TTL index in a safe way (i.e. catching mongo's possible exceptions)
  //
  bool safeEnsureTTLIndex(mongocxx::collection& collection, const std::string& key, int ttlSeconds) const;


protected:
  std::string _ns;
	mutable ConnectionInfo _info;
  boost::circular_buffer<std::int64_t> _updateTimerSamples;
  boost::circular_buffer<std::int64_t> _readTimerSamples;
  mutable boost::mutex _updateTimerSamplesMutex;
  mutable boost::mutex _readTimerSamplesMutex;
  std::int64_t _lastReadSpeed;
  std::int64_t _lastUpdateSpeed;
  long _lastAlarmLog;
};

class UpdateTimer
{
public:
  UpdateTimer(BaseDB& db);
  ~UpdateTimer();
  inline void setDBConnOK(bool state) {_isDBConnOK = state;};

protected:
  std::int64_t _start;
  std::int64_t _end;
  BaseDB& _db;
  bool _isDBConnOK;
  friend class BaseDB;
};

class ReadTimer
{
public:
  ReadTimer(BaseDB& db);
  ~ReadTimer();
  inline void setDBConnOK(bool state) {_isDBConnOK = state;};

protected:
  std::int64_t _start;
  std::int64_t _end;
  BaseDB& _db;
  bool _isDBConnOK;
  friend class BaseDB;
};

class MongoException : public std::runtime_error {
public:
  explicit MongoException(const std::string& message)
      : std::runtime_error("MongoException: " + message) {}
};


} // namespace MongoDB

#endif	/* MONGODB_H */

