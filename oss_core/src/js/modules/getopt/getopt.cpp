#include "OSS/JS/JSPlugin.h"
#include "OSS/UTL/CoreUtils.h"
#include "OSS/UTL/Logger.h"
#include <unistd.h>
#include <getopt.h>

static int gArgc = 0;
static char** gArgv = 0;

JS_METHOD_IMPL(__getopt)
{
  js_method_declare_string(optstring, 0);
  js_method_set_return_handle(js_method_uint32(::getopt(gArgc, gArgv, optstring.c_str())));
}

JS_ACCESSOR_GETTER_IMPL(__optind_get) 
{
  js_method_set_return_integer(optind);
}

JS_ACCESSOR_SETTER_IMPL(__optind_set)
{
  optind = value->Int32Value(js_method_context()).ToChecked();
}

JS_ACCESSOR_GETTER_IMPL(__opterr_get) 
{
  js_method_set_return_integer(opterr);
}

JS_ACCESSOR_SETTER_IMPL(__opterr_set)
{
  opterr = value->Int32Value(js_method_context()).ToChecked();
}

JS_ACCESSOR_GETTER_IMPL(__optarg_get) 
{
  js_method_set_return_string(optarg);
}

JS_ACCESSOR_GETTER_IMPL(__optopt_get)
{
  js_method_set_return_integer(optopt);
}

JS_EXPORTS_INIT()
{  
  OSS::OSS_argv(&gArgc, &gArgv);
  optind = 2;

  js_export_uint32( "argc", gArgc);

  JSLocalArrayHandle argv = js_method_array(gArgc);
  for (int i = 0; i < gArgc; i++)
  {
    argv->Set( js_method_context(), i, js_method_string(gArgv[i])  );
  }

  js_export_value( "argv", argv);
  js_export_method("getopt", __getopt );
  js_export_accessor("opterr", __opterr_get, __opterr_set);
  js_export_accessor("optind", __optind_get, __optind_set);
  js_export_accessor("optarg", __optarg_get, NULL);
  js_export_accessor("optopt", __optopt_get, NULL);

  js_export_finalize();
}

JS_REGISTER_MODULE(GetOpt);