/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.CodecUtil;
import uk.co.real_logic.sbe.generation.java.DirectBuffer;
import uk.co.real_logic.sbe.generation.java.MessageFlyweight;

public class SerializedFrame implements MessageFlyweight
{
    private static final int BLOCK_LENGTH = 2;
    private static final long TEMPLATE_ID = 1;
    private static final int TEMPLATE_VERSION = 0;

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

    public SerializedFrame resetForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = TEMPLATE_VERSION;
        position(offset + actingBlockLength);
        return this;
    }

    public SerializedFrame resetForDecode(final DirectBuffer buffer, final int offset,
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
        CodecUtil.checkPosition(position, offset, buffer.capacity());
        this.position = position;
    }

    public long sbeIrVersionId()
    {
        return 1;
    }

    public short sbeIrVersion()
    {
        return CodecUtil.uint8Get(buffer, offset + 0);
    }

    public SerializedFrame sbeIrVersion(final short value)
    {
        CodecUtil.uint8Put(buffer, offset + 0, value);
        return this;
    }

    public long schemaVersionId()
    {
        return 2;
    }

    public short schemaVersion()
    {
        return CodecUtil.uint8Get(buffer, offset + 1);
    }

    public SerializedFrame schemaVersion(final short value)
    {
        CodecUtil.uint8Put(buffer, offset + 1, value);
        return this;
    }

    public String packageValCharacterEncoding()
    {
        return "UTF-8";
    }

    public long packageValId()
    {
        return 4;
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
