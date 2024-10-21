.. index:: SOAP API Reference

.. _soap-api-reference:

==================
SOAP API Reference
==================

.. note::
  Web services defined in this section for configuration of the system are all SOAP based services and require administrator privileges to be used.

About SOAP
----------

The SOAP API enables administrators to perform a variety of functions offered by sipxconfig, but without the need to directly interacting with the sipxconfig webui.

The server utilizes the `Apache Axis <https://axis.apache.org/axis/>`_ framework.

SOAP base URL
~~~~~~~~~~~~~

The base URL for the configuration API is the following::

  https://host.domain/sipxconfig/services/

SOAP use case examples
~~~~~~~~~~~~~~~~~~~~~~

Use case examples for the SOAP APIs might be:

  * Integration of sipxcom functionality with your company Intranet site.
  * Automate or script processes such as adding or importing users, updating phones, assigning phones to groups, etc.
  * Customize the sipxconfig webui to suit your needs.

You can use SOAP with WDSL, which is a formal API definition, and generate bindings in your preferred programming language such as Python, Perl, Ruby, Java, and others. It is recommended to select a programming language with good SOAP client support.

**Ruby:** From the WSDL you can use the `SOAP4R project <https://rubygems.org/gems/soap4r/versions/1.5.8>`_ to build client bindings.

**Perl:** Install `SOAP for Perl <https://metacpan.org/pod/SOAP::Lite>`_ with::

  perl -MCPAN -e 'install SOAP::lite'

**Command line:** Use the following command::

  java -jar $WsdlDocDir/wsdldoc.jar {color}
  -title "sipXconfig SOAP API v3.2" {color}
  -dir `pwd`"/ws-api-3.2" {color}
  http://sipXcom.sipfoundry.org/rep/sipXcom/main/sipXconfig/web/src/org/sipfoundry/sipxconfig/api/sipxconfig.wsdl

Administration Services
-----------------------
The following resources for the Configuration API are only available for users with administration rights.

**Permissions**
  * Add permissions
  * Find permissions
  * Manage permissions

**Call Groups**
  * Add call group
  * Find call groups
  * WSDL Call Group

**Users**
  * Add users
  * Find users
  * Manage users

**Phones**
  * Add phones
  * Find phones
  * Manage phones

**Tests**
  * Reset

Permissions
~~~~~~~~~~~
The Permission Web Services supported are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported.

**URI** :: 

  https://host.domain/sipxconfig/services/PermissionService

**WSDL** ::

  <?xml version="1.0" encoding="UTF-8" ?>
  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  - <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="Permission">
  <sequence>
  <element name="name" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="label" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="description" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="defaultValue" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="type" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="builtIn" nillable="true" type="xsd:boolean" />
  </sequence>
  </complexType>
  <complexType name="AddPermission">
  <sequence>
  <element name="permission" type="impl:Permission" />
  </sequence>
  </complexType>
  <element name="AddPermission" type="impl:AddPermission" />
  <complexType name="PermissionSearch">
  <sequence>
  <element maxOccurs="1" minOccurs="0" name="byName" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="byLabel" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="FindPermission">
  <sequence>
  <element name="search" type="impl:PermissionSearch" />
  </sequence>
  </complexType>
  <element name="FindPermission" type="impl:FindPermission" />
  <complexType name="ArrayOfPermission">
  <sequence>
  <element maxOccurs="unbounded" minOccurs="0" name="item" type="impl:Permission" />
  </sequence>
  </complexType>
  <complexType name="FindPermissionResponse">
  <sequence>
  <element name="permissions" type="impl:ArrayOfPermission" />
  </sequence>
  </complexType>
  <element name="FindPermissionResponse" type="impl:FindPermissionResponse" />
  <complexType name="Property">
  <sequence>
  <element name="property" type="xsd:string" />
  <element name="value" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="ManagePermission">
  <sequence>
  <element name="search" type="impl:PermissionSearch" />
  <element maxOccurs="unbounded" name="edit" type="impl:Property" />
  <element maxOccurs="1" minOccurs="0" name="deletePermission" nillable="true" type="xsd:boolean" />
  </sequence>
  </complexType>
  <element name="ManagePermission" type="impl:ManagePermission" />
  </schema>
  </wsdl:types>
  <wsdl:message name="findPermissionRequest">
  <wsdl:part element="impl:FindPermission" name="FindPermission" />
  </wsdl:message>
  <wsdl:message name="managePermissionRequest">
  <wsdl:part element="impl:ManagePermission" name="ManagePermission" />
  </wsdl:message>
  <wsdl:message name="addPermissionRequest">
  <wsdl:part element="impl:AddPermission" name="AddPermission" />
  </wsdl:message>
  <wsdl:message name="findPermissionResponse">
  <wsdl:part element="impl:FindPermissionResponse" name="FindPermissionResponse" />
  </wsdl:message>
  <wsdl:message name="addPermissionResponse" />
  <wsdl:message name="managePermissionResponse" />
  <wsdl:portType name="PermissionService">
  <wsdl:operation name="addPermission" parameterOrder="AddPermission">
  <wsdl:input message="impl:addPermissionRequest" name="addPermissionRequest" />
  <wsdl:output message="impl:addPermissionResponse" name="addPermissionResponse" />
  </wsdl:operation>
  <wsdl:operation name="findPermission" parameterOrder="FindPermission">
  <wsdl:input message="impl:findPermissionRequest" name="findPermissionRequest" />
  <wsdl:output message="impl:findPermissionResponse" name="findPermissionResponse" />
  </wsdl:operation>
  <wsdl:operation name="managePermission" parameterOrder="ManagePermission">
  <wsdl:input message="impl:managePermissionRequest" name="managePermissionRequest" />
  <wsdl:output message="impl:managePermissionResponse" name="managePermissionResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="PermissionServiceSoapBinding" type="impl:PermissionService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="addPermission">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="addPermissionRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="addPermissionResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="findPermission">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="findPermissionRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="findPermissionResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="managePermission">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="managePermissionRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="managePermissionResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:PermissionServiceSoapBinding" name="PermissionService">
  <wsdlsoap:address location="https://47.134.206.174:8443/sipxconfig/services/PermissionService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

