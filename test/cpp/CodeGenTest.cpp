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

#define SERIAL_NUMBER 1234
#define MODEL_YEAR 2013
#define AVAILABLE (BooleanType::TRUE)
#define CODE (Model::A)
#define CRUISE_CONTROL (true)
#define SPORTS_PACK (true)
#define SUNROOF (false)

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *MAKE = "Honda";
static const char *MODEL = "Civic VTi";

class CodeGenTest : public testing::Test
{
public:

    virtual int encodeHdr(char *buffer, int offset, int bufferLength)
    {
        hdr_.wrap(buffer, offset, 0, bufferLength)
            .blockLength(Car::sbeBlockLength())
            .templateId(Car::sbeTemplateId())
            .schemaId(Car::sbeSchemaId())
            .version(Car::sbeSchemaVersion());

        return hdr_.size();
    }

    virtual int encodeCar(char *buffer, int offset, int bufferLength)
    {
        car_.wrapForEncode(buffer, offset, bufferLength)
            .serialNumber(SERIAL_NUMBER)
            .modelYear(MODEL_YEAR)
            .available(AVAILABLE)
            .code(CODE)
            .putVehicleCode(VEHICLE_CODE);

        car_.extras().clear()
            .cruiseControl(CRUISE_CONTROL)
            .sportsPack(SPORTS_PACK)
            .sunRoof(SUNROOF);

        car_.engine()
            .capacity(2000)
            .numCylinders((short)4)
            .putManufacturerCode(MANUFACTURER_CODE);

        car_.fuelFiguresCount(3)
            .next().speed(30).mpg(35.9f)
            .next().speed(55).mpg(49.0f)
            .next().speed(75).mpg(40.0f);

        Car::PerformanceFigures &perfFigs = car_.performanceFiguresCount(2);

        perfFigs.next()
            .octaneRating((short)95)
            .accelerationCount(3)
                .next().mph(30).seconds(4.0f)
                .next().mph(60).seconds(7.5f)
                .next().mph(100).seconds(12.2f);

        perfFigs.next()
            .octaneRating((short)99)
            .accelerationCount(3)
                .next().mph(30).seconds(3.8f)
                .next().mph(60).seconds(7.1f)
                .next().mph(100).seconds(11.8f);

        car_.putMake(MAKE, strlen(MAKE));
        car_.putModel(MODEL, strlen(MODEL));

        return car_.size();
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
    EXPECT_EQ(Car::sbeSemanticType(), "");
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeMessageHeaderCorrectly)
{
    char buffer[2048];

    int sz = encodeHdr(buffer, 0, sizeof(buffer));

    EXPECT_EQ(*((::uint16_t *)buffer), Car::sbeBlockLength());
    EXPECT_EQ(*((::uint16_t *)(buffer + 2)), Car::sbeTemplateId());
    EXPECT_EQ(*((::uint16_t *)(buffer + 4)), Car::sbeSchemaId());
    EXPECT_EQ(*((::uint16_t *)(buffer + 6)), Car::sbeSchemaVersion());
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
    EXPECT_EQ(Car::serialNumberId(), 1);
    EXPECT_EQ(Car::modelYearId(), 2);
    EXPECT_EQ(Car::availableId(), 3);
    EXPECT_EQ(Car::codeId(), 4);
    EXPECT_EQ(Car::vehicleCodeId(), 6);
    EXPECT_EQ(Car::extrasId(), 5);
    EXPECT_EQ(Car::engineId(), 7);
    EXPECT_EQ(Car::fuelFiguresId(), 8);
    EXPECT_EQ(Car::FuelFigures::speedId(), 9);
    EXPECT_EQ(Car::FuelFigures::mpgId(), 10);
    EXPECT_EQ(Car::performanceFiguresId(), 11);
    EXPECT_EQ(Car::PerformanceFigures::octaneRatingId(), 12);
    EXPECT_EQ(Car::PerformanceFigures::accelerationId(), 13);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::mphId(), 14);
    EXPECT_EQ(Car::PerformanceFigures::Acceleration::secondsId(), 15);
    EXPECT_EQ(Car::makeId(), 16);
    EXPECT_EQ(Car::modelId(), 17);
    EXPECT_EQ(Car::makeCharacterEncoding(), "UTF-8");
    EXPECT_EQ(Car::modelCharacterEncoding(), "UTF-8");
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeCarCorrectly)
{
    char buffer[2048];
    int sz = encodeCar(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, 105);

    EXPECT_EQ(*(::uint32_t *)buffer, SERIAL_NUMBER);
    EXPECT_EQ(*(::uint16_t *)(buffer + 4), MODEL_YEAR);
    EXPECT_EQ(*(::uint8_t *)(buffer + 6), 1);
    EXPECT_EQ(*(buffer + 7), 'A');
    EXPECT_EQ(std::string(buffer + 8, 6), std::string(VEHICLE_CODE, 6));
    EXPECT_EQ(*(buffer + 14), 0x6);
    EXPECT_EQ(*(::uint16_t *)(buffer + 15), 2000);
    EXPECT_EQ(*(buffer + 17), 4);
    EXPECT_EQ(std::string(buffer + 18, 3), std::string(MANUFACTURER_CODE, 3));

    // fuel figures
    EXPECT_EQ(*(::uint16_t *)(buffer + 21), 6);
    EXPECT_EQ(*(buffer + 23), 3);
    EXPECT_EQ(*(::uint16_t *)(buffer + 24), 30);
    EXPECT_EQ(*(float *)(buffer + 26), 35.9f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 30), 55);
    EXPECT_EQ(*(float *)(buffer + 32), 49.0f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 36), 75);
    EXPECT_EQ(*(float *)(buffer + 38), 40.0f);

    // performance figures
    EXPECT_EQ(*(::uint16_t *)(buffer + 42), 1);
    EXPECT_EQ(*(buffer + 44), 2);
    EXPECT_EQ(*(buffer + 45), 95);
    // acceleration
    EXPECT_EQ(*(::uint16_t *)(buffer + 46), 6);
    EXPECT_EQ(*(buffer + 48), 3);
    EXPECT_EQ(*(::uint16_t *)(buffer + 49), 30);
    EXPECT_EQ(*(float *)(buffer + 51), 4.0f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 55), 60);
    EXPECT_EQ(*(float *)(buffer + 57), 7.5f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 61), 100);
    EXPECT_EQ(*(float *)(buffer + 63), 12.2f);

    EXPECT_EQ(*(buffer + 67), 99);
    // acceleration
    EXPECT_EQ(*(::uint16_t *)(buffer + 68), 6);
    EXPECT_EQ(*(buffer + 70), 3);
    EXPECT_EQ(*(::uint16_t *)(buffer + 71), 30);
    EXPECT_EQ(*(float *)(buffer + 73), 3.8f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 77), 60);
    EXPECT_EQ(*(float *)(buffer + 79), 7.1f);
    EXPECT_EQ(*(::uint16_t *)(buffer + 83), 100);
    EXPECT_EQ(*(float *)(buffer + 85), 11.8f);

    // make & model
    EXPECT_EQ(*(buffer + 89), 5);
    EXPECT_EQ(std::string(buffer + 90, 5), MAKE);
    EXPECT_EQ(*(buffer + 95), 9);
    EXPECT_EQ(std::string(buffer + 96, 9), MODEL);
}

