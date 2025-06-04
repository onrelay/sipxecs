/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.rest;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_XML;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendantManager;

public class SelectSpecialAttendantResource extends UserResource {
    private AutoAttendantManager m_autoAttendantManager;
    private String m_attendantId;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
        m_attendantId = (String) request.getAttributes().get("attendant");
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {        
        AutoAttendant aa = m_autoAttendantManager.getAutoAttendantBySystemName(m_attendantId);
        if (aa == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        boolean specialMode = m_autoAttendantManager.getSpecialMode();
        m_autoAttendantManager.setAttendantSpecialMode(specialMode, aa);
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    @Delete
    public void removeRepresentations() throws ResourceException {
        AutoAttendant aa = m_autoAttendantManager.getAutoAttendantBySystemName(m_attendantId);
        if (aa == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }

        if (m_autoAttendantManager.getSpecialMode()) {
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
            return;
        }

        m_autoAttendantManager.deselectSpecial(aa);
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
    }

    
    public void setAutoAttendantManager(AutoAttendantManager autoAttendantManager) {
        m_autoAttendantManager = autoAttendantManager;
    }
}
