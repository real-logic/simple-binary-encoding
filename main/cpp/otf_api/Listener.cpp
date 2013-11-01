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

#include <stdio.h>
#include <iostream>
#include <sys/types.h>

#include "otf_api/Listener.h"

/*
 * builtins for GCC. MSVC has similar ones.
 */

#if __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__
    #define BSWAP16(b,v) ((b == Ir::SBE_LITTLE_ENDIAN) ? (v) : __builtin_bswap16((uint16_t)v))
    #define BSWAP32(b,v) ((b == Ir::SBE_LITTLE_ENDIAN) ? (v) : __builtin_bswap32((uint32_t)v))
    #define BSWAP64(b,v) ((b == Ir::SBE_LITTLE_ENDIAN) ? (v) : __builtin_bswap64((uint64_t)v))
#elif __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #define BSWAP16(b,v) ((b == Ir::SBE_BIG_ENDIAN) ? (v) : __builtin_bswap16((uint16_t)v))
    #define BSWAP32(b,v) ((b == Ir::SBE_BIG_ENDIAN) ? (v) : __builtin_bswap32((uint32_t)v))
    #define BSWAP64(b,v) ((b == Ir::SBE_BIG_ENDIAN) ? (v) : __builtin_bswap64((uint64_t)v))
#else
    #error "Byte Ordering of platform not determined. Set __BYTE_ORDER__ manually before including this file."
#endif /* byte ordering */

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

const uint16_t Field::INVALID_ID;
const int Field::FIELD_INDEX;

Listener::Listener() : onNext_(NULL), onError_(NULL), onCompleted_(NULL),
                       ir_(NULL), buffer_(NULL), bufferLen_(0), bufferOffset_(0),
                       relativeOffsetAnchor_(0), irCallback_(NULL), messageFrame_()
{
}

Listener &Listener::resetForDecode(const char *data, const int length)
{
    ir_->begin();
    buffer_ = data;
    bufferLen_ = length;
    bufferOffset_ = 0;
    templateId_ = Ir::INVALID_ID;
    while (!stack_.empty())
    {
        stack_.pop();
    }
    return *this;
}

int Listener::subscribe(OnNext *onNext, 
                        OnError *onError,
                        OnCompleted *onCompleted)
{
    int result = 0;

    if (irCallback_ == NULL)
    {
        onNext_ = onNext;
        onError_ = onError;
        onCompleted_ = onCompleted;
        result = process();
    }
    else
    {
        onNext_ = onNext;
        onError_ = onError;
        onCompleted_ = onCompleted;
        result = process();
        if (result != -1)
        {
            // cout << "offset " << bufferOffset_ << "/" << bufferLen_ << endl;
            if (templateId_ != Ir::INVALID_ID)
            {
                ir_ = irCallback_->irForTemplateId(templateId_);
                irCallback_ = NULL;
                if (ir_ != NULL)
                {
                    ir_->begin();
                    result = process();
                }
                else if (onError_ != NULL)
                {
                    onError_->onError(Error("no IR found for message"));
                    result = -1;
                }
            }
            else if (onError_ != NULL)
            {
                onError_->onError(Error("template ID encoding name not found"));
                result = -1;
            }
        }
    }
    return result;
}

