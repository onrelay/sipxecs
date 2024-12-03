
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
#include "OSS/UTL/Thread.h"
#include "OSS/UTL/CoreUtils.h"
#include "OSS/UTL/BlockingQueue.h"
#include "OSS/UDNS/dnsresolver.h"
#include "OSS/UTL/Logger.h"
#include "OSS/JS/modules/Async.h"
#include "OSS/JS/JSIsolate.h"
#include "OSS/JS/JSEventLoop.h"

#include <poll.h>

using namespace OSS::UDNS;

#define POOL_SIZE 10
#define MAX_TIMEOUT_SEC 1
typedef OSS::BlockingQueue<DNSResolver*> ContextPool;
typedef std::map<int, DNSContext*> ContextMap;
static ContextPool _pool;
static ContextMap _map;
JSCopyablePersistentFunctionHandle* _work_cb = 0;
pollfd pfds[POOL_SIZE];

static void initialize_pool(uint32_t size)
{
  int index = 0;
  for (uint32_t i = 0; i < size; i++)
  {
    DNSContext* context = new DNSContext();
    context->context(true);
    DNSResolver* resolver = new DNSResolver(context, true);
    _pool.enqueue(resolver);
    pfds[index].fd = context->getSocketFd();
    pfds[index].events = POLLIN;
    ++index;
    _map[context->getSocketFd()] = context;
  }
}

static void relinquish_context(DNSResolver* resolver)
{
  assert(resolver);
  OSS::JS::JSIsolate::Ptr pIsolate = OSS::JS::JSIsolate::getIsolate();
  if (pIsolate)
  {
    pIsolate->eventLoop()->fdManager().removeFileDescriptor(resolver->context()->getSocketFd());
    _pool.enqueue(resolver);
  }
}

template <typename T>
void call_result_callback( v8::Isolate* isolate, const T& record, const JSLocalValueHandle& result, void* userData)
{
  v8::Local<v8::Context> context = isolate->GetCurrentContext();
  v8::Local<v8::Object> global = context->Global();

  JSCopyablePersistentFunctionHandle* persistentCallback = (JSCopyablePersistentFunctionHandle*)userData;
  
  JSLocalValueHandle common = JSObject(isolate);
  common->ToObject(context).ToLocalChecked()->Set(context, JSString(isolate,"cname"), JSString(isolate,record.getCName().c_str()));
  common->ToObject(context).ToLocalChecked()->Set(context, JSString(isolate,"qname"), JSString(isolate,record.getQName().c_str()));
  common->ToObject(context).ToLocalChecked()->Set(context, JSString(isolate,"ttl"), JSUint32(isolate,record.getTTL()));
  
  JSLocalArgumentVector args;
  args.push_back(result);
  args.push_back(common);

  JSFunctionHandle callback = persistentCallback->Get(isolate);
  
  JSValueHandle obj = callback->Get(context, JSString(isolate, "resolver")).ToLocalChecked();

  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj->ToObject(context).ToLocalChecked());
  relinquish_context(resolver);
  
  if (obj->ToObject(context).ToLocalChecked()->HasOwnProperty(context,JSString(isolate,"timerId")).ToChecked())
  {
    JSValueHandle timerId = obj->ToObject(context).ToLocalChecked()->Get(context,JSString(isolate,"timerId")).ToLocalChecked();
    Async::clear_timer(timerId->Int32Value(context).ToChecked());
  }
  
  callback->Call(context, global, args.size(), args.data());
  persistentCallback->Reset();
  delete persistentCallback;
  
  JSFunctionHandle workCallback = _work_cb->Get(isolate);

  workCallback->Call(context, global, 0, 0);
}

static void on_a_lookup(const DNSARecordV4& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSAddressList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate, records.size());
  std::size_t index = 0;
  for (DNSAddressList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    result->ToObject(context).ToLocalChecked()->Set(context, index++, JSString(isolate, *iter));
  }
  call_result_callback<DNSARecordV4>(isolate, record, result, userData);
}

static void on_aaaa_lookup(const DNSARecordV6& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSAddressList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate, records.size());
  std::size_t index = 0;
  for (DNSAddressList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    result->ToObject(context).ToLocalChecked()->Set(context, index++, JSString(isolate, *iter));
  }
  call_result_callback<DNSARecordV6>(isolate, record, result, userData);
}

