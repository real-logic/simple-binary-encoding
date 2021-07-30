package uk.co.real_logic.sbe.generation.java;

import baseline.CarDecoder;
import baseline.EngineDecoder;
import baseline.MessageHeaderDecoder;
import baseline.OptionalExtrasDecoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.EncodedCarTestBase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RewindTest extends EncodedCarTestBase
{
    private static final int MSG_BUFFER_CAPACITY = 4 * 1024;

    @Test
    void shouldRewindAfterReadingFullMessage()
    {
        final ByteBuffer encodedMsgBuffer = ByteBuffer.allocate(MSG_BUFFER_CAPACITY);
        encodeTestMessage(encodedMsgBuffer);

        MessageHeaderDecoder header = new MessageHeaderDecoder();
        CarDecoder carDecoder = new CarDecoder();
        carDecoder.wrapAndApplyHeader(new UnsafeBuffer(encodedMsgBuffer), 0, header);

        final ArrayList<Object> passOne = getValues(carDecoder);
        carDecoder.sbeRewind();
        final ArrayList<Object> passTwo = getValues(carDecoder);
        assertEquals(passOne, passTwo);

        carDecoder.sbeRewind();
        final ArrayList<Object> partialPassOne = getPartialValues(carDecoder);
        carDecoder.sbeRewind();
        final ArrayList<Object> partialPassTwo = getPartialValues(carDecoder);
        assertNotEquals(passOne, partialPassOne);
        assertEquals(partialPassOne, partialPassTwo);

        carDecoder.sbeRewind();
        final ArrayList<Object> passThree = getValues(carDecoder);
        assertEquals(passOne, passThree);
    }

    private ArrayList<Object> getValues(final CarDecoder carDecoder)
    {
        final ArrayList<Object> values = new ArrayList<>();

        values.add(carDecoder.serialNumber());
        values.add(carDecoder.modelYear());
        values.add(carDecoder.available());
        values.add(carDecoder.code());
        values.add(CarDecoder.someNumbersLength());
        for (int i = 0, n = CarDecoder.someNumbersLength(); i < n; i++)
        {
            values.add(carDecoder.someNumbers(i));
        }
        values.add(carDecoder.vehicleCode());
        final OptionalExtrasDecoder extras = carDecoder.extras();
        values.add(extras.sunRoof());
        values.add(extras.sportsPack());
        values.add(extras.cruiseControl());
        final EngineDecoder engine = carDecoder.engine();
        values.add(engine.capacity());
        values.add(engine.numCylinders());
        values.add(engine.maxRpm());
        values.add(engine.manufacturerCode());
        values.add(engine.fuel());
        final CarDecoder.FuelFiguresDecoder fuelFigures = carDecoder.fuelFigures();
        while (fuelFigures.hasNext())
        {
            fuelFigures.next();
            values.add(fuelFigures.speed());
            values.add(fuelFigures.mpg());
        }
        final CarDecoder.PerformanceFiguresDecoder performanceFigures = carDecoder.performanceFigures();
        while (performanceFigures.hasNext())
        {
            performanceFigures.next();
            values.add(performanceFigures.octaneRating());
            final CarDecoder.PerformanceFiguresDecoder.AccelerationDecoder acceleration =
                performanceFigures.acceleration();
            while (acceleration.hasNext())
            {
                acceleration.next();
                values.add(acceleration.mph());
                values.add(acceleration.seconds());
            }
        }
        values.add(carDecoder.manufacturer());
        values.add(carDecoder.model());
        return values;
    }

    private ArrayList<Object> getPartialValues(final CarDecoder carDecoder)
    {
        final ArrayList<Object> values = new ArrayList<>();

        values.add(carDecoder.serialNumber());
        values.add(carDecoder.modelYear());
        values.add(carDecoder.available());
        values.add(carDecoder.code());
        values.add(CarDecoder.someNumbersLength());
        for (int i = 0, n = CarDecoder.someNumbersLength(); i < n; i++)
        {
            values.add(carDecoder.someNumbers(i));
        }
        values.add(carDecoder.vehicleCode());
        final OptionalExtrasDecoder extras = carDecoder.extras();
        values.add(extras.sunRoof());
        values.add(extras.sportsPack());
        values.add(extras.cruiseControl());
        final EngineDecoder engine = carDecoder.engine();
        values.add(engine.capacity());
        values.add(engine.numCylinders());
        values.add(engine.maxRpm());
        values.add(engine.manufacturerCode());
        values.add(engine.fuel());
        final CarDecoder.FuelFiguresDecoder fuelFigures = carDecoder.fuelFigures();
        while (fuelFigures.hasNext())
        {
            fuelFigures.next();
            values.add(fuelFigures.speed());
            values.add(fuelFigures.mpg());
        }
        final CarDecoder.PerformanceFiguresDecoder performanceFigures = carDecoder.performanceFigures();

        // Stop decoding part way through the message.
        if (performanceFigures.hasNext())
        {
            performanceFigures.next();
            values.add(performanceFigures.octaneRating());
            final CarDecoder.PerformanceFiguresDecoder.AccelerationDecoder acceleration =
                performanceFigures.acceleration();
            if (acceleration.hasNext())
            {
                acceleration.next();
                values.add(acceleration.mph());
            }
        }

        return values;
    }
}
