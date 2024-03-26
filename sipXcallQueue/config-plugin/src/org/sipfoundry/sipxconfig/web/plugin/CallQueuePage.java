/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.web.plugin;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;

/*sipXecs WEB components API imports */
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.callqueue.CallQueueSettings;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class CallQueuePage extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "plugin/CallQueuePage";
    private static final String SETTINGS = "settings";

    @Persist
    @InitialValue(value = "literal:queues")
    public abstract String getTab();

    public abstract void setTab(String id);

    public abstract CallQueueSettings getSettings();

    public abstract void setSettings(CallQueueSettings settings);

    @InjectObject("spring:callQueueContext")
    public abstract CallQueueContext getCallQueueContext();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public void editQueues() {
        setTab("queues");
    }

    public void editAgents() {
        setTab("agents");
    }

    public void editSettings() {
        setTab(SETTINGS);
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        if (null == getSettings() && getTab().equals(SETTINGS)) {
            setSettings(getCallQueueContext().getSettings());
        }
    }

    public void saveSettings() {
        if (null != getCallQueueContext()) {
            if (null != getSettings()) {
                getCallQueueContext().saveSettings(getSettings());
            }
        }
    }
}