static void on_srv_lookup(const DNSSRVRecord& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSSRVRecordList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate,records.size());
  std::size_t index = 0;
  for (DNSSRVRecordList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    JSObjectHandle srv = JSObject(isolate);
    srv->Set(context,JSString(isolate,"priority"), JSInt32(isolate,iter->priority));
    srv->Set(context,JSString(isolate,"weight"), JSInt32(isolate,iter->weight));
    srv->Set(context,JSString(isolate,"port"), JSInt32(isolate,iter->port));
    srv->Set(context,JSString(isolate,"name"), JSString(isolate,iter->name));
    result->ToObject(context).ToLocalChecked()->Set(context, index++, srv);
  }
  call_result_callback<DNSSRVRecord>(isolate, record, result, userData);
}

static void on_ptr_lookup(const DNSPTRRecord& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSPTRRecordList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate,records.size());
  std::size_t index = 0;
  for (DNSPTRRecordList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    result->ToObject(context).ToLocalChecked()->Set(context, index++, JSString(isolate,*iter));
  }
  call_result_callback<DNSPTRRecord>(isolate, record, result, userData);
}

static void on_txt_lookup(const DNSTXTRecord& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSTXTRecordList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate,records.size());
  std::size_t index = 0;
  for (DNSTXTRecordList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    result->ToObject(context).ToLocalChecked()->Set(context, index++, JSString(isolate,*iter));
  }
  call_result_callback<DNSTXTRecord>(isolate, record, result, userData);
}

static void on_naptr_lookup(const DNSNAPTRRecord& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSNAPTRRecordList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate,records.size());
  std::size_t index = 0;
  for (DNSNAPTRRecordList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    JSObjectHandle srv = JSObject(isolate);
    srv->Set(context, JSString(isolate,"order"), JSInt32(isolate,iter->order));
    srv->Set(context, JSString(isolate,"preference"), JSInt32(isolate,iter->preference));
    srv->Set(context, JSString(isolate,"flags"), JSString(isolate,iter->flags));
    srv->Set(context, JSString(isolate,"service"), JSString(isolate,iter->service));
    srv->Set(context, JSString(isolate,"regexp"), JSString(isolate,iter->regexp));
    srv->Set(context, JSString(isolate,"replacement"), JSString(isolate,iter->flags));
    result->ToObject(context).ToLocalChecked()->Set(context, index++, srv);
  }
  call_result_callback<DNSNAPTRRecord>(isolate, record, result, userData);
}

static void on_mx_lookup(const DNSMXRecord& record, void* userData)
{
  v8::Isolate* isolate = js_get_v8_isolate();
  v8::Local<v8::Context> context = isolate->GetCurrentContext();

  const DNSMXRecordList& records = record.getRecords();
  JSLocalValueHandle result = JSArray(isolate,records.size());
  std::size_t index = 0;
  for (DNSMXRecordList::const_iterator iter = records.begin(); iter != records.end(); iter++)
  {
    JSObjectHandle srv = JSObject(isolate);
    srv->Set(context,JSString(isolate,"priority"), JSInt32(isolate,iter->priority));
    srv->Set(context,JSString(isolate,"name"), JSString(isolate,iter->name));
    result->ToObject(context).ToLocalChecked()->Set(context,index++, srv);
  }
  call_result_callback<DNSMXRecord>(isolate, record, result, userData);
}

