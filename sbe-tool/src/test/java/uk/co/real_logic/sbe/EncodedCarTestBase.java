package uk.co.real_logic.sbe;

import baseline.*;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.BeforeClass;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class EncodedCarTestBase
{
    protected static final MessageHeaderEncoder MESSAGE_HEADER = new MessageHeaderEncoder();
    protected static final CarEncoder CAR = new CarEncoder();

    private static byte[] vehicleCode;
    private static byte[] manufacturerCode;
    private static byte[] make;
    private static byte[] model;

    @BeforeClass
    public static void setupExampleData()
    {
        try
        {
            vehicleCode = "abcdef".getBytes(CarEncoder.vehicleCodeCharacterEncoding());
            manufacturerCode = "123".getBytes(EngineEncoder.manufacturerCodeCharacterEncoding());
            make = "Honda".getBytes(CarEncoder.makeCharacterEncoding());
            model = "Civic VTi".getBytes(CarEncoder.modelCharacterEncoding());
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    protected static void encodeTestMessage(final ByteBuffer buffer)
    {
        final UnsafeBuffer directBuffer = new UnsafeBuffer(buffer);

        int bufferOffset = 0;
        MESSAGE_HEADER
            .wrap(directBuffer, bufferOffset)
            .blockLength(CAR.sbeBlockLength())
            .templateId(CAR.sbeTemplateId())
            .schemaId(CAR.sbeSchemaId())
            .version(CAR.sbeSchemaVersion());

        bufferOffset += MESSAGE_HEADER.encodedLength();

        final int srcOffset = 0;

        CAR.wrap(directBuffer, bufferOffset)
            .serialNumber(1234)
            .modelYear(2013)
            .available(BooleanType.T)
            .code(Model.A)
            .putVehicleCode(vehicleCode, srcOffset);

        for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
        {
            CAR.someNumbers(i, i);
        }

        CAR.extras()
            .clear()
            .cruiseControl(true)
            .sportsPack(true)
            .sunRoof(false);

        CAR.engine()
            .capacity(2000)
            .numCylinders((short)4)
            .putManufacturerCode(manufacturerCode, srcOffset);

        CAR.fuelFiguresCount(3)
            .next().speed(30).mpg(35.9f)
            .next().speed(55).mpg(49.0f)
            .next().speed(75).mpg(40.0f);

        final CarEncoder.PerformanceFiguresEncoder perfFigures = CAR.performanceFiguresCount(2);
        perfFigures.next()
            .octaneRating((short)95)
            .accelerationCount(3)
            .next().mph(30).seconds(4.0f)
            .next().mph(60).seconds(7.5f)
            .next().mph(100).seconds(12.2f);
        perfFigures.next()
            .octaneRating((short)99)
            .accelerationCount(3)
            .next().mph(30).seconds(3.8f)
            .next().mph(60).seconds(7.1f)
            .next().mph(100).seconds(11.8f);

        CAR.make(new String(make));
        CAR.putModel(model, srcOffset, model.length);

        bufferOffset += CAR.encodedLength();

        buffer.position(bufferOffset);
    }
}
