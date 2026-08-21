package org.sipfoundry.sipxconfig.webrtc;

import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class WebrtcImpl implements Webrtc {
    private BeanWithSettingsDao<WebrtcSettings> m_settingsDao;

    @Override
    public WebrtcSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(WebrtcSettings settings) {
        m_settingsDao.upsert(settings);
    }

    public void setSettingsDao(BeanWithSettingsDao<WebrtcSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
}