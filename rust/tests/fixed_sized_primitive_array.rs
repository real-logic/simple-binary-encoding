use fixed_sized_primitive_array::{
    demo_codec::{DemoDecoder, DemoEncoder},
    message_header_codec::{MessageHeaderDecoder, ENCODED_LENGTH},
    ReadBuf, WriteBuf,
};

fn create_encoder(buffer: &mut [u8]) -> DemoEncoder {
    let encoder = DemoEncoder::default().wrap(WriteBuf::new(buffer), ENCODED_LENGTH);
    let mut header = encoder.header(0);
    header.parent().unwrap()
}

#[test]
fn test_encode_then_decode_u8_slice() {
    let uninit = 1u8;

    let test_data = [
        b"" as &[u8],
        b"0" as &[u8],
        b"01" as &[u8],
        b"012" as &[u8],
        b"0123" as &[u8],
        b"01234" as &[u8],
        b"012345" as &[u8],
        b"0123456" as &[u8],
        b"01234567" as &[u8],
        b"012345678" as &[u8],
        b"0123456789" as &[u8],
        b"0123456789A" as &[u8],
        b"0123456789AB" as &[u8],
        b"0123456789ABC" as &[u8],
        b"0123456789ABCD" as &[u8],
        b"0123456789ABCDE" as &[u8],
        b"0123456789ABCDEF" as &[u8],
        b"0123456789abcdef" as &[u8],
        b"0123456789abcdef0" as &[u8],
        b"0123456789abcdef01" as &[u8],
        b"0123456789abcdef012" as &[u8],
        b"0123456789abcdef0123" as &[u8],
        b"0123456789abcdef01234" as &[u8],
        b"0123456789abcdef012345" as &[u8],
        b"0123456789abcdef0123456" as &[u8],
        b"0123456789abcdef01234567" as &[u8],
        b"0123456789abcdef012345678" as &[u8],
        b"0123456789abcdef0123456789" as &[u8],
        b"0123456789abcdef0123456789A" as &[u8],
        b"0123456789abcdef0123456789AB" as &[u8],
        b"0123456789abcdef0123456789ABC" as &[u8],
        b"0123456789abcdef0123456789ABCD" as &[u8],
        b"0123456789abcdef0123456789ABCDE" as &[u8],
        b"0123456789abcdef0123456789ABCDEF" as &[u8],
    ];

    // <field name="fixed16Char" id="1" type="Fixed16Char"/>
    // <field name="fixed16AsciiChar" id="2" type="Fixed16AsciiChar"/>
    // <field name="fixed16Gb18030Char" id="3" type="Fixed16Gb18030Char"/>
    // <field name="fixed16Utf8Char" id="4" type="Fixed16Utf8Char"/>
    // <field name="fixed16U8" id="11" type="Fixed16U8"/>
    // <field name="fixed16AsciiU8" id="12" type="Fixed16AsciiU8"/>
    // <field name="fixed16Gb18030U8" id="13" type="Fixed16Gb18030U8"/>
    // <field name="fixed16Utf8U8" id="14" type="Fixed16Utf8U8"/>
    macro_rules! run_encode_then_decode_for_array_of_u8_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr) => {
            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice.into_iter().copied());

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx], decoded[each_idx],
                        "Item mismatched at {}/{}",
                        each_idx, cur_len
                    );
                }
                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx], uninit,
                        "Item should not be padded ZERO at {}/{}",
                        each_idx, cur_len
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_char_from_iter,
        DemoDecoder::fixed_16_char,
        DemoEncoder::fixed_16_char_end,
        DemoDecoder::fixed_16_char_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_ascii_char_from_iter,
        DemoDecoder::fixed_16_ascii_char,
        DemoEncoder::fixed_16_ascii_char_end,
        DemoDecoder::fixed_16_ascii_char_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_gb_18030_char_from_iter,
        DemoDecoder::fixed_16_gb_18030_char,
        DemoEncoder::fixed_16_gb_18030_char_end,
        DemoDecoder::fixed_16_gb_18030_char_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_utf_8_char_from_iter,
        DemoDecoder::fixed_16_utf_8_char,
        DemoEncoder::fixed_16_utf_8_char_end,
        DemoDecoder::fixed_16_utf_8_char_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_u8_from_iter,
        DemoDecoder::fixed_16_u8,
        DemoEncoder::fixed_16_u8_end,
        DemoDecoder::fixed_16_u8_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_ascii_u8_from_iter,
        DemoDecoder::fixed_16_ascii_u8,
        DemoEncoder::fixed_16_ascii_u8_end,
        DemoDecoder::fixed_16_ascii_u8_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_gb_18030_u8_from_iter,
        DemoDecoder::fixed_16_gb_18030_u8,
        DemoEncoder::fixed_16_gb_18030_u8_end,
        DemoDecoder::fixed_16_gb_18030_u8_end
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_utf_8_u8_from_iter,
        DemoDecoder::fixed_16_utf_8_u8,
        DemoEncoder::fixed_16_utf_8u_8_end,
        DemoDecoder::fixed_16_utf_8u_8_end
    );
}