.. note::
  wsdlsoap:address location specified at the end of the WSDL will be specific to your system.

Add Permissions
~~~~~~~~~~~~~~~

**Name:** *addPermission*

**Description:** Add a custom call permission to the system.

**Input Parameters:**

.. list-table::

  * - **Name**
    - **Value type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read only**
  * - *name*
    - string
    - Required
    - The name of the permission to add. Even though it is a required parameter its value is ignored and an internal name is generated.
    - Editable
  * - *label*
    - string
    - Optional
    - The label of the permission to add. Label is the name displayed in the webui.
    - Editable
  * - *description*
    - string
    - Optional
    - Indicates if the permission is enabled (true) or disabled (false) by default for users.
    - Editable
  * - *defaultValue*
    - boolean
    - Optional
    - Indicates if the permission is enabled (true) or disabled (false) by default for users.
    - Editable
  * - *type*
    - string
    - 
    - The type of permission. Read only, any string on add will be ignored.
    - Read Only
  * - *builtIn*
    - boolean
    - 
    - Indicates if the permission is builtin (true) or not (false). Read only, any string on add will be ignored.
    - Read Only

**Output parameters:** Empty response.

**Example:** Adding a call permission with label "Test three" whose default value is false::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:AddPermission>
  <permission>
  <name>Test3</name>
  <label>Test Three</label>
  <description>Third test permission</description>
  <defaultValue>false</defaultValue>
  </permission>
  </con:AddPermission>
  </soapenv:Body>
  </soapenv:Envelope>

Find Permissions
~~~~~~~~~~~~~~~~

**Name:** *findPermission*

**Description:** Search for a permission or permissions defined in the system.

**Input Parameters:**

.. list-table::

  * - **Name**
    - **Value type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read only**
  * - *byName*
    - string
    - Optional
    - Indicates a name for permissions by their name. May be null.
    - Editable
  * - *byLabel*
    - string
    - Optional
    - Indicates a search for permissions by their label. May be null.
    - Editable

**Output Parameters:** Array of items representing the permissions found in the search.

.. list-table::
  
  * - **Name**
    - **Value Type**
    - **Description**
  * - *name*
    - string
    - The name of the permission.
  * - *label*
    - string
    - The value representing the label of the permission.
  * - *description*
    - string
    - Describes the permission.
  * - *defaultValue*
    - 
    - Boolean indicating if the permission is enabled (true) or disabled (false) by default for users.
  * - *builtIn*
    - boolean
    - Indicates if the permission is builtin (true) or not (false).

**Example:** Search to find all permissions defined in the system. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindPermission>
  </con:FindPermission>
  </soapenv:Body>
  </soapenv:Envelope>   

**Example:** Search to find permission with label "test three". ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindPermission>
  <search>
  <byLabel>Test Three</byLabel>
  </search>
  </con:FindPermission>
  </soapenv:Body>
  </soapenv:Envelope>

Manage Permissions
~~~~~~~~~~~~~~~~~~

**Name:** *managePermission*

**Description:** Manage (update or delete) existing permissions defined in the system. Only permissions which are not built into the system can be edited or deleted.

**Input Parameters:**

.. list-table::

  * - **Name**
    - **Value type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read only**
  * - *byName*
    - string
    - Optional
    - String used to indicate a search for permissions by their name. May be null. Name is internally generated value and may not be useful for searching.
    - Editable
  * - *byLabel*
    - string
    - Optional
    - Indicates a search for permissions by their label. May be null.
    - Editable
  * - *property*
    - 
    - 
    - Name of the permission field to edit.
    - 
  * - *value*
    - 
    - 
    - Value to use for the permission field being edited.
    - 
  * - *deletePermission*
    - boolean
    - Optional
    - Indicating to delete (true) a permission.
    - 

**Output Parameters:** Empty response.

**Example**:: 

  foo

Call Groups
-----------

The call group web services are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported. A call group service deals with information related to hunt groups. Any information queried or added in one of the implemented services are mapped to a hunt group in the configuratio database.

**URI** :: 
  
  https://host.domain/sipxconfig/services/CallGroupService

**WSDL** ::

  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="UserRing">
  <sequence>
  <element name="expiration" type="xsd:int" />
  <element name="type" type="xsd:string" />
  <element name="position" type="xsd:int" />
  <element name="userName" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="CallGroup">
  <sequence>
  <element name="name" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="extension" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="description" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="enabled" nillable="true" type="xsd:boolean" />
  <element maxOccurs="unbounded" minOccurs="0" name="rings" nillable="true" type="impl:UserRing" />
  </sequence>
  </complexType>
  <complexType name="AddCallGroup">
  <sequence>
  <element name="callGroup" type="impl:CallGroup" />
  </sequence>
  </complexType>
  <element name="AddCallGroup" type="impl:AddCallGroup" />
  <complexType name="ArrayOfCallGroup">
  <sequence>
  <element maxOccurs="unbounded" minOccurs="0" name="item" type="impl:CallGroup" />
  </sequence>
  </complexType>
  <complexType name="GetCallGroupsResponse">
  <sequence>
  <element name="callGroups" type="impl:ArrayOfCallGroup" />
  </sequence>
  </complexType>
  <element name="GetCallGroupsResponse" type="impl:GetCallGroupsResponse" />
  </schema>
  </wsdl:types>
  <wsdl:message name="getCallGroupsResponse">
  <wsdl:part element="impl:GetCallGroupsResponse" name="GetCallGroupsResponse" />
  </wsdl:message>
  <wsdl:message name="getCallGroupsRequest" />
  <wsdl:message name="addCallGroupResponse" />
  <wsdl:message name="addCallGroupRequest">
  <wsdl:part element="impl:AddCallGroup" name="AddCallGroup" />
  </wsdl:message>
  <wsdl:portType name="CallGroupService">
  <wsdl:operation name="addCallGroup" parameterOrder="AddCallGroup">
  <wsdl:input message="impl:addCallGroupRequest" name="addCallGroupRequest" />
  <wsdl:output message="impl:addCallGroupResponse" name="addCallGroupResponse" />
  </wsdl:operation>
  <wsdl:operation name="getCallGroups">
  <wsdl:input message="impl:getCallGroupsRequest" name="getCallGroupsRequest" />
  <wsdl:output message="impl:getCallGroupsResponse" name="getCallGroupsResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="CallGroupServiceSoapBinding" type="impl:CallGroupService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="addCallGroup">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="addCallGroupRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="addCallGroupResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="getCallGroups">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="getCallGroupsRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="getCallGroupsResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:CallGroupServiceSoapBinding" name="CallGroupService">
  <wsdlsoap:address location="https://47.134.206.174:8443/sipxconfig/services/CallGroupService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

