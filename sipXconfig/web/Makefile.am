include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/config/sipxconfig.am
include $(top_srcdir)/common.am

SUBDIRS = \
	. \
	test

EXTRA_DIST = \
	$(gwt_SRC) \
	context \
	packages \
	$(web_SRC) \
	$(addprefix src/,$(web_RESOURCES)) \
	unitelite

noinst_DATA = java-gwt javac-web 


# G W T
gwt_PKGS = \
	jsinterop-annotations \
	asm \
	asm-commons \
	asm-util \
	colt \
	gwt-user-jakarta \
	gwt-dev-jakarta \
	smartgwt-jakarta \
	smartgwt-skins  \
	tapestry-annotations \
	tapestry-contrib \
	tapestry-framework-jakarta \
	validation-api

gwt_SRC = $(shell cd $(srcdir); find gwt -type f) 

gwt_DEPS = \
	$(srcdir)/gwt/src \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons $(top_srcdir)/lib,$(gwt_PKGS))

# preserved from ant port, probably true on some systems but not verified post port
gwt_JAVACFLAGS = -Xmx512m -Xms256m

java-gwt : $(gwt_SRC)
	$(call JavaCommand,gwt) com.google.gwt.dev.Compiler \
		-style PRETTY \
		-war \
		out/gwt \
		org.sipfoundry.sipxconfig.userportal.user_phonebook_search \
		org.sipfoundry.sipxconfig.userportal.contact_information
	@echo timestamp > $@

	mv out/gwt/org.sipfoundry.sipxconfig.userportal.user_phonebook_search/org.sipfoundry.sipxconfig.userportal.user_phonebook_search.nocache.js out/gwt/org.sipfoundry.sipxconfig.userportal.user_phonebook_search/nocache.js
	mv out/gwt/org.sipfoundry.sipxconfig.userportal.contact_information/org.sipfoundry.sipxconfig.userportal.contact_information.nocache.js out/gwt/org.sipfoundry.sipxconfig.userportal.contact_information/nocache.js

# file in packages get compiled in thru compiling page dart files w/main()s
DART_HOME ?= /opt/dart-sdk

DART_SRC = $(shell cd $(srcdir)/context/WEB-INF; find . -name '*.dart' ! -path '*/packages/*')
DART_PKGS = $(shell cd $(top_srcdir)/packages; find . -name '*.js')

java-dart : $(addprefix $(web_JAVAROOT)/,$(DART_SRC:.dart=.js)) $(addprefix $(web_JAVAROOT)/,$(DART_PKGS))

# Copy .js files from packages/
$(addprefix $(web_JAVAROOT)/,$(DART_PKGS)) : $(web_JAVAROOT)/% : $(top_srcdir)/%
	test -d $(dir $@) || mkdir -p $(dir $@)
	cp $< $@

# Compile .dart files (excluding those under packages/)
$(addprefix $(web_JAVAROOT)/,$(DART_SRC:.dart=.js)) : $(web_JAVAROOT)/%.js : context/WEB-INF/%.dart $(wildcard $(top_srcdir)/lib/*.dart)
	test -d $(dir $@) || mkdir -p $(dir $@)
	$(DART_HOME)/bin/dart compile js -o $@ $<

# W E B
web_COMPILE_PKGS = \
	$(web_PKGS) \
	junit \
	dbunit \
	p6spy \
	junit \
	easymock \
	spring-mock

# use the compiled output dirs when possible in case jar files have not been updated
web_DEPS = \
	$(neoconf_SRCDIR) \
	$(neoconf_JAVAROOT) \
	$(web_SRCDIR) \
	$(web_JAVAROOT) \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons $(top_srcdir)/lib,$(web_COMPILE_PKGS))


web_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')
web_RESOURCES = $(shell cd $(srcdir)/src; find . -type f \( \
	  -name '*.png' \
	  -o -name '*.gif' \
	  -o -name '*.jpg' \
	  -o -name '*.css' \
	  -o -name '*.xml' \
	  -o -name '*.dtd' \
	  -o -name '*.vm' \
	  -o -name '*.properties' \
	  -o -name '*.license' \
	  -o -name '*.js' \
	  -o -name '*.ico' \
	\))

if TEST_MODE
context_TESTPAGE = 
else
context_TESTPAGE = \) -a \( -not -name 'TestPage.*'
endif

context_SRC = $(shell cd $(srcdir)/context; find . -type f \( \
	  -name '*.png' \
	  -o -name '*.gif' \
	  -o -name '*.application' \
	  -o -name '*.jpg' \
	  -o -name '*.page' \
	  -o -name '*.jwc' \
	  -o -name '*.properties' \
	  -o -name '*.html' \
	  -o -name '*.script' \
      -o -name '*.js' \	  
	  -o -name '*.xml' \
	  $(context_TESTPAGE) \
	\))

# W A R
wardir = @SIPX_JAVADIR@/sipXconfig
war_DATA = sipxconfig.war sipxconfig-web.jar

sipxconfig-web.jar : javac-web java-dart $(addprefix src/,$(web_RESOURCES)) $(neoconf_JAR)
	jar cf $@ \
		$(call JarInclude,$(web_JAVAROOT),.) \
		$(call JarInclude,$(srcdir)/src,$(web_RESOURCES))

# create war, then update war.  There's a 'bug' in jar command will create 2 separate ./WEB-INF entries
# in jar file if listed twice. 
sipxconfig.war : java-gwt $(WSDD) $(addprefix context/,$(context_SRC))
	jar cf $@.tmp $(call JarInclude,$(war_ROOT),.)
	cd $(srcdir)/context; jar uf $(abspath $@.tmp) $(context_SRC)		
	mv $@.tmp $@

style_SRC = $(filter-out src/com/%,$(web_SRC))
style_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons $(top_srcdir)/lib,$(web_PKGS))

unitelitedir=@SIPX_VARDIR@/unitelite
install-data-hook :
	cp -r $(srcdir)/unitelite $(DESTDIR)@SIPX_VARDIR@/ 

precommit : style validate-xml

# Not validating
#  src/org/sipfoundry/sipxconfig/site/tapestry.xml 
# because it makes heavy use of SGML's ENTITY and needs to be ported to Schema
#  see http://www.w3.org/TR/xmlschema-0/#usingEntities
SPRING_XML = \
	src/org/sipfoundry/sipxconfig/rest/rest.beans.xml \
	src/org/sipfoundry/sipxconfig/site/about/about.beans.xml \
	test/sipxplugin.beans.xml

validate-xml : $(SPRING_XML)
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans.xsd  \
	$^

clean :
	$(call CleanDirs,\
		$(war_ROOT) \
		$(web_JAVAROOT) \
	)
	$(call CleanFiles,\
		sipxconfig.jar \
		sipxconfig-web.jar \
		sipxconfig.war \
	    javac-web \
	    java-gwt \
	    java-dart \
		wsdl-src \
		$(WSDD) \
		server-config.wsdd.dirty \
		server-config.wsdd \
		server-config-no-spring.wsdd.dirty \
	)	

