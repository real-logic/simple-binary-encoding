package org.agrona.sbe;

import org.agrona.DirectBuffer;

/**
 * A <code>sbe:composite</code> decoder flyweight.
 */
public interface CompositeDecoderFlyweight<T extends CompositeStructure> extends Flyweight<T>, DecoderFlyweight<T>
{
    CompositeDecoderFlyweight<T> wrap(final DirectBuffer buffer, final int offset);
}