// protected
int Listener::process(void)
{
    Ir *ir = ir_;

    stack_.push(messageFrame_);

    // consolidate the IR and the data buffer and invoke methods that represent events for a semantic
    // layer to coalesce for higher up
    for (; !ir->end(); ir->next())
    {
        //cout << "IR @ " << ir->position() << " " << ir->signal() << endl;
        //cout << "offsets " << bufferOffset_ << "/" << bufferLen_ << endl;
        if (bufferOffset_ > bufferLen_)
        {
            if (onError_ != NULL)
            {
                onError_->onError(Error("buffer too short"));
            }
            return -1;
        }

        // overloaded method for encoding callback. 1 per primitiveType. Don't need type passed as method has typed value
        switch (ir->signal())
        {
        case Ir::BEGIN_MESSAGE:
            processBeginMessage(ir);
            break;

        case Ir::END_MESSAGE:
            processEndMessage();
            break;

        case Ir::BEGIN_COMPOSITE:
            processBeginComposite(ir);
            break;

        case Ir::END_COMPOSITE:
            processEndComposite();
            break;

        case Ir::BEGIN_FIELD:
            processBeginField(ir);
            break;

        case Ir::END_FIELD:
            processEndField();
            break;

        case Ir::BEGIN_ENUM:
            {
                const char *valuePosition = buffer_ + bufferOffset_;

                switch (ir->primitiveType())
                {
                case Ir::CHAR:
                    bufferOffset_ += processBeginEnum(ir, *((char *)valuePosition));
                    break;

                case Ir::UINT8:
                    bufferOffset_ += processBeginEnum(ir, *((uint8_t *)valuePosition));
                    break;

                default:
                    break;
                }
                break;

            }
        case Ir::VALID_VALUE:
            processEnumValidValue(ir);
            break;

        case Ir::END_ENUM:
            processEndEnum();
            break;

        case Ir::BEGIN_SET:
            {
                const char *valuePosition = buffer_ + bufferOffset_;

                switch (ir->primitiveType())
                {
                case Ir::UINT8:
                    bufferOffset_ += processBeginSet(ir, *((uint8_t *)valuePosition));
                    break;

                case Ir::UINT16:
                    bufferOffset_ += processBeginSet(ir, (uint64_t)BSWAP16(ir->byteOrder(), *((uint16_t *)valuePosition)));
                    break;

                case Ir::UINT32:
                    bufferOffset_ += processBeginSet(ir, (uint64_t)BSWAP32(ir->byteOrder(), *((uint32_t *)valuePosition)));
                    break;

                case Ir::UINT64:
                    bufferOffset_ += processBeginSet(ir, (uint64_t)BSWAP64(ir->byteOrder(), *((uint64_t *)valuePosition)));
                    break;

                default:
                    break;
                }
                break;
            }
        case Ir::CHOICE:
            processSetChoice(ir);
            break;

        case Ir::END_SET:
            processEndSet();
            break;

        case Ir::BEGIN_VAR_DATA:
            processBeginVarData(ir);
            break;

        case Ir::END_VAR_DATA:
            processEndVarData();
            break;

        case Ir::BEGIN_GROUP:
            // TODO: before saving anchor, update based on desired offset of group (this is only encountered 1st time)
            processBeginGroup(ir);
            break;

        case Ir::END_GROUP:
            processEndGroup();
            break;

        case Ir::ENCODING:
            {
                // TODO: fix for offset values in IR
                // TODO: bump buffOffset_ for offset value in IR. Offset in IR is relative to saved relativeOffsetAnchor_
                //  Message, Group, Composite are relativeOffsetAnchor_ points that must be saved when encountered.
                //  Encoding, Enum, Set, Group must honor offset and adjust
                //  Group moves bufferOffset_ to start of group
                //  Group then also updates relativeOffsetAnchor_ value

                const char *valuePosition = buffer_ + bufferOffset_;
                const char *constVal = ir->constVal();
                int *calculatedOffset = &bufferOffset_;
                int constOffset;

                // use ir->constVal() for value if this token is a constant
                if (constVal != NULL)
                {
                    valuePosition = constVal;
                    calculatedOffset = &constOffset;  // use a dummy variable for offset as constant comes from IR
                }

                // if this is an array or variable size field (0xFFFFFFFF size), then handle it
                if (ir->size() != Ir::size(ir->primitiveType()))
                {
                    *calculatedOffset += processEncoding(ir, valuePosition, ir->size());
                    break;
                }

                // fall through to single items
                switch (ir->primitiveType())
                {
                case Ir::CHAR:
                    *calculatedOffset += processEncoding(ir, (int64_t)*((char *)(valuePosition)));
                    break;

                case Ir::INT8:
                    *calculatedOffset += processEncoding(ir, (int64_t)*((int8_t *)(valuePosition)));
                    break;

                case Ir::INT16:
                    *calculatedOffset += processEncoding(ir, (int64_t)((int16_t)BSWAP16(ir->byteOrder(), *((int16_t *)(valuePosition)))));
                    break;

                case Ir::INT32:
                    *calculatedOffset += processEncoding(ir, (int64_t)((int32_t)BSWAP32(ir->byteOrder(), *((int32_t *)(valuePosition)))));
                    break;

                case Ir::INT64:
                    *calculatedOffset += processEncoding(ir, (int64_t)((int64_t)BSWAP64(ir->byteOrder(), *((int64_t *)(valuePosition)))));
                    break;

                case Ir::UINT8:
                    *calculatedOffset += processEncoding(ir, (uint64_t)*((uint8_t *)(valuePosition)));
                    break;

                case Ir::UINT16:
                    *calculatedOffset += processEncoding(ir, (uint64_t)BSWAP16(ir->byteOrder(), *((uint16_t *)(valuePosition))));
                    break;

                case Ir::UINT32:
                    *calculatedOffset += processEncoding(ir, (uint64_t)BSWAP32(ir->byteOrder(), *((uint32_t *)(valuePosition))));
                    break;

                case Ir::UINT64:
                    *calculatedOffset += processEncoding(ir, (uint64_t)BSWAP64(ir->byteOrder(), *((uint64_t *)(valuePosition))));
                    break;

                case Ir::FLOAT:
                    *calculatedOffset += processEncoding(ir, (double)BSWAP32(ir->byteOrder(), *((float *)(valuePosition))));
                    break;

                case Ir::DOUBLE:
                    *calculatedOffset += processEncoding(ir, (double)BSWAP64(ir->byteOrder(), *((double *)(valuePosition))));
                    break;

                default:
                    break;
                }
                break;
            }
        default:
            break;
        }
    }

    if (onCompleted_ != NULL && irCallback_ == NULL)
    {
        onCompleted_->onCompleted();
    }
    return 0;
}

