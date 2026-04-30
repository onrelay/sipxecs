/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxrest;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

public class RestServerDefault extends ServerResource {
    private static final Logger logger = Logger.getLogger(RestServerDefault.class);
    
    @Override
    protected Representation get() {
        try {
            // Generate the response content
            if (RestServer.getServiceFinder() == null) {
                logger.error("RestServiceFinder is null");
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return new StringRepresentation("Error: REST service finder not initialized", MediaType.TEXT_PLAIN);
            }
            String descriptionPage = RestServer.getServiceFinder().getDescriptions();
            if (descriptionPage == null) {
                logger.warn("Plugin descriptions are null");
                descriptionPage = "<html><body><h1>sipXrest REST API Server</h1><p>No plugins loaded. Check logs for errors.</p></body></html>";
            }

            // Set the response status
            setStatus(Status.SUCCESS_OK);

            // Return the response as a Representation
            return new StringRepresentation(descriptionPage, MediaType.TEXT_HTML);
        } catch (Exception e) {
            logger.error("Error generating default response", e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new StringRepresentation("Error: " + e.getMessage(), MediaType.TEXT_PLAIN);
        }
    }
}
