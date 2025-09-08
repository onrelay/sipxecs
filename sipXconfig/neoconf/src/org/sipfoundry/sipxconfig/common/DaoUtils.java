/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.common;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.BeanWithId.IdToBean;
import org.sipfoundry.sipxconfig.common.event.DaoEventPublisher;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.setting.BeanWithGroups;
import org.sipfoundry.sipxconfig.setting.Group;


/**
 * Utilities for Hibernate DAOs
 */
public final class DaoUtils {
    public static final int PAGE_SIZE = 1000;
    private static final String ID_PROPERTY_NAME = "id";

    private DaoUtils() {
        // Utility class - do not instantiate
    }

    /**
     * Return true if query returns objects other than obj. Used to check for duplicates. The
     * query returns the ID strings of all objects for which the specified property has the
     * specified value. If exception is non-null, then throw the exception instead of returning
     * true.
     *
     * @param session spring session 
     * @param beanClass klass of the entity bean, it's usually one of the base classes of the obj
     *        and not obj.getClass()
     * @param bean object to be checked
     * @param propName name of the property to be checked
     * @param exception exception to throw if query returns other object than passed in the query
     */
    public static boolean checkDuplicates(
            Session session,
            Class<? extends BeanWithId> beanClass,
            BeanWithId bean,
            String propName,
            UserException exception) {

        Object propValue = getProperty_(bean, propName);
        if (propValue == null) {
            return false;
        }

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<? extends BeanWithId> root = cq.from(beanClass);

        cq.select(root.get("id"))
        .where(cb.equal(root.get(propName), propValue));

        List<Integer> idList = session.createQuery(cq).getResultList();
        return checkDuplicates(bean, idList, exception);
    }

