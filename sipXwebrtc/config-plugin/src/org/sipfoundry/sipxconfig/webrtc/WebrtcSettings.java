package org.sipfoundry.sipxconfig.webrtc;

import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class WebrtcSettings extends PersistableSettings {
    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxwebrtc/sipxwebrtc.xml");
    }

    @Override
    public String getBeanId() {
        return "webrtcSettings";
    }
}