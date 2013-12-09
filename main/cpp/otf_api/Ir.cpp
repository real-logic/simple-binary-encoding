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
#include "uk_co_real_logic_sbe_ir_generated/SerializedToken.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedFrame.hpp"

using namespace sbe::on_the_fly;
using namespace uk_co_real_logic_sbe_ir_generated;
using ::std::cout;
using ::std::endl;

struct Ir::Impl
{
    SerializedToken serializedToken;
    char name[256];
    char constVal[256];
    int nameLength;
    int constValLength;
    uint32_t serializedTokenSize;
};

#if !defined(WIN32)
const int Ir::INVALID_ID;
const uint32_t Ir::VARIABLE_SIZE;
#endif /* WIN32 */

Ir::Ir(const char *buffer, const int len, const int64_t templateId, const int64_t templateVersion) :
    buffer_(buffer), len_(len), templateId_(templateId), templateVersion_(templateVersion)
{
    impl_ = new Ir::Impl;
    begin();
}

void Ir::readTokenAtCurrentPosition()
{
    char tmp[256];
    int length;

    //printf("read buffer_ %p offset %d\n", buffer_, cursorOffset_);

    impl_->serializedToken.wrapForDecode((char *)buffer_, cursorOffset_,
        impl_->serializedToken.blockLength(), impl_->serializedToken.templateVersion());

    // read all the var data and save in Impl then save size

    impl_->nameLength = impl_->serializedToken.getName(impl_->name, sizeof(impl_->name));

    impl_->constValLength = impl_->serializedToken.getConstVal(impl_->constVal, sizeof(impl_->constVal));

    // don't really do anything with min/max/null/encoding right now
    length = impl_->serializedToken.getMinVal(tmp, sizeof(tmp));
    length = impl_->serializedToken.getMaxVal(tmp, sizeof(tmp));
    length = impl_->serializedToken.getNullVal(tmp, sizeof(tmp));
    length = impl_->serializedToken.getCharacterEncoding(tmp, sizeof(tmp));

    impl_->serializedTokenSize = impl_->serializedToken.size();

//    printf("token %p %d offset=%d size=%d id=%d signal=%d type=%d order=%d name=%s constLen=%d\n",
//           buffer_, cursorOffset_, offset(), size(), schemaId(), signal(), primitiveType(), byteOrder(),
//           name().c_str(), impl_->constValLength);

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

int32_t Ir::offset() const
{
    return impl_->serializedToken.tokenOffset();
}

int32_t Ir::size() const
{
    return impl_->serializedToken.tokenSize();
}

Ir::TokenSignal Ir::signal() const
{
    // the serialized IR and the Ir::TokenSignal enums MUST be kept in sync!
    return (Ir::TokenSignal)impl_->serializedToken.signal();
}

Ir::TokenByteOrder Ir::byteOrder() const
{
    // the serialized IR and the Ir::TokenByteOrder enums MUST be kept in sync!
    return (Ir::TokenByteOrder)impl_->serializedToken.byteOrder();
}

Ir::TokenPrimitiveType Ir::primitiveType() const
{
    // the serialized IR and the Ir::TokenPrimitiveType enums MUST be kept in sync!
    return (Ir::TokenPrimitiveType)impl_->serializedToken.primitiveType();
}

int32_t Ir::schemaId() const
{
    return impl_->serializedToken.schemaID();
}

uint64_t Ir::validValue() const
{
    // constVal holds the validValue. primitiveType holds the type
    switch (primitiveType())
    {
        case Ir::CHAR:
            return impl_->constVal[0];
            break;

        case Ir::UINT8:
            return impl_->constVal[0];
            break;

        default:
            throw "do not know validValue primitiveType";
            break;
    }
}

uint64_t Ir::choiceValue() const
{
    // constVal holds the validValue. primitiveType holds the type
    switch (primitiveType())
    {
        case Ir::UINT8:
            return impl_->constVal[0];
            break;

        case Ir::UINT16:
            return *(uint16_t *)(impl_->constVal);
            break;

        case Ir::UINT32:
            return *(uint32_t *)(impl_->constVal);
            break;

        case Ir::UINT64:
            return *(uint64_t *)(impl_->constVal);
            break;

        default:
            throw "do not know choice primitiveType";
            break;
    }
}

int64_t Ir::nameLen() const
{
    return impl_->nameLength;
}

std::string Ir::name() const
{
    return std::string(impl_->name, nameLen());
}

int64_t Ir::constLen() const
{
    return impl_->constValLength;
}

const char *Ir::constVal() const
{
    if (constLen() == 0)
    {
        return NULL;
    }

    return impl_->constVal;
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

void Ir::addToken(uint32_t offset,
                  uint32_t size,
                  TokenSignal signal,
                  TokenByteOrder byteOrder,
                  TokenPrimitiveType primitiveType,
                  uint16_t schemaId,
                  const std::string &name,
                  const char *constVal,
                  int constValLength)
{
    SerializedToken serializedToken;

    if (buffer_ == NULL)
    {
        buffer_ = new char[4098];
    }

    //printf("buffer_ %p offset %d\n", buffer_, cursorOffset_);

    serializedToken.wrapForEncode((char *)buffer_, cursorOffset_);

    serializedToken.tokenOffset(offset)
                   .tokenSize(size)
                   .schemaID(schemaId)
                   .tokenVersion(0)
                   .signal((SerializedSignal::Value)signal)
                   .primitiveType((SerializedPrimitiveType::Value)primitiveType)
                   .byteOrder((SerializedByteOrder::Value)byteOrder);

    serializedToken.putName(name.c_str(), name.size());
    serializedToken.putConstVal(constVal, constValLength);
    serializedToken.putMinVal(NULL, 0);
    serializedToken.putMaxVal(NULL, 0);
    serializedToken.putNullVal(NULL, 0);
    serializedToken.putCharacterEncoding(NULL, 0);

    cursorOffset_ += serializedToken.size();
    len_ = cursorOffset_;
}
