/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */


package org.sipfoundry.openfire.plugin.presence.servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

import org.sipfoundry.openfire.plugin.presence.SipXOpenfirePlugin;
import org.sipfoundry.openfire.plugin.presence.XmlRpcPresenceProvider;


public class PresenceStatusServlet extends SipXOpenfireServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        if (SipXOpenfirePlugin.getInstance() == null) {
            throw new ServletException("sipx-openfire plugin is not loaded.");
        }

        super.init(
            servletConfig,
            XmlRpcPresenceProvider.SERVER,
            XmlRpcPresenceProvider.SERVICE_NAME,
            XmlRpcPresenceProvider.class
        );
    }
}