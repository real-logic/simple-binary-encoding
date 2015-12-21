/*
 * Copyright 2014 - 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <iostream>

#include "gtest/gtest.h"
#include "code_generation_test/MessageHeader.hpp"
#include "code_generation_test/Car.hpp"

using namespace std;
using namespace code_generation_test;

static const sbe_uint32_t SERIAL_NUMBER = 1234;
static const sbe_uint16_t MODEL_YEAR = 2013;
static const BooleanType::Value AVAILABLE = BooleanType::T;
static const Model::Value CODE = Model::A;
static const bool CRUISE_CONTROL = true;
static const bool SPORTS_PACK = true;
static const bool SUNROOF = false;

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *FUEL_FIGURES_1_USAGE_DESCRIPTION = "Urban Cycle";
static const char *FUEL_FIGURES_2_USAGE_DESCRIPTION = "Combined Cycle";
static const char *FUEL_FIGURES_3_USAGE_DESCRIPTION = "Highway Cycle";
static const char *MAKE = "Honda";
static const char *MODEL = "Civic VTi";
static const char *ACTIVATION_CODE = "deadbeef";

static const int VEHICLE_CODE_LENGTH = sizeof(VEHICLE_CODE);
static const int MANUFACTURER_CODE_LENGTH = sizeof(MANUFACTURER_CODE);
static const int FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH = 11;
static const int FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH = 14;
static const int FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH = 13;
static const int MAKE_LENGTH = 5;
static const int MODEL_LENGTH = 9;
static const int ACTIVATION_CODE_LENGTH = 8;
static const int PERFORMANCE_FIGURES_COUNT = 2;
static const int FUEL_FIGURES_COUNT = 3;
static const int ACCELERATION_COUNT = 3;

static const int expectedHeaderSize = 8;
static const int expectedCarSize = 179;

static const sbe_uint16_t fuel1Speed = 30;
static const sbe_float_t fuel1Mpg = 35.9f;
static const sbe_uint16_t fuel2Speed = 55;
static const sbe_float_t fuel2Mpg = 49.0f;
static const sbe_uint16_t fuel3Speed = 75;
static const sbe_float_t fuel3Mpg = 40.0f;

static const sbe_uint8_t perf1Octane = 95;
static const sbe_uint16_t perf1aMph = 30;
static const sbe_float_t perf1aSeconds = 4.0f;
static const sbe_uint16_t perf1bMph = 60;
static const sbe_float_t perf1bSeconds = 7.5f;
static const sbe_uint16_t perf1cMph = 100;
static const sbe_float_t perf1cSeconds = 12.2f;

static const sbe_uint8_t perf2Octane = 99;
static const sbe_uint16_t perf2aMph = 30;
static const sbe_float_t perf2aSeconds = 3.8f;
static const sbe_uint16_t perf2bMph = 60;
static const sbe_float_t perf2bSeconds = 7.1f;
static const sbe_uint16_t perf2cMph = 100;
static const sbe_float_t perf2cSeconds = 11.8f;

static const sbe_uint16_t engineCapacity = 2000;
static const sbe_uint8_t engineNumCylinders = 4;

class CodeGenTest : public testing::Test
{
public:

    static int encodeHdr(MessageHeader& hdr)
    {
        hdr.blockLength(Car::sbeBlockLength())
            .templateId(Car::sbeTemplateId())
            .schemaId(Car::sbeSchemaId())
            .version(Car::sbeSchemaVersion());

        return hdr.size();
    }

    static int encodeCar(Car& car)
    {
        car.serialNumber(SERIAL_NUMBER)
            .modelYear(MODEL_YEAR)
            .available(AVAILABLE)
            .code(CODE)
            .putVehicleCode(VEHICLE_CODE);

        for (int i = 0; i < Car::someNumbersLength(); i++)
        {
            car.someNumbers(i, i);
        }

        car.extras().clear()
            .cruiseControl(CRUISE_CONTROL)
            .sportsPack(SPORTS_PACK)
            .sunRoof(SUNROOF);

        car.engine()
            .capacity(engineCapacity)
            .numCylinders(engineNumCylinders)
            .putManufacturerCode(MANUFACTURER_CODE);

        Car::FuelFigures& fuelFigures = car.fuelFiguresCount(FUEL_FIGURES_COUNT);

        fuelFigures
            .next().speed(fuel1Speed).mpg(fuel1Mpg);

        fuelFigures.putUsageDescription(
            FUEL_FIGURES_1_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_1_USAGE_DESCRIPTION)));

        fuelFigures
            .next().speed(fuel2Speed).mpg(fuel2Mpg);
        fuelFigures.putUsageDescription(
            FUEL_FIGURES_2_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_2_USAGE_DESCRIPTION)));

        fuelFigures
            .next().speed(fuel3Speed).mpg(fuel3Mpg);
        fuelFigures.putUsageDescription(
            FUEL_FIGURES_3_USAGE_DESCRIPTION, static_cast<int>(strlen(FUEL_FIGURES_3_USAGE_DESCRIPTION)));

        Car::PerformanceFigures &perfFigs = car.performanceFiguresCount(PERFORMANCE_FIGURES_COUNT);

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

        car.putMake(MAKE, static_cast<int>(strlen(MAKE)));
        car.putModel(MODEL, static_cast<int>(strlen(MODEL)));
        car.putActivationCode(ACTIVATION_CODE, static_cast<int>(strlen(ACTIVATION_CODE)));

        return car.size();
    }

    virtual int encodeHdr(char *buffer, int offset, int bufferLength)
    {
        hdr_.wrap(buffer, offset, 0, bufferLength);
        return encodeHdr(hdr_);
    }

    virtual int encodeCar(char *buffer, int offset, int bufferLength)
    {
        car_.wrapForEncode(buffer, offset, bufferLength);
        return encodeCar(car_);
    }

    MessageHeader hdr_;
    MessageHeader hdrDecoder_;
    Car car_;
    Car carDecoder_;
};

TEST_F(CodeGenTest, shouldReturnCorrectValuesForMessageHeaderStaticFields)
{
    EXPECT_EQ(MessageHeader::size(), 8);
    // only checking the block length field
    EXPECT_EQ(MessageHeader::blockLengthNullValue(), 65535);
    EXPECT_EQ(MessageHeader::blockLengthMinValue(), 0);
    EXPECT_EQ(MessageHeader::blockLengthMaxValue(), 65534);
}

TEST_F(CodeGenTest, shouldReturnCorrectValuesForCarStaticFields)
{
    EXPECT_EQ(Car::sbeBlockLength(), 45);
    EXPECT_EQ(Car::sbeTemplateId(), 1);
    EXPECT_EQ(Car::sbeSchemaId(), 6);
    EXPECT_EQ(Car::sbeSchemaVersion(), 0);
    EXPECT_EQ(std::string(Car::sbeSemanticType()), std::string(""));
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeMessageHeaderCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;

    int sz = encodeHdr(buffer, 0, sizeof(buffer));

    EXPECT_EQ(*((::uint16_t *)bp), Car::sbeBlockLength());
    EXPECT_EQ(*((::uint16_t *)(bp + 2)), Car::sbeTemplateId());
    EXPECT_EQ(*((::uint16_t *)(bp + 4)), Car::sbeSchemaId());
    EXPECT_EQ(*((::uint16_t *)(bp + 6)), Car::sbeSchemaVersion());
    EXPECT_EQ(sz, 8);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeAndDecodeMessageHeaderCorrectly)
{
    char buffer[2048];

    encodeHdr(buffer, 0, sizeof(buffer));

    hdrDecoder_.wrap(buffer, 0, 0, sizeof(buffer));
    EXPECT_EQ(hdrDecoder_.blockLength(), Car::sbeBlockLength());
    EXPECT_EQ(hdrDecoder_.templateId(), Car::sbeTemplateId());
    EXPECT_EQ(hdrDecoder_.schemaId(), Car::sbeSchemaId());
    EXPECT_EQ(hdrDecoder_.version(), Car::sbeSchemaVersion());
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
static const uint8_t fieldIdMake = 18;
static const uint8_t fieldIdModel = 19;
static const uint8_t fieldIdActivationCode = 20;

TEST_F(CodeGenTest, shouldReturnCorrectValuesForCarFieldIdsAndCharacterEncoding)
{
    EXPECT_EQ(Car::serialNumberId(), fieldIdSerialNumber);
    EXPECT_EQ(Car::modelYearId(), fieldIdModelYear);
    EXPECT_EQ(Car::availableId(), fieldIdAvailable);
    EXPECT_EQ(Car::codeId(), fieldIdCode);
    EXPECT_EQ(Car::someNumbersId(), fieldIdSomeNumbers);
    EXPECT_EQ(Car::vehicleCodeId(), fieldIdVehicleCode);
    EXPECT_EQ(Car::extrasId(), fieldIdExtras);
    EXPECT_EQ(Car::discountedModelId(), fieldIdDiscountedModel);
    EXPECT_EQ(Car::engineId(), fieldIdEngine);
    EXPECT_EQ(Car::fuelFiguresId(), fieldIdFuelFigures);
    EXPECT_EQ(Car::FuelFigures::speedId(), fieldIdFuelSpeed);
    EXPECT_EQ(Car::FuelFigures::mpgId(), fieldIdFuelMpg);
    EXPECT_EQ(Car::FuelFigures::usageDescriptionId(), fieldIdFuelUsageDescription);
    EXPECT_EQ(Car::FuelFigures::usageDescriptionCharacterEncoding(), std::string("UTF-8"));
    EXPECT_EQ(Car::performanceFiguresId(), fieldIdPerformanceFigures);
    EXPECT_EQ(Car::PerformanceFigures::octaneRatingId(), fieldIdPerfOctaneRating);
    EXPECT_EQ(Car::PerformanceFigures::accelerationId(), fieldIdPerfAcceleration);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::mphId(), fieldIdPerfAccMph);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::secondsId(), fieldIdPerfAccSeconds);
    EXPECT_EQ(Car::makeId(), fieldIdMake);
    EXPECT_EQ(Car::modelId(), fieldIdModel);
    EXPECT_EQ(Car::activationCodeId(), fieldIdActivationCode);
    EXPECT_EQ(std::string(Car::makeCharacterEncoding()), std::string("UTF-8"));
    EXPECT_EQ(std::string(Car::modelCharacterEncoding()), std::string("UTF-8"));
    EXPECT_EQ(std::string(Car::activationCodeCharacterEncoding()), std::string("UTF-8"));
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeCarCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeCar(buffer, 0, sizeof(buffer));

    int offset = 0;
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

    // fuel figures
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 6);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), FUEL_FIGURES_COUNT);
    offset += sizeof(std::uint8_t);

    EXPECT_EQ(*(::uint16_t *)(bp + offset), fuel1Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel1Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_1_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH;

    EXPECT_EQ(*(std::uint16_t *)(bp + offset), fuel2Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel2Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_2_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH;

    EXPECT_EQ(*(std::uint16_t *)(bp + offset), fuel3Speed);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel3Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_3_USAGE_DESCRIPTION);
    offset += FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH;

    // performance figures
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 1);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), PERFORMANCE_FIGURES_COUNT);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(*(bp + offset), perf1Octane);
    offset += sizeof(std::uint8_t);
    // acceleration
    EXPECT_EQ(*(std::uint16_t *)(bp + offset), 6);
    offset += sizeof(std::uint16_t);
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(std::uint8_t);
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
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(std::uint8_t);
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

    // make & model
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), MAKE_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MAKE_LENGTH), MAKE);
    offset += MAKE_LENGTH;
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), MODEL_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MODEL_LENGTH), MODEL);
    offset += MODEL_LENGTH;
    EXPECT_EQ(*(std::uint8_t *)(bp + offset), ACTIVATION_CODE_LENGTH);
    offset += sizeof(std::uint8_t);
    EXPECT_EQ(std::string(bp + offset, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);
    offset += ACTIVATION_CODE_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeHeaderPlusCarCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;

    int hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    int carSz = encodeCar(buffer, hdr_.size(), sizeof(buffer) - hdr_.size());

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    EXPECT_EQ(*((std::uint16_t *)bp), Car::sbeBlockLength());
    const size_t activationCodePosition = hdrSz + carSz - ACTIVATION_CODE_LENGTH;
    const size_t activationCodeLengthPosition = activationCodePosition - 1;
    EXPECT_EQ(*(std::uint8_t *)(bp + activationCodeLengthPosition), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(bp + activationCodePosition, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);
}

TEST_F(CodeGenTest, shouldbeAbleToEncodeAndDecodeHeaderPlusCarCorrectly)
{
    char buffer[2048];

    int hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    int carSz = encodeCar(buffer, hdr_.size(), sizeof(buffer) - hdr_.size());

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    hdrDecoder_.wrap(buffer, 0, 0, sizeof(buffer));

    EXPECT_EQ(hdrDecoder_.blockLength(), Car::sbeBlockLength());
    EXPECT_EQ(hdrDecoder_.templateId(), Car::sbeTemplateId());
    EXPECT_EQ(hdrDecoder_.schemaId(), Car::sbeSchemaId());
    EXPECT_EQ(hdrDecoder_.version(), Car::sbeSchemaVersion());
    EXPECT_EQ(hdrDecoder_.size(), expectedHeaderSize);

    carDecoder_.wrapForDecode(buffer, hdrDecoder_.size(), Car::sbeBlockLength(), Car::sbeSchemaVersion(), sizeof(buffer));

    EXPECT_EQ(carDecoder_.serialNumber(), SERIAL_NUMBER);
    EXPECT_EQ(carDecoder_.modelYear(), MODEL_YEAR);
    EXPECT_EQ(carDecoder_.available(), AVAILABLE);
    EXPECT_EQ(carDecoder_.code(), CODE);

    EXPECT_EQ(carDecoder_.someNumbersLength(), 5);
    for (int i = 0; i < 5; i++)
    {
        EXPECT_EQ(carDecoder_.someNumbers(i), i);
    }

    EXPECT_EQ(carDecoder_.vehicleCodeLength(), VEHICLE_CODE_LENGTH);
    EXPECT_EQ(std::string(carDecoder_.vehicleCode(), VEHICLE_CODE_LENGTH), std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));

    EXPECT_EQ(carDecoder_.extras().cruiseControl(), true);
    EXPECT_EQ(carDecoder_.extras().sportsPack(), true);
    EXPECT_EQ(carDecoder_.extras().sunRoof(), false);

    EXPECT_EQ(carDecoder_.discountedModel(), Model::C);

    Engine &engine = carDecoder_.engine();
    EXPECT_EQ(engine.capacity(), engineCapacity);
    EXPECT_EQ(engine.numCylinders(), engineNumCylinders);
    EXPECT_EQ(engine.maxRpm(), 9000);
    EXPECT_EQ(engine.manufacturerCodeLength(), MANUFACTURER_CODE_LENGTH);
    EXPECT_EQ(std::string(engine.manufacturerCode(), MANUFACTURER_CODE_LENGTH), std::string(MANUFACTURER_CODE, MANUFACTURER_CODE_LENGTH));
    EXPECT_EQ(engine.fuelLength(), 6);
    EXPECT_EQ(std::string(engine.fuel(), 6), std::string("Petrol"));

    Car::FuelFigures &fuelFigures = carDecoder_.fuelFigures();
    EXPECT_EQ(fuelFigures.count(), FUEL_FIGURES_COUNT);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), fuel1Speed);
    EXPECT_EQ(fuelFigures.mpg(), fuel1Mpg);
    EXPECT_EQ(fuelFigures.usageDescriptionLength(), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(fuelFigures.usageDescription(), FUEL_FIGURES_1_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_1_USAGE_DESCRIPTION);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), fuel2Speed);
    EXPECT_EQ(fuelFigures.mpg(), fuel2Mpg);
    EXPECT_EQ(fuelFigures.usageDescriptionLength(), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(fuelFigures.usageDescription(), FUEL_FIGURES_2_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_2_USAGE_DESCRIPTION);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), fuel3Speed);
    EXPECT_EQ(fuelFigures.mpg(), fuel3Mpg);
    EXPECT_EQ(fuelFigures.usageDescriptionLength(), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH);
    EXPECT_EQ(std::string(fuelFigures.usageDescription(), FUEL_FIGURES_3_USAGE_DESCRIPTION_LENGTH), FUEL_FIGURES_3_USAGE_DESCRIPTION);

    Car::PerformanceFigures &performanceFigures = carDecoder_.performanceFigures();
    EXPECT_EQ(performanceFigures.count(), PERFORMANCE_FIGURES_COUNT);

    ASSERT_TRUE(performanceFigures.hasNext());
    performanceFigures.next();
    EXPECT_EQ(performanceFigures.octaneRating(), perf1Octane);

    Car::PerformanceFigures::Acceleration &acceleration = performanceFigures.acceleration();
    EXPECT_EQ(acceleration.count(), ACCELERATION_COUNT);
    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf1aMph);
    EXPECT_EQ(acceleration.seconds(), perf1aSeconds);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf1bMph);
    EXPECT_EQ(acceleration.seconds(), perf1bSeconds);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf1cMph);
    EXPECT_EQ(acceleration.seconds(), perf1cSeconds);

    ASSERT_TRUE(performanceFigures.hasNext());
    performanceFigures.next();
    EXPECT_EQ(performanceFigures.octaneRating(), perf2Octane);

    acceleration = performanceFigures.acceleration();
    EXPECT_EQ(acceleration.count(), ACCELERATION_COUNT);
    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf2aMph);
    EXPECT_EQ(acceleration.seconds(), perf2aSeconds);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf2bMph);
    EXPECT_EQ(acceleration.seconds(), perf2bSeconds);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), perf2cMph);
    EXPECT_EQ(acceleration.seconds(), perf2cSeconds);

    EXPECT_EQ(carDecoder_.makeLength(), MAKE_LENGTH);
    EXPECT_EQ(std::string(carDecoder_.make(), MAKE_LENGTH), MAKE);

    EXPECT_EQ(carDecoder_.modelLength(), MODEL_LENGTH);
    EXPECT_EQ(std::string(carDecoder_.model(), MODEL_LENGTH), MODEL);

    EXPECT_EQ(carDecoder_.activationCodeLength(), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(carDecoder_.activationCode(), ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);

    EXPECT_EQ(carDecoder_.size(), expectedCarSize);
}

struct CallbacksForEach
{
    int countOfFuelFigures;
    int countOfPerformanceFigures;
    int countOfAccelerations;

    CallbacksForEach() : countOfFuelFigures(0), countOfPerformanceFigures(0), countOfAccelerations(0) {}

    void operator()(Car::FuelFigures& fuelFigures)
    {
        countOfFuelFigures++;
        fuelFigures.usageDescription();
    }

    void operator()(Car::PerformanceFigures& performanceFigures)
    {
        Car::PerformanceFigures::Acceleration acceleration = performanceFigures.acceleration();

        countOfPerformanceFigures++;
        acceleration.forEach(*this);
    }

    void operator()(Car::PerformanceFigures::Acceleration&)
    {
        countOfAccelerations++;
    }
};

TEST_F(CodeGenTest, shouldbeAbleUseOnStackCodecsAndGroupForEach)
{
    char buffer[2048];
    MessageHeader hdr(buffer, sizeof(buffer), 0);
    Car car(buffer + hdr.size(), sizeof(buffer) - hdr.size(), Car::sbeBlockLength(), Car::sbeSchemaVersion());

    int hdrSz = encodeHdr(hdr);
    int carSz = encodeCar(car);

    EXPECT_EQ(hdrSz, expectedHeaderSize);
    EXPECT_EQ(carSz, expectedCarSize);

    const MessageHeader hdrDecoder(buffer, sizeof(buffer), 0);

    EXPECT_EQ(hdrDecoder.blockLength(), Car::sbeBlockLength());
    EXPECT_EQ(hdrDecoder.templateId(), Car::sbeTemplateId());
    EXPECT_EQ(hdrDecoder.schemaId(), Car::sbeSchemaId());
    EXPECT_EQ(hdrDecoder.version(), Car::sbeSchemaVersion());
    EXPECT_EQ(hdrDecoder.size(), expectedHeaderSize);

    Car carDecoder(buffer + hdrDecoder.size(), sizeof(buffer) - hdrDecoder.size(), hdrDecoder.blockLength(), hdrDecoder.version());
    CallbacksForEach cbs;

    Car::FuelFigures &fuelFigures = carDecoder.fuelFigures();
    EXPECT_EQ(fuelFigures.count(), FUEL_FIGURES_COUNT);

#if __cplusplus >= 201103L
    fuelFigures.forEach([&](Car::FuelFigures &fuelFigures)
    {
        cbs.countOfFuelFigures++;

        char tmp[256];
        fuelFigures.getUsageDescription(tmp, sizeof(tmp));
    });
#else
    fuelFigures.forEach(cbs);
#endif

    Car::PerformanceFigures &performanceFigures = carDecoder.performanceFigures();
    EXPECT_EQ(performanceFigures.count(), PERFORMANCE_FIGURES_COUNT);

#if __cplusplus >= 201103L
    performanceFigures.forEach([&](Car::PerformanceFigures& performanceFigures)
    {
        Car::PerformanceFigures::Acceleration acceleration = performanceFigures.acceleration();

        cbs.countOfPerformanceFigures++;
        acceleration.forEach([&](Car::PerformanceFigures::Acceleration&)
        {
            cbs.countOfAccelerations++;
        });
    });
#else
    performanceFigures.forEach(cbs);
#endif

    EXPECT_EQ(cbs.countOfFuelFigures, FUEL_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfPerformanceFigures, PERFORMANCE_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfAccelerations, ACCELERATION_COUNT * PERFORMANCE_FIGURES_COUNT);

    char tmp[256];

    EXPECT_EQ(carDecoder.getMake(tmp, sizeof(tmp)), MAKE_LENGTH);
    EXPECT_EQ(std::string(tmp, MAKE_LENGTH), MAKE);

    EXPECT_EQ(carDecoder.getModel(tmp, sizeof(tmp)), MODEL_LENGTH);
    EXPECT_EQ(std::string(tmp, MODEL_LENGTH), MODEL);

    EXPECT_EQ(carDecoder.getMake(tmp, sizeof(tmp)), ACTIVATION_CODE_LENGTH);
    EXPECT_EQ(std::string(tmp, ACTIVATION_CODE_LENGTH), ACTIVATION_CODE);

    EXPECT_EQ(carDecoder.size(), expectedCarSize);
}

