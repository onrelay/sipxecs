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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost; 

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.address.AddressType.Protocol;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.search.SearchableBean;
import org.sipfoundry.sipxconfig.search.SearchableService;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elastic search implementation for SearchableService
 */
public class ElasticsearchServiceImpl implements SearchableService, FeatureProvider, ProcessProvider, FirewallProvider, AddressProvider {

    public static final String ELASTICSEARCH = "elasticsearch";
    public static final LocationFeature FEATURE = new LocationFeature(ELASTICSEARCH);
    public static final AddressType ES_UDP = new AddressType("esUdp", Protocol.udp);
    public static final AddressType ES_TCP = new AddressType("esTcp", Protocol.tcp);
    private static final Collection<AddressType> ADDRESS_TYPES = Arrays.asList(ES_UDP, ES_TCP);

    private static final Log LOG = LogFactory.getLog(ElasticsearchServiceImpl.class);
    private static final String FILTERING_ERROR_MESSAGE = "Filtering is supported only by QueryBuilder objects.";
    private static final String NO_NODE_AVAILABLE_ERROR_MESSAGE = "No available nodes in ElasticSearch.";
    private static final String ELASTICSEARCH_REGEXP = ".*\\java -Xms256m -Xmx1g -Djava.awt.headless=true\\s.*";

    private ElasticsearchClient m_client;
    private String m_hostName;
    private int m_port;
    private Gson m_gson;
    private LocationsManager m_locationsManager;

    public void setHostName(String hostName) {
        m_hostName = hostName;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public void setGson(Gson gson) {
        m_gson = gson;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public void setClient( ElasticsearchClient client ) {
        m_client = client;
    }

    private ElasticsearchClient getClient() {
        if (m_client == null) {
            try {
                String fqdn = m_locationsManager.getPrimaryLocation().getFqdn();
                // Use org.apache.http.HttpHost (from httpclient 4.x)
                RestClient restClient = RestClient.builder(new HttpHost(fqdn, m_port, "http")).build();
                m_client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
            } catch (Exception e) {
                LOG.error("Cannot create elasticsearch client, probably elasticsearch service is not up yet.", e);
            }
        }
        return m_client;
    }

    @Override
    public void storeDoc(String index, SearchableBean source) {
        try {
            getClient().index(i -> i
                .index(index)
                .id(source.getId())
                .document(source)
            );
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void storeBulkDocs(String index, List<SearchableBean> source) {
        try {
            List<BulkOperation> ops = source.stream()
                .map(bean -> BulkOperation.of(b -> b
                    .index(idx -> idx
                        .index(index)
                        .id(bean.getId())
                        .document(bean)
                    )
                ))
                .collect(Collectors.toList());

            getClient().bulk(b -> b.index(index).operations(ops));
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public <T extends SearchableBean> List<T> searchDocs(String indexName, Object filter,
            int start, int size, Class<T> clazz, String orderBy, boolean orderAscending) {
        if (!checkIndexExists(indexName)) {
            return new ArrayList<T>();
        }
        try {
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                .index(indexName)
                .from(start)
                .size(size);

            if (orderBy != null) {
                searchBuilder.sort(s -> s.field(f -> f.field(orderBy).order(orderAscending ? SortOrder.Asc : SortOrder.Desc)));
            }
            // Filtering: You must build the query using the new Query DSL
            // Example: searchBuilder.query(q -> q.matchAll(m -> m));
            // If you have a QueryBuilder, you'll need to translate it to the new API

            // For now, only support match_all if filter is null
            if (filter == null) {
                searchBuilder.query(q -> q.matchAll(m -> m));
            } else {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }

            SearchResponse<T> response = getClient().search(searchBuilder.build(), clazz);
            return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return new ArrayList<T>();
        }
    }

    @Override
    public <T extends SearchableBean> T searchDocById(String indexName, String id, Class<T> clazz) {
        if (!checkIndexExists(indexName)) {
            return null;
        }
        try {
            GetResponse<T> response = getClient().get(g -> g.index(indexName).id(id), clazz);
            return response.found() ? response.source() : null;
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return null;
        }
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!ADDRESS_TYPES.contains(type)) {
            return null;
        }
        Collection<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        Collection<Address> addresses = new ArrayList<>(locations.size());

        for (Location location : locations) {
            Address address = null;
            if (type.equals(ES_UDP)) {
                address = new Address(ES_UDP, location.getAddress(), 9300);
            } else if (type.equals(ES_TCP)) {
                address = new Address(ES_TCP, location.getAddress(), 9300);
            }
            addresses.add(address);
        }

        return addresses;
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        return DefaultFirewallRule.rules(ADDRESS_TYPES, FirewallRule.SystemId.CLUSTER);
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(ProcessDefinition.sysvByRegex(
                ELASTICSEARCH, ELASTICSEARCH_REGEXP, true)) : null);
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.primaryLocationOnly(FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public int countDocs(String indexName, Object filter) {
        if (!checkIndexExists(indexName)) {
            return 0;
        }
        try {
            // Filtering: You must build the query using the new Query DSL
            // For now, only support match_all if filter is null
            CountRequest.Builder countBuilder = new CountRequest.Builder().index(indexName);
            if (filter == null) {
                countBuilder.query(q -> q.matchAll(m -> m));
            } else {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }
            CountResponse response = getClient().count(countBuilder.build());
            return (int) response.count();
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return 0;
        }
    }

    private boolean checkIndexExists(String indexName) {
        try {
            BooleanResponse exists = getClient().indices().exists(e -> e.index(indexName));
            return exists.value();
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
            return false;
        }
    }

    @Override
    public void deleteDocs(String indexName, Object filter) {
        if (!checkIndexExists(indexName)) {
            return;
        }
        try {
            // Filtering: You must build the query using the new Query DSL
            // For now, only support match_all if filter is null
            DeleteByQueryRequest.Builder deleteBuilder = new DeleteByQueryRequest.Builder().index(indexName);
            if (filter == null) {
                deleteBuilder.query(q -> q.matchAll(m -> m));
            } else {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }
            getClient().deleteByQuery(deleteBuilder.build());
        } catch (IOException e) {
            LOG.error(NO_NODE_AVAILABLE_ERROR_MESSAGE, e);
        }
    }
}
