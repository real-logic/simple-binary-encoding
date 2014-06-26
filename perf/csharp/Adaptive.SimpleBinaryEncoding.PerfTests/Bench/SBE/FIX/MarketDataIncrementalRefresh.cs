/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class MarketDataIncrementalRefresh
    {
    public const ushort BlockLength = (ushort)2;
    public const ushort TemplateId = (ushort)88;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "X";

    private readonly MarketDataIncrementalRefresh _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MarketDataIncrementalRefresh()
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


    public const int TradeDateId = 75;

    public static string TradeDateMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const ushort TradeDateNullValue = (ushort)65535;

    public const ushort TradeDateMinValue = (ushort)0;

    public const ushort TradeDateMaxValue = (ushort)65534;

    public ushort TradeDate
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


    private readonly EntriesGroup _entries = new EntriesGroup();

    public const long EntriesId = 268;


    public EntriesGroup Entries
    {
        get
        {
            _entries.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _entries;
        }
    }

    public EntriesGroup EntriesCount(int count)
    {
        _entries.WrapForEncode(_parentMessage, _buffer, count);
        return _entries;
    }

    public sealed partial class EntriesGroup
    {
        private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
        private MarketDataIncrementalRefresh _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MarketDataIncrementalRefresh parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MarketDataIncrementalRefresh parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.BlockLength = (ushort)82;
            _dimensions.NumInGroup = (byte)count;
            _index = -1;
            _count = count;
            _blockLength = 82;
            parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
        }

        public const int SbeBlockLength = 82;
        public const int SbeHeaderSize = 3;
        public int ActingBlockLength { get { return _blockLength; } }

        public int Count { get { return _count; } }

        public bool HasNext { get { return (_index + 1) < _count; } }

        public EntriesGroup Next()
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
                return (MDUpdateAction)_buffer.Uint8Get(_offset + 0);
            }
            set
            {
                _buffer.Uint8Put(_offset + 0, (byte)value);
            }
        }


        public const int MdPriceLevelId = 1023;

        public static string MdPriceLevelMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "MDPriceLevel";
            }

            return "";
        }

        public const byte MdPriceLevelNullValue = (byte)255;

        public const byte MdPriceLevelMinValue = (byte)0;

        public const byte MdPriceLevelMaxValue = (byte)254;

        public byte MdPriceLevel
        {
            get
            {
                return _buffer.Uint8Get(_offset + 1);
            }
            set
            {
                _buffer.Uint8Put(_offset + 1, value);
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
                return (MDEntryType)_buffer.CharGet(_offset + 2);
            }
            set
            {
                _buffer.CharPut(_offset + 2, (byte)value);
            }
        }


        public const int SecurityIdSourceId = 22;

        public static string SecurityIdSourceMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "SecurityID";
            }

            return "";
        }

        public const byte SecurityIdSourceNullValue = (byte)0;

        public const byte SecurityIdSourceMinValue = (byte)32;

        public const byte SecurityIdSourceMaxValue = (byte)126;

        public byte SecurityIdSource
        {
            get
            {
                return _buffer.CharGet(_offset + 3);
            }
            set
            {
                _buffer.CharPut(_offset + 3, value);
            }
        }


        public const int SecurityIdId = 48;

        public static string SecurityIdMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "InstrumentID";
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
                return _buffer.Uint64GetLittleEndian(_offset + 4);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 4, value);
            }
        }


        public const int RptSeqId = 83;

        public static string RptSeqMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "SequenceNumber";
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
                return _buffer.Uint8Get(_offset + 12);
            }
            set
            {
                _buffer.Uint8Put(_offset + 12, value);
            }
        }


        public const int QuoteConditionId = 276;

        public static string QuoteConditionMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public QuoteCondition QuoteCondition
        {
            get
            {
                return (QuoteCondition)_buffer.Uint8Get(_offset + 13);
            }
            set
            {
                _buffer.Uint8Put(_offset + 13, (byte)value);
            }
        }

        public const int MdEntryPxId = 270;

        public static string MdEntryPxMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "Price";
            }

            return "";
        }

        private readonly Decimal64 _mdEntryPx = new Decimal64();

        public Decimal64 MdEntryPx
        {
            get
            {
                _mdEntryPx.Wrap(_buffer, _offset + 14, _actingVersion);
                return _mdEntryPx;
            }
        }

        public const int NumberOfOrdersId = 346;

        public static string NumberOfOrdersMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "NumberOfOrders";
            }

            return "";
        }

        public const uint NumberOfOrdersNullValue = 4294967294U;

        public const uint NumberOfOrdersMinValue = 0U;

        public const uint NumberOfOrdersMaxValue = 4294967293U;

        public uint NumberOfOrders
        {
            get
            {
                return _buffer.Uint32GetLittleEndian(_offset + 22);
            }
            set
            {
                _buffer.Uint32PutLittleEndian(_offset + 22, value);
            }
        }


        public const int MdEntryTimeId = 273;

        public static string MdEntryTimeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong MdEntryTimeNullValue = 0xffffffffffffffffUL;

        public const ulong MdEntryTimeMinValue = 0x0UL;

        public const ulong MdEntryTimeMaxValue = 0xfffffffffffffffeUL;

        public ulong MdEntryTime
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 26);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 26, value);
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
                _mdEntrySize.Wrap(_buffer, _offset + 34, _actingVersion);
                return _mdEntrySize;
            }
        }

        public const int TradingSessionIdId = 336;

        public static string TradingSessionIdMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public MarketStateIdentifier TradingSessionId
        {
            get
            {
                return (MarketStateIdentifier)_buffer.Uint8Get(_offset + 38);
            }
            set
            {
                _buffer.Uint8Put(_offset + 38, (byte)value);
            }
        }


        public const int NetChgPrevDayId = 451;

        public static string NetChgPrevDayMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        private readonly Decimal64 _netChgPrevDay = new Decimal64();

        public Decimal64 NetChgPrevDay
        {
            get
            {
                _netChgPrevDay.Wrap(_buffer, _offset + 39, _actingVersion);
                return _netChgPrevDay;
            }
        }

        public const int TickDirectionId = 274;

        public static string TickDirectionMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public TickDirection TickDirection
        {
            get
            {
                return (TickDirection)_buffer.Uint8Get(_offset + 47);
            }
            set
            {
                _buffer.Uint8Put(_offset + 47, (byte)value);
            }
        }


        public const int OpenCloseSettleFlagId = 286;

        public static string OpenCloseSettleFlagMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public OpenCloseSettleFlag OpenCloseSettleFlag
        {
            get
            {
                return (OpenCloseSettleFlag)_buffer.Uint16GetLittleEndian(_offset + 48);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 48, (ushort)value);
            }
        }


        public const int SettleDateId = 64;

        public static string SettleDateMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong SettleDateNullValue = 0xffffffffffffffffUL;

        public const ulong SettleDateMinValue = 0x0UL;

        public const ulong SettleDateMaxValue = 0xfffffffffffffffeUL;

        public ulong SettleDate
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 50);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 50, value);
            }
        }


        public const int TradeConditionId = 277;

        public static string TradeConditionMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public TradeCondition TradeCondition
        {
            get
            {
                return (TradeCondition)_buffer.Uint8Get(_offset + 58);
            }
            set
            {
                _buffer.Uint8Put(_offset + 58, (byte)value);
            }
        }

        public const int TradeVolumeId = 1020;

        public static string TradeVolumeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        private readonly IntQty32 _tradeVolume = new IntQty32();

        public IntQty32 TradeVolume
        {
            get
            {
                _tradeVolume.Wrap(_buffer, _offset + 59, _actingVersion);
                return _tradeVolume;
            }
        }

        public const int MdQuoteTypeId = 1070;

        public static string MdQuoteTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public MDQuoteType MdQuoteType
        {
            get
            {
                return (MDQuoteType)_buffer.Uint8Get(_offset + 63);
            }
            set
            {
                _buffer.Uint8Put(_offset + 63, (byte)value);
            }
        }


        public const int FixingBracketId = 5790;

        public static string FixingBracketMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "";
            }

            return "";
        }

        public const ulong FixingBracketNullValue = 0xffffffffffffffffUL;

        public const ulong FixingBracketMinValue = 0x0UL;

        public const ulong FixingBracketMaxValue = 0xfffffffffffffffeUL;

        public ulong FixingBracket
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 64);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 64, value);
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
                return (Side)_buffer.CharGet(_offset + 72);
            }
            set
            {
                _buffer.CharPut(_offset + 72, (byte)value);
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
                return (MatchEventIndicator)_buffer.CharGet(_offset + 73);
            }
            set
            {
                _buffer.CharPut(_offset + 73, (byte)value);
            }
        }


        public const int TradeIdId = 1003;

        public static string TradeIdMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "ExecID";
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
                return _buffer.Uint64GetLittleEndian(_offset + 74);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 74, value);
            }
        }

    }
    }
}
