include $(top_srcdir)/config/utility.am

confdir = $(SIPX_CONFDIR)/sipxcallqueue

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	00_sipxcallqueue.cf

dbpatchdir = $(SIPX_CONFDIR)/database/plugin.d/v12
dist_dbpatch_DATA = \
	call-queue.sql

xml = \
	sipxcallqueue/CallQueueAgent.xml \
	sipxcallqueue/CallQueue.xml \
	sipxcallqueue/CallQueueSettings.xml

dist_conf_DATA = \
	$(xml) \
	sipxcallqueue/CallQueueAgent.properties \
	sipxcallqueue/CallQueue.properties \
	sipxcallqueue/CallQueueSettings.properties

fsdir = $(SIPX_CONFDIR)/freeswitch
dist_fs_DATA = \
	freeswitch/callcenter.conf.xml.vm

precommit : validate-xml

validate-xml : $(xml)
	xmllint --noout $^
