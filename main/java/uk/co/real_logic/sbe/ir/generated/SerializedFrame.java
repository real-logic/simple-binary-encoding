/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.CodecUtil;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;

public class SerializedFrame
{
    public static final int TEMPLATE_ID = 1;
    public static final short TEMPLATE_VERSION = (short)0;
    public static final int BLOCK_LENGTH = 8;

    private SerializedFrame parentMessage = this;
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

    public SerializedFrame wrapForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = TEMPLATE_VERSION;
        position(offset + actingBlockLength);

        return this;
    }

    public SerializedFrame wrapForDecode(final DirectBuffer buffer, final int offset,
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

    public static int sbeIrVersionSchemaId()
    {
        return 1;
    }

    public static int sbeIrVersionNullVal()
    {
        return -2147483648;
    }

    public static int sbeIrVersionMinVal()
    {
        return -2147483647;
    }

    public static int sbeIrVersionMaxVal()
    {
        return 2147483647;
    }

    public int sbeIrVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedFrame sbeIrVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int schemaVersionSchemaId()
    {
        return 2;
    }

    public static int schemaVersionNullVal()
    {
        return -2147483648;
    }

    public static int schemaVersionMinVal()
    {
        return -2147483647;
    }

    public static int schemaVersionMaxVal()
    {
        return 2147483647;
    }

    public int schemaVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public SerializedFrame schemaVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int packageValSchemaId()
    {
        return 4;
    }

    public static String packageValCharacterEncoding()
    {
        return "UTF-8";
    }

    public int getPackageVal(final byte[] dst, final int dstOffset, final int length)
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

    public int putPackageVal(final byte[] src, final int srcOffset, final int length)
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
