package org.sipfoundry.sipxconfig.webrtc;

public interface Webrtc {
    WebrtcSettings getSettings();

    void saveSettings(WebrtcSettings settings);
}