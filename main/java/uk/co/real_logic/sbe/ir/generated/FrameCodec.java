/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;

public class FrameCodec
{
    public static final int BLOCK_LENGTH = 12;

    public static final int TEMPLATE_ID = 1;
    public static final short SCHEMA_ID = (short)0;
    public static final short SCHEMA_VERSION = (short)0;
    private final FrameCodec parentMessage = this;
    private DirectBuffer buffer;
    private int offset;
    private int limit;
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

    public short schemaId()
    {
        return SCHEMA_ID;
    }

    public short schemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String semanticType()
    {
        return "";
    }

    public int offset()
    {
        return offset;
    }

    public FrameCodec wrapForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = SCHEMA_VERSION;
        limit(offset + actingBlockLength);

        return this;
    }

    public FrameCodec wrapForDecode(final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        limit(offset + actingBlockLength);

        return this;
    }

    public int size()
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

    public static int sbeIrIdId()
    {
        return 1;
    }

    public static String sbeIrIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbeIrIdNullValue()
    {
        return -2147483648;
    }

    public static int sbeIrIdMinValue()
    {
        return -2147483647;
    }

    public static int sbeIrIdMaxValue()
    {
        return 2147483647;
    }

    public int sbeIrId()
    {
        return CodecUtil.int32Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public FrameCodec sbeIrId(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int sbeIrVersionId()
    {
        return 2;
    }

    public static String sbeIrVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbeIrVersionNullValue()
    {
        return -2147483648;
    }

    public static int sbeIrVersionMinValue()
    {
        return -2147483647;
    }

    public static int sbeIrVersionMaxValue()
    {
        return 2147483647;
    }

    public int sbeIrVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public FrameCodec sbeIrVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int sbeSchemaVersionId()
    {
        return 3;
    }

    public static String sbeSchemaVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbeSchemaVersionNullValue()
    {
        return -2147483648;
    }

    public static int sbeSchemaVersionMinValue()
    {
        return -2147483647;
    }

    public static int sbeSchemaVersionMaxValue()
    {
        return 2147483647;
    }

    public int sbeSchemaVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public FrameCodec sbeSchemaVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int sbePackageNameId()
    {
        return 4;
    }

    public static String sbePackageNameCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String sbePackageNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbePackageNameHeaderSize()
    {
        return 1;
    }

    public int getSbePackageName(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        CodecUtil.int8sGet(buffer, limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int putSbePackageName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int sbeNamespaceNameId()
    {
        return 5;
    }

    public static String sbeNamespaceNameCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String sbeNamespaceNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbeNamespaceNameHeaderSize()
    {
        return 1;
    }

    public int getSbeNamespaceName(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        CodecUtil.int8sGet(buffer, limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int putSbeNamespaceName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int sbeSemanticVersionId()
    {
        return 6;
    }

    public static String sbeSemanticVersionCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String sbeSemanticVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int sbeSemanticVersionHeaderSize()
    {
        return 1;
    }

    public int getSbeSemanticVersion(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        CodecUtil.int8sGet(buffer, limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int putSbeSemanticVersion(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }
}
