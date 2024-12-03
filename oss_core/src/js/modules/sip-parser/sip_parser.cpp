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


#include <OSS/SIP/SIPHeaderTokens.h>

#include "OSS/UTL/CoreUtils.h"
#include "OSS/Net/Net.h"
#include "OSS/UTL/Logger.h"
#include "v8.h"

#include "OSS/SIP/SIPRequestLine.h"
#include "OSS/SIP/SIPStatusLine.h"
#include "OSS/SIP/SIPURI.h"
#include "OSS/SIP/SIPFrom.h"
#include "OSS/SIP/SIPFrom.h"
#include "OSS/SIP/SIPURI.h"
#include "OSS/SIP/SIPContact.h"
#include "OSS/SIP/SIPCSeq.h"
#include "OSS/SIP/B2BUA/SIPB2BTransaction.h"

#include "OSS/JS/JSModule.h"
#include "OSS/JS/JSUtil.h"


using namespace OSS::SIP;
using B2BUA::SIPB2BTransaction;

JS_METHOD_IMPL(msgGetMethod)
{
  if (_args_.Length() != 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    std::string cseq = pMsg->hdrGet(OSS::SIP::HDR_CSEQ);
    OSS::SIP::SIPCSeq hCSeq(cseq.c_str());
    js_method_set_return_string(hCSeq.getMethod().c_str());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgGetMethod - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrPresent)
{
  if (_args_.Length() != 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->hdrPresent(headerName.c_str()));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrPresent - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrGetSize)
{
  if (_args_.Length() != 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_undefined();
    return;
  }
  
  try
  {
    js_method_set_return_integer(pMsg->hdrGetSize(headerName.c_str()));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrGetSize - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrGet)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_undefined();
    return;
  }
  try
  {
    if (_args_.Length() > 2)
    {
      size_t index = _args_[2]->NumberValue(js_method_context()).ToChecked();
      js_method_set_return_string(pMsg->hdrGet(headerName.c_str(), index).c_str());
      return;
    }

    js_method_set_return_string(pMsg->hdrGet(headerName.c_str()).c_str());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrGet - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrSet)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_false();
    return;
  }

  std::string headerValue = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  if (headerValue.empty())
  {
    js_method_set_return_false();
    return;
  }

  size_t index = 0;
  if (_args_.Length() > 3)
    index = _args_[3]->NumberValue(js_method_context()).ToChecked();
  
  try
  {
    js_method_set_return_boolean(pMsg->hdrSet(headerName.c_str(), headerValue, index));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrSet - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrRemove)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_false();
    return;
  }
  
  try
  {
    js_method_set_return_boolean(pMsg->hdrRemove(headerName.c_str()));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrRemove - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgHdrListAppend)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_false();
    return;
  }

  std::string headerValue = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  if (headerValue.empty())
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->hdrListAppend(headerName.c_str(), headerValue));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrListAppend - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgHdrListPrepend)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_false();
    return;
  }

  std::string headerValue = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  if (headerValue.empty())
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->hdrListPrepend(headerName.c_str(), headerValue));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrListPrepend - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgHdrListPopFront)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_string(pMsg->hdrListPopFront(headerName.c_str()).c_str());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrListPopFront - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgHdrListRemove)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string headerName = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (headerName.empty())
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->hdrListRemove(headerName.c_str()));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrListRemove - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgIsRequest)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string method;
  if (_args_.Length() >= 2)
    method = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  try
  {
    if (method.empty())
    {
      js_method_set_return_boolean(pMsg->isRequest());
      return;
    }
    else
    {
      js_method_set_return_boolean(pMsg->isRequest(method.c_str()));
      return;
    }
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIsRequest - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}


JS_METHOD_IMPL(msgIsResponse)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->isResponse());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIsResponse - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs1xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is1xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs1xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs2xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is2xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs2xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs3xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is3xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs3xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs4xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is4xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs4xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs5xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is5xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs5xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIs6xx)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->is6xx());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIs6xx - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIsResponseFamily)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  int responseCode = _args_[1]->NumberValue(js_method_context()).ToChecked();

  try
  {
    js_method_set_return_boolean(pMsg->isResponseFamily(responseCode));
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIsResponseFamily - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
  
}

