#include "MongoDbVerifier.h"


MongoDbVerifier::MongoDbVerifier(MongoDB::MongoConnection& conn,
                              const std::string& ns,
                              int maxTimeToWaitMs,
                              int timeToWaitBetweenRetriesMs) :
                              _conn(conn),
                              _ns(ns),
                              _maxTimeToWaitMs(maxTimeToWaitMs),
                              _timeToWaitBetweenRetriesMs(timeToWaitBetweenRetriesMs)
{
}

MongoDbVerifier::~MongoDbVerifier()
{
}

void MongoDbVerifier::wait(bsoncxx::document::view query, bool expectEmpty)
{
    int totalTimeMs = 0;
    int sleepTimeMs = _timeToWaitBetweenRetriesMs;

    // Retrieve the collection directly using the namespace
    mongocxx::collection collection = _conn.collection(_ns);

    mongocxx::options::find findOptions;
    findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

    while (totalTimeMs < _maxTimeToWaitMs)
    {
        // Execute the query and check if the result matches the expectation
        std::optional<bsoncxx::document::value> result = collection.find_one(query, findOptions);
        bool isEmpty = !static_cast<bool>(result);  

        if (isEmpty == expectEmpty)
        {
            return;  // Query result matches the expectation
        }

        // Wait before retrying
        std::this_thread::sleep_for(std::chrono::milliseconds(sleepTimeMs));
        totalTimeMs += sleepTimeMs;
    }

    // Timeout occurred
    throw std::runtime_error("MongoDbVerifier::wait timed out waiting for the query result.");
}

void MongoDbVerifier::waitUntilReachNumberOfEntries(bsoncxx::document::view bSONObj, unsigned long long numberOfEntries)
{
  int totalTimeMs = 0;
  int sleepTimeMs = _timeToWaitBetweenRetriesMs;
  while (numberOfEntries != _conn->get()->count(_ns, bSONObj) &&
      totalTimeMs < _maxTimeToWaitMs)
  {
    usleep(sleepTimeMs * 1000);
    totalTimeMs += sleepTimeMs;
  }
}

void MongoDbVerifier::waitUntilEmpty(bsoncxx::document::view bSONObj)
{
  wait(bSONObj, false);
}

void MongoDbVerifier::waitUntilHaveOneEntry(bsoncxx::document::view bSONObj)
{
  wait(bSONObj, true);
}
