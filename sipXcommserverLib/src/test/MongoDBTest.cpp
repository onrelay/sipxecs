#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestCase.h>
#include <sipdb/MongoDB.h>
#include <bsoncxx/document/view.hpp>


using namespace std;

/* class MongoDBTest: public CppUnit::TestCase
{
	CPPUNIT_TEST_SUITE(MongoDBTest);
	CPPUNIT_TEST(testReadHAConfig);
    CPPUNIT_TEST(testReadSingleConfig);
	CPPUNIT_TEST_SUITE_END();

public:

    void testReadSingleConfig()
    {
        try {
            mongo::CnnectionString s = MongoDB::ConnectionInfo::connectionStringFromFile(TEST_DATA_DIR "/sipxmongo-single-config");
            CPPUNIT_ASSERT_EQUAL(string("sipxecs"), s.getSetName());
            std::vector<mongo::HostAndPort> servers = s.getServers();
            CPPUNIT_ASSERT_EQUAL(1, (int) servers.size());
            mongo::HostAndPort first = servers.front();
            CPPUNIT_ASSERT_EQUAL(string("localhost"), first.host());
            CPPUNIT_ASSERT_EQUAL(27017, first.port());
        } catch (exception& e) {
            cout << e.what() << endl;
        }
    }

	void testReadHAConfig()
	{
		mongo::CnnectionString s = MongoDB::ConnectionInfo::connectionStringFromFile(TEST_DATA_DIR "/sipxmongo-ha-config");
		CPPUNIT_ASSERT_EQUAL(string("sipxecs"), s.getSetName());
		std::vector<mongo::HostAndPort> servers = s.getServers();
		CPPUNIT_ASSERT_EQUAL(2, (int) servers.size());
		mongo::HostAndPort first = servers.front();
		CPPUNIT_ASSERT_EQUAL(string("localhost"), first.host());
		CPPUNIT_ASSERT_EQUAL(27017, first.port());
		mongo::HostAndPort second = servers.back();
		CPPUNIT_ASSERT_EQUAL(string("localhost"), second.host());
		CPPUNIT_ASSERT_EQUAL(27018, second.port());
	}
};
CPPUNIT_TEST_SUITE_REGISTRATION(MongoDBTest);
 */

class BaseDBTest: public CppUnit::TestCase
{
    CPPUNIT_TEST_SUITE(BaseDBTest);
    CPPUNIT_TEST(testForEach);
    CPPUNIT_TEST_SUITE_END();

    const MongoDB::ConnectionInfo _info;
    int _row;

public:

    BaseDBTest() :
        _info(MongoDB::ConnectionInfo(string("127.0.0.1"), string("test.BaseDBTest")))
    {
    }

    void forEachFunction(const bsoncxx::document::view& record)
    {
        CPPUNIT_ASSERT_EQUAL(_row * 10, record.getIntField("a"));
        _row++;
    }

void testForEach()
{
    try
    {
        _row = 0;

        // Initialize MongoDB connection
        MongoDB::MongoConnection connection(_info);

        // Construct the query (empty in this case)
        auto query = bsoncxx::builder::basic::make_document();

        // Retrieve the collection
        mongocxx::collection collection = connection.collection(_info.getNS());

        // Remove any existing documents
        collection.delete_many(query.view());

        // Insert test data
        collection.insert_one(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string("a"), 0)));
        collection.insert_one(bsoncxx::builder::basic::make_document(
            bsoncxx::builder::basic::kvp(std::string("a"), 10)));

        // Initialize the BaseDB instance
        MongoDB::BaseDB db(_info);

        // Perform the forEach operation
        db.forEach(query.view(), bind(&BaseDBTest::forEachFunction, this, _1));

        // Assert that the forEach function was called twice
        CPPUNIT_ASSERT_EQUAL(2, _row);
    }
    catch (const mongocxx::exception& e)
    {
        // Log and re-throw exception for higher-level handling
        OS_LOG_ERROR(FAC_ODBC, "testForEach - MongoDB exception: " << e.what());
        throw MongoDB::MongoException("Failed to execute testForEach: " + std::string(e.what()));
    }
}
};

CPPUNIT_TEST_SUITE_REGISTRATION(BaseDBTest);
