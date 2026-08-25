EXTRA_DIST = \
	sipxdao.spec.in

BUILD_DIST = ../build/sipXdao

all-local :
	cd $(srcdir) && pnpm install --no-frozen-lockfile
	cd $(srcdir) && pnpm run build:make

clean-local :
	rm -rf $(BUILD_DIST) $(srcdir)/build $(srcdir)/node_modules
