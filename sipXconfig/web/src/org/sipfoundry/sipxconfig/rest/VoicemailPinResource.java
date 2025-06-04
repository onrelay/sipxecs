/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rest;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.common.AbstractUser;
import org.sipfoundry.sipxconfig.common.User;

public class VoicemailPinResource extends UserResource {
    private String m_newPin;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        m_newPin = (String) getRequest().getAttributes().get("pin");
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {        
        if (!(m_newPin == null) && m_newPin.length() >= AbstractUser.VOICEMAIL_PIN_LEN) {
            User user = getUser();
            user.setVoicemailPin(m_newPin);
            user.setForcePinChange(false);
            getCoreContext().saveUser(user);
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, String.format(
                "Voicemail PIN must be at least %d characters long", AbstractUser.VOICEMAIL_PIN_LEN));
        }
        return null;
    }
}
