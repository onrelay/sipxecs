package org.sipfoundry.sipxconfig.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;

@Path("/apiban/")
@Produces({
    MediaType.APPLICATION_JSON
})
@Description("APIBAN Management REST API")
public interface ApibanApi {

    @Path("banned")
    @GET
    public Response getBanned();
}
