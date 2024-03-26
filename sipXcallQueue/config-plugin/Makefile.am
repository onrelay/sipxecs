include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/config/sipxconfig.am

SUBDIRS = \
	. \
	test

EXTRA_DIST = \
	plugin \
	src

jardir = @SIPX_JAVADIR@/sipXconfig/plugins
JAR_FILE = sipxcallqueue-config.jar
jar_DATA = $(JAR_FILE)

callqueue_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')
callqueue_RESOURCES = $(shell cd $(srcdir)/src; find -type f -not -name '*.java')

plugin_RESOURCES = \
	$(shell cd $(srcdir); find plugin -type f)

callqueue_PKGS = \
	sipxconfig-neoconf \
	sipxconfig-web \
	$(sipxconfig_web_PKGS)

callqueue_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons @SIPX_JAVADIR@/sipXconfig,$(callqueue_PKGS))

$(JAR_FILE) : javac-callqueue $(addprefix src/,$(callqueue_RESOURCES))
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(plugin_RESOURCES)) \
	  $(call JarInclude,$(srcdir)/src,$(callqueue_RESOURCES))

# style checks java source code coding style agains proper sipxecs conforming. Not nec.
# but you will probably find very useful
style_SRC = $(callqueue_SRC)

precommit : style validate;

validate : src/sipxplugin.beans.xml
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans-2.0.xsd  \
	$^
