/*
 *  Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 *  Contributors retain copyright to elements licensed under a Contributor Agreement.
 *  Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxbridge;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.SipProvider;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

/**
 * Keeps a mapping of account ID to ItspAccountInfo and a mapping of account ID to sip pbx account
 * info.
 *
 * @author M. Ranganathan
 *
 */
public class AccountManagerImpl implements gov.nist.javax.sip.clientauthutils.AccountManager {

    static final String SIPXECS_LINEID = "sipxecs-lineid";

    private static Logger logger = Logger.getLogger(AccountManagerImpl.class);

    private HashSet<ItspAccountInfo> itspAccounts = new HashSet<ItspAccountInfo>();

    private BridgeConfiguration bridgeConfiguration;

    public AccountManagerImpl() {

    }

    // //////////////////////////////////////////////////////////////////
    // Package local methods.
    // //////////////////////////////////////////////////////////////////
    /**
     * @return the bridgeConfiguration
     */
    public BridgeConfiguration getBridgeConfiguration() {
        return bridgeConfiguration;
    }

    /**
     * Start the failure timout timers for each of the accounts we manage.
     */
    void startAuthenticationFailureTimers() {

        for (ItspAccountInfo itspAccountInfo : this.getItspAccounts()) {
            itspAccountInfo.startFailureCounterScanner();
        }
    }

    /**
     * Get the default outbound ITSP account for outbound calls.
     */
    public ItspAccountInfo getDefaultAccount() {
        return itspAccounts.iterator().next();
    }

    // xx-4785
    boolean checkSipxecsLineid(ArrayList<String> ids, SipURI uri) {

        // label not found, we'll pick based on the proxyDomain only.
        if (uri.getParameter(SIPXECS_LINEID) ==  null) {
            return true;
        }

        // Otherwise, the label value has to match.
        for (String sipxecsLineid : ids) {
            if (uri.getParameter(SIPXECS_LINEID).equals(sipxecsLineid))
                return true;
        }

        return false;
    }

    /**
     * Get the outbound ITSP account for a specific outbund SipURI.
     */
    ItspAccountInfo getAccount(Request request) {

        SipURI sipUri = (SipURI) request.getRequestURI();
        
        if ( logger.isDebugEnabled() ) logger.debug("getAccount: fetching account for " + sipUri);
        
        // Request originated from LAN. If In-DIALOG request use previously discovered ITSP account
        
        if (request.getMethod().equals(Request.INVITE) && request.getHeader(ReplacesHeader.NAME) != null) {
        	
           Dialog replacesDialog =  ((SipStackExt)ProtocolObjects.getSipStack()).getReplacesDialog((ReplacesHeader)request.getHeader(ReplacesHeader.NAME)); 
           
           if ( replacesDialog == null ) {
               logger.error("Could not find replaces dialog - returning null for ITSP account");
               return null;
           }
           
           ItspAccountInfo accountInfo = DialogContext.get(replacesDialog).getItspInfo();
           
           if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: matched via replaces dialog " + accountInfo);
           return accountInfo;           
        }
        
        
        try {

            for (ItspAccountInfo accountInfo : itspAccounts) {
            	
                if (accountInfo.getProxyDomain() != null && sipUri.getHost().endsWith(accountInfo.getProxyDomain())
                		
                        && checkSipxecsLineid(accountInfo.getSipxecsLineIds(), sipUri)) {
                	
                	if ( sipUri.getParameter(SIPXECS_LINEID) != null ) {
                		
                        if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: matched via line ID " + accountInfo);
                		return accountInfo;
                		
                	} else if (accountInfo.getCallerId() == null) {
                        /*
                         * A null override caller ID has been provided. This case occurs when you
                         * override the default P-A-I to blank. (see XX-7159)
                         */
                        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);

                        String userStr = ((SipURI) fromHeader.getAddress().getURI()).getUser();

                        if (accountInfo.getUserName() != null && userStr.equals(accountInfo.getUserName())) {
                        	
                            if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: matched in from header " + accountInfo);
                            return accountInfo;
                        }
                    } else {
                    	
                        String callerId = accountInfo.getCallerId();
                        
                        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);

                        String userStr = ((SipURI) fromHeader.getAddress().getURI()).getUser();
                        
                        String domainStr = ((SipURI) fromHeader.getAddress().getURI()).getHost();
                        
                        if (userStr.equals("anonymous") && domainStr.equals("invalid")) {
                        	
                            PAssertedIdentityHeader pai = (PAssertedIdentityHeader) request
                                    .getHeader(PAssertedIdentityHeader.NAME);
                            
                            if (pai == null) {
                            	
                                logger.warn("Anonymous call without P-Asserted-Identity ");
                                // BUGBUG - this is really a mistake we should reject
                                // the call if the PAI header is missing
                                
                                if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: found after pai == null " + accountInfo);
                                return accountInfo;
                            }
                            userStr = ((SipURI) pai.getAddress().getURI()).getUser();
                        }

                        if (callerId.startsWith(userStr)) {
                            
                            if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: found from user " + accountInfo);

                            return accountInfo;
                        }

                    }
                }
            }

