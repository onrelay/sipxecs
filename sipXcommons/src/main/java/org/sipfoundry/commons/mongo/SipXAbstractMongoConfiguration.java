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

    protected String getDatabaseName() {
        return DATABASE_NAME;
    }
    
    @Bean
    public MongoClient mongoClient() {

        return MongoFactory.fromConnectionFile();
    }
}