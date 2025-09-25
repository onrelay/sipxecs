/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.openfire.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jivesoftware.database.ConnectionProvider;
import org.jivesoftware.openfire.container.Plugin;

/**
 * Wraps access to MongoDB-enabled storage.
 * 
 * This is no longer tied into Openfire's internal ConnectionManagerWrapper,
 * which was removed after Openfire 4.x. Instead, it serves as a helper for
 * plugins that want to use a ConnectionProvider backed by Mongo.
 */
public class MongoConnMgrWrapper {

    private static boolean profilingEnabled;
    private static ConnectionProvider provider;
    private static final Object PROVIDER_LOCK = new Object();

    public boolean checkPluginSchema(Plugin plugin) {
        return true; // Nothing to check
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return new MongoMetaData();
    }

    public String getTestQuery(String driver) {
        return "";
    }

    public int getTransactionIsolation() {
        return Connection.TRANSACTION_NONE;
    }

    public boolean isEmbeddedDB() {
        return false;
    }

    public boolean isProfilingEnabled() {
        return profilingEnabled;
    }

    public boolean isSetupMode() {
        return provider == null;
    }

    public void setConnectionProvider(ConnectionProvider provider) {
        synchronized (PROVIDER_LOCK) {
            MongoConnMgrWrapper.provider = provider;
        }
    }

    public ConnectionProvider getConnectionProvider() {
        return provider;
    }

    public void setProfilingEnabled(boolean enabled) {
        synchronized (PROVIDER_LOCK) {
            profilingEnabled = enabled;
        }
    }

    public void shutdown() {
        synchronized (PROVIDER_LOCK) {
            if (provider != null) {
                provider.destroy();
                provider = null;
            }
        }
    }
}