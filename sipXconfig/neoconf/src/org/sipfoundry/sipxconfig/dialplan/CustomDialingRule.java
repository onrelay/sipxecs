/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.dialplan;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.sipfoundry.sipxconfig.dialplan.config.FullTransform;
import org.sipfoundry.sipxconfig.dialplan.config.Transform;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.sipfoundry.sipxconfig.permission.Permission;

/**
 * CustomDialingRule
 */
public class CustomDialingRule extends LocationBasedDialingRule {

    private List<DialPattern> m_dialPatterns;
    private CallPattern m_callPattern = new CallPattern();
    private List<String> m_permissionNames;

    public CustomDialingRule() {
        getDialPatterns().add(new DialPattern());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CustomDialingRule clone = (CustomDialingRule) super.clone();
        clone.replacePermissionNames(getPermissionNames());
        clone.replaceDialPatterns(getDialPatterns());
        return clone;
    }

    public List<DialPattern> getDialPatterns() {
        if( m_dialPatterns == null ) {
           m_dialPatterns = new ArrayList<DialPattern>();
        }
        return m_dialPatterns;
    }

    public void setDialPatterns(List<DialPattern> dialPatterns) {
        m_dialPatterns = dialPatterns;
    }

    public void replaceDialPatterns(List<DialPattern> dialPatterns) {
        getDialPatterns().clear();
        if( dialPatterns != null ) {
            getDialPatterns().addAll( dialPatterns );
        }
    }

    public CallPattern getCallPattern() {
        return m_callPattern;
    }

    public void setCallPattern(CallPattern callPattern) {
        m_callPattern = callPattern;
    }

    @Override
    public String[] getPatterns() {
        String[] patterns = new String[getDialPatterns().size()];
        for (int i = 0; i < patterns.length; i++) {
            DialPattern p = getDialPatterns().get(i);
            patterns[i] = p.calculatePattern();
        }
        return patterns;
    }

    @Override
    public Transform[] getTransforms() {
        final String outPattern = getOutPattern();
        List<Gateway> gateways = getEnabledGateways();
        Transform[] transforms;
        if (gateways.isEmpty()) {
            FullTransform transform = new FullTransform();
            transform.setUser(outPattern);
            if (getSchedule() != null) {
                String validTime = getSchedule().calculateValidTime();
                String scheduleParam = String.format(VALID_TIME_PARAM, validTime);
                transform.setFieldParams(scheduleParam);
            }
            transforms = new Transform[] {
                transform
            };
        } else {
            transforms = new Transform[gateways.size()];
            ForkQueueValue q = new ForkQueueValue(gateways.size());
            for (int i = 0; i < transforms.length; i++) {
                transforms[i] = getGatewayTransform(gateways.get(i), outPattern, q);
            }
        }
        return transforms;
    }

    public void setPermissions(List<Permission> permissions) {
        List<String> permissionNames = getPermissionNames();
        permissionNames.clear();
        for (Permission permission : permissions) {
            permissionNames.add(permission.getName());
        }
    }

    @Override
    public List<String> getPermissionNames() {
        if( m_permissionNames == null ) {
            m_permissionNames = new ArrayList<String>();
        }
        return m_permissionNames;
    }

    public void setPermissionNames(List<String> permissionNames) {
        m_permissionNames = permissionNames;
    }


    public void replacePermissionNames(List<String> permissionNames) {
        getPermissionNames().clear();
        if( permissionNames != null ) {
            getPermissionNames().addAll( permissionNames );
        }
    }

    @Override
    public DialingRuleType getType() {
        return DialingRuleType.CUSTOM;
    }

    @Override
    public CallTag getCallTag() {
        CallTag callTag = super.getCallTag();
        if (callTag != null) {
            return callTag;
        }
        return CallTag.CUST;
    }

    /**
     * External rule if there are gateways. Internal if no gateways
     */
    public boolean isInternal() {
        return getGateways().isEmpty();
    }

    public boolean isGatewayAware() {
        return true;
    }

    @Override
    public String[] getTransformedPatterns(Gateway gateway) {
        List<DialPattern> dialPatterns = getDialPatterns();
        Set<String> transformedPatterns = new LinkedHashSet<String>();
        for (DialPattern dp : dialPatterns) {
            DialPattern tdp = m_callPattern.transform(dp);
            if (gateway != null) {
                String pattern = gateway.getCallPattern(tdp.calculatePattern());
                transformedPatterns.add(pattern);
            } else {
                String pattern = tdp.calculatePattern();
                transformedPatterns.add(pattern);
            }
        }
        return transformedPatterns.toArray(new String[transformedPatterns.size()]);
    }

    @Override
    public String getOutPattern() {
        return m_callPattern.calculatePattern();
    }
}
