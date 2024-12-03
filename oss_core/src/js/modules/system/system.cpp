#include <unistd.h>
#include <pthread.h>

#include "OSS/JS/JSPlugin.h"
#include "OSS/UTL/CoreUtils.h"
#include "OSS/UTL/Logger.h"
#include "OSS/JS/modules/BufferObject.h"
#include "OSS/Net/Net.h"




JS_METHOD_IMPL(__close)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_int32(0);
  int fd = js_method_arg_as_int32(0);
  js_method_set_return_handle(js_method_int32(::close(fd)));
}

JS_METHOD_IMPL(__exit)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_int32(0);
  ::exit(js_method_arg_as_int32(0));
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(___exit)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_int32(0);
  ::_exit(js_method_arg_as_int32(0));
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__write)
{
  js_method_args_assert_size_gteq(2);
  js_method_arg_assert_int32(0);
  int32_t fd = js_method_arg_as_int32(0);
  uint32_t size = 0;
  if (js_method_args_length() == 3)
  {
    js_method_arg_assert_uint32(2);
    size = js_method_arg_as_uint32(2);
  }
  
  if (js_method_arg_is_string(1))
  {
    std::string data = js_method_arg_as_std_string(1);
    js_method_set_return_handle(js_method_int32(::write(fd, data.data(), size ? size : data.size())));
    return;
  }
  else if (js_method_arg_is_array(1))
  {
    JSArrayHandle args1 = js_method_arg_as_array(1);
    ByteArray bytes;
    if (!js_int_array_to_byte_array(js_method_isolate(), args1, bytes))
    {
      js_method_set_return_handle(js_method_int32(-1));
      return;
    }
    js_method_set_return_handle(js_method_int32(::write(fd, bytes.data(), size ? size : bytes.size())));
    return;
  }
  else if (js_method_arg_is_buffer(1))
  {
    BufferObject* pBuffer = js_method_unwrap_object(BufferObject, 1);
    js_method_set_return_handle(js_method_int32(::write(fd, pBuffer->buffer().data(), size ? size : pBuffer->buffer().size())));
    return;
  }
  
  js_method_throw("Invalid Argument");
}

