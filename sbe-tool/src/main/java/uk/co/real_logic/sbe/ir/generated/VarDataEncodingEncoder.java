/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.agrona.MutableDirectBuffer;

@javax.annotation.Generated(value = {"uk.co.real_logic.sbe.ir.generated.VarDataEncodingEncoder"})
@SuppressWarnings("all")
public class VarDataEncodingEncoder
{
    public static final int ENCODED_LENGTH = -1;
    private MutableDirectBuffer buffer;
    private int offset;

    public VarDataEncodingEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        return this;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public static short lengthNullValue()
    {
        return (short)255;
    }

    public static short lengthMinValue()
    {
        return (short)0;
    }

    public static short lengthMaxValue()
    {
        return (short)254;
    }

    public VarDataEncodingEncoder length(final short value)
    {
        buffer.putByte(offset + 0, (byte)value);
        return this;
    }


    public static short varDataNullValue()
    {
        return (short)255;
    }

    public static short varDataMinValue()
    {
        return (short)0;
    }

    public static short varDataMaxValue()
    {
        return (short)254;
    }

}
