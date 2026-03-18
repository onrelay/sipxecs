/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.setting;

import org.sipfoundry.sipxconfig.common.BeanWithId;

public abstract class BeanWithSettings extends BeanWithId {

    private static final String EMPTY_STRING = "";

    private ModelFilesContext m_modelFilesContext;
    private Setting m_settings;
    private BeanWithSettingsModel m_model;

    /**
     * While settings are getting decorated, this represents the settings that should be decorated
     */
    private ValueStorage m_valueStorage;

    public BeanWithSettings() {
        initializeSettingModel();
    }

    protected void initializeSettingModel() {
        m_model = new BeanWithSettingsModel(this);
    }

    /**
     * Called the when someone needs to access settings for the first time.
     */
    protected void initialize() {
        // default implementation empty
    }

    protected synchronized void setSettingModel(BeanWithSettingsModel model) {
        m_model = model;
    }

    protected synchronized BeanWithSettingsModel getSettingModel() {
        return m_model;
    }

    public synchronized void addDefaultSettingHandler(SettingValueHandler handler) {
        m_model.addDefaultsHandler(handler);
    }

    public void addDefaultBeanSettingHandler(Object bean) {
        addDefaultSettingHandler(new BeanValueStorage(bean));
    }

    /**
     * @return decorated model - use this to modify phone settings
     */
    public synchronized Setting getSettings() {
        if (m_settings != null) {
            return m_settings;
        }
        m_settings = loadSettings();
        m_model.setSettings(m_settings);
        initialize();
        return m_settings;
    }

    protected abstract Setting loadSettings();

    public synchronized void setSettings(Setting settings) {
        m_settings = settings;
        m_model.setSettings(m_settings);
    }

    public synchronized void setValueStorage(ValueStorage valueStorage) {
        m_valueStorage = valueStorage;
    }

    public synchronized ValueStorage getValueStorage() {
        return m_valueStorage;
    }

    public synchronized ValueStorage getInitializeValueStorage() {
        if (m_valueStorage == null) {
            m_valueStorage = new ValueStorage();
        }

        return m_valueStorage;
    }

    public String getSettingValue(String path) {
        if (getSetting(path) != null) {
            return getSetting(path).getValue();
        }
        return EMPTY_STRING;
    }

    public String getSettingDefaultValue(String path) {
        if (getSetting(path) != null) {
            return getSetting(path).getDefaultValue();
        }
        return EMPTY_STRING;
    }

    public Object getSettingTypedValue(String path) {
        if (getSetting(path) != null) {
            return getSetting(path).getTypedValue();
        }
        return null;
    }

    private Setting getSetting(String path) {
        if (getSettings() != null) {
            return getSettings().getSetting(path);
        }
        return null;
    }

    public void setSettingValue(String path, String value) {
        if (getSetting(path) != null) {
            Setting setting = getSetting(path);
            setting.setValue(value);
        }
    }

    public void setSettingTypedValue(String path, Object value) {
        Setting setting = getSetting(path);
        setting.setTypedValue(value);
    }

    public synchronized void setModelFilesContext(ModelFilesContext modelFilesContext) {
        m_modelFilesContext = modelFilesContext;
    }

    public synchronized ModelFilesContext getModelFilesContext() {
        return m_modelFilesContext;
    }
}
