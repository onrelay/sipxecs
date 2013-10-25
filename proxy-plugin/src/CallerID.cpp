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
#include <os/OsLogger.h>
#include "sipdb/MongoDB.h"
#include <sipxproxy/SipRouter.h>
#include <boost/shared_ptr.hpp>
#include <mongo/client/connpool.h>
#include <CallerID.h>

extern "C" AuthPlugin* getAuthPlugin(const UtlString& pluginName)
{
  MongoDB::ConnectionInfo info = MongoDB::ConnectionInfo::globalInfo();
	boost::shared_ptr<CallerDB> db((CallerDB *)new CallerMongoDB(info, std::string("imdb")));
	CallerID* instance = new CallerID(pluginName, db);
	return instance;
}

CallerID::CallerID(const UtlString& pluginName,  boost::shared_ptr<CallerDB> db) :
	AuthPlugin(pluginName), _db(db), mpSipRouter(0)
{
}

AuthPlugin::AuthResult CallerID::authorizeAndModify(const UtlString& id,
		const Url& requestUri, RouteState& routeState, const UtlString& method,
		AuthResult priorResult, SipMessage& request, bool bSpiralingRequest,
		UtlString& reason)
{
	if (method.compareTo(SIP_INVITE_METHOD) == 0)
	{
		// no point in modifying a request that won't be sent
		if (priorResult != DENY)
		{
			// a new dialog?
			if (routeState.isMutable() && routeState.directionIsCallerToCalled(
					mInstanceName.data()))
			{ // FIXME
				try
				{
          // Get the callers identity by getting the caller URI.
				   UtlString originalFromField, cNewFromUrl;
				   request.getFromField(&originalFromField);
				   Url originalFromUrl(originalFromField);

				   // Retrieve callerName from the number
				   UtlString cCallerNumber;
				   originalFromUrl.getUserId(cCallerNumber);

				   bool identityIsLocal = mpSipRouter->isLocalDomain(originalFromUrl);
				   if (identityIsLocal)
				   {
				     Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "Processing caller number %s", cCallerNumber.data());

		          const std::string origCaller(cCallerNumber.data());
		          UtlString cCallerName = _db->getCallerName(origCaller);

		          if (!cCallerName.isNull())
		          {
                // Actually rewriting the FROM header with its new Displayname
                originalFromUrl.setUserId(cCallerName);
                originalFromUrl.toString(cNewFromUrl);
                request.setRawFromField(cNewFromUrl.data());
		          }
				   }
				}
				catch (std::exception &e)
				{
					Os::Logger::instance().log(FAC_SIP, PRI_ERR,
							"CallerID[%s]::authorizeAndModify ", e.what());
				}
			}
			else
			{
				Os::Logger::instance().log(FAC_SIP, PRI_DEBUG,
						"CallerID[%s]::authorizeAndModify "
							"not mutable - no rewrite", mInstanceName.data());
			}
		}
	}

	return AuthPlugin::CONTINUE;
}

CallerMongoDB::CallerMongoDB(MongoDB::ConnectionInfo& connectionInfo, const std::string& ns)
: _ns(ns), _connectionInfo(connectionInfo) {
}

std::string CallerMongoDB::getCalledName(const std::string& number) {
	return getRewrite(number, COLLECTION_DDI);
}

std::string CallerMongoDB::getCallerName(const std::string& number) {
	return getRewrite(number, COLLECTION_DNIS);
}

std::string CallerMongoDB::getRewrite(const std::string& number, const std::string& collection) {
    
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_connectionInfo.getConnectionString().toString()));

  mongo::Query query = QUERY("from" << number);
	std::string ns_col(_ns);
	ns_col.append(".").append(collection);

	mongo::BSONObj r = conn->get()->findOne(ns_col, query);
	conn->done();

	std::string out = r.toString();
	if (!r.isEmpty() && r.hasField("to")) {
		return r.getStringField("to");
	}

  return std::string();
}

void CallerID::announceAssociatedSipRouter(SipRouter* sipRouter)
{
   mpSipRouter = sipRouter;
}
