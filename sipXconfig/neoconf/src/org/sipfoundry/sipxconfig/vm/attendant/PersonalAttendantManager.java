/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.vm.attendant;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.common.User;
import org.springframework.dao.support.DataAccessUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;

public abstract class PersonalAttendantManager extends SipxHibernateDaoSupport<PersonalAttendant> {

    public PersonalAttendant loadPersonalAttendantForUser(User user) {
        PersonalAttendant pa = findPersonalAttendant(user);
        if (pa == null) {
            pa = new PersonalAttendant();
            pa.setUser(user);
            super.mergeEntity(pa);
        }
        return pa;
    }

    public PersonalAttendant getPersonalAttendantForUser(User user) {
        return findPersonalAttendant(user);
    }

    public final void removePersonalAttendantForUser(User user) {
        PersonalAttendant pa = findPersonalAttendant(user);
        if (pa != null) {
            super.removeEntity(pa);
        }
    }

    public final void storePersonalAttendant(PersonalAttendant pa) {
        super.saveEntity(pa);
    }

    public final void clearPersonalAttendants() {
        List<PersonalAttendant> allPersonalAttendants = super.loadAllEntities(PersonalAttendant.class);
        super.removeAllEntities(allPersonalAttendants);
    }

    private PersonalAttendant findPersonalAttendant(User user) {
        Collection<PersonalAttendant> pas = super.findByNamedQueryAndNamedParam("personalAttendantForUser", 
            "user",
            user,
            PersonalAttendant.class);
        return (PersonalAttendant) DataAccessUtils.singleResult(pas);
    }

}
