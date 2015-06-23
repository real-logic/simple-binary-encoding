/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;
import uk.co.real_logic.agrona.MutableDirectBuffer;

public class FrameCodecEncoder
{
    public static final int BLOCK_LENGTH = 12;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;

    private final FrameCodecEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    protected int offset;
    protected int limit;
    protected int actingBlockLength;
    protected int actingVersion;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public int offset()
    {
        return offset;
    }

    public FrameCodecEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);
        return this;
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        buffer.checkLimit(limit);
        this.limit = limit;
    }

    public static int irIdNullValue()
    {
        return -2147483648;
    }

    public static int irIdMinValue()
    {
        return -2147483647;
    }

    public static int irIdMaxValue()
    {
        return 2147483647;
    }
    public FrameCodecEncoder irId(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int irVersionNullValue()
    {
        return -2147483648;
    }

    public static int irVersionMinValue()
    {
        return -2147483647;
    }

    public static int irVersionMaxValue()
    {
        return 2147483647;
    }
    public FrameCodecEncoder irVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int schemaVersionNullValue()
    {
        return -2147483648;
    }

    public static int schemaVersionMinValue()
    {
        return -2147483647;
    }

    public static int schemaVersionMaxValue()
    {
        return 2147483647;
    }
    public FrameCodecEncoder schemaVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int packageNameId()
    {
        return 4;
    }

    public static String packageNameCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String packageNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putPackageName(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putPackageName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public FrameCodecEncoder packageName(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }

    public static int namespaceNameId()
    {
        return 5;
    }

    public static String namespaceNameCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String namespaceNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putNamespaceName(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putNamespaceName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public FrameCodecEncoder namespaceName(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }

    public static int semanticVersionId()
    {
        return 6;
    }

    public static String semanticVersionCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String semanticVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putSemanticVersion(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putSemanticVersion(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public FrameCodecEncoder semanticVersion(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }
}
