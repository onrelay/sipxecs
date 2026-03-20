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


#include <algorithm>
#include <bsoncxx/document/view.hpp>

#include "sipdb/EntityRecord.h"

//
// Field names
//
const char* EntityRecord::oid_fld(){ static std::string fld = "_id"; return fld.c_str(); }
const char* EntityRecord::userId_fld(){ static std::string fld = "uid"; return fld.c_str(); }
const char* EntityRecord::identity_fld(){ static std::string fld = "ident"; return fld.c_str(); }
const char* EntityRecord::realm_fld(){ static std::string fld = "rlm"; return fld.c_str(); }
const char* EntityRecord::password_fld(){ static std::string fld = "pstk"; return fld.c_str(); }
const char* EntityRecord::pin_fld(){ static std::string fld = "pntk"; return fld.c_str(); }
const char* EntityRecord::authType_fld(){ static std::string fld = "authtp"; return fld.c_str(); }
const char* EntityRecord::location_fld(){ static std::string fld = "loc"; return fld.c_str(); }
const char* EntityRecord::permission_fld(){ static std::string fld = "prm"; return fld.c_str(); }
const char* EntityRecord::allowed_locations_fld(){ static std::string fld = "locations"; return fld.c_str(); }
const char* EntityRecord::associated_locations_fld(){ static std::string fld = "loc_assoc"; return fld.c_str(); }
const char* EntityRecord::associated_location_fallback_fld(){ static std::string fld = "loc_assoc_fallback"; return fld.c_str(); }
const char* EntityRecord::inbound_associated_locations_fld(){ static std::string fld = "loc_assoc_inbound"; return fld.c_str(); }
const char* EntityRecord::entity_fld(){ static std::string fld = "ent"; return fld.c_str(); }
const char* EntityRecord::authc_fld(){ static std::string fld = "authc"; return fld.c_str(); }

const char* EntityRecord::callerId_fld(){ static std::string fld = "clrid"; return fld.c_str(); }
const char* EntityRecord::callerIdEnforcePrivacy_fld(){ static std::string fld = "blkcid"; return fld.c_str(); }
const char* EntityRecord::callerIdIgnoreUserCalleId_fld(){ static std::string fld = "ignorecid"; return fld.c_str(); }
const char* EntityRecord::callerIdTransformExtension_fld(){ static std::string fld = "trnsfrmext"; return fld.c_str(); }
const char* EntityRecord::callerIdExtensionLength_fld(){ static std::string fld = "kpdgts"; return fld.c_str(); }
const char* EntityRecord::callerIdExtensionPrefix_fld(){ static std::string fld = "pfix"; return fld.c_str(); }

const char* EntityRecord::aliases_fld(){ static std::string fld = "als"; return fld.c_str(); }
const char* EntityRecord::aliasesId_fld(){ static std::string fld = "id"; return fld.c_str(); }
const char* EntityRecord::aliasesContact_fld(){ static std::string fld = "cnt"; return fld.c_str(); }
const char* EntityRecord::aliasesRelation_fld(){ static std::string fld = "rln"; return fld.c_str(); }
const char* EntityRecord::callForwardTime_fld(){ static std::string fld = "cfwdtm"; return fld.c_str(); }       
const char* EntityRecord::staticUserLoc_fld(){ static std::string fld = "stc"; return fld.c_str(); }
const char* EntityRecord::staticUserLocEvent_fld(){ static std::string fld = "evt"; return fld.c_str(); }
const char* EntityRecord::staticUserLocContact_fld(){ static std::string fld = "cnt"; return fld.c_str(); }
const char* EntityRecord::staticUserLocFromUri_fld(){ static std::string fld = "from"; return fld.c_str(); }
const char* EntityRecord::staticUserLocToUri_fld(){ static std::string fld = "to"; return fld.c_str(); }
const char* EntityRecord::staticUserLocCallId_fld(){ static std::string fld = "cid"; return fld.c_str(); }
const char* EntityRecord::loc_restr_dom_fld(){ static std::string fld = "loc_restr_dom"; return fld.c_str(); }
const char* EntityRecord::loc_restr_sbnet_fld(){ static std::string fld = "loc_restr_sbnet"; return fld.c_str(); }
const char* EntityRecord::entity_branch_str(){ static std::string fld = "branch"; return fld.c_str(); }

const char* EntityRecord::vmOnDnd_fld(){ static std::string fld = "vmondnd"; return fld.c_str(); };

EntityRecord::EntityRecord()
{
    _callForwardTime = 0;
    _vmOnDnd = false;
}

