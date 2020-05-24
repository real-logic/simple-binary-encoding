/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <string>

#include <gtest/gtest.h>

#include <code_generation_test/Car.h>
#include <code_generation_test/messageHeader.h>
#include <stdexcept>

#define CGT(name) code_generation_test_##name

static const std::size_t BUFFER_LEN = 2048;

static const std::uint32_t SERIAL_NUMBER = 1234;
static const std::uint16_t MODEL_YEAR = 2013;
static const CGT(BooleanType) AVAILABLE = CGT(BooleanType_T);
static const CGT(Model) CODE = CGT(Model_A);
static const bool CRUISE_CONTROL = true;
static const bool SPORTS_PACK = true;
static const bool SUNROOF = false;
static const CGT(BoostType) BOOST_TYPE = CGT(BoostType_NITROUS);
static const std::uint8_t BOOSTER_HORSEPOWER = 200;

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char FUEL_FIGURES_1_USAGE_DESCRIPTION[] = "Urban Cycle";
static const char FUEL_FIGURES_2_USAGE_DESCRIPTION[] = "Combined Cycle";
static const char FUEL_FIGURES_3_USAGE_DESCRIPTION[] = "Highway Cycle";
static const char MANUFACTURER[] = "Honda";
static const char MODEL[] = "Civic VTi";
static const char ACTIVATION_CODE[] = "deadbeef";

static const std::uint64_t VEHICLE_CODE_LENGTH = sizeof(VEHICLE_CODE);
static const std::uint64_t MANUFACTURER_CODE_LENGTH = sizeof(MANUFACTURER_CODE);
static const std::uint64_t FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH = 11;
static const std::uint64_t FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH = 14;
static const std::uint64_t FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH = 13;
static const std::uint64_t MANUFACTURER_LENGTH = 5;
static const std::uint64_t MODEL_LENGTH = 9;
static const std::uint64_t ACTIVATION_CODE_LENGTH = 8;
static const std::uint8_t PERFORMANCE_FIGURES_COUNT = 2;
static const std::uint8_t FUEL_FIGURES_COUNT = 3;
static const std::uint8_t ACCELERATION_COUNT = 3;

static const std::uint64_t expectedHeaderSize = 8;
static const std::uint64_t expectedCarSize = 191;

static const std::uint16_t fuel1Speed = 30;
static const float fuel1Mpg = 35.9f;
static const std::uint16_t fuel2Speed = 55;
static const float fuel2Mpg = 49.0f;
static const std::uint16_t fuel3Speed = 75;
static const float fuel3Mpg = 40.0f;

static const std::uint8_t perf1Octane = 95;
static const std::uint16_t perf1aMph = 30;
static const float perf1aSeconds = 4.0f;
static const std::uint16_t perf1bMph = 60;
static const float perf1bSeconds = 7.5f;
static const std::uint16_t perf1cMph = 100;
static const float perf1cSeconds = 12.2f;

static const std::uint8_t perf2Octane = 99;
static const std::uint16_t perf2aMph = 30;
static const float perf2aSeconds = 3.8f;
static const std::uint16_t perf2bMph = 60;
static const float perf2bSeconds = 7.1f;
static const std::uint16_t perf2cMph = 100;
static const float perf2cSeconds = 11.8f;

static const std::uint16_t engineCapacity = 2000;
static const std::uint8_t engineNumCylinders = 4;

class CodeGenTest : public testing::Test
{
public:
    static std::uint64_t encodeHdr(CGT(messageHeader)& hdr)
    {
        CGT(messageHeader_set_blockLength)(&hdr, CGT(Car_sbe_block_length)());
        CGT(messageHeader_set_templateId)(&hdr, CGT(Car_sbe_template_id)());
        CGT(messageHeader_set_schemaId)(&hdr, CGT(Car_sbe_schema_id)());
        CGT(messageHeader_set_version)(&hdr, CGT(Car_sbe_schema_version)());

        return CGT(messageHeader_encoded_length)();
    }

