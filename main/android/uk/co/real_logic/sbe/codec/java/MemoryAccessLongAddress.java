/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.codec.java;

import libcore.io.Memory;

final class MemoryAccessLongAddress implements MemoryAccess
{
    MemoryAccessLongAddress()
    {
    }

    @Override
    public byte peekByte(long address)
    {
        return Memory.peekByte(address);
    }

    @Override
    public int peekInt(long address, boolean swap)
    {
        return Memory.peekInt(address, swap);
    }

    @Override
    public long peekLong(long address, boolean swap)
    {
        return Memory.peekLong(address, swap);
    }

    @Override
    public short peekShort(long address, boolean swap)
    {
        return Memory.peekShort(address, swap);
    }

    @Override
    public void peekByteArray(long address, byte[] dst, int dstOffset, int byteCount)
    {
        Memory.peekByteArray(address, dst, dstOffset, byteCount);
    }

    @Override
    public void pokeByte(long address, byte value)
    {
        Memory.pokeByte(address, value);
    }

    @Override
    public void pokeInt(long address, int value, boolean swap)
    {
        Memory.pokeInt(address, value, swap);
    }

    @Override
    public void pokeLong(long address, long value, boolean swap)
    {
        Memory.pokeLong(address, value, swap);
    }

    @Override
    public void pokeShort(long address, short value, boolean swap)
    {
        Memory.pokeShort(address, value, swap);
    }

    @Override
    public void pokeByteArray(long address, byte[] src, int offset, int count)
    {
        Memory.pokeByteArray(address, src, offset, count);
    }
}
