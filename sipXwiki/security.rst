.. index:: security

===================
Security 
===================

sipXcom supports both secure signaling (TLS) and encrypted media (SRTP) for both trunks and extensions.

If you want to use SRTP for encrypted media, you must ensure ALL endpoints connected to sipXcom support SRTP, or calls may fail to connect.

Secure Trunking
----------------------

sipXcom supports secure trunking in its internal SBC on port 5081. 

These are the sipXcom config changes required to enable secure trunking for both signaling (TLS) and media (SRTP):

- Under Gateway configuration, select TLS as transport protocol and connect to the ITSP using a security enabled port such as 5061. 

- The remote ITSP should connect to port 5081 on sipXcom. 

- Under System - Services - Media Services - Server check Secure RTP if you want to encrypt media with SRTP.

.. note::
  *  The Letsencrypt Web SSL security certs under Security settings are automatically reused for the internal SBC.
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
 * Unlike secure trunking, extensions use self-signed SSL certs as configured under SIP certs under Security settings.
 * This means SIP extensions must have SSL cert validity checks disabled. 

To check port 5061 is enabled to receive TLS connections, you may run the following command:
*   .. code-block:: bash

    openssl s_client -connect <sipXcom IP or domain>:5081                       

