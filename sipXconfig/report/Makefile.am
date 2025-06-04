include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am

EXTRA_DIST = \
	$(report_SRC)

reportdir = @SIPX_VARDIR@/reports

report_SRC = $(shell cd $(srcdir); find src -name '*.jrxml')

report_PKGS = \
	ant \
	ant-launcher \
	commons-digester \
	commons-logging \
	commons-collections \
	commons-beanutils-core \
	ant-apache-regexp \
	ant-apache-oro \
	jasperreports \
	regexp \
	jasper-compiler

report_DEPS = $(call JavaDep,@SIPX_JAVADIR@/sipXcommons,$(report_PKGS))

CLEANFILES =