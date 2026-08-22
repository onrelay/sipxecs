package org.sipfoundry.sipxconfig.webrtc.device;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class WebrtcPhone extends Phone {
    public static final String MIME_TYPE_JSON = "application/json";

    private static final String DISPLAY_NAME = "identity/display_name";
    private static final String AOR = "identity/aor";
    private static final String USERNAME = "auth/username";
    private static final String PASSWORD = "auth/password";

    @Override
    protected void setLineInfo(Line line, LineInfo info) {
        line.setSettingValue(DISPLAY_NAME, info.getDisplayName());
        line.setSettingValue(USERNAME, info.getUserId());
        line.setSettingValue(PASSWORD, info.getPassword());

        String userId = info.getUserId();
        String registrationServer = info.getRegistrationServer();
        if (userId != null && userId.contains("@")) {
            line.setSettingValue(AOR, userId);
        } else if (userId != null && registrationServer != null) {
            line.setSettingValue(AOR, userId + "@" + registrationServer);
        }
    }

    @Override
    protected LineInfo getLineInfo(Line line) {
        LineInfo info = new LineInfo();
        info.setDisplayName(line.getSettingValue(DISPLAY_NAME));
        info.setUserId(line.getSettingValue(USERNAME));
        info.setPassword(line.getSettingValue(PASSWORD));

        String aor = line.getSettingValue(AOR);
        if (aor != null) {
            int at = aor.indexOf('@');
            if (at > -1 && at + 1 < aor.length()) {
                info.setRegistrationServer(aor.substring(at + 1));
            }
        }
        return info;
    }

    @Override
    public void initializeLine(Line line) {
        line.addDefaultBeanSettingHandler(new WebrtcLineDefaults(line));
    }

    @Override
    public String getProfileFilename() {
        return getSerialNumber() + ".json";
    }

    @Override
    public Profile[] getProfileTypes() {
        return new Profile[] {
            new PhoneProfile(getProfileFilename())
        };
    }

    static class PhoneProfile extends Profile {
        PhoneProfile(String name) {
            super(name, MIME_TYPE_JSON);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext<WebrtcPhone> createContext(Device device) {
            WebrtcPhone phone = (WebrtcPhone) device;
            return new WebrtcProfileContext(phone, phone.getModel().getProfileTemplate());
        }
    }

    public static class WebrtcLineDefaults {
        private final Line m_line;
        private final User m_user;

        public WebrtcLineDefaults(Line line) {
            m_line = line;
            m_user = m_line.getUser();
        }

        @SettingEntry(path = DISPLAY_NAME)
        public String getDisplayName() {
            if (m_user == null) {
                return null;
            }
            return m_user.getDisplayName();
        }

        @SettingEntry(path = AOR)
        public String getAddressOfRecord() {
            DeviceDefaults defaults = m_line.getPhoneContext().getPhoneDefaults();
            return m_line.getUserName() + "@" + defaults.getDomainName();
        }

        @SettingEntry(path = USERNAME)
        public String getUsername() {
            return m_line.getUserName();
        }

        @SettingEntry(path = PASSWORD)
        public String getPassword() {
            if (m_user == null) {
                return null;
            }
            return m_user.getSipPassword();
        }
    }
}
