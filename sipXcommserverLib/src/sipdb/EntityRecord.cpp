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

void EntityRecord::fillStaticUserLoc(EntityRecord::StaticUserLoc& userLoc, const bsoncxx::document::view& innerObj) {

    bsoncxx::document::element eventElement = innerObj[EntityRecord::staticUserLocEvent_fld()];
    if (eventElement && eventElement.type() == bsoncxx::type::k_string) {
        userLoc.event = std::string(eventElement.get_string().value);
    }

    bsoncxx::document::element contactElement = innerObj[EntityRecord::staticUserLocContact_fld()];
    if (contactElement && contactElement.type() == bsoncxx::type::k_string) {
        userLoc.contact = std::string(contactElement.get_string().value);
    }

    bsoncxx::document::element fromUriElement = innerObj[EntityRecord::staticUserLocFromUri_fld()];
    if (fromUriElement && fromUriElement.type() == bsoncxx::type::k_string) {
        userLoc.fromUri = std::string(fromUriElement.get_string().value);
    }

    bsoncxx::document::element toUriElement = innerObj[EntityRecord::staticUserLocToUri_fld()];
    if (toUriElement && toUriElement.type() == bsoncxx::type::k_string) {
        userLoc.toUri = std::string(toUriElement.get_string().value);
    }

    bsoncxx::document::element callIdElement = innerObj[EntityRecord::staticUserLocCallId_fld()];
    if (callIdElement && callIdElement.type() == bsoncxx::type::k_string) {
        userLoc.callId = std::string(callIdElement.get_string().value);
    }

}


