#include <string>
#include <iostream>
#include <fstream>
#include <memory>

#include <os/OsLogger.h>
#include <os/OsDateTime.h>

#include <mongocxx/options/client.hpp>

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/json.hpp>

#include "sipdb/MongoDB.h"

const int READ_TIMER_SAMPLES = 5; // Number of samples for getting the read delay
const int UPDATE_TIMER_SAMPLES = 5; // Number of samples for getting the update delay
const int MAX_READ_DELAY_MS = 100; // Maximum allowable delay for reads in milliseconds
const int MAX_UPDATE_DELAY_MS = 500; // Maximum allowable delay for updates in milliseconds
const int ALARM_RATE_SEC = 300; // Send alarm ever nth seconds when threshold is violated

using namespace std;

namespace pod = boost::program_options::detail;

namespace MongoDB
{
MongoConnection::MongoConnection(const ConnectionInfo& connectionInfo) :
    MongoConnection( connectionInfo.getConnectionUri() ) {
}

MongoConnection::MongoConnection(const std::string& connectionUrl) :
    MongoConnection(mongocxx::uri(connectionUrl )) {
}

MongoConnection::MongoConnection(const mongocxx::uri& connectionUrl) {

    try {
        mongocxx::options::client clientOptions;

        // Optional: Set MongoDB API version (useful for stable behavior in MongoDB 4.x+)
        mongocxx::options::server_api serverApi(mongocxx::options::server_api::version::k_version_1);
        clientOptions.server_api_opts(serverApi);

        // Initialize client
        _ptr = std::make_unique<mongocxx::client>(connectionUrl, clientOptions);
    }
    catch (const mongocxx::exception& e) {
        throw MongoException(std::string("Failed to connect to MongoDB: ") + e.what());
    }
}

 mongocxx::client& MongoConnection::client() {
  if (!_ptr) {
      throw std::runtime_error("MongoDB client is not initialized.");
  }
  return *_ptr;
}

bool MongoConnection::ok() {
  return _ptr != NULL;
}

std::string MongoConnection::databaseName(const std::string& ns) {

  auto pos = ns.find('.');
  return (pos != std::string::npos) ? ns.substr(0, pos) : ns;
}

std::string MongoConnection::collectionName(const std::string& ns) {

  auto pos = ns.find('.');

  if (pos == std::string::npos) {
      throw MongoException("No collection name in namespace: " + ns);
  }

  return ns.substr(pos + 1);
}

mongocxx::database MongoConnection::database(const std::string& ns) {
  try {
      // Ensure the client is connected before attempting to access the database
      if (!ok()) {
          throw MongoException("Failed to access database for " + ns);
      }

      std::string name = databaseName(ns);

      return _ptr->database(name);
  }
  catch (const mongocxx::exception& e) {
      throw MongoException("Failed to access database for " + ns + ": " + e.what());
  }
}

mongocxx::collection MongoConnection::collection(const std::string& ns ) {
  try {
      // Ensure the client is connected before attempting to access the database
      if (!ok()) {
          throw MongoException("MongoDB client is not connected.");
      }

      mongocxx::database database = this->database( ns );
      std::string name = collectionName( ns );

      return database[name];
  }
  catch (const mongocxx::exception& e) {
      throw MongoException("Failed to access collection for " + ns + ": " + std::string(e.what()));
  }
}

  BaseDB::BaseDB(const ConnectionInfo& info, const std::string& ns) :
    _ns(ns),
    _info(info),
    _updateTimerSamples(UPDATE_TIMER_SAMPLES),
    _readTimerSamples(READ_TIMER_SAMPLES),
    _lastReadSpeed(0),
    _lastUpdateSpeed(0),
    _lastAlarmLog(0)
  {
  }

bool ConnectionInfo::testConnection(const mongocxx::uri& connectionUrl, std::string& errmsg)
{
    bool ret = false;

    try
    {
        // Using MongoConnection to test the connection
        MongoDB::MongoConnection connection(connectionUrl);

        ret = connection.ok();
    }  
    catch (const std::exception& e)
    {
        ret = false;
        errmsg = e.what();
    }

    return ret;
}

  ConnectionInfo ConnectionInfo::globalInfo()
  {
    const char* fname = SIPX_CONFDIR "/mongo-client.ini";
    std::ifstream file(fname);
    if (!file.is_open())
    {
        BOOST_THROW_EXCEPTION(ConfigError() <<  errmsg_info(std::string("Missing file ")  + fname));
    }
    return ConnectionInfo(file);
  }

  ConnectionInfo ConnectionInfo::localInfo()
  {
    std::ifstream file(SIPX_CONFDIR "/mongo-local.ini");
    if (!file.is_open())
    {
      return ConnectionInfo();
    }
    return ConnectionInfo(file);
  }

