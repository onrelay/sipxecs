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


#ifndef OSS_JS_INCLUDED
#define OSS_JS_INCLUDED


#define OSS_JS_MAJOR_VERSION 1
#define OSS_JS_MINOR_VERSION 0
#define OSS_JS_BUILD_NUMBER 0


#include <v8.h>
#include "OSS/OSS.h"
#include <vector>

#include "OSS/JS/JSObjectWrap.h"

#define js_get_v8_isolate() v8::Isolate::GetCurrent()
#define js_get_v8_context() js_get_v8_isolate()->GetCurrentContext()
#define js_get_v8_global() js_get_v8_context()->Global()
//#define js_get_v8_global() OSS::JS::JSIsolateManager::instance().getIsolate()->getGlobal()

// Type Definitions
//
#define JSAccessorInfo const v8::PropertyCallbackInfo<v8::Value>& 
#define JSSetAccessorInfo const v8::PropertyCallbackInfo<void>& 
#define JSCallbackInfo const v8::FunctionCallbackInfo<v8::Value>&
#define JSValueHandle v8::Local<v8::Value>
#define JSLocalValueHandle v8::Local<v8::Value>
#define JSMaybeLocalValueHandle v8::MaybeLocal<v8::Value>
#define JSUndefinedHandle v8::Local<v8::Primitive>
#define JSStringHandle v8::Local<v8::String>
#define JSBooleanHandle v8::Local<v8::Boolean>
#define JSIntegerHandle v8::Local<v8::Integer>
#define JSLocalStringHandle v8::Local<v8::String>
#define JSArrayHandle v8::Handle<v8::Array>
#define JSLocalArrayHandle v8::Local<v8::Array>
#define JSContextHandle v8::Local<v8::Context>
#define JSObjectHandle v8::Local<v8::Object>
#define JSExternalHandle v8::Local<v8::External>
#define JSObjectTemplateHandle v8::Local<v8::ObjectTemplate>
#define JSLocalObjectHandle v8::Local<v8::Object>
#define JSLocalObjectTemplateHandle v8::Local<v8::ObjectTemplate>
#define JSPersistentValueHandle v8::Persistent<v8::Value>
#define JSCopyablePersistentValueHandle v8::Persistent<v8::Value, v8::CopyablePersistentTraits<v8::Value>>
#define JSPersistentObjectHandle v8::Persistent<v8::Object>
#define JSPersistentObjectTemplateHandle v8::Persistent<v8::ObjectTemplate>
#define JSCopyablePersistentObjectTemplateHandle v8::Persistent<v8::ObjectTemplate, v8::CopyablePersistentTraits<v8::ObjectTemplate>>
#define JSPersistentContextHandle v8::Persistent<v8::Context>
#define JSCopyablePersistentContextHandle v8::Persistent<v8::Context, v8::CopyablePersistentTraits<v8::Context>>
#define JSExternalHandle v8::Local<v8::External> 
#define JSPersistentFunctionHandle v8::Persistent<v8::Function>
#define JSCopyablePersistentFunctionHandle v8::Persistent<v8::Function, v8::CopyablePersistentTraits<v8::Function>>
#define JSPersistentFunctionTemplateHandle v8::Persistent<v8::FunctionTemplate>
#define JSFunctionTemplateHandle v8::Local<v8::FunctionTemplate>
#define JSFunctionHandle v8::Local<v8::Function>
#define JSLocalFunctionHandle v8::Local<v8::Function>
#define JSArgumentVector std::vector< v8::Local<v8::Value> >
#define JSLocalArgumentVector std::vector< v8::Local<v8::Value> >
#define JSPersistentArgumentVector std::vector< v8::Persistent<v8::Value> >
#define JSCopyablePersistentArgumentVector std::vector< v8::Persistent<v8::Value, v8::CopyablePersistentTraits<v8::Value>> >

//
// Helper functions
//
//#define js_throw(what) JSException(what)
//#define js_assert(Expression, what) if (!(Expression)) { js_throw(what); }
//#define js_is_function(handle) handle->IsFunction()
//#define js_get_global_method(name) js_get_v8_global()->Get(JSString(name))
//#define js_enter_scope(isolate) v8::HandleScope _scope_(isolate)
//#define js_method_try_catch() v8::TryCatch _try_catch_(js_get_v8_isolate());
#define js_unwrap_object(class, object) OSS::JS::JSObjectWrap::Unwrap<class>(object)

