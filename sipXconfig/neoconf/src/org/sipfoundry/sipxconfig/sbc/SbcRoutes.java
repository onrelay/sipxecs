/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.sbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * List of domains and subnets that need special routing: either through session border controller
 * or only to intranet.
 */
public class SbcRoutes {
    private List<String> m_domains = new ArrayList<String>();
    private List<String> m_subnets = new ArrayList<String>();

    public void setDomains(List<String> domains) {
        m_domains = domains;
    }

    public List<String> getDomains() {
        return m_domains;
    }

    public boolean addDomain() {
        return getDomains().add(StringUtils.EMPTY);
    }

    public String removeDomain(int index) {
        return getDomains().remove(index);
    }

    public void replaceDomains(List<String> domains) {
        getDomains().clear();
        if( domains != null ) {
            getDomains().addAll( domains );
        }
    }

    public void setSubnets(List<String> subnets) {
        m_subnets = subnets;
    }

    public List<String> getSubnets() {
        return m_subnets;
    }

    public void replaceSubnets(List<String> subnets) {
        getSubnets().clear();
        if( subnets != null ) {
            getSubnets().addAll( subnets );
        }
    }

    public boolean addSubnet() {
        return getSubnets().add(StringUtils.EMPTY);
    }

    public String removeSubnet(int index) {
        return getSubnets().remove(index);
    }

    public boolean isEmpty() {
        return getSubnets().isEmpty() && getDomains().isEmpty();
    }
}
