package org.sipfoundry.commons.mongo;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoException;
import com.mongodb.ConnectionString;

public class MongoFactory {
    private static final Logger log = Logger.getLogger(MongoFactory.class);

    private static final byte[] FILE_LOCK = new byte[0];

    private static String connectionURL;

    public static final MongoClient fromConnectionFile() throws MongoException {
        if (connectionURL == null) {
            synchronized (FILE_LOCK) {
                String configurationPathDef = "/etc/sipxpbx";
                if (MongoUtil.isFedora()) {
                    configurationPathDef = "/usr/local/sipx/" + configurationPathDef;
                }
                String configurationPath = System.getProperty("conf.dir", configurationPathDef);
                InputStream is = null;
                String config = null;

                try {
                    if (isBlank(configurationPath)) {
                        File openfireTmp = new File("/tmp/sipx.properties");
                        if (openfireTmp.exists()) {
                            is = new FileInputStream(openfireTmp);
                            System.getProperties().load(is);
                            configurationPath = System.getProperty("conf.dir", "/etc/sipxpbx");
                        }
                    }
                    config = configurationPath + "/mongo-client.ini";
                    connectionURL = MongoFactory.readConfig(config);
                } catch (IOException e) {
                    log.error("Error getting connection URL from [" + config + "]: " + e.getMessage());
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }

        return fromConnectionString(connectionURL);
    }

    public static final MongoClient fromConnectionString(String connectionUrl) throws MongoException {
        // Use the MongoDB Java 4.x driver to connect
        ConnectionString connString = new ConnectionString(connectionUrl);
        return MongoClients.create(connString);
    }

    public static String readConfig(String configFile) {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(configFile);
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return p.getProperty("connectionUrl");
    }

    public static String getConnectionURL() {
        return connectionURL;
    }
}