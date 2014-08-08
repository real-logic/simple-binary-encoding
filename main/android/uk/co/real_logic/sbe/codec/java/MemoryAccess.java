package uk.co.real_logic.sbe.codec.java;

interface MemoryAccess {

    byte peekByte(long address);

    int peekInt(long address, boolean swap);

    long peekLong(long address, boolean swap);

    short peekShort(long address, boolean swap);

    void peekByteArray(long address, byte[] dst, int dstOffset, int byteCount);

    void pokeByte(long address, byte value);

    void pokeInt(long address, int value, boolean swap);

    void pokeLong(long address, long value, boolean swap);

    void pokeShort(long address, short value, boolean swap);

    void pokeByteArray(long address, byte[] src, int offset, int count);
}
