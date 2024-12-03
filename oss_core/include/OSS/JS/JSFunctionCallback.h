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

#ifndef OSS_JSFUNCTIONCALLBACK_H_INCLUDED
#define OSS_JSFUNCTIONCALLBACK_H_INCLUDED

#include "OSS/build.h"
#if ENABLE_FEATURE_V8
#include "OSS/JS/JS.h"
#include <vector>
#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/enable_shared_from_this.hpp>
#include "OSS/JS/JSIsolateManager.h"
#include "OSS/UTL/Thread.h"


namespace OSS {
namespace JS {


class JSFunctionCallback : public boost::enable_shared_from_this<JSFunctionCallback>, private boost::noncopyable
{
public:
  typedef boost::shared_ptr<JSFunctionCallback> Ptr;
  
  JSFunctionCallback(JSValueHandle func);
  JSFunctionCallback(JSValueHandle func, JSValueHandle args);
  JSFunctionCallback(JSValueHandle func, JSValueHandle args, JSValueHandle resultHandler);
  virtual ~JSFunctionCallback();
  virtual void execute();
  void dispose();
  bool& autoDisposeOnExecute();
  bool autoDisposeOnExecute() const;

private:
  v8::Isolate* getV8Isolate();

  JSCopyablePersistentFunctionHandle _function;
  JSCopyablePersistentArgumentVector _args;
  JSCopyablePersistentFunctionHandle _resultHandler;
  
  bool _disposed;
  bool _autoDisposeOnExecute;
  OSS::mutex_critic_sec _disposeMutex;
};

inline v8::Isolate* JSFunctionCallback::getV8Isolate() 
{
  return js_get_v8_isolate();
}

inline bool& JSFunctionCallback::autoDisposeOnExecute()
{
    return _autoDisposeOnExecute;
}

inline bool JSFunctionCallback::autoDisposeOnExecute() const
{
    return _autoDisposeOnExecute;
}
  
inline JSFunctionCallback::JSFunctionCallback(JSValueHandle func) :
    _disposed(false),
    _autoDisposeOnExecute(false)
{
  _function = JSCopyablePersistentFunctionHandle(getV8Isolate(),JSFunctionHandle::New( getV8Isolate(), JSFunctionHandle::Cast( func ) ));
}

inline JSFunctionCallback::JSFunctionCallback(JSValueHandle func, v8::Handle<v8::Value> args)  :
    _disposed(false),
    _autoDisposeOnExecute(false)
{
  _function = JSCopyablePersistentFunctionHandle(getV8Isolate(),JSFunctionHandle::New( getV8Isolate(), JSFunctionHandle::Cast( func ) ));
  handle_to_persistent_arg_vector(getV8Isolate(), args, _args);
}

inline JSFunctionCallback::JSFunctionCallback(JSValueHandle func, v8::Handle<v8::Value> args, v8::Handle<v8::Value> resultHandler)  :
    _disposed(false),
    _autoDisposeOnExecute(false)
{
  _function = JSCopyablePersistentFunctionHandle(getV8Isolate(),JSFunctionHandle::New( getV8Isolate(), JSFunctionHandle::Cast( func ) ));
  _resultHandler = JSCopyablePersistentFunctionHandle(getV8Isolate(),JSFunctionHandle::New( getV8Isolate(), JSFunctionHandle::Cast( resultHandler ) ));
  handle_to_persistent_arg_vector(getV8Isolate(), args, _args);
}

inline JSFunctionCallback::~JSFunctionCallback()
{
    dispose();
}

inline void JSFunctionCallback::dispose()
{
    OSS::mutex_critic_sec_lock lock(_disposeMutex);
    
    if (_disposed)
    {
        return;
    }
    
    _function.Reset();
    if (!_resultHandler.IsEmpty())
    {
      _resultHandler.Reset();
    }
    for (JSCopyablePersistentArgumentVector::iterator iter = _args.begin(); iter != _args.end(); iter++)
    {
      iter->Reset();
    }
    _disposed = true;
}

inline void JSFunctionCallback::execute()
{
  v8::Local<v8::Context> context = getV8Isolate()->GetCurrentContext();
  v8::Local<v8::Object> global = context->Global();

  JSLocalArgumentVector args;
  persistent_arg_vector_to_arg_vector(getV8Isolate(),_args, args);

  JSFunctionHandle func = JSFunctionHandle::New(getV8Isolate(),_function);
  if (_resultHandler.IsEmpty())
  {
    func->Call(context, global, args.size(), args.data());
  }
  else
  {
    v8::MaybeLocal<v8::Value> maybeResult = 
      func->Call(context, global, args.size(), args.data());
    if( !maybeResult.IsEmpty() )
    {
      JSValueHandle result = maybeResult.ToLocalChecked();
      JSArgumentVector resultArg;
      handle_to_arg_vector(getV8Isolate(), result, resultArg);
      JSFunctionHandle resultHandler = JSFunctionHandle::New(getV8Isolate(),_resultHandler);
      resultHandler->Call(context, global, resultArg.size(), resultArg.data());
    }
  }
  
  if (_autoDisposeOnExecute)
  {
      dispose();
  }
}

} } // OSS::JS


#endif // ENABLE_FEATURE_V8
#endif // OSS_JSFUNCTIONCALLBACK_H_INCLUDED

