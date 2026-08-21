package org.sipfoundry.sipxwebrtc.gateway;

/** Correlates an internal JAIN SIP transaction with its external WSS endpoint. */
final class WebrtcTransactionContext {
    private final WssEndpoint m_wssEndpoint;

    WebrtcTransactionContext(WssEndpoint wssEndpoint) {
        m_wssEndpoint = wssEndpoint;
    }

    WssEndpoint getWssEndpoint() {
        return m_wssEndpoint;
    }
}