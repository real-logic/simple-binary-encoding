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
import uk.co.real_logic.sbe.ir.generated.SerializedByteOrder;
import uk.co.real_logic.sbe.ir.generated.SerializedPrimitiveType;
import uk.co.real_logic.sbe.ir.generated.SerializedSignal;

import java.nio.ByteOrder;

public class SerializationUtils
{
    public static SerializedByteOrder byteOrder(final ByteOrder byteOrder)
    {
        if (byteOrder == ByteOrder.BIG_ENDIAN)
        {
            return SerializedByteOrder.BIG_ENDIAN;
        }

        return SerializedByteOrder.LITTLE_ENDIAN;
    }

    public static ByteOrder byteOrder(final SerializedByteOrder byteOrder)
    {
        switch (byteOrder)
        {
            case LITTLE_ENDIAN:
                return ByteOrder.LITTLE_ENDIAN;

            case BIG_ENDIAN:
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
            default:
                return PrimitiveType.CHAR;
        }
    }

    public static int putVal(final byte[] array, final PrimitiveValue value)
    {
        if (value == null)
        {
            return 0;
        }

        final byte[] stringRep = value.toString().getBytes();

        System.arraycopy(stringRep, 0, array, 0, stringRep.length);
        return stringRep.length;
    }

    public static PrimitiveValue getVal(final byte[] array, final int length, final PrimitiveType type)
    {
        if (length == 0)
        {
            return null;
        }

        final String stringRep = new String(array, 0, length);
        System.out.println("getVal(" + type.toString() + ")=" + stringRep);

        return PrimitiveValue.parse(stringRep, type);
    }
}
