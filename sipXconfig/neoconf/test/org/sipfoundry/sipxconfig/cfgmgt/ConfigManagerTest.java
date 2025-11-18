package org.sipfoundry.sipxconfig.cfgmgt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Pattern;

import org.junit.Test;

public class ConfigManagerTest {

    @Test
    public void test() {
        ConfigManager cfg = new ConfigManagerImpl();
        String expected = "/usr/bin/ssh -o 'StrictHostKeyChecking=no' -i /.+/.ssh/ppkeys/localhost.nopass.priv sipx@foo";
        String actual = cfg.getRemoteCommand("foo");
        if (!Pattern.matches(expected, actual)) {
            fail(String.format("'%s' ~= '%s'", expected, actual));            
        }
        assertTrue(true);
    }
}
