/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.test;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sipfoundry.commons.userdb.profile.UserProfileService;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.common.event.DaoEventPublisherImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.TransactionException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.operation.DatabaseOperation;


public abstract class IntegrationTestCase {
    private static final String ROOT_RES_PATH = "/org/sipfoundry/sipxconfig/";
    private static final Log LOG = LogFactory.getLog(IntegrationTestCase.class);
    private static final String CANNOT_SET_PROP_MSG = "Unable to set property %s on target %s";

    @Autowired(required = false)
    private SessionFactory m_sessionFactory;

    private JdbcTemplate m_db;
    private Map<Object, Map<String, Object>> m_modifiedContextObjectMap;

    private int transactionsStarted = 0;
    private PlatformTransactionManager transactionManager;
    protected TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    protected TransactionStatus transactionStatus;

    private boolean complete = false;

    @Autowired(required = false)
    private DaoEventPublisherImpl m_daoEventPublisher;

    @Autowired(required = false)
    private UserProfileService m_userProfileService;

    @Autowired(required = false)
    private MongoTemplate m_profilesDb;

    public IntegrationTestCase() {
        // no-op constructor for subclass use
    }


   public final void setUp() throws Exception {

         this.onSetUpBeforeTransaction();

         this.onStartNewTransaction();

         try {
            this.onSetUpInTransaction();
         } catch (Exception var2) {
            this.endTransaction();
            throw var2;
         }
   }

    protected final void tearDown() throws Exception {
         try {
            this.onTearDownInTransaction();
         } finally {
            this.endTransaction();
         }
      
         this.onTearDownAfterTransaction();
   }

