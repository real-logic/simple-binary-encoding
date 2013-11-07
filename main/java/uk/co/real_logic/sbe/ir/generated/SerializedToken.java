/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import java.nio.ByteOrder;
import java.util.*;
import uk.co.real_logic.sbe.generation.java.*;

public class SerializedToken implements MessageFlyweight
{
    private static final int BLOCKLENGTH = 16;

    private DirectBuffer buffer;
    private int offset;
    private int position;

    public int blockLength()
    {
        return BLOCKLENGTH;
    }

    public int offset()
    {
        return offset;
    }

    public SerializedToken reset(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        position(offset + blockLength());
        return this;
    }

    public int size()
    {
        return position - offset;
    }

    public long templateId()
    {
        return 2;
    }

    public int position()
    {
        return position;
    }

    public void position(final int position)
    {
        CodecUtil.checkPosition(position, offset, buffer.capacity());
        this.position = position;
    }

    public long tokenOffsetId()
    {
        return 11;
    }

    public int tokenOffset()
    {
        return CodecUtil.int32Get(buffer, offset + 0, ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken tokenOffset(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long tokenSizeId()
    {
        return 12;
    }

    public int tokenSize()
    {
        return CodecUtil.int32Get(buffer, offset + 4, ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken tokenSize(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long schemaIDId()
    {
        return 13;
    }

    public int schemaID()
    {
        return CodecUtil.int32Get(buffer, offset + 8, ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken schemaID(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long signalId()
    {
        return 14;
    }

    public SerializedSignal signal()
    {
        return SerializedSignal.get(CodecUtil.uint8Get(buffer, offset + 12));
    }

    public SerializedToken signal(final SerializedSignal value)
    {
        CodecUtil.uint8Put(buffer, offset + 12, value.value());
        return this;
    }

    public long primitiveTypeId()
    {
        return 15;
    }

    public SerializedPrimitiveType primitiveType()
    {
        return SerializedPrimitiveType.get(CodecUtil.uint8Get(buffer, offset + 13));
    }

    public SerializedToken primitiveType(final SerializedPrimitiveType value)
    {
        CodecUtil.uint8Put(buffer, offset + 13, value.value());
        return this;
    }

    public long byteOrderId()
    {
        return 16;
    }

    public SerializedByteOrder byteOrder()
    {
        return SerializedByteOrder.get(CodecUtil.uint8Get(buffer, offset + 14));
    }

    public SerializedToken byteOrder(final SerializedByteOrder value)
    {
        CodecUtil.uint8Put(buffer, offset + 14, value.value());
        return this;
    }

    public long sinceVersionId()
    {
        return 17;
    }

    public short sinceVersion()
    {
        return CodecUtil.uint8Get(buffer, offset + 15);
    }

    public SerializedToken sinceVersion(final short value)
    {
        CodecUtil.uint8Put(buffer, offset + 15, value);
        return this;
    }

    public String nameCharacterEncoding()
    {
        return "UTF-8";
    }

    public long nameId()
    {
        return 18;
    }

    public int getName(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }

    public String constValCharacterEncoding()
    {
        return "UTF-8";
    }

    public long constValId()
    {
        return 19;
    }

    public int getConstVal(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putConstVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }

    public String minValCharacterEncoding()
    {
        return "UTF-8";
    }

    public long minValId()
    {
        return 20;
    }

    public int getMinVal(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putMinVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }

    public String maxValCharacterEncoding()
    {
        return "UTF-8";
    }

    public long maxValId()
    {
        return 21;
    }

    public int getMaxVal(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putMaxVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }

    public String nullValCharacterEncoding()
    {
        return "UTF-8";
    }

    public long nullValId()
    {
        return 22;
    }

    public int getNullVal(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putNullVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }

    public String characterEncodingCharacterEncoding()
    {
        return "UTF-8";
    }

    public long characterEncodingId()
    {
        return 23;
    }

    public int getCharacterEncoding(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, lengthPosition);
        final int bytesCopied = Math.min(length, dataLength);
        CodecUtil.int8sGet(buffer, position(), dst, dstOffset, bytesCopied);
        position(position() + dataLength);
        return bytesCopied;
    }

    public int putCharacterEncoding(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int lengthPosition = position();
        CodecUtil.uint8Put(buffer, lengthPosition, (short)length);
        position(lengthPosition + sizeOfLengthField);
        CodecUtil.int8sPut(buffer, position(), src, srcOffset, length);
        position(position() + length);
        return length;
    }
}
