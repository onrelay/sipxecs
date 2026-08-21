package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/** External TLS socket boundary for the future SIP-over-WebSocket accept loop. */
final class WebrtcWebSocketListener implements AutoCloseable {
    private final String m_bindAddress;
    private final int m_port;
    private final AtomicBoolean m_running = new AtomicBoolean();
    private SSLServerSocket m_serverSocket;
    private Thread m_acceptThread;

    WebrtcWebSocketListener(String bindAddress, int port) {
        m_bindAddress = bindAddress;
        m_port = port;
    }

    void start() throws IOException {
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        m_serverSocket = (SSLServerSocket) factory.createServerSocket();
        m_serverSocket.bind(new InetSocketAddress(m_bindAddress, m_port));
        m_running.set(true);
        m_acceptThread = new Thread(this::acceptConnections, "sipxwebrtc-wss");
        m_acceptThread.start();
    }

    private void acceptConnections() {
        while (m_running.get()) {
            try {
                m_serverSocket.accept().close();
            } catch (IOException exception) {
                if (m_running.get()) {
                    throw new IllegalStateException("WSS listener failed", exception);
                }
            }
        }
    }

    @Override
    public void close() {
        m_running.set(false);
        if (m_serverSocket != null) {
            try {
                m_serverSocket.close();
            } catch (IOException ignored) {
                // The listener is already stopping.
            }
            m_serverSocket = null;
        }
    }
}
