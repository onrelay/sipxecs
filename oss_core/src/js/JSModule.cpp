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

#include "OSS/build.h"
#include "OSS/UTL/CoreUtils.h"
#if ENABLE_FEATURE_V8

#include "OSS/JS/JS.h"
#include "OSS/JS/JSModule.h"
#include "OSS/JS/JSPlugin.h"
#include "OSS/JS/JSUtil.h"
#include "OSS/UTL/Logger.h"
#include "OSS/JS/JSIsolate.h"
#include "OSS/JS/JSIsolateManager.h"


namespace OSS {
namespace JS {


boost::filesystem::path JSModule::_mainScript;
  
static JSModule* get_current_module_manager()
{
  return JSIsolateManager::instance().getIsolate()->getModuleManager();
}
  
static JS_METHOD_IMPL(js_include) 
{
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);
  
  for (int i = 0; i < _args_.Length(); i++) 
  {
    std::string fileName = string_from_js_value(js_method_isolate(), _args_[i]);
    if (boost::filesystem::exists(fileName))
    {
      v8::Handle<v8::String> script = js_method_string( read_file(fileName) );
      v8::MaybeLocal<v8::Script> maybeCompiled = v8::Script::Compile(js_method_context(),script);
      if( maybeCompiled.IsEmpty() )
      {
        // The TryCatch above is still in effect and will have caught the error.
        report_js_exception(js_method_isolate(), _try_catch_, true);
        js_method_set_return_undefined();
        return;
      }
      v8::Handle<v8::Script> compiled = maybeCompiled.ToLocalChecked();
      v8::MaybeLocal<v8::Value> maybeResult = compiled->Run(js_method_context());
      if (maybeResult.IsEmpty())
      {
        // The TryCatch above is still in effect and will have caught the error.
        report_js_exception(js_method_isolate(), _try_catch_, true);
        js_method_set_return_undefined();
        return;
      }
      v8::Handle<v8::Value> result = maybeResult.ToLocalChecked();
      js_method_set_return_handle( result );
      return;
    }
    else
    {
      OSS_LOG_ERROR("Unable to locate external script " << fileName);
    }
  }
  js_method_set_return_undefined();
}

static bool module_path_exists(const std::string& canonicalName, std::string& absolutePath)
{
  if (OSS::string_starts_with(canonicalName, "/"))
  {
    if (boost::filesystem::exists(boost::filesystem::path(canonicalName.c_str())))
    {
      absolutePath = canonicalName;
      return true;
    }
    return false;
  }

  if (OSS::string_starts_with(canonicalName, "~/"))
  {
    boost::filesystem::path currentPath(getenv("HOME"));
    currentPath = OSS::boost_path_concatenate(currentPath, canonicalName.substr(2, std::string::npos));
    if (boost::filesystem::exists(currentPath))
    {
      absolutePath = OSS::boost_path(currentPath);
      return true;
    }
    return false;
  }

  if (OSS::string_starts_with(canonicalName, "./"))
  {
    boost::filesystem::path currentPath = boost::filesystem::current_path();
    currentPath = OSS::boost_path_concatenate(currentPath, canonicalName.substr(2, std::string::npos));
    if (boost::filesystem::exists(currentPath))
    {
      absolutePath = OSS::boost_path(currentPath);
      return true;
    }
    return false;
  }

  boost::filesystem::path path(canonicalName.c_str());
  boost::filesystem::path absPath = boost::filesystem::absolute(path);
  if (boost::filesystem::exists(absPath))
  {
    absolutePath = OSS::boost_path(absPath);
    return true;
  }

  //
  // check it against the directory of the main script
  //
  boost::filesystem::path parent_path = JSModule::_mainScript.parent_path();
  absPath = OSS::boost_path_concatenate(parent_path, canonicalName);
  if (boost::filesystem::exists(absPath))
  {
    absolutePath = OSS::boost_path(absPath);
    return true;
  }
  
  //
  // check it against the oss_modules directory of the main script
  //
  parent_path = JSModule::_mainScript.parent_path();
  boost::filesystem::path module_path = OSS::boost_path_concatenate(parent_path, "oss_modules");
  absPath = OSS::boost_path_concatenate(module_path, canonicalName);
  if (boost::filesystem::exists(absPath))
  {
    absolutePath = OSS::boost_path(absPath);
    return true;
  }

  //
  // Check it against current path
  //
  absPath = OSS::boost_path_concatenate(boost::filesystem::current_path(), canonicalName);
  if (boost::filesystem::exists(absPath))
  {
    absolutePath = OSS::boost_path(absPath);
    return true;
  }

  
  //
  // Check the global module directory
  //
  const JSModule::ModulesDir& modulesDir = get_current_module_manager()->getModulesDir();
  for (JSModule::ModulesDir::const_iterator iter = modulesDir.begin(); iter != modulesDir.end(); iter++)
  {
    boost::filesystem::path modDir(iter->c_str());
    absPath = OSS::boost_path_concatenate(modDir, canonicalName);
    if (boost::filesystem::exists(absPath))
    {
      absolutePath = OSS::boost_path(absPath);
      return true;
    }
  }

  return false;
}

JS_METHOD_IMPL(__add_module_directory) 
{
  js_method_enter_scope();
  js_method_declare_string(path, 0);
  get_current_module_manager()->setModulesDir(path);
  js_method_set_return_undefined();
}

static std::string get_plugin_canonical_file_name(const std::string& fileName)
{
  try
  {
    std::string canonicalName = fileName;
    OSS::string_trim(canonicalName);
    
    if (!OSS::string_ends_with(canonicalName, ".jso"))
    {
      canonicalName += ".jso";
    }

    std::string absolutePath;
    if (module_path_exists(canonicalName, absolutePath))
    {
      return absolutePath;
    }
  }
  catch(...)
  {
  }
  return fileName;
}

static std::string get_directory_module_canonical_file_name(const std::string& fileName)
{
  try
  {
    std::string canonicalName = fileName;
    OSS::string_trim(canonicalName);
    
    if (!OSS::string_ends_with(canonicalName, "/index.js"))
    {
      canonicalName += "/index.js";
    }

    std::string absolutePath;
    if (module_path_exists(canonicalName, absolutePath))
    {
      return absolutePath;
    }
  }
  catch(...)
  {
  }
  return fileName;
}

static std::string get_module_canonical_file_name(const std::string& fileName)
{
  try
  {
    std::string canonicalName = fileName;
    OSS::string_trim(canonicalName);
    
    JSModule::InternalModules& modules = get_current_module_manager()->getInternalModules();
    JSModule::InternalModules::iterator iter = modules.find(fileName);
    if (iter != modules.end())
    {
      return fileName;
    }
    
    if (OSS::string_ends_with(canonicalName, ".jso"))
    {
      return get_plugin_canonical_file_name(fileName);
    }

    if (!OSS::string_ends_with(canonicalName, ".js"))
    {
      canonicalName += ".js";
    }

    std::string absolutePath;
    if (module_path_exists(canonicalName, absolutePath))
    {
      return absolutePath;
    }
  }
  catch(...)
  {
  }
  
  boost::filesystem::path directoryModule(get_directory_module_canonical_file_name(fileName));
  if (boost::filesystem::exists(directoryModule))
  {
    return OSS::boost_path(directoryModule);
  }
  
  return get_plugin_canonical_file_name(fileName);
}

static JS_METHOD_IMPL(js_get_module_cononical_file_name) 
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);
  std::string fileName = string_from_js_value(js_method_isolate(), _args_[0]);
  std::string canonical = get_module_canonical_file_name(fileName);
  if (canonical.empty())
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(canonical.c_str());
}

