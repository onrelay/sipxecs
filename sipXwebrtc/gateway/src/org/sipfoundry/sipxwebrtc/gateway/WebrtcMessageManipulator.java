package org.sipfoundry.sipxwebrtc.gateway;

import javax.sip.message.Message;

/** One directional transformation in the WSS/JAIN SIP message pipeline. */
public interface WebrtcMessageManipulator {
    Message fromWss(Message message);

    Message toWss(Message message);
}