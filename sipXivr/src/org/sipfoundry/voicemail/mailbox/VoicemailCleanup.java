/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.voicemail.mailbox;

import static org.sipfoundry.commons.mongo.MongoConstants.DAYS_TO_KEEP_VM;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.userdb.ValidUsers;

import com.mongodb.client.FindIterable;
import org.bson.Document;

public class VoicemailCleanup {

    private static final int DISABLE_VOICEMAIL_CLEANUP = 0;
    private static final Log LOG = LogFactory.getLog(VoicemailCleanup.class);
    private MailboxManager m_mailboxManager;
    private ValidUsers m_validUsers;

    public void run() {
        LOG.warn("Starting Voicemail cleanup");
        FindIterable<Document> users = m_validUsers.getUsers();
        for( Document user : users ) {
            String userName = ValidUsers.getStringValue(user, UID);
            Integer daysToKeepVM = ValidUsers.getIntegerValue(user, DAYS_TO_KEEP_VM);
            if (daysToKeepVM != null && daysToKeepVM != DISABLE_VOICEMAIL_CLEANUP) {
                LOG.debug(String.format("Cleanup voicemail for user %s ", userName));
                m_mailboxManager.cleanupMailbox(userName, daysToKeepVM);
            }
        }
        LOG.warn("Finished Voicemail cleanup");
    }

    
    public void setMailboxManager(MailboxManager mailboxManager) {
        m_mailboxManager = mailboxManager;
    }

    
    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }
}
