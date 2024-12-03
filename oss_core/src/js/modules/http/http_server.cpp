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
#include "OSS/UTL/Logger.h"
#include "OSS/JS/modules/HttpServerObject.h"
#include "OSS/JS/modules/Async.h"
#include "OSS/JS/modules/BufferObject.h"


using OSS::JS::JSObjectWrap;

class RequestHandler: public HttpServerObject::HTTPRequestHandler
{
public:
  HttpServerObject* _pSelf;
  RequestHandler(HttpServerObject* pSelf) :
    _pSelf(pSelf)
  {
  }
  void handleRequest(HttpServerObject::HTTPServerRequest& request, HttpServerObject::HTTPServerResponse& response)
  {
    _pSelf->handleRequest(request, response);
  }
};

class RequestHandlerFactory: public HttpServerObject::HTTPRequestHandlerFactory
{
public:
  HttpServerObject* _pSelf;
  RequestHandlerFactory(HttpServerObject* pSelf) :
    _pSelf(pSelf)
  {
  }

  HttpServerObject::HTTPRequestHandler* createRequestHandler(const HttpServerObject::HTTPServerRequest& request)
  {
    return new RequestHandler(_pSelf);
  }
};


JS_CLASS_INTERFACE(HttpServerObject, "HttpServer") 
{
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "setMaxQueued", setMaxQueued);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "setMaxThreads", setMaxThreads);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "_listen", _listen);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "_read", _read);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "_write", _write);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "_sendResponse", _sendResponse);
  JS_CLASS_METHOD_DEFINE(HttpServerObject, "_setRpcId", _setRpcId);
  JS_CLASS_INTERFACE_END(HttpServerObject);
}

JS_CONSTRUCTOR_IMPL(HttpServerObject)
{
  bool useHttps = false;
  if (js_method_args_length() > 0)
  {
    js_method_declare_bool(isSecure, 0);
    useHttps = isSecure;
  }
  
  HttpServerObject* pServer = new HttpServerObject(useHttps);
  pServer->Wrap(js_method_self());
  js_method_set_return_self();
}

HttpServerObject::HttpServerObject(bool secure) :
  _pParams(0),
  _pSocket(0),
  _pHttpServer(0),
  _inputStreamId(0),
  _responseId(0),
  _secure(secure)
{
  _pParams = new HTTPServerParams();
  _pParams->setMaxQueued(100);
  _pParams->setMaxThreads(16);
}

HttpServerObject::~HttpServerObject()
{
  if (!_requestHandler.IsEmpty())
  {
    _requestHandler.Reset();
  }
  delete _pHttpServer;
  delete _pSocket;
}

int HttpServerObject::storeInputStream(std::istream* strm)
{
  OSS::mutex_critic_sec_lock lock(_inputStreamsMutex);
  _inputStreams[++_inputStreamId] = strm;
  return _inputStreamId;
}

std::istream* HttpServerObject::findInputStream(int id)
{
  OSS::mutex_critic_sec_lock lock(_inputStreamsMutex);
  InputStreamMap::iterator iter = _inputStreams.find(id);
  if (iter != _inputStreams.end())
  {
    return iter->second;
  }
  return 0;
}

int HttpServerObject::storeOutputStream(int id, std::ostream* strm)
{
  OSS::mutex_critic_sec_lock lock(_outputStreamsMutex);
  _outputStreams[id] = strm;
  return id;
}

std::ostream* HttpServerObject::findOutputStream(int id)
{
  OSS::mutex_critic_sec_lock lock(_outputStreamsMutex);
  OutputStreamMap::iterator iter = _outputStreams.find(id);
  if (iter != _outputStreams.end())
  {
    return iter->second;
  }
  return 0;
}

void HttpServerObject::removeInputStream(int id)
{
  OSS::mutex_critic_sec_lock lock(_inputStreamsMutex);
  _inputStreams.erase(id);
}

void HttpServerObject::removeOutputStream(int id)
{
  OSS::mutex_critic_sec_lock lock(_outputStreamsMutex);
  _outputStreams.erase(id);
}

int HttpServerObject::storeResponse(HTTPServerResponse* pResponse)
{
  OSS::mutex_critic_sec_lock lock(_responsesMutex);
  _responses[++_responseId] = pResponse;
  return _responseId;
}

