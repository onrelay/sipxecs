/**
 * Copyright (C) 2016 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.web.plugin;

import org.sipfoundry.sipxconfig.callqueue.CallQueue;
import org.sipfoundry.sipxconfig.site.search.EnumEditPageProviderPlugin;

public class CallQueueEditPageProviderPlugin implements EnumEditPageProviderPlugin {

    private static final Object[] CALL_QUEUE_PAGES = {
            CallQueue.class, new String[] {
                CallQueueEditQueue.PAGE, "callQueueId"
            }
            };

    @Override
    public Object[] getPages() {
        return CALL_QUEUE_PAGES;
    }

}
