SUBDIRS = \
	server

EXTRA_DIST = \
	sipxserver.spec.in

serverdir = @SIPX_VARDIR@/sipxserver
BUILD_DIST = ../build/sipXserver/server/dist

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(serverdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(serverdir)
