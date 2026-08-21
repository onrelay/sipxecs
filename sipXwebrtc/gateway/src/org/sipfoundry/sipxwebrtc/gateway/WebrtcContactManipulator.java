package org.sipfoundry.sipxwebrtc.gateway;

import java.text.ParseException;

import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.message.Message;

/** Converts Contact transport parameters at the WebRTC/JAIN SIP boundary. */
public final class WebrtcContactManipulator implements WebrtcMessageManipulator {
    @Override
    public Message fromWss(Message message) {
        return setContactTransport(message, "ws");
    }

    @Override
    public Message toWss(Message message) {
        return setContactTransport(message, "ws");
    }

    private Message setContactTransport(Message message, String transport) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            ContactHeader contact = (ContactHeader) message.getHeader(ContactHeader.NAME);
            if (contact != null && !contact.isWildCard()
                    && contact.getAddress().getURI() instanceof SipURI) {
                ((SipURI) contact.getAddress().getURI()).setTransportParam(transport);
            }
            return message;
        } catch (ParseException exception) {
            throw new RuntimeException("Unable to set WebRTC Contact transport", exception);
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to manipulate WebRTC Contact", exception);
        }
    }
}