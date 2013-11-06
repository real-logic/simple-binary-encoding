/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import java.nio.ByteOrder;
import java.util.*;
import uk.co.real_logic.sbe.generation.java.*;

public enum SerializedPrimitiveType
{
    INT32((short)4),
    INT8((short)2),
    CHAR((short)1),
    DOUBLE((short)11),
    FLOAT((short)10),
    UINT32((short)8),
    UINT8((short)6),
    INT64((short)5),
    UINT64((short)9),
    UINT16((short)7),
    INT16((short)3);

    private final short value;

    SerializedPrimitiveType(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static SerializedPrimitiveType get(final short value)
    {
        switch (value)
        {
            case 4: return INT32;
            case 2: return INT8;
            case 1: return CHAR;
            case 11: return DOUBLE;
            case 10: return FLOAT;
            case 8: return UINT32;
            case 6: return UINT8;
            case 5: return INT64;
            case 9: return UINT64;
            case 7: return UINT16;
            case 3: return INT16;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
