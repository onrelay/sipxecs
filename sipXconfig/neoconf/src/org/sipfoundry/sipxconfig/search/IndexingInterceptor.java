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

import org.hibernate.type.Type;

import org.sipfoundry.sipxconfig.common.SpringHibernateInterceptor;

import org.sipfoundry.sipxconfig.common.event.KeepsOriginalCopy;


public class IndexingInterceptor extends SpringHibernateInterceptor {
    private Indexer m_indexer;
    private BeanIndexHelper m_beanIndexHelper;

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    public Indexer getIndexer() {
        return m_indexer;
    }

    public void setBeanIndexHelper(BeanIndexHelper beanIndexHelper) {
        m_beanIndexHelper = beanIndexHelper;
    }

    public BeanIndexHelper getBeanIndexHelper() {
        return m_beanIndexHelper;
    }

    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames,
            Type[] types) {
        BeanIndexProperties beanIndexProperties = new BeanIndexProperties(entity, id, state, propertyNames, types);
        getBeanIndexHelper().setupIndexProperties(beanIndexProperties);
        indexBean(entity, id, beanIndexProperties.getState(), beanIndexProperties.getPropertyNames(),
                beanIndexProperties.getTypes(), true);
        return super.onSave(entity, id, state, propertyNames, types);
    }

    public void onDelete(Object entity, Object id, Object[] state_,
            String[] propertyNames_, Type[] types_) {
        removeBean(entity, id);
        super.onDelete(entity, id, state_, propertyNames_, types_);
    }

    public boolean onFlushDirty(Object entity, Object id, Object[] currentState,
            Object[] previousState_, String[] propertyNames, Type[] types) {
        BeanIndexProperties beanIndexProperties = new BeanIndexProperties(entity, id, currentState,
                propertyNames, types);
        getBeanIndexHelper().setupIndexProperties(beanIndexProperties);
        indexBean(entity, id, beanIndexProperties.getState(), beanIndexProperties.getPropertyNames(),
                beanIndexProperties.getTypes(), false);
        return super.onFlushDirty(entity, id, currentState, previousState_, propertyNames, types);
    }

    public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        try {
            boolean modified = super.onLoad(entity, id, state, propertyNames, types);
            if (entity instanceof KeepsOriginalCopy) {
                ((KeepsOriginalCopy<?>) entity).makeBackupAsOriginalCopy();
            }
            // Return the result of the super call (usually false) 
            return modified;
        } catch (Exception e) {
            // Log or handle gracefully
            throw new RuntimeException("onLoad() failed", e);
        }
    }

    private void indexBean(Object bean,
        Object id, 
        Object[] state, 
        String[] fieldNames,
        Type[] types, 
        boolean newInstance ) {

        getIndexer().indexBean( bean, id, state, fieldNames, types, newInstance );
    }

    private void removeBean(Object bean, Object id) {

        getIndexer().removeBean(bean, id);

    }
}