TEST_F(CodeGenTest, shouldBeAbleToEncodeHeaderPlusCarCorrectly)
{
    char buffer[2048];

    int hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    int carSz = encodeCar(buffer, hdr_.size(), sizeof(buffer) - hdr_.size());

    EXPECT_EQ(hdrSz, 8);
    EXPECT_EQ(carSz, 105);

    EXPECT_EQ(*((::uint16_t *)buffer), Car::sbeBlockLength());
    EXPECT_EQ(*(buffer + 103), 9);
    EXPECT_EQ(std::string(buffer + 104, 9), MODEL);
}

TEST_F(CodeGenTest, shouldbeAbleToEncodeAndDecodeHeaderPlusCarCorrectly)
{
    char buffer[2048];

    int hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    int carSz = encodeCar(buffer, hdr_.size(), sizeof(buffer) - hdr_.size());

    EXPECT_EQ(hdrSz, 8);
    EXPECT_EQ(carSz, 105);

    hdrDecoder_.wrap(buffer, 0, 0, sizeof(buffer));

    EXPECT_EQ(hdrDecoder_.blockLength(), Car::sbeBlockLength());
    EXPECT_EQ(hdrDecoder_.templateId(), Car::sbeTemplateId());
    EXPECT_EQ(hdrDecoder_.schemaId(), Car::sbeSchemaId());
    EXPECT_EQ(hdrDecoder_.version(), Car::sbeSchemaVersion());

    carDecoder_.wrapForDecode(buffer, hdr_.size(), Car::sbeBlockLength(), Car::sbeSchemaVersion(), sizeof(buffer));

    EXPECT_EQ(std::string(carDecoder_.charConst(), 1), std::string("g", 1));
    EXPECT_EQ(carDecoder_.serialNumber(), SERIAL_NUMBER);
    EXPECT_EQ(carDecoder_.modelYear(), MODEL_YEAR);
    EXPECT_EQ(carDecoder_.available(), AVAILABLE);
    EXPECT_EQ(carDecoder_.code(), CODE);
    EXPECT_EQ(carDecoder_.vehicleCodeLength(), 6);
    EXPECT_EQ(std::string(carDecoder_.vehicleCode(), 6), std::string(VEHICLE_CODE, 6));
    EXPECT_EQ(carDecoder_.extras().cruiseControl(), true);
    EXPECT_EQ(carDecoder_.extras().sportsPack(), true);
    EXPECT_EQ(carDecoder_.extras().sunRoof(), false);

    Engine &engine = carDecoder_.engine();
    EXPECT_EQ(engine.capacity(), 2000);
    EXPECT_EQ(engine.numCylinders(), 4);
    EXPECT_EQ(engine.maxRpm(), 9000);
    EXPECT_EQ(engine.manufacturerCodeLength(), 3);
    EXPECT_EQ(std::string(engine.manufacturerCode(), 3), std::string(MANUFACTURER_CODE, 3));
    EXPECT_EQ(engine.fuelLength(), 6);
    EXPECT_EQ(std::string(engine.fuel(), 6), std::string("Petrol"));

    Car::FuelFigures &fuelFigures = carDecoder_.fuelFigures();
    EXPECT_EQ(fuelFigures.count(), 3);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), 30);
    EXPECT_EQ(fuelFigures.mpg(), 35.9f);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), 55);
    EXPECT_EQ(fuelFigures.mpg(), 49.0f);

    ASSERT_TRUE(fuelFigures.hasNext());
    fuelFigures.next();
    EXPECT_EQ(fuelFigures.speed(), 75);
    EXPECT_EQ(fuelFigures.mpg(), 40.0f);

    Car::PerformanceFigures &performanceFigures = carDecoder_.performanceFigures();
    EXPECT_EQ(performanceFigures.count(), 2);

    ASSERT_TRUE(performanceFigures.hasNext());
    performanceFigures.next();
    EXPECT_EQ(performanceFigures.octaneRating(), 95);

    Car::PerformanceFigures::Acceleration &acceleration = performanceFigures.acceleration();
    EXPECT_EQ(acceleration.count(), 3);
    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 30);
    EXPECT_EQ(acceleration.seconds(), 4.0f);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 60);
    EXPECT_EQ(acceleration.seconds(), 7.5f);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 100);
    EXPECT_EQ(acceleration.seconds(), 12.2f);

    ASSERT_TRUE(performanceFigures.hasNext());
    performanceFigures.next();
    EXPECT_EQ(performanceFigures.octaneRating(), 99);

    acceleration = performanceFigures.acceleration();
    EXPECT_EQ(acceleration.count(), 3);
    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 30);
    EXPECT_EQ(acceleration.seconds(), 3.8f);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 60);
    EXPECT_EQ(acceleration.seconds(), 7.1f);

    ASSERT_TRUE(acceleration.hasNext());
    acceleration.next();
    EXPECT_EQ(acceleration.mph(), 100);
    EXPECT_EQ(acceleration.seconds(), 11.8f);

    EXPECT_EQ(carDecoder_.makeLength(), 5);
    EXPECT_EQ(std::string(carDecoder_.make(), 5), "Honda");

    EXPECT_EQ(carDecoder_.modelLength(), 9);
    EXPECT_EQ(std::string(carDecoder_.model(), 9), "Civic VTi");

    EXPECT_EQ(carDecoder_.size(), 105);
}