    static std::uint64_t encodeCar(CGT(Car)& car)
    {
        CGT(Car_set_serialNumber)(&car, SERIAL_NUMBER);
        CGT(Car_set_modelYear)(&car, MODEL_YEAR);
        CGT(Car_set_available)(&car, AVAILABLE);
        CGT(Car_set_code)(&car, CODE);
        CGT(Car_put_vehicleCode)(&car, VEHICLE_CODE);

        for (std::uint64_t i = 0; i < CGT(Car_someNumbers_length)(); i++)
        {
            CGT(Car_set_someNumbers_unsafe)(&car, i, static_cast<std::int32_t>(i));
        }

        CGT(OptionalExtras) extras;
        if (!CGT(Car_extras)(&car, &extras))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(OptionalExtras_clear)(&extras);
        CGT(OptionalExtras_set_cruiseControl)(&extras, CRUISE_CONTROL);
        CGT(OptionalExtras_set_sportsPack)(&extras, SPORTS_PACK);
        CGT(OptionalExtras_set_sunRoof)(&extras, SUNROOF);

        CGT(Engine) engine;
        if (!CGT(Car_engine)(&car, &engine))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(Engine_set_capacity)(&engine, engineCapacity);
        CGT(Engine_set_numCylinders)(&engine, engineNumCylinders);
        CGT(Engine_put_manufacturerCode)(&engine, MANUFACTURER_CODE);

        CGT(BoosterT) booster;
        if (!CGT(Engine_booster)(&engine, &booster))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(BoosterT_set_BoostType)(&booster, BOOST_TYPE);
        CGT(BoosterT_set_horsePower)(&booster, BOOSTER_HORSEPOWER);

        CGT(Car_fuelFigures) fuelFigures;
        if (!CGT(Car_fuelFigures_set_count)(&car, &fuelFigures, FUEL_FIGURES_COUNT))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(Car_fuelFigures_next)(&fuelFigures);
        CGT(Car_fuelFigures_set_speed)(&fuelFigures, fuel1Speed);
        CGT(Car_fuelFigures_set_mpg)(&fuelFigures, fuel1Mpg);
        CGT(Car_fuelFigures_put_usageDescription)(
            &fuelFigures,
            FUEL_FIGURES_1_USAGE_DESCRIPTION,
            static_cast<int>(strlen(FUEL_FIGURES_1_USAGE_DESCRIPTION)));

        CGT(Car_fuelFigures_next)(&fuelFigures);
        CGT(Car_fuelFigures_set_speed)(&fuelFigures, fuel2Speed);
        CGT(Car_fuelFigures_set_mpg)(&fuelFigures, fuel2Mpg);
        CGT(Car_fuelFigures_put_usageDescription)(
            &fuelFigures,
            FUEL_FIGURES_2_USAGE_DESCRIPTION,
            static_cast<int>(strlen(FUEL_FIGURES_2_USAGE_DESCRIPTION)));

        CGT(Car_fuelFigures_next)(&fuelFigures);
        CGT(Car_fuelFigures_set_speed)(&fuelFigures, fuel3Speed);
        CGT(Car_fuelFigures_set_mpg)(&fuelFigures, fuel3Mpg);
        CGT(Car_fuelFigures_put_usageDescription)(
            &fuelFigures,
            FUEL_FIGURES_3_USAGE_DESCRIPTION,
            static_cast<int>(strlen(FUEL_FIGURES_3_USAGE_DESCRIPTION)));

        CGT(Car_performanceFigures) perfFigs;
        if (!CGT(Car_performanceFigures_set_count)(
                &car,
                &perfFigs,
                PERFORMANCE_FIGURES_COUNT))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(Car_performanceFigures_next)(&perfFigs);
        CGT(Car_performanceFigures_set_octaneRating)(&perfFigs, perf1Octane);

        CGT(Car_performanceFigures_acceleration) acc;
        if (!CGT(Car_performanceFigures_acceleration_set_count)(&perfFigs, &acc, ACCELERATION_COUNT))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf1aMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf1aSeconds);
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf1bMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf1bSeconds);
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf1cMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf1cSeconds);

        CGT(Car_performanceFigures_next)(&perfFigs);
        CGT(Car_performanceFigures_set_octaneRating)(&perfFigs, perf2Octane);

        if (!CGT(Car_performanceFigures_acceleration_set_count)(&perfFigs, &acc, ACCELERATION_COUNT))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf2aMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf2aSeconds);
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf2bMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf2bSeconds);
        CGT(Car_performanceFigures_acceleration_next)(&acc);
        CGT(Car_performanceFigures_acceleration_set_mph)(&acc, perf2cMph);
        CGT(Car_performanceFigures_acceleration_set_seconds)(&acc, perf2cSeconds);

        CGT(Car_put_manufacturer)(&car, MANUFACTURER, static_cast<int>(strlen(MANUFACTURER)));
        CGT(Car_put_model)(&car, MODEL, static_cast<int>(strlen(MODEL)));
        CGT(Car_put_activationCode)(&car, ACTIVATION_CODE, static_cast<int>(strlen(ACTIVATION_CODE)));

        return CGT(Car_encoded_length)(&car);
    }

    std::uint64_t encodeHdr(char *buffer, std::uint64_t offset, std::uint64_t bufferLength)
    {
        if (!CGT(messageHeader_wrap)(&m_hdr, buffer, offset, 0, bufferLength))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        return encodeHdr(m_hdr);
    }

    std::uint64_t encodeCar(char *buffer, std::uint64_t offset, std::uint64_t bufferLength)
    {
        if (!CGT(Car_wrap_for_encode)(&m_car, buffer, offset, bufferLength))
        {
            throw std::runtime_error(sbe_strerror(errno));
        }
        return encodeCar(m_car);
    }

    CGT(messageHeader) m_hdr;
    CGT(messageHeader) m_hdrDecoder;
    CGT(Car) m_car;
    CGT(Car) m_carDecoder;
};

TEST_F(CodeGenTest, shouldReturnCorrectValuesForMessageHeaderStaticFields)
{
    EXPECT_EQ(CGT(messageHeader_encoded_length)(), 8u);
    // only checking the block length field
    EXPECT_EQ(CGT(messageHeader_blockLength_null_value)(), 65535);
    EXPECT_EQ(CGT(messageHeader_blockLength_min_value)(), 0);
    EXPECT_EQ(CGT(messageHeader_blockLength_max_value)(), 65534);
}

