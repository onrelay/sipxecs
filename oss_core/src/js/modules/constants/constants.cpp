// Library: OSS_CORE - Foundation API for SIP B2BUA
// Copyright (c) OSS Software Solutions
// Contributor: Joegen Baclor - mailto:joegen@ossapp.com
//
// Permission is hereby granted, to any person or organization
// obtaining a copy of the software and accompanying documentation covered by
// this license (the "Software") to use, execute, and to prepare
// derivative works of the Software, all subject to the
// "GNU Lesser General Public License (LGPL)".
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
// SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
// FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

#include "OSS/OSS.h"
#include "OSS/JS/JSPlugin.h"
#include "OSS/UTL/CoreUtils.h"
#include <fcntl.h>
#include <unistd.h>
#include <signal.h>

JS_EXPORTS_INIT()
{  
  //
  // Mutable Properties
  //
  
  //
  // Standard fcntl system constants
  //
#ifdef __USE_XOPEN2K8
  js_export_const(O_DIRECTORY);    /* Must be a directory.	 */
  js_export_const(O_NOFOLLOW);     /* Do not follow links.	 */
  js_export_const(O_CLOEXEC);      /* Set close_on_exec.  */
#endif

#ifdef __USE_GNU
  js_export_const(O_DIRECT);       /* Direct disk access.	*/
  js_export_const(O_NOATIME);      /* Do not set atime.  */
  js_export_const(O_PATH);         /* Resolve pathname but do not open file.  */
#ifdef O_TMPFILE
  js_export_const(O_TMPFILE);      /* Atomically create nameless file.  */
#endif
#endif
  
  js_export_const(STDOUT_FILENO);
  js_export_const(STDIN_FILENO);
  js_export_const(STDERR_FILENO);
  
  //
  // Signals
  //
  js_export_const(SIGHUP);   /* Hangup (POSIX).  */
  js_export_const(SIGINT);   /* Interrupt (ANSI).  */
  js_export_const(SIGQUIT);  /* Quit (POSIX).  */
  js_export_const(SIGILL);   /* Illegal instruction (ANSI).  */
  js_export_const(SIGTRAP);  /* Trace trap (POSIX).  */
  js_export_const(SIGABRT);  /* Abort (ANSI).  */
  js_export_const(SIGIOT);   /* IOT trap (4.2 BSD).  */
  js_export_const(SIGBUS);   /* BUS error (4.2 BSD).  */
  js_export_const(SIGFPE);   /* Floating-point exception (ANSI).  */
  js_export_const(SIGKILL);  /* Kill, unblockable (POSIX).  */
  js_export_const(SIGUSR1);  /* User-defined signal 1 (POSIX).  */
  js_export_const(SIGSEGV);  /* Segmentation violation (ANSI).  */
  js_export_const(SIGUSR2);  /* User-defined signal 2 (POSIX).  */
  js_export_const(SIGPIPE);  /* Broken pipe (POSIX).  */
  js_export_const(SIGALRM);  /* Alarm clock (POSIX).  */
  js_export_const(SIGCHLD);  /* Child status has changed (POSIX).  */
  js_export_const(SIGCONT);  /* Continue (POSIX).  */
  js_export_const(SIGSTOP);  /* Stop, unblockable (POSIX).  */
  js_export_const(SIGTSTP);  /* Keyboard stop (POSIX).  */
  js_export_const(SIGTTIN);  /* Background read from tty (POSIX).  */
  js_export_const(SIGTTOU);  /* Background write to tty (POSIX).  */
  js_export_const(SIGURG);   /* Urgent condition on socket (4.2 BSD).  */
  js_export_const(SIGXCPU);  /* CPU limit exceeded (4.2 BSD).  */
  js_export_const(SIGXFSZ);  /* File size limit exceeded (4.2 BSD).  */
  js_export_const(SIGVTALRM);/* Virtual alarm clock (4.2 BSD).  */
  js_export_const(SIGPROF);  /* Profiling alarm clock (4.2 BSD).  */
  js_export_const(SIGSYS);   /* Bad system call.  */
  js_export_const(SIGTERM);  /* Termination (ANSI).  */
  js_export_const(SIGWINCH); /* Window size change (4.3 BSD, Sun).  */
  js_export_const(SIGIO);    /* I/O now possible (4.2 BSD).  */
#if !OSS_PLATFORM_MAC_OS_X
  js_export_const(SIGSTKFLT);/* Stack fault.  */
  js_export_const(SIGPWR);   /* Power failure restart (System V).  */
  js_export_const(SIGPOLL);  /* Pollable event occurred (System V).  */
  js_export_const(SIGCLD);   /* Same as SIGCHLD (System V).  */
#endif
  js_export_finalize();
}

JS_REGISTER_MODULE(Const);