Add Call Groups
~~~~~~~~~~~~~~~

.. list-table::

  * - **Name**
    - **Value type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read only**
  * - *name*
    - string
    - Required
    - String representing the name of the hunt group to add.
    - Editable
  * - *extension*
    - string
    - Optional
    - The extension to be associated with the hunt group.
    - Editable
  * - *description*
    - string
    - Optional
    - Describes the hunt group.
    - Editable
  * - *enabled*
    - boolean
    - Optional
    - Describing the members of the hunt group, their position in the group, time (in seconds) to ring the user.
    - 0 or more repetitions.
  * - *expiration*
    - 
    - 
    - Time in seconds to present the call to the user.
    - 
  * - *type*
    - string
    - 
    - The ring sequence. Can be "delayed" or "immediate". Delay is used to build a sequential type hunt group and immediate a broadcast type hunt group. A mix can be used.
    - 
  * - *position*
    - 
    -
    - The unique position (starting at 0) of the user in the group.
    - 
  * - *username*
    - string
    - 
    - The extension of the user.
    - 

**Output Parameters:** Empty response.

**Example:** Add a new hunt group "TestGroup2" that is enabled, dialable at extension 556 and contains 4 members (212, 215, 211, and 221). ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:AddCallGroup>
  <callGroup>
  <name>TestGroup2</name>
  <!-Optional:->
  <extension>556</extension>
  <!-Optional:->
  <description>Sample SOAP created Hunt Group</description>
  <!-Optional:->
  <enabled>true</enabled>
  <!-Zero or more repetitions:->
  <rings>
  <expiration>10</expiration>
  <type>delayed</type>
  <position>0</position>
  <userName>212</userName>
  </rings>
  <rings>
  <expiration>10</expiration>
  <type>immediate</type>
  <position>1</position>
  <userName>215</userName>
  </rings>
  <rings>
  <expiration>15</expiration>
  <type>immediate</type>
  <position>2</position>
  <userName>211</userName>
  </rings>
  <rings>
  <expiration>15</expiration>
  <type>delayed</type>
  <position>3</position>
  <userName>221</userName>
  </rings>
  </callGroup>
  </con:AddCallGroup>
  </soapenv:Body>
  </soapenv:Envelope>

Get Call Groups
~~~~~~~~~~~~~~~

**Name:** *getCallGroups*

**Description:** Query the hunt groups defined in the system.

**Input Parameters:** None

**Output Parameters:** Array of items representing the permissions found in the search.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Description**
  * - *name*
    - string
    - The name of the hunt group.
  * - *extension*
    - string
    - Representing the extension associated with the hunt group.
  * - *description*
    - string
    - Describes the hunt group.
  * - *enabled*
    - boolean
    - Indicates if the hunt group is enabled (true) or disabled (false).
  * - *rings*
    - array
    - The members of the hunt group, their position, time (in seconds) to ring the user (0 or more repetitions).
  * - *expiration*
    - 
    - Time in seconds to present the call to user.
  * - *type*
    - string
    - The ring sequence. Can be 'delayed' or 'immediate'. Delayed is sequential, immediate is broadcast.
  * - *position*
    - 
    - The unique position (starting at 0) of the user in the group.
  * - *username*
    - string
    - The extension of the user.

**Example:** Query the hunt groups defined in the system. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header/>
  <soapenv:Body/>
  </soapenv:Envelope>

Users
-----

The user web services are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported.

**URI** ::

  https://host.domain/sipxconfig/services/UserService

