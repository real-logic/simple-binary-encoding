/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** Class to hold the state while generating the {@link IntermediateRepresentation}. */
public class IrGenerator
{
    private final List<Token> tokenList = new ArrayList<>();
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    /**
     * Generate a complete {@link IntermediateRepresentation} for a given schema.
     *
     * @param schema from which the {@link IntermediateRepresentation} should be generated.
     * @return complete {@link IntermediateRepresentation} for a given schema.
     */
    public IntermediateRepresentation generate(final MessageSchema schema)
    {
        final IntermediateRepresentation ir =
            new IntermediateRepresentation(schema.packageName(), generateForHeader(schema));

        for (final Message message : schema.messages())
        {
            final long msgId = message.id();
            ir.addMessage(msgId, generateForMessage(schema, msgId));
        }

        return ir;
    }

    private List<Token> generateForMessage(final MessageSchema schema, final long messageId)
    {
        tokenList.clear();
        byteOrder = schema.byteOrder();

        final Message msg = schema.getMessage(messageId);

        addMessageSignal(msg, Signal.BEGIN_MESSAGE);
        addAllFields(msg.fields());
        addMessageSignal(msg, Signal.END_MESSAGE);

        return tokenList;
    }

    private List<Token> generateForHeader(final MessageSchema schema)
    {
        tokenList.clear();

        byteOrder = schema.byteOrder();
        add(schema.messageHeader(), 0);

        return tokenList;
    }

    private void addMessageSignal(final Message msg, final Signal signal)
    {
        Token token = new Token.Builder()
            .signal(signal)
            .name(msg.name())
            .size(msg.blockLength())
            .schemaId(msg.id())
            .build();

        tokenList.add(token);
    }

    private void addFieldSignal(final Field field, final Signal signal)
    {
        Token token = new Token.Builder()
            .signal(signal)
            .size(field.computedBlockLength())
            .name(field.name())
            .schemaId(field.id())
            .build();

        tokenList.add(token);
    }

    private void addAllFields(final List<Field> fieldList)
    {
        for (final Field field : fieldList)
        {
            final Type type = field.type();

            if (type == null)
            {
                addFieldSignal(field, Signal.BEGIN_GROUP);
                add(field.dimensionType(), 0);
                addAllFields(field.groupFields());
                addFieldSignal(field, Signal.END_GROUP);
            }
            else if (type instanceof CompositeType && field.isVariableLength())
            {
                addFieldSignal(field, Signal.BEGIN_VAR_DATA);
                add((CompositeType)type, field.computedOffset());
                addFieldSignal(field, Signal.END_VAR_DATA);
            }
            else
            {
                addFieldSignal(field, Signal.BEGIN_FIELD);

                if (type instanceof EncodedDataType)
                {
                    add((EncodedDataType)type, field.computedOffset());
                }
                else if (type instanceof CompositeType)
                {
                    add((CompositeType)type, field.computedOffset());
                }
                else if (type instanceof EnumType)
                {
                    add((EnumType)type, field.computedOffset());
                }
                else if (type instanceof SetType)
                {
                    add((SetType)type, field.computedOffset());
                }
                else
                {
                    throw new IllegalStateException("Unknown type: " + type);
                }

                addFieldSignal(field, Signal.END_FIELD);
            }
        }
    }

    private void add(final CompositeType type, final int currOffset)
    {
        Token.Builder builder = new Token.Builder()
            .signal(Signal.BEGIN_COMPOSITE)
            .name(type.name())
            .offset(currOffset)
            .size(type.size());

        tokenList.add(builder.build());

        int offset = 0;
        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt, offset);
            offset += edt.size();
        }

        tokenList.add(builder.signal(Signal.END_COMPOSITE).build());
    }

    private void add(final EnumType type, final int offset)
    {
        PrimitiveType encodingType = type.encodingType();
        Encoding.Builder encodingBuilder = new Encoding.Builder()
            .primitiveType(encodingType)
            .byteOrder(byteOrder);

        if (type.presence() == Presence.OPTIONAL)
        {
            encodingBuilder.nullVal(encodingType.nullVal());
        }

        Token.Builder builder = new Token.Builder()
            .signal(Signal.BEGIN_ENUM)
            .name(type.name())
            .size(encodingType.size())
            .offset(offset)
            .encoding(encodingBuilder.build());

        tokenList.add(builder.build());

        for (final EnumType.ValidValue validValue : type.validValues())
        {
            add(validValue, encodingType);
        }

        builder.signal(Signal.END_ENUM);

        tokenList.add(builder.build());
    }

    private void add(final EnumType.ValidValue value, final PrimitiveType encodingType)
    {
        Token.Builder builder = new Token.Builder()
            .signal(Signal.VALID_VALUE)
            .name(value.name())
            .encoding(new Encoding.Builder()
                          .byteOrder(byteOrder)
                          .primitiveType(encodingType)
                          .constVal(value.primitiveValue())
                          .build());

        tokenList.add(builder.build());
    }

    private void add(final SetType type, final int offset)
    {
        PrimitiveType encodingType = type.encodingType();

        Token.Builder builder = new Token.Builder()
            .signal(Signal.BEGIN_SET)
            .name(type.name())
            .size(encodingType.size())
            .offset(offset)
            .encoding(new Encoding.Builder()
                          .primitiveType(encodingType)
                          .build());

        tokenList.add(builder.build());

        for (final SetType.Choice choice : type.choices())
        {
            add(choice, encodingType);
        }

        builder.signal(Signal.END_SET);

        tokenList.add(builder.build());
    }

    private void add(final SetType.Choice value, final PrimitiveType encodingType)
    {
        Token token = new Token.Builder()
            .signal(Signal.CHOICE)
            .name(value.name())
            .encoding(new Encoding.Builder()
                          .constVal(value.primitiveValue())
                          .byteOrder(byteOrder)
                          .primitiveType(encodingType)
                          .build())
            .build();

        tokenList.add(token);
    }

    private void add(final EncodedDataType type, final int offset)
    {
        Encoding.Builder encodingBuilder = new Encoding.Builder()
            .primitiveType(type.primitiveType())
            .byteOrder(byteOrder);

        switch (type.presence())
        {
            case REQUIRED:
                encodingBuilder.minVal(type.minVal())
                               .maxVal(type.maxVal());
                break;

            case OPTIONAL:
                encodingBuilder.minVal(type.minVal())
                               .maxVal(type.maxVal())
                               .nullVal(type.nullVal());
                break;

            case CONSTANT:
                encodingBuilder.constVal(type.constVal());
                break;
        }

        Token token = new Token.Builder()
            .signal(Signal.ENCODING)
            .name(type.name())
            .size(type.size())
            .offset(offset)
            .encoding(encodingBuilder.build())
            .build();

        tokenList.add(token);
    }
}
