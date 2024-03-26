package org.sipfoundry.sipxconfig.callqueue.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;

@XmlRootElement(name = "agents")
public class CallQueueAgentList {
    private List<CallQueueAgentBean> m_callQueueAgents;

    public void setCallQueueAgents(List<CallQueueAgentBean> callQueueAgents) {
        m_callQueueAgents = callQueueAgents;
    }

    @XmlElement(name = "agent")
    public List<CallQueueAgentBean> getCallQueueAgents() {
        if (m_callQueueAgents == null) {
            m_callQueueAgents = new ArrayList<CallQueueAgentBean>();
        }
        return m_callQueueAgents;
    }

    public static CallQueueAgentList convertCallQueueAgentList(Collection<CallQueueAgent> callQueueAgentList) {
        List<CallQueueAgentBean> callQueueAgents = new ArrayList<CallQueueAgentBean>();
        for (CallQueueAgent callQueueAgent : callQueueAgentList) {
            callQueueAgents.add(CallQueueAgentBean.convertAgent(callQueueAgent));
        }
        CallQueueAgentList list = new CallQueueAgentList();
        list.setCallQueueAgents(callQueueAgents);
        return list;
    }
}
