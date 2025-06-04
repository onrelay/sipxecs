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
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.common.User;

public class PersonalAttendantOperatorResource extends UserResource {
    private String m_operator;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        m_operator = (String) getRequest().getAttributes().get("operator");
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {        
        User user = getUser();
        user.getSettings().getSetting("personal-attendant/operator").setValue(m_operator);
        getCoreContext().saveUser(user);
        return null;
    }

}
