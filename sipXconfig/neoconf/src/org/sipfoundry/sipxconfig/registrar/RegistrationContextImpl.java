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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.userdb.profile.UserProfile;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.RegistrationItem;
import org.sipfoundry.sipxconfig.commserver.imdb.TimeRegistrationStatistics;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

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
        return getItems(getRegistrarCollection().find(getRegistrationsQuery()));
    }

    @Override
    public long getRegistrationsCount() {
        return getRegistrarCollection().find(getRegistrationsQuery()).size();
    } 
    
    @Override
    public List<RegistrationItem> getRegistrations(Integer start, Integer count) {
        return getItems(getRegistrarCollection().find(getRegistrationsQuery())
                .sort(new BasicDBObject(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByUser(User user) {
        return getItems(getRegistrarCollection().find(getUserQuery(user)));
    }
    
    @Override
    public List<RegistrationItem> getRegistrationsByUsers(Collection<User> users) {
        return getItems(getRegistrarCollection().find(getUsersQuery(users)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByUser(User user, Integer start, Integer count) {
        return getItems(getRegistrarCollection().find(getUserQuery(user))
                .sort(new BasicDBObject(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }
    
    @Override
    public List<RegistrationItem> getRegistrationsByUsers(Collection<User> users, Integer start, Integer count) {
        return getItems(getRegistrarCollection().find(getUsersQuery(users))
                .sort(new BasicDBObject(EXPIRATION_TIME, -1)).skip(start).limit(count));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByLineId(String line) {
        return getItems(getRegistrarCollection().find(getLineQuery(line)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByMac(String mac) {
        return getItems(getRegistrarCollection().find(getMacQuery(mac)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByIp(String ip) {
        return getItems(getRegistrarCollection().find(getIpQuery(ip)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByServer(String server) {
        return getItems(getRegistrarCollection().find(getServerQuery(server)));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByServer(String server, Integer start, Integer limit) {
        return getItems(getRegistrarCollection().find(getServerQuery(server))
                .sort(new BasicDBObject("expirationTime", -1)).skip(start).limit(limit));
    }

    @Override
    public List<RegistrationItem> getRegistrationsByCallId(String callId) {
        return getItems(getRegistrarCollection().find(getCallIdQuery(callId)));
    }

    @Override
    public void dropRegistrationsByUser(User user) {
        getRegistrarCollection().remove(getUserQuery(user));

    }

    @Override
    public void dropRegistrationsByMac(String mac) {
        getRegistrarCollection().remove(getMacQuery(mac));
    }

    @Override
    public void dropRegistrationsByIp(String ip) {
        getRegistrarCollection().remove(getIpQuery(ip));
    }

    @Override
    public void dropRegistrationsByServer(String server) {
        getRegistrarCollection().remove(getServerQuery(server));
    }

    @Override
    public void dropRegistrationsByCallId(String callId) {
        getRegistrarCollection().remove(getCallIdQuery(callId));
    }

    @Override
    public DBCursor getMongoDbCursorRegistrationsByLineId(String line) {
        return getRegistrarCollection().find(getLineQuery(line));
    }

    @Override
    public DBCursor getMongoDbCursorRegistrationsByMac(String mac) {
        return getRegistrarCollection().find(getMacQuery(mac));
    }

    @Override
    public DBCursor getMongoDbCursorRegistrationsByIp(String ip) {
        return getRegistrarCollection().find(getIpQuery(ip));
    }
    
    @Override
    public void saveTimeRegistrationStatistics(TimeRegistrationStatistics trs) {
        m_nodedb.save(trs);
    }
    
    @Override
    public long getTimeRegStatCount() {
        return getTimeRegStatCollection().count();
    }
    
    @Override
    public List<TimeRegistrationStatistics> getTimeRegStats() {
        List<DBObject> statsDbObjects = getTimeRegStatCollection().find().toArray();
        List<TimeRegistrationStatistics> listStats = new ArrayList<TimeRegistrationStatistics>();
        TimeRegistrationStatistics timeRegStats = null;
        for (DBObject object : statsDbObjects) {
            timeRegStats = new TimeRegistrationStatistics();
            timeRegStats.setActive((Integer)object.get("m_active"));
            timeRegStats.setTime((Date)object.get("m_time"));
            timeRegStats.setTotal((Integer)object.get("m_total"));
            listStats.add(timeRegStats);
        }
        return listStats;
    }
    
    
    public void operateTimeRegistrationStatistics() {
        LOG.debug("Run registration statistics: ");
        long startRenderingTime = System.currentTimeMillis() / DateUtils.MILLIS_PER_SECOND;
        RegistrationMetrics metrics = new RegistrationMetrics();
        metrics.setRegistrations(getRegistrations());
        metrics.setStartTime(startRenderingTime);
        int activeRegistrations = metrics.getActiveRegistrationCount();
        int totalRegistrations = metrics.getUniqueRegistrations().size();
        TimeRegistrationStatistics trs = new TimeRegistrationStatistics();
        trs.setActive(activeRegistrations);
        trs.setTotal(totalRegistrations);
        trs.setTime(Calendar.getInstance().getTime());
        saveTimeRegistrationStatistics(trs);
        if (getTimeRegStatCount() > 1440) {
            getTimeRegStatCollection().remove(getTimeRegStatCollection().findOne());
        }
        LOG.debug("Finished running registration statistics");
    }

    private static List<RegistrationItem> getItems(DBCursor cursor) {
        List<RegistrationItem> items = new ArrayList<RegistrationItem>(cursor.size());
        while (cursor.hasNext()) {
            DBObject registration = cursor.next();
            RegistrationItem item = new RegistrationItem();
            item.setContact((String) registration.get(REG_CONTACT));
            item.setPrimary(StringUtils.substringBefore((String) registration.get(LOCAL_ADDRESS), "/"));
            // handle change from integer type to long in expiration time
            Object expires = registration.get(EXPIRATION_TIME);
            if (expires instanceof Date) {
                item.setExpires((Date) expires);
            } else {
                item.setExpires(new Date());
            }
            item.setUri((String) registration.get(URI));
            item.setInstrument((String) registration.get(INSTRUMENT));
            item.setRegCallId((String) registration.get(CALL_ID));
            item.setIdentity((String) registration.get(IDENTITY));
            items.add(item);
        }
        return items;
    }

    private DBObject getRegistrationsQuery() {
        return QueryBuilder.start(EXPIRED).is(Boolean.FALSE).get();
    }

    private DBObject getUserQuery(User user) {
        return QueryBuilder.start(IDENTITY).is(user.getIdentity(m_domainManager.getDomainName())).and(EXPIRED)
                .is(Boolean.FALSE).get();
    }
    
    private DBObject getUsersQuery(Collection<User> users) {
    	List<String> identities = new ArrayList<String>();
    	for (User user : users) {
    		identities.add(user.getIdentity(m_domainManager.getDomainName()));
    	}
        return QueryBuilder.start(IDENTITY).in(identities).and(EXPIRED)
                .is(Boolean.FALSE).get();
    }

    private DBObject getLineQuery(String line) {
        Pattern linePattern = Pattern.compile("sip:" + line + "@.*");
        return QueryBuilder.start(URI).regex(linePattern).and(EXPIRED).is(Boolean.FALSE).get();
    }

    private DBObject getMacQuery(String mac) {
        return QueryBuilder.start(INSTRUMENT).is(mac).and(EXPIRED).is(Boolean.FALSE).get();
    }

    private DBObject getIpQuery(String ip) {
        Pattern ipPattern = Pattern.compile(PATTERN_ALL + ip + PATTERN_ALL);
        return QueryBuilder.start("binding").regex(ipPattern).and(EXPIRED).is(Boolean.FALSE).get();
    }

    private DBCollection getRegistrarCollection() {
        DB datasetDb = m_nodedb.getDb();
        return datasetDb.getCollection(DB_COLLECTION_NAME);
    }
    
    private DBCollection getTimeRegStatCollection() {
        DB datasetDb = m_nodedb.getDb();
        return datasetDb.getCollection(DB_STATISTICS_COLLECTION_NAME);
    }

    private DBObject getServerQuery(String server) {
        Pattern serverPattern = Pattern.compile(PATTERN_ALL + server + PATTERN_ALL);
        return QueryBuilder.start(LOCAL_ADDRESS).regex(serverPattern).and(EXPIRED).is(Boolean.FALSE).get();
    }

    private DBObject getCallIdQuery(String callId) {
        return QueryBuilder.start(CALL_ID).is(callId).get();
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
