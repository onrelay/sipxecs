AC_PREREQ(2.57)
AC_INIT(sipxcustomcallerid, 19.04, sipx-dev@list.sipfoundry.org)
AC_CONFIG_AUX_DIR(config)
AM_INIT_AUTOMAKE(foreign tar-ustar)
m4_include([config/general.m4])
m4_include([config/sipXlib.m4])
AC_CONFIG_SRCDIR([proxy-plugin/src/CallerID.cpp])
AM_DISABLE_STATIC
AC_PROG_LIBTOOL
AC_PROG_CXX
AC_PROG_CC
AX_COMPILER_VENDOR
SFAC_STRICT_COMPILE_NO_WARNINGS_ALLOWED
SFAC_INIT_FLAGS
SFAC_LIB_COMMSERVER
m4_include([config/java.m4])
m4_include([config/java2.m4])
AC_PROG_JAVA_CC([javac])
AC_PROG_JAVA([java])
PROG_JAVA_DEP
AC_CONFIG_FILES([
    Makefile
    etc/Makefile
    config-plugin/Makefile
    proxy-plugin/Makefile
    proxy-plugin/src/Makefile
])
AC_OUTPUT
