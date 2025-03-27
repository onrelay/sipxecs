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

#include <bsoncxx/json.hpp>

#include <os/OsLogger.h>

#include "sipdb/RegBinding.h"

const char* RegBinding::identity_fld(){ static std::string fld = "identity"; return fld.c_str(); }
const char* RegBinding::uri_fld(){ static std::string fld = "uri"; return fld.c_str(); }
const char* RegBinding::callId_fld(){ static std::string fld = "callId"; return fld.c_str(); }
const char* RegBinding::contact_fld(){ static std::string fld = "contact"; return fld.c_str(); }
const char* RegBinding::binding_fld(){ static std::string fld = "binding"; return fld.c_str(); }
const char* RegBinding::qvalue_fld(){ static std::string fld = "qvalue"; return fld.c_str(); }
const char* RegBinding::instanceId_fld(){ static std::string fld = "instanceId"; return fld.c_str(); }
const char* RegBinding::gruu_fld(){ static std::string fld = "gruu"; return fld.c_str(); }
const char* RegBinding::path_fld(){ static std::string fld = "path"; return fld.c_str(); }
const char* RegBinding::shardId_fld(){ static std::string fld = "shardId"; return fld.c_str(); }
const char* RegBinding::cseq_fld(){ static std::string fld = "cseq"; return fld.c_str(); }
const char* RegBinding::expirationTime_fld(){ static std::string fld = "expirationTime"; return fld.c_str(); }
const char* RegBinding::instrument_fld(){ static std::string fld = "instrument"; return fld.c_str(); }
const char* RegBinding::localAddress_fld(){ static std::string fld = "localAddress"; return fld.c_str(); }
const char* RegBinding::timestamp_fld(){ static std::string fld = "timestamp"; return fld.c_str(); }
const char* RegBinding::expired_fld(){ static std::string fld = "expired"; return fld.c_str(); }

RegBinding::RegBinding() :
    _shardId(0),
    _cseq(0),
    _expirationTime(0),
    _timestamp(0),
    _expired(false)
{
}

RegBinding::RegBinding(const RegBinding& binding) :
  _shardId(0),
  _cseq(0),
  _expirationTime(0),
  _timestamp(0),
  _expired(false)
{
    _identity = binding._identity;
    _uri = binding._uri;
    _callId = binding._callId;
    _contact = binding._contact;
    _binding = binding._binding;
    _qvalue = binding._qvalue;
    _instanceId = binding._instanceId;
    _gruu = binding._gruu;
    _shardId = binding._shardId;
    _path = binding._path;
    _cseq = binding._cseq;
    _expirationTime = binding._expirationTime;
    _instrument = binding._instrument;
    _localAddress = binding._localAddress;
    _timestamp = binding._timestamp;
    _expired = binding._expired;
}

RegBinding::~RegBinding()
{
}

void RegBinding::swap(RegBinding& binding)
{
    std::swap(_identity, binding._identity);
    std::swap(_uri, binding._uri);
    std::swap(_callId, binding._callId);
    std::swap(_contact, binding._contact);
    std::swap(_binding, binding._binding);
    std::swap(_qvalue, binding._qvalue);
    std::swap(_instanceId, binding._instanceId);
    std::swap(_gruu, binding._gruu);
    std::swap(_shardId, binding._shardId);
    std::swap(_path, binding._path);
    std::swap(_cseq, binding._cseq);
    std::swap(_expirationTime, binding._expirationTime);
    std::swap(_instrument, binding._instrument);
    std::swap(_localAddress, binding._localAddress);
    std::swap(_timestamp, binding._timestamp);
    std::swap(_expired, binding._expired);
}

