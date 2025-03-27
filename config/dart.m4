AC_DEFUN([CHECK_DART_SDK],
[
  AC_ARG_VAR(DART_HOME, [Dartlang SDK home directory])
  AC_MSG_CHECKING([dart-sdk])

  if test "x${DART_HOME}" != "x"; then
    AC_CHECK_FILE([${DART_HOME}/bin/dart],,[
      SF_MISSING_DEP([Cannot find dart in dart sdk at ${DART_HOME}/bin/dart])
    ])
  else
    candidates="/opt/dart/dart-sdk /opt/dart-sdk ~/dart-sdk"
    for candidate in $candidates; do
      if test -x "$candidate/bin/dart"; then
        DART_HOME="$candidate"
        break
      fi
    done
  fi

  if test "x${DART_HOME}" = "x"; then
    SF_MISSING_DEP([Cannot find dart-sdk])
  else
    AC_MSG_RESULT(${DART_HOME})
  fi

  SRC_DIR=`cd $srcdir && pwd`

  AC_MSG_NOTICE([Looking for pubspec.yaml in ${SRC_DIR}])
  if test -f "${SRC_DIR}/pubspec.yaml"; then
    AC_MSG_NOTICE([Building dart .pub-cache ...])
    (cd "${SRC_DIR}" && "${DART_HOME}/bin/dart" pub get --offline) || \
    (cd "${SRC_DIR}" && "${DART_HOME}/bin/dart" pub get) || \
    SF_MISSING_DEP([Failed to get public dart dependencies])
  fi
])