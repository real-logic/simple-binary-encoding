/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.agrona.DirectBuffer;

@javax.annotation.Generated(value = {"uk.co.real_logic.sbe.ir.generated.VarDataEncodingDecoder"})
@SuppressWarnings("all")
public class VarDataEncodingDecoder
{
    public static final int ENCODED_LENGTH = -1;
    private DirectBuffer buffer;
    private int offset;

    public VarDataEncodingDecoder wrap(final DirectBuffer buffer, final int offset)
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


    public short length()
    {
        return (short)(buffer.getByte(offset + 0) & 0xFF);
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
