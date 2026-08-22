package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import org.apache.log4j.Logger;

/** Jetty-backed WSS endpoint exposed through the gateway WSS abstraction. */
public final class JettyWssEndpointImpl implements WssEndpoint {
    private static final Logger log = Logger.getLogger(JettyWssEndpointImpl.class);
    private final String m_id;
    private final JettyWssProviderImpl m_provider;
    private Session m_session;

    JettyWssEndpointImpl(String id, JettyWssProviderImpl provider) {
        m_id = id;
        m_provider = provider;
    }

    @Override
    public String getId() {
        return m_id;
    }

    @Override
    public synchronized void send(String message) throws IOException {
        try {
            if (message == null) {
                throw new IllegalArgumentException("WSS message cannot be null");
            }
            if (m_session == null || !m_session.isOpen()) {
                throw new IOException("WSS endpoint is not open: " + m_id);
            }
            log.debug("Sending WSS message to " + m_id);
            m_session.getBasicRemote().sendText(message);
        } catch (IOException exception) {
            log.warn("Unable to send WSS message to " + m_id, exception);
            throw exception;
        } catch (Throwable exception) {
            log.error("Unexpected WSS send failure for " + m_id, exception);
            throw new IOException("Unable to send WSS message to " + m_id, exception);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (m_session != null && m_session.isOpen()) {
                log.info("Closing WSS endpoint " + m_id);
                m_session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Closed"));
            } else {
                log.debug("WSS endpoint already closed: " + m_id);
            }
        } catch (IOException exception) {
            log.warn("Unable to close WSS endpoint " + m_id, exception);
            throw exception;
        } catch (Throwable exception) {
            log.error("Unexpected WSS close failure for " + m_id, exception);
            throw new IOException("Unable to close WSS endpoint " + m_id, exception);
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            m_session = session;
            log.info("Opened WSS endpoint " + m_id);
            m_provider.endpointConnected(this);
        } catch (Throwable exception) {
            log.error("Unable to process WSS endpoint open for " + m_id, exception);
            throw new RuntimeException("Unable to process WSS endpoint open", exception);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            m_provider.endpointMessage(this, message);
        } catch (Throwable exception) {
            log.error("Unable to process WSS message for " + m_id, exception);
            throw new RuntimeException("Unable to process WSS message", exception);
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {
        try {
            log.info("Closed WSS endpoint " + m_id + ": " + reason);
            m_provider.endpointDisconnected(this, null);
        } catch (Throwable exception) {
            log.error("Unable to process WSS endpoint close for " + m_id, exception);
            throw new RuntimeException("Unable to process WSS endpoint close", exception);
        }
    }

    @OnError
    public void onError(Session session, Throwable exception) {
        try {
            log.info("WSS endpoint error: " + m_id, exception);
            m_provider.endpointDisconnected(this, new IOException("WSS endpoint failure", exception));
        } catch (Throwable callbackException) {
            log.error("Unable to process WSS endpoint error for " + m_id, callbackException);
            throw new RuntimeException("Unable to process WSS endpoint error", callbackException);
        }
    }
}