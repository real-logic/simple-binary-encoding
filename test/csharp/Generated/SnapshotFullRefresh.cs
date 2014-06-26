/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class SnapshotFullRefresh
    {
    public const ushort TemplateId = (ushort)25;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)59;
    public const string SematicType = "W";

    private readonly SnapshotFullRefresh _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public SnapshotFullRefresh()
    {
        _parentMessage = this;
    }

    public void WrapForEncode(DirectBuffer buffer, int offset)
    {
        _buffer = buffer;
        _offset = offset;
        _actingBlockLength = BlockLength;
        _actingVersion = TemplateVersion;
        Limit = offset + _actingBlockLength;
    }

    public void WrapForDecode(DirectBuffer buffer, int offset,
                              int actingBlockLength, int actingVersion)
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


    public const int LastMsgSeqNumProcessedSchemaId = 369;

    public static string LastMsgSeqNumProcessedMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "SeqNum";
        }

        return "";
    }

    public const uint LastMsgSeqNumProcessedNullValue = 4294967294U;

    public const uint LastMsgSeqNumProcessedMinValue = 0U;

    public const uint LastMsgSeqNumProcessedMaxValue = 4294967293U;

    public uint LastMsgSeqNumProcessed
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


    public const int TotNumReportsSchemaId = 911;

    public static string TotNumReportsMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const uint TotNumReportsNullValue = 4294967294U;

    public const uint TotNumReportsMinValue = 0U;

    public const uint TotNumReportsMaxValue = 4294967293U;

    public uint TotNumReports
    {
        get
        {
            return _buffer.Uint32GetLittleEndian(_offset + 4);
        }
        set
        {
            _buffer.Uint32PutLittleEndian(_offset + 4, value);
        }
    }


    public const int SecurityIDSchemaId = 48;

    public static string SecurityIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const int SecurityIDNullValue = -2147483648;

    public const int SecurityIDMinValue = -2147483647;

    public const int SecurityIDMaxValue = 2147483647;

    public int SecurityID
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 8);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 8, value);
        }
    }


    public const int RptSeqSchemaId = 83;

    public static string RptSeqMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "SeqNum";
        }

        return "";
    }

    public const uint RptSeqNullValue = 4294967294U;

    public const uint RptSeqMinValue = 0U;

    public const uint RptSeqMaxValue = 4294967293U;

    public uint RptSeq
    {
        get
        {
            return _buffer.Uint32GetLittleEndian(_offset + 12);
        }
        set
        {
            _buffer.Uint32PutLittleEndian(_offset + 12, value);
        }
    }


    public const int TransactTimeSchemaId = 60;

    public static string TransactTimeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "UTCTimestamp";
        }

        return "";
    }

    public const ulong TransactTimeNullValue = 0x8000000000000000UL;

    public const ulong TransactTimeMinValue = 0x0UL;

    public const ulong TransactTimeMaxValue = 0x7fffffffffffffffUL;

    public ulong TransactTime
    {
        get
        {
            return _buffer.Uint64GetLittleEndian(_offset + 16);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 16, value);
        }
    }


    public const int LastUpdateTimeSchemaId = 779;

    public static string LastUpdateTimeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "UTCTimestamp";
        }

        return "";
    }

    public const ulong LastUpdateTimeNullValue = 0x8000000000000000UL;

    public const ulong LastUpdateTimeMinValue = 0x0UL;

    public const ulong LastUpdateTimeMaxValue = 0x7fffffffffffffffUL;

    public ulong LastUpdateTime
    {
        get
        {
            return _buffer.Uint64GetLittleEndian(_offset + 24);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 24, value);
        }
    }


    public const int TradeDateSchemaId = 75;

    public static string TradeDateMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "LocalMktDate";
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
            return _buffer.Uint16GetLittleEndian(_offset + 32);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 32, value);
        }
    }


    public const int MDSecurityTradingStatusSchemaId = 1682;

    public static string MDSecurityTradingStatusMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public SecurityTradingStatus MDSecurityTradingStatus
    {
        get
        {
            return (SecurityTradingStatus)_buffer.Uint8Get(_offset + 34);
        }
        set
        {
            _buffer.Uint8Put(_offset + 34, (byte)value);
        }
    }


    public const int HighLimitPriceSchemaId = 1149;

    public static string HighLimitPriceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICENULL _highLimitPrice = new PRICENULL();

    public PRICENULL HighLimitPrice
    {
        get
        {
            _highLimitPrice.Wrap(_buffer, _offset + 35, _actingVersion);
            return _highLimitPrice;
        }
    }

    public const int LowLimitPriceSchemaId = 1148;

    public static string LowLimitPriceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICENULL _lowLimitPrice = new PRICENULL();

    public PRICENULL LowLimitPrice
    {
        get
        {
            _lowLimitPrice.Wrap(_buffer, _offset + 43, _actingVersion);
            return _lowLimitPrice;
        }
    }

    public const int MaxPriceVariationSchemaId = 1143;

    public static string MaxPriceVariationMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICENULL _maxPriceVariation = new PRICENULL();

    public PRICENULL MaxPriceVariation
    {
        get
        {
            _maxPriceVariation.Wrap(_buffer, _offset + 51, _actingVersion);
            return _maxPriceVariation;
        }
    }

    private readonly NoMDEntriesGroup _noMDEntries = new NoMDEntriesGroup();

    public const long NoMDEntriesSchemaId = 268;


    public NoMDEntriesGroup NoMDEntries
    {
        get
        {
            _noMDEntries.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noMDEntries;
        }
    }

    public NoMDEntriesGroup NoMDEntriesCount(int count)
    {
        _noMDEntries.WrapForEncode(_parentMessage, _buffer, count);
        return _noMDEntries;
    }

    public class NoMDEntriesGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private SnapshotFullRefresh _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(SnapshotFullRefresh parentMessage, DirectBuffer buffer, int actingVersion)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);
            _count = _dimensions.NumInGroup;
            _blockLength = _dimensions.BlockLength;
            _actingVersion = actingVersion;
            _index = -1;
            _parentMessage.Limit = parentMessage.Limit + 3;
        }

        public void WrapForEncode(SnapshotFullRefresh parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)22;
            _index = -1;
            _count = count;
            _blockLength = 22;
            parentMessage.Limit = parentMessage.Limit + 3;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public NoMDEntriesGroup Next()
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

        public const int MDEntryTypeSchemaId = 269;

        public static string MDEntryTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "char";
            }

            return "";
        }

        public MDEntryType MDEntryType
        {
            get
            {
                return (MDEntryType)_buffer.CharGet(_offset + 0);
            }
            set
            {
                _buffer.CharPut(_offset + 0, (byte)value);
            }
        }


        public const int MDPriceLevelSchemaId = 1023;

        public static string MDPriceLevelMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte MDPriceLevelNullValue = (sbyte)127;

        public const sbyte MDPriceLevelMinValue = (sbyte)-127;

        public const sbyte MDPriceLevelMaxValue = (sbyte)127;

        public sbyte MDPriceLevel
        {
            get
            {
                return _buffer.Int8Get(_offset + 1);
            }
            set
            {
                _buffer.Int8Put(_offset + 1, value);
            }
        }


        public const int MDEntryPxSchemaId = 270;

        public static string MDEntryPxMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "Price";
            }

            return "";
        }

        private readonly PRICENULL _mDEntryPx = new PRICENULL();

        public PRICENULL MDEntryPx
        {
            get
            {
                _mDEntryPx.Wrap(_buffer, _offset + 2, _actingVersion);
                return _mDEntryPx;
            }
        }

        public const int MDEntrySizeSchemaId = 271;

        public static string MDEntrySizeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "Qty";
            }

            return "";
        }

        public const int MDEntrySizeNullValue = 2147483647;

        public const int MDEntrySizeMinValue = -2147483647;

        public const int MDEntrySizeMaxValue = 2147483647;

        public int MDEntrySize
        {
            get
            {
                return _buffer.Int32GetLittleEndian(_offset + 10);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 10, value);
            }
        }


        public const int NumberOfOrdersSchemaId = 346;

        public static string NumberOfOrdersMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const int NumberOfOrdersNullValue = 2147483647;

        public const int NumberOfOrdersMinValue = -2147483647;

        public const int NumberOfOrdersMaxValue = 2147483647;

        public int NumberOfOrders
        {
            get
            {
                return _buffer.Int32GetLittleEndian(_offset + 14);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 14, value);
            }
        }


        public const int OpenCloseSettlFlagSchemaId = 286;

        public static string OpenCloseSettlFlagMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public OpenCloseSettlFlag OpenCloseSettlFlag
        {
            get
            {
                return (OpenCloseSettlFlag)_buffer.Uint8Get(_offset + 18);
            }
            set
            {
                _buffer.Uint8Put(_offset + 18, (byte)value);
            }
        }


        public const int SettlPriceTypeSchemaId = 731;

        public static string SettlPriceTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "MultipleCharValue";
            }

            return "";
        }

        public SettlPriceType SettlPriceType
        {
            get
            {
                return (SettlPriceType)_buffer.Uint8Get(_offset + 19);
            }
            set
            {
                _buffer.Uint8Put(_offset + 19, (byte)value);
            }
        }

        public const int TradingReferenceDateSchemaId = 5796;

        public static string TradingReferenceDateMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "LocalMktDate";
            }

            return "";
        }

        public const ushort TradingReferenceDateNullValue = (ushort)65535;

        public const ushort TradingReferenceDateMinValue = (ushort)0;

        public const ushort TradingReferenceDateMaxValue = (ushort)65534;

        public ushort TradingReferenceDate
        {
            get
            {
                return _buffer.Uint16GetLittleEndian(_offset + 20);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 20, value);
            }
        }

    }
    }
}
