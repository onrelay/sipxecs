include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/src/common.am

EXTRA_DIST = \
	$(commons_SRC) \
	$(commons_RESOURCES)

noinst_DATA = \
	javac-commons

commons_SRC = \
	$(shell cd $(srcdir); find org -type f -name '*.java')

commons_RESOURCES = \
	$(shell cd $(srcdir); find -type f -not -name '*.java')

commons_DEPS = \
	$(JAVAROOT) \
	$(call JavaDep, $(top_srcdir)/lib, $(commons_PKGS)) \
	$(commons_runtime_DEPS)
