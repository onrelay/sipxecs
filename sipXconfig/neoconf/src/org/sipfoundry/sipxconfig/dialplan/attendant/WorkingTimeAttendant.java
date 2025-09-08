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

public class WorkingTimeAttendant extends ScheduledAttendant {
    private WorkingHours[] m_workingHours;

    /**
     * Initialization is a bit tricky - days here are numbered from 0 to 6, with - 0 being Monday
     * and 6 being Sunday. Days in getScheduleDay and in Calendar object are number from 1 to 7
     * with 1 being Sunday and 7 being Saturday.
     *
     */
    public WorkingTimeAttendant() {
        final int days = ScheduledDay.DAYS_OF_WEEK.length;
        final int lastWorkingDay = Calendar.FRIDAY - Calendar.MONDAY;
        m_workingHours = new WorkingHours[days];
        for (int i = 0; i < days; i++) {
            WorkingHours whs = new WorkingHours();
            int dayOfWeek = (i + Calendar.SUNDAY) % days + 1;
            whs.setDay(ScheduledDay.getScheduledDay(dayOfWeek));
            whs.setEnabled(i <= lastWorkingDay);
            m_workingHours[i] = whs;
        }
    }

    public WorkingHours[] getWorkingHours() {
        return m_workingHours;
    }

    public void setWorkingHours(WorkingHours[] workingHours) {
        m_workingHours = workingHours;
    }

    public Object clone() throws CloneNotSupportedException {
        WorkingTimeAttendant clone = (WorkingTimeAttendant) super.clone();
        clone.m_workingHours = m_workingHours.clone();
        return clone;
    }

    public List<WorkingHours.Interval> calculateValidTime(TimeZone timeZone) {
        int timeZoneOffsetInMinutes = DateTimeZone.forTimeZone(timeZone).getOffset(
                new DateTime(DateTimeZone.forTimeZone(timeZone)).getMillis()) / 1000 / 60;
        return calculateValidTimes(timeZoneOffsetInMinutes);
    }

    private List<WorkingHours.Interval> calculateValidTimes(int timeZoneOffsetInMinutes) {
        WorkingHours[] workingHours = getWorkingHours();
        List<WorkingHours.Interval> validTimeList = new ArrayList<WorkingHours.Interval>();
        for (WorkingHours wk : workingHours) {
            wk.addMinutesFromSunday(validTimeList, timeZoneOffsetInMinutes);
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
