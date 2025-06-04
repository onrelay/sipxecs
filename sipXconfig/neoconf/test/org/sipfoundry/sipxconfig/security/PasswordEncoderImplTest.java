/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.security;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.createMock;

import junit.framework.TestCase;

import org.sipfoundry.commons.security.PasswordEncoderImpl;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.login.LoginContext;
import org.sipfoundry.sipxconfig.permission.PermissionManager;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class PasswordEncoderImplTest extends TestCase {
    private static final String USER_NAME = "angelina";
    private static final String RAW_PASSWORD = "croft";
    // dummy result based on userName and password
    private static final String ENCODED_PASSWORD = USER_NAME + RAW_PASSWORD;
    private LoginContext m_loginContext;
    private PasswordEncoderImpl m_passwordEncoder;

    @Override
    protected void setUp() throws Exception {
        m_passwordEncoder = new PasswordEncoderImpl();

        final User user = new UserDetailsImplTest.RegularUser();
        user.setUserName(USER_NAME);
        PermissionManager pManager = createMock(PermissionManager.class);
        pManager.getPermissionModel();
        expectLastCall().andReturn(TestHelper.loadSettings("commserver/user-settings.xml")).anyTimes();
        replay(pManager);
        user.setPermissionManager(pManager);
    }

    public void testisPasswordValid() {
        m_loginContext.getEncodedPassword(RAW_PASSWORD);
        expectLastCall().andReturn(ENCODED_PASSWORD).times(2);
        replay(m_loginContext);
        assertTrue(m_passwordEncoder.matches(RAW_PASSWORD,ENCODED_PASSWORD));
        assertFalse(m_passwordEncoder.matches(RAW_PASSWORD,"bad encoded password"));
        verify(m_loginContext);
    }

    public void testEncodePassword() {
        m_loginContext.getEncodedPassword(RAW_PASSWORD);
        expectLastCall().andReturn(ENCODED_PASSWORD);
        replay(m_loginContext);
        assertEquals(ENCODED_PASSWORD, m_passwordEncoder.encode(RAW_PASSWORD));
        verify(m_loginContext);
    }
}
