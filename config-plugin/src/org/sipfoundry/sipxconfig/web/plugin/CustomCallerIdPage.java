/*
 * Copyright (c) 2011 Telecats B.V. All rights reserved. Contributed to
 * SIPfoundry and eZuce, Inc. under a Contributor Agreement.
 * This library or application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public
 * License (AGPL) as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library or application is distributed in the hope that it will be
 * useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License (AGPL) for more details.
 *
 * $
 */
package org.sipfoundry.sipxconfig.web.plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import nl.telecats.customcid.CustomCallerAlias;
import nl.telecats.customcid.CustomCallerAliasCsv;
import nl.telecats.customcid.CustomCallerIdManager;
import nl.telecats.customcid.CustomCallerIdSettings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hivemind.Messages;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.request.IUploadFile;
import org.apache.tapestry.valid.ValidationConstraint;
import org.apache.tapestry.web.WebResponse;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

public abstract class CustomCallerIdPage extends SipxBasePage implements PageBeginRenderListener {
    private static final Log LOG = LogFactory.getLog(CustomCallerIdPage.class);

    public abstract IUploadFile getUploadFile();

    @InjectObject("spring:customCallerIdManager")
    public abstract CustomCallerIdManager getCustomCallerManager();

    public abstract void setAlias(CustomCallerAlias alias);

    @InjectObject(value = "service:tapestry.globals.WebResponse")
    public abstract WebResponse getResponse();

    @Persist
    @InitialValue(value = "literal:ddiImport")
    public abstract void setTab(String tab);
    public abstract String getTab();

    public abstract CustomCallerIdSettings getSettings();

    public abstract void setSettings(CustomCallerIdSettings settings);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public void pageBeginRender(PageEvent event) {
        if (getSettings() == null) {
            setSettings(getCustomCallerManager().getSettings());
        }
    }

    public void apply(IRequestCycle cycle) {
        getCustomCallerManager().saveSettings(getSettings());
    }

    public void ddiImport() {
        List<CustomCallerAlias> aliases = loadCsv();
        if (aliases != null) {
            getCustomCallerManager().setDdiRewrites(aliases);
            onSuccess();
        }
    }

    public Collection<CustomCallerAlias> getAliases() {
        if ("ddiImport".equals(getTab())) {
            return getCustomCallerManager().getDdiRewrites();
        }
        return getCustomCallerManager().getDnisRewrites();
    }

    public void dnisImport() {
        List<CustomCallerAlias> aliases = loadCsv();
        if (aliases != null) {
            getCustomCallerManager().setDnisRewrites(aliases);
            onSuccess();
        }
    }

    public void export() {
        PrintWriter writer = null;
        try {
            writer = TapestryUtils.getCsvExportWriter(getResponse(), getTab() + ".csv");
            CustomCallerAliasCsv csv = new CustomCallerAliasCsv();
            csv.write(getAliases(), writer);
        } catch (IOException e) {
            LOG.error("Error during CDR export", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void onSuccess() {
        SipxValidationDelegate validator = (SipxValidationDelegate) TapestryUtils
            .getValidator(this);
        validator.recordSuccess(getMessages().getMessage("msg.success"));
    }

    private List<CustomCallerAlias> loadCsv() {
        if (!TapestryUtils.isValid(this)) {
            return null;
        }
        SipxValidationDelegate validator = (SipxValidationDelegate) TapestryUtils
                .getValidator(this);
        IUploadFile uploadFile = getUploadFile();
        if (uploadFile == null || StringUtils.isBlank(uploadFile.getFilePath())) {
            validator.record(getMessages().getMessage("msg.errorNoFile"), ValidationConstraint.REQUIRED);
            return null;
        }

        Reader in = null;
        try {
            in = new InputStreamReader(uploadFile.getStream());
            CustomCallerAliasCsv importer = new CustomCallerAliasCsv();
            return importer.parse(in);
        } catch (IOException e) {
            LogFactory.getLog(getClass()).error("Cannot import file", e);
            Messages messages = getMessages();
            validator.record(messages.format("msg.error", e.getLocalizedMessage()),
                    ValidationConstraint.CONSISTENCY);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return null;
    }
}
