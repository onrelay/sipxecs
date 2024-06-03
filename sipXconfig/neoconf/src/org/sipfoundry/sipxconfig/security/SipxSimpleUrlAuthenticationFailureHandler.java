/**
 *
 *
 * Copyright (c) 2013 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.security;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;


public class SipxSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String REFERER = "Referer";

    private String sipxDefaultFailureUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        if( sipxDefaultFailureUrl != null ) {
            try {
                String referer = request.getHeader(REFERER);
                URL refererUrl = new URL(referer);
                StringBuilder url = new StringBuilder();
                String protocol = refererUrl.getProtocol();
                url.append(protocol);
                url.append("://");
                url.append(refererUrl.getHost());
                int port = refererUrl.getPort();
                if (port > 0
                        && ((protocol.equalsIgnoreCase("http") && port != 80)
                        || (protocol.equalsIgnoreCase("https") && port != 443))) {
                    url.append(':');
                    url.append(port);
                }
                url.append(request.getContextPath());
                url.append(sipxDefaultFailureUrl);

                super.setDefaultFailureUrl( url.toString() );

            } catch (Exception ex) {
                this.logger.error("Error updating failure URL: " + ex.getMessage() );
            }
        }

        super.onAuthenticationFailure( request, response, exception );
    }

    @Override // Must override since superclass defaultFailureUrl is not public
    public void setDefaultFailureUrl(String defaultFailureUrl) {

        super.setDefaultFailureUrl(defaultFailureUrl);

        this.sipxDefaultFailureUrl = defaultFailureUrl;

     }

}
