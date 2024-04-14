.. index:: REST API Reference

.. _rest-api-reference:

==================
REST API Reference
==================

Overview
--------

A number of APIs have been implemented to facilitate customization in the following areas:

  * Phones
  * Phone groups
  * Gateways
  * IVR
  * DNS
  * Message Waiting Indication (MWI)
  * SIP Registrar
  * Registrations
  * Page groups
  * Park orbits
  * SIP Proxy
  * My Buddy
  * Shared Appearance Agent (SAA)
  * REST service
  * Schedules
  * Dial Plan
  * Call Detail Records (CDR)
  * Servers
  * The E911 functionality has been enhanced to provide user location of 911 callers.

About REST
~~~~~~~~~~

REST (REpresentational State Transfer) represents a new approach to systems architecture and a lightweight alternative to web services. RESTlet is the framework used by sipxconfig for exposing the RESTful API. Through the REST API you can retrieve information about an instance or make configuration changes. Requests are implemented with standard HTTP methods:

  * GET to read
  * PUT to create
  * POST to update
  * DELETE to delete

.. note::
  The REST API is served over HTTPS only to ensure data privacy.

REST base URL
~~~~~~~~~~~~~

The base URL for the REST API is usually::

  https://host.domain/sipxconfig/rest/

There are some resource URIs beneath /api instead::

  https://host.domain/sipxconfig/api/

Using REST with cURL
~~~~~~~~~~~~~~~~~~~~

`cURL <https://curl.haxx.se/>`_ is a open source linux command line application for transferring data with URLs. Below is an example of using curl to print the content of a phonebook named 'sales' to standard CSV output::

  curl -k https://superadmin:password@host.domain/sipxconfig/rest/phonebook/sales

Another example of placing a call as a regular user::

  curl -k -X PUT https://200:password@host.domain/sipxconfig/rest/call/{phonenumber}

The HTTP **PUT** to the service URL indicates sipxconfig should place a call to {phonenumber}. The call can only be placed with authorized user credentials. It works in the same was as the click-to-call functionality in the User Portal. The user phone will ring first, then when answered the system places a call to {phonenumber}, then connects those two calls together. The SIP signaling is similar to a consultative/attended transfer.

OpenFire APIs
~~~~~~~~~~~~~

Openfire is a cross platform realtime communication server project based on the XMPP (Jabber) protocol developed by `Ignite Realtime <https://www.igniterealtime.org/>`_. The `OpenFire REST API documentation <https://www.igniterealtime.org/projects/openfire/plugins/1.2.2/restAPI/readme.html>`_ is available on their site.

Auto Attendant (AA)
-------------------

View AA List
~~~~~~~~~~~~

**Resource URI**: /rest/auto-attendant

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *autoattendant*
    - The items displayed in the list
  * - *name*
    - Auto attendant name
  * - *systemId*
    - System ID
  * - *specialSelected*
    - Determines whether the AA is active or not.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the list of auto-attendants configured.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/rest/auto-attendant/
  <autoAttendants>
    <autoAttendant>
      <name>Operator</name>
      <systemId>operator</systemId>
      <specialSelected>false</specialSelected>
    </autoAttendant>
    <autoAttendant>
      <name>After hours</name>
      <systemId>afterhour</systemId>
      <specialSelected>false</specialSelected>
    </autoAttendant>

**Unsupported HTTP Methods:** POST, PUT, DELETE

View or modify AA special mode
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/auto-attendant/specialmode

**Default Resource Properties**
  The resource is represented by the following properties when the GET is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *specialMode*
    - The status of the AA special mode. Displays **true** if the AA is on and **false** if the AA is off.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Displays if the auto attendant is activated or not.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/rest/auto-attendant/specialmode
  <specialAttendant>
    <specialMode>false</specialMode>

**HTTP Method:** PUT
  The status is set to true and the special mode is activated.

**Example**::

  # curl -k -X PUT -H "Content-Type: application/json" -d '{"specialMode":"true"}'  https://superadmin:password@192.168.1.31/sipxconfig/rest/auto-attendant/specialmode

**HTTP Method:** DELETE
  The status is set to false and the special mode is deactivated.

**Example**::

  # curl -k -X DELETE -H "Content-Type: application/json" -d '{"specialMode":"true"}'  https://superadmin:password@192.168.1.31/sipxconfig/rest/auto-attendant/specialmode

**Unsupported HTTP Methods:** POST

Setting an AA in special mode
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/auto-attendant/{attendant}/special

**Default Resource Properties** N/A

**Specific Response Codes:**
  * Error 400 - when the {attendant} is not found on PUT or DELETE.
  * Error 409 - when the special mode is true on DELETE.

**HTTP Method:** PUT
  The auto attendant is marked as special.

**HTTP Method:** DELETE
  Remove the attendant special mode.

**Unsupported HTTP Method:** GET, POST

Enable an AA
~~~~~~~~~~~~

**Resource URI:** /rest/auto-attendant/livemode/{code}

**Default Resource Properties** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  The auto attendant with the specified code is enabled. Note that hte code represents the phones extension.

**HTTP Method:** DELETE
  The auto attendant with the specified code is disabled. Note that hte code represents the phones extension.

**Unsupported HTTP Method:** GET, POST

Branch
------

View or modify branches
~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/branch

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *totalResults*
    - The total number of results.
  * - *currentPage*
    - Number of the current page.
  * - *totalPages*
    - The number of total pages.
  * - *resultPerPage*
    - Number of results per page.
  * - *ID*
    - Unique identification number of the branch.
  * - *name*
    - Branch name
  * - *description*
    - Short description provided by the user.
  * - *address*
    - The complete address of the branch.
  * - *street*
    - The name of the street.
  * - *city*
    - The name of the city.
  * - *country*
    - The name of the country.
  * - *state*
    - The name of the state.
  * - *zip*
    - The postal zip code.
  * - *officeDesignation*
    - The mail stop field.
  * - *phoneNumber*
    - The phone number of the branch.
  * - *faxNumber*
    - The fax number of the branch.

**Specific Response Codes:**
  Error 400 - Wrong ID when updating the branch

**HTTP Method:** GET
  Retrieves a list of branches defined in the system.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/rest/branch
  <branch>
    <metadata>
      <totalResults>1</totalResults>
      <currentPage>1</currentPage>
      <totalPages>1</totalPages>
      <resultsPerPage>1</resultsPerPage>
    </metadata>
    <branches>
      <branch>
        <id>1</id>
        <name>whitehouse</name>
        <description>location description field</description>
        <address>
          <id>1</id>
          <street>1600 Pennsylvania Avenue NW</street>
          <city>Washington</city>
          <country>US</country>
          <state>DC</state>
          <zip>20500</zip>
        </address>
      </branch>
    </branches>
  </branch>

**HTTP Method:** PUT
  Adds a new branch. The ID is automatically generated and any value entered is ignored.

**Example**::

  # curl -k -X PUT -H "Content-Type: application/json" -d '{"branch":{"name":"libofcongress","description":"library of congress","address":{"street":"101 Independence Ave SE","city":"Washington","state":"DC","zip":"20540"}}}'  https://superadmin:password@192.168.1.31/sipxconfig/rest/branch
  <?xml version="1.0" encoding="UTF-8"?><response><code>SUCCESS_CREATED</code><message>Created</message><data><id>3</id></data></response>

**Unsupported HTTP Method:** DELETE