TEST_F(CodeGenTest, shouldReturnCorrectValuesForCarStaticFields)
{
    EXPECT_EQ(CGT(Car_sbe_block_length)(), 47u);
    EXPECT_EQ(CGT(Car_sbe_template_id)(), 1u);
    EXPECT_EQ(CGT(Car_sbe_schema_id)(), 6u);
    EXPECT_EQ(CGT(Car_sbe_schema_version)(), 0u);
    EXPECT_EQ(std::string(CGT(Car_sbe_semantic_type)()), std::string(""));
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeMessageHeaderCorrectly)
{
    char buffer[BUFFER_LEN];
    const char *bp = buffer;

    std::uint64_t sz = encodeHdr(buffer, 0, sizeof(buffer));

    EXPECT_EQ(*((uint16_t *)bp), CGT(Car_sbe_block_length)());
    EXPECT_EQ(*((uint16_t *)(bp + 2)), CGT(Car_sbe_template_id)());
    EXPECT_EQ(*((uint16_t *)(bp + 4)), CGT(Car_sbe_schema_id)());
    EXPECT_EQ(*((uint16_t *)(bp + 6)), CGT(Car_sbe_schema_version)());
    EXPECT_EQ(sz, 8u);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeAndDecodeMessageHeaderCorrectly)
{
    char buffer[BUFFER_LEN];

    encodeHdr(buffer, 0, sizeof(buffer));

    if (!CGT(messageHeader_wrap)(&m_hdrDecoder, buffer, 0, 0, sizeof(buffer)))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    EXPECT_EQ(CGT(messageHeader_blockLength)(&m_hdrDecoder), CGT(Car_sbe_block_length)());
    EXPECT_EQ(CGT(messageHeader_templateId)(&m_hdrDecoder), CGT(Car_sbe_template_id)());
    EXPECT_EQ(CGT(messageHeader_schemaId)(&m_hdrDecoder), CGT(Car_sbe_schema_id)());
    EXPECT_EQ(CGT(messageHeader_version)(&m_hdrDecoder), CGT(Car_sbe_schema_version)());
}

static const uint8_t fieldIdSerialNumber = 1;
static const uint8_t fieldIdModelYear = 2;
static const uint8_t fieldIdAvailable = 3;
static const uint8_t fieldIdCode = 4;
static const uint8_t fieldIdSomeNumbers = 5;
static const uint8_t fieldIdVehicleCode = 6;
static const uint8_t fieldIdExtras = 7;
static const uint8_t fieldIdDiscountedModel = 8;
static const uint8_t fieldIdEngine = 9;
static const uint8_t fieldIdFuelFigures = 10;
static const uint8_t fieldIdFuelSpeed = 11;
static const uint8_t fieldIdFuelMpg = 12;
static const uint8_t fieldIdFuelUsageDescription = 200;
static const uint8_t fieldIdPerformanceFigures = 13;
static const uint8_t fieldIdPerfOctaneRating = 14;
static const uint8_t fieldIdPerfAcceleration = 15;
static const uint8_t fieldIdPerfAccMph = 16;
static const uint8_t fieldIdPerfAccSeconds = 17;
static const uint8_t fieldIdManufacturer = 18;
static const uint8_t fieldIdModel = 19;
static const uint8_t fieldIdActivationCode = 20;

