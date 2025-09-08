/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;

public class IndexManagerImpl extends SipxHibernateDaoSupport<Object> implements IndexManager {
    private static final Log LOG = LogFactory.getLog(IndexManagerImpl.class);

    private Indexer m_indexer;
    private BeanAdaptor m_beanAdaptor;
    private Class<?>[] m_indexedClasses;

    /**
     * Loads all entities to be indexed.
     */
    public void indexAll() {
        Session session = null;
        try {
            LOG.info("Creating database index...");
            m_indexer.open();
    
            session = getSessionFactory().openSession();
    
            for (int i = 0; i < m_indexedClasses.length; i++) {
                Class<?> clazz = m_indexedClasses[i];
                m_beanAdaptor.setIndexedClasses(new Class[] { clazz });
    
                @SuppressWarnings("unused")
                List<?> entities = session.createQuery("from " + clazz.getName()).list();
    
                // if the indexer or bean adaptor needs to process the list, pass it here
            }
        } catch (Exception e) {
            LOG.error("Error during indexing", e);
        } finally {
            m_indexer.close();
            if (session != null) {
                session.close();
            }
            LOG.info("Index created");
        }
    }
    

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    /**
     * In order for this code to work this has to be the same beanAdaptor that is used by Indexer
     */
    public void setBeanAdaptor(BeanAdaptor beanAdaptor) {
        m_beanAdaptor = beanAdaptor;
        m_indexedClasses = m_beanAdaptor.getIndexedClasses();
    }
}
