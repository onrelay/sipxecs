include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am

EXTRA_DIST = \
	$(customcallerid_SRC) \
	$(customcallerid_WEB) \
	$(customcallerid_RES)

jardir = @SIPX_JAVADIR@/sipXconfig/plugins
JAR_FILE = sipxcustomcallerid.jar
jar_DATA = $(JAR_FILE)

customcallerid_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')
customcallerid_WEB = \
	$(shell cd $(srcdir); find plugin -type f)

customcallerid_RES = \
	src/sipxplugin.beans.xml

customcallerid_PKGS = \
	commons-lang \
	commons-io \
	commons-collections \
	commons-logging \
	tapestry-framework \
	tapestry-annotations \
	spring-data-mongodb \
	mongo \
	hivemind \
	sipxcommons \
	sipxconfig-neoconf \
	sipxconfig-web

customcallerid_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons @SIPX_JAVADIR@/sipXconfig,$(customcallerid_PKGS))

$(JAR_FILE) : javac-customcallerid $(customcallerid_WEB) $(customcallerid_RES)
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(customcallerid_WEB))  \
	  $(call JarInclude,$(srcdir)/src,$(subst src/,,$(customcallerid_RES)))

style_SRC = $(customcallerid_SRC)

precommit : validate style;

validate : src/sipxplugin.beans.xml
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans-2.0.xsd  \
	$^
