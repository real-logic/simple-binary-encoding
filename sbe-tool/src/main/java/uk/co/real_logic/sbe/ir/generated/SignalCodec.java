/* Generated SBE (Simple Binary Encoding) message codec. */
package uk.co.real_logic.sbe.ir.generated;


/**
 * Token signal type in IR
 */
public enum SignalCodec
{
    BEGIN_MESSAGE((short)1),

    END_MESSAGE((short)2),

    BEGIN_COMPOSITE((short)3),

    END_COMPOSITE((short)4),

    BEGIN_FIELD((short)5),

    END_FIELD((short)6),

    BEGIN_GROUP((short)7),

    END_GROUP((short)8),

    BEGIN_ENUM((short)9),

    VALID_VALUE((short)10),

    END_ENUM((short)11),

    BEGIN_SET((short)12),

    CHOICE((short)13),

    END_SET((short)14),

    BEGIN_VAR_DATA((short)15),

    END_VAR_DATA((short)16),

    ENCODING((short)17),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((short)255);

    private final short value;

    SignalCodec(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static SignalCodec get(final short value)
    {
        switch (value)
        {
            case 1: return BEGIN_MESSAGE;
            case 2: return END_MESSAGE;
            case 3: return BEGIN_COMPOSITE;
            case 4: return END_COMPOSITE;
            case 5: return BEGIN_FIELD;
            case 6: return END_FIELD;
            case 7: return BEGIN_GROUP;
            case 8: return END_GROUP;
            case 9: return BEGIN_ENUM;
            case 10: return VALID_VALUE;
            case 11: return END_ENUM;
            case 12: return BEGIN_SET;
            case 13: return CHOICE;
            case 14: return END_SET;
            case 15: return BEGIN_VAR_DATA;
            case 16: return END_VAR_DATA;
            case 17: return ENCODING;
            case 255: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
