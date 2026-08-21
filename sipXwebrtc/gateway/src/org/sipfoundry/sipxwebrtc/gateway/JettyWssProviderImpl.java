package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.ServletContext;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/** Concrete WSS transport provider behind the gateway WSS abstraction. */
public final class JettyWssProviderImpl implements WssProvider {
    private static final Logger LOGGER = Logger.getLogger(JettyWssProviderImpl.class);

    private final String m_bindAddress;
    private final int m_port;
        private final Map<String, WssEndpoint> m_endpoints =
            new ConcurrentHashMap<String, WssEndpoint>();
    private final AtomicLong m_endpointSequence = new AtomicLong();
    private WssListener m_listener;
    private Server m_server;
    private boolean m_started;

    public JettyWssProviderImpl(String bindAddress, int port) {
        m_bindAddress = bindAddress;
        m_port = port;
    }

    @Override
    public void addWssListener(WssListener listener) {
        try {
            if (listener == null) {
                throw new IllegalArgumentException("WSS listener cannot be null");
            }
            m_listener = listener;
            LOGGER.debug("Registered WSS listener");
        } catch (Throwable exception) {
            LOGGER.error("Unable to register WSS listener", exception);
            throw new RuntimeException("Unable to register WSS listener", exception);
        }
    }

    @Override
    public void registerEndpoint(String id, WssEndpoint endpoint) {
        try {
            if (id == null || id.isEmpty() || endpoint == null) {
                throw new IllegalArgumentException("WSS endpoint identity and endpoint are required");
            }
            m_endpoints.put(id, endpoint);
            LOGGER.debug("Registered WSS endpoint " + id);
        } catch (Throwable exception) {
            LOGGER.error("Unable to register WSS endpoint " + id, exception);
            throw new RuntimeException("Unable to register WSS endpoint", exception);
        }
    }

    @Override
    public void send(String endpointId, String message) {
        try {
            WssEndpoint endpoint = m_endpoints.get(endpointId);
            if (endpoint == null) {
                LOGGER.warn("No WSS endpoint found for " + endpointId);
                return;
            }
            endpoint.send(message);
        } catch (Throwable exception) {
            LOGGER.error("Unable to send WSS message to " + endpointId, exception);
            throw new RuntimeException("Unable to send WSS message", exception);
        }
    }

    @Override
    public synchronized void start() {
        if (m_started) {
            LOGGER.debug("WSS provider is already started");
            return;
        }
        try {
            LOGGER.info("Starting WSS provider on " + m_bindAddress + ":" + m_port);
            m_server = new Server();
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(System.getProperty("javax.net.ssl.keyStore"));
            sslContextFactory.setKeyStorePassword(System.getProperty("javax.net.ssl.keyStorePassword"));
            sslContextFactory.setKeyStoreType(System.getProperty("javax.net.ssl.keyStoreType", "JKS"));

            ServerConnector connector = new ServerConnector(m_server, sslContextFactory);
            connector.setHost(m_bindAddress);
            connector.setPort(m_port);
            m_server.addConnector(connector);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");
            m_server.setHandler(context);
            configureWebSocket(context);
            m_server.start();
            m_started = true;
            LOGGER.info("Started WSS provider on " + m_bindAddress + ":" + m_port);
        } catch (Throwable exception) {
            close();
            LOGGER.error("Unable to start WSS provider on " + m_bindAddress + ":" + m_port, exception);
            throw new RuntimeException("Unable to start WSS provider", exception);
        }
    }

    @Override
    public boolean isStarted() {
        try {
            return m_started;
        } catch (Throwable exception) {
            LOGGER.error("Unable to read WSS provider state", exception);
            throw new RuntimeException("Unable to read WSS provider state", exception);
        }
    }

    @Override
    public synchronized void close() {
        try {
            LOGGER.info("Stopping WSS provider");
            m_started = false;
            if (m_server != null) {
                m_server.stop();
                m_server = null;
            }
            for (WssEndpoint endpoint : m_endpoints.values()) {
                try {
                    endpoint.close();
                } catch (Throwable exception) {
                    LOGGER.warn("Unable to close WSS endpoint " + endpoint.getId(), exception);
                }
            }
            m_endpoints.clear();
            LOGGER.info("Stopped WSS provider");
        } catch (Throwable exception) {
            LOGGER.warn("Unable to close WSS provider cleanly", exception);
            throw new RuntimeException("Unable to close WSS provider", exception);
        }
    }

