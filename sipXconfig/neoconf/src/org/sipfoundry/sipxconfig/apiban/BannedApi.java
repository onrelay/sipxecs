package org.sipfoundry.sipxconfig.apiban;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.sipxconfig.apiban.model.BannedBean;

@Path("/banned")
@Produces({
    MediaType.APPLICATION_JSON
})
@Description("Banned REST API")
public interface BannedApi {

    @GET
    public BannedBean getBanned();
}
