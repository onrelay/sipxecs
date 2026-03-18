/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.dialplan.attendant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.sipfoundry.sipxconfig.common.ScheduledDay;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;


public class Attendant implements Cloneable {
    
    private boolean m_enabled;

    private AutoAttendant m_attendant;

    private List<WorkingHours> m_workingHours = new ArrayList<WorkingHours>();

    /**
     * Initialization is a bit tricky - days here are numbered from 0 to 6, with - 0 being Monday
     * and 6 being Sunday. Days in getScheduleDay and in Calendar object are number from 1 to 7
     * with 1 being Sunday and 7 being Saturday.
     *
     */
    public Attendant() {
        final int days = ScheduledDay.DAYS_OF_WEEK.length;
        final int lastWorkingDay = Calendar.FRIDAY - Calendar.MONDAY;
        m_workingHours = new ArrayList<WorkingHours>();
        for (int i = 0; i < days; i++) {
            WorkingHours workingHoursItem = new WorkingHours();
            int dayOfWeek = (i + Calendar.SUNDAY) % days + 1;
            workingHoursItem.setDay(ScheduledDay.getScheduledDay(dayOfWeek));
            workingHoursItem.setEnabled(i <= lastWorkingDay);
            m_workingHours.add( workingHoursItem );
        }
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public AutoAttendant getAttendant() {
        return m_attendant;
    }

    public void setAttendant(AutoAttendant attendant) {
        m_attendant = attendant;
        setEnabled(attendant != null);
    }

    /**
     * Check if the attendant in question is referenced by this schedule
     *
     * @param attendant
     * @return true if any references have been found false otherwise
     */
    public boolean checkAttendant(AutoAttendant attendant) {
        if (m_attendant == null) {
            return false;
        }
        return m_attendant.equals(attendant);
    }

    public List<WorkingHours> getWorkingHours() {
        return m_workingHours;
    }

    public void setWorkingHours(List<WorkingHours> workingHours) {
        m_workingHours = workingHours;
    }

    public void replaceWorkingHours(List<WorkingHours> workingHours) {
        m_workingHours.clear();
        if( workingHours != null ) {
            m_workingHours.addAll( workingHours );
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Attendant clone = (Attendant) super.clone();
        clone.m_workingHours = new ArrayList<WorkingHours>();
        for( WorkingHours workingHoursItem : m_workingHours ) {
            clone.m_workingHours.add( workingHoursItem );
        }
        return clone;
    }

    public List<WorkingHours.Interval> calculateValidTime(TimeZone timeZone) {
        int timeZoneOffsetInMinutes = DateTimeZone.forTimeZone(timeZone).getOffset(
                new DateTime(DateTimeZone.forTimeZone(timeZone)).getMillis()) / 1000 / 60;
        return calculateValidTimes(timeZoneOffsetInMinutes);
    }

    private List<WorkingHours.Interval> calculateValidTimes(int timeZoneOffsetInMinutes) {
        List<WorkingHours> workingHours = getWorkingHours();
        List<WorkingHours.Interval> validTimeList = new ArrayList<WorkingHours.Interval>();
        for (WorkingHours workingHoursItem : workingHours) {
            workingHoursItem.addMinutesFromSunday(validTimeList, timeZoneOffsetInMinutes);
        }
        return validTimeList;
    }

    public void checkValid() {
        for (WorkingHours workingHour : m_workingHours) {
            if (workingHour.isInvalidPeriod()) {
                throw new InvalidPeriodException();
            }
            if (workingHour.isTheSameHour()) {
                throw new SameStartAndStopHoursException();
            }
        }
        if (overlappingPeriods()) {
            throw new OverlappingPeriodsException();
        }
    }

    public boolean overlappingPeriods() {
        List<WorkingHours.Interval> intervals = calculateValidTimes(0);
        Collections.sort(intervals);
        if (intervals.size() < 2) {
            return false;
        }

        int lastStop = intervals.get(0).getStop();
        for (int i = 1; i < intervals.size(); i++) {
            WorkingHours.Interval interval = intervals.get(i);
            if (interval.getStart() < lastStop) {
                return true;
            }
            lastStop = interval.getStop();
        }
        return false;
    }

    public static class OverlappingPeriodsException extends UserException {
        private static final String ERROR = "&error.overlappingPeriodsException";

        public OverlappingPeriodsException() {
            super(ERROR);
        }
    }

    public static class InvalidPeriodException extends UserException {
        private static final String ERROR = "&error.invalidPeriodException";

        public InvalidPeriodException() {
            super(ERROR);
        }
    }

    public static class SameStartAndStopHoursException extends UserException {
        private static final String ERROR = "&error.sameStartAndStopHoursException";

        public SameStartAndStopHoursException() {
            super(ERROR);
        }
    }

}
