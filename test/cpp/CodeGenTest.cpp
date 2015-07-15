/*
 * Copyright 2014 Real Logic Ltd.
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
static const BooleanType::Value AVAILABLE = BooleanType::TRUE;
static const Model::Value CODE = Model::A;
static const bool CRUISE_CONTROL = true;
static const bool SPORTS_PACK = true;
static const bool SUNROOF = false;

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *MAKE = "Honda";
static const char *MODEL = "Civic VTi";

static const int VEHICLE_CODE_LENGTH = sizeof(VEHICLE_CODE);
static const int MANUFACTURER_CODE_LENGTH = sizeof(MANUFACTURER_CODE);
static const int MAKE_LENGTH = 5;
static const int MODEL_LENGTH = 9;
static const int PERFORMANCE_FIGURES_COUNT = 2;
static const int FUEL_FIGURES_COUNT = 3;
static const int ACCELERATION_COUNT = 3;

static const int expectedHeaderSize = 8;
static const int expectedCarSize = 105;

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

        car.extras().clear()
            .cruiseControl(CRUISE_CONTROL)
            .sportsPack(SPORTS_PACK)
            .sunRoof(SUNROOF);

        car.engine()
            .capacity(engineCapacity)
            .numCylinders(engineNumCylinders)
            .putManufacturerCode(MANUFACTURER_CODE);

        car.fuelFiguresCount(FUEL_FIGURES_COUNT)
            .next().speed(fuel1Speed).mpg(fuel1Mpg)
            .next().speed(fuel2Speed).mpg(fuel2Mpg)
            .next().speed(fuel3Speed).mpg(fuel3Mpg);

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

        car.putMake(MAKE, strlen(MAKE));
        car.putModel(MODEL, strlen(MODEL));

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
    EXPECT_EQ(Car::sbeBlockLength(), 21);
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

TEST_F(CodeGenTest, shouldReturnCorrectValuesForCarFieldIdsAndCharacterEncoding)
{
    EXPECT_EQ(Car::charConstId(), 100);
    int expectedId = 1;
    EXPECT_EQ(Car::serialNumberId(), expectedId++);
    EXPECT_EQ(Car::modelYearId(), expectedId++);
    EXPECT_EQ(Car::availableId(), expectedId++);
    EXPECT_EQ(Car::codeId(), expectedId++);
    EXPECT_EQ(Car::extrasId(), expectedId++);
    EXPECT_EQ(Car::vehicleCodeId(), expectedId++);
    EXPECT_EQ(Car::engineId(), expectedId++);
    EXPECT_EQ(Car::fuelFiguresId(), expectedId++);
    EXPECT_EQ(Car::FuelFigures::speedId(), expectedId++);
    EXPECT_EQ(Car::FuelFigures::mpgId(), expectedId++);
    EXPECT_EQ(Car::performanceFiguresId(), expectedId++);
    EXPECT_EQ(Car::PerformanceFigures::octaneRatingId(), expectedId++);
    EXPECT_EQ(Car::PerformanceFigures::accelerationId(), expectedId++);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::mphId(), expectedId++);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::secondsId(), expectedId++);
    EXPECT_EQ(Car::makeId(), expectedId++);
    EXPECT_EQ(Car::modelId(), expectedId++);
    EXPECT_EQ(std::string(Car::makeCharacterEncoding()), std::string("UTF-8"));
    EXPECT_EQ(std::string(Car::modelCharacterEncoding()), std::string("UTF-8"));
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeCarCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeCar(buffer, 0, sizeof(buffer));

    int offset = 0;
    EXPECT_EQ(*(::uint32_t *)(bp + offset), SERIAL_NUMBER);
    offset += sizeof(::uint32_t);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), MODEL_YEAR);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(::uint8_t *)(bp + offset), 1);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(bp + offset), 'A');
    offset += sizeof(char);
    EXPECT_EQ(std::string(bp + offset, VEHICLE_CODE_LENGTH), std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));
    offset += VEHICLE_CODE_LENGTH;
    EXPECT_EQ(*(bp + offset), 0x6);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), engineCapacity);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(bp + offset), engineNumCylinders);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MANUFACTURER_CODE_LENGTH), std::string(MANUFACTURER_CODE, MANUFACTURER_CODE_LENGTH));
    offset += MANUFACTURER_CODE_LENGTH;

    // fuel figures
    EXPECT_EQ(*(::uint16_t *)(bp + offset), 6);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(::uint8_t *)(bp + offset), FUEL_FIGURES_COUNT);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), fuel1Speed);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel1Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), fuel2Speed);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel2Mpg);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), fuel3Speed);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), fuel3Mpg);
    offset += sizeof(float);

    // performance figures
    EXPECT_EQ(*(::uint16_t *)(bp + offset), 1);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(::uint8_t *)(bp + offset), PERFORMANCE_FIGURES_COUNT);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(bp + offset), perf1Octane);
    offset += sizeof(::uint8_t);
    // acceleration
    EXPECT_EQ(*(::uint16_t *)(bp + offset), 6);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(::uint8_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf1aMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1aSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf1bMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1bSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf1cMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf1cSeconds);
    offset += sizeof(float);

    EXPECT_EQ(*(bp + offset), perf2Octane);
    offset += sizeof(::uint8_t);
    // acceleration
    EXPECT_EQ(*(::uint16_t *)(bp + offset), 6);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(::uint8_t *)(bp + offset), ACCELERATION_COUNT);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf2aMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2aSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf2bMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2bSeconds);
    offset += sizeof(float);
    EXPECT_EQ(*(::uint16_t *)(bp + offset), perf2cMph);
    offset += sizeof(::uint16_t);
    EXPECT_EQ(*(float *)(bp + offset), perf2cSeconds);
    offset += sizeof(float);

    // make & model
    EXPECT_EQ(*(::uint8_t *)(bp + offset), MAKE_LENGTH);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MAKE_LENGTH), MAKE);
    offset += MAKE_LENGTH;
    EXPECT_EQ(*(::uint8_t *)(bp + offset), MODEL_LENGTH);
    offset += sizeof(::uint8_t);
    EXPECT_EQ(std::string(bp + offset, MODEL_LENGTH), MODEL);
    offset += MODEL_LENGTH;

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

    EXPECT_EQ(*((::uint16_t *)bp), Car::sbeBlockLength());
    const size_t modelPosition = hdrSz + carSz - MODEL_LENGTH;
    const size_t modelLengthPosition = modelPosition - 1;
    EXPECT_EQ(*(::uint8_t *)(bp + modelLengthPosition /* 103*/), MODEL_LENGTH);
    EXPECT_EQ(std::string(bp + modelPosition /*104*/, MODEL_LENGTH), MODEL);
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

    EXPECT_EQ(*carDecoder_.charConst(), 'g');
    EXPECT_EQ(carDecoder_.serialNumber(), SERIAL_NUMBER);
    EXPECT_EQ(carDecoder_.modelYear(), MODEL_YEAR);
    EXPECT_EQ(carDecoder_.available(), AVAILABLE);
    EXPECT_EQ(carDecoder_.code(), CODE);
    EXPECT_EQ(carDecoder_.vehicleCodeLength(), VEHICLE_CODE_LENGTH);
    EXPECT_EQ(std::string(carDecoder_.vehicleCode(), VEHICLE_CODE_LENGTH), std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));
    EXPECT_EQ(carDecoder_.extras().cruiseControl(), true);
    EXPECT_EQ(carDecoder_.extras().sportsPack(), true);
    EXPECT_EQ(carDecoder_.extras().sunRoof(), false);

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

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), fuel2Speed);
    EXPECT_EQ(fuelFigures.mpg(), fuel2Mpg);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), fuel3Speed);
    EXPECT_EQ(fuelFigures.mpg(), fuel3Mpg);

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

    EXPECT_EQ(carDecoder_.size(), expectedCarSize);
}

