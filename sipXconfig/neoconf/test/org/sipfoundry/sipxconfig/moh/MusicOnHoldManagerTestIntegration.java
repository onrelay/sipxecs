/**
 *
 *
 * Copyright (c) 2011 / 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.moh;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;


import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.dialplan.config.FullTransform;
import org.sipfoundry.sipxconfig.test.ImdbTestCase;

import org.bson.Document;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

public class MusicOnHoldManagerTestIntegration extends ImdbTestCase {

    private MusicOnHoldManager m_mohManager;
    private MohSettings m_settings;
    private LocationsManager m_locationsManager;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
        sql("commserver/SeedLocations.sql");
        sql("moh/seed-ivr.sql");        
    }

    public void testReplicateAliasMoh() throws Exception {
        m_mohManager.saveSettings(m_settings);
        Pattern mohPattern = Pattern.compile("MohSettings*");
        Bson query = Filters.regex(ID, mohPattern);
        assertEquals(1, getEntityCollection().countDocuments(query));
        Document obj = getEntityCollection().find(query).first();
        List<Document> aliases = (List<Document>) obj.get(MongoConstants.ALIASES);
        assertEquals(4, aliases.size());
        assertEquals("~~mh~", aliases.get(0).get(MongoConstants.ALIAS_ID));
        assertEquals("<sip:IVR@vm.example.org;action=moh;moh=l>", aliases.get(0).get(MongoConstants.CONTACT));
        assertEquals("moh", aliases.get(0).get(MongoConstants.RELATION));

        assertEquals("~~mh~l", aliases.get(1).get(MongoConstants.ALIAS_ID));
        assertEquals("<sip:IVR@vm.example.org;action=moh;moh=l>", aliases.get(1).get(MongoConstants.CONTACT));
        assertEquals("moh", aliases.get(1).get(MongoConstants.RELATION));

        assertEquals("~~mh~p", aliases.get(2).get(MongoConstants.ALIAS_ID));
        assertEquals("<sip:IVR@vm.example.org;action=moh;moh=p>", aliases.get(2).get(MongoConstants.CONTACT));
        assertEquals("moh", aliases.get(2).get(MongoConstants.RELATION));

        assertEquals("~~mh~n", aliases.get(3).get(MongoConstants.ALIAS_ID));
        assertEquals("<sip:IVR@vm.example.org;action=moh;moh=n>", aliases.get(3).get(MongoConstants.CONTACT));
        assertEquals("moh", aliases.get(2).get(MongoConstants.RELATION));
    }

    public void testDialingRule() throws Exception {
        Location location = m_locationsManager.getLocation(102);
        List<MohRule> rules = (List<MohRule>)m_mohManager.getDialingRules(location);
        assertEquals(1, rules.size());
        MohRule rule = (MohRule) rules.get(0);
        FullTransform transform = (FullTransform) rule.getTransforms()[0];
        assertEquals("IVR", transform.getUser());
        assertEquals("vm.example.org", transform.getHost());
        assertEquals(2, transform.getUrlParams().length);
        assertEquals("action=moh", transform.getUrlParams()[0]);
        assertEquals("moh=u{vdigits}", transform.getUrlParams()[1]);
        location = m_locationsManager.getLocation(101);
        rules = (List<MohRule>)m_mohManager.getDialingRules(location);
        assertEquals(1, rules.size());
        rule = (MohRule) rules.get(0);
        transform = (FullTransform) rule.getTransforms()[0];
        assertEquals("IVR", transform.getUser());
        assertEquals("vm.primary.example.org", transform.getHost());
        assertEquals(2, transform.getUrlParams().length);
        assertEquals("action=moh", transform.getUrlParams()[0]);
        assertEquals("moh=u{vdigits}", transform.getUrlParams()[1]);
    }

    public void setMusicOnHoldManager(MusicOnHoldManager mgr) {
        m_mohManager = mgr;
    }

    public void setMohSettings(MohSettings settings) {
        m_settings = settings;
    }

    public void setLocationsManager(LocationsManager mgr) {
        m_locationsManager = mgr;
    }

}
