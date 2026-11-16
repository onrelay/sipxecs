
//
// Copyright (C) 2009 Nortel, certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
// $$
//////////////////////////////////////////////////////////////////////////////

// SYSTEM INCLUDES
#include <ctime>
#include <cstdint>

// APPLICATION INCLUDES
#include <utl/UtlRegex.h>
#include <utl/UtlBool.h>
#include "net/SipXauthIdentity.h"
#include "sipXecsService/SipXecsService.h"
#include "sipXecsService/SharedSecret.h"
#include "os/OsDateTime.h"
#include "os/OsLock.h"
#include "os/OsLogger.h"
#include "SipRedirectorPresenceRouting.h"
#include "registry/SipRedirectServer.h"
#include "sipdb/EntityDB.h"
#include "registry/SipRegistrar.h"

#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/document/view.hpp>
#include <mongocxx/exception/exception.hpp>

// DEFINES
#define CONFIG_SETTING_SIP_DOMAIN "SIP_DOMAIN"
#define CONFIG_SETTING_VOICEMAIL_ON_BUSY "VOICEMAIL_ON_BUSY"
#define CONFIG_SETTING_USER_PREFS_FILE "USER_PREFS_FILE"

#define VOICEMAIL_CONTACT_PREFIX ("~~vm~")

UnifiedPresence::UnifiedPresence( UtlString aor ) :
    UtlString( aor )
{
}

const UtlString& UnifiedPresence::getXmppPresence( void ) const
{
    return mXmppPresence;
}

void UnifiedPresence::setXmppPresence( const UtlString& xmppPresence )
{
    mXmppPresence = xmppPresence;
}

const UtlString& UnifiedPresence::getXmppStatusMessage( void ) const
{
    return mXmppStatusMessage;
}

void UnifiedPresence::setXmppStatusMessage( const UtlString& xmppStatusMessage )
{
    mXmppStatusMessage = xmppStatusMessage;
}

const UtlString& UnifiedPresence::getSipState( void ) const
{
    return mSipState;
}

void UnifiedPresence::setSipState( const UtlString& sipState )
{
    mSipState = sipState;
}

const UtlString& UnifiedPresence::getUnifiedPresence( void ) const
{
    return mUnifiedPresence;
}

void UnifiedPresence::setUnifiedPresence( const UtlString& unifiedPresence )
{
    mUnifiedPresence = unifiedPresence;
}

// Unified presence documents are maintained by the sipXopenfire presence
// plugin (see MongoUnifiedPresenceWriter in sipXopenfire), which re-stamps the
// updated timestamp of every document it actively tracks at least every 30
// seconds. Documents with an older timestamp are left-overs from a crashed or
// restarted openfire and must be ignored. Other components may contribute
// additional fields (for example sipState) to the documents.
const std::string UnifiedPresenceDB::NS("imdb.unifiedPresence");

// Writers refresh tracked documents every 30s; allow a 10s grace on top of
// the refresh period (scheduler jitter, mongo write latency, clock skew
// between hosts) before declaring the data stale.
#define MAX_PRESENCE_AGE_MSECS ((30 + 10) * 1000)

static std::string getStringField( const bsoncxx::document::view& doc, const char* fieldName )
{
    bsoncxx::document::element element = doc[fieldName];
    if( element && element.type() == bsoncxx::type::k_string )
    {
        return std::string( element.get_string().value );
    }
    return std::string();
}

