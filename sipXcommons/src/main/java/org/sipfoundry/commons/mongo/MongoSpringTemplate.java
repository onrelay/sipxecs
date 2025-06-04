package org.sipfoundry.commons.mongo;

import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoDatabase;


/**
 * Wrap template so database name can be set on template and not on factory. Factory
 * is singleton for all dbs to share Mongo object and reduce parsing of ini file 
 */
public class MongoSpringTemplate extends MongoTemplate {
    private String m_dbname;
    private MongoDatabaseFactory m_factory;    
    
    public MongoSpringTemplate(MongoDatabaseFactory factory) {
        super(factory);
        m_factory = factory;
    }
    
    public MongoDatabase getDb() {
        return m_factory.getMongoDatabase(m_dbname);
    }

    public String getDbname() {
        return m_dbname;
    }

    
    public void setDbname(String dbname) {
        m_dbname = dbname;
    }
}
