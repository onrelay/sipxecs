/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.callqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.ValueStorage;
import org.sipfoundry.sipxconfig.setting.type.FileSetting;
import org.sipfoundry.sipxconfig.setting.type.SettingType;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;

public class CallQueue extends CallQueueExtension implements SystemAuditable {
    private static final String SET = "set";
    private static final String RECORD_DIR = "call-queue/record-calls-directory";
    private static final String PLAYBACK = "playback";
    private static final String QUEUE_NAME = "${lua(~stream:write(&quot;(%s) - &quot; .. " +
    		"string.gsub(session:getVariable(&quot;caller_id_name&quot;), &quot;/&quot;, &quot;-&quot;)))}";
    private static final String DELIM = "/";
    private String m_promptsDirectory;
    private Set<Branch> m_locations = new HashSet<Branch>();

    public void setPromptsDirectory(String promptsDirectory) {
        m_promptsDirectory = promptsDirectory;
    }

    public Set<Branch> getLocations() {
        return m_locations;
    }

    public void setLocations(Set<Branch> locations) {
        m_locations = locations;
    }

    public List<Branch> getLocationsList() {
        return new ArrayList<Branch>(m_locations);
    }

    public void setLocationsList(List<Branch> locations) {
        m_locations.clear();
        m_locations.addAll(locations);
    }

    /* Set extension handling for CallQueue */
    public void setExtension(String extension) {
        FreeswitchCondition condition;
        if (getConditions() == null) {
            condition = createCondition();
        } else {
            condition = getNumberCondition();
        }
        Set<FreeswitchAction> actions = new LinkedHashSet<FreeswitchAction>();
        actions.add(createAction(SET, "hangup_after_bridge=true"));

        if (getBypassMedia()) {
            actions.add(createAction(SET, "bypass_media=true"));
        }


        String name = getName();
        if (StringUtils.isNotBlank(name)) {
            actions.add(createAction(SET, "effective_caller_id_name=" + String.format(QUEUE_NAME, name)));
        }

        String breakAwayDigit = (String) getSettingTypedValue("call-queue/breakaway-digit");
        if (StringUtils.isNotBlank(breakAwayDigit)) {
            actions.add(createAction(SET, "cc_exit_keys=" + breakAwayDigit));
        }

        boolean answered = false;
        String welcomeAudio = (String) getSettingTypedValue("call-queue/welcome-audio");
        if (null != welcomeAudio) {
            answered = true;
            actions.add(getAnswerAction());
            actions.add(createAction(PLAYBACK, m_promptsDirectory + DELIM + welcomeAudio));
        }
        actions.add(createAction("callcenter", String.format("queue-%s", extension)));
        String goodbyeAudio = (String) getSettingTypedValue("call-queue/goodbye-audio");
        if (null != goodbyeAudio) {
            if (!answered) {
                actions.add(getAnswerAction());
            }
            actions.add(createAction(PLAYBACK, m_promptsDirectory + DELIM + goodbyeAudio));
        }
        String transferTo = (String) getSettingTypedValue("call-queue/transfer-on-timeout");
        if (null != transferTo) {
            actions.add(createAction("bridge", "sofia/$${domain}/" + transferTo + "@$${domain}"));
        }
        actions.add(createAction("hangup", null));
        condition.setExpression(String.format("^%s$", extension));
        condition.setActions(actions);
        addCondition(condition);
    }

    private FreeswitchAction getAnswerAction() {
        FreeswitchAction answer = new FreeswitchAction();
        answer.setApplication("answer");
        return answer;
    }

    @Override
    public boolean isValidUser() {
        return true;
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxcallqueue/CallQueue.xml");
    }

    public void copySettingsTo(CallQueue dst) {
        dst.setExtension(getExtension() + CallQueueContextImpl.COPIED);
        // Copy bean settings
        ValueStorage valueStorage = (ValueStorage) getValueStorage();
        ValueStorage clonedValueStorage = valueStorage.duplicateDeep();
        dst.setValueStorage(clonedValueStorage);
    }

    public String getStrategy() {
        return (String) getSettingTypedValue("call-queue/strategy");
    }

    public Boolean getBypassMedia() {
        return (Boolean) getSettingTypedValue("call-queue/bypass-media");
    }

    public String getMohSound() {
        String mohSound = (String) getSettingTypedValue("call-queue/moh-sound");
        if (StringUtils.isNotEmpty(mohSound)) {
            return m_promptsDirectory + DELIM + mohSound; 
        }
        return StringUtils.EMPTY;
    }

    public Boolean getRecordEnabled() {
        return (Boolean) getSettingTypedValue("call-queue/record-calls");
    }

    public String getRecordTemplate() {
        if (getRecordEnabled() && StringUtils.isNotEmpty((String) getSettingTypedValue(RECORD_DIR))) {
            return (String) getSettingTypedValue(RECORD_DIR) + DELIM
                    + "${strftime(%Y-%m-%d-%H-%M-%S)}.to-${destination_number}-from-${caller_id_number}.${uuid}.wav";
        }
        return StringUtils.EMPTY;
    }

    public Boolean getTierRulesApply() {
        return (Boolean) getSettingTypedValue("call-queue/tier-rules-apply");
    }

    public Integer getTierRuleWaitSecond() {
        return (Integer) getSettingTypedValue("call-queue/tier-rule-wait-second");
    }

    public Boolean getTierRuleWaitMultiplyLevel() {
        return (Boolean) getSettingTypedValue("call-queue/tier-rule-wait-multiply-level");
    }

    public Boolean getTierRuleNoAgentNoWait() {
        return (Boolean) getSettingTypedValue("call-queue/tier-rule-no-agent-no-wait");
    }

    public Integer getMaxWaitTime() {
        return (Integer) getSettingTypedValue("call-queue/max-wait-time");
    }

    public Integer getMaxWaitTimeWithNoAgent() {
        return (Integer) getSettingTypedValue("call-queue/max-wait-time-with-no-agent");
    }

    public Integer getMaxWaitTimeWithNoAgentTimeReached() {
        return (Integer) getSettingTypedValue("call-queue/max-wait-time-with-no-agent-time-reached");
    }

    @Override
    public void setSettings(Setting settings) {
        settings.acceptVisitor(new AudioDirectorySetter(m_promptsDirectory, "welcome-audio", "goodbye-audio", "moh-sound"));
        super.setSettings(settings);
    }

    private class AudioDirectorySetter extends AbstractSettingVisitor {
        private final String m_audioDirectory;
        private final List<String> m_settingNames;

        public AudioDirectorySetter(String directory, String... settingNames) {
            m_audioDirectory = directory;
            m_settingNames = Arrays.asList(settingNames);
        }

        @Override
        public void visitSetting(Setting setting) {
            SettingType type = setting.getType();
            if (type instanceof FileSetting) {
                if (m_settingNames.contains(setting.getName())) {
                    FileSetting fileType = (FileSetting) type;
                    fileType.setDirectory(m_audioDirectory);
                }
            }
        }
    }

    @Override
    public String getEntityIdentifier() {
        return getAlias();
    }

    @Override
    public String getConfigChangeType() {
        return CallQueue.class.getSimpleName();
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> props = new HashMap<String, Object>();
        List<String> locations = new ArrayList<String>();
        for (Branch branch : m_locations) {
            locations.add(branch.getName());
        }
        props.put(MongoConstants.LOCATIONS, locations);
        return props;
    }
}
