/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.UnsupportedOperationException;
import java.lang.IllegalStateException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.beanutils.BeanUtils;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.dao.support.DaoSupport;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Join;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.persister.entity.EntityPersister;

import org.sipfoundry.sipxconfig.common.event.DaoEventPublisher;
import org.sipfoundry.sipxconfig.setting.BeanWithSettings;
import org.sipfoundry.sipxconfig.setting.Storage;
import org.sipfoundry.sipxconfig.setting.ValueStorage;


public class SipxHibernateDaoSupport<T> extends DaoSupport implements DataObjectSource<T> {
    
    private SessionFactory m_sessionFactory;

    private DaoEventPublisher m_daoEventPublisher;


    public SipxHibernateDaoSupport() {
    }

    public DaoEventPublisher getDaoEventPublisher() {
        return m_daoEventPublisher;
    }

    public void setDaoEventPublisher(DaoEventPublisher daoEventPublisher) {
        m_daoEventPublisher = daoEventPublisher;
    }

    protected final void checkDaoConfig() {
      if (m_sessionFactory == null) {
         throw new IllegalArgumentException("SessionFactory not set");
      }
    }

    public SessionFactory getSessionFactory() {
        return m_sessionFactory;
    }

    public void setSessionFactory( SessionFactory sessionFactory ) {
        m_sessionFactory = sessionFactory;
    }

    public T load(Class<T> klass, Serializable id) {
        return loadEntity( klass, id );
    }