void Listener::processBeginMessage(const Ir *ir)
{
    stack_.top().scopeName_ = ir->name();
    relativeOffsetAnchor_ = bufferOffset_;
}

void Listener::processEndMessage(void)
{
    stack_.top().scopeName_ = "";
}

void Listener::processBeginComposite(const Ir *ir)
{
    cachedField_.compositeName(ir->name());

    if (cachedField_.type() != Field::VAR_DATA)
    {
        cachedField_.type(Field::COMPOSITE);
    }

    if (stack_.top().state_ == Frame::BEGAN_GROUP)
    {
        stack_.top().state_ = Frame::DIMENSIONS;
    }

    relativeOffsetAnchor_ = bufferOffset_;
}

void Listener::processEndComposite(void)
{
    if (cachedField_.fieldName() == "" && stack_.top().state_ == Frame::MESSAGE)
    {
        onNext_->onNext(cachedField_);
        cachedField_.reset();
    }

    if (stack_.top().state_ == Frame::DIMENSIONS)
    {
        cachedField_.reset(); // probably saved some state in the encodings, so reset it out for the fields to follow

        stack_.top().state_ = Frame::BODY_OF_GROUP;

        stack_.top().irPosition_ = ir_->position();
        //cout << "save IR position " << stack_.top().irPosition_ << endl;

        cachedGroup_.name(stack_.top().scopeName_)
            .iteration(0)
            .numInGroup(stack_.top().numInGroup_)
            .event(Group::START);
        onNext_->onNext(cachedGroup_);
        cachedGroup_.reset();
    }
}

void Listener::processBeginField(const Ir *ir)
{
    cachedField_.fieldName(ir->name())
        .schemaId(ir->schemaId())
        .type(Field::ENCODING);
}