bool UnifiedPresenceDB::findByIdentity( const std::string& identity, UnifiedPresence& presence ) const
{
    MongoDB::ReadTimer readTimer( const_cast<UnifiedPresenceDB&>(*this) );

    try
    {
        MongoDB::MongoConnection connection(_info);

        readTimer.setDBConnOK(connection.ok());

        mongocxx::collection collection = connection.collection(_ns);

        bsoncxx::builder::basic::document queryBuilder;
        queryBuilder.append(bsoncxx::builder::basic::kvp("_id", identity));

        mongocxx::cursor cursor = collection.find(queryBuilder.view(), mongocxx::options::find{}.limit(1));

        mongocxx::cursor::iterator it = cursor.begin();
        if (it == cursor.end())
        {
            OS_LOG_DEBUG(FAC_ODBC, "UnifiedPresenceDB::findByIdentity - no presence for " << identity
                    << " in namespace " << _ns);
            return false;
        }

        bsoncxx::document::view doc = *it;

        // Ignore stale documents: the openfire plugin refreshes the updated
        // timestamp of all tracked presence documents periodically, so an old
        // timestamp means the writer is gone and the state can not be trusted.
        bsoncxx::document::element updatedElement = doc["updated"];
        if( !updatedElement || updatedElement.type() != bsoncxx::type::k_date )
        {
            OS_LOG_INFO(FAC_ODBC, "UnifiedPresenceDB::findByIdentity - presence for " << identity
                    << " has no valid updated timestamp - ignoring");
            return false;
        }
        int64_t updatedMsecs = updatedElement.get_date().to_int64();
        int64_t nowMsecs = static_cast<int64_t>( std::time(NULL) ) * 1000;
        if( nowMsecs - updatedMsecs > MAX_PRESENCE_AGE_MSECS )
        {
            OS_LOG_INFO(FAC_ODBC, "UnifiedPresenceDB::findByIdentity - presence for " << identity
                    << " is stale (" << (nowMsecs - updatedMsecs) / 1000 << "s old) - ignoring");
            return false;
        }

        presence.setXmppPresence( getStringField( doc, "xmppPresence" ).c_str() );
        presence.setXmppStatusMessage( getStringField( doc, "xmppStatusMessage" ).c_str() );
        presence.setSipState( getStringField( doc, "sipState" ).c_str() );
        presence.setUnifiedPresence( getStringField( doc, "unifiedPresence" ).c_str() );
        return true;
    }
    catch (mongocxx::exception& e)
    {
        readTimer.setDBConnOK(false);
        OS_LOG_ERROR(FAC_ODBC, "UnifiedPresenceDB::findByIdentity - Error querying MongoDB: " << e.what());
    }
    return false;
}

UtlString SipRedirectorPresenceRouting::sLocalDomain;

// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS


const RegEx TelUri( "tel:([\\+\\.\\(\\)0-9\\-]+?)(,|\\s|\\Z)" );
const RegEx SipUri( "(sip:.+@.+?)(,|\\s|\\Z)" );

// Static factory function.
extern "C" RedirectPlugin* getRedirectPlugin(const UtlString& instanceName)
{
    return new SipRedirectorPresenceRouting(instanceName);
}

// Constructor
SipRedirectorPresenceRouting::SipRedirectorPresenceRouting(const UtlString& instanceName) :
    RedirectPlugin(instanceName),
    mpPresenceDb( NULL )
{
    mLogName.append("[");
    mLogName.append(instanceName);
    mLogName.append("] SipRedirectorPresenceRouting");
}

// Destructor
SipRedirectorPresenceRouting::~SipRedirectorPresenceRouting()
{
   delete mpPresenceDb;
   mpPresenceDb = NULL;
}

// Read config information.
void SipRedirectorPresenceRouting::readConfig(OsConfigDb& configDb)
{
    // extract the SIP domain information from the config DB - we need this part
    // to transform the tel URI into a SIP URI
    if ((configDb.get(CONFIG_SETTING_SIP_DOMAIN, mSipDomain) != OS_SUCCESS) ||
         mSipDomain.isNull())
    {
        Os::Logger::instance().log(FAC_SIP, PRI_ERR,
                     "%s::readConfig No SIP domain specified in the configuration",
                     mLogName.data());
    }
    else
    {
        Os::Logger::instance().log(FAC_SIP, PRI_INFO,
                    "%s::readConfig mSipDomain = '%s'",
                    mLogName.data(), mSipDomain.data() );
    }

    mbForwardToVmOnBusy = configDb.getBoolean(CONFIG_SETTING_VOICEMAIL_ON_BUSY, FALSE);
    Os::Logger::instance().log(FAC_SIP, PRI_INFO,
                 "%s::readConfig mbForwardToVmOnBusy = %d",
                 mLogName.data(), mbForwardToVmOnBusy);

    
    UtlString prefsFilename;
    configDb.get(CONFIG_SETTING_USER_PREFS_FILE, prefsFilename);
    Os::Logger::instance().log(FAC_SIP, PRI_INFO,
                 "%s::readConfig prefsFilename = %s",
                 mLogName.data(), prefsFilename.data());
    mUserPrefs.setFileName( &prefsFilename );

    // read the domain configuration
    OsConfigDb domainConfiguration;
    domainConfiguration.loadFromFile(SipXecsService::domainConfigPath());
   // get the shared secret for generating signatures
    SharedSecret secret(domainConfiguration);

    // Set secret for signing SipXauthIdentity
    SipXauthIdentity::setSecret(secret.data());
}

