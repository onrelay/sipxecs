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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.api.model.CallParkBean;

@Path("/orbits/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("Call Park Management REST API")
public interface CallParkApi extends PromptsApi {

    @GET
    public Response getOrbits();

    @POST
    public Response newOrbit(@Description("Call Park bean to save") CallParkBean bean);

    @Path("servers/{serverId}")
    @GET
    public Response getOrbitsByServer(
            @Description("Server internal id or FQDN") @PathParam("serverId") String serverId);

    @Path("servers/{serverId}")
    @POST
    public Response newOrbit(@Description("Server internal id or FQDN") @PathParam("serverId") String serverId,
            @Description("Call Park bean to save") CallParkBean bean);

    @Path("servers/{serverId}")
    @DELETE
    public Response deleteOrbitsByServer(
            @Description("Server internal id or FQDN") @PathParam("serverId") String serverId);

    @Path("{orbitId}")
    @GET
    public Response getOrbit(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId);

    @Path("{orbitId}")
    @DELETE
    public Response deleteOrbit(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId);

    @Path("{orbitId}")
    @PUT
    public Response updateOrbit(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId,
            @Description("Call Park bean to save") CallParkBean bean);

    @Path("{orbitId}/settings")
    @GET
    public Response getOrbitSettings(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId,
            @Context HttpServletRequest request);

    @Path("{orbitId}/settings/{path:.*}")
    @GET
    public Response getOrbitSetting(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId,
            @Description("Path to Orbit setting") @PathParam("path") String path, @Context HttpServletRequest request);

    @Path("{orbitId}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setOrbitSetting(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId,
            @Description("Path to Orbit setting") @PathParam("path") String path, String value);

    @Path("{orbitId}/settings/{path:.*}")
    @DELETE
    public Response deleteOrbitSetting(@Description("Orbit internal id") @PathParam("orbitId") Integer orbitId,
            @Description("Path to Orbit setting") @PathParam("path") String path);

}
