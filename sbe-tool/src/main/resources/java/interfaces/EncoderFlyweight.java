package org.agrona.sbe;

import org.agrona.MutableDirectBuffer;

/**
 * An encoder flyweight.
 */
public interface EncoderFlyweight<T extends Structure> extends Flyweight<T>
{
    EncoderFlyweight<T> wrap(final MutableDirectBuffer buffer, final int offset);
}
