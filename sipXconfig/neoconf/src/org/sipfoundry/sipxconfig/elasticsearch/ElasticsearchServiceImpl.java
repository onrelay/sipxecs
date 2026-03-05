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
    public static final AddressType ES_TCP = new AddressType("esTcp", Protocol.tcp);
    private static final Collection<AddressType> ADDRESS_TYPES = Arrays.asList(ES_TCP);
    private static final Log LOG = LogFactory.getLog(ElasticsearchServiceImpl.class);
    private static final String FILTERING_ERROR_MESSAGE = "Filtering is supported only by QueryBuilder objects.";
    private static final String NO_CONNECTION_AVAILABLE_ERROR_MESSAGE = "Not able to reach Elasticsearch at: ";

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
                RestClient restClient = RestClient.builder(new HttpHost(fqdn, m_port, "http")).build();
                m_client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
            } catch (Exception e) {
                LOG.error("Cannot create elasticsearch client, probably elasticsearch service is not up yet: " + e.getMessage());
            }
        }
        return m_client;
    }

    @Override
    public void storeDoc(String index, SearchableBean source) {
        try {
            IndexRequest<SearchableBean> request = IndexRequest.of(i -> i
                .index(index)
                .id(source.getId())
                .document(source)
            );
            getClient().index(request);
        } catch (IOException e) {
            LOG.error("Error indexing document into index " + index, e);
        }
    }

    @Override
    public void storeBulkDocs(String index, List<SearchableBean> sourceList) {
        if (sourceList.isEmpty()) return;

        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (SearchableBean bean : sourceList) {
                bulkBuilder.operations(op -> op
                    .index(idx -> idx
                        .index(index)
                        .id(bean.getId())
                        .document(bean)
                    )
                );
            }

            BulkResponse bulkResponse = getClient().bulk(bulkBuilder.build());
            if (bulkResponse.errors()) {
                String errors = bulkResponse.items().stream()
                    .filter(item -> item.error() != null)
                    .map(BulkResponseItem::error)
                    .map(err -> err.reason())
                    .collect(Collectors.joining(", "));
                LOG.error("Bulk indexing errors: " + errors);
            }
        } catch (IOException e) {
            LOG.error("Error executing bulk request on index " + index, e);
        }
    }

    @Override
    public <T extends SearchableBean> List<T> searchDocs(
            String indexName, Object filter, int start, int size, Class<T> clazz,
            String orderBy, boolean orderAscending) {

        if (!checkIndexExists(indexName)) return Collections.emptyList();

        try {
            SearchRequest.Builder searchReq = new SearchRequest.Builder()
                .index(indexName)
                .from(start)
                .size(size);

            if (orderBy != null) {
                searchReq.sort(s -> s
                    .field(f -> f
                        .field(orderBy)
                        .order(orderAscending ? SortOrder.Asc : SortOrder.Desc)
                    )
                );
            }

            if (filter != null) {
                if (!(filter instanceof co.elastic.clients.elasticsearch._types.query_dsl.Query)) {
                    LOG.error(FILTERING_ERROR_MESSAGE);
                } else {
                    searchReq.query((co.elastic.clients.elasticsearch._types.query_dsl.Query) filter);
                }
            }

            SearchResponse<T> response = getClient().search(searchReq.build(), clazz);
            return response.hits().hits().stream()
                    .map(hit -> {
                        T obj = hit.source();
                        obj.setId(hit.id());
                        return obj;
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            LOG.error(NO_CONNECTION_AVAILABLE_ERROR_MESSAGE + m_hostName + ":" + m_port, e);
            return Collections.emptyList();
        }
    }

    @Override
    public <T extends SearchableBean> T searchDocById(String indexName, String id, Class<T> clazz) {
        if (!checkIndexExists(indexName)) return null;

        try {
            GetResponse<T> response = getClient().get(g -> g
                .index(indexName)
                .id(id), clazz);
            if (response.found()) {
                T obj = response.source();
                obj.setId(response.id());
                return obj;
            }
            return null;
        } catch (IOException e) {
            LOG.error(NO_CONNECTION_AVAILABLE_ERROR_MESSAGE + m_hostName + ":" + m_port, e);
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
            Address address = new Address(ES_TCP, location.getAddress(), m_port);
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
        return (enabled ? Collections.singleton(ProcessDefinition.javaSystemctl(ELASTICSEARCH, true)) : null);
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
        if (!checkIndexExists(indexName)) return 0;

        try {
            CountRequest.Builder countReq = new CountRequest.Builder().index(indexName);
            if (filter instanceof co.elastic.clients.elasticsearch._types.query_dsl.Query) {
                countReq.query((co.elastic.clients.elasticsearch._types.query_dsl.Query) filter);
            } else if (filter != null) {
                LOG.error(FILTERING_ERROR_MESSAGE);
            }

            CountResponse response = getClient().count(countReq.build());
            return (int) response.count();
        } catch (IOException e) {
            LOG.error("Error counting documents on index " + indexName, e);
            return 0;
        }
    }

    private boolean checkIndexExists(String indexName) {
        try {
            BooleanResponse response = getClient().indices()
                .exists(ExistsRequest.of(e -> e.index(indexName)));
            return response.value();
        } catch (IOException e) {
            LOG.error(NO_CONNECTION_AVAILABLE_ERROR_MESSAGE + m_hostName + ":" + m_port, e);
            return false;
        }
    }

    @Override
    public void deleteDocs(String indexName, Object filter) {
        if (!checkIndexExists(indexName)) return;

        if (!(filter instanceof co.elastic.clients.elasticsearch._types.query_dsl.Query)) {
            LOG.error(FILTERING_ERROR_MESSAGE);
            return;
        }

        try {
            DeleteByQueryRequest req = DeleteByQueryRequest.of(d -> d
                .index(indexName)
                .query((co.elastic.clients.elasticsearch._types.query_dsl.Query) filter)
            );
            getClient().deleteByQuery(req);
        } catch (IOException e) {
            LOG.error("Error deleting documents from index " + indexName, e);
        }
    }
}
