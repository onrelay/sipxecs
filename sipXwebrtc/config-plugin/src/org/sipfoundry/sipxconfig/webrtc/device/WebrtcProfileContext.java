package org.sipfoundry.sipxconfig.webrtc.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;

public class WebrtcProfileContext extends ProfileContext<WebrtcPhone> {
    public WebrtcProfileContext(WebrtcPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        mapDataInContext(context);
        return context;
    }

    public void mapDataInContext(Map<String, Object> context) {
        WebrtcPhone phone = getDevice();
        context.put("phone", phone);

        List<Line> lines = phone.getLines();
        HashMap<String, Line> linesSettings = new HashMap<String, Line>();
        for (Line line : lines) {
            linesSettings.put(line.getUserName(), line);
        }
        context.put("lines", linesSettings);
    }
}
