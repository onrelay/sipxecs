package org.sipfoundry.commons.util;

import java.net.UnknownHostException;

import org.sipfoundry.commons.mongo.MongoFactory;
import org.sipfoundry.commons.userdb.ValidUsers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Connection factory to mongo.  Need to call this before using
 *   UnfortunateLackOfSpringSupportFactory.initialize("/file/to/client/config");
 * File is probably
 *   @SIPX_CONFDIR@/mongo-client.ini
 */
public class UnfortunateLackOfSpringSupportFactory {
    private static ValidUsers s_validUsers;
    private static MongoDatabase s_imdb;
    private static MongoDatabase s_openfiredb;

    public synchronized static void initialize() throws UnknownHostException {
        if (s_validUsers == null) {

            // useful in unit tests to direct operations to imdb_TEST
            String imdbNs = System.getProperty("mongo_ns", "imdb");
            String openfireNs = System.getProperty("openfire_ns", "openfiredb");

            MongoClient mongoClient = MongoFactory.fromConnectionFile();
            try {
                s_imdb = mongoClient.getDatabase(imdbNs);
                s_openfiredb = mongoClient.getDatabase(openfireNs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ValidUsers validUsers = new ValidUsers();
            validUsers.setImdb(s_imdb);
            // be sure this is the last successful thing you do to ensure
            // singleton is properly initialized
            s_validUsers = validUsers;
        }
    }

    public static ValidUsers getValidUsers() {
        checkinit();
        return s_validUsers;
    }

    public static MongoDatabase getImdb() {
        checkinit();
        return s_imdb;
    }

    public static MongoDatabase getOpenfiredb() {
        checkinit();
        return s_openfiredb;
    }

    public static String getConnectionURL() {
        checkinit();
        return MongoFactory.getConnectionURL();
    }

    private static void checkinit() {
        if (s_validUsers == null) {
            try {
				initialize();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
        }
    }
}
