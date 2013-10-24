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

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

const uint16_t Field::INVALID_ID;
const int Field::FIELD_INDEX;

Listener::Listener() : onNext_(NULL), onError_(NULL), onCompleted_(NULL),
                       ir_(NULL), buffer_(NULL), bufferLen_(0), bufferOffset_(0),
                       irCallback_(NULL), topFrame_(messageFrame_), messageFrame_()
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
            processBeginMessage(ir->name());
            break;

        case Ir::END_MESSAGE:
            processEndMessage();
            break;

        case Ir::BEGIN_COMPOSITE:
            processBeginComposite(ir->name());
            break;

        case Ir::END_COMPOSITE:
            processEndComposite();
            break;

        case Ir::BEGIN_FIELD:
            processBeginField(ir->name(), ir->schemaId());
            break;

        case Ir::END_FIELD:
            processEndField();
            break;

        case Ir::BEGIN_ENUM:
            switch (ir->primitiveType())
            {
            case Ir::CHAR:
                bufferOffset_ += processBeginEnum(ir->name(), ir->primitiveType(), *((char *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT8:
                bufferOffset_ += processBeginEnum(ir->name(), ir->primitiveType(), *((uint8_t *)(buffer_ + bufferOffset_)));
                break;

            default:
                break;
            }
            break;

        case Ir::VALID_VALUE:
            processEnumValidValue(ir->name(), ir->primitiveType(), ir->validValue());
            break;

        case Ir::END_ENUM:
            processEndEnum();
            break;

        case Ir::BEGIN_SET:
            switch (ir->primitiveType())
            {
            case Ir::UINT8:
                bufferOffset_ += processBeginSet(ir->name(), ir->primitiveType(), *((uint8_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT16:
                bufferOffset_ += processBeginSet(ir->name(), ir->primitiveType(), *((uint16_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT32:
                bufferOffset_ += processBeginSet(ir->name(), ir->primitiveType(), *((uint32_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT64:
                bufferOffset_ += processBeginSet(ir->name(), ir->primitiveType(), *((uint64_t *)(buffer_ + bufferOffset_)));
                break;

            default:
                break;
            }
            break;

        case Ir::CHOICE:
            processSetChoice(ir->name(), ir->primitiveType(), ir->choiceValue());
            break;

        case Ir::END_SET:
            processEndSet();
            break;

        case Ir::BEGIN_VAR_DATA:
            processBeginVarData(ir->name(), ir->schemaId());
            break;

        case Ir::END_VAR_DATA:
            processEndVarData();
            break;

        case Ir::BEGIN_GROUP:
            processBeginGroup(ir->name());
            break;

        case Ir::END_GROUP:
            processEndGroup();
            break;

        case Ir::ENCODING:

            // TODO: fix for offset values in IR
            // bump buffOffset_ for offset value in IR.

            // if this is an array or variable size field (0xFFFFFFFF size), then handle it
            if (ir->size() != Ir::size(ir->primitiveType()))
            {
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), buffer_ + bufferOffset_, ir->size());
                break;
            }

            // fall through to single items
            switch (ir->primitiveType())
            {
            case Ir::CHAR:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((char *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT8:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int8_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT16:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int16_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT32:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int32_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT64:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int64_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT8:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint8_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT16:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint16_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT32:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint32_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT64:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint64_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::FLOAT:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (double)*((float *)(buffer_ + bufferOffset_)));
                break;

            case Ir::DOUBLE:
                bufferOffset_ += processEncoding(ir->name(), ir->primitiveType(), (double)*((double *)(buffer_ + bufferOffset_)));
                break;

            default:
                break;
            }
            // TODO: fix for variable length fields and offsets
            break;

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

void Listener::processBeginMessage(const std::string &name)
{
    topFrame_.scopeName_ = name;
}

void Listener::processEndMessage(void)
{
    topFrame_.scopeName_ = "";
}

void Listener::processBeginComposite(const std::string &name)
{
    cachedField_.compositeName(name);

    if (cachedField_.type() != Field::VAR_DATA)
    {
        cachedField_.type(Field::COMPOSITE);
    }

    if (topFrame_.state_ == Frame::BEGAN_GROUP)
    {
        topFrame_.state_ = Frame::DIMENSIONS;
    }
}

void Listener::processEndComposite(void)
{
    if (cachedField_.fieldName() == "" && topFrame_.state_ == Frame::MESSAGE)
    {
        onNext_->onNext(cachedField_);
        cachedField_.reset();
    }

    if (topFrame_.state_ == Frame::DIMENSIONS)
    {
        topFrame_.state_ = Frame::BODY_OF_GROUP;

        topFrame_.irPosition_ = ir_->position();
        // cout << "save IR position " << topFrame_.irPosition_ << endl;

        cachedGroup_.name(topFrame_.scopeName_)
            .currentIndex(0)
            .numInGroup(topFrame_.numInGroup_)
            .event(Group::START);
        onNext_->onNext(cachedGroup_);
        cachedGroup_.reset();
    }
}

void Listener::processBeginField(const std::string &name, const uint16_t schemaId)
{
    cachedField_.fieldName(name)
        .schemaId(schemaId)
        .type(Field::ENCODING);
}

void Listener::processEndField(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

uint64_t Listener::processBeginEnum(const std::string &name, const Ir::TokenPrimitiveType type, const char value)
{
    cachedField_.type(Field::ENUM)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

uint64_t Listener::processBeginEnum(const std::string &name, const Ir::TokenPrimitiveType type, uint8_t value)
{
    cachedField_.type(Field::ENUM)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

void Listener::processEnumValidValue(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value)
{
    // TODO: can only have 1 valid value, so, could abandon the next one that comes in
    if (cachedField_.getUInt() == value)
    {
        cachedField_.addValidValue(name);
    }
}

void Listener::processEndEnum(void)
{
    // not much to do
}

uint64_t Listener::processBeginSet(const std::string &name, const Ir::TokenPrimitiveType type, const uint8_t value)
{
    cachedField_.type(Field::SET)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

uint64_t Listener::processBeginSet(const std::string &name, const Ir::TokenPrimitiveType type, const uint16_t value)
{
    cachedField_.type(Field::SET)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

uint64_t Listener::processBeginSet(const std::string &name, const Ir::TokenPrimitiveType type, const uint32_t value)
{
    cachedField_.type(Field::SET)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

uint64_t Listener::processBeginSet(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value)
{
    cachedField_.type(Field::SET)
        .addEncoding(name, type, (uint64_t)value);
    return Ir::size(type);
}

void Listener::processSetChoice(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value)
{
    if (cachedField_.getUInt() & ((uint64_t)0x1 << value))
    {
        cachedField_.addChoice(name);
    }
}

void Listener::processEndSet(void)
{
    // not much to do
}

void Listener::processBeginVarData(const std::string &name, const uint16_t schemaId)
{
    cachedField_.fieldName(name)
        .schemaId(schemaId)
        .type(Field::VAR_DATA);
}

void Listener::processEndVarData(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

uint64_t Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const int64_t value)
{
    cachedField_.addEncoding(name, type, value);
    return Ir::size(type);
}

uint64_t Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value)
{
    cachedField_.addEncoding(name, type, value);

    if (irCallback_ != NULL && headerEncodingName_ == name)
    {
        templateId_ = value;
    }
    else if (cachedField_.type() == Field::VAR_DATA && name == "length")
    {
        cachedField_.varDataLength(value);
    }
    else if (topFrame_.state_ == Frame::DIMENSIONS)
    {
        if (name == "blockLength")
        {
            topFrame_.blockLength_ = value;
         }
        else if (name == "numInGroup")
        {
            topFrame_.numInGroup_ = value;
            topFrame_.currentIndex_ = 0;
         }
    }
    return Ir::size(type);
}

uint64_t Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const double value)
{
    cachedField_.addEncoding(name, type, value);
    return Ir::size(type);
}

uint64_t Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const char *value, const int size)
{
    // arrays and variable length fields both come through here
    if (cachedField_.type() == Field::VAR_DATA)
    {
        cachedField_.addEncoding(name, type, value, cachedField_.varDataLength());
        return cachedField_.varDataLength();
    }
    else
    {
        cachedField_.addEncoding(name, type, value, size);
        return size;
    }
}

void Listener::processBeginGroup(const std::string &name)
{
    topFrame_ = Frame(ir_->name().c_str());
    stack_.push(topFrame_);
    topFrame_.state_ = Frame::BEGAN_GROUP;
}

void Listener::processEndGroup(void)
{
    cachedGroup_.name(topFrame_.scopeName_)
        .currentIndex(topFrame_.currentIndex_)
        .numInGroup(topFrame_.numInGroup_)
        .event(Group::END);
    onNext_->onNext(cachedGroup_);
    cachedGroup_.reset();

    if (++topFrame_.currentIndex_ < topFrame_.numInGroup_)
    {
        // don't pop frame yet
        ir_->position(topFrame_.irPosition_);  // rewind IR to first field in group
    }
    else
    {
        // pop frame
        stack_.pop();
        topFrame_ = stack_.top();
    }

    // cout << "IR position " << ir_->position() << endl;
    if (topFrame_.state_ != Frame::MESSAGE)
    {
        cachedGroup_.name(topFrame_.scopeName_)
            .currentIndex(topFrame_.currentIndex_)
            .numInGroup(topFrame_.numInGroup_)
            .event(Group::START);
        onNext_->onNext(cachedGroup_);
        cachedGroup_.reset();
    }
}
