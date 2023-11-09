/*
 * Copyright 2013-2023 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.real_logic.sbe.properties.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.ListArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.properties.schema.*;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.ParserOptions;
import org.agrona.BitUtil;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableInteger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static uk.co.real_logic.sbe.ir.Signal.*;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

@SuppressWarnings("EnhancedSwitchMigration")
public final class SbeArbitraries
{
    private static final int MAX_COMPOSITE_DEPTH = 3;
    private static final int MAX_GROUP_DEPTH = 3;
    public static final int NULL_VALUE = Integer.MIN_VALUE;

    private SbeArbitraries()
    {
    }

    /**
     * This combinator adds duplicates to an arbitrary list. We prefer this to JQwik's built-in functionality,
     * as that is inefficient and dominates test runs.
     * <p>
     * This method works by generating a list of integers, which represent, in an alternating manner,
     * the number of items to skip before <i>selecting</i> an item to duplicate
     * and the number of items to skip before <i>inserting</i> the duplicate.
     *
     * @param maxDuplicates the maximum number of duplicates to add
     * @param arbitrary     the arbitrary list to duplicate items in
     * @param <T>           the type of the list
     * @return an arbitrary list with duplicates
     */
    private static <T> Arbitrary<List<T>> withDuplicates(
        final int maxDuplicates,
        final ListArbitrary<T> arbitrary)
    {
        return Combinators.combine(
            Arbitraries.integers().list().ofMaxSize(2 * maxDuplicates),
            arbitrary
        ).as((positions, originalItems) ->
        {
            if (originalItems.isEmpty())
            {
                return originalItems;
            }

            final List<T> items = new ArrayList<>(originalItems);
            T itemToDupe = null;
            int j = 0;

            for (final int position : positions)
            {
                j += position;
                j %= items.size();
                j = Math.abs(j);
                if (itemToDupe == null)
                {
                    itemToDupe = items.get(j);
                }
                else
                {
                    items.add(j, itemToDupe);
                    itemToDupe = null;
                }
            }

            return items;
        });
    }

    private static Arbitrary<EncodedDataTypeSchema> encodedDataTypeSchema()
    {
        return Combinators.combine(
            Arbitraries.of(PrimitiveType.values()),
            Arbitraries.of(1, 1, 1, 2, 13),
            presence(),
            Arbitraries.of(true, false)
        ).as(EncodedDataTypeSchema::new);
    }

    public enum CharGenerationMode
    {
        UNRESTRICTED,
        JSON_PRINTER_COMPATIBLE
    }

    private static Arbitrary<EnumTypeSchema> enumTypeSchema()
    {
        return Arbitraries.oneOf(
            Arbitraries.chars().alpha()
                .map(Character::toUpperCase)
                .list()
                .ofMaxSize(10)
                .uniqueElements()
                .map(values -> new EnumTypeSchema(
                    "char",
                    values.stream().map(String::valueOf).collect(Collectors.toList())
                )),
            Arbitraries.integers()
                .between(1, 254)
                .list()
                .ofMaxSize(254)
                .uniqueElements()
                .map(values -> new EnumTypeSchema(
                    "uint8",
                    values.stream().map(String::valueOf).collect(Collectors.toList())
                ))
        );
    }

    private static Arbitrary<SetSchema> setTypeSchema()
    {
        return Arbitraries.oneOf(
            Arbitraries.integers().between(0, 7).set()
                .ofMaxSize(8)
                .map(choices -> new SetSchema("uint8", choices)),
            Arbitraries.integers().between(0, 15).set()
                .ofMaxSize(16)
                .map(choices -> new SetSchema("uint16", choices)),
            Arbitraries.integers().between(0, 31).set()
                .ofMaxSize(32)
                .map(choices -> new SetSchema("uint32", choices)),
            Arbitraries.integers().between(0, 63).set()
                .ofMaxSize(64)
                .map(choices -> new SetSchema("uint64", choices))
        );
    }

    private static Arbitrary<TypeSchema> compositeTypeSchema(final int depth)
    {
        return withDuplicates(2, typeSchema(depth - 1).list().ofMinSize(1).ofMaxSize(3))
            .map(CompositeTypeSchema::new);
    }

    private static Arbitrary<TypeSchema> typeSchema(final int depth)
    {
        if (depth == 1)
        {
            return Arbitraries.oneOf(
                encodedDataTypeSchema(),
                enumTypeSchema(),
                setTypeSchema()
            );
        }
        else
        {
            return Arbitraries.oneOf(
                compositeTypeSchema(depth),
                encodedDataTypeSchema(),
                enumTypeSchema(),
                setTypeSchema()
            );
        }
    }

    private static Arbitrary<FieldSchema> addedField()
    {
        return Combinators.combine(
            typeSchema(MAX_COMPOSITE_DEPTH),
            Arbitraries.of(Encoding.Presence.OPTIONAL),
            Arbitraries.of((short)1, (short)2)
        ).as(FieldSchema::new);
    }

    private static Arbitrary<FieldSchema> originalField()
    {
        return Combinators.combine(
            typeSchema(MAX_COMPOSITE_DEPTH),
            presence(),
            Arbitraries.of((short)0)
        ).as(FieldSchema::new);
    }

    private static Arbitrary<FieldSchema> skewedFieldDistribution()
    {
        final Arbitrary<FieldSchema> originalField = originalField();
        final Arbitrary<FieldSchema> addedField = addedField();

        return Arbitraries.oneOf(
            originalField,
            originalField,
            originalField,
            addedField
        );
    }

    private static Arbitrary<GroupSchema> groupSchema(final int depth)
    {
        final Arbitrary<List<GroupSchema>> subGroups = depth == 1 ?
            Arbitraries.of(0).map(ignored -> new ArrayList<>()) :
            groupSchema(depth - 1).list().ofMaxSize(3);

        return Combinators.combine(
            withDuplicates(
                2,
                skewedFieldDistribution().list().ofMaxSize(5)
            ),
            subGroups,
            varDataSchema(Arbitraries.of((short)0)).list().ofMaxSize(3)
        ).as(GroupSchema::new);
    }

    private static Arbitrary<Encoding.Presence> presence()
    {
        return Arbitraries.of(
            Encoding.Presence.REQUIRED,
            Encoding.Presence.OPTIONAL
        );
    }

    private static Arbitrary<VarDataSchema> varDataSchema(final Arbitrary<Short> sinceVersion)
    {
        return Combinators.combine(
            Arbitraries.of(VarDataSchema.Encoding.values()),
            Arbitraries.of(
                PrimitiveType.UINT8,
                PrimitiveType.UINT16,
                PrimitiveType.UINT32
            ),
            sinceVersion
        ).as(VarDataSchema::new);
    }

    private static Arbitrary<VarDataSchema> varDataSchema()
    {
        return varDataSchema(
            Arbitraries.of(
                (short)0,
                (short)0,
                (short)0,
                (short)1,
                (short)2
            )
        );
    }

    public static Arbitrary<MessageSchema> messageSchema()
    {
        return Combinators.combine(
            withDuplicates(
                3,
                skewedFieldDistribution().list().ofMaxSize(10)
            ),
            groupSchema(MAX_GROUP_DEPTH).list().ofMaxSize(3),
            varDataSchema().list().ofMaxSize(3)
        ).as(MessageSchema::new);
    }

    public interface Encoder
    {
        void encode(MutableDirectBuffer buffer, int offset, MutableInteger limit);
    }

    private static Encoder combineEncoders(final Collection<Encoder> encoders)
    {
        return (buffer, offset, limit) ->
        {
            for (final Encoder encoder : encoders)
            {
                encoder.encode(buffer, offset, limit);
            }
        };
    }

    private static Arbitrary<Encoder> combineArbitraryEncoders(final List<Arbitrary<Encoder>> encoders)
    {
        if (encoders.isEmpty())
        {
            return Arbitraries.of(emptyEncoder());
        }
        else
        {
            return Combinators.combine(encoders).as(SbeArbitraries::combineEncoders);
        }
    }

    public static CharacterArbitrary chars(final CharGenerationMode mode)
    {
        switch (mode)
        {
            case UNRESTRICTED:
                return Arbitraries.chars();
            case JSON_PRINTER_COMPATIBLE:
                return Arbitraries.chars().alpha();
            default:
                throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }

    private static Arbitrary<Encoder> encodedTypeEncoder(
        final Encoding encoding,
        final CharGenerationMode charGenerationMode)
    {
        final PrimitiveValue minValue = encoding.applicableMinValue();
        final PrimitiveValue maxValue = encoding.applicableMaxValue();

        switch (encoding.primitiveType())
        {
            case CHAR:
                assert minValue.longValue() <= maxValue.longValue();
                return chars(charGenerationMode).map(c ->
                    (buffer, offset, limit) -> buffer.putChar(offset, c, encoding.byteOrder()));

            case UINT8:
            case INT8:
                assert (short)minValue.longValue() <= (short)maxValue.longValue();
                return Arbitraries.shorts()
                    .between((short)minValue.longValue(), (short)maxValue.longValue())
                    .map(b -> (buffer, offset, limit) -> buffer.putByte(offset, (byte)(short)b));

            case UINT16:
            case INT16:
                assert (int)minValue.longValue() <= (int)maxValue.longValue();
                return Arbitraries.integers()
                    .between((int)minValue.longValue(), (int)maxValue.longValue())
                    .map(s -> (buffer, offset, limit) -> buffer.putShort(offset, (short)(int)s, encoding.byteOrder()));

            case UINT32:
            case INT32:
                assert minValue.longValue() <= maxValue.longValue();
                return Arbitraries.longs()
                    .between(minValue.longValue(), maxValue.longValue())
                    .map(i -> (buffer, offset, limit) -> buffer.putInt(offset, (int)(long)i, encoding.byteOrder()));

            case UINT64:
                return Arbitraries.longs()
                    .map(l -> (buffer, offset, limit) ->
                    {
                        final long nullValue = encoding.applicableNullValue().longValue();
                        final long nonNullValue = l == nullValue ? minValue.longValue() : l;
                        buffer.putLong(offset, nonNullValue, encoding.byteOrder());
                    });

            case INT64:
                assert minValue.longValue() <= maxValue.longValue();
                return Arbitraries.longs()
                    .between(minValue.longValue(), maxValue.longValue())
                    .map(l -> (buffer, offset, limit) -> buffer.putLong(offset, l, encoding.byteOrder()));

            case FLOAT:
                return Arbitraries.floats()
                    .map(f -> (buffer, offset, limit) -> buffer.putFloat(offset, f, encoding.byteOrder()));

            case DOUBLE:
                return Arbitraries.doubles()
                    .map(d -> (buffer, offset, limit) -> buffer.putDouble(offset, d, encoding.byteOrder()));

            default:
                throw new IllegalArgumentException("Unsupported type: " + encoding.primitiveType());
        }
    }

    private static Arbitrary<Encoder> encodedTypeEncoder(
        final int offset,
        final Token typeToken,
        final CharGenerationMode charGenerationMode)
    {
        final Encoding encoding = typeToken.encoding();
        final Arbitrary<Encoder> arbEncoder = encodedTypeEncoder(encoding, charGenerationMode);

        if (typeToken.arrayLength() == 1)
        {
            return arbEncoder.map(encoder -> (buffer, bufferOffset, limit) ->
                encoder.encode(buffer, bufferOffset + offset, limit));
        }
        else
        {
            return arbEncoder.list().ofSize(typeToken.arrayLength())
                .map(encoders -> (buffer, bufferOffset, limit) ->
                {
                    for (int i = 0; i < typeToken.arrayLength(); i++)
                    {
                        final int elementOffset = bufferOffset + offset + i * encoding.primitiveType().size();
                        encoders.get(i).encode(buffer, elementOffset, limit);
                    }
                });
        }
    }

    private static Encoder emptyEncoder()
    {
        return (buffer, offset, limit) ->
        {
        };
    }

    private static Encoder integerValueEncoder(final Encoding encoding, final long value)
    {
        final PrimitiveType type = encoding.primitiveType();
        switch (type)
        {
            case CHAR:
            case UINT8:
            case INT8:
                return (buffer, offset, limit) -> buffer.putByte(offset, (byte)value);

            case UINT16:
            case INT16:
                return (buffer, offset, limit) -> buffer.putShort(offset, (short)value, encoding.byteOrder());

            case UINT32:
            case INT32:
                return (buffer, offset, limit) -> buffer.putInt(offset, (int)value, encoding.byteOrder());

            case UINT64:
            case INT64:
                return (buffer, offset, limit) -> buffer.putLong(offset, value, encoding.byteOrder());

            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static Arbitrary<Encoder> enumEncoder(
        final int offset,
        final List<Token> tokens,
        final Token typeToken,
        final MutableInteger cursor,
        final int endIdxInclusive)
    {
        cursor.increment();

        final List<Encoder> encoders = new ArrayList<>();
        for (; cursor.get() <= endIdxInclusive; cursor.increment())
        {
            final Token token = tokens.get(cursor.get());

            if (VALID_VALUE != token.signal())
            {
                throw new IllegalArgumentException("Expected VALID_VALUE token");
            }

            final Encoding encoding = token.encoding();
            final Encoder caseEncoder = integerValueEncoder(encoding, encoding.constValue().longValue());
            encoders.add(caseEncoder);
        }

        if (encoders.isEmpty())
        {
            final Encoder nullEncoder = integerValueEncoder(
                typeToken.encoding(),
                typeToken.encoding().nullValue().longValue());
            encoders.add(nullEncoder);
        }

        return Arbitraries.of(encoders).map(encoder ->
            (buffer, bufferOffset, limit) -> encoder.encode(buffer, bufferOffset + offset, limit));
    }

    private static Encoder choiceEncoder(final Encoding encoding)
    {
        final long choiceBitIdx = encoding.constValue().longValue();
        final PrimitiveType type = encoding.primitiveType();
        switch (type)
        {
            case UINT8:
            case INT8:
                return (buffer, offset, limit) ->
                {
                    buffer.checkLimit(offset + BitUtil.SIZE_OF_BYTE);
                    final byte oldValue = buffer.getByte(offset);
                    final byte newValue = (byte)(oldValue | (1 << choiceBitIdx));
                    buffer.putByte(offset, newValue);
                };

            case UINT16:
            case INT16:
                return (buffer, offset, limit) ->
                {
                    buffer.checkLimit(offset + BitUtil.SIZE_OF_SHORT);
                    final short oldValue = buffer.getShort(offset, encoding.byteOrder());
                    final short newValue = (short)(oldValue | (1 << choiceBitIdx));
                    buffer.putShort(offset, newValue, encoding.byteOrder());
                };

            case UINT32:
            case INT32:
                return (buffer, offset, limit) ->
                {
                    buffer.checkLimit(offset + BitUtil.SIZE_OF_INT);
                    final int oldValue = buffer.getInt(offset, encoding.byteOrder());
                    final int newValue = oldValue | (1 << choiceBitIdx);
                    buffer.putInt(offset, newValue, encoding.byteOrder());
                };

            case UINT64:
            case INT64:
                return (buffer, offset, limit) ->
                {
                    buffer.checkLimit(offset + BitUtil.SIZE_OF_LONG);
                    final long oldValue = buffer.getLong(offset, encoding.byteOrder());
                    final long newValue = oldValue | (1L << choiceBitIdx);
                    buffer.putLong(offset, newValue, encoding.byteOrder());
                };

            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static Arbitrary<Encoder> bitSetEncoder(
        final int offset,
        final List<Token> tokens,
        final MutableInteger cursor,
        final int endIdxInclusive)
    {
        cursor.increment();

        final List<Encoder> encoders = new ArrayList<>();
        for (; cursor.get() <= endIdxInclusive; cursor.increment())
        {
            final Token token = tokens.get(cursor.get());

            if (CHOICE != token.signal())
            {
                throw new IllegalArgumentException("Expected CHOICE token");
            }

            final Encoding encoding = token.encoding();
            final Encoder choiceEncoder = choiceEncoder(encoding);
            encoders.add(choiceEncoder);
        }

        if (encoders.isEmpty())
        {
            return Arbitraries.of(emptyEncoder());
        }

        return Arbitraries.subsetOf(encoders)
            .map(SbeArbitraries::combineEncoders)
            .map(encoder -> (buffer, bufferOffset, limit) -> encoder.encode(buffer, bufferOffset + offset, limit));
    }

    private static Arbitrary<Encoder> fieldsEncoder(
        final List<Token> tokens,
        final MutableInteger cursor,
        final int endIdxInclusive,
        final boolean expectFields,
        final CharGenerationMode charGenerationMode)
    {
        final List<Arbitrary<Encoder>> encoders = new ArrayList<>();
        while (cursor.get() <= endIdxInclusive)
        {
            final Token memberToken = tokens.get(cursor.get());
            final int nextFieldIdx = cursor.get() + memberToken.componentTokenCount();

            Token typeToken = memberToken;
            int endFieldTokenCount = 0;

            if (BEGIN_FIELD == memberToken.signal())
            {
                cursor.increment();
                typeToken = tokens.get(cursor.get());
                endFieldTokenCount = 1;
            }
            else if (expectFields)
            {
                break;
            }

            final int offset = typeToken.offset();

            if (!memberToken.isConstantEncoding())
            {
                switch (typeToken.signal())
                {
                    case BEGIN_COMPOSITE:
                        cursor.increment();
                        final int endCompositeTokenCount = 1;
                        final int lastMemberIdx = nextFieldIdx - endCompositeTokenCount - endFieldTokenCount - 1;
                        final Arbitrary<Encoder> encoder = fieldsEncoder(
                            tokens, cursor, lastMemberIdx, false, charGenerationMode);
                        final Arbitrary<Encoder> positionedEncoder = encoder.map(e ->
                            (buffer, bufferOffset, limit) -> e.encode(buffer, bufferOffset + offset, limit));
                        encoders.add(positionedEncoder);
                        break;

                    case BEGIN_ENUM:
                        final int endEnumTokenCount = 1;
                        final int lastValidValueIdx = nextFieldIdx - endFieldTokenCount - endEnumTokenCount - 1;
                        encoders.add(enumEncoder(offset, tokens, typeToken, cursor, lastValidValueIdx));
                        break;

                    case BEGIN_SET:
                        final int endSetTokenCount = 1;
                        final int lastChoiceIdx = nextFieldIdx - endFieldTokenCount - endSetTokenCount - 1;
                        encoders.add(bitSetEncoder(offset, tokens, cursor, lastChoiceIdx));
                        break;

                    case ENCODING:
                        encoders.add(encodedTypeEncoder(offset, typeToken, charGenerationMode));
                        break;

                    default:
                        break;
                }
            }

            cursor.set(nextFieldIdx);
        }

        return combineArbitraryEncoders(encoders);
    }


    private static Arbitrary<Encoder> groupsEncoder(
        final List<Token> tokens,
        final MutableInteger cursor,
        final int endIdxInclusive,
        final CharGenerationMode charGenerationMode)
    {
        final List<Arbitrary<Encoder>> encoders = new ArrayList<>();

        while (cursor.get() <= endIdxInclusive)
        {
            final Token token = tokens.get(cursor.get());
            if (BEGIN_GROUP != token.signal())
            {
                break;
            }
            final int nextFieldIdx = cursor.get() + token.componentTokenCount();

            cursor.increment(); // consume BEGIN_GROUP
            cursor.increment(); // consume BEGIN_COMPOSITE
            final Token blockLengthToken = tokens.get(cursor.get());
            final int blockLength = token.encodedLength();
            final Encoder blockLengthEncoder = integerValueEncoder(blockLengthToken.encoding(), blockLength);
            cursor.increment(); // consume ENCODED
            final Token numInGroupToken = tokens.get(cursor.get());
            cursor.increment(); // consume ENCODED
            cursor.increment(); // consume END_COMPOSITE
            final int headerLength = blockLengthToken.encodedLength() + numInGroupToken.encodedLength();


            final Arbitrary<Encoder> groupElement = Combinators.combine(
                fieldsEncoder(tokens, cursor, nextFieldIdx - 1, true, charGenerationMode),
                groupsEncoder(tokens, cursor, nextFieldIdx - 1, charGenerationMode),
                varDataEncoder(tokens, cursor, nextFieldIdx - 1, charGenerationMode)
            ).as((fieldsEncoder, groupsEncoder, varDataEncoder) ->
                (buffer, ignored, limit) ->
                {
                    final int offset = limit.get();
                    fieldsEncoder.encode(buffer, offset, null);
                    limit.set(offset + blockLength);
                    groupsEncoder.encode(buffer, NULL_VALUE, limit);
                    varDataEncoder.encode(buffer, NULL_VALUE, limit);
                });

            final Arbitrary<Encoder> repeatingGroupEncoder = groupElement.list()
                .ofMaxSize(10)
                .map(elements -> (buffer, ignored, limit) ->
                {
                    final int offset = limit.get();
                    limit.set(offset + headerLength);
                    blockLengthEncoder.encode(buffer, offset, null);
                    integerValueEncoder(numInGroupToken.encoding(), elements.size())
                        .encode(buffer, offset + blockLengthToken.encodedLength(), null);

                    for (final Encoder element : elements)
                    {
                        element.encode(buffer, NULL_VALUE, limit);
                    }
                });

            encoders.add(repeatingGroupEncoder);

            cursor.set(nextFieldIdx);
        }

        return combineArbitraryEncoders(encoders);
    }

    private static Arbitrary<Encoder> varDataEncoder(
        final List<Token> tokens,
        final MutableInteger cursor,
        final int endIdxInclusive,
        final CharGenerationMode charGenerationMode)
    {
        final List<Arbitrary<Encoder>> encoders = new ArrayList<>();

        while (cursor.get() <= endIdxInclusive)
        {
            final Token token = tokens.get(cursor.get());
            if (BEGIN_VAR_DATA != token.signal())
            {
                break;
            }
            final int nextFieldIdx = cursor.get() + token.componentTokenCount();

            cursor.increment(); // BEGIN_COMPOSITE
            cursor.increment(); // ENCODED
            final Token lengthToken = tokens.get(cursor.get());
            cursor.increment(); // ENCODED
            final Token varDataToken = tokens.get(cursor.get());
            cursor.increment(); // END_COMPOSITE

            final String characterEncoding = varDataToken.encoding().characterEncoding();
            final Arbitrary<Byte> arbitraryByte = null == characterEncoding ?
                Arbitraries.bytes() :
                chars(charGenerationMode).map(c -> (byte)c.charValue());
            encoders.add(arbitraryByte.list().map(bytes ->
                (buffer, ignored, limit) ->
                {
                    final int offset = limit.get();
                    final int elementLength = varDataToken.encoding().primitiveType().size();
                    limit.set(offset + lengthToken.encodedLength() + bytes.size() * elementLength);
                    integerValueEncoder(lengthToken.encoding(), bytes.size())
                        .encode(buffer, offset, null);
                    for (int i = 0; i < bytes.size(); i++)
                    {
                        final int dataOffset = offset + lengthToken.encodedLength() + i * elementLength;
                        integerValueEncoder(varDataToken.encoding(), bytes.get(i))
                            .encode(buffer, dataOffset, null);
                    }
                }));

            cursor.set(nextFieldIdx);
        }

        return combineArbitraryEncoders(encoders);
    }

    public static Arbitrary<Encoder> messageValueEncoder(
        final Ir ir,
        final short messageId,
        final CharGenerationMode charGenerationMode)
    {
        final List<Token> tokens = ir.getMessage(messageId);
        final MutableInteger cursor = new MutableInteger(1);

        final Token token = tokens.get(0);
        if (BEGIN_MESSAGE != token.signal())
        {
            throw new IllegalArgumentException("Expected BEGIN_MESSAGE token");
        }

        final Arbitrary<Encoder> fieldsEncoder = fieldsEncoder(
            tokens, cursor, tokens.size() - 1, true, charGenerationMode);
        final Arbitrary<Encoder> groupsEncoder = groupsEncoder(
            tokens, cursor, tokens.size() - 1, charGenerationMode);
        final Arbitrary<Encoder> varDataEncoder = varDataEncoder(
            tokens, cursor, tokens.size() - 1, charGenerationMode);
        return Combinators.combine(fieldsEncoder, groupsEncoder, varDataEncoder)
            .as((fields, groups, varData) -> (buffer, offset, limit) ->
            {
                final int blockLength = token.encodedLength();
                buffer.putShort(0, (short)blockLength, ir.byteOrder());
                buffer.putShort(2, messageId, ir.byteOrder());
                buffer.putShort(4, (short)ir.id(), ir.byteOrder());
                buffer.putShort(6, (short)ir.version(), ir.byteOrder());
                final int headerLength = 8;
                fields.encode(buffer, offset + headerLength, null);
                limit.set(offset + headerLength + blockLength);
                groups.encode(buffer, NULL_VALUE, limit);
                varData.encode(buffer, NULL_VALUE, limit);
            });
    }

    public static final class EncodedMessage
    {
        private final String schema;
        private final Ir ir;
        private final ExpandableArrayBuffer buffer;
        private final int length;

        private EncodedMessage(
            final String schema,
            final Ir ir,
            final ExpandableArrayBuffer buffer,
            final int length)
        {
            this.schema = schema;
            this.ir = ir;
            this.buffer = buffer;
            this.length = length;
        }

        public String schema()
        {
            return schema;
        }

        public Ir ir()
        {
            return ir;
        }

        public ExpandableArrayBuffer buffer()
        {
            return buffer;
        }

        public int length()
        {
            return length;
        }
    }

    public static Arbitrary<EncodedMessage> encodedMessage(final CharGenerationMode mode)
    {
        return SbeArbitraries.messageSchema().flatMap(testSchema ->
        {
            final String xml = TestXmlSchemaWriter.writeString(testSchema);
            try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
            {
                final ParserOptions options = ParserOptions.builder()
                    .suppressOutput(false)
                    .warningsFatal(true)
                    .stopOnError(true)
                    .build();
                final uk.co.real_logic.sbe.xml.MessageSchema parsedSchema = parse(in, options);
                final Ir ir = new IrGenerator().generate(parsedSchema);
                return SbeArbitraries.messageValueEncoder(ir, testSchema.templateId(), mode)
                    .map(encoder ->
                    {
                        final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
                        final MutableInteger limit = new MutableInteger();
                        encoder.encode(buffer, 0, limit);
                        return new EncodedMessage(xml, ir, buffer, limit.get());
                    });
            }
            catch (final Exception e)
            {
                throw new AssertionError(
                    "Failed to generate encoded value for schema.\n\n" +
                    "SCHEMA:\n" + xml,
                    e);
            }
        }).withoutEdgeCases();
    }
}
