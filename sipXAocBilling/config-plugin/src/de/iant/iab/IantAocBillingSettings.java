/*
 *
 * Copyright (C) 2015 eZuce Inc.
 *
 * $
 */
package de.iant.iab;

import java.util.Arrays;
import java.util.Collection;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class IantAocBillingSettings extends PersistableSettings implements DeployConfigOnEdit {

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) IantAocBilling.IAB_FEATURE);
    }

    @Override
    public String getBeanId() {
        return "iantaocbillingSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("iantaocbilling/iantaocbilling.xml");
    }

}
