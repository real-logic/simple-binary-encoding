#include <stdio.h>
#include <iostream>
#include <sys/types.h>

#include "Ir.h"

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

/*
 * Until we have SBE generating C++, just layering a struct over each token
 */
struct IrToken
{
    uint32_t offset;
    uint32_t size;
    uint16_t schemaId;
    uint8_t signal;
    uint8_t primitiveType;
    uint8_t byteOrder;
    uint8_t nameLen;
    // name follows for nameLen bytes
};

Ir::Ir(const char *buffer, const int len) :
    buffer_(buffer), len_(len)
{
    begin();
}

void Ir::begin()
{
    cursorOffset_ = 0;
}

void Ir::next()
{
    struct IrToken *currToken = (struct IrToken *)(buffer_ + cursorOffset_);

    cursorOffset_ += sizeof(struct IrToken) + currToken->nameLen;
}

bool Ir::end() const
{
    if (cursorOffset_ < len_)
    {
        return false;
    }
    return true;
}

uint32_t Ir::offset() const
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->offset;
}

uint32_t Ir::size() const
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->size;
}

Ir::TokenSignal Ir::signal() const
{
    return (Ir::TokenSignal)((struct IrToken *)(buffer_ + cursorOffset_))->signal;
}

Ir::TokenByteOrder Ir::byteOrder() const
{
    return (Ir::TokenByteOrder)((struct IrToken *)(buffer_ + cursorOffset_))->byteOrder;
}

Ir::TokenPrimitiveType Ir::primitiveType() const
{
    return (Ir::TokenPrimitiveType)((struct IrToken *)(buffer_ + cursorOffset_))->primitiveType;
}

uint16_t Ir::schemaId() const
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->schemaId;    
}

uint8_t Ir::nameLen() const
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->nameLen;
}

std::string Ir::name() const
{
    return std::string((buffer_ + cursorOffset_ + sizeof(struct IrToken)), nameLen());
}

// protected
void Ir::addToken(uint32_t offset,
                  uint32_t size,
                  TokenSignal signal,
                  TokenByteOrder byteOrder,
                  TokenPrimitiveType primitiveType,
                  uint16_t schemaId,
                  const std::string &name)
{
    if (buffer_ == NULL)
    {
        buffer_ = new char[2048];
    }

    struct IrToken *token = (struct IrToken *)(buffer_ + cursorOffset_);
    token->offset = offset;
    token->size = size;
    token->signal = signal;
    token->byteOrder = byteOrder;
    token->primitiveType = primitiveType;
    token->schemaId = schemaId;
    token->nameLen = name.size();
    ::strncpy((char *)(buffer_ + cursorOffset_ + sizeof(struct IrToken)), name.c_str(), name.size());
    cursorOffset_ += sizeof(struct IrToken) + name.size();
    len_ = cursorOffset_;
}
