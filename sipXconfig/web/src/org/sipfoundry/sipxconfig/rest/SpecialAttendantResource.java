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
import static org.sipfoundry.sipxconfig.permission.PermissionName.RECORD_SYSTEM_PROMPTS;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.commons.rest.XStreamRepresentation;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendantManager;

import com.thoughtworks.xstream.XStream;

public class SpecialAttendantResource extends UserResource {

    private AutoAttendantManager m_autoAttendantManager;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
    }


    @Get
    public Representation represent(Variant variant) throws ResourceException {        
        AutoAttendantSpecialModeRestInfo specialModeRest = new AutoAttendantSpecialModeRestInfo();
        specialModeRest.setSpecialMode(m_autoAttendantManager.getSpecialMode());
        return new SpecialAttendantRepresentation(variant.getMediaType(), specialModeRest);
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {        
        m_autoAttendantManager.setAttendantSpecialMode(true, null);
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    @Delete
    public Representation removeRepresentations() throws ResourceException {
        m_autoAttendantManager.setAttendantSpecialMode(false, null);
        getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    public void setAutoAttendantManager(AutoAttendantManager autoAttendantManager) {
        m_autoAttendantManager = autoAttendantManager;
    }

    static class AutoAttendantSpecialModeRestInfo {
        private boolean m_specialMode;

        public void setSpecialMode(boolean specialMode) {
            m_specialMode = specialMode;
        }

        public boolean getSpecialMode() {
            return m_specialMode;
        }
    }

    static class SpecialAttendantRepresentation extends XStreamRepresentation<AutoAttendantSpecialModeRestInfo> {

        public SpecialAttendantRepresentation(MediaType mediaType, AutoAttendantSpecialModeRestInfo object) {
            super(mediaType, object);
        }

        public SpecialAttendantRepresentation(Representation representation) {
            super(representation);
        }

        @Override
        protected void configureXStream(XStream xstream) {
            xstream.alias("specialAttendant", AutoAttendantSpecialModeRestInfo.class);
        }
    }
}
