package org.agrona.sbe;

/**
 * An SBE flyweight.
 */
public interface Flyweight<T extends Structure>
{
    int encodedLength();
}