struct CallbacksForEach
{
    int countOfFuelFigures;
    int countOfPerformanceFigures;
    int countOfAccelerations;

    CallbacksForEach() : countOfFuelFigures(0), countOfPerformanceFigures(0), countOfAccelerations(0) {}

    void operator()(Car::FuelFigures&)
    {
        countOfFuelFigures++;
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
    fuelFigures.forEach(cbs);

    Car::PerformanceFigures &performanceFigures = carDecoder.performanceFigures();
    EXPECT_EQ(performanceFigures.count(), PERFORMANCE_FIGURES_COUNT);
    performanceFigures.forEach(cbs);

    EXPECT_EQ(cbs.countOfFuelFigures, FUEL_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfPerformanceFigures, PERFORMANCE_FIGURES_COUNT);
    EXPECT_EQ(cbs.countOfAccelerations, ACCELERATION_COUNT * PERFORMANCE_FIGURES_COUNT);

    EXPECT_EQ(carDecoder.makeLength(), MAKE_LENGTH);
    EXPECT_EQ(std::string(carDecoder.make(), MAKE_LENGTH), MAKE);

    EXPECT_EQ(carDecoder.modelLength(), MODEL_LENGTH);
    EXPECT_EQ(std::string(carDecoder.model(), MODEL_LENGTH), MODEL);

    EXPECT_EQ(carDecoder.size(), expectedCarSize);
}

