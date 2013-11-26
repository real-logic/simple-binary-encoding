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
    hdr.reset(buffer, offset, messageHeaderVersion)
       .blockLength(car.blockLength())
       .templateId(car.templateId())
       .version(car.templateVersion())
       .reserved(0);
}

void decodeHdr(MessageHeader &hdr, char *buffer, int offset)
{
    hdr.reset(buffer, offset, messageHeaderVersion);

    // decode the header
    cout << "messageHeader.blockLength=" << hdr.blockLength() << endl;
    cout << "messageHeader.templateId=" << hdr.templateId() << endl;
    cout << "messageHeader.version=" << (sbe_uint32_t)hdr.version() << endl;
    cout << "messageHeader.reserved=" << (sbe_uint32_t)hdr.reserved() << endl;
}

void encodeCar(Car &car, char *buffer, int offset)
{
    car.resetForEncode(buffer, offset)
       .serialNumber(1234)
       .modelYear(2013)
       .available(BooleanType::TRUE)
       .code(Model::A)
       .putVehicleCode(VEHICLE_CODE);

    for (int i = 0, size = car.someNumbersLength(); i < size; i++)
    {
        car.someNumbers(i, i);
    }

    car.extras()
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
    car.resetForDecode(buffer, offset, actingBlockLength, actingVersion);
    std::string sb;

    sb.append("\ncar.serialNumberId=").append(format(car.serialNumberSchemaId()));
    sb.append("\ncar.modelYearId=").append(format(car.modelYearSchemaId()));
    sb.append("\ncar.availableId=").append(format(car.availableSchemaId()));
    sb.append("\ncar.codeId=").append(format(car.codeSchemaId()));
    sb.append("\ncar.someNumbersId=").append(format(car.someNumbersSchemaId()));
    sb.append("\ncar.vehicleCodeId=").append(format(car.vehicleCodeSchemaId()));

    sb.append("\n");

    sb.append("\ncar.serialNumber=").append(format((int)car.serialNumber()));
    sb.append("\ncar.modelYear=").append(format((int)car.modelYear()));
    sb.append("\ncar.available=").append(format(car.available()));
    sb.append("\ncar.code=").append(format(car.code()));

    sb.append("\ncar.someNumbers=");
    for (int i = 0, size = car.someNumbersLength(); i < size; i++)
    {
        sb.append(format(car.someNumbers(i))).append(", ");
    }

    sb.append("\ncar.vehicleCodeLength=").append(format((int)car.vehicleCodeLength()));
    sb.append("\ncar.vehicleCode=");
    for (int i = 0, size = car.vehicleCodeLength(); i < size; i++)
    {
        sb.append(format((char)car.vehicleCode(i)));
    }

    OptionalExtras &extras = car.extras();
    sb.append("\ncar.extrasId=").append(format(car.extrasSchemaId()));
    sb.append("\ncar.extras.cruiseControl=").append(format(extras.cruiseControl()));
    sb.append("\ncar.extras.sportsPack=").append(format(extras.sportsPack()));
    sb.append("\ncar.extras.sunRoof=").append(format(extras.sunRoof()));

    Engine &engine = car.engine();
    sb.append("\ncar.engineId=").append(format(car.engineSchemaId()));
    sb.append("\ncar.engine.capacity=").append(format((int)engine.capacity()));
    sb.append("\ncar.engine.numCylinders=").append(format((int)engine.numCylinders()));
    sb.append("\ncar.engine.maxRpm=").append(format((int)engine.maxRpm()));
    sb.append("\ncar.engine.manufacturerCodeLength=").append(format((int)engine.manufacturerCodeLength()));
    sb.append("\ncar.engine.manufacturerCode=");
    for (int i = 0, size = engine.manufacturerCodeLength(); i < size; i++)
    {
        sb.append(format((char)engine.manufacturerCode(i)));
    }

    char tmp[1024];
    int bytesCopied = engine.getFuel(tmp, sizeof(tmp));
    sb.append("\ncar.engine.fuelLength=").append(format(bytesCopied));
    sb.append("\ncar.engine.fuel=").append(tmp, bytesCopied);

    Car::FuelFigures &fuelFigures = car.fuelFigures();
    sb.append("\ncar.fuelFiguresId=").append(format(car.fuelFiguresSchemaId()));
    sb.append("\ncar.fuelFigures.speedId=").append(format(fuelFigures.speedSchemaId()));
    sb.append("\ncar.fuelFigures.mpgId=").append(format(fuelFigures.mpgSchemaId()));
    while (fuelFigures.hasNext())
    {
        fuelFigures.next();
        sb.append("\ncar.fuelFigures.speed=").append(format((int)fuelFigures.speed()));
        sb.append("\ncar.fuelFigures.mpg=").append(format((double)fuelFigures.mpg()));
    }

    Car::PerformanceFigures &performanceFigures = car.performanceFigures();
    sb.append("\ncar.performanceFiguresId=").append(format(car.performanceFiguresSchemaId()));
    sb.append("\ncar.performanceFigures.octaneRatingId=").append(format(performanceFigures.octaneRatingSchemaId()));
    sb.append("\ncar.performanceFigures.accelerationId=").append(format(performanceFigures.accelerationSchemaId()));
    while (performanceFigures.hasNext())
    {
        performanceFigures.next();
        sb.append("\ncar.performanceFigures.octaneRating=").append(format((int)performanceFigures.octaneRating()));

        Car::PerformanceFigures::Acceleration &acceleration = performanceFigures.acceleration();
        sb.append("\ncar.performanceFigures.acceleration.mphId=").append(format(acceleration.mphSchemaId()));
        sb.append("\ncar.performanceFigures.acceleration.secondsId=").append(format(acceleration.secondsSchemaId()));
        while (acceleration.hasNext())
        {
            acceleration.next();
            sb.append("\ncar.performanceFigures.acceleration.mph=").append(format((int)acceleration.mph()));
            sb.append("\ncar.performanceFigures.acceleration.seconds=").append(format((double)acceleration.seconds()));
        }
    }

    bytesCopied = car.getMake(tmp, sizeof(tmp));
    sb.append("\ncar.makeId=").append(format(car.makeSchemaId()));
    sb.append("\ncar.makeLength=").append(format((int)bytesCopied));
    sb.append("\ncar.make=").append(tmp, bytesCopied);
    sb.append("\ncar.makeCharacterEncoding=").append(car.makeCharacterEncoding());

    bytesCopied = car.getModel(tmp, sizeof(tmp));
    sb.append("\ncar.modelId=").append(format(car.modelSchemaId()));
    sb.append("\ncar.modelLength=").append(format((int)bytesCopied));
    sb.append("\ncar.model=").append(tmp, bytesCopied);
    sb.append("\ncar.modelCharacterEncoding=").append(car.modelCharacterEncoding());

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