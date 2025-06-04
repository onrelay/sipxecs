/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxbridge;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jakarta.servlet.ServletException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.sipfoundry.sipxbridge.xmlrpc.SipXbridgeXmlRpcServer;

public class SipXbridgeXmlRpcServerImpl implements SipXbridgeXmlRpcServer {

	private static Logger logger = Logger
			.getLogger(SipXbridgeXmlRpcServerImpl.class);
	/*
	 * THe Webserver for the xml rpc interface.
	 */
	private static Server webServer;

	private static boolean isWebServerRunning;

	public static void startXmlRpcServer() throws SipXbridgeException {
		try {
			if (!isWebServerRunning) {
				Logger.getLogger("org.eclipse.jetty").setLevel(Level.OFF);
				Logger.getLogger("org.apache.xmlrpc").setLevel(Level.OFF);
				isWebServerRunning = true;

				int port = Gateway.getBridgeConfiguration().getXmlRpcPort();
				String host = Gateway.getBridgeConfiguration().getLocalAddress();

				if (logger.isDebugEnabled()) {
					logger.debug("Starting XML-RPC server on " + host + ":" + port);
				}

				webServer = new Server();

				ServerConnector connector = new ServerConnector(webServer);
				connector.setHost(host);
				connector.setPort(port);
				webServer.addConnector(connector);

				ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
				context.setContextPath("/");
				context.addServlet(new ServletHolder(new SipxbridgeServlet()), "/*");

				webServer.setHandler(context);
				webServer.start();

				if (logger.isDebugEnabled()) {
					logger.debug("Web server started.");
				}
			}
		} catch (Exception ex) {
			throw new SipXbridgeException("Exception starting web server", ex);
		}
	}

	public static void stopXmlRpcServer() {
		try {
			if (webServer != null && webServer.isRunning()) {
				webServer.stop();
				webServer = null;
			}
			isWebServerRunning = false;
		} catch (Exception ex) {
			logger.error("Error stopping XML-RPC server.", ex);
		}
	}

	private String formatStackTrace(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	public Map<String, String> getRegistrationStatus() throws ServletException {
	    HashMap<String, String> retval = new HashMap<String, String>();
		try {
			@SuppressWarnings("unused")
			int counter = 1;
			for (ItspAccountInfo itspAccount : Gateway.getAccountManager()
					.getItspAccounts()) {
				if (itspAccount.isRegisterOnInitialization()) {
				    retval.put(itspAccount.getRegistrationRecord().getRegisteredAddress() + " [" + itspAccount.getUserName()  + "]" ,
				            itspAccount.getRegistrationRecord().getRegistrationStatus());
				}
			}

		} catch (Throwable ex) {
		    throw new ServletException(formatStackTrace(ex), ex);
		}

		if ( logger.isDebugEnabled() ) logger.debug("getRegistrationStatus: " + retval);
		return retval;
	}

	public Integer getCallCount() throws ServletException {
	    int retval = 0;
		try {
			retval = Gateway.getBackToBackUserAgentFactory().getBackToBackUserAgentCount();

		} catch (Exception ex) {
		    throw new ServletException(formatStackTrace(ex), ex);
		}
		return Integer.valueOf(retval);
	}

	public Boolean start() throws ServletException {

		if ( logger.isDebugEnabled() ) logger.debug("Gateway.start()");
		try {
			Gateway.start();
		} catch (Exception ex) {
		    throw new ServletException(formatStackTrace(ex), ex);
		}

		return true;
	}

	public Boolean stop() throws ServletException {

		if ( logger.isDebugEnabled() ) logger.debug("Gateway.stop()");

		try {
			Gateway.stop();
		} catch (Exception ex) {
		    throw new ServletException(formatStackTrace(ex), ex);
		}
		return true;
	}

	public Boolean exit() throws ServletException {
		if ( logger.isDebugEnabled() ) logger.debug("Gateway.exit()");

		try {

			if (Gateway.getState() == GatewayState.INITIALIZED) {
				Gateway.stop();
			}
			/*
			 * Need a fresh timer here because the gateway timer is canceled.
			 */

			new Timer().schedule(new TimerTask() {
				public void run() {
					if ( logger.isDebugEnabled() ) logger.debug("Exiting bridge!");
					System.exit(0);
				}
			}, 1000);

		} catch (Exception ex) {
		    throw new ServletException(formatStackTrace(ex), ex);
		}
		return true;
	}

}
