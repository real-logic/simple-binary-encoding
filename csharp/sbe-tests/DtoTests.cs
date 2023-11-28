using Extension;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Org.SbeTool.Sbe.Dll;

namespace Org.SbeTool.Sbe.Tests
{
    [TestClass]
    public class DtoTests
    {
        [TestMethod]
        public void ShouldRoundTripCar1()
        {
            var inputByteArray = new byte[1024];
            var inputBuffer = new DirectBuffer(inputByteArray);
            EncodeCar(inputBuffer, 0);
            var decoder = new Car();
            decoder.WrapForDecode(inputBuffer, 0, Car.BlockLength, Car.SchemaVersion);
            var decoderString = decoder.ToString();
            var dto = CarDto.DecodeWith(decoder);
            var outputByteArray = new byte[1024];
            var outputBuffer = new DirectBuffer(outputByteArray);
            var encoder = new Car();
            encoder.WrapForEncode(outputBuffer, 0);
            CarDto.EncodeWith(encoder, dto);
            var dtoString = dto.ToSbeString();
            CollectionAssert.AreEqual(inputByteArray, outputByteArray);
            Assert.AreEqual(decoderString, dtoString);
        }
        
        [TestMethod]
        public void ShouldRoundTripCar2()
        {
            var inputByteArray = new byte[1024];
            var inputBuffer = new DirectBuffer(inputByteArray);
            var length = EncodeCar(inputBuffer, 0);
            var dto = CarDto.DecodeFrom(inputBuffer, 0, length, Car.BlockLength, Car.SchemaVersion);
            var outputByteArray = new byte[1024];
            var outputBuffer = new DirectBuffer(outputByteArray);
            CarDto.EncodeInto(outputBuffer, 0, dto);
            CollectionAssert.AreEqual(inputByteArray, outputByteArray);
        }

        private static int EncodeCar(DirectBuffer buffer, int offset)
        {
            var car = new Car();
            car.WrapForEncode(buffer, offset);
            car.SerialNumber = 1234;
            car.ModelYear = 2013;
            car.Available = BooleanType.T;
            car.Code = Model.A;
            car.SetVehicleCode("ABCDEF");

            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                car.SetSomeNumbers(i, (uint)i);
            }

            car.Extras = OptionalExtras.CruiseControl | OptionalExtras.SportsPack;

            car.CupHolderCount = 119;

            car.Engine.Capacity = 2000;
            car.Engine.NumCylinders = 4;
            car.Engine.SetManufacturerCode("ABC");
            car.Engine.Efficiency = 35;
            car.Engine.BoosterEnabled = BooleanType.T;
            car.Engine.Booster.BoostType = BoostType.NITROUS;
            car.Engine.Booster.HorsePower = 200;

            var fuelFigures = car.FuelFiguresCount(3);
            fuelFigures.Next();
            fuelFigures.Speed = 30;
            fuelFigures.Mpg = 35.9f;
            fuelFigures.SetUsageDescription("this is a description");

            fuelFigures.Next();
            fuelFigures.Speed = 55;
            fuelFigures.Mpg = 49.0f;
            fuelFigures.SetUsageDescription("this is a description");

            fuelFigures.Next();
            fuelFigures.Speed = 75;
            fuelFigures.Mpg = 40.0f;
            fuelFigures.SetUsageDescription("this is a description");

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

            car.SetManufacturer("Ford");
            car.SetModel("Fiesta");
            car.SetActivationCode("1234");

            return car.Limit - offset;
        }
    }
}