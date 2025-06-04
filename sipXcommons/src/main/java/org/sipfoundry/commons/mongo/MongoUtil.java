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
package org.sipfoundry.commons.mongo;

import org.apache.commons.lang3.StringUtils;
import org.bson.BasicBSONObject;
import org.bson.Document;

import com.mongodb.client.MongoDatabase;

/**
 * Utility function when dealing with mongo
 */
public final class MongoUtil {
    
    private MongoUtil() {        
    }

    public static class MongoCommandException extends RuntimeException {
        public MongoCommandException(String msg) {
            super(msg);
        }
    }

    /**
     * Running java script commands. With throw MongoCommandException if
     * command wasn't successful.
     * 
     * Example:
     *  BasicBSONObject ret = MongoUtil.runCommand(m_db, "rs.config()");
     */
    public static Document runCommand(MongoDatabase db, String command) {
        
        try {
            Document commandDoc = new Document(command, 1);  // Convert command string into BSON format
            Document result = db.runCommand(commandDoc);
            return result;
        } catch (MongoCommandException e) {
            String msg = String.format("Cannot run command '%s'. Result: '%s'.", command, e.getMessage());
            throw new MongoCommandException( msg );
        }
    }
    
    public static void checkForError(BasicBSONObject o) {
        int ok = o.getInt("ok");
        if (ok == 0) {
            String what = o.getString("assertion");
            if (StringUtils.isBlank(what)) {
                what = o.getString("errmsg");
                if (StringUtils.isBlank(what)) {
                    what = "undetermined error";
                }                
            }
            throw new MongoCommandException(what);
        }
    }

    /**
     * Get an object from a nest result set of objects
     * 
     * Example:
     *  BasicBSONObject city = getObject(m_db, "country", "state", "city");
     */
    public static BasicBSONObject getObject(BasicBSONObject o, String... keys) {
        BasicBSONObject s = o;
        for (String key : keys) {
            s = (BasicBSONObject) s.get(key);
            if (s == null) {
                // make this a safe call, and just return null if full
                // tree isn't there
                return s;
            }
        }
        return s;
    }

    public static boolean isFedora() {
        if (System.getProperty("os.name").equals("Linux")
                && System.getProperty("os.version").contains("fc")) {
            return true;
        } else {
            return false;
        }
    }
}
