/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.commons.mongo;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.ClientSessionOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

/**
 * Creates a Mongo instance from a properties file for spring based projects
 * 
 */
public class MongoSpringFactory implements MongoDatabaseFactory {
    private SimpleMongoClientDatabaseFactory m_delegate;
    private String m_configFile;
    private String m_connectionUrl;

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return getDelegate().getMongoDatabase();
    }

    @Override
    public MongoDatabase getMongoDatabase(String name) throws DataAccessException {        
        return getDelegate().getMongoDatabase(name);
    }
    
private MongoDatabaseFactory getDelegate() {

    if (m_delegate == null) {

        if (m_connectionUrl == null) {
            m_connectionUrl = MongoFactory.readConfig(m_configFile);
        }
        
        try {
            ConnectionString connectionString = new ConnectionString(m_connectionUrl);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);

            m_delegate = new SimpleMongoClientDatabaseFactory(mongoClient, "notused");
        } catch (Exception e) {
            throw new MongoConfigException(e);
        }
    }

    return m_delegate;
}
    
    static class MongoConfigException extends DataAccessException {
        public MongoConfigException(Throwable cause) {
            super("Cannot get mongo connection", cause);
        }        
    }
    
    public void setConnectionUrl(String url)   {
        m_connectionUrl = url;
    }

    public void setConfigFile(String configFile) {
        m_configFile = configFile;
    }

    @Override
    public PersistenceExceptionTranslator getExceptionTranslator() {
        return new MongoExceptionTranslator();
    }

    @Override
    public ClientSession getSession(ClientSessionOptions options) {
        throw new UnsupportedOperationException("Unimplemented method 'getSession'");
    }

    @Override
    public MongoDatabaseFactory withSession(ClientSession session) {
        throw new UnsupportedOperationException("Unimplemented method 'withSession'");
    }
}
