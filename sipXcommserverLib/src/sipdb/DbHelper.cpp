#include "sipdb/MongoDB.h"
#include "sipdb/EntityRecord.h"
#include "sipdb/EntityDB.h"
#include "sipdb/DbHelper.h"

#include <mongocxx/client.hpp>          
#include <mongocxx/database.hpp>        
#include <mongocxx/collection.hpp>      
#include <mongocxx/uri.hpp>             
#include <mongocxx/options/find.hpp>    
#include <mongocxx/exception/exception.hpp> 

#include <bsoncxx/types.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/json.hpp>    

#include <os/OsLogger.h>
#include <os/OsDateTime.h>


const char* pTimeNowMacro = "$now";


void DbHelper::printSetElements(std::ostream& strm,
                                const std::string& setName,
                                const std::set<std::string>& set,
                                bool multipleLines)
{
   int i = 0;

   if (multipleLines)
   {
      strm << boost::format("%-20s\n") % setName;

      for (std::set<std::string>::const_iterator iter = set.begin();
           iter != set.end(); iter++)
      {
         strm << boost::format(" %-3d %-20s\n") % i % *iter;
         i++;
      }
   }
   else
   {
      strm << boost::format("\"%s\":[") % setName;

      for (std::set<std::string>::const_iterator iter = set.begin();
           iter != set.end(); iter++)
      {
         strm << boost::format("%d %s ") % i % *iter;
         i++;
      }

      strm << boost::format("]   ") ;
   }
}

boost::posix_time::ptime DbHelper::convertSecondsToLocalTime(unsigned int seconds)
{
   typedef boost::date_time::c_local_adjustor<boost::posix_time::ptime> localAdj;

   boost::posix_time::ptime utcTime = boost::posix_time::ptime(boost::gregorian::date(1970,boost::gregorian::Jan,1),
                                       boost::posix_time::seconds(seconds));

   boost::posix_time::ptime localTime = localAdj::utc_to_local(utcTime);

   return localTime;
}

void DbHelper::printRegBindingEntry(std::ostream& strm,
                                    const bsoncxx::document::view& bson,
                                    int currentNr,
                                    bool multipleLines)
{
   RegBinding binding(bson);

   printCell(strm, "Nr", currentNr, currentNr, multipleLines);

   printCell(strm, binding.callId_fld(), binding.getCallId(), currentNr, multipleLines);                    // "callId"
   printCell(strm, binding.contact_fld(), binding.getContact(), currentNr, multipleLines);                  // "contact"
   printCell(strm, binding.cseq_fld(), binding.getCseq(), currentNr, multipleLines);                        // "cseq"
   printCell(strm, binding.expirationTime_fld(), binding.getExpirationTime(), currentNr, multipleLines);                                        // "expirationTime"
   printCell(strm, binding.expirationTime_fld(),
             DbHelper::convertSecondsToLocalTime(binding.getExpirationTime()), currentNr, multipleLines);   // "expirationTime"
   printCell(strm, binding.expired_fld(), binding.getExpired(), currentNr, multipleLines);                  // "expired"
   printCell(strm, binding.gruu_fld(), binding.getGruu(), currentNr, multipleLines);                        // "gruu"
   printCell(strm, binding.identity_fld(), binding.getIdentity(), currentNr, multipleLines);                // "identity"
   printCell(strm, binding.instanceId_fld(), binding.getInstanceId(), currentNr, multipleLines);            // "instanceId"
   printCell(strm, binding.instrument_fld(), binding.getInstrument(), currentNr, multipleLines);            // "instrument"
   printCell(strm, binding.localAddress_fld(), binding.getLocalAddress(), currentNr, multipleLines);        // "localAddress"
   printCell(strm, binding.path_fld(), binding.getPath(), currentNr, multipleLines);                        // "path"
   printCell(strm, binding.qvalue_fld(), binding.getQvalue(), currentNr, multipleLines);                    // "qvalue"
   printCell(strm, binding.timestamp_fld(), binding.getTimestamp(), currentNr, multipleLines);              // "timeStamp"
   printCell(strm, binding.timestamp_fld(),
             DbHelper::convertSecondsToLocalTime(binding.getTimestamp()), currentNr, multipleLines);        // "timeStamp"
   printCell(strm, binding.uri_fld(), binding.getUri(), currentNr, multipleLines);                          // "uri"

   strm << "\n";
}

