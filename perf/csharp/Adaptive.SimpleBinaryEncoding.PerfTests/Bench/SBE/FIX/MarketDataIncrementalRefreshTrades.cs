/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class MarketDataIncrementalRefreshTrades
    {
    public const ushort BlockLength = (ushort)11;
    public const ushort TemplateId = (ushort)2;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "X";

    private readonly MarketDataIncrementalRefreshTrades _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MarketDataIncrementalRefreshTrades()
    {
        _parentMessage = this;
    }

    public void WrapForEncode(DirectBuffer buffer, int offset)
    {
        _buffer = buffer;
        _offset = offset;
        _actingBlockLength = BlockLength;
        _actingVersion = Schema_Version;
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


    public const int TransactTimeId = 60;

    public static string TransactTimeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanossecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const ulong TransactTimeNullValue = 0xffffffffffffffffUL;

    public const ulong TransactTimeMinValue = 0x0UL;

    public const ulong TransactTimeMaxValue = 0xfffffffffffffffeUL;

    public ulong TransactTime
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


    public const int EventTimeDeltaId = 37704;

    public static string EventTimeDeltaMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const ushort EventTimeDeltaNullValue = (ushort)65535;

    public const ushort EventTimeDeltaMinValue = (ushort)0;

    public const ushort EventTimeDeltaMaxValue = (ushort)65534;

    public ushort EventTimeDelta
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


    public const int MatchEventIndicatorId = 5799;

    public static string MatchEventIndicatorMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public MatchEventIndicator MatchEventIndicator
    {
        get
        {
            return (MatchEventIndicator)_buffer.CharGet(_offset + 10);
        }
        set
        {
            _buffer.CharPut(_offset + 10, (byte)value);
        }
    }


    private readonly MdIncGrpGroup _mdIncGrp = new MdIncGrpGroup();

    public const long MdIncGrpId = 268;


    public MdIncGrpGroup MdIncGrp
    {
        get
        {
            _mdIncGrp.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _mdIncGrp;
        }
    }

    public MdIncGrpGroup MdIncGrpCount(int count)
    {
        _mdIncGrp.WrapForEncode(_parentMessage, _buffer, count);
        return _mdIncGrp;
    }

    public sealed partial class MdIncGrpGroup
    {
        private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
        private MarketDataIncrementalRefreshTrades _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MarketDataIncrementalRefreshTrades parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MarketDataIncrementalRefreshTrades parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.BlockLength = (ushort)34;
            _dimensions.NumInGroup = (byte)count;
            _index = -1;
            _count = count;
            _blockLength = 34;
            parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
        }

        public const int SbeBlockLength = 34;
        public const int SbeHeaderSize = 3;
        public int ActingBlockLength { get { return _blockLength; } }

        public int Count { get { return _count; } }

        public bool HasNext { get { return (_index + 1) < _count; } }

        public MdIncGrpGroup Next()
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

        public const int TradeIdId = 1003;

        public static string TradeIdMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong TradeIdNullValue = 0xffffffffffffffffUL;

        public const ulong TradeIdMinValue = 0x0UL;

        public const ulong TradeIdMaxValue = 0xfffffffffffffffeUL;

        public ulong TradeId
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


        public const int SecurityIdId = 48;

        public static string SecurityIdMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong SecurityIdNullValue = 0xffffffffffffffffUL;

        public const ulong SecurityIdMinValue = 0x0UL;

        public const ulong SecurityIdMaxValue = 0xfffffffffffffffeUL;

        public ulong SecurityId
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 8);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 8, value);
            }
        }


        public const int MdEntryPxId = 270;

        public static string MdEntryPxMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        private readonly Decimal64 _mdEntryPx = new Decimal64();

        public Decimal64 MdEntryPx
        {
            get
            {
                _mdEntryPx.Wrap(_buffer, _offset + 16, _actingVersion);
                return _mdEntryPx;
            }
        }

        public const int MdEntrySizeId = 271;

        public static string MdEntrySizeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        private readonly IntQty32 _mdEntrySize = new IntQty32();

        public IntQty32 MdEntrySize
        {
            get
            {
                _mdEntrySize.Wrap(_buffer, _offset + 24, _actingVersion);
                return _mdEntrySize;
            }
        }

        public const int NumberOfOrdersId = 346;

        public static string NumberOfOrdersMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ushort NumberOfOrdersNullValue = (ushort)65535;

        public const ushort NumberOfOrdersMinValue = (ushort)0;

        public const ushort NumberOfOrdersMaxValue = (ushort)65534;

        public ushort NumberOfOrders
        {
            get
            {
                return _buffer.Uint16GetLittleEndian(_offset + 28);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 28, value);
            }
        }


        public const int MdUpdateActionId = 279;

        public static string MdUpdateActionMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public MDUpdateAction MdUpdateAction
        {
            get
            {
                return (MDUpdateAction)_buffer.Uint8Get(_offset + 30);
            }
            set
            {
                _buffer.Uint8Put(_offset + 30, (byte)value);
            }
        }


        public const int RptSeqId = 83;

        public static string RptSeqMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const byte RptSeqNullValue = (byte)255;

        public const byte RptSeqMinValue = (byte)0;

        public const byte RptSeqMaxValue = (byte)254;

        public byte RptSeq
        {
            get
            {
                return _buffer.Uint8Get(_offset + 31);
            }
            set
            {
                _buffer.Uint8Put(_offset + 31, value);
            }
        }


        public const int AggressorSideId = 5797;

        public static string AggressorSideMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public Side AggressorSide
        {
            get
            {
                return (Side)_buffer.CharGet(_offset + 32);
            }
            set
            {
                _buffer.CharPut(_offset + 32, (byte)value);
            }
        }


        public const int MdEntryTypeId = 269;

        public static string MdEntryTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public MDEntryType MdEntryType
        {
            get
            {
                return (MDEntryType)_buffer.CharGet(_offset + 33);
            }
            set
            {
                _buffer.CharPut(_offset + 33, (byte)value);
            }
        }

    }
    }
}