            for (ItspAccountInfo accountInfo : itspAccounts) {
            	
                if (accountInfo.getProxyDomain() != null) {
                	
                    if (sipUri.getHost().endsWith(accountInfo.getProxyDomain())) {
                    	
                        if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: found via proxy domain " + accountInfo);
                        return accountInfo;
                    }
                }
            }

            String userName = ((SipURI) ((FromHeader) request.getHeader(FromHeader.NAME)).getAddress().getURI())
                    .getUser();


            /*
             * If an account is not found return an account record with the domain set to the
             * outbound request domain. The INVITE will be forwarded. If the other side does not
             * like the INVITE it an complain about it. See issue XX-5623
             */
            ItspAccountInfo defaultAccountInfo = null;

            defaultAccountInfo = new ItspAccountInfo();
            defaultAccountInfo.setProxyDomain(sipUri.getHost());
            defaultAccountInfo.setUserName(userName);
            defaultAccountInfo.setOutboundProxyPort(sipUri.getPort());
            defaultAccountInfo.setOutboundTransport(sipUri.getTransportParam() == null ? 
            		Gateway.DEFAULT_ITSP_TRANSPORT : sipUri
                    .getTransportParam());
            defaultAccountInfo.setGlobalAddressingUsed(true);
            defaultAccountInfo.setDummyAccount(true);
            defaultAccountInfo.setRegisterOnInitialization(false);
            this.addItspAccount(defaultAccountInfo);
            
