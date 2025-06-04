/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.rest;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_XML;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.commons.rest.XStreamRepresentation;
import org.sipfoundry.sipxconfig.cdr.Cdr;
import org.sipfoundry.sipxconfig.cdr.CdrManager;
import org.sipfoundry.sipxconfig.common.User;

import com.thoughtworks.xstream.XStream;

public class UserActiveCdrsResource extends UserResource {

    private static final String RECIPIENT = "recipient";
    private static final String DURATION = "duration";
    private CdrManager m_cdrManager;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
    }

    @Get
    public Representation represent(Variant variant) throws ResourceException {        
        List<Representable> representableList = null;
        try {
            List<Cdr> cdrs = m_cdrManager.getActiveCallsREST(getUserToQuery());
            representableList = convertCdrs(cdrs);
            return new CdrRepresentation(variant.getMediaType(), representableList);
        } catch (InterruptedException iex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, iex);
        } catch (IOException ioex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ioex);
        }
    }

    protected User getUserToQuery() {
        return getUser();
    }

    protected ArrayList<Representable> convertCdrs(List<Cdr> cdrs) {
        ArrayList<Representable> cdrsArray = new ArrayList<Representable>();
        for (Cdr cdr : cdrs) {
            cdrsArray.add(new Representable(cdr));
        }
        return cdrsArray;
    }

    static class Representable implements Serializable {
        @SuppressWarnings("unused")
        private String m_caller;
        @SuppressWarnings("unused")
        private String m_callerAor;
        @SuppressWarnings("unused")
        private String m_callee;
        @SuppressWarnings("unused")
        private String m_calleeAor;
        @SuppressWarnings("unused")
        private String m_calleeRoute;
        @SuppressWarnings("unused")
        private String m_callDirection;
        @SuppressWarnings("unused")
        private String m_recipient;
        @SuppressWarnings("unused")
        private boolean m_callerInternal;
        @SuppressWarnings("unused")
        private String m_callTypeName;
        @SuppressWarnings("unused")
        private long m_startTime;
        @SuppressWarnings("unused")
        private long m_duration;

        public Representable(Cdr cdr) {
            m_caller = cdr.getCaller();
            m_callerAor = cdr.getCallerAor();
            m_callee = cdr.getCallee();
            m_calleeAor = cdr.getCalleeAor();
            m_calleeRoute = cdr.getCalleeRoute();
            m_callDirection = cdr.getCallDirection();
            m_recipient = cdr.getRecipient();
            m_callerInternal = cdr.getCallerInternal();
            m_callTypeName = cdr.getCallTypeName();
            //put timestamp long value here, the REST client will parse it in any format wanted for display
            m_startTime = cdr.getStartTime().getTime();
            m_duration = cdr.getDuration();
        }
    }

    static class CdrRepresentation extends XStreamRepresentation<Collection<Representable>> {

        public CdrRepresentation(MediaType mediaType, Collection<Representable> object) {
            super(mediaType, object);
        }

        public CdrRepresentation(Representation representation) {
            super(representation);
        }

        @Override
        protected void configureXStream(XStream xstream) {
            xstream.alias("cdrs", List.class);
            xstream.alias("cdr", Representable.class);
            xstream.aliasField("from", Representable.class, "caller");
            xstream.aliasField("from-aor", Representable.class, "callerAor");
            xstream.aliasField("to", Representable.class, "callee");
            xstream.aliasField("to-aor", Representable.class, "calleeAor");
            xstream.aliasField("route", Representable.class, "calleeRoute");
            xstream.aliasField("direction", Representable.class, "callDirection");
            xstream.aliasField(RECIPIENT, Representable.class, RECIPIENT);
            xstream.aliasField("internal", Representable.class, "callerInternal");
            xstream.aliasField("type", Representable.class, "callTypeName");
            xstream.aliasField("start-time", Representable.class, "startTime");
            xstream.aliasField(DURATION, Representable.class, DURATION);
        }
    }

    
    public void setCdrManager(CdrManager cdrManager) {
        m_cdrManager = cdrManager;
    }
}