EntityRecord::EntityRecord(const EntityRecord& entity)
{
    _oid = entity._oid;
    _userId = entity._userId;
    _identity = entity._identity;
    _realm = entity._realm;
    _password = entity._password;
    _pin = entity._pin;
    _authType = entity._authType;
    _location = entity._location;
    _permissions = entity._permissions;
    _allowedLocations = entity._allowedLocations;
    _associatedLocations = entity._associatedLocations;
    _associatedLocationFallback = entity._associatedLocationFallback;
    _inboundAssociatedLocations = entity._inboundAssociatedLocations;
    _entity = entity._entity;
    _authc = entity._authc;
    _callerId = entity._callerId;
    _aliases = entity._aliases;
    _callForwardTime = entity._callForwardTime;
    _staticUserLoc = entity._staticUserLoc;
    _vmOnDnd = entity._vmOnDnd;
    _locRestrDom = entity._locRestrDom;
    _locRestrSbnet = entity._locRestrSbnet;
}

EntityRecord::~EntityRecord()
{
}

EntityRecord& EntityRecord::operator=(const EntityRecord& entity)
{
    EntityRecord clonable(entity);
    swap(clonable);
    return *this;
}

void EntityRecord::swap(EntityRecord& entity)
{
    std::swap(_oid, entity._oid);
    std::swap(_userId, entity._userId);
    std::swap(_identity, entity._identity);
    std::swap(_realm, entity._realm);
    std::swap(_password, entity._password);
    std::swap(_pin, entity._pin);
    std::swap(_authType, entity._authType);
    std::swap(_location, entity._location);
    std::swap(_permissions, entity._permissions);
    std::swap(_allowedLocations, entity._allowedLocations);
    std::swap(_associatedLocations, entity._associatedLocations);
    std::swap(_associatedLocationFallback, entity._associatedLocationFallback);
    std::swap(_inboundAssociatedLocations, entity._inboundAssociatedLocations);
    std::swap(_entity, entity._entity);
    std::swap(_authc, entity._authc);
    std::swap(_callerId, entity._callerId);
    std::swap(_aliases, entity._aliases);
    std::swap(_callForwardTime, entity._callForwardTime);
    std::swap(_staticUserLoc, entity._staticUserLoc);
    std::swap(_vmOnDnd, entity._vmOnDnd);
    std::swap(_locRestrDom, entity._locRestrDom);
    std::swap(_locRestrSbnet, entity._locRestrSbnet);
}

void EntityRecord::fillStaticUserLoc(EntityRecord::StaticUserLoc& userLoc,
                                     const bsoncxx::document::view& innerObj)
{
    bsoncxx::document::element el;

    el = innerObj[EntityRecord::staticUserLocEvent_fld()];
    if (el && el.type() == bsoncxx::type::k_string)
        userLoc.event = std::string(el.get_string().value);

    el = innerObj[EntityRecord::staticUserLocContact_fld()];
    if (el && el.type() == bsoncxx::type::k_string)
        userLoc.contact = std::string(el.get_string().value);

    el = innerObj[EntityRecord::staticUserLocFromUri_fld()];
    if (el && el.type() == bsoncxx::type::k_string)
        userLoc.fromUri = std::string(el.get_string().value);

    el = innerObj[EntityRecord::staticUserLocToUri_fld()];
    if (el && el.type() == bsoncxx::type::k_string)
        userLoc.toUri = std::string(el.get_string().value);

    el = innerObj[EntityRecord::staticUserLocCallId_fld()];
    if (el && el.type() == bsoncxx::type::k_string)
        userLoc.callId = std::string(el.get_string().value);
}

