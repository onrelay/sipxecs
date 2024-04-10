AC_DEFUN([CHECK_DART_SDK],
[
   AC_ARG_VAR(DART_HOME, [Dartlang SDK home directory])
   AC_MSG_CHECKING([dart-sdk])
   if test x_"${DART_HOME}" != x_
   then
     # AC_CHECK_FILE([${DART_HOME}/bin/dart2js],,[
     #   SF_MISSING_DEP([Cannot find dart2js in dart sdk at ${DART_HOME}/bin/dart2js])
     # ])
    if test ! -f "${DART_HOME}/bin/dart2js" ; then
      SF_MISSING_DEP([Cannot find dart2js in dart sdk at ${DART_HOME}/bin/dart2js])
    fi
   else
     candidates="${DART_HOME} /opt/dart/dart-sdk /opt/dart-sdk ~/dart-sdk"
     for candidate in $candidates; do
       # AC_CHECK_FILE([$candidate/bin/dart2js],
       # [
       #    DART_HOME=$candidate
	     # break
       # ],)
      if test -f "$candidate/bin/dart2js" ; then
        DART_HOME=$candidate
        break
      fi
     done
   fi

   if test x$DART_HOME == x; then
     SF_MISSING_DEP([Cannot find dart-sdk])
   else
     AC_MSG_RESULT(${DART_HOME})
   fi

   AC_SUBST(DART_HOME)
   AC_SUBST(DART_PKGS, [${datadir}/dart])
])
