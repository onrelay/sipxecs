/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.xmlrpc;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class XmlRpcProxyFactoryBeanTest extends TestCase {
    private Server m_server;

    protected void setUp() throws Exception {
        m_server = new Server();
    }

    protected void tearDown() throws Exception {
        m_server.stop();
    }

    public void testProxy() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context).loadBeanDefinitions(new ClassPathResource("beans.xml", getClass()));
        context.refresh();

        TestFunctions testFunctions = context.getBean("testXmlRpcFunctions", TestFunctions.class);
        String result = testFunctions.multiplyTest("ab", 2);
        assertEquals("abab", result);
    }
}
