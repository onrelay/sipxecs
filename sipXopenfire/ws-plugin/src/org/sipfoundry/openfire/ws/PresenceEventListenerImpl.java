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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class PresenceEventListenerImpl implements PresenceEventListener {

	@Override
	public void availableSession(ClientSession arg0, Presence presence) {
		sendPresenceMessage(presence);
	}

	@Override
	public void presenceChanged(ClientSession arg0, Presence presence) {
		sendPresenceMessage(presence);
	}

    private void sendPresenceMessage(Presence presence) {
        try {
            String status = presence.getStatus();
            String message = StringUtils.isEmpty(status) ? "Offline" : status;
            User user = UnfortunateLackOfSpringSupportFactory.getValidUsers().getUserByJid(presence.getFrom().getNode());
            if (user != null) {
                invokePost(user.getUserName(), message);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	@Override
	public void subscribedToPresence(JID arg0, JID arg1) {
	}

	@Override
	public void unavailableSession(ClientSession arg0, Presence presence) {
		sendPresenceMessage(presence);

	}

	@Override
	public void unsubscribedToPresence(JID arg0, JID arg1) {
	}

    private String getRestServerUrl(String fqdn, int port) {
        return String.format("https://%s:%d/receiver", fqdn, port);
    }

private String invokePost(String userId, String message) throws Exception {
    String response = null;
    String websocketAddress = System.getProperty("websocket.address");
    String websocketPort = System.getProperty("websocket.port");

    if (websocketAddress != null && websocketPort != null) {
        String url = getRestServerUrl(websocketAddress, Integer.parseInt(websocketPort));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("user_id", userId)
                .header("Content-Type", "text/x-json")
                .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            response = httpResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    return response;
}
}
