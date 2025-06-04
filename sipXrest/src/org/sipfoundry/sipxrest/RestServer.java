package org.sipfoundry.sipxrest;

import java.io.File;
import java.util.Timer;

import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.restlet.ext.servlet.ServerServlet;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import org.sipfoundry.commons.log4j.SipFoundryAppender;
import org.sipfoundry.commons.log4j.SipFoundryLayout;
import org.sipfoundry.commons.restconfig.RestServerConfig;
import org.sipfoundry.commons.restconfig.RestServerConfigFileParser;
import org.sipfoundry.commons.util.DomainConfiguration;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;

public class RestServer {

    private static Logger logger = Logger.getLogger(RestServer.class);

    static final String PACKAGE = "org.sipfoundry.sipxrest";

    private static String configFileName = "/etc/sipxpbx/sipxrest-config.xml";

    private static Appender appender;

    private static RestServerConfig restServerConfig;

    private static Server webServer;

    public static final Timer timer = new Timer();

    private static RestServiceFinder restServiceFinder;

    private static AccountManagerImpl accountManager;

    private static SipStackBean sipStackBean;

    public static RestServerConfig getRestServerConfig() {
        return restServerConfig;
    }

    public static void setAppender(Appender appender) {
        RestServer.appender = appender;
    }

    public static Appender getAppender() {
        return appender;
    }

    private static void initWebServer() throws Exception {
        webServer = new Server();

        // Create connectors for public and internal ports
        ServerConnector publicConnector = new ServerConnector(webServer);
        publicConnector.setPort(restServerConfig.getPublicHttpPort());
        webServer.addConnector(publicConnector);

        ServerConnector internalConnector = new ServerConnector(webServer);
        internalConnector.setPort(restServerConfig.getHttpPort());
        webServer.addConnector(internalConnector);

        // Set up the servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setInitParameter("org.restlet.application", RestServerApplication.class.getName());

        // Add the Restlet servlet
        ServletHolder servletHolder = new ServletHolder(ServerServlet.class);
        servletHolder.setInitParameter("org.restlet.application", RestServerApplication.class.getName());
        context.addServlet(servletHolder, "/*");

        webServer.setHandler(context);

        // Start the server
        webServer.start();
    }

    public static MessageFactory getMessageFactory() {
        return sipStackBean.getMessageFactory();
    }

    public static AddressFactory getAddressFactory() {
        return sipStackBean.getAddressFactory();
    }

    public static HeaderFactory getHeaderFactory() {
        return sipStackBean.getHeaderFactory();
    }

    public static RestServiceFinder getServiceFinder() {
        return restServiceFinder;
    }

    public static AccountManagerImpl getAccountManager() {
        return accountManager;
    }

    public static void main(String[] args) throws Exception {
        String configDir = System.getProperties().getProperty("conf.dir", "/etc/sipxpbx");
        configFileName = configDir + "/sipxrest-config.xml";

        if (!new File(configFileName).exists()) {
            System.err.println("Cannot find the config file");
            System.exit(-1);
        }

        PropertyConfigurator.configureAndWatch(configDir + "/sipxrest/log4j.properties",
                SipFoundryLayout.LOG4J_MONITOR_FILE_DELAY);

        restServerConfig = new RestServerConfigFileParser().parse("file://" + configFileName);
        setAppender(new SipFoundryAppender(new SipFoundryLayout(),
                RestServer.getRestServerConfig().getLogDirectory() + "/sipxrest.log"));

        accountManager = new AccountManagerImpl();
        sipStackBean = new SipStackBean();

        restServiceFinder = new RestServiceFinder();
        restServiceFinder.search(System.getProperty("plugin.dir"));

        try {
            UnfortunateLackOfSpringSupportFactory.initialize();
        } catch (Exception e) {
            logger.error(e);
        }

        initWebServer();

        logger.debug("Web server started.");
    }

    public static SipStackBean getSipStack() {
        return sipStackBean;
    }

    public static String getRealm() {
        DomainConfiguration config = new DomainConfiguration(System.getProperty("conf.dir") + "/domain-config");
        return config.getSipRealm();
    }
}