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
import org.sipfoundry.sipxconfig.dialplan.attendant.AfterHoursAttendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

@XmlRootElement(name = "afterHoursAttendant")
public class AfterHoursAttendantBean extends AttendantBean {

    public static AfterHoursAttendantBean convertAfterHoursAttendantBean(AfterHoursAttendant afterHoursAttendant) {

        AttendantBean attendantBean = 
            AttendantBean.convertAttendantBean( afterHoursAttendant );

        AfterHoursAttendantBean afterHoursAttendantBean = new AfterHoursAttendantBean();

        afterHoursAttendantBean.replaceWorkingHours( attendantBean.getWorkingHours() );

        return afterHoursAttendantBean;
    }

    public static AfterHoursAttendant convertToAfterHoursAttendant(AfterHoursAttendantBean afterHoursAttendantBean ) {

        Attendant attendant = 
            AttendantBean.convertToAttendant( afterHoursAttendantBean );

        AfterHoursAttendant afterHoursAttendant = new AfterHoursAttendant();

        afterHoursAttendant.replaceWorkingHours( attendant.getWorkingHours() );

        return afterHoursAttendant;
    }

    @XmlElement(name = "workingHours")
    @JsonProperty(value = "workingHours")
    public List<WorkingHoursBean> getWorkingHours() {
        return super.getWorkingHours();
    }
}