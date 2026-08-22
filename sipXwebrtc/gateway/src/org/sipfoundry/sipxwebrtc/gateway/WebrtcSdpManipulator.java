package org.sipfoundry.sipxwebrtc.gateway;

import org.apache.log4j.Logger;

import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sip.message.Message;

/** Applies SDP-specific transformations in both pipeline directions. */
public final class WebrtcSdpManipulator implements WebrtcMessageManipulator {
    private static final Logger log = Logger.getLogger(WebrtcSdpManipulator.class);

    @Override
    public Message fromWss(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            SessionDescription sessionDescription = getSessionDescription(message);
            if (sessionDescription == null) {
                log.debug("Skipping fromWss SDP manipulation because SDP is missing or invalid");
                return message;
            }

            // Skeleton only: actual sipXrelay SDP rewrite policy is pending.
            return message;
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to rewrite WebRTC SDP from WSS", exception);
        }
    }

    @Override
    public Message toWss(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            SessionDescription sessionDescription = getSessionDescription(message);
            if (sessionDescription == null) {
                log.debug("Skipping toWss SDP manipulation because SDP is missing or invalid");
                return message;
            }

            // Skeleton only: actual sipXrelay SDP rewrite policy is pending.
            return message;
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to rewrite WebRTC SDP to WSS", exception);
        }
    }

    private SessionDescription getSessionDescription(Message message) {
        try {
            byte[] rawContent = message.getRawContent();
            if (rawContent == null || rawContent.length == 0) {
                log.debug("SIP message has no raw body content");
                return null;
            }
            String sdpText = new String(rawContent, "UTF-8");
            if (sdpText.trim().isEmpty()) {
                log.debug("SIP message body content is empty");
                return null;
            }
            return SdpFactory.getInstance().createSessionDescription(sdpText);
        } catch (Throwable exception) {
            log.warn("Unable to parse SDP from SIP message body", exception);
            return null;
        }
    }
}