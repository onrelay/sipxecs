/**
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
package org.sipfoundry.sipxconfig.backup;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.RunRequest;
import org.sipfoundry.sipxconfig.cdr.CdrManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.ivr.Ivr;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.admin.AdminContext;

public class RestoreApi extends ServerResource {
    private static final Log LOG = LogFactory.getLog(RestoreApi.class);

    private BackupRunner m_backupRunner;
    private LocationsManager m_locationsManager;
    private BackupConfig m_backupConfig;
    private BackupApi m_backupApi;
    private ConfigManager m_configManager;

    /**
     * POST : Restore
     */
    @Post
    public Representation acceptRepresentation(Representation entity) throws ResourceException {        
        m_backupApi.putOrPost(entity);
        return null;
    }

    public void restore(BackupPlan plan, BackupSettings settings, Collection<String> selections)
        throws ResourceException {

        File planFile = null;
        Writer planWtr = null;
        String configuration = StringUtils.EMPTY;
        RestoreServices services = null;
        boolean restartDeferred = false;
        try {
            //write user selection in a dedicated temp file : archive-backup-tmp-local.yaml/archive-backup-tmp-ftp.yaml
            //we cannot use a temp file because if HA setup
            //when backup runs on many nodes, a timeout may be returned and the temp file gets silently deleted
            planFile = m_backupApi.getBackupManager().getTmpRestoreFile(plan);
            planWtr = new FileWriter(planFile);
            Collection<Location> hosts = m_locationsManager.getLocationsList();
            m_backupConfig.writeConfig(planWtr, plan, hosts, settings);
            IOUtils.closeQuietly(planWtr);
            planWtr = null;
            configuration = FileUtils.readFileToString(planFile);
            services = getRestoreServices(plan, settings);
            runServiceActions("Stop services for restore", services.m_stop);
            try {
                m_backupRunner.restore(planFile, selections);
            } catch (BackupRunnerImpl.TimeoutException e) {
                restartDeferred = true;
                scheduleServiceStartAfterBackgroundRestore(services.m_start);
                throw e;
            }
            //if we are here, than restore finished successful
            LOG.info("Restore SUCCEEDED for configuration: " + configuration);
        } catch (Exception e) {
            LOG.error("Restore FAILED for configuration: " + configuration, e);
            if (e instanceof BackupRunnerImpl.TimeoutException) {
                throw new ResourceException(Status.CLIENT_ERROR_REQUEST_TIMEOUT, e);
            } else {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
            }
        } finally {
            IOUtils.closeQuietly(planWtr);
            if (!restartDeferred && services != null) {
                try {
                    runServiceActions("Start services after restore", services.m_start);
                } catch (RuntimeException e) {
                    LOG.error("Failed to start services after restore", e);
                    throw e;
                }
            }
        }
    }

    private RestoreServices getRestoreServices(BackupPlan plan, BackupSettings settings) {
        RestoreServices services = new RestoreServices();
        Set<String> selected = plan.getDefinitionIds();
        Set<String> restoredSingleNodeDefinitions = new LinkedHashSet<String>();
        Collection<Location> locations = m_locationsManager.getLocationsList();
        FeatureManager featureManager = m_backupApi.getBackupManager().getFeatureManager();

        for (Location location : locations) {
            Collection<ArchiveDefinition> definitions = m_backupApi.getBackupManager()
                    .getArchiveDefinitions(location, plan, settings);
            if (definitions == null) {
                continue;
            }
            for (ArchiveDefinition definition : definitions) {
                String id = definition.getId();
                if (!selected.contains(id) || StringUtils.isBlank(definition.getRestoreCommand())) {
                    continue;
                }
                if (definition.isSingleNodeRestore() && !restoredSingleNodeDefinitions.add(id)) {
                    continue;
                }

                if (AdminContext.ARCHIVE.equals(id)) {
                    services.addStart(location, "restart_sipxconfig");
                } else if (Ivr.ARCHIVE.equals(id)) {
                    services.addStop(location, "stop_sipxivr");
                    services.addStart(location, "start_sipxivr");
                } else if (CdrManager.ARCHIVE.equals(id)) {
                    services.addStop(location, "stop_sipxcdr");
                    services.addStart(location, "start_sipxcdr");
                    for (Location proxyLocation : locations) {
                        if (featureManager.isFeatureEnabled(ProxyManager.FEATURE, proxyLocation)) {
                            services.addStop(proxyLocation, "stop_sipxproxy");
                            services.addStart(proxyLocation, "start_sipxproxy");
                        }
                    }
                }
            }
        }
        return services;
    }

    private void runServiceActions(String label, Map<Location, Set<String>> actions) {
        for (Map.Entry<Location, Set<String>> entry : actions.entrySet()) {
            RunRequest request = new RunRequest(label, java.util.Collections.singleton(entry.getKey()));
            request.setDefines(entry.getValue().toArray(new String[entry.getValue().size()]));
            m_configManager.run(request);
        }
    }

    private void scheduleServiceStartAfterBackgroundRestore(final Map<Location, Set<String>> actions) {
        Thread restartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean interrupted = false;
                try {
                    while (m_backupRunner.isInProgress()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                } finally {
                    try {
                        runServiceActions("Start services after background restore", actions);
                    } catch (RuntimeException e) {
                        LOG.error("Failed to start services after background restore", e);
                    }
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "restore-service-restart");
        restartThread.setDaemon(true);
        restartThread.start();
    }

    private static class RestoreServices {
        private final Map<Location, Set<String>> m_stop = new LinkedHashMap<Location, Set<String>>();
        private final Map<Location, Set<String>> m_start = new LinkedHashMap<Location, Set<String>>();

        void addStop(Location location, String className) {
            add(m_stop, location, className);
        }

        void addStart(Location location, String className) {
            add(m_start, location, className);
        }

        private void add(Map<Location, Set<String>> actions, Location location, String className) {
            Set<String> classes = actions.get(location);
            if (classes == null) {
                classes = new LinkedHashSet<String>();
                actions.put(location, classes);
            }
            classes.add(className);
        }
    }

    public void setBackupRunner(BackupRunner backupRunner) {
        m_backupRunner = backupRunner;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public void setBackupConfig(BackupConfig backupConfig) {
        m_backupConfig = backupConfig;
    }

    public void setBackupApi(BackupApi backupApi) {
        m_backupApi = backupApi;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
}
