/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxivr.rest;

import java.net.InetSocketAddress;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.AuthenticationState;

public class SipXSecurityHandler extends AbstractHandler.Wrapper {
    private final int m_publicHttpPort;
    private DigestAuthenticator m_digestAuthenticator;

    public SipXSecurityHandler(int publicHttpPort) {
        this.m_publicHttpPort = publicHttpPort;
    }

    public void setDigestAuthenticator( DigestAuthenticator digestAuthenticator ) {
        this.m_digestAuthenticator =digestAuthenticator;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        InetSocketAddress localSocketAddress = 
            (InetSocketAddress)request.getConnectionMetaData().getLocalSocketAddress();

        if(localSocketAddress.getPort() == m_publicHttpPort) {

            AuthenticationState state = 
                m_digestAuthenticator.validateRequest(request, response, callback);

            if (state == null || state != AuthenticationState.SEND_SUCCESS ) {
                return true; 
            }
        }

        return getHandler().handle(request, response, callback);
    }
            
}