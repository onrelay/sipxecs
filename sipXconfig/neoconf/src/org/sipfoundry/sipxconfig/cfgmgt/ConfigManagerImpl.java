/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.cfgmgt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.common.ConfigManagerEvent;
import org.sipfoundry.sipxconfig.common.LazyDaemon;
import org.sipfoundry.sipxconfig.common.MongoGenerationFinishedEvent;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.job.JobContext;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeAction;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

public class ConfigManagerImpl implements AddressProvider, ConfigManager, BeanFactoryAware, AlarmProvider,
    ConfigCommands, SetupListener, ApplicationListener<MongoGenerationFinishedEvent>, DaoEventListener,
    ApplicationContextAware {
    private static final Log LOG = LogFactory.getLog(ConfigManagerImpl.class);
    private File m_cfDataDir;
    private DomainManager m_domainManager;
    private FeatureManager m_featureManager;
    private AddressManager m_addressManager;
    private LocationsManager m_locationManager;
    private Collection<ConfigProvider> m_providers;
    private ListableBeanFactory m_beanFactory;
    private final int m_sleepInterval = 7000;
    private final ConfigWorker m_worker = new ConfigWorker();
    private final ConfigRequest[] m_outstandingRequest = new ConfigRequest[1];
    private ConfigAgent m_configAgent;
    private RunBundleAgent m_runAgent;
    private SipxReplicationContext m_sipxReplicationContext;
    private JobContext m_jobContext;
    private String m_uploadDir;
    private Set<String> m_registeredIps = new HashSet<String>();
    private boolean m_postSetup;
    private boolean m_unregisteredConfigurable;
    private final Object m_lock = new Object();
    // No strict host key checking is only for initial handshake. Once that passes, ssh will
    // check host name with key.
    private String m_remoteCommand = "/usr/bin/ssh -o 'StrictHostKeyChecking=no' "
            + "-i %s/.cfagent/ppkeys/localhost.nopass.priv root@%s";
    private String m_remoteHostsFile = "%s/.ssh/known_hosts";
    private boolean m_flag;
    private SystemAuditManager m_systemAuditManager;
    private ApplicationContext m_applicationContext;

    @Override
    public synchronized void configureEverywhere(Feature... features) {
        // (re)start timer
        m_outstandingRequest[0] = ConfigRequest.merge(ConfigRequest.only(features), m_outstandingRequest[0]);
        notifyWorker();
    }

    private void notifyWorker() {
        synchronized (m_worker) {
            m_worker.workScheduled();
            m_worker.notify();
        }
    }

    @Override
    public String getRemoteCommand(String server) {
        return String.format(m_remoteCommand, getHomeDir(), server);
    }

    private String getHomeDir() {
        return System.getProperty("user.home");
    }

    private String getRemoteHostsFile() {
        return String.format(m_remoteHostsFile, getHomeDir());
    }

    @Override
    public synchronized void configureAllFeaturesEverywhere() {
        m_outstandingRequest[0] = ConfigRequest.merge(ConfigRequest.always(), m_outstandingRequest[0]);
        notifyWorker();
    }

    @Override
    public synchronized void configureAllFeatures(Collection<Location> locations) {
        m_outstandingRequest[0] = ConfigRequest.merge(ConfigRequest.only(locations), m_outstandingRequest[0]);
        notifyWorker();
    }

    @Override
    public synchronized void regenerateMongo(Collection<Location> locations) {
        if (locations.contains(m_locationManager.getPrimaryLocation())) {
            m_flag = true;
            m_sipxReplicationContext.generateAll();
        }
    }

    @Override
    public void sendProfiles(Collection<Location> locations) {
        regenerateMongo(locations);
        configureAllFeatures(locations);
        m_outstandingRequest[0].setSendProfiles(true);
        for (Location location : locations) {
            m_systemAuditManager.onConfigChangeAction(location, ConfigChangeAction.SEND_PROFILE, null, null, null);
        }
    }

    public synchronized boolean hasWork() {
        return m_outstandingRequest[0] != null;
    }

    public synchronized ConfigRequest getWork() {
        ConfigRequest work = m_outstandingRequest[0];
        m_outstandingRequest[0] = null;
        return work;
    }

    @Override
    public Collection<Location> getRegisteredLocations() {
        return getRegisteredLocations(m_locationManager.getLocationsList());
    }


    @Override
    public Collection<Location> getRegisteredLocations(Collection<Location> locations) {
        File csv = new File(m_uploadDir + "/lastseen.csv");
        RegisteredLocationResolver resolver = new RegisteredLocationResolver(this, m_registeredIps, csv);
        Collection<Location> registered = resolver.getRegisteredLocations(locations);
        m_registeredIps = resolver.getRegisteredIps();
        return registered;
    }

    // not synchronized so new incoming work can accumulate.
    public void doWork(ConfigRequest request) {
        String jobLabel = "Configuration generation";
        if (runPreProviders(request, jobLabel)) {
            runProviders(request, jobLabel);
            runCfengine(request, jobLabel);
            runPostProviders(request, jobLabel);
            m_applicationContext.publishEvent(new ConfigManagerEvent(request));
        }
    }

    @Override
    public void run() {
        ConfigRequest work = getWork();
        if (work != null) {
            doWork(work);
        }
    }

    @Override
    public void runProviders() {
        ConfigRequest work = getWork();
        if (work != null) {
            runProviders(work, "setup");
        }
    }

    private void runProviders(ConfigRequest request, String jobLabel) {
        synchronized (m_lock) {
            try {
                while (m_flag) {
                    m_lock.wait();
                }
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted. Config might be in stale state; rerun send profiles.");
            }
        }
        LOG.info("Configuration work to do. Notifying providers.");
        Serializable job = m_jobContext.schedule(jobLabel);
        m_jobContext.start(job);
        Stack<Exception> errors = new Stack<Exception>();
        for (ConfigProvider provider : getProviders()) {
            try {
                provider.replicate(this, request);
            } catch (Exception e) {
                LOG.error(jobLabel, e);
                errors.push(e);
            }
        }

        // even though there are errors, proceed to deploy phase. May want to
        // reevaluate this decision --Douglas
        if (errors.size() == 0) {
            m_jobContext.success(job);
        } else {
            fail(m_jobContext, jobLabel, job, errors.pop());
            // Tricky alert - show additional errors as new jobs
            while (!errors.empty()) {
                Serializable jobError = m_jobContext.schedule(jobLabel);
                m_jobContext.start(jobError);
                fail(m_jobContext, jobLabel, jobError, errors.pop());
            }
        }
    }

    private void runCfengine(ConfigRequest request, String jobLabel) {
        Collection<Location> all = m_locationManager.getLocationsList();
        Collection<Location> registered = getRegisteredLocations(all);
        m_configAgent.run(registered);
    }

    private void runPostProviders(ConfigRequest request, String jobLabel) {
        // After config has rolled out
        for (ConfigProvider provider : getProviders()) {
            try {
                if (provider instanceof PostConfigListener) {
                    ((PostConfigListener) provider).postReplicate(this, request);
                }
            } catch (Exception e) {
                Serializable jobError = m_jobContext.schedule(jobLabel);
                m_jobContext.start(jobError);
                fail(m_jobContext, jobLabel, jobError, e);
            }
        }
    }

    private boolean runPreProviders(ConfigRequest request, String jobLabel) {
        for (ConfigProvider provider : getProviders()) {
            try {
                if (provider instanceof PreConfigListener) {
                    ((PreConfigListener) provider).preReplicate(this, request);
                }
            } catch (Exception e) {
                Serializable jobError = m_jobContext.schedule(jobLabel);
                m_jobContext.start(jobError);
                fail(m_jobContext, jobLabel, jobError, e);
                return false;
            }
        }
        return true;
    }

    static void fail(JobContext jc, String label, Serializable job, Exception e) {
        // ConfigException's error message is useful to user, otherwise emit raw error
        LOG.error(label, e);
        if (e instanceof ConfigException) {
            jc.failure(job, e.getMessage(), new RuntimeException());
        } else {
            jc.failure(job, "Internal Error", e);
        }
    }

    public String getCfDataDir() {
        return m_cfDataDir.getAbsolutePath();
    }

    public void setCfDataDir(String cfDataDir) {
        m_cfDataDir = new File(cfDataDir);
    }

    @Override
    public File getGlobalDataDirectory() {
        return m_cfDataDir;
    }

    @Override
    public File getPrimaryLocationDataDirectory() {
        File d = new File(m_cfDataDir, String.valueOf(m_locationManager.getPrimaryLocationId()));
        if (!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    @Override
    public File getLocationDataDirectory(Location location) {
        File d = new File(m_cfDataDir, String.valueOf(location.getId()));
        if (!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    @Override
    public DomainManager getDomainManager() {
        return m_domainManager;
    }

    @Required
    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return m_featureManager;
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Override
    public AddressManager getAddressManager() {
        return m_addressManager;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

    @Override
    public LocationsManager getLocationManager() {
        return m_locationManager;
    }

    public void setLocationManager(LocationsManager locationManager) {
        m_locationManager = locationManager;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!type.equals(SUPERVISOR_ADDRESS)) {
            return null;
        }
        // this will eventually phase out in favor of sipxsupervisor-lite
        return Collections.singleton(new Address(SUPERVISOR_ADDRESS, null, 8092));
    }

    private Collection<ConfigProvider> getProviders() {
        if (m_providers == null) {
            Map<String, ConfigProvider> providers = m_beanFactory.getBeansOfType(ConfigProvider.class, false, false);
            m_providers = providers.values();
        }
        return m_providers;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }

    class ConfigWorker extends LazyDaemon {
        ConfigWorker() {
            super("Replication worker thread", m_sleepInterval);
        }

        @Override
        protected void waitForWork() throws InterruptedException {
            if (!hasWork()) {
                synchronized (ConfigWorker.this) {
                    ConfigWorker.this.wait();
                }
            }
        }

        @Override
        protected boolean work() {
            ConfigRequest work = getWork();
            // work could be null if call to run() was made explicitly
            // then this is a nop.
            if (work != null) {
                doWork(work);
            }
            return true;
        }
    }

    public void setConfigAgent(ConfigAgent configAgent) {
        m_configAgent = configAgent;
    }

    public void setSipxReplicationContext(SipxReplicationContext sipxReplicationContext) {
        m_sipxReplicationContext = sipxReplicationContext;
    }

    public void setJobContext(JobContext jobContext) {
        m_jobContext = jobContext;
    }

    @Override
    public void run(RunRequest request) {
        LOG.info("Running " + request.getLabel());
        m_runAgent.run(request.getLocations(), request.getLabel(), request.getBundles(), request.getDefines());
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        return Arrays.asList(PROCESS_FAILED, PROCESS_RESTARTED);
    }

    @Override
    public void restartServices() {
        restartServices(getRegisteredLocations());
    }

    @Override
    public void restartServices(Collection<Location> locations) {
        RunRequest r = new RunRequest("restart services", locations);
        r.setDefines("restart_sipxecs");
        run(r);
    }

    @Override
    public void lastSeen() {
        Location primary = m_locationManager.getPrimaryLocation();
        RunRequest r = new RunRequest("registration check", Collections.singleton(primary));
        r.setBundles("last_seen");
        run(r);
    }

    @Override
    public void collectSnapshot(Location location) {
        RunRequest collect = new RunRequest("collect snapshot", Collections.singleton(location));
        collect.setBundles("collect_snapshot");
        run(collect);
    }

    @Override
    public void uploadSnapshot(Location location) {
        RunRequest collect = new RunRequest("upload snapshot", Collections.singleton(location));
        collect.setBundles("upload_snapshot");
        run(collect);
    }

    @Override
    public void resetKeys(Collection<Location> locations) {
        if (locations.size() == 0) {
            return;
        }
        File resetKeysFile = new File(getCfDataDir() + "/1/reset_cfkey.cfdat");
        Writer out = null;
        try {
            out = new FileWriter(resetKeysFile);
            CfengineModuleConfiguration w = new CfengineModuleConfiguration(out);
            @SuppressWarnings("unchecked")
            Collection<String> ips = CollectionUtils.collect(locations, Location.GET_ADDRESS);
            w.writeList("reset_cfkeys", ips);
        } catch (IOException err) {
            throw new UserException("Could not reset cfengine keys", err);
        } finally {
            IOUtils.closeQuietly(out);
        }
        Location primary = getLocationManager().getPrimaryLocation();
        RunRequest reset = new RunRequest("reset cfengine keys", Collections.singleton(primary));
        reset.setBundles("reset_cfkey");
        // cfengine promise should delete file as this job is asynchronous
        run(reset);

        // clobbering of ~/.ssh/hosts wholesale ensures remote ssh command accept new
        // ssh keys
        File remoteHosts = new File(getRemoteHostsFile());
        if (remoteHosts.exists()) {
            remoteHosts.delete();
        }

        for (Location location : locations) {
            m_systemAuditManager.onConfigChangeAction(location,
                    ConfigChangeAction.RESET_KEYS, null, null, null);
        }
    }

    @Override
    public ConfigCommands getConfigCommands() {
        return this;
    }

    public void setRunAgent(RunBundleAgent runAgent) {
        m_runAgent = runAgent;
    }

    public void setUploadDir(String uploadDir) {
        m_uploadDir = uploadDir;
    }

    public void setRemoteCommand(String remoteCommand) {
        m_remoteCommand = remoteCommand;
    }

    @Override
    public boolean setup(SetupManager manager) {
        if (!m_postSetup) {
            m_postSetup = true;
            return false;
        }
        m_worker.start();
        notifyWorker();
        return true;
    }

    @Override
    public void onApplicationEvent(MongoGenerationFinishedEvent event) {
        synchronized (m_lock) {
            m_flag = false;
            m_lock.notifyAll();
        }
    }

    public void setRemoteHostsFile(String remoteHostsFile) {
        m_remoteHostsFile = remoteHostsFile;
    }

    @Override
    public void onSave(Object entity) {
    }

    @Override
    public void onDelete(Object entity) {
        if (entity instanceof Location) {
            Location deletedLocation = (Location) entity;
            m_registeredIps.remove(deletedLocation.getAddress());
        }
    }

    public void setUnregisteredConfigurable(boolean unregisteredConfigurable) {
        m_unregisteredConfigurable = unregisteredConfigurable;
    }

    @Override
    public Collection<Location> getConfigurableLocations() {
        if (m_unregisteredConfigurable) {
            return m_locationManager.getLocationsList();
        }
        // TODO Auto-generated method stub
        return getRegisteredLocations();
    }

    @Required
    public void setSystemAuditManager(SystemAuditManager systemAuditManager) {
        m_systemAuditManager = systemAuditManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
}
