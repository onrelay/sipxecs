include config/utility.am
include config/project.am
include config/lang.am

@PACKAGE@_EXTRA_DIST = \
	sipxpbx \
	stdprompts \
	sipxivr

prompts_SOURCE = $(shell find $(srcdir)/stdprompts -name '*.wav' -type f -printf '%P\n')
confplugin_DATA = sipxlang-@LANG_ID@.jar
ivrplugin_DATA = sipxlangivr-@LANG_ID@.jar

install-data-hook :
	echo "$$Installer" | \
	  InstallFileList="$(prompts_SOURCE)" \
	  InstallSourceDir=$(srcdir)/stdprompts \
	  InstallDestDir=$(DESTDIR)$(promptsdir) \
	  bash
