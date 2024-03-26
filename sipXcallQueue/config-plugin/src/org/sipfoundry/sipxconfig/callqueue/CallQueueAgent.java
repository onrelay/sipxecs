/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.callqueue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.setting.ValueStorage;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;
import org.springframework.beans.factory.annotation.Required;

public class CallQueueAgent extends BeanWithSettings implements DeployConfigOnEdit, SystemAuditable {

    private static String s_callTimeout = "call-queue-agent/call-timeout";
    private static String STATUS = "call-queue-agent/status";
    private String m_name;
    private String m_extension;
    private String m_description;
    private CallQueueContext m_callQueueContext;
    private CallQueueTiers m_tiers = new CallQueueTiers();
    private DomainManager m_domainManager;
    private String m_state;

    /* Enabled */
    public boolean isEnabled() {
        return true;
    }

    /* Name */
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    /* Extension */
    public String getExtension() {
        return m_extension;
    }

    public void setExtension(String extension) {
        m_extension = extension;
    }

    /* Description */
    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    @Required
    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    @Required
    public void setCallQueueContext(CallQueueContext callqueuecontext) {
        m_callQueueContext = callqueuecontext;
    }

    public CallQueueContext getCallQueueContext() {
        return m_callQueueContext;
    }

    public CallQueueTiers getTiers() {
        return m_tiers;
    }

    public void setTiers(CallQueueTiers tiers) {
        m_tiers = tiers;
    }

    public String getState() {
        return m_state;
    }

    public void setState(String state) {
        m_state = state;
    }

    public boolean isInIdleState() {
        return StringUtils.contains(m_state, "Idle");
    }

    public String getContactUri() {
        boolean callForwarding = (Boolean) getSettingTypedValue("call-queue-agent/follow-call-forwarding");
        StringBuilder contactFormat = new StringBuilder();
        contactFormat.append("sofia/%s/%s@%s;sipx-noroute=VoiceMail;");
        if (!callForwarding) {
            contactFormat.append("sipx-userforward=false;");
        }
        contactFormat.append("sipx-expires=%s");
        return String.format(contactFormat.toString(), m_domainManager.getDomainName(), getExtension(),
                m_domainManager.getDomainName(), getSettingValue(s_callTimeout));
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new Defaults());
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxcallqueue/CallQueueAgent.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) CallQueueContext.FEATURE, (Feature) FreeswitchFeature.FEATURE);
    }

    public String getEntityName() {
        return getClass().getSimpleName();
    }

    public void removeFromQueue(Integer callqueueid) {
        CallQueueTiers tiers = getTiers();
        tiers.removeFromQueue(callqueueid);
    }

    public CallQueue getQueueByTier(CallQueueTier callqueuetier) {
        return getCallQueueContext().loadCallQueue(callqueuetier.getCallQueueId());
    }

    public class Defaults {

        @SettingEntry(path = "call-queue-agent/max-no-answer")
        public String getDefaultMaxNoAnswer() {
            return getCallQueueContext().getSettings().getSettingValue("call-queue-agent/max-no-answer");
        }

        @SettingEntry(path = "call-queue-agent/wrap-up-time")
        public String getDefaultWrapUpTime() {
            return getCallQueueContext().getSettings().getSettingValue("call-queue-agent/wrap-up-time");
        }

        @SettingEntry(path = "call-queue-agent/reject-delay-time")
        public String getDefaultRejectDelayTime() {
            return getCallQueueContext().getSettings().getSettingValue("call-queue-agent/reject-delay-time");
        }

        @SettingEntry(path = "call-queue-agent/busy-delay-time")
        public String getDefaultBusyDelayTime() {
            return getCallQueueContext().getSettings().getSettingValue("call-queue-agent/busy-delay-time");
        }

        @SettingEntry(path = "call-queue-agent/no-answer-delay-time")
        public String getDefaultNoAnswerDelayTime() {
            return getCallQueueContext().getSettings().getSettingValue("call-queue-agent/no-answer-delay-time");
        }

        @SettingEntry(path = "call-queue-agent/call-timeout")
        public String getDefaultCallTimeout() {
            return getCallQueueContext().getSettings().getSettingValue(s_callTimeout);
        }
    }

    public void copySettingsTo(CallQueueAgent dst) {
        // Copy CallQueueTiers
        CallQueueTiers newTiers = new CallQueueTiers();
        m_tiers.copyTiersTo(newTiers.getTiers());
        dst.setTiers(newTiers);
        // Copy bean settings
        ValueStorage valueStorage = (ValueStorage) getValueStorage();
        ValueStorage clonedValueStorage = valueStorage.duplicateDeep();
        // the agent status is the agent status and not meant to be copied
        clonedValueStorage.revertSettingToDefault(dst.getSettings().getSetting(STATUS));
        dst.setValueStorage(clonedValueStorage);
    }
    @Override
    public String getEntityIdentifier() {
        return getName();
    }
    @Override
    public String getConfigChangeType() {
        return CallQueueAgent.class.getSimpleName();
    }
}
