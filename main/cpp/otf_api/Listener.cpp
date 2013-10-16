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
    return *this;
}

// protected
int Listener::process(void)
{
    // consolidate the IR and the data buffer and invoke methods that represent events for a semantic
    // layer to coalesce for higher up
    for (; !ir_->end(); ir_->next())
    {
        if ((bufferOffset_ + ir_->offset()) > bufferLen_)
        {
            if (onError_ != NULL)
            {
                onError_->onError(Error("buffer too short"));
            }
            break;
        }

        // overloaded method for encoding callback. 1 per primitiveType. Don't need type passed as method has typed value
        switch (ir_->signal())
        {
        case Ir::BEGIN_MESSAGE:
            break;

        case Ir::END_MESSAGE:
            break;

        case Ir::BEGIN_COMPOSITE:
            processBeginComposite(ir_->name());
            break;

        case Ir::END_COMPOSITE:
            processEndComposite();
            break;

        case Ir::BEGIN_FIELD:
            processBeginField(ir_->name(), ir_->schemaId());
            break;

        case Ir::END_FIELD:
            processEndField();
            break;

        case Ir::BEGIN_ENUM:
            switch (ir_->primitiveType())
            {
            case Ir::CHAR:
                processBeginEnum(ir_->name(), *((char *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

           case Ir::INT8:
                processBeginEnum(ir_->name(), *((uint8_t *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            case Ir::UINT8:
                processBeginEnum(ir_->name(), *((uint8_t *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            default:
                break;
            }
            break;

        case Ir::VALID_VALUE:
            processEnumValidValue(ir_->name(), /* TODO: need to pass up value as int or other large enough type */ 0);
            break;

        case Ir::END_ENUM:
            processEndEnum();
            break;

            // TODO: groups will "rewind" IR and keep track of the count

        case Ir::ENCODING:
            switch (ir_->primitiveType())
            {
            case Ir::CHAR:
                processEncoding(ir_->name(), ir_->primitiveType(), (int64_t)*((char *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            case Ir::INT8:
                processEncoding(ir_->name(), ir_->primitiveType(), (int64_t)*((int8_t *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            case Ir::UINT8:
                processEncoding(ir_->name(), ir_->primitiveType(), (uint64_t)*((uint8_t *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            case Ir::UINT16:
                processEncoding(ir_->name(), ir_->primitiveType(), (uint64_t)*((uint16_t *)(buffer_ + bufferOffset_ + ir_->offset())));
                break;

            default:
                break;
            }
            break;

        default:
            break;
        }
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
        .schemaId(schemaId);
}

void Listener::processEndField(void)
{
    onNext_->onNext(cachedField_);
    cachedField_.reset();
}

void Listener::processBeginEnum(const std::string &name, const char value)
{

}

void Listener::processBeginEnum(const std::string &name, const uint8_t value)
{

}

void Listener::processEnumValidValue(const std::string &name, const int value)
{

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
}

void Listener::processEncoding(const std::string &name, const Ir::TokenPrimitiveType type, const double value)
{
    cachedField_.addEncoding(name, type, value);
}
