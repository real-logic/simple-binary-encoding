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

#define __STDC_LIMIT_MACROS 1
#include <stdint.h>
#include <string.h>

#include <stack>

#include "otf_api/OnNext.h"
#include "otf_api/OnError.h"
#include "otf_api/OnCompleted.h"
#include "otf_api/Field.h"
#include "otf_api/Group.h"
#include "otf_api/Ir.h"

/**
 * \mainpage Simple Binary Encoding
 *
 * \section intro Introduction
 *
 * Simple Binary Encoding (SBE) is an implementation of the FIX/SBE specification.
 * SBE is an OSI layer 6 representation for encoding and decoding application messages
 * in binary format for low-latency applications.
 *
 * Further details on the background and usage of SBE can be found on the Wiki at
 * <a href="https://github.com/real-logic/simple-binary-encoding/wiki"/>
 * https://github.com/real-logic/simple-binary-encoding/wiki</a>.
 *
 * The On-The-Fly (OTF) Decoder for SBE is concerned with being able to take a
 * piece of data and a schema and being able to decode it, well, on-the-fly. The
 * decoder uses a reactive, Rx, style API to accomplish this. Applications subclass
 * callback interfaces, such as OnNext, OnError, and OnComplete to be notified of
 * decoding actions. Listener is a decoding engine object. It is configured with
 * the data to decode as well as the schema. The schema is represented in a "compiled"
 * form called Intermediate Representation, or IR.
 *
 * \example SbeOtfDecoder.cpp
 */
namespace sbe {
namespace on_the_fly {

/** \brief Encapsulation of a decoding engine
 *
 * Instances of this class may be reused for different buffers as well as different Ir. The methods use
 * a fluent style for composition.
 *
 * The basic usage pattern is:
 * - instantiate a Listener
 * - set the Ir to use for the decoding that describes the data format
 * - pass in a pointer to the start of the data along with the length of the data in bytes
 * - subscribe callbacks for Field, Group, Error, and OnCompleted. When called, Listener::subscribe
 * initiates the decoding of the message. Thus all callbacks come from the calling thread.
 *
 * This is demonstrated in the example below.
 *
 \code{.cpp}
 Listener listener();                   // instantiate a decoder
 Ir ir(irBuffer, irLen);                // create an Ir object for the format
 listener.resetForDecode(buffer, len)   // get ready to decode data located at buffer for len bytes
         .ir(ir)                        // pass in the Ir to use
         .subscribe(...);               // subscribe callbacks and initiate decoding
 \endcode
 *
 * A more advanced usage pattern is when a header is used to dispatch to a set of different formats for
 * the data. This is accomplished using the Listener::dispatchMessageByHeader method. An example is below.
 \code{.cpp}
 Listener listener();
 Ir headerIr(headerIrBuffer, headerIrLen);                   // the Ir for the header
 listener.resetForDecode(buffer, len)
         .dispatchByMessageHeader(std::string("templateId"), // the header field that is used for dispatch
                                  headerIr,                  // the Ir of the header
                                  irCallback)                // the callback called for dispatch choices
         .subscribe(...);
 \endcode
 *
 * \sa Listener, OnNext, OnError, OnCompleted, Field, Group, Ir, IrCollection
 *
 */
class Listener
{
public:
    /// Construct a Listener
    Listener();

    /**
     * \brief Set the IR to use for decoding
     *
     * The IR is encapsulated in an Ir object. Alternatively, an IrCollection may be used
     * to hold IR for several messages and for a common header for dispatching.
     * Mutually exclusive with Listener::dispatchMessageByHeader.
     *
     * \sa Ir, IrCollection
     * \param ir to use for decoding
     * \return Listener
     */
    Listener &ir(Ir &ir)
    {
        ir_ = &ir;
        return *this;
    }

    virtual ~Listener() {}

    /**
     * \brief Reset state and initialize for decode of the given buffer.
     *
     * The IR is kept constant if previously set.
     *
     * \param data location of the buffer in memory
     * \param length of the buffer
     * \return Listener
     */
    Listener &resetForDecode(const char *data, const int length);

