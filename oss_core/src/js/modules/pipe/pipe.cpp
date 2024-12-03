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
#include "OSS/UTL/Logger.h"
#include <unistd.h>
#include <fcntl.h>

JS_METHOD_IMPL(__pipe)
{
  int flags = 0;
  if (_args_.Length() >= 1 && _args_[0]->IsInt32())
  {
    flags = _args_[0]->Int32Value(js_method_context()).ToChecked();
  }
  int ret = 0;
  int pipefd[2];
  if (!flags)
  {
    ret = ::pipe(pipefd);
  }
#if !defined(__APPLE__)
  else
  {
    ret = ::pipe2(pipefd, flags);
  }
#endif  
  if (ret == 0)
  {
    JSArrayHandle result = js_method_array(2);
    result->Set(js_method_context(), 0, js_method_int32(pipefd[0]));
    result->Set(js_method_context(), 1, js_method_int32(pipefd[1]));
    js_method_set_return_handle(result);
  }
  
  js_method_set_return_undefined();
}

JS_EXPORTS_INIT()
{
  js_export_method("pipe", __pipe );
  js_export_finalize();
}

JS_REGISTER_MODULE(Pipe);
