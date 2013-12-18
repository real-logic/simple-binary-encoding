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
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.codec.java.CodecUtil;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.generated.SerializedByteOrder;
import uk.co.real_logic.sbe.ir.generated.SerializedPrimitiveType;
import uk.co.real_logic.sbe.ir.generated.SerializedSignal;

import java.nio.ByteOrder;

public class IrUtil
{
    public static SerializedByteOrder byteOrder(final ByteOrder byteOrder)
    {
        if (byteOrder == ByteOrder.BIG_ENDIAN)
        {
            return SerializedByteOrder.SBE_BIG_ENDIAN;
        }

        return SerializedByteOrder.SBE_LITTLE_ENDIAN;
    }

    public static ByteOrder byteOrder(final SerializedByteOrder byteOrder)
    {
        switch (byteOrder)
        {
            case SBE_LITTLE_ENDIAN:
                return ByteOrder.LITTLE_ENDIAN;

            case SBE_BIG_ENDIAN:
                return ByteOrder.BIG_ENDIAN;
        }

        return ByteOrder.LITTLE_ENDIAN;
    }

    public static SerializedSignal signal(final Signal signal)
    {
        switch (signal)
        {
            case BEGIN_MESSAGE:
                return SerializedSignal.BEGIN_MESSAGE;

            case END_MESSAGE:
                return SerializedSignal.END_MESSAGE;

            case BEGIN_FIELD:
                return SerializedSignal.BEGIN_FIELD;

            case END_FIELD:
                return SerializedSignal.END_FIELD;

            case BEGIN_COMPOSITE:
                return SerializedSignal.BEGIN_COMPOSITE;

            case END_COMPOSITE:
                return SerializedSignal.END_COMPOSITE;

            case BEGIN_ENUM:
                return SerializedSignal.BEGIN_ENUM;

            case END_ENUM:
                return SerializedSignal.END_ENUM;

            case BEGIN_SET:
                return SerializedSignal.BEGIN_SET;

            case END_SET:
                return SerializedSignal.END_SET;

            case BEGIN_GROUP:
                return SerializedSignal.BEGIN_GROUP;

            case END_GROUP:
                return SerializedSignal.END_GROUP;

            case BEGIN_VAR_DATA:
                return SerializedSignal.BEGIN_VAR_DATA;

            case END_VAR_DATA:
                return SerializedSignal.END_VAR_DATA;

            case VALID_VALUE:
                return SerializedSignal.VALID_VALUE;

            case CHOICE:
                return SerializedSignal.CHOICE;

            case ENCODING:
            default:
                return SerializedSignal.ENCODING;
        }
    }

    public static Signal signal(final SerializedSignal signal)
    {
        switch (signal)
        {
            case BEGIN_MESSAGE:
                return Signal.BEGIN_MESSAGE;

            case END_MESSAGE:
                return Signal.END_MESSAGE;

            case BEGIN_FIELD:
                return Signal.BEGIN_FIELD;

            case END_FIELD:
                return Signal.END_FIELD;

            case BEGIN_COMPOSITE:
                return Signal.BEGIN_COMPOSITE;

            case END_COMPOSITE:
                return Signal.END_COMPOSITE;

            case BEGIN_ENUM:
                return Signal.BEGIN_ENUM;

            case END_ENUM:
                return Signal.END_ENUM;

            case BEGIN_SET:
                return Signal.BEGIN_SET;

            case END_SET:
                return Signal.END_SET;

            case BEGIN_GROUP:
                return Signal.BEGIN_GROUP;

            case END_GROUP:
                return Signal.END_GROUP;

            case BEGIN_VAR_DATA:
                return Signal.BEGIN_VAR_DATA;

            case END_VAR_DATA:
                return Signal.END_VAR_DATA;

            case VALID_VALUE:
                return Signal.VALID_VALUE;

            case CHOICE:
                return Signal.CHOICE;

            case ENCODING:
            default:
                return Signal.ENCODING;
        }
    }

    public static SerializedPrimitiveType primitiveType(final PrimitiveType type)
    {
        if (type == null)
        {
            return SerializedPrimitiveType.NONE;
        }

        switch (type)
        {
            case INT8:
                return SerializedPrimitiveType.INT8;

            case INT16:
                return SerializedPrimitiveType.INT16;

            case INT32:
                return SerializedPrimitiveType.INT32;

            case INT64:
                return SerializedPrimitiveType.INT64;

            case UINT8:
                return SerializedPrimitiveType.UINT8;

            case UINT16:
                return SerializedPrimitiveType.UINT16;

            case UINT32:
                return SerializedPrimitiveType.UINT32;

            case UINT64:
                return SerializedPrimitiveType.UINT64;

            case FLOAT:
                return SerializedPrimitiveType.FLOAT;

            case DOUBLE:
                return SerializedPrimitiveType.DOUBLE;

            case CHAR:
                return SerializedPrimitiveType.CHAR;

            default:
                return SerializedPrimitiveType.NONE;
        }
    }

