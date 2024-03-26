include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am

SUBDIRS = \
	.

EXTRA_DIST = \
	$(iantaocbilling_SRC) \
	$(iantaocbilling_RESOURCES)

jardir = @SIPX_JAVADIR@/sipXconfig/plugins
JAR_FILE = iantaocbilling-config.jar
jar_DATA = $(JAR_FILE)

iantaocbilling_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')

iantaocbilling_RESOURCES = \
	iantaocbilling-plugin.properties \
	sipxplugin.beans.xml

iantaocbilling_PKGS = \
	sipxcommons \
	sipxconfig-neoconf \
	sipxconfig-web \
	commons-io \
	commons-lang \
	commons-logging \
	mongo \
	tapestry-annotations \
	tapestry-framework \
	spring-core \
	org.springframework.transaction \
	org.springframework.jdbc \
	spring-context \
	spring-beans \
	jedis \
	spring-data-redis \
	hivemind

iantaocbilling_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons @SIPX_JAVADIR@/sipXconfig,$(iantaocbilling_PKGS))

$(JAR_FILE) : javac-iantaocbilling $(iantaocbilling_RESOURCES)
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(iantaocbilling_RESOURCES))

style_SRC = $(iantaocbilling_SRC)

precommit : style validate;

validate : sipxplugin.beans.xml
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans-2.0.xsd  \
	$^
