package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;

/** Receives lifecycle and text-message events from external WSS flows. */
public interface WssListener {
    /** Called after a WSS flow has been accepted. */
    void onConnected(String endpointId);

    /** Called for each complete WebSocket text message. */
    void onMessage(String endpointId, String message);

    /** Called when a WSS flow closes or fails. */
    void onDisconnected(String endpointId, IOException exception);
}
