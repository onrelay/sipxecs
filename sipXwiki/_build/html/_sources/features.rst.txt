.. index:: features

Features
========

System Application Services
---------------------------

All the sipXcom application services are allocated to specific server roles. Using the centralized cluster management system each role can be instantiated on a dedicated server or several (all) roles can be run on a single server. Configuration of all services and participating servers is fully automatic and Web UI based.

  * SIP Session Router, optionally geo-redundant and load sharing
  * Media server for unified messaging and IVR (auto-attendant) services
  * Conferencing server based on FreeSWITCH
  * XMPP Instant Messaging (IM) and presence server (based on Openfire)
  * Contact center (ACD) server
  * Call park / Music on Hold (MoH) server
  * Presence server (Broadsoft and IETF compliant resource list server for BLF)
  * Shared Appearance Agent server to support shared lines (BLA)
  * Group paging server
  * SIP trunking server (media anchoring and B2BUA for SIP trunking & remote worker support)
  * Call Detail Record (CDR) collection & processing server
  * Third party call control (3PCC) server using REST interfaces
  * Management and configuration server
  * Process management server for centralized cluster management

SOA Architecture / Business Process Integration using Web Services
------------------------------------------------------------------

  * Web Services SOAP interfacefor key administrative functions
  * Web Services REST interface for user portal functions and third party call control
  * All components centrally managed using XML RPC
  * Google Web Toolkit (GWT)

Core Calling Features (Telephony Features)
------------------------------------------

  * Transfer (consultative & blind)
  * Call coverage
  * Call hold / retrieve
  * Consultation hold
  * Music on Hold for IETF standards compliant phones
  * User-specific MoH files
  * MoH music from an external streaming source
  * Admin or user configurable Busy Lamp Field (BLF) presence and softkeys
  * Shared Line Appearance / Bridged Line Appearance (Polycom only)
  * Uploadable music file
  * 3-way / 5-way video and voice conference on the phone
  * Call pickup (global and directed call pickup)
  * Call park & retrieve
  * Hunt groups
  * Intercom with auto-answer (bi-directional)
  * SIP URI dialing
  * CLID (Calling Line Identification)
  * CNIP (Calling party Name Identification Presentation)
  * CLIP (Call Line Identification Presentation)
  * CLIR (Call Line Identification Restriction)
  * Per gateway CLIP manipulation
  * Call waiting / retrieve
  * Do not Disturb (DnD)
  * Forward on busy, no answer, do not disturb
  * Multiple line appearances
  * Multiple calls per line
  * Multiple station appearance
  * Outbound call blocking - Calls from phones to PSTN numbers, or classes of numbers, can be blocked based on:
	- The destination of the call; for example, when a user or device cannot initiate an international long distance call.
	- The source of the call; for example, when a lobby phone can only initiate calls to internal numbers.
  * Click-to-call
  * Redial
  * Call history (dialed, received, missed)
  * Auto off-hook / ring down
  * Incoming only
  * Configuration of individual Speed Dial softkeys
  * Auto-generation of directory information

E911 Emergency Response
-----------------------

  * Internal notification using email and SMS

Remote Branch office support
----------------------------

  * Centralized deployment: Branch only provides phones and optionally PSTN gateway for failover, reduced WAN BW consumption or E911 calls
  * Distributed deployment: Branch provides full call server with SIP site-to-site dialing between offices
  * Branch office locations can be defined in the mgmt UI with a postal address
  * Users, phones, gateways, SBC, and servers can be assigned to a branch location
  * A PSTN gateway can be available for calls that originate in a specific branch only or for general use
  * Source routing allows call routing based on location (branch local calls are routed through local gateway preferably)
  * Branch postal address automatically proliferates to user's office address
  * Survivable branch configuration possible with Audiocodes gateways SAS functionality (auto-configured)
  * Certain sipXcom services can be deployed in the branch as part of the cluster (e.g. conferencing)

