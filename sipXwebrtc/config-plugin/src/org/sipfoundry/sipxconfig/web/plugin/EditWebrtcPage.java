package org.sipfoundry.sipxconfig.web.plugin;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.webrtc.Webrtc;
import org.sipfoundry.sipxconfig.webrtc.WebrtcSettings;

public abstract class EditWebrtcPage extends PageWithCallback implements PageBeginRenderListener {
    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:webrtc")
    public abstract Webrtc getWebrtc();

    public abstract WebrtcSettings getSettings();

    public abstract void setSettings(WebrtcSettings settings);

    @Override
    public void pageBeginRender(PageEvent event) {
        if (getSettings() == null) {
            setSettings(getWebrtc().getSettings());
        }
    }

    public void apply() {
        getWebrtc().saveSettings(getSettings());
    }
}