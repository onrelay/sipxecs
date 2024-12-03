// OSS Software Solutions Application Programmer Interface
//
// Author: Joegen E. Baclor - mailto:joegen@ossapp.com
//
// Package: SBC
//
// Copyright (c) OSS Software Solutions
//
// Permission is hereby granted, to any person or organization
// obtaining a copy of the software and accompanying documentation covered by
// this license (the "Software") to use, execute, and to prepare
// derivative works of the Software, all subject to the
// "OSS Software Solutions OSS API General License Agreement".
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
// SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
// FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

#include "OSS/UTL/CoreUtils.h"
#include "OSS/UTL/Logger.h"
#include "OSS/Net/IPAddress.h"
#include "OSS/SIP/SIPMessage.h"
#include "OSS/SIP/B2BUA/SIPB2BTransaction.h"
#include "OSS/SIP/SIPRequestLine.h"
#include "OSS/SIP/SIPStatusLine.h"
#include "OSS/SIP/SIPFrom.h"
#include "OSS/SIP/SIPVia.h"
#include "OSS/SIP/SIPURI.h"
#include "OSS/SIP/SIPContact.h"
#include "v8.h"
#include "OSS/SIP/SBC/SBCManager.h"
#include "OSS/SIP/SIPTransportSession.h"
#include "OSS/SIP/SBC/SBCRegisterBehavior.h"
#include "OSS/SIP/SBC/SBCJSModuleManager.h"
#include "OSS/SIP/SBC/SBCDirectories.h"

#include "OSS/JS/JSModule.h"

namespace OSS {
namespace SIP {
namespace SBC {



static SBCManager* _pSBCManager = 0;

OSS::SIP::SIPMessage* unwrapRequest(JSCallbackInfo _args_)
{
  if (_args_.Length() < 1)
    return 0;
  JSValueHandle obj = _args_[0];
  if (!obj->IsObject())
    return 0;
  JSExternalHandle field = JSExternalHandle::Cast(obj->ToObject(js_method_context()).ToLocalChecked()->GetInternalField(0));
  void* ptr = field->Value();
  return static_cast<OSS::SIP::SIPMessage*>(ptr);
}

std::string jsvalToString(JSCallbackInfo _args_, int index)
{
  if (!_args_[index]->IsString())
    return "";
  v8::String::Utf8Value value(js_method_isolate(),_args_[index]);
  return *value;
}

bool jsvalToBoolean(JSCallbackInfo _args_, int index)
{
  if (!_args_[index]->IsBoolean())
    return false;
  return _args_[index]->IsTrue();
}

int jsvalToInt(JSCallbackInfo _args_, int index)
{
  if (!_args_[index]->IsNumber())
    return 0;
  return _args_[index]->Int32Value(js_method_context()).ToChecked();
}

JS_METHOD_IMPL(msgRouteByAOR)
{
  if (!_pSBCManager)
  {
    js_method_set_return_false();
    return;
  }

  bool userComparisonOnly = true;
  if (_args_.Length() == 2)
  {
    userComparisonOnly = _args_[1]->IsTrue();
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = unwrapRequest(_args_);
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

  if (!_pSBCManager)
  {
    js_method_set_return_false();
    return;
  }
  OSS::Net::IPAddress localInterface;
  OSS::Net::IPAddress target;

  js_method_set_return_boolean(_pSBCManager->onRouteByAOR(pMsg, pTrn, userComparisonOnly, localInterface, target));
}

JS_METHOD_IMPL(msgRouteByRURI)
{
  if (!_pSBCManager)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = unwrapRequest(_args_);
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

  if (!_pSBCManager)
  {
    js_method_set_return_false();
    return;
  }

  js_method_set_return_boolean(_pSBCManager->onRouteByRURI(pMsg, pTrn));
}


JS_METHOD_IMPL(msgResetMaxForwards)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }

  js_method_enter_scope();
  OSS::SIP::SIPMessage* pMsg = unwrapRequest(_args_);
  if (!pMsg)
  {
    js_method_set_return_false();
    return;
  }

  std::string maxForwards = jsvalToString(_args_,1);
  if (maxForwards.empty())
  {
    js_method_set_return_false();
    return;
  }

