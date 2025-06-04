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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

public class DnsPreviewApi extends ServerResource {
    private DnsPreview m_dnsPreview;
    private DnsViewApi m_dnsViewApi;
    private DnsPreview.Show m_showLevel;

    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(APPLICATION_JSON));
        String showLevel = (String) getRequest().getAttributes().get("show");
        m_showLevel = DnsPreview.Show.valueOf(showLevel);
    }

    // POST
    @Post
    public Representation acceptRepresentation(Representation entity) throws ResourceException {        
        DnsView view = m_dnsViewApi.readViewHandleErrors(entity);
        getResponse().setEntity(new StringRepresentation(m_dnsPreview.getZone(view, m_showLevel)));
        return null;
    }

    public void setDnsPreview(DnsPreview dnsPreview) {
        m_dnsPreview = dnsPreview;
    }

    public void setDnsViewApi(DnsViewApi dnsViewApi) {
        m_dnsViewApi = dnsViewApi;
    }
}
