use issue_984::{
    message_header_codec::MessageHeaderDecoder,
    simple_message_codec::{encoder::MyGroupEncoder, *},
    *,
};

fn create_encoder(buffer: &mut Vec<u8>) -> SimpleMessageEncoder {
    let simple_msg = SimpleMessageEncoder::default().wrap(
        WriteBuf::new(buffer.as_mut_slice()),
        message_header_codec::ENCODED_LENGTH,
    );
    let mut header = simple_msg.header(0);
    header.parent().unwrap()
}

#[test]
fn round_trip() -> SbeResult<()> {
    // encode...
    let mut buffer = vec![0u8; 256];
    let mut simple_msg_encoder = create_encoder(&mut buffer);
    simple_msg_encoder.id(1985);

    let mut my_grp_encoder = simple_msg_encoder.my_group_encoder(1, MyGroupEncoder::default());
    my_grp_encoder.advance()?;
    my_grp_encoder.f1(&[1, 2, 3, 4]);
    my_grp_encoder.f2(&[1, 2, 3, 4, 5]);
    my_grp_encoder.f3(&[1, 2, 3, 4, 5, 6]);

    // decode...
    let buf = ReadBuf::new(buffer.as_slice());
    let header = MessageHeaderDecoder::default().wrap(buf, 0);
    assert_eq!(SBE_BLOCK_LENGTH, header.block_length());
    assert_eq!(SBE_SCHEMA_VERSION, header.version());
    assert_eq!(SBE_TEMPLATE_ID, header.template_id());
    assert_eq!(SBE_SCHEMA_ID, header.schema_id());

    let simple_msg_decoder = SimpleMessageDecoder::default().header(header, 0);
    assert_eq!(1985, simple_msg_decoder.id());
    let mut grp_decoder = simple_msg_decoder.my_group_decoder();
    assert_eq!(1, grp_decoder.count());
    grp_decoder.advance()?;

    assert_eq!([1, 2, 3, 4], grp_decoder.f1());
    assert_eq!([1, 2, 3, 4, 5], grp_decoder.f2());
    assert_eq!([1, 2, 3, 4, 5, 6], grp_decoder.f3());
    Ok(())
}