static JS_METHOD_IMPL(js_load_plugin) 
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);
  std::string fileName = string_from_js_value(js_method_isolate(), _args_[0]);
  js_method_declare_isolate(pIsolate);
  JSPlugin* pPlugin = pIsolate->getPluginManager()->loadPlugin(fileName);
  if (!pPlugin)
  {
    js_method_set_return_undefined();
    return;
  }
  
  std::string exportFunc;
  if (pPlugin->initExportFunc(js_method_isolate(), exportFunc))
  {
    JSStringHandle func_name = js_method_string(exportFunc.c_str());
    js_method_set_return_handle(js_method_global()->Get(js_method_context(),func_name).ToLocalChecked());
    return;
  }
  
  js_method_set_return_undefined();
}

static JS_METHOD_IMPL(js_get_module_script) 
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);

  std::string fileName = string_from_js_value(js_method_isolate(), _args_[0]);
  
  JSModule::InternalModules& modules = get_current_module_manager()->getInternalModules();
  JSModule::InternalModules::iterator iter = modules.find(fileName);
  if (iter != modules.end())
  {
    js_method_set_return_string(iter->second.script);
    return;
  }
  
  if (boost::filesystem::exists(fileName))
  {
    js_method_set_return_string(read_file(fileName));
    return;
  }
  else
  {
    OSS_LOG_ERROR("Unable to locate module " << fileName);
  }
  js_method_set_return_undefined();
}

