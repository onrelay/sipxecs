package org.sipfoundry.sipxconfig.phone.polycom;

import junit.framework.TestCase;

public class PolycomModelTest extends TestCase {

    public void testGetVersion() {
        assertEquals(PolycomModel.VER_3_1_X, PolycomModel.getPhoneDeviceVersion("3.1.X"));
        assertEquals(PolycomModel.VER_3_2_X, PolycomModel.getPhoneDeviceVersion("3.2.X"));
        assertEquals(PolycomModel.VER_4_0_X, PolycomModel.getPhoneDeviceVersion("4.0.X"));
        assertEquals(PolycomModel.VER_2_0, PolycomModel.getPhoneDeviceVersion("X.X.X"));
    }

    public void testCheck() {
        assertEquals(0, PolycomModel.compareVersions(PolycomModel.VER_4_0_X, new Integer[] {
            4, 0
        }));
        assertEquals(1, PolycomModel.compareVersions(PolycomModel.VER_4_0_X, new Integer[] {
            3, 2
        }));
        assertEquals(-1, PolycomModel.compareVersions(PolycomModel.VER_4_0_X, new Integer[] {
            4, 1
        }));
        assertEquals(0, PolycomModel.compareVersions(PolycomModel.VER_4_1_X, new Integer[] {
            4, 1
        }));
        assertEquals(0, PolycomModel.compareVersions(PolycomModel.VER_4_1_X, new Integer[] {
            4
        }));
        assertEquals(-3, PolycomModel.compareVersions(PolycomModel.VER_4_1_X, new Integer[] {
            4, 1, 3
        }));
        assertEquals(1, PolycomModel.compareVersions(PolycomModel.VER_5_9_8, new Integer[] {
            5, 9, 8
        }));
        assertEquals(-1, PolycomModel.compareVersions(PolycomModel.VER_6_4_6, new Integer[] {
            6, 4, 6
        }));
    }

    public void testNullVersion() {
    }
}
