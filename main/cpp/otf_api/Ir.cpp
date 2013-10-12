#include <stdio.h>
#include <sys/types.h>

#include "Ir.h"

using namespace sbe::on_the_fly;

/*
 * Until we have SBE generating C++, just layering a struct over each token
 */
struct IrToken
{
    uint32_t offset;
    uint32_t size;
    uint8_t signal;
    uint8_t primitiveType;
    uint8_t byteOrder;
    uint8_t schemaId;
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

bool Ir::end()
{
    if (cursorOffset_ < len_)
    {
        return false;
    }
    return true;
}

uint32_t Ir::offset()
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->offset;
}

uint32_t Ir::size()
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->size;
}

Ir::TokenSignal Ir::signal()
{
    return (Ir::TokenSignal)((struct IrToken *)(buffer_ + cursorOffset_))->signal;
}

Ir::TokenByteOrder Ir::byteOrder()
{
    return (Ir::TokenByteOrder)((struct IrToken *)(buffer_ + cursorOffset_))->byteOrder;
}

Ir::TokenPrimitiveType Ir::primitiveType()
{
    return (Ir::TokenPrimitiveType)((struct IrToken *)(buffer_ + cursorOffset_))->primitiveType;
}

uint16_t Ir::schemaId()
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->schemaId;    
}

uint8_t Ir::nameLen()
{
    return ((struct IrToken *)(buffer_ + cursorOffset_))->nameLen;
}

std::string Ir::name()
{
    return std::string((buffer_ + sizeof(struct IrToken)), nameLen());
}