JS_METHOD_IMPL(__lookup_a)
{
  static DNSARecordV4CB resolver_cb = boost::bind(on_a_lookup, _1, _2);
  
  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(),js_method_string("resolver"), obj);
  
  resolver->resolveA4(query, 0, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_aaaa)
{
  static DNSARecordV6CB resolver_cb = boost::bind(on_aaaa_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolveA6(query, 0, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_srv)
{
   static DNSSRVRecordCB resolver_cb = boost::bind(on_srv_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolveSRV(query, 0, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_ptr4)
{
  static DNSPTRRecordCB resolver_cb = boost::bind(on_ptr_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolvePTR4(query, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_ptr6)
{
  static DNSPTRRecordCB resolver_cb = boost::bind(on_ptr_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolvePTR6(query, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_naptr)
{
  static DNSNAPTRRecordCB resolver_cb = boost::bind(on_naptr_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolveNAPTR(query, 0, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_txt)
{
  static DNSTXTRecordCB resolver_cb = boost::bind(on_txt_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolveTXT(query, 0, DNS_C_ANY, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__lookup_mx)
{
  static DNSMXRecordCB resolver_cb = boost::bind(on_mx_lookup, _1, _2);

  js_method_args_assert_size_eq(3);
  js_method_arg_assert_string(0);
  js_method_arg_assert_function(1);
  js_method_arg_assert_object(2);
  
  std::string query = js_method_arg_as_std_string(0);
  JSCopyablePersistentFunctionHandle* persistentCallback = new JSCopyablePersistentFunctionHandle; 
  *persistentCallback = js_method_arg_as_persistent_function(1);
  JSFunctionHandle callback = persistentCallback->Get(js_method_isolate());
  JSLocalObjectHandle obj = js_method_arg_as_object(2);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  callback->Set(js_method_context(), js_method_string("resolver"), obj);
  
  resolver->resolveMX(query, 0, resolver_cb, persistentCallback);
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(),MAX_TIMEOUT_SEC, 0)));
}

JS_METHOD_IMPL(__relinquish_context)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_object(0);
  JSLocalObjectHandle obj = js_method_arg_as_object(0);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  relinquish_context(resolver);
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__acquire_context)
{
  
  DNSResolver* resolver;
  _pool.dequeue(resolver);
  if (!resolver)
  {
    js_method_throw("Unable to acquire context");
  }
  
  JSObjectHandle obj = js_method_wrap_pointer_to_local_object(resolver);
  obj->Set(js_method_context(), js_method_string("fd"), js_method_uint32(resolver->context()->getSocketFd()));
  
  js_method_set_return_handle(obj);
}

JS_METHOD_IMPL(__get_context_count)
{
  js_method_set_return_handle(js_method_uint32(_pool.size()));
}

JS_METHOD_IMPL(__process_io_events)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_uint32(0);
  uint32_t fd = js_method_arg_as_uint32(0);
  
  ContextMap::iterator iter = _map.find(fd);
  if (iter != _map.end())
  {
    dns_ioevent(iter->second->context(), 0);
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__set_work_callback)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_function(0);
  _work_cb = new JSCopyablePersistentFunctionHandle; 
  *_work_cb = js_method_arg_as_persistent_function(0);
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__get_next_context_timeout)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_object(0);
  JSLocalObjectHandle obj = js_method_arg_as_object(0);
  DNSResolver* resolver = js_unwrap_pointer_from_local_object<DNSResolver>(obj);
  if (!resolver)
  {
    js_method_set_return_handle(js_method_int32(-1));
    return;
  }
  js_method_set_return_handle(js_method_int32(dns_timeouts(resolver->context()->context(), MAX_TIMEOUT_SEC, 0)));
}


JS_EXPORTS_INIT()
{
  initialize_pool(POOL_SIZE);
  js_export_method("_acquire_context", __acquire_context);
  js_export_method("_relinquish_context", __relinquish_context);
  js_export_method("_get_context_count", __get_context_count);
  js_export_method("_process_io_events", __process_io_events);
  js_export_method("_set_work_callback", __set_work_callback);
  js_export_method("_get_next_context_timeout", __get_next_context_timeout);
  js_export_method("_lookup_a", __lookup_a);
  js_export_method("_lookup_aaaa", __lookup_aaaa);
  js_export_method("_lookup_srv", __lookup_srv);
  js_export_method("_lookup_txt", __lookup_txt);
  js_export_method("_lookup_mx", __lookup_mx);
  js_export_method("_lookup_ptr", __lookup_ptr4);
  js_export_method("_lookup_ptr4", __lookup_ptr4);
  js_export_method("_lookup_ptr6", __lookup_ptr6);
  js_export_method("_lookup_naptr", __lookup_naptr);
  js_export_finalize();
}

JS_REGISTER_MODULE(JSDNS);
