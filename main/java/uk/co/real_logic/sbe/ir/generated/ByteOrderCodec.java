/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;

public enum ByteOrderCodec
{
    SBE_LITTLE_ENDIAN((short)0),
    SBE_BIG_ENDIAN((short)1),
    NULL_VAL((short)255);

    private final short value;

    ByteOrderCodec(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static ByteOrderCodec get(final short value)
    {
        switch (value)
        {
            case 0: return SBE_LITTLE_ENDIAN;
            case 1: return SBE_BIG_ENDIAN;
            case 255: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
