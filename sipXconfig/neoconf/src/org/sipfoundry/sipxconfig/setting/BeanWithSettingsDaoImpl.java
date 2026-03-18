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
package org.sipfoundry.sipxconfig.setting;

import java.util.List;

import org.hibernate.Session;

import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

public class BeanWithSettingsDaoImpl<T extends BeanWithSettings> extends SipxHibernateDaoSupport<BeanWithSettings>
        implements BeanWithSettingsDao<T>, BeanFactoryAware {
    private Class<T> m_class;
    private ListableBeanFactory m_beanFactory;

    public BeanWithSettingsDaoImpl(String className) throws ClassNotFoundException {
        m_class = (Class<T>) Class.forName(className);
    }

    public BeanWithSettingsDaoImpl(Class<T> type) {
        m_class = type;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public T findOrCreateOne() {

        T beanWithSettings = super.getEntity( m_class );

        if( beanWithSettings == null ) {
            beanWithSettings = m_beanFactory.getBean(m_class);
        }

        return beanWithSettings;
    }

    @Override
    public List<T> findAll() {
        return super.getEntities( m_class );
    }

    @Override
    public void upsert(T beanWithSettings ) {
        super.saveEntity( beanWithSettings );
    }
}
