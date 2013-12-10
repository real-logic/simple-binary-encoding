/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class Car : IMessageFlyweight
    {
    public const ushort TemplateId = (ushort)1;
    public const byte TemplateVersion = (byte)0;
    public const ushort BlockLength = (ushort)41;

    private readonly IMessageFlyweight _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _position;
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
        _actingVersion = TemplateVersion;
        Position = offset + _actingBlockLength;
    }

    public void WrapForDecode(DirectBuffer buffer, int offset,
                              int actingBlockLength, int actingVersion)
    {
        _buffer = buffer;
        _offset = offset;
        _actingBlockLength = actingBlockLength;
        _actingVersion = actingVersion;
        Position = offset + _actingBlockLength;
    }

    public int Size
    {
        get
        {
            return _position - _offset;
        }
    }

    public int Position
    {
        get
        {
            return _position;
        }
        set
        {
            _buffer.CheckPosition(_position);
            _position = value;
        }
    }


    public const int SerialNumberSchemaId = 1;

    public const uint SerialNumberNullVal = 4294967294U;

    public const uint SerialNumberMinVal = 0U;

    public const uint SerialNumberMaxVal = 4294967293U;

    public uint SerialNumber
    {
        get
        {
            return _buffer.Uint32GetLittleEndian(_offset + 0);
        }
        set
        {
            _buffer.Uint32PutLittleEndian(_offset + 0, value);
        }
    }


    public const int ModelYearSchemaId = 2;

    public const ushort ModelYearNullVal = (ushort)65535;

    public const ushort ModelYearMinVal = (ushort)0;

    public const ushort ModelYearMaxVal = (ushort)65534;

    public ushort ModelYear
    {
        get
        {
            return _buffer.Uint16GetLittleEndian(_offset + 4);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 4, value);
        }
    }


    public const int AvailableSchemaId = 3;

    public BooleanType Available
    {
        get
        {
            return (BooleanType)_buffer.Uint8Get(_offset + 6);
        }
        set
        {
            _buffer.Uint8Put(_offset + 6, (byte)value);
        }
    }


    public const int CodeSchemaId = 4;

    public Model Code
    {
        get
        {
            return (Model)_buffer.CharGet(_offset + 7);
        }
        set
        {
            _buffer.CharPut(_offset + 7, (byte)value);
        }
    }


    public const int SomeNumbersSchemaId = 5;

    public const int SomeNumbersNullVal = -2147483648;

    public const int SomeNumbersMinVal = -2147483647;

    public const int SomeNumbersMaxVal = 2147483647;

    public const int SomeNumbersLength  = 5;

    public int GetSomeNumbers(int index)
    {
        if (index < 0 || index >= 5)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.Int32GetLittleEndian(_offset + 8 + (index * 4));
    }

    public void SetSomeNumbers(int index, int value)
    {
        if (index < 0 || index >= 5)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.Int32PutLittleEndian(_offset + 8 + (index * 4), value);
    }

    public const int VehicleCodeSchemaId = 6;

    public const byte VehicleCodeNullVal = (byte)0;

    public const byte VehicleCodeMinVal = (byte)32;

    public const byte VehicleCodeMaxVal = (byte)126;

    public const int VehicleCodeLength  = 6;

    public byte GetVehicleCode(int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 28 + (index * 1));
    }

    public void SetVehicleCode(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 28 + (index * 1), value);
    }

    public const string VehicleCodeCharacterEncoding = "UTF-8";

    public int GetVehicleCode(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 28, dst, dstOffset, length);
        return length;
    }

    public void SetVehicleCode(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 28, src, srcOffset, length);
    }

    public const int ExtrasSchemaId = 7;

    public OptionalExtras Extras
    {
        get
        {
            return (OptionalExtras)_buffer.Uint8Get(_offset + 34);
        }
        set
        {
            _buffer.Uint8Put(_offset + 34, (byte)value);
        }
    }

    public const int EngineSchemaId = 8;

    private readonly Engine _engine = new Engine();

    public Engine Engine
    {
        get
        {
            _engine.Wrap(_buffer, _offset + 35, _actingVersion);
            return _engine;
        }
    }

    private readonly FuelFiguresGroup _fuelFigures = new FuelFiguresGroup();

    public const long FuelFiguresSchemaId = 9;


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

    public class FuelFiguresGroup : IGroupFlyweight<FuelFiguresGroup>
    {
        private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
        private IMessageFlyweight _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(IMessageFlyweight parentMessage, DirectBuffer buffer, int actingVersion)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Position, actingVersion);
            _count = _dimensions.NumInGroup;
            _blockLength = _dimensions.BlockLength;
            _actingVersion = actingVersion;
            _index = -1;
            const int dimensionsHeaderSize = 3;
            _parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
        }

        public void WrapForEncode(IMessageFlyweight parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Position, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)6;
            _index = -1;
            _count = count;
            _blockLength = 6;
            const int dimensionsHeaderSize = 3;
            parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public FuelFiguresGroup Next()
        {
            if (_index + 1 >= _count)
            {
                throw new InvalidOperationException();
            }

            _offset = _parentMessage.Position;
            _parentMessage.Position = _offset + _blockLength;
            ++_index;

            return this;
        }

        public const int SpeedSchemaId = 10;

        public const ushort SpeedNullVal = (ushort)65535;

        public const ushort SpeedMinVal = (ushort)0;

        public const ushort SpeedMaxVal = (ushort)65534;

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


        public const int MpgSchemaId = 11;

        public const float MpgNullVal = float.NaN;

        public const float MpgMinVal = 1.401298464324817E-45f;

        public const float MpgMaxVal = 3.4028234663852886E38f;

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

    }

    private readonly PerformanceFiguresGroup _performanceFigures = new PerformanceFiguresGroup();

    public const long PerformanceFiguresSchemaId = 12;


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

    public class PerformanceFiguresGroup : IGroupFlyweight<PerformanceFiguresGroup>
    {
        private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
        private IMessageFlyweight _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(IMessageFlyweight parentMessage, DirectBuffer buffer, int actingVersion)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Position, actingVersion);
            _count = _dimensions.NumInGroup;
            _blockLength = _dimensions.BlockLength;
            _actingVersion = actingVersion;
            _index = -1;
            const int dimensionsHeaderSize = 3;
            _parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
        }

        public void WrapForEncode(IMessageFlyweight parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Position, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)1;
            _index = -1;
            _count = count;
            _blockLength = 1;
            const int dimensionsHeaderSize = 3;
            parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public PerformanceFiguresGroup Next()
        {
            if (_index + 1 >= _count)
            {
                throw new InvalidOperationException();
            }

            _offset = _parentMessage.Position;
            _parentMessage.Position = _offset + _blockLength;
            ++_index;

            return this;
        }

        public const int OctaneRatingSchemaId = 13;

        public const byte OctaneRatingNullVal = (byte)255;

        public const byte OctaneRatingMinVal = (byte)90;

        public const byte OctaneRatingMaxVal = (byte)110;

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

        public const long AccelerationSchemaId = 14;


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

        public class AccelerationGroup : IGroupFlyweight<AccelerationGroup>
        {
            private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
            private IMessageFlyweight _parentMessage;
            private DirectBuffer _buffer;
            private int _blockLength;
            private int _actingVersion;
            private int _count;
            private int _index;
            private int _offset;

            public void WrapForDecode(IMessageFlyweight parentMessage, DirectBuffer buffer, int actingVersion)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Position, actingVersion);
                _count = _dimensions.NumInGroup;
                _blockLength = _dimensions.BlockLength;
                _actingVersion = actingVersion;
                _index = -1;
                const int dimensionsHeaderSize = 3;
                _parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
            }

            public void WrapForEncode(IMessageFlyweight parentMessage, DirectBuffer buffer, int count)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Position, _actingVersion);
                _dimensions.NumInGroup = (byte)count;
                _dimensions.BlockLength = (ushort)6;
                _index = -1;
                _count = count;
                _blockLength = 6;
                const int dimensionsHeaderSize = 3;
                parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;
            }

            public int Count { get { return _count; } }

            public bool HasNext { get { return _index + 1 < _count; } }

            public AccelerationGroup Next()
            {
                if (_index + 1 >= _count)
                {
                    throw new InvalidOperationException();
                }

                _offset = _parentMessage.Position;
                _parentMessage.Position = _offset + _blockLength;
                ++_index;

                return this;
            }

            public const int MphSchemaId = 15;

            public const ushort MphNullVal = (ushort)65535;

            public const ushort MphMinVal = (ushort)0;

            public const ushort MphMaxVal = (ushort)65534;

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


            public const int SecondsSchemaId = 16;

            public const float SecondsNullVal = float.NaN;

            public const float SecondsMinVal = 1.401298464324817E-45f;

            public const float SecondsMaxVal = 3.4028234663852886E38f;

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

    public const int MakeSchemaId = 17;

    public const string MakeCharacterEncoding = "UTF-8";

    public int GetMake(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int lengthPosition = Position;
        Position = lengthPosition + sizeOfLengthField;
        int dataLength = _buffer.Uint8Get(lengthPosition);
        int bytesCopied = Math.Min(length, dataLength);
        _buffer.GetBytes(Position, dst, dstOffset, bytesCopied);
        Position = Position + dataLength;

        return bytesCopied;
    }

    public int SetMake(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int lengthPosition = Position;
        _buffer.Uint8Put(lengthPosition, (byte)length);
        Position = lengthPosition + sizeOfLengthField;
        _buffer.SetBytes(Position, src, srcOffset, length);
        Position = Position + length;

        return length;
    }

    public const int ModelSchemaId = 18;

    public const string ModelCharacterEncoding = "UTF-8";

    public int GetModel(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int lengthPosition = Position;
        Position = lengthPosition + sizeOfLengthField;
        int dataLength = _buffer.Uint8Get(lengthPosition);
        int bytesCopied = Math.Min(length, dataLength);
        _buffer.GetBytes(Position, dst, dstOffset, bytesCopied);
        Position = Position + dataLength;

        return bytesCopied;
    }

    public int SetModel(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int lengthPosition = Position;
        _buffer.Uint8Put(lengthPosition, (byte)length);
        Position = lengthPosition + sizeOfLengthField;
        _buffer.SetBytes(Position, src, srcOffset, length);
        Position = Position + length;

        return length;
    }
    }
}