    /**
     * \brief Instruct listener to expect a header to dispatch various formats to.
     *
     * Mutually exclusive with Listener::ir.
     *
     * Listener will expect a header followed by a message. The message template ID will
     * be in the header encoding given by encodingName. The
     * callback is be called when the template ID encoding is encountered. This callback must
     * return the Ir to use for the message.
     *
     * \param headerIr to use for the header
     * \param irCallback called when the encodingName element is encountered passing the template ID value
     * \return Listener
     */
    Listener &dispatchMessageByHeader(Ir &headerIr,
                                      Ir::Callback *irCallback)
    {
        ir_ = &headerIr;
        irCallback_ = irCallback;
        return *this;
    }

    /**
     * \brief Subscribe callbacks for decoding and initiate decode
     *
     * \param onNext instance to call when Field and Group encountered during decode
     * \param onError instance to call when an error in decoding is encountered or NULL for not to be called
     * \param onCompleted instance to call when decode complete or NULL for not to be called
     * \return Listener
     * \sa OnNext, OnError, OnCompleted
     */
    int subscribe(OnNext *onNext, 
                  OnError *onError = NULL,
                  OnCompleted *onCompleted = NULL);

    /**
     * \brief Return offset within decode buffer that decoding is currently at
     *
     * \return position from start of buffer that decoding as progressed so far
     */
    int bufferOffset(void) const
    {
        return bufferOffset_;
    }

protected:
    /*
     * These deliver methods are protected and normally not used by applications, but useful for testing purposes
     */
    int deliver(const Field &field)
    {
        return ((onNext_) ? onNext_->onNext(field) : 0);
    }

    int deliver(const Group &group)
    {
        return ((onNext_) ? onNext_->onNext(group) : 0);
    }

    int error(const Error &error)
    {
        return ((onError_) ? onError_->onError(error) : 0);
    }

    /*
     * Called once callbacks are setup and processing of the buffer should begin. This could be overridden by
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
    virtual ::uint64_t processBeginEnum(const Ir *ir, const char value);
    virtual ::uint64_t processBeginEnum(const Ir *ir, const ::uint8_t value);
    virtual void processEnumValidValue(const Ir *ir);
    virtual void processEndEnum(void);
    virtual ::uint64_t processBeginSet(const Ir *ir, const ::uint64_t value);
    virtual void processSetChoice(const Ir *ir);
    virtual void processEndSet(void);
    virtual void processBeginVarData(const Ir *ir);
    virtual void processEndVarData(void);
    virtual ::uint64_t processEncoding(const Ir *ir, const ::int64_t value);
    virtual ::uint64_t processEncoding(const Ir *ir, const ::uint64_t value);
    virtual ::uint64_t processEncoding(const Ir *ir, const double value);
    virtual ::uint64_t processEncoding(const Ir *ir, const char *value, const int size);
    virtual void processBeginGroup(const Ir *ir);
    virtual void processEndGroup(void);

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
     * The data buffer that we want to decode and associated state of it
     */
    const char *buffer_;
    int bufferLen_;
    int bufferOffset_;
    int relativeOffsetAnchor_;
    int messageBlockLength_;

    /*
     * Cached and reused Field and Group objects
     */
    Field cachedField_;
    Group cachedGroup_;

    /*
     * State associated with message dispatching from header
     */
    Ir::Callback *irCallback_;
    ::int64_t templateId_;
    ::int64_t templateVersion_;

    /*
     * Stack frame to hold the repeating group state
     */
    struct Frame
    {
        enum State
        {
            BEGAN_GROUP, DIMENSIONS, BODY_OF_GROUP, MESSAGE, SKIP_TO_END_GROUP
        };

        std::string scopeName_;
        int blockLength_;
        int numInGroup_;
        int iteration_;
        int irPosition_;
        State state_;
        ::int32_t schemaId_;

        Frame(const std::string &name = "") : scopeName_(name), blockLength_(-1),
                                              numInGroup_(-1), iteration_(-1), irPosition_(-1), state_(MESSAGE), schemaId_(Ir::INVALID_ID) {};
    };

    Frame messageFrame_;
    std::stack<Frame, std::vector<Frame> > stack_;

    void updateBufferOffsetFromIr(const Ir *ir);
}; // class Listener

} // namespace on_the_fly
} // namespace sbe

#endif /* _LISTENER_H_ */
