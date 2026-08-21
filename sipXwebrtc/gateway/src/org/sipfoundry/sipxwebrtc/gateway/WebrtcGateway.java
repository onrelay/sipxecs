package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;

import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;

/**
 * Runtime bootstrap for the sipXwebrtc SIP transport gateway.
 *
 * <p>This class owns the JAIN SIP stack and the internal proxy-facing SIP
 * transports. WSS is an external WebRTC listener and is intentionally kept
 * separate because WebSocket framing is not a standard JAIN SIP listening
 * point transport.</p>
 */
public final class WebrtcGateway implements AutoCloseable {
    private static final String DEFAULT_CONFIG = "/etc/sipxpbx/sipxwebrtc.properties";
    private static final Logger LOGGER = Logger.getLogger(WebrtcGateway.class.getName());

    private final Properties m_properties;
    private SipStack m_sipStack;
    private SipProvider m_internalSipProvider;
    private WssProvider m_wssProvider;
    private WebrtcUserAgent m_userAgent;

    public WebrtcGateway(Properties properties) {
        m_properties = new Properties();
        m_properties.putAll(properties);
    }

    public static void main(String[] args) throws Exception {
        String configPath = args.length == 0 ? DEFAULT_CONFIG : args[0];
        WebrtcGateway gateway = new WebrtcGateway(loadProperties(Paths.get(configPath)));
        gateway.start();
        Runtime.getRuntime().addShutdownHook(new Thread(gateway::close, "sipxwebrtc-shutdown"));
    }

    public void start() {
        try {
            startInternal();
        } catch (Throwable exception) {
            LOGGER.error("Unable to start sipXwebrtc gateway", exception);
            close();
            throw new RuntimeException("Unable to start sipXwebrtc gateway", exception);
        }
    }

    private void startInternal() throws SipException {
        configureTls();

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName(m_properties.getProperty("sip.stack-path", "gov.nist"));

        Properties stackProperties = new Properties();
        stackProperties.setProperty("javax.sip.STACK_NAME",
                m_properties.getProperty("sip.stack-name", "org.sipfoundry.sipxwebrtc"));
        stackProperties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "true");
        stackProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "true");

        m_sipStack = sipFactory.createSipStack(stackProperties);
        String bindAddress = m_properties.getProperty("internal.sip.bind-address", "0.0.0.0");
        int sipPort = getPort("internal.sip.port", 5060);
        ListeningPoint udp = m_sipStack.createListeningPoint(bindAddress, sipPort, ListeningPoint.UDP);
        ListeningPoint tcp = m_sipStack.createListeningPoint(bindAddress, sipPort, ListeningPoint.TCP);
        m_internalSipProvider = m_sipStack.createSipProvider(udp);
        m_internalSipProvider.addListeningPoint(tcp);
        m_sipStack.start();

        m_wssProvider = new JettyWssProviderImpl(
            m_properties.getProperty("wss.bind-address", "0.0.0.0"),
            getPort("wss.port", 7443));
        m_userAgent = new WebrtcUserAgent(m_internalSipProvider, m_wssProvider,
            m_properties.getProperty("internal.sip.transport", ListeningPoint.UDP));
        m_wssProvider.start();
    }

    public SipProvider getInternalSipProvider() {
        return m_internalSipProvider;
    }

    @Override
    public void close() {
        try {
            if (m_wssProvider != null) {
                m_wssProvider.close();
            }
            if (m_sipStack != null) {
                m_sipStack.stop();
            }
        } catch (Throwable exception) {
            LOGGER.warn("Unable to close sipXwebrtc gateway cleanly", exception);
        }
        m_wssProvider = null;
        m_sipStack = null;
        m_internalSipProvider = null;
        m_userAgent = null;
    }

    private void configureTls() {
        setSystemProperty("javax.net.ssl.keyStore", "tls.key-store");
        setSystemProperty("javax.net.ssl.keyStorePassword", "tls.key-store-password");
        setSystemProperty("javax.net.ssl.trustStore", "tls.trust-store");
        setSystemProperty("javax.net.ssl.trustStorePassword", "tls.trust-store-password");
        setSystemProperty("javax.net.ssl.keyStoreType", "tls.key-store-type");
    }

    private void setSystemProperty(String systemKey, String configKey) {
        String value = m_properties.getProperty(configKey);
        if (value != null && !value.isEmpty()) {
            System.setProperty(systemKey, value);
        }
    }

    private int getPort(String key, int defaultValue) {
        return Integer.parseInt(m_properties.getProperty(key, Integer.toString(defaultValue)));
    }

    private static Properties loadProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

}