/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.rest;

import org.apache.commons.lang3.StringUtils;
import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;

/**
 * This router behaves similarly to standard Spring bean router, but it adds additional routes.
 *
 * Every route that starts with 'my' gets mirrored by route that starts with 'private'. Also for
 * compatibility reasons '/my/call' routes is mirrored by 'call' route.
 */
public class SipxSpringBeanRouter extends SpringBeanRouter {

    private static final String MY_CALL_PREFIX = "/my/call";
    private static final String MY_PREFIX = "/my";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
        String[] names = isFindingInAncestors() ? 
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(factory, ServerResource.class, true, true) : 
                factory.getBeanNamesForType(ServerResource.class, true, true);

        for (String name : names) {
            final String uri = resolveUri(name, factory);
            if (uri != null) {
                attachFinder(uri, createFinder(factory, name));
            }
        }
    }

    protected void attachFinder(String uri, Finder finder) {
        attach(uri, finder);
        if (uri.startsWith(MY_PREFIX)) {
            String privateUri = StringUtils.replaceOnce(uri, MY_PREFIX, "/private/{puk}");
            attach(privateUri, finder);
        }
        if (uri.startsWith(MY_CALL_PREFIX)) {
            String privateUri = StringUtils.replaceOnce(uri, MY_CALL_PREFIX, "/call");
            attach(privateUri, finder);
        }
    }
}
