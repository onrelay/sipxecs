/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.commons.userdb.profile.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class UserAvatarResource extends ServerResource {
    private static final Log LOG = LogFactory.getLog(UserAvatarResource.class);

    private String m_userName;
    private UserProfileService m_avatarService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_ALL));
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        m_userName = (String) getRequest().getAttributes().get("user");
    }

    @Get
    public Representation represent(Variant variant) throws ResourceException {
        return new AvatarRepresentation(MediaType.IMAGE_PNG, m_avatarService.getAvatar(m_userName));
    }

    @Post
    public Representation acceptRepresentation(Representation entity) throws ResourceException {
        if (entity != null) {
            try {
                // Extract the underlying HttpServletRequest
                HttpServletRequest servletRequest =
                        (HttpServletRequest) getRequest().getAttributes().get("jakarta.servlet.request");

                if (servletRequest != null) {
                    Part filePart = servletRequest.getPart("file");
                    if (filePart == null) {
                        LOG.error("No file part in request");
                        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                "Missing 'file' part in multipart request");
                    }

                    try (InputStream is = filePart.getInputStream()) {
                        m_avatarService.saveAvatar(m_userName, is);
                    }
                } else {
                    // Fallback: treat entity as raw body (non-multipart)
                    try (InputStream is = entity.getStream()) {
                        m_avatarService.saveAvatar(m_userName, is);
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot upload avatar", e);
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        return null;
    }

    static class AvatarRepresentation extends OutputRepresentation {
        private final InputStream m_is;

        public AvatarRepresentation(MediaType mediaType, InputStream is) {
            super(mediaType);
            m_is = is;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            if (m_is != null) {
                IOUtils.copy(m_is, outputStream);
            }
            IOUtils.closeQuietly(m_is);
        }
    }

    public void setUserAvatarService(UserProfileService service) {
        m_avatarService = service;
    }
}
