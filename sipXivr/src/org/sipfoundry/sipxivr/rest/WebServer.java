/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxivr.rest;
import java.util.Map;

import org.apache.log4j.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;

public class WebServer implements BeanFactoryAware {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");
    private ServletHandler m_servletHandler;
    private int m_httpPort;
    private int m_publicHttpPort;
    private BeanFactory m_beanFactory;
    private SipxIvrUserLoginService m_userLoginService;
    private DigestAuthenticator m_digestAuthenticator;

    public void init() {
        Map<String, RestApiBean> beans = ((ListableBeanFactory) m_beanFactory).getBeansOfType(RestApiBean.class);
        for (RestApiBean bean : beans.values()) {
            addServlet(bean.getName(), bean.getPathSpec(), bean.getServletClass());
        }
        start();
    }

    private void addServlet(String name, String pathSpec, String servletClass) {
        m_servletHandler.addServletWithMapping(servletClass, pathSpec);
        LOG.info(String.format("Adding Servlet %s on %s", pathSpec, servletClass));
    }

    private void start() {
        try {
            Server server = new Server();

            // Internal HTTP connector
            ServerConnector internalConnector = new ServerConnector(server);
            internalConnector.setPort(m_httpPort);
            server.addConnector(internalConnector);

            // Public HTTP connector
            ServerConnector publicConnector = new ServerConnector(server);
            publicConnector.setPort(m_publicHttpPort);
            server.addConnector(publicConnector);

            // Servlet context
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            // Security constraint
            Constraint digestConstraint = Constraint.from("IvrRole");

            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec("/*");
            mapping.setConstraint(digestConstraint);

            // Security handler
            ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
            securityHandler.setAuthenticator(m_digestAuthenticator);
            securityHandler.setRealmName(m_userLoginService.getName());
            securityHandler.setLoginService(m_userLoginService);
            securityHandler.addConstraintMapping(mapping);

            // Attach servlet handler
            securityHandler.setHandler(m_servletHandler);

            // Set security handler as the context handler
            context.setSecurityHandler(securityHandler);

            server.setHandler(context);

            LOG.info(String.format("Starting Jetty server on ports *:%d, *:%d", m_httpPort, m_publicHttpPort));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setServletHandler(ServletHandler handler) {
        m_servletHandler = handler;
    }

    public void setUserLoginService(SipxIvrUserLoginService userLoginService) {
        m_userLoginService = userLoginService;
    }

    public void setHttpPort(int httpPort) {
        m_httpPort = httpPort;
    }

    public void setPublicHttpPort(int publicHttpPort) {
        m_publicHttpPort = publicHttpPort;
    }

    public void setBeanFactory(BeanFactory factory) {
        m_beanFactory = factory;
    }

    public void setDigestAuthenticator(DigestAuthenticator digestAuthenticator) {
        m_digestAuthenticator = digestAuthenticator;
    }
}

