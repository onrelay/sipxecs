/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.device;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Loads phone models from bean factory BeanFactoryPhoneModelSource
 */
public class BeanFactoryModelSource<T extends Model> implements ModelSource, BeanFactoryAware {

    private ListableBeanFactory m_beanFactory;
    private Map<String, T> m_modelCache = new TreeMap<String, T>();
    /** T.class, but no such access at compile time */
    private Class m_class;

    public BeanFactoryModelSource(String className) {
        try {
            m_class = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns collection of retrieved models. Please note that returned collection is not
     * modifiable.
     *
     */
    public Collection<T> getModels() {
        m_modelCache = m_beanFactory.getBeansOfType(m_class, false, false);
        return m_modelCache.values();
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
        // invalidate cache
        m_modelCache = new TreeMap<String, T>();
    }

    public T getModel(String modelId) {

        T model = m_modelCache.get(modelId);
        if( model != null ){
            return model;
        }
        model = (T) m_beanFactory.getBean(modelId);
        if (model == null) {
            throw new IllegalArgumentException("No such model with id '" + modelId + "'");
        }
        m_modelCache.put(modelId, model);
        return model;
    }
}
