/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.callcontroller;

import org.restlet.Context;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.Request;
import org.sipfoundry.sipxrest.Plugin;




public class CallControllerPlugin extends Plugin {

    @Override
    public void attachContext(Filter filter, Context context, Router router) {
        filter.setNext(new CallControllerRestlet(context));
        String suffix = String.format("/{%s}/{%s}", CallControllerParams.CALLING_PARTY, CallControllerParams.CALLED_PARTY);
        Route route = router.attach(this.getMetaInf().getUriPrefix() + suffix,filter);
        extractQuery(route,CallControllerParams.AGENT,CallControllerParams.AGENT,true);
        extractQuery(route,CallControllerParams.FORWARDING_ALLOWED, CallControllerParams.FORWARDING_ALLOWED, true);
        extractQuery(route,CallControllerParams.SUBJECT, CallControllerParams.SUBJECT, true);
        extractQuery(route,CallControllerParams.TIMEOUT, CallControllerParams.TIMEOUT, true);
        extractQuery(route,CallControllerParams.CONFERENCE_PIN, CallControllerParams.CONFERENCE_PIN, true);
        extractQuery(route,CallControllerParams.RESULTCACHETIME, CallControllerParams.RESULTCACHETIME, true );
        extractQuery(route,CallControllerParams.METHOD, CallControllerParams.METHOD, true);
        extractQuery(route,CallControllerParams.ACTION,CallControllerParams.ACTION,true);
        extractQuery(route,CallControllerParams.TARGET, CallControllerParams.TARGET, true);
    }

    @Override
    public String getAgent(Request request) {
        String retval =  (String) request.getAttributes().get(CallControllerParams.AGENT);
        if ( retval == null ){
            retval =  (String) request.getAttributes().get(CallControllerParams.CALLING_PARTY);
        } 
        
        return retval;
    }

}
