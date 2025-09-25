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
package org.sipfoundry.sipxconfig.rest;

import static org.sipfoundry.sipxconfig.rest.JacksonConvert.fromRepresentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.sipfoundry.sipxconfig.common.ScheduledDay;
import org.sipfoundry.sipxconfig.common.TimeOfDay;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant.InvalidPeriodException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingTimeAttendant.OverlappingPeriodsException;
import org.sipfoundry.sipxconfig.dialplan.attendant.WorkingHours;
import org.sipfoundry.sipxconfig.forwarding.ForwardingContext;
import org.sipfoundry.sipxconfig.forwarding.Schedule;
import org.sipfoundry.sipxconfig.forwarding.Schedule.ScheduleException;
import org.sipfoundry.sipxconfig.forwarding.UserSchedule;

public class CallFwdScheduleResource extends UserResource {
    private static final Log LOG = LogFactory.getLog(CallFwdScheduleResource.class);

    public static final Status CLIENT_ERROR_UNPROCESSABLE_ENTITY = 
        new Status(422, 
            "Unprocessable Entity", 
            "The server understands the content type and syntax, but it was semantically incorrect.");

    private ForwardingContext m_forwardingContext;

    @Get
    public Representation represent(Variant variant) throws ResourceException {       
     
        try {
            Representation r;
            Integer id = getIdFromRequest();
            if (id == null) {
                List<Schedule> schedules = m_forwardingContext.getAllAvailableSchedulesForUser(getUser());
                List<ScheduleBean> beans = toScheduleBeanList(schedules);

                LOG.debug("Returning call fwd schedules:\t" + beans);

                r = toRepresentation(beans);
            } else {
                Schedule sch = m_forwardingContext.getScheduleById(id);
                ScheduleBean bean = toScheduleBean(sch);

                LOG.debug("Returning call fwd schedule:\t" + bean);

                r = toRepresentation(bean);
            }

            return r;

        } catch( IOException ex ) {
            throw new ResourceException( ex );
        }
    }

