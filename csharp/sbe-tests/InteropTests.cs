// Copyright (C) 2017 MarketFactory, Inc
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Org.SbeTool.Sbe.Dll;
using Baseline;

// If you make modifications here you probably have to modify
// the Interop tests as well

namespace Org.SbeTool.Sbe.Tests
{
    [TestClass]
    public unsafe class InteropTests
    {
        // The byte array is from the java example for interop test made by
        // running with -Dsbe.encoding.filename and then decoded using od -tu1
        static byte[] _decodeBuffer = { 49, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 35, 1, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102 };
        static byte[] _encodeBuffer;

        static DirectBuffer _directBuffer;
        static MessageHeader _messageHeader;
        static Car Car;

        [TestInitialize]
        public void Setup()        {
            _encodeBuffer = new byte[_decodeBuffer.Length];
            _messageHeader = new MessageHeader();
        }

        [TestMethod]
        public void InteropDecodeTest()
        {
            _directBuffer = new DirectBuffer(_decodeBuffer);
            _messageHeader.Wrap(_directBuffer, 0, Baseline.Car.SchemaVersion);

            var Car = new Car();
            Car.WrapForDecode(_directBuffer, MessageHeader.Size, _messageHeader.BlockLength, _messageHeader.Version);

            Assert.AreEqual(1234UL, Car.SerialNumber);
            Assert.AreEqual((ushort)2013, Car.ModelYear);
            Assert.AreEqual(BooleanType.T, Car.Available);
            Assert.AreEqual(Model.A, Car.Code);
            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                Assert.AreEqual(Car.GetSomeNumbers(i), (uint)i);
            }

            var btmp = new byte[6];
            Car.GetVehicleCode(btmp, 0);
            var s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("abcdef", s);
            Assert.AreEqual(OptionalExtras.CruiseControl | OptionalExtras.SportsPack, Car.Extras);
            Assert.AreEqual((ushort)2000, Car.Engine.Capacity);
            Assert.AreEqual((byte)4, Car.Engine.NumCylinders);

            btmp = new byte[3];
            Car.Engine.GetManufacturerCode(btmp, 0);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("123", s);
            Assert.AreEqual((sbyte)35, Car.Engine.Efficiency);
            Assert.AreEqual(BooleanType.T, Car.Engine.BoosterEnabled);
            Assert.AreEqual(BoostType.NITROUS, Car.Engine.Booster.BoostType);
            Assert.AreEqual((byte)200, Car.Engine.Booster.HorsePower);

            var fuelFigures = Car.FuelFigures;
            Assert.AreEqual(3, fuelFigures.Count);

            fuelFigures.Next();
            Assert.AreEqual(30, fuelFigures.Speed);
            Assert.AreEqual(35.9f, fuelFigures.Mpg);
            btmp = new byte[11];
            fuelFigures.GetUsageDescription(btmp, 0, 11);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("Urban Cycle", s);

            fuelFigures.Next();
            Assert.AreEqual(55, fuelFigures.Speed);
            Assert.AreEqual(49.0f, fuelFigures.Mpg);
            btmp = new byte[14];
            fuelFigures.GetUsageDescription(btmp, 0, 14);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("Combined Cycle", s);

            fuelFigures.Next();
            Assert.AreEqual(75, fuelFigures.Speed);
            Assert.AreEqual(40.0f, fuelFigures.Mpg);
            btmp = new byte[13];
            fuelFigures.GetUsageDescription(btmp, 0, 13);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("Highway Cycle", s);

            var perfFigures = Car.PerformanceFigures;
            Assert.AreEqual(2, perfFigures.Count);

            perfFigures.Next();
            Assert.AreEqual((byte)95, perfFigures.OctaneRating);
            var acceleration = perfFigures.Acceleration;
            Assert.AreEqual(3, acceleration.Count);
            acceleration.Next();
            Assert.AreEqual((ushort) 30, acceleration.Mph);
            Assert.AreEqual(4.0f, acceleration.Seconds);
            acceleration.Next();
            Assert.AreEqual((ushort) 60, acceleration.Mph);
            Assert.AreEqual(7.5f, acceleration.Seconds);
            acceleration.Next();
            Assert.AreEqual((ushort) 100, acceleration.Mph);
            Assert.AreEqual(12.2f, acceleration.Seconds);

