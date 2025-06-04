package org.sipfoundry.sipxconfig.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantGenericSettingsBean;
import org.sipfoundry.sipxconfig.api.model.AutoAttendantSpecialModeBean;
import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/autoattendant/")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
})
public interface AutoAttendantApi {

    @GET
    public Response getAutoAttendants();
    
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newAutoAttendant(@Description("AutoAttendant bean to save") AutoAttendantBean callGroupBean);
    
    @Path("{name}")
    @GET
    public Response getAutoAttendant(@Description("AutoAttendant name") @PathParam("name") String name);    
    
    @Path("{name}")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendant(
            @Description("AutoAttendant name") @PathParam("name") String name,
            @Description("AutoAttendant to save") AutoAttendantBean aaBean);
    
    @Path("{name}")
    @DELETE
    public Response deleteAutoAttendant(@Description("AutoAttendant  name") @PathParam("name") String name);
    
    @Path("settings")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendantGenericSettings (
            @Description("AutoAttendant generic settings to save") AutoAttendantGenericSettingsBean aaGenSettingsBean);
    
    @Path("settings")
    @GET
    public Response getAutoAttendantGenericSettings();     
    
    @Path("specialmode")
    @PUT
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response updateAutoAttendantSpecialMode (
            @Description("AutoAttendant special mode to save") AutoAttendantSpecialModeBean aaSpecialModeBean);
    
    @Path("specialmode")
    @GET
    public Response getAutoAttendantSpecialMode();     
    
}
