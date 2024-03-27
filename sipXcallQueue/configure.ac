AC_PREREQ(2.57)
AC_INIT(sipxcallqueue, 24.01, sipxcom-dev@googlegroups.com)
AC_CONFIG_AUX_DIR(config)
AM_INIT_AUTOMAKE(foreign tar-ustar)
m4_include([config/general.m4])
m4_include([config/sipXlib.m4])
SFAC_INIT_FLAGS
m4_include([config/java.m4])
m4_include([config/java2.m4])
AC_PROG_JAVA_CC([javac])
AC_PROG_JAVA([java])
PROG_JAVA_DEP
CHECK_POSTGRES
AM_CONDITIONAL(IS_RPMBUILD, test x$enable_rpmbuild = xyes)

AC_CONFIG_FILES([
    Makefile
    etc/Makefile
    config-plugin/Makefile
    config-plugin/test/Makefile
])
AC_OUTPUT
