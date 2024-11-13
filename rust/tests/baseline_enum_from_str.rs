use examples_baseline::{
    boost_type::BoostType,
};

#[test]
fn test_boost_type_from_str() -> Result<(), ()> {
    assert_eq!("TURBO".parse::<BoostType>()?, BoostType::TURBO, "Parse \"TURBO\" as BoostType");
    assert_eq!("SUPERCHARGER".parse::<BoostType>()?, BoostType::SUPERCHARGER, "Parse \"SUPERCHARGER\" as BoostType");
    assert_eq!("NITROUS".parse::<BoostType>()?, BoostType::NITROUS, "Parse \"NITROUS\" as BoostType");
    assert_eq!("KERS".parse::<BoostType>()?, BoostType::KERS, "Parse \"KERS\" as BoostType");

    assert_eq!("Turbo".parse::<BoostType>()?, BoostType::NullVal, "Parse \"Turbo\" as BoostType");
    assert_eq!("Supercharger".parse::<BoostType>()?, BoostType::NullVal, "Parse \"Supercharger\" as BoostType");
    assert_eq!("Nitrous".parse::<BoostType>()?, BoostType::NullVal, "Parse \"Nitrous\" as BoostType");
    assert_eq!("Kers".parse::<BoostType>()?, BoostType::NullVal, "Parse \"Kers\" as BoostType");

    assert_eq!("AA".parse::<BoostType>()?, BoostType::NullVal, "Parse \"AA\" as BoostType");
    assert_eq!("".parse::<BoostType>().unwrap(), BoostType::NullVal, "Parse \"\" as BoostType");

    Ok(())
}