inline JSStringHandle JSString(v8::Isolate* isolate, const char* str) { return v8::String::NewFromUtf8(isolate,str).ToLocalChecked(); }
//inline JSStringHandle JSString(const char* str) { return JSString(js_get_v8_isolate(),str); }
inline JSStringHandle JSString(v8::Isolate* isolate, const std::string& str) { return v8::String::NewFromUtf8(isolate, str.data(), v8::NewStringType::kNormal ).ToLocalChecked(); }
//inline JSStringHandle JSString(const std::string& str) { return JSString(js_get_v8_isolate(),str); }
inline JSStringHandle JSString(v8::Isolate* isolate, const char* str, std::size_t len) { return v8::String::NewFromUtf8(isolate, str, v8::NewStringType::kNormal, len).ToLocalChecked(); }
//inline JSStringHandle JSString(const char* str, std::size_t len) { return JSString(js_get_v8_isolate(), str, len); }
inline JSBooleanHandle JSBoolean(v8::Isolate* isolate, bool exp) { return v8::Boolean::New(isolate,exp); }
//inline JSBooleanHandle JSBoolean( bool exp) { return v8::Boolean::New(js_get_v8_isolate(),exp); }
inline JSArrayHandle JSArray(v8::Isolate* isolate, int size) { return v8::Array::New(isolate,size); }
//inline JSArrayHandle JSArray(int size) { return v8::Array::New(js_get_v8_isolate(),size); }
inline JSIntegerHandle JSInt32(v8::Isolate* isolate, int32_t value) { return v8::Int32::New(isolate,value); }
//inline JSIntegerHandle JSInt32(int32_t value) { return v8::Int32::New(js_get_v8_isolate(),value); }
inline JSIntegerHandle JSUint32(v8::Isolate* isolate, uint32_t value) { return v8::Uint32::NewFromUnsigned(isolate,value); }
//inline JSIntegerHandle JSUint32(uint32_t value) { return v8::Uint32::NewFromUnsigned(js_get_v8_isolate(),value); }
inline JSIntegerHandle JSInteger(v8::Isolate* isolate, int value) { return v8::Integer::New(isolate,value); }
//inline JSIntegerHandle JSInteger( int value) { return v8::Integer::New(js_get_v8_isolate(),value); }
inline JSObjectHandle JSObject(v8::Isolate* isolate) { return  v8::Object::New(isolate); }
//inline JSObjectHandle JSObject() { return v8::Object::New(js_get_v8_isolate()); }
inline JSObjectTemplateHandle JSObjectTemplate(v8::Isolate* isolate) { return v8::ObjectTemplate::New(isolate); }
//inline JSObjectTemplateHandle JSObjectTemplate() { return v8::ObjectTemplate::New(js_get_v8_isolate()); }
inline JSExternalHandle JSExternal(v8::Isolate* isolate, void* ptr) { return v8::External::New(isolate,ptr); }
//inline JSExternalHandle JSExternal(void* ptr) { return v8::External::New(js_get_v8_isolate(),ptr); }
inline JSUndefinedHandle JSUndefined(v8::Isolate* isolate) { return v8::Undefined(isolate); }
//inline JSUndefinedHandle JSUndefined() { return v8::Undefined(js_get_v8_isolate()); }
inline JSFunctionTemplateHandle JSFunctionTemplate(v8::Isolate* isolate,v8::FunctionCallback callback) { return v8::FunctionTemplate::New(isolate,callback); }
//inline JSFunctionTemplateHandle JSFunctionTemplate(v8::FunctionCallback callback) { return v8::FunctionTemplate::New(js_get_v8_isolate(),callback); }

inline void JSException(v8::Isolate* isolate, const char* what) { isolate->ThrowException(v8::Exception::Error(JSString(isolate,what))); }
inline void JSException(v8::Isolate* isolate, std::string what) { isolate->ThrowException(v8::Exception::Error(JSString(isolate,what))); }
//inline void JSException(const char* what) { js_get_v8_isolate()->ThrowException(v8::Exception::Error(JSString(what))); }
//inline void JSException(std::string what) { js_get_v8_isolate()->ThrowException(v8::Exception::Error(JSString(what))); }


//
// Class and method implementation macros
//


