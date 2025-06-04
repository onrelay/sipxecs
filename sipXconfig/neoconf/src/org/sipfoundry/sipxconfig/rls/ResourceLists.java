/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rls;

import static org.sipfoundry.commons.mongo.MongoConstants.BUTTONS;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.SPEEDDIAL;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;
import static org.sipfoundry.commons.mongo.MongoConstants.URI;
import static org.sipfoundry.commons.mongo.MongoConstants.USER;
import static org.sipfoundry.commons.mongo.MongoConstants.USER_CONS;
import static org.sipfoundry.sipxconfig.common.SpecialUser.SpecialUserType.XMPP_SERVER;
import static org.sipfoundry.sipxconfig.speeddial.SpeedDial.getResourceListId;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.sipfoundry.sipxconfig.dialplan.config.XmlFile;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.SipUri;

import com.mongodb.client.FindIterable;
import org.bson.Document;

public class ResourceLists {
    private static final String NAMESPACE = "http://www.sipfoundry.org/sipX/schema/xml/resource-lists-00-01";
    private CoreContext m_coreContext;
    private ValidUsers m_validUsers;

    public org.dom4j.Document getDocument(boolean xmppPresenceEnabled) {
        org.dom4j.Document document = XmlFile.FACTORY.createDocument();
        org.dom4j.Element lists = document.addElement("lists", NAMESPACE);
        org.dom4j.Element imList = null;

        FindIterable<Document> cursor = m_validUsers.getUsersWithSpeedDial();
        for ( Document user : cursor ) {
            String userName = user.get(UID).toString();
            if (userName.equals("superadmin")) {
                continue;
            }
            if (xmppPresenceEnabled && BooleanUtils.toBoolean(user.get(IM_ENABLED).toString())) {
                if (imList == null) {
                    imList = createResourceList(lists, XMPP_SERVER.getUserName());
                }
                String userAddrSpec = SipUri.format(userName, m_coreContext.getDomainName(), false);
                createResource(imList, userAddrSpec, userName);
            }
            Document speedDial = (Document) user.get(SPEEDDIAL);

            // ignore disabled orbits
            if (speedDial == null) {
                continue;
            }

            List<Document> buttons = (List<Document>) speedDial.get(BUTTONS);
            org.dom4j.Element list = null;
            if (buttons != null) {
                list = createResourceList(lists, user.get(UID).toString(), speedDial.get(USER).toString(),
                        speedDial.get(USER_CONS).toString());
                Iterator<Document> iter = buttons.iterator();
                while (iter.hasNext()) {
                    Document button = (Document) iter.next();
                    // Append "sipx-noroute=Voicemail" and "sipx-userforward=false"
                    // URI parameters to the target URI to control how the proxy forwards
                    // SUBSCRIBEs to the resource URI.
                    createResource(list, button.get(URI).toString(), button.get(NAME).toString());
                }
            }
        }
        return document;
    }

    private org.dom4j.Element createResource(org.dom4j.Element list, String uri, String name) {
        org.dom4j.Element resource = list.addElement("resource");
        // Append "sipx-noroute=Voicemail" and "sipx-userforward=false"
        // URI parameters to the target URI to control how the proxy forwards
        // SUBSCRIBEs to the resource URI.
        resource.addAttribute("uri", uri + ";sipx-noroute=VoiceMail;sipx-userforward=false");
        addNameElement(resource, name);
        return resource;
    }

    private void addNameElement(org.dom4j.Element parent, String name) {
        parent.addElement("name").setText(name);
    }

    private org.dom4j.Element createResourceList(org.dom4j.Element lists, String name, String full, String consolidated) {
        org.dom4j.Element list = lists.addElement("list");
        list.addAttribute("user", full);
        list.addAttribute("user-cons", consolidated);
        addNameElement(list, name);
        return list;
    }

    private org.dom4j.Element createResourceList(org.dom4j.Element lists, String name) {
        return createResourceList(lists, name, getResourceListId(name, false), getResourceListId(name, true));
    }

    
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public ValidUsers getValidUsers() {
        return m_validUsers;
    }

    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }
}