Enterprise Instant Messaging (IM) and Presence
----------------------------------------------

  * XMPP based IM and presence server based on Openfire
  * Supports XMPP standards based clients
  * Auto-configuration of user's IM accounts
  * Auto-configuration of IM user groups
  * Personal group chat room for every user auto-configured
  * Federation of phone presence with IM presence
  * Customizable "on the phone" presence status message
  * Dynamic call routing based on user's presence status
  * Message archiving and search for compliance (pending)
  * Server-to-server XMPP federation
  * Optional secure client connections
  * Client-to-client file transfer
  * Group chat rooms
  * XMPP search
  * Integration of user profile information and avatar (pending)

Personal Assistant IM Bot
-------------------------

  * My Buddy Personal Assistant feature
  * Dynamic call control using IM
  * Dynamic conference management using IM
  * Unified messaging management using IM
  * Call history / missed calls
  * Call initiation using corporate dialplan
  * Corporate directory look-ups

Presence and IM Federation
--------------------------

  * Server side federation with other public XMPP IM systems
  * Allows group chat sessions across systems
  * Allows message archiving (if enabled) across systems
  * User self-administration of credentials for other IM systems

Fixed Mobile Convergence (FMC) Application
------------------------------------------

  * 3rd Party FMC application with the following functionality:
  * Enterprise number dialing
  * System call-back saves on wireless toll charges
  * Corporate directory look-ups
  * Call history
  * Presence sharing
  * IM

Web Conferencing & Collaboration
--------------------------------

  * Commercial options available through eZuce's viewme and viewme Cloud products

User Self-Control (User Web Configuration Portal)
-------------------------------------------------

  * Every user on the system gets access to a personal Web user portal for self-management and control
  * Management of unified messaging (voicemail)
  * Configuration of unified messaging preferences
  * Time based find-me / follow-me
  * Flexible configuration of call forwarding
  * Management of personal profile data including avatar
  * Personal call history
  * Personal phone book, speed dial and presence management
  * Click-to-call
  * Individual phone management
  * Personal auto-attendant
  * Management of personal IM account
  * Personal MoH music upload and preferences

Superior Voice Quality
----------------------

  * Peer-to-peer media routing for best quality (media not routed through the sipXcom server)
  * Unmatched voice quality with lowest delay and jitter
  * Support for any codec supported by the phone or gateway (including video)
  * Support for HD Voice (Polycom and other phones)
  * Codec negotiation (no transcoding required)
  * Conferencing, auto-attendant and voicemail support HD voice w/ transcoding if necessary

User Management
---------------

  * Create a user, provision a phone and assign a line in only three clicks - easy!
  * Numeric or alpha-numeric User ID
  * User PIN management (UI or TUI)
  * Aliasing facility (numeric and alpha-numeric aliases)
  * Extension and alias uniqueness assurance
  * Management or auto-assignment of user's IM ID and display name
  * Automatic IM buddy list creation based on user groups
  * Granular per user permissions

Call permissions
----------------

  * 900 Dialing
  * International Dialing
  * Long Distance Dialing
  * Mobile Dialing
  * Local Dialing
  * Toll Free Dialing

System permissions
------------------

  * User has voicemail inbox
  * User listed in auto-attendant directory
  * User can record system prompts
  * User has superuser access
  * User allowed to change PIN from TUI
  * User can use Microsoft Exchange VM
  * User has a personal auto-attendant
  * Custom permissions as defined by the admin
  * Supervisor permission for groups (e.g. Call Center supervisor)
  * Management of user contact record (user profile)
  * Comprehensive profile data
  * Work and home address
  * In-building location information
  * Assistant information
  * Support for avatar including support for gravatar
  * SIP password management for security
  * User groups with group properties
  * Per user call forwarding (follow me)
	- To local extension, PSTN number, or SIP address
	- Based on user or admin defined time schedules
	- Parallel or serial ring
	- Allows definition of ring time before trying next number
	- Allows several forwarding destinations
	- Follow-me configuration using user portal

  * Extension pool with automatic assignment
  * Per user Caller ID (CLID) assignment