void DbHelper::printEntityRecordEntry(std::ostream& strm,
                                      const bsoncxx::document::view& bson,
                                      int currentNr,
                                      bool multipleLines)
{
   EntityRecord entityRecord;
   EntityRecord& entityRecordRef = entityRecord;

   entityRecordRef = bson;


   printCell(strm, "Nr", currentNr, currentNr, multipleLines);

   printCell(strm, entityRecord.identity_fld(), entityRecord.identity(), currentNr, multipleLines);                  // "ident"
   printCell(strm, entityRecord.userId_fld(), entityRecord.userId(), currentNr, multipleLines);                      // "uid"
   printCell(strm, entityRecord.realm_fld(), entityRecord.realm(), currentNr, multipleLines);                        // "rlm"
   printCell(strm, entityRecord.password_fld(), entityRecord.password(), currentNr, multipleLines);                  // "pstk"
   printCell(strm, entityRecord.pin_fld(), entityRecord.pin(), currentNr, multipleLines);                            // "pntk"
   printCell(strm, entityRecord.authType_fld(), entityRecord.authType(), currentNr, multipleLines);                  // "authtp"
   printCell(strm, entityRecord.callForwardTime_fld(), entityRecord.callForwardTime(), currentNr, multipleLines);    // "cfwdtm"
   printCell(strm, entityRecord.location_fld(), entityRecord.location(), currentNr, multipleLines);                  // "loc"

   printCell(strm, entityRecord.callerId_fld(), entityRecord.callerId().id, currentNr, multipleLines);               // "clrid"
   printCell(strm, entityRecord.callerIdEnforcePrivacy_fld(),
             entityRecord.callerId().enforcePrivacy, currentNr, multipleLines);                                      // "blkcid"
   printCell(strm, entityRecord.callerIdIgnoreUserCalleId_fld(),
             entityRecord.callerId().ignoreUserCalleId, currentNr, multipleLines);                                   // "ignorecid"
   printCell(strm, entityRecord.callerIdTransformExtension_fld(),
             entityRecord.callerId().transformExtension, currentNr, multipleLines);                                  // "trnsfrmext"
   printCell(strm, entityRecord.callerIdExtensionLength_fld(),
             entityRecord.callerId().extensionLength, currentNr, multipleLines);                                     // "kpdgts"
   printCell(strm, entityRecord.callerIdExtensionPrefix_fld(),
             entityRecord.callerId().extensionPrefix, currentNr, multipleLines);                                     // "pfix"

   DbHelper::printSetElements(strm, entityRecord.permission_fld(), entityRecord.permissions(), multipleLines);       // "prm"

   printEntityRecordAliases(strm, entityRecord.aliases_fld(), entityRecord.aliases(), multipleLines);                // "als"
   printEntityRecordStaticUserLocations(strm, entityRecord.staticUserLoc_fld(),
                                        entityRecord.staticUserLoc(), multipleLines);                                // "stc"


   strm << "\n";
}

void DbHelper::printEntityRecordAliases(std::ostream& strm,
                                        const std::string& aliasesName,
                                        const std::vector<EntityRecord::Alias>& aliases,
                                        bool multipleLines)
{
   int i = 0;

   if (multipleLines)
   {
      strm << boost::format("%-20s\n") % aliasesName;

      for (std::vector<EntityRecord::Alias>::const_iterator iter = aliases.begin();
           iter != aliases.end(); iter++)
      {
         strm << boost::format(" %d \"%s\":  %s\n") % i % EntityRecord::aliasesId_fld() % iter->id;               // "id"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::aliasesContact_fld() % iter->contact;     // "cnt"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::aliasesRelation_fld() % iter->relation;   // "rln"
         i++;
      }
   }
   else
   {
      strm << boost::format("\"%s\":[") % aliasesName;

      for (std::vector<EntityRecord::Alias>::const_iterator iter = aliases.begin();
           iter != aliases.end(); iter++)
      {
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::aliasesId_fld() % iter->id;             // "id"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::aliasesContact_fld() % iter->contact;   // "cnt"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::aliasesRelation_fld() % iter->relation; // "rln"                                    // "rln"
         i++;
      }

      strm << boost::format("]   ") ;
   }
}

