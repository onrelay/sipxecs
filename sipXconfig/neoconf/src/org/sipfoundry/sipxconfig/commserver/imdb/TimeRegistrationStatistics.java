package org.sipfoundry.sipxconfig.commserver.imdb;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TimeRegistrationStatistics {
    @Indexed
    private Date m_time;
    private int m_total;
    private int m_active;
    
    public Date getTime() {
        return m_time;
    }
    public void setTime(Date time) {
        m_time = time;
    }
    public int getTotal() {
        return m_total;
    }
    public void setTotal(int total) {
        m_total = total;
    }
    public int getActive() {
        return m_active;
    }
    public void setActive(int active) {
        m_active = active;
    }    
}