Dial Plan
---------

  * Easy to use GUI based dial plan manipulation
  * Time-based dialing rules with different admin defined schedules
  * Rules based least cost routing
  * Dynamic call routing based on user's IM presence status
  * Directly route to voicemail on IM status DND
  * Dynamically add forwarding destination based on phone number in custom presence status
  * Automatic gateway redundancy and fail-over
  * Specific E911 routing
  * Permission based rules
  * Prefix manipulation
  * Dialplan templating for international dial plans
  * Built-in support for U.S., German, Swiss, and Polish local dial plans (Any other local dial plan can be added as a plugin)
  * Specify internal extension length
  * Specific rule for site-to-site call routing between SIP systems
  * Redirector plugins - any imaginable dial rule can be added as a plugin

Internet Calling
----------------

  * Ability to configure SIP URI based call routing to other domains
  * Specific SBC selection for call routing
  * Configuration of native NAT traversal w/ optionally redundant media anchoring if necessary
  * Media anchoring supports voice and video for any codec

Directory, Softkeys, Speed Dial
-------------------------------

  * Automated generation of directory information per user or per user group
  * Support for complete contact information and user profile, including avatar
  * Crreation and Management of many different directories (per user, per user group, per location, etc.)
  * Upload of contacts from GMail and Outlook
  * User management of directory information
  * Automated provisioning of directory information into user's phones
  * Allows adding contacts to the directory from a .csv file (Excel)
  * User configurable speed dial (internal / external numbers, SIP URIs)
  * Speed dial generated server side and backed up
  * Auto-provisioning of speed dial to phones
  * User configuration of Busy Lamp Field (BLF) to monitor presence of other users or phones (e.g. attendant console)

PSTN Trunking
-------------

  * Unlimited number of PSTN gateways and trunk lines
  * Supports most SIP compliant gateways (e.g. Audiocodes, Mediatrix, Sangoma, Patton, etc.)
  * Gateways can be in any location
  * Gateway selection per dialing rule
  * Source routing of calls so that calls can be routed through a local gateway to save WAN bandwidth
  * DID
  * Local DID per gateway
  * DNIS
  * CLIP Management
	- User CLIP
	- Gateway default CLIP
	- Prefix stripping / appending

  * Per gateway CLIR
  * Automatic Route Selection (ARS)
	- Implemented with XML-formatted mapping rules.
	- Mapping values re-write SIP URLs to specify the next hop or destination for a SIP message that has been received by the Communications Server component.
	- Direct messages to different SIP/PSTN trunk gateways, either on premise or at a remote premise location, based on any portion of SIP URL or E.164 number.
	- Route messages to commercial SIP/PSTN service providers, which reduces or eliminates the need for on-premise trunk gateways.
  * Least-cost routing (LCR)
  * Automatic failover if unavailable
  * Automatic failover if busy
  * Inbound FAX support
  * Mixing of PSTN and SIP trunks with least cost routing

SIP Trunking
------------

  * Basic SIP trunking gateway w/ NAT traversal
  * Remote worker support w/ near-end and far-end NAT traversal and auto-detection
  * ITSP templates for simplified configuration. Interop (not certified) with the following ITSPs. Many other ITSP are compatible, see SIP Trunking section
	- BT (UK)
	- AT&T
	- Bandwidth.com
	- CBeyond
	- Bandtel
	- CallWithUs
	- Eutelia (Italy)
	- LES.NET
	- SIPcall (Switzerland)
	- Vitality
	- VOIPUser (UK)
	- VOIP.MS
	- Appia
  * SIP interop with Nortel CS1000 R6
  * SIP call origination & termination
  * Branch office routing
  * Proxy to proxy interconnect using ACLs
  * Least-cost-routing (LCR)
  * Mixing of PSTN trunks with SIP trunks
  * TLS support for secure signaling
  * Route header for flexible call routing through an SBC
  * Flexible rules for SBC selection (route selection)
  * Support for Skype for Business SIP trunking

