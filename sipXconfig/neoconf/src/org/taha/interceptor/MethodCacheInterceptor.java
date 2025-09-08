/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.taha.interceptor;

import java.io.Serializable;

import javax.cache.Cache;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * JSR-107 compatible Method Cache Interceptor
 * Originally by Omar Irbouh (irbouh@gmail.com)
 */
public class MethodCacheInterceptor implements MethodInterceptor, InitializingBean {
    private static final Log logger = LogFactory.getLog(MethodCacheInterceptor.class);

    private Cache<String, Serializable> cache;

    /**
     * Set the JCache (javax.cache) cache to be used.
     */
    public void setCache(Cache<String, Serializable> cache) {
        this.cache = cache;
    }

    /**
     * Validates that a cache has been injected.
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(cache, "A cache is required. Use setCache(Cache) to provide one.");
    }

    /**
     * Intercepts method execution and caches the result.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String targetName = invocation.getThis().getClass().getName();
        String methodName = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();

        logger.debug("Looking for method result in cache");
        String cacheKey = getCacheKey(targetName, methodName, arguments);
        Serializable result = cache.get(cacheKey);

        if (result == null) {
            logger.debug("Calling intercepted method");
            Object invocationResult = invocation.proceed();

            if (!(invocationResult instanceof Serializable)) {
                throw new IllegalArgumentException("Cached result must be Serializable: " + invocationResult);
            }

            logger.debug("Caching result");
            result = (Serializable) invocationResult;
            cache.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Builds cache key: targetName.methodName.argument0.argument1...
     */
    private String getCacheKey(String targetName, String methodName, Object[] arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append(targetName).append('.').append(methodName);
        if (arguments != null && arguments.length > 0) {
            for (Object arg : arguments) {
                sb.append('.').append(getCacheKey(arg));
            }
        }
        return sb.toString();
    }

    /**
     * Recursively builds key string from objects or arrays.
     */
    public static String getCacheKey(Object o) {
        if (o == null) {
            return "null";
        }

        if (o instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (Object item : (Object[]) o) {
                sb.append(getCacheKey(item)).append(',');
            }
            sb.append(']');
            return sb.toString();
        }

        return o.toString();
    }
}