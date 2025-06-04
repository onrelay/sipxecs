/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.callcontroller;

import gov.nist.javax.sip.clientauthutils.UserCredentialHash;

import java.util.HashMap;
import java.util.Random;
import javax.sip.Dialog;

import org.apache.log4j.Logger;

import org.sipfoundry.sipxrest.RestServer;


public class SipUtils {

    private static final Logger logger = Logger.getLogger(SipUtils.class);

    private static Random rand = new Random();

    private static HashMap<String, DialogContext> dialogContextTable = new HashMap<String, DialogContext>();

    public synchronized static DialogContext createDialogContext(String key, int timeout, int cachetimeout, 
    		UserCredentialHash credentials, String method,String agent, String callingParty, String calledParty) {
        logger.debug("createDialogContext " + key);
        DialogContext dialogContext = getDialogContext(key);
        if (dialogContext != null) {
            dialogContext.remove();
        }
        dialogContext = new DialogContext(key, timeout, cachetimeout,method);
        dialogContext.setUserCredentials(credentials);
        dialogContext.setAgent(agent);
        dialogContext.setCallingParty(callingParty);
        dialogContext.setCalledParty(calledParty);
        dialogContextTable.put(key, dialogContext);

        return dialogContext;
    }

    public synchronized static void removeDialogContext(String key, DialogContext dialogContext) {
        logger.debug("removeDialogContext " + key);
        if (dialogContextTable.get(key) == dialogContext) {
            dialogContextTable.remove(key);
        }
    }

    public synchronized static DialogContext getDialogContext(String key) {
        logger.debug("getDialogContext " + key);
        return dialogContextTable.get(key);
    }

    public static String formatWithIpAddress(String format) {
        String ipAddress = RestServer.getRestServerConfig().getIpAddress();
        String sessionId = Long.toString(Math.abs(rand.nextLong()));
        return String.format(format, ipAddress, sessionId);
    }

    /**
     * We set up a timer to terminate the INVITE dialog if we do not see a 200 OK in the transfer.
     * 
     * @param dialog dialog to terminate
     */
    public static void scheduleTerminate(Dialog dialog, int timeout) {
        ReferTimerTask referTimerTask = new ReferTimerTask(dialog);
        RestServer.timer.schedule(referTimerTask, timeout * 1000);
    }

}
