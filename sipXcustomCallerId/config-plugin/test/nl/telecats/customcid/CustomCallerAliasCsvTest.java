/*
 * Copyright (c) 2011 Telecats B.V. All rights reserved. Contributed to
 * SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 * This library or application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public
 * License (AGPL) as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library or application is distributed in the hope that it will be
 * useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License (AGPL) for more details.
 *
 * $
 */
package nl.telecats.customcid;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class CustomCallerAliasCsvTest {

    @Test
    public void testParse() throws IOException {
        CustomCallerAliasCsv p = new CustomCallerAliasCsv();
        StringReader in = new StringReader("from,to\na,b");
        List<CustomCallerAlias> actual = p.parse(in);
        assertEquals(1, actual.size());
        assertEquals("a", actual.get(0).getFrom());
        assertEquals("b", actual.get(0).getTo());
    }

    @Test
    public void testWriter() throws IOException {
        Collection<CustomCallerAlias> src = Collections.singleton(new CustomCallerAlias("x", "y"));
        CustomCallerAliasCsv p = new CustomCallerAliasCsv();
        StringWriter actual = new StringWriter();
        p.write(src, actual);
        assertEquals("\"from\",\"to\"\n\"x\",\"y\"\n", actual.toString());
    }
}
