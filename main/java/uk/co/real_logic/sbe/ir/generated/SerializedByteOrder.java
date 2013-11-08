/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.*;

public enum SerializedByteOrder
{
    SBE_BIG_ENDIAN((short)1),
    SBE_LITTLE_ENDIAN((short)0);

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
            case 1: return SBE_BIG_ENDIAN;
            case 0: return SBE_LITTLE_ENDIAN;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
