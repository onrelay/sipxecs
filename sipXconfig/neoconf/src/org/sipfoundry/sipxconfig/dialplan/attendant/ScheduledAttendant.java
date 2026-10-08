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

public class ScheduledAttendant extends Attendant {
	@Override
	protected WorkingHours createWorkingHours() {
		return new ScheduleWorkingHours();
	}
}
