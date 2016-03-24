/* Generated SBE (Simple Binary Encoding) message codec */
package uk.co.real_logic.sbe.ir.generated;

import org.agrona.DirectBuffer;

@javax.annotation.Generated(value = {"uk.co.real_logic.sbe.ir.generated.MessageDecoder"})
@SuppressWarnings("all")
public interface MessageDecoder
{
    int sbeBlockLength();

    int sbeTemplateId();

    int sbeSchemaId();

    int sbeSchemaVersion();

    String sbeSemanticType();

    int offset();

    MessageDecoder wrap(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion);

    int encodedLength();
}
