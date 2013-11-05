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
package uk.co.real_logic.sbe.examples;

import uk.co.real_logic.sbe.generation.java.DirectBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class SbeExample
{
    private static final byte[] VEHICLE_CODE = {'a', 'b', 'c', 'd', 'e', 'f'};
    private static final byte[] MANUFACTURER_CODE = {'1', '2', '3'};
    private static final byte[] MAKE;
    private static final byte[] MODEL;

    static
    {
        try
        {
            MAKE = "Honda".getBytes("UTF-8");
            MODEL = "Civic VTi".getBytes("UTF-8");
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static void main(final String[] args)throws Exception
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final DirectBuffer directBuffer = new DirectBuffer(byteBuffer);

        final Car car = new Car();

        encode(car, directBuffer);
        decode(car, directBuffer);
    }

    private static void encode(final Car car, final DirectBuffer directBuffer)
    {
        final int bufferOffset = 0;

        car.reset(directBuffer, bufferOffset)
           .serialNumber(1234)
           .modelYear(2013)
           .available(BooleanType.TRUE)
           .code(Model.A);

        for (int i = 0, size = car.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i, i);
        }

        car.putVehicleCode(VEHICLE_CODE, 0, VEHICLE_CODE.length);

        car.extras()
           .cruiseControl(true)
           .sportsPack(true)
           .sunRoof(false);

        car.engine()
           .capacity(2000)
           .numCylinders((short)4)
           .putManufacturerCode(MANUFACTURER_CODE, 0, MANUFACTURER_CODE.length);

        car.fuelFiguresCount(3)
           .next().speed(30).mpg(35.9f)
           .next().speed(55).mpg(49.0f)
           .next().speed(75).mpg(40.0f);

        final Car.PerformanceFigures performanceFigures = car.performanceFiguresCount(2);
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

        final int offset = 0;
        car.putMake(MAKE, offset, MAKE.length);
        car.putModel(MODEL, offset, MODEL.length);
    }

    private static void decode(final Car car, final DirectBuffer directBuffer)
        throws Exception
    {
        car.reset(directBuffer, 0);

        final byte[] buffer = new byte[128];
        final StringBuilder sb = new StringBuilder();

        sb.append("\ncar.templateId=").append(car.templateId());
        sb.append("\ncar.serialNumber=").append(car.serialNumber());
        sb.append("\ncar.modelYear=").append(car.modelYear());
        sb.append("\ncar.available=").append(car.available());
        sb.append("\ncar.code=").append(car.code());

        sb.append("\ncar.someNumbers=");
        for (int i = 0, size = car.someNumbersLength(); i < size; i++)
        {
            sb.append(car.someNumbers(i)).append(", ");
        }

        sb.append("\ncar.vehicleCode=");
        for (int i = 0, size = car.vehicleCodeLength(); i < size; i++)
        {
            sb.append((char)car.vehicleCode(i));
        }

        final OptionalExtras extras = car.extras();
        sb.append("\ncar.extras.cruiseControl=").append(extras.cruiseControl());
        sb.append("\ncar.extras.sportsPack=").append(extras.sportsPack());
        sb.append("\ncar.extras.sunRoof=").append(extras.sunRoof());

        final Engine engine = car.engine();
        sb.append("\ncar.engine.capacity=").append(engine.capacity());
        sb.append("\ncar.engine.numCylinders=").append(engine.numCylinders());
        sb.append("\ncar.engine.maxRpm=").append(engine.maxRpm());
        sb.append("\ncar.engine.manufacturerCode=");
        for (int i = 0, size = engine.manufacturerCodeLength(); i < size; i++)
        {
            sb.append((char)engine.manufacturerCode(i));
        }

        sb.append("\ncar.engine.fuel=").append(new String(buffer, 0, engine.getFuel(buffer, 0, buffer.length), "ASCII"));

        for (final Car.FuelFigures fuelFigures : car.fuelFigures())
        {
            sb.append("\ncar.fuelFigures.speed=").append(fuelFigures.speed());
            sb.append("\ncar.fuelFigures.mpg=").append(fuelFigures.mpg());
        }

        for (final Car.PerformanceFigures performanceFigures : car.performanceFigures())
        {
            sb.append("\ncar.performanceFigures.octaneRating=").append(performanceFigures.octaneRating());

            for (final Car.PerformanceFigures.Acceleration acceleration : performanceFigures.acceleration())
            {
                sb.append("\ncar.performanceFigures.acceleration.mph=").append(acceleration.mph());
                sb.append("\ncar.performanceFigures.acceleration.seconds=").append(acceleration.seconds());
            }
        }

        sb.append("\ncar.make=").append(new String(buffer, 0, car.getMake(buffer, 0, buffer.length), car.makeCharacterEncoding()));
        sb.append("\ncar.model=").append(new String(buffer, 0, car.getModel(buffer, 0, buffer.length), car.modelCharacterEncoding()));

        sb.append("\ncar.size=").append(car.size());

        System.out.println(sb);
    }
}
