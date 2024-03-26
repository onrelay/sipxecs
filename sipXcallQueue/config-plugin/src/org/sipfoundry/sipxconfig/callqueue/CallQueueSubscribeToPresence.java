/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.callqueue;

import java.util.List;

import org.sipfoundry.sipxconfig.speeddial.SpeedDialButtons;
import org.sipfoundry.sipxconfig.speeddial.SubscribeToPresenceValidator;
import org.springframework.beans.factory.annotation.Required;

public class CallQueueSubscribeToPresence implements SubscribeToPresenceValidator{

    private CallQueueContext m_callQueueContext;
    
    @Override
    public boolean validateSubscriptions(SpeedDialButtons speedDial, String number) {
        // Do not validate numbers which are also extensions for CallQueue
        List<CallQueueExtension> extensions = m_callQueueContext.getFreeswitchExtensions();
        for (CallQueueExtension extension : extensions) {
            if (number.equals(extension.getExtension())) {
                return false;
            }
        }
        return true;
    }

    @Required
    public void setCallQueueContext(CallQueueContext callQueueContext) {
        m_callQueueContext = callQueueContext;
    }

}
