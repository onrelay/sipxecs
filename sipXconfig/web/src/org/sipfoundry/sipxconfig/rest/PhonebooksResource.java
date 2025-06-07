/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rest;

import static org.restlet.data.MediaType.APPLICATION_ALL_XML;
import static org.restlet.data.MediaType.TEXT_XML;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.common.rest.Dom4jRepresentation;

public class PhonebooksResource extends ServerResource {
    private PhonebookManager m_phonebookManager;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_ALL_XML));
    }

    @Get
    public Representation represent(Variant variant) throws ResourceException {        
        
        if (TEXT_XML.equals(variant.getMediaType())) {
            return new Dom4jRepresentation(getDom());
        }
        return null;
    }

    private Document getDom() {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element rootItem = document.addElement("phonebooks");
        for (Phonebook phonebook : m_phonebookManager.getPhonebooks()) {
            Element phonebookEl = rootItem.addElement("phonebook");
            phonebookEl.addAttribute("name", phonebook.getName());
        }
        return document;
    }

    public void setPhonebookManager(PhonebookManager phonebookManager) {
        m_phonebookManager = phonebookManager;
    }
}
