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
import org.sipfoundry.sipxconfig.api.model.GatewayBean;

@Path("/gateways/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("Gateway Management REST API")
public interface GatewayApi {

    @GET
    public Response getGateways();

    @Path("models")
    @GET
    public Response getGatewayModels();

    @Path("{gatewayId}/availablerules")
    @GET
    public Response getAvailableRules(@Description("Gateway id")
        @PathParam("gatewayId") String gatewayId);

    @Path("{gatewayId}")
    @GET
    public Response getGateway(@Description("Gateway id")
        @PathParam("gatewayId") String gatewayId);

    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newGateway(@Description("Gateway bean to save") GatewayBean gatewayBean);

    @Path("{gatewayId}")
    @DELETE
    public Response deleteGateway(@Description("Gateway Id") @PathParam("gatewayId") String gatewayId);

    @Path("{gatewayId}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateGateway(
            @Description("Gateway id") @PathParam("gatewayId") String gatewayId,
            @Description("Gateway bean to save") GatewayBean gatewayBean);

    @Path("{gatewayId}/rules/{ruleId}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response addGatewayToRule(
            @Description("Gateway id") @PathParam("gatewayId") String gatewayId,
            @Description("ruleId id") @PathParam("ruleId") Integer ruleId);

    @Path("{gatewayId}/rules/{ruleId}")
    @DELETE
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response removeGatewayFromRule(
            @Description("Gateway id") @PathParam("gatewayId") String gatewayId,
            @Description("Rule id") @PathParam("ruleId") Integer ruleId);

    @Path("{gatewayId}/settings")
    @GET
    public Response getGatewaySettings(
            @Description("Gateway id/serial number")
            @PathParam("gatewayId") String gatewayId,
            @Context HttpServletRequest request);

    @Path("{gatewayId}/settings/{path:.*}")
    @GET
    public Response getGatewaySetting(
            @Description("Gateway id or serial")
            @PathParam("gatewayId") String gatewayId,
            @Description("Path to Gateway setting") @PathParam("path") String path,
            @Context HttpServletRequest request);

    @Path("{gatewayId}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setGatewaySetting(
            @Description("Gateway internal id or name")
            @PathParam("gatewayId") String gatewayId,
            @Description("Path to Gateway setting") @PathParam("path") String path, String value);

    @Path("{gatewayId}/settings/{path:.*}")
    @DELETE
    public Response deleteGatewaySetting(
            @Description("Gateway internal id or name")
            @PathParam("gatewayId") String gatewayId,
            @Description("Path to Gateway setting") @PathParam("path") String path);

    @Path("{gatewayId}/ports")
    @GET
    public Response getPorts(@Description("Gateway id")
        @PathParam("gatewayId") String gatewayId);

    @Path("{gatewayId}/ports")
    @POST
    public Response addPort(@Description("Gateway id")
        @PathParam("gatewayId") String gatewayId);

    @Path("{gatewayId}/ports/{portId}")
    @DELETE
    public Response removePort(@Description("Gateway id")
        @PathParam("gatewayId") String gatewayId, @PathParam("portId") Integer portId);

    @Path("{gatewayId}/ports/{portId}/settings")
    @GET
    public Response getGatewayPortSettings(
            @Description("Gateway id/serial number")
            @PathParam("gatewayId") String gatewayId, @PathParam("portId") Integer portId,
            @Context HttpServletRequest request);

    @Path("{gatewayId}/ports/{portId}/settings/{path:.*}")
    @GET
    public Response getGatewayPortSetting(
            @Description("Gateway id or serial")
            @PathParam("gatewayId") String gatewayId,
            @PathParam("portId") Integer portId,
            @Description("Path to Gateway Port setting") @PathParam("path") String path,
            @Context HttpServletRequest request);

    @Path("{gatewayId}/ports/{portId}/settings/{path:.*}")
    @PUT
    @Consumes({
        MediaType.TEXT_PLAIN
    })
    public Response setGatewayPortSetting(
            @Description("Gateway internal id or name")
            @PathParam("gatewayId") String gatewayId,
            @PathParam("portId") Integer portId,
            @Description("Path to Gateway Port setting") @PathParam("path") String path, String value);

    @Path("{gatewayId}/ports/{portId}/settings/{path:.*}")
    @DELETE
    public Response deleteGatewayPortSetting(
            @Description("Gateway internal id or name")
            @PathParam("gatewayId") String gatewayId,
            @PathParam("portId") Integer portId,
            @Description("Path to Phone Group setting") @PathParam("path") String path);

}
