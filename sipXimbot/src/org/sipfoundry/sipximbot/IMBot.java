package org.sipfoundry.sipximbot;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.stringprep.XmppStringprepException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;

import org.sipfoundry.commons.freeswitch.ConfBasicThread;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.sipximbot.IMUser.UserPresence;



public class IMBot {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipximbot");

    // in milliseconds
    private static final long RETRY_INTERVAL = 30 * 1000;
    private static final long MAX_RETRIES = 30;

    private static Roster m_roster;

    // key is Jabber Id, value is IMUser
    private static Map<String, IMUser> m_ChatsMap = Collections.synchronizedMap(new HashMap<String, IMUser>());

    private static class IMClientThread extends Thread {
        private static XMPPTCPConnection m_con;
        private static Localizer m_localizer;

        public IMClientThread() {
        }

        /*
         * IMbot client computes the SHA1 hash of the avatar image data itself. Include this hash
         * in the user's presence information as the XML character data of the <photo/> child of
         * an <x/> element qualified by the 'vcard-temp:x:update' namespace
         */
        private class AvatarUpdateExtension implements ExtensionElement {
            private String photoHash;

            public void setPhotoHash(String hash) {
                photoHash = hash;
            }

            @Override
            public String getElementName() {
                return "x";
            }

            @Override
            public String getNamespace() {
                return "vcard-temp:x:update";
            }

            @Override
            public CharSequence toXML(XmlEnvironment xmlEnvironment) {
                XmlStringBuilder xml = new XmlStringBuilder(this);
                xml.rightAngleBracket();  // closes <x xmlns="...">
                if (photoHash != null) {
                    xml.element("photo", photoHash);
                }
                xml.closeElement(this);
                return xml;
            }
        }

        private void updateAvatar() {

            boolean savingAvatar = false;
            VCard vCard = new VCard();
            try {
                vCard.load(m_con);
                URL url;
                try {
                    url = new URL("file://" + System.getProperty("var.dir", "/var/sipxdata")
                        + "/sipximbot/image/avatar.jpg");
                    vCard.setAvatar(url);
                    savingAvatar = true;
                    vCard.save(m_con);
                    updateAvatarPresence(vCard);
                } catch (MalformedURLException | XmppStringprepException e) {
                    LOG.error("Malformed URL in updateAvatar ");
                }

            } catch ( XMPPException | NoResponseException | NotConnectedException | InterruptedException e ) {
                if (savingAvatar) {
                    // google talk doesn't like us saving avatars immediately after login!
                    try {
                        sleep(10000);
                    } catch (InterruptedException e2 ) {

                    }
                    try {
                        vCard.save(m_con);
                        updateAvatarPresence(vCard);
                    } catch (XMPPException | NoResponseException | XmppStringprepException | NotConnectedException | InterruptedException e2 ) {
                        LOG.error("could not update Avatar " + e2.getMessage());
                    }
                }
            }
        }

        private void updateAvatarPresence(VCard vCard) throws XmppStringprepException, NotConnectedException, InterruptedException {
            Presence aPresence = new Presence(Presence.Type.available);
            AvatarUpdateExtension AvatarExt = new AvatarUpdateExtension();
            AvatarExt.setPhotoHash(vCard.getAvatarHash());
            aPresence.addExtension(AvatarExt);
            aPresence.setStatus(m_localizer.localize("cmd_help"));
            aPresence.setFrom(JidCreate.entityBareFrom(ImbotConfiguration.get().getMyAsstAcct()));
            m_con.sendStanza(aPresence);
        }

        private static boolean connectToXMPPServer() {
            ImbotConfiguration config = ImbotConfiguration.get();

            try {
                // Create service JID from the configured host
                DomainBareJid serviceName = JidCreate.domainBareFrom(config.getOpenfireHost());

                XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(serviceName)                            // domain part of JID
                    .setHost(config.getOpenfireHost())                     // actual host/IP
                    .setPort(5222)                                         // or config.getPort()
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.ifpossible)
                    .build();

                Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

                m_con = new XMPPTCPConnection(conf);

            } catch (Exception e) {
                throw new RuntimeException("Error building XMPP connection configuration", e);
            }

            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    // Extract username (localpart of JID)
                    String rawUser = config.getMyAsstAcct();
                    String username = rawUser.contains("@") ? rawUser.split("@")[0] : rawUser;

                    // Build proper Localpart
                    Localpart localpart = Localpart.from(username);

                    m_con.login(localpart, config.getMyAsstPswd());

