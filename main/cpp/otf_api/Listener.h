/* -*- mode: c++; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef _LISTENER_H_
#define _LISTENER_H_

#include "otf_api/OnNext.h"
#include "otf_api/OnError.h"
#include "otf_api/OnCompleted.h"
#include "otf_api/Field.h"
#include "otf_api/Ir.h"

/*
 * The SBE On-The-Fly Decoder
 */
namespace sbe {
namespace on_the_fly {

/*
 * class MessageHeaderDemux : OnNext
 * {
 *    // may or may not be overriden. is called when listener.subscribe() is called
 *    int OnSubscribe(Listener listener)
 *    {
 *        listener_ = listener;
 *    }
 *
 *    int OnNext(const Field &field)
 *    {
 *        if (field.composite.findByName("templateId") != NULL)
 *             templateId_ = field.composite.findByName("templateId").value();
 *    }
 *
 *    int OnCompleted(void)
 *    {
 *        messageOffset = listener_.offset();
 *        // set IR for listener to next callback object based on grabbing IR from message or from something else
 *        listener_.intermediateRepresentation(messageIr, messageIrLen);
 *        // shouldn't need to reset buffer in listener as cursor should already be at the right location
 *    }
 * }
 *
 * MessageHeaderDemux dmux;
 * MessageHandler handler;
 *
 * Listener listener = new Listener();
 * listener.intermediateRepresentation(messageHeaderIr, irlen);
 * ...
 * // per buffer of data (containing header and 1 message)
 * listener.resetForDecode(buffer, len)
 *         .demuxMessageHeaderByField("templateId", messageHeaderIr, IrCallbackObject) // adds a standard demuxing of header and uses cb to get IR
 *         .subscribe(handler);
 *
 * The "trick" is to encompass header and message parse into listener. The header IR is given by the user. The message IR is determined via
 * the templateId and a callback to the user. 
 *     int messageIrCallback(IntermediateRepresentation &ir, const int templateIdValue)
 * listener.OnError called if templateId field is not known/found in IR
 *         .OnError called if buffer is too small (i.e. no templateId value)
 *         .OnError called if IR not found for templateId (i.e. unknown ID value)
 */

/**
 * Usage:
 * \code{cpp}
 * Listener &listener();
 * Ir &ir = new Ir(irBuffer, irLen);
 * listener.resetForDecode(buffer, len).ir(&ir).subscribe(...);
 * \endcode
 */
class Listener
{
private:
    /*
     * The callbacks
     */
    OnNext *onNext_;
    OnError *onError_;
    OnCompleted *onCompleted_;

    /*
     * Iterator around the serialized IR
     */
    const Ir *ir_;

    /*
     * Cached and reused Field and Group objects
     */
    Field cachedField_;
    //Group cachedGroup_;

protected:
    /*
     * These deliver methods are protected and normally not used by applications, but useful for testing purposes
     */
    int deliver(const Field &field)
    {
        return ((onNext_) ? onNext_->onNext(field) : 0);
    };

    // int deliver(const Group &group)
    // {
    //     return ((onNext_) ? onNext_(group) : 0);
    // };

    int error(const Error &error)
    {
        return ((onError_) ? onError_->onError(error) : 0);
    };

    /*
     * Called once callbacks are setup and processing of the buffer should begin. This could be overriden by 
     * a subclass for testing purposes.
     */
    virtual int process(void)
    {
        return 0;
    };

public:

    /// Basic constructor
    Listener() : onNext_(NULL), onError_(NULL), onCompleted_(NULL), ir_(NULL) {};

    /**
     * Set the IR to use for all buffers
     *
     * \param data location of the buffer in memory containing the serialized IR
     * \param length of the buffer
     * \return listener object
     */
    Listener &ir(const Ir *ir)
    {
        ir_ = ir;
        return *this;
    };

    /**
     * Reset state and initialize for decode of the given buffer. The IR is kept constant.
     *
     * \param data location of the buffer in memory
     * \param length of the buffer
     * \return listener object
     */
    Listener &resetForDecode(const char *data, const int length);

    /**
     * 
     */
    Listener &dispatchMessageByHeaderField(const char *fieldName,
                                           const Ir *headerIr,
                                           const Ir::Callback *irCallback);

    /**
     * Informs listener object that groups should contain aggregates of fields instead of marking start
     * and end of the field. i.e. the OnNext(Group) callback object is called with Groups containing Fields.
     */
    Listener &completeGroups();

    /**
     * 
     */
    Listener &subscribe(OnNext *onNext, 
                        OnError *onError = NULL,
                        OnCompleted *onCompleted = NULL)
    {
        onNext_ = onNext;
        onError_ = onError;
        onCompleted_ = onCompleted;
        // now start everything off
        process();
        return *this;
    };

}; // class Listener

} // namespace on_the_fly
} // namespace sbe

#endif /* _LISTENER_H_ */
