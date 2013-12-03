/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import uk.co.real_logic.sbe.generation.java.*;

public class VarDataEncoding implements FixedFlyweight
{
    private DirectBuffer buffer;
    private int offset;
    private int actingVersion;

    public VarDataEncoding wrap(final DirectBuffer buffer, final int offset, final int actingVersion)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingVersion = actingVersion;
        return this;
    }

    public int size()
    {
        return -1;
    }

    public short length()
    {
        return CodecUtil.uint8Get(buffer, offset + 0);
    }

    public VarDataEncoding length(final short value)
    {
        CodecUtil.uint8Put(buffer, offset + 0, value);
        return this;
    }
}
