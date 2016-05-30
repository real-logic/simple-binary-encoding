package org.agrona.sbe;

import org.agrona.DirectBuffer;

/**
 * An <code>sbe:message</code> decoder flyweight.
 */
public interface MessageDecoderFlyweight<T extends MessageStructure> extends MessageFlyweight<T>, DecoderFlyweight<T>
{
    MessageDecoderFlyweight<T> wrap(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion);
}
