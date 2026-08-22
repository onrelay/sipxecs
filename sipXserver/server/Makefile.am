BUILD_DIST = ../../build/sipXserver/server/dist

all-local :
	cd $(srcdir) && npm install --no-audit --no-fund
	cd $(srcdir) && npm run build

clean-local :
	rm -rf $(BUILD_DIST) $(srcdir)/node_modules

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(serverdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(serverdir)
