/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package nl.telecats.customcid;


public class CustomCallerAlias {

    private String m_from;
    private String m_to;

    public CustomCallerAlias() {
    }

    public CustomCallerAlias(String from, String to) {
        m_from = from;
        m_to = to;
    }

    public String getFrom() {
        return m_from;
    }

    public void setFrom(String from) {
        m_from = from;
    }

    public String getTo() {
        return m_to;
    }

    public void setTo(String to) {
        m_to = to;
    }
}