**WSDL** ::

  <?xml version="1.0" encoding="UTF-8" ?>
  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  - <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="User">
  <sequence>
  <element name="userName" type="xsd:string" />
  <element name="pintoken" nillable="true" type="xsd:string" />
  <element name="lastName" nillable="true" type="xsd:string" />
  <element name="firstName" nillable="true" type="xsd:string" />
  <element name="sipPassword" nillable="true" type="xsd:string" />
  <element maxOccurs="unbounded" minOccurs="0" name="aliases" nillable="true" type="xsd:string" />
  <element name="emailAddress" nillable="true" type="xsd:string" />
  <element maxOccurs="unbounded" minOccurs="0" name="groups" nillable="true" type="xsd:string" />
  <element maxOccurs="unbounded" minOccurs="0" name="permissions" nillable="true" type="xsd:string" />
  <element maxOccurs="1" name="branchName" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="AddUser">
  <sequence>
  <element name="user" type="impl:User" />
  <element name="pin" type="xsd:string" />
  </sequence>
  </complexType>
  <element name="AddUser" type="impl:AddUser" />
  <complexType name="UserSearch">
  <sequence>
  <element maxOccurs="1" minOccurs="0" name="byUserName" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="byFuzzyUserNameOrAlias" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="byGroup" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="FindUser">
  <sequence>
  <element name="search" type="impl:UserSearch" />
  </sequence>
  </complexType>
  <element name="FindUser" type="impl:FindUser" />
  <complexType name="ArrayOfUser">
  <sequence>
  <element maxOccurs="unbounded" minOccurs="0" name="item" type="impl:User" />
  </sequence>
  </complexType>
  <complexType name="FindUserResponse">
  <sequence>
  <element name="users" type="impl:ArrayOfUser" />
  </sequence>
  </complexType>
  <element name="FindUserResponse" type="impl:FindUserResponse" />
  <complexType name="Property">
  <sequence>
  <element name="property" type="xsd:string" />
  <element name="value" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="ManageUser">
  <sequence>
  <element name="search" type="impl:UserSearch" />
  <element maxOccurs="unbounded" name="edit" type="impl:Property" />
  <element maxOccurs="1" minOccurs="0" name="deleteUser" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="addGroup" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="removeGroup" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="updateGroup" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <element name="ManageUser" type="impl:ManageUser" />
  </schema>
  </wsdl:types>
  <wsdl:message name="findUserRequest">
  <wsdl:part element="impl:FindUser" name="FindUser" />
  </wsdl:message>
  <wsdl:message name="addUserRequest">
  <wsdl:part element="impl:AddUser" name="AddUser" />
  </wsdl:message>
  <wsdl:message name="manageUserResponse" />
  <wsdl:message name="addUserResponse" />
  <wsdl:message name="manageUserRequest">
  <wsdl:part element="impl:ManageUser" name="ManageUser" />
  </wsdl:message>
  <wsdl:message name="findUserResponse">
  <wsdl:part element="impl:FindUserResponse" name="FindUserResponse" />
  </wsdl:message>
  <wsdl:portType name="UserService">
  <wsdl:operation name="addUser" parameterOrder="AddUser">
  <wsdl:input message="impl:addUserRequest" name="addUserRequest" />
  <wsdl:output message="impl:addUserResponse" name="addUserResponse" />
  </wsdl:operation>
  <wsdl:operation name="findUser" parameterOrder="FindUser">
  <wsdl:input message="impl:findUserRequest" name="findUserRequest" />
  <wsdl:output message="impl:findUserResponse" name="findUserResponse" />
  </wsdl:operation>
  <wsdl:operation name="manageUser" parameterOrder="ManageUser">
  <wsdl:input message="impl:manageUserRequest" name="manageUserRequest" />
  <wsdl:output message="impl:manageUserResponse" name="manageUserResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="UserServiceSoapBinding" type="impl:UserService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="addUser">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="addUserRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="addUserResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="findUser">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="findUserRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="findUserResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="manageUser">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="manageUserRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="manageUserResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:UserServiceSoapBinding" name="UserService">
  <wsdlsoap:address location="https://47.134.206.174:8443/sipxconfig/services/UserService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

.. note::
  wsdlsoap:address location specified at the end of the WSDL will be specific to your system.

Add Users
~~~~~~~~~

**Name:** *addUser*

**Description:** Add a new user to the system.

**Input Parameters:**

.. list-table::

  * - **Name**
    - **Value type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read only**
  * - *userName*
    - string
    - Required
    - The name of the user to add.
    - Editable
  * - *pinToken*
    - string
    - optional
    - Internally generated token that is an encrypted version of the voicemail pin. This should not be specified as it will be internally generated.
    - Read Only
  * - *lastName*
    - string
    - Optional
    - The last name of the user.
    - Editable
  * - *firstName*
    - string
    - Optional
    - The first name of the user.
    - Editable
  * - *sipPassword*
    - string
    - Optional
    - The SIP password for the user.
    - 
  * - *aliases*
    - array
    - 
    - Array of strings, each representing membership in defined groups.
    - 
  * - *permissions*
    - array
    - 
    - Array of strings, each representing a permission name that is granted to the user. Permissions can be general or call permissions. See *findPermission*
    - 
  * - *pin*
    - string
    - Required
    - The PIN for the user.
    - Editable
  * - *branchName*
    - string
    - Optional
    - User branch
    - Editable

**Output Parameters:** Empty response.

**Example:** Add a new user 223 to the system with sip password 4567, pin 1234, along with various permissions and group memberships in the branch Berlin. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:AddUser>
  <user>
  <userName>223</userName>
  <lastName>Einstein</lastName>
  <firstName>Albert</firstName>
  <sipPassword>4567</sipPassword>
  <emailAddress>albertE@yahoo.com</emailAddress>
  <branchName>Berlin</branchName>
  <groups>Managers</groups>
  <permissions>FreeswitchVoicemailServer</permissions>
  <permissions>InternationalDialing</permissions>
  <permissions>LocalDialing</permissions>
  <permissions>LongDistanceDialing</permissions>
  <permissions>Mobile</permissions>
  <permissions>TollFree</permissions>
  <permissions>Voicemail</permissions>
  <permissions>music-on-hold</permissions>
  <permissions>perm_8</permissions>
  <permissions>personal-auto-attendant</permissions>
  <permissions>tui-change-pin</permissions>
  </user>
  <pin>1234</pin>
  </con:AddUser>
  </soapenv:Body>
  </soapenv:Envelope>

Find Users
~~~~~~~~~~

**Name:** *findUser*

**Description:** Find defined user(s) in the system.

**Input Parameters:** Either null for a listing of all users, or one of the following optional parameters.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *byUserName*
    - string
    - Optional
    - The name of the user to find.
    - Editable
  * - *byFuzzyUserNameOrAlias*
    - string
    - Optional
    - A partial user name or alias to search for, a type of wildcard search.
    - Editable
  * - *byGroup*
    - string
    - Optional
    - The users which are members of a particular defined group.
    - Editable

**Output parameters:** An array of 0 or more of the following.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Description**
  * - *userName*
    - string
    - The name of the user to add.
  * - *pinToken*
    - string
    - Internally generated token that is an encrypted version of the pin.
  * - *lastName*
    - string
    - The last name of the user.
  * - *firstName*
    - string
    - The first name of the user.
  * - *sipPassword*
    - string
    - The SIP password for the user.
  * - *aliases*
    - array
    - Array of strings, each representing a user alias.
  * - *emailAddress*
    - string
    - The email address of the user.
  * - *groups*
    - array
    - Array of strings, each representing membership in defined groups.
  * - *pin*
    - string
    - The PIN for the user.
  * - *branchName*
    -  string
    - Users branch.

