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

package uk.co.real_logic.protobuf;

import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import uk.co.real_logic.protobuf.examples.Examples;
import uk.co.real_logic.protobuf.examples.Examples.Car.Model;
import uk.co.real_logic.protobuf.examples.Examples.PerformanceFigures;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CarBenchmark
{
    private static final String VEHICLE_CODE = "abcdef";
    private static final String ENG_MAN_CODE = "abc";
    private static final String MAKE = "AUDI";
    private static final String MODEL = "R8";

    @State(Scope.Benchmark)
    public static class MyState
    {
        final Examples.Car.Builder car = Examples.Car.newBuilder();
        final byte[] decodeBuffer;

        final ByteArrayInputStream in;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        {
            try
            {
                encode(car, out);
            }
            catch (final Exception ex)
            {
                throw new RuntimeException(ex);
            }

            decodeBuffer = out.toByteArray();
            in = new ByteArrayInputStream(decodeBuffer);
        }
    }

    @GenerateMicroBenchmark
    public int testEncode(final MyState state) throws Exception
    {
        final Examples.Car.Builder car = state.car;
        final ByteArrayOutputStream out = state.out;

        return encode(car,  out);
    }

    @GenerateMicroBenchmark
    public int testDecode(final MyState state) throws Exception
    {
        final Examples.Car.Builder car = state.car;
        final ByteArrayInputStream in = state.in;

        return decode(car, in);
    }

    private static int encode(final Examples.Car.Builder car, final ByteArrayOutputStream out) throws Exception
    {
        out.reset();

        car.clear()
           .setCode(Model.A)
           .setModelYear(2005)
           .setSerialNumber(12345)
           .setAvailable(true)
           .setVehicleCode(VEHICLE_CODE);

        for (int i = 0, size = 5; i < size; i++)
        {
            car.addSomeNumbers(i);
        }

        car.addOptionalExtras(Examples.Car.Extras.SPORTS_PACK)
           .addOptionalExtras(Examples.Car.Extras.SUN_ROOF);

        car.getEngineBuilder().setCapacity(4200)
           .setNumCylinders(8)
           .setManufacturerCode(ENG_MAN_CODE);

        car.addFuelFiguresBuilder().setSpeed(30).setMpg(35.9F);
        car.addFuelFiguresBuilder().setSpeed(30).setMpg(49.0F);
        car.addFuelFiguresBuilder().setSpeed(30).setMpg(40.0F);

        final PerformanceFigures.Builder perf1 = car.addPerformanceBuilder().setOctaneRating(95);
        perf1.addAccelerationBuilder().setMph(30).setSeconds(4.0f);
        perf1.addAccelerationBuilder().setMph(60).setSeconds(7.5f);
        perf1.addAccelerationBuilder().setMph(100).setSeconds(12.2f);

        final PerformanceFigures.Builder perf2 = car.addPerformanceBuilder().setOctaneRating(95);
        perf2.addAccelerationBuilder().setMph(30).setSeconds(3.8f);
        perf2.addAccelerationBuilder().setMph(60).setSeconds(7.1f);
        perf2.addAccelerationBuilder().setMph(100).setSeconds(11.8f);

        car.setMake(MAKE);
        car.setModel(MODEL);

        car.build().writeTo(out);

        return out.size();
    }

    private static int decode(final Examples.Car.Builder car,
                              final ByteArrayInputStream in) throws Exception
    {
        in.mark(in.available());

        car.clear();
        car.mergeFrom(in);

        car.getSerialNumber();
        car.getModelYear();
        car.hasAvailable();
        car.getCode();

        for (int i = 0, size = car.getSomeNumbersCount(); i < size; i++)
        {
            car.getSomeNumbers(i);
        }

        car.getVehicleCode();

        for (int i = 0, size = car.getOptionalExtrasCount(); i < size; i++)
        {
            car.getOptionalExtras(i);
        }

        final Examples.Engine engine = car.getEngine();
        engine.getCapacity();
        engine.getNumCylinders();
        engine.getMaxRpm();
        engine.getManufacturerCode();
        engine.getFuel();

        for (final Examples.FuelFigures fuelFigures : car.getFuelFiguresList())
        {
            fuelFigures.getSpeed();
            fuelFigures.getMpg();
        }

        for (final PerformanceFigures performanceFigures : car.getPerformanceList())
        {
            performanceFigures.getOctaneRating();

            for (final Examples.Acceleration acceleration : performanceFigures.getAccelerationList())
            {
                acceleration.getMph();
                acceleration.getSeconds();
            }
        }

        car.getMake();
        car.getModel();

        in.reset();

        return in.available();
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args) throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            perfTestEncode(i);
            perfTestDecode(i);
        }
    }

    private static void perfTestEncode(final int runNumber) throws Exception
    {
        final int reps = 1 * 1000 * 1000;
        final MyState state = new MyState();
        final CarBenchmark benchmark = new CarBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf("%d - %d(ns) average duration for %s.testEncode() - message size %d\n",
                          Integer.valueOf(runNumber),
                          Long.valueOf(totalDuration / reps),
                          benchmark.getClass().getName(),
                          Integer.valueOf(state.car.getSomeNumbersCount()));
    }

    private static void perfTestDecode(final int runNumber) throws Exception
    {
        final int reps = 1 * 1000 * 1000;
        final MyState state = new MyState();
        final CarBenchmark benchmark = new CarBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf("%d - %d(ns) average duration for %s.testDecode() - message size %d\n",
                          Integer.valueOf(runNumber),
                          Long.valueOf(totalDuration / reps),
                          benchmark.getClass().getName(),
                          Integer.valueOf(state.car.getSomeNumbersCount()));
    }
}