Integration with Microsoft Active Directory and Exchange
--------------------------------------------------------

  * Synchronization with Microsoft Active Directory
	- Using LDAP interface
	- On demand or automatically based on a schedule
	- Graphical query design combines ease of use with flexibility
	- Allows preview of records to be imported

  * Dialplan integration with Microsoft Exchange voicemail server
	- Allows mixed environment with groups of users on Exchange or the sipXcom VM server
	- Permission based selection of VM server per user or user group
	- Automatic dialplan routing to Exchange VM

  * Enables sll speech based Exchange capabilities

Supported Softclients
---------------------

Combined SIP and XMPP clients
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Jitsi and Counterpath Bria clients can be used with the provisioning server for automated mass deployment of SIP and XMPP account setup.

  * `Counterpath <https://www.counterpath.com/>`_ Bria professional
  * `Jitsi <https://jitsi.org/>`_

XMPP (IM only) clients
~~~~~~~~~~~~~~~~~~~~~~

  * `Pidgin <https://pidgin.im/>`_
  * `Trillian <https://trillian.im/>`_
  * `Spark <https://igniterealtime.org/projects/spark/>`_

Analog Gateways (FXO and FXS)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  * Supports any SIP compliant FXO or FXS gateway
  * Analog fax machines FXS gateways
  * Analog cordless phone support with FXS gateways
  * Plug & play management of many analog gateway models

Performance
-----------

  * Unlimited number of simultaneous calls (voice, HD voice, video) - only depends on LAN/WAN bandwidth
  * 54,000 BHCC, 120,000 BHCC two-way redundant (depends on server HW)
  * Up to three-way redundant configuration using cluster mgmt Web GUI
  * Up to 10,000 users per dual-server HA system
  * Tested up to 10,000 IM users
  * 450 simultaneous calls through the SIP trunking gateway require < 20% CPU on dual core system
  * Up to 500 simultaneous conferencing ports per server
  * Up to 300 media server ports for unified messaging (supports 15,000 users)
  * Automatic time distribution of re-registration and subscription events

High Availability
-----------------

  * Optionally fully redundant call control system
  * Geo-redundant SIP session manager
  * Based in DNS SRV (no cluster required)
  * Load balance under normal operating conditions
  * Geographic dispersion of redundant systems
  * Real-time synchronization of state information
  * Automatic recovery after server failure
  * Reports on load distribution

Call Detail Records collection and reporting
--------------------------------------------

  * Call State Events (CSE) collected for all signaling activity
  * Processing of CSEs into CDRs
  * All data stored in a database at all times
  * Flexible report generation using Jasper Reports, built-in
  * Supports redundant call control
  * Determines and records call type information
  * Internal / external calls
  * Calls to specific sipXcom services
  * Collates call legs
  * Historic Call Detail Record reporting in real-time
  * Additional reports using call type info
  * Monitoring of currently active (on-going) calls
  * Export of active and historic CDRs to Excel (.csv file)
  * Direct database access for reporting application (e.g. Crystal Reports, Jasper Reports)
  * SOAP Web Services access to CDR data
  * Individual call history per user in the user portal

Security
--------

  * All outbound calls authenticated
  * Secure user password management
  * DoS attack prevention
  * HTTPS secure Web access
  * TLS based signaling for SIP trunks
  * HTTPS secures non-SIP communication between sipX components.
  * HTTPS secures communications between sipX components and admin and user consoles.
  * Secure channel for retrieving messages from voicemail repository.
  * HTTP digest authentication for SIP signaling, as specified in RFC 2617, is used for authentication challenges between SIP endpoints and sipX components.
  * HTTP digest implementation supports MD5.

System Administration Features
------------------------------

  * Browser based configuration and management
  * Several admin accounts
  * Notification when new version or patches are available
  * GUI based software upgrade
  * GUI based certificate management
  * LDAP integration
  * Integration with Microsoft Exchange 2007 for voicemail and Active Directory
  * SOAP Web Services interface
  * CSV import and export of user and device data
  * Administration of Instant Messaging (IM) and Presence settings
  * Integrated backup & restore
  * Scheduled backups
  * Diagnostics
	- Display active registrations
	- Display job status
	- Status of services
	- Snapshot logs for debugging
	- Logging (customizable log levels, message log per service)
	- Display active calls
  * Domain Aliasing
  * Support for DNS SRV
  * Support for DNS NAPTR based call routing
  * Automatic restart after power failure
	- Single sipXcom application can start all other application processes associated with starting up sipXcom, including dependent processes that must be started in particular order.
	- Configured from browser interface
  * Login history report (successful and unsuccessful)
  * Automated testing of network services (DHCP, DNS, NTP, TFTP, FTP, HTTP) for proper configuration

