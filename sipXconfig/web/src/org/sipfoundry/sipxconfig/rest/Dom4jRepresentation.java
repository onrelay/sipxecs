/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.rest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMWriter;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Dom4jRepresentation extends WriterRepresentation {
    private final org.w3c.dom.Document domDocument;

    public Dom4jRepresentation(Document dom4jDocument) {
        super(MediaType.TEXT_XML);
        this.domDocument = transformtoDOM(dom4jDocument);
    }

    @Override
    public void write(Writer writer) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(domDocument), new StreamResult(writer));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static org.w3c.dom.Document transformtoDOM(Document dom4jDocument) {
        try {
            DOMWriter writer = new DOMWriter();
            return writer.write(dom4jDocument);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}