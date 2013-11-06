/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import java.nio.ByteOrder;
import java.util.*;
import uk.co.real_logic.sbe.generation.java.*;

public class SerializedFrame implements MessageFlyweight
{
    private static final int BLOCKLENGTH = 2;

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

    public SerializedFrame reset(final DirectBuffer buffer, final int offset)
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
        return 1;
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
