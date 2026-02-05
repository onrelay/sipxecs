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
import org.sipfoundry.commons.util.HolidayPeriod;
import org.sipfoundry.sipxconfig.dialplan.attendant.HolidayAttendant;

@XmlRootElement(name = "holidayPeriods")
public class HolidayAttendantBean {
    private List<HolidayPeriodBean> m_holidayPeriods;

    public static HolidayAttendantBean convertHolidayAttendantBean(HolidayAttendant holidayAttendant) {
        List<HolidayPeriodBean> holidayPeriodList = new ArrayList<HolidayPeriodBean>();
        for (HolidayPeriod hPeriod : holidayAttendant.getPeriods()) {
            holidayPeriodList.add(HolidayPeriodBean.convertHolidayPeriod(hPeriod));
        }
        HolidayAttendantBean holidayAttendantBean = new HolidayAttendantBean();
        holidayAttendantBean.setHolidayPeriods(holidayPeriodList);
        return holidayAttendantBean;
    }

    public static void convertToHolidayAttendant(HolidayAttendantBean holidayAttendantBean, HolidayAttendant holidayAttendant) {
        
        holidayAttendant.getPeriods().clear();

        if (holidayAttendantBean == null) {
            return;
        }
        for (HolidayPeriodBean bean : holidayAttendantBean.getHolidayPeriods()) {
            holidayAttendant.addPeriod(HolidayPeriodBean.convertToHolidayPeriod(bean));
        }
    }

    public void setHolidayPeriods(List<HolidayPeriodBean> holidayPeriods) {
        m_holidayPeriods = holidayPeriods;
    }

    @XmlElement(name = "holidayPeriods")
    @JsonProperty(value = "holidayPeriods")
    public List<HolidayPeriodBean> getHolidayPeriods() {
        if (m_holidayPeriods == null) {
            return new ArrayList<HolidayPeriodBean>();
        }
        return m_holidayPeriods;
    }
}
