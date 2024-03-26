/*
 *
 * Copyright (C) 2015 eZuce Inc.
 *
 */
package de.iant.iab;

import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.proxy.ProxyHookPlugin;

public class IantAocBillingProxyHook implements ProxyHookPlugin {
    private FeatureManager m_mgr;
    private String m_name;
    private String m_value;

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setFeatureManager(FeatureManager manager) {
        m_mgr = manager;
    }

    @Override
    public String getProxyHookName() {
        return m_name;
    }

    public void setProxyHookName(String name) {
        m_name = name;
    }

    @Override
    public String getProxyHookValue() {
        return m_value;
    }

    public void setProxyHookValue(String value) {
        m_value = value;
    }

}