                    return true;
                } catch (Exception e) {
                    LOG.error("Could not login to XMPP server ", e);
                }

                try {
                    LOG.info(String.format(
                        "Waiting %d seconds before attempting another connection to XMPP server.",
                        RETRY_INTERVAL / 1000));
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException e) {
                    return false;
                }
            }

            throw new RuntimeException("Could not establish connection to XMPP server after " + MAX_RETRIES + " attempts");
        }

        public static void addToRoster(User user) throws XmppStringprepException, NotConnectedException, InterruptedException {
            Presence presPacket = new Presence(Presence.Type.subscribe);
            presPacket.setFrom(JidCreate.entityBareFrom(ImbotConfiguration.get().getMyAsstAcct()));

            if (user.getJid() != null) {
                presPacket.setTo( JidCreate.entityBareFrom(user.getJid()));
                m_con.sendStanza(presPacket);
            }

            if (user.getAltJid() != null) {
                presPacket.setTo(JidCreate.entityBareFrom(user.getAltJid()));
                m_con.sendStanza(presPacket);
            }
        }

        @Override
        public void run() {

            boolean running = true;

            if (!connectToXMPPServer()) {
                return;
            }

            m_localizer = new Localizer();

            updateAvatar();
            m_roster = Roster.getInstanceFor(m_con);
            class PresenceListener implements StanzaListener {

                @Override
                public void processStanza(Stanza packet) {

                    try {
                        Presence presence = (Presence) packet;
                        Presence presPacket = null;

                        switch (presence.getType()) {
                        case subscribe:
                            String jid = presence.getFrom().toString();
                            if (jid.indexOf('/') > 0) {
                                jid = jid.substring(0, jid.indexOf('/'));
                            }

                            User user = findUser(jid);
                            if (user == null) {
                                LOG.error("Rejected subscription from " + jid);
                                presPacket = new Presence(Presence.Type.unsubscribed);
                            } else {
                                LOG.info("Accepted subscription from " + jid);
                                presPacket = new Presence(Presence.Type.subscribed);
                            }
                            presPacket.setTo(presence.getFrom());
                            presPacket.setFrom(presence.getTo());

                            m_con.sendStanza(presPacket);

                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                            }

                            if ((user != null) && (m_ChatsMap.get(jid) == null)) {
                                // now subscribe the sender's presence
                                presPacket.setType(Presence.Type.subscribe);

                                m_con.sendStanza(presPacket);

                                IMUser imuser = new IMUser(user, jid, null, m_con, m_localizer);
                                m_ChatsMap.put(jid, imuser);
                            }

                            break;
                        case unsubscribe:
                            LOG.error("Received unexpected unsubscribe for " + presence.getFrom());
                            break;
                        }
                    } catch( NotConnectedException | InterruptedException e ) {
                        LOG.error("Not connected processing packet: " + packet, e );
                    }
                }
            }

            StanzaFilter filter = new StanzaTypeFilter(Presence.class);
            m_con.addAsyncStanzaListener(new PresenceListener(), filter);

            // create map with initial presence and status info

            User user;
            Collection<RosterEntry> entries = m_roster.getEntries();
            for (RosterEntry entry : entries) {
                user = findUser(entry.getUser());
                if (user != null) {
                    try {
                        IMUser imuser = new IMUser(user, 
                            entry.getUser(), 
                            m_roster.getPresence(JidCreate.entityBareFrom(entry.getUser())), 
                            m_con,
                            m_localizer);
                        m_ChatsMap.put(entry.getUser(), imuser);
                    } catch( XmppStringprepException e ) {
                        LOG.error("Invalid user JID: " + entry.getUser());
                    }
                } else {
                    try {
                        m_roster.removeEntry(entry);
                        LOG.error("Removing old roster entry " + entry.getUser());
                    } catch (XMPPException | NotLoggedInException | NoResponseException | NotConnectedException | InterruptedException e) {
                        LOG.error("Could not remove roster entry " + entry.getUser());
                    }
                }
            }

            m_roster.addRosterListener(new RosterListener() {

                @Override
                public void entriesAdded(Collection<Jid> entries) {
                    for (Jid jid : entries) {
                        String address = jid.asBareJid().toString();

                        if (m_ChatsMap.get(address) != null) {
                            // already in chat map, this happens if subscription was successful
                            continue;
                        }

                        User user = findUser(address);
                        if (user == null) {
                            LOG.error("Rejected addition from " + address);
                        } else {
                            IMUser imuser = new IMUser(user, address, null, m_con, m_localizer);
                            m_ChatsMap.put(address, imuser);
                            LOG.debug("Entry added: " + address);
                        }
                    }
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    // Contacts have been removed from the roster
                    for (Jid jid : addresses) {
                        String address = jid.asBareJid().toString();
                        for (RosterEntry entry : m_roster.getEntries()) {
                            if (address.equals(entry.getJid().asBareJid().toString())) {
                                LOG.debug("Removing from roster: " + entry.getJid());
                                m_ChatsMap.remove(address);
                            }
                        }
                    }
                }

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    // No-op for now
                }

                @Override
                public void presenceChanged(Presence presence) {
                    String from = presence.getFrom().asBareJid().toString();
                    IMUser imuser = m_ChatsMap.get(from);
                    if (imuser != null) {
                        imuser.setPresence(presence);
                    }
                }
            });
            while (running) {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
            m_con.disconnect();
        }
    }

    static public synchronized User findUser(String jid) {
        return (FullUsers.INSTANCE.findByjid(jid));
    }

    static private String getjid(User user) {
        String jid = user.getJid();
        if (jid == null) {
            jid = user.getAltJid();
        }
        return jid;
    }

    static public String getUserStatus(User user) {
        // return status corresponding to primary IM Id. If not filled in
        // try altId and if not filled in either then assume AVAILABLE

        try {
            String jid = getjid(user);
            if (jid == null) {
                return null;
            }

            Presence pres = m_roster.getPresence(JidCreate.entityBareFrom(jid));

            if (pres != null) {
                return pres.getStatus();
            }
        }
        catch( XmppStringprepException e ) {
            LOG.error("Invalid user: " + user);
        }

        return null;
    }

    static public UserPresence getUserPresence(User user) {
        // return presence corresponding to primary IM Id. If not filled in
        // try altId and if not filled in either then assume AVAILABLE

        try {
            if (ConfBasicThread.inConferenceSince(user.getUserName()) != null) {
                return UserPresence.INCONFERENCE;
            }

            String jid = getjid(user);
            if (jid == null) {
                return UserPresence.AVAILABLE;
            }
            Presence pres = m_roster.getPresence(JidCreate.entityBareFrom(jid));

            if (pres == null) {
                return UserPresence.UNKNOWN;
            }

            if (pres.getType() == Presence.Type.unavailable) {
                return UserPresence.UNKNOWN;
            }

            Presence.Mode mode = pres.getMode();
            if (mode == null) {
                return UserPresence.AVAILABLE;
            }

            if (mode == Presence.Mode.away) {
                return UserPresence.AWAY;
            }

            if (mode == Presence.Mode.chat) {
                return UserPresence.AVAILABLE;
            }

            if (mode == Presence.Mode.dnd) {
                return UserPresence.BUSY;
            }

            if (mode == Presence.Mode.xa) {
                return UserPresence.AWAY;
            }
        }
        catch( XmppStringprepException e ) {
            LOG.error("Invalid user: " + user);
        }

        return UserPresence.UNKNOWN;

    }

    public static void sendIM(User user, String msg) {
        if (user != null) {
            IMUser toIMUser;

            String jid = user.getJid();
            if (jid != null) {
                toIMUser = m_ChatsMap.get(jid);
                if (toIMUser != null) {
                    toIMUser.sendIM(msg);
                }
            }

            jid = user.getAltJid();
            if (jid != null) {
                toIMUser = m_ChatsMap.get(jid);
                if (toIMUser != null) {
                    toIMUser.sendIM(msg);
                }
            }
        }
    }

    static public IMUser getIMUser(User user) {
        String jid = getjid(user);
        if (jid == null) {
            return null;
        }
        return m_ChatsMap.get(jid);
    }

    static public void SendReturnCallIM(User toUser, User fromUser, String callingName, String callingNumber) {

        IMUser toIMuser = getIMUser(toUser);
        IMUser fromIMuser = getIMUser(fromUser);

        if (toIMuser != null && fromIMuser != null) {
            toIMuser.setCallingIMUser(fromIMuser);

            toIMuser.sendIM(callingName + " (" + callingNumber + ") called and would like you to call back");
        }
    }

    public static void addToRoster(User user) throws XmppStringprepException, NotConnectedException, InterruptedException {
        IMClientThread.addToRoster(user);
    }

    static public void init() {
        IMClientThread imThread = new IMClientThread();
        imThread.start();
    }
}
