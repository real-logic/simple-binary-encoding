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

#define SERIAL_NUMBER 1234u
#define MODEL_YEAR 2013
#define AVAILABLE (BooleanType::T)
#define CODE (Model::A)
#define CRUISE_CONTROL (true)
#define SPORTS_PACK (true)
#define SUNROOF (false)

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *MAKE = "Honda";
static const char *MODEL = "Civic VTi";
static const char *ACTIVATION_CODE = "deadbeef";

static const int encodedHdrSz = 8;
static const int encodedCarSz = 179;

class BoundsCheckTest : public testing::Test
{
public:

    virtual int encodeHdr(char *buffer, int offset, int bufferLength)
    {
        m_hdr.wrap(buffer, offset, 0, bufferLength)
            .blockLength(Car::sbeBlockLength())
            .templateId(Car::sbeTemplateId())
            .schemaId(Car::sbeSchemaId())
            .version(Car::sbeSchemaVersion());

        return m_hdr.size();
    }

    virtual int decodeHdr(char *buffer, int offset, int bufferLength)
    {
        m_hdrDecoder.wrap(buffer, offset, 0, bufferLength);

        EXPECT_EQ(m_hdrDecoder.blockLength(), Car::sbeBlockLength());
        EXPECT_EQ(m_hdrDecoder.templateId(), Car::sbeTemplateId());
        EXPECT_EQ(m_hdrDecoder.schemaId(), Car::sbeSchemaId());
        EXPECT_EQ(m_hdrDecoder.version(), Car::sbeSchemaVersion());

        return m_hdrDecoder.size();
    }

    virtual int encodeCarRoot(char *buffer, int offset, int bufferLength)
    {
        m_car.wrapForEncode(buffer, offset, bufferLength)
            .serialNumber(SERIAL_NUMBER)
            .modelYear(MODEL_YEAR)
            .available(AVAILABLE)
            .code(CODE)
            .putVehicleCode(VEHICLE_CODE);

        for (int i = 0; i < Car::someNumbersLength(); i++)
        {
            m_car.someNumbers(i, i);
        }

        m_car.extras().clear()
            .cruiseControl(CRUISE_CONTROL)
            .sportsPack(SPORTS_PACK)
            .sunRoof(SUNROOF);

        m_car.engine()
            .capacity(2000)
            .numCylinders((short)4)
            .putManufacturerCode(MANUFACTURER_CODE);

        return m_car.size();
    }

    virtual int encodeCarFuelFigures()
    {
        Car::FuelFigures& fuelFigures = m_car.fuelFiguresCount(3);

        fuelFigures
            .next().speed(30).mpg(35.9f);
        fuelFigures.putUsageDescription("Urban Cycle", 11);

        fuelFigures
            .next().speed(55).mpg(49.0f);
        fuelFigures.putUsageDescription("Combined Cycle", 14);

        fuelFigures
            .next().speed(75).mpg(40.0f);
        fuelFigures.putUsageDescription("Highway Cycle", 13);

        return m_car.size();
    }

    virtual int encodeCarPerformanceFigures()
    {
        Car::PerformanceFigures &perfFigs = m_car.performanceFiguresCount(2);

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

        return m_car.size();
    }

    virtual int encodeCarMakeModelAndActivationCode()
    {
        m_car.putMake(MAKE, static_cast<int>(strlen(MAKE)));
        m_car.putModel(MODEL, static_cast<int>(strlen(MODEL)));
        m_car.putActivationCode(ACTIVATION_CODE, static_cast<int>(strlen(ACTIVATION_CODE)));

        return m_car.size();
    }

    virtual int decodeCarRoot(char *buffer, const int offset, const int bufferLength)
    {
        m_carDecoder.wrapForDecode(buffer, offset, Car::sbeBlockLength(), Car::sbeSchemaVersion(), bufferLength);
        EXPECT_EQ(m_carDecoder.serialNumber(), SERIAL_NUMBER);
        EXPECT_EQ(m_carDecoder.modelYear(), MODEL_YEAR);
        EXPECT_EQ(m_carDecoder.available(), AVAILABLE);
        EXPECT_EQ(m_carDecoder.code(), CODE);

        EXPECT_EQ(m_carDecoder.someNumbersLength(), 5);
        for (int i = 0; i < 5; i++)
        {
            EXPECT_EQ(m_carDecoder.someNumbers(i), i);
        }

        EXPECT_EQ(m_carDecoder.vehicleCodeLength(), 6);
        EXPECT_EQ(std::string(m_carDecoder.vehicleCode(), 6), std::string(VEHICLE_CODE, 6));
        EXPECT_EQ(m_carDecoder.extras().cruiseControl(), true);
        EXPECT_EQ(m_carDecoder.extras().sportsPack(), true);
        EXPECT_EQ(m_carDecoder.extras().sunRoof(), false);

        Engine &engine = m_carDecoder.engine();
        EXPECT_EQ(engine.capacity(), 2000);
        EXPECT_EQ(engine.numCylinders(), 4);
        EXPECT_EQ(engine.maxRpm(), 9000);
        EXPECT_EQ(engine.manufacturerCodeLength(), 3);
        EXPECT_EQ(std::string(engine.manufacturerCode(), 3), std::string(MANUFACTURER_CODE, 3));
        EXPECT_EQ(engine.fuelLength(), 6);
        EXPECT_EQ(std::string(engine.fuel(), 6), std::string("Petrol"));

        return m_carDecoder.size();
    }

