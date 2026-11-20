***
FAQ
***

Why sipxcom over asterisk, freepbx, etc?
----------------------------------------

The biggest difference is sipxcom proxy is a `stateless proxy <https://tools.ietf.org/html/rfc3261#page-116>`_, where other proxies such as Asterisk are `B2BUAs <https://tools.ietf.org/html/rfc7092>`_.

This means sipxcom is only involved in the call setup. It is never involved in relaying audio or video (RTP) media unless you're using a b2bua function, like :ref:`conferencing` , :ref:`voicemail`, :ref:`auto-attendants`, or :ref:`call-queue`.
Once there is a 200 OK with SDP to a INVITE, and ACK to the 200 OK with SDP, the media (RTP) is direct between phone to phone.

Because of this sipxcom (on sufficient hardware) can handle 10s of thousands of SIP transactions per second, per proxy instance. Some services such as proxy and registrar can run on multiple servers, increasing capability and reliability.

