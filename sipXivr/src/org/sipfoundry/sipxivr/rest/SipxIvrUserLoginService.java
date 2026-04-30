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

package org.sipfoundry.sipxivr.rest;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserIdentity;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.security.Password;

import org.sipfoundry.commons.security.Md5Encoder;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.userdb.ValidUsers;

public class SipxIvrUserLoginService extends AbstractLifeCycle implements LoginService {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");

    private String m_sipRealm;
    private String m_sharedSecret;
    private ValidUsers m_validUsers;
    private IdentityService m_identityService;
    private Configuration m_configuration;

    public SipxIvrUserLoginService() {
        m_identityService = new DefaultIdentityService();
        m_configuration = new Configuration( this );
    }

    public Configuration getConfiguration() {
        return m_configuration;
    }

    @Override
    public String getName() {
        return m_sipRealm; // This is your realm name
    }

    @Override
    public UserIdentity login(String username, Object credentials, Request request, Function<Boolean, Session> sessionFunction ) {
        Principal principal = authenticate(username, credentials);
        if (principal != null) {
            return getUserIdentity( principal );
        }
        return null;
    }

    public Principal authenticate(String username, Object credentials) {
        Principal principal = null;
        try {
            User user = m_validUsers.getUser(username);
            if (user != null) {
                principal = checkCredentials(user.getUserName(), user.getPintoken(), credentials);
                if (principal == null) {
                    // 2nd try with shared secret
                    String hashedSharedSecret = Md5Encoder.digestEncryptPassword(user.getUserName(), m_sharedSecret);
                    principal = checkCredentials(user.getUserName(), hashedSharedSecret, credentials);
                }
            }
        } catch (Exception ex) {
            LOG.warn("Could not authenticate user " + username);
        }
        return principal;
    }

    private Principal checkCredentials(String userName, String pintoken, Object credentials) {
        Password password = new Password(Md5Encoder.digestEncryptPassword(userName, pintoken));
        if (password.check(credentials)) {
            return new SipxIvrPrincipal(userName, "IvrRole");
        }
        return null;
    }


    @Override
    public void logout(UserIdentity user) {
        // No-op for now
    }

    public UserIdentity getUserIdentity(Principal principal) {
        if (principal != null) {
            Subject subject = new Subject(false, 
                Collections.singleton(principal), 
                Collections.emptySet(), 
                Collections.emptySet()
            );
            return UserIdentity.from(subject, principal, "IvrRole");
        }
        return null;
    }

    @Override
    public IdentityService getIdentityService() {
        return m_identityService;
    }

    @Override
    public void setIdentityService(IdentityService identityService) {
        m_identityService = identityService;
    }

    @Override
    public boolean validate(UserIdentity userIdentity ) {

        if (userIdentity == null || userIdentity.getUserPrincipal() == null) {
            return false;
        }
        String username = userIdentity.getUserPrincipal().getName();
        return m_validUsers.getUser(username) != null; 

    }

    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }

    public void setSipRealm(String sipRealm) {
        m_sipRealm = sipRealm;
    }

    public void setSecret(String secret) {
        m_sharedSecret = secret;
    }

    private static class SipxIvrPrincipal implements Principal {
        private final String m_userName;
        private final String m_role;

        SipxIvrPrincipal(String name, String role) {
            m_userName = name;
            m_role = role;
        }

        @Override
        public String getName() {
            return m_userName;
        }

        @SuppressWarnings("unused")
        public boolean isAuthenticated() {
            return true;
        }

        @SuppressWarnings("unused")
        public String getRole() {
            return m_role;
        }
    }

    public static class Configuration implements Authenticator.Configuration {

        private final SipxIvrUserLoginService m_userLoginService;

        public Configuration( SipxIvrUserLoginService userLoginService ) {
            m_userLoginService = userLoginService;
        }
        @Override
        public String getRealmName() {
            return m_userLoginService.getName(); 
        }
        @Override
        public String getAuthenticationType() {
            return "DIGEST";
        }
        @Override
        public IdentityService getIdentityService() {
            return m_userLoginService.getIdentityService();
        }
        @Override
        public org.eclipse.jetty.security.LoginService getLoginService() {
            return m_userLoginService;
        }
        @Override 
        public boolean isSessionRenewedOnAuthentication() { 
            return true; 
        }
        @Override 
        public int getSessionMaxInactiveIntervalOnAuthentication() { 
            return -1; 
        }
        @Override 
        public String getParameter(String param) {
            return null;
        }
        @Override 
        public Set<String> getParameterNames() {
            return new HashSet<String>();
        }
    }
}
