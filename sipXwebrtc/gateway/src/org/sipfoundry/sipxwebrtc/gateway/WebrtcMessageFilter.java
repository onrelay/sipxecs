package org.sipfoundry.sipxwebrtc.gateway;

import javax.sip.message.Message;

/** Decides whether a SIP message may cross the WebRTC boundary. */
public interface WebrtcMessageFilter {
    boolean allows(Message message);
}