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

        if (eventNumber_ == 0)
        {
            EXPECT_EQ(f.isComposite(), true);
            EXPECT_EQ(f.numEncodings(), 4);
            EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
            EXPECT_EQ(f.getUInt(0), Car::sbeBlockLength());
            EXPECT_EQ(f.primitiveType(1), Ir::UINT16);
            EXPECT_EQ(f.getUInt(1), Car::sbeTemplateId());
            EXPECT_EQ(f.primitiveType(2), Ir::UINT16);
            EXPECT_EQ(f.getUInt(2), Car::sbeSchemaId());
            EXPECT_EQ(f.primitiveType(3), Ir::UINT16);
            EXPECT_EQ(f.getUInt(3), Car::sbeSchemaVersion());
        }
        else if (eventNumber_ == 1)
        {
            EXPECT_EQ(f.schemaId(), 100);
            EXPECT_EQ(f.primitiveType(), Ir::CHAR);
            EXPECT_EQ(f.length(), 1);
            EXPECT_EQ(f.getUInt(), 'g');
        }
        else if (eventNumber_ == 2)
        {
            EXPECT_EQ(f.schemaId(), 1);
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), SERIAL_NUMBER);
        }
        else if (eventNumber_ == 3)
        {
            EXPECT_EQ(f.schemaId(), 2);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), MODEL_YEAR);
        }
        else if (eventNumber_ == 4)
        {
            EXPECT_EQ(f.schemaId(), 3);
            EXPECT_EQ(f.primitiveType(), Ir::UINT8);
            EXPECT_EQ(f.getUInt(), 1);
        }
        else if (eventNumber_ == 5)
        {
            EXPECT_EQ(f.schemaId(), 4);
            EXPECT_EQ(f.primitiveType(), Ir::CHAR);
            EXPECT_EQ(f.getUInt(), 'A');
        }
        else if (eventNumber_ == 6)
        {
            EXPECT_EQ(f.schemaId(), 6);
            EXPECT_EQ(f.primitiveType(), Ir::CHAR);
            EXPECT_EQ(f.length(), 6);
            f.getArray(0, tmp, 0, f.length());
            EXPECT_EQ(std::string(tmp, 6), std::string(VEHICLE_CODE, 6));
        }
        else if (eventNumber_ == 7)
        {
            EXPECT_EQ(f.isSet(), true);
            EXPECT_EQ(f.schemaId(), 5);
            EXPECT_EQ(f.primitiveType(), Ir::UINT8);
            EXPECT_EQ(f.getUInt(), 0x6);
        }
        else if (eventNumber_ == 8)
        {
            EXPECT_EQ(f.isComposite(), true);
            EXPECT_EQ(f.schemaId(), 7);
            EXPECT_EQ(f.numEncodings(), 5);
            EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
            EXPECT_EQ(f.getUInt(0), 2000);
            EXPECT_EQ(f.primitiveType(1), Ir::UINT8);
            EXPECT_EQ(f.getUInt(1), 4);
            EXPECT_EQ(f.primitiveType(2), Ir::UINT16);
            EXPECT_EQ(f.getUInt(2), 9000);
            EXPECT_EQ(f.primitiveType(3), Ir::CHAR);
            EXPECT_EQ(f.length(3), 3);
            f.getArray(3, tmp, 0, f.length(3));
            EXPECT_EQ(std::string(tmp, 3), std::string(MANUFACTURER_CODE, 3));
            EXPECT_EQ(f.primitiveType(4), Ir::CHAR);
            EXPECT_EQ(f.length(4), 6);
            f.getArray(4, tmp, 0, f.length(4));
            EXPECT_EQ(std::string(tmp, 6), std::string("Petrol"));
        }
        else if (eventNumber_ == 9)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 10)
        {
            EXPECT_EQ(f.schemaId(), 9);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 30);
        }
        else if (eventNumber_ == 11)
        {
            EXPECT_EQ(f.schemaId(), 10);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 35.9f);
        }
        else if (eventNumber_ == 12)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 13)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 14)
        {
            EXPECT_EQ(f.schemaId(), 9);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 55);
        }
        else if (eventNumber_ == 15)
        {
            EXPECT_EQ(f.schemaId(), 10);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 49.0f);
        }
        else if (eventNumber_ == 16)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 17)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 18)
        {
            EXPECT_EQ(f.schemaId(), 9);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 75);
        }
        else if (eventNumber_ == 19)
        {
            EXPECT_EQ(f.schemaId(), 10);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 40.0f);
        }
        else if (eventNumber_ == 20)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 8);
        }
        else if (eventNumber_ == 21)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 11);
        }
        else if (eventNumber_ == 22)
        {
            EXPECT_EQ(f.schemaId(), 12);
            EXPECT_EQ(f.primitiveType(), Ir::UINT8);
            EXPECT_EQ(f.getUInt(), 95);
        }
        else if (eventNumber_ == 23)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 24)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 30);
        }
        else if (eventNumber_ == 25)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 4.0f);
        }
        else if (eventNumber_ == 26)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 27)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 28)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 60);
        }
        else if (eventNumber_ == 29)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 7.5f);
        }
        else if (eventNumber_ == 30)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 31)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 32)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 100);
        }
        else if (eventNumber_ == 33)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 12.2f);
        }
        else if (eventNumber_ == 34)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 35)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 11);
        }
        else if (eventNumber_ == 36)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 11);
        }
        else if (eventNumber_ == 37)
        {
            EXPECT_EQ(f.schemaId(), 12);
            EXPECT_EQ(f.primitiveType(), Ir::UINT8);
            EXPECT_EQ(f.getUInt(), 99);
        }
        else if (eventNumber_ == 38)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 39)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 30);
        }
        else if (eventNumber_ == 40)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 3.8f);
        }
        else if (eventNumber_ == 41)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 42)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 43)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 60);
        }
        else if (eventNumber_ == 44)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 7.1f);
        }
        else if (eventNumber_ == 45)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 46)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 47)
        {
            EXPECT_EQ(f.schemaId(), 14);
            EXPECT_EQ(f.primitiveType(), Ir::UINT16);
            EXPECT_EQ(f.getUInt(), 100);
        }
        else if (eventNumber_ == 48)
        {
            EXPECT_EQ(f.schemaId(), 15);
            EXPECT_EQ(f.primitiveType(), Ir::FLOAT);
            EXPECT_EQ(f.getDouble(), 11.8f);
        }
        else if (eventNumber_ == 49)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 13);
        }
        else if (eventNumber_ == 50)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 11);
        }
        else if (eventNumber_ == 51)
        {
            EXPECT_EQ(f.schemaId(), 16);
            EXPECT_EQ(f.length(1), 5);
            f.getArray(1, tmp, 0, f.length(1));
            EXPECT_EQ(std::string(tmp, 5), std::string(MAKE, 5));
        }
        else if (eventNumber_ == 52)
        {
            EXPECT_EQ(f.schemaId(), 17);
            EXPECT_EQ(f.length(1), 9);
            f.getArray(1, tmp, 0, f.length(1));
            EXPECT_EQ(std::string(tmp, 9), std::string(MODEL, 9));
        }
        else
        {
            exit(1);
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
        std::cout << "Error " << e.message() << "\n";
        exit(1);
        return 0;
    }

    virtual int onCompleted()
    {
        EXPECT_EQ(eventNumber_, 53);
        return 0;
    }

};

TEST_F(OtfFullIrTest, shouldHandleAllEventsCorrectltInOrder)
{
    ASSERT_EQ(encodeHdrAndCar(), 113);

    ASSERT_GE(IrCollection::loadFromFile("target/test/cpp/code-generation-schema-cpp.sbeir"), 0);

    listener.dispatchMessageByHeader(IrCollection::header(), this)
            .resetForDecode(buffer, 113)
            .subscribe(this, this, this);

    ASSERT_EQ(listener.bufferOffset(), 113);
}