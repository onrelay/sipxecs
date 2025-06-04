package org.sipfoundry.openfire.provider;

import java.net.UnknownHostException;

import org.jivesoftware.openfire.XMPPServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;

/**
 * Setup and teardown for all tests going to OpenfireDB is the same.
 */
public abstract class BaseMongoTest {
    private static final String IM_DB_NAME = "imdb_TEST";
    private static final String OF_DB_NAME = "openfiredb_TEST";

    private static MongoClient mongoClient;
    private static MongoDatabase openfiredb;
    private static MongoDatabase imdb;
    private static XMPPServer server;

    @BeforeClass
    public static void classSetup() throws UnknownHostException {
        System.setProperty("mongo_ns", IM_DB_NAME);
        System.setProperty("openfire_ns", OF_DB_NAME);
        System.setProperty("openfireHome", "./mongo-lib/src/test/resources");
        System.setProperty("conf.dir", "./mongo-lib/src/test/resources");
        System.setProperty("provider.properties.className", "org.jivesoftware.util.FilePropertiesProvider");
        System.setProperty("configFile", "mongo-lib/src/test/resources/openfire.properties");

        // MongoDB client setup
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017"); // Replace with your MongoDB connection string
        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .applyConnectionString(connectionString)
                                                          .build();
        mongoClient = MongoClients.create(settings);

        // Get databases
        imdb = mongoClient.getDatabase(IM_DB_NAME);
        openfiredb = mongoClient.getDatabase(OF_DB_NAME);

        // Initialize XMPP server
        try {
            server = new XMPPServer();
        } catch (IllegalStateException ex) {
            server = XMPPServer.getInstance();
        }
    }

    @AfterClass
    public static void classTeardown() {
        // Stop the server and drop the test databases
        server.stop();
        mongoClient.getDatabase(IM_DB_NAME).drop();
        mongoClient.getDatabase(OF_DB_NAME).drop();
        mongoClient.close();
    }

    protected static MongoDatabase getOpenfireDb() {
        return openfiredb;
    }

    protected static MongoDatabase getImdb() {
        return imdb;
    }
}