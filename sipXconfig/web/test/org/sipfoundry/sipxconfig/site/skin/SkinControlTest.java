/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.skin;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.tapestry.IAsset;
import org.easymock.EasyMock;

public class SkinControlTest extends TestCase {

    public void testGetStylesheetAssets() {
        DummySkinControl skin = new DummySkinControl();
        IAsset[] firefox = skin.getStylesheetAssets("foo");
        assertEquals(2, firefox.length);
    }

    static class DummySkinControl extends SkinControl {
        Set m_paths = new HashSet();
        public IAsset getAsset(String path) {
            m_paths.add(path);
            return EasyMock.createNiceControl().createMock(IAsset.class);
        }
    }
}
