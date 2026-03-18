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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import org.sipfoundry.sipxconfig.common.DaoUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.support.DataAccessUtils;

public class SbcManagerImpl extends SipxHibernateDaoSupport<Sbc> implements SbcManager, BeanFactoryAware {
    private DomainManager m_domainManager;
    private BeanFactory m_beanFactory;

    public DefaultSbc loadDefaultSbc() {
        List<DefaultSbc> sbcs = super.loadAllEntities(DefaultSbc.class);
        DefaultSbc sbc = (DefaultSbc) DataAccessUtils.singleResult(sbcs);
        return sbc;
    }


    public DefaultSbc getDefaultSbc() {
        DefaultSbc sbc = loadDefaultSbc();
        if (sbc == null ) {
            sbc = new DefaultSbc();

            SbcRoutes sbcRoutes = createDefaultSbcRoutes();
            sbc.replaceRoutes(sbcRoutes);

            super.persistEntity(sbc);
        }
        return sbc;
    }

    public List<AuxSbc> loadAuxSbcs() {
        return super.loadAllEntities(AuxSbc.class);
    }

    public void saveSbc(Sbc sbc) {
        super.saveEntity(sbc);
    }

    public AuxSbc loadSbc(Integer sbcId) {
        return (AuxSbc) super.loadEntity(AuxSbc.class, sbcId);
    }

    public void removeSbcs(Collection<Integer> selectedRows) {

        Collection<Object> sbcs = super.getSessionFactory().fromTransaction( session -> {

            return DaoUtils.loadBeanByIds(session, AuxSbc.class, selectedRows);
        });

        getDaoEventPublisher().publishDeleteCollection(sbcs);
        super.removeAllEntities(sbcs);
    }

    public void deleteSbc(Sbc sbc) {
        super.removeEntity(sbc);
    }

    public SbcRoutes getAllRoutes() {
        SbcRoutes routes = new SbcRoutes();
        Set<String> sbcDomains = new HashSet<String>();
        Set<String> sbcSubnets = new HashSet<String>();
        List<Sbc> sbcs = super.loadAllEntities(Sbc.class);
        for (Sbc sbc : sbcs) {
            sbcDomains.addAll(sbc.getRoutes().getDomains());
            sbcSubnets.addAll(sbc.getRoutes().getSubnets());
        }

        List<String> domains = new ArrayList<String>(sbcDomains);
        List<String> subnets = new ArrayList<String>(sbcSubnets);
        routes.setDomains(domains);
        routes.setSubnets(subnets);

        return routes;
    }

    public void clear() {
        removeAll(Sbc.class);
    }

    
    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

    protected SbcRoutes createDefaultSbcRoutes() {
        Domain domain = m_domainManager.getDomain();
        String wildcard = String.format("*.%s", domain.getName());
        List<String> domains = new ArrayList<String>();
        domains.add(wildcard);

        SbcRoutes sbcRoutes = (SbcRoutes) m_beanFactory.getBean("defaultSbcRoutes",
                SbcRoutes.class);
        sbcRoutes.setDomains(domains);

        return sbcRoutes;
    }
}
