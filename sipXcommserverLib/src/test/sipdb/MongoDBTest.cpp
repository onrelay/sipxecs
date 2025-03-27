#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestCase.h>
#include <sipdb/MongoDB.h>
#include <bsoncxx/document/view.hpp>

using namespace std;

#define LOCALHOST_ADDR "127.0.0.1"
#define DATABASE_NAME "test.BaseDBTest"
#define FIELD_NAME "a"


//class MongoDBTest: public CppUnit::TestCase
//{
//	CPPUNIT_TEST_SUITE(MongoDBTest);
//	CPPUNIT_TEST(testReadHAConfig);
//    CPPUNIT_TEST(testReadSingleConfig);
//	CPPUNIT_TEST_SUITE_END();
//
//public:
//
//    void testReadSingleConfig()
//    {
//        try {
//            mongo::ConnectionString s = MongoDB::ConnectionInfo::connectionStringFromFile(TEST_DATA_DIR "/sipxmongo-single-config");
//            CPPUNIT_ASSERT_EQUAL(string("sipxecs"), s.getSetName());
//            std::vector<mongo::HostAndPort> servers = s.getServers();
//            CPPUNIT_ASSERT_EQUAL(1, (int) servers.size());
//            mongo::HostAndPort first = servers.front();
//            CPPUNIT_ASSERT_EQUAL(string("localhost"), first.host());
//            CPPUNIT_ASSERT_EQUAL(27017, first.port());
//        } catch (exception& e) {
//            cout << e.what() << endl;
//        }
//    }
//
//	void testReadHAConfig()
//	{
//		mongo::ConnectionString s = MongoDB::ConnectionInfo::connectionStringFromFile(TEST_DATA_DIR "/sipxmongo-ha-config");
//		CPPUNIT_ASSERT_EQUAL(string("sipxecs"), s.getSetName());
//		std::vector<mongo::HostAndPort> servers = s.getServers();
//		CPPUNIT_ASSERT_EQUAL(2, (int) servers.size());
//		mongo::HostAndPort first = servers.front();
//		CPPUNIT_ASSERT_EQUAL(string("localhost"), first.host());
//		CPPUNIT_ASSERT_EQUAL(27017, first.port());
//		mongo::HostAndPort second = servers.back();
//		CPPUNIT_ASSERT_EQUAL(string("localhost"), second.host());
//		CPPUNIT_ASSERT_EQUAL(27018, second.port());
//	}
//};
//CPPUNIT_TEST_SUITE_REGISTRATION(MongoDBTest);


class BaseDBTest: public CppUnit::TestCase
{
  CPPUNIT_TEST_SUITE(BaseDBTest);
  CPPUNIT_TEST(testForEach);
  CPPUNIT_TEST_SUITE_END();

  const MongoDB::ConnectionInfo _info;
  int _row;
  std::string _ns;

public:

  BaseDBTest() :
    _info(MongoDB::ConnectionInfo(std::string(LOCALHOST_ADDR))),
    _ns(DATABASE_NAME)
{
}

  void forEachFunction(const bsoncxx::document::view& record)
  {
    // TEST: That row0 is 0 and row1 is 10
    CPPUNIT_ASSERT_EQUAL(_row * 10, record.getIntField(FIELD_NAME));
    _row++;
  }

void testForEach()
{
    _row = 0;

    try
    {
        // Initialize the MongoDB connection and collections
        MongoDB::MongoConnection connection(_info);
        mongocxx::collection collection = connection.collection(_ns);

        // Clear the collection and insert test data
        collection.delete_many({});
        collection.insert_one(BSON(FIELD_NAME << 0));
        collection.insert_one(BSON(FIELD_NAME << 10));

        // Set up BaseDB and run the forEach method
        MongoDB::BaseDB db(_info);
        db.forEach({}, _ns, [this](const bsoncxx::document::view& doc) {
            forEachFunction(doc);
        });

        // TEST: The number of rows should be 2
        CPPUNIT_ASSERT_EQUAL(2, _row);
    }
    catch (const mongocxx::exception& e)
    {
        OS_LOG_ERROR(FAC_SIP, "testForEach - MongoDB exception: " << e.what());
        throw;  // Re-throw for higher-level handling
    }
}
};

CPPUNIT_TEST_SUITE_REGISTRATION(BaseDBTest);
