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
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.common.ScheduledDay;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant.InvalidPeriodException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant.OverlappingPeriodsException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant.SameStartAndStopHoursException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;

public class WorkingTimeAttendantTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetStartTime() throws Exception {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1, 10, 14);
        WorkingHours hours = new WorkingHours();
        hours.setStart(c.getTime());
        assertEquals("10:14", hours.getStartTime());
        assertEquals(c.getTime(), hours.getStart());
    }

    public void testGetStopTime() throws Exception {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);
        c.set(Calendar.HOUR, 6);
        c.set(Calendar.MINUTE, 16);
        c.set(Calendar.AM_PM, Calendar.PM);
        WorkingHours hours = new WorkingHours();
        hours.setStop(c.getTime());
        assertEquals("18:16", hours.getStopTime());
        assertEquals(c.getTime(), hours.getStop());
    }

    public void testInitWokingHours() {
        WorkingHours hours = new WorkingHours();
        assertEquals("09:00", hours.getStartTime());
        assertEquals("18:00", hours.getStopTime());
    }

    public void testInitAttendantWorkingTime() {
        WorkingTimeAttendant workingTimeAttendant = new WorkingTimeAttendant();
        List<WorkingHours> workingHours = workingTimeAttendant.getWorkingHours();
        assertEquals(7, workingHours.size());
        assertTrue(workingHours.get(0).isEnabled());
        assertEquals(ScheduledDay.MONDAY, workingHours.get(0).getDay());
        assertTrue(workingHours.get(4).isEnabled());
        assertFalse(workingHours.get(5).isEnabled());
        assertFalse(workingHours.get(6).isEnabled());
        assertEquals(ScheduledDay.SUNDAY, workingHours.get(6).getDay());
    }

    public void testGeneralAddMinutesFromSunday() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 0);
        assertEquals(1, minutes.size());
        assertEquals(63, minutes.get(0).getStart());
        assertEquals(125, minutes.get(0).getStop());

        hours.setDay(ScheduledDay.TUESDAY);
        minutes.clear();
        hours.addMinutesFromSunday(minutes, 0);
        assertEquals(1, minutes.size());
        assertEquals(2 * 24 * 60 + 63, minutes.get(0).getStart());
        assertEquals(2 * 24 * 60 + 125, minutes.get(0).getStop());
    }

    public void testGeneralAddMinutesFromSundayWithTimezone() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.TUESDAY);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 120);
        assertEquals(1, minutes.size());
        assertEquals(2 * 24 * 60 + 63 - 120, minutes.get(0).getStart());
        assertEquals(2 * 24 * 60 + 125 - 120, minutes.get(0).getStop());

        minutes.clear();
        hours.addMinutesFromSunday(minutes, -183);
        assertEquals(1, minutes.size());
        assertEquals(2 * 24 * 60 + 63 + 183, minutes.get(0).getStart());
        assertEquals(2 * 24 * 60 + 125 + 183, minutes.get(0).getStop());
    }

    public void testGeneralAddMinutesFromSundayRollback() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 180);
        assertEquals(1, minutes.size());
        assertEquals(63 + WorkingHours.MINUTES_PER_WEEK - 180, minutes.get(0).getStart());
        assertEquals(125 + WorkingHours.MINUTES_PER_WEEK - 180, minutes.get(0).getStop());

        hours.setDay(ScheduledDay.SATURDAY);
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 22);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        minutes.clear();
        hours.addMinutesFromSunday(minutes, -360);
        assertEquals(1, minutes.size());
        assertEquals(6 * 24 * 60 + 20 * 60 + 3 + 360 - WorkingHours.MINUTES_PER_WEEK, minutes
                .get(0).getStart());
        assertEquals(6 * 24 * 60 + 22 * 60 + 5 + 360 - WorkingHours.MINUTES_PER_WEEK, minutes
                .get(0).getStop());
    }

    public void testGeneralAddMinutesFromSundayRollbackSplit() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 80);
        assertEquals(2, minutes.size());
        assertEquals(63 + WorkingHours.MINUTES_PER_WEEK - 80, minutes.get(0).getStart());
        assertEquals(WorkingHours.MINUTES_PER_WEEK, minutes.get(0).getStop());
        assertEquals(0, minutes.get(1).getStart());
        assertEquals(125 - 80, minutes.get(1).getStop());

        hours.setDay(ScheduledDay.SATURDAY);
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 22);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        minutes.clear();
        hours.addMinutesFromSunday(minutes, -130);
        assertEquals(2, minutes.size());
        assertEquals(6 * 24 * 60 + 20 * 60 + 3 + 130, minutes.get(0).getStart());
        assertEquals(WorkingHours.MINUTES_PER_WEEK, minutes.get(0).getStop());
        assertEquals(0, minutes.get(1).getStart());
        assertEquals(6 * 24 * 60 + 22 * 60 + 5 + 130 - WorkingHours.MINUTES_PER_WEEK, minutes
                .get(1).getStop());
    }

    public void testGeneralAddMinutesFromSundayWithEveryDaySchedule() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.EVERYDAY);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 0);
        assertEquals(7, minutes.size());
        for (int i = 0; i < 7; i++) {
            assertEquals(i * 24 * 60 + 63, minutes.get(i).getStart());
            assertEquals(i * 24 * 60 + 125, minutes.get(i).getStop());
        }
    }

    public void testGeneralAddMinutesFromSundayWithWeekDaysSchedule() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.WEEKDAYS);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 0);
        assertEquals(5, minutes.size());
        for (int i = 0; i < 5; i++) {
            assertEquals((i + 1) * 24 * 60 + 63, minutes.get(i).getStart());
            assertEquals((i + 1) * 24 * 60 + 125, minutes.get(i).getStop());
        }
    }

    public void testGeneralAddMinutesFromSundayWithWeekEndSchedule() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.WEEKEND);

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        List<WorkingHours.Interval> minutes = new ArrayList<WorkingHours.Interval>();
        hours.addMinutesFromSunday(minutes, 0);
        assertEquals(2, minutes.size());
        assertEquals(6 * 24 * 60 + 63, minutes.get(0).getStart());
        assertEquals(6 * 24 * 60 + 125, minutes.get(0).getStop());
        assertEquals(0 * 24 * 60 + 63, minutes.get(1).getStart());
        assertEquals(0 * 24 * 60 + 125, minutes.get(1).getStop());
    }

    public void testInvalidPeriod() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        hours.setStop(c.getTime());

        assertTrue(hours.isInvalidPeriod());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 15);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 50);
        hours.setStop(c.getTime());

        assertFalse(hours.isInvalidPeriod());
    }

    public void testSameHours() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingHours hours = new WorkingHours();
        hours.setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        hours.setStop(c.getTime());

        assertTrue(hours.isTheSameHour());

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 30);
        hours.setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 40);
        hours.setStop(c.getTime());

        assertFalse(hours.isTheSameHour());
    }

    public void testOverlappingPeriods() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        WorkingTimeAttendant workingTimeAttendant = new WorkingTimeAttendant();
        assertFalse(workingTimeAttendant.overlappingPeriods());

        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        workingHours.add(new WorkingHours());
        workingHours.add(new WorkingHours());
        workingTimeAttendant.setWorkingHours(workingHours);

        workingHours.get(0).setDay(ScheduledDay.SUNDAY);
        workingHours.get(1).setDay(ScheduledDay.SUNDAY);

        // First schedule : SUNDAY, 03:03 -> 15:10
        // Second schedule : SUNDAY, 12:00 -> 17:23
        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        assertTrue(workingTimeAttendant.overlappingPeriods());

        // First schedule : SUNDAY, 03:03 -> 15:10
        // Second schedule : SUNDAY, 01:00 -> 08:23
        // First schedule : SUNDAY, 02:15 -> 17:10
        // Second schedule : SUNDAY, 08:00 -> 10:23
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 15);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(2).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertTrue(workingTimeAttendant.overlappingPeriods());

        // First schedule : SUNDAY, 08:13 -> 10:10
        // Second schedule : SUNDAY, 03:00 -> 17:23
        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 13);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertTrue(workingTimeAttendant.overlappingPeriods());

        // First schedule : SUNDAY, 03:03 -> 15:00
        // Second schedule : SUNDAY, 15:00 -> 20:23
        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertTrue(workingTimeAttendant.overlappingPeriods());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertFalse(workingTimeAttendant.overlappingPeriods());

        // First schedule : SUNDAY, 03:03 -> 15:00
        // Second schedule : SUNDAY, 01:00 -> 03:03
        // First schedule : SUNDAY, 03:03 -> 15:00
        // Second schedule : SUNDAY, 18:00 -> 20:23
        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertFalse(workingTimeAttendant.overlappingPeriods());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        assertFalse(workingTimeAttendant.overlappingPeriods());

        // First schedule : SUNDAY, 03:03 -> 15:00
        // Second schedule : MONDAY, 08:00 -> 14:23
        workingHours.get(0).setDay(ScheduledDay.SUNDAY);
        workingHours.get(1).setDay(ScheduledDay.MONDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 14);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        assertFalse(workingTimeAttendant.overlappingPeriods());
    }

    public void testCheckValid() {
        WorkingTimeAttendant workingTimeAttendant = new WorkingTimeAttendant();

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(1970, Calendar.JANUARY, 1);

        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();
        workingHours.add( new WorkingHours() );

        workingHours.get(0).setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 5);
        workingHours.get(0).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            fail("Should throw a InvalidPeriodException");
        } catch (InvalidPeriodException ex) {
            assertTrue(true);
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 5);
        workingHours.get(0).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (InvalidPeriodException ex) {
            fail("Shouldn't throw a InvalidPeriodException");
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStop(c.getTime());

        try {
            workingTimeAttendant.checkValid();
            fail("Should throw a SameStartAndStopHoursException");
        } catch (SameStartAndStopHoursException ex) {
            assertTrue(true);
        }

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 30);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 40);
        workingHours.get(0).setStop(c.getTime());

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (SameStartAndStopHoursException ex) {
            fail("Shouldn't throw a SameStartAndStopHoursException");
        }

        workingHours = new ArrayList<WorkingHours>();
        workingHours.add( new WorkingHours() );
        workingHours.add( new WorkingHours() );

        workingHours.get(0).setDay(ScheduledDay.SUNDAY);
        workingHours.get(1).setDay(ScheduledDay.SUNDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            fail("Should throw a OverlappingPeriodsException");
        } catch (OverlappingPeriodsException ex) {
            assertTrue(true);
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 10);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            fail("Should throw a OverlappingPeriodsException");
        } catch (OverlappingPeriodsException ex) {
            assertTrue(true);
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (OverlappingPeriodsException ex) {
            fail("Shouldn't throw a OverlappingPeriodsException");
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (OverlappingPeriodsException ex) {
            fail("Shouldn't throw a OverlappingPeriodsException");
        }

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        workingTimeAttendant.setWorkingHours(workingHours);

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (OverlappingPeriodsException ex) {
            fail("Shouldn't throw a OverlappingPeriodsException");
        }

        workingHours.get(0).setDay(ScheduledDay.SUNDAY);
        workingHours.get(1).setDay(ScheduledDay.MONDAY);

        c.set(Calendar.HOUR_OF_DAY, 3);
        c.set(Calendar.MINUTE, 3);
        workingHours.get(0).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(0).setStop(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        workingHours.get(1).setStart(c.getTime());

        c.set(Calendar.HOUR_OF_DAY, 14);
        c.set(Calendar.MINUTE, 23);
        workingHours.get(1).setStop(c.getTime());

        try {
            workingTimeAttendant.checkValid();
            assertTrue(true);
        } catch (OverlappingPeriodsException ex) {
            fail("Shouldn't throw a OverlappingPeriodsException");
        }
    }
}
