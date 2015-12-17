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
#include "otf/Token.h"
#include "otf/IrDecoder.h"
#include "otf/OtfHeaderDecoder.h"
#include "otf/OtfMessageDecoder.h"

using namespace std;
using namespace code_generation_test;

// Field Ids in the header.
enum HeaderIds
{
    HID_BlockLength = 0,
    HID_TemplateId,
    HID_SchemaId,
    HID_SchemaVersion,
    HID_NumEncodings
};

static const sbe_uint32_t SERIAL_NUMBER = 1234;
static const sbe_uint16_t MODEL_YEAR = 2013;
static const BooleanType::Value AVAILABLE = BooleanType::T;
static const Model::Value CODE = Model::A;
static const bool CRUISE_CONTROL = true;
static const bool SPORTS_PACK = true;
static const bool SUNROOF = false;

static char VEHICLE_CODE[] = { 'a', 'b', 'c', 'd', 'e', 'f' };
static char MANUFACTURER_CODE[] = { '1', '2', '3' };
static const char *MAKE = "Honda";
static const char *MODEL = "Civic VTi";

static const size_t PERFORMANCE_FIGURES_COUNT = 2;
static const size_t FUEL_FIGURES_COUNT = 3;
static const size_t ACCELERATION_COUNT = 3;

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

// This enum represents the expected events that
// will be received during the decoding process.
// Warning: this is for testing only.  Do not use this technique in production code.
enum EventNumber {
    EN_header = 0,
    EN_charConst,
    EN_serialNumber,
    EN_modelYear,
    EN_available,
    EN_code,
    EN_vehicleCode,
    EN_extras,
    EN_engine,
    EN_fuelFigures1,
    EN_fuelFigures1_speed,
    EN_fuelFigures1_mpg,
    EN_fuelFigures1_end,
    EN_fuelFigures2,
    EN_fuelFigures2_speed,
    EN_fuelFigures2_mpg,
    EN_fuelFigures2_end,
    EN_fuelFigures3,
    EN_fuelFigures3_speed,
    EN_fuelFigures3_mpg,
    EN_fuelFigures3_end,
    EN_performanceFigures1,
    EN_performanceFigures1_octaneRating,
    EN_performanceFigures1_acceleration1,
    EN_performanceFigures1_acceleration1_mph,
    EN_performanceFigures1_acceleration1_seconds,
    EN_performanceFigures1_acceleration1_end,
    EN_performanceFigures1_acceleration2,
    EN_performanceFigures1_acceleration2_mph,
    EN_performanceFigures1_acceleration2_seconds,
    EN_performanceFigures1_acceleration2_end,
    EN_performanceFigures1_acceleration3,
    EN_performanceFigures1_acceleration3_mph,
    EN_performanceFigures1_acceleration3_seconds,
    EN_performanceFigures1_acceleration3_end,
    EN_performanceFigures1_end,
    EN_performanceFigures2,
    EN_performanceFigures2_octaneRating,
    EN_performanceFigures2_acceleration1,
    EN_performanceFigures2_acceleration1_mph,
    EN_performanceFigures2_acceleration1_seconds,
    EN_performanceFigures2_acceleration1_end,
    EN_performanceFigures2_acceleration2,
    EN_performanceFigures2_acceleration2_mph,
    EN_performanceFigures2_acceleration2_seconds,
    EN_performanceFigures2_acceleration2_end,
    EN_performanceFigures2_acceleration3,
    EN_performanceFigures2_acceleration3_mph,
    EN_performanceFigures2_acceleration3_seconds,
    EN_performanceFigures2_acceleration3_end,
    EN_performanceFigures2_end,
    EN_make,
    EN_model,
    EN_complete
};

class Rc3OtfFullIrTest : public testing::Test
{
public:
    char buffer[2048];
    IrDecoder m_irDecoder;
    int m_eventNumber;

    virtual void SetUp()
    {
        m_eventNumber = 0;
    }

    virtual int encodeHdrAndCar()
    {
        MessageHeader hdr;
        Car car;

        hdr.wrap(buffer, 0, 0, sizeof(buffer))
            .blockLength(Car::sbeBlockLength())
            .templateId(Car::sbeTemplateId())
            .schemaId(Car::sbeSchemaId())
            .version(Car::sbeSchemaVersion());

        car.wrapForEncode(buffer, hdr.size(), sizeof(buffer))
            .serialNumber(SERIAL_NUMBER)
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

        return hdr.size() + car.size();
    }

};

TEST_F(Rc3OtfFullIrTest, shouldHandleAllEventsCorrectlyInOrder)
{
    ASSERT_EQ(encodeHdrAndCar(), 113);

    ASSERT_GE(m_irDecoder.decode("code-generation-schema-cpp.sbeir"), 0);

    std::shared_ptr<std::vector<Token>> headerTokens = m_irDecoder.header();
    std::shared_ptr<std::vector<Token>> messageTokens = m_irDecoder.message(Car::sbeTemplateId(), Car::sbeSchemaVersion());

    ASSERT_TRUE(headerTokens != nullptr);
    ASSERT_TRUE(messageTokens!= nullptr);
//    listener.dispatchMessageByHeader(IrCollection::header(), this)
//    .resetForDecode(buffer, 113)
//    .subscribe(this, this, this);
//
//    ASSERT_EQ(listener.bufferOffset(), 113);
}