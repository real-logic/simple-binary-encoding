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

class BoundsCheckTest : public testing::Test
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

    virtual int encodeCarRoot(char *buffer, int offset, int bufferLength)
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

        return car_.size();
    }

    virtual int encodeCarFuelFigures()
    {
        car_.fuelFiguresCount(3)
            .next().speed(30).mpg(35.9f)
            .next().speed(55).mpg(49.0f)
            .next().speed(75).mpg(40.0f);

        return car_.size();
    }

    virtual int encodeCarPerformanceFigures()
    {
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

        return car_.size();
    }

    virtual int encodeCarMakeAndModel()
    {
        car_.putMake(MAKE, strlen(MAKE));
        car_.putModel(MODEL, strlen(MODEL));

        return car_.size();
    }

    MessageHeader hdr_;
    MessageHeader hdrDecoder_;
    Car car_;
    Car carDecoder_;
    char buffer[2048];
};

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForHeaderWrap)
{
    // 0 offset
    EXPECT_THROW(
    {
        hdrDecoder_.wrap(buffer, 0, 0, hdr_.size() - 1);
    }, const char *);

    EXPECT_NO_THROW(
    {
        hdrDecoder_.wrap(buffer, 0, 0, hdr_.size());
    });

    // non-0 offset
    EXPECT_THROW(
    {
        hdrDecoder_.wrap(buffer, 5, 0, hdr_.size() + 4);
    }, const char *);

    EXPECT_NO_THROW(
    {
        hdrDecoder_.wrap(buffer, 5, 0, hdr_.size() + 5);
    });
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForCarWraps)
{
    // 0 offset
    EXPECT_THROW(
    {
        car_.wrapForEncode(buffer, 0, Car::sbeBlockLength() - 1);
    }, const char *);

    EXPECT_NO_THROW(
    {
        car_.wrapForEncode(buffer, 0, Car::sbeBlockLength());
    });

    // non-0 offset
    EXPECT_THROW(
    {
        car_.wrapForEncode(buffer, 5, Car::sbeBlockLength() + 4);
    }, const char *);

    EXPECT_NO_THROW(
    {
        car_.wrapForEncode(buffer, 5, Car::sbeBlockLength() + 5);
    });
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFiguresEncode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() - 1;

    ASSERT_NO_THROW(
        car_.wrapForEncode(buffer, 0, sz);
    );

    EXPECT_THROW(
    {
        car_.fuelFiguresCount(2);
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFiguresDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() - 1;

    ASSERT_NO_THROW(
        carDecoder_.wrapForDecode(buffer, 0, 0, 0, sz);
    );

    EXPECT_THROW(
    {
        car_.fuelFigures();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures1stNextEncode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + Car::FuelFigures::sbeBlockLength() - 1;

    car_.wrapForEncode(buffer, 0, sz);
    Car::FuelFigures &fuelFigures = car_.fuelFiguresCount(2);

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures1stNextDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + Car::FuelFigures::sbeBlockLength() - 1;

    car_.wrapForEncode(buffer, 0, sz);
    Car::FuelFigures &fuelFigures = car_.fuelFigures();

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures2ndNextEncode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + (Car::FuelFigures::sbeBlockLength() * 2) - 1;

    car_.wrapForEncode(buffer, 0, sz);
    Car::FuelFigures &fuelFigures = car_.fuelFiguresCount(3);
    fuelFigures.next();

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures2ndNextDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + (Car::FuelFigures::sbeBlockLength() * 2) - 1;

    car_.wrapForEncode(buffer, 0, sz);
    Car::FuelFigures &fuelFigures = car_.fuelFigures();
    fuelFigures.next();

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldEncodeCorrectSizesAtEachStage)
{
    EXPECT_EQ(encodeHdr(buffer, 0, sizeof(buffer)), 8);
    EXPECT_EQ(encodeCarRoot(buffer, 8, sizeof(buffer)), 21);
    EXPECT_EQ(encodeCarFuelFigures(), 42);
    EXPECT_EQ(encodeCarPerformanceFigures(), 89);
    EXPECT_EQ(encodeCarMakeAndModel(), 105);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForPerformanceFigures)
{
    // fail in outer dimensions
    encodeCarRoot(buffer, 0, 43);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, const char *);

    // fail in 1st inner dimensions
    encodeCarRoot(buffer, 0, 47);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, const char *);

    // fail in accel
    encodeCarRoot(buffer, 0, 51);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, const char *);

    // fail in random spot before end
    encodeCarRoot(buffer, 0, 63);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, const char *);

    // fail just short of end
    encodeCarRoot(buffer, 0, 88);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForMakeAndModel)
{
    // fail short of string
    encodeCarRoot(buffer, 0, 90);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, const char *);

    // fail in string
    encodeCarRoot(buffer, 0, 92);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, const char *);

    // fail short of string
    encodeCarRoot(buffer, 0, 95);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, const char *);

    // fail in string
    encodeCarRoot(buffer, 0, 104);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, const char *);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferJustRightForEntireEncode)
{
    ASSERT_NO_THROW(
    {
        encodeHdr(buffer, 0, 113);
        encodeCarRoot(buffer, hdr_.size(), 113);
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeAndModel();
    });
}
