/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import java.nio.ByteOrder;
import java.util.*;
import uk.co.real_logic.sbe.generation.java.*;

public enum SerializedByteOrder
{
    BIG_ENDIAN((short)1),
    LITTLE_ENDIAN((short)0);

    private final short value;

    SerializedByteOrder(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static SerializedByteOrder get(final short value)
    {
        switch (value)
        {
            case 1: return BIG_ENDIAN;
            case 0: return LITTLE_ENDIAN;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
