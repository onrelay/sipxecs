package org.sipfoundry.sipxconfig.callqueue.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.sipfoundry.sipxconfig.api.model.SettingsList;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.callqueue.CallQueue;

@XmlRootElement(name = "queue")
@XmlType(propOrder = {
    "id", "enabled", "name", "description", "extension", "didNumber", "locations",
    "settingsList"
    })
@JsonPropertyOrder({
    "id", "enabled", "name", "description", "extension", "didNumber", "locations",
    "settingsList"
    })
public class CallQueueBean {
    private int m_id;
    private boolean m_enabled;
    private SettingsList m_settingsList;
    private String m_name;
    private String m_description;
    private String m_extension;
    private String m_didNumber;
    private List<String> m_locations = new ArrayList<String>();
    public int getId() {
        return m_id;
    }
    public void setId(int id) {
        m_id = id;
    }
    public boolean isEnabled() {
        return m_enabled;
    }
    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
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
    public String getDidNumber() {
        return m_didNumber;
    }
    public void setDidNumber(String didNumber) {
        m_didNumber = didNumber;
    }
    public List<String> getLocations() {
        return m_locations;
    }
    public void setLocations(List<String> locations) {
        m_locations = locations;
    }  
    public SettingsList getSettingsList() {
        return m_settingsList;
    }
    public void setSettingsList(SettingsList settingsList) {
        m_settingsList = settingsList;
    }
    
    public static CallQueueBean convertQueue(CallQueue callQueue) {
        CallQueueBean bean = new CallQueueBean();
        bean.setId(callQueue.getId());
        bean.setName(callQueue.getName());
        bean.setDidNumber(callQueue.getDid());
        bean.setEnabled(callQueue.isEnabled());
        bean.setExtension(callQueue.getExtension());
        bean.setSettingsList(SettingsList.convertSettingsList(callQueue.getSettings(), Locale.ENGLISH));
        Set<Branch> locations = callQueue.getLocations();
        List<String> locationNames = new ArrayList<String>();
        for (Branch location : locations) {
            locationNames.add(location.getName());
        }
        bean.setLocations(locationNames);
        return bean;
    }
}
