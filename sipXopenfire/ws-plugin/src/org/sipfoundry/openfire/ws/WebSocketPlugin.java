/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.openfire.ws;

import java.io.File;
import java.security.GeneralSecurityException;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;

public class WebSocketPlugin implements Plugin {

    @Override
    public void initializePlugin(PluginManager pluginManager, File file) {
        try {
            trustAllCerts(); // Only use in dev/test environments
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        PresenceEventDispatcher.addListener(new PresenceEventListenerImpl());
    }

    @Override
    public void destroyPlugin() {
        // No-op
    }

    /**
     * Trusts all HTTPS certificates, insecure!
     */
    private void trustAllCerts() throws GeneralSecurityException {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        // Set the default SSL socket factory to trust all
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Also trust all hostnames (optional, also insecure)
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}