#[test]
fn test_encode_then_decode_non_u8_signed_primitive_slice() {
    let uninit = 1u8;

    // <field name="fixed16i8" id="21" type="Fixed16i8"/>
    // <field name="fixed16i16" id="22" type="Fixed16i16"/>
    // <field name="fixed16i32" id="23" type="Fixed16i32"/>
    // <field name="fixed16i64" id="24" type="Fixed16i64"/>
    macro_rules! run_encode_then_decode_for_array_of_signed_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr, $i_type:ty, $existed:expr) => {
            let test_data = [
                &[] as &[$i_type],
                &[1 as $i_type] as &[$i_type],
                &[0 as $i_type] as &[$i_type],
                &[-1 as $i_type] as &[$i_type],
                &[-1, 1 as $i_type] as &[$i_type],
                &[-1, 0, 1 as $i_type] as &[$i_type],
                &[-2, -1, 1, 2 as $i_type] as &[$i_type],
                &[-2, -1, 0, 1, 2 as $i_type] as &[$i_type],
                &[-3, -2, -1, 1, 2, 3 as $i_type] as &[$i_type],
                &[-3, -2, -1, 0, 1, 2, 3 as $i_type] as &[$i_type],
                &[-4, -3, -2, -1, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[-4, -3, -2, -1, 0, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[-5, -4, -3, -2, -1, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[-6, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[-7, -6, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6, 7 as $i_type] as &[$i_type],
                &[
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7 as $i_type,
                ] as &[$i_type],
                &[
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    -9,
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
                &[
                    -9,
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
            ];

            let existed = $existed;

            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice.into_iter().copied());

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx],
                        decoded[each_idx],
                        "Item mismatched at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx],
                        existed,
                        "Item should not be padded ZERO at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i8_from_iter,
        DemoDecoder::fixed_16_i8,
        DemoEncoder::fixed_16_i8_end,
        DemoDecoder::fixed_16_i8_end,
        i8, i8::from_le_bytes([uninit])
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i16_from_iter,
        DemoDecoder::fixed_16_i16,
        DemoEncoder::fixed_16_i16_end,
        DemoDecoder::fixed_16_i16_end,
        i16, i16::from_le_bytes([uninit, uninit])
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i32_from_iter,
        DemoDecoder::fixed_16_i32,
        DemoEncoder::fixed_16_i32_end,
        DemoDecoder::fixed_16_i32_end,
        i32, i32::from_le_bytes([uninit, uninit, uninit, uninit])
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i64_from_iter,
        DemoDecoder::fixed_16_i64,
        DemoEncoder::fixed_16_i64_end,
        DemoDecoder::fixed_16_i64_end,
        i64, i64::from_le_bytes([uninit, uninit, uninit, uninit, uninit, uninit, uninit, uninit])
    );
}

#[test]
fn test_encode_then_decode_non_u8_unsigned_primitive_slice() {
    let uninit = 1u8;

    // <field name="fixed16u16" id="32" type="Fixed16u16"/>
    // <field name="fixed16u32" id="33" type="Fixed16u32"/>
    macro_rules! run_encode_then_decode_for_array_of_unsigned_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr, $i_type:ty, $existed:expr) => {
            let test_data = [
                &[] as &[$i_type],
                &[1 as $i_type] as &[$i_type],
                &[0 as $i_type] as &[$i_type],
                &[11 as $i_type] as &[$i_type],
                &[11, 1 as $i_type] as &[$i_type],
                &[11, 0, 1 as $i_type] as &[$i_type],
                &[12, 11, 1, 2 as $i_type] as &[$i_type],
                &[12, 11, 0, 1, 2 as $i_type] as &[$i_type],
                &[13, 12, 11, 1, 2, 3 as $i_type] as &[$i_type],
                &[13, 12, 11, 0, 1, 2, 3 as $i_type] as &[$i_type],
                &[14, 13, 12, 11, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[14, 13, 12, 11, 0, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[15, 14, 13, 12, 11, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[15, 14, 13, 12, 11, 0, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[16, 15, 14, 13, 12, 11, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[16, 15, 14, 13, 12, 11, 0, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[17, 16, 15, 14, 13, 12, 11, 1, 2, 3, 4, 5, 6, 7 as $i_type] as &[$i_type],
                &[
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7 as $i_type,
                ] as &[$i_type],
                &[
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    19,
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
                &[
                    19,
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
            ];

            let existed = $existed;

            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice.into_iter().copied());

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx],
                        decoded[each_idx],
                        "Item mismatched at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }

                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx],
                        existed,
                        "Item should not be padded ZERO at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u16_from_iter,
        DemoDecoder::fixed_16_u16,
        DemoEncoder::fixed_16_u16_end,
        DemoDecoder::fixed_16_u16_end,
        u16,
        u16::from_le_bytes([uninit, uninit])
    );
    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u32_from_iter,
        DemoDecoder::fixed_16_u32,
        DemoEncoder::fixed_16_u32_end,
        DemoDecoder::fixed_16_u32_end,
        u32,
        u32::from_le_bytes([uninit, uninit, uninit, uninit])
    );
    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u64_from_iter,
        DemoDecoder::fixed_16_u64,
        DemoEncoder::fixed_16_u64_end,
        DemoDecoder::fixed_16_u64_end,
        u64,
        u64::from_le_bytes([uninit, uninit, uninit, uninit, uninit, uninit, uninit, uninit])
    );
}

#[test]
fn test_encode_then_decode_u8_slice_padded() {
    let uninit = 1u8;

    let test_data = [
        b"" as &[u8],
        b"0" as &[u8],
        b"01" as &[u8],
        b"012" as &[u8],
        b"0123" as &[u8],
        b"01234" as &[u8],
        b"012345" as &[u8],
        b"0123456" as &[u8],
        b"01234567" as &[u8],
        b"012345678" as &[u8],
        b"0123456789" as &[u8],
        b"0123456789A" as &[u8],
        b"0123456789AB" as &[u8],
        b"0123456789ABC" as &[u8],
        b"0123456789ABCD" as &[u8],
        b"0123456789ABCDE" as &[u8],
        b"0123456789ABCDEF" as &[u8],
        b"0123456789abcdef" as &[u8],
        b"0123456789abcdef0" as &[u8],
        b"0123456789abcdef01" as &[u8],
        b"0123456789abcdef012" as &[u8],
        b"0123456789abcdef0123" as &[u8],
        b"0123456789abcdef01234" as &[u8],
        b"0123456789abcdef012345" as &[u8],
        b"0123456789abcdef0123456" as &[u8],
        b"0123456789abcdef01234567" as &[u8],
        b"0123456789abcdef012345678" as &[u8],
        b"0123456789abcdef0123456789" as &[u8],
        b"0123456789abcdef0123456789A" as &[u8],
        b"0123456789abcdef0123456789AB" as &[u8],
        b"0123456789abcdef0123456789ABC" as &[u8],
        b"0123456789abcdef0123456789ABCD" as &[u8],
        b"0123456789abcdef0123456789ABCDE" as &[u8],
        b"0123456789abcdef0123456789ABCDEF" as &[u8],
    ];

    // <field name="fixed16Char" id="1" type="Fixed16Char"/>
    // <field name="fixed16AsciiChar" id="2" type="Fixed16AsciiChar"/>
    // <field name="fixed16Gb18030Char" id="3" type="Fixed16Gb18030Char"/>
    // <field name="fixed16Utf8Char" id="4" type="Fixed16Utf8Char"/>
    // <field name="fixed16U8" id="11" type="Fixed16U8"/>
    // <field name="fixed16AsciiU8" id="12" type="Fixed16AsciiU8"/>
    // <field name="fixed16Gb18030U8" id="13" type="Fixed16Gb18030U8"/>
    // <field name="fixed16Utf8U8" id="14" type="Fixed16Utf8U8"/>
    macro_rules! run_encode_then_decode_for_array_of_u8_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr,) => {
            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice);

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx], decoded[each_idx],
                        "Item mismatched at {}/{}",
                        each_idx, cur_len
                    );
                }
                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx], 0,
                        "Item should be padded ZERO at {}/{}",
                        each_idx, cur_len
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_char_zero_padded,
        DemoDecoder::fixed_16_char,
        DemoEncoder::fixed_16_char_end,
        DemoDecoder::fixed_16_char_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_ascii_char_zero_padded,
        DemoDecoder::fixed_16_ascii_char,
        DemoEncoder::fixed_16_ascii_char_end,
        DemoDecoder::fixed_16_ascii_char_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_gb_18030_char_zero_padded,
        DemoDecoder::fixed_16_gb_18030_char,
        DemoEncoder::fixed_16_gb_18030_char_end,
        DemoDecoder::fixed_16_gb_18030_char_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_utf_8_char_zero_padded,
        DemoDecoder::fixed_16_utf_8_char,
        DemoEncoder::fixed_16_utf_8_char_end,
        DemoDecoder::fixed_16_utf_8_char_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_u8_zero_padded,
        DemoDecoder::fixed_16_u8,
        DemoEncoder::fixed_16_u8_end,
        DemoDecoder::fixed_16_u8_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_ascii_u8_zero_padded,
        DemoDecoder::fixed_16_ascii_u8,
        DemoEncoder::fixed_16_ascii_u8_end,
        DemoDecoder::fixed_16_ascii_u8_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_gb_18030_u8_zero_padded,
        DemoDecoder::fixed_16_gb_18030_u8,
        DemoEncoder::fixed_16_gb_18030_u8_end,
        DemoDecoder::fixed_16_gb_18030_u8_end,
    );
    run_encode_then_decode_for_array_of_u8_len_16!(
        DemoEncoder::fixed_16_utf_8_u8_zero_padded,
        DemoDecoder::fixed_16_utf_8_u8,
        DemoEncoder::fixed_16_utf_8u_8_end,
        DemoDecoder::fixed_16_utf_8u_8_end,
    );
}