Plug & Play Device Management
-----------------------------
  * Auto-discovery of phones & gateways on the LAN
  * Auto-registration of Polycom phones simplifies installation
  * Plug & play management of phones
  * Plug & play management of PSTN gateways
  * Auto-generation of phone / gateway config profile
  * Auto-pickup of profile by phone / gateway
  * Centralized management of all the parameters
  * Centralized backup and restore of all the configs
  * Auto-generation of lines by assigning users to devices
  * Device group management & properties
  * Firmware upgrade management

Unified Messaging (Voicemail)
-----------------------------

  * Integrated unified messaging system
  * Localized per user by installing language packs
  * Number of voicemail boxes only limited by disk size (tested up to 10,000)
  * Performance tested up to 300 simultaneous calls (ports) on dual core server
  * IMAP back-end connection
  * Acts as an IMAP client into MSFT Exchange and other compatible email systems
  * User manageable credentials for IMAP federation
  * Properly controls MWI on the phone when message is "read" using the email client
  * Browser based user portal for unified messaging management
  * RSS feed for new messages
  * Message Waiting Indication (MWI)
  * User configurable distribution lists
  * Group and system distribution lists
  * Unified Messaging:
  * Email notification of new voicemail messages
  * Forwarding of message as .wav file
  * Supports several parallel notifications
  * IMAP client into Exchange
  * Per user selectable templates for email format used when forwarding voicemail
  * Manage folders: Folders for message organization
  * Manage greetings: Multiple customizable greetings
  * Operator escape from anywhere
  * Remote voicemail access using a phone
  * SOA Web Services (REST) access to messages and greetings
  * Unlimited number of inboxes
  * Auto-removal of deleted messages

Personal Auto Attendant
-----------------------

  * User configurable personal auto-attendant for every user on the system
  * Up to 10 individual forwarding choices (keys 0 through 9)
  * User can record greeting that corresponds with key configuration
  * Individual zero-out to a personal assistant or receptionist
  * Individual selection of language based on installed language packs
  * Personal greeting

Auto Attendant Features
-----------------------

  * Unlimited number of auto-attendants
  * Dial by extension and name
  * Night and holiday service
  * Special auto-attendant
  * Transfer on invalid response
  * Nested auto-attendants (multi-level)
  * Fully customizable actions:
	- Operator
	- Dial by Name
	- Repeat Prompt
	- Voicemail login
	- Disconnect
	- Auto-Attendant
	- Goto Extension
	- Deposit Voicemail
  * Uploadable custom prompts
  * Configurable DTMF handling

Presence Server Features
------------------------

  * Compatible with Broadsoft or IETF implementations
  * Centralized management of resource lists for dialog events
  * Busy Lamp Field (BLF) feature based on presence
  * Used to support shared lines (BLA)
  * Presence federated with IM presence to show "on the phone" status
  * Support for 3rd party Attendant Consoles (such as Voice Operator Panel)

Hunt Groups
-----------

  * Unlimited number of hunt groups
  * Serial and parallel forking (rings sequentially or at the same time)
  * Configurable ring time per attempt
  * Enable / disable user call forwarding rules while hunting
  * Flexible configuration of destination if no answer

Call Park Server
----------------

  * Unlimited number of park orbits
  * Visual indication on the phone of the state of the park orbit using the presence server (BLF)
  * Music on park
  * Uploadable music file
  * Configurable call retrieve code
  * Configurable call retrieve timeout
  * Automatic park timeout with configurable time
  * Configurable park escape key
  * Allow multiple calls on one orbit

Group Paging Server
-------------------

  * Integrated group paging server
  * Unlimited number of paging groups
  * Supports regular SIP phones using auto-answer
  * Supports dedicated in-ceiling devices (SIP)
  * Configurable paging prefix

