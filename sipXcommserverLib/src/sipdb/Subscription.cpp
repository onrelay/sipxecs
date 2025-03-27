/*
 * Copyright (c) 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

#include <os/OsLogger.h>

#include "sipdb/Subscription.h"

using namespace std;

const char* Subscription::oid_fld(){ static std::string fld = "_id"; return fld.c_str(); }
const char* Subscription::component_fld(){ static std::string fld = "component"; return fld.c_str(); }
const char* Subscription::uri_fld(){ static std::string fld = "uri"; return fld.c_str(); }
const char* Subscription::callId_fld(){ static std::string fld = "callId"; return fld.c_str(); }
const char* Subscription::contact_fld(){ static std::string fld = "contact"; return fld.c_str(); }
const char* Subscription::notifyCseq_fld(){ static std::string fld = "notifyCseq"; return fld.c_str(); }
const char* Subscription::subscribeCseq_fld(){ static std::string fld = "subscribeCseq"; return fld.c_str(); }
const char* Subscription::eventTypeKey_fld(){ static std::string fld = "eventTypeKey"; return fld.c_str(); }
const char* Subscription::eventType_fld(){ static std::string fld = "eventType"; return fld.c_str(); }
const char* Subscription::id_fld(){ static std::string fld = "id"; return fld.c_str(); }
const char* Subscription::toUri_fld(){ static std::string fld = "toUri"; return fld.c_str(); }
const char* Subscription::fromUri_fld(){ static std::string fld = "fromUri"; return fld.c_str(); }
const char* Subscription::key_fld(){ static std::string fld = "key"; return fld.c_str(); }
const char* Subscription::recordRoute_fld(){ static std::string fld = "recordRoute"; return fld.c_str(); }
const char* Subscription::accept_fld(){ static std::string fld = "accept"; return fld.c_str(); }
const char* Subscription::file_fld(){ static std::string fld = "file"; return fld.c_str(); }
const char* Subscription::version_fld(){ static std::string fld = "version"; return fld.c_str(); }
const char* Subscription::expires_fld(){ static std::string fld = "expires"; return fld.c_str(); }
const char* Subscription::shardId_fld(){ static std::string fld = "shardId"; return fld.c_str(); }

Subscription::Subscription() :
    _notifyCseq(0),
    _subscribeCseq(0),
    _version(0),
    _expires(0)
{
}

Subscription::Subscription(const Subscription& subscription)
{
    _oid = subscription._oid;
    _component = subscription._component;
    _uri = subscription._uri;
    _callId = subscription._callId;
    _contact = subscription._contact;
    _notifyCseq = subscription._notifyCseq;
    _subscribeCseq = subscription._subscribeCseq;
    _eventTypeKey = subscription._eventTypeKey;
    _eventType = subscription._eventType;
    _id = subscription._id;
    _toUri = subscription._toUri;
    _fromUri = subscription._fromUri;
    _key = subscription._key;
    _recordRoute = subscription._recordRoute;
    _accept = subscription._accept;
    _file = subscription._file;
    _version = subscription._version;
    _expires = subscription._expires;
}

Subscription::Subscription(
    const UtlString& component,
    const UtlString& uri,
    const UtlString& callId,
    const UtlString& contact,
    unsigned int expires,
    unsigned int subscribeCseq,
    const UtlString& eventTypeKey,
    const UtlString& eventType,
    const UtlString& id,
    const UtlString& toUri,
    const UtlString& fromUri,
    const UtlString& key,
    const UtlString& recordRoute,
    unsigned int notifyCseq,
    const UtlString& accept,
    unsigned int version)
{
    _component = component.str();
    _uri = uri.str();
    _callId = callId.str();
    _contact = contact.str();
    _notifyCseq = notifyCseq;
    _subscribeCseq = subscribeCseq;
    _eventTypeKey = eventTypeKey;
    _eventType = eventType;
    _id = id;
    _toUri = toUri;
    _fromUri = fromUri;
    _key = key;
    _recordRoute = recordRoute;
    _accept = accept;
    _version = version;
    _expires = expires;
}

Subscription::Subscription(const bsoncxx::document::view& bson)
{
    operator=(bson);
}

Subscription::~Subscription()
{
}

Subscription& Subscription::operator=(const Subscription& subscription)
{
    Subscription clonable(subscription);
    swap(clonable);
    return *this;
}

Subscription& Subscription::operator=(const bsoncxx::document::view& bsonObj)
{
    // Extract ObjectId (_id)
    bsoncxx::document::element idElem = bsonObj[Subscription::id_fld()];
    if (idElem && idElem.type() == bsoncxx::type::k_oid)
    {
        _oid = std::string(idElem.get_oid().value.to_string());
    }

    // Extract string fields
    bsoncxx::document::element componentElem = bsonObj[Subscription::component_fld()];
    if (componentElem && componentElem.type() == bsoncxx::type::k_string)
    {
        _component = std::string(componentElem.get_string().value);
    }

    bsoncxx::document::element uriElem = bsonObj[Subscription::uri_fld()];
    if (uriElem && uriElem.type() == bsoncxx::type::k_string)
    {
        _uri = std::string(uriElem.get_string().value);
    }

    bsoncxx::document::element callIdElem = bsonObj[Subscription::callId_fld()];
    if (callIdElem && callIdElem.type() == bsoncxx::type::k_string)
    {
        _callId = std::string(callIdElem.get_string().value);
    }

    bsoncxx::document::element contactElem = bsonObj[Subscription::contact_fld()];
    if (contactElem && contactElem.type() == bsoncxx::type::k_string)
    {
        _contact = std::string(contactElem.get_string().value);
    }

    // Extract integer fields
    bsoncxx::document::element notifyCseqElem = bsonObj[Subscription::notifyCseq_fld()];
    if (notifyCseqElem && notifyCseqElem.type() == bsoncxx::type::k_int32)
    {
        _notifyCseq = notifyCseqElem.get_int32().value;
    }

    bsoncxx::document::element subscribeCseqElem = bsonObj[Subscription::subscribeCseq_fld()];
    if (subscribeCseqElem && subscribeCseqElem.type() == bsoncxx::type::k_int32)
    {
        _subscribeCseq = subscribeCseqElem.get_int32().value;
    }

    // Extract expiration time
    bsoncxx::document::element expiresElem = bsonObj[Subscription::expires_fld()];
    if (expiresElem)
    {
        if (expiresElem.type() == bsoncxx::type::k_date)
        {
            _expires = static_cast<unsigned int>(expiresElem.get_date().to_int64() / 1000);
        }
        else if (expiresElem.type() == bsoncxx::type::k_int32)
        {
            _expires = static_cast<unsigned int>(expiresElem.get_int32().value);
        }
        else if (expiresElem.type() == bsoncxx::type::k_int64)
        {
            _expires = static_cast<unsigned int>(expiresElem.get_int64().value);
        }
        else
        {
            OS_LOG_ERROR(FAC_SIP, "Unsupported type for Subscription::expires field");
        }
    }

    return *this;
}

void Subscription::swap(Subscription& subscription)
{
    std::swap(_oid, subscription._oid);
    std::swap(_component, subscription._component);
    std::swap(_uri, subscription._uri);
    std::swap(_callId, subscription._callId);
    std::swap(_contact, subscription._contact);
    std::swap(_notifyCseq, subscription._notifyCseq);
    std::swap(_subscribeCseq, subscription._subscribeCseq);
    std::swap(_eventTypeKey, subscription._eventTypeKey);
    std::swap(_eventType, subscription._eventType);
    std::swap(_id, subscription._id);
    std::swap(_toUri, subscription._toUri);
    std::swap(_fromUri, subscription._fromUri);
    std::swap(_key, subscription._key);
    std::swap(_recordRoute, subscription._recordRoute);
    std::swap(_accept, subscription._accept);
    std::swap(_file, subscription._file);
    std::swap(_version, subscription._version);
    std::swap(_expires, subscription._expires);
}
