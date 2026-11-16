/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin.presence;

import java.time.Duration;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.TaskEngine;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

/**
 * Singleton object that is meant to collect all the presence-related information related the
 * openfire users on the system. More specifically, by virtue of being a PresenceEventListener, it
 * will receive notifications whenever openfire users change their XMPP presence or status message
 * via their XMPP clients.
 *
 * This object aggregates the XMPP presence and status into a 'unified presence' which can be
 * queried via the getUnifiedPresence() method. Every change is persisted into the shared
 * imdb.unifiedPresence mongo collection so that other sipXecs components (for example the
 * registrar presence routing plugin) can act on IM presence.
 */
public class PresenceUnifier implements PresenceEventListener {
    private static PresenceUnifier instance = null;
    private static SipXOpenfirePlugin sipXopenfirePlugin = null;
    private static Logger log = Logger.getLogger(PresenceUnifier.class);
    private final Map<String, UnifiedPresence> unifiedPresenceMap = new ConcurrentHashMap<String, UnifiedPresence>();
    private MongoUnifiedPresenceWriter presenceWriter;
    private final TimerTask refreshTask;

    public static void setPlugin(SipXOpenfirePlugin plugin) {
        sipXopenfirePlugin = plugin;
    }

    public static PresenceUnifier getInstance() {
        if (instance == null) {
            instance = new PresenceUnifier();
        }
        return instance;
    }

    public UnifiedPresence getUnifiedPresence(String xmppUsername) throws UserNotFoundException {
        UnifiedPresence unifiedPresence = unifiedPresenceMap.get(xmppUsername);
        if (unifiedPresence == null) {
            unifiedPresence = new UnifiedPresence(xmppUsername);
            unifiedPresenceMap.put(xmppUsername, unifiedPresence);
            User ofUser = XMPPServer.getInstance().getUserManager().getUser(xmppUsername);
            Presence presence = XMPPServer.getInstance().getPresenceManager().getPresence(ofUser);
            if (presence != null) {
                unifiedPresence.setXmppPresence(presence);
            }
        }
        return unifiedPresence;
    }

    public void setUnifiedPresence(String xmppUsername, UnifiedPresence.XmppPresence presence) {
        UnifiedPresence unifiedPresence;
        unifiedPresence = unifiedPresenceMap.get(xmppUsername);
        if (unifiedPresence == null) {
            unifiedPresence = new UnifiedPresence(xmppUsername);
            unifiedPresenceMap.put(xmppUsername, unifiedPresence);
        }
        unifiedPresence.setXmppPresence(presence);
        persistToMongo(unifiedPresence);
    }

    // Notification that XMPP presence changed
    @Override
    public void presenceChanged(ClientSession session, Presence presence) {
        try {
            presenceChanged(session.getUsername(), presence);
        } catch (UserNotFoundException ex) {
            log.debug("presenceChanged caught " + ex.getMessage());
        }
    }

    // Notification that XMPP presence changed
    public void presenceChanged(String xmppUsername, Presence presence) {
        UnifiedPresence unifiedPresence = handleXmppPresenceChange(xmppUsername, presence);
        log.debug("presenceChanged[2] for " + xmppUsername + " new presence show: " + presence.getShow()
                + " new presence type: " + presence.getType() + " new presence status: " + presence.getStatus()
                + " new unified presence: " + unifiedPresence.getUnifiedPresence());
    }

    @Override
    public void availableSession(ClientSession session, Presence presence) {
        try {
            UnifiedPresence unifiedPresence = handleXmppPresenceChange(session.getUsername(), presence);
            log.debug("availableSession for " + session.getUsername() + " new presence show: " + presence.getShow()
                    + " new presence type: " + presence.getType() + " new presence status: " + presence.getStatus()
                    + " new unified presence: " + unifiedPresence.getUnifiedPresence());
        } catch (UserNotFoundException ex) {
            log.debug("unavailableSession caught " + ex.getMessage());
        }
    }

    @Override
    public void unavailableSession(ClientSession session, Presence presence) {
        try {
            UnifiedPresence unifiedPresence = handleXmppPresenceChange(session.getUsername(), presence);
            log.debug("unavailableSession for " + session.getUsername() + " new presence show: "
                    + presence.getShow() + " new presence type: " + presence.getType() + " new presence status: "
                    + presence.getStatus() + " new unified presence: " + unifiedPresence.getUnifiedPresence());
        } catch (UserNotFoundException ex) {
            log.debug("unavailableSession caught " + ex.getMessage());
        }
    }

    @Override
    public void subscribedToPresence(JID subscriberJID, JID authorizerJID) {
        // nothing to do
    }

    @Override
    public void unsubscribedToPresence(JID subscriberJID, JID authorizerJID) {
        // nothing to do
    }

    private UnifiedPresence handleXmppPresenceChange(String xmppUsername, Presence presence) {
        UnifiedPresence unifiedPresence;
        unifiedPresence = unifiedPresenceMap.get(xmppUsername);
        if (unifiedPresence == null) {
            unifiedPresence = new UnifiedPresence(xmppUsername);
            unifiedPresenceMap.put(xmppUsername, unifiedPresence);
        }

        String newXmppStatusMessage = presence.getStatus();
        unifiedPresence.setXmppPresence(presence);
        unifiedPresence.setXmppStatusMessage(newXmppStatusMessage);
        persistToMongo(unifiedPresence);
        return unifiedPresence;
    }

    private void persistToMongo(UnifiedPresence unifiedPresence) {
        if (sipXopenfirePlugin == null) {
            log.debug("persistToMongo skipped: plugin not yet initialized");
            return;
        }
        try {
            String sipUserName = sipXopenfirePlugin.getSipId(unifiedPresence.getXmppUsername());
            if (sipUserName == null || sipUserName.isEmpty()) {
                // openfire user without an associated SIP identity - nothing to publish
                return;
            }
            String sipIdentity = sipUserName + "@" + sipXopenfirePlugin.getSipDomain();
            getPresenceWriter().write(sipIdentity, unifiedPresence.getJidAsString(), unifiedPresence);
        } catch (UserNotFoundException ex) {
            log.debug("persistToMongo could not map xmpp user to sip identity: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("persistToMongo failed", ex);
        }
    }

    private synchronized MongoUnifiedPresenceWriter getPresenceWriter() throws Exception {
        if (presenceWriter == null) {
            presenceWriter = new MongoUnifiedPresenceWriter();
        }
        return presenceWriter;
    }

    // Re-stamps the mongo documents of all actively tracked users so readers can
    // distinguish live presence from stale left-overs of a crashed openfire.
    private void refreshPresenceTimestamps() {
        for (UnifiedPresence unifiedPresence : unifiedPresenceMap.values()) {
            persistToMongo(unifiedPresence);
        }
    }

    public void shutdown() {
        TaskEngine.getInstance().cancelScheduledTask(refreshTask);
    }

    private PresenceUnifier() {
        PresenceEventDispatcher.addListener(this);
        refreshTask = new TimerTask() {
            @Override
            public void run() {
                refreshPresenceTimestamps();
            }
        };
        TaskEngine.getInstance().schedule(refreshTask,
                Duration.ofMillis(MongoUnifiedPresenceWriter.PRESENCE_REFRESH_PERIOD_MILLIS),
                Duration.ofMillis(MongoUnifiedPresenceWriter.PRESENCE_REFRESH_PERIOD_MILLIS));
    }
}
