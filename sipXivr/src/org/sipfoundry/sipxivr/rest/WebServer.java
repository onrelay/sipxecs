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
import java.util.Collections;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import jakarta.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.ServletMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;

public class WebServer implements BeanFactoryAware {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");
    private ServletHandler m_servletHandler;
    private int m_httpPort;
    private int m_publicHttpPort;
    private BeanFactory m_beanFactory;
    private SipxIvrUserLoginService m_userLoginService;
    private SipXSecurityHandler m_securityHandler;
    private DigestAuthenticator m_digestAuthenticator;

    public void init() {
        Map<String, RestApiBean> beans = ((ListableBeanFactory) m_beanFactory).getBeansOfType(RestApiBean.class);
        for (RestApiBean bean : beans.values()) {
            addServlet(bean.getName(), bean.getPathSpec(), bean.getServletClass());
        }
        start();
    }

    private void addServlet(String name, String pathSpec, String servletClass) {

        try {
            Class<? extends Servlet> clazz = (Class<? extends Servlet>) Class.forName(servletClass);

            ServletHolder servletHolder = new ServletHolder(name, clazz);
            m_servletHandler.addServlet(servletHolder);
            
            ServletMapping servletMapping = new ServletMapping();
            servletMapping.setServletName(name);
            servletMapping.setPathSpecs(new String[] { pathSpec });
            m_servletHandler.addServletMapping(servletMapping);

            LOG.info(String.format("Adding Servlet %s [Class: %s] on path %s", name, servletClass, pathSpec));
            
        } catch (ClassNotFoundException e) {
            LOG.error("Could not find servlet class: " + servletClass, e);
        }
    }

    private void start() {
        try {
            Server server = new Server();

            ServerConnector internalConnector = new ServerConnector(server);
            internalConnector.setPort(m_httpPort);
            server.addConnector(internalConnector);

            ServerConnector publicConnector = new ServerConnector(server);
            publicConnector.setPort(m_publicHttpPort);
            server.addConnector(publicConnector);

            ServletContextHandler httpContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            httpContext.setContextPath("/");

            m_digestAuthenticator = new DigestAuthenticator();
            m_digestAuthenticator.setLoginService(m_userLoginService);
            m_digestAuthenticator.setConfiguration(m_userLoginService.getConfiguration());
            m_securityHandler.setDigestAuthenticator( m_digestAuthenticator );

            httpContext.insertHandler(m_securityHandler);
            httpContext.setServletHandler(m_servletHandler);

            server.setHandler(httpContext);

            server.start();
        } catch (Exception e) {
            LOG.error("Start failed", e);
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

    public void setSecurityHandler(SipXSecurityHandler securityHandler) {
        m_securityHandler = securityHandler;
    }
}

