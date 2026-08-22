SUBDIRS = \
	webapp

EXTRA_DIST = \
	sipxclient.spec.in

clientdir = @SIPX_VARDIR@/sipxclient
BUILD_DIST = ../build/sipXclient/webapp/dist

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(clientdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(clientdir)