#define JS_GLOBAL_FUNCTION_DECLARE(global) void global(JSCallbackInfo _args_)
#define JS_GLOBAL_FUNCTION_IMPL(function) void function(JSCallbackInfo _args_)

#define JS_METHOD_DECLARE(method) static void method(JSCallbackInfo _args_)
#define JS_METHOD_IMPL(method) void method(JSCallbackInfo _args_)

#define JS_INDEX_GETTER_DECLARE(method) static void method(uint32_t index, JSAccessorInfo _args_);
#define JS_INDEX_GETTER_IMPL(method) void method(uint32_t index, JSAccessorInfo _args_)

#define JS_INDEX_SETTER_DECLARE(method) static void method(uint32_t index, v8::Local<v8::Value> value, JSAccessorInfo _args_);
#define JS_INDEX_SETTER_IMPL(method) void method(uint32_t index,v8::Local<v8::Value> value, JSAccessorInfo _args_)

#define JS_ACCESSOR_GETTER_DECLARE(method) static void method(v8::Local<v8::Value> property, JSAccessorInfo _args_);
#define JS_ACCESSOR_GETTER_IMPL(method) void method(v8::Local<v8::Name> property, JSAccessorInfo _args_)

#define JS_ACCESSOR_SETTER_DECLARE(method) static void method(v8::Local<v8::Value> property, v8::Local<v8::Value> value, JSSetAccessorInfo _args_);
#define JS_ACCESSOR_SETTER_IMPL(method) void method(v8::Local<v8::Name> property, v8::Local<v8::Value> value, JSSetAccessorInfo _args_)

#define JS_CONSTRUCTOR_DECLARE() \
  static JSCopyablePersistentFunctionHandle _constructor; \
  static void Init(v8::Isolate* isolate, JSObjectHandle exports); \
  static void New(JSCallbackInfo _args_);

#define JS_CONSTRUCTOR_IMPL(class) JS_METHOD_IMPL(class::New)

#define js_method_self() _args_.This()
#define js_method_args_length() _args_.Length()
#define js_method_arg(index) _args_[index]
#define js_method_isolate() _args_.GetIsolate()  
#define js_method_context() js_method_isolate()->GetCurrentContext()
#define js_method_global() js_method_context()->Global()

#define js_method_string(value) JSString(js_method_isolate(),value)
#define js_method_boolean(value) JSBoolean(js_method_isolate(),value)
#define js_method_array(size) JSArray(js_method_isolate(),size)
#define js_method_int32(value) JSInt32(js_method_isolate(),value)
#define js_method_uint32(value) JSUint32(js_method_isolate(),value)
#define js_method_integer(value) JSInteger(js_method_isolate(),value)
#define js_method_object() JSObject(js_method_isolate())
#define js_method_object_template() JSObjectTemplate(js_method_isolate())
#define js_method_external(ptr) JSExternal(js_method_isolate(),ptr)
#define js_method_undefined() JSUndefined(js_method_isolate())
#define js_method_function_template(callback) JSFunctionTemplate(js_method_isolate(),callback)

#define js_method_try_catch() v8::TryCatch _try_catch_(js_method_isolate());
#define js_method_exception(what) JSException(js_method_isolate(),what)

#define js_method_arg_is_object(index) _args_[index]->IsObject()
#define js_method_arg_is_string(index) _args_[index]->IsString()
#define js_method_arg_is_array(index) _args_[index]->IsArray()
#define js_method_arg_is_number(index) _args_[index]->IsNumber()
#define js_method_arg_is_int32(index) _args_[index]->IsInt32()
#define js_method_arg_is_uint32(index) _args_[index]->IsUint32()
#define js_method_arg_is_bool(index) _args_[index]->IsBoolean()
#define js_method_arg_is_date(index) _args_[index]->IsDate()
#define js_method_arg_is_buffer(index) BufferObject::isBuffer(js_method_isolate(),_args_[index])
#define js_method_arg_is_function(index) _args_[index]->IsFunction()

