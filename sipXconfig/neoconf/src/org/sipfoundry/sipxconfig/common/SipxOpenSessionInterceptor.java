package org.sipfoundry.sipxconfig.common;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class SipxOpenSessionInterceptor implements MethodInterceptor, InitializingBean {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean participate = false;

        // Check if a session is already bound
        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            participate = true;
        } else {
            Session session = sessionFactory.openSession();
            TransactionSynchronizationManager.bindResource(sessionFactory, session);
        }

        try {
            return invocation.proceed();
        } finally {
            if (!participate) {
                Session session = (Session) TransactionSynchronizationManager.unbindResource(sessionFactory);
                if (session.isOpen()) {
                    session.close();
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (this.sessionFactory == null) {
            throw new IllegalArgumentException("Property 'sessionFactory' is required");
        }
    }
}