package uk.co.real_logic.sbe.codec.java;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

/**
 * Miscellaneous useful functions for dealing with low level bits and bytes.
 */
final class BitUtil
{

    //
    //"effectiveDirectAddress" type changed from int to long in changeset: 0121106d9dc1ba713b53822886355e4d9339e852 (Android 4.3 - api 18)
    //at the same time, methods inside libcore.io.Memory where changed to use long addresses instead of ints
    //https://android.googlesource.com/platform/libcore/+/0121106d9dc1ba713b53822886355e4d9339e852%5E%21/luni/src/main/java/java/nio/Buffer.java
    //
    //In api 14 org/apache/harmony/luni/platform/OSMemory.java was renamed to libcore/io/Memory.java
    //https://android.googlesource.com/platform/libcore/+/f934c3d2c8dd9e6bc5299cef41adace2a671637d
    //DirectBuffer class supports api 14 or later.
    //
    //Added to Buffer in changeset: bd8ecd863aa83df50d7ce8f5950d8645ab6356af (Android 2.3 - api 9)
    //https://android.googlesource.com/platform/libcore/+/bd8ecd863aa83df50d7ce8f5950d8645ab6356af%5E%21/nio/src/main/java/java/nio/Buffer.java

    private static final long EFFECTIVE_DIRECT_ADDRESS_FIELD_OFFSET;
    static final long POSITION_FIELD_OFFSET;
    private static final Unsafe UNSAFE;
    static final boolean USE_LONG_ADDRESS;

    static
    {
        try
        {
            UNSAFE = getStaticFieldValue(Unsafe.class, "THE_ONE");
            Field positionField = getField(Buffer.class, "position");
            POSITION_FIELD_OFFSET = UNSAFE.objectFieldOffset(positionField);
            Field effectiveDirectAddressField = getField(Buffer.class, "effectiveDirectAddress");
            EFFECTIVE_DIRECT_ADDRESS_FIELD_OFFSET = UNSAFE.objectFieldOffset(effectiveDirectAddressField);
            USE_LONG_ADDRESS = effectiveDirectAddressField.getType() == long.class;
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private BitUtil()
    {
    }

    static void setBufferPosition(final ByteBuffer dstBuffer, int position)
    {
        UNSAFE.putInt(dstBuffer, POSITION_FIELD_OFFSET, position);
    }

    static long getEffectiveDirectAddress(final ByteBuffer buffer)
    {
        return USE_LONG_ADDRESS ? UNSAFE.getLong(buffer,
                EFFECTIVE_DIRECT_ADDRESS_FIELD_OFFSET) : UNSAFE.getInt(buffer,
                    EFFECTIVE_DIRECT_ADDRESS_FIELD_OFFSET);
    }


    /**
     * Get the instance of {@link sun.misc.Unsafe}.
     *
     * @return the instance of Unsafe
     */
    static Unsafe getUnsafe()
    {
        return UNSAFE;
    }

    static <T> T getStaticFieldValue(final Class<?> clazz, final String name) throws PrivilegedActionException
    {
        final PrivilegedExceptionAction<T> action = new PrivilegedExceptionAction<T>()
        {
            @SuppressWarnings("unchecked")
            public T run() throws Exception
            {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(null);
            }
        };
        return AccessController.doPrivileged(action);
    }

    static Field getField(final Class<?> clazz, final String name) throws PrivilegedActionException
    {
        final PrivilegedExceptionAction<Field> action = new PrivilegedExceptionAction<Field>()
        {
            public Field run() throws Exception
            {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            }
        };
        return AccessController.doPrivileged(action);
    }
}
