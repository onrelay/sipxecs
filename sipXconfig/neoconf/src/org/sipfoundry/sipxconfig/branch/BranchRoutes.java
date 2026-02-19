/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.branch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class BranchRoutes {
    private List<String> m_domains;
    private List<String> m_subnets;

    public void setDomains(List<String> domains) {
        getDomains().clear();
        if( domains != null ) {
            getDomains().addAll( domains );
        }
    }

    public List<String> getDomains() {
        if( m_domains == null ) {
            m_domains = new ArrayList<String>();
        }
        return m_domains;
    }

    public void setSubnets(List<String> subnets) {
        getSubnets().clear();
        if( subnets != null ) {
            getSubnets().addAll( subnets );
        }
    }

    public List<String> getSubnets() {
        if( m_subnets == null ) {
            m_subnets = new ArrayList<String>();
        }
        return m_subnets;
    }

    public boolean addDomain() {
        return getDomains().add(StringUtils.EMPTY);
    }

    public String removeDomain(int index) {
        return getDomains().remove(index);
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
