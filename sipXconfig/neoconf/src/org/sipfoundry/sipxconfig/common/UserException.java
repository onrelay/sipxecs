/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.common;

import java.text.MessageFormat;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Caught in application layer, this informs the user they've done something wrong. Despite being
 * an unchecked exception, this is not meant to be a fatal error and application layer should
 * handle it gracefully.
 *
 * If error message have parameters throw it like this:
 *
 * throw new UserException("This value should be {0} and not {1}", "bongo", 5);
 *
 * If you prefix the message or the parameter with '&' the UI layer will try to localize them
 *
 * throw new UserException("&msg.key", "bongo", 5);
 *
 * The UI layer page will have to have msg.key defined somewhere in the localization bundle.
 *
 * If you just rethrowing some other exception:
 *
 * throw new UserException(e)
 *
 */
@SuppressWarnings("serial")
public class UserException extends RuntimeException {
    private String m_message;

    private String[] m_params = new String[0];

    public UserException() {
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(String message, Throwable cause) {
        super(cause);
        m_message = message;
    }

    public UserException(String message, String... params) {
        m_message = message;
        m_params = params;
    }

    @Override
    public String getMessage() {
        return getLocalizedMessage( m_message, m_params );
    }

    public String getLocalizedMessage( String localizedMessage, String[] localizedParams ) {

        if (localizedMessage != null && getCause() != null ) {

            String result = MessageFormat.format(localizedMessage, getCause().getLocalizedMessage());
            // Comment below out when done with development
            result += "\n" + ExceptionUtils.getStackTrace( getCause() );
            return result;
        }
        if (localizedMessage != null) {
            return MessageFormat.format(localizedMessage, (Object[])localizedParams);
        }
        if (getCause() != null) {
            return getCause().getLocalizedMessage();
        }
        return StringUtils.EMPTY;
    }

    public String[] getRawParams() {
        return m_params;
    }

    public String getRawMessage() {
        return m_message;
    }

    protected void setMessage(String message) {
        m_message = message;
    }
}
