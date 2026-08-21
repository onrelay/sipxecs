package org.sipfoundry.sipxwebrtc.gateway;

import javax.sip.header.ViaHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Message;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import java.util.ListIterator;

/** Handles the WebRTC transport identity carried by the SIP Via header. */
public final class WebrtcViaManipulator implements WebrtcMessageManipulator {
    private final SipProvider m_internalSipProvider;
    private final String m_internalTransport;
    private final HeaderFactory m_headerFactory;

    public WebrtcViaManipulator(SipProvider internalSipProvider, String internalTransport) {
        if (internalSipProvider == null || internalTransport == null || internalTransport.isEmpty()) {
            throw new IllegalArgumentException("Internal SIP provider and transport are required");
        }
        try {
            m_internalSipProvider = internalSipProvider;
            m_internalTransport = internalTransport;
            m_headerFactory = SipFactory.getInstance().createHeaderFactory();
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to initialize WebRTC Via manipulator", exception);
        }
    }

    @Override
    public Message fromWss(Message message) {
        try {
            validateWssVia(message);
            if (message instanceof Request) {
                ViaHeader internalVia = createInternalVia();
                message.addFirst(internalVia);
            }
            return message;
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to add internal WebRTC Via", exception);
        }
    }

    @Override
    public Message toWss(Message message) {
        return removeInternalVia(message);
    }

    private Message removeInternalVia(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
            if (via == null) {
                throw new IllegalArgumentException("SIP message has no Via header");
            }
            if (!"WSS".equalsIgnoreCase(via.getTransport()) && hasWssVia(message)) {
                message.removeFirst(ViaHeader.NAME);
            }
            return message;
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to remove internal WebRTC Via", exception);
        }
    }

    private boolean hasWssVia(Message message) {
        ListIterator vias = message.getHeaders(ViaHeader.NAME);
        while (vias.hasNext()) {
            ViaHeader via = (ViaHeader) vias.next();
            if ("WSS".equalsIgnoreCase(via.getTransport())) {
                return true;
            }
        }
        return false;
    }

    private ViaHeader createInternalVia() {
        try {
            javax.sip.ListeningPoint listeningPoint =
                    m_internalSipProvider.getListeningPoint(m_internalTransport);
            if (listeningPoint == null) {
                throw new IllegalArgumentException("No internal SIP listening point for " + m_internalTransport);
            }
            return m_headerFactory.createViaHeader(
                    listeningPoint.getIPAddress(), listeningPoint.getPort(),
                    listeningPoint.getTransport(), null);
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to create internal WebRTC Via", exception);
        }
    }

    private Message validateWssVia(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("SIP message cannot be null");
        }
        try {
            ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
            if (via == null) {
                throw new IllegalArgumentException("SIP message has no Via header");
            }
            if (!"WSS".equalsIgnoreCase(via.getTransport())) {
                throw new IllegalArgumentException("SIP message Via is not WSS: " + via.getTransport());
            }
            return message;
        } catch (Throwable exception) {
            throw new RuntimeException("Unable to manipulate WebRTC Via", exception);
        }
    }
}