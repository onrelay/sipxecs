EXTRA_DIST = \
	sipxserver.spec.in

serverdir = @SIPX_VARDIR@/sipxserver
BUILD_DIST = ../build/sipXserver/dist

all-local :
	cd $(srcdir) && pnpm install --no-frozen-lockfile
	cd $(srcdir) && pnpm run build

clean-local :
	rm -rf $(BUILD_DIST) $(srcdir)/node_modules

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(serverdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(serverdir)
