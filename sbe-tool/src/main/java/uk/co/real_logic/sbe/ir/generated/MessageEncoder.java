/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import org.agrona.MutableDirectBuffer;

@javax.annotation.Generated(value = {"uk.co.real_logic.sbe.ir.generated.MessageEncoder"})
@SuppressWarnings("all")
public interface MessageEncoder
{
    int sbeBlockLength();

    int sbeTemplateId();

    int sbeSchemaId();

    int sbeSchemaVersion();

    String sbeSemanticType();

    int offset();

    MessageEncoder wrap(MutableDirectBuffer buffer, int offset);

    int encodedLength();
}
