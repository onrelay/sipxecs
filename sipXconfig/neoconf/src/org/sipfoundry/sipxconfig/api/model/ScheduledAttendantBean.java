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
import org.sipfoundry.sipxconfig.dialplan.attendant.Attendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.ScheduledAttendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

@XmlRootElement(name = "scheduledAttendant")
public class ScheduledAttendantBean extends AttendantBean {

    public static ScheduledAttendantBean convertScheduledAttendantBean(ScheduledAttendant scheduledAttendant) {

        AttendantBean attendantBean = 
            AttendantBean.convertAttendantBean( scheduledAttendant );

        ScheduledAttendantBean scheduledAttendantBean = new ScheduledAttendantBean();

        scheduledAttendantBean.setWorkingHours( attendantBean.getWorkingHours() );

        return scheduledAttendantBean;
    }

    public static ScheduledAttendant convertToScheduledAttendant(ScheduledAttendantBean scheduledAttendantBean ) {

        Attendant attendant = 
            AttendantBean.convertToAttendant( scheduledAttendantBean );

        ScheduledAttendant scheduledAttendant = new ScheduledAttendant();

        scheduledAttendant.setWorkingHours( attendant.getWorkingHours() );

        return scheduledAttendant;
    }

    public void setWorkingHours(List<WorkingHoursBean> workingHours) {
        super.setWorkingHours( workingHours );
    }

    @XmlElement(name = "workingHours")
    @JsonProperty(value = "workingHours")
    public List<WorkingHoursBean> getWorkingHours() {
        return super.getWorkingHours();
    }
}