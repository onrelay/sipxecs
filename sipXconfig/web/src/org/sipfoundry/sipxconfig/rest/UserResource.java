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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.security.StandardUserDetailsService;
import org.sipfoundry.sipxconfig.security.UserDetailsImpl;

/**
 * Special type of the resource accessible for an individual users
 */
public class UserResource extends ServerResource {

    private CoreContext m_coreContext;
    private User m_user;

    public UserResource() {
        super();
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        UserDetailsImpl userDetails = StandardUserDetailsService.getUserDetails();
        if (userDetails != null) {
            m_user = m_coreContext.loadUser(userDetails.getUserId());
        }
    }

    
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    protected User getUser() {
        return m_user;
    }

    protected final CoreContext getCoreContext() {
        return m_coreContext;
    }

    @Get
    public Representation represent(Variant variant) throws ResourceException {
        // Override
        return null;
    }
}
