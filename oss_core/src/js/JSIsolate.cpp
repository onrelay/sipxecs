
#include <v8.h>

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


#include "OSS/JS/JSIsolate.h"
#include "OSS/JS/JSIsolateManager.h"
#include "OSS/JS/JSEventLoop.h"
#include "OSS/JS/JSModule.h"
#include "OSS/JS/modules/Async.h"
#include "OSS/JS/JSUtil.h"
#include "OSS/UTL/Logger.h"
#include "OSS/JS/JSPluginManager.h"
#include "OSS/JS/JSTask.h"


namespace OSS {
namespace JS {




JSIsolate::JSIsolate(pthread_t parentThreadId) :
  _pIsolate(0),
  _pModuleManager(0),
  _exitValue(0),
  _threadId(0),
  _parentThreadId(parentThreadId),
  _pEventLoop(0),
  _isRoot(false),
  _pThread(0),
  _eventEmitterFd(0),
  _forceAsync(false)
{
  _pPluginManager = new JSPluginManager(this);
  _pModuleManager = new JSModule(this);
  _pEventLoop = new JSEventLoop(this);
  if (parentThreadId)
  {
    _pParentIsolate = OSS::JS::JSIsolateManager::instance().findIsolate(_parentThreadId);
    assert(_pParentIsolate);
  }
  else
  {
    _isRoot = true;
  }
}

JSIsolate::~JSIsolate()
{
  dispose();
}

void JSIsolate::dispose()
{
  if (_pIsolate)
  {
    terminate();
    join();
    delete _pEventLoop;
    _pEventLoop = 0;
    delete _pModuleManager;
    _pModuleManager = 0;
    delete _pPluginManager;
    _pPluginManager = 0;
    _pIsolate->Dispose();
    //
    // V8 will delete the isolate.  No need to delete it here
    //
    _pIsolate = 0;
    _source = std::string();
  }
}

void JSIsolate::internal_run()
{
  v8::Isolate::CreateParams params;
  _pIsolate = v8::Isolate::New(params);
  v8::Isolate::Scope global_scope(_pIsolate);
  _threadId  = pthread_self();
  JSIsolateManager::instance().registerIsolate(shared_from_this());
  v8::HandleScope handle_scope(_pIsolate);

  JSObjectTemplateHandle global = v8::ObjectTemplate::New(_pIsolate);
  _globalTemplate = JSCopyablePersistentObjectTemplateHandle(_pIsolate, global);
  
  JSObjectTemplateHandle objectTemplate = v8::ObjectTemplate::New(_pIsolate);
  objectTemplate->SetInternalFieldCount(1);
  _objectTemplate = JSCopyablePersistentObjectTemplateHandle(_pIsolate, objectTemplate);
  
  //
  // Set the thread id and update the manager
  //
  _threadId = pthread_self();

  //
  // Initialize global and assign it to the context
  //
  JSIsolateManager::instance().modulesMutex().lock();
  JSIsolateManager::instance().initGlobalExports(_pIsolate,global);
  _pModuleManager->initGlobalExports(_pIsolate,global);
  JSIsolateManager::instance().modulesMutex().unlock();

  if (isRoot())
  {
    _pModuleManager->setMainScript(_script);
  }

  v8::Handle<v8::Context> context = v8::Context::New(_pIsolate, 0, global);
  _context = JSCopyablePersistentContextHandle(_pIsolate, context);
  v8::Context::Scope context_scope(context);
  v8::TryCatch try_catch( _pIsolate );
  try_catch.SetVerbose(true);
  
  JSIsolateManager::instance().modulesMutex().lock();
  if (!_pModuleManager->initialize(_pIsolate, try_catch, global))
  {
    OSS_LOG_ERROR("Unable to initialize module manager");
    report_js_exception(_pIsolate, try_catch, true);
    return;
  }
  JSIsolateManager::instance().modulesMutex().unlock();
  
  v8::MaybeLocal<v8::Script> maybeCompiledScript;
  if (_source.empty())
  {
    std::string strScriptSource = read_file_skip_shebang(OSS::boost_path(_script), true);
    v8::Handle<v8::String> scriptSource = JSString( _pIsolate, strScriptSource );
    v8::ScriptOrigin scriptOrigin(JSString(_pIsolate, OSS::boost_path(_script) ) );
    maybeCompiledScript = v8::Script::Compile(context, scriptSource, &scriptOrigin );
  }
  else
  {
    std::ostringstream strm;
    strm << "try { " << _source << " } catch(e) {console.printStackTrace(e); _exit(-1); } ;async.processEvents();";
    v8::Handle<v8::String> scriptSource = JSString( _pIsolate, strm.str());
    maybeCompiledScript = v8::Script::Compile(context, scriptSource);
  }
  
  if (maybeCompiledScript.IsEmpty())
  {
    OSS_LOG_ERROR("Unable to compile script");
    report_js_exception(_pIsolate, try_catch, true);
    _exitValue = -1;
    return;
  }
  v8::Handle<v8::Script> compiledScript = maybeCompiledScript.ToLocalChecked();

  v8::MaybeLocal<v8::Value> result = compiledScript->Run( context );
  if (result.IsEmpty())
  {
    OSS_LOG_ERROR("Unable to run script");
    report_js_exception(_pIsolate, try_catch, true);
    _exitValue = -1;
    return;
  }

  _exitValue = 0;
}

void JSIsolate::run(const boost::filesystem::path& script)
{
  _script = script;
  _source = std::string();
  if (isRoot())
  {
    internal_run();
  }
  else
  {
    _pThread = new boost::thread(boost::bind(&JSIsolate::internal_run, this));
  }
}

void JSIsolate::runSource(const std::string& source)
{
  _source = source;
  _script = boost::filesystem::path();
  if (isRoot())
  {
    internal_run();
  }
  else
  {
    _pThread = new boost::thread(boost::bind(&JSIsolate::internal_run, this));
  }
}

void JSIsolate::notify(const std::string& request, void* userData)
{
  _pEventLoop->interIsolate().notify(request, userData);
}

void JSIsolate::notify(const Request& request, void* userData)
{
  _pEventLoop->interIsolate().notify(request, userData);
}

void JSIsolate::notify(const std::string& request, void* userData, JSPersistentFunctionHandle* cb)
{
  _pEventLoop->interIsolate().notify(request, userData, cb);
}

void JSIsolate::notify(const Request& request, void* userData, JSPersistentFunctionHandle* cb)
{
  _pEventLoop->interIsolate().notify(request, userData, cb);
}

bool JSIsolate::execute(const std::string& request, std::string& result, uint32_t timeout, void* userData)
{
  return _pEventLoop->interIsolate().execute(request, result, timeout, userData);
}

bool JSIsolate::execute(const Request& request, Result& result, uint32_t timeout, void* userData)
{
  return _pEventLoop->interIsolate().execute(request, result, timeout, userData);
}

void JSIsolate::emit(const std::string& eventName, const OSS::JSON::Array& args, int queueFd)
{
  JSEventArgument event(eventName, args, queueFd);
  _pEventLoop->eventEmitter().emit(event);
}

void JSIsolate::doTask(const Task& cb, void* userData)
{
  _pEventLoop->taskManager().queueTask(cb, userData);
}

void JSIsolate::terminate()
{
  _pEventLoop->terminate();
}

bool JSIsolate::isThreadSelf()
{
  return _threadId == pthread_self();
}

JSIsolate::Ptr JSIsolate::getIsolate()
{
  return JSIsolateManager::instance().findIsolate(pthread_self());
}

JSEventLoop* JSIsolate::eventLoop()
{
  return _pEventLoop;
}

JSModule* JSIsolate::getModuleManager()
{
  return _pModuleManager;
}

JSPluginManager* JSIsolate::getPluginManager()
{
  return _pPluginManager;
}

JSObjectHandle JSIsolate::getGlobal()
{
  v8::Handle<v8::Context> context = v8::Handle<v8::Context>::New( _pIsolate, _context );
  return context->Global();
}

JSValueHandle JSIsolate::parseJSON(const std::string& json)
{
  v8::HandleScope scope(_pIsolate);
  JSContextHandle context = v8::Handle<v8::Context>::New( _pIsolate, _context );
  JSObjectHandle JSON = JSObjectHandle::Cast( getGlobal()->Get(context,JSString(_pIsolate,"JSON")).ToLocalChecked() );
  JSValueHandle parseFunc = JSON->Get(context,JSString(_pIsolate,"parse")).ToLocalChecked();
  JSFunctionHandle parse = JSFunctionHandle::Cast(parseFunc);

  JSValueHandle val = JSString(_pIsolate,json);
  JSArgumentVector args;
  args.push_back(val);
  
  return parse->Call(context, getGlobal(), args.size(), args.data()).ToLocalChecked();
}

void JSIsolate::join()
{
  if (_pThread)
  {
    _pThread->join();
    delete _pThread;
    _pThread = 0;
  }
}

JSLocalObjectHandle JSIsolate::wrapExternalPointer(void* ptr)
{
  JSContextHandle context = v8::Handle<v8::Context>::New( _pIsolate, _context );
  JSObjectTemplateHandle objectTemplate = JSObjectTemplateHandle::New( _pIsolate, _objectTemplate );
  JSLocalObjectHandle pObject = objectTemplate->NewInstance(context).ToLocalChecked();
  pObject->SetInternalField(0, JSExternal(_pIsolate,ptr));
  return pObject;
}

} } 



