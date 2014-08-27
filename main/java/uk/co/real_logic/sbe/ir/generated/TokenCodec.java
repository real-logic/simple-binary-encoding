/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;

public class TokenCodec
{
    public static final int BLOCK_LENGTH = 20;
    public static final int TEMPLATE_ID = 2;
    public static final int SCHEMA_ID = 0;
    public static final int SCHEMA_VERSION = 0;

    private final TokenCodec parentMessage = this;
    private DirectBuffer buffer;
    private int offset;
    private int limit;
    private int actingBlockLength;
    private int actingVersion;

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

    public TokenCodec wrapForEncode(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = BLOCK_LENGTH;
        this.actingVersion = SCHEMA_VERSION;
        limit(offset + actingBlockLength);

        return this;
    }

    public TokenCodec wrapForDecode(
        final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion)
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

    public static int tokenOffsetId()
    {
        return 11;
    }

    public static String tokenOffsetMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int tokenOffsetNullValue()
    {
        return -2147483648;
    }

    public static int tokenOffsetMinValue()
    {
        return -2147483647;
    }

    public static int tokenOffsetMaxValue()
    {
        return 2147483647;
    }

    public int tokenOffset()
    {
        return CodecUtil.int32Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public TokenCodec tokenOffset(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int tokenSizeId()
    {
        return 12;
    }

    public static String tokenSizeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int tokenSizeNullValue()
    {
        return -2147483648;
    }

    public static int tokenSizeMinValue()
    {
        return -2147483647;
    }

    public static int tokenSizeMaxValue()
    {
        return 2147483647;
    }

    public int tokenSize()
    {
        return CodecUtil.int32Get(buffer, offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public TokenCodec tokenSize(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int fieldIdId()
    {
        return 13;
    }

    public static String fieldIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int fieldIdNullValue()
    {
        return -2147483648;
    }

    public static int fieldIdMinValue()
    {
        return -2147483647;
    }

    public static int fieldIdMaxValue()
    {
        return 2147483647;
    }

    public int fieldId()
    {
        return CodecUtil.int32Get(buffer, offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public TokenCodec fieldId(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int tokenVersionId()
    {
        return 14;
    }

    public static String tokenVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int tokenVersionNullValue()
    {
        return -2147483648;
    }

    public static int tokenVersionMinValue()
    {
        return -2147483647;
    }

    public static int tokenVersionMaxValue()
    {
        return 2147483647;
    }

    public int tokenVersion()
    {
        return CodecUtil.int32Get(buffer, offset + 12, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public TokenCodec tokenVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 12, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int signalId()
    {
        return 15;
    }

    public static String signalMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public SignalCodec signal()
    {
        return SignalCodec.get(CodecUtil.uint8Get(buffer, offset + 16));
    }

    public TokenCodec signal(final SignalCodec value)
    {
        CodecUtil.uint8Put(buffer, offset + 16, value.value());
        return this;
    }

    public static int primitiveTypeId()
    {
        return 16;
    }

    public static String primitiveTypeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public PrimitiveTypeCodec primitiveType()
    {
        return PrimitiveTypeCodec.get(CodecUtil.uint8Get(buffer, offset + 17));
    }

    public TokenCodec primitiveType(final PrimitiveTypeCodec value)
    {
        CodecUtil.uint8Put(buffer, offset + 17, value.value());
        return this;
    }

    public static int byteOrderId()
    {
        return 17;
    }

    public static String byteOrderMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public ByteOrderCodec byteOrder()
    {
        return ByteOrderCodec.get(CodecUtil.uint8Get(buffer, offset + 18));
    }

    public TokenCodec byteOrder(final ByteOrderCodec value)
    {
        CodecUtil.uint8Put(buffer, offset + 18, value.value());
        return this;
    }

    public static int presenceId()
    {
        return 18;
    }

    public static String presenceMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public PresenceCodec presence()
    {
        return PresenceCodec.get(CodecUtil.uint8Get(buffer, offset + 19));
    }

    public TokenCodec presence(final PresenceCodec value)
    {
        CodecUtil.uint8Put(buffer, offset + 19, value.value());
        return this;
    }

    public static int nameId()
    {
        return 19;
    }

    public static String nameCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String nameMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int nameHeaderSize()
    {
        return 1;
    }

    public int getName(final byte[] dst, final int dstOffset, final int length)
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

    public int putName(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int constValueId()
    {
        return 20;
    }

    public static String constValueCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String constValueMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int constValueHeaderSize()
    {
        return 1;
    }

    public int getConstValue(final byte[] dst, final int dstOffset, final int length)
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

    public int putConstValue(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int minValueId()
    {
        return 21;
    }

    public static String minValueCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String minValueMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int minValueHeaderSize()
    {
        return 1;
    }

    public int getMinValue(final byte[] dst, final int dstOffset, final int length)
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

    public int putMinValue(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int maxValueId()
    {
        return 22;
    }

    public static String maxValueCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String maxValueMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int maxValueHeaderSize()
    {
        return 1;
    }

    public int getMaxValue(final byte[] dst, final int dstOffset, final int length)
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

    public int putMaxValue(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int nullValueId()
    {
        return 23;
    }

    public static String nullValueCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String nullValueMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int nullValueHeaderSize()
    {
        return 1;
    }

    public int getNullValue(final byte[] dst, final int dstOffset, final int length)
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

    public int putNullValue(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int characterEncodingId()
    {
        return 24;
    }

    public static String characterEncodingCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String characterEncodingMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int characterEncodingHeaderSize()
    {
        return 1;
    }

    public int getCharacterEncoding(final byte[] dst, final int dstOffset, final int length)
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

    public int putCharacterEncoding(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int epochId()
    {
        return 25;
    }

    public static String epochCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String epochMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int epochHeaderSize()
    {
        return 1;
    }

    public int getEpoch(final byte[] dst, final int dstOffset, final int length)
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

    public int putEpoch(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int timeUnitId()
    {
        return 26;
    }

    public static String timeUnitCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String timeUnitMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int timeUnitHeaderSize()
    {
        return 1;
    }

    public int getTimeUnit(final byte[] dst, final int dstOffset, final int length)
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

    public int putTimeUnit(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int semanticTypeId()
    {
        return 27;
    }

    public static String semanticTypeCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String semanticTypeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int semanticTypeHeaderSize()
    {
        return 1;
    }

    public int getSemanticType(final byte[] dst, final int dstOffset, final int length)
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

    public int putSemanticType(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }
}
