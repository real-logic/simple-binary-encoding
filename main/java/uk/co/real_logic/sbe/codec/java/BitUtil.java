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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * Miscellaneous useful functions for dealing with low level bits and bytes.
 */
class BitUtil
{
    /**
     * Size of a byte in bytes
     */
    public static final int SIZE_OF_BYTE = 1;
    /**
     * Size of a boolean in bytes
     */
    public static final int SIZE_OF_BOOLEAN = 1;

    /**
     * Size of a char in bytes
     */
    public static final int SIZE_OF_CHAR = 2;
    /**
     * Size of a short in bytes
     */
    public static final int SIZE_OF_SHORT = 2;

    /**
     * Size of an int in bytes
     */
    public static final int SIZE_OF_INT = 4;
    /**
     * Size of a a float in bytes
     */
    public static final int SIZE_OF_FLOAT = 4;

    /**
     * Size of a long in bytes
     */
    public static final int SIZE_OF_LONG = 8;
    /**
     * Size of a double in bytes
     */
    public static final int SIZE_OF_DOUBLE = 8;

    private static final Unsafe UNSAFE;

    static
    {
        try
        {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>()
            {
                public Unsafe run() throws Exception
                {
                    final Field field = Unsafe.class.getDeclaredField("theUnsafe");
                    field.setAccessible(true);
                    return (Unsafe)field.get(null);
                }
            };

            UNSAFE = AccessController.doPrivileged(action);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the instance of {@link sun.misc.Unsafe}.
     *
     * @return the instance of Unsafe
     */
    public static Unsafe getUnsafe()
    {
        return UNSAFE;
    }

    /**
     * Set the private address of direct {@link ByteBuffer}.
     *
     * <b>Note:</b> It is assumed a cleaner is not responsible for reclaiming the memory under this buffer and that
     * the caller is responsible for memory allocation and reclamation.
     *
     * @param byteBuffer to set the address on.
     * @param address    to set for the underlying buffer.
     * @param capacity   of the new underlying buffer.
     * @return the modified {@link ByteBuffer}
     */
    public static ByteBuffer resetAddressAndCapacity(final ByteBuffer byteBuffer, final long address, final int capacity)
    {
        if (!byteBuffer.isDirect())
        {
            throw new IllegalArgumentException("Can only change address of direct buffers");
        }

        try
        {
            final Field addressField = Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            addressField.set(byteBuffer, Long.valueOf(address));

            final Field capacityField = Buffer.class.getDeclaredField("capacity");
            capacityField.setAccessible(true);
            capacityField.set(byteBuffer, Integer.valueOf(capacity));

            final Field cleanerField = byteBuffer.getClass().getDeclaredField("cleaner");
            cleanerField.setAccessible(true);
            cleanerField.set(byteBuffer, null);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }

        return byteBuffer;
    }
}