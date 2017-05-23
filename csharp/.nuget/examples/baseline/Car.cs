/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Org.SbeTool.Sbe.Dll;

namespace Baseline
{
    public sealed partial class Car
    {
        public const ushort BlockLength = (ushort)49;
        public const ushort TemplateId = (ushort)1;
        public const ushort SchemaId = (ushort)1;
        public const ushort SchemaVersion = (ushort)0;
        public const string SemanticType = "";

        private readonly Car _parentMessage;
        private DirectBuffer _buffer;
        private int _offset;
        private int _limit;
        private int _actingBlockLength;
        private int _actingVersion;

        public int Offset { get { return _offset; } }

        public Car()
        {
            _parentMessage = this;
        }

        public void WrapForEncode(DirectBuffer buffer, int offset)
        {
            _buffer = buffer;
            _offset = offset;
            _actingBlockLength = BlockLength;
            _actingVersion = SchemaVersion;
            Limit = offset + _actingBlockLength;
        }

        public void WrapForDecode(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion)
        {
            _buffer = buffer;
            _offset = offset;
            _actingBlockLength = actingBlockLength;
            _actingVersion = actingVersion;
            Limit = offset + _actingBlockLength;
        }

        public int Size
        {
            get
            {
                return _limit - _offset;
            }
        }

        public int Limit
        {
            get
            {
                return _limit;
            }
            set
            {
                _buffer.CheckLimit(value);
                _limit = value;
            }
        }


        public const int SerialNumberId = 1;
        public const int SerialNumberSinceVersion = 0;
        public const int SerialNumberDeprecated = 0;
        public bool SerialNumberInActingVersion()
        {
            return _actingVersion >= SerialNumberSinceVersion;
        }

        public static string SerialNumberMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong SerialNumberNullValue = 0xffffffffffffffffUL;
        public const ulong SerialNumberMinValue = 0x0UL;
        public const ulong SerialNumberMaxValue = 0xfffffffffffffffeUL;

