/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.cdr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.cdr.Cdr;
import org.sipfoundry.sipxconfig.cdr.CdrManager;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.site.user_portal.UserBasePage;
import org.sipfoundry.sipxconfig.time.NtpManager;

public abstract class CdrPage extends UserBasePage {
    private static final String ACTIVE_TAB = "active";
    private static final String HISTORIC_TAB = "historic";
    private static final String REPORTS_TAB = "reports";

    @Override
    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Persist(value = "client")
    @InitialValue(value = "literal:active")
    public abstract String getTab();

    public abstract void setTab(String tab);

    @InjectObject("spring:featureManager")
    public abstract FeatureManager getFeatureManager();

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    @InjectObject("spring:cdrManager")
    public abstract CdrManager getCdrManager();

    public abstract boolean isCallResolverInstalled();

    public abstract void setCallResolverInstalled(boolean callResolverInstalled);

    public abstract int getTotalActiveCalls();

    public abstract void setTotalActiveCalls(int total);

    @Persist
    public abstract String getSelectedTimeZone();

    public abstract void setSelectedTimeZone(String selectedTimeZone);

    @Persist
    public abstract String getDefaultTimezone();

    public abstract void setDefaultTimezone(String defaultTimezone);

    @InjectObject(value = "spring:ntpManager")
    public abstract NtpManager getTimeManager();

    public Collection<String> getTabNames() {
        if (isCallResolverInstalled()) {
            return Arrays.asList(ACTIVE_TAB, HISTORIC_TAB, REPORTS_TAB);
        }
        return Arrays.asList(HISTORIC_TAB, REPORTS_TAB);
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        setCallResolverInstalled(getFeatureManager().isFeatureEnabled(CdrManager.FEATURE)
                && getFeatureManager().isFeatureEnabled(ProxyManager.FEATURE));
        if (!isCallResolverInstalled() && getTab().equals(ACTIVE_TAB)) {
            setTab(HISTORIC_TAB);
        }

        if (getSelectedTimeZone() == null || getSelectedTimeZone().equals(getDefaultTimezone())) {
            String defaultTimezone = CdrPage.getDefaultTimeZoneId(null, getTimeManager());
            setSelectedTimeZone(defaultTimezone);
            setDefaultTimezone(defaultTimezone);
        }

        List<Cdr> activeCalls = new ArrayList<Cdr>();
        try {
            activeCalls = getCdrManager().getActiveCalls();
        } catch (Exception e) {
            activeCalls = Collections.emptyList();
        }
        setTotalActiveCalls(activeCalls.size());
    }

    protected static String getDefaultTimeZoneId(User user, NtpManager timeManager) {
        TimeZone timeZone = null;
        if (user != null) {
            timeZone = user.getTimezone();
        } else {
            String systemTimezone = timeManager.getSystemTimezone();
            timeZone = (systemTimezone == null) ? TimeZone.getDefault() : TimeZone.getTimeZone(systemTimezone);
        }
        return timeZone.getID();
    }
}
