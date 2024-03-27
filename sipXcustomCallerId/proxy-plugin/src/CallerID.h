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
#ifndef _CALLERID_H_
#define _CALLERID_H_

#include <os/OsConfigDb.h>
#include <sipxproxy/SipRouter.h>
#include <sipxproxy/AuthPlugin.h>
#include <boost/shared_ptr.hpp>

extern "C" AuthPlugin* getAuthPlugin(const UtlString& name);

/**
 * Modify the From header of a request based on lookup in the caller-alias database.
 *
 * This AuthPlugin provides a caller aliasing feature by modifying the From header
 * in an initial request and maintaining the translation throughout the dialog.
 * It is intended to provide a way to modify the caller-id presented in a PSTN call.
 *
 * The translation is controlled by entries in the caller-alias sipdb, which has two key
 * columns: the caller identity and the target domain; and one output column: the From header
 * field value.
 *
 * On an initial request, the first test is for an exact match of the identity (user@domain,
 * with no parameters) in the From header of the request and the target domain (from the
 * request URI).  Note that the target domain for a PSTN call is the domain of some gateway
 * with an unique identifier "sipxecs-lineid" . The gateway could be a local device or a
 * remote SIP Trunk service.  The lookup is _not_ * affected by any Route on the request,
 * so a request to an ITSP that is Routed through a local SBC matches on the ITSP domain,
 * not that of the SBC.
 *
 * If an exact match is found, then the resulting From header from the output column
 * is substituted.  This is used to replace the identity of a specific user when calling
 * a specific domain.
 *
 * If no exact match is found, and if the existing From header identity is a user in the
 * domain of this proxy, then a second check is made in the caller-alias database for a
 * "wildcard" match: the caller identity used for this lookup is "*", and the target domain
 * from the request URI (as in the exact match test).  If this matches a row in the caller-alias
 * database, the From value from that row is substituted.  Note the exclusion: this wildcard
 * match is not attempted if the caller is not within the domain, so a call coming from outside
 * the domain that is forwarded by a local user is not aliased (the original outside
 * caller-id is maintained).
 *
 * If neither match finds a row in the caller-alias database, the From header is left unchanged.
 *
 * When a From header value is substituted, the entire value from the table is used, and the only
 * part of the original From header value that is preserved is the 'tag' field parameter.
 * Both the full original From header value and the aliased From header value are added to the
 * RouteState for the request.  This causes these values to be preserved in the route set.  As
 * subsequent requests and responses follow the route set through this proxy, this plugin
 * modifies the From header using these saved values so that each participant in the dialog
 * sees only the From header value they expect to see (the caller sees only the original value,
 * and the called party sees only the aliased value).
 */

class CallerDB {
public:
	virtual ~CallerDB() {}
	virtual std::string getCallerName(const std::string& number) = 0;
	virtual std::string getCalledName(const std::string& number) = 0;
};

class CallerMongoDB : public CallerDB {

public:
	// DDI is called DID in US
	static const std::string COLLECTION_DDI;
	static const std::string COLLECTION_DNIS;

	CallerMongoDB(MongoDB::ConnectionInfo& connectionInfo, const std::string& ns);
	~CallerMongoDB() {};
	virtual std::string getCallerName(const std::string& number);
	virtual std::string getCalledName(const std::string& number);

protected:
	friend class CallerDbTest;

private:
	std::string getRewrite(const std::string& number, const std::string& collection);
	std::string _ns;
  MongoDB::ConnectionInfo& _connectionInfo;
};

const std::string CallerMongoDB::COLLECTION_DDI = "cci_ddi";
const std::string CallerMongoDB::COLLECTION_DNIS = "cci_dnis";

class CallerID: public AuthPlugin {
public:

	virtual ~CallerID() {};

	virtual AuthResult authorizeAndModify(const UtlString& id,
			const Url& requestUri, RouteState& routeState,
			const UtlString& method, AuthResult priorResult,
			SipMessage& request, bool bSpiralingRequest, UtlString& reason);

	virtual void announceAssociatedSipRouter(SipRouter* sipRouter);

	void readConfig(OsConfigDb& configDb) {}

protected:
	friend class CallerIDTest;

private:
	friend AuthPlugin* getAuthPlugin(const UtlString& name);
	CallerID(const UtlString& instanceName, boost::shared_ptr<CallerDB> db);
	boost::shared_ptr<CallerDB> _db;

	SipRouter* mpSipRouter;
};

#endif // _CALLERID_H_
