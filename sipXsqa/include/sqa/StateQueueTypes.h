

#ifndef STATEQUEUETYPES_H_INCLUDED
#define STATEQUEUETYPES_H_INCLUDED


#if !defined(BOOST_BIND_GLOBAL_PLACEHOLDERS)
  #define BOOST_BIND_GLOBAL_PLACEHOLDERS
#endif
#include <boost/algorithm/string.hpp>
#include <boost/asio.hpp>
#include <boost/any.hpp>
#include <boost/bind.hpp>
#include <boost/enable_shared_from_this.hpp>
#include <boost/function.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/asio.hpp>
#include <boost/noncopyable.hpp>

#include <zmq.hpp>


#define SQA_LINGER_TIME_MILLIS 5000
#define SQA_TERMINATE_STRING "__TERMINATE__"
#define SQA_CONN_MAX_READ_BUFF_SIZE 65536
#define SQA_CONN_CONNECTION_TIMEOUT_MSEC 5000
#define SQA_CONN_READ_TIMEOUT 1000
#define SQA_CONN_WRITE_TIMEOUT 1000
#define SQA_KEY_MIN 22172
#define SQA_KEY_ALPHA 22180
#define SQA_KEY_DEFAULT SQA_KEY_MIN
#define SQA_KEY_MAX 22200
#define SQA_KEEP_ALIVE_TICKS 30

// Defines the interval, in seconds, to wait between keep alive loop calls
#define SQA_KEEP_ALIVE_LOOP_INTERVAL_SECS 1

#endif
