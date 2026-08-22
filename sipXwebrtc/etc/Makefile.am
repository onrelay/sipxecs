include $(top_srcdir)/../config/utility.am

EXTRA_DIST = \
	$(conf_DATA:=.in)

confdir = @SIPX_CONFDIR@
conf_DATA = \
	sipxwebrtc.properties

$(conf_DATA) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

DISTCLEANFILES = $(conf_DATA)

cfgdir = $(SIPX_CONFDIR)/webrtc

dist_cfg_DATA = \
	webrtc/line.properties \
	webrtc/line.xml \
	webrtc/phone.properties \
	webrtc/phone.xml \
	webrtc/provisioning.json.vm \
	webrtc/types.xml

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxwebrtc.cf