  ConnectionInfo::ConnectionInfo()
  {
    _shard = 0;
    _useReadTags = false;
    _readQueryTimeoutMs = 0;
    _writeQueryTimeoutMs = 0;
  }

  ConnectionInfo::ConnectionInfo(const ConnectionInfo& rhs)
	{
    _connectionUrl = mongocxx::uri(rhs._connectionUrl.to_string());
    _shard = rhs._shard;
    _useReadTags = rhs._useReadTags;
    _clusterId = rhs._clusterId;
    _readQueryTimeoutMs = rhs._readQueryTimeoutMs;
    _writeQueryTimeoutMs = rhs._writeQueryTimeoutMs;
	}

  ConnectionInfo& ConnectionInfo::operator=(const ConnectionInfo& rhs)
  {
    string errmsg;
    _connectionUrl = mongocxx::uri(rhs._connectionUrl.to_string());
    _shard = rhs._shard;
    _useReadTags = rhs._useReadTags;
    _clusterId = rhs._clusterId;
    _readQueryTimeoutMs = rhs._readQueryTimeoutMs;
    _writeQueryTimeoutMs = rhs._writeQueryTimeoutMs;
    return *this;
  }

  ConnectionInfo::ConnectionInfo(const std::string& connectionUrl) :
                 _connectionUrl(mongocxx::uri(connectionUrl)),
                 _shard(0),
                 _useReadTags(false),
                 _readQueryTimeoutMs(0),
                 _writeQueryTimeoutMs(0)
	{
	}

  ConnectionInfo::ConnectionInfo(const mongocxx::uri& connectionUrl) :
                 _connectionUrl(mongocxx::uri(connectionUrl.to_string())),
                 _shard(0),
                 _useReadTags(false),
                 _readQueryTimeoutMs(0),
                 _writeQueryTimeoutMs(0)
	{
	}


  ConnectionInfo::ConnectionInfo(ifstream& file)
      : _shard(0), 
      _useReadTags(false),
      _readQueryTimeoutMs(0), 
      _writeQueryTimeoutMs(0)
  {
    using boost::property_tree::ptree;
    ptree pt;

    try {
        boost::property_tree::ini_parser::read_ini(file, pt);

        _connectionUrl = mongocxx::uri(pt.get<std::string>("connectionUrl", ""));
        _shard = pt.get<int>("shardId", 0);
        _clusterId = pt.get<std::string>("clusterId", "");
        _useReadTags = pt.get<std::string>("useReadTags", "false") == "true";
        _readQueryTimeoutMs = pt.get<int>("read-query-timeout-ms", 0);
        _writeQueryTimeoutMs = pt.get<int>("write-query-timeout-ms", 0);
    }
    catch (const mongocxx::exception& e) {
        BOOST_THROW_EXCEPTION(ConfigError() << errmsg_info(e.what()));
    }
    catch (const std::exception& e) {
        BOOST_THROW_EXCEPTION(ConfigError() << errmsg_info(std::string("Failed to parse config file: ") + e.what()));
    }

    Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "Loaded DB connection info for %s", _connectionUrl.to_string().c_str());
  }

  void BaseDB::setReadPreference(bsoncxx::builder::basic::document& builder, 
                                  const bsoncxx::document::view& query, 
                                  const char* readPreference) const
  {
    if (_info.useReadTags())
    {
        Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "Using read preferences tags for ");
        
        std::string shardIdStr = std::to_string(getShardId());
        std::string clusterId = getClusterId();

        if (clusterId.empty())
        {
            clusterId = "1"; // for backward compatibility with old behavior
        }

        // Create an array builder
        bsoncxx::builder::basic::array tags;
        {
            // Use a temporary document builder for the inner document
            bsoncxx::builder::basic::document tagDoc;
            tagDoc.append(
                bsoncxx::builder::basic::kvp("clusterId", clusterId),
                bsoncxx::builder::basic::kvp("shardId", shardIdStr)
            );
            
            // Append the created document to the array
            tags.append(tagDoc.view());
        }

        // Use explicit document builder
        bsoncxx::builder::basic::document readPreferenceDoc;
        readPreferenceDoc.append(
            bsoncxx::builder::basic::kvp("mode", readPreference),
            bsoncxx::builder::basic::kvp("tags", tags.view())
        );

        // Append to the main builder
        builder.append(bsoncxx::builder::basic::kvp("$readPreference", readPreferenceDoc.view()));
    }
    else
    {
      bsoncxx::builder::basic::document readPreferenceDoc;
      readPreferenceDoc.append(bsoncxx::builder::basic::kvp("mode", readPreference));

      builder.append(bsoncxx::builder::basic::kvp("$readPreference", readPreferenceDoc.view()));

    }

    builder.append(bsoncxx::builder::basic::kvp(std::string("query"), query));
  }

  void BaseDB::primaryPreferred(mongocxx::options::find& findOptions) const
  {
      mongocxx::read_preference readPref;
      readPref.mode(mongocxx::read_preference::read_mode::k_primary_preferred);
      findOptions.read_preference(readPref);
  }

  void BaseDB::nearest(mongocxx::options::find& findOptions) const
  {
      mongocxx::read_preference readPref;
      readPref.mode(mongocxx::read_preference::read_mode::k_nearest);
      findOptions.read_preference(readPref);
  }

