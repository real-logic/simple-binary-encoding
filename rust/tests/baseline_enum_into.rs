use examples_baseline::{
    boost_type::BoostType,
};

#[test]
fn test_boost_type_from_str() -> Result<(), ()> {
    assert_eq!(<BoostType as Into<u8>>::into(BoostType::TURBO), 84_u8, "BoostType::TURBO into value");
    assert_eq!(<BoostType as Into<u8>>::into(BoostType::SUPERCHARGER), 83_u8, "BoostType::SUPERCHARGER into value");
    assert_eq!(<BoostType as Into<u8>>::into(BoostType::NITROUS), 78_u8, "BoostType::NITROUS into value");
    assert_eq!(<BoostType as Into<u8>>::into(BoostType::KERS), 75_u8, "BoostType::KERS into value");
    assert_eq!(<BoostType as Into<u8>>::into(BoostType::NullVal), 0_u8, "BoostType::NullVal into value");

    Ok(())
}
