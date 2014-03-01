using System;
using System.Text;
using Adaptive.SimpleBinaryEncoding.Examples.Generated;

namespace Adaptive.SimpleBinaryEncoding.Examples.generated_stub
{
    public static class ExampleUsingGeneratedStub
    {
        private static readonly byte[] VehicleCode;
        private static readonly byte[] ManufacturerCode;
        private static readonly byte[] Make;
        private static readonly byte[] Model;

        private static readonly MessageHeader MessageHeader = new MessageHeader();
        private static readonly Car Car = new Car();

        static ExampleUsingGeneratedStub()
        {
            try
            {
                // convert some sample strings to the correct encoding for this sample
                VehicleCode = Encoding.GetEncoding(Car.VehicleCodeCharacterEncoding).GetBytes("abcdef");
                ManufacturerCode = Encoding.GetEncoding(Engine.ManufacturerCodeCharacterEncoding).GetBytes("123");
                Make = Encoding.GetEncoding(Car.MakeCharacterEncoding).GetBytes("Honda");
                Model = Encoding.GetEncoding(Car.MakeCharacterEncoding).GetBytes("Civic VTi");
            }
            catch (Exception ex)
            {
                throw new Exception("An error occured while reading encodings", ex);
            }
        }

        public static void Main()
        {
            // This byte array is used for encoding and decoding, this is what ou would send on the wire or save to disk
            var byteBuffer = new byte[4096];
            // You need to "wrap" the array with a DirectBuffer, this class is used by the generated code to read and write efficiently to the underlying byte array
            var directBuffer = new DirectBuffer(byteBuffer);
            const short messageTemplateVersion = 0; 
            int bufferOffset = 0;

            // Before encoding a message we need to create a SBE header which specify what we are going to encode (this will allow the decoder to detect that it's an encoded 'car' object)
            // We will probably simplify this part soon, so the header gets applied automatically, but for now it's manual
            MessageHeader.Wrap(directBuffer, bufferOffset, messageTemplateVersion); // position the MessageHeader on the DirectBuffer, at the correct position
            MessageHeader.BlockLength = Car.BlockLength; // size that a car takes on the wire
            MessageHeader.SchemaId = Car.SchemaId;
            MessageHeader.TemplateId = Car.TemplateId;   // identifier for the car object (SBE template ID)
            MessageHeader.Version = Car.Schema_Version; // this can be overriden if we want to support different versions of the car object (advanced functionality)

            // Now that we have encoded the header in the byte array we can encode the car object itself
            bufferOffset += MessageHeader.Size;
            Encode(Car, directBuffer, bufferOffset);


            // Now we have encoded the message is the byte array, we are going to decode it

            // first we decode the header (in a real world scenario you would need the header to decide which SBE decoder you are going to use
            bufferOffset = 0;
            // position the MessageHeader object at the beginning of the array
            MessageHeader.Wrap(directBuffer, bufferOffset, messageTemplateVersion);

            // Extract infos from the header
            // In a real app you would use that to lookup the applicable flyweight to decode this type of message based on templateId and version.
            int actingBlockLength = MessageHeader.BlockLength;
            int schemaId = MessageHeader.SchemaId;
            int actingVersion = MessageHeader.Version;

            bufferOffset += MessageHeader.Size;
            // now we decode the message
            Decode(Car, directBuffer, bufferOffset, actingBlockLength, schemaId, actingVersion);
        }

        public static int Encode(Car car, DirectBuffer directBuffer, int bufferOffset)
        {
            const int srcOffset = 0;

            // we position the car encoder on the direct buffer, at the correct offset (ie. just after the header)
            car.WrapForEncode(directBuffer, bufferOffset);
            car.SerialNumber = 1234; // we set the different fields, just as normal properties and they get written straight to the underlying byte buffer
            car.ModelYear = 2013;
            car.Available = BooleanType.TRUE; // enums are supports
            car.Code = Generated.Model.A;
            car.SetVehicleCode(VehicleCode, srcOffset); // we set a constant string

            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                car.SetSomeNumbers(i, i); // this property is defined as a constant length array of integers
            }

            car.Extras = OptionalExtras.CruiseControl | OptionalExtras.SunRoof; // bit set (flag enums in C#) are supported

            car.Engine.Capacity = 2000;
            car.Engine.NumCylinders = 4;
            car.Engine.SetManufacturerCode(ManufacturerCode, srcOffset);

            // we have written all the constant length fields, now we can write the repeatable groups

            var fuelFigures = car.FuelFiguresCount(3); // we specify that we are going to write 3 FueldFigures (the API is not very .NET friendly yet, we will address that)
            fuelFigures.Next(); // move to the first element
            fuelFigures.Speed = 30;
            fuelFigures.Mpg = 35.9f;

            fuelFigures.Next(); // second
            fuelFigures.Speed = 55;
            fuelFigures.Mpg = 49.0f;

            fuelFigures.Next();
            fuelFigures.Speed = 75;
            fuelFigures.Mpg = 40.0f;

            Car.PerformanceFiguresGroup perfFigures = car.PerformanceFiguresCount(2); // demonstrates how to create a nested group
            perfFigures.Next();
            perfFigures.OctaneRating = 95;

            Car.PerformanceFiguresGroup.AccelerationGroup acceleration = perfFigures.AccelerationCount(3).Next(); // this group is going to be nested in the first element of the previous group
            acceleration.Mph = 30;
            acceleration.Seconds = 4.0f;

