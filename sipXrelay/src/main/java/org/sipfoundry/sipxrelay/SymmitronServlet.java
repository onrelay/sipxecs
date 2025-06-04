/*
 * 
 * 
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 *
 */

package org.sipfoundry.sipxrelay;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;

public class SymmitronServlet extends HttpServlet {
    private XmlRpcServletServer server;


    /*
     * (non-Javadoc)
     * 
     * @see jakarta.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
    	 try {
            
             PropertyHandlerMapping handlerMapping = new PropertyHandlerMapping();

             handlerMapping.addHandler("sipXrelay", SymmitronServer.class);

             server = new XmlRpcServletServer();
            

             XmlRpcServerConfigImpl serverConfig = new XmlRpcServerConfigImpl();
             serverConfig.setKeepAliveEnabled(true);
             serverConfig.setEnabledForExceptions(true);
             serverConfig.setEnabledForExtensions(true);
             server.setMaxThreads(32);

             server.setConfig(serverConfig);
             server.setHandlerMapping(handlerMapping);
         } catch (Exception ex) {
             throw new ServletException("Initialization failure", ex);
         }
    }

    /*
     * (non-Javadoc)
     * 
     * @see jakarta.servlet.http.HttpServlet#doPost(jakarta.servlet.http.HttpServletRequest,
     *      jakarta.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.server.execute(request, response);

    }

}
