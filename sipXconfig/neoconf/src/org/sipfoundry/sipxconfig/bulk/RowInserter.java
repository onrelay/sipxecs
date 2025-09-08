/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.bulk;

import java.io.Serializable;

import org.apache.commons.collections4.Closure;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.job.JobContext;
import org.springframework.transaction.annotation.Transactional;

public abstract class RowInserter<T> extends SipxHibernateDaoSupport<T> implements Closure {
    private static final Log LOG = LogFactory.getLog(RowInserter.class);

    public enum RowStatus {
        FAILURE, SUCCESS, WARNING_PIN_RESET, WARNING_ALIAS_COLLISION;
    }

    private JobContext m_jobContext;

    protected Log getLog() {
        return LOG;
    }

    public void setJobContext(JobContext jobContext) {
        m_jobContext = jobContext;
    }

    /**
     * Entry point for row execution.
     * Delegates to a transactional method to handle persistence.
     */
    public final void execute(Object input) {
        T row = (T) input;
        String jobDescription = dataToString(row);
        final Serializable jobId = m_jobContext.schedule("Import data: " + jobDescription);

        try {
            executeInTransaction(jobId, row);
        } catch (UserException e) {
            // ignore user exceptions - just log them
            m_jobContext.failure(jobId, null, e);
        } catch (RuntimeException e) {
            // log and rethrow other exceptions
            m_jobContext.failure(jobId, null, e);
            throw e;
        }
    }

    /**
     * Transactional method runs inside a Spring-managed transaction.
     */
    @Transactional
    protected void executeInTransaction(Serializable jobId, T row) {
        m_jobContext.start(jobId);
        RowResult result = checkRowData(row);

        switch (result.getRowStatus()) {
            case SUCCESS:
                insertRow(row);
                m_jobContext.success(jobId);
                afterInsert();
                break;

            case FAILURE:
                String errorMessage = "Invalid data format when importing: " + dataToString(row);
                String wrongData = result.getErrorMessage();
                if (StringUtils.isNotBlank(wrongData)) {
                    errorMessage += " - unsupported value: " + wrongData;
                }
                getLog().warn(errorMessage);
                m_jobContext.failure(jobId, errorMessage, null);
                break;

            case WARNING_PIN_RESET:
                insertRow(row);
                String warnMessage = "Unable to import Voicemail PIN: PIN has been reset.";
                getLog().warn(warnMessage);
                m_jobContext.warning(jobId, warnMessage);
                break;

            case WARNING_ALIAS_COLLISION:
                insertRow(row);
                warnMessage = "Alias collision - skip alias for: " + dataToString(row);
                getLog().warn(warnMessage);
                m_jobContext.warning(jobId, warnMessage);
                afterInsert();
                break;

            default:
                throw new IllegalArgumentException("Need to handle all status cases.");
        }
    }

    protected abstract void insertRow(T input);

    /**
     * Verify data format before insert.
     */
    protected RowResult checkRowData(T input) {
        return input == null
            ? new RowResult(RowStatus.FAILURE)
            : new RowResult(RowStatus.SUCCESS);
    }

    public void beforeInserting(Object... inputs) {
        // no-op
    }

    public void afterInserting() {
        // no-op
    }

    public void afterInsert() {
        super.flush();
        super.clear();
    }

    protected abstract String dataToString(T input);

    public static class RowResult {
        private RowStatus m_rowStatus;
        private String m_errorMessage;

        public RowResult(RowStatus status) {
            m_rowStatus = status;
        }

        public RowResult(RowStatus status, String mess) {
            m_rowStatus = status;
            m_errorMessage = mess;
        }

        public RowStatus getRowStatus() {
            return m_rowStatus;
        }

        public String getErrorMessage() {
            return m_errorMessage;
        }
    }
}