  if (!_pSBCManager)
  {
    js_method_set_return_false();
    return;
  }
  //
  // First check if we are not spiraling by checking the via address
  //
  std::size_t count = pMsg->hdrGetSize("via");
  for (std::size_t i = 0; i < count; i++)
  {
    const std::string& via = pMsg->hdrGet("via", i);
    std::vector<std::string> elements;
    SIPVia::splitElements(via, elements);
    for (std::vector<std::string>::const_iterator iter = elements.begin(); iter != elements.end(); iter++)
    {
      SIPVia hVia(*iter);
      std::string sentBy = hVia.getSentBy();
      std::string transport = hVia.getTransport();
      OSS::string_to_lower(transport);
      OSS::Net::IPAddress address(OSS::Net::IPAddress::fromV4IPPort(sentBy.c_str()));

      if (_pSBCManager->isLocalTransport(transport, address))
      {
        js_method_set_return_false();
    return;
      }
    }
  }

  pMsg->hdrSet("Max-Forwards", maxForwards);

  js_method_set_return_true();
}

JS_METHOD_IMPL(sbc_jsexec_async)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string bin = jsvalToString(_args_,0);
  std::string commandArgs = jsvalToString(_args_,1);

#if OSS_OS_FAMILY_WINDOWS
  return jsvoid();
#else
  std::ostringstream cmd;
  cmd << bin << " " << commandArgs;
  js_method_set_return_integer(system(cmd.str().c_str()));
#endif
}

JS_METHOD_IMPL(sbc_jsexec)
{
  if (_args_.Length() < 2)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string bin = jsvalToString(_args_,0);
  std::string commandArgs = jsvalToString(_args_,1);
  std::ostringstream cmd;
  cmd << bin << " " << commandArgs;
  std::string command = cmd.str();
#ifndef OSS_OS_FAMILY_WINDOWS
  FILE *fd = popen( command.c_str(), "r" );
  std::string result;
  result.reserve(1024);
  if (!fd)
  {
    js_method_set_return_undefined();
    return;
  }
  while (true)
  {
    int c = fgetc(fd);
    if (c != EOF)
      result.push_back((char)c);
    else
      pclose(fd);
  }

  js_method_set_return_string(result.c_str());
#else
  js_method_set_return_undefined();
#endif

}


JS_METHOD_IMPL(sbc_white_list_address)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  std::string entry = jsvalToString(_args_,0);

  boost::system::error_code ec;
  boost::asio::ip::address ip = boost::asio::ip::address::from_string(entry, ec);
  if (!ec)
  {
    SIPTransportSession::rateLimit().whiteListAddress(ip);
    js_method_set_return_true();
    return;
  }

  js_method_set_return_false();
}

JS_METHOD_IMPL(sbc_white_list_network)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }

  std::string network = jsvalToString(_args_,0);
  if (!network.empty())
  {
    SIPTransportSession::rateLimit().whiteListNetwork(network);
  }
  
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(sbc_deny_all_incoming)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }

  bool deny = jsvalToBoolean(_args_,0);
  SIPTransportSession::rateLimit().denyAll(deny);
  js_method_set_return_true();
}

JS_METHOD_IMPL(sbc_set_transport_threshold)
{
  if (!_pSBCManager || _args_.Length() < 3)
  {
    js_method_set_return_false();
    return;
  }
  
  int packetsPerSecondThreshold = jsvalToInt(_args_,0);
  int thresholdViolationRate = jsvalToInt(_args_,1);
  int banLifeTime = jsvalToInt(_args_,2);
  
  _pSBCManager->transactionManager().stack().setTransportThreshold(packetsPerSecondThreshold, thresholdViolationRate, banLifeTime);
  
  js_method_set_return_true();
}

JS_METHOD_IMPL(sbc_add_channel_limit)
{
  if (!_pSBCManager || _args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }
  
  std::string prefix = jsvalToString(_args_,0);
  unsigned int limit = jsvalToInt(_args_,1);
  
  _pSBCManager->cdr().channelLimits().registerDialPrefix(prefix, limit);
  
  js_method_set_return_true();
}

JS_METHOD_IMPL(sbc_add_domain_channel_limit)
{
  if (!_pSBCManager || _args_.Length() < 2)
  {
    js_method_set_return_false();
    return;
  }
  
  std::string domain = jsvalToString(_args_,0);
  unsigned int limit = jsvalToInt(_args_,1);
  
  _pSBCManager->cdr().domainLimits().registerDomain(domain, limit);
  
  js_method_set_return_true();
}

JS_METHOD_IMPL(sbc_get_channel_count)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_integer(0);
    return;
  }
  
  std::string prefix = jsvalToString(_args_,0);
  js_method_set_return_integer(_pSBCManager->cdr().channelLimits().getCallCount(prefix));
}

JS_METHOD_IMPL(sbc_get_domain_channel_count)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_integer(0);
    return;
  }
  
  std::string domain = jsvalToString(_args_,0);
  js_method_set_return_integer(_pSBCManager->cdr().domainLimits().getCallCount(domain));
}

