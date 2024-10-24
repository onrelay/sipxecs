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
package org.sipfoundry.sipxconfig.freeswitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.commserver.SettingsWithLocation;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class FreeswitchSettings extends SettingsWithLocation implements DeployConfigOnEdit {
    public static final int RTP_START_PORT = 11000;
    public static final int RTP_END_PORT = 12999;
    private static final String FREESWITCH_XMLRPC_PORT = "freeswitch-config/FREESWITCH_XMLRPC_PORT";
    private static final String FREESWITCH_SIP_PORT = "freeswitch-config/FREESWITCH_SIP_PORT";
    private static final String FREESWITCH_CODECS = "freeswitch-config/FREESWITCH_CODECS";
    private static final String FREESWITCH_SECURE_RTP = "freeswitch-config/FREESWITCH_SECURE_RTP";
    private static final String FREESWITCH_BLIND_TRANSFER = "freeswitch-config/FREESWITCH_BLIND_TRANSFER";
    private static final String FREESWITCH_SIMPLIFY = "freeswitch-config/FREESWITCH_SIMPLIFY";
    private static final String FREESWITCH_MAX_FORWARDS = "freeswitch-config/FREESWITCH_MAX_FORWARDS";
    private static final String FREESWITCH_CORE = "freeswitch-config/FREESWITCH_CORE";

    public int getEventSocketPort() {
        return 8084; // not configurable at this time, no particular reason. --Douglas
    }

    public int getAccEventSocketPort() {
        return 8184; // not configurable at this time, no particular reason. --Douglas
    }

    public int getCallbackEventSocketPort() {
        return 8284;
    }

    public int getXmlRpcPort() {
        return (Integer) getSettingTypedValue(FREESWITCH_XMLRPC_PORT);
    }

    public int getFreeswitchSipPort() {
        return (Integer) getSettingTypedValue(FREESWITCH_SIP_PORT);
    }

    public boolean isSecureRtpEnabled() {
        return (Boolean) getSettingTypedValue(FREESWITCH_SECURE_RTP);
    }

    public boolean isBlindTransferEnabled() {
        return (Boolean) getSettingTypedValue(FREESWITCH_BLIND_TRANSFER);
    }

    public boolean isSimplifyEnabled() {
        return (Boolean) getSettingTypedValue(FREESWITCH_SIMPLIFY);
    }

    public Integer getMaxForwards() {
        if (getSettingTypedValue(FREESWITCH_MAX_FORWARDS) == null) {
            return null;
        }
        return (Integer) getSettingTypedValue(FREESWITCH_MAX_FORWARDS);
    }

    public boolean isCoreEnabled() {
        return (Boolean) getSettingTypedValue(FREESWITCH_CORE);
    }

    public class Defaults {
        @SettingEntry(path = FREESWITCH_CODECS)
        public List<String> getFreeswitchCodecs() {
            ArrayList<String> returnList = new ArrayList<String>();
            returnList.add("G722");
            returnList.add("PCMU@20i");
            returnList.add("PCMA@20i");
            returnList.add("speex");
            returnList.add("L16");
            return returnList;
        }
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("freeswitch/freeswitch.xml");
    }

    @Override
    public String getBeanId() {
        return "freeswithSettings";
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) FreeswitchFeature.FEATURE, (Feature) ProxyManager.FEATURE);
    }
}
