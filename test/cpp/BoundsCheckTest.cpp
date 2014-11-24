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

    virtual int decodeHdr(char *buffer, int offset, int bufferLength)
    {
        hdrDecoder_.wrap(buffer, offset, 0, bufferLength);

        EXPECT_EQ(hdrDecoder_.blockLength(), Car::sbeBlockLength());
        EXPECT_EQ(hdrDecoder_.templateId(), Car::sbeTemplateId());
        EXPECT_EQ(hdrDecoder_.schemaId(), Car::sbeSchemaId());
        EXPECT_EQ(hdrDecoder_.version(), Car::sbeSchemaVersion());

        return hdrDecoder_.size();
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

    virtual int decodeCarRoot(char *buffer, const int offset, const int bufferLength)
    {
        carDecoder_.wrapForDecode(buffer, offset, Car::sbeBlockLength(), Car::sbeSchemaVersion(), bufferLength);
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

        return carDecoder_.size();
    }

    virtual int decodeCarFuelFigures()
    {
        Car::FuelFigures &fuelFigures = carDecoder_.fuelFigures();
        EXPECT_EQ(fuelFigures.count(), 3);

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 30);
        EXPECT_EQ(fuelFigures.mpg(), 35.9f);

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 55);
        EXPECT_EQ(fuelFigures.mpg(), 49.0f);

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 75);
        EXPECT_EQ(fuelFigures.mpg(), 40.0f);

        return carDecoder_.size();
    }

    virtual int decodeCarPerformanceFigures()
    {
        Car::PerformanceFigures &performanceFigures = carDecoder_.performanceFigures();
        EXPECT_EQ(performanceFigures.count(), 2);

        EXPECT_TRUE(performanceFigures.hasNext());
        performanceFigures.next();
        EXPECT_EQ(performanceFigures.octaneRating(), 95);

        Car::PerformanceFigures::Acceleration &acceleration = performanceFigures.acceleration();
        EXPECT_EQ(acceleration.count(), 3);
        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 30);
        EXPECT_EQ(acceleration.seconds(), 4.0f);

        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 60);
        EXPECT_EQ(acceleration.seconds(), 7.5f);

        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 100);
        EXPECT_EQ(acceleration.seconds(), 12.2f);

        EXPECT_TRUE(performanceFigures.hasNext());
        performanceFigures.next();
        EXPECT_EQ(performanceFigures.octaneRating(), 99);

        acceleration = performanceFigures.acceleration();
        EXPECT_EQ(acceleration.count(), 3);
        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 30);
        EXPECT_EQ(acceleration.seconds(), 3.8f);

        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 60);
        EXPECT_EQ(acceleration.seconds(), 7.1f);

        EXPECT_TRUE(acceleration.hasNext());
        acceleration.next();
        EXPECT_EQ(acceleration.mph(), 100);
        EXPECT_EQ(acceleration.seconds(), 11.8f);

        return carDecoder_.size();
    }

    virtual int decodeCarMakeAndModel()
    {
        EXPECT_EQ(carDecoder_.makeLength(), 5);
        EXPECT_EQ(std::string(carDecoder_.make(), 5), "Honda");

        EXPECT_EQ(carDecoder_.modelLength(), 9);
        EXPECT_EQ(std::string(carDecoder_.model(), 9), "Civic VTi");

        EXPECT_EQ(carDecoder_.size(), 105);

        return carDecoder_.size();
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
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        hdrDecoder_.wrap(buffer, 0, 0, hdr_.size());
    });

    // non-0 offset
    EXPECT_THROW(
    {
        hdrDecoder_.wrap(buffer, 5, 0, hdr_.size() + 4);
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        hdrDecoder_.wrap(buffer, 5, 0, hdr_.size() + 5);
    });
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForCarWrapForEncodes)
{
    // 0 offset
    EXPECT_THROW(
    {
        car_.wrapForEncode(buffer, 0, Car::sbeBlockLength() - 1);
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        car_.wrapForEncode(buffer, 0, Car::sbeBlockLength());
    });

    // non-0 offset
    EXPECT_THROW(
    {
        car_.wrapForEncode(buffer, 5, Car::sbeBlockLength() + 4);
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        car_.wrapForEncode(buffer, 5, Car::sbeBlockLength() + 5);
    });
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForCarWrapForDecodes)
{
    int actingBlockLength = Car::sbeBlockLength();
    int actingVersion = Car::sbeSchemaVersion();

    encodeCarRoot(buffer, 0, sizeof(buffer));

    // 0 offset
    EXPECT_THROW(
    {
        carDecoder_.wrapForDecode(buffer, 0, actingBlockLength, actingVersion, Car::sbeBlockLength() - 1);
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        carDecoder_.wrapForDecode(buffer, 0, actingBlockLength, actingVersion, Car::sbeBlockLength());
    });

    // non-0 offset
    EXPECT_THROW(
    {
        carDecoder_.wrapForDecode(buffer, 5, actingBlockLength, actingVersion, Car::sbeBlockLength() + 4);
    }, std::runtime_error);

    EXPECT_NO_THROW(
    {
        carDecoder_.wrapForDecode(buffer, 5, actingBlockLength, actingVersion, Car::sbeBlockLength() + 5);
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
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFiguresDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() - 1;

    encodeCarRoot(buffer, 0, sizeof(buffer));
    encodeCarFuelFigures();

    ASSERT_NO_THROW(
        carDecoder_.wrapForDecode(buffer, 0, Car::sbeBlockLength(), Car::sbeSchemaVersion(), sz);
    );

    EXPECT_THROW(
    {
        carDecoder_.fuelFigures();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures1stNextEncode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + Car::FuelFigures::sbeBlockLength() - 1;

    car_.wrapForEncode(buffer, 0, sz);
    Car::FuelFigures &fuelFigures = car_.fuelFiguresCount(2);

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures1stNextDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + Car::FuelFigures::sbeBlockLength() - 1;

    encodeCarRoot(buffer, 0, sizeof(buffer));
    encodeCarFuelFigures();

    carDecoder_.wrapForDecode(buffer, 0, Car::sbeBlockLength(), Car::sbeSchemaVersion(), sz);
    Car::FuelFigures &fuelFigures = carDecoder_.fuelFigures();

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, std::runtime_error);
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
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForFuelFigures2ndNextDecode)
{
    int sz = Car::sbeBlockLength() + Car::FuelFigures::sbeHeaderSize() + (Car::FuelFigures::sbeBlockLength() * 2) - 1;

    encodeCarRoot(buffer, 0, sizeof(buffer));
    encodeCarFuelFigures();

    carDecoder_.wrapForDecode(buffer, 0, Car::sbeBlockLength(), Car::sbeSchemaVersion(), sz);
    Car::FuelFigures &fuelFigures = carDecoder_.fuelFigures();
    fuelFigures.next();

    EXPECT_THROW(
    {
        fuelFigures.next();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldEncodeCorrectSizesAtEachStage)
{
    EXPECT_EQ(encodeHdr(buffer, 0, sizeof(buffer)), 8);
    EXPECT_EQ(encodeCarRoot(buffer, 8, sizeof(buffer)), 21);
    EXPECT_EQ(encodeCarFuelFigures(), 42);
    EXPECT_EQ(encodeCarPerformanceFigures(), 89);
    EXPECT_EQ(encodeCarMakeAndModel(), 105);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForEncodingPerformanceFigures)
{
    // fail in outer dimensions
    encodeCarRoot(buffer, 0, 43);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in 1st inner dimensions
    encodeCarRoot(buffer, 0, 47);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in accel
    encodeCarRoot(buffer, 0, 51);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in random spot before end
    encodeCarRoot(buffer, 0, 63);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail just short of end
    encodeCarRoot(buffer, 0, 88);
    encodeCarFuelFigures();
    EXPECT_THROW(
    {
        encodeCarPerformanceFigures();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForEncodingMakeAndModel)
{
    // fail short of string
    encodeCarRoot(buffer, 0, 90);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, std::runtime_error);

    // fail in string
    encodeCarRoot(buffer, 0, 92);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, std::runtime_error);

    // fail short of string
    encodeCarRoot(buffer, 0, 95);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, std::runtime_error);

    // fail in string
    encodeCarRoot(buffer, 0, 104);
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        encodeCarMakeAndModel();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldNotExceptionWhenBufferJustRightForEntireEncode)
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

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForDecodingPerformanceFigures)
{
    ASSERT_NO_THROW(
    {
        encodeCarRoot(buffer, 0, sizeof(buffer));
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeAndModel();
    });

    // fail in dimensions 43
    decodeCarRoot(buffer, 0, 43);
    decodeCarFuelFigures();

    EXPECT_THROW(
    {
        decodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in 1st inner dimensions 47
    decodeCarRoot(buffer, 0, 47);
    decodeCarFuelFigures();

    EXPECT_THROW(
    {
        decodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in accel 51
    decodeCarRoot(buffer, 0, 51);
    decodeCarFuelFigures();

    EXPECT_THROW(
    {
        decodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail in random spot before end 63
    decodeCarRoot(buffer, 0, 63);
    decodeCarFuelFigures();

    EXPECT_THROW(
    {
        decodeCarPerformanceFigures();
    }, std::runtime_error);

    // fail just short of end 88
    decodeCarRoot(buffer, 0, 88);
    decodeCarFuelFigures();

    EXPECT_THROW(
    {
        decodeCarPerformanceFigures();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldExceptionWhenBufferTooShortForDecodingMakeAndModel)
{
    ASSERT_NO_THROW(
    {
        encodeCarRoot(buffer, 0, sizeof(buffer));
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeAndModel();
    });

    // fail short of string
    decodeCarRoot(buffer, 0, 90);
    decodeCarFuelFigures();
    decodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        decodeCarMakeAndModel();
    }, std::runtime_error);

    // fail in string
    decodeCarRoot(buffer, 0, 92);
    decodeCarFuelFigures();
    decodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        decodeCarMakeAndModel();
    }, std::runtime_error);

    // fail short of string
    decodeCarRoot(buffer, 0, 95);
    decodeCarFuelFigures();
    decodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        decodeCarMakeAndModel();
    }, std::runtime_error);

    // fail in string
    decodeCarRoot(buffer, 0, 104);
    decodeCarFuelFigures();
    decodeCarPerformanceFigures();
    EXPECT_THROW(
    {
        decodeCarMakeAndModel();
    }, std::runtime_error);
}

TEST_F(BoundsCheckTest, shouldDecodeCorrectSizesAtEachStage)
{
    ASSERT_NO_THROW(
    {
        encodeHdr(buffer, 0, sizeof(buffer));
        encodeCarRoot(buffer, hdr_.size(), sizeof(buffer));
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeAndModel();
    });

    EXPECT_EQ(decodeHdr(buffer, 0, sizeof(buffer)), 8);
    EXPECT_EQ(decodeCarRoot(buffer, hdrDecoder_.size(), sizeof(buffer)), 21);
    EXPECT_EQ(decodeCarFuelFigures(), 42);
    EXPECT_EQ(decodeCarPerformanceFigures(), 89);
    EXPECT_EQ(decodeCarMakeAndModel(), 105);
}

TEST_F(BoundsCheckTest, shouldNotExceptionWhenBufferJustRightForEntireDecode)
{
    ASSERT_NO_THROW(
    {
        encodeHdr(buffer, 0, sizeof(buffer));
        encodeCarRoot(buffer, hdr_.size(), sizeof(buffer));
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeAndModel();
    });

    ASSERT_NO_THROW(
    {
        decodeHdr(buffer, 0, 113);
        decodeCarRoot(buffer, hdr_.size(), 113);
        decodeCarFuelFigures();
        decodeCarPerformanceFigures();
        decodeCarMakeAndModel();
    });
}