    protected int countRowsInTable(String tableName) {
        return db().queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    protected void sql(String resource) throws IOException {
        SqlFileReader sql = new SqlFileReader(getClass().getResourceAsStream(ROOT_RES_PATH + resource));
        m_db.batchUpdate(sql.parse().toArray(new String[0]));
    }

    protected void sql(InputStream in) throws IOException {
        SqlFileReader sql = new SqlFileReader(in);
        m_db.batchUpdate(sql.parse().toArray(new String[0]));
    }

    protected JdbcTemplate db() {
        return m_db;
    }

    protected void divertDaoEvents(DaoEventListener listener) {
        if (m_daoEventPublisher != null) {
            m_daoEventPublisher.divertEvents(listener);
        }
    }

    protected void disableDaoEventPublishing() {
        if (m_daoEventPublisher != null) {
            DaoEventListener stub = org.easymock.EasyMock.createNiceMock(DaoEventListener.class);
            m_daoEventPublisher.divertEvents(stub);
        }
    }

    protected void onSetUpBeforeTransaction() throws Exception {
        // default no-op; override in subclass if needed
    }

    protected void onStartNewTransaction() throws Exception {
        // default no-op; override in subclass if needed
    }

    protected void onSetUpInTransaction() throws Exception {
        m_modifiedContextObjectMap = new HashMap<Object, Map<String, Object>>();
    }


    protected void onTearDownInTransaction() throws Exception {
        if (m_modifiedContextObjectMap != null) {
            resetContext();
        }
        m_profilesDb.dropCollection("userProfile");
    }

    protected void onTearDownAfterTransaction() throws Exception {
        // default no-op; override in subclass if needed
    }


    public void setDataSource(DataSource dataSource) {
        this.m_db = new JdbcTemplate(dataSource);
    }

    public void setConfigJdbcTemplate(JdbcTemplate db) {
        m_db = db;
    }

    protected String[] getConfigLocations() {
        // There are many interdependencies between spring files so in general you need
        // to load them all. However, if you do have isolated spring file, this is definitely
        // overrideable
        return new String[] {
            "classpath:/org/sipfoundry/sipxconfig/system.beans.xml",
            "classpath*:/org/sipfoundry/sipxconfig/*/**/*.beans.xml",
            "classpath*:/sipxplugin.beans.xml"
        };
    }


    void dumpSqlExceptionMessages(SQLException e) {
        for (SQLException next = e; next != null; next = next.getNextException()) {
            LOG.info(next.getMessage());
        }
    }

    void dumpSqlExceptionMessages(DataIntegrityViolationException e) {
        if (e.getCause() instanceof SQLException) {
            dumpSqlExceptionMessages((SQLException) e.getCause());
        }
    }

    protected void loadDataSet(String resource) {
        IDatabaseConnection connection = getConnection();
        try {
            IDataSet dataSet = TestHelper.loadDataSetFlat(resource);
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void loadDataSetXml(String resource) throws Exception {
        IDatabaseConnection connection = getConnection();
        IDataSet dataSet = TestHelper.loadDataSet(resource);
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    protected ReplacementDataSet loadReplaceableDataSetFlat(String fileResource) throws Exception {
        IDataSet ds = TestHelper.loadDataSetFlat(fileResource);
        ReplacementDataSet relaceable = new ReplacementDataSet(ds);
        relaceable.addReplacementObject("[null]", null);
        return relaceable;
    }

    protected IDatabaseConnection getConnection() {
        final IDatabaseConnection[] dbunitConnectionHolder = new IDatabaseConnection[1];
        m_sessionFactory.getCurrentSession().doWork(connection -> {
            IDatabaseConnection dbunitConnection = new DatabaseConnection(connection);
            DatabaseConfig config = dbunitConnection.getConfig();
            config.setFeature(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, true);
            dbunitConnectionHolder[0] = dbunitConnection;
        });
        return dbunitConnectionHolder[0];
    }

    protected void flush() {
        m_sessionFactory.getCurrentSession().flush();
    }

    protected void evict(Object o) {
        m_sessionFactory.getCurrentSession().evict(o);
    }

    protected void commit() {
        if (transactionManager == null) {
            throw new IllegalStateException("No transaction manager available");
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        transactionManager.commit(status);
    }

    protected void clear() {
        db().execute("select truncate_all()");
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        m_sessionFactory = sessionFactory;
    }

    protected void modifyContext(Object target, String propertyName, Object originalValue, Object valueForTest) {
        m_modifiedContextObjectMap.computeIfAbsent(target, k -> new HashMap<>()).put(propertyName, originalValue);
        try {
            BeanUtils.setProperty(target, propertyName, valueForTest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error(format(CANNOT_SET_PROP_MSG, propertyName, target), e);
        }
    }

    private void resetContext() {
        for (Map.Entry<Object, Map<String, Object>> entry : m_modifiedContextObjectMap.entrySet()) {
            Object target = entry.getKey();
            Map<String, Object> props = entry.getValue();
            for (Map.Entry<String, Object> propEntry : props.entrySet()) {
                try {
                    BeanUtils.setProperty(target, propEntry.getKey(), propEntry.getValue());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error(format(CANNOT_SET_PROP_MSG, propEntry.getKey(), target), e);
                }
            }
        }
        m_modifiedContextObjectMap.clear();
        if (m_daoEventPublisher != null) {
            m_daoEventPublisher.stopDivertingEvents();
        }
    }

    public void setDaoEventPublisherImpl(DaoEventPublisherImpl daoEventPublisher) {
        m_daoEventPublisher = daoEventPublisher;
    }

    public DaoEventPublisherImpl getDaoEventPublisher() {
        return m_daoEventPublisher;
    }

    public SessionFactory getSessionFactory() {
        return m_sessionFactory;
    }

    public Session getCurrentSession() {
        return m_sessionFactory.getCurrentSession();
    }

    public UserProfileService getUserProfileService() {
        return m_userProfileService;
    }

    public void setUserProfileService(UserProfileService service) {
        m_userProfileService = service;
    }

    public MongoTemplate getProfilesDb() {
        return m_profilesDb;
    }

    public void setProfilesDb(MongoTemplate template) {
        m_profilesDb = template;
    }

    protected void endTransaction() {
      if (this.transactionStatus != null) {
         try {
            if (!this.complete) {
               this.transactionManager.rollback(this.transactionStatus);
            } else {
               this.transactionManager.commit(this.transactionStatus);
            }
         } finally {
            this.transactionStatus = null;
         }
      }

   }

   protected void startNewTransaction() throws TransactionException {
      if (this.transactionStatus != null) {
         throw new IllegalStateException("Cannot start new transaction without ending existing transaction: Invoke endTransaction() before startNewTransaction()");
      } else if (this.transactionManager == null) {
         throw new IllegalStateException("No transaction manager set");
      } else {
         this.transactionStatus = this.transactionManager.getTransaction(this.transactionDefinition);
         ++this.transactionsStarted;
         this.complete = true;
      }
   }
}