EntityRecord& EntityRecord::operator =(const bsoncxx::document::view& document)
{
    // Access the fields from the document
    _oid = std::string(document[EntityRecord::oid_fld()].get_string().value);

    if (document[EntityRecord::userId_fld()])
    {
        _userId = std::string(document[EntityRecord::userId_fld()].get_string().value);
    }

    if (document[EntityRecord::identity_fld()])
    {
        _identity = std::string(document[EntityRecord::identity_fld()].get_string().value);
    }

    if (document[EntityRecord::realm_fld()])
    {
        _realm = std::string(document[EntityRecord::realm_fld()].get_string().value);
    }

    if (document[EntityRecord::password_fld()])
    {
        _password = std::string(document[EntityRecord::password_fld()].get_string().value);
    }

    if (document[EntityRecord::pin_fld()])
    {
        _pin = std::string(document[EntityRecord::pin_fld()].get_string().value);
    }

    if (document[EntityRecord::authType_fld()])
    {
        _authType = std::string(document[EntityRecord::authType_fld()].get_string().value);
    }

    if (document[EntityRecord::location_fld()])
    {
        _location = std::string(document[EntityRecord::location_fld()].get_string().value);
    }

    if (document[EntityRecord::callForwardTime_fld()])
    {
        _callForwardTime = document[EntityRecord::callForwardTime_fld()].get_int32().value;
    }

    if (document[EntityRecord::vmOnDnd_fld()])
    {
        _vmOnDnd = document[EntityRecord::vmOnDnd_fld()].get_bool().value;
    }

    // Handling array fields like loc_restr_dom_fld
    if (document[EntityRecord::loc_restr_dom_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::loc_restr_dom_fld()].get_array();
        _locRestrDom.clear();
        for (bsoncxx::array::element item : array_elem) {
            _locRestrDom.push_back(std::string(item.get_string().value));
        }
    }

    // Handling array fields like loc_restr_sbnet_fld
    if (document[EntityRecord::loc_restr_sbnet_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::loc_restr_sbnet_fld()].get_array();
        _locRestrSbnet.clear();
        for (bsoncxx::array::element item : array_elem) {
            _locRestrSbnet.push_back(std::string(item.get_string().value));
        }
    }

    // Handling embedded object like callerId_fld
    if (document[EntityRecord::callerId_fld()]) {
        bsoncxx::document::view caller_id_document = document[EntityRecord::callerId_fld()].get_document();
        _callerId.id = std::string(caller_id_document[EntityRecord::callerId_fld()].get_string().value);
        _callerId.enforcePrivacy = caller_id_document[EntityRecord::callerIdEnforcePrivacy_fld()].get_bool().value;
        _callerId.ignoreUserCalleId = caller_id_document[EntityRecord::callerIdIgnoreUserCalleId_fld()].get_bool().value;
        _callerId.transformExtension = caller_id_document[EntityRecord::callerIdTransformExtension_fld()].get_bool().value;
        _callerId.extensionLength = caller_id_document[EntityRecord::callerIdExtensionLength_fld()].get_int32().value;
        _callerId.extensionPrefix = std::string(caller_id_document[EntityRecord::callerIdExtensionPrefix_fld()].get_string().value);
        _callerId.type = (_userId == "~~gw") ? "gateway" : "user";
    }

    // Handling permissions (set of strings)
    if (document[EntityRecord::permission_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::permission_fld()].get_array();
        _permissions.clear();
        for (bsoncxx::array::element item : array_elem) {
            _permissions.insert(std::string(item.get_string().value));
        }
    }

    // Handling allowed_locations_fld
    if (document[EntityRecord::allowed_locations_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::allowed_locations_fld()].get_array();
        _allowedLocations.clear();
        for (bsoncxx::array::element item : array_elem) {
            _allowedLocations.insert(std::string(item.get_string().value));
        }
    }

    // Handling associated_locations_fld
    if (document[EntityRecord::associated_locations_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::associated_locations_fld()].get_array();
        _associatedLocations.clear();
        for (bsoncxx::array::element item : array_elem) {
            _associatedLocations.insert(std::string(item.get_string().value));
        }
    }

    // Handling inbound_associated_locations_fld
    if (document[EntityRecord::inbound_associated_locations_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::inbound_associated_locations_fld()].get_array();
        _inboundAssociatedLocations.clear();
        for (bsoncxx::array::element item : array_elem) {
            _inboundAssociatedLocations.insert(std::string(item.get_string().value));
        }
    }

    if (document[EntityRecord::entity_fld()]) {
        _entity = std::string(document[EntityRecord::entity_fld()].get_string().value);
    }

    if (document[EntityRecord::authc_fld()]) {
        _authc = std::string(document[EntityRecord::authc_fld()].get_string().value);
    }

    if (document[EntityRecord::associated_location_fallback_fld()]) {
        _associatedLocationFallback = std::string(document[EntityRecord::associated_location_fallback_fld()].get_string().value);
    }

    // Handling aliases array of embedded objects
    if (document[EntityRecord::aliases_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::aliases_fld()].get_array();
        for (bsoncxx::array::element item : array_elem) {
            bsoncxx::document::view inner_document = item.get_document();
            Alias alias;
            if (inner_document[EntityRecord::aliasesId_fld()])
                alias.id = std::string(inner_document[EntityRecord::aliasesId_fld()].get_string().value);
            if (inner_document[EntityRecord::aliasesContact_fld()])
                alias.contact = std::string(inner_document[EntityRecord::aliasesContact_fld()].get_string().value);
            if (inner_document[EntityRecord::aliasesRelation_fld()])
                alias.relation = std::string(inner_document[EntityRecord::aliasesRelation_fld()].get_string().value);
            _aliases.push_back(alias);
        }
    }

    // Handling staticUserLoc array of embedded objects
    if (document[EntityRecord::staticUserLoc_fld()]) {
        bsoncxx::array::view array_elem = document[EntityRecord::staticUserLoc_fld()].get_array();
        for (bsoncxx::array::element item : array_elem) {
            bsoncxx::document::view inner_document = item.get_document();
            StaticUserLoc userLoc;
            fillStaticUserLoc(userLoc, inner_document);
            _staticUserLoc.push_back(userLoc);
        }
    }
    else if (document[EntityRecord::staticUserLoc_fld()]) {
        bsoncxx::document::view inner_document = document[EntityRecord::staticUserLoc_fld()].get_document();
        StaticUserLoc userLoc;
        fillStaticUserLoc(userLoc, inner_document);
        _staticUserLoc.push_back(userLoc);
    }

    return *this;
}