void DbHelper::printEntityRecordStaticUserLocations(std::ostream& strm,
                                                      const std::string& staticUserLocationsName,
                                                      const std::vector<EntityRecord::StaticUserLoc>& staticUserLocations,
                                                      bool multipleLines)
{
   int i = 0;

   if (multipleLines)
   {
      strm << boost::format("%-20s\n") % staticUserLocationsName;

      for (std::vector<EntityRecord::StaticUserLoc>::const_iterator iter = staticUserLocations.begin();
           iter != staticUserLocations.end(); iter++)
      {
         strm << boost::format(" %d \"%s\":  %s\n") % i % EntityRecord::staticUserLocEvent_fld() % iter->event;      // "evt"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::staticUserLocContact_fld() % iter->contact;   // "cnt"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::staticUserLocFromUri_fld() % iter->fromUri;   // "from"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::staticUserLocToUri_fld() % iter->toUri;       // "to"
         strm << boost::format(" %d \"%s\": %s\n") % i % EntityRecord::staticUserLocCallId_fld() % iter->callId;     // "cid"
         i++;
      }
   }
   else
   {
      strm << boost::format("\"%s\":[") % staticUserLocationsName;

      for (std::vector<EntityRecord::StaticUserLoc>::const_iterator iter = staticUserLocations.begin();
           iter != staticUserLocations.end(); iter++)
      {
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::staticUserLocEvent_fld() % iter->event;       // "evt"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::staticUserLocContact_fld() % iter->contact;   // "cnt"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::staticUserLocFromUri_fld() % iter->fromUri;   // "from"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::staticUserLocToUri_fld() % iter->toUri;       // "to"
         strm << boost::format("%d \"%s\":%s ") % i % EntityRecord::staticUserLocCallId_fld() % iter->callId;     // "cid"
         i++;
      }

      strm << boost::format("]   ") ;
   }
}

bool DbHelper::getLogicalOperator(const std::string& logicalOperator,
                                  std::string& label) 
                                  {
    if (logicalOperator == ">") 
    {
        label = "$gt"; // Greater than
        return true;
    } 
    else if (logicalOperator == "<") 
    {
        label = "$lt"; // Less than
        return true;
    } 
    else if (logicalOperator == "<=") 
    {
        label = "$lte"; // Less than or equal to
        return true;
    } 
    else if (logicalOperator == ">=") 
    {
        label = "$gte"; // Greater than or equal to
        return true;
    } 
    else if (logicalOperator == "!=") 
    {
        label = "$ne"; // Not equal
        return true;
    } 
    else if (logicalOperator == "=") 
    {
        return false; // No operator applied
    }

    // If the operator is unknown, throw an exception
    BOOST_THROW_EXCEPTION(DbHelperException() <<
                          DbHelperTagInfo((boost::format("Unknown logical operator %s\n") % logicalOperator).str()));

    return false; // Unreachable, but ensures no compiler warnings
}