**Example:** Find all defined users in the system. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindUser>
  </con:FindUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Find all users that are members of the group "Managers". ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindUser>
  <search>
  <byGroup>Managers</byGroup>
  </search>
  </con:FindUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Find the user 223. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindUser>
  <search>
  <byUserName>223</byUserName>
  </search>
  </con:FindUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Find users whose userName begins with 22. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindUser>
  <search>
  <byFuzzyUserNameOrAlias>22</byFuzzyUserNameOrAlias>
  </search>
  </con:FindUser>
  </soapenv:Body>
  </soapenv:Envelope>

Manage Users
~~~~~~~~~~~~

**Name:** *manageUser*

**Description:** Manage (update or delete) users defined in the system.

**Input Paramters:** Either null to list all, or one of the following optional parameters.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *byUserName*
    - string
    - Optional
    - The name of the user to find.
    - Editable
  * - *byFuzzyUserNameOrAlias*
    - string
    - Optional
    - A partial username or alias to search for. A type of wildcard search.
    - Editable
  * - *byGroup*
    - string
    - Optional
    - The users which are members of a particular defined group.
    - Editable
  * - *property*
    - string
    - Optional
    - Name of the user field to edit.
    - Editable
  * - *value*
    - string
    - Optional
    - Value to use for the user field being edited.
    - Editable
  * - *deleteUser*
    - boolean
    - Optional
    - Indicates to delete (true) user(s). Dependent upon search results.
    - Editable
  * - *addGroup*
    - string
    - Optional
    - The name of the group to add the user(s) to. Dependent upon search results.
    - Editable
  * - *removeGroup*
    - string
    - Optional
    - The name of the group to remove the user(s) from. Dependent upon search results.
    - Editable
  * - *updateBranch*
    - string
    - Optional
    - The name of the branch to update the user(s) to.
    - Editable

**Output Parameters:** Empty response

**Example:** Remove all users in the system beginning with 22 from group Managers ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManageUser>
  <search>
  <byFuzzyUserNameOrAlias>22</byFuzzyUserNameOrAlias>
  </search>
  <removeGroup>Managers</removeGroup>
  </con:ManageUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Add user with username 211 to the group Managers ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManageUser>
  <byUserName>211</byUserName>
  </search>
  <addGroup>Managers</addGroup>
  </con:ManageUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Change all users from group Managers to have a *lastName* of "SuperDog" and *firstName* of "I am" ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManageUser>
  <search>
  <!-Optional:->
  <byGroup>Managers</byGroup>
  </search>
  <!-1 or more repetitions:->
  <edit>
  <!-You may enter the following 2 items in any order->
  <property>lastName</property>
  <value>SuperDog</value>
  </edit>
  <edit>
  <!-You may enter the following 2 items in any order->
  <property>firstName</property>
  <value>I am</value>
  </edit>
  </con:ManageUser>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Update user with username 211 to the branch Berlin ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManageUser>
  <byUserName>211</byUserName>
  </search>
  <updateBranch>Berlin</updateBranch>
  </con:ManageUser>
  </soapenv:Body>
  </soapenv:Envelope>

Park Orbits
-----------

The park orbit web services are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported.

**URI** ::

  https://host.domain:8443/sipxconfig/services/ParkOrbitService

**WSDL** ::

  <?xml version="1.0" encoding="UTF-8" ?>
  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  - <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="ParkOrbit">
  <sequence>
  <element name="name" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="extension" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="description" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="enabled" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="music" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="AddParkOrbit">
  <sequence>
  <element name="parkOrbit" type="impl:ParkOrbit" />
  </sequence>
  </complexType>
  <element name="AddParkOrbit" type="impl:AddParkOrbit" />
  <complexType name="ArrayOfParkOrbit">
  <sequence>
  <element maxOccurs="unbounded" minOccurs="0" name="item" type="impl:ParkOrbit" />
  </sequence>
  </complexType>
  <complexType name="GetParkOrbitsResponse">
  <sequence>
  <element name="parkOrbits" type="impl:ArrayOfParkOrbit" />
  </sequence>
  </complexType>
  <element name="GetParkOrbitsResponse" type="impl:GetParkOrbitsResponse" />
  </schema>
  </wsdl:types>
  <wsdl:message name="addParkOrbitRequest">
  <wsdl:part element="impl:AddParkOrbit" name="AddParkOrbit" />
  </wsdl:message>
  <wsdl:message name="addParkOrbitResponse" />
  <wsdl:message name="getParkOrbitsResponse">
  <wsdl:part element="impl:GetParkOrbitsResponse" name="GetParkOrbitsResponse" />
  </wsdl:message>
  <wsdl:message name="getParkOrbitsRequest" />
  <wsdl:portType name="ParkOrbitService">
  <wsdl:operation name="addParkOrbit" parameterOrder="AddParkOrbit">
  <wsdl:input message="impl:addParkOrbitRequest" name="addParkOrbitRequest" />
  <wsdl:output message="impl:addParkOrbitResponse" name="addParkOrbitResponse" />
  </wsdl:operation>
  <wsdl:operation name="getParkOrbits">
  <wsdl:input message="impl:getParkOrbitsRequest" name="getParkOrbitsRequest" />
  <wsdl:output message="impl:getParkOrbitsResponse" name="getParkOrbitsResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="ParkOrbitServiceSoapBinding" type="impl:ParkOrbitService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="addParkOrbit">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="addParkOrbitRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="addParkOrbitResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="getParkOrbits">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="getParkOrbitsRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="getParkOrbitsResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:ParkOrbitServiceSoapBinding" name="ParkOrbitService">
  <wsdlsoap:address location="https://47.134.206.174:8443/sipxconfig/services/ParkOrbitService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

Add Park Orbits
~~~~~~~~~~~~~~~

**Name:** *addParkOrbit*

**Description:** Add a new call park orbit to the system.

**Input Parameters:** Either null to list all users, or provide one of the following optional parameters.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *name*
    - string
    - Required
    - The name of the call park orbit to add.
    - Editable
  * - *extension*
    - string
    - Optional
    - Dialable extension to be used.
    - Editable
  * - *description*
    - string
    - Optional
    - Description of the call park orbit.
    - Editable
  * - *enabled*
    - boolean
    - Optional
    - Indicates if the call park is enabled (true) or disabled (false)
    - Editable
  * - *music*
    - path
    - Optional
    - Path to a wav file to play as background music for parked calls.
    - Editable

