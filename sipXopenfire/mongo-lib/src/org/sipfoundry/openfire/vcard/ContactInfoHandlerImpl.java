/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.openfire.vcard;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.sipfoundry.openfire.vcard.synchserver.ContactInfoHandler;
import org.sipfoundry.openfire.vcard.synchserver.Util;

public class ContactInfoHandlerImpl implements ContactInfoHandler {
    private static Logger logger = Logger.getLogger(ContactInfoHandlerImpl.class);

    @Override
    public void notifyContactChange(String userName) {
        try {
            VCardManager vCardManager = XMPPServer.getInstance().getVCardManager();
            
            // Load the user's current vCard from Openfire
            Element userVCard = vCardManager.getVCard(userName);

            if (userVCard == null) {
                logger.warn("No existing vCard found for user: " + userName);
                return;
            }

            logger.debug("Start synchronizing vCard for user: " + userName);

            // Update the vCard in Openfire
            vCardManager.setVCard(userName, userVCard);

            // Update the avatar and notify clients
            Util.updateAvatar(userName, userVCard);
            Util.notify(userName);

            logger.debug("Finished synchronizing vCard for user: " + userName);
        } catch (Exception e) {
            logger.error("Cannot synchronize vCard for user: " + userName, e);
        }
    }
}
