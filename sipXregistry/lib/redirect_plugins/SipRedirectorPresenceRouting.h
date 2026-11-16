//
//
// Copyright (C) 2009 Nortel, certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
// $$
//////////////////////////////////////////////////////////////////////////////

#ifndef SIPREDIRECTORPRESENCEROUTING_H
#define SIPREDIRECTORPRESENCEROUTING_H

// SYSTEM INCLUDES
#include <string>

// APPLICATION INCLUDES
#include "filereader/RefreshingFileReader.h"
#include "registry/RedirectPlugin.h"
#include "os/OsMutex.h"
#include "sipdb/MongoDB.h"

// DEFINES
// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS

class PresenceRoutingUserPreferences : public RefreshingFileReader
{
public:
   PresenceRoutingUserPreferences();
   bool forwardToVoicemailOnDnd(const UtlString& sipUsername );
   virtual ~PresenceRoutingUserPreferences(){};

protected:
   virtual OsStatus initialize();

private:
   UtlHashMap mUserVmOnDndPreferences;
   OsMutex    mMutex;
   OsStatus parseDocument();
};

class UnifiedPresence : public UtlString
{
public:
   UnifiedPresence( UtlString aor );
   const UtlString& getXmppPresence( void ) const;
   void setXmppPresence( const UtlString& xmppPresence );
   const UtlString& getXmppStatusMessage( void ) const;
   void setXmppStatusMessage( const UtlString& xmppStatusMessage );
   const UtlString& getSipState( void ) const;
   void setSipState( const UtlString& sipState );
   const UtlString& getUnifiedPresence( void ) const;
   void setUnifiedPresence( const UtlString& unifiedPresence );
private:
   UtlString mXmppPresence;
   UtlString mXmppStatusMessage;
   UtlString mSipState;
   UtlString mUnifiedPresence;
};

/**
 * Reads unified presence state from the shared imdb.unifiedPresence mongo
 * collection. The collection is maintained by the sipXopenfire presence
 * plugin, which owns the XMPP related fields of each document; other
 * components may contribute additional fields (for example sipState).
 * Documents are keyed by the SIP identity (user@domain).
 */
class UnifiedPresenceDB : public MongoDB::BaseDB
{
public:
   static const std::string NS;

   UnifiedPresenceDB(const MongoDB::ConnectionInfo& info) :
      MongoDB::BaseDB(info, NS)
   {
   }

   bool findByIdentity(const std::string& identity, UnifiedPresence& presence) const;
};

/**
 * SipRedirectorPresenceRouting is a class whose object adds or removes contacts
 * based on the presence state of the called users.
 */
class SipRedirectorPresenceRouting : public RedirectPlugin
{
  public:

   explicit SipRedirectorPresenceRouting(const UtlString& instanceName);

   ~SipRedirectorPresenceRouting();

   virtual void readConfig(OsConfigDb& configDb);

   virtual OsStatus initialize(OsConfigDb& configDb,
                               int redirectorNo,
                               const UtlString& localDomainHost);

   virtual void finalize();

   virtual RedirectPlugin::LookUpStatus lookUp(
      const SipMessage& message,
      UtlString& requestString,
      Url& requestUri,
      const UtlString& method,
      ContactList& contactList,
      RequestSeqNo requestSeqNo,
      int redirectorNo,
      SipRedirectorPrivateStorage*& privateStorage,
      ErrorDescriptor& errorDescriptor);

   // created for unit testability
   RedirectPlugin::LookUpStatus
   doLookUp( const Url& requestUri,
             const SipMessage& message,
             ContactList& contactList );

   virtual const UtlString& name( void ) const;

   static const UtlString& getLocalDomainName( void );

  protected:

   // String to use in place of class name in log messages:
   // "[instance] class".
   UtlString mLogName;
   UtlString mSipDomain;
   UtlBoolean mbForwardToVmOnBusy;

  private:
   void addContactToContactList( const UtlString& target, const Url& requestUri, const SipMessage& message, ContactList& contactList );
   void removeNonVoicemailContacts( ContactList& contactList );
   static UtlString  sLocalDomain;
   UnifiedPresenceDB* mpPresenceDb;
   PresenceRoutingUserPreferences mUserPrefs;
};

#endif // SIPREDIRECTORPRESENCEROUTING_H