.. note::
  wav files must be in the appropriate format of RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz

**Output parameters:** Empty response.

**Example:** Add a new call park orbit with the name ParkSales at extension 46 and is enabled. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:AddParkOrbit>
  <parkOrbit>
  <name>ParkSales</name>
  <!-Optional:->
  <extension>46</extension>
  <!-Optional:->
  <description>Sales calls park orbit</description>
  <!-Optional:->
  <enabled>true</enabled>
  </parkOrbit>
  </con:AddParkOrbit>
  </soapenv:Body>
  </soapenv:Envelope>

Get Park Orbits
~~~~~~~~~~~~~~~

**Name:** *getParkOrbits*

**Description:** Queries information on all call park orbits defined in the system.

**Input Parameters:** None

**Output Parameters:**

.. list-table::

  * - **Name**
    - **Value Type**
    - **Description**
  * - *name*
    - string
    - The name of the call park orbit.
  * - *extension*
    - string
    - The dialable extension.
  * - *description*
    - string
    - A description of the call park orbit.
  * - *enabled*
    - boolean
    - Indicates if the call park orbit is enabled (true) or disabled (false).
  * - *music*
    - 
    - Path to a wav file to play as background music for parked calls.

.. note::
  wav files must be in the appropriate format of RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz

**Example:** Query the call park orbits defined in the system. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header/>
  <soapenv:Body/>
  </soapenv:Envelope>

Phones
------

The phone web services are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported.

**URI** ::

  https://host.domain/sipxconfig/services/PhoneService

**WSDL** ::

  <?xml version="1.0" encoding="UTF-8" ?>
  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  - <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="Line">
  <sequence>
  <element name="userId" type="xsd:string" />
  <element name="uri" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="Phone">
  <sequence>
  <element name="serialNumber" type="xsd:string" />
  <element name="modelId" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="description" nillable="true" type="xsd:string" />
  <element maxOccurs="unbounded" minOccurs="0" name="groups" nillable="true" type="xsd:string" />
  <element maxOccurs="unbounded" minOccurs="0" name="lines" nillable="true" type="impl:Line" />
  <element maxOccurs="1" minOccurs="0" name="deviceVersion" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="AddPhone">
  <sequence>
  <element name="phone" type="impl:Phone" />
  </sequence>
  </complexType>
  <element name="AddPhone" type="impl:AddPhone" />
  <complexType name="PhoneSearch">
  <sequence>
  <element maxOccurs="1" minOccurs="0" name="bySerialNumber" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="byGroup" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="FindPhone">
  <sequence>
  <element name="search" type="impl:PhoneSearch" />
  </sequence>
  </complexType>
  <element name="FindPhone" type="impl:FindPhone" />
  <complexType name="ArrayOfPhone">
  <sequence>
  <element maxOccurs="unbounded" minOccurs="0" name="item" type="impl:Phone" />
  </sequence>
  </complexType>
  <complexType name="FindPhoneResponse">
  <sequence>
  <element name="phones" type="impl:ArrayOfPhone" />
  </sequence>
  </complexType>
  <element name="FindPhoneResponse" type="impl:FindPhoneResponse" />
  <complexType name="Property">
  <sequence>
  <element name="property" type="xsd:string" />
  <element name="value" nillable="true" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="AddExternalLine">
  <sequence>
  <element name="userId" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="displayName" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="password" type="xsd:string" />
  <element name="registrationServer" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="voiceMail" type="xsd:string" />
  </sequence>
  </complexType>
  <complexType name="ManagePhone">
  <sequence>
  <element name="search" type="impl:PhoneSearch" />
  <element maxOccurs="unbounded" name="edit" type="impl:Property" />
  <element maxOccurs="1" minOccurs="0" name="deletePhone" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="addGroup" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="removeGroup" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="addLine" nillable="true" type="impl:Line" />
  <element maxOccurs="1" minOccurs="0" name="addExternalLine" nillable="true" type="impl:AddExternalLine" />
  <element maxOccurs="1" minOccurs="0" name="removeLineByUserId" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="removeLineByUri" nillable="true" type="xsd:string" />
  <element maxOccurs="1" minOccurs="0" name="generateProfiles" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="restart" nillable="true" type="xsd:boolean" />
  </sequence>
  </complexType>
  <element name="ManagePhone" type="impl:ManagePhone" />
  </schema>
  </wsdl:types>
  <wsdl:message name="findPhoneRequest">
  <wsdl:part element="impl:FindPhone" name="FindPhone" />
  </wsdl:message>
  <wsdl:message name="managePhoneResponse" />
  <wsdl:message name="addPhoneResponse" />
  <wsdl:message name="managePhoneRequest">
  <wsdl:part element="impl:ManagePhone" name="ManagePhone" />
  </wsdl:message>
  <wsdl:message name="addPhoneRequest">
  <wsdl:part element="impl:AddPhone" name="AddPhone" />
  </wsdl:message>
  <wsdl:message name="findPhoneResponse">
  <wsdl:part element="impl:FindPhoneResponse" name="FindPhoneResponse" />
  </wsdl:message>
  <wsdl:portType name="PhoneService">
  <wsdl:operation name="addPhone" parameterOrder="AddPhone">
  <wsdl:input message="impl:addPhoneRequest" name="addPhoneRequest" />
  <wsdl:output message="impl:addPhoneResponse" name="addPhoneResponse" />
  </wsdl:operation>
  <wsdl:operation name="findPhone" parameterOrder="FindPhone">
  <wsdl:input message="impl:findPhoneRequest" name="findPhoneRequest" />
  <wsdl:output message="impl:findPhoneResponse" name="findPhoneResponse" />
  </wsdl:operation>
  <wsdl:operation name="managePhone" parameterOrder="ManagePhone">
  <wsdl:input message="impl:managePhoneRequest" name="managePhoneRequest" />
  <wsdl:output message="impl:managePhoneResponse" name="managePhoneResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="PhoneServiceSoapBinding" type="impl:PhoneService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="addPhone">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="addPhoneRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="addPhoneResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="findPhone">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="findPhoneRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="findPhoneResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  <wsdl:operation name="managePhone">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="managePhoneRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="managePhoneResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:PhoneServiceSoapBinding" name="PhoneService">
  <wsdlsoap:address location="https://47.134.206.174/sipxconfig/services/PhoneService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

