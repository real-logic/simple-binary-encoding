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
#include "otf_api/Group.h"
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
     * The current callbacks
     */
    OnNext *onNext_;
    OnError *onError_;
    OnCompleted *onCompleted_;

    /*
     * Iterator around the serialized IR
     */
    Ir *ir_;

    /*
     * The data buffer that we want to decode and asscoiated state of it
     */
    const char *buffer_;
    int bufferLen_;
    int bufferOffset_;

    /*
     * Cached and reused Field and Group objects
     */
    Field cachedField_;
    Group cachedGroup_;

    /*
     *
     */
    std::string headerEncodingName_;
    Ir::Callback *irCallback_;
    uint64_t templateId_;

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
    //     return ((onNext_) ? onNext_->onNext(group) : 0);
    // };

    int error(const Error &error)
    {
        return ((onError_) ? onError_->onError(error) : 0);
    };

    /*
     * Called once callbacks are setup and processing of the buffer should begin. This could be overriden by 
     * a subclass for testing purposes.
     */
    virtual int process(void);

    // consolidated IR and data events
    virtual void processBeginComposite(const std::string &name);
    virtual void processEndComposite(void);
    virtual void processBeginField(const std::string &name, const uint16_t schemaId);
    virtual void processEndField(void);
    virtual void processBeginEnum(const std::string &name, const Ir::TokenPrimitiveType type, const char value);
    virtual void processBeginEnum(const std::string &name, const Ir::TokenPrimitiveType type, const uint8_t value);
    virtual void processEnumValidValue(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value);
    virtual void processEndEnum(void);
    virtual void processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const int64_t value);
    virtual void processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value);
    virtual void processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const double value);

public:

    /// Basic constructor
    Listener() : onNext_(NULL), onError_(NULL), onCompleted_(NULL),
                 ir_(NULL), buffer_(NULL), bufferLen_(0), bufferOffset_(0),
                 irCallback_(NULL)
    {
    };

    /**
     * Set the IR to use for all buffers
     *
     * \param data location of the buffer in memory containing the serialized IR
     * \param length of the buffer
     * \return listener object
     */
    Listener &ir(Ir &ir)
    {
        ir_ = &ir;
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
     * Instruct Listener to expect a messageHeader followed by message. The Message template will
     * be in the messageHeader encoding given by encodingName. The IR for the messageHeader is given. The
     * callback to be called when the template ID encoding is encountered. This callback must
     * return the IR to use for the message.
     */
    Listener &dispatchMessageByHeader(const std::string &encodingName,
                                      Ir &headerIr,
                                      Ir::Callback *irCallback)
    {
        ir_ = &headerIr;
        headerEncodingName_ = encodingName;
        irCallback_ = irCallback;
        return *this;
    };

    /**
     * Informs listener object that groups should contain aggregates of fields instead of marking start
     * and end of the field. i.e. the OnNext(Group) callback object is called with Groups containing Fields.
     */
    Listener &completeGroups();

    // TODO: add OnNext::onNext(Event) for MESSAGE and GROUP begin/end event markers instead of OnNext(Group)
    // TODO: make Event base class of Field/Group? and change OnNext to always be Event? or Item? or 
    // TODO: OnNext MESSAGE marker should include templateId

    /**
     * 
     */
    int subscribe(OnNext *onNext, 
                  OnError *onError = NULL,
                  OnCompleted *onCompleted = NULL);

}; // class Listener

} // namespace on_the_fly
} // namespace sbe

#endif /* _LISTENER_H_ */