TEST_F(CodeGenTest, shouldReturnCorrectValuesForCarFieldIdsAndCharacterEncoding)
{
    EXPECT_EQ(CGT(Car_serialNumber_id)(), fieldIdSerialNumber);
    EXPECT_EQ(CGT(Car_modelYear_id)(), fieldIdModelYear);
    EXPECT_EQ(CGT(Car_available_id)(), fieldIdAvailable);
    EXPECT_EQ(CGT(Car_code_id)(), fieldIdCode);
    EXPECT_EQ(CGT(Car_someNumbers_id)(), fieldIdSomeNumbers);
    EXPECT_EQ(CGT(Car_vehicleCode_id)(), fieldIdVehicleCode);
    EXPECT_EQ(CGT(Car_extras_id)(), fieldIdExtras);
    EXPECT_EQ(CGT(Car_discountedModel_id)(), fieldIdDiscountedModel);
    EXPECT_EQ(CGT(Car_engine_id)(), fieldIdEngine);
    EXPECT_EQ(CGT(Car_fuelFigures_id)(), fieldIdFuelFigures);
    EXPECT_EQ(CGT(Car_fuelFigures_speed_id)(), fieldIdFuelSpeed);
    EXPECT_EQ(CGT(Car_fuelFigures_mpg_id)(), fieldIdFuelMpg);
    EXPECT_EQ(CGT(Car_fuelFigures_usageDescription_id)(), fieldIdFuelUsageDescription);
    EXPECT_EQ(CGT(Car_fuelFigures_usageDescription_character_encoding)(), std::string("UTF-8"));
    EXPECT_EQ(CGT(Car_performanceFigures_id)(), fieldIdPerformanceFigures);
    EXPECT_EQ(CGT(Car_performanceFigures_octaneRating_id)(), fieldIdPerfOctaneRating);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_id)(), fieldIdPerfAcceleration);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph_id)(), fieldIdPerfAccMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds_id)(), fieldIdPerfAccSeconds);
    EXPECT_EQ(CGT(Car_manufacturer_id)(), fieldIdManufacturer);
    EXPECT_EQ(CGT(Car_model_id)(), fieldIdModel);
    EXPECT_EQ(CGT(Car_activationCode_id)(), fieldIdActivationCode);
    EXPECT_EQ(std::string(CGT(Car_manufacturer_character_encoding())), std::string("UTF-8"));
    EXPECT_EQ(std::string(CGT(Car_model_character_encoding())), std::string("UTF-8"));
    EXPECT_EQ(std::string(CGT(Car_activationCode_character_encoding())), std::string("UTF-8"));
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeCarCorrectly)
{
    char buffer[BUFFER_LEN];
    const char *bp = buffer;
    std::uint64_t sz = encodeCar(buffer, 0, sizeof(buffer));

    std::uint64_t offset = 0;
    EXPECT_EQ(*(std::uint64_t *)(bp + offset), SERIAL_NUMBER);
    offset += sizeof(std::uint64_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), MODEL_YEAR);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), 1);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(*(bp + offset), 'A');
    offset += sizeof(char);

    EXPECT_EQ(*(std::int32_t *)(bp + offset), 0);
    offset += sizeof(std::int32_t);
    EXPECT_EQ(*(std::int32_t *)(bp + offset), 1);
    offset += sizeof(std::int32_t);
    EXPECT_EQ(*(std::int32_t *)(bp + offset), 2);
    offset += sizeof(std::int32_t);
    EXPECT_EQ(*(std::int32_t *)(bp + offset), 3);
    offset += sizeof(std::int32_t);
    EXPECT_EQ(*(std::int32_t *)(bp + offset), 4);
    offset += sizeof(std::int32_t);

    EXPECT_EQ(std::string(bp + offset, VEHICLE_CODE_LENGTH), std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));
    offset += VEHICLE_CODE_LENGTH;
    EXPECT_EQ(*(bp + offset), 0x6);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), engineCapacity);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(bp + offset), engineNumCylinders);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MANUFACTURER_CODE_LENGTH), std::string(MANUFACTURER_CODE, MANUFACTURER_CODE_LENGTH));
    offset += MANUFACTURER_CODE_LENGTH;
    EXPECT_EQ(*(bp + offset), 'N');
    offset += sizeof(char);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), BOOSTER_HORSEPOWER);
    offset += sizeof(std::uint8_t);

    // fuel figures
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 6);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), FUEL_FIGURES_COUNT);
    offset += sizeof(std::uint16_t);

    EXPECT_EQ(*(::uint16_t *)(bp + offset), fuel1Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel1Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(
        std::string(bp + offset, FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_1_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH;

    EXPECT_EQ(*(std::uint16_t *)(bp + offset), fuel2Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel2Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(
        std::string(bp + offset, FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_2_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH;

    EXPECT_EQ(*(std::uint16_t *)(bp + offset), fuel3Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel3Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(
        std::string(bp + offset, FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_3_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH;

    // performance figures
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 1);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), PERFORMANCE_FIGURES_COUNT);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(bp + offset), perf1Octane);
    offset += sizeof(std::uint8_t);
    // acceleration
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 6);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf1aMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1aSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf1bMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1bSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf1cMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1cSeconds);
    offset += sizeof(float);

    EXPECT_EQ(*(bp + offset), perf2Octane);
    offset += sizeof(std::uint8_t);
    // acceleration
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 6);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf2aMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2aSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf2bMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2bSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), perf2cMph);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2cSeconds);
    offset += sizeof(float);

    // manufacturer & model
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), MANUFACTURER_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(std::string(bp + offset, MANUFACTURER_LENGTH), MANUFACTURER);
    offset += MANUFACTURER_LENGTH;
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), MODEL_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(std::string(bp + offset, MODEL_LENGTH), MODEL);
    offset += MODEL_LENGTH;
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), ACTIVATION_CODE_LENGTH);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(std::string(bp + offset, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);
    offset += ACTIVATION_CODE_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeHeaderPlusCarCorrectly)
{
    char buffer[BUFFER_LEN];
    const char *bp = buffer;

    std::uint64_t hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    std::uint64_t carSz = encodeCar(
        buffer, CGT(messageHeader_encoded_length)(), sizeof(buffer) - CGT(messageHeader_encoded_length)());

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    EXPECT_EQ(*((std::uint16_t *)bp), CGT(Car_sbe_block_length)());
    const size_t activationCodePosition = hdrSz + carSz - ACTIVATION_CODE_LENGTH;
    const size_t activationCodeLengthPosition = activationCodePosition - sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint16_t *)(bp + activationCodeLengthPosition), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(bp + activationCodePosition, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeAndDecodeHeaderPlusCarCorrectly)
{
    char buffer[BUFFER_LEN];

    std::uint64_t hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    std::uint64_t carSz = encodeCar(
        buffer, CGT(messageHeader_encoded_length)(), sizeof(buffer) - CGT(messageHeader_encoded_length)());

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    if (!CGT(messageHeader_wrap)(&m_hdrDecoder, buffer, 0, 0, hdrSz))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_EQ(CGT(messageHeader_blockLength)(&m_hdrDecoder), CGT(Car_sbe_block_length)());
    EXPECT_EQ(CGT(messageHeader_templateId)(&m_hdrDecoder), CGT(Car_sbe_template_id)());
    EXPECT_EQ(CGT(messageHeader_schemaId)(&m_hdrDecoder), CGT(Car_sbe_schema_id)());
    EXPECT_EQ(CGT(messageHeader_version)(&m_hdrDecoder), CGT(Car_sbe_schema_version)());
    EXPECT_EQ(CGT(messageHeader_encoded_length)(), expectedHeaderSize);

    if (!CGT(Car_wrap_for_decode)(
        &m_carDecoder,
        buffer,
        CGT(messageHeader_encoded_length)(),
        CGT(Car_sbe_block_length)(),
        CGT(Car_sbe_schema_version)(),
        hdrSz + carSz))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_EQ(CGT(Car_serialNumber)(&m_carDecoder), SERIAL_NUMBER);
    EXPECT_EQ(CGT(Car_modelYear)(&m_carDecoder), MODEL_YEAR);
    {
        CGT(BooleanType) out = CGT(BooleanType_NULL_VALUE);
        ASSERT_TRUE(CGT(Car_available)(&m_carDecoder, &out));
        EXPECT_EQ(out, AVAILABLE);
    }
    {
        CGT(Model) out = CGT(Model_NULL_VALUE);
        ASSERT_TRUE(CGT(Car_code)(&m_carDecoder, &out));
        EXPECT_EQ(out, CODE);
    }
    EXPECT_EQ(CGT(Car_someNumbers_length)(), 5u);
    for (std::uint64_t i = 0; i < 5; i++)
    {
        EXPECT_EQ(CGT(Car_someNumbers_unsafe)(&m_carDecoder, i), static_cast<std::int32_t>(i));
    }

    EXPECT_EQ(CGT(Car_vehicleCode_length)(), VEHICLE_CODE_LENGTH);
    EXPECT_EQ(
        std::string(CGT(Car_vehicleCode_buffer)(&m_carDecoder), VEHICLE_CODE_LENGTH),
        std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));

    CGT(OptionalExtras) extras;
    if (!CGT(Car_extras)(&m_carDecoder, &extras))
    {
         throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_TRUE(CGT(OptionalExtras_cruiseControl)(&extras));
    EXPECT_TRUE(CGT(OptionalExtras_sportsPack)(&extras));
    EXPECT_FALSE(CGT(OptionalExtras_sunRoof)(&extras));
    EXPECT_EQ(CGT(Car_discountedModel)(&m_carDecoder), CGT(Model_C));

    CGT(Engine) engine;
    if (!CGT(Car_engine)(&m_carDecoder, &engine))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    EXPECT_EQ(CGT(Engine_capacity)(&engine), engineCapacity);
    EXPECT_EQ(CGT(Engine_numCylinders)(&engine), engineNumCylinders);
    EXPECT_EQ(CGT(Engine_maxRpm)(), 9000);
    EXPECT_EQ(CGT(Engine_manufacturerCode_length)(), MANUFACTURER_CODE_LENGTH);
    EXPECT_EQ(
        std::string(CGT(Engine_manufacturerCode_buffer)(&engine), MANUFACTURER_CODE_LENGTH),
        std::string(MANUFACTURER_CODE, MANUFACTURER_CODE_LENGTH));
    EXPECT_EQ(CGT(Engine_fuel_length)(), 6u);
    EXPECT_EQ(std::string(CGT(Engine_fuel)(), 6), std::string("Petrol"));

    CGT(Car_fuelFigures) fuelFigures;
    CGT(Car_get_fuelFigures)(&m_carDecoder, &fuelFigures);
    EXPECT_EQ(CGT(Car_fuelFigures_count)(&fuelFigures), FUEL_FIGURES_COUNT);

    ASSERT_TRUE(CGT(Car_fuelFigures_has_next)(&fuelFigures));
    CGT(Car_fuelFigures_next)(&fuelFigures);
    EXPECT_EQ(CGT(Car_fuelFigures_speed)(&fuelFigures), fuel1Speed);
    EXPECT_EQ(CGT(Car_fuelFigures_mpg)(&fuelFigures), fuel1Mpg);
    EXPECT_EQ(CGT(Car_fuelFigures_usageDescription_length)(&fuelFigures), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(
        std::string(CGT(Car_fuelFigures_usageDescription)(&fuelFigures), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH),
        FUEL_FIGURES_1_USAGE_DESCRIPTION);

    ASSERT_TRUE(CGT(Car_fuelFigures_has_next)(&fuelFigures));
    CGT(Car_fuelFigures_next)(&fuelFigures);
    EXPECT_EQ(CGT(Car_fuelFigures_speed)(&fuelFigures), fuel2Speed);
    EXPECT_EQ(CGT(Car_fuelFigures_mpg)(&fuelFigures), fuel2Mpg);
    EXPECT_EQ(CGT(Car_fuelFigures_usageDescription_length)(&fuelFigures), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(
        std::string(CGT(Car_fuelFigures_usageDescription)(&fuelFigures), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH),
        FUEL_FIGURES_2_USAGE_DESCRIPTION);

    ASSERT_TRUE(CGT(Car_fuelFigures_has_next)(&fuelFigures));
    CGT(Car_fuelFigures_next)(&fuelFigures);
    EXPECT_EQ(CGT(Car_fuelFigures_speed)(&fuelFigures), fuel3Speed);
    EXPECT_EQ(CGT(Car_fuelFigures_mpg)(&fuelFigures), fuel3Mpg);
    EXPECT_EQ(CGT(Car_fuelFigures_usageDescription_length)(&fuelFigures), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(
        std::string(CGT(Car_fuelFigures_usageDescription)(&fuelFigures), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH),
        FUEL_FIGURES_3_USAGE_DESCRIPTION);

    CGT(Car_performanceFigures) performanceFigures;
    CGT(Car_get_performanceFigures)(&m_carDecoder, &performanceFigures);
    EXPECT_EQ(CGT(Car_performanceFigures_count)(&performanceFigures), PERFORMANCE_FIGURES_COUNT);

    ASSERT_TRUE(CGT(Car_performanceFigures_has_next)(&performanceFigures));
    CGT(Car_performanceFigures_next)(&performanceFigures);
    EXPECT_EQ(CGT(Car_performanceFigures_octaneRating)(&performanceFigures), perf1Octane);

    CGT(Car_performanceFigures_acceleration) acc;
    CGT(Car_performanceFigures_get_acceleration)(&performanceFigures, &acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_count)(&acc), ACCELERATION_COUNT);
    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf1aMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf1aSeconds);

    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf1bMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf1bSeconds);

    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf1cMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf1cSeconds);

    ASSERT_TRUE(CGT(Car_performanceFigures_has_next)(&performanceFigures));
    CGT(Car_performanceFigures_next)(&performanceFigures);
    EXPECT_EQ(CGT(Car_performanceFigures_octaneRating)(&performanceFigures), perf2Octane);

    CGT(Car_performanceFigures_get_acceleration)(&performanceFigures, &acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_count)(&acc), ACCELERATION_COUNT);
    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf2aMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf2aSeconds);

    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf2bMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf2bSeconds);

    ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_has_next)(&acc));
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_mph)(&acc), perf2cMph);
    EXPECT_EQ(CGT(Car_performanceFigures_acceleration_seconds)(&acc), perf2cSeconds);

    EXPECT_EQ(CGT(Car_manufacturer_length)(&m_carDecoder), MANUFACTURER_LENGTH);
    EXPECT_EQ(std::string(CGT(Car_manufacturer)(&m_carDecoder), MANUFACTURER_LENGTH), MANUFACTURER);

    EXPECT_EQ(CGT(Car_model_length)(&m_carDecoder), MODEL_LENGTH);
    EXPECT_EQ(std::string(CGT(Car_model)(&m_carDecoder), MODEL_LENGTH), MODEL);

    EXPECT_EQ(CGT(Car_activationCode_length)(&m_carDecoder), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(CGT(Car_activationCode)(&m_carDecoder), ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);

    EXPECT_EQ(CGT(Car_encoded_length)(&m_carDecoder), expectedCarSize);
}

