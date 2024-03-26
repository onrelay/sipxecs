/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.web.plugin;

/* Tapestry 4 page API imports */
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;

/*sipXecs WEB components API imports */
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.components.ObjectSelectionModel;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

/*sipXecs WEB settings API imports */
import org.sipfoundry.sipxconfig.callqueue.CallQueue;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.site.branch.LocationsAware;

public abstract class CallQueueEditQueue extends PageWithCallback implements PageBeginRenderListener, LocationsAware {
    public static final String PAGE = "plugin/CallQueueEditQueue";

    /* Properties */
    @InjectObject("spring:callQueueContext")
    public abstract CallQueueContext getCallQueueContext();

    @Persist("client")
    public abstract Integer getCallQueueId();

    public abstract void setCallQueueId(Integer id);

    public abstract CallQueue getCallQueue();

    public abstract void setCallQueue(CallQueue callQueue);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:branchManager")
    public abstract BranchManager getBranchManager();

    /*  Methods */

    public IPropertySelectionModel getLocationsModel() {
        ObjectSelectionModel model = new ObjectSelectionModel();
        model.setCollection(getBranchManager().getBranches());
        model.setLabelExpression("name");
        return model;
    }

    public void pageBeginRender(PageEvent event) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        CallQueue callqueue = getCallQueue();
        if (null != callqueue) {
            return;
        }

        Integer id = getCallQueueId();
        if (null != id) {
            CallQueueContext context = getCallQueueContext();
            callqueue = context.loadCallQueue(id);
        } else {
            callqueue = getCallQueueContext().newCallQueue();
        }
        setCallQueue(callqueue);

        if (getCallback() == null) {
            setReturnPage(CallQueuePage.PAGE);
        }
    }

    public void commit() {
        if (isValid()) {
            saveValid();
        }
    }

    private boolean isValid() {
        return TapestryUtils.isValid(this);
    }

    private void saveValid() {
        CallQueueContext context = getCallQueueContext();
        CallQueue callQueue = getCallQueue();
        // call set extension - hack to regenerate FS dialplan
        callQueue.setExtension(callQueue.getExtension());
        context.saveCallQueue(callQueue);
        Integer id = getCallQueue().getId();
        setCallQueueId(id);
    }

    public void setFeatureId(Integer featureId) {
        setCallQueueId(featureId);
    }
}
