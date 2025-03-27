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

#include "os/OsLogger.h"
#include "os/OsConfigDb.h"
#include "os/OsFS.h"

#if !defined(BOOST_BIND_GLOBAL_PLACEHOLDERS)
  #define BOOST_BIND_GLOBAL_PLACEHOLDERS
#endif
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string_regex.hpp>
#include <boost/scoped_ptr.hpp>

#include <bsoncxx/document/view.hpp>

#include "IantAocBilling.h"
#include "digitmaps/EmergencyRulesUrlMapping.h"
#include "sipXecsService/SipXecsService.h"

static const int SIPX_PLUGIN_PRIORITY = 991;
static const std::string COLLECTION_IANT_BILLING = "iant_billing";
static const std::string DATABASE_IANT_BILLING = "iant";

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
    OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: CallID: " << callId << " Amount: " << amount << " Timeout is " << getWriteQueryTimeout());

    try {
		// Create MongoConnection with the configured connection information
   	 	MongoDB::MongoConnection connection(_info);

        // Access the target database and collection
        auto database = connection.database(DATABASE_IANT_BILLING);
        auto collection = database[COLLECTION_IANT_BILLING];

        // Build the query to search for the document
        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp(std::string("_id"), callId.str()));

        mongocxx::options::find findOptions;
        findOptions.max_time(std::chrono::milliseconds(_info.getReadQueryTimeoutMs()));

        // Find the document matching the query
        std::optional<bsoncxx::document::value> maybeResult = collection.find_one(queryBuilder.view(), findOptions );

        int oldAmount = 0;
        int newAmount = 0;

        if (maybeResult) {
            bsoncxx::document::view result = maybeResult->view();
            if (result["amount"]) {
                oldAmount = result["amount"].get_int32();
                OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Entry was already in MongoDB! Old Amount: " << oldAmount);
            }
        }

        try {
            newAmount = std::stoi(amount.str());
            OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Parsed new Amount: " << newAmount);
        } catch (const std::exception& e) {
            OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Parsing new Amount failed: " << e.what());
        }

        if (oldAmount < newAmount) {
            // Current time for last update
            std::time_t currentTime = std::time(nullptr);
            std::string timeString = std::ctime(&currentTime);
            timeString.erase(timeString.find_last_not_of("\n") + 1); // Remove trailing newline

            // Build the data to insert or update
            bsoncxx::builder::basic::document dataBuilder;
            dataBuilder.append(bsoncxx::builder::basic::kvp(std::string("_id"), callId.str()));
            dataBuilder.append(bsoncxx::builder::basic::kvp(std::string("amount"), newAmount));
            dataBuilder.append(bsoncxx::builder::basic::kvp(std::string("lastupdate"), timeString));
            dataBuilder.append(bsoncxx::builder::basic::kvp(std::string("fromUrl"), fromField.str()));
            dataBuilder.append(bsoncxx::builder::basic::kvp(std::string("toUrl"), toField.str()));

            bsoncxx::document::view data = dataBuilder.view();

            // Perform the upsert operation
            collection.update_one(
                queryBuilder.view(),
                bsoncxx::builder::basic::make_document(
                    bsoncxx::builder::basic::kvp(std::string("$set"), data)),
                mongocxx::options::update{}.upsert(true)
            );

            OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Valid Amount. Document updated or inserted.");
        } else {
            OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Invalid Amount. No update performed.");
        }
    } catch (const std::exception& e) {
        OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Connection or operation failed: " << e.what());
        throw; // Rethrow the exception to allow higher-level handling
    }

    OS_LOG_DEBUG(FAC_SIP, "IAB: insertDataToMongoDb: Operation completed.");
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


