/**
 * Copyright (c) 2016 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/my/moh/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("My Music On Hold's Management REST API")
public interface MyMohApi extends PromptsApi {

    @Path("settings/moh/audio-source")
    @GET
    public Response getMohAudioSourceSetting(@Context HttpServletRequest request);

    @Path("settings/moh/audio-source")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setMohAudioSourceSetting(String value);

    @Path("settings/moh/audio-source")
    @DELETE
    public Response deleteMohAudioSourceSetting();

    @Path("permission")
    @GET
    public Response getUserMohPermission(@Context HttpServletRequest request);

    @Path("path")
    @PUT
    public Response createCurrentUserPath();
}
