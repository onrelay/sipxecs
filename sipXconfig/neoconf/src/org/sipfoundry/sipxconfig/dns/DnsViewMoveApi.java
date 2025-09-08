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
package org.sipfoundry.sipxconfig.dns;

import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;

public class DnsViewMoveApi extends ServerResource {
    private DnsManager m_dnsManager;
    private ObjectMapper m_jsonMapper = new ObjectMapper();

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(APPLICATION_JSON));
    }



    // PUT
    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {       
        try {
            JsonNode data = m_jsonMapper.reader().readTree(entity.getStream());
            int step = Integer.valueOf(data.get("step").asInt());
            List<Integer> ids = new ArrayList<Integer>();
            Iterator<JsonNode> nodes = data.get("ids").iterator();
            while (nodes.hasNext()) {
                ids.add(nodes.next().asInt());
            }
            m_dnsManager.moveViewById(ids.toArray(new Integer[0]), step);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setDnsManager(DnsManager dnsManager) {
        m_dnsManager = dnsManager;
    }
}
