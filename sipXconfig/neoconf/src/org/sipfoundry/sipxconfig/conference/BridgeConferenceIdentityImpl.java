/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.conference;

import java.io.Serializable;

import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;

public class BridgeConferenceIdentityImpl extends SipxHibernateDaoSupport<Conference>  implements BridgeConferenceIdentity {
    private Bridge m_bridge;

    public Conference load(Class c, Object id) {
        Conference conf = (Conference) super.loadEntity(c, id);
        if (conf.getBridge().equals(m_bridge)) {
            return  conf;
        }
        return null;
    }

    public void setBridge(Bridge bridge) {
        m_bridge = bridge;
    }
}
