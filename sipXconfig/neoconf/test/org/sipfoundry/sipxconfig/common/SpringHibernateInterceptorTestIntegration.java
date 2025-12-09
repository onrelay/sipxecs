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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


import org.apache.commons.lang3.StringUtils;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.sipfoundry.sipxconfig.gateway.acme.AcmeGateway;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.springframework.context.ApplicationContext;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class SpringHibernateInterceptorTestIntegration
    extends IntegrationTestCase {

    private SpringHibernateInterceptor m_instantiator;

    /* This method is named init as the superclass has defined the
     * setUp method to be final.  Therefore, each test must explicitly
     * call this method
     */
    protected void init() throws Exception {
        ApplicationContext m_applicationContext = TestHelper.getApplicationContext();
        m_instantiator = new SpringHibernateInterceptor();
        m_instantiator.setBeanFactory(m_applicationContext);
        // to make sure that test are valid
        assertTrue(m_applicationContext.getBeanNamesForType(Gateway.class).length > 1);
    }

/* No longer supported by Hibernate
    public void testInstantiate() throws Exception {
        init();
        Integer id = Integer.valueOf(5);
        BeanWithId bean = (BeanWithId) m_instantiator.instantiate(Gateway.class, id);
        assertSame(Gateway.class, bean.getClass());
        assertSame(id, bean.getId());
        BeanWithId bean2 = (BeanWithId) m_instantiator.instantiate(Gateway.class, id);
        assertNotSame(bean2, bean);
    }

    public void testInstantiateSubclass() throws Exception {
        init();
        Integer id = Integer.valueOf(5);
        BeanWithId bean = (BeanWithId) m_instantiator.instantiate(AcmeGateway.class, id);
        assertSame(AcmeGateway.class, bean.getClass());
        assertSame(id, bean.getId());
    }

    public void testInstantiateUnknown() throws Exception {
        init();
        Integer id = Integer.valueOf(5);
        // there is a good chance we will not have StringUtils in beanFactory
        Object bean = m_instantiator.onLoad(new String(), id);
        assertNull(bean);
        Object bean2 = m_instantiator.onLoad(new String(), id);
        assertNull(bean2);
    }
    */
}