#define js_method_args_assert_size_eq(value) if (_args_.Length() != value) js_method_throw("Invalid Argument Count")
#define js_method_args_assert_size_gt(value) if (_args_.Length() <= value) js_method_throw("Invalid Argument Count")
#define js_method_args_assert_size_lt(value) if (_args_.Length() >= value) js_method_throw("Invalid Argument Count")
#define js_method_args_assert_size_gteq(value) if (_args_.Length() < value) js_method_throw("Invalid Argument Count")
#define js_method_args_assert_size_lteq(value) if (_args_.Length() > value) js_method_throw("Invalid Argument Count")
#define js_method_arg_assert_object(index) if (!js_method_arg_is_object(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_string(index) if (!js_method_arg_is_string(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_array(index) if (!js_method_arg_is_array(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_number(index) if (!js_method_arg_is_number(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_int32(index) if (!js_method_arg_is_int32(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_uint32(index) if (!js_method_arg_is_uint32(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_bool(index) if (!js_method_arg_is_bool(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_date(index) if (!js_method_arg_is_date(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_buffer(index) if (!js_method_arg_is_buffer(index)) js_method_throw("Invalid Argument Type")
#define js_method_arg_assert_function(index) if (!js_method_arg_is_function(index)) js_method_throw("Invalid Argument Type")

#define js_method_arg_as_object(index) _args_[index]->ToObject(js_method_context()).ToLocalChecked()
#define js_method_arg_as_string(index) v8::String::Utf8Value(js_method_isolate(),_args_[index])
#define js_method_arg_as_std_string(index) std::string((const char*) *js_method_arg_as_string(index))
#define js_method_arg_as_array(index) JSArrayHandle::Cast(_args_[index])
#define js_method_arg_as_integer(index) _args_[index]->IntegerValue(js_method_context()).ToChecked()
#define js_method_arg_as_number(index) _args_[index]->NumberValue(js_method_context()).ToChecked()
#define js_method_arg_as_int32(index) _args_[index]->Int32Value(js_method_context()).ToChecked()
#define js_method_arg_as_uint32(index) _args_[index]->Uint32Value(js_method_context()).ToChecked()
#define js_method_arg_as_bool(index) _args_[index]->BooleanValue(js_method_isolate())

#define js_method_unwrap_object(class, index) js_unwrap_object(class, js_method_arg_as_object(index))
#define js_method_unwrap_self(class) js_unwrap_object(class, js_method_self())

#define js_method_arg_as_buffer(index) js_method_unwrap_object(BufferObject, index)
#define js_method_arg_as_persistent_function(index) JSCopyablePersistentFunctionHandle(js_method_isolate(),v8::Handle<v8::Function>::Cast(_args_[index]))

#define js_method_enter_scope() v8::HandleScope _scope_(js_method_isolate())
#define js_method_throw(what) js_method_exception(what)
#define js_method_assert(expression, what) if (!(expression)) { js_method_throw(what); }

#define js_setter_info_unwrap_self js_method_unwrap_self
#define js_getter_info_unwrap_self js_method_unwrap_self
#define js_setter_value_as_uint32() value->Uint32Value(js_method_context()).ToChecked()
#define js_setter_index() index
#define js_getter_index js_setter_index

#define js_method_handle_as_string(handle) v8::String::Utf8Value(js_method_isolate(),handle)
#define js_method_handle_as_std_string(handle) std::string((const char*) *js_method_handle_as_string(handle))

#define js_method_type(type, var, index, func, msg) \
  type var(0); \
  if (index >= _args_.Length()) \
    js_method_throw("Invalid Argument Count"); \
  if (!func(_args_, var, _args_[index])) \
    js_method_throw(msg);

#define js_method_instance(class, instance, index, func, msg) \
  class instance; \
  if (index >= _args_.Length()) \
    js_method_throw("Invalid Argument Count"); \
  if (!func(_args_, instance, _args_[index])) \
    js_method_throw(msg);