HttpServerObject::HTTPServerResponse* HttpServerObject::findResponse(int id)
{
  OSS::mutex_critic_sec_lock lock(_responsesMutex);
  ResponseMap::iterator iter = _responses.find(id);
  if (iter != _responses.end())
  {
    return iter->second;
  }
  return 0;
}

void HttpServerObject::removeResponse(int id)
{
  OSS::mutex_critic_sec_lock lock(_responsesMutex);
  _responses.erase(id);
}

uint32_t HttpServerObject::write(int streamId, const char* data, uint32_t len)
{
  OSS::mutex_critic_sec_lock lock(_outputStreamsMutex);
  OutputStreamMap::iterator iter = _outputStreams.find(streamId);
  if (iter != _outputStreams.end())
  {
    if (iter->second->write(data, len))
    {
      return len;
    }
  }
  return 0;
}

uint32_t HttpServerObject::read(int streamId, char* data, uint32_t len)
{
  OSS::mutex_critic_sec_lock lock(_inputStreamsMutex);
  InputStreamMap::iterator iter = _inputStreams.find(streamId);
  if (iter != _inputStreams.end())
  {
    iter->second->read(data, len);
    return iter->second->gcount();
  }
  return 0;
}

void HttpServerObject::processError(OSS::JSON::Object& result, HTTPServerResponse& response)
{
}

void HttpServerObject::handleRequest(HTTPServerRequest& request, HTTPServerResponse& response)
{
  OSS::JSON::Object requestObject, jsonRequest, jsonParams, jsonReply;
  requestObject["uri"] = OSS::JSON::String(request.getURI());
  for (HTTPServerRequest::ConstIterator iter = request.begin(); iter != request.end(); iter++)
  {
    requestObject[iter->first] = OSS::JSON::String(iter->second);
  }
  
  int inputStreamId = storeInputStream(&request.stream());
  int responseId = storeResponse(&response);
  
  jsonRequest["method"] = OSS::JSON::String("HttpServerObject__handleRequest");
  jsonParams["request"] = requestObject;
  jsonParams["rpcId"] = OSS::JSON::String(_rpcId);
  jsonParams["inputStreamId"] = OSS::JSON::Number(inputStreamId);
  jsonParams["outputStreamId"] = OSS::JSON::Number(responseId);
  jsonParams["clientAddress"] = OSS::JSON::String(request.clientAddress().toString());
  jsonParams["serverAddress"] = OSS::JSON::String(request.serverAddress().toString());
  jsonRequest["arguments"] = jsonParams;
  
  if (Async::json_execute_promise(_pIsolate, jsonRequest, jsonReply))
  {
    OSS::JSON::Object::iterator errorIter = jsonReply.Find("error");
    if (errorIter != jsonReply.End())
    {
      OSS::JSON::Object error = errorIter->element;
      processError(error, response);
    }
  }
  
  removeInputStream(inputStreamId);
  removeResponse(responseId);
}

