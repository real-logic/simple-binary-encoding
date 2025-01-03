/*
 * Copyright 2013-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.real_logic.sbe.generation.java;

import extension.*;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DtoTest
{
    @Test
    void shouldRoundTripCar1()
    {
        final ExpandableArrayBuffer inputBuffer = new ExpandableArrayBuffer();
        encodeCar(inputBuffer, 0);

        final CarDecoder decoder = new CarDecoder();
        decoder.wrap(inputBuffer, 0, CarDecoder.BLOCK_LENGTH, CarDecoder.SCHEMA_VERSION);
        final String decoderString = decoder.toString();

        final CarDto dto = new CarDto();
        CarDto.decodeWith(decoder, dto);

        final ExpandableArrayBuffer outputBuffer = new ExpandableArrayBuffer();
        final CarEncoder encoder = new CarEncoder();
        encoder.wrap(outputBuffer, 0);
        CarDto.encodeWith(encoder, dto);

        final String dtoString = dto.toString();

        assertThat(outputBuffer.byteArray(), equalTo(inputBuffer.byteArray()));
        assertThat(dtoString, equalTo(decoderString));
    }

    @Test
    void shouldRoundTripCar2()
    {
        final ExpandableArrayBuffer inputBuffer = new ExpandableArrayBuffer();
        final int inputLength = encodeCar(inputBuffer, 0);

        final CarDto dto = CarDto.decodeFrom(inputBuffer, 0, CarDecoder.BLOCK_LENGTH, CarDecoder.SCHEMA_VERSION);

        final ExpandableArrayBuffer outputBuffer = new ExpandableArrayBuffer();
        final int outputLength = CarDto.encodeWith(dto, outputBuffer, 0);

        assertThat(outputLength, equalTo(inputLength));
        assertThat(outputBuffer.byteArray(), equalTo(inputBuffer.byteArray()));
    }

    private static int encodeCar(final MutableDirectBuffer buffer, final int offset)
    {
        final CarEncoder car = new CarEncoder();
        car.wrap(buffer, offset);
        car.serialNumber(1234);
        car.modelYear(2013);
        car.available(BooleanType.T);
        car.code(Model.A);
        car.vehicleCode("ABCDEF");

        for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i, i);
        }

        car.extras().cruiseControl(true).sportsPack(true);

        car.cupHolderCount((short)119);

        car.engine().capacity(2000)
            .numCylinders((short)4)
            .manufacturerCode("ABC")
            .efficiency((byte)35)
            .boosterEnabled(BooleanType.T)
            .booster().boostType(BoostType.NITROUS)
            .horsePower((short)200);

        final CarEncoder.FuelFiguresEncoder fuelFigures = car.fuelFiguresCount(3);
        fuelFigures.next()
            .speed(30)
            .mpg(35.9f)
            .usageDescription("this is a description");

        fuelFigures.next()
            .speed(55)
            .mpg(49.0f)
            .usageDescription("this is a description");

        fuelFigures.next()
            .speed(75)
            .mpg(40.0f)
            .usageDescription("this is a description");

        final CarEncoder.PerformanceFiguresEncoder perfFigures = car.performanceFiguresCount(2);

        perfFigures.next()
            .octaneRating((short)95);

        CarEncoder.PerformanceFiguresEncoder.AccelerationEncoder acceleration = perfFigures.accelerationCount(3);

        acceleration.next()
            .mph(30)
            .seconds(4.0f);

        acceleration.next()
            .mph(60)
            .seconds(7.5f);

        acceleration.next()
            .mph(100)
            .seconds(12.2f);

        perfFigures.next()
            .octaneRating((short)99);

        acceleration = perfFigures.accelerationCount(3);

        acceleration.next()
            .mph(30)
            .seconds(3.8f);

        acceleration.next()
            .mph(60)
            .seconds(7.1f);

        acceleration.next()
            .mph(100)
            .seconds(11.8f);

        car.manufacturer("Ford");
        car.model("Fiesta");
        car.activationCode("1234");

        return car.limit();
    }
}
