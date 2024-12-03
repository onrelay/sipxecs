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
#include <errno.h>
#include <poll.h>

typedef std::vector<pollfd> PollFD;

static void array_to_pollfd_vector(v8::Isolate* isolate, v8::Handle<v8::Array>& input, PollFD& output)
{
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  for(uint32_t i = 0; i < input->Length(); i++)
  {
    v8::Handle<v8::Object> item = 
      input->Get(context,i).ToLocalChecked()->ToObject(context).ToLocalChecked();
    v8::Handle<v8::Integer> fd = 
      item->Get(context,JSString(isolate,"fd")).ToLocalChecked()->ToInteger(context).ToLocalChecked();
    v8::Handle<v8::Integer> events = 
      item->Get(context,JSString(isolate,"events")).ToLocalChecked()->ToInteger(context).ToLocalChecked();

    pollfd pfd;
    pfd.fd = fd->Value();
    pfd.events = events->Value();
    pfd.revents = 0;
    output.push_back(pfd);
  }
}

static void pollfd_vector_to_array(v8::Isolate* isolate, PollFD& input, v8::Handle<v8::Array>& output)
{
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  uint32_t index = 0;
  for (PollFD::iterator iter = input.begin(); iter != input.end(); iter++)
  {
    v8::Handle<v8::Object> item = JSObject(isolate);
    v8::Handle<v8::Integer> fd = JSInteger(isolate,iter->fd);
    v8::Handle<v8::Integer> events = JSInteger(isolate,iter->events);
    v8::Handle<v8::Integer> revents = JSInteger(isolate,iter->revents);
    
    item->Set(context, JSString(isolate,"fd"), fd);
    item->Set(context, JSString(isolate,"events"), events);
    item->Set(context, JSString(isolate,"revents"), revents);
    
    output->Set(context, index++, item);
  }
}

JS_METHOD_IMPL(__poll)
{
  if (_args_.Length() < 1 || !_args_[0]->IsArray())
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  
  PollFD pfds;
  v8::Handle<v8::Array> args0 = v8::Handle<v8::Array>::Cast(_args_[0]);
  int timeout = -1;
  array_to_pollfd_vector(js_method_isolate(), args0, pfds);
  
  if (_args_.Length() >= 2 && _args_[1]->IsNumber())
  {
    timeout = _args_[1]->IntegerValue(js_method_context()).ToChecked();
  }
  
  v8::Handle<v8::Array> result = js_method_array(2);
  int ret = ::poll(pfds.data(), pfds.size(), timeout);
  result->Set(js_method_context(), 0, js_method_integer(ret));
  
  if (ret == 0 || ret == -1)
  {
    js_method_set_return_handle(result);
    return;
  }
  
  v8::Handle<v8::Array> pfdsout = js_method_array(pfds.size());
  pollfd_vector_to_array(js_method_isolate(), pfds, pfdsout);
  result->Set(js_method_context(), 1, pfdsout);
  js_method_set_return_handle(result);
}

JS_EXPORTS_INIT()
{
  //
  // Methods
  //

  js_export_method("poll", __poll );

  //
  // Mutable Properties
  //

  //
  // Constants
  //
  js_export_const(POLLIN);  /* There is data to read.  */
  js_export_const(POLLPRI);  /* There is urgent data to read.  */
  js_export_const(POLLOUT);  /* Writing now will not block.  */

  #if defined __USE_XOPEN || defined __USE_XOPEN2K8
    js_export_const(POLLRDNORM);  /* Normal data may be read.  */
    js_export_const(POLLRDBAND);  /* Priority data may be read.  */
    js_export_const(POLLWRNORM);  /* Writing now will not block.  */
    js_export_const(POLLWRBAND);  /* Priority data may be written.  */
  #endif

  #ifdef __USE_GNU
    js_export_const(POLLMSG);
    js_export_const(POLLREMOVE);
    js_export_const(POLLRDHUP);
  #endif

  js_export_const(POLLERR);    /* Error condition.  */
  js_export_const(POLLHUP);    /* Hung up.  */
  js_export_const(POLLNVAL);   /* Invalid polling request.  */

  js_export_finalize();
}

JS_REGISTER_MODULE(Poll);