// Initialize
OsStatus
SipRedirectorPresenceRouting::initialize(OsConfigDb& configDb,
                                 int redirectorNo,
                                 const UtlString& localDomainName)
{
   Os::Logger::instance().log(FAC_SIP, PRI_DEBUG,
                 "%s::SipRedirectorPresenceRouting::initialize", mLogName.data() );

   sLocalDomain = localDomainName;

   OsStatus rc = OS_FAILED;
   try
   {
      MongoDB::ConnectionInfo globalInfo = MongoDB::ConnectionInfo::globalInfo();
      mpPresenceDb = new UnifiedPresenceDB( globalInfo );
      rc = OS_SUCCESS;
   }
   catch (std::exception& e)
   {
      Os::Logger::instance().log(FAC_SIP, PRI_ERR,
                    "%s::initialize failed to create unified presence DB connection: %s",
                    mLogName.data(), e.what() );
   }
   return rc;
}


// Finalize
void
SipRedirectorPresenceRouting::finalize()
{
}

RedirectPlugin::LookUpStatus
SipRedirectorPresenceRouting::lookUp(
   const SipMessage& message,
   UtlString& requestString,
   Url& requestUri,
   const UtlString& method,
   ContactList& contactList,
   RequestSeqNo requestSeqNo,
   int redirectorNo,
   SipRedirectorPrivateStorage*& privateStorage,
   ErrorDescriptor& errorDescriptor)
{
   RedirectPlugin::LookUpStatus rc = RedirectPlugin::SUCCESS;

   // this is a new request that we need to process.
   // If the called party is a local user,
   // look up the unified presence of the called party
   // to find out its presence state.

   // If the request URI can be found in the identity column of the credentials
   // database, then a request is to a local user, find out its presence state...
   EntityRecord entity;
   EntityDB* entityDb = SipRegistrar::getInstance(NULL)->getEntityDB();
   if (entityDb->findByIdentity(requestUri, entity))
   {
      // If url param sipx-userforward = false, do not redirect...
      UtlString disableForwarding;
      requestUri.getUrlParameter("sipx-userforward", disableForwarding);
      if (disableForwarding.compareTo("false", UtlString::ignoreCase) == 0)
      {
         Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "%s::lookUp user forwarding disabled by parameter",
                       mLogName.data());
      }
      else
      {
         Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "%s::lookUp identity '%s'",
                       mLogName.data(), requestString.data());
         rc = doLookUp( requestUri, message, contactList );
      }
   }
   return rc;
}