    public <S extends Object> S loadEntity(Class<S> klass, Serializable id) {
   
        S entity = getSessionFactory().fromTransaction( session -> {

            return session.byId(klass).load(id);

        });

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }
        return entity;
    }

    public <S extends Object> List<S> loadAllEntities(Class<S> klass) {

        try {
            List<S> allEntities = getSessionFactory().fromTransaction( session -> {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<S> cq = cb.createQuery(klass);
                cq.from(klass);
                return session.createQuery(cq).getResultList();
            });

            if( allEntities != null ) {
                for( S entity : allEntities ) {
                    if( entity instanceof BeanWithSettings ) {
                        updateBeanValueStorage( (BeanWithSettings)entity);
                    }                
                }
            }
            return allEntities;

        } catch( IllegalStateException e ) {
            // server not ready
            return new ArrayList<S>();
        }
    }

    public <S extends Object> S findEntity(Class<S> klass, Serializable id) {

        S entity = getSessionFactory().fromTransaction( session -> {

            try {
                return (S) session.find(klass,id);
            } catch( IllegalStateException e ) {
                // server not ready
                return null;
            }
        });

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }
        return entity;
    }

    public <S extends Object> void saveEntity(S entity) {

        boolean isNew = false;

        if( entity instanceof BeanWithId ) {
            isNew = ((BeanWithId)entity).isNew();
        }

        if( isNew ) {
            persistEntity( entity );
        }
        else {
            mergeEntity( entity );
        }
    }

    public <S extends Object> void persistEntity(S entity) {

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }

        getSessionFactory().inTransaction( session -> {
            
            try{ 
                session.persist(entity);

            } catch( IllegalStateException e ) {
            }
        });
    }

    public <S extends Object> void mergeEntity(S entity) {

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }

        getSessionFactory().inTransaction( session -> {
            
            try {
                S mergedEntity = session.merge(entity);
        
                BeanUtils.copyProperties(mergedEntity, entity);

            } catch( IllegalStateException | IllegalAccessException | InvocationTargetException e ) {
            }
        });
    }

    public <S extends Object> void refreshEntity(S entity) {

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }

        getSessionFactory().inTransaction( session -> {
            
            try {
                session.refresh(entity);
            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    public <S extends Object> void removeEntity(S entity) {

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }

        getSessionFactory().inTransaction( session -> {
            
            try {
                session.remove(entity);
            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    public <S extends Object> void removeAllEntities(Collection<S> entities) {

        for (S entity : entities) {

            if( entity instanceof BeanWithSettings ) {
                updateBeanValueStorage( (BeanWithSettings)entity);
            }

            getSessionFactory().inTransaction( session -> {
                
                try {
                    session.remove(entity);
                                
                } catch( IllegalStateException e ) {
                    // server not ready
                }
            });
        }
    }

    public <S extends Object> void evictEntity(S entity) {

        if( entity instanceof BeanWithSettings ) {
            updateBeanValueStorage( (BeanWithSettings)entity);
        }

        getSessionFactory().inTransaction( session -> {
                
            try {
                session.evict(entity);
            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    public void flush() {
        
        getSessionFactory().inTransaction( session -> {
                
            try {
                session.flush();
            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    public void clear() {
        
        getSessionFactory().inTransaction( session -> {
                
            try {
                session.clear();
            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedParam(String queryText, String[] paramNames, Object[] values, Class<S> resultClass) {
        
        if( paramNames.length != values.length ) {
            throw new RuntimeException("queryNames and values must have same size");
        }
        
        List<S> entities = getSessionFactory().fromTransaction( session -> {
                
            try {
                Query<S> query = session.createQuery(queryText, resultClass);
                for( int i = 0; i < paramNames.length; i++ ) {

                    query = query.setParameter(paramNames[i], values[i]);
                }
                return query.getResultList();
            } catch( IllegalStateException e ) {
                // server not ready
                return new ArrayList<S>();
            }
        });

        if( entities != null ) {
            for( S entity : entities ) {
                if( entity instanceof BeanWithSettings ) {
                    updateBeanValueStorage( (BeanWithSettings)entity);
                }                
            }
        }

        return entities;
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedParam(String queryText, String paramName, Object value, Class<S> resultClass) {
        
        return findByNamedParam( queryText, new String[] { paramName },  new Object[] { value }, resultClass );
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> find(String queryText, Class<S> resultClass) {
        
        return findByNamedParam( queryText, new String[0],  new Object[0], resultClass );
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values, Class<S> resultClass) {
        
        if( paramNames.length != values.length ) {
            throw new RuntimeException("queryNames and values must have same size");
        }
        
        List<S> entities = getSessionFactory().fromTransaction( session -> {

            try {
                Query<S> query = session.createNamedQuery(queryName, resultClass);
                for( int i = 0; i < paramNames.length; i++ ) {

                    query = query.setParameter(paramNames[i], values[i]);
                }
                return query.getResultList();
            } catch( IllegalStateException e ) {
                // server not ready
                return new ArrayList<S>();
            }
        });

        if( entities != null ) {
            for( S entity : entities ) {
                if( entity instanceof BeanWithSettings ) {
                    updateBeanValueStorage( (BeanWithSettings)entity);
                }                
            }
        }

        return entities;
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedQueryAndNamedParam(String queryName, String paramName, Object value, Class<S> resultClass) {
        
        return findByNamedQueryAndNamedParam( queryName, new String[] { paramName },  new Object[] { value }, resultClass );
    }


    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedQuery(String queryName, Object[] values, Class<S> resultClass) {

        List<S> entities = getSessionFactory().fromTransaction( session -> {

            try {

                Query<S> query = session.createNamedQuery(queryName, resultClass);
                for( int i = 0; i < values.length; i++ ) {

                    query = query.setParameter(i + 1, values[i]);
                }
                return query.getResultList();
            } catch( IllegalStateException e ) {
                // server not ready
                return new ArrayList<S>();
            }
        });

        if( entities != null ) {
            for( S entity : entities ) {
                if( entity instanceof BeanWithSettings ) {
                    updateBeanValueStorage( (BeanWithSettings)entity);
                }                
            }
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedQuery(String queryName, Object value, Class<S> resultClass) {

        return findByNamedQuery( queryName, new Object[] { value }, resultClass );
    }

    @SuppressWarnings("unchecked")
    public <S> List<S> findByNamedQuery(String queryName, Class<S> resultClass) {

        return findByNamedQuery( queryName, new Object[0], resultClass );
    }

    /**
     * Duplicate the bean and return the duplicate. If the bean is a NamedObject, then give the
     * duplicate a new, unique name. The queryName identifies a named query that returns the IDs
     * of all objects with a given name. (Return IDs rather than objects to avoid the overhead of
     * loading all the objects.) Use the query to ensure that the new name is unique.
     *
     * @param bean bean to duplicate
     * @param queryName name of the query to be executed (define in *.hbm.xml file)
     */
    public BeanWithId duplicateBean(BeanWithId bean, String queryName) {
        
        BeanWithId copy = bean.duplicate();

        if (bean instanceof NamedObject) {

            NamedObject namedCopy = (NamedObject) copy;

            namedCopy.setName(((NamedObject) bean).getName());

            boolean duplicates = true;

            while( duplicates ) {

                namedCopy.setName("CopyOf" + namedCopy.getName());

                duplicates = getSessionFactory().fromTransaction( session -> {

                    return DaoUtils.checkDuplicatesByNamedQuery(session, copy, queryName, namedCopy.getName(), null);
                });
            }
        }

        return copy;
    }

    @SuppressWarnings("rawtypes")
    public List<T> loadBeansByPage(Class beanClass, Integer groupId, int firstRow, int pageSize,
            String[] orderBy, boolean orderAscending) {
        return loadBeansByPage(beanClass, groupId, null, firstRow, pageSize, orderBy, orderAscending);
    }

    public List<T> loadBeansByPage(Class<T> beanClass, Integer groupId, Integer branchId, int firstRow, int pageSize,
                                String[] orderBy, boolean orderAscending) {
        
        List<T> beans = getSessionFactory().fromTransaction( session -> {
            try {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<T> cq = cb.createQuery(beanClass);
                Root<T> root = cq.from(beanClass);

                List<Predicate> predicates = new ArrayList<>();
                
                Predicate groupPredicate = addByGroupCriteria(cb, root, groupId);
                if (groupPredicate != null) {
                    predicates.add(groupPredicate);
                }
                
                Predicate branchPredicate = addByBranchCriteria(cb, root, branchId);
                if (branchPredicate != null) {
                    predicates.add(branchPredicate);
                }

                if (!predicates.isEmpty()) {
                    cq.where(cb.and(predicates.toArray(new Predicate[0])));
                }

                if (orderBy != null) {
                    List<Order> orders = new ArrayList<>();
                    for (String o : orderBy) {
                        orders.add(orderAscending ? cb.asc(root.get(o)) : cb.desc(root.get(o)));
                    }
                    cq.orderBy(orders);
                }

                TypedQuery<T> query = session.createQuery(cq);
                query.setFirstResult(firstRow);
                query.setMaxResults(pageSize);

                return query.getResultList();

            } catch( IllegalStateException e ) {
                // server not ready
                return new ArrayList<T>();
            }
        });

        if( beans != null ) {
            for( T bean : beans ) {
                if( bean instanceof BeanWithSettings ) {
                    updateBeanValueStorage( (BeanWithSettings)bean);
                }                
            }
        }

        return beans;
    }

    @SuppressWarnings("rawtypes")
    public List<T> loadBeansByPage(Class beanClass, int firstRow, int pageSize) {
        String[] orderBy = new String[] {
            "id"
        };
        return loadBeansByPage(beanClass, null, null, firstRow, pageSize, orderBy, true);
    }

    /**
     * Return the count of beans of type beanClass in the specified group. If groupId is null,
     * then don't filter by group, just count all the beans.
     */
    public <T> int getBeansInGroupCount(Class<T> beanClass, Integer groupId) {
        
        return getSessionFactory().fromTransaction( session -> {
            
            try {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                Root<T> root = cq.from(beanClass);

                Predicate groupPredicate = addByGroupCriteria(cb, root, groupId);
                if (groupPredicate != null) {
                    cq.where(groupPredicate);
                }

                cq.select(cb.count(root));

                Long count = session.createQuery(cq).getSingleResult();

                return count;

            } catch( IllegalStateException e ) {
                // server not ready
                return 0;
            }
        }).intValue();
    }

    protected void removeAll(Class<?> klass, Collection<Integer> ids) {
        
        getSessionFactory().inTransaction( session -> {
            
            try {
                Collection<Object> entities = new ArrayList<>(ids.size());

                for (Integer id : ids) {
                    Object entity = session.find(klass, id); 
                    if (entity != null) {
                        entities.add(entity);
                        m_daoEventPublisher.publishDelete(entity);
                    }
                }

                for (Object entity : entities) {
                    session.remove(entity);
                }

                session.flush();

            } catch( IllegalStateException e ) {
                // server not ready
            }
        });
    }

    protected void removeAll(Class<?> klass) {
        
        List<?> entities = getSessionFactory().fromTransaction( session -> {
            
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<?> cq = cb.createQuery(klass);
            cq.from(klass);

            return session.createQuery(cq).getResultList();
        });

        for (Object entity : entities) {

            m_daoEventPublisher.publishDelete(entity);

            getSessionFactory().inTransaction( session -> {
                session.remove(entity);
            });
        }

        getSessionFactory().inTransaction( session -> {

            session.flush(); 
        });
       
    }

    protected void updateBeanValueStorage(BeanWithSettings bean) {
        Storage origStorage = bean.getInitializeValueStorage();
        Storage cleanStorage = clearUnsavedValueStorage(origStorage);
        bean.setValueStorage(cleanStorage);
    }

    protected Storage clearUnsavedValueStorage(Storage storage) {
        // requirement, otherwise you wouldn't be calling this function
        ValueStorage vs = (ValueStorage) storage;

        // If no settings don't bother saving anything.
        return vs != null && vs.isNew() && vs.size() == 0 ? null : vs;
    }

    /**
     * Returns the original value of an object before it was modified by application. Represent
     * the original value from the database.
     */
    protected Object getOriginalValue(PrimaryKeySource obj, String propertyName) {
        
        return getSessionFactory().fromTransaction( session -> {

            try {            
                return new GetOriginalValueCallback(obj, propertyName).doInSession(session);
            } catch( IllegalStateException e ) {
                // server not ready
                return null;
            }
        });
    }

    /**
     * Update a Criteria object for filtering beans by group membership. If groupId is null, then
     * don't filter by group.
     */
    public static <T> Predicate addByGroupCriteria(CriteriaBuilder cb, Root<T> root, Integer groupId) {
        if (groupId != null) {
            Join<Object, Object> groupsJoin = root.join("groups");
            return cb.equal(groupsJoin.get("id"), groupId);
        }
        return null;
    }


    /**
     * Update a Criteria object for filtering beans by branch membership. If brnachId is null,
     * then don't filter by branch.
     */
    public static <T> Predicate addByBranchCriteria(CriteriaBuilder cb, Root<T> root, Integer branchId) {
        if (branchId != null) {
            Join<Object, Object> branchJoin = root.join("branch");
            return cb.equal(branchJoin.get("id"), branchId);
        }
        return null;
    }

    static class GetOriginalValueCallback {
        private final PrimaryKeySource m_object;
        private final String m_propertyName;

        GetOriginalValueCallback(PrimaryKeySource object, String propertyName) {
            m_object = object;
            m_propertyName = propertyName;
        }

        public Object doInSession(Session session) {
            Class<?> entityClass = m_object.getClass();
            Serializable id = (Serializable) m_object.getPrimaryKey();

            Object dbObject = session.byId(entityClass).load(id);
            if (dbObject == null) {
                return null;
            }

            try {
                PropertyDescriptor[] props = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();
                for (PropertyDescriptor prop : props) {
                    if (prop.getName().equals(m_propertyName)) {
                        Method getter = prop.getReadMethod();
                        if (getter != null) {
                            return getter.invoke(dbObject);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to access property '" + m_propertyName + "'", e);
            }

            return null;
        }
    }
}
