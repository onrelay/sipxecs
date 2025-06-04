/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
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
package org.sipfoundry.sipxconfig.commserver.imdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MongoTestCaseHelper {
    public static final String DOMAIN = "mydomain.org";
    public static final String ID = "_id";
    public static final String EXCEPTION = "fields and values do not match (they have different lengths)";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MongoTestCaseHelper() {}

    public static void assertObjectPresent(MongoCollection<Document> collection, Document ref) {
        TestCase.assertTrue(collection.find(ref).into(new ArrayList<>()).size() > 0);
    }

    public static void assertObjectNotPresent(MongoCollection<Document> collection, Document ref) {
        TestCase.assertTrue(collection.find(ref).into(new ArrayList<>()).isEmpty());
    }

    public static void assertObjectWithIdPresent(MongoCollection<Document> collection, String id) {
        Document ref = new Document(ID, id);
        TestCase.assertEquals(1, collection.countDocuments(ref));
    }

    public static void assertObjectWithIdNotPresent(MongoCollection<Document> collection, Object id) {
        Document ref = new Document(ID, id);
        TestCase.assertEquals(0, collection.countDocuments(ref));
    }

    public static void assertCollectionItemsCount(MongoCollection<Document> collection, Document ref, int count) {
        TestCase.assertEquals(count, collection.find(ref).into(new ArrayList<>()).size());
    }

    public static void assertCollectionCount(MongoCollection<Document> collection, int count) {
        TestCase.assertEquals(count, collection.countDocuments());
    }

    public static void assertObjectListFieldCount(MongoCollection<Document> collection, String id, String listField, int count) {
        Document ref = new Document(ID, id);
        Document obj = collection.find(ref).first();
        TestCase.assertNotNull(obj);
        TestCase.assertTrue(obj.containsKey(listField));
        TestCase.assertEquals(count, ((List<?>) obj.get(listField)).size());
    }

    public static void assertObjectWithFieldsValuesPresent(MongoCollection<Document> collection, String[] fields, Object[] values) {
        if (fields.length != values.length) {
            throw new RuntimeException(EXCEPTION);
        }
        Document ref = new Document();
        for (int i = 0; i < fields.length; i++) {
            ref.put(fields[i], values[i]);
        }
        TestCase.assertEquals(1, collection.countDocuments(ref));
    }

    public static void assertObjectWithFieldsValuesNotPresent(MongoCollection<Document> collection, String[] fields, Object[] values) {
        if (fields.length != values.length) {
            throw new RuntimeException(EXCEPTION);
        }
        Document ref = new Document();
        for (int i = 0; i < fields.length; i++) {
            ref.put(fields[i], values[i]);
        }
        TestCase.assertEquals(0, collection.countDocuments(ref));
    }

    public static void assertObjectWithIdFieldValuePresent(MongoCollection<Document> collection, Object id, String field, Object value) {
        Document ref = new Document(ID, id).append(field, value);
        TestCase.assertEquals(1, collection.countDocuments(ref));
    }

    public static void assertObjectWithIdFieldValueNotPresent(MongoCollection<Document> collection, Object id, String field, Object value) {
        Document ref = new Document(ID, id).append(field, value);
        TestCase.assertEquals(0, collection.countDocuments(ref));
    }

    public static void insert(MongoCollection<Document> collection, Document doc) {
        collection.insertOne(doc);
    }

    public static void insertJson(MongoCollection<Document> collection, String... jsons) {
        for (String json : jsons) {
            try {
                Map<String, Object> map = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
                collection.insertOne(new Document(map));
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse JSON: " + json, e);
            }
        }
    }

    public static void assertJson(FindIterable<Document> actual, String expectedJson) {
        List<Document> docs = actual.into(new ArrayList<>());
        try {
            String actualJson = MAPPER.writeValueAsString(docs);
            TestCase.assertEquals(expectedJson, actualJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}