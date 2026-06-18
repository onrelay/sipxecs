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
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;

public class IndexManagerImpl extends SipxHibernateDaoSupport<Object> implements IndexManager {
    private static final Log LOG = LogFactory.getLog(IndexManagerImpl.class);

    private Indexer m_indexer;
    private BeanAdaptor m_beanAdaptor;
    private BeanIndexHelper m_beanIndexHelper;
    private Class<?>[] m_indexedClasses;

    /**
     * Loads all entities to be indexed.
     */
    public void indexAll() {

        getSessionFactory().inTransaction( session -> {

            try {
                LOG.info("Creating database index...");

                m_indexer.open();
            
                for (int i = 0; i < m_indexedClasses.length; i++) {
                    Class<?> clazz = m_indexedClasses[i];

                    SessionFactoryImplementor sessionFactoryImplementor = 
                        getSessionFactory().unwrap(SessionFactoryImplementor.class);

                    EntityPersister persister = 
                        sessionFactoryImplementor.getMappingMetamodel().getEntityDescriptor(clazz);

                    String[] propertyNames = persister.getPropertyNames();
                    Type[] propertyTypes = persister.getPropertyTypes();

                    // Use getResultList and specify the class for better type safety in Hibernate 6/7
                    List<?> entities = session.createQuery("from " + clazz.getName(), clazz).getResultList();
                    for (Object entity : entities) {
                        Object id = persister.getIdentifier(entity, (SharedSessionContractImplementor) session);
                        Object[] state = persister.getPropertyValues(entity);
 
                        BeanIndexProperties bip = new BeanIndexProperties(entity, id, state, propertyNames, propertyTypes);
                        if (m_beanIndexHelper != null) {
                            m_beanIndexHelper.setupIndexProperties(bip);
                        }
                        m_indexer.indexBean(entity, id, bip.getState(), bip.getPropertyNames(), bip.getTypes(), true);
                        
                        // Evict the entity to prevent the Hibernate Session from bloating during bulk indexing
                        session.evict(entity);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error during indexing", e);
            } finally {
                m_indexer.close();
                LOG.info("Index created");
            }
        });
    }
    

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    public void setBeanIndexHelper(BeanIndexHelper beanIndexHelper) {
        m_beanIndexHelper = beanIndexHelper;
    }

    /**
     * In order for this code to work this has to be the same beanAdaptor that is used by Indexer
     */
    public void setBeanAdaptor(BeanAdaptor beanAdaptor) {
        m_beanAdaptor = beanAdaptor;
        m_indexedClasses = m_beanAdaptor.getIndexedClasses();
    }
}
