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
package org.sipfoundry.sipxconfig.cert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.springframework.beans.factory.annotation.Required;

public class CertificateConfig implements ConfigProvider {
    private static final String OPENFIRE_KEY = "ssl-openfire.key";
    private CertificateManager m_certificateManager;
    private VelocityEngine m_velocityEngine;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(CertificateManager.FEATURE)) {
            return;
        }

        boolean chainCertificate = false;
        boolean caCertificate = false;
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            
            CertificateSettings settings = m_certificateManager.getSettings();

            String domain = Domain.getDomain().getName();

            File dir = manager.getLocationDataDirectory(location);

            boolean useLetsEncrypt = settings.getUseLetsEncrypt();

            File authDir = new File(dir, "authorities");
            //remove old certs
            FileUtils.deleteQuietly(authDir);
            authDir.mkdir();
            JavaKeyStore store = new JavaKeyStore( new FileInputStream( CertificateManager.JAVA_CACERTS ) );
            for (String authority : m_certificateManager.getAuthorities()) {
                String authCert = m_certificateManager.getAuthorityCertificate(authority);
                FileUtils.writeStringToFile(new File(authDir, authority + ".crt"), authCert);
                store.addAuthority(authority, authCert);
            }
            OutputStream authoritiesStore = null;
            try {
                store.storeIfDifferent(new File(dir, "authorities.jks"));
            } finally {
                IOUtils.closeQuietly(authoritiesStore);
            }

            // Rebuild internal SIP extension certificate
            String sipCert = m_certificateManager.getCommunicationsCertificate();

            FileUtils.writeStringToFile(new File(dir, "ssl.crt"), sipCert);

            String sipKey = m_certificateManager.getCommunicationsPrivateKey();

            FileUtils.writeStringToFile(new File(dir, "ssl.key"), sipKey);

            JavaKeyStore sslSip = new JavaKeyStore();

            sslSip.addKey(domain, sipCert, sipKey);

            sslSip.storeIfDifferent(new File(dir, "ssl.keystore"));

            if( useLetsEncrypt )  {
                
                // These are updated by the set_le_cert script
                File chainCertificateFile = new File(dir, "server-chain.crt");
                chainCertificate = chainCertificateFile.exists() && chainCertificateFile.length() > 0;

                File caCertificateFile = new File(dir, "ca-bundle.crt");
                caCertificate = caCertificateFile.exists() && caCertificateFile.length() > 0;
            }
            else {

                // Rebuild public certificate if letsencrypt not enabled (also used by SBC)

                String webCert = m_certificateManager.getWebCertificate();

                FileUtils.writeStringToFile(new File(dir, "ssl-web.crt"), webCert);

                String webKey = m_certificateManager.getWebPrivateKey();

                File sslWebKey = new File(dir, "ssl-web.key");

                FileUtils.writeStringToFile(sslWebKey, webKey);

                String chainCert = m_certificateManager.getChainCertificate();
                if (chainCert != null) {
                    FileUtils.writeStringToFile(new File(dir, "server-chain.crt"), chainCert);
                    chainCertificate = true;
                }

                String caCert = m_certificateManager.getCACertificate();
                if (caCert != null) {
                    FileUtils.writeStringToFile(new File(dir, "ca-bundle.crt"), caCert);
                    caCertificate = true;
                }

                JavaKeyStore sslWeb = new JavaKeyStore();

                sslWeb.addKey(domain, webCert, webKey);

                sslWeb.storeIfDifferent(new File(dir, "ssl-web.keystore"));

                // Openfire certs

                StringBuffer openfireCert = new StringBuffer();

                openfireCert.append(webCert);

                if (chainCertificate) {
                    openfireCert.append(chainCert);
                }

                if (caCertificate) {
                    openfireCert.append(caCert);
                }

                String openfireSslKey = CertificateUtils.convertSslKeyToRSA(sslWebKey);

                if (openfireSslKey != null) {
                    FileUtils.writeStringToFile(new File(dir, OPENFIRE_KEY), openfireSslKey);
                } else {
                    FileUtils.writeStringToFile(new File(dir, OPENFIRE_KEY), webKey);
                }

                FileUtils.writeStringToFile(new File(dir, "ssl-openfire.crt"), openfireCert.toString());

                //store the full chain for openfire certificate

                JavaKeyStore sslOpenfire = new JavaKeyStore();

                sslOpenfire.addKeys(domain, openfireCert.toString(), new String(openfireSslKey));

                sslOpenfire.storeIfDifferent(new File(dir, "ssl-openfire.keystore"));

            } // useLetsEncrypt

            Writer sslConfWriter = new FileWriter(new File(dir, "ssl.conf"));
            try {
                write(sslConfWriter, chainCertificate, caCertificate);
            } finally {
                IOUtils.closeQuietly(sslConfWriter);
            }

            if (location.isPrimary()) {

                Writer letsEncryptWriter = new FileWriter(new File(dir, "letsencrypt.cfdat"));

                try {
                    CfengineModuleConfiguration config = new CfengineModuleConfiguration(letsEncryptWriter);

                    config.writeClass("letsencrypt", settings.getUseLetsEncrypt());
                    config.write("letsencrypt_certbot_params", settings.getCertbotParams());
                    config.write("letsencrypt_email", settings.getLetsEncryptEmail());
                    config.write("letsencrypt_key_size", settings.getLetsEncryptKeySize());
                } finally {
                    IOUtils.closeQuietly(letsEncryptWriter);
                }
            }

        }
    }

    public void write(Writer writer, boolean chainCertificate, boolean caCertificate) throws IOException {
        VelocityContext context = new VelocityContext();
        if (chainCertificate) {
            context.put("chainCertificate", true);
        }
        if (caCertificate) {
            context.put("caCertificate", true);
        }
        try {
            m_velocityEngine.mergeTemplate("apache/ssl.conf.vm", context, writer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Required
    public void setCertificateManager(CertificateManager certificateManager) {
        m_certificateManager = certificateManager;
    }

    @Required
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }
}
