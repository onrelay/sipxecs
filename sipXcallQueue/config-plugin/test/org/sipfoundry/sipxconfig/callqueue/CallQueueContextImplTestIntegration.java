/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.callqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.registrar.Registrar;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CallQueueContextImplTestIntegration extends IntegrationTestCase {
    private CallQueueContext m_callQueueContext;
    private FeatureManager m_featureManager;
    private LocationsManager m_locationsManager;

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
        List<String> jars = new ArrayList<String>();
        jars.add("classpath:/org/sipfoundry/sipxconfig/system.beans.xml");
        jars.add("classpath:/sipxplugin.beans.xml");
        jars.add("classpath*:/org/sipfoundry/sipxconfig/*/**/*.beans.xml");        
        return new ClassPathXmlApplicationContext(jars.toArray(new String[0]));
    }

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        loadDataSetXml("callqueue/CallQueueSeed.xml");
        loadDataSetXml("commserver/seedLocations.xml");
        m_featureManager.enableLocationFeature(Registrar.FEATURE, m_locationsManager.getPrimaryLocation(), true);
        m_featureManager.enableLocationFeature(FreeswitchFeature.FEATURE, m_locationsManager.getPrimaryLocation(), true);       
        m_featureManager.enableLocationFeature(CallQueueContext.FEATURE, m_locationsManager.getPrimaryLocation(), true);
        
    }

// Utility methods
    public void setCallQueueContext(CallQueueContext callQueueContext) {
        m_callQueueContext = callQueueContext;
    }

// Test methods for CallQueue
    public void testNewCallQueue() throws Exception {
        CallQueue callQueue = m_callQueueContext.newCallQueue();
        callQueue.setName("Queue 10");
        callQueue.setExtension("8110");
        m_callQueueContext.saveCallQueue(callQueue);
        commit();
        // table should have additional row now - 8 = 4 static CallQueues + 1 dynamic CallQueue + 3 CallQueueCommands
        assertEquals(8, countRowsInTable("freeswitch_extension"));
    }

    public void testLoadCallQueue() throws Exception {
        CallQueue callQueue = m_callQueueContext.loadCallQueue(new Integer(300001));
        assertEquals("Queue 1", callQueue.getName());
        assertEquals("8101", callQueue.getDid());
    }

    public void testGetCallQueues() throws Exception {
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        assertEquals(4, callQueues.size());
    }

    public void testRemoveCallQueues() throws Exception {
        Collection<Integer> callQueueIds = new HashSet<Integer>(Arrays.asList(new Integer(300004)));
        m_callQueueContext.deleteCallQueues(callQueueIds);
        commit();
        // table should have less rows now - 6
        assertEquals(6, countRowsInTable("freeswitch_extension"));
    }

    public void testDuplicateCallQueues() throws Exception {
        Collection<Integer> callQueueIds = new HashSet<Integer>(Arrays.asList(new Integer(300001), new Integer(300002), new Integer(300003)));
        m_callQueueContext.duplicateCallQueues(callQueueIds);
        commit();
        // table should have additional row now - 10
        assertEquals(10, countRowsInTable("freeswitch_extension"));
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        assertEquals(7, callQueues.size());
        ArrayList<CallQueue> a = new ArrayList(callQueues);
        compareClonedQueue(a.get(0), a.get(4));
        compareClonedQueue(a.get(1), a.get(5));
        compareClonedQueue(a.get(2), a.get(6));
    }

