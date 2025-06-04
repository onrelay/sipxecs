/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.setting.type;

import junit.framework.TestCase;

public class RealSettingTest extends TestCase {

    public void testConvertToTypedValue() {
        SettingType type = new RealSetting();
        assertEquals(Double.valueOf(51), type.convertToTypedValue("51"));
        assertNull(type.convertToTypedValue("kuku"));
    }

    public void testConvertToStringValue() {
        SettingType type = new RealSetting();
        assertEquals("52.51", type.convertToStringValue(Float.valueOf(52.51f)));
        assertNull(type.convertToStringValue(null));
    }
}
