/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.codec.java.*;

public enum PresenceCodec
{
    REQUIRED((short)0),
    OPTIONAL((short)1),
    CONSTANT((short)2),
    NULL_VAL((short)255);

    private final short value;

    PresenceCodec(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static PresenceCodec get(final short value)
    {
        switch (value)
        {
            case 0: return REQUIRED;
            case 1: return OPTIONAL;
            case 2: return CONSTANT;
        }

        if ((short)255 == value)
        {
            return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
