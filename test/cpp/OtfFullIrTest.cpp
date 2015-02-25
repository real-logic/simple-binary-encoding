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
#include "otf_api/Ir.h"
#include "otf_api/IrCollection.h"
#include "otf_api/Listener.h"

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

// Template Id for the Car message
static const uint8_t carTemplateId = 1;
// Ids of fields within the Car message
static const uint8_t fieldIdCharConst = 100;
static const uint8_t fieldIdSerialNumber = 1;
static const uint8_t fieldIdModelYear = 2;
static const uint8_t fieldIdAvailable = 3;
static const uint8_t fieldIdCode = 4;
static const uint8_t fieldIdVehicleCode = 6;
static const uint8_t fieldIdExtras = 5;
static const uint8_t fieldIdEngine = 7;
static const uint8_t fieldIdFuelFigures = 8;
static const uint8_t fieldIdFuelSpeed = 9;
static const uint8_t fieldIdFuelMpg = 10;
static const uint8_t fieldIdPerformanceFigures = 11;
static const uint8_t fieldIdPerfOctaneRating = 12;
static const uint8_t fieldIdPerfAcceleration = 13;
static const uint8_t fieldIdPerfAccMph = 14;
static const uint8_t fieldId_PerfAccSeconds = 15;
static const uint8_t fieldIdMake = 16;
static const uint8_t fieldIdModel = 17;

// Sample data for the Car message
static const char charConstValue = 'g';
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

static const size_t VEHICLE_CODE_LENGTH = sizeof(VEHICLE_CODE);
static const size_t MANUFACTURER_CODE_LENGTH = sizeof(MANUFACTURER_CODE);
static const size_t MAKE_LENGTH = 5;
static const size_t MODEL_LENGTH = 9;
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

