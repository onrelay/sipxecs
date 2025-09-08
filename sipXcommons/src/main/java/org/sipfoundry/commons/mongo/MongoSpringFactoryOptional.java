package org.sipfoundry.commons.mongo;

import java.io.File;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;

/**
 * Like MongoSpringFactory but config file might not exist.  If it doesn't then
 * this instance returns null for Db.  I created a separate class for this behavior
 * because under many systems the config file is not optional and you'd expect failfast
 * behavior there.
 * 
 * Ultimately sipxconfig uses this class to determine if system has a local
 * database defined and if it does, use it.
 */
public class MongoSpringFactoryOptional implements MongoDatabaseFactory {
    private MongoSpringFactory m_delegate;

    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return m_delegate != null ? m_delegate.getMongoDatabase() : null;
    }

    public MongoDatabase getMongoDatabase(String dbname) throws DataAccessException {
        return m_delegate != null ? m_delegate.getMongoDatabase(dbname) : null;
    }
    
    public void setConfigFile(String configFile) {
        if (new File(configFile).exists()) {
            m_delegate = new MongoSpringFactory();
            m_delegate.setConfigFile(configFile);
        }
    }

    public PersistenceExceptionTranslator getExceptionTranslator() {
        return new MongoExceptionTranslator();
    }

    public ClientSession getSession(ClientSessionOptions options) {
        throw new UnsupportedOperationException("Unimplemented method 'getSession'");
    }

    public MongoDatabaseFactory withSession(ClientSession session) {
        throw new UnsupportedOperationException("Unimplemented method 'withSession'");
    }    
}