    private void configureWebSocket(ServletContextHandler context) {
        try {
            JakartaWebSocketServletContainerInitializer.configure(context, this::configureEndpoints);
        } catch (Throwable exception) {
            LOGGER.error("Unable to configure Jetty WebSocket support", exception);
            throw new RuntimeException("Unable to configure Jetty WebSocket support", exception);
        }
    }

    private void configureEndpoints(ServletContext servletContext, ServerContainer container)
            throws DeploymentException {
        try {
            ServerEndpointConfig endpointConfig = ServerEndpointConfig.Builder
                .create(JettyWssEndpointImpl.class, "/")
                .subprotocols(java.util.Collections.singletonList("sip"))
                .configurator(new ServerEndpointConfig.Configurator() {
                    @Override
                    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                        String id = "wss-" + m_endpointSequence.incrementAndGet();
                        return endpointClass.cast(new JettyWssEndpointImpl(id, JettyWssProviderImpl.this));
                    }
                })
                    .build();
            container.addEndpoint(endpointConfig);
            LOGGER.debug("Configured SIP WebSocket endpoint at /");
        } catch (Throwable exception) {
            LOGGER.error("Unable to configure SIP WebSocket endpoint", exception);
            throw new RuntimeException("Unable to configure SIP WebSocket endpoint", exception);
        }
    }

    void endpointConnected(JettyWssEndpointImpl endpoint) {
        try {
            m_endpoints.put(endpoint.getId(), endpoint);
            LOGGER.info("WSS endpoint connected: " + endpoint.getId());
            notifyConnected(endpoint.getId());
        } catch (Throwable exception) {
            LOGGER.error("Unable to process WSS endpoint connection", exception);
            throw new RuntimeException("Unable to process WSS endpoint connection", exception);
        }
    }

    void endpointMessage(JettyWssEndpointImpl endpoint, String message) {
        try {
            LOGGER.debug("Received WSS message from " + endpoint.getId());
            notifyMessage(endpoint.getId(), message);
        } catch (Throwable exception) {
            LOGGER.error("Unable to process WSS message from " + endpoint.getId(), exception);
            throw new RuntimeException("Unable to process WSS message", exception);
        }
    }

    void endpointDisconnected(JettyWssEndpointImpl endpoint, IOException exception) {
        try {
            m_endpoints.entrySet().removeIf(entry -> entry.getValue() == endpoint);
            LOGGER.warn("WSS endpoint disconnected: " + endpoint.getId(), exception);
            notifyDisconnected(endpoint.getId(), exception);
        } catch (Throwable callbackException) {
            LOGGER.error("Unable to process WSS endpoint disconnect", callbackException);
            throw new RuntimeException("Unable to process WSS endpoint disconnect", callbackException);
        }
    }

    private void notifyMessage(String endpointId, String message) {
        try {
            if (m_listener == null) {
                throw new IllegalStateException("WSS listener is not registered");
            }
            m_listener.onMessage(endpointId, message);
        } catch (Throwable exception) {
            LOGGER.error("Unable to notify WSS listener of a message", exception);
            throw new RuntimeException("Unable to notify WSS listener", exception);
        }
    }

    private void notifyDisconnected(String endpointId, IOException exception) {
        try {
            if (m_listener != null) {
                m_listener.onDisconnected(endpointId, exception);
            }
        } catch (Throwable callbackException) {
            LOGGER.error("Unable to notify WSS listener of disconnect", callbackException);
            throw new RuntimeException("Unable to notify WSS listener", callbackException);
        }
    }
 
    private void notifyConnected(String endpointId) {
        try {
            if (m_listener == null) {
                throw new IllegalStateException("WSS listener is not registered");
            }
            m_listener.onConnected(endpointId);
        } catch (Throwable exception) {
            LOGGER.error("Unable to notify WSS listener of connection", exception);
            throw new RuntimeException("Unable to notify WSS listener", exception);
        }
    }
}