class OtfFullIrTest : public testing::Test, public IrCollection, public Ir::Callback,
                      public OnNext, public OnError, public OnCompleted
{
public:
    char buffer[2048];
    Listener listener;
    int eventNumber_;

    virtual void SetUp()
    {
        eventNumber_ = 0;
    }

    virtual int encodeHdrAndCar()
    {
        MessageHeader hdr_;
        Car car_;

        hdr_.wrap(buffer, 0, 0, sizeof(buffer))
            .blockLength(Car::sbeBlockLength())
            .templateId(Car::sbeTemplateId())
            .schemaId(Car::sbeSchemaId())
            .version(Car::sbeSchemaVersion());

        car_.wrapForEncode(buffer, hdr_.size(), sizeof(buffer))
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
            .capacity(engineCapacity)
            .numCylinders(engineNumCylinders)
            .putManufacturerCode(MANUFACTURER_CODE);

        car_.fuelFiguresCount(FUEL_FIGURES_COUNT)
            .next().speed(fuel1Speed).mpg(fuel1Mpg)
            .next().speed(fuel2Speed).mpg(fuel2Mpg)
            .next().speed(fuel3Speed).mpg(fuel3Mpg);

        Car::PerformanceFigures &perfFigs = car_.performanceFiguresCount(PERFORMANCE_FIGURES_COUNT);

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

        car_.putMake(MAKE, strlen(MAKE));
        car_.putModel(MODEL, strlen(MODEL));

        return hdr_.size() + car_.size();
    }

    virtual Ir *irForTemplateId(const int templateId, const int schemaVersion)
    {
        EXPECT_EQ(templateId, Car::sbeTemplateId());
        EXPECT_EQ(schemaVersion, Car::sbeSchemaVersion());

        Ir *tmplt = (Ir *)IrCollection::message(templateId, schemaVersion);
        return tmplt;
    }
    
    void checkEvent(const Field &f, const Group &g)
    {
        char tmp[256];

        switch(EventNumber(eventNumber_))
        {
            case EN_header:
            {
                EXPECT_EQ(f.isComposite(), true);
                EXPECT_EQ(f.numEncodings(), HID_NumEncodings);
                EXPECT_EQ(f.primitiveType(HID_BlockLength), Ir::UINT16);
                EXPECT_EQ(f.getUInt(HID_BlockLength), Car::sbeBlockLength());
                EXPECT_EQ(f.primitiveType(HID_TemplateId), Ir::UINT16);
                EXPECT_EQ(f.getUInt(HID_TemplateId), Car::sbeTemplateId());
                EXPECT_EQ(f.primitiveType(HID_SchemaId), Ir::UINT16);
                EXPECT_EQ(f.getUInt(HID_SchemaId), Car::sbeSchemaId());
                EXPECT_EQ(f.primitiveType(HID_SchemaVersion), Ir::UINT16);
                EXPECT_EQ(f.getUInt(HID_SchemaVersion), Car::sbeSchemaVersion());
                break;
            }
            case EN_charConst:
            {
                EXPECT_EQ(f.schemaId(), fieldIdCharConst);
                EXPECT_EQ(f.primitiveType(), Ir::CHAR);
                EXPECT_EQ(f.length(), 1);
                EXPECT_EQ(f.getUInt(), charConstValue);
                break;
            }
            case EN_serialNumber:
            {
                EXPECT_EQ(f.schemaId(), fieldIdSerialNumber);
                EXPECT_EQ(f.primitiveType(), Ir::UINT32);
                EXPECT_EQ(f.getUInt(), SERIAL_NUMBER);
                break;
            }
            case EN_modelYear:
            {
                EXPECT_EQ(f.schemaId(), fieldIdModelYear);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), MODEL_YEAR);
                break;
            }
            case EN_available:
            {
                EXPECT_EQ(f.schemaId(), fieldIdAvailable);
                EXPECT_EQ(f.primitiveType(), Ir::UINT8);
                EXPECT_EQ(f.getUInt(), 1);
                break;
            }
            case EN_code:
            {
                EXPECT_EQ(f.schemaId(), fieldIdCode);
                EXPECT_EQ(f.primitiveType(), Ir::CHAR);
                EXPECT_EQ(f.getUInt(), 'A');
                break;
            }
            case EN_vehicleCode:
            {
                EXPECT_EQ(f.schemaId(), fieldIdVehicleCode);
                EXPECT_EQ(f.primitiveType(), Ir::CHAR);
                EXPECT_EQ(f.length(), VEHICLE_CODE_LENGTH);
                f.getArray(0, tmp, 0, f.length());
                EXPECT_EQ(std::string(tmp, VEHICLE_CODE_LENGTH), std::string(VEHICLE_CODE, VEHICLE_CODE_LENGTH));
                break;
            }
            case EN_extras:
            {
                EXPECT_EQ(f.isSet(), true);
                EXPECT_EQ(f.schemaId(), fieldIdExtras);
                EXPECT_EQ(f.primitiveType(), Ir::UINT8);
                EXPECT_EQ(f.getUInt(), 0x6);
                break;
            }
            case EN_engine:
            {
                EXPECT_EQ(f.isComposite(), true);
                EXPECT_EQ(f.schemaId(), fieldIdEngine);
                EXPECT_EQ(f.numEncodings(), 5);
                EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
                EXPECT_EQ(f.getUInt(0), engineCapacity);
                EXPECT_EQ(f.primitiveType(1), Ir::UINT8);
                EXPECT_EQ(f.getUInt(1), 4);
                EXPECT_EQ(f.primitiveType(2), Ir::UINT16);
                EXPECT_EQ(f.getUInt(2), 9000);
                EXPECT_EQ(f.primitiveType(3), Ir::CHAR);
                EXPECT_EQ(f.length(3), MANUFACTURER_CODE_LENGTH);
                f.getArray(3, tmp, 0, f.length(3));
                EXPECT_EQ(std::string(tmp, MANUFACTURER_CODE_LENGTH), std::string(MANUFACTURER_CODE, MANUFACTURER_CODE_LENGTH));
                EXPECT_EQ(f.primitiveType(4), Ir::CHAR);
                EXPECT_EQ(f.length(4), 6);
                f.getArray(4, tmp, 0, f.length(4));
                EXPECT_EQ(std::string(tmp, 6), std::string("Petrol"));
                break;
            }
            case EN_fuelFigures1:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_fuelFigures1_speed:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelSpeed);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), fuel1Speed);
                break;
            }
            case EN_fuelFigures1_mpg:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelMpg);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), fuel1Mpg);
                break;
            }
            case EN_fuelFigures1_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_fuelFigures2:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_fuelFigures2_speed:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelSpeed);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), fuel2Speed);
                break;
            }
            case EN_fuelFigures2_mpg:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelMpg);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), fuel2Mpg);
                break;
            }
            case EN_fuelFigures2_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_fuelFigures3:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_fuelFigures3_speed:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelSpeed);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), fuel3Speed);
                break;
            }
            case EN_fuelFigures3_mpg:
            {
                EXPECT_EQ(f.schemaId(), fieldIdFuelMpg);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), fuel3Mpg);
                break;
            }
            case EN_fuelFigures3_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdFuelFigures);
                break;
            }
            case EN_performanceFigures1:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerformanceFigures);
                break;
            }
            case EN_performanceFigures1_octaneRating:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfOctaneRating);
                EXPECT_EQ(f.primitiveType(), Ir::UINT8);
                EXPECT_EQ(f.getUInt(), perf1Octane);
                break;
            }
            case EN_performanceFigures1_acceleration1:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_acceleration1_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf1aMph);
                break;
            }
            case EN_performanceFigures1_acceleration1_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf1aSeconds);
                break;
            }
            case EN_performanceFigures1_acceleration1_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_acceleration2:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(),fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_acceleration2_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf1bMph);
                break;
            }
            case EN_performanceFigures1_acceleration2_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf1bSeconds);
                break;
            }
            case EN_performanceFigures1_acceleration2_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_acceleration3:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_acceleration3_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf1cMph);
                break;
            }
            case EN_performanceFigures1_acceleration3_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf1cSeconds);
                break;
            }
            case EN_performanceFigures1_acceleration3_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures1_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerformanceFigures);
                break;
            }
            case EN_performanceFigures2:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerformanceFigures);
                break;
            }
            case EN_performanceFigures2_octaneRating:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfOctaneRating);
                EXPECT_EQ(f.primitiveType(), Ir::UINT8);
                EXPECT_EQ(f.getUInt(), perf2Octane);
                break;
            }
            case EN_performanceFigures2_acceleration1:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_acceleration1_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf2aMph);
                break;
            }
            case EN_performanceFigures2_acceleration1_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf2aSeconds);
                break;
            }
            case EN_performanceFigures2_acceleration1_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_acceleration2:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_acceleration2_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf2bMph);
                break;
            }
            case EN_performanceFigures2_acceleration2_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf2bSeconds);
                break;
            }
            case EN_performanceFigures2_acceleration2_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_acceleration3:
            {
                EXPECT_EQ(g.event(), Group::START);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_acceleration3_mph:
            {
                EXPECT_EQ(f.schemaId(), fieldIdPerfAccMph);
                EXPECT_EQ(f.primitiveType(), Ir::UINT16);
                EXPECT_EQ(f.getUInt(), perf2cMph);
                break;
            }
            case EN_performanceFigures2_acceleration3_seconds:
            {
                EXPECT_EQ(f.schemaId(), fieldId_PerfAccSeconds);
                EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
                EXPECT_EQ(f.getDouble(), perf2cSeconds);
                break;
            }
            case EN_performanceFigures2_acceleration3_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerfAcceleration);
                break;
            }
            case EN_performanceFigures2_end:
            {
                EXPECT_EQ(g.event(), Group::END);
                EXPECT_EQ(g.schemaId(), fieldIdPerformanceFigures);
                break;
            }
            case EN_make:
            {
                EXPECT_EQ(f.schemaId(), fieldIdMake);
                EXPECT_EQ(f.length(1), MAKE_LENGTH);
                f.getArray(1, tmp, 0, f.length(1));
                EXPECT_EQ(std::string(tmp, MAKE_LENGTH), MAKE);
                break;
            }
            case EN_model:
            {
                EXPECT_EQ(f.schemaId(), fieldIdModel);
                EXPECT_EQ(f.length(1), MODEL_LENGTH);
                f.getArray(1, tmp, 0, f.length(1));
                EXPECT_EQ(std::string(tmp, MODEL_LENGTH), MODEL);
                break;
            }
            case EN_complete:
            {
                EXPECT_TRUE(false)<< "More events than expected!" << std::endl;
            }
        }
    }

    virtual int onNext(const Field &f)
    {
        Group dummy;

        checkEvent(f, dummy);
        eventNumber_++;
        return 0;
    }

    virtual int onNext(const Group &g)
    {
        Field dummy;

        checkEvent(dummy, g);
        eventNumber_++;
        return 0;
    }

    virtual int onError(const Error &e)
    {
        EXPECT_TRUE(false) << "Error " << e.message() << "\n";
        return 0;
    }

    virtual int onCompleted()
    {
        EXPECT_EQ(eventNumber_, EN_complete);
        return 0;
    }

};

TEST_F(OtfFullIrTest, shouldHandleAllEventsCorrectltInOrder)
{
    ASSERT_EQ(encodeHdrAndCar(), 113);

    ASSERT_GE(IrCollection::loadFromFile("code-generation-schema-cpp.sbeir"), 0);

    listener.dispatchMessageByHeader(IrCollection::header(), this)
            .resetForDecode(buffer, 113)
            .subscribe(this, this, this);

    ASSERT_EQ(listener.bufferOffset(), 113);
}