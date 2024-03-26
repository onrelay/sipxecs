package org.sipfoundry.sipxconfig.callqueue.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
