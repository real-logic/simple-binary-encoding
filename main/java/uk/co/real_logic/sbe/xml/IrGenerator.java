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
import uk.co.real_logic.sbe.ir.Settings;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold the state while generating the {@link IntermediateRepresentation}.
 */
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
        CompositeType type = schema.getMessageHeader();
        add(type, 0);

        return tokenList;
    }

    private void addMessageSignal(final Message msg, final Signal signal)
    {
        tokenList.add(new Token(signal, msg.getName(), msg.getId()));
    }

    private void addTypeSignal(final Type type, final Signal signal)
    {
        tokenList.add(new Token(signal, type.getName(), Token.INVALID_ID));
    }

    private void addFieldSignal(final Field field, final Signal signal)
    {
        tokenList.add(new Token(signal, field.getName(), field.getId()));
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
        int offset = currOffset;

        addTypeSignal(type, Signal.BEGIN_COMPOSITE);

        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt, offset);

            offset += edt.size();
        }

        addTypeSignal(type, Signal.END_COMPOSITE);
    }

    private void add(final EnumType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Settings.Builder builder = new Settings.Builder();

        if (type.getPresence() == Presence.OPTIONAL)
        {
            builder.nullVal(encodingType.nullVal());
        }

        tokenList.add(new Token(Signal.BEGIN_ENUM,
                                type.getName(),
                                Token.INVALID_ID,
                                encodingType,
                                encodingType.size(),
                                offset,
                                byteOrder,
                                builder.build()));

        for (final EnumType.ValidValue v : type.getValidValues())
        {
            add(v, encodingType);
        }

        tokenList.add(new Token(Signal.END_ENUM,
                                type.getName(),
                                Token.INVALID_ID,
                                encodingType,
                                encodingType.size(),
                                offset,
                                byteOrder,
                                builder.build()));
    }

    private void add(final EnumType.ValidValue value, final PrimitiveType encodingType)
    {
        tokenList.add(new Token(Signal.VALID_VALUE,
                                value.getName(),
                                Token.INVALID_ID,
                                encodingType,
                                0,
                                0,
                                byteOrder,
                                new Settings.Builder()
                                .constVal(value.getPrimitiveValue())
                                .build()));
    }

    private void add(final SetType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();

        tokenList.add(new Token(Signal.BEGIN_SET,
                                type.getName(),
                                Token.INVALID_ID,
                                encodingType,
                                encodingType.size(),
                                offset,
                                byteOrder,
                                new Settings()));

        for (final SetType.Choice choice : type.getChoices())
        {
            add(choice);
        }

        tokenList.add(new Token(Signal.END_SET,
                                type.getName(),
                                Token.INVALID_ID,
                                encodingType,
                                encodingType.size(),
                                offset,
                                byteOrder,
                                new Settings()));
    }

    private void add(final SetType.Choice value)
    {
        tokenList.add(new Token(Signal.CHOICE,
                                value.getName(),
                                Token.INVALID_ID,
                                new Settings.Builder()
                                .constVal(value.getPrimitiveValue())
                                .build()));
    }

    private void add(final EncodedDataType type, final int offset)
    {
        Settings.Builder builder = new Settings.Builder();

        switch (type.getPresence())
        {
            case REQUIRED:
                builder.minVal(type.getMinValue())
                    .maxVal(type.getMaxValue());
                break;

            case OPTIONAL:
                builder.minVal(type.getMinValue())
                    .maxVal(type.getMaxValue())
                    .nullVal(type.getNullValue());
                break;

            case CONSTANT:
                builder.constVal(type.getConstValue());
                break;
        }

        tokenList.add(new Token(Signal.ENCODING,
                                type.getName(),
                                Token.INVALID_ID,
                                type.getPrimitiveType(),
                                type.size(),
                                offset,
                                byteOrder,
                                builder.build()));
    }
}
