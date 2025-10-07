/**
 *
 *
 * Copyright (c) 2015 eZuce Corp. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */

package org.sipfoundry.sipxconfig.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;

import com.google.gson.GsonBuilder;
import org.sipfoundry.sipxconfig.search.SearchableBean;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChange;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeValue;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditException;

public class ElasticsearchServiceTest extends TestCase {

    private static final String INDEX = "testindex";
    private static final String UNIQUE_DETAILS = "52653";

    private ElasticsearchClient client;
    private ElasticsearchServiceImpl elasticsearchService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (client != null) {
            return;
        }

        // Connect to a running ES cluster (default localhost:9200 for tests)
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);

        elasticsearchService = new ElasticsearchServiceImpl();
        elasticsearchService.setClient(client);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        elasticsearchService.setGson(gsonBuilder.create());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            DeleteIndexResponse deleteResponse =
                client.indices().delete(d -> d.index(INDEX));
        } catch (Exception e) {
            // ignore, index may not exist
        }
    }

    public void testStoreElasticsearchBean() {
        try {
            waitForRefresh();
            SearchableBean testConfigChange = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            elasticsearchService.storeDoc(INDEX, testConfigChange);
            Thread.sleep(2000L);
            int docCount = elasticsearchService.countDocs(INDEX, null);
            assertEquals(1, docCount);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testOperations() {
        try {
            waitForRefresh();
            SearchableBean testConfigChange1 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            SearchableBean testConfigChange2 = buildElasticsearchBean("Modified", "User",
                    UNIQUE_DETAILS, "200", "192.168.1.1", null, null, null);
            SearchableBean testConfigChange3 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);

            List<SearchableBean> docs = new ArrayList<>();
            docs.add(testConfigChange1);
            docs.add(testConfigChange2);
            docs.add(testConfigChange3);
            elasticsearchService.storeBulkDocs(INDEX, docs);

            List<ConfigChange> searchResponse = elasticsearchService.searchDocs(
                    INDEX, null, 0, 10, ConfigChange.class, ConfigChange.ACTION, true);

            boolean itemFound = false;
            for (ConfigChange configChange : searchResponse) {
                if (configChange.getDetails().equals(UNIQUE_DETAILS)) {
                    ConfigChange configChange2 = searchResponse.get(2);
                    assertEquals(configChange.getAction(), configChange2.getAction());
                    assertEquals(configChange.getConfigChangeType(), configChange2.getConfigChangeType());
                    assertEquals(configChange.getUserName(), configChange2.getUserName());
                    assertEquals(configChange.getDetails(), configChange2.getDetails());
                    assertEquals(configChange.getDateTime(), configChange2.getDateTime());
                    assertEquals(configChange.getIpAddress(), configChange2.getIpAddress());
                    itemFound = true;
                }
            }
            assertTrue(itemFound);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testStoreBulkEmptyElasticsearchBeans() {
        try {
            waitForRefresh();
            List<SearchableBean> docs = new ArrayList<>();
            elasticsearchService.storeBulkDocs(INDEX, docs);
            assertTrue(true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testCountElasticsearchBeans() {
        try {
            waitForRefresh();

            SearchableBean testConfigChange1 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);
            SearchableBean testConfigChange2 = buildElasticsearchBean("Modified", "User",
                    UNIQUE_DETAILS, "200", "192.168.1.1", null, null, null);
            SearchableBean testConfigChange3 = buildElasticsearchBean("Added", "Phone",
                    "52658", "200", "192.168.1.1", null, null, null);

            List<SearchableBean> docs = new ArrayList<>();
            docs.add(testConfigChange1);
            docs.add(testConfigChange2);
            docs.add(testConfigChange3);
            elasticsearchService.storeBulkDocs(INDEX, docs);

            int docCount = elasticsearchService.countDocs(INDEX, null);
            assertTrue(docCount > 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void waitForRefresh() throws IOException {
        RefreshResponse refreshResponse =
            client.indices().refresh(r -> r.index(INDEX));
    }

    protected SearchableBean buildElasticsearchBean(String action, String type,
            String details, String userName, String ipAddress,
            String propertyName, String before, String after)
            throws SystemAuditException {
        ConfigChange configChange = new ConfigChange();
        configChange.setAction(action);
        configChange.setConfigChangeType(type);
        configChange.setDetails(details);
        configChange.setUserName(userName);
        configChange.setIpAddress(ipAddress);
        if (propertyName != null) {
            ConfigChangeValue configChangeValue = new ConfigChangeValue();
            configChangeValue.setPropertyName(propertyName);
            configChangeValue.setValueBefore(before);
            configChangeValue.setValueAfter(after);
            configChange.addValue(configChangeValue);
        }
        return configChange;
    }
}