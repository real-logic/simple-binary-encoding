/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.*;

public class SerializedToken implements MessageFlyweight
{
    private static final long TEMPLATE_ID = 2L;
    private static final int TEMPLATE_VERSION = 0;
    private static final int BLOCK_LENGTH = 19;

    private DirectBuffer buffer;
    private int offset;
    private int position;
    private int actingBlockLength;
    private int actingVersion;

    public int blockLength()
    {
        return BLOCK_LENGTH;
    }

    public long templateId()
    {
        return TEMPLATE_ID;
    }

    public int templateVersion()
    {
        return TEMPLATE_VERSION;
    }

    public int offset()
    {
        return offset;
    }

    public SerializedToken resetForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = TEMPLATE_VERSION;
        position(offset + actingBlockLength);

        return this;
    }

    public SerializedToken resetForDecode(final DirectBuffer buffer, final int offset,
                             final int actingBlockLength, final int actingVersion)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        position(offset + actingBlockLength);

        return this;
    }

    public int size()
    {
        return position - offset;
    }

    public int position()
    {
        return position;
    }

    public void position(final int position)
    {
        CodecUtil.checkPosition(position, buffer.capacity());
        this.position = position;
    }

    public long tokenOffsetId()
    {
        return 11L;
    }

    public int tokenOffset()
    {
        return CodecUtil.int32Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken tokenOffset(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long tokenSizeId()
    {
        return 12L;
    }

    public int tokenSize()
    {
        return CodecUtil.int32Get(buffer, offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken tokenSize(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long schemaIDId()
    {
        return 13L;
    }

    public int schemaID()
    {
        return CodecUtil.int32Get(buffer, offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken schemaID(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long tokenVersionId()
    {
        return 17L;
    }

    public int tokenVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 12, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedToken tokenVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 12, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public long signalId()
    {
        return 14L;
    }

    public SerializedSignal signal()
    {
        return SerializedSignal.get(CodecUtil.uint8Get(buffer, offset + 16));
    }

    public SerializedToken signal(final SerializedSignal value)
    {
        CodecUtil.uint8Put(buffer, offset + 16, value.value());
        return this;
    }

    public long primitiveTypeId()
    {
        return 15L;
    }

    public SerializedPrimitiveType primitiveType()
    {
        return SerializedPrimitiveType.get(CodecUtil.uint8Get(buffer, offset + 17));
    }

    public SerializedToken primitiveType(final SerializedPrimitiveType value)
    {
        CodecUtil.uint8Put(buffer, offset + 17, value.value());
        return this;
    }

    public long byteOrderId()
    {
        return 16L;
    }

    public SerializedByteOrder byteOrder()
    {
        return SerializedByteOrder.get(CodecUtil.uint8Get(buffer, offset + 18));
    }

    public SerializedToken byteOrder(final SerializedByteOrder value)
    {
        CodecUtil.uint8Put(buffer, offset + 18, value.value());
        return this;
    }

    public long nameId()
    {
        return 18L;
    }

    public String nameCharacterEncoding()
    {
        return "UTF-8";
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

    public long constValId()
    {
        return 19L;
    }

    public String constValCharacterEncoding()
    {
        return "UTF-8";
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

    public long minValId()
    {
        return 20L;
    }

    public String minValCharacterEncoding()
    {
        return "UTF-8";
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

    public long maxValId()
    {
        return 21L;
    }

    public String maxValCharacterEncoding()
    {
        return "UTF-8";
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

    public long nullValId()
    {
        return 22L;
    }

    public String nullValCharacterEncoding()
    {
        return "UTF-8";
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

    public long characterEncodingId()
    {
        return 23L;
    }

    public String characterEncodingCharacterEncoding()
    {
        return "UTF-8";
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