JS_METHOD_IMPL(msgIsErrorResponse)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->isErrorResponse());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgIsErrorResponse - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgIsMidDialog)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  try
  {
    js_method_set_return_boolean(pMsg->isMidDialog());
  }
  catch(const OSS::Exception& e)
  {
    std::ostringstream msg;
    msg << "JavaScript->C++ Exception: msgHdrGet - " << e.message();
    OSS::log_error(msg.str());
    js_method_set_return_undefined();
  }
}

JS_METHOD_IMPL(msgGetBody)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_set_return_string(pMsg->getBody().c_str());
}

JS_METHOD_IMPL(msgSetBody)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string body = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  pMsg->setBody(body);

  js_method_set_return_true();
}

JS_METHOD_IMPL(msgGetStartLine)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_set_return_string(pMsg->getStartLine().c_str());
}

JS_METHOD_IMPL(msgSetStartLine)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string sline = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  pMsg->setStartLine(sline);

  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgGetData)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(pMsg->data().c_str());
}

JS_METHOD_IMPL(msgGetSize)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_integer(0);
    return;
  }
  js_method_set_return_integer(pMsg->data().size());
}

JS_METHOD_IMPL(msgCommitData)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }
  pMsg->commitData();
  js_method_set_return_string(pMsg->data().c_str());
}

//
// Request-Line Processing
//

JS_METHOD_IMPL(requestLineGetMethod)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  if (OSS::SIP::SIPRequestLine::getMethod(input, result))
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}


JS_METHOD_IMPL(requestLineGetURI)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  if (OSS::SIP::SIPRequestLine::getURI(input, result))
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(requestLineGetVersion)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  if (OSS::SIP::SIPRequestLine::getVersion(input, result))
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(requestLineSetMethod)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPRequestLine::setMethod(input, newval.c_str()));
}

JS_METHOD_IMPL(requestLineSetURI)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (!OSS::SIP::SIPRequestLine::setURI(input, newval.c_str()))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(input.c_str());
}


JS_METHOD_IMPL(requestLineSetVersion)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPRequestLine::setVersion(input, newval.c_str()));
}


//
// Status-Line Processing
//

JS_METHOD_IMPL(statusLineGetVersion)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  if (OSS::SIP::SIPStatusLine::getVersion(input, result))
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(statusLineSetVersion)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPStatusLine::setVersion(input, newval.c_str()));
}


JS_METHOD_IMPL(statusLineGetStatusCode)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  if (OSS::SIP::SIPStatusLine::getStatusCode(input, result))
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}


JS_METHOD_IMPL(statusLineSetStatusCode)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPStatusLine::setStatusCode(input, newval.c_str()));
}


JS_METHOD_IMPL(statusLineGetReasonPhrase)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  {
    js_method_set_return_string(result.c_str());
    return;
  }
  js_method_set_return_undefined();
}


JS_METHOD_IMPL(statusLineSetReasonPhrase)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPStatusLine::setReasonPhrase(input, newval.c_str()));
}

//
// URI Processing
//

JS_METHOD_IMPL(uriSetScheme)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPURI::setScheme(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetScheme)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getScheme(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getUser(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriSetUserInfo)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPURI::setUserInfo(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetPassword)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getPassword(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getHostPort(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriSetHostPort)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPURI::setHostPort(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetParams)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getParams(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriSetParams)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPURI::setParams(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriHasParam)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  js_method_set_return_boolean(OSS::SIP::SIPURI::hasParam(input, val.c_str()));
}

JS_METHOD_IMPL(uriGetParam)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string value;
  if (OSS::SIP::SIPURI::getParam(input, name.c_str(), value))
  {
    js_method_set_return_string(value.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriGetParamEx)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string value;
  if (OSS::SIP::SIPURI::getParamEx(input, name.c_str(), value))
  {
    js_method_set_return_string(value.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriSetParam)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  js_method_set_return_boolean(OSS::SIP::SIPURI::setParam(input, name.c_str(), newval.c_str()));
}

JS_METHOD_IMPL(uriSetParamEx)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  js_method_set_return_boolean(OSS::SIP::SIPURI::setParam(input, name.c_str(), newval.c_str()));
}

JS_METHOD_IMPL(uriEscapeUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  OSS::SIP::SIPURI::escapeUser(result, input.c_str());
  js_method_set_return_string(result.c_str());
}

JS_METHOD_IMPL(uriEscapeParam)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string result;
  OSS::SIP::SIPURI::escapeParam(result, input.c_str());
  js_method_set_return_string(result.c_str());
}

