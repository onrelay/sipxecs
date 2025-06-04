package org.sipfoundry;

import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.resource.ResourceException;

/**
 * REST API that will provide a programming interface to your feature as well as
 * support for dart calls in UI web page.
 */
public class ExampleApi extends ServerResource {
    private Example m_example;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(APPLICATION_JSON));
    }

    @Get
    public Representation getExample() throws ResourceException {
        return new StringRepresentation(m_example.hello(), APPLICATION_JSON);
    }

    @Post
    public Representation postExample(Representation entity) throws ResourceException {
        try {
            String body = IOUtils.toString(entity.getReader());
            // Handle POST data here (body)
            return null;
        } catch (IOException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    public void setExample(Example example) {
        m_example = example;
    }
}