use issue_987::{
    issue_987_codec::{Issue987Decoder, Issue987Encoder, SBE_BLOCK_LENGTH, SBE_SCHEMA_ID, SBE_SCHEMA_VERSION, SBE_TEMPLATE_ID}, message_header_codec::MessageHeaderDecoder, *
};

fn create_encoder(buffer: &mut Vec<u8>, off: usize) -> Issue987Encoder {
    let encoder = Issue987Encoder::default().wrap(
        WriteBuf::new(buffer.as_mut_slice()),
        off + message_header_codec::ENCODED_LENGTH,
    );
    let mut header = encoder.header(off);
    header.parent().unwrap()
}

#[test]
fn encode_2_messages_and_then_decode() -> SbeResult<()> {
    let mut buffer = vec![0u8; 256];

    let mut off = 0;

    // encode message 1
    let mut encoder = create_encoder(&mut buffer, off);
    encoder.old_field(10);
    let mut other_encoder = encoder.new_field_encoder();
    other_encoder.f1(11);
    other_encoder.f2(12);
    let encoder = other_encoder.parent().unwrap();

    // Update offset
    off += message_header_codec::ENCODED_LENGTH + encoder.encoded_length();

    // encode message 2
    let mut encoder = create_encoder(&mut buffer, off);
    encoder.old_field(20);
    let mut other_encoder = encoder.new_field_encoder();
    other_encoder.f1(21);
    other_encoder.f2(22);

    // decoding ...
    off = 0;

    // decode message 1
    let buf = ReadBuf::new(buffer.as_slice());
    let header = MessageHeaderDecoder::default().wrap(buf, off);
    assert_eq!(SBE_BLOCK_LENGTH, header.block_length());
    assert_eq!(SBE_SCHEMA_VERSION, header.version());
    assert_eq!(SBE_TEMPLATE_ID, header.template_id());
    assert_eq!(SBE_SCHEMA_ID, header.schema_id());
    let decoder = Issue987Decoder::default().header(header, off);
    assert_eq!(10, decoder.old_field());
    let other_decoder = decoder.new_field_decoder();
    assert_eq!(11, other_decoder.f1());
    assert_eq!(12, other_decoder.f2());

    // Update offset
    off += message_header_codec::ENCODED_LENGTH + decoder.encoded_length();

    // decode message 2
    let buf = ReadBuf::new(buffer.as_slice());
    let header = MessageHeaderDecoder::default().wrap(buf, off);
    assert_eq!(SBE_BLOCK_LENGTH, header.block_length());
    assert_eq!(SBE_SCHEMA_VERSION, header.version());
    assert_eq!(SBE_TEMPLATE_ID, header.template_id());
    assert_eq!(SBE_SCHEMA_ID, header.schema_id());
    let decoder = Issue987Decoder::default().header(header, off);
    assert_eq!(20, decoder.old_field());
    let other_decoder = decoder.new_field_decoder();
    assert_eq!(21, other_decoder.f1());
    assert_eq!(22, other_decoder.f2());

    Ok(())
}