    @Post
    public Representation acceptRepresentation(Representation entity) throws ResourceException {        
        
        ScheduleBean bean = fromRepresentation(entity, ScheduleBean.class);
        LOG.debug("Creating call fwd schedule bean:\t" + bean);

        Schedule sch = new UserSchedule();
        fromScheduleBean(bean, sch);
        sch.setUser(getUser());

        LOG.debug("Creating call fwd schedule:\t" + sch);

        try {
            m_forwardingContext.saveSchedule(sch);
            return null;
        } catch (UserException e) {
            throw new ResourceException(CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
        }
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {       
        
        Integer id = getIdFromRequest();

        if (id != null) {
            ScheduleBean bean = fromRepresentation(entity, ScheduleBean.class);
            LOG.debug("Saving call fwd schedule bean:\t" + bean);

            Schedule sch = m_forwardingContext.getScheduleById(id);
            fromScheduleBean(bean, sch);

            LOG.debug("Saving call fwd schedule:\t" + sch);

            try {
                m_forwardingContext.saveSchedule(sch);
                return null;
            } catch (UserException e) {
                throw new ResourceException(CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

    @Delete
    public void removeRepresentations() throws ResourceException {
        Integer id = getIdFromRequest();
        if (id != null) {
            m_forwardingContext.deleteSchedulesById(Arrays.asList(new Integer[] {
                id
            }));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

    private static List<ScheduleBean> toScheduleBeanList(List<Schedule> schedules) {
        List<ScheduleBean> scheduleBeans = new ArrayList<ScheduleBean>();

        for (Schedule schedule : schedules) {
            ScheduleBean bean = toScheduleBean(schedule);

            scheduleBeans.add(bean);
        }

        return scheduleBeans;
    }

    private static ScheduleBean toScheduleBean(Schedule schedule) {
        ScheduleBean bean = new ScheduleBean();
        bean.setScheduleId(schedule.getId());
        bean.setName(schedule.getName());
        bean.setDescription(schedule.getDescription());
        bean.setPeriods(toPeriodBeanList(schedule.getWorkingTimeAttendant()));
        return bean;
    }

    private static void fromScheduleBean(ScheduleBean bean, Schedule sch) throws ResourceException {
        sch.setName(bean.getName());
        sch.setDescription(bean.getDescription());
        WorkingTimeAttendant wTime = new WorkingTimeAttendant();
        wTime.setWorkingHours(fromPeriodBeanList(bean.getPeriods()));
        sch.setWorkingTimeAttendant(wTime);

        try {
            sch.checkForValidSchedule();
        } catch (ScheduleException e) {
            throw new ResourceException(CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        } catch (InvalidPeriodException e) {
            throw new ResourceException(CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        } catch (OverlappingPeriodsException e) {
            throw new ResourceException(CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        }
    }

    private Integer getIdFromRequest() {
        Integer id = null;
        try {
            String strObj = (String) getRequest().getAttributes().get("id");
            if (strObj != null) {
                id = Integer.valueOf(strObj);
            }
        } catch (NumberFormatException e) {
            // I'd just ignore this, but precommit won't let me
            LOG.debug("id attribute not found");
        }

        return id;
    }

    
    public void setForwardingContext(ForwardingContext forwardingContext) {
        m_forwardingContext = forwardingContext;
    }

    private static List<PeriodBean> toPeriodBeanList(WorkingTimeAttendant wTimes) {
        List<PeriodBean> periodBeans = new ArrayList<PeriodBean>();

        for (WorkingHours wHours : wTimes.getWorkingHours()) {
            PeriodBean bean = new PeriodBean();

            bean.setScheduledDay(wHours.getDay().getDayOfWeek());
            bean.setStart(wHours.getStartTimeOfDay());
            bean.setEnd(wHours.getStopTimeOfDay());

            periodBeans.add(bean);
        }

        return periodBeans;
    }

    private static List<WorkingHours> fromPeriodBeanList(List<PeriodBean> periodBeans) {
        List<WorkingHours> workingHours = new ArrayList<WorkingHours>();

        for (PeriodBean bean : periodBeans) {
            WorkingHours workingHoursItem = new WorkingHours();

            workingHoursItem.setDay(ScheduledDay.getScheduledDay(bean.getScheduledDay()));
            workingHoursItem.setStartTimeOfDay(bean.getStart());
            workingHoursItem.setStopTimeOfDay(bean.getEnd());

            workingHours.add(workingHoursItem);
        }

        return workingHours;
    }

    private static class ScheduleBean {
        private Integer m_scheduleId;
        private String m_name;
        private String m_description;
        private List<PeriodBean> m_periods;

        @SuppressWarnings("unused")
        public Integer getScheduleId() {
            return m_scheduleId;
        }

        public void setScheduleId(Integer scheduleId) {
            m_scheduleId = scheduleId;
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

        public List<PeriodBean> getPeriods() {
            return m_periods;
        }

        public void setPeriods(List<PeriodBean> periods) {
            m_periods = periods;
        }

        @Override
        public String toString() {
            return "ScheduleBean [m_scheduleId=" + m_scheduleId + ", m_name=" + m_name + ", m_description="
                + m_description + ", period=" + m_periods + "]";
        }
    }

    private static class PeriodBean {
        private int m_scheduledDay;
        private TimeOfDay m_start;
        private TimeOfDay m_end;

        public int getScheduledDay() {
            return m_scheduledDay;
        }

        public void setScheduledDay(int scheduledDay) {
            m_scheduledDay = scheduledDay;
        }

        public TimeOfDay getStart() {
            return m_start;
        }

        public void setStart(TimeOfDay start) {
            m_start = start;
        }

        public TimeOfDay getEnd() {
            return m_end;
        }

        public void setEnd(TimeOfDay end) {
            m_end = end;
        }

        @Override
        public String toString() {
            return "PeriodBean [m_scheduledDay=" + m_scheduledDay + ", m_start=" + m_start + ", m_end=" + m_end
                + "] ";
        }
    }
}