    virtual int decodeCarFuelFigures()
    {
        Car::FuelFigures &fuelFigures = m_carDecoder.fuelFigures();
        EXPECT_EQ(fuelFigures.count(), 3);

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 30);
        EXPECT_EQ(fuelFigures.mpg(), 35.9f);
        EXPECT_EQ(fuelFigures.usageDescriptionLength(), 11);
        EXPECT_EQ(std::string(fuelFigures.usageDescription(), 11), "Urban Cycle");

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 55);
        EXPECT_EQ(fuelFigures.mpg(), 49.0f);
        EXPECT_EQ(fuelFigures.usageDescriptionLength(), 14);
        EXPECT_EQ(std::string(fuelFigures.usageDescription(), 14), "Combined Cycle");

        EXPECT_TRUE(fuelFigures.hasNext());
        fuelFigures.next();
        EXPECT_EQ(fuelFigures.speed(), 75);
        EXPECT_EQ(fuelFigures.mpg(), 40.0f);
        EXPECT_EQ(fuelFigures.usageDescriptionLength(), 13);
        EXPECT_EQ(std::string(fuelFigures.usageDescription(), 13), "Highway Cycle");

        return m_carDecoder.size();
    }

    virtual int decodeCarPerformanceFigures()
    {
        Car::PerformanceFigures &performanceFigures = m_carDecoder.performanceFigures();
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

        return m_carDecoder.size();
    }

    virtual int decodeCarMakeModelAndActivationCode()
    {
        EXPECT_EQ(m_carDecoder.makeLength(), 5);
        EXPECT_EQ(std::string(m_carDecoder.make(), 5), "Honda");

        EXPECT_EQ(m_carDecoder.modelLength(), 9);
        EXPECT_EQ(std::string(m_carDecoder.model(), 9), "Civic VTi");

        EXPECT_EQ(m_carDecoder.activationCodeLength(), 8);
        EXPECT_EQ(std::string(m_carDecoder.activationCode(), 8), "deadbeef");

        EXPECT_EQ(m_carDecoder.size(), encodedCarSz);

        return m_carDecoder.size();
    }

    MessageHeader m_hdr;
    MessageHeader m_hdrDecoder;
    Car m_car;
    Car m_carDecoder;
    char m_buffer[2048];
};

class HeaderBoundsCheckTest : public BoundsCheckTest, public ::testing::WithParamInterface<int>
{
};

TEST_P(HeaderBoundsCheckTest, shouldExceptionWhenBufferTooShortForEncodeOfHeader)
{
    const int length = GetParam();

    EXPECT_THROW(
    {
        encodeHdr(m_buffer, 0, length);
    }, std::runtime_error);
}

TEST_P(HeaderBoundsCheckTest, shouldExceptionWhenBufferTooShortForDecodeOfHeader)
{
    const int length = GetParam();

    encodeHdr(m_buffer, 0, sizeof(m_buffer));

    EXPECT_THROW(
    {
        decodeHdr(m_buffer, 0, length);
    }, std::runtime_error);
}

INSTANTIATE_TEST_CASE_P(
    HeaderLengthTest,
    HeaderBoundsCheckTest,
    ::testing::Range(0, encodedHdrSz, 1));

class MessageBoundsCheckTest : public BoundsCheckTest, public ::testing::WithParamInterface<int>
{
};

TEST_P(MessageBoundsCheckTest, shouldExceptionWhenBufferTooShortForEncodeOfMessage)
{
    const int length = GetParam();

    EXPECT_THROW(
    {
        encodeCarRoot(m_buffer, 0, length);
        encodeCarFuelFigures();
        encodeCarPerformanceFigures();
        encodeCarMakeModelAndActivationCode();
    }, std::runtime_error);
}

TEST_P(MessageBoundsCheckTest, shouldExceptionWhenBufferTooShortForDecodeOfMessage)
{
    const int length = GetParam();

    encodeCarRoot(m_buffer, 0, sizeof(m_buffer));
    encodeCarFuelFigures();
    encodeCarPerformanceFigures();
    encodeCarMakeModelAndActivationCode();

    EXPECT_THROW(
    {
        decodeCarRoot(m_buffer, 0, length);
        decodeCarFuelFigures();
        decodeCarPerformanceFigures();
        decodeCarMakeModelAndActivationCode();
    }, std::runtime_error);
}

INSTANTIATE_TEST_CASE_P(
    MessageLengthTest,
    MessageBoundsCheckTest,
    ::testing::Range(0, encodedCarSz, 1));
