/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.registrar;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.map.LazyMap;
import org.sipfoundry.sipxconfig.commserver.imdb.RegistrationItem;

/**
 * Metrics about registration distributions
 */
public class RegistrationMetrics {

    private Set<RegistrationItem> m_registrations;

    private Set<RegistrationItem> m_activeRegistrations;

    private Map<String, RegistrationItem> m_uniqueRegistrations;

    private long m_startTime;

    public Collection<RegistrationItem> getRegistrations() {
        if( m_registrations == null ) {
            m_registrations = new LinkedHashSet<>();
        }
        return m_registrations;
    }

    public Map<String, RegistrationItem> getUniqueRegistrations() {
        if( m_uniqueRegistrations == null ) {
            m_uniqueRegistrations = new TreeMap<>();
        }
        return m_uniqueRegistrations;
    }

    public Collection<RegistrationItem> getActiveRegistrations() {
        if( m_activeRegistrations == null ) {
            m_activeRegistrations = new LinkedHashSet<>();
        }
        return m_activeRegistrations;
    }

    public int getActiveRegistrationCount() {
        return getActiveRegistrations().size();
    }

    public Collection<RegistrationItem> getTotalRegistrations() {

        return getUniqueRegistrations().values();
    }

    public int getTotalRegistrationCount() {
        return getTotalRegistrations().size();
    }

    public Collection<String> getContacts() {
        return getUniqueRegistrations().keySet();
    }

    /**
     * All registrations from registration.xml which may include redunant
     * registrations from clients that reregister w/slighlty different uri
     * w/o unregistering last uri.
     */
    public void setRegistrations(List<RegistrationItem> registrations) {

        getRegistrations().clear();

        if( registrations != null ) {

            for( RegistrationItem registration : registrations ) {

                if( registration != null ) {

                    String contact = registration.getContact();

                    RegistrationItem oldRegistration = getUniqueRegistrations().get(contact);

                    if( oldRegistration == null || registration.compareTo(oldRegistration) > 0 )  {

                        getUniqueRegistrations().put( contact, registration );
                    }

                    if( registration.timeToExpireAsSeconds(m_startTime) > 0) {
                        getActiveRegistrations().add( registration );
                    }
                }
                getRegistrations().add( registration );
            }
        }
    }

    public void setStartTime(long startTime) {
        m_startTime = startTime;
    }

    public double getLoadBalance() {
        LoadDistribution metric = new LoadDistribution();

        for( RegistrationItem registrationItem : getTotalRegistrations() ) {
            metric.execute( registrationItem );
        }
        
        double loadBalance = metric.getLoadBalance();
        return loadBalance;
    }

    /**
     * Calculate how many registrations originated on each regististrar
     */
    static class LoadDistribution implements Closure {

        private long m_total;

        private Map<String, Integer> m_distribution =
            LazyMap.lazyMap(new HashMap<>(), FactoryUtils.constantFactory(0));

        public int getRegistrationCount(String server) {
            return m_distribution.get(server);
        }

        public void execute(Object input) {
            RegistrationItem reg = (RegistrationItem) input;
            String primary = reg.getPrimary();
            int count = m_distribution.get(primary);
            m_distribution.put(primary, count + 1);
            m_total++;
        }

        public double getLoadBalance() {
            double loadBalanceInverse = 0;
            for (int m : m_distribution.values()) {
                double ratioSquared = Math.pow((double) m / m_total, 2);
                loadBalanceInverse += ratioSquared;
            }
            if (loadBalanceInverse == 0) {
                return 1;
            }
            return 1 / loadBalanceInverse;
        }
    }
}
