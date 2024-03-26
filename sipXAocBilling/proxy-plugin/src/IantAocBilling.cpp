// All code (c)2010-2015 IANT GmbH all rights reserved
// Contributed to eZuce under a Contributor Agreement
//
// This software is free software; you can redistribute it and/or modify it under
// the terms of the Affero General Public License (AGPL) as published by the
// Free Software Foundation; either version 3 of the License, or (at your option)
// any later version.
//
// This software is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
// details.

#include <cassert>
#include <utl/UtlString.h>
#include <sys/time.h>
#include <sstream>
#include <string>
#include <stdlib.h>

#include "IantAocBilling.h"
#include "digitmaps/EmergencyRulesUrlMapping.h"
#include "sipXecsService/SipXecsService.h"
#include "os/OsLogger.h"
#include "os/OsConfigDb.h"
#include "os/OsFS.h"

#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string_regex.hpp>
#include <boost/scoped_ptr.hpp>
#include <mongo/client/connpool.h>
#include <mongo/client/dbclient.h>
#include <mongo/client/dbclient_rs.h>
#include "sipdb/MongoMod.h"

static const int SIPX_PLUGIN_PRIORITY = 991;
static const std::string COLLECTION_IANT_BILLING = "iant_billing";
static const std::string NAMESPACE_IANT_BILLING = "iant";

/// Factory used by PluginHooks to dynamically link the plugin instance
extern "C" SipBidirectionalProcessorPlugin* getTransactionPlugin(const UtlString& pluginName)
{
    MongoDB::ConnectionInfo global = MongoDB::ConnectionInfo::globalInfo();
    return new IantAocBilling(pluginName, SIPX_PLUGIN_PRIORITY, global);
}

IantAocBilling::IantAocBilling(const UtlString& instanceName, int priority, const MongoDB::ConnectionInfo& info) :
    SipBidirectionalProcessorPlugin(instanceName, priority),
    MongoDB::BaseDB(info,COLLECTION_IANT_BILLING)
{
}

IantAocBilling::~IantAocBilling()
{
}

void IantAocBilling::readConfig(OsConfigDb& configDb)
{
	OS_LOG_NOTICE(FAC_SIP, "IAB: IANT AOC Billing Plugin config Loaded");
}

void IantAocBilling::initialize()
{
	OS_LOG_NOTICE(FAC_SIP, "IAB: IANT AOC Billing Plugin initialized");
}

/*
*	Regex for Parsing amout out of xml body
*/
std::string IantAocBilling::getAmount(const std::string xml)
{
	boost::regex re(AOC_CURRENCY_REGEX);
	boost::cmatch matches;
	if(boost::regex_match(xml.c_str(), matches, re))
	{
	    // Get Amount from Match and trim empty Parts of string
        std::string val = matches[1];
        boost::trim(val);
        return val;
	}
	return "";
}

/*
* Find Amount in XML from AOC-D and AOC-E
*/
std::string IantAocBilling::aocParser(const std::string xml)
{
    try 
	{
        if(boost::starts_with(xml,AOC_XML_TAG))
        {
            if(boost::contains(xml,AOC_NS))
	        {
		        if(boost::contains(xml,AOC_D) || boost::contains(xml,AOC_E))
		        {
		            if(boost::contains(xml,AOC_CURRENCY_AMOUNT))
		            {
			           return getAmount(xml);
		            }
		        }
			}
	    }
	    return "";
    }
    catch (...)
    {
	    return "";
    }
}

