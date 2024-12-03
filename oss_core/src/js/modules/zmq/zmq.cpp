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

#include <list>
#include "OSS/JS/JSPlugin.h"
#include "OSS/JS/modules/ZMQSocketObject.h"
#include "OSS/JS/modules/BufferObject.h"
#include "OSS/UTL/Logger.h"
#include "OSS/UTL/Semaphore.h"

using OSS::JS::JSObjectWrap;
boost::thread* ZMQSocketObject::_pPollThread;
typedef std::list<ZMQSocketObject*> Sockets;
static Sockets _socketList;
static OSS::mutex_critic_sec _socketListMutex;
static ZMQSocketObject* _notifyReceiver = 0;
static ZMQSocketObject* _notifySender = 0;
typedef OSS::ZMQ::ZMQSocket::PollItems PollItems;
static bool _wakeupPipeEnabled = false;
static zmq::context_t* _context = 0;
static OSS::Semaphore* _notifySem;
static bool _isTerminating = false;


static void __wakeup_pipe()
{
  if (_wakeupPipeEnabled)
  {
    std::string buf(" ");
    _notifySender->getSocket()->sendRequest(buf);
  }
}

void ZMQSocketObject::notifyReadable()
{
  std::size_t w = 0;
  w = write(_pipe[1], " ", 1);
  (void)w;
  _notifySem->wait();
}

void ZMQSocketObject::clearReadable()
{
  std::size_t r = 0;
  char buf[1];
  r = read(_pipe[0], buf, 1);
  (void)r;
  _notifySem->signal();
}

void ZMQSocketObject::pollForReadEvents()
{
   _wakeupPipeEnabled = true;
  while (!_isTerminating)
  {
    PollItems items;
    zmq_pollitem_t notifier;
    _notifyReceiver->getSocket()->initPollItem(notifier);
    items.push_back(notifier);
    {
      OSS::mutex_critic_sec_lock lock(_socketListMutex);
      for (Sockets::iterator iter = _socketList.begin(); iter != _socketList.end(); iter++)
      {
        zmq_pollitem_t item;
        (*iter)->getSocket()->initPollItem(item);
        items.push_back(item); 
      }
    }

    int rc = OSS::ZMQ::ZMQSocket::poll(items, -1);

    if (_isTerminating)
    {
      break;
    }
    else if (rc < 0) 
    {
      if (zmq_errno() == EINTR) 
      {
        continue;
      } 
      else 
      {
        break;
      }
    }

    for (PollItems::iterator iter = items.begin(); iter != items.end(); iter++)
    {
      zmq_pollitem_t& pfd = *iter;
      if (pfd.revents & ZMQ_POLLIN)
      {
        if (pfd.socket == _notifyReceiver->getSocket()->socket()->get())
        {
          std::string buf;
          assert(_notifyReceiver->getSocket()->receiveReply(buf));
        }
        else
        {
          bool found = false;
          {
            OSS::mutex_critic_sec_lock lock(_socketListMutex);
            for (Sockets::iterator iter = _socketList.begin(); iter != _socketList.end(); iter++)
            {
              if (pfd.socket == (*iter)->getSocket()->socket()->get())
              {
                (*iter)->notifyReadable();
                found = true;
                break;
              }
            }
          }

          if (found)
          {
            break;
          }
        }
        break;
      }
    }
  }
}

ZMQSocketObject::ZMQSocketObject(Socket::SocketType type) :
  _pSocket(0)
{
  _pSocket = new Socket(type, _context);
  ::pipe(_pipe);

}

ZMQSocketObject::~ZMQSocketObject()
{
  delete _pSocket;
  OSS::mutex_critic_sec_lock lock(_socketListMutex);
  _socketList.remove(this);
  __wakeup_pipe();
}


JS_CONSTRUCTOR_IMPL(ZMQSocketObject)
{
  js_method_enter_scope();

  if (_args_.Length() < 1 || !_args_[0]->IsInt32())
  {
    js_method_throw("Invalid Argument.  Must provide socket type.");
  }

  ZMQSocketObject* pSocket = new ZMQSocketObject((Socket::SocketType)_args_[0]->Int32Value(js_method_context()).ToChecked());
  pSocket->Wrap(js_method_self());
  
  js_method_set_return_self();
}

static bool __get_string_arg(JSCallbackInfo _args_, std::string& value)
{
  js_method_enter_scope();
  if (_args_.Length() == 0 || !_args_[0]->IsString())
  {
    return false;
  }
  value = *v8::String::Utf8Value(js_method_isolate(),_args_[0]);
  return true;
}

static bool __get_buffer_arg(JSCallbackInfo _args_, std::string& value)
{
  js_method_enter_scope();
  if (_args_.Length() == 0 || !BufferObject::isBuffer(js_method_isolate(), _args_[0]))
  {
    return false;
  }
  BufferObject* pBuffer = JSObjectWrap::Unwrap<BufferObject>(_args_[0]->ToObject(js_method_context()).ToLocalChecked());
  std::copy(pBuffer->buffer().begin(), pBuffer->buffer().end(), std::back_inserter(value));
  return true;
}

JS_METHOD_IMPL(ZMQSocketObject::connect)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string arg;
  if (!__get_string_arg(_args_, arg))
  {
    js_method_throw("Invalid Argument");
  }
   
  bool ret = pObject->_pSocket->connect(arg);
  if (ret)
  {
    OSS::mutex_critic_sec_lock lock(_socketListMutex);
    _socketList.push_back(pObject);
    __wakeup_pipe();
  }
  js_method_set_return_boolean(ret);
}

