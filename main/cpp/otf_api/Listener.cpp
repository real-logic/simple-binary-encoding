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
const int Field::COMPOSITE_INDEX;

Listener &Listener::resetForDecode(const char *data, const int length)
{
    ir_->begin();
    buffer_ = data;
    bufferLen_ = length;
    bufferOffset_ = 0;
    templateId_ = Ir::INVALID_ID;
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
            break;

        case Ir::END_MESSAGE:
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
                processBeginEnum(ir->name(), *((char *)(buffer_ + bufferOffset_ + ir->offset())));
                break;

           case Ir::INT8:
                processBeginEnum(ir->name(), *((uint8_t *)(buffer_ + bufferOffset_ + ir->offset())));
                break;

            case Ir::UINT8:
                processBeginEnum(ir->name(), *((uint8_t *)(buffer_ + bufferOffset_ + ir->offset())));
                break;

            default:
                break;
            }
            bufferOffset_ += ir->size();
            break;

        case Ir::VALID_VALUE:
            processEnumValidValue(ir->name(), /* TODO: need to pass up value as int or other large enough type */ 0);
            break;

        case Ir::END_ENUM:
            processEndEnum();
            break;

            // TODO: groups will "rewind" IR and keep track of the count

            // TODO: set will check each choice in processChoice() and save name to vector if it works

        case Ir::ENCODING:

            // TODO: fix for offset values in IR

            switch (ir->primitiveType())
            {
            case Ir::CHAR:
                processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((char *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT8:
                processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int8_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT16:
                processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int16_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT32:
                processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int32_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::INT64:
                processEncoding(ir->name(), ir->primitiveType(), (int64_t)*((int64_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT8:
                processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint8_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT16:
                processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint16_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT32:
                processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint32_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::UINT64:
                processEncoding(ir->name(), ir->primitiveType(), (uint64_t)*((uint64_t *)(buffer_ + bufferOffset_)));
                break;

            case Ir::FLOAT:
                processEncoding(ir->name(), ir->primitiveType(), (double)*((float *)(buffer_ + bufferOffset_)));
                break;

            case Ir::DOUBLE:
                processEncoding(ir->name(), ir->primitiveType(), (double)*((double *)(buffer_ + bufferOffset_)));
                break;

            default:
                break;
            }
            bufferOffset_ += ir->size();
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

void Listener::processBeginComposite(const std::string &name)
{
    cachedField_.name(Field::COMPOSITE_INDEX, name)
        .type(Field::COMPOSITE);
}

void Listener::processEndComposite(void)
{
    if (cachedField_.name(Field::FIELD_INDEX) == "")
    {
        onNext_->onNext(cachedField_);
        cachedField_.reset();
    }
}

void Listener::processBeginField(const std::string &name, const uint16_t schemaId)
{
    cachedField_.name(Field::FIELD_INDEX, name)
        .schemaId(schemaId)
        .type(Field::ENCODING);
}

void Listener::processEndField(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

void Listener::processBeginEnum(const std::string &name, const char value)
{
    cachedField_.type(Field::ENUM);
}

void Listener::processBeginEnum(const std::string &name, const uint8_t value)
{
    cachedField_.type(Field::ENUM);
}

void Listener::processEnumValidValue(const std::string &name, const int value)
{
    // TODO: if value works, then add value to Field
}

void Listener::processEndEnum(void)
{

}

void Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const int64_t value)
{
    cachedField_.addEncoding(name, type, value);
}

void Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const uint64_t value)
{
    cachedField_.addEncoding(name, type, value);

    if (irCallback_ != NULL && headerEncodingName_ == name)
    {
        templateId_ = value;
    }
}

void Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const double value)
{
    cachedField_.addEncoding(name, type, value);
}
