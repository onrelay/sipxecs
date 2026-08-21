package org.sipfoundry.sipxwebrtc.gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.ViaHeader;
import javax.sip.header.ContactHeader;
import javax.sip.address.SipURI;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/**
 * Translates and manipulates messages between WSS and the internal SIP provider.
 */
public final class WebrtcUserAgent implements WssListener, SipListener {
    private static final Logger LOGGER = Logger.getLogger(WebrtcUserAgent.class);
    private final SipProvider m_internalSipProvider;
    private final WssProvider m_wssProvider;
    private final WssSipMessageConverter m_messageConverter;
        private final List<WebrtcMessageManipulator> m_messageManipulators =
            new ArrayList<WebrtcMessageManipulator>();

        public WebrtcUserAgent(SipProvider internalSipProvider, WssProvider wssProvider,
            String internalTransport) {
        m_internalSipProvider = internalSipProvider;
        m_wssProvider = wssProvider;
        m_messageConverter = new WssSipMessageConverter();
        m_messageManipulators.add(new WebrtcContactManipulator());
        m_messageManipulators.add(new WebrtcViaManipulator(internalSipProvider, internalTransport));
        try {
            m_internalSipProvider.addSipListener(this);
            m_wssProvider.addWssListener(this);
        } catch (Throwable exception) {
            LOGGER.error("Unable to register WebRTC user agent listeners", exception);
            throw new RuntimeException("Unable to register WebRTC user agent listeners", exception);
        }
    }

    public SipProvider getInternalSipProvider() {
        return m_internalSipProvider;
    }

    public WssProvider getWssProvider() {
        return m_wssProvider;
    }

    public WssSipMessageConverter getMessageConverter() {
        return m_messageConverter;
    }

    public List<WebrtcMessageManipulator> getMessageManipulators() {
        return Collections.unmodifiableList(m_messageManipulators);
    }

    @Override
    public void onConnected(String endpointId) {
    }

    @Override
    public void onMessage(String endpointId, String message) {
        try {
            Message sipMessage = m_messageConverter.fromWss(message);
            for (WebrtcMessageManipulator manipulator : m_messageManipulators) {
                sipMessage = manipulator.fromWss(sipMessage);
                if (sipMessage == null) {
                    return;
                }
            }
            if (!(sipMessage instanceof Request)) {
                LOGGER.debug("Ignoring non-request WSS message from " + endpointId);
                return;
            }
            sendToInternal(sipMessage);
        } catch (Throwable exception) {
            LOGGER.error("Unable to process WSS message from " + endpointId, exception);
            throw new RuntimeException("Unable to process WSS message", exception);
        }
    }

    @Override
    public void onDisconnected(String endpointId, IOException exception) {
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        processInternalMessage(requestEvent.getRequest());
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        processInternalMessage(responseEvent.getResponse());
    }

    private void processInternalMessage(Message message) {
        try {
            sendToWss(message);
        } catch (Throwable exception) {
            LOGGER.error("Unable to process internal SIP message", exception);
            throw new RuntimeException("Unable to process internal SIP message", exception);
        }
    }

    public void sendToInternal(Message message) {
        try {
            if (message instanceof Request) {
                m_internalSipProvider.sendRequest((Request) message);
            } else if (message instanceof Response) {
                m_internalSipProvider.sendResponse((Response) message);
            } else {
                throw new IllegalArgumentException("Unsupported SIP message type");
            }
        } catch (Throwable exception) {
            LOGGER.error("Unable to send WebRTC message to internal SIP", exception);
            throw new RuntimeException("Unable to send WebRTC message to internal SIP", exception);
        }
    }

    public void sendToWss(String endpointId, Message message) {
        try {
            Message transformed = reverseManipulate(message);
            if (transformed != null) {
                m_wssProvider.send(endpointId, m_messageConverter.toWss(transformed));
            }
        } catch (Throwable exception) {
            LOGGER.error("Unable to send internal SIP message to WSS", exception);
            throw new RuntimeException("Unable to send internal SIP message to WSS", exception);
        }
    }

    public void sendToWss(Message message) {
        try {
            Message transformed = reverseManipulate(message);
            if (transformed == null) {
                return;
            }
            String endpointId = null;
            ListIterator vias = transformed.getHeaders(ViaHeader.NAME);
            while (vias.hasNext()) {
                ViaHeader via = (ViaHeader) vias.next();
                if ("WSS".equalsIgnoreCase(via.getTransport())) {
                    endpointId = via.getHost();
                    break;
                }
            }
            if (endpointId == null) {
                ContactHeader contact = (ContactHeader) transformed.getHeader(ContactHeader.NAME);
                if (contact != null && !contact.isWildCard()
                        && contact.getAddress().getURI() instanceof SipURI) {
                    SipURI contactUri = (SipURI) contact.getAddress().getURI();
                    if ("ws".equalsIgnoreCase(contactUri.getTransportParam())) {
                        endpointId = contactUri.getHost();
                    }
                }
            }
            if (endpointId == null || endpointId.isEmpty()) {
                throw new IllegalArgumentException("Internal SIP message has no WSS Via or Contact");
            }
            m_wssProvider.send(endpointId, m_messageConverter.toWss(transformed));
        } catch (Throwable exception) {
            LOGGER.error("Unable to route internal SIP message to WSS", exception);
            throw new RuntimeException("Unable to route internal SIP message to WSS", exception);
        }
    }

    private Message reverseManipulate(Message message) {
        if (message == null) {
            return null;
        }
        List<WebrtcMessageManipulator> reverseManipulators =
                new ArrayList<WebrtcMessageManipulator>(m_messageManipulators);
        Collections.reverse(reverseManipulators);
        for (WebrtcMessageManipulator manipulator : reverseManipulators) {
            message = manipulator.toWss(message);
            if (message == null) {
                return null;
            }
        }
        return message;
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent event) {
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent event) {
    }
}