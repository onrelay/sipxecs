/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.dialplan.sbc;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.sbc.AuxSbc;
import org.sipfoundry.sipxconfig.sbc.Sbc;
import org.sipfoundry.sipxconfig.sbc.SbcDevice;
import org.sipfoundry.sipxconfig.sbc.SbcManager;
import org.sipfoundry.sipxconfig.sbc.SbcRoutes;

public abstract class EditSbc extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "dialplan/sbc/EditSbc";

    @InjectObject(value = "spring:sbcManager")
    public abstract SbcManager getSbcManager();

    @Persist
    public abstract Integer getSbcId();

    public abstract void setSbcId(Integer sbcId);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract AuxSbc getSbc();

    public abstract void setSbc(AuxSbc sbc);

    public abstract void setSelectedSbcDevice(SbcDevice selectedSbcDevice);

    public abstract SbcDevice getSelectedSbcDevice();

    @Persist
    public abstract void setEnforceInternetCallingSupport(boolean enforce);

    public abstract boolean getEnforceInternetCallingSupport();

    public void pageBeginRender(PageEvent event) {

        AuxSbc sbc = getSbc();
        if( sbc == null ) {
            sbc = new AuxSbc();
        }

        Integer sbcId = getSbcId();

        if( sbcId != null && sbc.isNew() ) {
            sbc = getSbcManager().loadSbc(sbcId);

            SbcDevice sbcDevice = sbc.getSbcDevice();
            if (sbcDevice != null) {
                setSelectedSbcDevice(sbcDevice);
            }
        }  

        setSbc(sbc);
    }

    public void save() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        AuxSbc sbc = getSbc();

        if (sbc.isEnabled() && (sbc.getAddress() == null || sbc.getPort() == 0)) {
            throw new UserException(getMessages().getMessage("error.requiredSbc"));
        }

        if (sbc.getRoutes().isEmpty()) {
            throw new UserException(getMessages().getMessage("error.requiredSubnet"));
        }

        sbc.setSbcDevice(getSelectedSbcDevice());
        getSbcManager().saveSbc(sbc);
        if (getSbcId() == null) {
            setSbcId(sbc.getId());
            setSbc( sbc );
        }
    }
}