        public ulong SerialNumber
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 0);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 0, value);
            }
        }


        public const int ModelYearId = 2;
        public const int ModelYearSinceVersion = 0;
        public const int ModelYearDeprecated = 0;
        public bool ModelYearInActingVersion()
        {
            return _actingVersion >= ModelYearSinceVersion;
        }

        public static string ModelYearMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ushort ModelYearNullValue = (ushort)65535;
        public const ushort ModelYearMinValue = (ushort)0;
        public const ushort ModelYearMaxValue = (ushort)65534;

        public ushort ModelYear
        {
            get
            {
                return _buffer.Uint16GetLittleEndian(_offset + 8);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 8, value);
            }
        }


        public const int AvailableId = 3;
        public const int AvailableSinceVersion = 0;
        public const int AvailableDeprecated = 0;
        public bool AvailableInActingVersion()
        {
            return _actingVersion >= AvailableSinceVersion;
        }

        public static string AvailableMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public BooleanType Available
        {
            get
            {
                return (BooleanType)_buffer.Uint8Get(_offset + 10);
            }
            set
            {
                _buffer.Uint8Put(_offset + 10, (byte)value);
            }
        }


        public const int CodeId = 4;
        public const int CodeSinceVersion = 0;
        public const int CodeDeprecated = 0;
        public bool CodeInActingVersion()
        {
            return _actingVersion >= CodeSinceVersion;
        }

        public static string CodeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public Model Code
        {
            get
            {
                return (Model)_buffer.CharGet(_offset + 11);
            }
            set
            {
                _buffer.CharPut(_offset + 11, (byte)value);
            }
        }


        public const int SomeNumbersId = 5;
        public const int SomeNumbersSinceVersion = 0;
        public const int SomeNumbersDeprecated = 0;
        public bool SomeNumbersInActingVersion()
        {
            return _actingVersion >= SomeNumbersSinceVersion;
        }

        public static string SomeNumbersMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const uint SomeNumbersNullValue = 4294967295U;
        public const uint SomeNumbersMinValue = 0U;
        public const uint SomeNumbersMaxValue = 4294967294U;

        public const int SomeNumbersLength = 5;

        public uint GetSomeNumbers(int index)
        {
            if (index < 0 || index >= 5)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.Uint32GetLittleEndian(_offset + 12 + (index * 4));
        }

        public void SetSomeNumbers(int index, uint value)
        {
            if (index < 0 || index >= 5)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.Uint32PutLittleEndian(_offset + 12 + (index * 4), value);
        }

        public const int VehicleCodeId = 6;
        public const int VehicleCodeSinceVersion = 0;
        public const int VehicleCodeDeprecated = 0;
        public bool VehicleCodeInActingVersion()
        {
            return _actingVersion >= VehicleCodeSinceVersion;
        }

        public static string VehicleCodeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const byte VehicleCodeNullValue = (byte)0;
        public const byte VehicleCodeMinValue = (byte)32;
        public const byte VehicleCodeMaxValue = (byte)126;

        public const int VehicleCodeLength = 6;

        public byte GetVehicleCode(int index)
        {
            if (index < 0 || index >= 6)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.CharGet(_offset + 32 + (index * 1));
        }

        public void SetVehicleCode(int index, byte value)
        {
            if (index < 0 || index >= 6)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.CharPut(_offset + 32 + (index * 1), value);
        }

        public const string VehicleCodeCharacterEncoding = "ASCII";

        public int GetVehicleCode(byte[] dst, int dstOffset)
        {
            const int length = 6;
            if (dstOffset < 0 || dstOffset > (dst.Length - length))
            {
                throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
            }

            _buffer.GetBytes(_offset + 32, dst, dstOffset, length);
            return length;
        }

        public void SetVehicleCode(byte[] src, int srcOffset)
        {
            const int length = 6;
            if (srcOffset < 0 || srcOffset > src.Length)
            {
                throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
            }

            if (src.Length > length)
            {
                throw new ArgumentOutOfRangeException($"src.Length={src.Length} is too large.");
            }

            _buffer.SetBytes(_offset + 32, src, srcOffset, src.Length - srcOffset);
        }

        public const int ExtrasId = 7;
        public const int ExtrasSinceVersion = 0;
        public const int ExtrasDeprecated = 0;
        public bool ExtrasInActingVersion()
        {
            return _actingVersion >= ExtrasSinceVersion;
        }

        public static string ExtrasMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public OptionalExtras Extras
        {
            get
            {
                return (OptionalExtras)_buffer.Uint8Get(_offset + 38);
            }
            set
            {
                _buffer.Uint8Put(_offset + 38, (byte)value);
            }
        }

        public const int DiscountedModelId = 8;
        public const int DiscountedModelSinceVersion = 0;
        public const int DiscountedModelDeprecated = 0;
        public bool DiscountedModelInActingVersion()
        {
            return _actingVersion >= DiscountedModelSinceVersion;
        }

        public static string DiscountedModelMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public Model DiscountedModel
        {
            get
            {
                return (Model)_buffer.CharGet(_offset + 39);
            }
            set
            {
                _buffer.CharPut(_offset + 39, (byte)value);
            }
        }


        public const int EngineId = 9;
        public const int EngineSinceVersion = 0;
        public const int EngineDeprecated = 0;
        public bool EngineInActingVersion()
        {
            return _actingVersion >= EngineSinceVersion;
        }

        public static string EngineMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        private readonly Engine _engine = new Engine();

        public Engine Engine
        {
            get
            {
                _engine.Wrap(_buffer, _offset + 39, _actingVersion);
                return _engine;
            }
        }

        private readonly FuelFiguresGroup _fuelFigures = new FuelFiguresGroup();

        public const long FuelFiguresId = 10;
        public const int FuelFiguresSinceVersion = 0;
        public const int FuelFiguresDeprecated = 0;
        public bool FuelFiguresInActingVersion()
        {
            return _actingVersion >= FuelFiguresSinceVersion;
        }

        public FuelFiguresGroup FuelFigures
        {
            get
            {
                _fuelFigures.WrapForDecode(_parentMessage, _buffer, _actingVersion);
                return _fuelFigures;
            }
        }

        public FuelFiguresGroup FuelFiguresCount(int count)
        {
            _fuelFigures.WrapForEncode(_parentMessage, _buffer, count);
            return _fuelFigures;
        }

        public sealed partial class FuelFiguresGroup
        {
            private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
            private Car _parentMessage;
            private DirectBuffer _buffer;
            private int _blockLength;
            private int _actingVersion;
            private int _count;
            private int _index;
            private int _offset;

            public void WrapForDecode(Car parentMessage, DirectBuffer buffer, int actingVersion)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);
                _blockLength = _dimensions.BlockLength;
                _count = _dimensions.NumInGroup;
                _actingVersion = actingVersion;
                _index = -1;
                _parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
            }

            public void WrapForEncode(Car parentMessage, DirectBuffer buffer, int count)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
                _dimensions.BlockLength = (ushort)6;
                _dimensions.NumInGroup = (ushort)count;
                _index = -1;
                _count = count;
                _blockLength = 6;
                parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
            }

            public const int SbeBlockLength = 6;
            public const int SbeHeaderSize = 4;
            public int ActingBlockLength { get { return _blockLength; } }

            public int Count { get { return _count; } }

            public bool HasNext { get { return (_index + 1) < _count; } }

            public FuelFiguresGroup Next()
            {
                if (_index + 1 >= _count)
                {
                    throw new InvalidOperationException();
                }

                _offset = _parentMessage.Limit;
                _parentMessage.Limit = _offset + _blockLength;
                ++_index;

                return this;
            }

            public System.Collections.IEnumerator GetEnumerator()
            {
                while (this.HasNext)
                {
                    yield return this.Next();
                }
            }

            public const int SpeedId = 11;
            public const int SpeedSinceVersion = 0;
            public const int SpeedDeprecated = 0;
            public bool SpeedInActingVersion()
            {
                return _actingVersion >= SpeedSinceVersion;
            }

            public static string SpeedMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "";
                }

                return "";
            }

            public const ushort SpeedNullValue = (ushort)65535;
            public const ushort SpeedMinValue = (ushort)0;
            public const ushort SpeedMaxValue = (ushort)65534;

            public ushort Speed
            {
                get
                {
                    return _buffer.Uint16GetLittleEndian(_offset + 0);
                }
                set
                {
                    _buffer.Uint16PutLittleEndian(_offset + 0, value);
                }
            }


            public const int MpgId = 12;
            public const int MpgSinceVersion = 0;
            public const int MpgDeprecated = 0;
            public bool MpgInActingVersion()
            {
                return _actingVersion >= MpgSinceVersion;
            }

            public static string MpgMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "";
                }

                return "";
            }

            public const float MpgNullValue = float.NaN;
            public const float MpgMinValue = 1.401298464324817E-45f;
            public const float MpgMaxValue = 3.4028234663852886E38f;

            public float Mpg
            {
                get
                {
                    return _buffer.FloatGetLittleEndian(_offset + 2);
                }
                set
                {
                    _buffer.FloatPutLittleEndian(_offset + 2, value);
                }
            }


            public const int UsageDescriptionId = 200;
            public const int UsageDescriptionSinceVersion = 0;
            public const int UsageDescriptionDeprecated = 0;
            public bool UsageDescriptionInActingVersion()
            {
                return _actingVersion >= UsageDescriptionSinceVersion;
            }

            public const string UsageDescriptionCharacterEncoding = "UTF-8";


            public static string UsageDescriptionMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "";
                }

                return "";
            }

            public const int UsageDescriptionHeaderSize = 4;

            public int GetUsageDescription(byte[] dst, int dstOffset, int length)
            {
                const int sizeOfLengthField = 4;
                int limit = _parentMessage.Limit;
                _buffer.CheckLimit(limit + sizeOfLengthField);
                int dataLength = (int)_buffer.Uint32GetLittleEndian(limit);
                int bytesCopied = Math.Min(length, dataLength);
                _parentMessage.Limit = limit + sizeOfLengthField + dataLength;
                _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

                return bytesCopied;
            }

            public int SetUsageDescription(byte[] src, int srcOffset, int length)
            {
                const int sizeOfLengthField = 4;
                int limit = _parentMessage.Limit;
                _parentMessage.Limit = limit + sizeOfLengthField + length;
                _buffer.Uint32PutLittleEndian(limit, (uint)length);
                _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

                return length;
            }
        }

        private readonly PerformanceFiguresGroup _performanceFigures = new PerformanceFiguresGroup();

        public const long PerformanceFiguresId = 13;
        public const int PerformanceFiguresSinceVersion = 0;
        public const int PerformanceFiguresDeprecated = 0;
        public bool PerformanceFiguresInActingVersion()
        {
            return _actingVersion >= PerformanceFiguresSinceVersion;
        }

        public PerformanceFiguresGroup PerformanceFigures
        {
            get
            {
                _performanceFigures.WrapForDecode(_parentMessage, _buffer, _actingVersion);
                return _performanceFigures;
            }
        }

        public PerformanceFiguresGroup PerformanceFiguresCount(int count)
        {
            _performanceFigures.WrapForEncode(_parentMessage, _buffer, count);
            return _performanceFigures;
        }

        public sealed partial class PerformanceFiguresGroup
        {
            private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
            private Car _parentMessage;
            private DirectBuffer _buffer;
            private int _blockLength;
            private int _actingVersion;
            private int _count;
            private int _index;
            private int _offset;

            public void WrapForDecode(Car parentMessage, DirectBuffer buffer, int actingVersion)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);
                _blockLength = _dimensions.BlockLength;
                _count = _dimensions.NumInGroup;
                _actingVersion = actingVersion;
                _index = -1;
                _parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
            }

            public void WrapForEncode(Car parentMessage, DirectBuffer buffer, int count)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
                _dimensions.BlockLength = (ushort)1;
                _dimensions.NumInGroup = (ushort)count;
                _index = -1;
                _count = count;
                _blockLength = 1;
                parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
            }

            public const int SbeBlockLength = 1;
            public const int SbeHeaderSize = 4;
            public int ActingBlockLength { get { return _blockLength; } }

            public int Count { get { return _count; } }

            public bool HasNext { get { return (_index + 1) < _count; } }

            public PerformanceFiguresGroup Next()
            {
                if (_index + 1 >= _count)
                {
                    throw new InvalidOperationException();
                }

                _offset = _parentMessage.Limit;
                _parentMessage.Limit = _offset + _blockLength;
                ++_index;

                return this;
            }

            public System.Collections.IEnumerator GetEnumerator()
            {
                while (this.HasNext)
                {
                    yield return this.Next();
                }
            }

            public const int OctaneRatingId = 14;
            public const int OctaneRatingSinceVersion = 0;
            public const int OctaneRatingDeprecated = 0;
            public bool OctaneRatingInActingVersion()
            {
                return _actingVersion >= OctaneRatingSinceVersion;
            }

            public static string OctaneRatingMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "";
                }

                return "";
            }

            public const byte OctaneRatingNullValue = (byte)255;
            public const byte OctaneRatingMinValue = (byte)90;
            public const byte OctaneRatingMaxValue = (byte)110;

            public byte OctaneRating
            {
                get
                {
                    return _buffer.Uint8Get(_offset + 0);
                }
                set
                {
                    _buffer.Uint8Put(_offset + 0, value);
                }
            }


            private readonly AccelerationGroup _acceleration = new AccelerationGroup();

            public const long AccelerationId = 15;
            public const int AccelerationSinceVersion = 0;
            public const int AccelerationDeprecated = 0;
            public bool AccelerationInActingVersion()
            {
                return _actingVersion >= AccelerationSinceVersion;
            }

            public AccelerationGroup Acceleration
            {
                get
                {
                    _acceleration.WrapForDecode(_parentMessage, _buffer, _actingVersion);
                    return _acceleration;
                }
            }

            public AccelerationGroup AccelerationCount(int count)
            {
                _acceleration.WrapForEncode(_parentMessage, _buffer, count);
                return _acceleration;
            }

            public sealed partial class AccelerationGroup
            {
                private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
                private Car _parentMessage;
                private DirectBuffer _buffer;
                private int _blockLength;
                private int _actingVersion;
                private int _count;
                private int _index;
                private int _offset;

                public void WrapForDecode(Car parentMessage, DirectBuffer buffer, int actingVersion)
                {
                    _parentMessage = parentMessage;
                    _buffer = buffer;
                    _dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);
                    _blockLength = _dimensions.BlockLength;
                    _count = _dimensions.NumInGroup;
                    _actingVersion = actingVersion;
                    _index = -1;
                    _parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
                }

                public void WrapForEncode(Car parentMessage, DirectBuffer buffer, int count)
                {
                    _parentMessage = parentMessage;
                    _buffer = buffer;
                    _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
                    _dimensions.BlockLength = (ushort)6;
                    _dimensions.NumInGroup = (ushort)count;
                    _index = -1;
                    _count = count;
                    _blockLength = 6;
                    parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
                }

                public const int SbeBlockLength = 6;
                public const int SbeHeaderSize = 4;
                public int ActingBlockLength { get { return _blockLength; } }

                public int Count { get { return _count; } }

                public bool HasNext { get { return (_index + 1) < _count; } }

                public AccelerationGroup Next()
                {
                    if (_index + 1 >= _count)
                    {
                        throw new InvalidOperationException();
                    }

                    _offset = _parentMessage.Limit;
                    _parentMessage.Limit = _offset + _blockLength;
                    ++_index;

                    return this;
                }

                public System.Collections.IEnumerator GetEnumerator()
                {
                    while (this.HasNext)
                    {
                        yield return this.Next();
                    }
                }

                public const int MphId = 16;
                public const int MphSinceVersion = 0;
                public const int MphDeprecated = 0;
                public bool MphInActingVersion()
                {
                    return _actingVersion >= MphSinceVersion;
                }

                public static string MphMetaAttribute(MetaAttribute metaAttribute)
                {
                    switch (metaAttribute)
                    {
                        case MetaAttribute.Epoch: return "unix";
                        case MetaAttribute.TimeUnit: return "nanosecond";
                        case MetaAttribute.SemanticType: return "";
                    }

                    return "";
                }

                public const ushort MphNullValue = (ushort)65535;
                public const ushort MphMinValue = (ushort)0;
                public const ushort MphMaxValue = (ushort)65534;

                public ushort Mph
                {
                    get
                    {
                        return _buffer.Uint16GetLittleEndian(_offset + 0);
                    }
                    set
                    {
                        _buffer.Uint16PutLittleEndian(_offset + 0, value);
                    }
                }


                public const int SecondsId = 17;
                public const int SecondsSinceVersion = 0;
                public const int SecondsDeprecated = 0;
                public bool SecondsInActingVersion()
                {
                    return _actingVersion >= SecondsSinceVersion;
                }

                public static string SecondsMetaAttribute(MetaAttribute metaAttribute)
                {
                    switch (metaAttribute)
                    {
                        case MetaAttribute.Epoch: return "unix";
                        case MetaAttribute.TimeUnit: return "nanosecond";
                        case MetaAttribute.SemanticType: return "";
                    }

                    return "";
                }

                public const float SecondsNullValue = float.NaN;
                public const float SecondsMinValue = 1.401298464324817E-45f;
                public const float SecondsMaxValue = 3.4028234663852886E38f;

                public float Seconds
                {
                    get
                    {
                        return _buffer.FloatGetLittleEndian(_offset + 2);
                    }
                    set
                    {
                        _buffer.FloatPutLittleEndian(_offset + 2, value);
                    }
                }

            }
        }

        public const int ManufacturerId = 18;
        public const int ManufacturerSinceVersion = 0;
        public const int ManufacturerDeprecated = 0;
        public bool ManufacturerInActingVersion()
        {
            return _actingVersion >= ManufacturerSinceVersion;
        }

        public const string ManufacturerCharacterEncoding = "UTF-8";


        public static string ManufacturerMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const int ManufacturerHeaderSize = 4;

        public int GetManufacturer(byte[] dst, int dstOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _buffer.CheckLimit(limit + sizeOfLengthField);
            int dataLength = (int)_buffer.Uint32GetLittleEndian(limit);
            int bytesCopied = Math.Min(length, dataLength);
            _parentMessage.Limit = limit + sizeOfLengthField + dataLength;
            _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public int SetManufacturer(byte[] src, int srcOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _parentMessage.Limit = limit + sizeOfLengthField + length;
            _buffer.Uint32PutLittleEndian(limit, (uint)length);
            _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

            return length;
        }

        public const int ModelId = 19;
        public const int ModelSinceVersion = 0;
        public const int ModelDeprecated = 0;
        public bool ModelInActingVersion()
        {
            return _actingVersion >= ModelSinceVersion;
        }

        public const string ModelCharacterEncoding = "UTF-8";


        public static string ModelMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const int ModelHeaderSize = 4;

        public int GetModel(byte[] dst, int dstOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _buffer.CheckLimit(limit + sizeOfLengthField);
            int dataLength = (int)_buffer.Uint32GetLittleEndian(limit);
            int bytesCopied = Math.Min(length, dataLength);
            _parentMessage.Limit = limit + sizeOfLengthField + dataLength;
            _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public int SetModel(byte[] src, int srcOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _parentMessage.Limit = limit + sizeOfLengthField + length;
            _buffer.Uint32PutLittleEndian(limit, (uint)length);
            _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

            return length;
        }

        public const int ActivationCodeId = 20;
        public const int ActivationCodeSinceVersion = 0;
        public const int ActivationCodeDeprecated = 0;
        public bool ActivationCodeInActingVersion()
        {
            return _actingVersion >= ActivationCodeSinceVersion;
        }

        public const string ActivationCodeCharacterEncoding = "UTF-8";


        public static string ActivationCodeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const int ActivationCodeHeaderSize = 4;

        public int GetActivationCode(byte[] dst, int dstOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _buffer.CheckLimit(limit + sizeOfLengthField);
            int dataLength = (int)_buffer.Uint32GetLittleEndian(limit);
            int bytesCopied = Math.Min(length, dataLength);
            _parentMessage.Limit = limit + sizeOfLengthField + dataLength;
            _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public int SetActivationCode(byte[] src, int srcOffset, int length)
        {
            const int sizeOfLengthField = 4;
            int limit = _parentMessage.Limit;
            _parentMessage.Limit = limit + sizeOfLengthField + length;
            _buffer.Uint32PutLittleEndian(limit, (uint)length);
            _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

            return length;
        }
    }
}