JS_METHOD_IMPL(sbc_set_log_level)
{
  if (_args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }
  
  std::string sLogLevel = jsvalToString(_args_,0);;
  if (sLogLevel.empty())
  {
    js_method_set_return_false();
    return;
  }

  if (sLogLevel == "fatal")//  A fatal error. The application will most likely terminate. This is the highest priority.
  {
    OSS::log_reset_level(OSS::PRIO_FATAL);
  }
  else if (sLogLevel == "critical")//  A critical error. The application might not be able to continue running successfully.
  {
    OSS::log_reset_level(OSS::PRIO_CRITICAL);
  }
  else if (sLogLevel == "error")//  An error. An operation did not complete successfully, but the application as a whole is not affected.
  {
    OSS::log_reset_level(OSS::PRIO_ERROR);
  }
  else if (sLogLevel == "warning")//  A warning. An operation completed with an unexpected result.
  {
    OSS::log_reset_level(OSS::PRIO_WARNING);
  }
  else if (sLogLevel == "notice")//  A notice, which is an information with just a higher priority.
  {
    OSS::log_reset_level(OSS::PRIO_NOTICE);
  }
  else if (sLogLevel == "information")//  An informational message, usually denoting the successful completion of an operation.
  {
    OSS::log_reset_level(OSS::PRIO_INFORMATION);
  }
  else if (sLogLevel == "debug")//  A debugging message.
  {
    OSS::log_reset_level(OSS::PRIO_DEBUG);
  }
  else if (sLogLevel == "trace")//  A tracing message. This is the lowest priority.
  {
    OSS::log_reset_level(OSS::PRIO_TRACE);
  }
  else
  {
    js_method_set_return_false();
    return;
  }

  js_method_set_return_true();
}


JS_METHOD_IMPL(sbc_ban_user_id)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string val = jsvalToString(_args_,0);
  if (!val.empty())
  {
    _pSBCManager->autoBanRules().banUserById(val);
  }
  
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(sbc_ban_user_display)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string val = jsvalToString(_args_,0);
  if (!val.empty())
  {
    _pSBCManager->autoBanRules().banUserByDisplayName(val);
  }
  
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(sbc_ban_user_agent)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_undefined();
    return;
  }
  std::string val = jsvalToString(_args_,0);
  if (!val.empty())
  {
    _pSBCManager->autoBanRules().banUserAgent(val);
  }
  
  js_method_set_return_undefined();
}

JS_METHOD_IMPL(sbc_run)
{
  js_method_set_return_boolean(_pSBCManager->run());
}

JS_METHOD_IMPL(sbc_start_options_keep_alive)
{
  if (!_pSBCManager || _args_.Length() < 1)
  {
    js_method_set_return_false();
    return;
  }
  bool start = jsvalToBoolean(_args_,0);
  _pSBCManager->registerHandler()->pauseKeepAlive(!start);
  js_method_set_return_true();
}

} } } // OSS::SIP::SBC

JS_EXPORTS_INIT()
{
  using namespace OSS::SIP::SBC;
  
  _pSBCManager = OSS::SIP::SBC::SBCManager::instance();
  
  js_export_method("msgRouteByAOR", msgRouteByAOR);
  js_export_method("msgRouteByRURI", msgRouteByRURI);
  js_export_method("msgResetMaxForwards", msgResetMaxForwards);
  
  js_export_method("sbc_run", sbc_run);
  js_export_method("sbc_jsexec", sbc_jsexec);
  js_export_method("sbc_jsexec_async", sbc_jsexec_async);
  js_export_method("sbc_white_list_address", sbc_white_list_address);
  js_export_method("sbc_white_list_network", sbc_white_list_network);
  js_export_method("sbc_deny_all_incoming", sbc_deny_all_incoming);
  js_export_method("sbc_set_transport_threshold", sbc_set_transport_threshold);
  js_export_method("sbc_add_channel_limit", sbc_add_channel_limit);
  js_export_method("sbc_get_channel_count", sbc_get_channel_count);
  js_export_method("sbc_add_domain_channel_limit", sbc_add_domain_channel_limit);
  js_export_method("sbc_get_domain_channel_count", sbc_get_domain_channel_count);
  js_export_method("sbc_set_log_level", sbc_set_log_level);
  js_export_method("sbc_start_options_keep_alive", sbc_start_options_keep_alive);
  
  //
  // Banning known attackers
  //
  js_export_method("sbc_ban_user_id", sbc_ban_user_id);
  js_export_method("sbc_ban_user_display", sbc_ban_user_display);
  js_export_method("sbc_ban_user_agent", sbc_ban_user_agent);
  
  js_export_finalize(); 
}

JS_REGISTER_MODULE(JSSBCHook);