// Test methods for CallQueueAgent
    public void testNewCallQueueAgent() throws Exception {
        CallQueueAgent callQueueAgent = m_callQueueContext.newCallQueueAgent();
        callQueueAgent.setName("Agent 10");
        callQueueAgent.setExtension("4010");
        m_callQueueContext.saveCallQueueAgent(callQueueAgent);
        commit();
        // table should have additional row now - 3
        assertEquals(4, countRowsInTable("call_queue_agent"));
    }

    public void testLoadCallQueueAgent() throws Exception {
        CallQueueAgent callQueueAgent = m_callQueueContext.loadCallQueueAgent(new Integer(100001));
        assertEquals("Agent 1", callQueueAgent.getName());
        assertEquals("4001", callQueueAgent.getExtension());
    }

    public void testGetCallQueueAgents() throws Exception {
        Collection<CallQueueAgent> callQueueAgents = m_callQueueContext.getCallQueueAgents();
        assertEquals(3, callQueueAgents.size());
    }

    public void testDeleteCallQueueAgents() throws Exception {
        Collection<Integer> callQueueAgentIds = new HashSet<Integer>(Arrays.asList(new Integer(100001), new Integer(100002), new Integer(100003)));
        m_callQueueContext.deleteCallQueueAgents(callQueueAgentIds);
        commit();
        // table should have no rows now - 0
        assertEquals(0, countRowsInTable("call_queue_agent"));
    }

    public void _testDuplicateCallQueueAgents() throws Exception {
        Collection<Integer> callQueueAgentIds = new HashSet<Integer>(Arrays.asList(new Integer(100001), new Integer(100002), new Integer(100003)));
        m_callQueueContext.duplicateCallQueueAgents(callQueueAgentIds);
        commit();
        // table should have additional row now - 6
        assertEquals(6, countRowsInTable("call_queue_agent"));
        Collection<CallQueueAgent> callQueueAgents = m_callQueueContext.getCallQueueAgents();
        assertEquals(6, callQueueAgents.size());
        ArrayList<CallQueueAgent> a = new ArrayList(callQueueAgents);
        compareTiersForClonedAgent(a.get(0), a.get(5));
        compareTiersForClonedAgent(a.get(1), a.get(3));
        compareTiersForClonedAgent(a.get(2), a.get(4));
    }

// Test methods for CallQueueTier
    public void testGetCallQueueTiersForAgent() {
        Collection<CallQueueTier> callQueueTiers100001 = m_callQueueContext.loadCallQueueAgent(new Integer(100001)).getTiers().getTiers();
        Collection<CallQueueTier> callQueueTiers100002 = m_callQueueContext.loadCallQueueAgent(new Integer(100002)).getTiers().getTiers();
        Collection<CallQueueTier> callQueueTiers100003 = m_callQueueContext.loadCallQueueAgent(new Integer(100003)).getTiers().getTiers();
        assertEquals(2, callQueueTiers100001.size());
        assertEquals(1, callQueueTiers100002.size());
        assertEquals(0, callQueueTiers100003.size());
    }

    public void testGetAvaiableQueuesForAgent(){
        List<CallQueue> callQueues100001 = m_callQueueContext.getAvaiableQueuesForAgent(new Integer(100001));
        List<CallQueue> callQueues100002 = m_callQueueContext.getAvaiableQueuesForAgent(new Integer(100002));
        List<CallQueue> callQueues100003 = m_callQueueContext.getAvaiableQueuesForAgent(new Integer(100003));
        assertEquals(2, callQueues100001.size());
        assertEquals(3, callQueues100002.size());
        assertEquals(4, callQueues100003.size());
    }


    // Utility methods - all private

    // Method is recursive - this is normal, we need to follow settings tree
    private void compareSettingsForCloned(Setting o, Setting c) {
        // Compare Settings count in root element for orginal and clone object
        if (o.isLeaf()) {
            assertEquals(o.getValue(), c.getValue());
        } else {
            assertEquals(o.getValues().size(), c.getValues().size());
            for (Setting oS : o.getValues()) {
                Setting cS = c.getSetting(oS.getName());
                assertEquals(true, null != cS);
                compareSettingsForCloned(oS, cS);
            }
        }
    }

    private void compareTiersForClonedAgent(CallQueueAgent o, CallQueueAgent c) {
        assertEquals(null, c.getExtension());
        // Compare original and cloned CallQeuueAgent tiers count
        assertEquals(o.getTiers().getTiers().size(), c.getTiers().getTiers().size());
        ArrayList<CallQueueTier> oTiers = new ArrayList(o.getTiers().getTiers());
        ArrayList<CallQueueTier> cTiers = new ArrayList(c.getTiers().getTiers());
        for (Integer i = 0 ; i < o.getTiers().getTiers().size(); i++) {
            CallQueueTier ot = oTiers.get(i);
            CallQueueTier ct = cTiers.get(i);
            assertEquals(ot.getCallQueueId(), ct.getCallQueueId());
            assertEquals(ot.getPosition(), ct.getPosition());
            assertEquals(ot.getLevel(), ct.getLevel());
        }
        compareSettingsForCloned(o.getSettings(), c.getSettings());
    }

    private void compareClonedQueue(CallQueue o, CallQueue c) {
        assertEquals(null, c.getDid());
        compareSettingsForCloned(o.getSettings(), c.getSettings());
    }

    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }
}
