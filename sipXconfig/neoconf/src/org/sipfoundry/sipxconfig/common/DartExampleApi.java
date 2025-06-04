/**
 * Copyright (c) 2013 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.common;

import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;


/**
 * Example backend API to DartExample.html page. Emits fake data regarding bird report
 */
class DartExampleApi extends ServerResource {
    private static final String DAY = "day";
    private static final String SPOTTINGS = "spottings";
    private static Map<String, Object> s_report;
    static {
        reset();
    }

    enum Actions {
        RESET, ADD, DELETE
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(APPLICATION_JSON));
    }


    @Get
    public Representation represent(Variant variant) throws ResourceException {   
        
        try {
            getResponse().setStatus(Status.SUCCESS_OK);
            String json = new ObjectMapper().writeValueAsString(s_report);        
            return new StringRepresentation(json);

        } catch( JsonProcessingException ex ) {
            throw new ResourceException( ex );
        }
    }

    public void acceptRepresentation(Representation entity) throws ResourceException {
        String json;
        try {
            json = IOUtils.toString(entity.getStream());
            parseForm(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void parseForm(String json) {
        try {
            Map<String, Object> form = new ObjectMapper().readValue(json, Map.class);
            Map<String, String[]> spottings = (Map<String, String[]>) s_report.get(SPOTTINGS);
            String action = (String) form.get("action");
            switch (Actions.valueOf(action)) {
            case DELETE:
                spottings.remove(form.get(DAY));
                break;
            case RESET:
                reset();
                break;
            case ADD:
                String bird = (String) form.get("bird");
                String day = (String) form.get(DAY);
                String[] existing = spottings.get(day);
                if (existing == null) {
                    spottings.put(day, new String[] {
                        bird
                    });
                } else {
                    List<String> add = new ArrayList<String>(Arrays.asList(existing));
                    add.add(bird);
                    spottings.put(day, add.toArray(new String[0]));
                }
                break;
        
            default:
                throw new RuntimeException("Unhandled action " + action);
            }
        } catch( IOException ex ) {
            throw new ResourceException( ex );
        }
    }

    Map<String, Object> getReport() {
        return s_report;
    }

    static void reset() {
        s_report = new HashMap<String, Object>();
        s_report.put("report", "birds");
        s_report.put("countries", new String[] {
            "USA", "Romania", "Philippines"
        });
        Map<String, String[]> spottings = new HashMap<String, String[]>();
        spottings.put("01/03/2013", new String[] {
            "Western Tanager", "Hearing Gull"
        });
        spottings.put("01/04/2013", new String[] {
            "Brown Thrasher", "Eastern Flycatcher", "Barn Owl"
        });
        s_report.put(SPOTTINGS, spottings);
    }
}
