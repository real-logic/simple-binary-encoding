/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import java.nio.ByteOrder;
import java.util.*;
import uk.co.real_logic.sbe.generation.java.*;

public class VarDataEncoding implements FixedFlyweight
{
    private DirectBuffer buffer;
    private int offset;

    public VarDataEncoding reset(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
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
