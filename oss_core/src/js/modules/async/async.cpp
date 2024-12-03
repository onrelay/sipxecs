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


#include <poll.h>
#include "OSS/JS/JSPlugin.h"
#include "OSS/JS/JSUtil.h"
#include "OSS/JS/JSIsolate.h"
#include "OSS/JS/JSIsolateManager.h"
#include "OSS/JS/JSEventLoop.h"
#include "OSS/JS/modules/Async.h"
#include "OSS/JS/modules/QueueObject.h"
#include "OSS/UTL/CoreUtils.h"
#include "OSS/UTL/Logger.h"
#include "OSS/Net/Net.h"
#include "OSS/UTL/Semaphore.h"


static bool _enableAsync = false;
static OSS::Semaphore* _exitSem = 0;
static OSS::UInt64  _garbageCollectionFrequency = 30; /// 30 seconds default
pthread_t Async::_threadId = 0;
JSCopyablePersistentObjectTemplateHandle Async::_externalPointerTemplate;

JS_METHOD_IMPL(__call)
{
  js_method_enter_scope();
  if (_args_.Length() != 3 || !_args_[0]->IsFunction() || !_args_[1]->IsArray() || !_args_[2]->IsFunction())
  {
    js_method_throw("Invalid Argument");
  }
  
  js_method_declare_isolate(pIsolate);
  pIsolate->eventLoop()->functionCallback().execute(_args_[0], _args_[1], _args_[2]);
  _enableAsync = true;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__schedule_one_shot_timer)
{
  js_method_enter_scope();
  if (_args_.Length() < 2 || !_args_[0]->IsFunction() || !_args_[1]->IsInt32())
  {
    js_method_throw("Invalid Argument");
  }
  int expire = _args_[1]->Int32Value(js_method_context()).ToChecked();
  
  js_method_declare_isolate(pIsolate);
  int timerId = 0;
  if (_args_.Length() >= 3)
  {
    timerId = pIsolate->eventLoop()->timerManager().scheduleTimer(expire, _args_[0], _args_[2]);
  }
  else
  {
    timerId = pIsolate->eventLoop()->timerManager().scheduleTimer(expire, _args_[0]);
  }
  _enableAsync = true;
  
  js_method_set_return_handle(js_method_int32(timerId));
}

JS_METHOD_IMPL(__cancel_one_shot_timer)
{
  js_method_enter_scope();
  if (_args_.Length() < 1 || !_args_[0]->IsInt32())
  {
    js_method_throw("Invalid Argument");
  }
  int32_t timerId = _args_[0]->Int32Value(js_method_context()).ToChecked();
  Async::clear_timer(timerId);
  js_method_set_return_undefined();
}

void Async::clear_timer(int timerId)
{
  OSS::JS::JSIsolate::Ptr pIsolate = OSS::JS::JSIsolateManager::instance().getIsolate();
  pIsolate->eventLoop()->timerManager().cancelTimer(timerId);
}

void Async::__wakeup_pipe(OSS::JS::JSIsolate* pIsolate)
{
  if (!pIsolate)
  {
    OSS::JS::JSIsolateManager::instance().getIsolate()->eventLoop()->wakeup();
  }
  else
  {
    pIsolate->eventLoop()->wakeup();
  }
}

JS_METHOD_IMPL(__monitor_descriptor)
{
  js_method_enter_scope();
  if (_args_.Length() < 2 || !_args_[0]->IsInt32() || !_args_[1]->IsFunction())
  {
    js_method_throw("Invalid Argument");
  }

  _enableAsync = true;
  
  js_method_declare_isolate(pIsolate);
  OSS::JS::JSEventLoop* pEventLoop = pIsolate->eventLoop();
  pEventLoop->fdManager().addFileDescriptor(js_method_isolate(), _args_[0]->Int32Value(js_method_context()).ToChecked(), _args_[1], POLLIN);
  Async::__wakeup_pipe();
  js_method_set_return_undefined();
}
void Async::unmonitor_fd(const OSS::JS::JSIsolate::Ptr& pIsolate, int fd)
{
  if (pIsolate->eventLoop()->fdManager().removeFileDescriptor(fd))
  {
    Async::__wakeup_pipe();
  }
}

JS_METHOD_IMPL(__unmonitor_descriptor)
{
  js_method_enter_scope();
  if (_args_.Length() < 1 || !_args_[0]->IsInt32())
  {
    js_method_throw("Invalid Argument");
  }
  js_method_declare_isolate(pIsolate);
  int fd = _args_[0]->Int32Value(js_method_context()).ToChecked();
  Async::unmonitor_fd(pIsolate, fd);
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__set_promise_callback)
{
  js_method_declare_copyable_persistent_function(func, 0);
  OSS::JS::JSIsolate::getIsolate()->eventLoop()->interIsolate().setHandler(func);
  _enableAsync = true;
  js_method_set_return_undefined();
}

bool Async::json_execute_promise(OSS::JS::JSIsolate* pIsolate, const OSS::JSON::Object& request, OSS::JSON::Object& reply, uint32_t timeout, void* promiseData)
{
  return pIsolate->eventLoop()->interIsolate().execute(request, reply, timeout, promiseData);
}


JS_METHOD_IMPL(__stop_event_loop)
{
  js_method_enter_scope();
  OSS::JS::JSIsolate::getIsolate()->eventLoop()->terminate();
  Async::__wakeup_pipe();
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__set_garbage_collection_frequency)
{
  js_method_enter_scope();
  if (_args_.Length() == 0 || !_args_[0]->IsInt32())
  {
    js_method_throw("Invalid Argument");
  }
  _garbageCollectionFrequency = _args_[0]->Int32Value(js_method_context()).ToChecked();
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__process_events)
{
  Async::_threadId = pthread_self();
  
  OSS::JS::JSIsolate::Ptr pIsolate = OSS::JS::JSIsolate::getIsolate();
  OSS::JS::JSEventLoop* pEventLoop = pIsolate->eventLoop();
  OSS::JS::JSEventQueueManager& queueManager = pEventLoop->queueManager();
  OSS::JS::JSInterIsolateCallManager& interIsolate = pEventLoop->interIsolate();
  
  if (pIsolate->getForceAsync() || queueManager.getSize() > 0 || interIsolate.isEnabled())
  {
    _enableAsync = true;
  }

  if (_enableAsync)
  {
    pEventLoop->processEvents();
  }
  
  if (_exitSem)
  {
    _exitSem->signal();
  }
  js_method_set_return_undefined();
}

JS_EXPORTS_INIT()
{
  js_export_method("call", __call);
  js_export_method("processEvents", __process_events);
  js_export_method("setTimeout", __schedule_one_shot_timer);
  js_export_method("clearTimeout", __cancel_one_shot_timer);
  js_export_method("monitorFd", __monitor_descriptor);
  js_export_method("unmonitorFd", __unmonitor_descriptor);
  js_export_method("setGCFrequency", __set_garbage_collection_frequency);
  
  js_export_method("__stop_event_loop", __stop_event_loop);
  js_export_method("__set_promise_callback", __set_promise_callback);
  
  js_export_class(QueueObject);
  
  //
  // Create the template we use to wrap C++ pointers
  //
  JSObjectTemplateHandle externalObjectTemplate = v8::ObjectTemplate::New(js_method_isolate());
  externalObjectTemplate->SetInternalFieldCount(1);
  Async::_externalPointerTemplate = JSCopyablePersistentObjectTemplateHandle(js_method_isolate(), externalObjectTemplate);
  
  js_export_finalize();
}

JS_REGISTER_MODULE(JSAsync);

