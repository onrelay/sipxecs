/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone.polycom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.PhoneModel;

/**
 * Static differences in polycom phone models
 */
public final class PolycomModel extends PhoneModel {
    /** Firmware 2.0 or beyond */
    public static final DeviceVersion VER_2_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "2.0");
    public static final DeviceVersion VER_3_1_X = new DeviceVersion(PolycomPhone.BEAN_ID, "3.1.X");
    public static final DeviceVersion VER_3_2_X = new DeviceVersion(PolycomPhone.BEAN_ID, "3.2.X");
    public static final DeviceVersion VER_3_3_X = new DeviceVersion(PolycomPhone.BEAN_ID, "3.3.X");
    public static final DeviceVersion VER_4_0_X = new DeviceVersion(PolycomPhone.BEAN_ID, "4.0.X");
    public static final DeviceVersion VER_4_1_X = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.X");
    public static final DeviceVersion VER_4_1_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.0");
    public static final DeviceVersion VER_4_1_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.2");
    public static final DeviceVersion VER_4_1_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.3");
    public static final DeviceVersion VER_4_1_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.4");
    public static final DeviceVersion VER_4_1_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.5");
    public static final DeviceVersion VER_4_1_6 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.6");
    public static final DeviceVersion VER_4_1_7 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.7");
    public static final DeviceVersion VER_4_1_8 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.8");
    public static final DeviceVersion VER_5_0_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.0.0");
    public static final DeviceVersion VER_5_0_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.0.1");
    public static final DeviceVersion VER_5_0_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.0.2");
    public static final DeviceVersion VER_5_1_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.1.1");
    public static final DeviceVersion VER_5_1_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.1.2");
    public static final DeviceVersion VER_5_1_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.1.3");
    public static final DeviceVersion VER_5_2_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.0");
    public static final DeviceVersion VER_5_2_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.1");
    public static final DeviceVersion VER_5_2_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.2");
    public static final DeviceVersion VER_5_2_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.3");
    public static final DeviceVersion VER_5_2_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.4");
    public static final DeviceVersion VER_5_2_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.2.5");
    public static final DeviceVersion VER_5_3_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.3.0");
    public static final DeviceVersion VER_5_3_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.3.1");
    public static final DeviceVersion VER_5_3_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.3.2");
    public static final DeviceVersion VER_5_3_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.3.3");
    public static final DeviceVersion VER_5_4_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.0");
    public static final DeviceVersion VER_5_4_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.1");
    public static final DeviceVersion VER_5_4_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.2");
    public static final DeviceVersion VER_5_4_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.3");
    public static final DeviceVersion VER_5_4_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.4");
    public static final DeviceVersion VER_5_4_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.5");
    public static final DeviceVersion VER_5_4_6 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.6");
    public static final DeviceVersion VER_5_5_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.5.0");
    public static final DeviceVersion VER_5_5_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.5.1");
    public static final DeviceVersion VER_5_5_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.5.2");
    public static final DeviceVersion VER_5_6_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.0");
    public static final DeviceVersion VER_5_6_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.1");
    public static final DeviceVersion VER_5_6_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.2");
    public static final DeviceVersion VER_5_6_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.3");
    public static final DeviceVersion VER_5_6_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.4");
    public static final DeviceVersion VER_5_6_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.6.5");
    public static final DeviceVersion VER_5_7_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.7.0");
    public static final DeviceVersion VER_5_7_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.7.1");
    public static final DeviceVersion VER_5_7_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.7.2");
    public static final DeviceVersion VER_5_7_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.7.3");
    public static final DeviceVersion VER_5_7_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.7.4");
    public static final DeviceVersion VER_5_8_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.8.0");
    public static final DeviceVersion VER_5_8_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.8.1");
    public static final DeviceVersion VER_5_8_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.8.2");
    public static final DeviceVersion VER_5_8_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.8.4");
    public static final DeviceVersion VER_5_8_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.8.5");
    public static final DeviceVersion VER_5_9_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.0");
    public static final DeviceVersion VER_5_9_1 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.1");
    public static final DeviceVersion VER_5_9_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.2");
    public static final DeviceVersion VER_5_9_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.3");
    public static final DeviceVersion VER_5_9_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.4");
    public static final DeviceVersion VER_5_9_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.5");
    public static final DeviceVersion VER_5_9_6 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.6");
    public static final DeviceVersion VER_5_9_7 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.7");
    public static final DeviceVersion VER_5_9_8 = new DeviceVersion(PolycomPhone.BEAN_ID, "5.9.8");

    /** Firmware for Polycom RealPresence */
    public static final DeviceVersion VER_5_4_0_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.0_RP");
    public static final DeviceVersion VER_5_4_1_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.1_RP");
    public static final DeviceVersion VER_5_4_2_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.2_RP");
    public static final DeviceVersion VER_5_4_3_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.3_RP");
    public static final DeviceVersion VER_5_4_4_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.4_RP");
    public static final DeviceVersion VER_5_4_5_RP = new DeviceVersion(PolycomPhone.BEAN_ID, "5.4.5_RP");    

    public static final DeviceVersion[] SUPPORTED_VERSIONS = new DeviceVersion[] {
        VER_3_1_X, 
        VER_3_2_X, 
        VER_4_0_X, 
        VER_4_1_X, VER_4_1_0, VER_4_1_2, VER_4_1_3, VER_4_1_4, VER_4_1_5, VER_4_1_6, VER_4_1_7, VER_4_1_8, 
        VER_5_0_0, VER_5_0_1, VER_5_0_2, 
        VER_5_1_1, VER_5_1_2, VER_5_1_3,
        VER_5_2_0, VER_5_2_1, VER_5_2_2, VER_5_2_3, VER_5_2_4, VER_5_2_5, 
        VER_5_3_0, VER_5_3_1, VER_5_3_2,
        VER_5_3_3, VER_5_4_0, VER_5_4_1, VER_5_4_2, VER_5_4_3, VER_5_4_4, VER_5_4_5, VER_5_4_6, 
        VER_5_5_0, VER_5_5_1, VER_5_5_2, 
        VER_5_6_0, VER_5_6_1, VER_5_6_2, VER_5_6_3, VER_5_6_4, VER_5_6_5, 
        VER_5_7_0, VER_5_7_1, VER_5_7_2, VER_5_7_3, VER_5_7_4, 
        VER_5_8_0, VER_5_8_1, VER_5_8_2, VER_5_8_4, VER_5_8_5, 
        VER_5_9_0, VER_5_9_1, VER_5_9_2, VER_5_9_3, VER_5_9_4, VER_5_9_5, VER_5_9_6, VER_5_9_7, VER_5_9_8, 
        VER_5_4_0_RP, VER_5_4_1_RP, VER_5_4_2_RP, VER_5_4_3_RP, VER_5_4_4_RP, VER_5_4_5_RP
    };
    private static final Log LOG = LogFactory.getLog(PolycomModel.class);
    private DeviceVersion m_deviceVersion;

    public PolycomModel() {
        super(PolycomPhone.BEAN_ID);
        setEmergencyConfigurable(true);
    }

    /**
     * checks if this version is greater than 4.0
     *
     * @return
     */
    protected static boolean is40orLater(DeviceVersion v) {
        return PolycomModel.compareVersions(v, new Integer[] {
            4, 0
        }) >= 0;
    }

    /**
     * generic method to compare versions
     *
     * @param deviceVersion
     * @param testVersion
     * @return negative if test version is greater than device version; positive if it is smaller
     *         and 0 if it they are equal
     */
    protected static int compareVersions(DeviceVersion deviceVersion, Integer[] testVersion) {
        DeviceVersion deviceVersionCopy = deviceVersion;
        if (deviceVersionCopy == null) {
            // This is wrong!! LOG AN ERROR, but assume it's about 3.3
            deviceVersionCopy = PolycomModel.VER_3_3_X;
            LOG.error("Phone has NULL version id. It might be that it is has 3.3 firmware. Please correct the db!");
        }
        String versionId = deviceVersionCopy.getVersionId();
        String[] tokens = versionId.split("\\.");
        for (int i = 0; i < testVersion.length; i++) {
            Integer ver;
            Integer test = testVersion[i];
            if (tokens[i].equals("X")) {
                ver = 0;
            } else {
                ver = Integer.parseInt(tokens[i]);
            }
            if (ver != test) {
                return ver - test;
            }
        }
        return 0;
    }

    public static DeviceVersion getPhoneDeviceVersion(String version) {
        for (DeviceVersion deviceVersion : SUPPORTED_VERSIONS) {
            if (deviceVersion.getName().contains(version)) {
                return deviceVersion;
            }
        }
        return VER_2_0;
    }

    public void setDefaultVersion(DeviceVersion ver) {
        m_deviceVersion = ver;
    }

    public DeviceVersion getDefaultVersion() {
        return m_deviceVersion;
    }
}
