/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.openfire.provider;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.AuthProvider;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.util.UnfortunateLackOfSpringSupportFactory;

public class MongoAuthProvider implements AuthProvider {

    public static final String SUPERADMIN = "superadmin";
    private String xmppDomain;

    @Override
    public void authenticate(String username, String password)
            throws UnauthorizedException, ConnectionException, InternalUnauthenticatedException {
        User user = UnfortunateLackOfSpringSupportFactory.getValidUsers()
                .getUserByInsensitiveJid(getUserName(username));
        if (user == null) {
            throw new UnauthorizedException("no user");
        }
        if (!user.getUserName().equals(SUPERADMIN) && !user.isImEnabled()) {
            throw new UnauthorizedException("IM not enabled for user");
        }
        if (!user.getPintoken().equals(password)) {
            throw new UnauthorizedException("wrong password");
        }
    }

    public String getPassword(String username) throws UserNotFoundException, UnsupportedOperationException {
        throw new UnsupportedOperationException("getPassword not supported");
    }

    @Override
    public void setPassword(String username, String password)
            throws UserNotFoundException, UnsupportedOperationException {
        throw new UnsupportedOperationException("setPassword not supported");
    }

    @Override
    public boolean supportsPasswordRetrieval() {
        return false;
    }

    @Override
    public boolean isScramSupported() {
        return false;
    }

    @Override
    public String getSalt(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException("SCRAM not supported");
    }

    @Override
    public int getIterations(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException("SCRAM not supported");
    }

    @Override
    public String getServerKey(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException("SCRAM not supported");
    }

    @Override
    public String getStoredKey(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException("SCRAM not supported");
    }

    private String getDomain() {
        if (xmppDomain == null) {
            xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        }
        return xmppDomain;
    }

    private String getUserName(String username) throws UnauthorizedException {
        String newUsername = username.trim().toLowerCase();
        if (newUsername.contains("@")) {
            int index = newUsername.indexOf("@");
            String domain = newUsername.substring(index + 1);
            if (domain.equals(getDomain())) {
                newUsername = newUsername.substring(0, index);
            } else {
                throw new UnauthorizedException("unknown domain: " + domain);
            }
        }
        return newUsername;
    }
}