package uk.co.real_logic.sbe.codec.java;

import libcore.io.Memory;

final class MemoryAccessIntAddress implements MemoryAccess
{
    MemoryAccessIntAddress()
    {
    }

    @Override
    public byte peekByte(long address)
    {
        return Memory.peekByte((int) address);
    }

    @Override
    public int peekInt(long address, boolean swap)
    {
        return Memory.peekInt((int) address, swap);
    }

    @Override
    public long peekLong(long address, boolean swap)
    {
        return Memory.peekLong((int) address, swap);
    }

    @Override
    public short peekShort(long address, boolean swap)
    {
        return Memory.peekShort((int) address, swap);
    }

    @Override
    public void peekByteArray(long address, byte[] dst, int dstOffset, int byteCount)
    {
        Memory.peekByteArray((int) address, dst, dstOffset, byteCount);
    }

    @Override
    public void pokeByte(long address, byte value)
    {
        Memory.pokeByte((int) address, value);
    }

    @Override
    public void pokeInt(long address, int value, boolean swap)
    {
        Memory.pokeInt((int) address, value, swap);
    }

    @Override
    public void pokeLong(long address, long value, boolean swap)
    {
        Memory.pokeLong((int) address, value, swap);
    }

    @Override
    public void pokeShort(long address, short value, boolean swap)
    {
        Memory.pokeShort((int) address, value, swap);
    }

    @Override
    public void pokeByteArray(long address, byte[] src, int offset, int count)
    {
        Memory.pokeByteArray((int) address, src, offset, count);
    }
}