static JS_METHOD_IMPL(js_compile)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);
  
  v8::Handle<v8::String> script = v8::Handle<v8::String>::Cast(_args_[0]);
  v8::ScriptOrigin name(JSStringHandle::Cast(_args_[1]));
  v8::MaybeLocal<v8::Script> maybeCompiled = v8::Script::Compile(js_method_context(),script, &name);
  if( maybeCompiled.IsEmpty() )
  {
    // The TryCatch above is still in effect and will have caught the error.
    report_js_exception(js_method_isolate(), _try_catch_, true);
    js_method_set_return_undefined();
    return;
  }
  v8::Handle<v8::Script> compiled = maybeCompiled.ToLocalChecked();
  v8::MaybeLocal<v8::Value> maybeResult = compiled->Run(js_method_context());
  if (maybeResult.IsEmpty())
  {
    // The TryCatch above is still in effect and will have caught the error.
    report_js_exception(js_method_isolate(), _try_catch_, true);
    js_method_set_return_undefined();
    return;
  }
  v8::Handle<v8::Value> result = maybeResult.ToLocalChecked();
  js_method_set_return_handle( result );
}

static JS_METHOD_IMPL(js_compile_module)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  js_method_try_catch();
  _try_catch_.SetVerbose(true);
  
  std::ostringstream strm;
  strm << "( function(module, exports) {";
  strm << "\"use-strict\";";
  strm << "try {";
  strm << string_from_js_value(js_method_isolate(), _args_[0]);
  strm << "} catch(e) { e.printStackTrace(); }";
  strm << "});";

  JSStringHandle script = js_method_string(strm.str()); 
  //v8::Handle<v8::Script> compiled = v8::Script::New(script, name); // changed in V8 3.25
  v8::ScriptOrigin name(JSStringHandle::Cast(_args_[1]));
  v8::MaybeLocal<v8::Script> maybeCompiled = v8::Script::Compile(js_method_context(),script, &name);
  if( maybeCompiled.IsEmpty() )
  {
    // The TryCatch above is still in effect and will have caught the error.
    report_js_exception(js_method_isolate(), _try_catch_, true);
    js_method_set_return_undefined();
    return;
  }
  v8::Handle<v8::Script> compiled = maybeCompiled.ToLocalChecked();

  std::string fileName = *v8::String::Utf8Value(js_method_isolate(),_args_[1]);
  boost::filesystem::path path(fileName.c_str());
  boost::filesystem::path parent_path = path.parent_path();
  boost::filesystem::path current_path = boost::filesystem::current_path();
  v8::MaybeLocal<v8::Value> maybeResult = compiled->Run(js_method_context());
  if (maybeResult.IsEmpty())
  {
    // The TryCatch above is still in effect and will have caught the error.
    report_js_exception(js_method_isolate(), _try_catch_, true);
    js_method_set_return_undefined();
    return;
  }
  v8::Handle<v8::Value> result = maybeResult.ToLocalChecked();
  js_method_set_return_handle( result );
}

