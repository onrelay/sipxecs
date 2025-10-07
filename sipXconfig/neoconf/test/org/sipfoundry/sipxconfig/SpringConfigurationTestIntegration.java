/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig;

import java.util.Map;

import static org.junit.Assert.assertTrue;


import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.sipfoundry.sipxconfig.test.TestHelper;

/**
 * Explicitly exercises the spring configuration
 */
public class SpringConfigurationTestIntegration extends IntegrationTestCase {
    public void testConfiguration() {
        assertTrue(true);
    }
    
    public void testBeansOfType() {
        Map<String, AddressProvider> beans = TestHelper.getApplicationContext().getBeansOfType(AddressProvider.class);
        for (AddressProvider a : beans.values()) {
            System.out.println(a.toString());            
        }
    }
}
