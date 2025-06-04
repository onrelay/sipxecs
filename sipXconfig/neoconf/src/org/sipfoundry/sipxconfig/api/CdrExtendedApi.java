package org.sipfoundry.sipxconfig.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrBean;


@Path("/extendedcdrs/")
@Description("Extended CDR Management REST API")
public interface CdrExtendedApi extends BaseCdrApi {
    @POST
    @Consumes({
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response newCdr(@Description("ExtendedCdr bean to save") ExtendedCdrBean extendedCdrBean);
    
    @Path("delete/{prefix}")
    @DELETE
    public Response deletePrefixCdrHistory(@Description("Prefix") @PathParam("prefix") String prefix);
}