void RegBinding::fromBSONObj(const bsoncxx::document::view& bson)
{
    if (bson[identity_fld()] && bson[identity_fld()].type() == bsoncxx::type::k_string)
        _identity = std::string(bson[identity_fld()].get_string().value);

    if (bson[uri_fld()] && bson[uri_fld()].type() == bsoncxx::type::k_string)
        _uri = std::string(bson[uri_fld()].get_string().value);

    if (bson[callId_fld()] && bson[callId_fld()].type() == bsoncxx::type::k_string)
        _callId = std::string(bson[callId_fld()].get_string().value);

    if (bson[contact_fld()] && bson[contact_fld()].type() == bsoncxx::type::k_string)
        _contact = std::string(bson[contact_fld()].get_string().value);

    if (bson[binding_fld()] && bson[binding_fld()].type() == bsoncxx::type::k_string)
        _binding = std::string(bson[binding_fld()].get_string().value);

    if (bson[qvalue_fld()] && bson[qvalue_fld()].type() == bsoncxx::type::k_string)
        _qvalue = std::string(bson[qvalue_fld()].get_string().value);

    if (bson[instanceId_fld()] && bson[instanceId_fld()].type() == bsoncxx::type::k_string)
        _instanceId = std::string(bson[instanceId_fld()].get_string().value);

    if (bson[gruu_fld()] && bson[gruu_fld()].type() == bsoncxx::type::k_string)
        _gruu = std::string(bson[gruu_fld()].get_string().value);

    if (bson[shardId_fld()] && bson[shardId_fld()].type() == bsoncxx::type::k_int32)
        _shardId = bson[shardId_fld()].get_int32().value;

    if (bson[path_fld()] && bson[path_fld()].type() == bsoncxx::type::k_string)
        _path = std::string(bson[path_fld()].get_string().value);

    if (bson[cseq_fld()] && bson[cseq_fld()].type() == bsoncxx::type::k_int32)
        _cseq = bson[cseq_fld()].get_int32().value;

    if (bson[expirationTime_fld()])
    {
        auto expirationTimeElement = bson[expirationTime_fld()];
        if (expirationTimeElement.type() == bsoncxx::type::k_date)
        {
            _expirationTime = static_cast<unsigned long>(expirationTimeElement.get_date().to_int64() / 1000); // Convert milliseconds to seconds
        }
        else if (expirationTimeElement.type() == bsoncxx::type::k_int64 ||
                 expirationTimeElement.type() == bsoncxx::type::k_double)
        {
            _expirationTime = static_cast<unsigned long>(expirationTimeElement.get_int64());
            OS_LOG_WARNING(FAC_SIP, "RegBinding::fromBSONObj found old-style registration"
                << " Identity: " << _identity
                << " Contact: " << _contact
                << " Call-Id: " << _callId);
        }
        else
        {
            OS_LOG_ERROR(FAC_SIP, "RegBinding::fromBSONObj "
                "unsupported BSON element for registration"
                << " Identity: " << _identity
                << " Contact: " << _contact
                << " Call-Id: " << _callId);
        }
    }

    if (bson[instrument_fld()] && bson[instrument_fld()].type() == bsoncxx::type::k_string)
        _instrument = std::string(bson[instrument_fld()].get_string().value);

    if (bson[localAddress_fld()] && bson[localAddress_fld()].type() == bsoncxx::type::k_string)
        _localAddress = std::string(bson[localAddress_fld()].get_string().value);

    if (bson[timestamp_fld()] && bson[timestamp_fld()].type() == bsoncxx::type::k_int64)
        _timestamp = bson[timestamp_fld()].get_int64().value;

    if (bson[expired_fld()] && bson[expired_fld()].type() == bsoncxx::type::k_bool)
        _expired = bson[expired_fld()].get_bool().value;
}

bsoncxx::document::value RegBinding::toBSONObj() const
{
    bsoncxx::builder::basic::document builder;

    builder.append(bsoncxx::builder::basic::kvp(std::string(timestamp_fld()), static_cast<int64_t>(_timestamp)));
    builder.append(bsoncxx::builder::basic::kvp(std::string(localAddress_fld()), _localAddress));
    builder.append(bsoncxx::builder::basic::kvp(std::string(identity_fld()), _identity));
    builder.append(bsoncxx::builder::basic::kvp(std::string(uri_fld()), _uri));
    builder.append(bsoncxx::builder::basic::kvp(std::string(callId_fld()), _callId));
    builder.append(bsoncxx::builder::basic::kvp(std::string(contact_fld()), _contact));
    builder.append(bsoncxx::builder::basic::kvp(std::string(binding_fld()), _binding));
    builder.append(bsoncxx::builder::basic::kvp(std::string(qvalue_fld()), _qvalue));
    builder.append(bsoncxx::builder::basic::kvp(std::string(instanceId_fld()), _instanceId));
    builder.append(bsoncxx::builder::basic::kvp(std::string(gruu_fld()), _gruu));
    builder.append(bsoncxx::builder::basic::kvp(std::string(shardId_fld()), _shardId));
    builder.append(bsoncxx::builder::basic::kvp(std::string(path_fld()), _path));
    builder.append(bsoncxx::builder::basic::kvp(std::string(cseq_fld()), _cseq));
    builder.append(bsoncxx::builder::basic::kvp(std::string(expirationTime_fld()), 
        bsoncxx::types::b_date(MongoDB::BaseDB::dateFromSecsSinceEpoch(_expirationTime))));
    builder.append(bsoncxx::builder::basic::kvp(std::string(instrument_fld()), _instrument));
    builder.append(bsoncxx::builder::basic::kvp(std::string(expired_fld()), _expired));

    return builder.extract();
}

RegBinding::RegBinding(const bsoncxx::document::view& bson) :
  _shardId(0),
  _cseq(0),
  _expirationTime(0),
  _timestamp(0),
  _expired(false)
{
  fromBSONObj(bson);
}

RegBinding& RegBinding::operator=(const bsoncxx::document::view& bson)
{
  fromBSONObj(bson);

  return *this;
}

RegBinding& RegBinding::operator=(const RegBinding& binding)
{
  RegBinding clonable(binding);
  swap(clonable);
  return *this;
}


