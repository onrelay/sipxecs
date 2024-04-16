/*
 *  Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 *  Contributors retain copyright to elements licensed under a Contributor Agreement.
 *  Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxbridge;

import java.util.Random;

import javax.sdp.Origin;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.apache.log4j.Logger;
import org.sipfoundry.sipxrelay.SymEndpointInterface;

class RtpReceiverEndpoint implements SymEndpointInterface {

    private Origin origin;

    private SessionDescription sessionDescription;

    private static Logger logger = Logger.getLogger(RtpReceiverEndpoint.class);


    private long sessionId;

    private int sessionVersion;

    //private SymEndpointInterface symReceiverEndpoint;

    private String ipAddress;
    
    private int port;

    private String globalAddress;

    private boolean useGlobalAddressing;

    RtpReceiverEndpoint(SymEndpointInterface symReceiverEndpoint) {
        //this.symReceiverEndpoint = symReceiverEndpoint;
        this.ipAddress = symReceiverEndpoint.getIpAddress();
        this.port = symReceiverEndpoint.getPort();
        try {
            this.sessionId = Math.abs(new Random().nextLong());
            this.sessionVersion = 1;

            String address = this.getIpAddress();
            origin = SdpFactory.getInstance().createOrigin("sipxbridge", sessionId,
                    sessionVersion, "IN", "IP4", address);
        } catch (Exception ex) {
            throw new SipXbridgeException("Unexpected exception creating origin ", ex);
        }
    }
    
    public SessionDescription getLocalSessionDescription() {
        SipUtilities.fixupSdpMediaAddresses(this.sessionDescription, 
                this.ipAddress, this.getPort());
        return this.sessionDescription;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public int getPort() {
        //return this.symReceiverEndpoint.getPort();
    	return this.port;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    SessionDescription getSessionDescription() {
        return this.sessionDescription;
    }
    
    /**
     * Set the session description for this receiver. 
     * 
     * @param sessionDescription -- the session description to set. Warning - this method will update
     * the sessionDescriptor and set its addresses to the address assigned to the endpoint.
     * 
     */

    void setSessionDescription(SessionDescription sessionDescription) {
        if (logger.isDebugEnabled()) {
            logger.debug("RtpReceiverEndpoint.setSessionDescription() Old SD  = " + this.sessionDescription);
            logger.debug("RtpReceiverEndpoint.setSessionDescription() newSD = " + sessionDescription);
            logger.debug("setSessionDescription at : " + SipUtilities.getStackTrace());
        }
        
        boolean updatePort = true;
        if( sessionDescription != null &&
        	this.sessionDescription != null &&
        	sessionDescription.toString().equals( this.sessionDescription.toString() ) ) {
        	// IPl: No change so we return. This check protects from something getting messed up below, which happens with
        	// re-invites since this.getPort() is not reliable due to incorrect shared references rather than clones.
            if ( logger.isDebugEnabled()) {
                logger.debug("RtpReceiverEndpoint.setSessionDescription() no change to SD: Do not update port" );                
            }
            updatePort = false;
        }
 
        try {
             	
            String address = useGlobalAddressing ? getGlobalAddress() : getIpAddress();
            int port = getPort();
            logger.debug("RtpReceiverEndpoint.setSessionDescription() address = " + address);
            logger.debug("RtpReceiverEndpoint.setSessionDescription() port = " + port);

            /*
             * Filter the codecs to the allow set. This filter is applied only for
             * audio codecs.
             */
            sessionDescription = SipUtilities.cleanSessionDescription( sessionDescription );
            
            /*
             * draft-ietf-sipping-sip-offeranswer-08 section 5.2.5 makes it clear that a UA cannot
             * change the session id field of the o-line when making a subsequent offer/answer.
             * We use the same Origin field for all interactions.
             */
            sessionDescription.setOrigin(origin);
            if( updatePort ) {           	
            	SipUtilities.fixupSdpMediaAddresses(sessionDescription, address, port );
            }
            else {
            	SipUtilities.fixupSdpMediaAddresses(sessionDescription, address );            	
            }

            if ( logger.isDebugEnabled() ) {
                 logger.debug("sessionDescription after fixup : " + sessionDescription);
            }
            
            this.sessionDescription = SipUtilities.cloneSessionDescription(sessionDescription);

        } catch (Exception ex) {
            logger.error("Unexpected exception ", ex);
            throw new SipXbridgeException("Unexpected exception", ex);
        }
    }

    public void setGlobalAddress(String publicAddress) {
        this.globalAddress = publicAddress;

    }

    public String getGlobalAddress() {
        return this.globalAddress;
    }

    /**
     * A flag that controls whether or not to assign global addresses to the RTP descriptor that
     * is assigned to this rtp receiver.
     * 
     * @param globalAddressingUsed
     */

    void setUseGlobalAddressing(boolean globalAddressingUsed) {
            if ( logger.isDebugEnabled() ) logger.debug("setUseGlobalAddressing " + globalAddressingUsed);
             this.useGlobalAddressing = globalAddressingUsed;
             // Reset the address in the sessoin description.
             if ( this.sessionDescription != null ) {
                this.setSessionDescription(sessionDescription);
             }
    }

}
