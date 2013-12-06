using System;
using System.Text;
using Adaptive.SimpleBinaryEncoding;

namespace Uk.Co.Real_logic.Sbe.Examples
{
    public class Program
    {
        private static readonly byte[] _vehicleCode;
        private static readonly byte[] _manufacturerCode;
        private static readonly byte[] _make;
        private static readonly byte[] _model;

        private static readonly MessageHeader MessageHeader = new MessageHeader();
        private static readonly Car Car = new Car();

        static Program()
        {
            try
            {
                _vehicleCode = Encoding.GetEncoding(Car.VehicleCodeCharacterEncoding).GetBytes("abcdef");
                _manufacturerCode = Encoding.GetEncoding(Engine.ManufacturerCodeCharacterEncoding).GetBytes("123");
                _make = Encoding.GetEncoding(Car.MakeCharacterEncoding).GetBytes("Honda");
                _model = Encoding.GetEncoding(Car.MakeCharacterEncoding).GetBytes("Civic VTi");
            }
            catch (Exception ex)
            {
                throw new Exception("An error occured while reading encodings", ex);
            }
        }

        private static void Main()
        {
            var byteBuffer = new byte[4096];
            var directBuffer = new DirectBuffer(byteBuffer);
            const short messageTemplateVersion = 0;
            int bufferOffset = 0;
            int encodingLength = 0;

            // Setup for encoding a message

            MessageHeader.Wrap(directBuffer, bufferOffset, messageTemplateVersion);
            MessageHeader.BlockLength = Car.BlockLength;
            MessageHeader.TemplateId = Car.TemplateId;
            MessageHeader.Version = Car.TemplateVersion;

            bufferOffset += MessageHeader.Size;
            encodingLength += MessageHeader.Size;
            encodingLength += Encode(Car, directBuffer, bufferOffset);

            // Decode the encoded message

            bufferOffset = 0;
            MessageHeader.Wrap(directBuffer, bufferOffset, messageTemplateVersion);

            // Lookup the applicable flyweight to decode this type of message based on templateId and version.
            int templateId = MessageHeader.TemplateId;
            short actingVersion = MessageHeader.Version;
            int actingBlockLength = MessageHeader.BlockLength;

            bufferOffset += MessageHeader.Size;
            Decode(Car, directBuffer, bufferOffset, actingBlockLength, actingVersion);

            Console.ReadKey();
        }

        private static int Encode(Car car, DirectBuffer directBuffer, int bufferOffset)
        {
            int srcOffset = 0;

            car.WrapForEncode(directBuffer, bufferOffset);
            car.SerialNumber = 1234;
            car.ModelYear = 2013;
            car.Available = BooleanType.TRUE;
            car.Code = Model.A;
            car.SetVehicleCode(_vehicleCode, srcOffset);

            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                car.SetSomeNumbers(i, i);
            }

            car.Extras = OptionalExtras.CruiseControl | OptionalExtras.SunRoof;

            car.Engine.Capacity = 2000;
            car.Engine.NumCylinders = 4;
            car.Engine.SetManufacturerCode(_manufacturerCode, srcOffset);

            var fuelFigures = car.FuelFiguresCount(3);
            fuelFigures.Next();
            fuelFigures.Speed = 30;
            fuelFigures.Mpg = 35.9f;

            fuelFigures.Next();
            fuelFigures.Speed = 55;
            fuelFigures.Mpg = 49.0f;

            fuelFigures.Next();
            fuelFigures.Speed = 75;
            fuelFigures.Mpg = 40.0f;

            Car.PerformanceFiguresGroup perfFigures = car.PerformanceFiguresCount(2);
            perfFigures.Next();
            perfFigures.OctaneRating = 95;

            Car.PerformanceFiguresGroup.AccelerationGroup acceleration = perfFigures.AccelerationCount(3).Next();
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

            car.SetMake(_make, srcOffset, _make.Length);
            car.SetMake(_model, srcOffset, _model.Length);

            return car.Size;
        }

        private static void Decode(Car car,
            DirectBuffer directBuffer,
            int bufferOffset,
            int actingBlockLength,
            int actingVersion)
        {
            var buffer = new byte[128];
            var sb = new StringBuilder();

            car.WrapForDecode(directBuffer, bufferOffset, actingBlockLength, actingVersion);

            sb.Append("\ncar.templateId=").Append(Car.TemplateId);
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
            sb.Append("\ncar.extras.cruiseControl=").Append((extras & OptionalExtras.CruiseControl) == OptionalExtras.CruiseControl);
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

            sb.Append("\ncar.engine.fuel=").Append(Encoding.ASCII.GetString(buffer, 0, length));


            var fuelFiguresGroup = car.FuelFigures;
            while (fuelFiguresGroup.HasNext)
            {
                var fuelFigures = fuelFiguresGroup.Next();
                sb.Append("\ncar.fuelFigures.speed=").Append(fuelFigures.Speed);
                sb.Append("\ncar.fuelFigures.mpg=").Append(fuelFigures.Mpg);
            }


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

            length = car.GetMake(buffer, 0, buffer.Length);
            sb.Append("\ncar.make=").Append(Encoding.GetEncoding(Car.MakeCharacterEncoding).GetString(buffer, 0, length));

            length = car.GetModel(buffer, 0, buffer.Length);
            sb.Append("\ncar.model=").Append(Encoding.GetEncoding(Car.ModelCharacterEncoding).GetString(buffer, 0, length));

            sb.Append("\ncar.size=").Append(car.Size);

            Console.WriteLine(sb.ToString());
        }
    }
}