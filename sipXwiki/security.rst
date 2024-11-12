.. index:: security

===================
Security 
===================

sipXcom supports a secure web interface, secure trunking and secure extensions via standard HTTPS, TLS, and SRTP protocols.

Certificates
----------------------

SSL certificates for sipXcom are configured under Settings - Security - Certificates.

Here you can enable a Let's Encrypt service that automatically generates and installs a valid SSL web certificate. Let's Encrypt certificates are authorized by the Internet Security Research Group (ISRG Root X1).
You may also import your own web certificates. 

sipXcom also supports both secure signaling (TLS) and encrypted media (SRTP) for both trunks and extensions. 
If you want to use SRTP for encrypted media, you must ensure all endpoints connected to sipXcom support SRTP, or calls may fail to connect.

.. note::
  * The Let's Encrypt web certificate is reused in the sipXcom built in SBC used for SIP trunking.
  * SIP extensions use automatically generated and auto-provisioned self-signed SSL certs.


Secure Trunking
----------------------

sipXcom supports secure trunking for its built in SBC on port 5081. 

These are the sipXcom config changes required to enable secure trunking for both signaling (TLS) and media (SRTP):

- Under Gateway configuration, select TLS as transport protocol and connect to the ITSP using a security enabled port such as 5061. 

- The remote ITSP should be configured to connect secure trunks to port 5081 on sipXcom. 

- Under System - Services - Media Services - Server check Secure RTP if you want to encrypt all media with SRTP.

.. note::
  *  The sipXcom SBC supports SDES type SRTP media negotiations, not DTLS as is common with WebRTC.

To test you have a valid public SSL cert on your SBC port 5081, run the following command:

  .. code-block:: bash

    openssl s_client -connect <sipXcom IP or domain>:5081                       


Secure Extensions
----------------------

Extensions may also connect securely to sipXcom's SIP proxy on port 5061 (default). 

- If you autoprovision phones, make sure they are configured to use TLS as outbound proxy transport and connect to port 5061 on sipXcom. 
- E.g. for Polycoms, under Security, select both Enable SRTP and Require SRTP

.. note::
 * Since SIP extension certificates are self generated, IP phones using TLS must have SSL cert validity checks disabled. 

To check port 5061 is enabled to receive TLS connections, you may run the following command:
  
  .. code-block:: bash

    openssl s_client -connect <sipXcom IP or domain>:5081                       

