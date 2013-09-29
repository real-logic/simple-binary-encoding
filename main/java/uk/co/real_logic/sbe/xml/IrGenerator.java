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
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.ir.Metadata;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold all the state while generating the {@link Token} list.
 * <p>
 * Usage:
 * <code>
 *     <pre>
 *    irg = new IrGenerator();
 *    list = irg.generateForMessage(message);
 *     </pre>
 * </code>
 */
public class IrGenerator
{
    private final List<Token> tokenList = new ArrayList<>();
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    public List<Token> generateForMessage(final MessageSchema schema, final long messageId)
    {
        final Message msg = schema.getMessage(messageId);
        if (null == msg)
        {
            throw new IllegalArgumentException("No message for id=" + messageId);
        }

        tokenList.clear();
        byteOrder = schema.getByteOrder();

        addMessageSignal(msg, Token.Signal.BEGIN_MESSAGE);

        addAllFields(msg.getFields());

        addMessageSignal(msg, Token.Signal.END_MESSAGE);

        return tokenList;
    }

    public List<Token> generateForHeader(final MessageSchema schema)
    {
        tokenList.clear();

        byteOrder = schema.getByteOrder();
        CompositeType type = schema.getMessageHeader();
        add(type, 0);

        return tokenList;
    }

    private void addMessageSignal(final Message msg, final Token.Signal signal)
    {
        Metadata.Builder builder = new Metadata.Builder(msg.getName());

        builder.schemaId(msg.getId());
        builder.id(0);
        builder.flag(signal);
        tokenList.add(new Token(builder.build()));
    }

    private void addTypeSignal(final Type type, final Token.Signal signal)
    {
        Metadata.Builder builder = new Metadata.Builder(type.getName());

        builder.flag(signal);
        tokenList.add(new Token(builder.build()));
    }

    private void addFieldSignal(final Message.Field field, final Token.Signal signal)
    {
        Metadata.Builder builder = new Metadata.Builder(field.getName());

        if (field.getEntryCountField() != null)
        {
            builder.refId(field.getEntryCountField().getIrId());
        }
        else if (field.getLengthField() != null)
        {
            builder.refId(field.getLengthField().getIrId());
        }
        else if (field.getGroupField() != null)
        {
            builder.refId(field.getGroupField().getIrId());
        }
        else if (field.getDataField() != null)
        {
            builder.refId(field.getDataField().getIrId());
        }

        builder.schemaId(field.getId());
        builder.id(field.getIrId());
        builder.flag(signal);
        tokenList.add(new Token(builder.build()));
    }

    private void addAllFields(final List<Message.Field> fieldList)
    {
        for (final Message.Field field : fieldList)
        {
            final Type type = field.getType();

            if (type == null)
            {
                addFieldSignal(field, Token.Signal.BEGIN_GROUP);
                addAllFields(field.getGroupFields());
                addFieldSignal(field, Token.Signal.END_GROUP);
            }
            else
            {
                addFieldSignal(field, Token.Signal.BEGIN_FIELD);

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

                addFieldSignal(field, Token.Signal.END_FIELD);
            }
        }
    }

    private void add(final CompositeType type, final int currOffset)
    {
        int offset = currOffset;

        addTypeSignal(type, Token.Signal.BEGIN_COMPOSITE);

        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt, offset);

            offset += edt.size(); // bump offset for next type in list
        }

        addTypeSignal(type, Token.Signal.END_COMPOSITE);
    }

    private void add(final EnumType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Metadata.Builder builder = new Metadata.Builder(encodingType.primitiveName());

        addTypeSignal(type, Token.Signal.BEGIN_ENUM);

        if (type.getPresence() == Presence.OPTIONAL)
        {
            builder.nullValue(encodingType.nullValue());
        }

        tokenList.add(new Token(encodingType, encodingType.size(), offset, byteOrder, builder.build()));

        for (final EnumType.ValidValue v : type.getValidValues())
        {
            add(v);
        }

        addTypeSignal(type, Token.Signal.END_ENUM);
    }

    private void add(final EnumType.ValidValue value)
    {
        Metadata.Builder builder = new Metadata.Builder(value.getName());

        builder.flag(Token.Signal.VALID_VALUE);
        builder.constValue(value.getPrimitiveValue());

        tokenList.add(new Token(builder.build()));
    }

    private void add(final SetType type, final int offset)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Metadata.Builder builder = new Metadata.Builder(encodingType.primitiveName());

        addTypeSignal(type, Token.Signal.BEGIN_SET);

        tokenList.add(new Token(encodingType, encodingType.size(), offset, byteOrder, builder.build()));

        for (final SetType.Choice choice : type.getChoices())
        {
            add(choice);
        }

        addTypeSignal(type, Token.Signal.END_SET);
    }

    private void add(final SetType.Choice value)
    {
        Metadata.Builder builder = new Metadata.Builder(value.getName());

        builder.flag(Token.Signal.CHOICE);
        builder.constValue(value.getPrimitiveValue());

        tokenList.add(new Token(builder.build()));
    }

    private void add(final EncodedDataType type, final int offset)
    {
        Metadata.Builder builder = new Metadata.Builder(type.getName());

        switch (type.getPresence())
        {
            case REQUIRED:
                builder.minValue(type.getMinValue());
                builder.maxValue(type.getMaxValue());
                break;

            case OPTIONAL:
                builder.minValue(type.getMinValue());
                builder.maxValue(type.getMaxValue());
                builder.nullValue(type.getNullValue());
                break;

            case CONSTANT:
                builder.constValue(type.getConstValue());
                break;
        }

        tokenList.add(new Token(type.getPrimitiveType(), type.size(), offset, byteOrder, builder.build()));
    }
}
