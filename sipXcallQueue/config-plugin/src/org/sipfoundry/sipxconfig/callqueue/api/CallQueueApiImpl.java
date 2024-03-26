package org.sipfoundry.sipxconfig.callqueue.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.impl.ResponseUtils;
import org.sipfoundry.sipxconfig.api.model.SettingBean;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.callqueue.CallQueue;
import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.callqueue.CallQueueSettings;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTier;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTiers;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.springframework.beans.factory.annotation.Required;

public class CallQueueApiImpl implements CallQueueApi {
    private CallQueueContext m_callQueueContext;
    private BranchManager m_branchManager;
    
    @Override
    public Response getQueues() {
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        return buildCallQueuesList(callQueues);
    }

    @Override
    public Response newQueue(CallQueueBean callQueueBean) {
        CallQueue queue = m_callQueueContext.newCallQueue();
        convertToCallQueue(callQueueBean, queue);
        m_callQueueContext.saveCallQueue(queue);
        return Response.ok().entity(queue.getId()).build();
    }

    @Override
    public Response getQueue(String name) {
        CallQueue callQueue = m_callQueueContext.getCallQueueByName(name);
        return getQueue(callQueue);
    }
    
    private Response getQueue(CallQueue callQueue) {
        if (callQueue != null) {
            return Response.ok().entity(CallQueueBean.convertQueue(callQueue)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }    

    @Override
    public Response updateQueue(String name, CallQueueBean queueBean) {
        CallQueue callQueue = m_callQueueContext.getCallQueueByName(name);
        return updateQueue(callQueue, queueBean);
    }
    
    private Response updateQueue(CallQueue callQueue, CallQueueBean callQueueBean) {
        if (callQueue != null) {
            convertToCallQueue(callQueueBean, callQueue);
            m_callQueueContext.saveCallQueue(callQueue);
            return Response.ok().entity(callQueue.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }    

    @Override
    public Response deleteQueue(String name) {
        m_callQueueContext.deleteCallQueue(name);
        return Response.ok().build();
    }

    @Override
    public Response getAgents() {
        Collection<CallQueueAgent> callQueueAgents = m_callQueueContext.getCallQueueAgents();
        return buildCallQueueAgentsList(callQueueAgents);
    }

    @Override
    public Response newAgent(CallQueueAgentBean callQueueAgentBean) {
        CallQueueAgent queueAgent = m_callQueueContext.newCallQueueAgent();
        convertToCallQueueAgent(callQueueAgentBean, queueAgent);
        m_callQueueContext.saveCallQueueAgent(queueAgent);
        return Response.ok().entity(queueAgent.getId()).build();    }

    @Override
    public Response getAgent(String name) {
        CallQueueAgent callQueueAgent = m_callQueueContext.getAgentByName(name);
        return getAgent(callQueueAgent);
    }
    
    private Response getAgent(CallQueueAgent callQueueAgent) {
        if (callQueueAgent != null) {
            return Response.ok().entity(CallQueueAgentBean.convertAgent(callQueueAgent)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response updateAgent(String name, CallQueueAgentBean agentBean) {
        CallQueueAgent callQueueAgent = m_callQueueContext.getAgentByName(name);
        return updateAgent(callQueueAgent, agentBean);
    }
    
    private Response updateAgent(CallQueueAgent callQueueAgent, CallQueueAgentBean callQueueAgentBean) {
        if (callQueueAgent != null) {
            convertToCallQueueAgent(callQueueAgentBean, callQueueAgent);
            m_callQueueContext.saveCallQueueAgent(callQueueAgent);
            return Response.ok().entity(callQueueAgent.getId()).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response deleteAgent(String name) {
        m_callQueueContext.deleteCallQueueAgent(name);
        return Response.ok().build();
    }
    
    private Response buildCallQueuesList(Collection<CallQueue> callQueues) {
        if (callQueues != null) {
            return Response.ok().entity(CallQueueList.convertCallQueueList(callQueues)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
        
    public void convertToCallQueue(CallQueueBean callqueueBean, CallQueue callqueue) {
        String name = callqueueBean.getName();
        if (name != null) {
            callqueue.setName(name);
        }
        callqueue.setEnabled(callqueueBean.isEnabled());
        String did = callqueueBean.getDidNumber();
        if (did != null) {
            callqueue.setDid(did);
        }
        String extension = callqueueBean.getExtension();
        if (extension != null) {
            callqueue.setExtension(extension);
        }
        SettingsList settingsList = callqueueBean.getSettingsList();
        if (settingsList != null) {
            for (SettingBean settingBean : settingsList.getSettings()) {
                callqueue.setSettingValue(settingBean.getPath(), settingBean.getValue());
            }
        }

        Set<Branch> locations = new HashSet<Branch>(); 
        List<String> locationNames = callqueueBean.getLocations();
        List<Branch> branches = m_branchManager.getBranches();
        if (branches != null) {
            for (Branch branch : branches) {
                if (locationNames.contains(branch.getName())) {
                    locations.add(branch);
                }
            }
            callqueue.setLocations(locations);
        }
    }
    
    private Response buildCallQueueAgentsList(Collection<CallQueueAgent> callQueueAgents) {
        if (callQueueAgents != null) {
            return Response.ok().entity(CallQueueAgentList.convertCallQueueAgentList(callQueueAgents)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
        
    public void convertToCallQueueAgent(CallQueueAgentBean callqueueAgentBean, CallQueueAgent callqueueAgent) {
        String name = callqueueAgentBean.getName();
        if (name != null) {
            callqueueAgent.setName(name);
        }
        
        String state = callqueueAgentBean.getState();
        if (state != null) {
            callqueueAgent.setState(state);
        }
        String extension = callqueueAgentBean.getExtension();
        if (extension != null) {
            callqueueAgent.setExtension(extension);
        }
        String description = callqueueAgentBean.getDescription();
        if (description != null) {
            callqueueAgent.setDescription(description);
        }
        SettingsList settingsList = callqueueAgentBean.getSettingsList();
        if (settingsList != null) {
            for (SettingBean settingBean : settingsList.getSettings()) {
                callqueueAgent.setSettingValue(settingBean.getPath(), settingBean.getValue());
            }
        }
        List<CallQueueTierBean> members = callqueueAgentBean.getMembers();
        if (members != null) {
            CallQueueTiers tiers = callqueueAgent.getTiers();
            for (CallQueueTierBean member : members) {
                CallQueueTier tier = tiers.addTier(member.getQueueId(), callqueueAgent.getId());
                tier.setLevel(member.getLevel());
                tier.setPosition(member.getPosition());
            }
        }
    }
    
    @Required
    public void setCallQueueContext(CallQueueContext callQueueContext) {
        m_callQueueContext = callQueueContext;
    }
    
    @Required
    public void setBranchManager(BranchManager branchManager) {
        m_branchManager = branchManager;
    }

    @Override
    public Response getSettings(HttpServletRequest request) {
        CallQueueSettings settings = m_callQueueContext.getSettings();
        return Response.ok().entity(SettingsList.convertSettingsList(settings.getSettings(), request.getLocale())).build();
    }
        
    @Override
    public Response setSettings(SettingsList settingsList) {
        List<SettingBean> settingsBean =  settingsList.getSettings();
        CallQueueSettings cqSettings = m_callQueueContext.getSettings();
        for (SettingBean bean : settingsBean) {
            cqSettings.setSettingValue(bean.getPath(), bean.getValue());
        }
        m_callQueueContext.saveSettings(cqSettings);
        return Response.ok().build();
    }
}
