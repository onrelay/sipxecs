/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.common;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.beans.Introspector;


import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;

import org.sipfoundry.sipxconfig.common.event.HibernateEntityChangeProvider;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeAction;

/**
 * Enables Spring to create the hibernate object. Use to allow Spring to manage object
 * dependencies with hibernate.
 *
 * Note: it inherits from IndexingInterceptor: only one interceptor can be registered with
 * hibernate session.
 */
public class SpringHibernateInterceptor implements Interceptor, BeanFactoryAware, ApplicationContextAware {
    private static final Log LOG = LogFactory.getLog(SpringHibernateInterceptor.class);
    private ListableBeanFactory m_beanFactory;
    private ApplicationContext m_applicationContext;
    private Map<Class<?>,String> m_beanNamesCache;
    private Map<String, EntityDecorator> m_decorators;
    private Collection<HibernateEntityChangeProvider> m_hbEntityProviders;

    private CopyOnWriteArraySet<HbEntity> m_inserts = new CopyOnWriteArraySet<HbEntity>();
    private CopyOnWriteArraySet<HbEntity> m_updates = new CopyOnWriteArraySet<HbEntity>();
    private CopyOnWriteArraySet<HbEntity> m_deletes = new CopyOnWriteArraySet<HbEntity>();


    private static class ClassToBeanName implements Transformer {
        private ListableBeanFactory m_beanFactory;

        ClassToBeanName(ListableBeanFactory beanFactory) {
            m_beanFactory = beanFactory;
        }

        public Object transform(Object input) {
            Class<?> clazz = (Class<?>) input;
            String[] beanDefinitionNames = m_beanFactory.getBeanNamesForType(clazz);
            LOG.debug(beanDefinitionNames.length + " beans registered for class: " + clazz.getName());
            for (int i = 0; i < beanDefinitionNames.length; i++) {
                Object bean = m_beanFactory.getBean(beanDefinitionNames[i]);

                if (clazz == bean.getClass()) {
                    // only return the bean name if class matches exactly - no
                    // subclasses
                    return beanDefinitionNames[i];
                }
            }
            return null;
        }
    }

    @Override
    public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {

        // Only Spring-inject objects that are defined as BeanWithId
        if ( !(entity instanceof BeanWithId) ) {
            return false;
        }

        if( !injectSpringDependencies( entity ) ) {
            return false;
        }

        EntityDecorator decorator = getDecorator(entity);
        if (decorator != null) {
            decorator.onLoad(entity, id);
        }

        return true;
    }

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        
        // Only Spring-inject objects that are defined as BeanWithId
        if ( !(entity instanceof BeanWithId) ) {
            return false;
        }
        
