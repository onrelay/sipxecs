package org.sipfoundry.sipxconfig.callqueue.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTier;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTiers;

@XmlRootElement(name = "agent")
@XmlType(propOrder = {
    "id", "name", "extension", "description", "state", "settingsList"
    })
@JsonPropertyOrder({
    "id", "name", "extension", "description", "state", "settingsList"
    })
public class CallQueueAgentBean {
    private int m_id;
    private String m_name;
    private String m_extension;
    private String m_description;
    private String m_state;
    private SettingsList m_settingsList;
    private List<CallQueueTierBean> m_members = new ArrayList<CallQueueTierBean>();
    public int getId() {
        return m_id;
    }
    public void setId(int id) {
        m_id = id;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public String getDescription() {
        return m_description;
    }
    public void setDescription(String description) {
        m_description = description;
    }   
    public String getExtension() {
        return m_extension;
    }
    public void setExtension(String extension) {
        m_extension = extension;
    }
    public String getState() {
        return m_state;
    }
    public void setState(String state) {
        m_state = state;
    }
    public SettingsList getSettingsList() {
        return m_settingsList;
    }
    public void setSettingsList(SettingsList settingsList) {
        m_settingsList = settingsList;
    }    
    public List<CallQueueTierBean> getMembers() {
        return m_members;
    }
    public void setMembers(List<CallQueueTierBean> members) {
        m_members = members;
    }
    public static CallQueueAgentBean convertAgent(CallQueueAgent callQueueAgent) {
        CallQueueAgentBean bean = new CallQueueAgentBean();
        bean.setId(callQueueAgent.getId());
        bean.setName(callQueueAgent.getName());
        bean.setExtension(callQueueAgent.getExtension());
        bean.setDescription(callQueueAgent.getDescription());
        bean.setState(callQueueAgent.getState());
        bean.setSettingsList(SettingsList.convertSettingsList(callQueueAgent.getSettings(), Locale.ENGLISH));
        CallQueueTiers tiers = callQueueAgent.getTiers();
        for (CallQueueTier tier : tiers.getTiers()) {
            CallQueueTierBean tierBean = new CallQueueTierBean();
            tierBean.setLevel(tier.getLevel());
            tierBean.setPosition(tier.getPosition());
            tierBean.setQueueId(tier.getCallQueueId());
            bean.getMembers().add(tierBean);
        }
        return bean;
    }
}
