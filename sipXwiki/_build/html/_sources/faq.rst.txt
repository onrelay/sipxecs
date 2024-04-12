***
FAQ
***

Why sipxcom over asterisk, freepbx, etc?
----------------------------------------

The biggest difference is sipxcom proxy is a `stateless proxy <https://tools.ietf.org/html/rfc3261#page-116>`_, where other proxies such as Asterisk are `B2BUAs <https://tools.ietf.org/html/rfc7092>`_.

This means sipxcom is only involved in the call setup. It is never involved in relaying audio or video (RTP) media unless you're using a b2bua function, like :ref:`conferencing` , :ref:`voicemail`, :ref:`auto-attendants`, or :ref:`call-queue`.
Once there is a 200 OK with SDP to a INVITE, and ACK to the 200 OK with SDP, the media (RTP) is direct between phone to phone.

Because of this sipxcom (on sufficient hardware) can handle 10s of thousands of SIP transactions per second, per proxy instance. Some services such as proxy and registrar can run on multiple servers, increasing capability and reliability.

Compare against Asterisk where `their wiki <https://wiki.asterisk.org/wiki/display/~mmichelson/SIP+performance+notes>`_ indicates the calls per second rate is somewhere between 30 to 40 on a HP DL360, and it is standalone.
At the time of this writing, that wiki entry was last modified Jan 31st of 2011. In 2011 a (Gen 7) HP DL360 would support a max of two Intel socket FCLGA1366 (Xeon 55xx) processors and 384GB of RAM.