JS_METHOD_IMPL(uriGetHeaders)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPURI::getHeaders(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriSetHeaders)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPURI::setHeaders(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(uriVerify)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  js_method_set_return_boolean(OSS::SIP::SIPURI::verify(input.c_str()));
}

//
// From Processing
//

JS_METHOD_IMPL(fromGetDisplayName)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPFrom::getDisplayName(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromSetDisplayName)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPFrom::setDisplayName(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromGetURI)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPFrom::getURI(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromSetURI)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPFrom::setURI(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromGetHeaderParams)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string val;
  if (OSS::SIP::SIPFrom::getHeaderParams(input, val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromSetHeaderParams)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (OSS::SIP::SIPFrom::setHeaderParams(input, newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromGetHeaderParam)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string val;
  if (OSS::SIP::SIPFrom::getHeaderParam(input, name.c_str(), val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromGetHeaderParamEx)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string val;
  if (OSS::SIP::SIPFrom::getHeaderParamEx(input, name.c_str(), val))
  {
    js_method_set_return_string(val.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromSetHeaderParam)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  if (OSS::SIP::SIPFrom::setHeaderParam(input, name.c_str(), newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(fromSetHeaderParamEx)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string input = OSS::JS::string_from_js_string( js_method_isolate(), _args_[0]);
  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string newval = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);
  if (OSS::SIP::SIPFrom::setHeaderParamEx(input, name.c_str(), newval.c_str()))
  {
    js_method_set_return_string(input.c_str());
    return;
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgGetRequestUri)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    js_method_set_return_string(requestURI.c_str());
    return;
  }
  js_method_set_return_undefined();
}


JS_METHOD_IMPL(msgSetRequestUri)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string uri = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  try
  {
    js_method_set_return_boolean(SIPRequestLine::setURI(pMsg->startLine(), uri.c_str()));
  }
  catch(...)
  {
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgGetRequestUriUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    std::string user;
    if (SIPURI::getUser(requestURI, user))
    {
      js_method_set_return_string(user.c_str());
      return;
    }
  }
  js_method_set_return_string("");
}

JS_METHOD_IMPL(msgSetRequestUriUser)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string user = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  try
  {
    std::string requestURI;
    if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
    {
      SIPURI::setUserInfo(requestURI, user.c_str());
      if (SIPRequestLine::setURI(pMsg->startLine(), requestURI.c_str()))
      {
        js_method_set_return_true();
        return;
      }
      else
      {
        js_method_set_return_false();
        return;
      }
    }
  }
  catch(...)
  {
    js_method_set_return_false();
    return;
  }

  js_method_set_return_false();
}

JS_METHOD_IMPL(msgGetRequestUriHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    std::string hostPort;
    if (SIPURI::getHostPort(requestURI, hostPort))
    {
      js_method_set_return_string(hostPort.c_str());
      return;
    }
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgSetRequestUriHostPort)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string hostPort = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  try
  {
    std::string requestURI;
    if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
    {
      SIPURI::setHostPort(requestURI, hostPort.c_str());
      if (SIPRequestLine::setURI(pMsg->startLine(), requestURI.c_str()))
      {
        js_method_set_return_true();
        return;
      }
      else
      {
         js_method_set_return_false();
        return;
      }
    }
  }
  catch(...)
  {
     js_method_set_return_false();
     return;
  }
   js_method_set_return_false();
}

JS_METHOD_IMPL(msgGetRequestUriHost)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    std::string host;
    if (SIPURI::getHost(requestURI, host))
    {
      js_method_set_return_string(host.c_str());
      return;
    }
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgGetRequestUriPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    std::string port;
    if (SIPURI::getPort(requestURI, port))
    {
      js_method_set_return_string(port.c_str());
      return;
    }
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgGetRequestUriParameters)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string requestURI;
  if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
  {
    std::string params;
    if (SIPURI::getParams(requestURI, params))
    {
      js_method_set_return_string(params.c_str());
      return;
    }
  }
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(msgSetRequestUriParameters)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }
  
  std::string params = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  try
  {
    std::string requestURI;
    if (SIPRequestLine::getURI(pMsg->startLine(), requestURI))
    {
      SIPURI::setParams(requestURI, params);
    }
  }
  catch(...)
  {
     js_method_set_return_false();
     return;
  }
  js_method_set_return_true();
}

