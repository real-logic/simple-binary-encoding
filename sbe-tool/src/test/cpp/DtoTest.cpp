/*
 * Copyright 2013-2023 Real Logic Limited.
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

#if (defined(_MSVC_LANG) && _MSVC_LANG < 201703L) || (!defined(_MSVC_LANG) && defined(__cplusplus) && __cplusplus < 201703L)
#error DTO code requires at least C++17.
#endif

#include <gtest/gtest.h>
#include "dto_test/ExtendedCar.h"
#include "dto_test/ExtendedCarDto.h"

using namespace dto_test;

static const std::size_t BUFFER_LEN = 2048;

static const std::uint32_t SERIAL_NUMBER = 1234;
static const std::uint16_t MODEL_YEAR = 2013;
static const BooleanType::Value AVAILABLE = BooleanType::T;
static const Model::Value CODE = Model::A;
static const bool CRUISE_CONTROL = true;
static const bool SPORTS_PACK = true;
static const bool SUNROOF = false;
static const BoostType::Value BOOST_TYPE = BoostType::NITROUS;
static const std::uint8_t BOOSTER_HORSEPOWER = 200;
static const std::int32_t ADDED1 = 7;
static const std::int8_t ADDED6_1 = 11;
static const std::int8_t ADDED6_2 = 13;

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *FUEL_FIGURES_1_USAGE_DESCRIPTION = "Urban Cycle";
static const char *FUEL_FIGURES_2_USAGE_DESCRIPTION = "Combined Cycle";
static const char *FUEL_FIGURES_3_USAGE_DESCRIPTION = "Highway Cycle";
static const char *MANUFACTURER = "Honda";
static const char *MODEL = "Civic VTi";
static const char *ACTIVATION_CODE = "deadbeef";
static const char *ADDED5 = "feedface";

static const std::uint8_t PERFORMANCE_FIGURES_COUNT = 2;
static const std::uint8_t FUEL_FIGURES_COUNT = 3;
static const std::uint8_t ACCELERATION_COUNT = 3;

static const std::uint16_t fuel1Speed = 30;
static const float fuel1Mpg = 35.9f;
static const std::int8_t fuel1Added2Element1 = 42;
static const std::int8_t fuel1Added2Element2 = 43;
static const std::int8_t fuel1Added3 = 44;
static const std::uint16_t fuel2Speed = 55;
static const float fuel2Mpg = 49.0f;
static const std::int8_t fuel2Added2Element1 = 45;
static const std::int8_t fuel2Added2Element2 = 46;
static const std::int8_t fuel2Added3 = 47;
static const std::uint16_t fuel3Speed = 75;
static const float fuel3Mpg = 40.0f;
static const std::int8_t fuel3Added2Element1 = 48;
static const std::int8_t fuel3Added2Element2 = 49;
static const std::int8_t fuel3Added3 = 50;

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

class DtoTest : public testing::Test
{

public:
    static std::uint64_t encodeCar(ExtendedCar &car)
    {
        car.serialNumber(SERIAL_NUMBER)
            .modelYear(MODEL_YEAR)
            .available(AVAILABLE)
            .code(CODE)
            .putVehicleCode(VEHICLE_CODE);

        car.extras().clear()
            .cruiseControl(CRUISE_CONTROL)
            .sportsPack(SPORTS_PACK)
            .sunRoof(SUNROOF);

        car.engine()
            .capacity(engineCapacity)
            .numCylinders(engineNumCylinders)
            .putManufacturerCode(MANUFACTURER_CODE)
            .efficiency(50)
            .boosterEnabled(BooleanType::Value::T)
            .booster().boostType(BOOST_TYPE).horsePower(BOOSTER_HORSEPOWER);

        car.added1(ADDED1);

        car.added4(BooleanType::Value::T);

        car.added6().one(ADDED6_1).two(ADDED6_2);

        ExtendedCar::FuelFigures &fuelFigures = car.fuelFiguresCount(FUEL_FIGURES_COUNT);

        fuelFigures
            .next().speed(fuel1Speed).mpg(fuel1Mpg)
            .putAdded2(fuel1Added2Element1, fuel1Added2Element2)
            .added3(fuel1Added3)
            .putUsageDescription(
                FUEL_FIGURES_1_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_1_USAGE_DESCRIPTION)));

        fuelFigures
            .next().speed(fuel2Speed).mpg(fuel2Mpg)
            .putAdded2(fuel2Added2Element1, fuel2Added2Element2)
            .added3(fuel2Added3)
            .putUsageDescription(
                FUEL_FIGURES_2_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_2_USAGE_DESCRIPTION)));

        fuelFigures
            .next().speed(fuel3Speed).mpg(fuel3Mpg)
            .putAdded2(fuel3Added2Element1, fuel3Added2Element2)
            .added3(fuel3Added3)
            .putUsageDescription(
                FUEL_FIGURES_3_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_3_USAGE_DESCRIPTION)));

        ExtendedCar::PerformanceFigures &perfFigs = car.performanceFiguresCount(PERFORMANCE_FIGURES_COUNT);

        perfFigs.next()
            .octaneRating(perf1Octane)
            .accelerationCount(ACCELERATION_COUNT)
            .next().mph(perf1aMph).seconds(perf1aSeconds)
            .next().mph(perf1bMph).seconds(perf1bSeconds)
            .next().mph(perf1cMph).seconds(perf1cSeconds);

        perfFigs.next()
            .octaneRating(perf2Octane)
            .accelerationCount(ACCELERATION_COUNT)
            .next().mph(perf2aMph).seconds(perf2aSeconds)
            .next().mph(perf2bMph).seconds(perf2bSeconds)
            .next().mph(perf2cMph).seconds(perf2cSeconds);

        car.putManufacturer(MANUFACTURER, static_cast<int>(strlen(MANUFACTURER)))
            .putModel(MODEL, static_cast<int>(strlen(MODEL)))
            .putActivationCode(ACTIVATION_CODE, static_cast<int>(strlen(ACTIVATION_CODE)))
            .putAdded5(ADDED5, static_cast<int>(strlen(ADDED5)));

        return car.encodedLength();
    }
};

TEST_F(DtoTest, shouldRoundTripCar1)
{
    char input[BUFFER_LEN];
    std::memset(input, 0, BUFFER_LEN);
    ExtendedCar encoder1;
    encoder1.wrapForEncode(input, 0, BUFFER_LEN);
    const std::uint64_t encodedCarLength = encodeCar(encoder1);

    ExtendedCar decoder;
    decoder.wrapForDecode(
        input,
        0,
        ExtendedCar::sbeBlockLength(),
        ExtendedCar::sbeSchemaVersion(),
        encodedCarLength);
    ExtendedCarDto dto;
    ExtendedCarDto::decodeWith(decoder, dto);

    char output[BUFFER_LEN];
    std::memset(output, 0, BUFFER_LEN);
    ExtendedCar encoder2;
    encoder2.wrapForEncode(output, 0, BUFFER_LEN);
    ExtendedCarDto::encodeWith(encoder2, dto);
    const std::uint64_t encodedCarLength2 = encoder2.encodedLength();

    decoder.sbeRewind();
    std::ostringstream originalStringStream;
    originalStringStream << decoder;
    std::string originalString = originalStringStream.str();

    std::ostringstream dtoStringStream;
    dtoStringStream << dto;
    std::string dtoString = dtoStringStream.str();

    EXPECT_EQ(encodedCarLength, encodedCarLength2);
    EXPECT_EQ(0, std::memcmp(input, output, encodedCarLength2));
    EXPECT_EQ(originalString, dtoString);
}

TEST_F(DtoTest, shouldRoundTripCar2)
{
    char input[BUFFER_LEN];
    std::memset(input, 0, BUFFER_LEN);
    ExtendedCar encoder;
    encoder.wrapForEncode(input, 0, BUFFER_LEN);
    const std::uint64_t encodedCarLength = encodeCar(encoder);

    ExtendedCarDto dto = ExtendedCarDto::decodeFrom(
        input,
        0,
        ExtendedCar::sbeBlockLength(),
        ExtendedCar::sbeSchemaVersion(),
        encodedCarLength);

    EXPECT_EQ(encodedCarLength, dto.computeEncodedLength());

    std::vector<std::uint8_t> output = ExtendedCarDto::bytes(dto);

    std::ostringstream originalStringStream;
    originalStringStream << encoder;
    std::string originalString = originalStringStream.str();

    std::ostringstream dtoStringStream;
    dtoStringStream << dto;
    std::string dtoString = dtoStringStream.str();

    EXPECT_EQ(originalString, dtoString);
    EXPECT_EQ(encodedCarLength, output.size());
    EXPECT_EQ(0, std::memcmp(input, output.data(), encodedCarLength));
}
