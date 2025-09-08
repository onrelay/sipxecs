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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang.enums.Enum;
import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Hibernate 7 compatible EnumUserType for commons-lang Enum.
 */
public class EnumUserType implements UserType {

    private final Class<? extends Enum> enumClass;

    public EnumUserType(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        initStaticFields();
    }

    private void initStaticFields() {
        Field[] fields = enumClass.getFields();
        if (fields.length > 0) {
            try {
                fields[0].get(null);
            } catch (Exception e) {
                LogFactory.getLog(getClass()).debug("Initializing static fields for: " + enumClass);
            }
        }
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<?> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return x == y || (x != null && x.equals(y));
    }

    @Override
    public int hashCode(Object x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position,
                              SharedSessionContractImplementor session, Object owner) throws SQLException {
        String name = rs.getString(position);
        if (rs.wasNull() || name == null) {
            return null;
        }
        return EnumUtils.getEnum(enumClass, name);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, ((Enum) value).getName());
        }
    }

    @Override
    public Object deepCopy(Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) {
        return value == null ? null : ((Enum) value).getName();
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached == null ? null : EnumUtils.getEnum(enumClass, cached.toString());
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }
}