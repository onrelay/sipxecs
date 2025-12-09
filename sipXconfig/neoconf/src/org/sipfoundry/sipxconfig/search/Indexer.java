/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import org.hibernate.type.Type;

public interface Indexer {
    public String DEFAULT_FIELD = "all";
    public String CLASS_FIELD = "class";

    void indexBean(Object bean, Object id, Object[] state, String[] fieldNames,
            Type[] types, boolean newInstance);

    void removeBean(Object bean, Object id);

    void open();

    void close();
}
