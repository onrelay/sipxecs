/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */

package org.sipfoundry.sipxconfig.forwarding;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.sipfoundry.sipxconfig.common.ScheduledDay;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.dialplan.attendant.ScheduledAttendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

public class ScheduleTest extends TestCase {
    private static final String COLON = ":";

    public void testCalculateValidTime() {
        User user = EasyMock.createMock(User.class);
        user.getTimezone();
        EasyMock.expectLastCall().andReturn(TimeZone.getTimeZone("GMT"));
        EasyMock.replay(user);
        Schedule sch = new UserSchedule();
        sch.setUser(user);
        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        ScheduledAttendant scheduledAttendant = new ScheduledAttendant();

        workingHours.add( new WorkingHours() );

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2006, Calendar.NOVEMBER, 30, 10, 00);
        workingHours.get(0).setStart(cal.getTime());

        Integer startHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer startMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        cal.set(2006, Calendar.NOVEMBER, 30, 11, 00);
        workingHours.get(0).setStop(cal.getTime());

        Integer stopHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer stopMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        workingHours.get(0).setEnabled(true);
        workingHours.get(0).setDay(ScheduledDay.WEDNESDAY);

        Integer minutesFromSunday = (workingHours.get(0).getDay().getDayOfWeek() - 1) * 24 * 60;

        scheduledAttendant.replaceWorkingHours(workingHours);
        scheduledAttendant.setEnabled(true);

        sch.setScheduledAttendant(scheduledAttendant);

        //int tz_offset = TimeZone.getDefault().getOffset((new Date()).getTime()) / 60000;
        int tz_offset = 0;

        Integer startWithTimezone = minutesFromSunday + startHour * 60 + startMinute - tz_offset;
        Integer stopWithTimezone = minutesFromSunday + stopHour * 60 + stopMinute - tz_offset;

        String expected = Integer.toHexString(startWithTimezone) + COLON
                + Integer.toHexString(stopWithTimezone);
        String actual = sch.calculateValidTime();

        assertEquals(expected, actual);
    }

    public void testCalculateValidTimeSaturdayWithTwoPeriods() {
        User user = EasyMock.createMock(User.class);
        user.getTimezone();
        EasyMock.expectLastCall().andReturn(TimeZone.getTimeZone("GMT-11"));
        EasyMock.replay(user);
        Schedule sch = new UserSchedule();
        sch.setUser(user);
        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        ScheduledAttendant scheduledAttendant = new ScheduledAttendant();

        workingHours.add( new WorkingHours() );

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2006, Calendar.NOVEMBER, 30, 10, 00);
        workingHours.get(0).setStart(cal.getTime());

        Integer startHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer startMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        cal.set(2006, Calendar.NOVEMBER, 30, 23, 00);
        workingHours.get(0).setStop(cal.getTime());

        Integer stopHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer stopMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        workingHours.get(0).setEnabled(true);
        workingHours.get(0).setDay(ScheduledDay.SATURDAY);

        Integer minutesFromSunday = (workingHours.get(0).getDay().getDayOfWeek() - 1) * 24 * 60;

        scheduledAttendant.replaceWorkingHours(workingHours);
        scheduledAttendant.setEnabled(true);

        sch.setScheduledAttendant(scheduledAttendant);

        int tz_offset = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-11")).getOffset(
                new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-11"))).getMillis()) / 1000 / 60;

        Integer startWithTimezone = minutesFromSunday + startHour * 60 + startMinute - tz_offset;
        Integer stopWithTimezone = minutesFromSunday + stopHour * 60 + stopMinute - tz_offset;

        String expected = Integer.toHexString(startWithTimezone) + COLON
                + Integer.toHexString(WorkingHours.MINUTES_PER_WEEK) + COLON + "0" + COLON
                + Integer.toHexString(stopWithTimezone - WorkingHours.MINUTES_PER_WEEK);
        String actual = sch.calculateValidTime();

        assertEquals(expected, actual);
    }

    public void testCalculateValidTimeSundayWithTwoPeriods() {
        User user = EasyMock.createMock(User.class);
        user.getTimezone();
        EasyMock.expectLastCall().andReturn(TimeZone.getTimeZone("GMT+5"));
        EasyMock.replay(user);
        Schedule sch = new UserSchedule();
        sch.setUser(user);
        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        ScheduledAttendant scheduledAttendant = new ScheduledAttendant();

        workingHours.add( new WorkingHours() );

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2006, Calendar.NOVEMBER, 30, 02, 00);
        workingHours.get(0).setStart(cal.getTime());

        Integer startHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer startMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        cal.set(2006, Calendar.NOVEMBER, 30, 15, 00);
        workingHours.get(0).setStop(cal.getTime());

        Integer stopHour = Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        Integer stopMinute = Integer.valueOf(cal.get(Calendar.MINUTE));

        workingHours.get(0).setEnabled(true);
        workingHours.get(0).setDay(ScheduledDay.SUNDAY);

        Integer minutesFromSunday = (workingHours.get(0).getDay().getDayOfWeek() - 1) * 24 * 60;

        scheduledAttendant.replaceWorkingHours(workingHours);
        scheduledAttendant.setEnabled(true);

        sch.setScheduledAttendant(scheduledAttendant);

        int tz_offset = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+5")).getOffset(
                new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+5"))).getMillis()) / 1000 / 60;

        Integer startWithTimezone = minutesFromSunday + startHour * 60 + startMinute - tz_offset;
        Integer stopWithTimezone = minutesFromSunday + stopHour * 60 + stopMinute - tz_offset;

        String expected = Integer.toHexString(startWithTimezone + WorkingHours.MINUTES_PER_WEEK)
                + COLON + Integer.toHexString(WorkingHours.MINUTES_PER_WEEK) + COLON + "0" + COLON
                + Integer.toHexString(stopWithTimezone);
        String actual = sch.calculateValidTime();

        assertEquals(expected, actual);
    }
}