    /**
     * Return true if query returns objects other than obj. Used to check for duplicates. If
     * exception is non-null, then throw the exception instead of returning true.
     *
     * @param session spring session 
     * @param obj object to be checked
     * @param queryName name of the query to be executed (define in *.hbm.xml file)
     * @param value parameter for the query
     * @param exception exception to throw if query returns other object than passed in the query
     */
    public static boolean checkDuplicatesByNamedQuery(Session session, BeanWithId obj, String queryName,
            Object value, UserException exception) {
        if (value == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Integer> objs = session.createNamedQuery(queryName, Integer.class)
                                    .setParameter("value", value)
                                    .getResultList();

        return checkDuplicates(obj, objs, exception);
    }

    /**
     * Return true if list contains objects other than obj. Used to check for duplicates. If
     * exception is non-null, then throw the exception instead of returning true.
     *
     * @param obj object to be checked
     * @param objs results for query
     * @param exception exception to throw if query returns other object than passed in the query
     */
    public static boolean checkDuplicates(BeanWithId obj, Collection<Integer> objs, UserException exception) {
        // no match
        if (objs.size() == 0) {
            return false; // there are no duplicates
        }

        // detect 1 match, itself
        if (!obj.isNew() && objs.size() == 1) {
            Integer found = (Integer) objs.iterator().next();
            if (found.equals(obj.getId())) {
                return false; // there are no duplicates
            }
        }

        // there are duplicates
        if (exception != null) {
            throw exception;
        }
        return true;
    }

    /**
     * Catch database corruption errors where more than one record exists. In general fields
     * should have unique indexes set up to protect against this. This method is created as a safe
     * check only, there have been not been any experiences of corrupt data to date.
     *
     * @param c
     * @param query
     *
     * @return first item from the collection
     * @throws IllegalStateException if more than one item in collection.
     */
    public static <T> T requireOneOrZero(Collection<T> c, String query) {
        if (c.size() > 1) {
            // DatabaseCorruptionException ?
            // TODO: move error string construction to new UnexpectedQueryResult(?) class, enable
            // localization
            StringBuffer error = new StringBuffer().append("read ").append(c.size())
                    .append(" and expected zero or one. query=").append(query);
            throw new IllegalStateException(error.toString());
        }
        Iterator<T> i = c.iterator();

        return (i.hasNext() ? c.iterator().next() : null);
    }

    // Put an underscore at the end of the method name to suppress a bogus
    // warning from Checkstyle about this method being unused.
    private static Object getProperty_(Object obj, String propName) {
        Object propValue = null;
        try {
            propValue = BeanUtils.getProperty(obj, propName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return propValue;
    }

    /**
     * Returns the collection of loaded hibernate beans
     *
     * @param session spring session 
     * @param klass klass of the objects to be loaded
     * @param ids collection of object ids
     * @return newly created collection of objects loaded by hibernate
     */
    public static Collection<Object> loadBeanByIds(Session session, Class<?> klass, Collection<Integer> ids) {
        IdToBean idToBean = new IdToBean(session, klass);
        return CollectionUtils.collect(ids, idToBean);
    }

    /**
     * Returns array of beans loaded thru DataObjectSource
     */
    public static <T> T[] loadBeansArrayByIds(DataObjectSource<T> source, Class<T> beanClass, Collection<Integer> ids) {
        T[] beans = (T[]) Array.newInstance(beanClass, ids.size());
        Iterator<Integer> idsIterator = ids.iterator();
        for (int i = 0; idsIterator.hasNext(); i++) {
            Integer id = (Integer) idsIterator.next();
            beans[i] = (T) source.load(beanClass, id);
        }

        return beans;
    }

    /**
     * Performs operation on all the bean in the list. List of the bean is passed as list of ids,
     * beans are loaded by hibernate before operation starts.
     *
     * After operation is performed all the beans are saved.
     */
    public static void doForAllBeanIds(
            Session session,
            DaoEventPublisher eventPublisher,
            Transformer<Object, Object> beanTransformer,
            Class<?> klass,
            Collection<Integer> ids) {

        Transformer<Integer, Object> transformer = new ChainedTransformer<>(
            new Transformer[] {
                (Transformer<Integer, Object>) id -> session.find(klass, id),
                beanTransformer
            });

        Collection<Object> beans = CollectionUtils.collect(ids, transformer);

        for (Object bean : beans) {
            eventPublisher.publishSave(bean);
            session.merge(bean);  // merge as we go
        }

    }

    public static void addToGroup(Session session,
                                  DaoEventPublisher eventPublisher,
                                  Integer groupId,
                                  Class<?> klass,
                                  Collection<Integer> ids) {
        Group group = session.byId(Group.class).load(groupId);
        Transformer<Object, Object> addTag = new BeanWithGroups.AddTag(group);
        doForAllBeanIds(session, eventPublisher, addTag, klass, ids);
    }

    public static void removeFromGroup(Session session,
                                    DaoEventPublisher eventPublisher,
                                    Integer groupId,
                                    Class<?> klass,
                                    Collection<Integer> ids) {
        Group group = session.find(Group.class, groupId);
        Transformer<Object, Object> removeTag = new BeanWithGroups.RemoveTag(group);
        doForAllBeanIds(session, eventPublisher, removeTag, klass, ids);
    }

    /**
     * Executes the given closure for all users.
     *
     * Whenever something needs to be done for all users this method (rather than loadUsers should
     * be used) since it does not load all users in the memory.
     *
     * @param closure the closure to perform
     */
    public static void forAllUsersDo(CoreContext coreContext, Closure<User> closure) {
        int userIndex = 0;
        int size = 0;
        do {
            userIndex += size;
            List<User> users = coreContext.loadUsersByPage(userIndex, PAGE_SIZE);
            for (User user : users) {
                closure.execute(user);
            }
            size = users.size();
        } while (size == PAGE_SIZE);
    }

    /**
     * Executes the given closure for all users.
     *
     * Whenever something needs to be done for all users this method (rather than loadUsers should
     * be used) since it does not load all users in the memory.
     *
     * It offers more flexibility, it allows specify start and limit.
     *
     * @param closure the closure to perform
     */
    public static void forAllUsersDo(CoreContext coreContext, Closure<User> closure, int start, int pageSize) {
        if (start >= coreContext.getAllUsersCount()) {
            return;
        }
        List<Integer> users = coreContext.loadUserIdsByPage(start, pageSize);
        for (Integer id : users) {
            closure.execute(coreContext.loadUser(id));
        }
    }

    public static void forAllGroupMembersDo(CoreContext coreContext, Group group, Closure<User> closure, int start,
            int pageSize) {
        if (start >= coreContext.getGroupMembersCount(group.getId())) {
            return;
        }
        Collection<Integer> users = coreContext.getGroupMembersByPage(group.getId(), start, pageSize);
        for (int id : users) {
            closure.execute(coreContext.loadUser(id));
        }
    }

    public static void forAllPhoneGroupMembersDo(PhoneContext phoneContext, Group group, Closure<Phone> closure,
            int start, int pageSize) {
        final String[] order = new String[] {
            ID_PROPERTY_NAME
        };
        if (start >= phoneContext.getPhonesInGroupCount(group.getId())) {
            return;
        }
        Collection<Phone> phones = phoneContext.loadPhonesByPage(group.getId(), start, pageSize, order, true);
        for (Phone phone : phones) {
            closure.execute(phone);
        }
    }

    public static void forAllBranchMembersDo(CoreContext coreContext, Branch branch, Closure<User> closure,
            int start, int pageSize) {
        if (start >= coreContext.getBranchMembersCount(branch.getId())) {
            return;
        }
        Collection<Integer> users = coreContext.getBranchMembersByPage(branch.getId(), start, pageSize);
        for (int id : users) {
            closure.execute(coreContext.loadUser(id));
        }
    }

    public static void forAllPhonesDo(PhoneContext phoneContext, Closure<Phone> closure, int start, int pageSize) {
        if (start >= phoneContext.getPhonesCount()) {
            return;
        }
        List<Phone> phones = phoneContext.loadPhonesByPage(start, pageSize);
        for (Phone phone : phones) {
            closure.execute(phone);
        }
    }
}
