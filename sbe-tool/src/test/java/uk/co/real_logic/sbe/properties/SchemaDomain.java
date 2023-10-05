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

package uk.co.real_logic.sbe.properties;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import uk.co.real_logic.sbe.PrimitiveType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SchemaDomain
{
    private static final int MAX_COMPOSITE_DEPTH = 3;
    private static final int MAX_GROUP_DEPTH = 3;

    private SchemaDomain()
    {
    }

    static final class EncodedDataTypeSchema implements TypeSchema
    {
        private final PrimitiveType primitiveType;
        private final boolean isEmbedded;

        private EncodedDataTypeSchema(
            final PrimitiveType primitiveType,
            final boolean isEmbedded
        )
        {
            this.primitiveType = primitiveType;
            this.isEmbedded = isEmbedded;
        }

        public PrimitiveType primitiveType()
        {
            return primitiveType;
        }

        @Override
        public boolean isEmbedded()
        {
            return isEmbedded;
        }

        @Override
        public void accept(final TypeSchemaVisitor visitor)
        {
            visitor.onEncoded(this);
        }

        static Arbitrary<EncodedDataTypeSchema> arbitrary()
        {
            return Combinators.combine(
                Arbitraries.of(PrimitiveType.values()),
                Arbitraries.of(true, false)
            ).as(EncodedDataTypeSchema::new);
        }
    }

    static final class CompositeTypeSchema implements TypeSchema
    {
        private final List<TypeSchema> fields;

        private CompositeTypeSchema(final List<TypeSchema> fields)
        {
            this.fields = fields;
        }

        public List<TypeSchema> fields()
        {
            return fields;
        }

        static Arbitrary<TypeSchema> arbitrary(final int depth)
        {
            return TypeSchema.arbitrary(depth - 1)
                .list()
                .ofMinSize(1)
                .ofMaxSize(3)
                .injectDuplicates(0.2)
                .map(CompositeTypeSchema::new);
        }

        @Override
        public void accept(final TypeSchemaVisitor visitor)
        {
            visitor.onComposite(this);
        }
    }

    static final class EnumTypeSchema implements TypeSchema
    {
        private final String encodingType;
        private final List<String> validValues;

        private EnumTypeSchema(
            final String encodingType,
            final List<String> validValues)
        {
            this.encodingType = encodingType;
            this.validValues = validValues;
        }

        public String encodingType()
        {
            return encodingType;
        }

        public List<String> validValues()
        {
            return validValues;
        }

        static Arbitrary<EnumTypeSchema> arbitrary()
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

        @Override
        public void accept(final TypeSchemaVisitor visitor)
        {
            visitor.onEnum(this);
        }
    }

    static final class SetSchema implements TypeSchema
    {
        private final String encodingType;
        private final int choiceCount;

        private SetSchema(
            final String encodingType,
            final int choiceCount)
        {
            this.choiceCount = choiceCount;
            this.encodingType = encodingType;
        }

        public String encodingType()
        {
            return encodingType;
        }

        public int choiceCount()
        {
            return choiceCount;
        }

        @Override
        public void accept(final TypeSchemaVisitor visitor)
        {
            visitor.onSet(this);
        }

        static Arbitrary<SetSchema> arbitrary()
        {
            return Combinators.combine(
                Arbitraries.of(
                    "uint8",
                    "uint16",
                    "uint32",
                    "uint64"
                ),
                Arbitraries.integers().between(1, 4)
            ).as(SetSchema::new);
        }
    }

    interface TypeSchema
    {
        static Arbitrary<TypeSchema> arbitrary(final int depth)
        {
            if (depth == 1)
            {
                return Arbitraries.oneOf(
                    EncodedDataTypeSchema.arbitrary(),
                    EnumTypeSchema.arbitrary(),
                    SetSchema.arbitrary()
                );
            }
            else
            {
                return Arbitraries.oneOf(
                    CompositeTypeSchema.arbitrary(depth),
                    EncodedDataTypeSchema.arbitrary(),
                    EnumTypeSchema.arbitrary(),
                    SetSchema.arbitrary()
                );
            }
        }

        default boolean isEmbedded()
        {
            return false;
        }

        void accept(TypeSchemaVisitor visitor);
    }

    interface TypeSchemaVisitor
    {
        void onEncoded(EncodedDataTypeSchema type);

        void onComposite(CompositeTypeSchema type);

        void onEnum(EnumTypeSchema type);

        void onSet(SetSchema type);
    }

    static final class VarDataSchema
    {
        private final Encoding encoding;

        VarDataSchema(final Encoding encoding)
        {
            this.encoding = encoding;
        }

        public Encoding encoding()
        {
            return encoding;
        }

        static Arbitrary<VarDataSchema> arbitrary()
        {
            return Arbitraries.of(Encoding.values())
                .map(VarDataSchema::new);
        }

        enum Encoding
        {
            ASCII,
            BYTES
        }
    }

    static final class GroupSchema
    {
        private final List<TypeSchema> blockFields;
        private final List<GroupSchema> groups;
        private final List<VarDataSchema> varData;

        GroupSchema(
            final List<TypeSchema> blockFields,
            final List<GroupSchema> groups,
            final List<VarDataSchema> varData)
        {
            this.blockFields = blockFields;
            this.groups = groups;
            this.varData = varData;
        }

        public List<TypeSchema> blockFields()
        {
            return blockFields;
        }

        public List<GroupSchema> groups()
        {
            return groups;
        }

        public List<VarDataSchema> varData()
        {
            return varData;
        }

        static Arbitrary<GroupSchema> arbitrary(final int depth)
        {
            final Arbitrary<List<GroupSchema>> subGroups = depth == 1 ?
                Arbitraries.of(0).map(ignored -> new ArrayList<>()) :
                arbitrary(depth - 1).list().ofMaxSize(3);

            return Combinators.combine(
                TypeSchema.arbitrary(MAX_COMPOSITE_DEPTH).list().ofMaxSize(5),
                subGroups,
                VarDataSchema.arbitrary().list().ofMaxSize(3)
            ).as(GroupSchema::new);
        }
    }

    static final class MessageSchema
    {
        private final List<TypeSchema> blockFields;
        private final List<GroupSchema> groups;
        private final List<VarDataSchema> varData;

        MessageSchema(
            final List<TypeSchema> blockFields,
            final List<GroupSchema> groups,
            final List<VarDataSchema> varData
        )
        {
            this.blockFields = blockFields;
            this.groups = groups;
            this.varData = varData;
        }

        public List<TypeSchema> blockFields()
        {
            return blockFields;
        }

        public List<GroupSchema> groups()
        {
            return groups;
        }

        public List<VarDataSchema> varData()
        {
            return varData;
        }

        static Arbitrary<MessageSchema> arbitrary()
        {
            return Combinators.combine(
                TypeSchema.arbitrary(MAX_COMPOSITE_DEPTH).list().ofMaxSize(10),
                GroupSchema.arbitrary(MAX_GROUP_DEPTH).list().ofMaxSize(3),
                VarDataSchema.arbitrary().list().ofMaxSize(3)
            ).as(MessageSchema::new);
        }
    }
}
