/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin.presence.servlets;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;
import org.apache.log4j.Logger;

public class SipXOpenfireServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private XmlRpcServletServer server;

    private static final ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();

    private static final Logger log = Logger.getLogger(SipXOpenfireServlet.class);

    public void init(ServletConfig servletConfig, String serverName, String serviceName, Class< ? > provider)
            throws ServletException {

        super.init(servletConfig);
        log.info(String.format("initializing Servlet for service name %s and provider %s", serviceName,
                provider.getCanonicalName()));

        PropertyHandlerMapping handlerMapping = new PropertyHandlerMapping();
        try {
            handlerMapping.setAuthenticationHandler(new BasicXmlRpcAuthenticationHandler() {
                    @Override
                    public boolean isAuthorized(XmlRpcRequest request) throws XmlRpcException {

                        String authenticationExcludePath = "/sipx-openfire/" + serviceName;

                        HttpServletRequest httpServletRequest = currentRequest.get();

                        if (httpServletRequest != null) {
                            String path = httpServletRequest.getRequestURI();
                            if (path != null && path.startsWith(authenticationExcludePath)) {
                                return true; // Skip authentication
                            }
                        }

                        // Fallback to normal authentication
                        return super.isAuthorized(request);
                    }
            });
            handlerMapping.addHandler(serverName, provider);
        } catch (XmlRpcException e) {
            throw new ServletException("XmlRpcInitialization failed");
        }

        server = new XmlRpcServletServer();

        XmlRpcServerConfigImpl serverConfig = new XmlRpcServerConfigImpl();
        serverConfig.setKeepAliveEnabled(true);
        serverConfig.setEnabledForExceptions(true);
        serverConfig.setEnabledForExtensions(true);
        server.setMaxThreads(4);

        server.setConfig(serverConfig);
        server.setHandlerMapping(handlerMapping);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            currentRequest.set(request);
            server.execute(request, response);
        } finally {
            currentRequest.remove(); // Always clean up
        }
    }
}
