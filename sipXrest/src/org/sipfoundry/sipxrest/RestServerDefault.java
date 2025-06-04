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

public class RestServerDefault extends ServerResource {
    
    @Override
    protected Representation get() {
        // Generate the response content
        String descriptionPage = RestServer.getServiceFinder().getDescriptions();

        // Set the response status
        setStatus(Status.SUCCESS_OK);

        // Return the response as a Representation
        return new StringRepresentation(descriptionPage, MediaType.TEXT_HTML);
    }
}