void Listener::processEndField(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

uint64_t Listener::processBeginEnum(const Ir *ir, const char value)
{
    cachedField_.type(Field::ENUM)
        .addEncoding(ir->name(), ir->primitiveType(), (uint64_t)value);
    return Ir::size(ir->primitiveType());
}

uint64_t Listener::processBeginEnum(const Ir *ir, uint8_t value)
{
    cachedField_.type(Field::ENUM)
        .addEncoding(ir->name(), ir->primitiveType(), (uint64_t)value);
    return Ir::size(ir->primitiveType());
}

void Listener::processEnumValidValue(const Ir *ir)
{
    // TODO: can only have 1 valid value, so, could abandon the next one that comes in
    if (cachedField_.getUInt() == ir->validValue())
    {
        cachedField_.addValidValue(ir->name());
    }
}

void Listener::processEndEnum(void)
{
    // not much to do
}

uint64_t Listener::processBeginSet(const Ir *ir, const uint64_t value)
{
    cachedField_.type(Field::SET)
        .addEncoding(ir->name(), ir->primitiveType(), value);
    return Ir::size(ir->primitiveType());
}

void Listener::processSetChoice(const Ir *ir)
{
    if (cachedField_.getUInt() & ((uint64_t)0x1 << ir->choiceValue()))
    {
        cachedField_.addChoice(ir->name());
    }
}

void Listener::processEndSet(void)
{
    // not much to do
}

void Listener::processBeginVarData(const Ir *ir)
{
    cachedField_.fieldName(ir->name())
        .schemaId(ir->schemaId())
        .type(Field::VAR_DATA);
}

void Listener::processEndVarData(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

uint64_t Listener::processEncoding(const Ir *ir, const int64_t value)
{
    cachedField_.addEncoding(ir->name(), ir->primitiveType(), value);
    return Ir::size(ir->primitiveType());
}

uint64_t Listener::processEncoding(const Ir *ir, const uint64_t value)
{
    cachedField_.addEncoding(ir->name(), ir->primitiveType(), value);

    if (irCallback_ != NULL && headerEncodingName_ == ir->name())
    {
        templateId_ = value;
    }
    else if (cachedField_.type() == Field::VAR_DATA && ir->name() == "length")
    {
        cachedField_.varDataLength(value);
    }
    else if (stack_.top().state_ == Frame::DIMENSIONS)
    {
        if (ir->name() == "blockLength")
        {
            stack_.top().blockLength_ = value;
         }
        else if (ir->name() == "numInGroup")
        {
            stack_.top().numInGroup_ = value;
            stack_.top().iteration_ = 0;
         }
    }
    return Ir::size(ir->primitiveType());
}

uint64_t Listener::processEncoding(const Ir *ir, const double value)
{
    cachedField_.addEncoding(ir->name(), ir->primitiveType(), value);
    return Ir::size(ir->primitiveType());
}

uint64_t Listener::processEncoding(const Ir *ir, const char *value, const int size)
{
    // arrays and variable length fields both come through here
    if (cachedField_.type() == Field::VAR_DATA)
    {
        cachedField_.addEncoding(ir->name(), ir->primitiveType(), value, cachedField_.varDataLength());
        return cachedField_.varDataLength();
    }
    else
    {
        cachedField_.addEncoding(ir->name(), ir->primitiveType(), value, size);
        return size;
    }
}

void Listener::processBeginGroup(const Ir *ir)
{
    stack_.push(Frame(ir->name().c_str()));
    stack_.top().state_ = Frame::BEGAN_GROUP;
    relativeOffsetAnchor_ = bufferOffset_;
}

void Listener::processEndGroup(void)
{
    bool popped = false;

    //cout << "END_GROUP " << stack_.top().scopeName_ << endl;
    cachedGroup_.name(stack_.top().scopeName_)
        .iteration(stack_.top().iteration_)
        .numInGroup(stack_.top().numInGroup_)
        .event(Group::END);
    onNext_->onNext(cachedGroup_);
    cachedGroup_.reset();

    if (++stack_.top().iteration_ < stack_.top().numInGroup_)
    {
        // don't pop frame yet
        ir_->position(stack_.top().irPosition_);  // rewind IR to first field in group

        cachedGroup_.name(stack_.top().scopeName_)
            .iteration(stack_.top().iteration_)
            .numInGroup(stack_.top().numInGroup_)
            .event(Group::START);
        onNext_->onNext(cachedGroup_);
        cachedGroup_.reset();
        relativeOffsetAnchor_ = bufferOffset_;
    }
    else
    {
        // pop frame
        stack_.pop();
        popped = true;
    }

    //cout << "IR position " << ir_->position() << endl;
}
