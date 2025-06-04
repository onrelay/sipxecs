/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package com.pingtel.sipviewer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

public class SipBranchData {
    String method;
    String responseCode;
    String responseText;
    String sourceEntity;
    String destinationEntity;
    String sourceAddress;
    String destinationAddress;
    String timeStamp;
    long timeStampInMicroseconds;
    int timeStampThreeDigitAccuracy;
    String timeIndexDisplay;
    String transactionId;
    String frameId;
    String message;
    Vector<String> branchIds;
    String transport;

    // contains DOM items from the XML file parse operation
    static Element nodeContainer = null;

    public SipBranchData(Element xmlBranchNode) {
        // Extracting values from the XML node
        method = getChildText(xmlBranchNode, "method");
        responseCode = getChildText(xmlBranchNode, "responseCode");
        responseText = getChildText(xmlBranchNode, "responseText");
        sourceEntity = getChildText(xmlBranchNode, "source");
        destinationEntity = getChildText(xmlBranchNode, "destination");
        sourceAddress = getChildText(xmlBranchNode, "sourceAddress");
        destinationAddress = getChildText(xmlBranchNode, "destinationAddress");
        timeStamp = getChildText(xmlBranchNode, "time");

        // setting DateFormatter so that it can correctly parse the source
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        try {
            // grabbing the actual time
            Date messageDate = dateFormatter.parse(timeStamp.substring(0, 23));

            // the logs are accurate to a microsecond but java gives us
            // support only to milliseconds, we have to do some calculations
            // later on to retain the microsecond accuracy, storing in
            // microsecond format
            timeStampInMicroseconds = messageDate.getTime() * 1000;

            // this stores the complete 3 digits of microsecond value used
            // later in calculations
            timeStampThreeDigitAccuracy = Integer.parseInt(timeStamp.substring(23, 26));

            // adding the microsecond values to the overall microsecond values
            timeStampInMicroseconds += timeStampThreeDigitAccuracy;

        } catch (ParseException e) {
            // we'll end up here if the logs are corrupted
            e.printStackTrace();
        }

        // this field is used to display the time index values to the user in
        // the time index column, initial timestamp is in the form
        // yyyy-MM-ddTHH:mm:ss.SSSSSSZ we're interested only in the time (not
        // date) so we grab everything after letter T and exclude the timezone
        // Z letter
        timeIndexDisplay = timeStamp.substring(timeStamp.indexOf('T') + 1, timeStamp.length() - 1);

        transactionId = getChildText(xmlBranchNode, "transactionId");
        if (!transactionId.isEmpty()) {
            int c = transactionId.charAt(0);
            if (c == 'C' || c == 'A') {
                transactionId = transactionId.substring(1);
            }
        }

        frameId = getChildText(xmlBranchNode, "frameId");
        message = getChildText(xmlBranchNode, "message");

        // we convert the entire message string to lower case and
        // then look for the "via" tag
        int viaIndex = message.toLowerCase().indexOf("via");

        // if we found it then lets see what transport we can find
        if (viaIndex != -1) {
            // lets get the first 22 characters of the string and convert to
            // lowercase, then we will search for udp, tcp and tls
            String sub = message.substring(viaIndex + 3, Math.min(message.length(), viaIndex + 22)).toLowerCase();
            if (sub.contains("udp")) {
                // set transport to udp
                transport = "udp";
            } else if (sub.contains("tcp")) {
                // set transport to tcp
                transport = "tcp";
            } else if (sub.contains("tls")) {
                // set transport to tls
                transport = "tls";
            } else {
                // we were unable to determine
                // the transport type
                transport = "other";
            }
        } else {
            // no via tag found so lets mark
            // transport as other
            transport = "other";
        }

        branchIds = new Vector<>();
        Element branchSet = getChild(xmlBranchNode, "branchIdSet");
        if (branchSet != null) {
            NodeList elements = branchSet.getElementsByTagName("branchId");
            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    branchIds.add(node.getTextContent().trim());
                }
            }
        }
    }

    // input is the root container of the input file, it contains individual
    // XML elements that are SIP messages
    public static Vector<SipBranchData> getSipBranchDataElements(Element branchContainer) {
        Vector<SipBranchData> nodes = new Vector<>();

        // puts all the <branchNode></branchNode> sections into their own
        // individual element on the list
        NodeList elementList = branchContainer.getElementsByTagName("branchNode");

        // loop through all the elements
        for (int i = 0; i < elementList.getLength(); i++) {
            Node node = elementList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // convert the DOM object to a SipBranchData object
                // and add it to the vector that will be used as a source
                // to store ChartDescriptor elements
                nodes.add(new SipBranchData((Element) node));
            }
        }

        return nodes;
    }

    // parses the input file and stores SIP data elements in a Vector
    // which is later processed to reorder the SIP messages (in case
    // they are not in the proper chronological sequence), then each
    // vector element is added to SIP Model
    public static Vector<SipBranchData> getSipBranchDataElements(URL traceFilename) {
        try {
            // open the file and create an input stream
            URLConnection uc = traceFilename.openConnection();
            InputStream input = uc.getInputStream();

            // parse the XML file using DOM
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);

            // normalize the document
            doc.getDocumentElement().normalize();

            // get the root container from the DOM document
            nodeContainer = doc.getDocumentElement();

            // get the individual sip elements and store them in a vector which
            // will be returned as part of this method
            return getSipBranchDataElements(nodeContainer);
        } catch (Exception e) {
            e.printStackTrace();
            return new Vector<>();
        }
    }

    public String getLabel() {
        String label;
        if (isRequest())
            label = method;
        else
            label = responseCode + " " + responseText;

        return (label);
    }

    public boolean isRequest() {
        return (method != null);
    }

    public String getMethod() {
        return (method);
    }

    public String getResponseCode() {
        return (responseCode);
    }

    public String getResponseText() {
        return (responseText);
    }

    public String getSourceEntity() {
        return (sourceEntity);
    }

    public String getDestinationEntity() {
        return (destinationEntity);
    }

    public String getSourceAddress() {
        return (sourceAddress);
    }

    public String getDestinationAddress() {
        return (destinationAddress);
    }

    public String getTimeStamp() {
        return (timeStamp);
    }

    public String getTransactionId() {
        return (transactionId);
    }

    public String getFrameId() {
        return (frameId);
    }

    public String getMessage() {
        return (message);
    }

    public String getThisBranchId() {
        return (branchIds.size() > 0 ? branchIds.elementAt(0) : null);
    }

    public Vector<String> getBranchIds() {
        return (branchIds);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        if (method != null)
            buffer.append("method: " + method + "\n");
        if (responseCode != null)
            buffer.append("responseCode: " + responseCode + "\n");
        if (responseText != null)
            buffer.append("responseText: " + responseText + "\n");
        if (sourceEntity != null)
            buffer.append("sourceEntity: " + sourceEntity + "\n");
        if (destinationEntity != null)
            buffer.append("destinationEntity: " + destinationEntity + "\n");
        if (sourceAddress != null)
            buffer.append("sourceAddress: " + sourceAddress + "\n");
        if (destinationAddress != null)
            buffer.append("destinationAddress: " + destinationAddress + "\n");
        if (timeStamp != null)
            buffer.append("timeStamp: " + timeStamp + "\n");
        if (transactionId != null)
            buffer.append("transactionId: " + transactionId + "\n");
        if (frameId != null)
            buffer.append("frameId: " + frameId + "\n");
        if (message != null)
            buffer.append("message: " + message + "\n");

        if (branchIds != null) {
            for (Enumeration<String> enumerator = branchIds.elements(); enumerator.hasMoreElements();) {
                buffer.append("   branchId: " + enumerator.nextElement() + "\n");
            }
        }

        return (buffer.toString());
    }

    private static String getChildText(Element parent, String tagName) {
        Element child = getChild(parent, tagName);
        return child != null ? child.getTextContent().trim() : "";
    }

    private static Element getChild(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getParentNode().equals(parent) && node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }
}