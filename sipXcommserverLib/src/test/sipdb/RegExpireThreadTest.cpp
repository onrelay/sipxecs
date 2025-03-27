#include <cppunit/TestCase.h>
#include <cppunit/extensions/HelperMacros.h>
#include <sipxunit/TestUtilities.h>
#include <sipdb/RegDB.h>
#include <sipdb/RegExpireThread.h>
#include <sipdb/MongoDB.h>
#include <os/OsDateTime.h>
#include <bsoncxx/document/view.hpp>


using namespace std;

const char* gLocalHostAddr = "localhost";
const char* gDatabaseName = "test.RegExpireThreadTest";

typedef struct
{
  const char* pContact;
  int expirationTimeDelta;
  const char* pQValue;
  const char* pInstanceId;
  const char* pGruu;
  const char* pPath;
  const char* pInstrument;
  const char* pCallId;
  unsigned int cseq;
  const char* pIdentity;
  const char* pUri;
} RegBindingTestData;

RegBindingTestData regBindingTestData[] =
{
  {
    "sip:alice@host1.atlanta.com;transport=tcp",
    -3600,
    "0",
    "",
    "",
    "sip:proxy01.atlanta.com",
    "instrument-test",
    "call-id@12345",
    1,
    "alice@atlanta.com",
    "sip:alice@atalanta.com"
  }
};

class RegExpireThreadTest: public CppUnit::TestCase
{
  CPPUNIT_TEST_SUITE(RegExpireThreadTest);
  CPPUNIT_TEST(testRegExpireThreadTest_Run);
  CPPUNIT_TEST_SUITE_END();

  RegDB* _db;
  const MongoDB::ConnectionInfo _info;
  const std::string _ns;
  unsigned long _timeNow;
public:
  RegExpireThreadTest() : _info(MongoDB::ConnectionInfo(std::string(gLocalHostAddr))),
                          _ns(gDatabaseName)
  {
  }

  void setUp()
  {
      // Initialize the RegDB object
      _db = new RegDB(_info, nullptr, _ns);

      // Create a MongoDB connection and get the collection
      MongoDB::MongoConnection connection(_info);
      mongocxx::collection collection = connection.collection(_ns);

      // Remove all documents from the collection
      collection.delete_many({});  // Equivalent to remove(_ns, mongo::Query())

      // Get the current time in seconds
      _timeNow = OsDateTime::getSecsSinceEpoch();
  }


  void tearDown()
  {
    delete _db;
    _db = 0;
  }

  void updateRegBindingTestData(RegBinding::Ptr& binding, int index)
  {
    binding->setContact(regBindingTestData[index].pContact);
    binding->setExpirationTime(_timeNow + regBindingTestData[index].expirationTimeDelta);
    binding->setQvalue(regBindingTestData[index].pQValue);
    binding->setInstanceId(regBindingTestData[index].pInstanceId);
    binding->setGruu(regBindingTestData[index].pGruu);
    binding->setPath(regBindingTestData[index].pPath);
    binding->setInstrument(regBindingTestData[index].pInstrument);
    binding->setCallId(regBindingTestData[index].pCallId);
    binding->setCseq(regBindingTestData[index].cseq);
    binding->setIdentity(regBindingTestData[index].pIdentity);
    binding->setUri(regBindingTestData[index].pUri);
    _db->updateBinding(binding);
  }

  void testRegExpireThreadTest_Run()
  {
    //
    // Create a binding that expired an hour ago
    //
    RegBinding::Ptr binding_0 = RegBinding::Ptr(new RegBinding());
    updateRegBindingTestData(binding_0, 0);

    RegDB::Bindings bindings;

    // TEST: Check that we successfully inserted one entry
    CPPUNIT_ASSERT(getAllOldBindings(_timeNow, bindings) == true);
    CPPUNIT_ASSERT(bindings.size() == 1);

    bindings.clear();

    RegExpireThread regExpireThread;

    // start reg Expire thread that will run every two second and will remove all expired records
    regExpireThread.run(_db, 2);

    // wait 3 seconds to be sure that the thread removed all expired entries
    sleep(3);

    // TEST: Check that there are no entries in test.RegExpireThreadTest database
    CPPUNIT_ASSERT(getAllOldBindings(_timeNow, bindings) == false);
    CPPUNIT_ASSERT(bindings.size() == 0);
  }

  bool getAllOldBindings(int timeNow, RegDB::Bindings& bindings)
  {
      // Create the query to find documents with expirationTime less than the current time
      bsoncxx::document::view query = bsoncxx::builder::stream::document{}
          << RegBinding::expirationTime_fld() 
          << bsoncxx::builder::stream::open_document
          << "$lt" << MongoDB::BaseDB::dateFromSecsSinceEpoch(static_cast<long long>(timeNow))
          << bsoncxx::builder::stream::close_document
          << bsoncxx::builder::stream::finalize;

      // Create the MongoDB connection
      MongoDB::MongoConnection connection(_info);
      mongocxx::collection collection = connection.collection(_ns);

      // Query the collection for documents matching the criteria
      auto cursor = collection.find(query);

      // Check if the cursor has documents and process them
      if (cursor.begin() != cursor.end())
      {
          for (auto&& doc : cursor)
          {
              RegBinding binding(doc);
              bindings.push_back(binding);
          }

          return true;
      }

      return false;
  }
};

CPPUNIT_TEST_SUITE_REGISTRATION(RegExpireThreadTest);

