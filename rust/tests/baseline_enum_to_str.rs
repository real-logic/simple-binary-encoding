use examples_baseline::{
    boost_type::BoostType,
};

#[test]
fn test_boost_type_from_str() -> Result<(), ()> {
    assert_eq!(format!("{}", BoostType::TURBO), "TURBO", "Display \"TURBO\"");
    assert_eq!(format!("{}", BoostType::SUPERCHARGER), "SUPERCHARGER", "Display \"SUPERCHARGER\"");
    assert_eq!(format!("{}", BoostType::NITROUS), "NITROUS", "Display \"NITROUS\"");
    assert_eq!(format!("{}", BoostType::KERS), "KERS", "Display \"KERS\"");
    assert_eq!(format!("{}", BoostType::NullVal), "NullVal", "Display \"NullVal\"");

    Ok(())
}
