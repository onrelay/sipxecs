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

#include "OSS/JS/JSPlugin.h"
#include "OSS/UTL/CoreUtils.h"
#include <unistd.h>
#include <errno.h>
#include <poll.h>

JS_ACCESSOR_GETTER_IMPL(__errno_get) 
{
  js_method_set_return_integer(errno);
}

JS_EXPORTS_INIT()
{
  //
  // Mutable Properties
  //
  js_export_accessor(("errno"),__errno_get, NULL);
  
  //
  // Standard system errors
  //
  js_export_const(EPERM);          /* Operation not permitted */
  js_export_const(ENOENT);         /* No such file or directory */
  js_export_const(ESRCH);          /* No such process */
  js_export_const(EINTR);          /* Interrupted system call */
  js_export_const(EIO);            /* I/O error */
  js_export_const(ENXIO);          /* No such device or address */
  js_export_const(E2BIG);          /* Argument list too long */
  js_export_const(ENOEXEC);        /* Exec format error */
  js_export_const(EBADF);          /* Bad file number */
  js_export_const(ECHILD);         /* No child processes */
  js_export_const(EAGAIN);         /* Try again */
  js_export_const(ENOMEM);         /* Out of memory */
  js_export_const(EACCES);         /* Permission denied */
  js_export_const(EFAULT);         /* Bad address */
  js_export_const(ENOTBLK);        /* Block device required */
  js_export_const(EBUSY);          /* Device or resource busy */
  js_export_const(EEXIST);         /* File exists */
  js_export_const(EXDEV);          /* Cross-device link */
  js_export_const(ENODEV);         /* No such device */
  js_export_const(ENOTDIR);        /* Not a directory */
  js_export_const(EISDIR);         /* Is a directory */
  js_export_const(EINVAL);         /* Invalid argument */
  js_export_const(ENFILE);         /* File table overflow */
  js_export_const(EMFILE);         /* Too many open files */
  js_export_const(ENOTTY);         /* Not a typewriter */
  js_export_const(ETXTBSY);        /* Text file busy */
  js_export_const(EFBIG);          /* File too large */
  js_export_const(ENOSPC);         /* No space left on device */
  js_export_const(ESPIPE);         /* Illegal seek */
  js_export_const(EROFS);          /* Read-only file system */
  js_export_const(EMLINK);         /* Too many links */
  js_export_const(EPIPE);          /* Broken pipe */
  js_export_const(EDOM);           /* Math argument out of domain of func */
  js_export_const(ERANGE);         /* Math result not representable */
  js_export_const(EDEADLK);        /* Resource deadlock would occur */
  js_export_const(ENAMETOOLONG);   /* File name too long */
  js_export_const(ENOLCK);         /* No record locks available */
  js_export_const(ENOSYS);         /* Invalid system call number */
  js_export_const(ENOTEMPTY);      /* Directory not empty */
  js_export_const(ELOOP);          /* Too many symbolic links encountered */
  js_export_const(EWOULDBLOCK);    /* Operation would block */
  js_export_const(ENOMSG);         /* No message of desired type */
  js_export_const(EIDRM);          /* Identifier removed */
  js_export_const(ENOSTR);         /* Device not a stream */
  js_export_const(ENODATA);        /* No data available */
  js_export_const(ETIME);          /* Timer expired */
  js_export_const(ENOSR);          /* Out of streams resources */
  js_export_const(EREMOTE);        /* Object is remote */
  js_export_const(ENOLINK);        /* Link has been severed */
  js_export_const(EPROTO);         /* Protocol error */
  js_export_const(EMULTIHOP);      /* Multihop attempted */
  js_export_const(EBADMSG);        /* Not a data message */
  js_export_const(EOVERFLOW);      /* Value too large for defined data type */
  js_export_const(EILSEQ);         /* Illegal byte sequence */
  js_export_const(EUSERS);         /* Too many users */
  js_export_const(ENOTSOCK);       /* Socket operation on non-socket */
  js_export_const(EDESTADDRREQ);   /* Destination address required */
  js_export_const(EMSGSIZE);       /* Message too long */
  js_export_const(EPROTOTYPE);     /* Protocol wrong type for socket */
  js_export_const(ENOPROTOOPT);    /* Protocol not available */
  js_export_const(EPROTONOSUPPORT);/* Protocol not supported */
  js_export_const(ESOCKTNOSUPPORT);/* Socket type not supported */
  js_export_const(EOPNOTSUPP);     /* Operation not supported on transport endpoint */
  js_export_const(EPFNOSUPPORT);   /* Protocol family not supported */
  js_export_const(EAFNOSUPPORT);   /* Address family not supported by protocol */
  js_export_const(EADDRINUSE);     /* Address already in use */
  js_export_const(EADDRNOTAVAIL);  /* Cannot assign requested address */
  js_export_const(ENETDOWN);       /* Network is down */
  js_export_const(ENETUNREACH);    /* Network is unreachable */
  js_export_const(ENETRESET);      /* Network dropped connection because of reset */
  js_export_const(ECONNABORTED);   /* Software caused connection abort */
  js_export_const(ECONNRESET);     /* Connection reset by peer */
  js_export_const(ENOBUFS);        /* No buffer space available */
  js_export_const(EISCONN);        /* Transport endpoint is already connected */
  js_export_const(ENOTCONN);       /* Transport endpoint is not connected */
  js_export_const(ESHUTDOWN);      /* Cannot send after transport endpoint shutdown */
  js_export_const(ETOOMANYREFS);   /* Too many references: cannot splice */
  js_export_const(ETIMEDOUT);      /* Connection timed out */
  js_export_const(ECONNREFUSED);   /* Connection refused */
  js_export_const(EHOSTDOWN);      /* Host is down */
  js_export_const(EHOSTUNREACH);   /* No route to host */
  js_export_const(EALREADY);       /* Operation already in progress */
  js_export_const(EINPROGRESS);    /* Operation now in progress */
  js_export_const(ESTALE);         /* Stale file handle */
  js_export_const(EDQUOT);         /* Quota exceeded */
  js_export_const(ECANCELED);      /* Operation Canceled */
  js_export_const(EOWNERDEAD);     /* Owner died */
  js_export_const(ENOTRECOVERABLE);/* State not recoverable */
  
  
#if !OSS_PLATFORM_MAC_OS_X
  js_export_const(ERFKILL);        /* Operation not possible due to RF-kill */
  js_export_const(EHWPOISON);      /* Memory page has hardware error */
  js_export_const(ENOKEY);         /* Required key not available */
  js_export_const(EKEYEXPIRED);    /* Key has expired */
  js_export_const(EKEYREVOKED);    /* Key has been revoked */
  js_export_const(EKEYREJECTED);   /* Key was rejected by service */
  js_export_const(ENOMEDIUM);      /* No medium found */
  js_export_const(EMEDIUMTYPE);    /* Wrong medium type */
  js_export_const(ECHRNG);         /* Channel number out of range */
  js_export_const(EL2NSYNC);       /* Level 2 not synchronized */
  js_export_const(EL3HLT);         /* Level 3 halted */
  js_export_const(EL3RST);         /* Level 3 reset */
  js_export_const(ELNRNG);         /* Link number out of range */
  js_export_const(EUNATCH);        /* Protocol driver not attached */
  js_export_const(ENOCSI);         /* No CSI structure available */
  js_export_const(EL2HLT);         /* Level 2 halted */
  js_export_const(EBADE);          /* Invalid exchange */
  js_export_const(EBADR);          /* Invalid request descriptor */
  js_export_const(EXFULL);         /* Exchange full */
  js_export_const(ENOANO);         /* No anode */
  js_export_const(EBADRQC);        /* Invalid request code */
  js_export_const(EBADSLT);        /* Invalid slot */
  js_export_const(EDEADLOCK);      /* Resource deadlock would occur */
  js_export_const(EBFONT);         /* Bad font file format */
  js_export_const(ENONET);         /* Machine is not on the network */
  js_export_const(ENOPKG);         /* Package not installed */
  js_export_const(EADV);           /* Advertise error */
  js_export_const(ESRMNT);         /* Srmount error */
  js_export_const(ECOMM);          /* Communication error on send */
  js_export_const(EDOTDOT);        /* RFS specific error */
  js_export_const(ENOTUNIQ);       /* Name not unique on network */
  js_export_const(EBADFD);         /* File descriptor in bad state */
  js_export_const(EREMCHG);        /* Remote address changed */
  js_export_const(ELIBACC);        /* Can not access a needed shared library */
  js_export_const(ELIBBAD);        /* Accessing a corrupted shared library */
  js_export_const(ELIBSCN);        /* lib section in a.out corrupted */
  js_export_const(ELIBMAX);        /* Attempting to link in too many shared libraries */
  js_export_const(ELIBEXEC);       /* Cannot exec a shared library directly */
  js_export_const(ERESTART);       /* Interrupted system call should be restarted */
  js_export_const(ESTRPIPE);       /* Streams pipe error */
  js_export_const(EUCLEAN);        /* Structure needs cleaning */
  js_export_const(ENOTNAM);        /* Not a XENIX named type file */
  js_export_const(ENAVAIL);        /* No XENIX semaphores available */
  js_export_const(EISNAM);         /* Is a named type file */
  js_export_const(EREMOTEIO);      /* Remote I/O error */
#endif
  js_export_finalize();
}

JS_REGISTER_MODULE(Error);