void BaseDB::forEach(const bsoncxx::document::view& query, const std::string& ns, 
                     boost::function<void(const bsoncxx::document::view&)> doSomething)
{
    try {
        // Use MongoConnection to manage the database connection
        MongoDB::MongoConnection conn(_info);

        mongocxx::collection collection = conn.collection(ns);

        // Retrieve cursor for the query
        mongocxx::cursor cursor = collection.find(query);

        // Iterate over the cursor
        for (const bsoncxx::document::view& doc : cursor) {
            doSomething(doc);
        }
    } 
    catch (const std::exception& e) {
        OS_LOG_ERROR(FAC_ODBC, "Error in forEach: " << e.what());
    }
}

  void BaseDB::registerTimer(const UpdateTimer* pTimer)
  {
    boost::lock_guard<boost::mutex> lock(_updateTimerSamplesMutex);

    _lastUpdateSpeed = pTimer->_end - pTimer->_start;
    _updateTimerSamples.push_back(_lastUpdateSpeed);

    if (_lastUpdateSpeed > MAX_UPDATE_DELAY_MS)
    {
      OsTime time;
      OsDateTime::getCurTimeSinceBoot(time);
      long now = time.seconds();

      if (!_lastAlarmLog || now >= _lastAlarmLog + ALARM_RATE_SEC)
      {
        _lastAlarmLog = now;

        if (pTimer->_isDBConnOK)
        {
          OS_LOG_EMERGENCY(FAC_SIP, "ALARM_MONGODB_SLOW_UPDATE Last Mongo update took a long time:"
              << " document: " << _ns
              << " delay: " << _lastUpdateSpeed << " milliseconds");
        }
        else
        {
          OS_LOG_EMERGENCY(FAC_SIP, "ALARM_MONGODB_SLOW_UPDATE Problem with database connection:"
              << " document: " << _ns);
        }
      }
    }
  }

  void BaseDB::registerTimer(const ReadTimer* pTimer)
  {
    boost::lock_guard<boost::mutex> lock(_readTimerSamplesMutex);

    _lastReadSpeed = pTimer->_end - pTimer->_start;
    _readTimerSamples.push_back(_lastReadSpeed);

    if (_lastReadSpeed > MAX_READ_DELAY_MS)
    {
      OsTime time;
      OsDateTime::getCurTimeSinceBoot(time);
      long now = time.seconds();

      if (!_lastAlarmLog || now >= _lastAlarmLog + ALARM_RATE_SEC)
      {
        _lastAlarmLog = now;

        if (pTimer->_isDBConnOK)
        {
          OS_LOG_EMERGENCY(FAC_SIP, "ALARM_MONGODB_SLOW_READ Last Mongo read took a long time:"
              << " document: " << _ns
              << " delay: " << _lastReadSpeed << " milliseconds");
        }
        else
        {
          OS_LOG_EMERGENCY(FAC_SIP, "ALARM_MONGODB_SLOW_READ Problem with database connection:"
              << " document: " << _ns);
        }
      }
    }
  }

  std::int64_t BaseDB::getUpdateAverageSpeed() const
  {
    boost::lock_guard<boost::mutex> lock(_updateTimerSamplesMutex);

    std::int64_t sum = 0;
    for (boost::circular_buffer<std::int64_t>::const_iterator iter = _updateTimerSamples.begin(); iter != _updateTimerSamples.end(); iter++)
    {
      sum += *iter;
    }

    if (_updateTimerSamples.empty())
      return 0;

    return sum / _updateTimerSamples.size();
  }

  std::int64_t BaseDB::getLastUpdateSpeed() const
  {
    boost::lock_guard<boost::mutex> lock(_updateTimerSamplesMutex);
    return _lastUpdateSpeed;
  }

  std::int64_t BaseDB::getReadAverageSpeed() const
  {
    boost::lock_guard<boost::mutex> lock(_readTimerSamplesMutex);

    std::int64_t sum = 0;
    for (boost::circular_buffer<std::int64_t>::const_iterator iter = _readTimerSamples.begin(); iter != _readTimerSamples.end(); iter++)
    {
      sum += *iter;
    }

    if (_readTimerSamples.empty())
      return 0;

    return sum / _readTimerSamples.size();
  }

  std::int64_t BaseDB::getLastReadSpeed() const
  {
    boost::lock_guard<boost::mutex> lock(_readTimerSamplesMutex);
    return _lastReadSpeed;
  } 

  bsoncxx::document::value BaseDB::queryMaxTimeMS(const bsoncxx::document::view& obj, std::int64_t maxTimeMs) const
  {
    bsoncxx::builder::basic::document queryBuilder;

    // Copy the original query document
    queryBuilder.append(bsoncxx::builder::basic::kvp("$query", obj));

    // Append maxTimeMS option
    queryBuilder.append(bsoncxx::builder::basic::kvp("$maxTimeMS", maxTimeMs));

    return queryBuilder.extract();
  }

  bsoncxx::document::value BaseDB::readQueryMaxTimeMS(const bsoncxx::document::view& obj) const
  {
    return queryMaxTimeMS(obj, _info.getReadQueryTimeoutMs());
  }

  bsoncxx::document::value BaseDB::writeQueryMaxTimeMS(const bsoncxx::document::view& obj) const
  {
    return queryMaxTimeMS(obj, _info.getWriteQueryTimeoutMs());
  }

  bsoncxx::types::b_date BaseDB::dateFromSecsSinceEpoch(unsigned long timestamp)
  {
      // Convert seconds to milliseconds
      return bsoncxx::types::b_date(std::chrono::milliseconds(1000 * timestamp));
  }

  bool BaseDB::safeDropIndex(mongocxx::collection& collection, const std::string& key) const
  {
      bool ret = true;
      try
      {
          // Drop the index using the index view
          collection.indexes().drop_one(key);
      }
      catch (const std::exception& e)
      {
          OS_LOG_INFO(FAC_SIP, "BaseDB::safeDropIndex index not found/not dropped: " << key
                          << ". " << e.what());
          ret = false;
      }
      catch (...)
      {
          OS_LOG_INFO(FAC_SIP, "BaseDB::safeDropIndex index not found/not dropped: " << key
                          << ". Unknown Exception");
          ret = false;
      }
      return ret;
  }

  bool BaseDB::safeEnsureTTLIndex(mongocxx::collection& collection, const std::string& key, int ttlSeconds) const
  {
    bool ret = true;
    try
    {
        collection.create_index(
            bsoncxx::builder::basic::make_document(
                bsoncxx::builder::basic::kvp(key, 1)),
            mongocxx::options::index{}.expire_after(std::chrono::seconds(ttlSeconds))
        );
    }
    catch (const std::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "BaseDB::safeEnsureTTLIndex failed for index: " << key
                     << ", ttlSeconds: " << ttlSeconds
                     << ". " << e.what());
        ret = false;
    }
    catch (...)
    {
        OS_LOG_ERROR(FAC_SIP, "BaseDB::safeEnsureTTLIndex failed for index: " << key
                     << ", ttlSeconds: " << ttlSeconds
                     << ". Unknown Exception");
        ret = false;
    }
    return ret;
  }

  UpdateTimer::UpdateTimer(BaseDB& db) :
    _end(0),
    _db(db),
    _isDBConnOK(false)
  {
    struct timeval sTimeVal;
    gettimeofday( &sTimeVal, NULL );
    _start = (std::int64_t)( sTimeVal.tv_sec * 1000 + ( sTimeVal.tv_usec / 1000 ) );
  }

  UpdateTimer::~UpdateTimer()
  {
    struct timeval sTimeVal;
    gettimeofday( &sTimeVal, NULL );
    _end = (std::int64_t)( sTimeVal.tv_sec * 1000 + ( sTimeVal.tv_usec / 1000 ) );
    _db.registerTimer(this);
  }

  ReadTimer::ReadTimer(BaseDB& db) :
    _end(0),
    _db(db),
    _isDBConnOK(false)
  {
    struct timeval sTimeVal;
    gettimeofday( &sTimeVal, NULL );
    _start = (std::int64_t)( sTimeVal.tv_sec * 1000 + ( sTimeVal.tv_usec / 1000 ) );
  }

  ReadTimer::~ReadTimer()
  {
    struct timeval sTimeVal;
    gettimeofday( &sTimeVal, NULL );
    _end = (std::int64_t)( sTimeVal.tv_sec * 1000 + ( sTimeVal.tv_usec / 1000 ) );
    _db.registerTimer(this);
  }

} // namespace MongoDB


