/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.*;

public class SerializedToken implements MessageFlyweight
{
    public static final int TEMPLATE_ID = 2;
    public static final short TEMPLATE_VERSION = (short)0;
    public static final int BLOCK_LENGTH = 19;

    private MessageFlyweight parentMessage = this;
    private DirectBuffer buffer;
    private int offset;
    private int position;
    private int actingBlockLength;
    private int actingVersion;

    public int blockLength()
    {
        return BLOCK_LENGTH;
    }

    public int templateId()
    {
        return TEMPLATE_ID;
    }

    public short templateVersion()
    {
        return TEMPLATE_VERSION;
    }

    public int offset()
    {
        return offset;
    }

    public SerializedToken wrapForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = TEMPLATE_VERSION;
        position(offset + actingBlockLength);

        return this;
    }

    public SerializedToken wrapForDecode(final DirectBuffer buffer, final int offset,
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
        buffer.checkPosition(position);
        this.position = position;
    }

    public static int tokenOffsetSchemaId()
    {
        return 11;
    }

    public static int tokenOffsetNullVal()
    {
        return -2147483648;
    }

    public static int tokenOffsetMinVal()
    {
        return -2147483647;
    }

    public static int tokenOffsetMaxVal()
    {
        return 2147483647;
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

    public static int tokenSizeSchemaId()
    {
        return 12;
    }

    public static int tokenSizeNullVal()
    {
        return -2147483648;
    }

    public static int tokenSizeMinVal()
    {
        return -2147483647;
    }

    public static int tokenSizeMaxVal()
    {
        return 2147483647;
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

    public static int schemaIDSchemaId()
    {
        return 13;
    }

    public static int schemaIDNullVal()
    {
        return -2147483648;
    }

    public static int schemaIDMinVal()
    {
        return -2147483647;
    }

    public static int schemaIDMaxVal()
    {
        return 2147483647;
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

    public static int tokenVersionSchemaId()
    {
        return 17;
    }

    public static int tokenVersionNullVal()
    {
        return -2147483648;
    }

    public static int tokenVersionMinVal()
    {
        return -2147483647;
    }

    public static int tokenVersionMaxVal()
    {
        return 2147483647;
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

    public static int signalSchemaId()
    {
        return 14;
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

    public static int primitiveTypeSchemaId()
    {
        return 15;
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

    public static int byteOrderSchemaId()
    {
        return 16;
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

    public static int nameSchemaId()
    {
        return 18;
    }

    public static String nameCharacterEncoding()
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

    public static int constValSchemaId()
    {
        return 19;
    }

    public static String constValCharacterEncoding()
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

    public static int minValSchemaId()
    {
        return 20;
    }

    public static String minValCharacterEncoding()
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

    public static int maxValSchemaId()
    {
        return 21;
    }

    public static String maxValCharacterEncoding()
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

    public static int nullValSchemaId()
    {
        return 22;
    }

    public static String nullValCharacterEncoding()
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

    public static int characterEncodingSchemaId()
    {
        return 23;
    }

    public static String characterEncodingCharacterEncoding()
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
