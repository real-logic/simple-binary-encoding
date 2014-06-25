/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class QuoteRequest
    {
    public const ushort TemplateId = (ushort)11;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)31;
    public const string SematicType = "R";

    private readonly QuoteRequest _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public QuoteRequest()
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


    public const int QuoteReqIDSchemaId = 131;

    public static string QuoteReqIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte QuoteReqIDNullValue = (byte)0;

    public const byte QuoteReqIDMinValue = (byte)32;

    public const byte QuoteReqIDMaxValue = (byte)126;

    public const int QuoteReqIDLength  = 23;

    public byte GetQuoteReqID(int index)
    {
        if (index < 0 || index >= 23)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 8 + (index * 1));
    }

    public void SetQuoteReqID(int index, byte value)
    {
        if (index < 0 || index >= 23)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 8 + (index * 1), value);
    }

    public const string QuoteReqIDCharacterEncoding = "UTF-8";

    public int GetQuoteReqID(byte[] dst, int dstOffset)
    {
        const int length = 23;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 8, dst, dstOffset, length);
        return length;
    }

    public void SetQuoteReqID(byte[] src, int srcOffset)
    {
        const int length = 23;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 8, src, srcOffset, length);
    }

    private readonly NoRelatedSymGroup _noRelatedSym = new NoRelatedSymGroup();

    public const long NoRelatedSymSchemaId = 146;


    public NoRelatedSymGroup NoRelatedSym
    {
        get
        {
            _noRelatedSym.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noRelatedSym;
        }
    }

    public NoRelatedSymGroup NoRelatedSymCount(int count)
    {
        _noRelatedSym.WrapForEncode(_parentMessage, _buffer, count);
        return _noRelatedSym;
    }

    public class NoRelatedSymGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private QuoteRequest _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(QuoteRequest parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(QuoteRequest parentMessage, DirectBuffer buffer, int count)
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

        public NoRelatedSymGroup Next()
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

        public const int SymbolSchemaId = 55;

        public static string SymbolMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "String";
            }

            return "";
        }

        public const byte SymbolNullValue = (byte)0;

        public const byte SymbolMinValue = (byte)32;

        public const byte SymbolMaxValue = (byte)126;

        public const int SymbolLength  = 20;

        public byte GetSymbol(int index)
        {
            if (index < 0 || index >= 20)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.CharGet(_offset + 0 + (index * 1));
        }

        public void SetSymbol(int index, byte value)
        {
            if (index < 0 || index >= 20)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.CharPut(_offset + 0 + (index * 1), value);
        }

    public const string SymbolCharacterEncoding = "UTF-8";

        public int GetSymbol(byte[] dst, int dstOffset)
        {
            const int length = 20;
            if (dstOffset < 0 || dstOffset > (dst.Length - length))
            {
                throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
            }

            _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
            return length;
        }

        public void SetSymbol(byte[] src, int srcOffset)
        {
            const int length = 20;
            if (srcOffset < 0 || srcOffset > (src.Length - length))
            {
                throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
            }

            _buffer.SetBytes(_offset + 0, src, srcOffset, length);
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
                return _buffer.Int32GetLittleEndian(_offset + 20);
            }
            set
            {
                _buffer.Int32PutLittleEndian(_offset + 20, value);
            }
        }


        public const int QuoteTypeSchemaId = 537;

        public static string QuoteTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte QuoteTypeNullValue = (sbyte)-128;

        public const sbyte QuoteTypeMinValue = (sbyte)-127;

        public const sbyte QuoteTypeMaxValue = (sbyte)127;

        public sbyte QuoteType
        {
            get
            {
                return _buffer.Int8Get(_offset + 24);
            }
            set
            {
                _buffer.Int8Put(_offset + 24, value);
            }
        }


        public const int OrderQtySchemaId = 38;

        public static string OrderQtyMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "Qty";
            }

            return "";
        }

        public const uint OrderQtyNullValue = 4294967294U;

        public const uint OrderQtyMinValue = 0U;

        public const uint OrderQtyMaxValue = 4294967293U;

        public uint OrderQty
        {
            get
            {
                return _buffer.Uint32GetLittleEndian(_offset + 25);
            }
            set
            {
                _buffer.Uint32PutLittleEndian(_offset + 25, value);
            }
        }


        public const int SideSchemaId = 54;

        public static string SideMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte SideNullValue = (sbyte)127;

        public const sbyte SideMinValue = (sbyte)-127;

        public const sbyte SideMaxValue = (sbyte)127;

        public sbyte Side
        {
            get
            {
                return _buffer.Int8Get(_offset + 29);
            }
            set
            {
                _buffer.Int8Put(_offset + 29, value);
            }
        }

    }
    }
}
