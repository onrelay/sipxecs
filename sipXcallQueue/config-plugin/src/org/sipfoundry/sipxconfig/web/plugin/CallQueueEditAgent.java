/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.web.plugin;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.valid.ValidatorException;
import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTier;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.user.SelectUsers;
import org.sipfoundry.sipxconfig.site.user.SelectUsersCallback;

public abstract class CallQueueEditAgent extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "plugin/CallQueueEditAgent";

    /* Properties */
    @InjectObject("spring:callQueueContext")
    public abstract CallQueueContext getCallQueueContext();

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @Persist
    public abstract Integer getCallQueueAgentId();

    public abstract void setCallQueueAgentId(Integer id);

    @Persist
    public abstract CallQueueAgent getCallQueueAgent();

    public abstract void setCallQueueAgent(CallQueueAgent callQueueAgent);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    /* Tiers */

    public abstract CallQueueTier getCallQueueTier();

    public abstract void setCallQueueTier(CallQueueTier tier);

    public abstract int getIndex();

    public abstract void setIndex(int i);

    public abstract Collection<Integer> getAddedUser();

    public abstract void setAddedUser(Collection<Integer> addedAdmins);

    public void pageBeginRender(PageEvent event) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        CallQueueAgent callqueueagent = getCallQueueAgent();

        if (callqueueagent == null) {
            Integer id = getCallQueueAgentId();
            if (null != id) {
                CallQueueContext context = getCallQueueContext();
                callqueueagent = context.loadCallQueueAgent(id);
            } else {
                callqueueagent = getCallQueueContext().newCallQueueAgent();
            }
        }

        if (getAddedUser() != null) {
            if (getAddedUser().size() == 1) {
                Integer userId = getAddedUser().iterator().next();
                callqueueagent.setExtension(getCoreContext().loadUser(userId).getUserName());
            } else {
                getValidator().record(new ValidatorException(getMessages().getMessage("err.notUnique")));
            }
        }

        setCallQueueAgent(callqueueagent);

        if (getCallback() == null) {
            setReturnPage(CallQueuePage.PAGE);
        }

    }

    /* Action listeners */

    public void commit() {
        if (StringUtils.isEmpty(getCallQueueAgent().getExtension())) {
            getValidator().record(new ValidatorException(getMessages().getMessage("err.noExtension")));
        }
        if (TapestryUtils.isValid(this)) {
            CallQueueContext context = getCallQueueContext();
            CallQueueAgent callQueueAgent = getCallQueueAgent();
            context.saveCallQueueAgent(callQueueAgent);
            Integer id = getCallQueueAgent().getId();
            setCallQueueAgent(null);
            setCallQueueAgentId(id);
        }
    }

    // TODO: commit only on Apply or OK submit button pressed
    public IPage addQueues(IRequestCycle cycle) {
        CallQueueSelectPage queuesSelectPage = (CallQueueSelectPage) cycle.getPage(CallQueueSelectPage.PAGE);
        queuesSelectPage.setCallQueueAgentId(getCallQueueAgentId());
        return queuesSelectPage;
    }

    public IPage removeQueue(IRequestCycle cycle, Integer callqueueid) {
        CallQueueAgent callQueueAgent = getCallQueueAgent();
        callQueueAgent.removeFromQueue(callqueueid);
        CallQueueEditAgent editAgentPage = (CallQueueEditAgent) cycle.getPage(CallQueueEditAgent.PAGE);
        return editAgentPage;
    }

    public IPage selectUser(IRequestCycle cycle) {
        SelectUsers selectUsersPage = (SelectUsers) cycle.getPage(SelectUsers.PAGE);
        SelectUsersCallback callback = new SelectUsersCallback(this.getPage());
        callback.setIdsPropertyName("addedUser");
        selectUsersPage.setCallback(callback);
        selectUsersPage.setTitle(getMessages().getMessage("title.selectUser"));
        selectUsersPage.setPrompt(getMessages().getMessage("prompt.selectUser"));
        return selectUsersPage;
    }
}