        EntityDecorator decorator = getDecorator(entity);
        if (decorator != null) {
            decorator.onSave(entity, id);
        }
        m_inserts.add(new HbEntity(entity, id, null, null, propertyNames, types, state));
        return true;
    }

    @Override
    public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
       
        // Only Spring-inject objects that are defined as BeanWithId
        if ( !(entity instanceof BeanWithId) ) {
            return;
        }

        EntityDecorator decorator = getDecorator(entity);
        if (decorator != null) {
            decorator.onDelete(entity, id);
        }
        m_deletes.add(new HbEntity(entity, id, null, null, propertyNames, types, state));

    }


    private EntityDecorator getDecorator(Object entity) {
        return getDecorator(entity.getClass());
    }

    private EntityDecorator getDecorator(Class<?> clazz) {
        String decoratorName = clazz.getSimpleName().toLowerCase() + "Decorator";
        if (getEntityDecorators().containsKey(decoratorName)) {
            EntityDecorator decorator = getEntityDecorators().get(decoratorName);
            return decorator;
        }
        return null;
    }

    private Map<String, EntityDecorator> getEntityDecorators() {
        if (m_decorators == null) {
            m_decorators = m_beanFactory.getBeansOfType(EntityDecorator.class, false, false);
        }
        return m_decorators;
    }

    /**
     * This can only be used withy listeable bean factory
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
        Transformer transformer = new ClassToBeanName(m_beanFactory);
        m_beanNamesCache = LazyMap.lazyMap(new HashMap<>(), transformer);
    }

    public BeanFactory getBeanFactory() {
        return m_beanFactory;
    }

    public ApplicationContext getApplicationContext() {
        return m_applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        m_applicationContext = applicationContext;
    }

    public boolean onFlushDirty(Object obj, Object id, Object[] newValues, Object[] oldValues,
            String[] properties, Type[] types) throws CallbackException {
        m_updates.add(new HbEntity(obj, id, newValues, oldValues, properties, types, null));
        return true;
    }

    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        for (HibernateEntityChangeProvider provider : getHbEntityChangeProviders()) {
            provider.onConfigChangeCollectionUpdate(collection, key);
        }
    }

    public void postFlush(Iterator iterator) {
        HbEntity hbEntity = null;
        for (Iterator<HbEntity> it = m_inserts.iterator(); it.hasNext();) {
            hbEntity = it.next();
            for (HibernateEntityChangeProvider provider : getHbEntityChangeProviders()) {
                provider.onConfigChangeAction(hbEntity.getEntity(), ConfigChangeAction.ADDED,
                    hbEntity.getProperties(), null, null);
            }
            m_inserts.remove(hbEntity);
        }
        for (Iterator<HbEntity> it = m_updates.iterator(); it.hasNext();) {
            hbEntity = it.next();
            for (HibernateEntityChangeProvider provider : getHbEntityChangeProviders()) {
                provider.onConfigChangeAction(hbEntity.getEntity(), ConfigChangeAction.MODIFIED,
                    hbEntity.getProperties(), hbEntity.getOldValues(), hbEntity.getNewValues());
            }
            m_updates.remove(hbEntity);
        }
        for (Iterator<HbEntity> it = m_deletes.iterator(); it.hasNext();) {
            hbEntity = it.next();
            for (HibernateEntityChangeProvider provider : getHbEntityChangeProviders()) {
                provider.onConfigChangeAction(hbEntity.getEntity(), ConfigChangeAction.DELETED,
                    hbEntity.getProperties(), null, null);
            }
            m_deletes.remove(hbEntity);
        }
    }

    private Collection<HibernateEntityChangeProvider> getHbEntityChangeProviders() {
        if (m_hbEntityProviders == null) {
            m_hbEntityProviders = new ArrayList<HibernateEntityChangeProvider>();
            Map<String, HibernateEntityChangeProvider> beanMap = m_beanFactory
                    .getBeansOfType(HibernateEntityChangeProvider.class, false,
                            false);
            for (HibernateEntityChangeProvider provider : beanMap.values()) {
                if (provider instanceof Proxy) {
                    m_hbEntityProviders.add(provider);
                }
            }
        }
        return m_hbEntityProviders;
    }

    private <S> boolean injectSpringDependencies(S entity ) {
                Class<?> klass = entity.getClass();

        String beanName = null;

        // Try exact bean name match by type
        String[] beanNames = m_applicationContext.getBeanNamesForType(klass);

        if (beanNames.length == 1) {
            beanName = beanNames[0];
        } 
        else {

            // Try simpleName decapitalized (standard Spring convention)
            String conventionalName = Introspector.decapitalize(klass.getSimpleName());

            if (m_applicationContext.containsBean(conventionalName)) {
                beanName = conventionalName;
            } 
            else {
                // Try fully qualified class name 
                String fqcnName = klass.getName(); 

                if (m_applicationContext.containsBean(fqcnName)) {
                    beanName = fqcnName;

                } 
                else {
                    // No spring bean found
                    return false;
                }
            }
        }

        m_applicationContext.getAutowireCapableBeanFactory().configureBean(entity, beanName);

        return true;
    }

}
