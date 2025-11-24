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
import java.util.TimeZone;

import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.springframework.dao.support.DataAccessUtils;

public class TimeZoneManagerImpl extends SipxHibernateDaoSupport<DeviceTimeZone> implements TimeZoneManager {

    public void setDeviceTimeZone(DeviceTimeZone dtz) {
        super.saveEntity(dtz);
        getDaoEventPublisher().publishSave(dtz);
    }

    public DeviceTimeZone getDeviceTimeZone() {
        Collection<DeviceTimeZone> timeZones = super.loadAllEntities(DeviceTimeZone.class);
        DeviceTimeZone dtz  = DataAccessUtils.singleResult(timeZones);
        if (dtz != null) {
            return dtz;
        }

        TimeZone tz = TimeZone.getDefault();
        dtz = new DeviceTimeZone();
        dtz.setTimeZone(tz);
        setDeviceTimeZone(dtz);
        return dtz;
    }
}
