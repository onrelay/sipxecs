/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.callqueue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.DaoUtils;
import org.sipfoundry.sipxconfig.common.ExtensionInUseException;
import org.sipfoundry.sipxconfig.common.NameInUseException;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SameExtensionException;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.imdb.ReplicationManager;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;

public class CallQueueContextImpl extends SipxHibernateDaoSupport implements CallQueueContext, BeanFactoryAware,
        FeatureProvider {

    private static final String QUERY_CALL_QUEUE_EXTENSIONS_WITH_NAMES = "callQueueExtensionWithName";
    private static final String QUERY_CALL_QUEUE_AGENT_WITH_NAME_OR_EXT = "callQueueAgentIds";
    private static final String QUERY_PARAM_VALUE = "value";
    private static final String QUERY_PARAM_AGENT_ID = "callqueueagentid";
    private static final String QUERY_PARAM_QUEUE_ID = "callqueueid";
    private static final String COPY_OF = "Copy of ";
    protected static final String COPIED = " (Copied)";

    private static final String ALIAS = "alias";
    private static final String EXTENSION = "extension";
    private static final String DID = "did";
    private static final String QUEUE_NAME = "&" + CALL_QUEUE;

    private BeanFactory m_beanFactory;
    private AliasManager m_aliasManager;
    private FeatureManager m_featureManager;
    private BeanWithSettingsDao<CallQueueSettings> m_settingsDao;
    private ReplicationManager m_replicationManager;
    private CallQueueDeployer m_fsDeployer;

    private static final Log LOG = LogFactory.getLog(CallQueueContextImpl.class);
    
    // TODO load only queues and agents that were changed
    private boolean m_reloadQueues;
    
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

    /* Bean properties */
    @Required
    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Required
    public void setReplicationManager(ReplicationManager replicationManager) {
        m_replicationManager = replicationManager;
    }

    @Required
    public void setCallQueueDeployer(CallQueueDeployer deployer) {
        m_fsDeployer = deployer;
    }

    /* Settings API */

    public void setSettingsDao(BeanWithSettingsDao<CallQueueSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    public CallQueueSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(CallQueueSettings settings) {
        m_settingsDao.upsert(settings);
        refreshCallQueueCommands(settings);
        refreshCallQueueAgents();
    }

    @Override
    public List<Replicable> getReplicables() {
        if (m_featureManager.isFeatureEnabled(FEATURE)) {
            List<Replicable> replicables = new ArrayList<Replicable>();
            replicables.addAll(getCallQueues());
            replicables.addAll(getCallQueueCommands());
            return replicables;
        }
        return Collections.EMPTY_LIST;
    }

    /* Alias support */
    @Override
    public Collection<BeanId> getBeanIdsOfObjectsWithAlias(String alias) {
        Collection<BeanId> bids = new ArrayList<BeanId>();

        List<CallQueue> lines = getHibernateTemplate().loadAll(CallQueue.class);
        for (CallQueue line : lines) {
            if (line.getExtension() != null && (line.getExtension().equals(alias) || line.getName().equals(alias))
                    || (line.getAlias() != null && line.getAlias().equals(alias))
                    || (line.getDid() != null && line.getDid().equals(alias))) {
                bids.add(new BeanId(line.getId(), CallQueue.class));
            }
        }
        // Add all beans, having alias(es)
        return bids;
    }

    @Override
    public boolean isAliasInUse(String alias) {
        List<CallQueueExtension> extensions = getFreeswitchExtensions();
        for (CallQueueExtension extension : extensions) {
            if (extension.getExtension() != null
                    && (extension.getExtension().equals(alias) || extension.getName().equals(alias))) {
                return true;
            }
            if (extension.getAlias() != null && extension.getAlias().equals(alias)) {
                return true;
            }
            if (extension.getDid() != null && extension.getDid().equals(alias)) {
                return true;
            }
        }
        return false;
    }

    /* FreeSwitchExtensionProvider */
    @Override
    public boolean isEnabled() {
        return m_featureManager.isFeatureEnabled(FEATURE);
    }

    public void deleteExtension(CallQueueExtension ext) {
        getHibernateTemplate().delete(ext);
    }

    public void saveExtension(CallQueueExtension extension) {
        if (extension.getName() == null) {
            throw new UserException("&null.name");
        }
        if (extension.getExtension() == null) {
            throw new UserException("&null.extension");
        }
        String capturedExt = extension.getCapturedExtension();

        if (!m_aliasManager.canObjectUseAlias(extension, extension.getName())) {
            throw new NameInUseException(QUEUE_NAME, extension.getName());
        } else if (!m_aliasManager.canObjectUseAlias(extension, capturedExt)) {
            throw new ExtensionInUseException(QUEUE_NAME, capturedExt);
        } else if (extension.getAlias() != null
                && !m_aliasManager.canObjectUseAlias(extension, extension.getAlias())) {
            throw new ExtensionInUseException(QUEUE_NAME, extension.getAlias());
        } else if (extension.getAlias() != null && extension.getAlias().equals(extension.getExtension())) {
            throw new SameExtensionException(ALIAS, EXTENSION);
        } else if (extension.getDid() != null && !m_aliasManager.canObjectUseAlias(extension, extension.getDid())) {
            throw new ExtensionInUseException(QUEUE_NAME, extension.getDid());
        } else if (extension.getDid() != null && extension.getDid().equals(extension.getExtension())) {
            throw new SameExtensionException(DID, EXTENSION);
        } else if (extension.getDid() != null && extension.getAlias() != null
                && extension.getDid().equals(extension.getAlias())) {
            throw new SameExtensionException(ALIAS, DID);
        }
        removeNullActions(extension);
        if (extension.isNew()) {
            getHibernateTemplate().saveOrUpdate(extension);
        } else {
            getHibernateTemplate().merge(extension);
        }
    }

    @Override
    public CallQueueExtension getExtensionById(Integer extensionId) {
        return getHibernateTemplate().load(CallQueueExtension.class, extensionId);
    }

    @Override
    public CallQueueExtension getExtensionByName(String extensionName) {
        List<CallQueue> extensions = getHibernateTemplate().findByNamedQueryAndNamedParam(
                QUERY_CALL_QUEUE_EXTENSIONS_WITH_NAMES, QUERY_PARAM_VALUE, extensionName);
        return DataAccessUtils.singleResult(extensions);
    }

    @Override
    public List<CallQueueExtension> getFreeswitchExtensions() {
        return getHibernateTemplate().loadAll(CallQueueExtension.class);
    }

    private void removeNullActions(CallQueueExtension extension) { // Should not be Tested
        if (extension.getConditions() == null) {
            return;
        }
        for (FreeswitchCondition condition : extension.getConditions()) {
            for (FreeswitchAction action : condition.getActions()) {
                if (action != null && action.getApplication() == null) {
                    condition.removeAction(action);
                }
            }
        }
    }

    /* CallQueue API */

    public CallQueue newCallQueue() { // Tested
        CallQueue callqueue = (CallQueue) m_beanFactory.getBean(CallQueue.class);
        return callqueue;
    }

    public void saveCallQueue(CallQueue callQueue) { // Tested
        saveExtension(callQueue);
        m_reloadQueues = true;
    }

    public CallQueue loadCallQueue(Integer id) { // Tested
        return (CallQueue) getHibernateTemplate().load(CallQueue.class, id);
    }
    
    @Override
    public CallQueue getCallQueueByName(String name) {
        String query = "from CallQueue c where c.name = :name";
        List<CallQueue> queueList = getHibernateTemplate().findByNamedParam(query, "name", name);

        return DaoUtils.requireOneOrZero(queueList, query);
    }
    
    public void duplicateCallQueues(Collection<Integer> ids) { // Tested
        for (Integer id : ids) {
            CallQueue srcCallQueue = (CallQueue) getHibernateTemplate().load(CallQueue.class, id);
            CallQueue newCallQueue = newCallQueue();
            // TODO: localize strings
            newCallQueue.setName(COPY_OF + srcCallQueue.getName());
            if (null != srcCallQueue.getDescription()) {
                newCallQueue.setDescription(srcCallQueue.getDescription() + COPIED);
            }
            srcCallQueue.copySettingsTo(newCallQueue);
            saveCallQueue(newCallQueue);
        }
        m_reloadQueues = true;
    }

    public void deleteCallQueues(Collection<Integer> ids) { // Tested
        if (ids.isEmpty()) {
            return;
        }
        for (Integer queueId : ids) {
            CallQueue queue = loadCallQueue(queueId);
            String extension = queue.getExtension();
            getHibernateTemplate().delete(queue);
            m_fsDeployer.deleteQueue(extension);
        }
    }
    
    @Required
    public void deleteCallQueue(String name) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        CallQueue queue = getCallQueueByName(name);
        String extension = queue.getExtension();
        getHibernateTemplate().delete(queue);
        m_fsDeployer.deleteQueue(extension);
    }

    public Collection<CallQueue> getCallQueues() { // Test
        return getHibernateTemplate().loadAll(CallQueue.class);
    }

    /* CallQueueCommand API */

    private void createCallQueueCommand(String command, String status) { // Should not be Tested
        CallQueueCommand callQueueCommand = newCallQueueCommand();
        callQueueCommand.setName(command);
        callQueueCommand.setAlias(getSettings().getSettingValue("call-queue-agent-code/" + command));
        callQueueCommand.setExtension(command, status);
        saveCallQueueCommand(callQueueCommand);
    }

    private void refreshCallQueueCommands(CallQueueSettings settings) { // Should not be Tested
        Collection<CallQueueCommand> callqueuecommands = getCallQueueCommands();
        for (CallQueueCommand callQueueCommand : callqueuecommands) {
            callQueueCommand.setAlias(settings.getSettingValue(String.format("call-queue-agent-code/%s",
                    callQueueCommand.getName())));
            saveCallQueueCommand(callQueueCommand);
        }
    }

    private void refreshCallQueueAgents() { // Should not be Tested
        Collection<CallQueueAgent> agents = getCallQueueAgents();
        for (CallQueueAgent agent : agents) {
            if ((Boolean) agent.getSettingTypedValue("call-queue-agent/use-agent-defaults")) {
                // save agents / redeploy configuration to FS
                saveCallQueueAgent(agent);
            }
        }
    }

    private CallQueueCommand newCallQueueCommand() { // Should not be Tested
        CallQueueCommand callqueuecommand = (CallQueueCommand) m_beanFactory.getBean(CallQueueCommand.class);
        return callqueuecommand;
    }

    @Override
    public void saveCallQueueCommand(CallQueueCommand callQueueCommand) {
        saveExtension(callQueueCommand);
        m_replicationManager.replicateEntity(callQueueCommand);
    }

    private CallQueueCommand loadCallQueueCommand(Integer id) { // Should not be Tested
        return (CallQueueCommand) getHibernateTemplate().load(CallQueueCommand.class, id);
    }

    private Collection<CallQueueCommand> getCallQueueCommands() { // Should not be Tested
        return getHibernateTemplate().loadAll(CallQueueCommand.class);
    }

    /* CallQueueAgent API */

    public CallQueueAgent newCallQueueAgent() { // Tested
        CallQueueAgent callqueueagent = (CallQueueAgent) m_beanFactory.getBean(CallQueueAgent.class);
        return callqueueagent;
    }
    
    @Override
    public CallQueueAgent getAgentByName(String agentName) {
        List<CallQueueAgent> clients = getHibernateTemplate().findByNamedQueryAndNamedParam(
                QUERY_CALL_QUEUE_AGENT_WITH_NAME_OR_EXT, QUERY_PARAM_VALUE, agentName);
        return DataAccessUtils.singleResult(clients);
    }

    private boolean checkForDuplicateAgentName(CallQueueAgent callQueueAgent) {
        String name = callQueueAgent.getName();
        CallQueueAgent existingClient = getAgentByName(name);
        if (existingClient != null && !existingClient.getId().equals(callQueueAgent.getId())) {
            return false;
        }
        return true;
    }

    public void saveCallQueueAgent(CallQueueAgent callQueueAgent) { // Tested
        // Check for duplicate names and extensions before saving the call queue
        final String callQueueAgentTypeName = "&label.callQueueAgent";
        if (!checkForDuplicateAgentName(callQueueAgent)) {
            throw new NameInUseException(callQueueAgentTypeName, callQueueAgent.getName());
        }
        List<Integer> queuesBefore = new ArrayList<Integer>();
        boolean isNew = callQueueAgent.isNew();
        if (!isNew) {
            queuesBefore = getCallQueueIds(callQueueAgent.getId());
        }
        if (isNew) {
            getHibernateTemplate().save(callQueueAgent);
        } else {
            getHibernateTemplate().merge(callQueueAgent);
        }
        getHibernateTemplate().flush();
        List<Integer> queuesAfter = getCallQueueIds(callQueueAgent.getId());
        queuesAfter = queuesAfter == null ? new ArrayList<Integer>() : queuesAfter;
        Collection<Integer> queuesToReload = CollectionUtils.union(queuesBefore, queuesAfter);
        m_fsDeployer.deployAgent(callQueueAgent, isNew);
        for (Integer callQueueId : queuesToReload) {
            CallQueue queue = getHibernateTemplate().load(CallQueue.class, callQueueId);
            m_fsDeployer.deployTiers(queue, callQueueAgent);
        }
    }

    public CallQueueAgent loadCallQueueAgent(Integer id) { // Tested
        return (CallQueueAgent) getHibernateTemplate().load(CallQueueAgent.class, id);
    }

    public void duplicateCallQueueAgents(Collection<Integer> ids) { // Tested
        for (Integer id : ids) {
            CallQueueAgent srcCallQueueAgent = loadCallQueueAgent(id);
            CallQueueAgent newCallQueueAgent = newCallQueueAgent();
            // TODO: localize strings
            newCallQueueAgent.setName(COPY_OF + srcCallQueueAgent.getName());
            if (null != srcCallQueueAgent.getDescription()) {
                newCallQueueAgent.setDescription(srcCallQueueAgent.getDescription() + COPIED);
            }
            srcCallQueueAgent.copySettingsTo(newCallQueueAgent);
            saveCallQueueAgent(newCallQueueAgent);
            m_fsDeployer.deployAgent(newCallQueueAgent, true);
        }
    }

    public void deleteCallQueueAgents(Collection<Integer> ids) { // Tested
        if (ids.isEmpty()) {
            return;
        }

        for (Integer agentId : ids) {
            CallQueueAgent agent = loadCallQueueAgent(agentId);
            String extension = agent.getExtension();
            getHibernateTemplate().delete(agent);
            m_fsDeployer.deleteAgent(extension);
        }

    }
    
    @Override
    public void deleteCallQueueAgent(String name) {        
        CallQueueAgent agent = getAgentByName(name);
        String extension = agent.getExtension();
        getHibernateTemplate().delete(agent);
        m_fsDeployer.deleteAgent(extension);
    }

    public Collection<CallQueueAgent> getCallQueueAgents() { // Tested
        return getHibernateTemplate().loadAll(CallQueueAgent.class);
    }

    public Collection<CallQueueAgent> getCallQueueAgentsWithState() {
        Map<String, String> states = m_fsDeployer.getAgentState();
        List<CallQueueAgent> agents = getHibernateTemplate().loadAll(CallQueueAgent.class);
        for (CallQueueAgent agent : agents) {
            String agentName = "agent-" + agent.getExtension();
            if (states.containsKey(agentName)) {
                agent.setState(states.get(agentName));
            }
        }
        return agents;
    }

    @Override
    public List<CallQueue> getAvaiableQueuesForAgent(Integer callqueueagentid) { // Tested
        if (null == callqueueagentid) {
            return Collections.EMPTY_LIST;
        }
        Query query = getSession()
                .createSQLQuery(
                        "(SELECT q.* FROM freeswitch_extension q WHERE freeswitch_ext_type = 'q')" + " EXCEPT"
                                + " (SELECT DISTINCT q.* FROM freeswitch_extension q"
                                + " LEFT JOIN call_queue_tier t USING(freeswitch_ext_id)"
                                + " WHERE t.call_queue_agent_id = :" + QUERY_PARAM_AGENT_ID + ")")
                .addEntity(CallQueue.class).setParameter(QUERY_PARAM_AGENT_ID, callqueueagentid.intValue());
        List<CallQueue> result = query.list();
        return result;
    }

    @Override
    public List<Integer> getCallQueueAgentsForQueue(Integer callqueueid) { // Tested
        if (null == callqueueid) {
            return Collections.EMPTY_LIST;
        }
        Query query = getSession().createSQLQuery(
                "SELECT DISTINCT t.call_queue_agent_id FROM call_queue_tier t WHERE t.freeswitch_ext_id = :"
                        + QUERY_PARAM_QUEUE_ID).setParameter(QUERY_PARAM_QUEUE_ID, callqueueid.intValue());
        List<Integer> result = query.list();
        return result;
    }

    private List<Integer> getCallQueueIds(Integer callqueueAgentId) {
        if (null == callqueueAgentId) {
            return Collections.EMPTY_LIST;
        }
        Query query = getSession().createSQLQuery(
                "SELECT q.freeswitch_ext_id FROM freeswitch_extension q INNER JOIN call_queue_tier t"
                        + " ON q.freeswitch_ext_id=t.freeswitch_ext_id where t.call_queue_agent_id=:callqueueagentid")
                .setParameter(QUERY_PARAM_AGENT_ID, callqueueAgentId);
        List<Integer> result = query.list();
        return result;
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.requiredOnSameHost(FEATURE, FreeswitchFeature.FEATURE);
        validator.singleLocationOnly(FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.getAllNewlyDisabledFeatures().contains(FEATURE)) {
            for (CallQueueCommand command : getCallQueueCommands()) {
                m_replicationManager.removeEntity(command);
            }
            for (CallQueue queue : getCallQueues()) {
                m_replicationManager.removeEntity(queue);
            }
        }

        if (request.getAllNewlyEnabledFeatures().contains(FEATURE)) {
            Collection<CallQueueCommand> commands = getCallQueueCommands();
            if (commands.isEmpty()) {
                createCallQueueCommand("agent-login", "Available");
                createCallQueueCommand("agent-on-break", "On Break");
                createCallQueueCommand("agent-logout", "Logged Out");
            }
        }
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(CallQueueContext.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        List<Location> enabledLocations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = enabledLocations.contains(location);
            ConfigUtils.enableCfengineClass(dir, "sipxcallqueue.cfdat", enabled, "callqueue");
        }
    }

    @Override
    public void postReplicate(ConfigManager manager, ConfigRequest request) throws IOException {
        // reload queues only after reloadxml finished
        if (request.applies(FEATURE) || request.applies(FreeswitchFeature.FEATURE)) {
            if (m_reloadQueues) {
                m_reloadQueues = false;
                for (CallQueue queue : getCallQueues()) {
                    m_fsDeployer.reloadQueue(queue.getExtension());
                }
            }
        }
    }
}
