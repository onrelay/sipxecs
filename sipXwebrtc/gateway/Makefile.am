include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/config/jetty.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(gateway_SRC)

noinst_DATA = javac-gateway

jardir = @SIPX_JAVADIR@/sipXwebrtc
JAR_FILE = sipXwebrtc-gateway.jar
jar_DATA = $(JAR_FILE)

gateway_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')

gateway_DEPS = \
	$(jetty_DEPS) \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons,$(webrtc_PKGS))

$(JAR_FILE) : javac-gateway
	jar -cf $@ -C $(JAVAROOT) .
