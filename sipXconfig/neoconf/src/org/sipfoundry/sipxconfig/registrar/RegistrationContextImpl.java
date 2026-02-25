/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.registrar;

import static org.sipfoundry.commons.mongo.MongoConstants.CALL_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.EXPIRATION_TIME;
import static org.sipfoundry.commons.mongo.MongoConstants.INSTRUMENT;
import static org.sipfoundry.commons.mongo.MongoConstants.REG_CONTACT;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.RegistrationItem;
import org.sipfoundry.sipxconfig.commserver.imdb.TimeRegistrationStatistics;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.conversions.Bson;

public class RegistrationContextImpl implements RegistrationContext {
    public static final Log LOG = LogFactory.getLog(RegistrationContextImpl.class);
    private static final String DB_COLLECTION_NAME = "registrar";
    private static final String DB_STATISTICS_COLLECTION_NAME = "timeRegistrationStatistics";
    private static final String EXPIRED = "expired";
    private static final String IDENTITY = "identity";
    private static final String URI = "uri";
    private static final String LOCAL_ADDRESS = "localAddress";
    private static final String PATTERN_ALL = ".*";
    private MongoTemplate m_nodedb;
    private DomainManager m_domainManager;

    /**
     * @see org.sipfoundry.sipxconfig.registrar.RegistrationContext#getRegistrations()
     */
    @Override
    public List<RegistrationItem> getRegistrations() {
        return parseRegistrations(getRegistrarCollection().find(getRegistrationsQuery()));
    }

    @Override
    public long getRegistrationsCount() {
        return getRegistrarCollection().countDocuments(getRegistrationsQuery());
    }
    