#define js_method_declare_bool(var, index) js_method_type(bool, var, index, js_method_assign_bool, "Invalid Type.  Expecting Bool")
#define js_method_declare_uint32(var, index) js_method_type(uint32_t, var, index, js_method_assign_uint32, "Invalid Type.  Expecting UInt32")
#define js_method_declare_int32(var, index) js_method_type(int32_t, var, index, js_method_assign_int32, "Invalid Type.  Expecting Int32")
#define js_method_declare_number(var, index) js_method_type(double, var, index, js_method_assign_number, "Invalid Type.  Expecting Nymber")
#define js_method_declare_string(var, index) js_method_type(std::string, var, index, js_method_assign_string, "Invalid Type.  Expecting String")
#define js_method_declare_array(var, index) js_method_instance(JSArrayHandle, var, index, js_method_assign_array, "Invalid Class.  Expecting Array")
#define js_method_declare_object(var, index) js_method_instance(JSObjectHandle, var, index, js_method_assign_object, "Invalid Class.  Expecting Object")
#define js_method_declare_unwrapped_object(class, var, index) js_method_declare_object(_##var##__temp, index); class* var = js_unwrap_object(class, _##var##__temp->ToObject())
#define js_method_declare_external_object(class, var, index) js_method_type(class*, var, index, js_method_assign_external_object<class>, "Invalid Class.  Expecting External Object")
#define js_method_declare_function(var, index) js_method_instance(JSLocalFunctionHandle, var, index, js_method_assign_function, "Invalid Type.  Expecting Function") 
#define js_method_declare_persistent_function(var, index) js_method_instance(JSPersistentFunctionHandle, var, index, js_method_assign_persistent_function, "Invalid Class.  Expecting Function") 
#define js_method_declare_copyable_persistent_function(var, index) js_method_instance(JSCopyablePersistentFunctionHandle, var, index, js_method_assign_persistent_function, "Invalid Class.  Expecting Function") 
#define js_method_declare_self(class, var) class* var = js_method_unwrap_self(class)

#define js_method_declare_isolate(var) OSS::JS::JSIsolate::Ptr var = OSS::JS::JSIsolateManager::instance().getIsolate(); \
  if (!var) { js_method_throw("Unable to retrieve isolate"); }

#define js_export_method(name, method) exports->Set(js_method_context(),js_method_string(name), v8::FunctionTemplate::New(js_method_isolate(),method)->GetFunction(js_method_context()).ToLocalChecked())
#define js_export_global_constructor(name, method) js_method_global()->Set(js_method_context(), js_method_string(name), method)
#define js_export_value(name, value) exports->Set(js_method_context(), js_method_string(name), value)
#define js_export_const(name) exports->Set( js_method_context(), js_method_string(#name), js_method_integer(name));
#define js_export_string(name, value) exports->Set(js_method_context(), js_method_string(name), js_method_string(value))
#define js_export_string_symbol(name) exports->Set(js_method_context(), js_method_string(name), js_method_string(name))
#define js_export_int32(name, value) exports->Set(js_method_context(), js_method_string(name), js_method_int32(value))
#define js_export_uint32(name, value) exports->Set(js_method_context(), js_method_string(name), js_method_uint32(value))
#define js_export_accessor(name, getFunc, setFunc) js_method_global()->SetAccessor(js_method_context(), js_method_string(name), getFunc, setFunc)

#define JS_CLASS_INTERFACE(class, name) \
  JSCopyablePersistentFunctionHandle class::_constructor; \
  void class::Init(v8::Isolate* isolate, v8::Handle<v8::Object> exports) {\
    v8::HandleScope scope(isolate); \
    std::string className = name; \
    v8::Local<v8::FunctionTemplate> tpl = OSS::JS::JSObjectWrap::ExportConstructorTemplate<class>(isolate, name, exports);

#define JS_CLASS_METHOD_DEFINE(class, name, method) OSS::JS::JSObjectWrap::ExportMethod<class>(isolate, tpl, name, method)

#define JS_CLASS_INDEX_ACCESSOR_DEFINE(class, getter, setter) OSS::JS::JSObjectWrap::ExportIndexHandler<class>(isolate, tpl, getter, setter);
#define JS_CLASS_INTERFACE_END(class) } OSS::JS::JSObjectWrap::FinalizeConstructorTemplate<class>(isolate, className.c_str(), tpl, exports);
#define js_export_class(class) class::Init(js_method_isolate(), exports)

#define JS_EXPORTS_INIT() static void init_exports(JSCallbackInfo _args_) { \
  js_method_enter_scope(); \
  v8::Local<v8::Object> exports = v8::Local<v8::Object>::New(js_method_isolate(),v8::Object::New(js_method_isolate()));

#define js_export_finalize() } _args_.GetReturnValue().Set(exports);



// Value Definitions
//
#define JSPersistentFunctionCast(handle) JSPersistentFunctionHandle::New(JSFunctionHandle::Cast(handle))

#define js_function_call(function, data, size) function->Call(js_method_context(), js_method_global(), size, data)

template <typename T>
bool js_method_assign_external_object(JSCallbackInfo _args_, T*& value, const JSValueHandle& handle)
{
  if (!handle->IsObject())
  {
    return false;
  }
  value = js_unwrap_object(T, handle->ToObject(js_method_context()).ToLocalChecked());
  return true;
}

inline bool js_method_assign_persistent_function(JSCallbackInfo _args_, JSCopyablePersistentFunctionHandle& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsFunction())
  {
    return false;
  } 
  JSFunctionHandle functionHandle = JSFunctionHandle::Cast(handle);
  value = JSCopyablePersistentFunctionHandle(js_method_isolate(),functionHandle);
  return true; 
}

