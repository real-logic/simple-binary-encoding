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

#include "otf_api/Ir.h"
#include "uk_co_real_logic_sbe_ir_generated/FrameCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/TokenCodec.hpp"

using namespace sbe::on_the_fly;
using namespace uk_co_real_logic_sbe_ir_generated;
using ::std::cout;
using ::std::endl;

struct Ir::Impl
{
    TokenCodec tokenCodec;
    char name[256];
    char constValue[256];
    char minValue[256];
    char maxValue[256];
    char nullValue[256];
    char characterEncoding[256];
    char epoch[256];
    char timeUnit[256];
    char semanticType[256];
    int nameLength;
    int constValueLength;
    int minValueLength;
    int maxValueLength;
    int nullValueLength;
    int characterEncodingLength;
    int epochLength;
    int timeUnitLength;
    int semanticTypeLength;
    ::uint32_t serializedTokenSize;
};

#if !defined(WIN32) && !defined(_WIN32)
const int Ir::INVALID_ID;
const ::uint32_t Ir::VARIABLE_SIZE;
#endif /* WIN32 */

Ir::Ir(const char *buffer, const int len, const ::int64_t templateId, const ::int64_t schemaId, const ::int64_t schemaVersion) :
    buffer_(buffer), len_(len), templateId_(templateId), id_(schemaId), schemaVersion_(schemaVersion)
{
    impl_ = new Ir::Impl;
    begin();
}

Ir::~Ir()
{
    if (impl_ != NULL)
    {
        delete impl_;
        impl_ = NULL;
    }
//    std::cout << "Ir being deleted" << "\n";
}

void Ir::readTokenAtCurrentPosition()
{
    char tmp[256];

    //printf("read buffer_ %p offset %d\n", buffer_, cursorOffset_);

    impl_->tokenCodec.wrapForDecode((char *)buffer_, cursorOffset_, impl_->tokenCodec.sbeBlockLength(), impl_->tokenCodec.sbeSchemaVersion(), len_);

    // read all the var data and save in Impl then save size

    impl_->nameLength = impl_->tokenCodec.getName(impl_->name, sizeof(impl_->name));

    impl_->constValueLength = impl_->tokenCodec.getConstValue(impl_->constValue, sizeof(impl_->constValue));

    // don't really do anything with min/max/null/encoding right now
    impl_->minValueLength = impl_->tokenCodec.getMinValue(impl_->minValue, sizeof(tmp));
    impl_->maxValueLength = impl_->tokenCodec.getMaxValue(impl_->maxValue, sizeof(tmp));
    impl_->nullValueLength = impl_->tokenCodec.getNullValue(impl_->nullValue, sizeof(tmp));
    impl_->characterEncodingLength = impl_->tokenCodec.getCharacterEncoding(impl_->characterEncoding, sizeof(tmp));
    impl_->epochLength = impl_->tokenCodec.getEpoch(impl_->epoch, sizeof(tmp));
    impl_->timeUnitLength = impl_->tokenCodec.getTimeUnit(impl_->timeUnit, sizeof(tmp));
    impl_->semanticTypeLength = impl_->tokenCodec.getSemanticType(impl_->semanticType, sizeof(tmp));

    impl_->serializedTokenSize = impl_->tokenCodec.size();
}

void Ir::begin()
{
    cursorOffset_ = 0;

    if (buffer_ != NULL)
    {
        readTokenAtCurrentPosition();
    }
}

void Ir::next()
{
    cursorOffset_ += impl_->serializedTokenSize;

    if (!end())
    {
        readTokenAtCurrentPosition();
    }
}

bool Ir::end() const
{
    if (cursorOffset_ < len_)
    {
        return false;
    }
    return true;
}

::int32_t Ir::offset() const
{
    return impl_->tokenCodec.tokenOffset();
}

::int32_t Ir::size() const
{
    return impl_->tokenCodec.tokenSize();
}

Ir::TokenSignal Ir::signal() const
{
    // the serialized IR and the Ir::TokenSignal enums MUST be kept in sync!
    return (Ir::TokenSignal)impl_->tokenCodec.signal();
}

Ir::TokenByteOrder Ir::byteOrder() const
{
    // the serialized IR and the Ir::TokenByteOrder enums MUST be kept in sync!
    return (Ir::TokenByteOrder)impl_->tokenCodec.byteOrder();
}

Ir::TokenPrimitiveType Ir::primitiveType() const
{
    // the serialized IR and the Ir::TokenPrimitiveType enums MUST be kept in sync!
    return (Ir::TokenPrimitiveType)impl_->tokenCodec.primitiveType();
}

Ir::TokenPresence Ir::presence() const
{
    return (Ir::TokenPresence)impl_->tokenCodec.presence();
}

::int32_t Ir::schemaId() const
{
    return impl_->tokenCodec.fieldId();
}

