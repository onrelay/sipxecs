include config/utility.am
include config/project.am

EXTRA_DIST = src

dashboarddir=@SIPX_VARDIR@/dashboard
install-data-hook :
	$(INSTALL) -d -m 755 $(DESTDIR)$(dashboarddir)
	cp -r $(srcdir)/src/* $(DESTDIR)@SIPX_VARDIR@/dashboard

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxdashboard.cf

