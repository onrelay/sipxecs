/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxrest;

import java.util.Collection;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.routing.Filter;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestServerApplication extends Application {
    private static Logger logger = Logger.getLogger(RestServerApplication.class);

    private Collection<Plugin> plugins;

    public RestServerApplication() throws Exception {
       super();
       try {
           RestServiceFinder finder = RestServer.getServiceFinder();
           if (finder == null) {
               logger.error("RestServiceFinder is null; no plugins will be loaded");
               this.plugins = new ArrayList<>();
           } else {
               this.plugins = finder.getPluginCollection();
               if (this.plugins == null) {
                   logger.error("Plugin collection is null; using empty collection");
                   this.plugins = new ArrayList<>();
               } else {
                   logger.info("Loaded " + this.plugins.size() + " plugins");
               }
           }
       } catch (Exception e) {
           logger.error("Error loading plugins during RestServerApplication initialization", e);
           this.plugins = new ArrayList<>();
       }
    }

    @Override
    public Restlet getInboundRoot() {
        logger.debug("getInboundRoot");
        Context context = getContext();
        Router router = new Router(context);
        try {
            for (Plugin restService : plugins) {
                Filter filter = null;
                if ( restService.getMetaInf().getSecurity().equals(MetaInf.LOCAL_ONLY)) {
                    filter = new LocalOnlyFilter();
                } else if ( restService.getMetaInf().getRemoteAuthenticationMethod().equals(MetaInf.HTTP_DIGEST)) {
                    filter = new DigestAuthenticationFilter(restService);
                } else if ( restService.getMetaInf().getRemoteAuthenticationMethod().equals(MetaInf.HTTP_BASIC)) {
                   filter = new BasicAuthenticationFilter(restService);
                }  else {
                    logger.error("Unknown remote authentication type -- rejecting the plugin");
                    continue;
                }
                try {
                    restService.attachContext(filter, context, router);
                    logger.debug("Successfully attached plugin: " + restService.getClass().getName());
                } catch (Exception pluginEx) {
                    logger.error("Failed to attach plugin: " + restService.getClass().getName(), pluginEx);
                }
            }
        } catch (Exception e) {
            logger.error("Exception thrown during plugin loading: " + e, e);
        }

        router.attachDefault(RestServerDefault.class);
        logger.debug("Successfully set up router with default handler");
        return router;
    }

}
