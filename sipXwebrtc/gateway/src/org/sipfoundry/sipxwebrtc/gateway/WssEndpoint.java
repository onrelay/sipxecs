package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;

/** A single external WebSocket flow carrying SIP text messages. */
public interface WssEndpoint {
    /** Stable identifier for logging and flow correlation. */
    String getId();

    /** Send one SIP message as a WebSocket text message. */
    void send(String message) throws IOException;

    /** Close the external WebSocket flow. */
    void close() throws IOException;
}