#[test]
fn test_encode_then_decode_non_u8_signed_primitive_slice_padded() {
    let uninit = 1u8;

    // <field name="fixed16i8" id="21" type="Fixed16i8"/>
    // <field name="fixed16i16" id="22" type="Fixed16i16"/>
    // <field name="fixed16i32" id="23" type="Fixed16i32"/>
    // <field name="fixed16i64" id="24" type="Fixed16i64"/>
    macro_rules! run_encode_then_decode_for_array_of_signed_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr, $i_type:ty,) => {
            let test_data = [
                &[] as &[$i_type],
                &[1 as $i_type] as &[$i_type],
                &[0 as $i_type] as &[$i_type],
                &[-1 as $i_type] as &[$i_type],
                &[-1, 1 as $i_type] as &[$i_type],
                &[-1, 0, 1 as $i_type] as &[$i_type],
                &[-2, -1, 1, 2 as $i_type] as &[$i_type],
                &[-2, -1, 0, 1, 2 as $i_type] as &[$i_type],
                &[-3, -2, -1, 1, 2, 3 as $i_type] as &[$i_type],
                &[-3, -2, -1, 0, 1, 2, 3 as $i_type] as &[$i_type],
                &[-4, -3, -2, -1, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[-4, -3, -2, -1, 0, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[-5, -4, -3, -2, -1, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[-6, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[-7, -6, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6, 7 as $i_type] as &[$i_type],
                &[
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7 as $i_type,
                ] as &[$i_type],
                &[
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    -9,
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
                &[
                    -9,
                    -8,
                    -7,
                    -6,
                    -5,
                    -4,
                    -3,
                    -2,
                    -1,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
            ];
            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice);

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx],
                        decoded[each_idx],
                        "Item mismatched at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx],
                        0,
                        "Item should be padded ZERO at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i8_zero_padded,
        DemoDecoder::fixed_16_i8,
        DemoEncoder::fixed_16_i8_end,
        DemoDecoder::fixed_16_i8_end,
        i8,
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i16_zero_padded,
        DemoDecoder::fixed_16_i16,
        DemoEncoder::fixed_16_i16_end,
        DemoDecoder::fixed_16_i16_end,
        i16,
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i32_zero_padded,
        DemoDecoder::fixed_16_i32,
        DemoEncoder::fixed_16_i32_end,
        DemoDecoder::fixed_16_i32_end,
        i32,
    );
    run_encode_then_decode_for_array_of_signed_len_16!(
        DemoEncoder::fixed_16_i64_zero_padded,
        DemoDecoder::fixed_16_i64,
        DemoEncoder::fixed_16_i64_end,
        DemoDecoder::fixed_16_i64_end,
        i64,
    );
}

#[test]
fn test_encode_then_decode_non_u8_unsigned_primitive_slice_padded() {
    let uninit = 1u8;

    // <field name="fixed16u16" id="32" type="Fixed16u16"/>
    // <field name="fixed16u32" id="33" type="Fixed16u32"/>
    // <field name="fixed16u64" id="33" type="Fixed16u64"/>
    macro_rules! run_encode_then_decode_for_array_of_unsigned_len_16 {
        ($encode_func:expr, $decode_func:expr, $end_encode_func:expr, $end_decode_func:expr, $i_type:ty) => {
            let test_data = [
                &[] as &[$i_type],
                &[1 as $i_type] as &[$i_type],
                &[0 as $i_type] as &[$i_type],
                &[11 as $i_type] as &[$i_type],
                &[11, 1 as $i_type] as &[$i_type],
                &[11, 0, 1 as $i_type] as &[$i_type],
                &[12, 11, 1, 2 as $i_type] as &[$i_type],
                &[12, 11, 0, 1, 2 as $i_type] as &[$i_type],
                &[13, 12, 11, 1, 2, 3 as $i_type] as &[$i_type],
                &[13, 12, 11, 0, 1, 2, 3 as $i_type] as &[$i_type],
                &[14, 13, 12, 11, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[14, 13, 12, 11, 0, 1, 2, 3, 4 as $i_type] as &[$i_type],
                &[15, 14, 13, 12, 11, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[15, 14, 13, 12, 11, 0, 1, 2, 3, 4, 5 as $i_type] as &[$i_type],
                &[16, 15, 14, 13, 12, 11, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[16, 15, 14, 13, 12, 11, 0, 1, 2, 3, 4, 5, 6 as $i_type] as &[$i_type],
                &[17, 16, 15, 14, 13, 12, 11, 1, 2, 3, 4, 5, 6, 7 as $i_type] as &[$i_type],
                &[
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7 as $i_type,
                ] as &[$i_type],
                &[
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8 as $i_type,
                ] as &[$i_type],
                &[
                    19,
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
                &[
                    19,
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    12,
                    11,
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9 as $i_type,
                ] as &[$i_type],
            ];
            for each_slice in test_data {
                let encode_func = $encode_func;
                let decode_func = $decode_func;
                let end_encode_func = $end_encode_func;
                let end_decode_func = $end_decode_func;

                let cur_len = each_slice.len();
                let effective_len = cur_len.min(16);

                // encode...
                let mut buffer = [uninit; 1024];
                let mut encoder = create_encoder(&mut buffer);

                encode_func(&mut encoder, each_slice);

                let end = 1i32;
                end_encode_func(&mut encoder, end);

                // decode...
                let buf = ReadBuf::new(buffer.as_slice());
                let header = MessageHeaderDecoder::default().wrap(buf, 0);

                let decoder = DemoDecoder::default().header(header, 0);
                let decoded = decode_func(&decoder);
                for each_idx in 0..effective_len {
                    assert_eq!(
                        each_slice[each_idx],
                        decoded[each_idx],
                        "Item mismatched at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                for each_idx in effective_len..16 {
                    assert_eq!(
                        decoded[each_idx],
                        0,
                        "Item should be padded ZERO at {}/{} for {}",
                        each_idx,
                        cur_len,
                        stringify!($i_type)
                    );
                }
                let decoded_end = end_decode_func(&decoder);
                assert_eq!(decoded_end, end, "End Item should equal",);
            }
        };
    }

    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u16_zero_padded,
        DemoDecoder::fixed_16_u16,
        DemoEncoder::fixed_16_u16_end,
        DemoDecoder::fixed_16_u16_end,
        u16
    );
    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u32_zero_padded,
        DemoDecoder::fixed_16_u32,
        DemoEncoder::fixed_16_u32_end,
        DemoDecoder::fixed_16_u32_end,
        u32
    );
    run_encode_then_decode_for_array_of_unsigned_len_16!(
        DemoEncoder::fixed_16_u64_zero_padded,
        DemoDecoder::fixed_16_u64,
        DemoEncoder::fixed_16_u64_end,
        DemoDecoder::fixed_16_u64_end,
        u64
    );
}
