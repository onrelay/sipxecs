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

import org.hibernate.Session;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.Transformer;

/**
 * BeanWithId - simplify implementation of the model layer
 *
 * Hibernate advises against using object IDs in equals and hashCode methods, as we do below. See
 * http://www.hibernate.org/109.html . However, we disagree. It's true that using the ID means
 * that an unsaved object (with ID = -1) doesn't have a unique identity and that can cause
 * problems. However, if you use "business keys" like, for example, the object name, in the equals
 * and hashcode methods, then the object identity changes if you change the values of those keys,
 * which is also bad. We feel that the unsaved object problems are easier to deal with.
 */
public class BeanWithId implements PrimaryKeySource, Cloneable {
    public static final Integer UNSAVED_ID = Integer.valueOf(-1);
    public static final String ID_PROPERTY = "id";

    private static int s_id = 1;

    private Integer m_id;

    public BeanWithId() {
        this(UNSAVED_ID);
    }

    public BeanWithId(Integer id) {
        setId(id);
    }

    void setId(Integer id) {
        m_id = id;
    }

    public Integer getId() {
        return m_id;
    }

    /**
     * Checks if the object has been saved to the database Works because hibernate changes id when
     * the object is saved
     *
     * @return true if the object has never been saved
     */
    @JsonIgnore
    public boolean isNew() {
        return UNSAVED_ID.equals(getId());
    }

    public boolean equals(Object o) {
        if (this == o) return true; // Identity first!
        if (!(o instanceof BeanWithId)) return false;
        BeanWithId other = (BeanWithId) o;

        // If both are new (-1), they are NOT the same object 
        // unless they are the same instance (checked above).
        if (this.isNew() || other.isNew()) return false; 

        return getId().equals(other.getId());
    }

    public int hashCode() {
        // If the entity is NOT saved yet, use the JVM's default identity hash.
        // This ensures two different "New" objects have different hashCodes.
        if (isNew()) {
            return System.identityHashCode(this);
        }
        // Once saved, the ID is the stable identity.
        return m_id.hashCode();
    }

    public void update(BeanWithId object) {
        try {
            Integer saveId = getId();
            BeanUtils.copyProperties(this, object);
            setId(saveId);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Special version of clone, replaces beanId with a new one in a cloned object. Do not
     * override duplicate, override clone instead.
     */
    public final BeanWithId duplicate() {
        try {
            BeanWithId clone = (BeanWithId) clone();
            clone.setId(UNSAVED_ID);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Assigns a unique id to a newly created object.
     *
     * For testing only. Most objects are created with id -1 and hibernate sets a proper id. We
     * want to be able to set the id to a unique value in tests.
     *
     * @return the same object - to allow for chaining calls
     */
    public BeanWithId setUniqueId() {
        setId(Integer.valueOf(s_id++));
        return this;
    }

    /**
     * Assigns a id to a newly created object.
     *
     * For testing ONLY. Most objects are created with id -1 and hibernate sets a proper id. We
     * want to be able to set the id to a unique value in tests.
     *
     * @return the same object - to allow for chaining calls
     */

    public BeanWithId setUniqueId(int val) {
        setId(Integer.valueOf(val));
        return this;
    }

    public static final class BeanToId implements Transformer {
        public Object transform(Object item) {
            BeanWithId bean = (BeanWithId) item;
            return bean.getId();
        }
    }

    public static final class IdToBean implements Transformer {
        private final Session m_session;
        private final Class<?> m_klass;

        public IdToBean(Session session, Class<?> klass) {
            this.m_session = session;
            this.m_klass = klass;
        }

        @Override
        public Object transform(Object input) {
            Object entity = m_session.find(m_klass, (Serializable) input);
            if (entity == null) {
                throw new RuntimeException("Entity not found: " + input);
            }
            return entity;        
        }
    }

    /**
     * Implementation of PrimaryKeySource
     */
    @JsonIgnore
    public Object getPrimaryKey() {
        return getId();
    }
}
