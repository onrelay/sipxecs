/*
 *  Copyright (C) 2024 OR, Inc. (dba OnRelay), certain elements licensed under a Contributor Agreement.
 *  Contributors retain copyright to elements licensed under a Contributor Agreement.
 *  Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.commons.util;

import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.socket.IceUdpSocketWrapper;
import org.ice4j.stunclient.SimpleAddressDetector;

/**
 * A utility class to lookup public server address via STUN
 * *
 */
public class AddressDiscovery {

    private static final Logger logger = Logger.getLogger(AddressDiscovery.class);

    private static final Transport STUN_TRANSPORT = Transport.UDP;

    public static InetAddress discoverAddress( String localAddresss, int localPort, String stunServerAddress, int stunServerPort ) {
       
        SimpleAddressDetector addressDetector = null;

        try {
            InetAddress publicAddress = null;

            if (localAddresss == null) {
                logger.error("Local address not specified");
                return null;
            }

            if( localPort <= 0 ) {
                logger.error("Local STUN lookup port not valid: " + localPort );
                return null;
            }

            TransportAddress localTransportAddress = new TransportAddress(
                localAddresss, localPort, STUN_TRANSPORT );

            if (stunServerAddress == null) {
                logger.error("Stun server address not specified");
                return null;
            }

            if( stunServerPort <= 0 ) {
                logger.error("Stun server port not valid: " + stunServerPort );
                return null;
            }

            TransportAddress serverTransportAddress = new TransportAddress(
                stunServerAddress, stunServerPort, STUN_TRANSPORT );

             
            addressDetector = new SimpleAddressDetector( serverTransportAddress );

            addressDetector.start();               

            IceUdpSocketWrapper localSocket = new IceUdpSocketWrapper(new DatagramSocket(localTransportAddress));

            TransportAddress publicTransportAddress = addressDetector.getMappingFor(localSocket);

            if( publicTransportAddress == null )
            {
                logger.error("No stun address - could not do address discovery from STUN server: " + serverTransportAddress );
                return null;
            }

            publicAddress = publicTransportAddress.getAddress();

            logger.info("Discovered public address " + publicAddress
                + " from STUN server " + stunServerAddress
                + " using local address " + localSocket);

            return publicAddress;

        } catch (Exception ex) {
             logger.error("Error discovering  address -- Check Stun Server", ex);
             return null;
        } finally {
            if (addressDetector != null) {
                try {
                    addressDetector.shutDown();
                } catch (Exception e ) {
                    logger.error("Problem shutting down address detector!",e);
                } 
            }
        }
    }
}