EntityRecord& EntityRecord::operator =(const bsoncxx::document::view& document)
{
    // ---- required ----
    _oid = std::string(document[EntityRecord::oid_fld()].get_string().value);

    // ---- simple fields ----
    {
        bsoncxx::document::element el;

        el = document[EntityRecord::userId_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _userId = std::string(el.get_string().value);

        el = document[EntityRecord::identity_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _identity = std::string(el.get_string().value);

        el = document[EntityRecord::realm_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _realm = std::string(el.get_string().value);

        el = document[EntityRecord::password_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _password = std::string(el.get_string().value);

        el = document[EntityRecord::pin_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _pin = std::string(el.get_string().value);

        el = document[EntityRecord::authType_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _authType = std::string(el.get_string().value);

        el = document[EntityRecord::location_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _location = std::string(el.get_string().value);

        el = document[EntityRecord::callForwardTime_fld()];
        if (el && el.type() == bsoncxx::type::k_int32)
            _callForwardTime = el.get_int32().value;

        el = document[EntityRecord::vmOnDnd_fld()];
        if (el && el.type() == bsoncxx::type::k_bool)
            _vmOnDnd = el.get_bool().value;
    }

    // ---- loc_restr_dom ----
    {
        bsoncxx::document::element el = document[EntityRecord::loc_restr_dom_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _locRestrDom.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _locRestrDom.push_back(std::string(it->get_string().value));
            }
        }
    }

    // ---- loc_restr_sbnet ----
    {
        bsoncxx::document::element el = document[EntityRecord::loc_restr_sbnet_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _locRestrSbnet.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _locRestrSbnet.push_back(std::string(it->get_string().value));
            }
        }
    }

    // ---- callerId ----
    {
        bsoncxx::document::element el_id = document[EntityRecord::callerId_fld()];
        if (el_id) {
            if (el_id.type() == bsoncxx::type::k_string)
                _callerId.id = std::string(el_id.get_string().value);

            bsoncxx::document::element el = document[EntityRecord::callerIdEnforcePrivacy_fld()];
            if (el && el.type() == bsoncxx::type::k_bool)
                _callerId.enforcePrivacy = el.get_bool().value;

            el = document[EntityRecord::callerIdIgnoreUserCalleId_fld()];
            if (el && el.type() == bsoncxx::type::k_bool)
                _callerId.ignoreUserCalleId = el.get_bool().value;

            el = document[EntityRecord::callerIdTransformExtension_fld()];
            if (el && el.type() == bsoncxx::type::k_bool)
                _callerId.transformExtension = el.get_bool().value;

            el = document[EntityRecord::callerIdExtensionLength_fld()];
            if (el && el.type() == bsoncxx::type::k_int32)
                _callerId.extensionLength = el.get_int32().value;

            el = document[EntityRecord::callerIdExtensionPrefix_fld()];
            if (el && el.type() == bsoncxx::type::k_string)
                _callerId.extensionPrefix = std::string(el.get_string().value);

            _callerId.type = (_userId == "~~gw") ? "gateway" : "user";
        }
    }

    // ---- permissions ----
    {
        bsoncxx::document::element el = document[EntityRecord::permission_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _permissions.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _permissions.insert(std::string(it->get_string().value));
            }
        }
    }

    // ---- allowedLocations ----
    {
        bsoncxx::document::element el = document[EntityRecord::allowed_locations_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _allowedLocations.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _allowedLocations.insert(std::string(it->get_string().value));
            }
        }
    }

    // ---- associatedLocations ----
    {
        bsoncxx::document::element el = document[EntityRecord::associated_locations_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _associatedLocations.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _associatedLocations.insert(std::string(it->get_string().value));
            }
        }
    }

    // ---- inboundAssociatedLocations ----
    {
        bsoncxx::document::element el = document[EntityRecord::inbound_associated_locations_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _inboundAssociatedLocations.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_string)
                    _inboundAssociatedLocations.insert(std::string(it->get_string().value));
            }
        }
    }

    // ---- simple string fields ----
    {
        bsoncxx::document::element el;

        el = document[EntityRecord::entity_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _entity = std::string(el.get_string().value);

        el = document[EntityRecord::authc_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _authc = std::string(el.get_string().value);

        el = document[EntityRecord::associated_location_fallback_fld()];
        if (el && el.type() == bsoncxx::type::k_string)
            _associatedLocationFallback = std::string(el.get_string().value);
    }

    // ---- aliases ----
    {
        bsoncxx::document::element el = document[EntityRecord::aliases_fld()];
        if (el && el.type() == bsoncxx::type::k_array) {
            bsoncxx::array::view arr = el.get_array().value;
            _aliases.clear();
            for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                if (it->type() == bsoncxx::type::k_document) {
                    bsoncxx::document::view inner = it->get_document().view();
                    Alias alias;

                    bsoncxx::document::element e;
                    e = inner[EntityRecord::aliasesId_fld()];
                    if (e && e.type() == bsoncxx::type::k_string)
                        alias.id = std::string(e.get_string().value);

                    e = inner[EntityRecord::aliasesContact_fld()];
                    if (e && e.type() == bsoncxx::type::k_string)
                        alias.contact = std::string(e.get_string().value);

                    e = inner[EntityRecord::aliasesRelation_fld()];
                    if (e && e.type() == bsoncxx::type::k_string)
                        alias.relation = std::string(e.get_string().value);

                    _aliases.push_back(alias);
                }
            }
        }
    }

    // ---- staticUserLoc ----
    {
        bsoncxx::document::element el = document[EntityRecord::staticUserLoc_fld()];
        if (el) {
            _staticUserLoc.clear();
            if (el.type() == bsoncxx::type::k_array) {
                bsoncxx::array::view arr = el.get_array().value;
                for (bsoncxx::array::view::const_iterator it = arr.begin(); it != arr.end(); ++it) {
                    if (it->type() == bsoncxx::type::k_document) {
                        bsoncxx::document::view inner = it->get_document().view();
                        StaticUserLoc userLoc;
                        fillStaticUserLoc(userLoc, inner);
                        _staticUserLoc.push_back(userLoc);
                    }
                }
            }
            else if (el.type() == bsoncxx::type::k_document) {
                bsoncxx::document::view inner = el.get_document().view();
                StaticUserLoc userLoc;
                fillStaticUserLoc(userLoc, inner);
                _staticUserLoc.push_back(userLoc);
            }
        }
    }

    return *this;
}