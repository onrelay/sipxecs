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
package org.sipfoundry.sipxconfig.proxy;


import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class ProxySettings extends PersistableSettings implements DeployConfigOnEdit {
    public static final String LOG_SETTING = "proxy-configuration/SIPX_PROXY_LOG_LEVEL";
    public static final String SIP_PORT_SETTING = "proxy-configuration/SIPX_PROXY_TCP_PORT";
    public static final String SIP_UDP_PORT_SETTING = "proxy-configuration/SIPX_PROXY_UDP_PORT";
    public static final String SIP_SECURE_PORT_SETTING = "proxy-configuration/SIPX_PROXY_TLS_PORT";
    public static final String DISABLE_REGISTRAR_DNS_LOOKUP_SETTING = "proxy-configuration/SIPX_DISABLE_REGISTRAR_DNS_LOOKUP";
    public static final String DEFAULT_SERIAL_EXPIRES_SETTING = "proxy-configuration/SIPX_PROXY_DEFAULT_SERIAL_EXPIRES";
    public static final String INFO_LOG_LEVEL = "INFO";
    public static final String NOTICE_LOG_LEVEL = "NOTICE";
    public static final String DEBUG_LOG_LEVEL = "DEBUG";

    public int getSipTcpPort() {
        return (Integer) getSettingTypedValue(SIP_PORT_SETTING);
    }

    public int getSipUdpPort() {
        return (Integer) getSettingTypedValue(SIP_UDP_PORT_SETTING);
    }

    public int getSecureSipPort() {
        return (Integer) getSettingTypedValue(SIP_SECURE_PORT_SETTING);
    }

    public int getDefaultInitDelay() {
        return (Integer) getSettingTypedValue(DEFAULT_SERIAL_EXPIRES_SETTING);
    }

    public boolean isDNSLookupDisable() {
        return (Boolean) getSettingTypedValue(DISABLE_REGISTRAR_DNS_LOOKUP_SETTING);
    }

    public String getLogLevel() {
        return (String) getSettingTypedValue(LOG_SETTING);
    }

    public void setLogLevel(String logLevel) {
        setSettingTypedValue(LOG_SETTING, logLevel);
    }

    public boolean isSipDiagramEnable() {
        return false;
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxproxy/sipxproxy.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) ProxyManager.FEATURE);
    }

    @Override
    public String getBeanId() {
        return "proxySettings";
    }
}
