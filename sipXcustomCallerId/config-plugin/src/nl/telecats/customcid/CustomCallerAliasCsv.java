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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.sipfoundry.sipxconfig.bulk.csv.CsvParserImpl;
import org.sipfoundry.sipxconfig.bulk.csv.SimpleCsvWriter;

public class CustomCallerAliasCsv {

    public List<CustomCallerAlias> parse(Reader csvRdr) throws IOException {
        final List<CustomCallerAlias> rows = new ArrayList<CustomCallerAlias>();
        CsvParserImpl csv = new CsvParserImpl();
        Closure converter = new Closure() {
            public void execute(Object o) {
                String[] line = (String[]) o;
                rows.add(new CustomCallerAlias(line[0], line[1]));
            }
        };
        csv.parse(csvRdr, converter);
        return rows;
    }

    public void write(Collection<CustomCallerAlias> in, Writer w) throws IOException {
        SimpleCsvWriter csv = new SimpleCsvWriter(w, true, new String[] {"from", "to"});
        String[] fields = new String[2];
        for (CustomCallerAlias alias : in) {
            fields[0] = alias.getFrom();
            fields[1] = alias.getTo();
            csv.write(fields);
        }
    }
}
