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

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventType;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.type.Type;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;

import org.hibernate.persister.entity.EntityPersister;
import org.sipfoundry.sipxconfig.common.SpringHibernateInstantiator;
import org.sipfoundry.sipxconfig.common.event.KeepsOriginalCopy;

/**
 * This is used to indexing on load
 *   ...AND completely unrelated...
 * support KeepOriginalCopy interface.
 */
public class LoadIndexingInterceptor extends SpringHibernateInstantiator implements PostLoadEventListener {
    private Indexer m_indexer;
    private BeanIndexHelper m_beanIndexHelper;

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    public void setBeanIndexHelper(BeanIndexHelper beanIndexHelper) {
        m_beanIndexHelper = beanIndexHelper;
    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
        if (sessionFactory instanceof SessionFactoryImplementor) {
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) sessionFactory;
            ServiceRegistry serviceRegistry = sfi.getServiceRegistry();
            EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
            listenerRegistry.appendListeners(EventType.POST_LOAD, this);
        }
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {

        try {
            Object entity = event.getEntity();
            Serializable id = (Serializable)event.getId();

            if (entity instanceof KeepsOriginalCopy) {
                ((KeepsOriginalCopy<?>) entity).makeBackupAsOriginalCopy();
            }

            Class<?> entityClass = entity.getClass();
            SessionFactory sessionFactory = event.getSession().getFactory();
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) sessionFactory;
            MappingMetamodelImplementor metamodel = sfi.getMappingMetamodel();            
            EntityPersister persister = metamodel.findEntityDescriptor(entityClass.getName()); 

            String[] propertyNames = persister.getPropertyNames();
            Object[] state = new Object[propertyNames.length];
            Type[] types = new Type[propertyNames.length];

            for (int i = 0; i < propertyNames.length; i++) {
                state[i] = persister.getPropertyValue(entity, i);
                types[i] = persister.getPropertyType(propertyNames[i]);  
            }

            BeanIndexProperties bip = new BeanIndexProperties(entity, id, state, propertyNames, types);
            m_beanIndexHelper.setupIndexProperties(bip, true);
            m_indexer.indexBean(entity, id, bip.getState(), bip.getPropertyNames(), bip.getTypes(), true);

        } catch (Exception e) {
            // Log or handle gracefully
            throw new RuntimeException("onPostLoad() failed", e);
        }
    }
}
