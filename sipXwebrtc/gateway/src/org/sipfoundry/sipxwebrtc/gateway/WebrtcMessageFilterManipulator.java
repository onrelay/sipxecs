package org.sipfoundry.sipxwebrtc.gateway;

import javax.sip.message.Message;

/** Message manipulator that allows or blocks a message in either direction. */
public final class WebrtcMessageFilterManipulator implements WebrtcMessageManipulator {
    private final WebrtcMessageFilter m_filter;

    public WebrtcMessageFilterManipulator(WebrtcMessageFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("WebRTC message filter cannot be null");
        }
        m_filter = filter;
    }

    @Override
    public Message fromWss(Message message) {
        return filter(message);
    }

    @Override
    public Message toWss(Message message) {
        return filter(message);
    }

    private Message filter(Message message) {
        if (message == null) {
            return null;
        }
        return m_filter.allows(message) ? message : null;
    }
}