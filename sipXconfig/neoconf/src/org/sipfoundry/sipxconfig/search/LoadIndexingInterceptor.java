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

import org.sipfoundry.sipxconfig.common.event.KeepsOriginalCopy;


public class LoadIndexingInterceptor extends IndexingInterceptor {

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
            getBeanIndexHelper().setupIndexProperties(bip);
            getIndexer().indexBean(entity, id, bip.getState(), bip.getPropertyNames(), bip.getTypes(), true);
            return true;

        } catch (Exception e) {
            // Log or handle gracefully
            throw new RuntimeException("onLoad() failed", e);
        }
    }
}
