package org.sipfoundry.sipxconfig.firewall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.sipfoundry.sipxconfig.apiban.BannedApi;

public class BannedTimer {
    private BannedApi m_bannedApi;
    private FirewallManager m_firewallManager;
    private int m_counter = 0;
    private String m_key;

    private static final Log LOG = LogFactory.getLog(BannedTimer.class);

    
    public void setBannedApi(BannedApi bannedApi) {
        m_bannedApi = bannedApi;
    }

    public void saveBannedIps() {
        LOG.debug("APIBAN Key : " + m_key);
        if (m_key == null) {
            return;
        }
        FirewallSettings settings = m_firewallManager.getSettings();
        int poolingPeriod = settings.getBannedPoolingPeriod();
        if (poolingPeriod != 0) {
            if (m_counter < poolingPeriod) {
                m_counter ++;
                LOG.debug("Banned Timer counter " + m_counter + " pooling period " + poolingPeriod);
                if (m_counter == poolingPeriod) {
                    LOG.debug("Banned Timer counter " + m_counter + " ready to save apiban banned list");
                    try {
                        String bannedIps = StringUtils.join(m_bannedApi.getBanned().getIpaddress(), ',');
                        String oldBannedIps = settings.getApibanIps();
                        if (!StringUtils.equals(bannedIps, oldBannedIps)) {
                            LOG.debug("Banned ips list changed  - save new list");                        	
                        	settings.setApibanIps(bannedIps);
                        	m_firewallManager.saveSettings(settings);
                        }
                    } catch (Exception ex) {
                        LOG.error("APIBAN Call failed with: ", ex);
                    }
                    m_counter = 0;
                }
            }
        }
    }

    
    public void setFirewallManager(FirewallManager firewallManager) {
        m_firewallManager = firewallManager;
    }

    
    public void setKey(String key) {
        m_key = key;
    }        
}