JS_METHOD_IMPL(HttpServerObject::_sendResponse)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_uint32(responseId, 0);
  js_method_declare_object(response, 1);

  HTTPServerResponse* pResponse = self->findResponse(responseId);
  if (!pResponse)
  {
    js_method_set_return_false();
    return;
  }
  
  JSMaybeLocalValueHandle status = response->Get(js_method_context(),js_method_string("status"));
  JSMaybeLocalValueHandle reason = response->Get(js_method_context(),js_method_string("reason"));
  JSMaybeLocalValueHandle contentType = response->Get(js_method_context(),js_method_string("contentType"));
  JSMaybeLocalValueHandle contentLength = response->Get(js_method_context(),js_method_string("contentLength"));
  JSMaybeLocalValueHandle transferEncoding = response->Get(js_method_context(),js_method_string("transferEncoding"));
  JSMaybeLocalValueHandle chunkedTransferEncoding = response->Get(js_method_context(),js_method_string("chunkedTransferEncoding"));
  JSMaybeLocalValueHandle keepAlive = response->Get(js_method_context(),js_method_string("keepAlive"));
  
  if (!status.IsEmpty() && status.ToLocalChecked()->IsUint32())
  {
    pResponse->setStatus((HTTPStatus)status.ToLocalChecked()->Uint32Value(js_method_context()).ToChecked());
  }
  
  if (!reason.IsEmpty() && reason.ToLocalChecked()->IsString())
  {
    pResponse->setReason(js_method_handle_as_std_string(reason.ToLocalChecked()));
  }
  
  if (!contentType.IsEmpty() && contentType.ToLocalChecked()->IsString())
  {
    pResponse->setContentType(js_method_handle_as_std_string(contentType.ToLocalChecked()));
  }
  
  if (!contentLength.IsEmpty() && contentLength.ToLocalChecked()->IsUint32())
  {
    pResponse->setContentLength(contentLength.ToLocalChecked()->Int32Value(js_method_context()).ToChecked());
  }
  
  if (!transferEncoding.IsEmpty() && transferEncoding.ToLocalChecked()->IsString())
  {
    pResponse->setTransferEncoding(js_method_handle_as_std_string(transferEncoding.ToLocalChecked()));
  }
  
  if (!chunkedTransferEncoding.IsEmpty() && chunkedTransferEncoding.ToLocalChecked()->IsBoolean())
  {
    pResponse->setChunkedTransferEncoding(chunkedTransferEncoding.ToLocalChecked()->ToBoolean(js_method_isolate())->Value());
  }
  
  if (!keepAlive.IsEmpty() && keepAlive.ToLocalChecked()->IsBoolean())
  {
    pResponse->setKeepAlive(keepAlive.ToLocalChecked()->ToBoolean(js_method_isolate())->Value());
  }
  
  
  std::ostream* ostrm = &(pResponse->send());
  
  if (ostrm->good())
  {
    self->storeOutputStream(responseId, ostrm);
  }
  
  self->removeResponse(responseId);
  
  js_method_set_return_boolean(ostrm->good());
}

JS_METHOD_IMPL(HttpServerObject::setMaxQueued)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_uint32(value, 0);
  self->_pParams->setMaxQueued(value);
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(HttpServerObject::setMaxThreads)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_uint32(value, 0);
  self->_pParams->setMaxThreads(value);
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(HttpServerObject::_setRpcId)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_string(rpcId, 0);
  self->_rpcId = rpcId;
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(HttpServerObject::_read)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_int32(streamId, 0);
  js_method_declare_external_object(BufferObject, buf, 1);
  js_method_declare_uint32(size, 2);
  js_method_set_return_handle(js_method_uint32(self->read(streamId, (char*)buf->buffer().data(), size)));
}

JS_METHOD_IMPL(HttpServerObject::_write)
{
  js_method_declare_self(HttpServerObject, self);
  js_method_declare_int32(streamId, 0);
  js_method_declare_external_object(BufferObject, buf, 1);
  js_method_declare_uint32(size, 2);
  js_method_set_return_handle(js_method_uint32(self->write(streamId, (char*)buf->buffer().data(), size)));
}

JS_METHOD_IMPL(HttpServerObject::_listen)
{
  js_method_declare_self(HttpServerObject, self);
  
  if (self->_pSocket)
  {
    js_method_throw("HTTP Server already running");
  }
  
  js_method_declare_string(ip, 0);
  js_method_declare_uint32(port, 1);
  js_method_declare_function(callback, 2);
  
  try
  {
    
    if (ip.empty() || ip == "*")
    {
      IPAddress wildcardAddr;
      SocketAddress address(wildcardAddr, port);
      if (!self->_secure)
      {
        self->_pSocket = new ServerSocket(address);
      }
      else
      {
        self->_pSocket = new SecureServerSocket(address);
      }
    }
    else
    {
      IPAddress ipAddress(ip);
      SocketAddress address(ipAddress, port);
      self->_pSocket = new ServerSocket(address);
    }
    
    self->_pHttpServer = new HTTPServer(new RequestHandlerFactory(self), *self->_pSocket, self->_pParams);
    self->_pHttpServer->start();
    js_function_call(callback, 0, 0);
  }
  catch(const Poco::Exception& e)
  {
    delete self->_pSocket;
    self->_pSocket = 0;
    delete self->_pHttpServer;
    self->_pHttpServer = 0;
    JSArgumentVector jsonArg;
    JSValueHandle error = js_method_string(e.displayText());
    jsonArg.push_back(error);
    js_function_call(callback, jsonArg.data(), jsonArg.size());
  }
  
  js_method_set_return_undefined();
}

JS_EXPORTS_INIT()
{
  js_export_class(HttpServerObject);
  js_export_finalize(); 
}

JS_REGISTER_MODULE(JSHttpServer)