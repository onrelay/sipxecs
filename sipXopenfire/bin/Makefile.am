include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = $(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in) \
	$(conf_DATA:=.in)

confdir = @SIPX_LIBEXECDIR@
conf_DATA = \
	openfire-config \
	openfire-config-functions

$(conf_DATA) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,plugin_PKGS provider_PKGS_RT)

initddir = /etc/init.d
initd_SCRIPTS = \
	sipxopenfire

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxopenfire.service

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(conf_DATA) $(initd_SCRIPTS) $(systemd_SCRIPTS) 
