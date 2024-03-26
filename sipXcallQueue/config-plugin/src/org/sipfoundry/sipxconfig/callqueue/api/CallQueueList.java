package org.sipfoundry.sipxconfig.callqueue.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.callqueue.CallQueue;

@XmlRootElement(name = "queues")
public class CallQueueList {
    private List<CallQueueBean> m_callQueues;

    public void setCallQueues(List<CallQueueBean> callQueues) {
        m_callQueues = callQueues;
    }

    @XmlElement(name = "queue")
    public List<CallQueueBean> getCallQueues() {
        if (m_callQueues == null) {
            m_callQueues = new ArrayList<CallQueueBean>();
        }
        return m_callQueues;
    }

    public static CallQueueList convertCallQueueList(Collection<CallQueue> callQueues) {
        List<CallQueueBean> callQueueList = new ArrayList<CallQueueBean>();
        for (CallQueue callQueue : callQueues) {
            callQueueList.add(CallQueueBean.convertQueue(callQueue));
        }
        CallQueueList list = new CallQueueList();
        list.setCallQueues(callQueueList);
        return list;
    }
}
