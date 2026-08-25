BUILD_DIST = ../build/sipXclient/dist

all-local :
	cd $(srcdir) && pnpm install --no-frozen-lockfile
	cd $(srcdir) && pnpm run build:make

clean-local :
	rm -rf $(BUILD_DIST) $(srcdir)/build $(srcdir)/node_modules

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(clientdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(clientdir)
