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
#include "OSS/UTL/Logger.h"
#include "OSS/UTL/CoreUtils.h"


JS_METHOD_IMPL(__log)
{
  if (_args_.Length() < 2 || !_args_[0]->IsInt32() || !_args_[1]->IsString())
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();

  std::string arg1 = *v8::String::Utf8Value(js_method_isolate(),_args_[1]);

  OSS::log(arg1, (OSS::LogPriority)_args_[0]->Int32Value(js_method_context()).ToChecked());

  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__log_init)
{
  if (_args_.Length() < 1 && !_args_[0]->IsString())
  {
    js_method_set_return_undefined();
    return;  
  }
  
  OSS::LogPriority logLevel = OSS::PRIO_INFORMATION;
  std::string format = "%h-%M-%S.%i: %t";
  bool compress = true;
  int purgeCount = 7;
  
  js_method_enter_scope();
  
  std::string path = *v8::String::Utf8Value(js_method_isolate(),_args_[0]);
  if (_args_.Length() >= 2 && _args_[1]->IsInt32())
  {
    logLevel = (OSS::LogPriority)_args_[1]->Int32Value(js_method_context()).ToChecked();
  }
  
  if (_args_.Length() >= 3 && _args_[2]->IsString())
  {
    format = *v8::String::Utf8Value(js_method_isolate(),_args_[2]);
  }
  
  if (_args_.Length() >= 4 && _args_[3]->IsBoolean())
  {
    compress = _args_[3]->BooleanValue(js_method_isolate());
  }
  
  if (_args_.Length() >= 5 && _args_[4]->IsInt32())
  {
    purgeCount = _args_[4]->Int32Value(js_method_context()).ToChecked();
  }
  
  OSS::logger_init(path, logLevel, format, compress ? "true" : "false", OSS::string_from_number<int>(purgeCount));

  js_method_set_return_undefined();
}

JS_ACCESSOR_GETTER_IMPL(__log_level_get) 
{
  js_method_set_return_integer(OSS::log_get_level());
}

JS_ACCESSOR_SETTER_IMPL(__log_level_set) 
{
  OSS::log_reset_level((OSS::LogPriority)value->Int32Value(js_method_context()).ToChecked());
}

JS_EXPORTS_INIT()
{
  
  //
  // Methods
  //

  js_export_method("log", __log );
  js_export_method("init", __log_init );


  //
  // Mutable Properties
  //

  js_export_accessor("level", __log_level_get, __log_level_set);

  //
  // Constants
  //
  js_export_int32("NOTICE",OSS::PRIO_NOTICE);
  js_export_int32("INFO",OSS::PRIO_INFORMATION);
  js_export_int32("DEBUG",OSS::PRIO_DEBUG);
  js_export_int32("TRACE",OSS::PRIO_TRACE);
  js_export_int32("WARNING",OSS::PRIO_WARNING);
  js_export_int32("ERROR",OSS::PRIO_ERROR);
  js_export_int32("CRITICAL",OSS::PRIO_CRITICAL);
  js_export_int32("FATAL",OSS::PRIO_FATAL);
  
  js_export_finalize();
}

JS_REGISTER_MODULE(Logger);


