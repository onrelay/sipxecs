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
package org.sipfoundry.commons.jetty;

import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class SipXSecurityHandler extends ConstraintSecurityHandler {
    @SuppressWarnings("unused")
    private int m_publicHttpPort;

    public SipXSecurityHandler(int port) {
        m_publicHttpPort = port;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        Handler handler = getHandler();
        if (handler != null) {
            // Delegate to the wrapped handler
            return handler.handle(request, response, callback);
        } else {
            // No handler set; just signal completion
            callback.succeeded();
            return true; // Handled
        }
    }
}
