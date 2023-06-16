/* Generated SBE (Simple Binary Encoding) message codec. */
package uk.co.real_logic.sbe.ir.generated;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;


/**
 * Frame Header for start of encoding IR.
 */
@SuppressWarnings("all")
public final class FrameCodecEncoder
{
    private static final boolean DEBUG_MODE = !Boolean.getBoolean("agrona.disable.bounds.checks");

    /**
     * The states in which a encoder/decoder/codec can live.
     *
     * <p>The state machine diagram below describes the valid state transitions
     * according to the order in which fields may be accessed safely.
     *
     * <pre>{@code
     *   digraph G {
     *       NOT_WRAPPED -> V0_BLOCK [label="  .wrap(...)  "];
     *       V0_BLOCK -> V0_BLOCK [label="  .irId(value)  "];
     *       V0_BLOCK -> V0_BLOCK [label="  .irVersion(value)  "];
     *       V0_BLOCK -> V0_BLOCK [label="  .schemaVersion(value)  "];
     *       V0_BLOCK -> V0_PACKAGENAME_DONE [label="  .packageName(value)  "];
     *       V0_PACKAGENAME_DONE -> V0_NAMESPACENAME_DONE [label="  .namespaceName(value)  "];
     *       V0_NAMESPACENAME_DONE -> V0_SEMANTICVERSION_DONE [label="  .semanticVersion(value)  "];
     *   }
     * }</pre>
     */
    private enum CodecState
    {
        NOT_WRAPPED,
        V0_BLOCK,
        V0_PACKAGENAME_DONE,
        V0_NAMESPACENAME_DONE,
        V0_SEMANTICVERSION_DONE,
    }

    private CodecState codecState = CodecState.NOT_WRAPPED;

    private CodecState codecState()
    {
        return codecState;
    }

    private void codecState(CodecState newState)
    {
        codecState = newState;
    }

    public static final int BLOCK_LENGTH = 12;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final String SEMANTIC_VERSION = "";
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final FrameCodecEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    private int initialOffset;
    private int offset;
    private int limit;

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

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int initialOffset()
    {
        return initialOffset;
    }

    public int offset()
    {
        return offset;
    }

    public FrameCodecEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.initialOffset = offset;
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        if (DEBUG_MODE)
        {
            codecState(CodecState.V0_BLOCK);
        }

        return this;
    }

    public FrameCodecEncoder wrapAndApplyHeader(
        final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder)
    {
        headerEncoder
            .wrap(buffer, offset)
            .blockLength(BLOCK_LENGTH)
            .templateId(TEMPLATE_ID)
            .schemaId(SCHEMA_ID)
            .version(SCHEMA_VERSION);

        return wrap(buffer, offset + MessageHeaderEncoder.ENCODED_LENGTH);
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
        this.limit = limit;
    }

    public static int irIdId()
    {
        return 1;
    }

    public static int irIdSinceVersion()
    {
        return 0;
    }

    public static int irIdEncodingOffset()
    {
        return 0;
    }

    public static int irIdEncodingLength()
    {
        return 4;
    }

    public static String irIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
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
        if (DEBUG_MODE)
        {
            if (codecState() == CodecState.NOT_WRAPPED)
            {
                throw new IllegalStateException("Cannot access field \"irId\" in state: " + codecState());
            }
        }

        buffer.putInt(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int irVersionId()
    {
        return 2;
    }

    public static int irVersionSinceVersion()
    {
        return 0;
    }

    public static int irVersionEncodingOffset()
    {
        return 4;
    }

    public static int irVersionEncodingLength()
    {
        return 4;
    }

    public static String irVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
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
        if (DEBUG_MODE)
        {
            if (codecState() == CodecState.NOT_WRAPPED)
            {
                throw new IllegalStateException("Cannot access field \"irVersion\" in state: " + codecState());
            }
        }

        buffer.putInt(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int schemaVersionId()
    {
        return 3;
    }

    public static int schemaVersionSinceVersion()
    {
        return 0;
    }

    public static int schemaVersionEncodingOffset()
    {
        return 8;
    }

    public static int schemaVersionEncodingLength()
    {
        return 4;
    }

    public static String schemaVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
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
        if (DEBUG_MODE)
        {
            if (codecState() == CodecState.NOT_WRAPPED)
            {
                throw new IllegalStateException("Cannot access field \"schemaVersion\" in state: " + codecState());
            }
        }

        buffer.putInt(offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int packageNameId()
    {
        return 4;
    }

    public static String packageNameCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.UTF_8.name();
    }

    public static String packageNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int packageNameHeaderLength()
    {
        return 2;
    }

    public FrameCodecEncoder putPackageName(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_BLOCK:
                    codecState(CodecState.V0_PACKAGENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"packageName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder putPackageName(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_BLOCK:
                    codecState(CodecState.V0_PACKAGENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"packageName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder packageName(final String value)
    {
        final byte[] bytes = (null == value || value.isEmpty()) ? org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        final int length = bytes.length;
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_BLOCK:
                    codecState(CodecState.V0_PACKAGENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"packageName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, bytes, 0, length);

        return this;
    }

    public static int namespaceNameId()
    {
        return 5;
    }

    public static String namespaceNameCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.UTF_8.name();
    }

    public static String namespaceNameMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int namespaceNameHeaderLength()
    {
        return 2;
    }

    public FrameCodecEncoder putNamespaceName(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_PACKAGENAME_DONE:
                    codecState(CodecState.V0_NAMESPACENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"namespaceName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder putNamespaceName(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_PACKAGENAME_DONE:
                    codecState(CodecState.V0_NAMESPACENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"namespaceName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder namespaceName(final String value)
    {
        final byte[] bytes = (null == value || value.isEmpty()) ? org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        final int length = bytes.length;
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_PACKAGENAME_DONE:
                    codecState(CodecState.V0_NAMESPACENAME_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"namespaceName\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, bytes, 0, length);

        return this;
    }

    public static int semanticVersionId()
    {
        return 6;
    }

    public static String semanticVersionCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.UTF_8.name();
    }

    public static String semanticVersionMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int semanticVersionHeaderLength()
    {
        return 2;
    }

    public FrameCodecEncoder putSemanticVersion(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_NAMESPACENAME_DONE:
                    codecState(CodecState.V0_SEMANTICVERSION_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"semanticVersion\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder putSemanticVersion(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_NAMESPACENAME_DONE:
                    codecState(CodecState.V0_SEMANTICVERSION_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"semanticVersion\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public FrameCodecEncoder semanticVersion(final String value)
    {
        final byte[] bytes = (null == value || value.isEmpty()) ? org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        final int length = bytes.length;
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        if (DEBUG_MODE)
        {
            switch (codecState())
            {
                case V0_NAMESPACENAME_DONE:
                    codecState(CodecState.V0_SEMANTICVERSION_DONE);
                    break;
                default:
                    throw new IllegalStateException("Cannot access field \"semanticVersion\" in state: " + codecState());
            }
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, bytes, 0, length);

        return this;
    }

    public String toString()
    {
        if (null == buffer)
        {
            return "";
        }

        return appendTo(new StringBuilder()).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        if (null == buffer)
        {
            return builder;
        }

        final FrameCodecDecoder decoder = new FrameCodecDecoder();
        decoder.wrap(buffer, initialOffset, BLOCK_LENGTH, SCHEMA_VERSION);

        return decoder.appendTo(builder);
    }
}