Conferencing Server
-------------------

  * Voice conferencing server that can run on the same sipXcom server or on dedicated hardware
  * Support for voice conferencing
  * Each user on the sipXcom system can have a personal conference bridge
  * Recording of conference calls
  * Dynamic conference controls from the user's Web portal (user portal)
  * Dynamic conference control using IM
  * Participant entry / exit messages
  * Roll call
  * Mute, isolate, disconnect, invite
  * Association of personal conference bridge with personal group chat room
  * Automatic migration of group chat to a voice conference using the @conf directive
  * Support for HD Audio and transcoding if necessary
  * Support for up to 500 ports of conferencing, dependent on hardware
  * Configurable DTMF keys for conference controls using the TUI
  * A sipXcom IP PBX system can have more than one conference server if more capacity is needed
  * All conferencing servers and services centrally managed and configured
  * Conferencing based on FreeSWITCH

Call Queueing (ACD)
-------------------

  * ACD server collocated or on a different server hardware
  * Several (unlimited) queues per server
  * Several lines per queue
  * Support trunk lines (many calls per line) or single call per line
  * Dedicated overflow queues or overflow to hunt group or voicemail
  * Configurable call routing scheme per queue:
  * Ring all
  * Circular
  * Linear
  * Longest idle
  * Agent presence monitor using presence server
  * Separate welcome and queue audio
  * Call termination tone or audio
  * Configurable answer mode
  * Agent wrap-up time
  * Auto sign-out of agents if calls are not answered
  * Configurable maximum ring delay
  * Configurable maximum queue length
  * Configurable maximum wait time until overflow condition
  * Unlimited number of agents per queue

sipXcom Managed Devices
-----------------------