RedirectPlugin::LookUpStatus
SipRedirectorPresenceRouting::doLookUp(
   const Url& requestUri,
   const SipMessage& message,
   ContactList& contactList)
{
   // check if we have unified presence info for this user
   UtlString identity;
   UtlString username;
   requestUri.getIdentity( identity );
   requestUri.getUserId( username );
   Os::Logger::instance().log(FAC_SIP, PRI_INFO, "%s::LookUpStatus is looking up '%s'",
                                    mLogName.data(),identity.data() );

   UnifiedPresence up( identity );
   if( mpPresenceDb && mpPresenceDb->findByIdentity( identity.str(), up ) )
   {
      // unified presence data is available for the called party.
      // Use it to make call routing decisions.
      Os::Logger::instance().log(FAC_SIP, PRI_INFO, "%s::LookUpStatus "
                                       "Presence information for '%s':\r\n"
                                       "    Telephony presence: '%s'"
                                       "    XMPP presence: '%s'"
                                       "    Custom presence message: '%s'",
                                       mLogName.data(),
                                       identity.data(), up.getSipState().data(),
                                       up.getXmppPresence().data(),
                                       up.getXmppStatusMessage().data() );

      // look for tel URI in the custom presence message
      RegEx telUri( TelUri );
      telUri.Search( up.getXmppStatusMessage().data() );
      UtlString target;
      if( !mSipDomain.isNull() && telUri.MatchString( &target, 1 ) )
      {
         // prepend 'sip:', append the SIP domain and add resulting target as contact 
         target.insert( 0, "sip:" );
         target.append( '@' );
         target.append( mSipDomain );
         addContactToContactList( target, requestUri, message, contactList );
      }
      else
      {
         // no tel URI - look for a sip URI then
         RegEx sipUri( SipUri );
         sipUri.Search( up.getXmppStatusMessage().data() );
         if( sipUri.MatchString( &target, 1 ) )
         {
            addContactToContactList( target, requestUri, message, contactList );
         }
         else
         {
            // no tel or sip URI, check to see if we need to send the call directly
            // to voicemail based on seetings, presence and user preferences
            if( ( up.getSipState().compareTo("BUSY", UtlString::ignoreCase ) == 0 && mbForwardToVmOnBusy ) ||
                ( up.getXmppPresence().compareTo("BUSY", UtlString::ignoreCase ) == 0 && mUserPrefs.forwardToVoicemailOnDnd( username ) ) )
            {
               // prune all non-voicemail contacts from the list
               removeNonVoicemailContacts( contactList );
            }
         }
      }   
   }
   return RedirectPlugin::SUCCESS;
}

void SipRedirectorPresenceRouting::addContactToContactList( const UtlString& target,
                                                            const Url& requestUri,
                                                            const SipMessage& message,
                                                            ContactList& contactList )
{
   UtlString requestIdentity;
   requestUri.getIdentity( requestIdentity );
   SipXauthIdentity authIdentity;
   authIdentity.setIdentity( requestIdentity );
   Url targetUri( target );
   // Encode AuthIdentity into the URI
   authIdentity.encodeUri(targetUri, message);
   contactList.add( targetUri, *this );
}

void SipRedirectorPresenceRouting::removeNonVoicemailContacts( ContactList& contactList )
{
   // walk the list to find the contact entry for voicemail
   size_t index;
   Url contactUrl;
   bool bVoicemailContactFound = false;
   for( index = 0; index < contactList.entries(); index++ )
   {
      if( contactList.get( index, contactUrl ) )
      {
         UtlString userPart;
         contactUrl.getUserId( userPart );
         if( userPart.index( VOICEMAIL_CONTACT_PREFIX ) == 0 )
         {
            bVoicemailContactFound = true;
            break;
         }
      }
   }

   // if vm contact found, remove all and put vm contact back in.
   if( bVoicemailContactFound )
   {
      contactList.removeAll( *this );
      contactList.add( contactUrl, *this );
   }
}

const UtlString& SipRedirectorPresenceRouting::getLocalDomainName( void )
{
   return sLocalDomain;
}

const UtlString& SipRedirectorPresenceRouting::name( void ) const
{
   return mLogName;
}

PresenceRoutingUserPreferences::PresenceRoutingUserPreferences() :
   mMutex( OsMutex::Q_FIFO )
{
}

OsStatus PresenceRoutingUserPreferences::initialize()
{
   OsStatus currentStatus = OS_SUCCESS;
   return currentStatus;
}

OsStatus PresenceRoutingUserPreferences::parseDocument()
{
  return OS_SUCCESS;
}

bool PresenceRoutingUserPreferences::forwardToVoicemailOnDnd(const UtlString& sipUsername )
{
   EntityRecord entity;
   EntityDB* entityDb = SipRegistrar::getInstance(NULL)->getEntityDB();
   if (entityDb->findByUserId(sipUsername.str(), entity))
   {
      return entity.vmOnDnd();
   }
   else
   {
      return false;
   }
}
