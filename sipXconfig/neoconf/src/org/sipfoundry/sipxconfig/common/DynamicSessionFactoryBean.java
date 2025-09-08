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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import org.hibernate.Interceptor;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.ClassPathResource;


public class DynamicSessionFactoryBean extends HibernateConfigurationPlugin implements FactoryBean<SessionFactory>, InitializingBean, BeanFactoryAware {
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
            + "   \"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">";
    public static final String MAPPING_PATTERN = "<hibernate-mapping default-lazy=\"false\">"
            + "<subclass name=\"%s\" extends=\"%s\" discriminator-value=\"%s\"/>"
            + "</hibernate-mapping>";

    private ListableBeanFactory m_beanFactory;

    private DataSource m_dataSource;

    private SessionFactory m_sessionFactory;

    /**
     * Collection of bean IDs that representing superclasses of the hierarchy. All beans of the
     * same type as bean ID will be automatically mapped to the same hibernate table.
     */
    private List<String> m_baseClassBeanIds = Collections.EMPTY_LIST;

    private Configuration m_config;

    private Properties m_hibernateProperties;

    public DynamicSessionFactoryBean() {}

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setHibernateProperties(Properties hibernateProperties) {
        m_hibernateProperties = hibernateProperties;
    }

    public Properties getHibernateProperties() {
        return m_hibernateProperties;
    }

    public Configuration getConfig() {
        return m_config;
    }

    public SessionFactory getObject() {
        return m_sessionFactory;
    }

    public Class<?> getObjectType() {
        return SessionFactory.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {

        try {
            Configuration config = new Configuration();

            config.setProperties(m_hibernateProperties);

            for (String mappingResource : super.getMappingResources() ) {
                config.addResource(mappingResource);  
            }

            StandardServiceRegistryBuilder registryBuilder = 
                new StandardServiceRegistryBuilder()
                    .applySettings(config.getProperties());

            registryBuilder.applySetting("hibernate.connection.datasource", m_dataSource);


            m_sessionFactory = config.buildSessionFactory( registryBuilder.build() );

            for (String baseClassBeanID : m_baseClassBeanIds) {
                bindSubclasses(config, baseClassBeanID);
            }

            Interceptor interceptor = m_beanFactory.getBean("springInstantiator", Interceptor.class);
            config.setInterceptor(interceptor);

            m_config = config;   

        } catch( UnsupportedOperationException e ) {
            
            if( "The application must supply JDBC connections".equals( e.getMessage() ) ) {
                throw new IllegalStateException( "JDBC interface not ready");
            }
            else {
                throw e;
            }
            
        } catch (NoSuchBeanDefinitionException ex) {
            throw new RuntimeException(ex);
        } 
    }



    /**
     * Finds all subclasesses of baseClass in the bean factory and binds them to the same table as
     * base class using bean id as a discriminator value.
     *
     * @param config hibernate config that will be modified
     * @param baseClass base class - needs to be already mapped statically
     */
    protected void bindSubclasses(Configuration config, Class<?> baseClass) {
        String[] beanDefinitionNames = m_beanFactory.getBeanNamesForType(baseClass);
        for (String beanId : beanDefinitionNames) {
            Class<?> subClass = m_beanFactory.getType(beanId);
            if (subClass == baseClass) {
                continue; // skip baseclass which is already mapped
            }
            String mapping = xmlMapping(baseClass, subClass, beanId);
            try (InputStream xmlStream = new ByteArrayInputStream(mapping.getBytes(StandardCharsets.UTF_8))) {
                config.addInputStream(xmlStream);
            } catch (IOException e) {
                throw new HibernateException("Failed to add subclass mapping for bean: " + beanId, e);
            }
        }
    }

    /**
     * Finds all subclasesses of baseClass in the bean factory and binds them to the same table as
     * base class using bean id as a discriminator value.
     *
     * @param config hibernate config that will be modified
     * @param baseClassBeanId - bean representing the base class - needs to be already mapped
     *        statically
     */
    protected void bindSubclasses(Configuration config, String baseClassBeanId) {
        Class baseClass = m_beanFactory.getType(baseClassBeanId);
        bindSubclasses(config, baseClass);
    }

    /**
     * Create XML that contains a single subclass mapping
     *
     * @param baseClass already mapped hibernate entity class
     * @param subClass new entity to be mapped
     * @param discriminator value of disciminator for this subclass
     * @return xml string that can be parsed by hibernate to add new mapping
     */
    String xmlMapping(Class baseClass, Class subClass, String discriminator) {
        StringBuilder mapping = new StringBuilder(HEADER);
        Formatter formatter = new Formatter(mapping);
        formatter.format(MAPPING_PATTERN, subClass.getName(), baseClass.getName(), discriminator);
        formatter.close();
        return mapping.toString();
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ListableBeanFactory)) {
            throw new BeanInitializationException(getClass()
                    + " only works with ListableBeanFactory");
        }
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }

    public void setBaseClassBeanIds(List<String> baseClassBeanIds) {
        m_baseClassBeanIds = baseClassBeanIds;
    }

    public void postProcessMappings(Configuration config) throws HibernateException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Map<String, HibernateConfigurationPlugin> beans = m_beanFactory
                .getBeansOfType(HibernateConfigurationPlugin.class);
        for (HibernateConfigurationPlugin bean : beans.values()) {
            InputStream is = null;
            for (String resourceName : bean.getMappingResources()) {
                try {
                    ClassPathResource resource = new ClassPathResource(resourceName, classLoader);
                    is = resource.getInputStream();
                    config.addInputStream(is);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }


}
