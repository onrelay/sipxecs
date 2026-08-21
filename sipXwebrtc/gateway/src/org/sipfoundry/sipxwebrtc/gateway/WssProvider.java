package org.sipfoundry.sipxwebrtc.gateway;

/** Owns the external WSS transport and reports flows to its listener. */
public interface WssProvider extends AutoCloseable {
    void addWssListener(WssListener listener);

    void registerEndpoint(String id, WssEndpoint endpoint);

    void send(String endpointId, String message);

    void start();

    boolean isStarted();

    @Override
    void close();
}