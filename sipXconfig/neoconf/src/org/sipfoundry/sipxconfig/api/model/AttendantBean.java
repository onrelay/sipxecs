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

import org.sipfoundry.sipxconfig.dialplan.attendant.Attendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

public class AttendantBean {

    private List<WorkingHoursBean> m_workingHours = new ArrayList<WorkingHoursBean>();

    public static AttendantBean convertAttendantBean(Attendant attendant) {
        List<WorkingHoursBean> workingHoursList = new ArrayList<WorkingHoursBean>();
        for (WorkingHours workingHours : attendant.getWorkingHours()) {
            workingHoursList.add(WorkingHoursBean.convertWorkingHours(workingHours));
        }
        AttendantBean attendantBean = new AttendantBean();
        attendantBean.replaceWorkingHours(workingHoursList);
        return attendantBean;
    }

    public static Attendant convertToAttendant(AttendantBean attendantBean) {
        Attendant attendant = new Attendant();
        ArrayList<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        for (WorkingHoursBean workingHoursBean : attendantBean.getWorkingHours()) {
            workingHours.add( WorkingHoursBean.convertToWorkingHours(workingHoursBean) );
        }
        attendant.replaceWorkingHours(workingHours);
        return attendant;
    }

    public void setWorkingHours(List<WorkingHoursBean> workingHours) {
        m_workingHours = workingHours;
    }

    public void replaceWorkingHours(List<WorkingHoursBean> workingHours) {
        m_workingHours.clear();
        if( workingHours != null ) {
            m_workingHours.addAll( workingHours );
        }
    }

    public List<WorkingHoursBean> getWorkingHours() {
        return m_workingHours;
    }
}
