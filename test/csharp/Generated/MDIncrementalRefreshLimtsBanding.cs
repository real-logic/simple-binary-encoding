/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class MDIncrementalRefreshLimtsBanding
    {
    public const ushort TemplateId = (ushort)8;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)9;
    public const string SematicType = "X";

    private readonly MDIncrementalRefreshLimtsBanding _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MDIncrementalRefreshLimtsBanding()
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
        private MDIncrementalRefreshLimtsBanding _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDIncrementalRefreshLimtsBanding parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDIncrementalRefreshLimtsBanding parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)32;
            _index = -1;
            _count = count;
            _blockLength = 32;
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

        public const sbyte MDUpdateActionNullValue = (sbyte)-128;

        public const sbyte MDUpdateActionMinValue = (sbyte)-127;

        public const sbyte MDUpdateActionMaxValue = (sbyte)127;

        public sbyte MDUpdateAction { get { return (sbyte)0; } }

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

        private static readonly byte[] _MDEntryTypeValue = {103};

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
                return _buffer.Int32GetLittleEndian(_offset + 0);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 0, value);
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
                return _buffer.Int32GetLittleEndian(_offset + 4);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 4, value);
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
                _lowLimitPrice.Wrap(_buffer, _offset + 8, _actingVersion);
                return _lowLimitPrice;
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
                _highLimitPrice.Wrap(_buffer, _offset + 16, _actingVersion);
                return _highLimitPrice;
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
                _maxPriceVariation.Wrap(_buffer, _offset + 24, _actingVersion);
                return _maxPriceVariation;
            }
        }
    }
    }
}