Almost any SIP compatible phone works with sipXcom if configured manually (i.e. by logging into the phone's Web interface to configure it one phone at a time). The following devices are plug & play managed automatically and centrally by sipXcom:

  * Polycom SoundPoint all models (IP 301, 320, 330, 430, 450, 501, 550, 560, 601, 650, 670)
  * Polycom SoundStation IP 4000, 6000, 7000 SIP
  * Polycom VVX phones (300/310, 400/410, 500, 600, 1500)
  * Audiocodes gateways MP112, MP114, MP118, MP124 FXS
  * Audiocodes gateways FXO and PRI/BRI
  * Counterpath Bria Professional

sipXcom Managed Devices (Community supported)
---------------------------------------------

Community supported means that the phone plugin for plug & play management is provided as is. These phone plugins are provided and maintained by community members. Some system functionality might not be implemented or supported.

  * Aastra 53i, 55i, 57i
  * Snom 300, 320, 360, 370 up to firmware 7.x
  * Grandstream BudgeTone, HandyTone
  * Grandstream GXP2000, GXP1200, GXP2010, GXP2020
  * Grandstream GXV3000 Video Phone
  * Hitachi IP3000 and IP5000 WiFi phones
  * Cisco ATA 186/188
  * Cisco 7960, 7940, 7912, 7905
  * Cisco 7911, 7941, 7945, 7961, 7965, 7970, 7975
  * ClearOne MaxIP Conference Phone
  * LG-Nortel LG 6804, 6812, 6830
  * Nortel video phone 1535
  * Linksys ATA 2102, ATA 3102
  * Linksys SPA8000
  * Linksys SPA901, SPA921, SPA922, SPA941, SPA942, SPA962
  * Nortel 1120 / 1140 SIP
  * G-Tec AQ10x, HL20x, VT20x

Centrally Managed sipXcom Distributed System (cluster)
------------------------------------------------------

  * Automated installation and configuration of a distributed system with specific server roles
  * Automated and central configuration of a high-availability redundant sipXcom system
  * Allows for dedicated server hardware for conferencing, voicemail, ACD Call Center, and Call Control
  * All configuration for remote servers is centrally generated and distributed securely

SIP Implementation
------------------

This is probably quite an incomplete list. In any case, sipXcom IP PBX is fully SIP standards compliant.

  * RFC 3261 Session Initiation Protocol using both UDP and TCP transports
  * Advanced call control using RFCs
	- RFC 3515 Refer Method
	- RFC 3891 Referred-By header
	- RFC 3892 Replaces header
  * Provide for consultative and blind transfer and third party call controls
	- Blind transfer (Unannounced) to a different phone without speaking to the other phone prior to transfer.
	- Consultative transfer (announced) to a different phone without speaking to the other phone prior to transfer.
  * RFC 3263 Locating SIP Servers - use of DNS SRV records for call routing control and server redundancy.
  * RFC 3581 Symmetric Response Routing (rport)
  * RFC 3265 SIP Event Notification - for phone configuration and
  * RFC 3842 Voice mail message waiting indication (MWI)
  * RFC 3262 Reliable Provisional Responses
  * RFC 2833 Out-of-band DTMF tones
  * RFC 3264 Offer/Answer model for SDP for Codec Negotiation
  * RFC 2617 HTTP Authentication: Basic and Digest Access Authentication
  * RFC 3327 Path header
  * RFC 3325 P-Asserted identity
  * RFC 4235 An INVITE-Initiated Dialog Event Package for the Session Initiation Protocol (SIP)
  * RFC 4662 A Session Initiation Protocol (SIP) Event Notification Extension for Resource Lists
  * RFC 2327 SDP: Session Description Protocol
  * RFC 3326 The Reason Header Field for the Session Initiation Protocol (SIP)
  * Early media (SDP in 180/183)
  * Delayed SDP (SDP in ACK)
  * Re-INVITE: Codec change, hold, off-hold
  * Route/Record-Route header fields
  * Configurable RTP/RTCP ports
  * Configurable SIP ports
  * BLA support
  * RFC 3680: A Session Initiation Protocol (SIP) Event Package for Registrations
  * RFC 3265: Session Initiation Protocol (SIP)-Specific Event Notification
  * draft-ietf-sipping-dialog-package-06
  * draft-anil-sipping-bla-02
  * XMPP Compliance
  * RFC 3920: XMPP Core
  * RFC 3921: XMPP IM
  * XEP-0030: Service Discovery
  * XEP-0077: In-Band Registration
  * XEP-0078: Non-SASL Authentication
  * XEP-0086: Error Condition Mappings
  * XEP-0073: Basic IM Protocol Suite
  * XEP-0004: Data Forms
  * XEP-0045: Multi-User Chat
  * XEP-0047: In-Band Bytestreams
  * XEP-0065: SOCKS5 Bytestreams
  * XEP-0071: XHTML-IM
  * XEP-0096: File Transfer
  * XEP-0115: Entity Capabilities
  * XEP-0004: Data Forms
  * XEP-0012: Last Activity
  * XEP-0013: Flexible Offline Message Retrieval
  * XEP-0030: Service Discovery
  * XEP-0033: Extended Stanza Addressing
  * XEP-0045: Multi-User Chat
  * XEP-0049: Private XML Storage
  * XEP-0050: Ad-Hoc Commands
  * XEP-0054: vcard-temp
  * XEP-0055: Jabber Search
  * XEP-0059: Result Set Management
  * XEP-0060: Publish-Subscribe
  * XEP-0065: SOCKS5 Bytestreams
  * XEP-0077: In-Band Registration
  * XEP-0078: Non-SASL Authentication
  * XEP-0082: Jabber Date and Time Profiles
  * XEP-0086: Error Condition Mappings
  * XEP-0090: Entity Time
  * XEP-0091: Delayed Delivery
  * XEP-0092: Software Version
  * XEP-0096: File Transfer
  * XEP-0106: JID Escaping
  * XEP-0114: Jabber Component Protocol
  * XEP-0115: Entity Capabilities
  * XEP-0124: HTTP Binding
  * XEP-0128: Service Discovery Extensions
  * XEP-0138: Stream Compression
  * XEP-0163: Personal Eventing via Pubsub
  * XEP-0175: Best Practices for Use of SASL ANONYMOUS
