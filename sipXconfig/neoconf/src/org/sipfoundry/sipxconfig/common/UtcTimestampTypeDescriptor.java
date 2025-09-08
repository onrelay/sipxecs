/**
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.common;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.java.JavaType;

public class UtcTimestampTypeDescriptor implements JdbcType {

    public static final UtcTimestampTypeDescriptor INSTANCE = new UtcTimestampTypeDescriptor();
    private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    @Override
    public int getJdbcTypeCode() {
        return Types.TIMESTAMP;
    }

    @Override
    public <X> ValueBinder<X> getBinder(final JavaType<X> javaType) {
        return new ValueBinder<X>() {
            @Override
            public void bind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                Timestamp ts = javaType.unwrap(value, Timestamp.class, options);
                st.setTimestamp(index, ts, UTC);
            }

            @Override
            public void bind(CallableStatement statement, X value, String name, WrapperOptions options) throws SQLException {
                Timestamp ts = javaType.unwrap(value, Timestamp.class, options);
                statement.setTimestamp(name, ts, UTC);
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(final JavaType<X> javaType) {
        return new ValueExtractor<X>() {
            @Override
            public X extract(ResultSet rs, int position, WrapperOptions options) throws SQLException {
                Timestamp ts = rs.getTimestamp(position, UTC);
                return javaType.wrap(ts, options);
            }

            @Override
            public X extract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                Timestamp ts = statement.getTimestamp(index, UTC);
                return javaType.wrap(ts, options);
            }

            @Override
            public X extract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                Timestamp ts = statement.getTimestamp(name, UTC);
                return javaType.wrap(ts, options);
            }
        };
    }
}
