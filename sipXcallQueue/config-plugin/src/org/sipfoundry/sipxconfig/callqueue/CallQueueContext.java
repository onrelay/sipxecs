/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.callqueue;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.alias.AliasOwner;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.PostConfigListener;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtensionProvider;

public interface CallQueueContext extends FreeswitchExtensionProvider, AliasOwner, ReplicableProvider,
        ConfigProvider, PostConfigListener {

    public static final String CALL_FEATURE_ID = CallQueueContext.CALL_QUEUE;
    public static final LocationFeature FEATURE = new LocationFeature(CALL_FEATURE_ID);
    public static final String CALL_QUEUE = "callqueue";
    public static final String CALL_QUEUE_GROUP_ID = CALL_QUEUE;
    public static final String CALL_QUEUE_AGENT_GROUP_ID = "callqueueagent";

    public static final String CONTEXT_BEAN_NAME = "callQueueContext";

    /* FreeSwitchExtensionProveder API */

    void saveExtension(CallQueueExtension extension);

    void deleteExtension(CallQueueExtension extension);

    void saveCallQueueCommand(CallQueueCommand callQueueCommand);

    CallQueueExtension getExtensionById(Integer extensionId);

    CallQueueExtension getExtensionByName(String extensionName);

    List<CallQueueExtension> getFreeswitchExtensions();

    /* Settings API */

    public CallQueueSettings getSettings();

    public void saveSettings(CallQueueSettings settings);

    /* CallQueue API */
    CallQueue loadCallQueue(Integer id);
    
    CallQueue getCallQueueByName(String name);

    void saveCallQueue(CallQueue callQueue);

    CallQueue newCallQueue();

    void duplicateCallQueues(Collection<Integer> ids);

    void deleteCallQueues(Collection<Integer> ids);
    
    void deleteCallQueue(String name);

    Collection<CallQueue> getCallQueues();

    /* CallQueueAgent API */
    CallQueueAgent loadCallQueueAgent(Integer id);

    void saveCallQueueAgent(CallQueueAgent callQueueAgent);

    CallQueueAgent newCallQueueAgent();
    
    CallQueueAgent getAgentByName(String agentName);

    void duplicateCallQueueAgents(Collection<Integer> ids);

    void deleteCallQueueAgents(Collection<Integer> ids);

    Collection<CallQueueAgent> getCallQueueAgents();

    Collection<CallQueueAgent> getCallQueueAgentsWithState();

    /* CallQueueTier API */
    List<CallQueue> getAvaiableQueuesForAgent(Integer agentid);

    List<Integer> getCallQueueAgentsForQueue(Integer callqueueid);
    
    void deleteCallQueueAgent(String name);
}