            acceleration.Next();
            acceleration.Mph = 60;
            acceleration.Seconds = 7.5f;

            acceleration.Next();
            acceleration.Mph = 100;
            acceleration.Seconds = 12.2f;

            perfFigures.Next();
            perfFigures.OctaneRating = 99;
            acceleration = perfFigures.AccelerationCount(3).Next();

            acceleration.Mph = 30;
            acceleration.Seconds = 3.8f;

            acceleration.Next();
            acceleration.Mph = 60;
            acceleration.Seconds = 7.1f;

            acceleration.Next();
            acceleration.Mph = 100;
            acceleration.Seconds = 11.8f;

            // once we have written all the repeatable groups we can write the variable length properties (you would use that for strings, byte[], etc)

            car.SetMake(Make, srcOffset, Make.Length);
            car.SetMake(Model, srcOffset, Model.Length);

            return car.Size;
        }

        public static void Decode(Car car,
            DirectBuffer directBuffer,
            int bufferOffset,
            int actingBlockLength,
            int schemaId,
            int actingVersion)
        {
            var buffer = new byte[128];
            var sb = new StringBuilder();

            // position the car flyweight just after the header on the DirectBuffer
            car.WrapForDecode(directBuffer, bufferOffset, actingBlockLength, actingVersion);

            // decode the car properties on by one, directly from the buffer
            sb.Append("\ncar.templateId=").Append(Car.TemplateId);
            sb.Append("\ncar.schemaId=").Append(schemaId);
            sb.Append("\ncar.schemaVersion=").Append(Car.Schema_Version);
            sb.Append("\ncar.serialNumber=").Append(car.SerialNumber);
            sb.Append("\ncar.modelYear=").Append(car.ModelYear);
            sb.Append("\ncar.available=").Append(car.Available);
            sb.Append("\ncar.code=").Append(car.Code);

            sb.Append("\ncar.someNumbers=");
            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                sb.Append(car.GetSomeNumbers(i)).Append(", ");
            }

            sb.Append("\ncar.vehicleCode=");
            for (int i = 0, size = Car.VehicleCodeLength; i < size; i++)
            {
                sb.Append((char) car.GetVehicleCode(i));
            }

            OptionalExtras extras = car.Extras;
            sb.Append("\ncar.extras.cruiseControl=").Append((extras & OptionalExtras.CruiseControl) == OptionalExtras.CruiseControl); // this is how you can find out if a specific flag is set in a flag enum
            sb.Append("\ncar.extras.sportsPack=").Append((extras & OptionalExtras.SportsPack) == OptionalExtras.SportsPack);
            sb.Append("\ncar.extras.sunRoof=").Append((extras & OptionalExtras.SunRoof) == OptionalExtras.SunRoof);

            Engine engine = car.Engine;
            sb.Append("\ncar.engine.capacity=").Append(engine.Capacity);
            sb.Append("\ncar.engine.numCylinders=").Append(engine.NumCylinders);
            sb.Append("\ncar.engine.maxRpm=").Append(engine.MaxRpm);
            sb.Append("\ncar.engine.manufacturerCode=");
            for (int i = 0, size = Engine.ManufacturerCodeLength; i < size; i++)
            {
                sb.Append((char) engine.GetManufacturerCode(i));
            }

            int length = engine.GetFuel(buffer, 0, buffer.Length);

            sb.Append("\ncar.engine.fuel=").Append(Encoding.ASCII.GetString(buffer, 0, length)); // string requires a bit of work to decode

            var fuelFiguresGroup = car.FuelFigures; // decode a repeatable group (we will change the API to support foreach soon)
            while (fuelFiguresGroup.HasNext)
            {
                var fuelFigures = fuelFiguresGroup.Next();
                sb.Append("\ncar.fuelFigures.speed=").Append(fuelFigures.Speed);
                sb.Append("\ncar.fuelFigures.mpg=").Append(fuelFigures.Mpg);
            }

            // the nested group
            var performanceFiguresGroup = car.PerformanceFigures;
            while (performanceFiguresGroup.HasNext)
            {
                var performanceFigures = performanceFiguresGroup.Next();
                sb.Append("\ncar.performanceFigures.octaneRating=").Append(performanceFigures.OctaneRating);

                var accelerationGroup = performanceFigures.Acceleration; 
                while (accelerationGroup.HasNext)
                {
                    var acceleration = accelerationGroup.Next();
                    sb.Append("\ncar.performanceFigures.acceleration.mph=").Append(acceleration.Mph);
                    sb.Append("\ncar.performanceFigures.acceleration.seconds=").Append(acceleration.Seconds);
                }
            }

            // variable length fields
            sb.Append("\ncar.make.semanticType=").Append(Car.MakeMetaAttribute(MetaAttribute.SemanticType));
            length = car.GetMake(buffer, 0, buffer.Length);
            sb.Append("\ncar.make=").Append(Encoding.GetEncoding(Car.MakeCharacterEncoding).GetString(buffer, 0, length));

            length = car.GetModel(buffer, 0, buffer.Length);
            sb.Append("\ncar.model=").Append(Encoding.GetEncoding(Car.ModelCharacterEncoding).GetString(buffer, 0, length));

            sb.Append("\ncar.size=").Append(car.Size);

            Console.WriteLine(sb.ToString());
        }
    }
}