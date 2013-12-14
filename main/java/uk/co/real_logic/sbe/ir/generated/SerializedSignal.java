/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.*;

public enum SerializedSignal
{
    END_SET((short)14),
    BEGIN_VAR_DATA((short)15),
    END_MESSAGE((short)2),
    BEGIN_FIELD((short)5),
    END_FIELD((short)6),
    END_COMPOSITE((short)4),
    ENCODING((short)17),
    VALID_VALUE((short)10),
    END_GROUP((short)8),
    BEGIN_COMPOSITE((short)3),
    END_VAR_DATA((short)16),
    BEGIN_GROUP((short)7),
    BEGIN_ENUM((short)9),
    END_ENUM((short)11),
    BEGIN_SET((short)12),
    CHOICE((short)13),
    BEGIN_MESSAGE((short)1),
    NULL_VAL((short)255);

    private final short value;

    SerializedSignal(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static SerializedSignal get(final short value)
    {
        switch (value)
        {
            case 14: return END_SET;
            case 15: return BEGIN_VAR_DATA;
            case 2: return END_MESSAGE;
            case 5: return BEGIN_FIELD;
            case 6: return END_FIELD;
            case 4: return END_COMPOSITE;
            case 17: return ENCODING;
            case 10: return VALID_VALUE;
            case 8: return END_GROUP;
            case 3: return BEGIN_COMPOSITE;
            case 16: return END_VAR_DATA;
            case 7: return BEGIN_GROUP;
            case 9: return BEGIN_ENUM;
            case 11: return END_ENUM;
            case 12: return BEGIN_SET;
            case 13: return CHOICE;
            case 1: return BEGIN_MESSAGE;
            case 255: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
