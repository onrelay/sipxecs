/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.common;

import java.util.Comparator;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.DateJavaType;


public class UtcTimestampType extends AbstractSingleColumnStandardBasicType<Date> {
    
    public static final UtcTimestampType INSTANCE = new UtcTimestampType();

    public UtcTimestampType() {
        super(UtcTimestampTypeDescriptor.INSTANCE, DateJavaType.INSTANCE);
    }

    @Override
    public String getName() {
        return "utc-timestamp";
    }

    public int getJdbcTypeCode() {
        return SqlTypes.TIMESTAMP;
    }

    public Date next(Date current, SharedSessionContractImplementor session) {
        return new Date(System.currentTimeMillis());
    }

    public Date seed(SharedSessionContractImplementor session) {
        return new Date(System.currentTimeMillis());
    }

    public Comparator<Date> getComparator() {
        return Comparator.naturalOrder();
    }

    public String objectToSQLString(Date value, Dialect dialect) throws Exception {
        return "'" + new java.sql.Timestamp(value.getTime()).toString() + "'";
    }

    public Date fromStringValue(String xml) throws HibernateException {
        return java.sql.Timestamp.valueOf(xml);
    }
}