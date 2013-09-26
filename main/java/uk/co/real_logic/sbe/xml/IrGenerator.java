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
 * Class to hold all the state while generating the {@link uk.co.real_logic.sbe.ir.Token} list.
 * <p/>
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
    private int currentOffset = 0;
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    public List<Token> generateForMessage(final Message msg)
    {
        tokenList.clear();
        currentOffset = 0;
        byteOrder = ByteOrder.LITTLE_ENDIAN;

        addMessageSignal(msg, Token.Signal.MESSAGE_START);

        addAllFields(msg.getFields());

        addMessageSignal(msg, Token.Signal.MESSAGE_END);

        return tokenList;
    }

    public List<Token> generateForHeader(final MessageSchema schema)
    {
        tokenList.clear();
        currentOffset = 0;

        byteOrder = schema.getByteOrder();
        CompositeType type = schema.getMessageHeader();
        add(type);

        return tokenList;
    }

    private void addMessageSignal(final Message msg, final Token.Signal signal)
    {
        Metadata.Builder builder = new Metadata.Builder(msg.getName());

        builder.schemaId(msg.getId());
        builder.id(0);
        builder.flag(signal);
        builder.fixUsage(msg.getFixMsgType());
        builder.description(msg.getDescription());
        tokenList.add(new Token(new Metadata(builder)));
    }

    private void addTypeSignal(final Type type, final Token.Signal signal)
    {
        Metadata.Builder builder = new Metadata.Builder(type.getName());

        builder.flag(signal);

        if (type.getFixUsage() != null)
        {
            builder.fixUsage(type.getFixUsage().getName());
        }

        builder.description(type.getDescription());
        tokenList.add(new Token(new Metadata(builder)));
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
        builder.description(field.getDescription());

        if (field.getFixUsage() != null)
        {
            builder.fixUsage(field.getFixUsage().getName());
        }
        else if (field.getType() != null && field.getType().getFixUsage() != null)
        {
            builder.fixUsage(field.getType().getFixUsage().getName());
        }

        tokenList.add(new Token(new Metadata(builder)));
    }

    private void addAllFields(final List<Message.Field> fieldList)
    {
        for (final Message.Field field : fieldList)
        {
            final Type type = field.getType();

            if (type == null)
            {
                addFieldSignal(field, Token.Signal.GROUP_START);
                addAllFields(field.getGroupFieldList());
                addFieldSignal(field, Token.Signal.GROUP_END);
            }
            else
            {
                addFieldSignal(field, Token.Signal.FIELD_START);

                if (type instanceof EncodedDataType)
                {
                    add((EncodedDataType)type);
                }
                else if (type instanceof CompositeType)
                {
                    add((CompositeType)type);
                }
                else if (type instanceof EnumType)
                {
                    add((EnumType)type);
                }
                else if (type instanceof SetType)
                {
                    add((SetType)type);
                }
                else
                {
                    throw new IllegalStateException("Unknown type: " + type);
                }

                addFieldSignal(field, Token.Signal.FIELD_END);
            }
        }
    }

    private void add(final CompositeType type)
    {
        addTypeSignal(type, Token.Signal.COMPOSITE_START);

        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt);
        }

        addTypeSignal(type, Token.Signal.COMPOSITE_END);
    }

    private void add(final EnumType type)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Metadata.Builder builder = new Metadata.Builder(encodingType.primitiveName());

        addTypeSignal(type, Token.Signal.ENUM_START);

        if (type.getPresence() == Presence.OPTIONAL)
        {
            builder.nullValue(encodingType.nullValue());
        }

        tokenList.add(new Token(encodingType, encodingType.size(), currentOffset, byteOrder, new Metadata(builder)));

        for (final EnumType.ValidValue v : type.getValidValues())
        {
            add(v);
        }

        addTypeSignal(type, Token.Signal.ENUM_END);

        currentOffset += encodingType.size();
    }

    private void add(final EnumType.ValidValue value)
    {
        Metadata.Builder builder = new Metadata.Builder(value.getName());

        builder.flag(Token.Signal.ENUM_VALUE);
        builder.constValue(value.getPrimitiveValue());
        builder.description(value.getDescription());

        tokenList.add(new Token(new Metadata(builder)));
    }

    private void add(final SetType type)
    {
        PrimitiveType encodingType = type.getEncodingType();
        Metadata.Builder builder = new Metadata.Builder(encodingType.primitiveName());

        addTypeSignal(type, Token.Signal.SET_START);

        tokenList.add(new Token(encodingType, encodingType.size(), currentOffset, byteOrder, new Metadata(builder)));

        for (final SetType.Choice choice : type.getChoices())
        {
            add(choice);
        }

        addTypeSignal(type, Token.Signal.SET_END);

        currentOffset += encodingType.size();
    }

    private void add(final SetType.Choice value)
    {
        Metadata.Builder builder = new Metadata.Builder(value.getName());

        builder.flag(Token.Signal.SET_CHOICE);
        builder.constValue(value.getPrimitiveValue());
        builder.description(value.getDescription());

        tokenList.add(new Token(new Metadata(builder)));
    }

    private void add(final EncodedDataType type)
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

        tokenList.add(new Token(type.getPrimitiveType(), type.size(), currentOffset, byteOrder, new Metadata(builder)));

        currentOffset += type.size();
    }
}
