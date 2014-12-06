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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Miscellaneous useful functions for dealing with low level bits and bytes.
 */
final class BitUtil
{

    //
    //"effectiveDirectAddress" type changed from int to long in changeset:
    // 0121106d9dc1ba713b53822886355e4d9339e852 (Android 4.3 - api 18)
    //at the same time, methods inside libcore.io.Memory where changed to use long addresses instead of ints
    //https://android.googlesource.com/platform/libcore/+/0121106d9dc1ba713b53822886355e4d9339e852%5E%21/
    //     luni/src/main/java/java/nio/Buffer.java
    //at the same time (api 18) ReadWriteDirectByteBuffer was merged into DirectByteBuffer
    //
    //In api 14 org/apache/harmony/luni/platform/OSMemory.java was renamed to libcore/io/Memory.java
    //https://android.googlesource.com/platform/libcore/+/f934c3d2c8dd9e6bc5299cef41adace2a671637d
    //DirectBuffer class supports api 14 or later.
    //
    //Added to Buffer in changeset: bd8ecd863aa83df50d7ce8f5950d8645ab6356af (Android 2.3 - api 9)
    //https://android.googlesource.com/platform/libcore/+/bd8ecd863aa83df50d7ce8f5950d8645ab6356af%5E%21/
    //     nio/src/main/java/java/nio/Buffer.java

    private static final Field EFFECTIVE_DIRECT_ADDRESS_FIELD;
    private static final MemoryAccess MEMORY_ACCESS;
    private static final boolean USE_LONG_ADDRESS;

    private static final Constructor<?> DIRECT_BYTE_BUFFER_CONSTRUCTOR;

    static
    {
        try
        {
            EFFECTIVE_DIRECT_ADDRESS_FIELD = getField(Buffer.class, "effectiveDirectAddress");

            USE_LONG_ADDRESS = EFFECTIVE_DIRECT_ADDRESS_FIELD.getType() == long.class;
            MEMORY_ACCESS = USE_LONG_ADDRESS ? new MemoryAccessLongAddress() : new MemoryAccessIntAddress();

            final PrivilegedExceptionAction<Constructor<?>> action = new PrivilegedExceptionAction<Constructor<?>>()
            {
                public Constructor<?> run() throws Exception
                {
                    final Class<?> clazz = Class.forName("java.nio.DirectByteBuffer");
                    final Constructor<?> constructor;
                    if (Modifier.isAbstract(clazz.getModifiers()))
                    {
                        //use this starting with api level < 18
                        final Class<?> rwClazz = Class.forName("java.nio.ReadWriteDirectByteBuffer");
                        constructor = rwClazz.getDeclaredConstructor(int.class, int.class);
                    }
                    else
                    {
                        //use this starting with api level >= 18
                        constructor = clazz.getDeclaredConstructor(long.class, int.class);
                    }
                    constructor.setAccessible(true);
                    return constructor;
                }
            };
            DIRECT_BYTE_BUFFER_CONSTRUCTOR = AccessController.doPrivileged(action);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private BitUtil()
    {
    }

    /**
     * Returns the memory address of a direct buffer.
     *
     * @param buffer the direct buffer
     * @return the memory address of the buffer
     */
    static long getEffectiveDirectAddress(final ByteBuffer buffer)
    {
        try
        {
            return USE_LONG_ADDRESS ? EFFECTIVE_DIRECT_ADDRESS_FIELD.getLong(buffer)
                    : EFFECTIVE_DIRECT_ADDRESS_FIELD.getInt(buffer);
        }
        catch (final IllegalArgumentException ignore)
        {
            return 0;
        }
        catch (final IllegalAccessException ignore)
        {
            return 0;
        }
    }

    /**
     * Get the instance of {@link MemoryAccess}.
     *
     * @return the instance of MemoryAccess
     */
    static MemoryAccess getMemoryAccess()
    {
        return MEMORY_ACCESS;
    }

    /**
     * Extracts a field from a class using reflection.
     *
     * @param clazz from which to get the field object
     * @param name the name of the field object
     * @return the field object.
     * @throws PrivilegedActionException
     */
    private static Field getField(final Class<?> clazz, final String name) throws PrivilegedActionException
    {
        final PrivilegedExceptionAction<Field> action = new PrivilegedExceptionAction<Field>()
        {
            public Field run() throws Exception
            {
                final Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            }
        };

        return AccessController.doPrivileged(action);
    }

    /**
     * Builds a DirectByteBuffer out of an address and a size. Uses the same
     * constructor as the buffers that are created from JNI, thus it does not
     * deallocate the memory when the {@link ByteBuffer} object is garbage collected.
     *
     * @param address  where the memory begins off-heap
     * @param size     of the buffer from the given address
     * @return the {@link ByteBuffer} associated with the address and size
     */
    static ByteBuffer newDirectByteBuffer(final long address, final int size)
    {
        try
        {
            if (USE_LONG_ADDRESS)
            {
                return (ByteBuffer) DIRECT_BYTE_BUFFER_CONSTRUCTOR.newInstance(address, size);
            }
            else
            {
                return (ByteBuffer) DIRECT_BYTE_BUFFER_CONSTRUCTOR.newInstance((int) address, size);
            }
        }
        catch (final IllegalArgumentException ignore)
        {
            return null;
        }
        catch (final IllegalAccessException ignore)
        {
            return null;
        }
        catch (InstantiationException ignore)
        {
            return null;
        }
        catch (InvocationTargetException ignore)
        {
            return null;
        }
    }
}
