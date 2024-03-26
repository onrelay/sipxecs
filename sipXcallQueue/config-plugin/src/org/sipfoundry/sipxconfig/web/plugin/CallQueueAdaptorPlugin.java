/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.web.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sipfoundry.sipxconfig.callqueue.CallQueue;
import org.sipfoundry.sipxconfig.search.BeanAdaptorPlugin;

public class CallQueueAdaptorPlugin implements BeanAdaptorPlugin {

    private static final Class[] CALLQUEUE_INDEXED_CLASSES = {
            CallQueue.class,
        };

    @Override
    public List<Class> getIndexedClasses() {
        List<Class> classesList = new ArrayList<Class>();
        classesList.addAll(Arrays.asList(CALLQUEUE_INDEXED_CLASSES));
        return classesList;
    }

}
