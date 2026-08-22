BUILD_DIST = ../../build/sipXclient/webapp/dist

all-local :
	cd $(srcdir) && npm install --no-audit --no-fund
	cd $(srcdir) && npm run build:make

clean-local :
	rm -rf $(BUILD_DIST) $(srcdir)/build $(srcdir)/node_modules

install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(clientdir)
	cp -r $(BUILD_DIST)/* $(DESTDIR)$(clientdir)