struct CallbacksForEach
{
    int countOfFuelFigures;
    int countOfPerformanceFigures;
    int countOfAccelerations;

    CallbacksForEach() : countOfFuelFigures(0), countOfPerformanceFigures(0), countOfAccelerations(0) {}
};

TEST_F(CodeGenTest, shouldBeAbleUseOnStackCodecsAndGroupForEach)
{
    char buffer[BUFFER_LEN];
    CGT(messageHeader) hdr;
    if (!CGT(messageHeader_reset)(&hdr, buffer, 0, sizeof(buffer), 0))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    CGT(Car) car;
    if (!CGT(Car_reset)(
        &car,
        buffer + CGT(messageHeader_encoded_length)(),
        0,
        sizeof(buffer) - CGT(messageHeader_encoded_length)(),
        CGT(Car_sbe_block_length)(),
        CGT(Car_sbe_schema_version)()))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    std::uint64_t hdrSz = encodeHdr(hdr);
    std::uint64_t carSz = encodeCar(car);

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    CGT(messageHeader) hdrDecoder;
    if (!CGT(messageHeader_reset)(&hdrDecoder, buffer, 0, hdrSz, 0))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_EQ(CGT(messageHeader_blockLength)(&hdrDecoder), CGT(Car_sbe_block_length)());
    EXPECT_EQ(CGT(messageHeader_templateId)(&hdrDecoder), CGT(Car_sbe_template_id)());
    EXPECT_EQ(CGT(messageHeader_schemaId)(&hdrDecoder), CGT(Car_sbe_schema_id)());
    EXPECT_EQ(CGT(messageHeader_version)(&hdrDecoder), CGT(Car_sbe_schema_version)());
    EXPECT_EQ(CGT(messageHeader_encoded_length)(), expectedHeaderSize);

    CGT(Car) carDecoder;
    if (!CGT(Car_reset)(
        &carDecoder,
        buffer + CGT(messageHeader_encoded_length)(),
        0,
        carSz,
        CGT(Car_sbe_block_length)(),
        CGT(Car_sbe_schema_version)()))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    CallbacksForEach cbs;

    CGT(Car_fuelFigures) fuelFigures;
    if (!CGT(Car_get_fuelFigures)(&carDecoder, &fuelFigures))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_EQ(CGT(Car_fuelFigures_count)(&fuelFigures), FUEL_FIGURES_COUNT);

    ASSERT_TRUE(CGT(Car_fuelFigures_for_each)(
        &fuelFigures,
        [](CGT(Car_fuelFigures) *const figures, void *cbs)
        {
            reinterpret_cast<CallbacksForEach*>(cbs)->countOfFuelFigures++;

            char tmp[256];
            CGT(Car_fuelFigures_get_usageDescription)(figures, tmp, sizeof(tmp));
        },
        &cbs));

    CGT(Car_performanceFigures) performanceFigures;
    if (!CGT(Car_get_performanceFigures)(&carDecoder, &performanceFigures))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    EXPECT_EQ(CGT(Car_performanceFigures_count)(&performanceFigures), PERFORMANCE_FIGURES_COUNT);

    ASSERT_TRUE(CGT(Car_performanceFigures_for_each)(
        &performanceFigures,
        [](CGT(Car_performanceFigures) *const figures, void *cbs)
        {
            CGT(Car_performanceFigures_acceleration) acceleration;
            if (!CGT(Car_performanceFigures_get_acceleration(figures, &acceleration)))
            {
                throw std::runtime_error(sbe_strerror(errno));
            }
            reinterpret_cast<CallbacksForEach*>(cbs)->countOfPerformanceFigures++;
            ASSERT_TRUE(CGT(Car_performanceFigures_acceleration_for_each)(
                &acceleration,
                [](CGT(Car_performanceFigures_acceleration) *const, void *cbs)
                {
                    reinterpret_cast<CallbacksForEach*>(cbs)->countOfAccelerations++;
                },
                cbs
            ));
        },
        &cbs));

    EXPECT_EQ(cbs.countOfFuelFigures, FUEL_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfPerformanceFigures, PERFORMANCE_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfAccelerations, ACCELERATION_COUNT * PERFORMANCE_FIGURES_COUNT);

    char tmp[256];

    EXPECT_EQ(CGT(Car_get_manufacturer)(&carDecoder, tmp, sizeof(tmp)), MANUFACTURER_LENGTH);
    EXPECT_EQ(std::string(tmp, MANUFACTURER_LENGTH), MANUFACTURER);

    EXPECT_EQ(CGT(Car_get_model)(&carDecoder, tmp, sizeof(tmp)), MODEL_LENGTH);
    EXPECT_EQ(std::string(tmp, MODEL_LENGTH), MODEL);

    EXPECT_EQ(CGT(Car_get_manufacturer)(&carDecoder, tmp, sizeof(tmp)), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(tmp, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);

    EXPECT_EQ(CGT(Car_encoded_length)(&carDecoder), expectedCarSize);
}

static const std::size_t offsetVehicleCode = 32;
static const std::size_t offsetUsageDesc1Length = 57;
static const std::size_t offsetUsageDesc1Data = offsetUsageDesc1Length + sizeof(std::uint16_t);
static const std::size_t offsetUsageDesc2Length = 76;
static const std::size_t offsetUsageDesc2Data = offsetUsageDesc2Length + sizeof(std::uint16_t);
static const std::size_t offsetUsageDesc3Length = 98;
static const std::size_t offsetUsageDesc3Data = offsetUsageDesc3Length + sizeof(std::uint16_t);
static const std::size_t offsetManufacturerLength = 163;
static const std::size_t offsetManufacturerData = offsetManufacturerLength + sizeof(std::uint16_t);
static const std::size_t offsetModelLength = 170;
static const std::size_t offsetModelData = offsetModelLength + sizeof(std::uint16_t);
static const std::size_t offsetActivationCodeLength = 181;
static const std::size_t offsetActivationCodeData = offsetActivationCodeLength + sizeof(std::uint16_t);

TEST_F(CodeGenTest, shouldBeAbleToUseStdStringMethodsForEncode)
{
    std::string vehicleCode(VEHICLE_CODE, CGT(Car_vehicleCode_length)());
    std::string usageDesc1(FUEL_FIGURES_1_USAGE_DESCRIPTION, FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    std::string usageDesc2(FUEL_FIGURES_2_USAGE_DESCRIPTION, FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    std::string usageDesc3(FUEL_FIGURES_3_USAGE_DESCRIPTION, FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    std::string manufacturer(MANUFACTURER, MANUFACTURER_LENGTH);
    std::string model(MODEL, MODEL_LENGTH);
    std::string activationCode(ACTIVATION_CODE, ACTIVATION_CODE_LENGTH);

    char buffer[BUFFER_LEN];
    std::uint64_t baseOffset = static_cast<std::uint64_t>(CGT(messageHeader_encoded_length)());
    CGT(Car) car;
    if (!CGT(Car_wrap_for_encode)(&car, buffer, baseOffset, sizeof(buffer)))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    CGT(Car_put_vehicleCode)(&car, vehicleCode.c_str());
    CGT(Car_fuelFigures) fuelFig;
    if (!CGT(Car_fuelFigures_set_count)(&car, &fuelFig, FUEL_FIGURES_COUNT))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    CGT(Car_fuelFigures_next)(&fuelFig);
    const char *desc1 = usageDesc1.c_str();
    CGT(Car_fuelFigures_put_usageDescription)(&fuelFig, desc1, (uint16_t)strlen(desc1));
    CGT(Car_fuelFigures_next)(&fuelFig);
    const char *desc2 = usageDesc2.c_str();
    CGT(Car_fuelFigures_put_usageDescription)(&fuelFig, desc2, (uint16_t)strlen(desc2));
    CGT(Car_fuelFigures_next)(&fuelFig);
    const char *desc3 = usageDesc3.c_str();
    CGT(Car_fuelFigures_put_usageDescription)(&fuelFig, desc3, (uint16_t)strlen(desc3));


    CGT(Car_performanceFigures) perfFigs;
    if (!CGT(Car_performanceFigures_set_count)(&car, &perfFigs, PERFORMANCE_FIGURES_COUNT))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    CGT(Car_performanceFigures_next)(&perfFigs);
    CGT(Car_performanceFigures_acceleration) acc;
    CGT(Car_performanceFigures_acceleration_set_count)(&perfFigs, &acc, ACCELERATION_COUNT);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);

    CGT(Car_performanceFigures_next)(&perfFigs);
    CGT(Car_performanceFigures_acceleration_set_count)(&perfFigs, &acc, ACCELERATION_COUNT);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);

    const char *manu = manufacturer.c_str();
    CGT(Car_put_manufacturer)(&car, manu, (uint16_t)strlen(manu));
    const char *model_c = model.c_str();
    CGT(Car_put_model)(&car, model_c, (uint16_t)strlen(model_c));
    const char *acti = activationCode.c_str();
    CGT(Car_put_activationCode)(&car, acti, (uint16_t)strlen(acti));

    EXPECT_EQ(CGT(Car_encoded_length)(&car), expectedCarSize);

    EXPECT_EQ(std::string(buffer + baseOffset + offsetVehicleCode, VEHICLE_CODE_LENGTH), vehicleCode);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetUsageDesc1Length), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetUsageDesc1Data, FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH), usageDesc1);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetUsageDesc2Length), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetUsageDesc2Data, FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH), usageDesc2);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetUsageDesc3Length), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetUsageDesc3Data, FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH), usageDesc3);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetManufacturerLength), MANUFACTURER_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetManufacturerData, MANUFACTURER_LENGTH), manufacturer);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetModelLength), MODEL_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetModelData, MODEL_LENGTH), model);

    EXPECT_EQ(*(std::uint16_t *)(buffer + baseOffset + offsetActivationCodeLength), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(buffer + baseOffset + offsetActivationCodeData, ACTIVATION_CODE_LENGTH), activationCode);
}