JS_METHOD_IMPL(msgGetToUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
  std::string user;
  if (!SIPFrom::getUser(to, user))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(user.c_str());
}

JS_METHOD_IMPL(msgSetToUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
    std::string user = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
    if (!SIPFrom::setUser(to, user.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    if (!pMsg->hdrSet(OSS::SIP::HDR_TO, to.c_str()))
      {
      js_method_set_return_false();
      return;
    }
    js_method_set_return_true();
  }
  catch(...)
  {
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgGetToHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
  std::string hostPort;
  if (!SIPFrom::getHostPort(to, hostPort))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(hostPort.c_str());
}

JS_METHOD_IMPL(msgGetToHost)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
  std::string host;
  if (!SIPFrom::getHost(to, host))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(host.c_str());
}

JS_METHOD_IMPL(msgGetToPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
  std::string host;
  if (!SIPFrom::getHostPort(to, host))
  {
    js_method_set_return_undefined();
    return;
  }
  
  std::vector<std::string> tokens = OSS::string_tokenize(host, ":");
  if (tokens.size() <= 1)
  {
    js_method_set_return_undefined();
    return;
  }
  
  js_method_set_return_string(tokens[1].c_str());
}

JS_METHOD_IMPL(msgSetToHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    std::string to = pMsg->hdrGet(OSS::SIP::HDR_TO);
    std::string hostPort = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
    if (!SIPFrom::setHostPort(to, hostPort.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    if (!pMsg->hdrSet(OSS::SIP::HDR_TO, to.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    js_method_set_return_true();
  }
  catch(...)
  {
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgGetFromUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_FROM);
  std::string user;
  if (!SIPFrom::getUser(to, user))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(user.c_str());
}

JS_METHOD_IMPL(msgSetFromUser)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    std::string from = pMsg->hdrGet(OSS::SIP::HDR_FROM);
    std::string user = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
    if (!SIPFrom::setUser(from, user.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    if (!pMsg->hdrSet(OSS::SIP::HDR_FROM, from.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    js_method_set_return_true();
  }
  catch(...)
  {
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgGetFromHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string to = pMsg->hdrGet(OSS::SIP::HDR_FROM);
  std::string hostPort;
  if (!SIPFrom::getHostPort(to, hostPort))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(hostPort.c_str());
}

JS_METHOD_IMPL(msgGetFromHost)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string from = pMsg->hdrGet(OSS::SIP::HDR_FROM);
  std::string host;
  if (!SIPFrom::getHost(from, host))
  {
    js_method_set_return_undefined();
    return;
  }
  js_method_set_return_string(host.c_str());
}

JS_METHOD_IMPL(msgGetFromPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string from = pMsg->hdrGet(OSS::SIP::HDR_FROM);
  std::string host;
  if (!SIPFrom::getHostPort(from, host))
  {
    js_method_set_return_undefined();
    return;
  }
  
  std::vector<std::string> tokens = OSS::string_tokenize(host, ":");
  if (tokens.size() <= 1)
  {
    js_method_set_return_undefined();
    return;
  }
  
  js_method_set_return_string(tokens[1].c_str());
}

JS_METHOD_IMPL(msgSetFromHostPort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  try
  {
    std::string from = pMsg->hdrGet(OSS::SIP::HDR_FROM);
    std::string hostPort = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
    if (!SIPFrom::setHostPort(from, hostPort.c_str()))
    {
      js_method_set_return_false();
      return;
    }
    if (!pMsg->hdrSet(OSS::SIP::HDR_FROM, from.c_str()))
    js_method_set_return_true();
  }
  catch(...)
  {
    js_method_set_return_false();
  }
}

JS_METHOD_IMPL(msgGetContactUri)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string hContactList = pMsg->hdrGet(OSS::SIP::HDR_CONTACT);
  if (hContactList.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  ContactURI contact;
  if (!hContactList.empty())
    SIPContact::getAt(hContactList, contact, 0);

  std::string contactUri = contact.getURI();

  js_method_set_return_string(contactUri.c_str());
}

JS_METHOD_IMPL(msgGetContactParameter)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string param = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  if (param.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  std::string hContactList = pMsg->hdrGet(OSS::SIP::HDR_CONTACT);
  if (hContactList.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  ContactURI contact;
  if (!hContactList.empty())
    SIPContact::getAt(hContactList, contact, 0);

  std::string value = contact.getHeaderParam(param.c_str());
  if (value.empty())
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_set_return_string(value.c_str());
}

JS_METHOD_IMPL(msgGetAuthenticator)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string realm = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  OSS::string_to_lower(realm);

  int wwwAuthSize = pMsg->hdrGetSize(OSS::SIP::HDR_AUTHORIZATION);
  int proxyAuthSize = pMsg->hdrGetSize(OSS::SIP::HDR_PROXY_AUTHORIZATION);

  std::ostringstream realmMatch;
  realmMatch << "realm=" << "\"" << realm << "\"";

  std::string authenticator = "";
  if (wwwAuthSize > 0)
  {
    for (int i = 0; i < wwwAuthSize; i++)
    {
      std::string hstr = pMsg->hdrGet(OSS::SIP::HDR_AUTHORIZATION, i);
      if (!hstr.empty())
      {
        if (realm == "*")
        {
          authenticator = hstr;
          break;
        }
        else
        {
          OSS::string_to_lower(hstr);
          if (hstr.find(realmMatch.str()) != std::string::npos)
          {
            authenticator = hstr;
            break;
          }
        }
      }
    }
  }

  if (proxyAuthSize > 0 && authenticator.empty())
  {
    for (int i = 0; i < proxyAuthSize; i++)
    {
      std::string hstr = pMsg->hdrGet(OSS::SIP::HDR_PROXY_AUTHORIZATION, i);
      if (!hstr.empty())
      {
        if (realm == "*")
        {
          authenticator = hstr;
          break;
        }
        else
        {
          OSS::string_to_lower(hstr);
          if (hstr.find(realmMatch.str()) != std::string::npos)
          {
            authenticator = hstr;
            break;
          }
        }
      }
    }
  }

  js_method_set_return_string(authenticator.c_str());
}

JS_METHOD_IMPL(msgSetTransactionProperty)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn)
  {
    js_method_set_return_false();
    return;
  }

  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string value = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);

  if (name.empty() || value.empty())
  {
    js_method_set_return_false();
    return;
  }

  pTrn->setProperty(name, value);

  js_method_set_return_true();
}

JS_METHOD_IMPL(msgGetTransactionProperty)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  if (name.empty())
  {
    js_method_set_return_undefined();
    return;
  }
  std::string value;
  if (name == "log-id")
    value = pTrn->getLogId();
  else
    pTrn->getProperty(name, value);

  js_method_set_return_string(value.c_str());
}

JS_METHOD_IMPL(msgSetProperty)
{
  if (_args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);
  std::string value = OSS::JS::string_from_js_string( js_method_isolate(), _args_[2]);

  if (name.empty() || value.empty())
  {
    js_method_set_return_false();
    return;
  }

  pMsg->setProperty(name, value);

  js_method_set_return_true();
}

JS_METHOD_IMPL(msgGetProperty)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string name = OSS::JS::string_from_js_string( js_method_isolate(), _args_[1]);

  if (name.empty())
  {
    js_method_set_return_undefined();
    return;
  }
  std::string value;
  pMsg->getProperty(name, value);

  js_method_set_return_string(value.c_str());
}


JS_METHOD_IMPL(msgGetSourceAddress)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn || !pTrn->serverTransport())
  {
    js_method_set_return_undefined();
    return;
  }

  OSS::Net::IPAddress addr = pTrn->serverTransport()->getRemoteAddress();

  js_method_set_return_string(addr.toString().c_str());
}

