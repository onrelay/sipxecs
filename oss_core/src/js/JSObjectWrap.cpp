// Copyright Joyent, Inc. and other Node contributors.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.


#include "OSS/JS/JSObjectWrap.h"
#include "OSS/JS/JSIsolate.h"


namespace OSS {
namespace JS {


JSObjectWrap::JSObjectWrap ( ) 
{
  refs_ = 0;
  JSIsolate::Ptr ptr = JSIsolate::getIsolate();
  _pIsolatePtr = new JSIsolate::Ptr();
  *((JSIsolate::Ptr*)_pIsolatePtr) = ptr;
  _pIsolate = ptr.get();
}

JSObjectWrap::~JSObjectWrap ( ) 
{
  delete (JSIsolate::Ptr*)_pIsolatePtr;
  
  if (!handle_.IsEmpty()) 
  {
    //assert(handle_.IsNearDeath()); // Removed in V8 7.5 API
    handle_.ClearWeak();
    v8::Local<v8::Object> obj = v8::Local<v8::Object>::New(_pIsolate->getV8Isolate(),handle_);
    obj->SetInternalField(0, v8::Undefined(_pIsolate->getV8Isolate()));
    //handle_.Dispose(); Removed V8 3.24 API, use Reset
    //handle_.Clear(); Removed V8 3.24 API, use Reset
    handle_.Reset();
  }
}

void JSObjectWrap::Wrap (v8::Handle<v8::Object> handle) 
{
  assert(handle_.IsEmpty());
  assert(handle->InternalFieldCount() > 0);
  handle->SetAlignedPointerInInternalField(0, this);
  handle_ = v8::Persistent<v8::Object, v8::CopyablePersistentTraits<v8::Object>>(_pIsolate->getV8Isolate(),handle);
  MakeWeak();
}


void JSObjectWrap::MakeWeak (void) 
{
  handle_.SetWeak(this, WeakCallback, v8::WeakCallbackType::kParameter);
  //handle_.MarkIndependent(); // Removed V8 7.6 API
}

/* Ref() marks the object as being attached to an event loop.
 * Refed objects will not be garbage collected, even if
 * all references are lost.
 */
void JSObjectWrap::Ref() 
{
  assert(!handle_.IsEmpty());
  refs_++;
  handle_.ClearWeak();
}

/* Unref() marks an object as detached from the event loop.  This is its
 * default state.  When an object with a "weak" reference changes from
 * attached to detached state it will be freed. Be careful not to access
 * the object after making this call as it might be gone!
 * (A "weak reference" means an object that only has a
 * persistant handle.)
 *
 * DO NOT CALL THIS FROM DESTRUCTOR
 */
void JSObjectWrap::Unref() 
{
  assert(!handle_.IsEmpty());
  assert(!handle_.IsWeak());
  assert(refs_ > 0);
  if (--refs_ == 0) 
  { 
    MakeWeak(); 
  }
}


void JSObjectWrap::WeakCallback(const v8::WeakCallbackInfo<JSObjectWrap>& data) 
{
  JSObjectWrap* objectWrap = data.GetParameter();
  //assert(value == objectWrap->handle_);
  assert(!objectWrap->refs_);
  //assert(value.IsNearDeath()); // Removed in V8 7.5 API
  delete objectWrap;
}


} } // OSS::JS

