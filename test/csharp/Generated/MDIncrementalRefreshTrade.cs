/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class MDIncrementalRefreshTrade
    {
    public const ushort TemplateId = (ushort)23;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)9;
    public const string SematicType = "X";

    private readonly MDIncrementalRefreshTrade _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MDIncrementalRefreshTrade()
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
        private MDIncrementalRefreshTrade _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDIncrementalRefreshTrade parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDIncrementalRefreshTrade parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)30;
            _index = -1;
            _count = count;
            _blockLength = 30;
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

        public const byte MDEntryTypeNullValue = (byte)0;

        public const byte MDEntryTypeMinValue = (byte)32;

        public const byte MDEntryTypeMaxValue = (byte)126;

        private static readonly byte[] _MDEntryTypeValue = {50};

        public const int MDEntryTypeLength = 1;
        public byte MDEntryType(int index)
        {
            return _MDEntryTypeValue[index];
        }

        public int GetMDEntryType(byte[] dst, int offset, int length)
        {
            int bytesCopied = Math.Min(length, 1);
            Array.Copy(_MDEntryTypeValue, 0, dst, offset, bytesCopied);
            return bytesCopied;
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
                return _buffer.Int32GetLittleEndian(_offset + 1);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 1, value);
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
                return _buffer.Int32GetLittleEndian(_offset + 5);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 5, value);
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

        private readonly PRICE _mDEntryPx = new PRICE();

        public PRICE MDEntryPx
        {
            get
            {
                _mDEntryPx.Wrap(_buffer, _offset + 9, _actingVersion);
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

        public const int MDEntrySizeNullValue = -2147483648;

        public const int MDEntrySizeMinValue = -2147483647;

        public const int MDEntrySizeMaxValue = 2147483647;

        public int MDEntrySize
        {
            get
            {
                return _buffer.Int32GetLittleEndian(_offset + 17);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 17, value);
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
                return _buffer.Int32GetLittleEndian(_offset + 21);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 21, value);
            }
        }


        public const int TradeIDSchemaId = 1003;

        public static string TradeIDMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const int TradeIDNullValue = -2147483648;

        public const int TradeIDMinValue = -2147483647;

        public const int TradeIDMaxValue = 2147483647;

        public int TradeID
        {
            get
            {
                return _buffer.Int32GetLittleEndian(_offset + 25);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 25, value);
            }
        }


        public const int AggressorSideSchemaId = 5797;

        public static string AggressorSideMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public AggressorSide AggressorSide
        {
            get
            {
                return (AggressorSide)_buffer.Uint8Get(_offset + 29);
            }
            set
            {
                _buffer.Uint8Put(_offset + 29, (byte)value);
            }
        }

    }
    }
}
