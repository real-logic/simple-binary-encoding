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

/**
 * This class is used to access memory addresses.
 * The 2 implementations that exists are proxies for the libcore.io.Memory class
 * which was changed in Android 4.3 (API 18) to use longs as addresses instead of integers.
 * The libcore.io.Memory class is available starting with Android 4.0 (API 14).
 */
interface MemoryAccess
{

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
