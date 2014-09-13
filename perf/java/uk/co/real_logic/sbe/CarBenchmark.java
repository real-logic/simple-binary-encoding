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

package uk.co.real_logic.sbe;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import uk.co.real_logic.sbe.examples.*;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class CarBenchmark
{
    private static final byte[] MAKE;
    private static final byte[] MODEL;
    private static final byte[] ENG_MAN_CODE;
    private static final byte[] VEHICLE_CODE;

    static
    {
        try
        {
            MAKE = "MAKE".getBytes(Car.makeCharacterEncoding());
            MODEL = "MODEL".getBytes(Car.modelCharacterEncoding());
            ENG_MAN_CODE = "abc".getBytes(Engine.manufacturerCodeCharacterEncoding());
            VEHICLE_CODE = "abcdef".getBytes(Car.vehicleCodeCharacterEncoding());
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @State(Scope.Benchmark)
    public static class MyState
    {
        final int bufferIndex = 0;
        final Car car = new Car();
        final MessageHeader messageHeader = new MessageHeader();
        final DirectBuffer encodeBuffer = new DirectBuffer(ByteBuffer.allocateDirect(1024));

        final byte[] tempBuffer = new byte[128];
        final DirectBuffer decodeBuffer = new DirectBuffer(ByteBuffer.allocateDirect(1024));

        {
            CarBenchmark.encode(messageHeader, car, decodeBuffer, bufferIndex);
        }
    }

    @Benchmark
    public int testEncode(final MyState state)
    {
        final Car car = state.car;
        final MessageHeader messageHeader = state.messageHeader;
        final DirectBuffer buffer = state.encodeBuffer;
        final int bufferIndex = state.bufferIndex;

        encode(messageHeader, car, buffer, bufferIndex);

        return car.size();
    }

    @Benchmark
    public int testDecode(final MyState state)
    {
        final Car car = state.car;
        final MessageHeader messageHeader = state.messageHeader;
        final DirectBuffer buffer = state.decodeBuffer;
        final int bufferIndex = state.bufferIndex;
        final byte[] tempBuffer = state.tempBuffer;

        decode(messageHeader, car, buffer, bufferIndex, tempBuffer);

        return car.size();
    }

    public static void encode(final MessageHeader messageHeader, final Car car, final DirectBuffer buffer, final int bufferIndex)
    {
        messageHeader.wrap(buffer, bufferIndex, 0)
                     .blockLength(car.sbeBlockLength())
                     .templateId(car.sbeTemplateId())
                     .schemaId(car.sbeSchemaId())
                     .version(car.sbeSchemaVersion());

        car.wrapForEncode(buffer, bufferIndex + messageHeader.size())
           .code(Model.A)
           .modelYear(2005)
           .serialNumber(12345)
           .available(BooleanType.TRUE)
           .putVehicleCode(VEHICLE_CODE, 0);

        for (int i = 0, size = Car.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i, i);
        }

        car.extras().clear()
           .sportsPack(true)
           .sunRoof(true);

        car.engine().capacity(4200)
           .numCylinders((short)8)
           .putManufacturerCode(ENG_MAN_CODE, 0);

        car.fuelFiguresCount(3).next().speed(30).mpg(35.9f)
           .next().speed(55).mpg(49.0f)
           .next().speed(75).mpg(40.0f);

        final Car.PerformanceFigures perfFigures = car.performanceFiguresCount(2);
        perfFigures.next().octaneRating((short)95)
                   .accelerationCount(3).next().mph(30).seconds(4.0f)
                   .next().mph(60).seconds(7.5f)
                   .next().mph(100).seconds(12.2f);
        perfFigures.next().octaneRating((short)99)
                   .accelerationCount(3).next().mph(30).seconds(3.8f)
                   .next().mph(60).seconds(7.1f)
                   .next().mph(100).seconds(11.8f);

        car.putMake(MAKE, 0, MAKE.length);
        car.putModel(MODEL, 0, MODEL.length);
    }


    private static void decode(
        final MessageHeader messageHeader,
        final Car car,
        final DirectBuffer buffer,
        final int bufferIndex,
        final byte[] tempBuffer)
    {
        messageHeader.wrap(buffer, bufferIndex, 0);

        final int actingVersion = messageHeader.version();
        final int actingBlockLength = messageHeader.blockLength();

        car.wrapForDecode(buffer, bufferIndex + messageHeader.size(), actingBlockLength, actingVersion);

        car.serialNumber();
        car.modelYear();
        car.available();
        car.code();

        for (int i = 0, size = Car.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i);
        }

        for (int i = 0, size = Car.vehicleCodeLength(); i < size; i++)
        {
            car.vehicleCode(i);
        }

        final OptionalExtras extras = car.extras();
        extras.cruiseControl();
        extras.sportsPack();
        extras.sunRoof();

        final Engine engine = car.engine();
        engine.capacity();
        engine.numCylinders();
        engine.maxRpm();
        for (int i = 0, size = Engine.manufacturerCodeLength(); i < size; i++)
        {
            engine.manufacturerCode(i);
        }

        engine.getFuel(tempBuffer, 0, tempBuffer.length);

        for (final Car.FuelFigures fuelFigures : car.fuelFigures())
        {
            fuelFigures.speed();
            fuelFigures.mpg();
        }

        for (final Car.PerformanceFigures performanceFigures : car.performanceFigures())
        {
            performanceFigures.octaneRating();

            for (final Car.PerformanceFigures.Acceleration acceleration : performanceFigures.acceleration())
            {
                acceleration.mph();
                acceleration.seconds();
            }
        }

        car.getMake(tempBuffer, 0, tempBuffer.length);
        car.getModel(tempBuffer, 0, tempBuffer.length);
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args)
    {
        for (int i = 0; i < 10; i++)
        {
            perfTestEncode(i);
            perfTestDecode(i);
        }
    }

    private static void perfTestEncode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final CarBenchmark benchmark = new CarBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testEncode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(state.car.size() + state.messageHeader.size()));
    }

    private static void perfTestDecode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final CarBenchmark benchmark = new CarBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testDecode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(state.car.size() + state.messageHeader.size()));
    }
}
