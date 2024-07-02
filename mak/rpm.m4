dnl Initial Version Copyright (C) 2010 eZuce, Inc., All Rights Reserved.
dnl Licensed to the User under the LGPL license.
dnl

AC_ARG_VAR(MIRROR_SITE, [Single place to find CentOS, Redhat and EPEL. Example: http://mirrors.kernel.org])

DEFAULT_REPO_PORT=40100
dnl Required for getting files to chroot.
AC_ARG_VAR(REPO_PORT, [Port to host yum repo. Default is $DEFAULT_REPO_PORT])
if test -z "$REPO_PORT" ; then
  REPO_PORT=$DEFAULT_REPO_PORT
fi

UPSTREAM=1
AC_ARG_ENABLE(upstream, [--disable-upstream Do not use upstream. Appropriate for master build servers or when you want to build all rpms.],
[
  UPSTREAM=0
])
AC_SUBST(UPSTREAM)

# AC_ARG_VAR(REPO_BASE_URL, [sipXecs package distribution root. Default: http://download.sipxcom.org/pub/sipXecs])
AC_ARG_VAR(REPO_BASE_URL, [sipXecs package distribution root. Default: https://us-central1-yum.pkg.dev/projects/sipxecs])
if test -z "$REPO_BASE_URL"; then
  # REPO_BASE_URL=http://download.sipxcom.org/pub
  REPO_BASE_URL=https://us-central1-yum.pkg.dev/projects/sipxecs
fi



# AC_ARG_VAR(DOWNLOAD_BASE_URL, [sipXecs download root. Default: http://download.sipxcom.org/pub/sipXecs])
AC_ARG_VAR(DOWNLOAD_BASE_URL, [sipXecs download root. Default: https://storage.googleapis.com/sipxecs])
if test -z "$DOWNLOAD_BASE_URL"; then
  # DOWNLOAD_BASE_URL=http://download.sipxcom.org/pub
  DOWNLOAD_BASE_URL=https://storage.googleapis.com/sipxecs
fi


AC_ARG_WITH(yum-proxy, [--with-yum-proxy send downloads thru caching proxy like squid to speed downloads], [
  AC_SUBST(DOWNLOAD_PROXY,$withval)
  AC_SUBST(DOWNLOAD_PROXY_CONFIG_LINE,"proxy=$withval")
  AC_SUBST(WGET_PROXY_OPTS,"http_proxy=$withval")


AC_ARG_VAR(FEDORA_BASE_URL, [Where to find Fedora distribution. Example: http://mirrors.kernel.org/fedora/linux])
if test -z "$FEDORA_BASE_URL"; then
  if test -n "$MIRROR_SITE"; then
    FEDORA_BASE_URL=$MIRROR_SITE/fedora/linux
  else
    AC_MSG_ERROR([You must provide a value for FEDORA_BASE_URL or MIRROR_SITE if you are using a download proxy.\
 See http://wiki.sipfoundry.org/display/sipXecs/Install+squid+caching+server+to+reduce+build+time for more details.])
  fi
fi

AC_ARG_VAR(FEDORA_ARCHIVE_BASE_URL, [Where to find Fedora archives. Example: http://mirrors.kernel.org/archive/fedora/linux])
if test -z "$FEDORA_ARCHIVE_BASE_URL"; then
  if test -n "$MIRROR_SITE"; then
    FEDORA_ARCHIVE_BASE_URL=$MIRROR_SITE/archive/fedora/linux
  else
    AC_MSG_ERROR([You must provide a value for FEDORA_ARCHIVE_BASE_URL or MIRROR_SITE if you are using a download proxy.\
 See http://wiki.sipfoundry.org/display/sipXecs/Install+squid+caching+server+to+reduce+build+time for more details.])
  fi
fi

AC_ARG_VAR(EPEL_BASE_URL, [Where to find EPEL distribution. Example: http://mirrors.kernel.org/epel])
if test -z "$EPEL_BASE_URL"; then
  if test -n "$MIRROR_SITE"; then
    EPEL_BASE_URL=$MIRROR_SITE/epel
  else
    AC_MSG_ERROR([You must provide a value for EPEL_BASE_URL or MIRROR_SITE if you are using a download proxy.\
 See http://wiki.sipfoundry.org/display/sipXecs/Install+squid+caching+server+to+reduce+build+time for more details.])
  fi
fi

],)

dnl NOTE: To support non-rpm based distos, write equivalent of this that defines DISTRO_* vars
AC_CHECK_FILE(/bin/rpm,
[
  RPMBUILD_TOPDIR="\$(shell rpm --eval '%{_topdir}')"
  AC_SUBST(RPMBUILD_TOPDIR)

  RPM_DIST=`rpm --eval '%{?dist}'`
  AC_SUBST(RPM_DIST)

  DistroArchDefault=`rpm --eval '%{_target_cpu}'`

  dnl NOTE LIMITATION: default doesn't account for distros besides centos, redhat and suse or redhat 6
  DistroOsDefault=`rpm --eval '%{?fedora:fedora}%{!?fedora:%{?suse_version:suse}%{!?suse_version:centos}}'`

  dnl NOTE LIMITATION: default doesn't account for distros besides centos, redhat and suse or redhat 6
  DistroVerDefault=`rpm --eval '%{?fedora:%fedora}%{!?fedora:%{?suse_version:%suse_version}%{!?suse_version:5}}'`

  DistroDefault="${DistroOsDefault}-${DistroVerDefault}-${DistroArchDefault}"
])

AC_ARG_VAR(DISTRO, [What operating system you are compiling for. Default is ${DistroDefault}])
test -n "${DISTRO}" || DISTRO="centos-7-x86_64"

AllDistrosDefault="fedora-16-i386 fedora-16-x86_64 fedora-17-i386 fedora-17-x86_64 fedora-18-i386 fedora-18-x86_64 fedora-19-i386 fedora-19-x86_64 centos-6-i386 centos-6-x86_64 centos-7-x86_64"
AC_ARG_VAR(ALL_DISTROS, [All distros which using cross distroy compiling (xc.* targets) Default is ${AllDistrosDefault}])
test -n "${ALL_DISTROS}" || ALL_DISTROS="${AllDistrosDefault}"


AC_ARG_VAR(REPO_NAME, [sipXecs repo name])
if test -z "$REPO_NAME"; then
  DASH_PACKAGE_VERSION=`echo $PACKAGE_VERSION | sed 's/\./-/'`
  DASH_DISTRO=`echo $DISTRO | sed 's/_/-/'`
  REPO_NAME=sipxcom-${DASH_PACKAGE_VERSION}-${DASH_DISTRO}
fi


AC_ARG_VAR(REPO_URL, [sipXecs current repo URL])
if test -z "$REPO_URL"; then
  DASH_PACKAGE_VERSION=`echo $PACKAGE_VERSION | sed 's/\./-/'`
  DASH_DISTRO=`echo $DISTRO | sed 's/_/-/'`
  REPO_URL=${REPO_BASE_URL}/${REPO_NAME}
fi

AC_ARG_VAR(UPSTREAM_URL, [sipXecs upstream URL])
if test -z "$UPSTREAM_URL"; then
  UPSTREAM_URL=${REPO_URL}
fi


SETUP_TARGET=src
AC_SUBST(SETUP_TARGET)
AC_ARG_ENABLE(rpm, [--enable-rpm Using mock package to build rpms],
[
  SETUP_TARGET=rpm

  # What is installed on host has little to do with what needs to be installed in chroot
  # so let's automatically disable checking host when building tarballs
  OPTIONS+=" --disable-dep-check"

  AC_ARG_VAR(DOWNLOAD_LIB_CACHE, [When to cache source files that are downloaded, default libsrc])
  if test -z "$DOWNLOAD_LIB_CACHE"; then
    DOWNLOAD_LIB_CACHE=libsrc  
    mkdir -p ${DOWNLOAD_LIB_CACHE}
  fi

  AC_ARG_VAR(DOWNLOAD_LIB_URL, [When to cache source files that are downloaded, default ${DOWNLOAD_BASE_URL}/libs])
  test -n "${DOWNLOAD_LIB_URL}" || DOWNLOAD_LIB_URL=${DOWNLOAD_BASE_URL}/libs

  AC_ARG_VAR(RPM_DIST_DIR, [Where to assemble final set of RPMs and SRPMs in preparation for publishing to a download server.])
  test -n "${RPM_DIST_DIR}" || RPM_DIST_DIR=repo

  if test -n "$FEDORA_BASE_URL"; then
    AC_SUBST(FEDORA_BASE_URL_ON,[])
    AC_SUBST(FEDORA_BASE_URL_OFF,[#])
  else
    AC_SUBST(FEDORA_BASE_URL_ON,[#])
    AC_SUBST(FEDORA_BASE_URL_OFF,[])
  fi

  if test -n "$EPEL_BASE_URL"; then
    AC_SUBST(EPEL_BASE_URL_ON,[])
    AC_SUBST(EPEL_BASE_URL_OFF,[#])
  else
    AC_SUBST(EPEL_BASE_URL_ON,[#])
    AC_SUBST(EPEL_BASE_URL_OFF,[])
  fi

  AC_CONFIG_FILES([mak/mock/logging.ini])
  AC_CONFIG_FILES([mak/mock/site-defaults.cfg])
  AC_CONFIG_FILES([mak/mock/centos-6-i386.cfg])
  AC_CONFIG_FILES([mak/mock/centos-6-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/centos-7-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/centos-9-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-16-i386.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-16-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-17-i386.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-17-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-18-i386.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-18-x86_64.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-19-i386.cfg])
  AC_CONFIG_FILES([mak/mock/fedora-19-x86_64.cfg])
  AC_CONFIG_FILES([mak/10-rpm.mk:mak/rpm.mk.in])
])