.. note::
  wsdlsoap:address location specified at the end of the WSDL will be specific to your system.

Add Phones
~~~~~~~~~~

**Name:** *addPhone*

**Description:** Add a new phone to the system.

**Input Parameters:** 

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *serialNumber*
    - string
    - Required
    - The MAC address of the phone.
    - Editable
  * - *modelId*
    - string
    - Required
    - A supported model ID.
    - Editable
  * - *description*
    - string
    - Optional
    - A description of the phone.
    - Editable
  * - *groups*
    - string
    - Optional
    - The phone group(s) the new phone will be a member of.
    - Editable
  * - *lines*
    - string
    - Optional
    - String representing assigned lines to the phone.
    - Editable
  * - *deviceVersion*
    - string
    - Optional
    - The version of the phone.
    - Editable

**Output Parameters:** Empty response.

**Example:** Add a new polycom spip 321 phone to the system, assign line 221, and add to phone group FirstPhoneGroup.  ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:AddPhone>
  <phone>
  <serialNumber>000000000002</serialNumber>
  <modelId>polycom321</modelId>
  <!-Optional:->
  <description>SOAP added phone</description>
  <!-Zero or more repetitions:->
  <groups>FirstPhoneGroup</groups>
  <!-Zero or more repetitions:->
  <lines>
  <userId>221</userId>
  <uri>221@Uniteme.ezuce.com</uri>
  </lines>
  <!-Optional:->
  </phone>
  </con:AddPhone>
  </soapenv:Body>
  </soapenv:Envelope>

**List of supported phones:** ::

  aastra53i
  aastra55i
  aastra57i
  aastra560m
  aastra sip ip 53i
  audiocodesMP112_FXS
  audiocodesMP114_FXS
  audiocodesMP118_FXS
  audiocodesMP124_FXS
  avaya-1210
  avaya-1220
  avaya-1230
  bria
  ciscoplus7911G
  ciscoplus7941G
  ciscoplus7945G
  ciscoplus7961G
  ciscoplus7965G
  ciscoplus7970G
  ciscoplus7975G
  cisco7960
  cisco7940
  cisco7912
  cisco7905
  cisco18x
  clearone
  gtekAq10x
  gtekHl20x
  gtekVt20x
  gsPhoneBt100
  gsPhoneBt200
  gsPhoneGxp2020
  gsPhoneGxp2010
  gsPhoneGxp2000
  gsPhoneGxp1200
  gsPhoneGxv3000
  gsFxsGxw4004
  gsFxsGxw4008
  gsHt286
  gsHt386
  gsHt486
  gsHt488
  gsHt496
  hitachi3000
  hitachi5000
  hitachi5000A
  ipDialog
  isphone
  karelIP116
  karelIP112
  karelIP111
  karelNT32I
  karelNT42I
  linksys901
  linksys921
  linksys922
  linksys941
  linksys942
  linksys962
  linksys2102
  linksys3102
  linksys8000
  SPA501G
  SPA502G
  SPA504G
  SPA508G
  SPA509G
  SPA525G
  mitel
  nortel11xx
  nortel1535
  lip6804
  lip6812
  lip6830
  polycom321
  polycom320
  polycom330
  polycom331
  polycom335
  polycom430
  polycom450
  polycom550
  polycom560
  polycom650
  polycom670
  polycomVVX1500
  polycom5000
  polycom6000
  polycom7000
  snom300
  snom320
  snom360
  snom370
  snomM3
  unidatawpu7700

Find Phones
~~~~~~~~~~~

**Name:** *findPhone*

**Description:** Find defined phone(s) in the system.

**Input Parameters:** Either null for a listing of all, or provide one of the following optional parameters.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *bySerialNumber*
    - string
    - Required
    - The MAC address of the phone to find
    - Editable
  * - *byGroup*
    - string
    - Optional
    - The phones which are members of the specified group.
    - Editable

**Output Parameters:** An array of 0 or more of the following.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Description**
  * - *serialNumber*
    - string
    - The MAC address of the phone.
  * - *extension*
    - string
    - The dialable extension
  * - *description*
    - string
    - The description of the phone

**Example:** List all defined phones. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindPhone>
  </con:FindPhone>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Find all phones that are members of the phone group FirstPhoneGroup. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindPhone>
  <search>
  <byGroup>FirstPhoneGroup</byGroup>
  </search>
  </con:FindPhone>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Find all phones with the MAC address of 000000000001 ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:FindPhone>
  <search>
  <bySerialNumber>000000000001</bySerialNumber>
  </search>
  </con:FindPhone>
  </soapenv:Body>
  </soapenv:Envelope>

Manage Phones
~~~~~~~~~~~~~

**Name:** *managePhone*

**Description:** Update or delete phones defined in the system.

**Input Parameters:** Either null for a listing of all, or provide one of the following optional parameters.

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *bySerialNumber*
    - string
    - Required
    - Search for a phone by MAC address. May be null.
    - Editable
  * - *byGroup*
    - string
    - Optional
    - Phones which are members of a specified phone group.
    - Editable
  * - *property*
    - 
    - 
    - The name of the phone field to edit.
    - 
  * - *value*
    - 
    - 
    - Value to use for the phone field being edited.
    - 
  * - *deletePhone*
    - boolean
    - Optional
    - Indicates to delete (true) the phone(s). Dependent upon search results.
    - 
  * - *addGroup*
    - string
    - Optional
    - The phone group to add the phone(s) to. Dependent upon search results.
    - 
  * - *removeGroup*
    - string
    - Optional
    - The phone group to remove the phone(s) from. Dependent upon search results.
    - 
  * - *addLine*
    - string
    - Optional
    - userid and uri to add to the phone(s). Dependent upon search results.
    -
  * - *addExternalLine*
    - string
    - Optional
    - userid, dispalyname, password, registrationserver and voicemail of the external line to add to the phone(s). Dependent upon search results.
    - 
  * - *userId*
    - string
    - Required
    - The user portion of the SIP URI and default value for authorization.
    - 
  * - *displayName*
    - string
    - Optional
    - The display name to use for the userId.
    -
  * - *password*
    - string
    - Optional
    - The password for the userid.
    -
  * - *registrationServer*
    - string
    - Required
    - The domain the userid should register to.
    - 
  * - *voicemail*
    - string
    - Optional
    - The voicemail extension for the userid.
    -
  * - *removeLineByUserId*
    - string
    - Optional
    - The userid of the line to remove from the phone(s). Dependent upon search results.
    - 
  * - *removeLineByUri*
    - string
    - Optional
    - The uri of the line to remove from the phone(s). Dependent upon search results.
    - 
  * - *restart*
    - boolean
    - Optional
    - Indicates to restart (true) the phone(s). Dependent upon search results.
    -

