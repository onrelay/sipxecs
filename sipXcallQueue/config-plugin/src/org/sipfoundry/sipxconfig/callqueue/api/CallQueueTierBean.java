package org.sipfoundry.sipxconfig.callqueue.api;

public class CallQueueTierBean {
    private Integer m_queueId;
    private Integer m_position = 1;
    private Integer m_level = 1;
    
    public Integer getQueueId() {
        return m_queueId;
    }
    public void setQueueId(Integer queueId) {
        m_queueId = queueId;
    }
    public Integer getPosition() {
        return m_position;
    }
    public void setPosition(Integer position) {
        m_position = position;
    }
    public Integer getLevel() {
        return m_level;
    }
    public void setLevel(Integer level) {
        m_level = level;
    }   
}
