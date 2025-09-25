include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(sync_SRC) \
	plugin.xml

noinst_DATA = javac-sync

sync_JAR = sipx-mongo-sync-plugin.jar
jar_DATA = $(sync_JAR)
jardir = @SIPX_JAVADIR@/sipXopenfire/plugins

jar_PKGS = \
	log4j \
	dom4j \
	commons-collections \
	commons-collections4 \
	spring-data-mongodb \
	sipxcommons \
	xmppserver-jakarta \
	commons-lang \
	commons-lang3 \
	bson \
	mongodb-driver-core \
	mongodb-driver-sync
	
	
sync_SRC = $(shell cd $(srcdir); find src -name '*.java')
sync_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons,$(jar_PKGS)) \
	$(mongolib_JAVAROOT)

$(sync_JAR) : javac-sync
	jar -cf $@ \
	  $(call JarInclude,$(mongosync_JAVAROOT),classes) \
	  $(call JarInclude,$(srcdir),plugin.xml)
