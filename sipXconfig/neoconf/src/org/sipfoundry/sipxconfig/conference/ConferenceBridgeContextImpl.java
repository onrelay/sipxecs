/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.conference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.ExtensionInUseException;
import org.sipfoundry.sipxconfig.common.NameInUseException;
import org.sipfoundry.sipxconfig.common.SameExtensionException;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.support.DataAccessUtils;

public class ConferenceBridgeContextImpl extends SipxHibernateDaoSupport<Conference> implements BeanFactoryAware,
        ConferenceBridgeContext, DaoEventListener {
    private static final String BUNDLE_CONFERENCE = "conference";
    private static final String CONFERENCE = "&label.conference";
    private static final String VALUE = "value";
    private static final String CONFERENCE_IDS_WITH_ALIAS = "conferenceIdsWithAlias";
    private static final String CONFERENCE_BY_NAME = "conferenceByName";
    private static final String CONFERENCE_BY_EXTENSION = "conferenceByExtension";
    private static final String OWNER = "owner";
    private static final String PERCENT = "%";

    private AliasManager m_aliasManager;
    private BeanFactory m_beanFactory;
    private DomainManager m_domainManager;

    public List<Bridge> getBridges() {
        return super.loadAllEntities(Bridge.class);
    }

    public void saveBridge(Bridge bridge) {

        super.saveEntity(bridge);
        super.flush();
    }

    public void saveConference(Conference conference) {
        validate(conference);
        super.saveEntity(conference);
    }

    public void validate(Conference conference) {
        String name = conference.getName();
        String extension = conference.getExtension();
        String did = conference.getDid();
        if (name == null) {
            throw new UserException("A conference must have a name");
        }
        if (extension == null) {
            throw new UserException("A conference must have an extension");
        }

        if (conference.getModeratorAccessCode() != null && conference.getParticipantAccessCode() == null) {
            throw new UserException("&error.moderator.and.participant.pin");
        }

        if (conference.getModeratorAccessCode() == null && !conference.isQuickstart()) {
            throw new UserException("&error.non.qs.no.mod");
        }

        if (!conference.isQuickstart()
                && StringUtils.equals(conference.getModeratorAccessCode(), conference.getParticipantAccessCode())) {
            throw new UserException("&error.moderator.eq.participant");
        }

        if (!m_aliasManager.canObjectUseAlias(conference, name)) {
            throw new NameInUseException(CONFERENCE, name);
        }
        if (!m_aliasManager.canObjectUseAlias(conference, extension)) {
            throw new ExtensionInUseException(CONFERENCE, extension);
        }
        if (!m_aliasManager.canObjectUseAlias(conference, did)) {
            throw new ExtensionInUseException(CONFERENCE, did);
        }
        if (StringUtils.isNotBlank(did) && did.equals(extension)) {
            throw new SameExtensionException("did", "extension");
        }
    }

    public Bridge newBridge() {
        return m_beanFactory.getBean(Bridge.BEAN_NAME, Bridge.class);
    }

    public Conference newConference() {
        Conference conference = m_beanFactory.getBean(Conference.BEAN_NAME, Conference.class);
        conference.generateAccessCodes();
        return conference;
    }

    private void removeConferences(List<Conference> conferences) {
        Collection<Integer> ids = new ArrayList<Integer>();
        for (Conference conf : conferences) {
            ids.add(conf.getId());
        }
        removeConferences(ids);
    }

    public void removeConferences(Collection<Integer> conferencesIds) {
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Iterator<Integer> i = conferencesIds.iterator(); i.hasNext();) {
            Object id = i.next();
            Conference conference = loadConference(id);
            getDaoEventPublisher().publishDelete(conference);
            Bridge bridge = conference.getBridge();
            bridge.removeConference(conference);
            bridges.add(bridge);
        }
        for( Bridge bridge : bridges ) {
            super.mergeEntity(bridge);
        }
        super.flush();
    }

    public Bridge loadBridge(Object id) {
        return super.loadEntity(Bridge.class, id);
    }

    public Bridge getBridgeByServer(String hostname) {
        // TODO JPA This is temporarily commented out until I can figure out why loading the
        // object in this way
        // does not load dependent objects like the service and location...
        // List<Bridge> bridges = super.findByNamedQueryAndNamedParam(
        // "bridgeByHost", VALUE, hostname, Bridge.class);
        // return (Bridge) DataAccessUtils.singleResult(bridges2);

        Bridge bridgeForServer = null;
        List<Bridge> bridges = super.loadAllEntities(Bridge.class);
        for (Bridge b : bridges) {
            if (b != null) {
                if (b.getLocation() != null) {
                    if (b.getLocation().getFqdn().equalsIgnoreCase(hostname)) {
                        bridgeForServer = b;
                        break;
                    }
                }
            }
        }
        return bridgeForServer;
    }

    public Conference loadConference(Object id) {
        return super.loadEntity(Conference.class, id);
    }

    public Conference findConferenceByName(String name) {
        List<Conference> conferences = (List<Conference>)super.findByNamedQueryAndNamedParam(
            CONFERENCE_BY_NAME,
            VALUE, 
            name,
            Conference.class );
        return DataAccessUtils.singleResult(conferences);
    }

    public Conference findConferenceByExtension(String extension) {
        List<Conference> conferences = 
            (List<Conference>)super.findByNamedQueryAndNamedParam(
                CONFERENCE_BY_EXTENSION,
                VALUE, 
                extension,
                Conference.class);
        return DataAccessUtils.singleResult(conferences);
    }    
    
    public void clear() {
        List<Bridge> bridges = getBridges();
        super.removeAllEntities(bridges);
    }

    // trivial get/set
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    public boolean isAliasInUse(String alias) {
        List<Object> confIds = (List<Object>)super.findByNamedQueryAndNamedParam(
            CONFERENCE_IDS_WITH_ALIAS, VALUE, alias, Object.class );
        return !confIds.isEmpty();
    }

    public Collection<BeanId> getBeanIdsOfObjectsWithAlias(String alias) {
        Collection<Integer> ids = (Collection<Integer>)super.findByNamedQueryAndNamedParam(
            CONFERENCE_IDS_WITH_ALIAS, VALUE, alias, Integer.class);
        Collection<BeanId> bids = BeanId.createBeanIdCollection(ids, Conference.class);
        return bids;
    }

    public List<Conference> findConferencesByOwner(User owner) {
        List<Conference> conferences = (List<Conference>)super.findByNamedQueryAndNamedParam(
            "conferencesByOwner",
            OWNER, 
            owner,
            Conference.class );
        return conferences;
    }

    private Query<Conference> filterConferencesCriteria(final Integer bridgeId, final Integer ownerGroupId, Session session) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Conference> cq = cb.createQuery(Conference.class);
        Root<Conference> root = cq.from(Conference.class);

        // Join bridge
        Join<Object, Object> bridgeJoin = root.join("bridge");
        Predicate bridgePredicate = cb.equal(bridgeJoin.get("id"), bridgeId);

        Predicate finalPredicate = bridgePredicate;

        if (ownerGroupId != null) {
            // Join owner -> groups
            Join<Object, Object> ownerJoin = root.join("owner"); // assuming OWNER is "owner"
            Join<Object, Object> groupJoin = ownerJoin.join("groups");
            Predicate groupPredicate = cb.equal(groupJoin.get("id"), ownerGroupId);
            finalPredicate = cb.and(finalPredicate, groupPredicate);
        }

        cq.select(root).where(finalPredicate).distinct(true);

        return session.createQuery(cq);
    }

    public List<Conference> getAllConferences() {
        return super.loadAllEntities(Conference.class);
    }

    public List<Conference> filterConferences(final Integer bridgeId, final Integer ownerGroupId) {

        return super.getSessionFactory().fromTransaction( session -> {

            Query<Conference> query = filterConferencesCriteria(bridgeId, ownerGroupId, session);
            return query.getResultList();
        });
    }

    public List<Conference> searchConferences(final String searchTerm) {
        String searchTermLike = (new StringBuilder()).append(PERCENT).append(searchTerm).append(PERCENT).toString();
        List<Conference> conferences = new ArrayList<Conference>();
        List<Object[]> results = (List<Object[]>)super.findByNamedQueryAndNamedParam(
            "searchConferences",
                new String[] {
                    "name", "ext", "description", "ownerName", "ownerUName"
                }, new String[] {
                    searchTermLike, searchTerm, searchTermLike, searchTermLike, searchTerm
                },
                Object[].class);
        for (Object[] result : results) {
            for (int i = 0; i < result.length; i++) {
                if (result[i] instanceof Conference) {
                    conferences.add((Conference) result[i]);
                }
            }
        }
        return conferences;
    }

    public int countFilterConferences(final Integer bridgeId, final Integer ownerGroupId) {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Conference> root = cq.from(Conference.class);

            // Join to bridge and add predicate
            Join<?, ?> bridgeJoin = root.join("bridge");
            Predicate predicate = cb.equal(bridgeJoin.get("id"), bridgeId);

            // Optional join to owner -> groups
            if (ownerGroupId != null) {
                Join<?, ?> ownerJoin = root.join("owner");
                Join<?, ?> groupJoin = ownerJoin.join("groups");
                Predicate groupPredicate = cb.equal(groupJoin.get("id"), ownerGroupId);
                predicate = cb.and(predicate, groupPredicate);
            }

            cq.select(cb.count(root)).where(predicate);

            // Wrap result as List to match original return shape
            Long count = session.createQuery(cq).getSingleResult();
        
            List<Long> list = List.of(count);;
            return (list != null && !list.isEmpty()) ? list.get(0) : 0;
        }).intValue();
    }

    public List<Conference> filterConferencesByPage(final Integer bridgeId, final Integer ownerGroupId,
            final int firstRow, final int pageSize, final String[] orderBy, final boolean orderAscending) {

        return super.getSessionFactory().fromTransaction( session -> {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conference> cq = cb.createQuery(Conference.class);
            Root<Conference> root = cq.from(Conference.class);

            // Build predicates same as before
            Join<Object, Object> bridgeJoin = root.join("bridge");
            Predicate predicate = cb.equal(bridgeJoin.get("id"), bridgeId);

            if (ownerGroupId != null) {
                Join<Object, Object> ownerJoin = root.join("owner");
                Join<Object, Object> groupsJoin = ownerJoin.join("groups");
                Predicate ownerGroupPredicate = cb.equal(groupsJoin.get("id"), ownerGroupId);
                predicate = cb.and(predicate, ownerGroupPredicate);
            }

            cq.where(predicate);

            // Build order list dynamically
            if (orderBy != null && orderBy.length > 0) {
                List<Order> orders = new ArrayList<>();
                for (String orderProp : orderBy) {
                    orders.add(orderAscending ? cb.asc(root.get(orderProp)) : cb.desc(root.get(orderProp)));
                }
                cq.orderBy(orders);
            }

            Query<Conference> query = session.createQuery(cq);
            query.setFirstResult(firstRow);
            query.setMaxResults(pageSize);

            return query.getResultList();
        });
    }

    public String getAddressSpec(Conference conference) {
        String domain = m_domainManager.getDomain().getName();
        return SipUri.fix(conference.getExtension(), domain);
    }

    public Bridge getBridgeForLocationId(Integer locationId) {
        List<Bridge> servers = (List<Bridge>)super.findByNamedQueryAndNamedParam(
            "bridgeForLocationId", 
            "locationId",
            locationId,
            Bridge.class);

        return DataAccessUtils.singleResult(servers);
    }

    
    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    @Override
    public void removeBridge(Bridge bridge) {
        super.removeEntity(bridge);
    }

    @Override
    public void onDelete(Object entity) {
        if (entity instanceof User) {
            User u = (User) entity;
            removeConferences(findConferencesByOwner(u));
        }
        if (entity instanceof Location) {
            Location l = (Location) entity;
            Bridge b = getBridgeForLocationId(l.getId());
            if (b != null) {
                removeBridge(b);
            }
        }
    }

    @Override
    public void onSave(Object entity) {
    }

}
