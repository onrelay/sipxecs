/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxrest.cdrlog;


import org.restlet.Context;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.Request;
import org.sipfoundry.sipxrest.Plugin;

public class CdrLogPlugin extends Plugin {

    @Override
    public void attachContext(Filter filter, Context context, Router router) {
       filter.setNext(new CdrLogRestlet());
       Route cdrRoute = router.attach(this.getMetaInf().getUriPrefix() + "/{user}",filter);       
       extractQuery(cdrRoute,CdrLogParams.LIMIT, CdrLogParams.LIMIT, true);
       extractQuery(cdrRoute,CdrLogParams.FROMDATE, CdrLogParams.FROMDATE, true);
       extractQuery(cdrRoute,CdrLogParams.OFFSET, CdrLogParams.OFFSET, true);
    }

    @Override
    public String getAgent(Request request) {
        return (String) request.getAttributes().get(CdrLogParams.USER);
    }
 
}

