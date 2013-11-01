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

#include <stack>

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
    int relativeOffsetAnchor_;

    /*
     * Cached and reused Field and Group objects
     */
    Field cachedField_;
    Group cachedGroup_;

    /*
     * State associated with message dispatching from header
     */
    std::string headerEncodingName_;
    Ir::Callback *irCallback_;
    uint64_t templateId_;

    /*
     * Stack frame to hold the repeating group state
     */
    struct Frame
    {
        enum State
        {
            BEGAN_GROUP, DIMENSIONS, BODY_OF_GROUP, MESSAGE
        };

        std::string scopeName_;
        int blockLength_;
        int numInGroup_;
        int iteration_;
        int irPosition_;
        State state_;

        Frame(const std::string &name = "") : scopeName_(name), blockLength_(-1), numInGroup_(-1), iteration_(-1),
                                              irPosition_(-1), state_(MESSAGE) {};
    };

    Frame messageFrame_;
    std::stack<Frame, std::vector<Frame> > stack_;

protected:
    /*
     * These deliver methods are protected and normally not used by applications, but useful for testing purposes
     */
    int deliver(const Field &field)
    {
        return ((onNext_) ? onNext_->onNext(field) : 0);
    };

    int deliver(const Group &group)
    {
        return ((onNext_) ? onNext_->onNext(group) : 0);
    };

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
    virtual void processBeginMessage(const Ir *ir);
    virtual void processEndMessage(void);
    virtual void processBeginComposite(const Ir *ir);
    virtual void processEndComposite(void);
    virtual void processBeginField(const Ir *ir);
    virtual void processEndField(void);
    virtual uint64_t processBeginEnum(const Ir *ir, const char value);
    virtual uint64_t processBeginEnum(const Ir *ir, const uint8_t value);
    virtual void processEnumValidValue(const Ir *ir);
    virtual void processEndEnum(void);
    virtual uint64_t processBeginSet(const Ir *ir, const uint64_t value);
    virtual void processSetChoice(const Ir *ir);
    virtual void processEndSet(void);
    virtual void processBeginVarData(const Ir *ir);
    virtual void processEndVarData(void);
    virtual uint64_t processEncoding(const Ir *ir, const int64_t value);
    virtual uint64_t processEncoding(const Ir *ir, const uint64_t value);
    virtual uint64_t processEncoding(const Ir *ir, const double value);
    virtual uint64_t processEncoding(const Ir *ir, const char *value, const int size);
    virtual void processBeginGroup(const Ir *ir);
    virtual void processEndGroup(void);

public:

    /// Basic constructor
    Listener();

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
