/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class MDIncrementalRefreshDailyStatistics
    {
    public const ushort TemplateId = (ushort)21;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)9;
    public const string SematicType = "X";

    private readonly MDIncrementalRefreshDailyStatistics _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MDIncrementalRefreshDailyStatistics()
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
            return _buffer.Uint64GetLittleEndian(_offset + 0);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 0, value);
        }
    }


    public const int MatchEventIndicatorSchemaId = 5799;

    public static string MatchEventIndicatorMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "MultipleCharValue";
        }

        return "";
    }

    public MatchEventIndicator MatchEventIndicator
    {
        get
        {
            return (MatchEventIndicator)_buffer.Uint8Get(_offset + 8);
        }
        set
        {
            _buffer.Uint8Put(_offset + 8, (byte)value);
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
        private MDIncrementalRefreshDailyStatistics _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDIncrementalRefreshDailyStatistics parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDIncrementalRefreshDailyStatistics parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)25;
            _index = -1;
            _count = count;
            _blockLength = 25;
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

        public const int MDUpdateActionSchemaId = 279;

        public static string MDUpdateActionMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public MDUpdateAction MDUpdateAction
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

        public MDEntryTypeDailyStatistics MDEntryType
        {
            get
            {
                return (MDEntryTypeDailyStatistics)_buffer.CharGet(_offset + 1);
            }
            set
            {
                _buffer.CharPut(_offset + 1, (byte)value);
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
                return _buffer.Int32GetLittleEndian(_offset + 2);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 2, value);
            }
        }


        public const int RptSeqSchemaId = 83;

        public static string RptSeqMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const int RptSeqNullValue = -2147483648;

        public const int RptSeqMinValue = -2147483647;

        public const int RptSeqMaxValue = 2147483647;

        public int RptSeq
        {
            get
            {
                return _buffer.Int32GetLittleEndian(_offset + 6);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 6, value);
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
                _mDEntryPx.Wrap(_buffer, _offset + 10, _actingVersion);
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
                return _buffer.Int32GetLittleEndian(_offset + 18);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 18, value);
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
                return (SettlPriceType)_buffer.Uint8Get(_offset + 22);
            }
            set
            {
                _buffer.Uint8Put(_offset + 22, (byte)value);
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
                return _buffer.Uint16GetLittleEndian(_offset + 23);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 23, value);
            }
        }

    }
    }
}
