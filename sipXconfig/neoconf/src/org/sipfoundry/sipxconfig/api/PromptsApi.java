/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.model.wadl.Description;

@Description("Prompts Management REST API")
public interface PromptsApi {

    @Path("prompts")
    @GET
    public Response getPrompts();

    @Path("prompts")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPrompts(List<Attachment> attachments, @Context HttpServletRequest request);

    @Path("prompts/{promptName}")
    @GET
    @Produces({
        "audio/x-wav", "audio/mpeg"
    })
    public Response downloadPrompt(@Description("Prompt name") @PathParam("promptName") String promptName);

    @Path("prompts/{promptName}")
    @DELETE
    public Response removePrompt(@Description("Prompt name") @PathParam("promptName") String promptName);

    @Path("prompts/{promptName}/stream")
    @GET
    @Produces({
        "audio/x-wav", "audio/mpeg"
    })
    public Response streamPrompt(@Description("Prompt name") @PathParam("promptName") String promptName);

}
