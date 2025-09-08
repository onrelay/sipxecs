/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.common;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SystemTaskRunner {

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                throw new IllegalArgumentException("bean to run is required as first argument");
            }
            new SystemTaskRunner().runMain(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }

    void runMain(String[] args) {
        ClassPathXmlApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext(
                "classpath:/org/sipfoundry/sipxconfig/system.beans.xml",
                "classpath*:/org/sipfoundry/sipxconfig/*/**/*.beans.xml",
                "classpath*:/org/sipfoundry/sipxconfig/api/jaxrs-server.xml",
                "classpath*:/org/sipfoundry/sipxconfig/api/jaxrs-client.xml",
                "classpath*:/sipxplugin2.beans.xml",
                "classpath*:/sipxplugin.beans.xml",
                "classpath*:/sipxplugin0.beans.xml");

            SystemTaskEntryPoint task = (SystemTaskEntryPoint) context.getBean(args[0]);
            task.runSystemTask(args);
        } finally {
            if( context != null ) {
                context.close();
            }
        }
    }
}