void testUsageDescription(CGT(Car_fuelFigures) *const fuelFigures, const std::string& expected)
{
    CGT(Car_fuelFigures_next)(fuelFigures);
    const std::uint16_t length = CGT(Car_fuelFigures_usageDescription_length)(fuelFigures);
    const char* const ptr = CGT(Car_fuelFigures_usageDescription)(fuelFigures);
    if (!ptr)
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    EXPECT_EQ(std::string(ptr, length), expected);
}

TEST_F(CodeGenTest, shouldBeAbleToUseStdStringMethodsForDecode)
{
    char buffer[2048];
    CGT(Car) carEncoder;
    CGT(Car_reset)(&carEncoder, buffer, 0, sizeof(buffer), CGT(Car_sbe_block_length)(), CGT(Car_sbe_schema_version)());

    std::uint64_t carSz = encodeCar(carEncoder);

    EXPECT_EQ(carSz, expectedCarSize);

    CGT(Car) carDecoder;
    CGT(Car_reset)(&carDecoder, buffer, 0, carSz, CGT(Car_sbe_block_length)(), CGT(Car_sbe_schema_version)());

    std::string vehicleCode(VEHICLE_CODE, CGT(Car_vehicleCode_length)());

    EXPECT_EQ(std::string(CGT(Car_vehicleCode_buffer)(&carDecoder),CGT(Car_vehicleCode_length)()), vehicleCode);

    CGT(Car_fuelFigures) fuelFigures;
    if (!CGT(Car_get_fuelFigures)(&carDecoder, &fuelFigures))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    testUsageDescription(
        &fuelFigures, std::string(FUEL_FIGURES_1_USAGE_DESCRIPTION, FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH));
    testUsageDescription(
        &fuelFigures, std::string(FUEL_FIGURES_2_USAGE_DESCRIPTION, FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH));
    testUsageDescription(
        &fuelFigures, std::string(FUEL_FIGURES_3_USAGE_DESCRIPTION, FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH));

    CGT(Car_performanceFigures) perfFigures;
    if (!CGT(Car_get_performanceFigures)(&carDecoder, &perfFigures))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }

    CGT(Car_performanceFigures_next)(&perfFigures);
    CGT(Car_performanceFigures_acceleration) acc;
    if (!CGT(Car_performanceFigures_get_acceleration)(&perfFigures, &acc))
    {
        throw std::runtime_error(sbe_strerror(errno));
    }
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_next)(&perfFigures);

    CGT(Car_performanceFigures_get_acceleration)(&perfFigures, &acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);
    CGT(Car_performanceFigures_acceleration_next)(&acc);

    {
        const uint16_t length = CGT(Car_manufacturer_length)(&carDecoder);
        const char* const ptr = CGT(Car_manufacturer)(&carDecoder);
        if (!ptr)
        {
            throw std::runtime_error(sbe_strerror(errno));
        }

        EXPECT_EQ(
            std::string(ptr, length),
            std::string(MANUFACTURER, MANUFACTURER_LENGTH));
    }
    {
        const uint16_t length = CGT(Car_model_length)(&carDecoder);
        const char* const ptr = CGT(Car_model)(&carDecoder);
        if (!ptr)
        {
            throw std::runtime_error(sbe_strerror(errno));
        }

        EXPECT_EQ(
            std::string(ptr, length),
            std::string(MODEL, MODEL_LENGTH));
    }
    {
        const uint16_t length = CGT(Car_activationCode_length)(&carDecoder);
        const char* const ptr = CGT(Car_activationCode)(&carDecoder);
        if (!ptr)
        {
            throw std::runtime_error(sbe_strerror(errno));
        }

        EXPECT_EQ(
            std::string(ptr, length),
            std::string(ACTIVATION_CODE, ACTIVATION_CODE_LENGTH));
    }

    EXPECT_EQ(CGT(Car_encoded_length)(&carDecoder), expectedCarSize);
}
