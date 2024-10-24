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
package org.sipfoundry.sipxconfig.time;

import java.util.List;

import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;

public interface NtpManager {
    public static final AddressType NTP_ADDRESS = new AddressType("ntpAddress", 123, AddressType.Protocol.udp);
    public static final AddressType NTP_SERVER = new AddressType("ntpServer");
    static final GlobalFeature FEATURE = new GlobalFeature("ntpd");
    static final String CONTEXT_BEAN_NAME = "timeManager";
    static final String TIMEZONE_INI = "timezone.ini";

    void setSystemTimezone(String timezone);

    String getSystemTimezone();

    List<String> getAvailableTimezones();

    NtpSettings getSettings();

    void saveSettings(NtpSettings settings);
}
