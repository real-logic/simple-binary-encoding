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
import java.nio.charset.Charset;

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

    public static void main(final String[] args)
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final DirectBuffer directBuffer = new DirectBuffer(byteBuffer);

        final Car car = new Car();

        encode(car, directBuffer);
        decode(car, directBuffer);
    }

    private static void encode(final Car car, final DirectBuffer directBuffer)
    {
        car.reset(directBuffer, 0);

        car.serialNumber(1234);
        car.modelYear(2013);
        car.available(BooleanType.TRUE);
        car.code(Model.A);
        car.putVehicleCode(VEHICLE_CODE, 0, VEHICLE_CODE.length);
        car.extras().cruiseControl(true).sportsPack(true).sunRoof(false);

        final Engine engine = car.engine();
        engine.capacity(2000);
        engine.numCylinders((short)4);
        engine.putManufacturerCode(MANUFACTURER_CODE, 0, MANUFACTURER_CODE.length);

        final Car.FuelFigures fuelFigures = car.fuelFiguresSize(3);

        fuelFigures.next();
        fuelFigures.speed(30);
        fuelFigures.mpg(35.9f);

        fuelFigures.next();
        fuelFigures.speed(55);
        fuelFigures.mpg(49.0f);

        fuelFigures.next();
        fuelFigures.speed(75);
        fuelFigures.mpg(40.0f);

        final Car.PerformanceFigures performanceFigures = car.performanceFiguresSize(2);

        performanceFigures.next();
        performanceFigures.octaneRating((short)95);

        Car.PerformanceFigures.Acceleration acceleration = performanceFigures.accelerationSize(2);

        acceleration.next();
        acceleration.mph(60);
        acceleration.seconds(7.5f);

        acceleration.next();
        acceleration.mph(100);
        acceleration.seconds(12.2f);

        performanceFigures.next();
        performanceFigures.octaneRating((short)99);

        acceleration = performanceFigures.accelerationSize(2);

        acceleration.next();
        acceleration.mph(60);
        acceleration.seconds(7.1f);

        acceleration.next();
        acceleration.mph(100);
        acceleration.seconds(11.8f);

        car.putMake(MAKE, 0, MAKE.length);
        car.putModel(MODEL, 0, MODEL.length);
    }

    private static void decode(final Car car, final DirectBuffer directBuffer)
    {
        car.reset(directBuffer, 0);

        final StringBuilder sb = new StringBuilder();

        sb.append("\ncar.serialNumber=").append(car.serialNumber());
        sb.append("\ncar.modelYear=").append(car.modelYear());
        sb.append("\ncar.available=").append(car.available());
        sb.append("\ncar.code=").append(car.code());
        sb.append("\ncar.vehicleCode=");
        for (int i = 0, size = car.vehicleCodeLength(); i < size; i++)
        {
            sb.append((char)car.vehicleCode(i));
        }

        final Engine engine = car.engine();
        sb.append("\ncar.engine.capacity=").append(engine.capacity());
        sb.append("\ncar.engine.numCylinders=").append(engine.numCylinders());
        sb.append("\ncar.vehicleCode=");
        for (int i = 0, size = engine.manufacturerCodeLength(); i < size; i++)
        {
            sb.append((char)engine.manufacturerCode(i));
        }

        final Car.FuelFigures fuelFigures = car.fuelFigures();
        while (fuelFigures.next())
        {
            sb.append("\ncar.fuelFigures.speed=").append(fuelFigures.speed());
            sb.append("\ncar.fuelFigures.mpg=").append(fuelFigures.mpg());
        }

        final Car.PerformanceFigures performanceFigures = car.performanceFigures();
        while (performanceFigures.next())
        {
            sb.append("\ncar.performanceFigures.octaneRating=").append(performanceFigures.octaneRating());

            final Car.PerformanceFigures.Acceleration acceleration = performanceFigures.acceleration();
            while (acceleration.next())
            {
                sb.append("\ncar.performanceFigures.acceleration.mph=").append(acceleration.mph());
                sb.append("\ncar.performanceFigures.acceleration.seconds=").append(acceleration.seconds());
            }
        }

        final byte[] buffer = new byte[128];

        int bytesCopied = car.getMake(buffer, 0, buffer.length);
        sb.append("\ncar.make=").append(new String(buffer, 0, bytesCopied, Charset.forName(car.makeEncoding())));

        bytesCopied = car.getModel(buffer, 0, buffer.length);
        sb.append("\ncar.model=").append(new String(buffer, 0, bytesCopied, Charset.forName(car.modelEncoding())));

        System.out.println(sb);
    }
}
