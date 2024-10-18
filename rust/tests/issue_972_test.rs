use issue_972::{issue_972_codec::*, message_header_codec::MessageHeaderDecoder, *};

fn create_encoder(buffer: &mut Vec<u8>) -> Issue972Encoder {
    let issue_972 = Issue972Encoder::default().wrap(
        WriteBuf::new(buffer.as_mut_slice()),
        message_header_codec::ENCODED_LENGTH,
    );
    let mut header = issue_972.header(0);
    header.parent().unwrap()
}

#[test]
fn round_trip() -> SbeResult<()> {
    // encode...
    let mut buffer = vec![0u8; 256];
    let encoder = create_encoder(&mut buffer);
    let mut new_composite_encoder = encoder.new_field_encoder();
    new_composite_encoder.f1(2007);
    new_composite_encoder.f2(2012);

    // decode...
    let buf = ReadBuf::new(buffer.as_slice());
    let header = MessageHeaderDecoder::default().wrap(buf, 0);
    assert_eq!(SBE_BLOCK_LENGTH, header.block_length());
    assert_eq!(SBE_SCHEMA_VERSION, header.version());
    assert_eq!(SBE_TEMPLATE_ID, header.template_id());
    assert_eq!(SBE_SCHEMA_ID, header.schema_id());

    let decoder = Issue972Decoder::default().header(header, 0);
    if let Either::Right(composite) = decoder.new_field_decoder() {
        assert_eq!(2007, composite.f1().unwrap());
        assert_eq!(2012, composite.f2().unwrap());
    } else {
        panic!()
    }
    Ok(())
}
