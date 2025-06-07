package org.sipfoundry.conference;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.sipfoundry.sipxrecording.RecordingConfiguration;

public class WebServer {
    private static int PORT = RecordingConfiguration.get().getJettyPort();
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxrecording");
    private static WebServer instance;
    private Server server;

    private WebServer() {
        try {
            server = new Server();

            // Create connector
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(PORT);
            server.addConnector(connector);

            // Create context handler (replaces HttpContext)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");

            // Add servlets
            addServlet(context, "conference", "/conference/*", ConferenceServlet.class);
            addServlet(context, "recordconference", "/recordconference/*", RecordConferenceServlet.class);

            // Attach context to server
            server.setHandler(context);

            LOG.info(String.format("Starting Jetty server on port: %d", PORT));
        } catch (Exception e) {
            server = null;
            LOG.error(String.format("Cannot instantiate Jetty server on port *:%d", PORT), e);
        }
    }

    public static synchronized WebServer getInstance() {
        if (instance == null) {
            instance = new WebServer();
        }
        return instance;
    }

    /**
     * Add a servlet for the Web server to use
     * @param name
     * @param pathSpec
     * @param servletClass must be of type jakarta.servlet.Servlet
     */
    private void addServlet(ServletContextHandler context, String name, String pathSpec, Class<? extends jakarta.servlet.Servlet> servletClass) {
        ServletHolder holder = new ServletHolder(servletClass);
        holder.setName(name);
        context.addServlet(holder, pathSpec);
        LOG.info(String.format("Adding Servlet %s on %s", name, pathSpec));
    }

    public boolean startServer() {
        if (server != null && !server.isStarted()) {
            try {
                server.start();
                return true;
            } catch (Exception ex) {
                LOG.error("Error starting server ", ex);
                return false;
            }
        } else if (server != null && server.isStarted()) {
            return true;
        } else {
            LOG.error("Error creating server");
            return false;
        }
    }
}