JS_METHOD_IMPL(msgGetSourcePort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn || !pTrn->serverTransport())
  {
    js_method_set_return_undefined();
    return;
  }

  OSS::Net::IPAddress addr = pTrn->serverTransport()->getRemoteAddress();

  js_method_set_return_integer(addr.getPort());
}

JS_METHOD_IMPL(msgGetInterfaceAddress)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn || !pTrn->serverTransport())
  {
    js_method_set_return_undefined();
    return;
  }

  OSS::Net::IPAddress addr = pTrn->serverTransport()->getLocalAddress();
  js_method_set_return_string(addr.toString().c_str());
}

JS_METHOD_IMPL(msgGetInterfacePort)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = OSS::JS::unwrap_external_object<OSS::SIP::SIPMessage>(_args_);
  if (!pMsg)
  {
    js_method_set_return_undefined();
    return;
  }

  SIPB2BTransaction* pTrn = static_cast<SIPB2BTransaction*>(pMsg->userData());
  if (!pTrn || !pTrn->serverTransport())
  {
    js_method_set_return_undefined();
    return;
  }

  OSS::Net::IPAddress addr = pTrn->serverTransport()->getLocalAddress();

  js_method_set_return_integer(addr.getPort());
}

JS_EXPORTS_INIT()
{
  js_export_method("msgGetMethod", msgGetMethod);
  js_export_method("msgHdrPresent", msgHdrPresent);
  js_export_method("msgHdrGetSize", msgHdrGetSize);
  js_export_method("msgHdrGet", msgHdrGet);
  js_export_method("msgHdrSet", msgHdrSet);
  js_export_method("msgHdrRemove", msgHdrRemove);
  js_export_method("msgHdrListAppend", msgHdrListAppend);
  js_export_method("msgHdrListPrepend", msgHdrListPrepend);
  js_export_method("msgHdrListPopFront", msgHdrListPopFront);
  js_export_method("msgHdrListRemove", msgHdrListRemove);
  js_export_method("msgIsRequest", msgIsRequest);
  js_export_method("msgIsResponse", msgIsResponse);
  js_export_method("msgIs1xx", msgIs1xx);
  js_export_method("msgIs2xx", msgIs2xx);
  js_export_method("msgIs3xx", msgIs3xx);
  js_export_method("msgIs4xx", msgIs4xx);
  js_export_method("msgIs5xx", msgIs5xx);
  js_export_method("msgIs6xx", msgIs6xx);
  js_export_method("msgIsResponseFamily", msgIsResponseFamily);
  js_export_method("msgIsErrorResponse", msgIsErrorResponse);
  js_export_method("msgIsMidDialog", msgIsMidDialog);
  js_export_method("msgGetBody", msgGetBody);
  js_export_method("msgSetBody", msgSetBody);
  js_export_method("msgGetStartLine", msgGetStartLine);
  js_export_method("msgSetStartLine", msgSetStartLine);
  js_export_method("msgGetData", msgGetData);
  js_export_method("msgGetData", msgGetSize);
  js_export_method("msgCommitData", msgCommitData);
  js_export_method("msgGetRequestUri", msgGetRequestUri);
  js_export_method("msgSetRequestUri", msgSetRequestUri);
  js_export_method("msgGetRequestUriUser", msgGetRequestUriUser);
  js_export_method("msgSetRequestUriUser", msgSetRequestUriUser);
  js_export_method("msgSetRequestUriHostPort", msgSetRequestUriHostPort);
  js_export_method("msgGetRequestUriHostPort", msgGetRequestUriHostPort);
  js_export_method("msgGetRequestUriHost", msgGetRequestUriHost);
  js_export_method("msgGetRequestUriPort", msgGetRequestUriPort);
  js_export_method("msgGetRequestUriParameters", msgGetRequestUriParameters);
  js_export_method("msgSetRequestUriParameters", msgSetRequestUriParameters);
  js_export_method("msgGetToUser", msgGetToUser);
  js_export_method("msgSetToUser", msgSetToUser);
  js_export_method("msgGetToHostPort", msgGetToHostPort);
  js_export_method("msgGetToHost", msgGetToHost);
  js_export_method("msgSetToHostPort", msgSetToHostPort);
  js_export_method("msgGetToPort", msgGetToPort);
  js_export_method("msgGetFromUser", msgGetFromUser);
  js_export_method("msgSetFromUser", msgSetFromUser);
  js_export_method("msgGetFromHostPort", msgGetFromHostPort);
  js_export_method("msgGetFromHost", msgGetFromHost);
  js_export_method("msgGetFromPort", msgGetFromPort);
  js_export_method("msgSetFromHostPort", msgSetFromHostPort);
  js_export_method("msgGetContactUri", msgGetContactUri);
  js_export_method("msgGetContactParameter", msgGetContactParameter);
  js_export_method("msgGetAuthenticator", msgGetAuthenticator);
  js_export_method("msgSetProperty", msgSetProperty);
  js_export_method("msgGetProperty", msgGetProperty);
  js_export_method("msgSetTransactionProperty", msgSetTransactionProperty);
  js_export_method("msgGetTransactionProperty", msgGetTransactionProperty);
  js_export_method("msgGetSourceAddress", msgGetSourceAddress);
  js_export_method("msgGetSourcePort", msgGetSourcePort);
  js_export_method("msgGetInterfaceAddress", msgGetInterfaceAddress);
  js_export_method("msgGetInterfacePort", msgGetInterfacePort);
  //
  // Request-Line
  //
  js_export_method("requestLineGetMethod", requestLineGetMethod);
  js_export_method("requestLineGetURI", requestLineGetURI);
  js_export_method("requestLineGetVersion", requestLineGetVersion);
  js_export_method("requestLineSetMethod", requestLineSetMethod);
  js_export_method("requestLineSetURI", requestLineSetURI);
  js_export_method("requestLineSetVersion", requestLineSetVersion);

  //
  // Status-Line
  //
  js_export_method("statusLineGetVersion", statusLineGetVersion);
  js_export_method("statusLineSetVersion", statusLineSetVersion);
  js_export_method("statusLineGetStatusCode", statusLineGetStatusCode);
  js_export_method("statusLineSetStatusCode", statusLineSetStatusCode);
  js_export_method("statusLineGetReasonPhrase", statusLineGetReasonPhrase);
  js_export_method("statusLineSetReasonPhrase", statusLineSetReasonPhrase);

  //
  // URI
  //
  js_export_method("uriSetScheme", uriSetScheme);
  js_export_method("uriGetScheme", uriGetScheme);
  js_export_method("uriGetUser", uriGetUser);
  js_export_method("uriSetUserInfo", uriSetUserInfo);
  js_export_method("uriGetPassword", uriGetPassword);
  js_export_method("uriGetHostPort", uriGetHostPort);
  js_export_method("uriSetHostPort", uriSetHostPort);
  js_export_method("uriGetParams", uriGetParams);
  js_export_method("uriSetParams", uriSetParams);
  js_export_method("uriHasParam", uriHasParam);
  js_export_method("uriGetParam", uriGetParam);
  js_export_method("uriGetParamEx", uriGetParamEx);
  js_export_method("uriSetParam", uriSetParam);
  js_export_method("uriSetParamEx", uriSetParamEx);
  js_export_method("uriEscapeUser", uriEscapeUser);
  js_export_method("uriEscapeParam", uriEscapeParam);
  js_export_method("uriGetHeaders", uriGetHeaders);
  js_export_method("uriSetHeaders", uriSetHeaders);
  js_export_method("uriVerify", uriVerify);
  
  //
  // From Processing
  //
  js_export_method("fromGetDisplayName", fromGetDisplayName);
  js_export_method("fromSetDisplayName", fromSetDisplayName);
  js_export_method("fromGetURI", fromGetURI);
  js_export_method("fromSetURI", fromSetURI);
  js_export_method("fromGetHeaderParams", fromGetHeaderParams);
  js_export_method("fromSetHeaderParams", fromSetHeaderParams);
  js_export_method("fromGetHeaderParam", fromGetHeaderParam);
  js_export_method("fromGetHeaderParamEx", fromGetHeaderParamEx);
  js_export_method("fromSetHeaderParam", fromSetHeaderParam);
  js_export_method("fromSetHeaderParamEx", fromSetHeaderParamEx);

  //
  // To Processing
  //
  js_export_method("toGetDisplayName", fromGetDisplayName);
  js_export_method("toSetDisplayName", fromSetDisplayName);
  js_export_method("toGetURI", fromGetURI);
  js_export_method("toSetURI", fromSetURI);
  js_export_method("toGetHeaderParams", fromGetHeaderParams);
  js_export_method("toSetHeaderParams", fromSetHeaderParams);
  js_export_method("toGetHeaderParam", fromGetHeaderParam);
  js_export_method("toGetHeaderParamEx", fromGetHeaderParamEx);
  js_export_method("toSetHeaderParam", fromSetHeaderParam);
  js_export_method("toSetHeaderParamEx", fromSetHeaderParamEx);

  js_export_finalize(); 
}

JS_REGISTER_MODULE(JSSIPParser);