inline bool js_method_assign_function(JSCallbackInfo _args_, JSLocalFunctionHandle& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsFunction())
  {
    return false;
  } 
  value = JSLocalFunctionHandle::New(js_method_isolate(),v8::Handle<v8::Function>::Cast(handle));
  return true; 
}

inline bool js_method_assign_uint32(JSCallbackInfo _args_, uint32_t& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsUint32())
  {
    return false;
  } 
  value =  handle->ToUint32(js_method_context()).ToLocalChecked()->Value();
  return true; 
}

inline bool js_method_assign_int32(JSCallbackInfo _args_, int32_t& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsInt32())
  {
    return false;
  } 
  value =  handle->ToInt32(js_method_context()).ToLocalChecked()->Value();
  return true; 
}

inline bool js_method_assign_number(JSCallbackInfo _args_, double& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsNumber())
  {
    return false;
  } 
  value =  handle->ToNumber(js_method_context()).ToLocalChecked()->Value();
  return true; 
}

inline bool js_method_assign_array(JSCallbackInfo _args_, JSArrayHandle& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsArray())
  {
    return false;
  } 
  value =  JSArrayHandle::Cast(handle);
  return true; 
}

inline bool js_method_assign_string(JSCallbackInfo _args_, std::string& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsString())
  {
    return false;
  } 
  value = (const char*) (*v8::String::Utf8Value(js_method_isolate(),handle)); 
  return true; 
}


inline bool js_method_assign_bool(JSCallbackInfo _args_, bool& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsBoolean())
  {
    return false;
  } 
  value = handle->ToBoolean(js_method_isolate())->Value(); 
  return true; 
}

inline bool js_method_assign_object(JSCallbackInfo _args_, JSObjectHandle& value, const JSValueHandle& handle) 
{ 
  if (!handle->IsObject())
  {
    return false;
  } 
  value = handle->ToObject(js_method_context()).ToLocalChecked();
  return true; 
}

inline void js_assign_persistent_arg_vector(v8::Isolate* isolate, JSCopyablePersistentArgumentVector& output, const JSValueHandle& handle)
{
  if (handle->IsArray())
  {
    v8::Handle<v8::Array> arrayArg = v8::Handle<v8::Array>::Cast(handle);
    for (std::size_t i = 0; i <arrayArg->Length(); i++)
    {
      v8::Handle<v8::Value> arrayItem = arrayArg->Get(isolate->GetCurrentContext(),i).ToLocalChecked();
      output.push_back(v8::Persistent<v8::Value>(isolate,arrayItem));
    }
  }
  else
  {
    output.push_back(v8::Persistent<v8::Value>(isolate,handle));
  }
}

//inline void js_assign_persistent_arg_vector(JSCopyablePersistentArgumentVector& output, const JSValueHandle& handle)
//{
//  js_assign_persistent_arg_vector( js_get_v8_isolate(), output, handle );
//}

//inline void js_method_assign_persistent_arg_vector(JSCopyablePersistentArgumentVector& output, const JSValueHandle& handle)
//{
//  js_assign_persistent_arg_vector( js_method_isolate(), output, handle );
//}



inline JSObjectHandle js_method_wrap_pointer_to_local_object(JSCallbackInfo _args_, void* ptr)
{
  JSLocalObjectTemplateHandle objectTemplate = JSObjectTemplate(js_method_isolate());
  objectTemplate->SetInternalFieldCount(1);
  
  JSLocalObjectTemplateHandle classTemplate = JSLocalObjectTemplateHandle::New(js_method_isolate(),objectTemplate);
  JSObjectHandle JSObjectWrapper = classTemplate->NewInstance(js_method_context()).ToLocalChecked();
  JSExternalHandle objectPointer = JSExternal(js_method_isolate(),ptr);
  JSObjectWrapper->SetInternalField(0, objectPointer);
  return JSObjectWrapper;
}

