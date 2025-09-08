package org.sipfoundry.sipxconfig.apiban.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@XmlRootElement(name = "ipaddress")

@JsonPropertyOrder({
        "ipaddress", "ID"
    })
public class BannedBean {
    private List<String> m_ipaddress;    
    private String m_ID;
    
    @JsonProperty("ipaddress")
    public List<String> getIpaddress() {
        return m_ipaddress;
    }
    
    public void setIpaddress(List<String> ipaddress) {
        m_ipaddress = ipaddress;
    }
    
    @JsonProperty("ID")
    public String getID() {
        return m_ID;
    }

    public void setID(String iD) {
        m_ID = iD;
    }
}
