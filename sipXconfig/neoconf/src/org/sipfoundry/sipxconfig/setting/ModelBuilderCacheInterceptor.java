/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.setting;

import java.io.File;
import java.io.Serializable;

import javax.cache.Cache;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModelBuilderCacheInterceptor implements MethodInterceptor {
    private static final Log LOG = LogFactory.getLog(ModelBuilderCacheInterceptor.class);

    private Cache<String, Serializable> cache;

    public void setCache(Cache<String, Serializable> cache) {
        this.cache = cache;
    }

    /**
     * Main method caches method result if method is configured. For caching method results must
     * be serializable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();

        LOG.trace("Looking for method result in cache");
        String cacheKey = getCacheKey(arguments);
        Serializable value = cache.get(cacheKey);

        if (value == null) {
            LOG.trace("Calling intercepted method");
            Object result = invocation.proceed();

            LOG.debug("Caching result");
            if (!(result instanceof Serializable)) {
                throw new IllegalArgumentException("Cached result must be Serializable");
            }

            value = (Serializable) result;
            cache.put(cacheKey, value);
        }
        return value;
    }

    /**
     * Creates cache key - this implementation creates a key based on the name of the file
     * passed as the first (and only) argument to the method.
     */
    protected String getCacheKey(Object[] arguments) {
        File file = (File) arguments[0];
        return file.getPath();
    }
}