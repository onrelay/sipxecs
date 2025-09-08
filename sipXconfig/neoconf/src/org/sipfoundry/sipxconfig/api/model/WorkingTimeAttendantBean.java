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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

@XmlRootElement(name = "workingTimeAttendant")
public class WorkingTimeAttendantBean {
    private WorkingHoursBean[] m_workingHours;

    public static WorkingTimeAttendantBean convertWorkingTimeAttendantBean(WorkingTimeAttendant workingTimeAttendant) {
        List<WorkingHoursBean> workingHoursList = new ArrayList<WorkingHoursBean>();
        for (WorkingHours workingHours : workingTimeAttendant.getWorkingHours()) {
            workingHoursList.add(WorkingHoursBean.convertWorkingHours(workingHours));
        }
        WorkingTimeAttendantBean workingTimeAttendantBean = new WorkingTimeAttendantBean();
        workingTimeAttendantBean.setWorkingHours(workingHoursList.toArray(new WorkingHoursBean[workingHoursList.size()]));
        return workingTimeAttendantBean;
    }

    public static WorkingTimeAttendant convertToWorkingTimeAttendant(WorkingTimeAttendantBean workingTimeAttendantBean) {
        WorkingTimeAttendant workingTimeAttendant = new WorkingTimeAttendant();
        WorkingHours[] workingHoursArray = new WorkingHours[workingTimeAttendantBean.getWorkingHours().length];
        int i = 0;
        for (WorkingHoursBean workingHoursBean : workingTimeAttendantBean.getWorkingHours()) {
            workingHoursArray[i++] = (WorkingHoursBean.convertToWorkingHours(workingHoursBean));
        }
        workingTimeAttendant.setWorkingHours(workingHoursArray);
        return workingTimeAttendant;
    }

    public void setWorkingHours(WorkingHoursBean[] workingHours) {
        m_workingHours = workingHours;
    }

    @XmlElement(name = "workingHours")
    @JsonProperty(value = "workingHours")
    public WorkingHoursBean[] getWorkingHours() {
        if (m_workingHours == null) {
            m_workingHours = new WorkingHoursBean[0];
        }
        return m_workingHours;
    }

}