void DbHelper::extractOperands(const std::string& string,
                                 std::string& leftOperand,
                                 std::string& logicalOperator,
                                 std::string& rightOperand)
{
    do
    {
       std::size_t pos = 0;
       std::size_t len = 0;
       const char* pSep = "><=!";

       // ex string="cseq<44"
       // find the first char of known logical operators
       len = string.find_first_of(pSep);
       if (len != std::string::npos)
       {
          //extract first token (left operand)
          leftOperand = string.substr(pos, len);
          boost::algorithm::trim(leftOperand);
       }
       else
          break;

       // update position and length in string for the second token (logical operator)
       pos = len;
       len = 1;

       // check if logical operator is 2 chars size based type (">=", "<=", "!=")
       if (string[pos + 1] == '=')
          len ++;

       // extract the second token (logical operator)
       logicalOperator = string.substr(pos, len);
       boost::algorithm::trim(logicalOperator);

       // update position and length in string for the third token
       pos += len;
       len = string.length() - pos;

       // extract third token (right operand)
       rightOperand = string.substr(pos, len);
       boost::algorithm::trim(rightOperand);
    }
    while(false);
}

void DbHelper::createQuery(bsoncxx::builder::basic::document& queryBuilder,
    MongoDB::MongoConnection& connection,
    std::vector<std::string>& whereOptVector,
    const std::string& ns)
{
    std::size_t size = whereOptVector.size();

    if (size > 0)
    {
        for (const std::string& whereOpt : whereOptVector)
        {
            // Temporary builder for sub-queries
            bsoncxx::builder::basic::document queryObjBuilderTmp;

            // Call appendRequiredType, which appends to queryObjBuilderTmp
            appendRequiredType(connection, queryObjBuilderTmp, ns, whereOpt);

            // Append the contents of queryObjBuilderTmp to queryBuilder
            for (const auto& element : queryObjBuilderTmp.view())
            {
                queryBuilder.append(bsoncxx::builder::basic::kvp(element.key(), element.get_value()));
            }
        }
    }
}

void DbHelper::deleteDbEntries(const MongoDB::ConnectionInfo& connectionInfo,
                               const std::string& ns,
                               std::vector<std::string>& whereOptVector)
{
    try {
         // Create a MongoConnection instance with the provided connection info
         MongoDB::MongoConnection connection(connectionInfo);

        // Access the database and collection
        auto collection = connection.collection(ns);

        // Create the query document
        bsoncxx::builder::basic::document queryBuilder;
        createQuery(queryBuilder, connection, whereOptVector, ns);

        // Perform the delete operation
        auto result = collection.delete_many(queryBuilder.view());

        // Log the result
        if (result) {
            OS_LOG_INFO(FAC_ODBC, "Deleted " << result->deleted_count() << " entries from the collection: " << ns);
        } else {
            OS_LOG_WARNING(FAC_ODBC, "No entries matched the delete query in collection: " << ns);
        }
    } catch (const std::exception& e) {
        // Log and rethrow any exception
        OS_LOG_ERROR(FAC_ODBC, "Error while deleting entries from collection: " << ns << " - " << e.what());
        throw;
    }
}

void DbHelper::printDbEntries(std::ostream& strm,
                              const MongoDB::ConnectionInfo& connectionInfo,
                              const std::string& ns,
                              std::vector<std::string>& whereOptVector,
                              const DbType dbType,
                              bool multipleLines)
{
    try {
         // Create a MongoConnection instance with the provided connection info
         MongoDB::MongoConnection connection(connectionInfo);

        // Access the database and collection
        auto collection = connection.collection(ns);

        // Set the function pointer for printing based on the database type
        if (dbType == DbTypeRegBinding) {
            _pFnPrintEntry = &DbHelper::printRegBindingEntry;
        } else if (dbType == DbTypeEntityRecord) {
            _pFnPrintEntry = &DbHelper::printEntityRecordEntry;
        } else {
            BOOST_THROW_EXCEPTION(DbHelperException() << 
                DbHelperTagInfo(std::string("Unknown database type")));
        }

        // Create the query document
        bsoncxx::builder::basic::document queryBuilder;
        createQuery(queryBuilder, connection, whereOptVector, ns);

        // Execute the query
        auto cursor = collection.find(queryBuilder.view());

        // Iterate over the query results and print each entry
        int dbEntryNr = 0;
        for (const auto& doc : cursor) {
            (this->*_pFnPrintEntry)(strm, doc, dbEntryNr, multipleLines);
            dbEntryNr++;
        }
    } catch (const std::exception& e) {
        // Log and rethrow any exceptions
        OS_LOG_ERROR(FAC_ODBC, "Error while printing database entries: " << e.what());
        throw;
    }
}

