/* Generated SBE (Simple Binary Encoding) message codec. */
package uk.co.real_logic.sbe.ir.generated;


/**
 * Primitive types in type system
 */
public enum PrimitiveTypeCodec
{
    NONE((short)0),

    CHAR((short)1),

    INT8((short)2),

    INT16((short)3),

    INT32((short)4),

    INT64((short)5),

    UINT8((short)6),

    UINT16((short)7),

    UINT32((short)8),

    UINT64((short)9),

    FLOAT((short)10),

    DOUBLE((short)11),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((short)255);

    private final short value;

    PrimitiveTypeCodec(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static PrimitiveTypeCodec get(final short value)
    {
        switch (value)
        {
            case 0: return NONE;
            case 1: return CHAR;
            case 2: return INT8;
            case 3: return INT16;
            case 4: return INT32;
            case 5: return INT64;
            case 6: return UINT8;
            case 7: return UINT16;
            case 8: return UINT32;
            case 9: return UINT64;
            case 10: return FLOAT;
            case 11: return DOUBLE;
            case 255: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