**Output Parameters:** Empty Response

**Example:** Delete all phones which are a part of the group FirstPhoneGroup. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManagePhone>
  <search>
  <byGroup>FirstPhoneGroup</byGroup>
  </search>
  <deletePhone>true</deletePhone>
  </con:ManagePhone>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Add line with userid 221 to the phones in FirstPhoneGroup, generate the profiles for the phones and restart them. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManagePhone>
  <search>
  <byGroup>FirstPhoneGroup</byGroup>
  </search>
  <addLine>
  <userId>221</userId>
  <uri>221@Uniteme.ezuce.com</uri>
  </addLine>
  <generateProfiles>true</generateProfiles>
  <restart>true</restart>
  </con:ManagePhone>
  </soapenv:Body>
  </soapenv:Envelope>

**Example:** Remove all the phones in FirstPhoneGroup. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ManagePhone>
  <search>
  <byGroup>FirstPhoneGroup</byGroup>
  </search>
  <removeGroup>FirstPhoneGroup</removeGroup>
  </con:ManagePhone>
  </soapenv:Body>
  </soapenv:Envelope>

Test
----

The test web services are SOAP based services. These services use the Web Service Definition Language (WSDL) to define the interfaces supported.

**URI** ::

  https://host.domain/sipxconfig/services/TestService

**WSDL** ::

  <wsdl:definitions targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:apachesoap="http://xml.apache.org/xml-soap" xmlns:impl="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:intf="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdlsoap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  - <!--
  WSDL created by Apache Axis version: 1.4
  Built on Apr 22, 2006 (06:55:48 PDT)
  -->
  <wsdl:types>
  <schema targetNamespace="http://www.sipfoundry.org/2007/08/21/ConfigService" xmlns="http://www.w3.org/2001/XMLSchema">
  <complexType name="ResetServices">
  <sequence>
  <element maxOccurs="1" minOccurs="0" name="callGroup" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="parkOrbit" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="permission" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="phone" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="user" nillable="true" type="xsd:boolean" />
  <element maxOccurs="1" minOccurs="0" name="superAdmin" nillable="true" type="xsd:boolean" />
  </sequence>
  </complexType>
  <element name="ResetServices" type="impl:ResetServices" />
  </schema>
  </wsdl:types>
  <wsdl:message name="resetServicesRequest">
  <wsdl:part element="impl:ResetServices" name="ResetServices" />
  </wsdl:message>
  <wsdl:message name="resetServicesResponse" />
  <wsdl:portType name="TestService">
  <wsdl:operation name="resetServices" parameterOrder="ResetServices">
  <wsdl:input message="impl:resetServicesRequest" name="resetServicesRequest" />
  <wsdl:output message="impl:resetServicesResponse" name="resetServicesResponse" />
  </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="TestServiceSoapBinding" type="impl:TestService">
  <wsdlsoap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
  <wsdl:operation name="resetServices">
  <wsdlsoap:operation soapAction="" />
  <wsdl:input name="resetServicesRequest">
  <wsdlsoap:body use="literal" />
  </wsdl:input>
  <wsdl:output name="resetServicesResponse">
  <wsdlsoap:body use="literal" />
  </wsdl:output>
  </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ConfigImplService">
  <wsdl:port binding="impl:TestServiceSoapBinding" name="TestService">
  <wsdlsoap:address location="https://47.134.206.174:8443/sipxconfig/services/TestService" />
  </wsdl:port>
  </wsdl:service>
  </wsdl:definitions>

Reset services
~~~~~~~~~~~~~~

**Name:** *resetServices*

**Description:** Resets (deletes) the data associated with one or more web services.

.. warning::
  This is an extremely dangerous service as it could permanently delete large amounts of configuration data. Use extreme caution!

**Input Parameters:**

.. list-table::

  * - **Name**
    - **Value Type**
    - **Required/Optional**
    - **Description**
    - **Editable/Read Only**
  * - *callGroup*
    - boolean
    - Optional
    - Indicates to delete (true) all callGroup (hunt group) data.
    - Editable
  * - *parkOrbit*
    - boolean
    - Optional
    - Indicates to delete (true) all call park orbits.
    - Editable
  * - *permission*
    - boolean
    - Optional
    - Indicates to delete (true) all non-system defined permissions.
    - Editable
  * - *phone*
    - boolean
    - Optional
    - Indicates to delete (true) all defined phones.
    - Editable
  * - *user*
    - boolean
    - Optional
    - Indicates to delete (true) all defined users except superadmin.
    - Editable
  * - *superadmin*
    - boolean
    - Optional
    - Indicates to delete (true) all superadmin data except for the PIN.
    - Editable

**Output Parameters:** Empty response.

**Example:** Remove all hunt groups, park orbits, and permissions defined. ::

  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.sipfoundry.org/2007/08/21/ConfigService">
  <soapenv:Header/>
  <soapenv:Body>
  <con:ResetServices>
  <!-Optional:->
  <callGroup>true</callGroup>
  <!-Optional:->
  <parkOrbit>true</parkOrbit>
  <!-Optional:->
  <permission>true</permission>
  </con:ResetServices>
  </soapenv:Body>
  </soapenv:Envelope>