            if ( logger.isDebugEnabled() ) logger.debug("getItspAccount: returning default " + defaultAccountInfo);
            return defaultAccountInfo;
            
        } catch( Exception e ) {
        	
            logger.warn("getItspAccount: Error looking up ITSP account: " + e.getMessage() );
            return null;
        } 
    }

    /**
     * Get a collection of Itsp accounts.
     *
     * @return
     */
    Collection<ItspAccountInfo> getItspAccounts() {

        return itspAccounts;
    }

    /**
     * Get an ITSP account based on the host and port of the inbound request. Look up the ITSP
     * account based on the topmost via header of the inbound request. Should we reject the
     * request if it is not from a known ITSP?
     *
     * @param host
     * @param port
     * @return
     */
    ItspAccountInfo getItspAccount(String host, int port) {
        if ( logger.isDebugEnabled() ) logger.debug("getItspAccount - for " + host + ":" + port);
        if (port == -1)
            port = 5060; // set default.
        try {
            String viaHost = InetAddress.getByName(host).getHostAddress();
            if ( logger.isDebugEnabled() ) logger.debug("viaHost = " + viaHost + " viaPort = " + port);
            for (ItspAccountInfo accountInfo : this.getItspAccounts()) {
                if (accountInfo.isRegisterOnInitialization()) {
                    // Account needs registration.
                    String registrarHost = accountInfo.getOutboundRegistrar();
                    // We assume that the Registrar is the same as the INBOUND proxy
                    // server.
                    if ( logger.isDebugEnabled() ) logger.debug("getItspAccount - registrarHost = " + registrarHost);
                    try {
                        Hop hop = accountInfo.getHopToRegistrar();
                        if (hop != null && viaHost.equals(InetAddress.getByName(hop.getHost()).getHostAddress())) {
                            if ( logger.isDebugEnabled() ) logger.debug("getItspAccount - found registered account " + accountInfo.getProxyDomain());
                            return accountInfo;

                        }
                    } catch (UnknownHostException ex) {
                        logger.error("Cannot resolve host address " + registrarHost);
                    }
                } else {
                    for (Hop hop : accountInfo.getInboundProxies()) {
                        if (hop != null) {
                           if ( logger.isDebugEnabled() ) logger.debug("getItspAccount - Checking " + hop.getHost() + " port " + hop.getPort());
                           try {
                               if (viaHost.equals(InetAddress.getByName(hop.getHost()).getHostAddress())
                                       && hop.getPort() == port) {
                                   if ( logger.isDebugEnabled() ) logger.debug("getItspAccount - found unregistered account : " + accountInfo.getProxyDomain());
                                   return accountInfo;
                               }
                           } catch ( UnknownHostException ex) {
                               logger.error("Cannot resolve host address " + hop.getHost());
                           }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("unexpected error parsing domain", ex);
            return null;
        }
        if ( logger.isDebugEnabled() ) logger.debug("Could not find ITSP account for inbound request");
        return null;
    }
    
    /**
     * Get an ITSP account based on the the via list of the inbound request. Look up the ITSP
     * account based on all the present via header of the inbound request. Should we reject the
     * request if it is not from a known ITSP?
     *
     * @param host
     * @param port
     * @return
     */
	ItspAccountInfo getItspAccount(Iterator<?> vias) {
		
		ItspAccountInfo itspAccountInfo = null;
		
		String host = null;
		
		int port = 0;
		
		if (vias != null) {
			
			ViaHeader nextInboundVia = null;
			
			while (vias.hasNext()) {
				
				nextInboundVia = (ViaHeader) vias.next();
				
				host = nextInboundVia.getReceived() != null ? nextInboundVia.getReceived() : nextInboundVia.getHost();
								
				port = nextInboundVia.getPort() > 0 ? nextInboundVia.getPort() : nextInboundVia.getRPort();
				
				itspAccountInfo = getItspAccount(host, port);
				
				if (itspAccountInfo != null) {
					
					return itspAccountInfo;
				}
			}
		}
		return null;
	}

    // //////////////////////////////////////////////////////////////////
    // Public methods.
    // //////////////////////////////////////////////////////////////////
    /**
     * Add the Bridge config.
     */
    public void setBridgeConfiguration(BridgeConfiguration bridgeConfiguration) {
        this.bridgeConfiguration = bridgeConfiguration;
    }

    /**
     * Add an ITSP account to the account database ( method is accessed by the digester).
     */
    public void addItspAccount(ItspAccountInfo accountInfo) throws SipXbridgeException {
        this.itspAccounts.add(accountInfo);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.nist.javax.sip.clientauthutils.AccountManager#getCredentials(javax.sip.ClientTransaction
     * , java.lang.String)
     */
    public UserCredentials getCredentials(ClientTransaction ctx, String authRealm) {

        SipProvider provider = ((TransactionExt) ctx).getSipProvider();

        /*
         * Challenge from the LAN side.
         */
        if (provider == Gateway.getLanProvider()) {
            if (Gateway.getBridgeConfiguration().getSipxbridgePassword() != null) {
                return new UserCredentials() {
                    public String getPassword() {
                        return Gateway.getBridgeConfiguration().getSipxbridgePassword();
                    }

                    public String getSipDomain() {
                        return Gateway.getSipxProxyDomain();
                    }

                    public String getUserName() {
                        return Gateway.getBridgeConfiguration().getSipxbridgeUserName();
                    }

                };

            } else {
                /*
                 * Credentials not available.
                 */
                return null;
            }

        } else {
            TransactionContext tad = (TransactionContext) ctx.getApplicationData();
            if ( tad.getItspAccountInfo() != null ) {
            	return tad.getItspAccountInfo().getUserCredentials();
            } else {
            	return null;
            }
        }

    }

  

}
