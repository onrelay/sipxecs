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
import org.hibernate.persister.entity.EntityPersister;

import org.sipfoundry.sipxconfig.common.SpringHibernateInterceptor;
import org.sipfoundry.sipxconfig.common.event.KeepsOriginalCopy;

/**
 * This is used to indexing on load
 *   ...AND completely unrelated...
 * support KeepOriginalCopy interface.
 */
public class LoadIndexingInterceptor extends SpringHibernateInterceptor {
    private Indexer m_indexer;
    private BeanIndexHelper m_beanIndexHelper;

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    public void setBeanIndexHelper(BeanIndexHelper beanIndexHelper) {
        m_beanIndexHelper = beanIndexHelper;
    }

    @Override
    public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {

        try {
            if( !super.onLoad( entity, id, state, propertyNames, types ) ) {
                return false;
            }

            if (entity instanceof KeepsOriginalCopy) {
                ((KeepsOriginalCopy<?>) entity).makeBackupAsOriginalCopy();
            }

            BeanIndexProperties bip = new BeanIndexProperties(entity, id, state, propertyNames, types);
            m_beanIndexHelper.setupIndexProperties(bip);
            m_indexer.indexBean(entity, id, bip.getState(), bip.getPropertyNames(), bip.getTypes(), true);
            return true;

        } catch (Exception e) {
            // Log or handle gracefully
            throw new RuntimeException("onLoad() failed", e);
        }
    }
}
