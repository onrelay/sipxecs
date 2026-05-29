.. index:: planning

========
Planning
========

Gathering Information
---------------------
You will need to gather a lot of information about the existing infrastructure, physical locations, configuration of equipment, etc. Know who is responsible at the site for any equipment or service configuration changes that may be necessary. Below are a few suggestions to begin with.

Network Diagram
~~~~~~~~~~~~~~~
Find or create a detailed network diagram that includes the make/model of all switches, routers, firewalls, DNS and DHCP servers. Review the current state of exiting wiring, patch panels, distribution closets, etc.

IP Addressing
~~~~~~~~~~~~~
Understand and document the existing network IP addressing scheme, VLANs used, routing, etc.

Internal/External DNS
~~~~~~~~~~~~~~~~~~~~~
The SIP domain name is important to establish up front. The sipXcom DNS service will consider itself authoritative for whatever the domain name is, so be mindful of any conflicts with existing DNS zones. For example if the existing network domain name is the same as your SIP domain name you may need to create MX or important A records manually on the sipXcom side.

SSL Certificates
~~~~~~~~~~~~~~~~
Is there an existing SSL certificate? Who is the provider or technical contact for site certificates?

Telephony Provider
~~~~~~~~~~~~~~~~~~
How will the site connect to the PSTN? Will it be through an analog gateway or SIP trunk? Gather technical contact information for the telco provider, make/model of the gateway or SBC, firmware versions, etc.

Client Requirements
~~~~~~~~~~~~~~~~~~~
Will users register from inside the private network, outside the private network, or both? What make and model phones will be used? Are any existing phones running current firmware versions? Are there any intercom, interconnect, or custom requirements at the site?

Security Concerns
~~~~~~~~~~~~~~~~~
If there will be remote workers (users registering across the WAN and outside the servers local network), what security measures are in place to protect the SIP proxies and registrars?
Who is the technical contact for IT or telecom security at the site?
Will there be signaling or audio encryption requirements (SIPS/SRTP)?
