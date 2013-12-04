/*
 * Copyright 2013 Real Logic Ltd.
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
#include <string>

#include <stdio.h>

#include "uk_co_real_logic_sbe_examples/MessageHeader.hpp"
#include "uk_co_real_logic_sbe_examples/Car.hpp"

using namespace std;
using namespace uk_co_real_logic_sbe_examples;

char VEHICLE_CODE[] = {'a', 'b', 'c', 'd', 'e', 'f'};
char MANUFACTURER_CODE[] = {'1', '2', '3'};
const char *MAKE = "Honda";
const char *MODEL = "Civic VTi";
const int messageHeaderVersion = 0;

void encodeHdr(MessageHeader &hdr, Car &car, char *buffer, int offset)
{
    // encode the header
    hdr.wrap(buffer, offset, messageHeaderVersion)
       .blockLength(Car::blockLength())
       .templateId(Car::templateId())
       .version(Car::templateVersion())
       .reserved(0);
}

void decodeHdr(MessageHeader &hdr, char *buffer, int offset)
{
    hdr.wrap(buffer, offset, messageHeaderVersion);

    // decode the header
    cout << "messageHeader.blockLength=" << hdr.blockLength() << endl;
    cout << "messageHeader.templateId=" << hdr.templateId() << endl;
    cout << "messageHeader.version=" << (sbe_uint32_t)hdr.version() << endl;
    cout << "messageHeader.reserved=" << (sbe_uint32_t)hdr.reserved() << endl;
}

void encodeCar(Car &car, char *buffer, int offset)
{
    car.wrapForEncode(buffer, offset)
       .serialNumber(1234)
       .modelYear(2013)
       .available(BooleanType::TRUE)
       .code(Model::A)
       .putVehicleCode(VEHICLE_CODE);

    for (int i = 0, size = car.someNumbersLength(); i < size; i++)
    {
        car.someNumbers(i, i);
    }

    car.extras().clear()
       .cruiseControl(true)
       .sportsPack(true)
       .sunRoof(false);

    car.engine()
       .capacity(2000)
       .numCylinders((short)4)
       .putManufacturerCode(MANUFACTURER_CODE);

    car.fuelFiguresCount(3)
       .next().speed(30).mpg(35.9f)
       .next().speed(55).mpg(49.0f)
       .next().speed(75).mpg(40.0f);

    Car::PerformanceFigures &performanceFigures = car.performanceFiguresCount(2);

    performanceFigures.next()
        .octaneRating((short)95)
        .accelerationCount(3)
            .next().mph(30).seconds(4.0f)
            .next().mph(60).seconds(7.5f)
            .next().mph(100).seconds(12.2f);

    performanceFigures.next()
        .octaneRating((short)99)
        .accelerationCount(3)
            .next().mph(30).seconds(3.8f)
            .next().mph(60).seconds(7.1f)
            .next().mph(100).seconds(11.8f);

    car.putMake(MAKE, strlen(MAKE));
    car.putModel(MODEL, strlen(MODEL));
}

const char *format(int value)
{
    static char buffer[1024];

    snprintf(buffer, sizeof(buffer) - 1, "%d", value);
    return buffer;
}

const char *format(char value)
{
    static char buffer[3];

    snprintf(buffer, sizeof(buffer) - 1, "%c", value);
    return buffer;
}

const char *format(double value)
{
    static char buffer[80];

    snprintf(buffer, sizeof(buffer) - 1, "%g", value);
    return buffer;
}

const char *format(BooleanType::Value value)
{
    if (value == BooleanType::TRUE)
    {
        return "TRUE";
    }
    else
    {
        return "FALSE";
    }
}

const char *format(Model::Value value)
{
    switch (value)
    {
    case Model::A: return "A";
    case Model::B: return "B";
    case Model::C: return "C";
    case Model::NULL_VALUE: return "NULL";
    }
}

const char *format(bool value)
{
    if (value == true)
    {
        return "true";
    }
    else
    {
        return "false";
    }
}

void decodeCar(Car &car, char *buffer, int offset, int actingBlockLength, int actingVersion)
{
    car.wrapForDecode(buffer, offset, actingBlockLength, actingVersion);
    std::string sb;

    sb.append("\ncar.serialNumberId=").append(format(Car::serialNumberSchemaId()));
    sb.append("\ncar.modelYearId=").append(format(Car::modelYearSchemaId()));
    sb.append("\ncar.availableId=").append(format(Car::availableSchemaId()));
    sb.append("\ncar.codeId=").append(format(Car::codeSchemaId()));
    sb.append("\ncar.someNumbersId=").append(format(Car::someNumbersSchemaId()));
    sb.append("\ncar.vehicleCodeId=").append(format(Car::vehicleCodeSchemaId()));
    sb.append("\ncar.extrasId=").append(format(Car::extrasSchemaId()));
    sb.append("\ncar.engineId=").append(format(Car::engineSchemaId()));
    sb.append("\ncar.fuelFiguresId=").append(format(Car::fuelFiguresSchemaId()));
    sb.append("\ncar.fuelFigures.speedId=").append(format(Car::FuelFigures::speedSchemaId()));
    sb.append("\ncar.fuelFigures.mpgId=").append(format(Car::FuelFigures::mpgSchemaId()));
    sb.append("\ncar.performanceFiguresId=").append(format(Car::performanceFiguresSchemaId()));
    sb.append("\ncar.performanceFigures.octaneRatingId=").append(format(Car::PerformanceFigures::octaneRatingSchemaId()));
    sb.append("\ncar.performanceFigures.accelerationId=").append(format(Car::PerformanceFigures::accelerationSchemaId()));
    sb.append("\ncar.performanceFigures.acceleration.mphId=").append(format(Car::PerformanceFigures::Acceleration::mphSchemaId()));
    sb.append("\ncar.performanceFigures.acceleration.secondsId=").append(format(Car::PerformanceFigures::Acceleration::secondsSchemaId()));
    sb.append("\ncar.makeId=").append(format(Car::makeSchemaId()));
    sb.append("\ncar.makeCharacterEncoding=").append(Car::makeCharacterEncoding());
    sb.append("\ncar.modelId=").append(format(Car::modelSchemaId()));
    sb.append("\ncar.modelCharacterEncoding=").append(Car::modelCharacterEncoding());

    sb.append("\n");

    sb.append("\ncar.serialNumber=").append(format((int)car.serialNumber()));
    sb.append("\ncar.modelYear=").append(format((int)car.modelYear()));
    sb.append("\ncar.available=").append(format(car.available()));
    sb.append("\ncar.code=").append(format(car.code()));

    sb.append("\ncar.someNumbers=");
    for (int i = 0, size = Car::someNumbersLength(); i < size; i++)
    {
        sb.append(format(car.someNumbers(i))).append(", ");
    }

    sb.append("\ncar.vehicleCodeLength=").append(format((int)car.vehicleCodeLength()));
    sb.append("\ncar.vehicleCode=");
    for (int i = 0, size = Car::vehicleCodeLength(); i < size; i++)
    {
        sb.append(format((char)car.vehicleCode(i)));
    }

    OptionalExtras &extras = car.extras();
    sb.append("\ncar.extras.cruiseControl=").append(format(extras.cruiseControl()));
    sb.append("\ncar.extras.sportsPack=").append(format(extras.sportsPack()));
    sb.append("\ncar.extras.sunRoof=").append(format(extras.sunRoof()));

    Engine &engine = car.engine();
    sb.append("\ncar.engine.capacity=").append(format((int)engine.capacity()));
    sb.append("\ncar.engine.numCylinders=").append(format((int)engine.numCylinders()));
    sb.append("\ncar.engine.maxRpm=").append(format((int)engine.maxRpm()));
    sb.append("\ncar.engine.manufacturerCodeLength=").append(format((int)engine.manufacturerCodeLength()));
    sb.append("\ncar.engine.manufacturerCode=");
    for (int i = 0, size = Engine::manufacturerCodeLength(); i < size; i++)
    {
        sb.append(format((char)engine.manufacturerCode(i)));
    }

    char tmp[1024];
    int bytesCopied = engine.getFuel(tmp, sizeof(tmp));
    sb.append("\ncar.engine.fuelLength=").append(format(bytesCopied));
    sb.append("\ncar.engine.fuel=").append(tmp, bytesCopied);

    Car::FuelFigures &fuelFigures = car.fuelFigures();
    while (fuelFigures.hasNext())
    {
        fuelFigures.next();
        sb.append("\ncar.fuelFigures.speed=").append(format((int)fuelFigures.speed()));
        sb.append("\ncar.fuelFigures.mpg=").append(format((double)fuelFigures.mpg()));
    }

    Car::PerformanceFigures &performanceFigures = car.performanceFigures();
    while (performanceFigures.hasNext())
    {
        performanceFigures.next();
        sb.append("\ncar.performanceFigures.octaneRating=").append(format((int)performanceFigures.octaneRating()));

        Car::PerformanceFigures::Acceleration &acceleration = performanceFigures.acceleration();
        while (acceleration.hasNext())
        {
            acceleration.next();
            sb.append("\ncar.performanceFigures.acceleration.mph=").append(format((int)acceleration.mph()));
            sb.append("\ncar.performanceFigures.acceleration.seconds=").append(format((double)acceleration.seconds()));
        }
    }

    bytesCopied = car.getMake(tmp, sizeof(tmp));
    sb.append("\ncar.makeLength=").append(format((int)bytesCopied));
    sb.append("\ncar.make=").append(tmp, bytesCopied);

    bytesCopied = car.getModel(tmp, sizeof(tmp));
    sb.append("\ncar.modelLength=").append(format((int)bytesCopied));
    sb.append("\ncar.model=").append(tmp, bytesCopied);

    cout << sb << endl;
}

int main(int argc, const char* argv[])
{
    char buffer[2048];
    MessageHeader hdr;
    Car car;

    encodeHdr(hdr, car, buffer, 0);
    encodeCar(car, buffer, hdr.size());

    cout << "Encoding size is " << hdr.size() << " + " << car.size() << endl;

    decodeHdr(hdr, buffer, 0);
    decodeCar(car, buffer, hdr.size(), hdr.blockLength(), hdr.version());
    return 0;
}
