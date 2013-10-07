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
import uk.co.real_logic.sbe.ir.Options;
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
            new IntermediateRepresentation(schema.getPackage(), generateForHeader(schema));

        for (final Message message : schema.getMessages())
        {
            final long msgId = message.getId();
            ir.addMessage(msgId, generateForMessage(schema, msgId));
        }

        return ir;
    }

    private List<Token> generateForMessage(final MessageSchema schema, final long messageId)
    {
        tokenList.clear();
        byteOrder = schema.getByteOrder();

        final Message msg = schema.getMessage(messageId);

        addMessageSignal(msg, Signal.BEGIN_MESSAGE);
        addAllFields(msg.getFields());
        addMessageSignal(msg, Signal.END_MESSAGE);

        return tokenList;
    }

    private List<Token> generateForHeader(final MessageSchema schema)
    {
        tokenList.clear();

        byteOrder = schema.getByteOrder();
        add(schema.getMessageHeader(), 0);

        return tokenList;
    }

    private void addMessageSignal(final Message msg, final Signal signal)
    {
        Token token = new Token.Builder()
            .signal(signal)
            .name(msg.getName())
            .schemaId(msg.getId())
            .build();

        tokenList.add(token);
    }

    private void addFieldSignal(final Field field, final Signal signal)
    {
        Token token = new Token.Builder()
            .signal(signal)
            .name(field.getName())
            .schemaId(field.getId())
            .build();

        tokenList.add(token);
    }

    private void addAllFields(final List<Field> fieldList)
    {
        for (final Field field : fieldList)
        {
            final Type type = field.getType();

            if (type == null)
            {
                addFieldSignal(field, Signal.BEGIN_GROUP);
                add(field.getDimensionType(), 0);
                addAllFields(field.getGroupFields());
                addFieldSignal(field, Signal.END_GROUP);
            }
            else
            {
                addFieldSignal(field, Signal.BEGIN_FIELD);

                if (type instanceof EncodedDataType)
                {
                    add((EncodedDataType)type, field.getCalculatedOffset());
                }
                else if (type instanceof CompositeType)
                {
                    add((CompositeType)type, field.getCalculatedOffset());
                }
                else if (type instanceof EnumType)
                {
                    add((EnumType)type, field.getCalculatedOffset());
                }
                else if (type instanceof SetType)
                {
                    add((SetType)type, field.getCalculatedOffset());
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
            .name(type.getName());

        tokenList.add(builder.build());

        int offset = currOffset;
        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt, offset);
            offset += edt.size();
        }

        tokenList.add(builder.signal(Signal.END_COMPOSITE).build());
    }

    private void add(final EnumType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Options.Builder optsBuilder = new Options.Builder();

        if (type.getPresence() == Presence.OPTIONAL)
        {
            optsBuilder.nullVal(encodingType.nullVal());
        }

        Token.Builder builder = new Token.Builder()
            .signal(Signal.BEGIN_ENUM)
            .name(type.getName())
            .primitiveType(encodingType)
            .size(encodingType.size())
            .offset(offset)
            .byteOrder(byteOrder)
            .options(optsBuilder.build());

        tokenList.add(builder.build());

        for (final EnumType.ValidValue validValue : type.getValidValues())
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
            .name(value.getName())
            .primitiveType(encodingType)
            .byteOrder(byteOrder)
            .options(new Options.Builder()
                         .constVal(value.getPrimitiveValue())
                         .build());

        tokenList.add(builder.build());
    }

    private void add(final SetType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();

        Token.Builder builder = new Token.Builder()
            .signal(Signal.BEGIN_SET)
            .name(type.getName())
            .primitiveType(encodingType)
            .size(encodingType.size())
            .offset(offset)
            .byteOrder(byteOrder);

        tokenList.add(builder.build());

        for (final SetType.Choice choice : type.getChoices())
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
            .name(value.getName())
            .primitiveType(encodingType)
            .options(new Options.Builder()
                         .constVal(value.getPrimitiveValue())
                         .build())
            .build();

        tokenList.add(token);
    }

    private void add(final EncodedDataType type, final int offset)
    {
        Options.Builder optsBuilder = new Options.Builder();

        switch (type.getPresence())
        {
            case REQUIRED:
                optsBuilder.minVal(type.getMinValue())
                           .maxVal(type.getMaxValue());
                break;

            case OPTIONAL:
                optsBuilder.minVal(type.getMinValue())
                           .maxVal(type.getMaxValue())
                           .nullVal(type.getNullValue());
                break;

            case CONSTANT:
                optsBuilder.constVal(type.getConstValue());
                break;
        }

        Token token = new Token.Builder()
            .signal(Signal.ENCODING)
            .name(type.getName())
            .primitiveType(type.getPrimitiveType())
            .size(type.size())
            .offset(offset)
            .byteOrder(byteOrder)
            .options(optsBuilder.build())
            .build();

        tokenList.add(token);
    }
}
