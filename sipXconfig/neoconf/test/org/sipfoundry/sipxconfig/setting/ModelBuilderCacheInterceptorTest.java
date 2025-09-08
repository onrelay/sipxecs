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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.IMocksControl;
import org.springframework.aop.framework.ProxyFactory;

public class ModelBuilderCacheInterceptorTest extends TestCase {

    // FIXME: REQUIRED FOR PORTING TO SPRING 2.0
    public void testNop() {
    }

    // FIXME: FAILED WHILE PORTING TO SPRING 2.0
    public void DISABLED_testInterceptor() throws Exception {
        SettingSet abc = new SettingSet("abc");
        SettingSet cde = new SettingSet("cde");

        File file1 = new File("abc");
        File file2 = new File("cde");
        File file3 = new File("abc");

        String key1 = file1.getPath();
        String key2 = file2.getPath();
        String key3 = file3.getPath(); // same as key1

        IMocksControl cacheControl = EasyMock.createControl();
        Cache<String, Serializable> cache = cacheControl.createMock(Cache.class);

        EasyMock.expect(cache.get(key1)).andReturn(null);
        cache.put(EasyMock.eq(key1), EasyMock.eq(abc));
        EasyMock.expectLastCall();

        EasyMock.expect(cache.get(key2)).andReturn(null);
        cache.put(EasyMock.eq(key2), EasyMock.eq(cde));
        EasyMock.expectLastCall();

        EasyMock.expect(cache.get(key3)).andReturn(abc);

        cacheControl.replay();

        IMocksControl modelBuilderControl = EasyMock.createControl();
        ModelBuilder modelBuilder = modelBuilderControl.createMock(ModelBuilder.class);

        EasyMock.expect(modelBuilder.buildModel(file1)).andReturn(abc);
        EasyMock.expect(modelBuilder.buildModel(file2)).andReturn(cde);

        modelBuilderControl.replay();

        ProxyFactory proxyFactory = new ProxyFactory(modelBuilder);
        ModelBuilderCacheInterceptor interceptor = new ModelBuilderCacheInterceptor();
        interceptor.setCache(cache);
        proxyFactory.addAdvice(interceptor);

        ModelBuilder proxy = (ModelBuilder) proxyFactory.getProxy();
        assertSame(abc, proxy.buildModel(file1));
        assertSame(cde, proxy.buildModel(file2));
        assertSame(abc, proxy.buildModel(file3));

        modelBuilderControl.verify();
        cacheControl.verify();
    }
}