::uint64_t Ir::validValue() const
{
    // constVal holds the validValue. primitiveType holds the type
    switch (primitiveType())
    {
        case Ir::CHAR:
            return impl_->constValue[0];
            break;

        case Ir::UINT8:
            return impl_->constValue[0];
            break;

        default:
            throw std::runtime_error("do not know validValue primitiveType [E109]");
            break;
    }
}

::uint64_t Ir::choiceValue() const
{
    // constVal holds the validValue. primitiveType holds the type
    switch (primitiveType())
    {
        case Ir::UINT8:
            return impl_->constValue[0];
            break;

        case Ir::UINT16:
            return *(::uint16_t *)(impl_->constValue);
            break;

        case Ir::UINT32:
            return *(::uint32_t *)(impl_->constValue);
            break;

        case Ir::UINT64:
            return *(::uint64_t *)(impl_->constValue);
            break;

        default:
            throw std::runtime_error("do not know choice primitiveType [E110]");
            break;
    }
}

::int64_t Ir::nameLen() const
{
    return impl_->nameLength;
}

std::string Ir::name() const
{
    return std::string(impl_->name, nameLen());
}

::int64_t Ir::constLen() const
{
    return impl_->constValueLength;
}

const char *Ir::constValue() const
{
    if (constLen() == 0)
    {
        return NULL;
    }

    return impl_->constValue;
}

::int64_t Ir::minLen() const
{
    return impl_->minValueLength;
}

const char *Ir::minValue() const
{
    if (minLen() == 0)
    {
        return NULL;
    }

    return impl_->minValue;
}

::int64_t Ir::maxLen() const
{
    return impl_->maxValueLength;
}

const char *Ir::maxValue() const
{
    if (maxLen() == 0)
    {
        return NULL;
    }

    return impl_->maxValue;
}

::int64_t Ir::nullLen() const
{
    return impl_->nullValueLength;
}

const char *Ir::nullValue() const
{
    if (nullLen() == 0)
    {
        return NULL;
    }

    return impl_->nullValue;
}

::int64_t Ir::characterEncodingLen() const
{
    return impl_->characterEncodingLength;
}

const char *Ir::characterEncoding() const
{
    if (characterEncodingLen() == 0)
    {
        return NULL;
    }

    return impl_->characterEncoding;
}

::int64_t Ir::epochLen() const
{
    return impl_->epochLength;
}

const char *Ir::epoch() const
{
    if (epochLen() == 0)
    {
        return NULL;
    }

    return impl_->epoch;
}

::int64_t Ir::timeUnitLen() const
{
    return impl_->timeUnitLength;
}

const char *Ir::timeUnit() const
{
    if (timeUnitLen() == 0)
    {
        return NULL;
    }

    return impl_->timeUnit;
}

::int64_t Ir::semanticTypeLen() const
{
    return impl_->semanticTypeLength;
}

const char *Ir::semanticType() const
{
    if (semanticTypeLen() == 0)
    {
        return NULL;
    }

    return impl_->semanticType;
}

int Ir::position() const
{
    return cursorOffset_;
}

void Ir::position(const int pos)
{
    cursorOffset_ = pos;
    readTokenAtCurrentPosition();
}

void Ir::addToken(::uint32_t offset,
                  ::uint32_t size,
                  TokenSignal signal,
                  TokenByteOrder byteOrder,
                  TokenPrimitiveType primitiveType,
                  ::uint16_t fieldId,
                  const std::string &name,
                  const char *constValue,
                  int constValueLength)
{
    TokenCodec tokenCodec;

    if (buffer_ == NULL)
    {
        buffer_ = new char[4098];
    }

    //printf("buffer_ %p offset %d\n", buffer_, cursorOffset_);

    tokenCodec.wrapForEncode((char *)buffer_, cursorOffset_, 4098);

    tokenCodec.tokenOffset(offset)
              .tokenSize(size)
              .fieldId(fieldId)
              .tokenVersion(0)
              .signal((SignalCodec::Value)signal)
              .primitiveType((PrimitiveTypeCodec::Value)primitiveType)
              .byteOrder((ByteOrderCodec::Value)byteOrder)
              .presence((constValue != NULL ? PresenceCodec::SBE_OPTIONAL : PresenceCodec::SBE_REQUIRED));

    tokenCodec.putName(name.c_str(), name.size());
    tokenCodec.putConstValue(constValue, constValueLength);
    tokenCodec.putMinValue(NULL, 0);
    tokenCodec.putMaxValue(NULL, 0);
    tokenCodec.putNullValue(NULL, 0);
    tokenCodec.putCharacterEncoding(NULL, 0);
    tokenCodec.putEpoch(NULL, 0);
    tokenCodec.putTimeUnit(NULL, 0);
    tokenCodec.putSemanticType(NULL, 0);

    cursorOffset_ += tokenCodec.size();
    len_ = cursorOffset_;
}
