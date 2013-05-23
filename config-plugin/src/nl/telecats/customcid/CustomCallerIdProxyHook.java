/**
 *
 *
 * Copyright (c) 2013 eZuce, Inc. All rights reserved.
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
package nl.telecats.customcid;

import org.sipfoundry.sipxconfig.proxy.ProxyHookPlugin;

public class CustomCallerIdProxyHook implements ProxyHookPlugin {
    private CustomCallerIdManager m_mgr;
    private String m_name;
    private String m_value;

    @Override
    public boolean isEnabled() {
        return m_mgr.getSettings().isEnabled();
    }

    public void setCcidManager(CustomCallerIdManager manager) {
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