    @Override
    public List<RegistrationItem> getRegistrations(Integer start, Integer count) {
        return parseRegistrations(getRegistrarCollection().find(getRegistrationsQuery())
                .sort(new Document(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByUser(User user) {
        return parseRegistrations(getRegistrarCollection().find(getUserQuery(user)));
    }
    
    @Override
    public List<RegistrationItem> getRegistrationsByUsers(Collection<User> users) {
        return parseRegistrations(getRegistrarCollection().find(getUsersQuery(users)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByUser(User user, Integer start, Integer count) {
        return parseRegistrations(getRegistrarCollection().find(getUserQuery(user))
                .sort(new Document(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }
    
    @Override
    public List<RegistrationItem> getRegistrationsByUsers(Collection<User> users, Integer start, Integer count) {
        return parseRegistrations(getRegistrarCollection().find(getUsersQuery(users))
                .sort(new Document(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByLineId(String line) {
        return parseRegistrations(getRegistrarCollection().find(getLineQuery(line)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByMac(String mac) {
        return parseRegistrations(getRegistrarCollection().find(getMacQuery(mac)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByIp(String ip) {
        return parseRegistrations(getRegistrarCollection().find(getIpQuery(ip)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByServer(String server) {
        return parseRegistrations(getRegistrarCollection().find(getServerQuery(server)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByServer(String server, Integer start, Integer limit) {
        return parseRegistrations(getRegistrarCollection().find(getServerQuery(server))
                .sort(new Document(EXPIRATION_TIME, -1)).skip(start).limit(limit));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByCallId(String callId) {
        return parseRegistrations(getRegistrarCollection().find(getCallIdQuery(callId)));
    }

    @Override
    public void dropRegistrationsByUser(User user) {
        getRegistrarCollection().deleteMany(getUserQuery(user));
    }
    
    @Override
    public void dropRegistrationsByMac(String mac) {
        getRegistrarCollection().deleteMany(getMacQuery(mac));
    }
    
    @Override
    public void dropRegistrationsByIp(String ip) {
        getRegistrarCollection().deleteMany(getIpQuery(ip));
    }
    
    @Override
    public void dropRegistrationsByServer(String server) {
        getRegistrarCollection().deleteMany(getServerQuery(server));
    }
    
    @Override
    public void dropRegistrationsByCallId(String callId) {
        getRegistrarCollection().deleteMany(getCallIdQuery(callId));
    }

    @Override
    public FindIterable<Document> getMongoDbCursorRegistrationsByLineId(String line) {
        return getRegistrarCollection().find(getLineQuery(line));
    }

    @Override
    public FindIterable<Document> getMongoDbCursorRegistrationsByMac(String mac) {
        return getRegistrarCollection().find(getMacQuery(mac));
    }

    @Override
    public FindIterable<Document> getMongoDbCursorRegistrationsByIp(String ip) {
        return getRegistrarCollection().find(getIpQuery(ip));
    }
    
    @Override
    public void saveTimeRegistrationStatistics(TimeRegistrationStatistics trs) {

        Document document = new Document()
            .append("m_time", trs.getTime())
            .append("m_total", trs.getTotal())
            .append("m_active", trs.getActive());

        getTimeRegStatCollection().insertOne(document);
    }

    
    @Override
    public List<TimeRegistrationStatistics> getTimeRegStats() {
        List<Document> statsDbObjects = getTimeRegStatCollection().find().into(new ArrayList<>());
        List<TimeRegistrationStatistics> listStats = new ArrayList<TimeRegistrationStatistics>();
        TimeRegistrationStatistics timeRegStats = null;
        for (Document object : statsDbObjects) {
            timeRegStats = new TimeRegistrationStatistics();
            timeRegStats.setActive((Integer)object.get("m_active"));
            timeRegStats.setTime((Date)object.get("m_time"));
            timeRegStats.setTotal((Integer)object.get("m_total"));
            listStats.add(timeRegStats);
        }
        return listStats;
    }
    
    @Override
    public void operateTimeRegistrationStatistics() {
        LOG.debug("Run registration statistics: ");
        long startRenderingTime = System.currentTimeMillis() / DateUtils.MILLIS_PER_SECOND;
        RegistrationMetrics metrics = new RegistrationMetrics();
        metrics.setRegistrations(getRegistrations());
        metrics.setStartTime(startRenderingTime);
        int activeRegistrations = metrics.getActiveRegistrationCount();
        int totalRegistrations = metrics.getTotalRegistrationCount();
        TimeRegistrationStatistics trs = new TimeRegistrationStatistics();
        trs.setActive(activeRegistrations);
        trs.setTotal(totalRegistrations);
        trs.setTime(Calendar.getInstance().getTime());
        saveTimeRegistrationStatistics(trs);
        if (totalRegistrations > 1440) {
            Document oldest = getTimeRegStatCollection()
                .find()
                .limit(1)
                .first();

            if (oldest != null) {
                getRegistrarCollection().deleteOne(Filters.eq("_id", oldest.getObjectId("_id")));
            }
        }
        LOG.debug("Finished running registration statistics");
    }

    private static List<RegistrationItem> parseRegistrations(FindIterable<Document> cursor ) {
        List<RegistrationItem> regisrationItems = new ArrayList<RegistrationItem>();
        for( Document document : cursor ) {
            RegistrationItem registrationItem = new RegistrationItem();
            registrationItem.setContact((String) document.get(REG_CONTACT));
            // Handle LOCAL_ADDRESS safely even if null or missing
            String localAddress = document.getString(LOCAL_ADDRESS);
            registrationItem.setPrimary(StringUtils.substringBefore(localAddress == null ? "" : localAddress, "/"));

            Object expires = document.get(EXPIRATION_TIME);
            if (expires instanceof Date) {
                registrationItem.setExpires((Date) expires);
            } else if (expires instanceof Number) {
                registrationItem.setExpires(new Date(((Number) expires).longValue()));
            } else {
                registrationItem.setExpires(new Date()); // Fallback
            }
            registrationItem.setUri((String) document.get(URI));
            registrationItem.setInstrument((String) document.get(INSTRUMENT));
            registrationItem.setRegCallId((String) document.get(CALL_ID));
            registrationItem.setIdentity((String) document.get(IDENTITY));
            regisrationItems.add(registrationItem);
        }
        return regisrationItems;
    }

    private Bson getRegistrationsQuery() {
        return Filters.eq(EXPIRED, false);
    }

    private Bson getUserQuery(User user) {
        String identity = user.getIdentity(m_domainManager.getDomainName());
        return Filters.and(Filters.eq(IDENTITY, identity), Filters.eq(EXPIRED, false));
    }
    
    private Bson getUsersQuery(Collection<User> users) {
        List<String> identities = new ArrayList<>();
        for (User user : users) {
            identities.add(user.getIdentity(m_domainManager.getDomainName()));
        }
        return Filters.and(Filters.in(IDENTITY, identities), Filters.eq(EXPIRED, false));
    }

    private Bson getLineQuery(String line) {
        Pattern linePattern = Pattern.compile("sip:" + line + "@.*");
        return Filters.and(Filters.regex(URI, linePattern), Filters.eq(EXPIRED, false));
    }

    private Bson getMacQuery(String mac) {
        return Filters.and(Filters.eq(INSTRUMENT, mac), Filters.eq(EXPIRED, false));
    }

    private Bson getIpQuery(String ip) {
        Pattern ipPattern = Pattern.compile(PATTERN_ALL + ip + PATTERN_ALL);
        return Filters.and(Filters.regex("binding", ipPattern), Filters.eq(EXPIRED, false));
    }

    private MongoCollection<Document> getRegistrarCollection() {
        MongoDatabase db = m_nodedb.getDb();
        return db.getCollection(DB_COLLECTION_NAME);
    }
    
    private MongoCollection<Document> getTimeRegStatCollection() {
        MongoDatabase db = m_nodedb.getDb();
        return db.getCollection(DB_STATISTICS_COLLECTION_NAME);
    }

    private Bson getServerQuery(String server) {
        Pattern serverPattern = Pattern.compile(PATTERN_ALL + server + PATTERN_ALL);
        return Filters.and(Filters.regex(LOCAL_ADDRESS, serverPattern), Filters.eq(EXPIRED, false));
    }

    private Bson getCallIdQuery(String callId) {
        return Filters.eq(CALL_ID, callId);
    }

    public MongoTemplate getNodedb() {
        return m_nodedb;
    }

    public void setNodedb(MongoTemplate nodedb) {
        m_nodedb = nodedb;
    }

    public void setDomainManager(DomainManager mgr) {
        m_domainManager = mgr;
    }
}
