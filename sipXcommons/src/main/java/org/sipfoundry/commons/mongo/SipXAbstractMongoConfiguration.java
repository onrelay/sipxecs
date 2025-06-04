/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.commons.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class SipXAbstractMongoConfiguration extends AbstractMongoClientConfiguration {

    public static final String DATABASE_NAME = "imdb";
    public static final String CONFIG_FILE = "/mongo-client.ini";
    public static final String CONNECTION_URL_KEY = "connectionUrl";

    protected String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Bean
    public MongoClient mongoClient() {

        try {
            // Load configuration properties from file
            String mongoClientIni = this.getClass().getResource(CONFIG_FILE).getFile();
            Properties prop = new Properties();
            FileInputStream input = new FileInputStream(mongoClientIni);
            
            prop.load(input);
            
            // Retrieve the connection string from the properties file
            String connString = prop.getProperty(CONNECTION_URL_KEY);

            // Create and return the MongoClient using the connection string
            return MongoClients.create(connString);

        } catch( IOException ioException ) {
            return null;
        }
    }
}