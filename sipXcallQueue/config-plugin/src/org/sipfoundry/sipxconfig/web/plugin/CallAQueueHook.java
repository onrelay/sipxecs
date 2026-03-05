
package org.sipfoundry.sipxconfig.web.plugin;

import org.sipfoundry.sipxconfig.site.PluginHook;

public class CallAQueueHook implements PluginHook {

    private String m_hookId;
    private String m_featureId;

    @Override
    public String getHookId() {
        return m_hookId;
    }

    public void setHookId( String hookId ) {
        m_hookId = hookId;
    }

    @Override
    public String getFeatureId() {
        return m_featureId;
    }

    public void setFeatureId( String featureId ) {
        m_featureId = featureId;
    }
}
