/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.commons.rest;

import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;



public class W3cDomRepresentation extends WriterRepresentation {
    private final Document m_document;

    public W3cDomRepresentation(MediaType mediaType, Document document) {
        super(mediaType);
        m_document = document;
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(m_document), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new IOException("Failed to write DOM document", e);
        }
    }

    public Document getDocument() {
        return m_document;
    }
}