JS_METHOD_IMPL(__cout)
{
  js_method_declare_string(msg, 0);
  std::cout << msg;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__oendl)
{
  std::cout << std::endl;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__cerr)
{
  js_method_declare_string(msg, 0);
  std::cerr << msg;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__eendl)
{
  std::cerr << std::endl;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__read)
{
  js_method_args_assert_size_gteq(2);
  js_method_arg_assert_int32(0);
  js_method_arg_assert_uint32(1);

  
  int32_t fd = js_method_arg_as_int32(0);
  uint32_t len = js_method_arg_as_uint32(1);
  
  if (js_method_args_length() == 2)
  {
    //
    // Create a new Buffer
    //
    JSValueHandle result = BufferObject::createNew(js_method_isolate(), len);
    BufferObject* pBuffer = js_unwrap_object(BufferObject, result->ToObject(js_method_context()).ToLocalChecked());

    ByteArray& buf = pBuffer->buffer();
    std::size_t ret = ::read(fd, buf.data(), buf.size());
    if (!ret)
    {
      js_method_set_return_undefined();
      return;
    }
    js_method_set_return_handle(result);
    return;
  }
  else if (js_method_args_length() == 3)
  {
    //
    // Use the provided buffer
    //
    js_method_arg_assert_buffer(2);
    BufferObject* pBuffer = js_method_unwrap_object(BufferObject, 2);
    if (len > pBuffer->buffer().size())
    {
      js_method_throw("Length paramater exceeds buffer size");
    }
    ByteArray& buf = pBuffer->buffer();
    std::size_t ret = ::read(fd, buf.data(), len);
    js_method_set_return_handle(js_method_int32(ret));
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__sleep)
{
  js_method_args_assert_size_eq(1);
  js_method_arg_assert_uint32(0);
  ::sleep(js_method_arg_as_uint32(0));
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__gc)
{
  js_method_isolate()->LowMemoryNotification();
  //while(!v8::V8::IdleNotification());
  // V8 6.5 API Changes: Do not use idle notification at all. This function  has been a no-op for almost all calls for a while now.
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(__setsid)
{
  js_method_set_return_handle(js_method_int32(setsid()));
}

JS_METHOD_IMPL(__getdtablesize)
{
  js_method_set_return_handle(js_method_int32(getdtablesize()));
}

JS_METHOD_IMPL(__getpid)
{
  js_method_set_return_handle(js_method_int32(getpid()));
}

JS_METHOD_IMPL(__getppid)
{
  js_method_set_return_handle(js_method_int32(getppid()));
}

#if !defined(__APPLE__)
JS_METHOD_IMPL(__thread_self)
{
  js_method_set_return_handle(js_method_uint32(pthread_self()));
}
#endif

JS_METHOD_IMPL(__write_pid_file)
{
  std::string pidFile;
  bool exclusive = false;
  
  int argc = js_method_args_length();
  if (argc < 1)
  {
    js_method_set_return_false();
    return;
  } 
  else if (argc == 1)
  {
    pidFile = js_method_arg_as_std_string(0);
  } 
  else if (argc == 2)
  {
    pidFile = js_method_arg_as_std_string(0);
    exclusive = js_method_arg_as_bool(1);
  }
  
  int handle = open(pidFile.c_str(), O_RDWR|O_CREAT, 0600);
  if (handle == -1)
  {
    js_method_set_return_false();
    return;  
  }
  
  if (exclusive && lockf(handle,F_TLOCK,0) == -1)
  {
    js_method_set_return_false();
    return;  
  }
  
  pid_t pid = getpid();
  
  char pidStr[10];
  sprintf(pidStr,"%d\n", pid);
  if (write(handle, pidStr, strlen(pidStr)) == -1)
  {
    js_method_set_return_false();
    return;  
  }
  
  if (!exclusive) {
    close(handle);
  }
  
  js_method_set_return_true();
}

JS_EXPORTS_INIT()
{
  js_export_method("read", __read);
  js_export_method("write", __write);
  js_export_method("close", __close);
  js_export_method("exit", __exit);
  js_export_method("_exit", ___exit);
  js_export_method("sleep", __sleep);
  js_export_method("gc", __gc);
  js_export_method("setsid", __setsid);
  js_export_method("getdtablesize", __getdtablesize);
  js_export_method("getpid", __getpid);
  js_export_method("getppid", __getppid);
  js_export_method("cout", __cout);
  js_export_method("endl", __oendl);
  js_export_method("cerr", __cerr);
  js_export_method("eendl", __eendl);
#if !defined(__APPLE__)
  js_export_method("thread_self", __thread_self);
#endif
  js_export_method("write_pid_file", __write_pid_file);
  

  //
  // Export system directory variables
  //
  js_export_string("PREFIX", OSS::system_prefix().c_str());
  js_export_string("EXEC_PREFIX", OSS::system_exec_prefix().c_str());
  js_export_string("BINDIR", OSS::system_bindir().c_str());
  js_export_string("SBINDIR", OSS::system_sbindir().c_str());
  js_export_string("LIBEXECDIR", OSS::system_libexecdir().c_str());
  js_export_string("DATADIR", OSS::system_datadir().c_str());
  js_export_string("CONFDIR", OSS::system_confdir().c_str());
  js_export_string("LOCALSTATEDIR", OSS::system_localstatedir().c_str());
  js_export_string("INCLUDEDIR", OSS::system_includedir().c_str());
  js_export_string("LIBDIR", OSS::system_libdir().c_str());
  
  //
  // Export default interface info
  //
  std::string iface, local_ipv4, local_ipv6;
  OSS::net_get_default_interface_name(iface);
  OSS::net_get_default_interface_address(local_ipv4, AF_INET);
  OSS::net_get_default_interface_address(local_ipv6, AF_INET6);
  js_export_string("NET_INTERFACE", iface.c_str());
  js_export_string("NET_IPV4", local_ipv4.c_str());
  js_export_string("NET_IPV6", local_ipv6.c_str());
  
  js_export_finalize(); 
}

JS_REGISTER_MODULE(System);