    public static PrimitiveType primitiveType(final SerializedPrimitiveType type)
    {
        switch (type)
        {
            case INT8:
                return PrimitiveType.INT8;

            case INT16:
                return PrimitiveType.INT16;

            case INT32:
                return PrimitiveType.INT32;

            case INT64:
                return PrimitiveType.INT64;

            case UINT8:
                return PrimitiveType.UINT8;

            case UINT16:
                return PrimitiveType.UINT16;

            case UINT32:
                return PrimitiveType.UINT32;

            case UINT64:
                return PrimitiveType.UINT64;

            case FLOAT:
                return PrimitiveType.FLOAT;

            case DOUBLE:
                return PrimitiveType.DOUBLE;

            case CHAR:
                return PrimitiveType.CHAR;

            case NONE:
            default:
                return null;
        }
    }

    public static int putVal(final DirectBuffer buffer,
                             final PrimitiveValue value,
                             final PrimitiveType type)
    {
        if (value == null)
        {
            return 0;
        }

        switch (type)
        {
            case CHAR:
                if (value.size() == 1)
                {
                    CodecUtil.charPut(buffer, 0, (byte)value.longValue());
                    return 1;
                }
                else
                {
                    CodecUtil.charsPut(buffer, 0, value.byteArrayValue(), 0, value.byteArrayValue().length);
                    return value.byteArrayValue().length;
                }

            case INT8:
                CodecUtil.int8Put(buffer, 0, (byte)value.longValue());
                return 1;

            case INT16:
                CodecUtil.int16Put(buffer, 0, (short)value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 2;

            case INT32:
                CodecUtil.int32Put(buffer, 0, (int)value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 4;

            case INT64:
                CodecUtil.int64Put(buffer, 0, value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 8;

            case UINT8:
                CodecUtil.uint8Put(buffer, 0, (short)value.longValue());
                return 1;

            case UINT16:
                CodecUtil.uint16Put(buffer, 0, (int)value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 2;

            case UINT32:
                CodecUtil.uint32Put(buffer, 0, value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 4;

            case UINT64:
                CodecUtil.uint64Put(buffer, 0, value.longValue(), ByteOrder.LITTLE_ENDIAN);
                return 8;

            case FLOAT:
                CodecUtil.floatPut(buffer, 0, (float)value.doubleValue(), ByteOrder.LITTLE_ENDIAN);
                return 4;

            case DOUBLE:
                CodecUtil.doublePut(buffer, 0, value.doubleValue(), ByteOrder.LITTLE_ENDIAN);
                return 8;

            default:
                return 0;
        }
    }

    public static PrimitiveValue getVal(final DirectBuffer buffer, final PrimitiveType type, final int length)
    {
        if (length == 0)
        {
            return null;
        }

        switch (type)
        {
            case CHAR:
                if (length == 1)
                {
                    return new PrimitiveValue(CodecUtil.charGet(buffer, 0), 1);
                }
                else
                {
                    final byte[] array = new byte[length];
                    CodecUtil.charsGet(buffer, 0, array, 0, array.length);
                    return new PrimitiveValue(array, "UTF-8", array.length);
                }

            case INT8:
                return new PrimitiveValue(CodecUtil.int8Get(buffer, 0), 1);

            case INT16:
                return new PrimitiveValue(CodecUtil.int16Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 2);

            case INT32:
                return new PrimitiveValue(CodecUtil.int32Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 4);

            case INT64:
                return new PrimitiveValue(CodecUtil.int64Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 8);

            case UINT8:
                return new PrimitiveValue(CodecUtil.uint8Get(buffer, 0), 1);

            case UINT16:
                return new PrimitiveValue(CodecUtil.uint16Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 2);

            case UINT32:
                return new PrimitiveValue(CodecUtil.uint32Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 4);

            case UINT64:
                return new PrimitiveValue(CodecUtil.uint64Get(buffer, 0, ByteOrder.LITTLE_ENDIAN), 8);

            case FLOAT:
                return new PrimitiveValue(CodecUtil.floatGet(buffer, 0, ByteOrder.LITTLE_ENDIAN), 4);

            case DOUBLE:
                return new PrimitiveValue(CodecUtil.doubleGet(buffer, 0, ByteOrder.LITTLE_ENDIAN), 8);

            default:
                return null;
        }
    }
}