JS_METHOD_IMPL(js_lock_isolate)
{
  JSIsolateManager::instance().modulesMutex().lock();
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(js_unlock_isolate)
{
  JSIsolateManager::instance().modulesMutex().unlock();
  js_method_set_return_undefined();
}

JSModule::JSModule(JSIsolate* pIsolate) :
  _pIsolate(pIsolate)
{
  _modulesDir.push_back(OSS::system_libdir() + "/oss_modules");
}

JSModule::~JSModule()
{
}

bool JSModule::initialize(v8::Isolate* isolate, v8::TryCatch& try_catch, v8::Handle<v8::ObjectTemplate>& global)
{
  //
  // Register the helpers
  //
  Module modules_js;
  modules_js.name = "modules.js";
  modules_js.script = std::string(
    #include "js/OSSJS_modules.js.h"
  );
  registerModuleHelper(modules_js);
  return compileModuleHelpers(isolate, try_catch, global);
}

void JSModule::registerInternalModule(const Module& module)
{
  assert(_modules.find(module.name) == _modules.end());
  _modules[module.name] = module;
}

void JSModule::registerModuleHelper(const Module& module)
{
  _moduleHelpers.push_back(module);
}

JS_METHOD_IMPL(__chdir)
{
  js_method_enter_scope();
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_string(0);
  std::string dir = js_method_arg_as_std_string(0);
  js_method_set_return_handle(js_method_int32(chdir(dir.c_str())));
}

JS_METHOD_IMPL(__current_path)
{
  js_method_enter_scope();
  boost::filesystem::path path = boost::filesystem::current_path();
  js_method_set_return_string(OSS::boost_path(path).c_str());
}

JS_METHOD_IMPL(__parent_path)
{
  js_method_enter_scope();
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_string(0);
  std::string pathStr = js_method_arg_as_std_string(0);
  boost::filesystem::path path(pathStr.c_str());
  boost::filesystem::path parent = path.parent_path();
  js_method_set_return_string(OSS::boost_path(parent).c_str());
}

bool JSModule::initGlobalExports(v8::Isolate* isolate, v8::Handle<v8::ObjectTemplate>& global)
{
  global->Set(JSString(isolate,"__include"), JSFunctionTemplate(isolate,js_include));
  global->Set(JSString(isolate,"__compile"), JSFunctionTemplate(isolate,js_compile));
  global->Set(JSString(isolate,"__compile_module"), JSFunctionTemplate(isolate,js_compile_module));
  global->Set(JSString(isolate,"__get_module_script"), JSFunctionTemplate(isolate,js_get_module_script));
  global->Set(JSString(isolate,"__get_module_cononical_file_name"), JSFunctionTemplate(isolate,js_get_module_cononical_file_name));
  global->Set(JSString(isolate,"__load_plugin"), JSFunctionTemplate(isolate,js_load_plugin));
  global->Set(JSString(isolate,"__current_path"), JSFunctionTemplate(isolate,__current_path));
  global->Set(JSString(isolate,"__parent_path"), JSFunctionTemplate(isolate,__parent_path));
  global->Set(JSString(isolate,"__chdir"), JSFunctionTemplate(isolate,__chdir));
  global->Set(JSString(isolate,"__lock_isolate"), JSFunctionTemplate(isolate,js_lock_isolate));
  global->Set(JSString(isolate,"__unlock_isolate"), JSFunctionTemplate(isolate,js_unlock_isolate));
  global->Set(JSString(isolate,"__add_module_directory"), JSFunctionTemplate(isolate,__add_module_directory));
  return true;
}

bool JSModule::compileModuleHelpers(v8::Isolate* isolate, v8::TryCatch& try_catch, v8::Handle<v8::ObjectTemplate>& global)
{
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  for (ModuleHelpers::iterator iter = _moduleHelpers.begin(); iter != _moduleHelpers.end(); iter++)
  {
    v8::Handle<v8::String> script(JSString(isolate,iter->script));
    v8::ScriptOrigin name(JSString(isolate,iter->name));
    v8::MaybeLocal<v8::Script> maybeCompiled = v8::Script::Compile(context,script, &name);
    if( maybeCompiled.IsEmpty() )
    {
      OSS_LOG_ERROR("JSModule::compileModuleHelpers is unable to compile " << iter->name);
      // The TryCatch above is still in effect and will have caught the error.
      report_js_exception(isolate, try_catch, true);
      return false;
    }
    v8::Handle<v8::Script> compiled = maybeCompiled.ToLocalChecked();

    v8::MaybeLocal<v8::Value> maybeResult = compiled->Run(context);
    if (maybeResult.IsEmpty())
    {
      OSS_LOG_ERROR("JSModule::compileModuleHelpers is unable to run " << iter->name);
      // The TryCatch above is still in effect and will have caught the error.
      report_js_exception(isolate, try_catch, true);
      return false;
    }
  } 
  return true;
}

JSIsolate* JSModule::getIsolate()
{
  return _pIsolate;
}




} }

#endif // ENABLE_FEATURE_V8


