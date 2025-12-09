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


import java.io.InputStream;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import org.hibernate.Interceptor;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import org.springframework.core.io.ClassPathResource;

public class DynamicSessionFactoryBean extends HibernateConfigurationPlugin
        implements FactoryBean<SessionFactory>, InitializingBean, BeanFactoryAware {


    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
            + "   \"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">";
    public static final String MAPPING_PATTERN = "<hibernate-mapping default-lazy=\"false\">"
            + "<subclass name=\"%s\" extends=\"%s\" discriminator-value=\"%s\"/>"
            + "</hibernate-mapping>";

    private ListableBeanFactory m_beanFactory;
    
    private DataSource m_dataSource;
    
    private SessionFactory m_sessionFactory;
    
    private List<String> m_baseClassBeanIds = Collections.EMPTY_LIST;
    
    private Properties m_hibernateProperties;

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setHibernateProperties(Properties props) {
        m_hibernateProperties = props;
    }

    public Properties getHibernateProperties() {
        return m_hibernateProperties;
    }

    @Override
    public SessionFactory getObject() {
        return m_sessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() {

        try {
            // Wrap datasource for Spring transaction awareness
            TransactionAwareDataSourceProxy dsProxy = new TransactionAwareDataSourceProxy(m_dataSource);
            m_hibernateProperties.put("hibernate.connection.datasource", dsProxy);

            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(m_hibernateProperties)
                    .build();

            List<String> baseClassMappings = Arrays.asList(super.getMappingResources());

            MetadataSources sources = new MetadataSources(registry);
            for (String mapping : baseClassMappings ) {
                sources.addResource(mapping);
            }

            Map<String, HibernateConfigurationPlugin> plugins = m_beanFactory.getBeansOfType(HibernateConfigurationPlugin.class);

            for (HibernateConfigurationPlugin plugin : plugins.values()) {

                ClassLoader cl = Thread.currentThread().getContextClassLoader();

                for (String resource : plugin.getMappingResources()) {

                    if (!baseClassMappings.contains(resource)) { 

                        try (InputStream is = new ClassPathResource(resource, cl).getInputStream()) {
                            sources.addInputStream(is);
                        }
                    }
                }
            }

            // Bind dynamic subclasses BEFORE building Metadata
            for (String baseId : m_baseClassBeanIds) {
                bindSubclasses(sources, baseId);
            }

            // Build metadata
            Metadata metadata = sources.getMetadataBuilder().build();

            // Build SessionFactory with Spring-instantiator interceptor if present
            SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();
            
            Interceptor interceptor = m_beanFactory.getBean("indexingInterceptor", Interceptor.class);
                
            sfb.applyInterceptor(interceptor);

            m_sessionFactory = sfb.build();

        } catch (Exception ex) {
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
    protected void bindSubclasses(MetadataSources sources, String baseClassBeanId) {
        Class<?> baseClass = m_beanFactory.getType(baseClassBeanId);
        String[] beanNames = m_beanFactory.getBeanNamesForType(baseClass);
        for (String beanId : beanNames) {
            Class<?> subClass = m_beanFactory.getType(beanId);
            if (subClass == baseClass) continue;

            String mappingXml = xmlMapping(baseClass, subClass, beanId);
            try (InputStream is = new ByteArrayInputStream(mappingXml.getBytes(StandardCharsets.UTF_8))) {
                sources.addInputStream(is); // works in Hibernate 7
            } catch (Exception e) {
                throw new HibernateException("Failed to add subclass mapping for bean: " + beanId, e);
            }
        }
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


    @Override
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

}