            perfFigures.Next();
            Assert.AreEqual((byte)99, perfFigures.OctaneRating);
            acceleration = perfFigures.Acceleration;
            Assert.AreEqual(3, acceleration.Count);
            acceleration.Next();
            Assert.AreEqual((ushort)30, acceleration.Mph);
            Assert.AreEqual(3.8f, acceleration.Seconds);
            acceleration.Next();
            Assert.AreEqual((ushort)60, acceleration.Mph);
            Assert.AreEqual(7.1f, acceleration.Seconds);
            acceleration.Next();
            Assert.AreEqual((ushort)100, acceleration.Mph);
            Assert.AreEqual(11.8f, acceleration.Seconds);

            btmp = new byte[5];
            Car.GetManufacturer(btmp, 0, 5);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("Honda", s);
            btmp = new byte[9];
            Car.GetModel(btmp, 0, 9);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("Civic VTi", s);
            btmp = new byte[6];
            Car.GetActivationCode(btmp, 0, 6);
            s = System.Text.Encoding.UTF8.GetString(btmp, 0, btmp.Length);
            Assert.AreEqual("abcdef", s);
        }

        [TestMethod]
        public void InteropEncodeTest()
        {
            Car = new Car();
            _directBuffer = new DirectBuffer(_encodeBuffer);
            _messageHeader.Wrap(_directBuffer, 0, Baseline.Car.SchemaVersion);
            _messageHeader.BlockLength = Car.BlockLength;
            _messageHeader.SchemaId = Car.SchemaId;
            _messageHeader.TemplateId = Car.TemplateId;
            _messageHeader.Version = Car.SchemaVersion;

            // Populate the car with the known interop values
            Car.WrapForEncode(_directBuffer, Baseline.MessageHeader.Size);
            Car.SerialNumber = 1234;
            Car.ModelYear = 2013;
            Car.Available = BooleanType.T;
            Car.Code = Model.A;
            for (int i = 0, size = Car.SomeNumbersLength; i < size; i++)
            {
                Car.SetSomeNumbers(i, (uint)i);
            }
            Car.SetVehicleCode(System.Text.Encoding.ASCII.GetBytes("abcdef"), 0);
            Car.Extras = OptionalExtras.CruiseControl | OptionalExtras.SportsPack;

            Car.Engine.Capacity = 2000;
            Car.Engine.NumCylinders = 4;
            Car.Engine.SetManufacturerCode(System.Text.Encoding.ASCII.GetBytes("123"), 0);
            Car.Engine.Efficiency = 35;
            Car.Engine.BoosterEnabled = BooleanType.T;
            Car.Engine.Booster.BoostType = BoostType.NITROUS;
            Car.Engine.Booster.HorsePower = 200;

            var fuelFigures = Car.FuelFiguresCount(3);
            fuelFigures.Next();
            fuelFigures.Speed = 30;
            fuelFigures.Mpg = 35.9f;
            fuelFigures.SetUsageDescription(System.Text.Encoding.ASCII.GetBytes("Urban Cycle"), 0, 11);

            fuelFigures.Next();
            fuelFigures.Speed = 55;
            fuelFigures.Mpg = 49.0f;
            fuelFigures.SetUsageDescription(System.Text.Encoding.ASCII.GetBytes("Combined Cycle"), 0, 14);

            fuelFigures.Next();
            fuelFigures.Speed = 75;
            fuelFigures.Mpg = 40.0f;
            fuelFigures.SetUsageDescription(System.Text.Encoding.ASCII.GetBytes("Highway Cycle"), 0, 13);

            var perfFigures = Car.PerformanceFiguresCount(2);
            perfFigures.Next();
            perfFigures.OctaneRating = 95;

            var acceleration = perfFigures.AccelerationCount(3).Next();
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

            Car.SetManufacturer(System.Text.Encoding.ASCII.GetBytes("Honda"), 0, 5);
            Car.SetModel(System.Text.Encoding.ASCII.GetBytes("Civic VTi"), 0, 9);
            Car.SetActivationCode(System.Text.Encoding.ASCII.GetBytes("abcdef"), 0, 6);

            for (int i = 0; i < _decodeBuffer.Length; i++)
            {
                Assert.AreEqual(_encodeBuffer[i], _decodeBuffer[i]);
            }
        }
    }
}