JS_METHOD_IMPL(ZMQSocketObject::bind)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string arg;
  if (!__get_string_arg(_args_, arg))
  {
    js_method_throw("Invalid Argument");
  }
  bool ret = pObject->_pSocket->bind(arg);
  if (ret)
  {
    OSS::mutex_critic_sec_lock lock(_socketListMutex);
    _socketList.push_back(pObject);
    __wakeup_pipe();
  }
  js_method_set_return_boolean(ret);
}

JS_METHOD_IMPL(ZMQSocketObject::subscribe)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string arg;
  if (!__get_string_arg(_args_, arg) && !__get_buffer_arg(_args_, arg))
  {
    js_method_throw("Invalid Argument");
  }
  js_method_set_return_boolean(pObject->_pSocket->subscribe(arg));
}

JS_METHOD_IMPL(ZMQSocketObject::publish)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string arg;
  if (!__get_string_arg(_args_, arg) && !__get_buffer_arg(_args_, arg))
  {
    js_method_throw("Invalid Argument");
  }
  js_method_set_return_boolean(pObject->_pSocket->publish(arg));
}

JS_METHOD_IMPL(ZMQSocketObject::send)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string arg;
  if (!__get_string_arg(_args_, arg) && !__get_buffer_arg(_args_, arg))
  {
    js_method_throw("Invalid Argument");
  }
  js_method_set_return_boolean(pObject->_pSocket->sendRequest(arg));
}

JS_METHOD_IMPL(ZMQSocketObject::receive)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  std::string msg;
  v8::Handle<v8::Value> result = js_method_undefined();
  if (pObject->_pSocket->receiveReply(msg, 0))
  {
    BufferObject* pBuffer = 0;
    if (_args_.Length() == 1 && BufferObject::isBuffer(js_method_isolate(), _args_[0]))
    {
      result = _args_[0];
      pBuffer = JSObjectWrap::Unwrap<BufferObject>(_args_[0]->ToObject(js_method_context()).ToLocalChecked());
      if (msg.size() > pBuffer->buffer().size())
      {
        js_method_throw("Size of read buffer is too small");
      }
      std::copy(msg.begin(), msg.end(), pBuffer->buffer().begin());
    }
    else
    {
      js_method_throw("Read buffer not provided");
    }
  }
  result->ToObject(js_method_context()).ToLocalChecked()->Set(js_method_context(), js_method_string("payloadSize"), js_method_uint32(msg.size()));
  pObject->clearReadable();
  js_method_set_return_handle(result);
}

JS_METHOD_IMPL(ZMQSocketObject::close)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  pObject->_pSocket->close();
  _socketList.remove(pObject);
  __wakeup_pipe();
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(ZMQSocketObject::getFd)
{
  js_method_enter_scope();
  ZMQSocketObject* pObject = js_method_unwrap_self(ZMQSocketObject);

  js_method_set_return_handle(js_method_int32(pObject->_pipe[0]));
}
  
JS_CLASS_INTERFACE(ZMQSocketObject,"ZMQSocket")
{
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "connect", connect);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "bind", bind);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "publish", publish);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "subscribe", subscribe);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "send", send);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "receive", receive);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "close", close);
  JS_CLASS_METHOD_DEFINE(ZMQSocketObject, "getFd", getFd);

  JS_CLASS_INTERFACE_END(ZMQSocketObject);

  _context = new zmq::context_t(1);
  _notifySem = new OSS::Semaphore();
  _notifyReceiver = new ZMQSocketObject(OSS::ZMQ::ZMQSocket::PULL);
  _notifySender = new ZMQSocketObject(OSS::ZMQ::ZMQSocket::PUSH);
  assert(_notifyReceiver->getSocket()->bind("inproc://zmq_notifier"));
  assert(_notifySender->getSocket()->connect("inproc://zmq_notifier"));
  ZMQSocketObject::_pPollThread = new boost::thread(boost::bind(pollForReadEvents));
}

JS_METHOD_IMPL(cleanup_exports)
{
  js_method_enter_scope();

  if (_isTerminating)
  {
    //
    // Don't let this function be called twice or we will double free stuff
    //
    js_method_set_return_undefined();
    return;
  }

  _isTerminating = true;
  __wakeup_pipe();
  
  
  //
  // Disable the wakeup pipe from here forward
  //
  _wakeupPipeEnabled = false;
  ZMQSocketObject::_pPollThread->join();
  delete ZMQSocketObject::_pPollThread;
  delete _notifyReceiver;
  delete _notifySender;
  delete _notifySem;
  //
  // Intentionally leak the context for it might block if there are
  // pending I/O 
  //
  // delete _context ;

  js_method_set_return_undefined();
}

JS_EXPORTS_INIT()
{  
  js_export_class(ZMQSocketObject);

  js_export_method("__cleanup_exports",cleanup_exports);
  
  js_export_int32("REQ",OSS::ZMQ::ZMQSocket::REQ);
  js_export_int32("REP",OSS::ZMQ::ZMQSocket::REP);
  js_export_int32("PUSH",OSS::ZMQ::ZMQSocket::PUSH);
  js_export_int32("PULL",OSS::ZMQ::ZMQSocket::PULL);
  js_export_int32("PUB",OSS::ZMQ::ZMQSocket::PUB);
  js_export_int32("SUB",OSS::ZMQ::ZMQSocket::SUB);

  js_export_finalize();
}

JS_REGISTER_MODULE(JSZMQSocket);


