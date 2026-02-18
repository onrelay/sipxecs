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
package org.sipfoundry.sipxconfig.snmp;

import static java.lang.String.format;

public final class ProcessDefinition {

    public static final String JAVA_PROCESS_PREFIX = "java-";
    public static final String RUBY_PROCESS_PREFIX = "ruby-";
    public static final String PERL_PROCESS_PREFIX = "perl-";

    public static final int MAX_SNMP_MATCH_LENGTH = 15;

    private String m_process;
    private String m_service;
    private String m_restartCommand;
    private String m_restartClass;
    private boolean m_hideFromGlobalServiceScript;

    private ProcessDefinition(String process) {
        m_process = process;
    }

    public static ProcessDefinition sipx(String process) {
        ProcessDefinition pd = new ProcessDefinition(process);
        pd.setSipxServiceName(process);
        return pd;
    }

    public static ProcessDefinition sipx(String process, String service) {
        ProcessDefinition pd = new ProcessDefinition(process);
        pd.setSipxServiceName(service);
        return pd;
    }

    public static ProcessDefinition javaSystemctl(String process) {
        ProcessDefinition pd = new ProcessDefinition(JAVA_PROCESS_PREFIX + process);
        pd.setSystemctlServiceName(process);
        return pd;
    }

    public static ProcessDefinition javaSystemctl(String process, boolean hideFromGlobalServiceScript) {
        ProcessDefinition pd = javaSystemctl(process);
        pd.setHideFromGlobalServiceScript(hideFromGlobalServiceScript);
        return pd;
    }

    public static ProcessDefinition javaSystemctl(String process, String service) {
        ProcessDefinition pd = new ProcessDefinition(JAVA_PROCESS_PREFIX + process);
        pd.setSystemctlServiceName(service);
        return pd;
    }

    public static ProcessDefinition sipxJava(String process) {
        ProcessDefinition pd = new ProcessDefinition(JAVA_PROCESS_PREFIX + process);
        pd.setSipxServiceName(process);
        return pd;
    }

    public static ProcessDefinition sipxJava(String process, boolean hideFromGlobalServiceScript) {
        ProcessDefinition pd = sipxJava(process);
        pd.setHideFromGlobalServiceScript(hideFromGlobalServiceScript);
        return pd;
    }

    public static ProcessDefinition sipxJava(String process, String service) {
        ProcessDefinition pd = sipxJava(process);
        pd.setSipxServiceName(service);
        return pd;
    }

    public static ProcessDefinition sipxRuby(String process) {
        ProcessDefinition pd = new ProcessDefinition(RUBY_PROCESS_PREFIX + process);
        pd.setSipxServiceName(process);
        return pd;
    }

    public static ProcessDefinition sipxRuby(String process, boolean hideFromGlobalServiceScript) {
        ProcessDefinition pd = sipxRuby(process);
        pd.setHideFromGlobalServiceScript(hideFromGlobalServiceScript);
        return pd;
    }

    public static ProcessDefinition sipxRuby(String process, String service) {
        ProcessDefinition pd = sipxRuby(process);
        pd.setSipxServiceName(service);
        return pd;
    }

    public static ProcessDefinition sipxPerl(String process) {
        ProcessDefinition pd = new ProcessDefinition(PERL_PROCESS_PREFIX + process);
        pd.setSipxServiceName(process);
        return pd;
    }

    public static ProcessDefinition sipxPerl(String process, boolean hideFromGlobalServiceScript) {
        ProcessDefinition pd = sipxPerl(process);
        pd.setHideFromGlobalServiceScript(hideFromGlobalServiceScript);
        return pd;
    }

    public static ProcessDefinition sipxPerl(String process, String service) {
        ProcessDefinition pd = sipxPerl(process);
        pd.setSipxServiceName(service);
        return pd;
    }

    public static ProcessDefinition java(String process) {
        return systemctl( JAVA_PROCESS_PREFIX + process );
    }

    public static ProcessDefinition java(String process, boolean hideFromGlobalServiceScript) {
        return systemctl( JAVA_PROCESS_PREFIX + process, hideFromGlobalServiceScript );
    }

    public static ProcessDefinition java(String process, String service) {
        return systemctl( JAVA_PROCESS_PREFIX + process, service );
    }

    public static ProcessDefinition systemctl(String process) {
        ProcessDefinition pd = new ProcessDefinition(process);
        pd.setSystemctlServiceName(process);
        return pd;
    }

    public static ProcessDefinition systemctl(String process, boolean hideFromGlobalServiceScript) {
        ProcessDefinition pd = new ProcessDefinition(process);
        pd.setSystemctlServiceName(process);
        pd.setHideFromGlobalServiceScript(true);
        return pd;
    }

    public static ProcessDefinition systemctl(String process, String service) {
        ProcessDefinition pd = new ProcessDefinition(process);
        pd.setSystemctlServiceName(service);
        return pd;
    }

    public String getProcess() {
        return m_process;
    }

    public String getSnmpProcess() {

        if( m_process.length() > MAX_SNMP_MATCH_LENGTH ) {
            return m_process.substring( 0, MAX_SNMP_MATCH_LENGTH );
        }

        return m_process;
    }

    public String getService() {
        return m_service;
    }

    public void setSipxServiceName(String service) {
        m_service = service;
        setServiceStartCommand(format("$(sipx.SIPX_SERVICEDIR)/%s start", service), service);
    }

    public void setSystemctlServiceName(String service) {
        m_service = service;
        setServiceStartCommand(format("systemctl start %s", service), service);
    }

    private void setServiceStartCommand(String restartCommand, String service) {
        setServiceStartCommand(restartCommand);
        m_restartClass = "restart_" + service;
    }

    public void setServiceStartCommand(String restartCommand) {
        m_restartCommand = restartCommand;
    }

    public String getRestartCommand() {
        return m_restartCommand;
    }

    public String getRestartClass() {
        return m_restartClass;
    }

    public void setRestartClass(String restartClass) {
        m_restartClass = restartClass;
    }

    public boolean isHideFromGlobalServiceScript() {
        return m_hideFromGlobalServiceScript;
    }

    public void setHideFromGlobalServiceScript(boolean hideFromGlobalServiceScript) {
        m_hideFromGlobalServiceScript = hideFromGlobalServiceScript;
    }
}
