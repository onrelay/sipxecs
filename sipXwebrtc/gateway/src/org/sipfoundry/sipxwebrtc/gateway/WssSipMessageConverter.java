package org.sipfoundry.sipxwebrtc.gateway;

import java.nio.charset.StandardCharsets;

import javax.sip.message.Message;

import gov.nist.javax.sip.parser.StringMsgParser;

/** Converts SIP text carried by WSS to and from JAIN SIP messages. */
public final class WssSipMessageConverter {
    private final StringMsgParser m_messageParser = new StringMsgParser();

    public Message fromWss(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("WSS SIP message cannot be empty");
        }
        try {
            return m_messageParser.parseSIPMessage(
                    message.getBytes(StandardCharsets.UTF_8), true, true, null);
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to parse WSS SIP message", exception);
        }
    }

    public String toWss(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            return new String(message.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to serialize SIP message for WSS", exception);
        }
    }
}
