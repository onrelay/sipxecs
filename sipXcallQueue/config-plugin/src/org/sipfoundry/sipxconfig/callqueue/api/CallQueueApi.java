package org.sipfoundry.sipxconfig.callqueue.api;

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
import org.sipfoundry.sipxconfig.api.model.SettingsList;

@Path("/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
@Description("Call Queue Management REST API")
public interface CallQueueApi {
    @Path("queue")
    @GET
    public Response getQueues();
    
    @Path("queue")
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newQueue(@Description("Queue bean to save") CallQueueBean callQueueBean);
    
    @Path("queue/{name}")
    @GET
    public Response getQueue(@Description("Queue name") @PathParam("name") String name);    
    
    @Path("queue/{name}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateQueue(
            @Description("Queue name") @PathParam("name") String name,
            @Description("Queue to save") CallQueueBean aaBean);
    
    @Path("queue/{name}")
    @DELETE
    public Response deleteQueue(@Description("Queue  name") @PathParam("name") String name);
    
    @Path("agent")
    @GET
    public Response getAgents();
    
    @Path("agent")
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newAgent(@Description("Agent bean to save") CallQueueAgentBean callQueueAgentBean);
    
    @Path("agent/{name}")
    @GET
    public Response getAgent(@Description("Agent name") @PathParam("name") String name);    
    
    @Path("agent/{name}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAgent(
            @Description("Agent name") @PathParam("name") String name,
            @Description("Agent to save") CallQueueAgentBean aaBean);
    
    @Path("agent/{name}")
    @DELETE
    public Response deleteAgent(@Description("Agent name") @PathParam("name") String name);
    
    @Path("settings")
    @GET
    public Response getSettings(@Context HttpServletRequest request);

    @Path("settings")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response setSettings(@Description("Settings to save") SettingsList settingsList);
}
