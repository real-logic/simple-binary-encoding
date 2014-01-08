/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;

public class TokenCodec
{
    public static final int TEMPLATE_ID = 2;
    public static final short TEMPLATE_VERSION = (short)0;
    public static final int BLOCK_LENGTH = 20;

    private final TokenCodec parentMessage = this;
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

    public short templateVersion()
    {
        return TEMPLATE_VERSION;
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
        this.actingVersion = TEMPLATE_VERSION;
        limit(offset + actingBlockLength);

        return this;
    }

    public TokenCodec wrapForDecode(final DirectBuffer buffer, final int offset,
                              final int actingBlockLength, final int actingVersion)
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

    public static int tokenOffsetSchemaId()
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

    public TokenCodec tokenOffset(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int tokenSizeSchemaId()
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

    public TokenCodec tokenSize(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int schemaIdSchemaId()
    {
        return 13;
    }

    public static String schemaIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public static int schemaIdNullVal()
    {
        return -2147483648;
    }

    public static int schemaIdMinVal()
    {
        return -2147483647;
    }

    public static int schemaIdMaxVal()
    {
        return 2147483647;
    }

    public int schemaId()
    {
        return CodecUtil.int32Get(buffer, offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public TokenCodec schemaId(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int tokenVersionSchemaId()
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

    public TokenCodec tokenVersion(final int value)
    {
        CodecUtil.int32Put(buffer, offset + 12, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int signalSchemaId()
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

    public static int primitiveTypeSchemaId()
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

    public static int byteOrderSchemaId()
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

    public static int presenceSchemaId()
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

    public static int nameSchemaId()
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

    public static int constValSchemaId()
    {
        return 20;
    }

    public static String constValCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String constValMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int getConstVal(final byte[] dst, final int dstOffset, final int length)
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

    public int putConstVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int minValSchemaId()
    {
        return 21;
    }

    public static String minValCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String minValMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int getMinVal(final byte[] dst, final int dstOffset, final int length)
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

    public int putMinVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int maxValSchemaId()
    {
        return 22;
    }

    public static String maxValCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String maxValMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int getMaxVal(final byte[] dst, final int dstOffset, final int length)
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

    public int putMaxVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int nullValSchemaId()
    {
        return 23;
    }

    public static String nullValCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String nullValMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int getNullVal(final byte[] dst, final int dstOffset, final int length)
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

    public int putNullVal(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public static int characterEncodingSchemaId()
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

    public static int epochSchemaId()
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

    public static int timeUnitSchemaId()
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

    public static int semanticTypeSchemaId()
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