void DbHelper::expandMacro(std::string& macro)
{
   std::size_t pos = 0;
   std::string expandedMacro = getTimeNowMacro();

   if (0 == macro.find("$"))
   {
      pos = macro.find(pTimeNowMacro);
      if (std::string::npos != pos)
      {
         macro.replace(pos, strlen(pTimeNowMacro), expandedMacro);
         return;
      }

      BOOST_THROW_EXCEPTION(DbHelperException() <<
            DbHelperTagInfo((boost::format("Unknown macro %s") % macro).str()));
   }
}

std::string DbHelper::getTimeNowMacro()
{
   std::int64_t timeNow = OsDateTime::getSecsSinceEpoch();

   return boost::lexical_cast<std::string>(timeNow);
}

void DbHelper::appendRequiredType(MongoDB::MongoConnection& connection,
                                  bsoncxx::builder::basic::document& queryObjBuilder,
                                  const std::string& ns,
                                  const std::string& inputString) {

    bsoncxx::document::view bsonObj;

    // Retrieve the collection from the client
    mongocxx::collection collection = connection.collection(ns);

    // Perform a query to retrieve the first document
    std::optional<bsoncxx::document::value> cursor = collection.find_one({});
    if (cursor) {
        bsonObj = cursor->view();
    }

    std::string key;
    std::string value;
    std::string logicalOperator;

    // Extract key, logicalOperator, and value from the input string
    extractOperands(inputString, key, logicalOperator, value);

    // Perform macro expansion on the value
    expandMacro(value);

    // Determine the logical operator to use
    std::string logicalOperatorLabel;
    bool found = getLogicalOperator(logicalOperator, logicalOperatorLabel);

    // Get the BSON element associated with the key
    auto element = bsonObj[key];

    if (!element) {
        throw DbHelperException() << DbHelperTagInfo(
            (boost::format("Key '%s' not found in document\n") % key).str());
    }

    // Handle different BSON types
    if (element.type() == bsoncxx::type::k_string) {
        // Handle string type
        if (found) {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(
                key, bsoncxx::builder::basic::make_document(
                         bsoncxx::builder::basic::kvp(logicalOperatorLabel, value))));
        } else {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(key, value));
        }
    } else if (element.type() == bsoncxx::type::k_bool) {
        // Handle boolean type
        bool boolValue = boost::lexical_cast<bool>(value);
        if (found) {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(
                key, bsoncxx::builder::basic::make_document(
                         bsoncxx::builder::basic::kvp(logicalOperatorLabel, boolValue))));
        } else {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(key, boolValue));
        }
    } else if (element.type() == bsoncxx::type::k_int32) {
        // Handle integer type
        int intValue = boost::lexical_cast<int>(value);
        if (found) {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(
                key, bsoncxx::builder::basic::make_document(
                         bsoncxx::builder::basic::kvp(logicalOperatorLabel, intValue))));
        } else {
            queryObjBuilder.append(bsoncxx::builder::basic::kvp(key, intValue));
        }
    } else {
        // Throw exception if type is not supported
        throw DbHelperException()
            << DbHelperTagInfo((boost::format("No such type defined: %d\n") % static_cast<int>(element.type())).str());
    }
}


template <class T>
void DbHelper::printCell(std::ostream& strm, const std::string& cellName, T cellValue, int currentNr, bool multipleLines)
{
   if (multipleLines)
   {
      // %1% - first argument - cellName
      // %2% - second argument - cellValue
      // %|20t| - tabulation of 20 spaces
      strm << boost::format("%1% %|20t|%2%\n") % cellName % cellValue;
   }
   else
   {
      strm << boost::format("\"%1%\":%2%   ") % cellName % cellValue;
   }
}

DbHelper::DbHelper() : _pFnPrintEntry(0)
{

}

DbHelper::~DbHelper()
{
}
