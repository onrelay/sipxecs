/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.api.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.sipfoundry.sipxconfig.forwarding.GeneralSchedule;
import org.sipfoundry.sipxconfig.forwarding.Schedule;
import org.sipfoundry.sipxconfig.forwarding.UserGroupSchedule;
import org.sipfoundry.sipxconfig.forwarding.UserSchedule;

@XmlRootElement(name = "Schedule")
@XmlType(propOrder = {
        "id", "name", "description", "userId", "groupId", "type", "workingTimeAttendant"
        })
@JsonPropertyOrder({
        "id", "name", "description", "userId", "groupId", "type", "workingTimeAttendant"
    })
public class ScheduleBean {
    private int m_id;
    private String m_name;
    private String m_description;
    private Integer m_userId = -1;
    private Integer m_groupId = -1;
    private ScheduleType m_type;
    private WorkingTimeAttendantBean m_workingTimeAttendant;

    @XmlType(name = "scheduleType")
    @XmlEnum
    public enum ScheduleType {
        @XmlEnumValue(value = "user")
        S,
        @XmlEnumValue(value = "general")
        G,
        @XmlEnumValue(value = "group")
        U
    }

    public static ScheduleBean convertSchedule(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleBean scheduleBean = new ScheduleBean();
        scheduleBean.setId(schedule.getId());
        scheduleBean.setDescription(schedule.getDescription());
        scheduleBean.setName(schedule.getName());
        if (schedule instanceof GeneralSchedule) {
            scheduleBean.setType(ScheduleType.G);
        } else if (schedule instanceof UserSchedule) {
            scheduleBean.setType(ScheduleType.S);
            scheduleBean.setUserId(schedule.getUser().getId());
        } else if (schedule instanceof UserGroupSchedule) {
            scheduleBean.setType(ScheduleType.U);
            scheduleBean.setGroupId(schedule.getUserGroup().getId());
        }
        scheduleBean.setWorkingTimeAttendant(
            WorkingTimeAttendantBean.convertWorkingTimeAttendantBean(
                schedule.getWorkingTimeAttendant()));
        return scheduleBean;
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public Integer getUserId() {
        return m_userId;
    }

    public void setUserId(Integer userId) {
        m_userId = userId;
    }

    public Integer getGroupId() {
        return m_groupId;
    }

    public void setGroupId(Integer groupId) {
        m_groupId = groupId;
    }

    public ScheduleType getType() {
        return m_type;
    }

    public void setType(ScheduleType type) {
        m_type = type;
    }

    public WorkingTimeAttendantBean getWorkingTimeAttendant() {
        return m_workingTimeAttendant;
    }

    public void setWorkingTimeAttendant(WorkingTimeAttendantBean workingTimeAttendant) {
        m_workingTimeAttendant = workingTimeAttendant;
    }
}