#define js_method_wrap_pointer_to_local_object(ptr) js_method_wrap_pointer_to_local_object(_args_,ptr)

template <typename T>
T* js_unwrap_pointer_from_local_object(JSObjectHandle obj, uint32_t index = 0)
{
  JSExternalHandle ptr = JSExternalHandle::Cast(obj->GetInternalField(index));
  return static_cast<T*>(ptr->Value());
}

#define js_method_set_return_arg(value) _args_.GetReturnValue().Set(value)

inline bool js_method_set_return(JSCallbackInfo _args_, const JSValueHandle& handle )
{
  if (!handle->IsObject())
  {
    js_method_set_return_arg(js_method_undefined());
    return false;
  }
  js_method_set_return_arg(handle);
  return true;
}

inline bool js_method_set_return(JSAccessorInfo _args_, const JSValueHandle& handle )
{
  if (!handle->IsObject())
  {
    js_method_set_return_arg(js_method_undefined());
    return false;
  }
  js_method_set_return_arg(handle);
  return true;
}

#define js_method_set_return_handle(handle) js_method_set_return(_args_,handle)

#define js_method_set_return_string(value) js_method_set_return_handle(js_method_string(value))

#define js_method_set_return_boolean(value) js_method_set_return_handle(js_method_boolean(value))
#define js_method_set_return_false() js_method_set_return_boolean(false)
#define js_method_set_return_true() js_method_set_return_boolean(true)

#define js_method_set_return_integer(value) js_method_set_return_handle(js_method_integer(value))

inline bool js_method_set_return_undefined(JSCallbackInfo _args_)
{
  js_method_set_return_arg(js_method_undefined());
  return true;
}

inline bool js_method_set_return_undefined(JSAccessorInfo _args_)
{
  js_method_set_return_arg(js_method_undefined());
  return true;
}


#define js_method_set_return_undefined() js_method_set_return_undefined(_args_)

#define js_method_set_return_self() js_method_set_return_arg(js_method_self())

inline void handle_to_persistent_arg_vector(v8::Isolate* isolate, JSValueHandle input, JSCopyablePersistentArgumentVector& output)
{
  if (input->IsArray())
  {
    JSArrayHandle arrayArg = JSArrayHandle::Cast(input);
    for (std::size_t i = 0; i < arrayArg->Length(); i++)
    {
      JSValueHandle item = JSValueHandle::New(isolate,arrayArg->Get(isolate->GetCurrentContext(),i).ToLocalChecked());
      output.push_back(JSPersistentValueHandle(isolate,item));
    }
  }
  else
  {
    output.push_back(JSPersistentValueHandle(isolate,input));
  }
}

//inline void handle_to_persistent_arg_vector(JSValueHandle input, JSCopyablePersistentArgumentVector& output) 
//{
//  handle_to_persistent_arg_vector( js_get_v8_isolate(), input, output );
//}


inline void handle_to_arg_vector(v8::Isolate* isolate, JSValueHandle input, JSArgumentVector& output)
{
  if (input->IsArray())
  {
    JSArrayHandle arrayArg = JSArrayHandle::Cast(input);
    for (std::size_t i = 0; i < arrayArg->Length(); i++)
    {
      JSValueHandle item = JSValueHandle::New(isolate,arrayArg->Get(isolate->GetCurrentContext(),i).ToLocalChecked());
      output.push_back(item);
    }
  }
  else
  {
    output.push_back(input);
  }
}

//inline void handle_to_arg_vector(JSValueHandle input, JSArgumentVector& output)
//{
// handle_to_arg_vector( js_get_v8_isolate(), input, output );
//}


inline void persistent_arg_vector_to_arg_vector(v8::Isolate* isolate, const JSCopyablePersistentArgumentVector& input, JSArgumentVector& output)
{
  for (JSCopyablePersistentArgumentVector::const_iterator iter = input.begin(); iter != input.end(); iter++)
  {
    JSValueHandle item = JSValueHandle::New(isolate, *iter);
    output.push_back(item);
  }
}

//inline void persistent_arg_vector_to_arg_vector(const JSCopyablePersistentArgumentVector& input, JSArgumentVector& output)
//{
//  persistent_arg_vector_to_arg_vector( js_get_v8_isolate(), input, output);
//}





#endif // OSS_JS_INCLUDED