/*
* Insert Data into DB
*/
void IantAocBilling::insertDataToMongoDb(const UtlString callId, const UtlString amount, const UtlString fromField, const UtlString toField)
{
    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: "<< "CallID: "<<callId << " Amount: "<< amount << " Timeout is "<<  getWriteQueryTimeout());
    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    mongo::DBClientBase* dbc = conn->get();

    try
    {
	    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Trying to Query ");
	    mongo::BSONObj query = BSON("_id" << callId);
	    mongo::BSONObj obj = dbc->findOne(NAMESPACE_IANT_BILLING + "." + COLLECTION_IANT_BILLING, query);
	    int oldAmount = 0;
	    int newAmount = 0;
	    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Query Object is " << obj.toString());
	    if (!obj.isEmpty() && obj.hasField("amount"))
	    {
	        OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Entry was already in MongoDB! ");
	        oldAmount = obj.getIntField("amount");
	    }
	    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Old Amount is "<<oldAmount);
	    try
	    {
	        OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Parsing new Amount");
	        newAmount = atoi(amount);
	    }
	    catch(...)
	    {
	        OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Parsing new Amount failed");
	    }
		
	    if(oldAmount<newAmount)
	    {
	        time_t ct = time(0);
	        OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Valid Amount ");
			mongo::BSONObj data = BSON("_id" << callId.str() << "amount" << amount.str() << "lastupdate" << ctime(&ct) << "fromUrl" << fromField.str() << "toUrl" << toField.str());
			dbc->update(NAMESPACE_IANT_BILLING + "." + COLLECTION_IANT_BILLING,query,data,true);
	    }
	    else
	    {
	        OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Invalid Amount ");
	    }
	    conn->done();
	    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Done() ");
    }
    catch (...)
    {
	    OS_LOG_DEBUG(FAC_SIP,"IAB: insertDataToMongoDb: Connection failed! ");
    }
}


void IantAocBilling::handleOutgoing(SipMessage& message, const char* address, int port)
{
    return;
}

void IantAocBilling::handleIncoming(SipMessage& message, const char* address, int port)
{
    OS_LOG_DEBUG(FAC_SIP,"IAB: Handle Incoming Message");
    if (!message.isResponse())
    {
	    UtlString method;
		message.getRequestMethod(&method);
	    if (0 == method.compareTo(SIP_INFO_METHOD, UtlString::ignoreCase) || 0 == method.compareTo(SIP_BYE_METHOD, UtlString::ignoreCase))
	    {
            OS_LOG_DEBUG(FAC_SIP,"IAB: Method is Info or Bye");
            if(checkContentType(message))
		    {
		        OS_LOG_DEBUG(FAC_SIP,"IAB: Content Type is correct");
		        parseInformationsFromSipMessage(message);
		    }
	    }
    } 
	else
	{
	    // Search for AOC in Responses 
	    int responseCode = message.getResponseStatusCode();
	    // Only for 1xx and 2xx responses check the record route and contact field of response
	    if ( responseCode >= SIP_TRYING_CODE && responseCode < SIP_MULTI_CHOICE_CODE)
	    {
	        OS_LOG_DEBUG(FAC_SIP,"IAB: Is valid Response Code: " << responseCode);
	        if(checkContentType(message))
	        {
                OS_LOG_DEBUG(FAC_SIP,"IAB: Content Type is correct");
                parseInformationsFromSipMessage(message);
	        }
	    }
    }
}

bool IantAocBilling::checkContentType(const SipMessage& message)
{
    const char* contentType = message.getHeaderValue(0,AOC_CONTENT_TYPE);	
    if(contentType)
    {
	    // Ok check if Content-Type: application/vnd.etsi.aoc+xml
	    UtlString temp = UtlString(contentType);
	    return 0==temp.compareTo(AOC_ETSI_HEADER, UtlString::ignoreCase);
    }
    return false;
}

void IantAocBilling::parseInformationsFromSipMessage(const SipMessage& message)
{
    OS_LOG_DEBUG(FAC_SIP,"IAB: Parsing Informations from Message");
    // Correct Message, get Content and parse Informations and safe to DB
	// Get Body
    UtlString body = message.getBody()->getBytes();
    OS_LOG_DEBUG(FAC_SIP,"IAB: message.getString() found "<< message.getString());
    if(!body.isNull())
    {
	    UtlString callId;
        message.getCallIdField(&callId);
        OS_LOG_DEBUG(FAC_SIP,"IAB: CallID found "<< callId);
	    OS_LOG_DEBUG(FAC_SIP,"IAB: Start Parsing");
	    std::string amount = aocParser(body.str());
	    OS_LOG_DEBUG(FAC_SIP,"IAB: Parsed Amount "<< amount);
	    if(!amount.empty())
	    {
            // Get additional Informations (From, To)
			UtlString fromField;
            UtlString toField;
			message.getFromField(&fromField);
            message.getToField(&toField);
		
	        // Found amount
	        insertDataToMongoDb(callId,amount,fromField,toField);
	        OS_LOG_DEBUG(FAC_SIP,"IAB: Data inserted to MongoDB!");
	    }
    }
}


