/*
 * Copyright (c) 2011 Telecats B.V. All rights reserved. Contributed to
 * SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 * This library or application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public
 * License (AGPL) as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library or application is distributed in the hope that it will be
 * useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License (AGPL) for more details.
 *
 * $
 */
#include <os/OsLogger.h>
#include <sipxproxy/SipRouter.h>
#include <bsoncxx/document/view.hpp>
#include <CallerID.h>

#include <cppunit/TestCase.h>
#include <cppunit/extensions/HelperMacros.h>

const mongocxx::uri connUrl("localhost");
const std::string testNs("test");

class CallerMongoDBTest: public CppUnit::TestCase {
	CPPUNIT_TEST_SUITE(CallerMongoDBTest);
	CPPUNIT_TEST(testGetCalledName);
	CPPUNIT_TEST(testGetCallerName);
	CPPUNIT_TEST_SUITE_END();

	CallerMongoDB* db;

public:

	void setUp() {
		db = new CallerMongoDB(connUrl, testNs);
	}

	void tearDown() {
		delete db;
		db = 0;
	}

	void testGetCalledName() {
		insertTestData(CallerMongoDB::COLLECTION_DDI, "finch", "goldfinch");
		CPPUNIT_ASSERT_EQUAL(std::string("goldfinch"), db->getCalledName("finch"));
	}

	void testGetCallerName() {
		insertTestData(CallerMongoDB::COLLECTION_DNIS, "chicken", "turkey");
		CPPUNIT_ASSERT_EQUAL(std::string("turkey"), db->getCallerName("chicken"));
	}

    void insertTestData(const std::string& collection, const char* number, const char* name) {
        try {
            // Create a connection using MongoConnection with connUrl
            MongoDB::MongoConnection conn(connUrl);

            std::string ns = testNs + "." + collection;
            
            // Access the collection
            auto coll = conn.collection(ns);
            
            // Prepare the data to insert
            bsoncxx::document::value data = bsoncxx::builder::stream::document{}
                << "from" << std::string(number)
                << "to" << std::string(name)
                << bsoncxx::builder::stream::finalize();
            
            // Remove existing data if needed (equivalent to the `remove` operation)
            coll.delete_many({});
            
            // Insert the new data
            coll.insert_one(data.view());
            
        } catch (const mongocxx::exception& e) {
            std::cerr << "MongoDB Error: " << e.what() << std::endl;
        }
    }

};
CPPUNIT_TEST_SUITE_REGISTRATION(CallerMongoDBTest);