View or modify a branch ID
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/branch/{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *branch*
    - The branch information is the same as /branch

**Specific Response Codes:**
  Error 400 - wrong ID when updating the branch

**HTTP Method:** GET
  Retrieves information on the branch with the specified ID.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/rest/branch/1
  <branch>
    <id>1</id>
    <name>whitehouse</name>
    <description>location description field</description>
    <address>
      <id>1</id>
      <street>1600 Pennsylvania Avenue NW</street>
      <city>Washington</city>
      <country>US</country>
      <state>DC</state>
      <zip>20500</zip>
      <officeDesignation>ovaloffice</officeDesignation>
    </address>
    <phoneNumber>4235551212</phoneNumber>
    <faxNumber>4235552323</faxNumber>
  </branch>

**HTTP Method:** PUT
  Updates the branch with the specified ID. Uses the same XML as for creation.

**HTTP Method:** DELETE
  Removes branch with the specified ID.

**Unsupported HTTP Method:** POST

DNS
---

View DNS settings
~~~~~~~~~~~~~~~~~

**Resource URI:** /api/dns/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - Path to the setting.
  * - *type*
    - Setting type. Possible options are **string**, **boolean**, or **enum**.
  * - *options*
    - Availble setting options.
  * - *value*
    - The current selected option of the setting.
  * - *defaultValue*
    - The default value of the setting.
  * - *label*
    - The setting label.
  * - *description*
    - Short description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the settings for all DNS entries in the system.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/dns/settings
  {"settings":[{"path":"named-config/forwarders/forwarder_0","type":"string","options":null,"value":"192.168.1.31","defaultValue":null,"label":"Primary External DNS server","description":"DNS server in your company or your ITSP. Can also be a publicly available DNS server like 8.8.8.8."},{"path":"named-config/forwarders/forwarder_1","type":"string","options":null,"value":null,"defaultValue":null,"label":"Secondary External DNS server","description":"In the event the primary DNS server is unavailable, system will use this server."},{"path":"named-config/forwarders/forwarder_2","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null},{"path":"named-config/forwarders/forwarder_3","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null},{"path":"named-config/forwarders/forwarder_4","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null},{"path":"acl/ips","type":"string","options":null,"value":"192.168.1.31,172.16.0.0/12,192.168.0.0/16,10.0.0.0/8,127.0.0.0/8","defaultValue":"192.168.1.31,172.16.0.0/12,192.168.0.0/16,10.0.0.0/8,127.0.0.0/8","label":"Allow Recursion ACL","description":"Groups of hosts (comma separated values of IP addresses or subnet) allowed to make recursive queries on the nameserver. <br/>Leave empty for allowing all hosts to perform recursive queries on the nameserver."},{"path":"sys/unmanaged","type":"boolean","options":null,"value":"0","defaultValue":"0","label":"Unmanaged Service","description":"Company or ITSP DNS servers to resolve ALL names instead of local DNS servers."},{"path":"sys/unmanaged_servers/unmanaged_0","type":"string","options":null,"value":null,"defaultValue":null,"label":"Primary Unmanaged DNS server","description":"DNS server in your company or your ITSP. Can also be a publicly available DNS server like 8.8.8.8."},{"path":"sys/unmanaged_servers/unmanaged_1","type":"string","options":null,"value":null,"defaultValue":null,"label":"Secondary Unmanaged DNS server","description":"In the event the primary DNS server is unavailable, system will use this server."},{"path":"sys/unmanaged_servers/unmanaged_2","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional Unmanaged DNS server","description":null},{"path":"sys/unmanaged_servers/unmanaged_3","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional Unmanaged DNS server","description":null}]}

View DNS settings from path
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/dns/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *setting*
    - The dns setting related information is similar to the one described under /dns/settings.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the DNS settings from the speicifed path.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/dns/settings/named-config/forwarders
  {"settings":[{"path":"named-config/forwarders/forwarder_0","type":"string","options":null,"value":"192.168.1.31","defaultValue":null,"label":"Primary External DNS server","description":"DNS server in your company or your ITSP. Can also be a publicly available DNS server like 8.8.8.8."},{"path":"named-config/forwarders/forwarder_1","type":"string","options":null,"value":null,"defaultValue":null,"label":"Secondary External DNS server","description":"In the event the primary DNS server is unavailable, system will use this server."},{"path":"named-config/forwarders/forwarder_2","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null},{"path":"named-config/forwarders/forwarder_3","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null},{"path":"named-config/forwarders/forwarder_4","type":"string","options":null,"value":null,"defaultValue":null,"label":"Additional External DNS server","description":null}]}

**HTTP Method:** PUT
  Updates the settings of the DNS server from the specified path. PUT data is plain text.

**HTTP Method:** DELETE
  Deletes the settings of the DNS server from the specified path.

**Unsupported HTTP Method:** POST

View DNS Advisor results
~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/dns/advisor/server/{serverId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *Missing naptr records*
    - List of the missing NAPTR records, if any.
  * - *Missing A records*
    - List of missing A records, if any.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Checks the DNS settings and if the settings are correct, no result is returned. Otherwise it retrieves the missing configurations.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/dns/advisor/server/1

  ;; Missing naptr records
  home.mattkeys.net. IN NAPTR \d 0 "s" "SIP+D2U" "" _sip._udp
  home.mattkeys.net. IN NAPTR 1 0 "s" "SIP+D2T" "" _sip._tcp

  ;; Missing a records
  sipxcom1.home.mattkeys.net  IN A 192.168.1.31

**Unsupported HTTP Method:** PUT, POST, DELETE

e911 (eZuce Uniteme only)
-------------------------

This feature and API resource only applies to eZuce Uniteme. There is a workaround for sipxcom to get a similar result.

.. note::

  The workaround isn't valid when using sipXbridge / SIP trunks because the webui will prevent adding multiple trunks to the same provider (IP or FQDN).

If using unmanaged gateways the workaround is to force outbound caller ID on the gateways utilized in the emergency dial plan.
You can create multiple unmanaged gateways that point to the same IP address.
Branch configurations can then be used to specify the outbound gateway, and permissions can then be used to secure the dial plan entries.

For example, a building with four floors could be configured as four branches -- floor1, floor2, floor3, and floor4.
Four gateways pointing to the same IP would be created for those four floors -- e911floor1, e911floor2, e911floor3, and e911floor4.
On each gateway specify the respective branch, and do not configure the gateways as "shared". Next force the outbound caller ID on each gateway, which is the ELIN that corresponds to the floor.
Finally configure users in their respective floor branch, and add those 4 gateways to the emergency dial plan.

.. note::

  It's a good idea to have a shared gateway as the last option in the emergency dial plan gateway list as a failsafe / last resort path.

About e911
~~~~~~~~~~
The Enhanced 911 (E911) functionality has been implemented for handling emergency situations. Administrators can perform the required set up in order for Uniteme and Unite users to be able to call the 911 number when needed. The functionality uses location based technology to pin point the location of 911 callers and connect them to the appropriate public resources.

The system to automatically associates a location with the origin of the call. This location may be a physical address or other geographic reference information such as X/Y GPS coordinates. In sipXcom administrators are able to define physical locations and link them to users. Physical locations have a DID/ELIN (Emergency Location Identification Number) that will be sent out to the 911 dispatcher. Based on the called ID sent operators will be able to dispatch emergency services directly to the user's location.

.. note::

  * E911 is a system used only in North America.
  * Calls made to other emergency telephone numbers are not supported.

Using the e911 REST API
~~~~~~~~~~~~~~~~~~~~~~~
sipXcom also defines a REST API to perform CRUD operations on the Emergency Resource Location (ERL) table and also to link users to locations. This API may be used by third parties in order to update the ERL data in the PS-ALI database (Private Switch/Automatic Location Identification). It also helps administrators update in bulk the locations table and link users to locations.
The following resources for the E911 API are only available for users with administration rights:

**Emergency Resource Location (ERL)**

  * View list of ERLs
  * Filter ERLs by ELIN
  * Filter ERLs by user name
  * Filter ERLs by user groups
  * Filter ERLs by the number of assigned phones
  * Update ERLs for one or multiple phones
  * Update ERLs for one or multiple phone groups

**Registrations**

  * View registrations for an IP
  * View registrations for a Line/Extension

**Phones**

  * View list of phones
  * View list of phones changed since dd/mm/yy

Emergency Resource Location (ERL)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erls

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *elin*
    - ELIN number.
  * - *location*
    - Caller location.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returns a list with all the ERLs defined in the system.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.14/sipxconfig/rest/erls
  
  <?xml version="1.0" encoding="UTF-8"?><e911Locations><e911Location><location>123 Test Street, Chattanooga, TN, 37412</location><elin>4235551212</elin><addressInfo>123 Test Street, Chattanooga, TN, 37412</addressInfo><description>test</description></e911Location></e911Locations>

**HTTP Method:** PUT
  Save a list of ERLs.

**Example**::

  bar

**HTTP Method:** DELETE
  Delete the ERL with the specified ELIN

**Unsupported HTTP Method:** POST

Filter ERLs by ELIN
~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/elin/{elin}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *elin*
    - ELIN number.
  * - *location*
    - Caller location.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returns the ERLs with the specified ELIN.

**Example**::

  foo

**HTTP Method:** PUT
  Update the ERL with the specified ELIN

**HTTP Method:** DELETE
  Delete the ERL with the specified ELIN

**Unsupported HTTP Method:** POST

Filter ERLs by user name
~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/user/{username}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *location*
    - The location
  * - *elin*
    - The ELIN number.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returns the ERL linked to the user identified by username. Data is plain text and represents the ELIN of the ERL.

**Example**::

  foo

**HTTP Method:** PUT
  Update the ERL of the user. PUT data is plain text and represents the ERL.

**Example**::

  bar

**HTTP Method:** DELETE
  Set the user ERL to none.

**Example**::

  foo

**Unsupported HTTP Method:** POST

Filter ERLs by user groups
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/group/{groupName}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *location*
    - The location
  * - *elin*
    - The ELIN number.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returns the ERL identified with the user group.

**Example**::

  foo

**HTTP Method:** PUT
  Updates the ERL of the user group.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Filter ERLs by the number of assigned phones
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/phone/{serial_number}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *location*
    - The location
  * - *elin*
    - The ELIN number.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.
  * - *serial*
    - phone serial

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list with the locations for the phone(s).

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

Update ERLs for one or multiple phones
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/phones

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Error 400 if wrong ELIN or serial is specified.

**HTTP Method:** PUT
  Update locations for one or more phones.

**Example**::

  bar

**Unsupported HTTP Method:** GET, POST, DELETE

Update ERLs for one or multiple phone groups
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /rest/erl/phonegroup/{groupName}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *location*
    - The location
  * - *elin*
    - The ELIN number.
  * - *addressInfo*
    - Address details.
  * - *description*
    - Optional description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves locations for phone groups.

**Example**::

  foo

**HTTP Method:** PUT
  Updates locations for phone groups.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes location for phone groups.

**Unsupported HTTP Method:** POST

Gateways
--------

View all gateways
~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways

**Default Resource Properties**
  The resource is prepresented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Gateway unique identification number.
  * - *name*
    - Gateway name.
  * - *description*
    - Short description provided by the user.
  * - *model*
    - The model of the gateway.
  * - *enabled*
    - Displays **true** if enabled, **false** if it is disabled.
  * - *address*
    - The gateway IP or FQDN address.
  * - *addressPort*
    - The gateway port number.
  * - *outboundPort*
    - The gateway outbound port number.
  * - *addressTransport*
    - The transport protocol to use.
  * - *shared*
    - Displays **true** if enabled, **false** if it is not shared.
  * - *useInternalBridge*
    - Displays **true** if using sipxbridge, **false** if it is not.
  * - *anonymous*
    - Displays **true** if caller ID is blocked, **false** if it is not.
  * - *ignoreUserInfo*
    - Displays **true** if 'ignore user caller id' is enabled, **false** if it is not.
  * - *transformUserExtensions*
    - Displays **true** if 'transform extension' is enabled, **false** if it is not.
  * - *keepDigits*
    - Number of ext digits that are kept before adding the caller ID prefix.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all the gateways defined.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/gateways
  {"gateways":[{"id":1,"name":"my_siptrunk","serialNo":null,"deviceVersion":null,"description":"DIDs 4235550000 through 4235551000","model":{"modelId":"sipTrunkStandard","label":"SIP trunk","vendor":null,"versions":null},"enabled":true,"address":"192.168.1.14","addressPort":5060,"outboundAddress":null,"outboundPort":5060,"addressTransport":"tcp","prefix":null,"shared":true,"useInternalBridge":true,"branch":null,"callerAliasInfo":{"defaultCallerAlias":null,"anonymous":false,"ignoreUserInfo":false,"transformUserExtension":false,"addPrefix":null,"keepDigits":0,"displayName":null,"urlParameters":null}}]}

Filter gateways by model
~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways/models

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *modelId*
    - Gateway model.
  * - *label*
    - Gateway label.
  * - *vendor*
    - Gateway model vendor.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all gateway models available in the database.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/gateways/models
  {"models":[{"modelId":"acmeGatewayStandard","label":"Acme 1000","vendor":"acme","versions":null},{"modelId":"audiocodesMP1X4_4_FXO","label":"AudioCodes MP114 FXO","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesMP1X8_8_FXO","label":"AudioCodes MP118 FXO","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesMediant1000","label":"AudioCodes Mediant 1000 PRI","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesMediant2000","label":"AudioCodes Mediant 2000 PRI","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesMediant3000","label":"AudioCodes Mediant 3000 PRI","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesMediantBRI","label":"AudioCodes Mediant 1000 BRI","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"audiocodesTP260","label":"AudioCodes TP260","vendor":"AudioCodes","versions":["audiocodes6.0","audiocodes5.8","audiocodes5.6","audiocodes5.4","audiocodes5.2","audiocodes5.0"]},{"modelId":"genericGatewayStandard","label":"Unmanaged gateway","vendor":null,"versions":null},{"modelId":"sipTrunkStandard","label":"SIP trunk","vendor":null,"versions":null}]}

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify gateway ID
~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /gateways/{gatewayId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *gateway*
    - The gateway related inforamtion is the same as the /gateways resource.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on the gateway with the specified ID.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/gateways/1
  {"id":1,"name":"my_siptrunk","serialNo":null,"deviceVersion":null,"description":"DIDs 4235550000 through 4235551000","model":{"modelId":"sipTrunkStandard","label":"SIP trunk","vendor":null,"versions":null},"enabled":true,"address":"192.168.1.14","addressPort":5060,"outboundAddress":null,"outboundPort":5060,"addressTransport":"tcp","prefix":null,"shared":true,"useInternalBridge":true,"branch":null,"callerAliasInfo":{"defaultCallerAlias":null,"anonymous":false,"ignoreUserInfo":false,"transformUserExtension":false,"addPrefix":null,"keepDigits":0,"displayName":null,"urlParameters":null}}

**HTTP Method:** PUT
  Updates the gateway with the specified ID. Uses the same XML as for creation.

**Example**::

  foo


**HTTP Method:** POST
  Creates a new gateway with the specified ID.

**Example**::

  bar

**HTTP Method:** DELETE
  Removes the gateway specified by ID.

**Example**::

  foo

**Unsupported HTTP Method:** N/A

View all settings of a gateway ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways/{gatewayId}/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - Path to the setting.
  * - *type*
    - Setting type. Possible options are **string**, **boolean**, or **enum**.
  * - *options*
    - Available setting options.
  * - *value*
    - The current selected option of the setting.
  * - *label*
    - Setting label.
  * - *description*
    - Short description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves settings for the specified gateway ID.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/gateways/2/settings
  {"settings":[{"path":"itsp-account/user-name","type":"string","options":null,"value":"5568","defaultValue":null,"label":null,"description":null},{"path":"itsp-account/authentication-user-name","type":"string","options":null,"value":null,"defaultValue":null,"label":null,"description":null},{"path":"itsp-account/password","type":"string","options":null,"value":"password","defaultValue":null,"label":null,"description":null},{"path":"itsp-account/register-on-initialization","type":"boolean","options":null,"value":"true","defaultValue":"false","label":null,"description":null},{"path":"itsp-account/itsp-proxy-address","type":"string","options":null,"value":"192.168.1.14","defaultValue":"192.168.1.14","label":null,"description":null},{"path":"itsp-account/use-global-addressing","type":"boolean","options":null,"value":"true","defaultValue":"true","label":null,"description":null},{"path":"itsp-account/strip-private-headers","type":"boolean","options":null,"value":"true","defaultValue":"false","label":null,"description":null},{"path":"itsp-account/default-asserted-identity","type":"boolean","options":null,"value":"false","defaultValue":"true","label":null,"description":null},{"path":"itsp-account/asserted-identity","type":"string","options":null,"value":"5568","defaultValue":null,"label":null,"description":null},{"path":"itsp-account/is-from-itsp","type":"boolean","options":null,"value":"true","defaultValue":null,"label":null,"description":null},{"path":"itsp-account/default-preferred-identity","type":"boolean","options":null,"value":"false","defaultValue":"false","label":null,"description":null},{"path":"itsp-account/preferred-identity","type":"string","options":null,"value":"5568@192.168.1.14","defaultValue":null,"label":null,"description":null},{"path":"itsp-account/is-user-phone","type":"boolean","options":null,"value":"true","defaultValue":"true","label":null,"description":null},{"path":"itsp-account/itsp-registrar-address","type":"string","options":null,"value":null,"defaultValue":null,"label":null,"description":null},{"path":"itsp-account/itsp-registrar-listening-port","type":"integer","options":null,"value":null,"defaultValue":null,"label":null,"description":null},{"path":"itsp-account/registration-interval","type":"integer","options":null,"value":"600","defaultValue":"600","label":null,"description":null},{"path":"itsp-account/sip-session-timer-interval-seconds","type":"integer","options":null,"value":"1800","defaultValue":"1800","label":null,"description":null},{"path":"itsp-account/sip-keepalive-method","type":"enum","options":{"CR-LF":null,"NONE":null},"value":"CR-LF","defaultValue":"CR-LF","label":null,"description":null},{"path":"itsp-account/rtp-keepalive-method","type":"enum","options":{"REPLAY-LAST-SENT-PACKET":null,"USE-DUMMY-RTP-PAYLOAD":null,"USE-EMPTY-PACKET":null,"NONE":null},"value":"NONE","defaultValue":"NONE","label":null,"description":null},{"path":"itsp-account/route-by-to-header","type":"boolean","options":null,"value":"false","defaultValue":"false","label":null,"description":null},{"path":"itsp-account/always-relay-media","type":"boolean","options":null,"value":"true","defaultValue":"true","label":null,"description":null}]}

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify a setting for a gateway ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways/{gatewayId}/settings/{path:.*}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *gateway*
    - The gateway related information is the same as the /gateways/{gatewayId} resource.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve the setting specified in the path for the gateway ID.

**Example**::

  foo

**HTTP Method:** PUT
  Updates the setting specified in the path for the gateway ID. PUT data is plain text.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the setting specified in the path for the gateway ID.

**Example**::

  foo

**Unsupported HTTP Method:** POST

View port settings for a gateway ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways/{gatewayId}/port/{portId}/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *setting*
    - The port setting related information is similar to the one described under /gateways/{gatewayId}/settings.

**HTTP Method:** GET
  View port settings for the gateway with the specified ID.

**Example**::

  bar

**HTTP Method:** PUT
  Updates the port settings for the gateway with the specified ID. PUT data is plain text.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the port settings for the gateway with the specified ID.

**Example**::

  bar

**Unsupported HTTP Method:** POST

View or modify port settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /api/gateways/{gatewayId}/port/{portId}/settings/{path:.*}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *setting*
    - The port setting related information is similar to the one described under /gateway/settings.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the port settings of the gateway with the specified ID.

**Example**::

  foo

**HTTP Method:** PUT
  Updates the settings of the port. PUT data is plain text.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the settings of the port.

**Example**::

  foo

**Unsupported HTTP Method:** POST

IVRs
----

View IVR Settings
~~~~~~~~~~~~~~~~~

**Resource URI:** /ivr/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - Path to the setting
  * - *type*
    - The setting type. Possible options are **string**, **boolean**, or **enum**.
  * - *options*
    - Available setting options.
  * - *value*
    - The current selected option of the setting.
  * - *defaultValue*
    - The default value of the setting.
  * - *label*
    - The setting label.
  * - *description*
    - The description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all the IVR settings.

**Example**::

  bar

**HTTP Method:** PUT
  Updates the settings of the gateway. PUT data is plain text.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the settings of the gateway.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Licensing (Uniteme only)
------------------------

**Resource URI:** /api/license

**Default Resource Properties** N/A

**Specific Response Codes:** N/A

View license information
~~~~~~~~~~~~~~~~~~~~~~~~

**HTTP Method:** GET
  Retrieves the current license file.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.14/sipxconfig/api/license/
  {"uid":"","domain":"home.mattkeys.net","version":"","expire":"03-Jan-2026","support":"","licenseType":"Deprecated OpenUC Reach","users":-1,"mobileDevices":-1,"company":"Matts Lab","contactEmail":"mkeys@email.domain","contactName":"","contactPhone":""}

Modify license UID
~~~~~~~~~~~~~~~~~~
Set the universal ID of the license.

**HTTP Method:** POST
  Used to update the license UID

**Example**::

  # curl -k -X POST https://superadmin:12345678@10.4.0.103/sipxconfig/api/license/508316691110519
  {"uid":"508316691110519","domain":"gabi.test","version":"19.08","expire":"31-Dec-2039","support":"31-Dec-2039","licenseType":"Subscription","users":500,"mobileDevices":500,"company":"developers","contactEmail":"martin.harcar@ezuce.com","contactName":"martin","contactPhone":"454614465465"}

**Unsupported HTTP Method:** PUT, DELETE

Message Waiting Indication (MWI)
--------------------------------

**Resource URI:** /mwi/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - 
  * - *type*
    - 
  * - *value*
    - 
  * - *defaultValue*
    - 
  * - *description*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all MWI settings.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify MWI settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /mwi/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all MWI settings of the specified path.

**Example**::

  bar

**HTTP Method:** PUT
  Updates the MWI settings of the specified path. PUT data is plain text.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the MWI setting from the specified path.

**Example**::

  bar


Music On Hold (MOH)
-------------------

View MOH settings
~~~~~~~~~~~~~~~~~

**Resource URI:** /moh/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *dnsManager*
    - Name of the DNS manager
  * - dnsTestContext
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all MOH settings.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/moh/settings
  {"settings":[{"path":"moh-config/MOH_SOURCE","type":"enum","options":{"FILES_SRC":"System Music Directory","NONE":"None","SOUNDCARD_SRC":"Sound Card"},"value":"FILES_SRC","defaultValue":"FILES_SRC","label":"Music On Hold Source","description":"Selects the source of the on hold music. If set to <em>System Music Directory</em> the server will play all the music files from the system directory on a continuous rotating basis. Setting it to <em>Sound Card</em> will stream audio from the local sound card. <em>None</em> option will disable music on hold."}]}

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify MOH settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /moh/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - The moh source
  * - *type*
    - 
  * - *options*
    - Group of options defined for the moh source

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the MOH settings for the specified path.

**Example**::

  # curl -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/moh/settings/moh-config/MOH_SOURCE
  {"path":"moh-config/MOH_SOURCE","type":"enum","options":{"FILES_SRC":"System Music Directory","NONE":"None","SOUNDCARD_SRC":"Sound Card"},"value":"FILES_SRC","defaultValue":"FILES_SRC","label":"Music On Hold Source","description":"Selects the source of the on hold music. If set to <em>System Music Directory</em> the server will play all the music files from the system directory on a continuous rotating basis. Setting it to <em>Sound Card</em> will stream audio from the local sound card. <em>None</em> option will disable music on hold."}

**HTTP Method:** PUT
  Updates the MOH settings of the specified path. PUT data is plain text.

**Example**::

  bar

**HTTP Method:** DELETE
  Reverts the setting to the default value.

**Unsupported HTTP Method:** POST

View or upload MOH prompt files
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /moh/prompts

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *dnsManager*
    - Name of the DNS Manager
  * - *dnsTestContext*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all MOH prompt files.

**Example**::

  foo

**HTTP Method:** POST
  Uploads a new MOH prompt file.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, DELETE

Download MOH prompt files
~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /moh/prompts/{promptName}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - 
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Downloads the MOH prompt based on the file name. Example: */moh/prompts/default.wav*.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the specified prompt message.

**Unsupported HTTP Method:** PUT, POST

Stream to MOH Prompt
~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /moh/prompts/{promptName}/stream

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Can be called by clients to stream prompts.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST, DELETE

My Buddy
--------

View My Buddy Settings
~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /imbot/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table:: 

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all My Buddy settings defined.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify My Buddy settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /imbot/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves My Buddy options for the setting from the specified path.

**Example**::

  bar

**HTTP Method:** PUT
  Modifies My Buddy options for the specified path.

**Example**::

  foo

**Unsupported HTTP Method:** POST

Page Groups
-----------

View or create page groups
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *ID*
    - Unique identification number of the page group
  * - *enabled*
    - The status of the page group. Displays **true** if enabled, **false** if disabled.
  * - *GroupNumber*
    - The group number of the page group.
  * - *timeout*
    - The timeout value measured in seconds.
  * - *sound*
    - Name of the file representing the sound to be played.
  * - *description*
    - Short description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all the page groups defined in the system.

**Example**::

  bar

**HTTP Method:** POST
  Creates a new page group.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, DELETE

View or modify page groups by group ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups/{pagegroupId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *ID*
    - Unique identification number of the page group.
  * - *enabled*
    - The status of the page group. Displays **true** if enabled, **false** if disabled.
  * - *GroupNumber*
    - The group number of the page group.
  * - *timeout*
    - The timeout value measured in seconds.
  * - *sound*
    - Name of the file representing the sound to be played.
  * - *description*
    - Short description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves page groups with the specified ID.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies a page group with the specified ID.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the page group specified by ID.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST

Manage page group services
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - The path of the page group
  * - *type*
    - The type of the page group. Possible options are **string**, **boolean**, or **enum**.
  * - *value*
    - The value of the field.
  * - *defaultValue*
    - The default value of the field.
  * - *label*
    - The label of the page group.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves options for the page groups in the system.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies options for the page groups in the system.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the paging group specified by path.

**Unsupported HTTP Method:** PUT, POST

View or create new prompt message
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups/prompts

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of page group prompts.

**Example**::

  foo

**HTTP Method:** POST
  Uploads a new page group prompt message.

**Unsupported HTTP Method:** PUT, DELETE

Download page group prompts
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups/prompts/{promptName}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Downloads prompt specified by file name.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

Stream the page group prompt
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /pagegroups/prompts/{promptName}/stream

**Default Resource Properties:** N/A

**Specific Resource Codes:** N/A

**HTTP Method:** GET
  Start the data stream of {promptName}.

**Unsupported HTTP Method:** PUT, POST, DELETE

Park Orbits
-----------

View park orbits
~~~~~~~~~~~~~~~~

**Resource URI:** /orbits

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Downloads prompts based upon file name.

**Example**::

  foo

**HTTP Method:** POST
  Uploads a park orbit prompt based upon file name.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, DELETE

View or modify park orbits
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/{orbitId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the park orbit settings of the specified ID.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies the park orbit settings of the specified ID.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the park orbit specified by ID.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST

Manage park orbit options
~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/{orbitId}/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves service options for the specified park orbit ID.

**Example**::

  bar

**HTTP Method:** PUT
  Modifies service options for the specified park orbit ID.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the options for the specified park orbit ID.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

Manage the park orbit service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/{orbitId}/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves general service options.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies the general service options.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes the service options.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST

View or create new prompt message
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/prompts

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of orbit prompts.

**Example**::

  bar

**HTTP Method:** POST
  Uploads a new prompt message.

**Unsupported HTTP Method:** PUT, DELETE

Download park orbit prompts
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/prompts/{promptName}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Downloads the prompt of the specified file name.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST

Stream the park orbit prompt
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /orbits/prompts/{promptName}/stream

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST, DELETE

Permissions
-----------

View or create permissions
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /permission

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *totalResults*
    - The total number of results.
  * - *currentPage*
    - The page you are currently viewing.
  * - *totalPages*
    - The number of total pages.
  * - *resultsPerPage*
    - Number of permissions displayed on each page.
  * - *name*
    - Name of the permission.
  * - *label*
    - Alternative name of the description.
  * - *defaultValue*
    - Default value. Displays **true** if enabled, **false** if disabled.
  * - *type*
    - The type. Possible options are **string**, **boolean**, or **enum**.
  * - *builtIn*
    - 

**Filtering Parameters:**


.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional, forward or reverse
  * - *sortby*
    - Optional, name or description

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all permissions

**Example**::

  foo

**HTTP Method:** PUT
  Adds a new permission. You must specify a body representing a new permission using the following template::

  <permission>
  <name>perm_7</name>
  <label>apicreate</label>
  <description>Created through the api</description>
  <defaultValue>true</defaultValue>
  </permission>

.. note::
  Any new permissions created will not have any call permissions applied.

**Unsupported HTTP Method:** POST, DELETE

View or modify a permission ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /permission/{name}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *permission*
    - The permissions related information similar to /permission.

**Specific Response Codes:**
  Error 400 when {name} is not found or invalid

**HTTP Method:** GET
  Retrieves information on the permission specified by ID

**Example**::

  foo

**HTTP Method:** PUT
  Updates permission with the specified ID. Uses the same XML as for creation.

**Example**::

  bar

**HTTP Method:** DELETE
  Removes the permission specified by ID.

**Example**::

  foo

**Unsupported HTTP Method:** POST

Phone
-----

Send a phone profile
~~~~~~~~~~~~~~~~~~~~

**Example**::

  # curl -k -X PUT https://superadmin:password@192.168.1.31/sipxconfig/api/phones/0004f280cc95/sendProfile/restart

Create a phone
~~~~~~~~~~~~~~

**Resource URI:** /phones

**Default Resource Properties**

.. list-table::

  * - **Property**
    - **Description**
  * - *serialNumber*
    - The serial number of the phone.
  * - *model*
    - The phone model.
  * - *description*
    - Description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information about all phones.

**Example**::

  foo

**HTTP Method:** POST
  Creates a new phone.

**Example**::

  bar

**Unsupported HTTP Methods:** PUT, DELETE

Retrieve a phone profile
~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{serialNumber}/profile/{name}

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Error 404 when {serialNumber} or {name} is not found.

**HTTP Method:** GET
  Retrieves the phone profile of the given serial number or filename

**Unsupported HTTP Methods:** POST, PUT, DELETE

View all phone models
~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/models

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *modelId*
    - Model unique ID
  * - *label*
    - Model label

**Filtering Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional. Forward or reverse.
  * - *sortby*
    - Optional. Name or description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all phone models.

**Example**::

  foo

**HTTP Method:** PUT
  Updates the settings of the gateway. PUT data is plain text.

**HTTP Method:** DELETE
  Deletes the settings of the gateway.

**Unsupported HTTP Method:** POST

View or create a phone
~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *ID*
    - Phone unique identification number.
  * - *serialNo*
    - The phone serial number.
  * - *description*
    - Short description provided by the user.
  * - *label*
    - Label of the phone model.
  * - *vendor*
    - Vendor of the phone model.
  * - *lines*
    - Lines assigned to the phone ID.
  * - *uri*
    - The URI for the instance.
  * - *user*
    - The user name.
  * - *userid*
    - The user unique identification number.
  * - *displayName*
    - The display name for the user.
  * - password
    - The SIP password of the user.
  * - *registrationServer*
    - The SIP registrar to use

**Specific Repsonse Codes:** N/A

**HTTP Method:** GET
  Retrieves all phones.

**Example**::

  foo

**HTTP Method:** POST
  Creates a new phone.

**Unsupported HTTP Method:** PUT, DELETE

View or modify a phone
~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *phone*
    - The phones information similar to /phones and /phones/models

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve details for the phone with the specified phone ID or MAC address.

**Example**::

  bar

**HTTP Method:** PUT
  Modify the phone with the specified phone ID or MAC address.

**Example**::

  foo

**HTTP Method:** DELETE
  Delete the phone with the specified phone ID or MAC address.

**Unsupported HTTP Method:** POST

View or delete phones from groups
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}/groups

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Group unique identification number.
  * - *name*
    - The group name.
  * - *description*
    - Description of the group provided by the user.
  * - *weight*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve the groups for the specified phone ID.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the groups for the specified phone ID.

**Unsupported HTTP Method:** POST, PUT

Delete or add phones in groups
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}/groups/{groupName}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** POST
  Add a phone in the specified group name.

**Example**::

  bar

**HTTP Method:** DELETE
  Delete a phone from the specified group name.

**Example**::

  foo

**Unsupported HTTP Method:** GET, PUT

View group settings
~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - Setting path
  * - *type*
    - Setting type. Possible values are string or boolean.
  * - *value*
    - 
  * - *defaultValue*
    - Default value.
  * - *label*
    - Label setting.
  * - *description*
    - Short description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the settings from the group of the specified path.

**Example**::

  foo

**HTTP Method:** PUT
  Update the setting or settings from the group of the specified path.

**Example**::

  bar

**HTTP Method:** DELETE
  Delete the setting from the group of the specified path.

**Example::**

  foo

**Unsupported HTTP Method:** POST

Delete or add lines to a phone ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}/lines

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Line unique identification number
  * - *uri*
    - The uri for the line.
  * - *user*
    -  The user name.
  * - *userId*
    - User unique identification number
  * - *password*
    - User password.
  * - *registrationServer*
    - Name of SIP registrar server
  * - *registrationServerPort*
    - The SIP registrar server port number

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve the lines for the phone with the speicifed ID.

**Example**::

  foo

**HTTP Method:** POST
  Add a new line for the phone with the specified ID.

**Example**::

  bar

**HTTP Method:** DELETE
  Delete the setting from the group specified path.

**Example**::

  foo

**Unsupported HTTP Method:** PUT

View or modify group settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phones/{phoneId}/lines/{lineId}/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *path*
    - The setting path.
  * - *type*
    - The setting type.
  * - *description*
    - The description provided by the user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve the group setting from the specified path.

**Example**::

  bar

**HTTP Method:** PUT
  Modify the group setting from the specified path.

**Example**::

  foo

**HTTP Method:** DELETE
  Delete the group setting from the specified path.

**Example**::

  bar

**Unuspported HTTP Method:** POST

Phone Book
----------

View a list of phone books
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phonebook

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *phonebooks*
    - The phonebook name.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all phone books saved in the database.

**Example**::

  foo

**Unsupported HTTP Method:** POST, PUT, DELETE

View phone book entries
~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phonebook/{name}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *firstName*
    - First name of the phone book entry.
  * - *lastName*
    - Last name of the phone book entry.
  * - *number*
    - The number associated to the phone book entry.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list with all the phone book entries saved in the database.

**Example**::

  bar

**Unsupported HTTP Method:** POST, PUT, DELETE

Phone groups
------------

View or create phone groups
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phoneGroups

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Group unique identification number.
  * - *name*
    - The group name.
  * - *description*
    - Description of the group provided by the user.
  * - *weight*
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all phone groups.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify phone groups
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /phoneGroups/{phoneGroupId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the phone group with the specified ID.

**Example**::

  bar

**HTTP Method:** PUT
  Updates the phone group. PUT data is plain text.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the phone group.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Move phone group up in ordering
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /{groupId}/up

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  Move the phone group up. PUT data is plain text.

**Example**::

  foo

**Unsupported HTTP Method:** GET, POST, DELETE

Move phone group down in ordering
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /{groupId}/down

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  Move the phone group down. PUT data is plain text.

**Example**::

  bar

**Unsupported HTTP Method:** GET, POST, DELETE

View settings for specific models in a phone group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /{groupId}/models

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *modelId*
    - Model ID
  * - *label*
    - Model label

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all phone models specified in the group.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify all settings for a phone model in a phone group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /{groupId}/model/{modelName}/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the phone group with the specified ID.

**Example**::

  bar

**HTTP Method:** PUT
  Updates the phone group. PUT data is plain text.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the phone group.

**Example**::

  bar

**Unsupported HTTP Method:** POST

View or modify one setting for a phone model in a phone group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /{groupId}/model/{modelName}/settings/{path:.*}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the setting in the specified path.

**Example**::

  foo

**HTTP Method:** PUT
  Updates the setting in the specified path. PUT data is plain text.

**Example**::

  bar

**HTTP Method:** DELETE
  Reverts the setting to the default value.

**Example**::

  foo

**Unsupported HTTP Method:** POST

Proxy
-----

View proxy settings
~~~~~~~~~~~~~~~~~~~

**Resource URI:** /proxy/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all proxy settings in the system.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify proxy settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /proxy/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves proxy settings from the specified path.

**Example**::

  bar

**HTTP Method:** PUT
  Modifies proxy options for the setting from the specified path.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes proxy options for the setting from the specified path.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Registrar
---------

View registrar settings
~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrar/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all SIP registrar settings in the system.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify registrar settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrar/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET

**Example**::

  bar

**HTTP Method:** PUT
  Modifies SIP registrar options for the specified setting path.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes SIP registrar options for the specified setting path.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Registrations
-------------

View all registrations
~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Filtering Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *start*
    - Required. The start date.
  * - *limit*
    - Required. The max number of results to be displayed.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves all registrations in the system.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

Filter registrations by users
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations/user/{userId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *ID*
    - The ID, name, or alias of the user.

**Filtering Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *start*
    - Required. The start date.
  * - *limit*
    - Required. The max number of results to be displayed.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves registrations for the specified user.

**Example**::

  foo

**HTTP Method:** DELETE
  Removes registrations of the specified user.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

Filter registrations by MAC address
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations/serialNo/{serialId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *registrations*
    - The number of registrations for the specified phone mac

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves registrations for the specified MAC address.

**Example**::

  foo

**HTTP Method:** DELETE
  Removes registrations for the specified MAC address.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

Filter regsitrations by IPs
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations/ip/{ip}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - 
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves registrations for the specified IP.

**Example**::

  foo

**HTTP Method:** DELETE
  Removes registrations of the specified IP.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

Filter registrations by Call-ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations/callId/{callId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves registrations of the specified Call-ID.

**Example**::

  foo

**HTTP Method:** DELETE
  Remove registrations of the specified Call-ID.

**Example**::

  bar


**Unsupported HTTP Method:** PUT, POST

Filter registrations by servers
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /registrations/server/{serverId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves registrations of the server based on internal ID or FQDN.

**Example**::

  foo

**HTTP Method:** DELETE
  Removes registrations for the specified server based on internal ID or FQDN.

**Example**::

  bar

**Unsupported HTTP Method:** PUT, POST

REST server
-----------

View REST server settings
~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /restserver/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all REST server settings in the system.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify REST server settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /restserver/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves REST server options of the specified path.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies REST server options for the specified path.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Schedules
---------

View all general schedules
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/general

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve all general schedules.

**Example**::

  foo

**Unsupported HTTP Method:** POST, PUT, DELETE

View all schedules for a group ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/group/{groupId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve schedules for the specified group ID.

**Example**::

  bar

**Unsupported HTTP Method:** POST, PUT, DELETE

View all schedules for a user ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/user/{userId}/all

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve all schedules for the specified user ID.

**Example**::

  foo

**Unsupported HTTP Method:** POST, PUT, DELETE

View personal schedules for a user ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/user/{userId}/personal

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve personal schedules for the specified user ID.

**Example**::

  bar

**HTTP Method:** POST
  Create a personal schedule for the specified user ID.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, DELETE

View description for a schedule ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/{scheduleId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  View the description of the specified schedule ID.

**Example**::

  bar

**HTTP Method:** PUT
  Update the description for the specified schedule ID.

**Example**::

  foo

**HTTP Method:** DELETE
  Delete the description of the specified schedule ID.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Add or remove periods to a schedule ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /schedules/{scheduleId}/period/{index}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** POST
  Create a schedule period for the specified schedule ID.

**Example**::

  foo

**HTTP Method:** DELETE
  Remove a period for the specified schedule ID.

**Example**::

  bar

**Unsupported HTTP Method:** GET, PUT

Shared Appearance Agent
-----------------------

View SAA settings
~~~~~~~~~~~~~~~~~

**Resource URI:** /saa/settings

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all SAA settings.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify SAA settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /saa/settings/{settingPath}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * -
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves a list of all SAA settings for the specified path.

**Example**::

  bar

**HTTP Method:** PUT
  Modifies SAA options for the specified path.

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes SAA options for the specified path.

**Example**::

  bar

**Unsupported HTTP Method:** POST

Users
-----

View avatar information
~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /avatar/{user}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves avatar content for the specified user.

**Example**::

  foo

**Unsupported HTTP Method:** POST, PUT, DELETE

View or modify users
~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *ID*
    - Unique identification number of the user. If specified the branch property must be blank.
  * - *username*
    - The user name used for authentication
  * - *lastName*
    - Last name of the user
  * - *firstName*
    - First name of the user
  * - *pin*
    - User voicemail PIN
  * - *sipPassword*
    - SIP password associated with the user
  * - *groups*
    - The groups the user is a member of.
  * - *branch*
    - Branch unique identification number. If specified the ID property must be blank.
  * - *alias*
    - Any aliases associated with the user.

**Filtering Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Required. Forward or reverse. If it is the only parameter used, it defaults to Name.
  * - *sortby*
    - Required. Name or description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on all users. Parameters to specify sorting are optional, but you should use both if you want sorting. If you only use the "sortdir" parameter, it defaults to "name".

**Example**::

  foo

**HTTP Method:** PUT
  Adds a new user.

**Example**::

  bar

.. note::
  * The ID is automatically generated. Any value entered will be ignored.
  * If the PIN is empty the current PIN value will be preserved.
  * The **branch**, **groups**, and **aliases** elements are optional.

**Unsupported HTTP Method:** POST, DELETE

View or modify a user ID
~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user/{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *user*
    - The user setting related information is the same as described under /user.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information about the user specified by ID.

**Example**::

  foo

**HTTP Method:** PUT
  Updates specified user ID. Uses the same XML as for creation. After an update the response data will contain an ID element with the id value of the item affected.

**Example**::

  bar

**HTTP Method:** DELETE
  Removes the user specified by ID.

**Unsupported HTTP Method:** POST

View permissions for all users
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-permission

**Default Resource Properties:** N/A

**Filtering Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional, forward or reverse.
  * - *sortby*
    - Optional, name or description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on all users and their permission settings.

**Example**::

**Unsupported HTTP Method:** POST, PUT, DELETE

View or modify permissions for a user ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-permission{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - The user ID. The value is automatically generated and any value is ignored.
  * - *lastName*
    - The last name of the user.
  * - *firstName*
    - The first name of the user.
  * - *permissions*
    - List of permissions.
  * - *setting*
    - List of settings.
  * - *name*
    - Name of the setting.
  * - *value*
    - Displays the status of the permission: **Enabled** or **Disabled**. It will be missing (empty) if the permission is set to the default and has never been changed.
  * - *defaultValue*
    - The default value of **true** or **false**, for information only. It does not need to be provided and will be ignored. Not all permissions need to be updated at once. They can be listed individually or in subgroups.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on user with the specified id and its permissions.

**Example**::

  foo

**HTTP Method:** PUT
  Sets permission values for a user.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

User Groups
-----------

View or modify user groups
~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-group

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Unique identification number of the user group.
  * - *name*
    - Name of the user group.

**Filter Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional, forward or reverse.
  * - *sortby*
    - Optional, name or description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on all the user groups.

**Example**::

  foo

**HTTP Method:** PUT
  Adds a new user group. The id is automatically generated any any value is ignored. The branch element is optional.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify a user group ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-group/{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *totalResults*
    - Number of total results.
  * - *currentPage*
    - Number of the current page.
  * - *totalPages*
    - Number of total pages.
  * - *resultsPerPage*
    - Number of results per page.
  * - *group*
    - Heading with details on the user group.
  * - *id*
    - The group ID.
  * - *name*
    - The group name.
  * - *description*
    - Description of the group

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on the user group specified by ID.

**Example**::

  foo

**HTTP Method:** PUT
  Updates group with the specified ID. Uses the same XML as for creation.

**Example**::

  bar

**HTTP Method:** DELETE
  Removes the user group specified by ID.

**Example**::

  foo

**Unsupported HTTP Method:** POST

View or modify user group permissions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-group-permission

**Default Resource Properties:** N/A

**Filter Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional, forward or reverse.
  * - *sortby*
    - Optional, name or description.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves information on all user groups and their permission settings.

**Unsupported HTTP Method:** POST, PUT, DELETE

View or modify permissions for a group ID
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Resource URI:** /user-group-permission/{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *id*
    - Group unique identification number.
  * - *name*
    - Group name.
  * - *description*
    - Description provided by the user.
  * - *name*
    - Permission name.
  * - *value*
    - Displays **true** if enabled, **false** if disabled.
  * - *defaultValue*
    - The default value.

**Filter Parameters:**

.. list-table::

  * - **Parameter**
    - **Description**
  * - *page*
    - Required. The requested page size.
  * - *pagesize*
    - Required. The number of results to be displayed per page.
  * - *sortdir*
    - Optional, forward or reverse.
  * - *sortby*
    - Optional, name or description.

**Specific Response Codes:**
  Error 400 when {id} is invalid or not found.

**HTTP Method:** GET
  Retrieves information on the user group with the specified ID and its permissions.

**Example**::

  foo

**HTTP Method:** PUT
  Sets permission values for a user group.

.. note::

  * The ID is automatically generated and any value will be ignored.
  * The setting element must contain a value element.
  * The value must be set to either enable or disable. The value element is blank if the permission is set to the default and has never been changed.
  * The defaultValue is for information only and is read-only.
  * Not all permissions need to be updated at once. THey can be listed individually or in subgroups.

**Unsupported HTTP Method:** POST, DELETE

User Services
-------------

Calls
~~~~~

Initiate Calls
^^^^^^^^^^^^^^

**Resource URI:** /my/call/{to} or /call/{to}

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Error 400 when {to} is not a valid SIP URI

**HTTP Method:** PUT
  PUT method requires a non empty body which is ignored. Supported as a GET for clients that do not handle PUT.

**Example**::

  foo

**Unsupported HTTP Method:** GET, POST, DELETE

View or modify user call forwarding
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/forward

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves user call forwarding settings without shcedules.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies user call forwarding settings without schedules.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify call forwarding (with schedules)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/callfwd

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *expiration*
    - The time measured in seconds that the phone will ring
  * - *type*
    - Options are 'if no response' (call is not forked) or 'at the same time' (call is forked)
  * - *enabled*
    - Value is **true** if the forward is enabled, **false** if it is disabled.
  * - *number*
    - The number or extension to forward to.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves user call forwarding scheme with schedules.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies user call forwarding scheme with schedules.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify call forwarding schedules
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/callfwdschedule

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *expiration*
    - The time measured in seconds that the phone will ring
  * - *type*
    - Options are 'if no response' (call is not forked) or 'at the same time' (call is forked)
  * - *enabled*
    - Value is **true** if the forward is enabled, **false** if it is disabled.
  * - *number*
    - The number or extension to forward to.

**Specific Response Codes:**
  Error 422 when schedule save or update failed.
  Error 403 on PUT or DELETE and the schedule id not specified.

**HTTP Method:** GET
  Retrieves call forwarding schedules.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies call fowarding schedules.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify a schedule ID
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/callfwdsched/{id}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *description*
    - Short description of the schedule.
  * - *periods*
    - The start and end dates of the period. The format is hours/minutes.
  * - *scheduleId*
    - The ID of the schedule.
  * - *name*
    - Alternative name of the schedule.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves call forwarding schedules.

**Example**::

  foo

**HTTP Method:** PUT
  Updates existing schedule of the specified ID.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes existing schedule of the specified ID.

**Example**::

  foo

**Unsupported HTTP Method:** POST

View active calls
^^^^^^^^^^^^^^^^^

**Resource URI:** /my/activecdrs

**Default Resource Parameters**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *from*
    - The user part of a sip uri, or pstn number
  * - *from-aor*
    - The entire From URI
  * - *to*
    - The target user part of a sip uri, or pstn number
  * - *to-aor*
    - The entire To URI
  * - *direction*
    - Direction of the call. Value is either INCOMING or OUTBOUND
  * - *recipient*
    - The user that answered the call.
  * - *internal*
    - If the call was internal extension to extension, **true** if yes, **false** if not.
  * - *type*
    - Field is read-only, for internal use
  * - *start-time*
    - Start time of the call.
  * - *duration*
    - Length of the call.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves user active calls in xml or json format.

**Example**::

  foo

**Unsupported HTTP Method:** POST, PUT, DELETE

Voicemail
~~~~~~~~~

Change voicemail PIN
^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/voicemail/pin/{pin}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *description*
    - Short description of the schedule
  * - *periods*
    - The start and end dates of the period. The format is hours/minutes.
  * - *scheduleId*
    - The ID of the schedule.
  * - *name*
    - Alternative name of the schedule.

**Specific Response Codes:**
  Error 400 on PUT when the new {pin} cannot be saved.

**HTTP Method:** GET
  Retrieves call forwarding schedules.

**Example**::

  foo

**HTTP Method:** PUT
  Updates existing schedule given {id}

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes existing schedule given {id}

**Example**::

  foo

**Unsupported HTTP Method:** POST

View or modify voicemail settings
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/vmprefs

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *voicemailPermission*
    - Determines whether the recipient of the call has the permission to receive voicemail. Value is **true** if the user has permission and **false** if not permitted.
  * - *emailformat*
    - Determines the email format type. Options are null (no email sent), full (detailed email sent), or medium.
  * - *altEmailFormat*
    - Determines the email format type for the secondary email address. Options are null, full, or medium.
  * - *greeting*
    - Voicemail prompt callers hear before leaving a message.
  * - *emailAttachType*
    - Determines whether the email has an attachment. Value is **yes** or **no**.
  * - *emailIncludeAudioAttachment*
    - Determines weather the voicemail message is attached to the email. Displays **true** or **false**.
  * - *email*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves call forwarding schedules.

**Example**::

  foo

**HTTP Method:** PUT
  Saves user voicemail settings.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View voicemail folder as a RSS feed
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/feed/voicemail/{folder}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  The voicemail folder is presented as a RSS feed.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View or modify voicemail personal attendant 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/voicemail/attendant

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *personalAttendantPermission*
    - 
  * - *language*
    - 
  * - *operator*
    - 
  * - *menu*
    - 
  * - *overrideLanguage*
    -
  * - *depositVM*
    - 
  * - *playVMDefaultOptions*
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the personal attendant settings.

**Example**::

  # curl -k -X GET https://200:11111111@localhost/sipxconfig/rest/my/voicemail/attendant
  {"personalAttendantPermission":true,"operator":"419","overrideLanguage":true,"menu":{"5":"255","7":"999"},"language":"en","depositVM":true,"forwardDeleteVM":true,"playVMDefaultOptions":true}

**HTTP Method:** PUT
  Updates the personal attendant settings.

**Example**::

  # curl -k -X PUT -H "Content-Type: application/json" -d '{"operator":"419","overrideLanguage":true,"menu":{"5":"255","7":"999"},"language":"en","depositVM":true,"forwardDeleteVM":true,"playVMDefaultOptions":true}' https://200:11111111@localhost/sipxconfig/rest/my/voicemail/attendant

**Unsupported HTTP Method:** POST, DELETE

Set operators' personal attendant settings
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/voicemail/operator/{operator}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  Sets personal attendant operator user given {operator} value.

**Example**::

  foo

**Unsupported HTTP Method:** GET, POST, DELETE

Reset operators' personal attendant settings
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/voicemail/operator

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  Resets personal attendant operator user given the {operator} value.

**Example**::

  bar

**Unsupported HTTP Method:** GET, POST, DELETE

Phonebook
~~~~~~~~~

Export phone book
^^^^^^^^^^^^^^^^^

**Resource URI** /my/phonebook

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *entry*
    - The phonebook entry
  * - *first-name*
    - Entry first name
  * - *last-name*
    - Entry last name
  * - *number*
    - 
  * - *contact-information*
    - 
  * - *homeAddress*
    - 
  * - *officeAddress*
    - 
  * - *imID*
    - 
  * - *imDisplayName*
    - 
  * - *avatar*
    - The avatar URL

**Specific Response Codes**:: N/A

**HTTP Method:** GET
  Retrieves the phonebook

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View phone book page by page
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/pagedphonebook?start={start-row}&end={end-row}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *size*
    - Total entries number
  * - *filtered-size*
    - Returned entries number
  * - *start-row*
    - First returned entry number
  * - *end-row*
    - Last returned entry number
  * - *show-on-phone*
    - 
  * - *google-domain*
    - 
  * - *entries*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returnes users from start row to end row.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

Private phone book entries
^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/phonebook/entry/{entryId}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *pb*
    - 
  * - *internalID*
    - 
  * - *uid*
    - 
  * - *vcard*
    - 
  * - *username*
    -

**Specific Response Codes:**
  Error 747 when entryid is duplicated during save (POST)

**HTTP Method:** GET
  Retrieves all the entries from the private phone book.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies entries in the private phone book.

**Example**::

  bar

**HTTP Method:** POST
  Creates a new private phone book entry

**Example**::

  foo

**HTTP Method:** DELETE
  Deletes the entry specified by ID from the private phone book.

**Example**::

  bar

**Unsupported HTTP Method:** N/A

Search for phone book contacts
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/search/phonebook?query={searchterm}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *entry*
    - The result.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Searches the phone book. Search term can be a value of any user field like firstname, lastname, extension, etc.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

Create or delete a private phone book
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/phonebookentry/{internalid}

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Error 404 when {internalid} is not found.

**HTTP Method:** PUT
  Creates a private phone book from a vcard template.

**Example**::

  bar

**HTTP Method:** DELETE
  Deletes a private phone book entry.

**Example**::

  foo

**Unsupported HTTP Method:** POST, DELETE

View contacts on display
^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/phonebook/showContactsOnPhone/{value}

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** PUT
  Marks the showContactsOnPhone flag true or false in the user private phonebook.

**Example**::

  bar

**Unsupported HTTP Method:** GET, POST, DELETE

Import Google Contacts
^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/phonebook/googleImport

**Default Resource Properties:** N/A

**Specific Response Codes:**
  *  Error 743 on POST when there is a google authentication error.
  *  Error 744 on POST when there is a google service error.
  *  Error 745 on POST when there is a google transport error.

**HTTP Method:** POST
  Imports google contacts into user private phonebook.

**Example**::

  foo

**Unsupported HTTP Method:** GET, PUT, DELETE

Preferences
~~~~~~~~~~~

View user contact information
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/contact-information

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *jobTitle*
    -
  * - *jobDept*
    -
  * - *companyName*
    -
  * - *homeAddress*
    -
  * - *officeAddress*
    -
  * - *branchAddress*
    -
  * - *imID*
    -
  * - *imDisplayName*
    -
  * - *emailAddress*
    -
  * - *useBranchAddress*
    -
  * - *salutation*
    -
  * - *twitterName*
    -
  * - *linkedinName*
    -
  * - *facebookName*
    -
  * - *xingName*
    -
  * - *timestamp*
    -
  * - *enabled*
    -
  * - *ldapManaged*
    - true if the user should be modified when importing from ldap
  * - *firstName*
    -
  * - *lastName*
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the etnries from the private phone book.

**Example**::

  foo

**HTTP Method:** PUT
  Updates user contact details.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify IM preferences
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/im/prefs

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *statusCallInfo*
    - If true include the caller info in the busy status of the XMPP message.
  * - *otpMessage*
    - The content of the message used as XMPP status when user is busy.
  * - *voicemailonDnd*
    - If true, all calls received when Do Not Disturb is set through XMPP client are forwarded directly to voicemail.
  * - *statusPhonePresence*
    - If true advertise the user's busy status in the XMPP status message.

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves instant messaging preferences

**Example**::

  foo

**HTTP Method:** PUT
  Saves the modified IM preferences.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify My Buddy preferences
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/imbot/prefs

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *confExit*
    - If true conference exit messages are sent to mybuddy
  * - *vmBegin*
    - If true notification are sent to mybuddy when callers enter the voicemail box.
  * - *vmEnd*
    - If true notifications are sent as the caller exits the voicemail box.
  * - *confEnter*
    - If true conference entry messages are sent to mybuddy

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves mybuddy preferences.

**Example**::

  foo

**HTTP Method:** PUT
  Modifies mybuddy preferences

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

View or modify speed dial preferences
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/speeddial

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *updatePhones*
    - 
  * - *canSubscribeToPresence*
    - 
  * - *buttons*
    - speeddial number, label, blf true or false
  * - *groupSpeedDial*
    - true to inherit group speed dials

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the speed dial preferences.

**Example**::

  foo

**HTTP Method:** PUT
  Saves the modified speed dial preferences.

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

Activate active greeting
^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/mailbox/{user}/preferences/activegreeting/{greeting}

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Plain text values should be none, standard, outofoffice, or extendedabsence. For other content the "none" greeting will be saved.

**HTTP Method:** PUT
  Sets active greeting setting for a specific user.

**Example**::

  foo

.. note::

  The greeting cannot be empty.

**Unsupported HTTP Method:** GET, POST, DELETE

Hunt Groups
~~~~~~~~~~~

Get all hunt groups
^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X GET https://superadmin:password@192.168.1.31/sipxconfig/api/callgroups

Get all huntgroups that have extension starting with a prefix
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X GET https://superadmin:password@localhost/sipxconfig/api/callgroups/prefix/33


Get call group given extension
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X GET https://superadmin:password@localhost/sipxconfig/api/callgroups/3399

Create a hunt group
^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X POST -H "Content-Type: application/json" -d '{"name":"ppp1","extension":"4444","description":"","enabled":true,"did":null,"ringBeans":[],"fallbackDestination":null,"voicemailFallback":true,"userForward":true,"useFwdTimers":false}' https://superadmin:password@localhost/sipxconfig/api/callgroups

Duplicate hunt group extension as new hunt group with a different extension
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X POST  https://superadmin:password@localhost/sipxconfig/api/callgroups/3399/duplicate/55665

Rotate rings for hunt group
^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X POST  https://superadmin:password@localhost/sipxconfig/api/callgroups/3399/rotate/3

Update hunt group with extension
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X PUT -H "Content-Type: application/json" -d '{"name":"ppp1","extension":"4444","description":"kkkkk","enabled":true,"did":null,"ringBeans":[],"fallbackDestination":null,"voicemailFallback":true,"userForward":true,"useFwdTimers":false}' https://superadmin:password@localhost/sipxconfig/api/callgroups/4444

Delete hunt group
^^^^^^^^^^^^^^^^^

**Example**::

  curl -v -k -X DELETE https://superadmin:password@localhost/sipxconfig/api/callgroups/3399
  
Conferences
~~~~~~~~~~~

Filter conferences for a user ID
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/conferences

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *conferences*
    - 
  * - *enabled*
    - 
  * - *name*
    - 
  * - *description*
    - 
  * - *extension*
    - 
  * - *accessCode*
    -

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Returns a list of all conferences for a specific user.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View conference details
^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/conferencedetails/{confName}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed.

.. list-table::

  * - **Property**
    - **Description**
  * - *conference*
    - 
  * - *extension*
    - 
  * - *locked*
    - 
  * - *members*
    - 
  * - *id*
    - 
  * - *name*
    - 
  * - *imID*
    - 
  * - *uuid*
    - 
  * - *volumeIn*
    - 
  * - *energyLevel*
    - 
  * - *canHear*
    - 
  * - *canSpeak*
    - 

**Specific Response Codes:**
  * Error 404 when {confName} is not found.
  * Error 403 when authenticated user is not the owner of {confName}
  * Error 406 when {confName} is found but not active (no participants)

**HTTP Method:** GET
  Returns conference details including participant details.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View conference settings for all users
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/conferences

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *conference*
    - 
  * - *enabled*
    - 
  * - *description*
    - 
  * - *extension*
    - 
  * - *accessCode*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Gets user conference settings for all user owned conferences.

**Example**::

  foo

**Unsupported HTTP Method:** POST, DELETE

View user conference details
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/conferences/{name}

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *enabled*
    - 
  * - *named*
    - 
  * - *autoRecord*
    - 
  * - *quickStart*
    - 
  * - *video*
    - 
  * - *sendActiveVideoOnly*
    - 
  * - *maxMembers*
    - 
  * - *moh*
    - 
  * - *moderatedRoom*
    - 
  * - *publicRoom*
    - 

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieve user conference details of {name}

**Example**::

  foo

**HTTP Method:** PUT
  Saves user conference details of {name}

**Example**::

  bar

**Unsupported HTTP Method:** POST, DELETE

FreeSWITCH Conference Commands
------------------------------

About Conference Services
~~~~~~~~~~~~~~~~~~~~~~~~~
The Conference Web Services APIs allow the administrator to send commands to the FreeSWITCH platform.

Base URL
~~~~~~~~
The base URL for the Conference web services is ::

  https://username:password@host.domain/sipxconfig/rest/my/conference/{conference-name}

For the above URL you can use the /{command}&{arg 1}&{arg 2}... URL with the PUT HTTP method to send the desired commands and arguments to FreeSwitch.

**Available FreeSWITCH commands and arguments**

.. list-table::

  * - **Command Name**
    - **Command Details**
    - **Usage**
  * - *bgdial*
    -
    - <endpoint_module_name>/<destination> <callerid number> <callerid name>
  * - *deaf*
    - Make a conference member deaf.
    - <[member_id|all]|last>
  * - *dial*
    - Dial a destination via a specific endpoint.
    - <endpoint_module_name>/<destination> <callerid number> <callerid name>
  * - *dtmf*
    - Send DTMF to any member of the conference.
    - <[member_id|all|last]> <digits>
  * - *energy*
    - Adjusts the conference energy level for a specific member.
    - <member_id|all|last> [<newval>]
  * - *hup*
    - Kick without the kick sound.
    - conference <confname> hup <[member_id|all|last]>
  * - *kick*
    - Kicks a specific member form a conference.
    - <[member_id|all|last]>
  * - *list*
    - Lists all or a specific conference members.
    - conference list [delim <string>]
  * - *lock*
    - Lock a conference so no new members will be allowed to enter.
    - lock
  * - *mute*
    - Mutes a specific member in a conference.
    - <[member_id|all]|last>
  * - *norecord*
    - Remove recording for a specific conference.
    - <[filename|all]>
  * - *nopin*
    - Removes a pin for a specific conference.
    - nopin
  * - *pin*
    - Sets or changes a pin number for a specific conference. Note: if you set a conference pin and then issue a command like conference <confname> dial sofia/default/123456@softswitch, 123456 will not be challenged with a pin but he will just joins the conference named <confname>.
    - <pin#>
  * - *play*
    - Play an audio file in a conference to all members or to a specific member. You can stop that same audio with the stop command below.
    - <file_path> [async|<member_id>]
  * - *record*
    -
    - <filename>
  * - *relate*
    - Mute or Deaf a specific member to another member.
    - <member_id> <other_member_id> [nospeak|nohear|clear]
  * - *say*
    - Write a message to all members in the conference.
    - <text>
  * - *saymember*
    - Write a messaget to a specific member in a conference.
    - <member_id> <text>
  * - *stop*
    - Stops any queued audio from playing.
    - <[current|all|async|last]> [<member_id>]
  * - *transfer*
    - Transfer a member from one conference to another conference. To transfer a member to another extension use the api transfer command with the uuid of their session.
    - <conference_name> <member id> [...<member id>]
  * - *unmute*
    - Unmute a specific member of a conference.
    - <[member_id|all]|last>
  * - *undeaf*
    - Allow a specific member to hear the conference.
    - <[member_id|all]|last>
  * - *unlock*
    - Unlock a conference so that new members can enter.
    - unlock
  * - *volume_in*
    - Adjusts the input volume for a specific conference member.
    - <member_id|all|last> [<newval>]
  * - *volume_out*
    - Adjust the output volume for a specific conference member.
    - <member_id|all|last> [<newval>]
  * - *xml_list*
    -
    -

**Specific Response Codes:**
  * Error 404 when {confName} is not found.
  * Error 403 when authenticated user is not the owner of {confName}
  * Error 400 when no {command} is specified or the command is incorrect.

Dial Additional Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~

If the caller ID values are not set, the variables set in the conference.conf.xml are used. Specifically, the value for caller-id-number is used for the number and the value for caller-id-name is used for the name. If the conference is dynamically created as a result of this API and the caller-id-number and caller-id-number is not provided in the API call then the number and name will be "00000000" and respectively "FreeSWITCH".

**Example**::

  conference testconf dial {originate_timeout=30}sofia/default/1000@softswitch 1234567890 FreeSWITCH_Conference

The above API call will dial out of a conference named "testconf" to the user located at the specified endpoint with a 30 second timeout. The endpoint will see the call as coming from "FreeSWITCH_Conference" with a caller id of 1234567890.

.. note::
  The values provided in the dial string overwrite the caller-id-number and caller-id-name variables provided at the end of the API call.

List Additional Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~

The output generated by the system is named by default with the following format::

  <conference name> (<member_count> member[s][locked]),

Where locked can represent either the locked or unlockes status of the conference.

The following items are a separated list in CSV format for each conference leg.

.. list-table::

  * - **Item**
    - **Description**
  * - *ID of participant*
    -
  * - *Register string of participants*
    -
  * - *UUID of participants call leg*
    -
  * - *Caller ID Number*
    -
  * - *Status*
    - Options are 'hear' (mute/deaf), 'speak' (deaf/undeaf), 'talking' (sound energy), 'video' (video enabled), and 'floor' (member owns the floor).
  * - *VolumeIn*
    -
  * - *VolumeOut*
    -
  * - *EnergyLevel*
    -

Related Additional Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Conference Examples**

Member 1 may now no longer speak to member 2, i.e. member 2 now cannot hear member 1 ::

  conference my_conf relate 1 2 nospeak:

Member 1 may now speak to member 2 again ::

  conference my_conf relate 1 2 clear:

Member 1 now cannot hear member 2 ::

  conference my_conf relate 1 2 nohear:

Member 1 can now hear member 2 again ::

  confernce my_conf relate 1 2 clear:

**Command Examples**

Lock a conference with name "WeeklyTeamConf" ::

  # curl -k -X PUT https://200:123@localhost/sipxconfig/rest/my/conference/WeeklyTeamConf/lock

Invite user in conference given username ::

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/invite\&401

Invite user in conference given IM ID ::

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/inviteim\&401im

Other examples ::

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/xml_list

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/kick\&all

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/record

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/record\&stop

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/record\&status

  # curl -k https://400:123@gerula-dev.buc.ro/sipxconfig/rest/my/conference/Conf400/record\&duration

Sample PHP click to call code ::

  <?php
  $to="101";//Number to dial
  $from="5001";//userid in sipx
  $pass="1234";//sipx pin (NOT SIP password)
  //replace sipx.gcgov.local with your sipx server
  $url = "http://sipx.gcgov.local:6667/callcontroller/".$from."/".$to."?isForwardingAllowed=true";
  $ch = curl_init();
  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_DIGEST);
  curl_setopt($ch, CURLOPT_POST, 1);
  curl_setopt($ch, CURLOPT_USERPWD, $from.":".$pass);
  $result = curl_exec($ch);
  curl_close($ch);
  ?>

Sample contact information ::

  <contact-information>
  <jobTitle>Data Entry Assistant</jobTitle>
  <jobDept>Data Management Services</jobDept>
  <companyName>Museum of Science</companyName>
  <homeAddress>
  <city>NY</city>
  </homeAddress>
  <officeAddress>
  <street>1 Science Park</street>
  <city>Boston</city>
  <country>US</country>
  <state>MA</state>
  <zip>02114</zip>
  </officeAddress>
  <imId>myId</imId>
  <emailAddress>john.doe@example.com</emailAddress>
  <useBranchAddress>false</useBranchAddress>
  <avatar>https://secure.gravatar.com/avatar/8eb1b522f60d11fa897de1dc6351b7e8?s=80&amp;d=G</avatar>
  <firstName>John</firstName>
  <lastName>Doe</lastName>
  </contact-information>

System
~~~~~~

Change user portal password
^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/portal/password/{password}

**Default Resource Properties:** N/A

**Specific Response Codes:**
  Error 400 on PUT when {password} is not valid (or less than 8 characters, or null)

**HTTP Method:** PUT
  Change user portal password with {password}

**Example**::

  foo

**Unsupported HTTP Method:** GET, POST, DELETE

View fax extensions and DID number
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/faxprefs

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *extension*
    - Extension number
  * - *did*
    - DID number

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Gets user fax extension and DID number.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View configuration servers' time
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/time

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves the configuration server time.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View login details
^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/logindetails

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *login-details*
    - Header for log in details
  * - *userName*
    - The user name
  * - *imID*
    - IM name
  * - *ldapImAuth*
    - Determines if LDAP auth is enabled, true or false
  * - *sipPassword*
    - The SIP password

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Retrieves login detail.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE

View user details, password, and the servers' hostname
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/faxprefs

**Default Resource Properties**
  The resource is represented by the following properties when the GET request is performed:

.. list-table::

  * - **Property**
    - **Description**
  * - *logindetails*
    - Header for log in details
  * - *userName*
    - The user name
  * - *imID*
    - IM name
  * - *ldapImAuth*
    - Determines if LDAP auth is enabled, true or false
  * - *sipPassword*
    - The SIP password
  * - *pin*
    - 
  * - *im-location*
    - 
  * - *fqdn*
    - The IM server FQDN

**Unsupported HTTP Method:** PUT, POST, DELETE

Keep session alive
^^^^^^^^^^^^^^^^^^

**Resource URI:** /my/keepalive

**Default Resource Properties:** N/A

**Specific Response Codes:** N/A

**HTTP Method:** GET
  Meant to be periodically called by clients in order to keep their web session alive.

**Example**::

  foo

**Unsupported HTTP Method:** PUT, POST, DELETE
