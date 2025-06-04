/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxivr;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.sipfoundry.sipxivr.rest.RestfulRequest;


public class RestfulRequestTest extends TestCase {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");
    Server m_server;
    boolean dontBother = false;

    protected void setUp() throws Exception {
        super.setUp();
        // Configure log4j (unchanged)
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "debug, cons");
        props.setProperty("log4j.appender.cons", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.cons.layout", "org.sipfoundry.commons.log4j.SipFoundryLayout");
        props.setProperty("log4j.appender.cons.layout.facility", "sipXivr");
        PropertyConfigurator.configure(props);

        try {
            m_server = new Server();
            ServerConnector connector = new ServerConnector(m_server);
            connector.setHost("localhost");
            connector.setPort(12345);
            m_server.addConnector(connector);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            m_server.setHandler(context);

            ServletHolder holder = new ServletHolder(new RestfulRequestTestServlet());
            context.addServlet(holder, "/woof/*");

            m_server.start();
        } catch (Exception e) {
            dontBother = true;
            LOG.warn("Problem starting Jetty.  Skip tests.", e);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (m_server != null) {
            m_server.stop();
            m_server.join();
        }
    }

    public void testPut() {
        if (dontBother)
            return ;
        
        RestfulRequest rr = new RestfulRequest("http://localhost:12345/woof/dog");
        try {
            boolean okay = rr.put("put");
            assertTrue("response not okay", okay);
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

    public void testPost() {
        if (dontBother)
            return ;

        RestfulRequest rr = new RestfulRequest("http://localhost:12345/woof/dog");
        try {
            boolean okay = rr.post("post");
            assertTrue("response not okay", okay);
            assertEquals("Next time, use mail, not POST!\n", rr.getContent());
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

    public void testDelete() {
        if (dontBother)
            return ;

        RestfulRequest rr = new RestfulRequest("http://localhost:12345/woof/dog");
        try {
            boolean okay = rr.delete();
            assertTrue("response not okay", okay);
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

    public void testGet() {
        if (dontBother)
            return ;

        RestfulRequest rr = new RestfulRequest("http://localhost:12345/woof/dog");
        try {
            boolean okay = rr.get();
            assertTrue("response not okay", okay);
            assertEquals("How now, fuzzy brown puppy!\n", rr.getContent());
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

}
