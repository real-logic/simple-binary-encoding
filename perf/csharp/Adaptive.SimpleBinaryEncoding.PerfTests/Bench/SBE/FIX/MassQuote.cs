/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class MassQuote
    {
    public const ushort BlockLength = (ushort)62;
    public const ushort TemplateId = (ushort)105;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "i";

    private readonly MassQuote _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MassQuote()
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


    public const int QuoteReqIDId = 131;

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

        return _buffer.CharGet(_offset + 0 + (index * 1));
    }

    public void SetQuoteReqID(int index, byte value)
    {
        if (index < 0 || index >= 23)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 0 + (index * 1), value);
    }

    public const string QuoteReqIDCharacterEncoding = "UTF-8";

    public int GetQuoteReqID(byte[] dst, int dstOffset)
    {
        const int length = 23;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
        return length;
    }

    public void SetQuoteReqID(byte[] src, int srcOffset)
    {
        const int length = 23;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 0, src, srcOffset, length);
    }

    public const int QuoteIDId = 117;

    public static string QuoteIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte QuoteIDNullValue = (byte)0;

    public const byte QuoteIDMinValue = (byte)32;

    public const byte QuoteIDMaxValue = (byte)126;

    public const int QuoteIDLength  = 10;

    public byte GetQuoteID(int index)
    {
        if (index < 0 || index >= 10)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 23 + (index * 1));
    }

    public void SetQuoteID(int index, byte value)
    {
        if (index < 0 || index >= 10)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 23 + (index * 1), value);
    }

    public const string QuoteIDCharacterEncoding = "UTF-8";

    public int GetQuoteID(byte[] dst, int dstOffset)
    {
        const int length = 10;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 23, dst, dstOffset, length);
        return length;
    }

    public void SetQuoteID(byte[] src, int srcOffset)
    {
        const int length = 10;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 23, src, srcOffset, length);
    }

    public const int MMAccountId = 9771;

    public static string MMAccountMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte MMAccountNullValue = (byte)0;

    public const byte MMAccountMinValue = (byte)32;

    public const byte MMAccountMaxValue = (byte)126;

    public const int MMAccountLength  = 12;

    public byte GetMMAccount(int index)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 33 + (index * 1));
    }

    public void SetMMAccount(int index, byte value)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 33 + (index * 1), value);
    }

    public const string MMAccountCharacterEncoding = "UTF-8";

    public int GetMMAccount(byte[] dst, int dstOffset)
    {
        const int length = 12;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 33, dst, dstOffset, length);
        return length;
    }

    public void SetMMAccount(byte[] src, int srcOffset)
    {
        const int length = 12;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 33, src, srcOffset, length);
    }

    public const int ManualOrderIndicatorId = 1028;

    public static string ManualOrderIndicatorMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public BooleanType ManualOrderIndicator
    {
        get
        {
            return (BooleanType)_buffer.Uint8Get(_offset + 45);
        }
        set
        {
            _buffer.Uint8Put(_offset + 45, (byte)value);
        }
    }


    public const int CustOrderHandlingInstId = 1031;

    public static string CustOrderHandlingInstMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public CustOrderHandlingInst CustOrderHandlingInst
    {
        get
        {
            return (CustOrderHandlingInst)_buffer.CharGet(_offset + 46);
        }
        set
        {
            _buffer.CharPut(_offset + 46, (byte)value);
        }
    }


    public const int CustomerOrFirmId = 204;

    public static string CustomerOrFirmMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public CustomerOrFirm CustomerOrFirm
    {
        get
        {
            return (CustomerOrFirm)_buffer.Uint8Get(_offset + 47);
        }
        set
        {
            _buffer.Uint8Put(_offset + 47, (byte)value);
        }
    }


    public const int SelfMatchPreventionIDId = 7928;

    public static string SelfMatchPreventionIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte SelfMatchPreventionIDNullValue = (byte)0;

    public const byte SelfMatchPreventionIDMinValue = (byte)32;

    public const byte SelfMatchPreventionIDMaxValue = (byte)126;

    public const int SelfMatchPreventionIDLength  = 12;

    public byte GetSelfMatchPreventionID(int index)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 48 + (index * 1));
    }

    public void SetSelfMatchPreventionID(int index, byte value)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 48 + (index * 1), value);
    }

    public const string SelfMatchPreventionIDCharacterEncoding = "UTF-8";

    public int GetSelfMatchPreventionID(byte[] dst, int dstOffset)
    {
        const int length = 12;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 48, dst, dstOffset, length);
        return length;
    }

    public void SetSelfMatchPreventionID(byte[] src, int srcOffset)
    {
        const int length = 12;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 48, src, srcOffset, length);
    }

    public const int CtiCodeId = 9702;

    public static string CtiCodeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public CtiCode CtiCode
    {
        get
        {
            return (CtiCode)_buffer.CharGet(_offset + 60);
        }
        set
        {
            _buffer.CharPut(_offset + 60, (byte)value);
        }
    }


    public const int MMProtectionResetId = 9773;

    public static string MMProtectionResetMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public MMProtectionReset MMProtectionReset
    {
        get
        {
            return (MMProtectionReset)_buffer.CharGet(_offset + 61);
        }
        set
        {
            _buffer.CharPut(_offset + 61, (byte)value);
        }
    }


    private readonly QuoteSetsGroup _quoteSets = new QuoteSetsGroup();

    public const long QuoteSetsId = 296;


    public QuoteSetsGroup QuoteSets
    {
        get
        {
            _quoteSets.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _quoteSets;
        }
    }

    public QuoteSetsGroup QuoteSetsCount(int count)
    {
        _quoteSets.WrapForEncode(_parentMessage, _buffer, count);
        return _quoteSets;
    }

    public sealed partial class QuoteSetsGroup
    {
        private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
        private MassQuote _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MassQuote parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MassQuote parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.BlockLength = (ushort)24;
            _dimensions.NumInGroup = (byte)count;
            _index = -1;
            _count = count;
            _blockLength = 24;
            parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
        }

        public const int SbeBlockLength = 24;
        public const int SbeHeaderSize = 3;
        public int ActingBlockLength { get { return _blockLength; } }

        public int Count { get { return _count; } }

        public bool HasNext { get { return (_index + 1) < _count; } }

        public QuoteSetsGroup Next()
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

        public const int QuoteSetIDId = 302;

        public static string QuoteSetIDMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "String";
            }

            return "";
        }

        public const byte QuoteSetIDNullValue = (byte)0;

        public const byte QuoteSetIDMinValue = (byte)32;

        public const byte QuoteSetIDMaxValue = (byte)126;

        public const int QuoteSetIDLength  = 3;

        public byte GetQuoteSetID(int index)
        {
            if (index < 0 || index >= 3)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.CharGet(_offset + 0 + (index * 1));
        }

        public void SetQuoteSetID(int index, byte value)
        {
            if (index < 0 || index >= 3)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.CharPut(_offset + 0 + (index * 1), value);
        }

    public const string QuoteSetIDCharacterEncoding = "UTF-8";

        public int GetQuoteSetID(byte[] dst, int dstOffset)
        {
            const int length = 3;
            if (dstOffset < 0 || dstOffset > (dst.Length - length))
            {
                throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
            }

            _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
            return length;
        }

        public void SetQuoteSetID(byte[] src, int srcOffset)
        {
            const int length = 3;
            if (srcOffset < 0 || srcOffset > (src.Length - length))
            {
                throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
            }

            _buffer.SetBytes(_offset + 0, src, srcOffset, length);
        }

        public const int UnderlyingSecurityDescId = 307;

        public static string UnderlyingSecurityDescMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "String";
            }

            return "";
        }

        public const byte UnderlyingSecurityDescNullValue = (byte)0;

        public const byte UnderlyingSecurityDescMinValue = (byte)32;

        public const byte UnderlyingSecurityDescMaxValue = (byte)126;

        public const int UnderlyingSecurityDescLength  = 20;

        public byte GetUnderlyingSecurityDesc(int index)
        {
            if (index < 0 || index >= 20)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.CharGet(_offset + 3 + (index * 1));
        }

        public void SetUnderlyingSecurityDesc(int index, byte value)
        {
            if (index < 0 || index >= 20)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.CharPut(_offset + 3 + (index * 1), value);
        }

    public const string UnderlyingSecurityDescCharacterEncoding = "UTF-8";

        public int GetUnderlyingSecurityDesc(byte[] dst, int dstOffset)
        {
            const int length = 20;
            if (dstOffset < 0 || dstOffset > (dst.Length - length))
            {
                throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
            }

            _buffer.GetBytes(_offset + 3, dst, dstOffset, length);
            return length;
        }

        public void SetUnderlyingSecurityDesc(byte[] src, int srcOffset)
        {
            const int length = 20;
            if (srcOffset < 0 || srcOffset > (src.Length - length))
            {
                throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
            }

            _buffer.SetBytes(_offset + 3, src, srcOffset, length);
        }

        public const int TotQuoteEntriesId = 304;

        public static string TotQuoteEntriesMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const byte TotQuoteEntriesNullValue = (byte)255;

        public const byte TotQuoteEntriesMinValue = (byte)0;

        public const byte TotQuoteEntriesMaxValue = (byte)254;

        public byte TotQuoteEntries
        {
            get
            {
                return _buffer.Uint8Get(_offset + 23);
            }
            set
            {
                _buffer.Uint8Put(_offset + 23, value);
            }
        }


        private readonly QuoteEntriesGroup _quoteEntries = new QuoteEntriesGroup();

        public const long QuoteEntriesId = 295;


        public QuoteEntriesGroup QuoteEntries
        {
            get
            {
                _quoteEntries.WrapForDecode(_parentMessage, _buffer, _actingVersion);
                return _quoteEntries;
            }
        }

        public QuoteEntriesGroup QuoteEntriesCount(int count)
        {
            _quoteEntries.WrapForEncode(_parentMessage, _buffer, count);
            return _quoteEntries;
        }

        public sealed partial class QuoteEntriesGroup
        {
            private readonly GroupSizeEncoding _dimensions = new GroupSizeEncoding();
            private MassQuote _parentMessage;
            private DirectBuffer _buffer;
            private int _blockLength;
            private int _actingVersion;
            private int _count;
            private int _index;
            private int _offset;

            public void WrapForDecode(MassQuote parentMessage, DirectBuffer buffer, int actingVersion)
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

            public void WrapForEncode(MassQuote parentMessage, DirectBuffer buffer, int count)
            {
                _parentMessage = parentMessage;
                _buffer = buffer;
                _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
                _dimensions.BlockLength = (ushort)90;
                _dimensions.NumInGroup = (byte)count;
                _index = -1;
                _count = count;
                _blockLength = 90;
                parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;
            }

            public const int SbeBlockLength = 90;
            public const int SbeHeaderSize = 3;
            public int ActingBlockLength { get { return _blockLength; } }

            public int Count { get { return _count; } }

            public bool HasNext { get { return (_index + 1) < _count; } }

            public QuoteEntriesGroup Next()
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

            public const int QuoteEntryIDId = 299;

            public static string QuoteEntryIDMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "String";
                }

                return "";
            }

            public const byte QuoteEntryIDNullValue = (byte)0;

            public const byte QuoteEntryIDMinValue = (byte)32;

            public const byte QuoteEntryIDMaxValue = (byte)126;

            public const int QuoteEntryIDLength  = 10;

            public byte GetQuoteEntryID(int index)
            {
                if (index < 0 || index >= 10)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                return _buffer.CharGet(_offset + 0 + (index * 1));
            }

            public void SetQuoteEntryID(int index, byte value)
            {
                if (index < 0 || index >= 10)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                _buffer.CharPut(_offset + 0 + (index * 1), value);
            }

    public const string QuoteEntryIDCharacterEncoding = "UTF-8";

            public int GetQuoteEntryID(byte[] dst, int dstOffset)
            {
                const int length = 10;
                if (dstOffset < 0 || dstOffset > (dst.Length - length))
                {
                    throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
                }

                _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
                return length;
            }

            public void SetQuoteEntryID(byte[] src, int srcOffset)
            {
                const int length = 10;
                if (srcOffset < 0 || srcOffset > (src.Length - length))
                {
                    throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
                }

                _buffer.SetBytes(_offset + 0, src, srcOffset, length);
            }

            public const int SymbolId = 55;

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

            public const int SymbolLength  = 6;

            public byte GetSymbol(int index)
            {
                if (index < 0 || index >= 6)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                return _buffer.CharGet(_offset + 10 + (index * 1));
            }

            public void SetSymbol(int index, byte value)
            {
                if (index < 0 || index >= 6)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                _buffer.CharPut(_offset + 10 + (index * 1), value);
            }

    public const string SymbolCharacterEncoding = "UTF-8";

            public int GetSymbol(byte[] dst, int dstOffset)
            {
                const int length = 6;
                if (dstOffset < 0 || dstOffset > (dst.Length - length))
                {
                    throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
                }

                _buffer.GetBytes(_offset + 10, dst, dstOffset, length);
                return length;
            }

            public void SetSymbol(byte[] src, int srcOffset)
            {
                const int length = 6;
                if (srcOffset < 0 || srcOffset > (src.Length - length))
                {
                    throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
                }

                _buffer.SetBytes(_offset + 10, src, srcOffset, length);
            }

            public const int SecurityDescId = 107;

            public static string SecurityDescMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "String";
                }

                return "";
            }

            public const byte SecurityDescNullValue = (byte)0;

            public const byte SecurityDescMinValue = (byte)32;

            public const byte SecurityDescMaxValue = (byte)126;

            public const int SecurityDescLength  = 20;

            public byte GetSecurityDesc(int index)
            {
                if (index < 0 || index >= 20)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                return _buffer.CharGet(_offset + 16 + (index * 1));
            }

            public void SetSecurityDesc(int index, byte value)
            {
                if (index < 0 || index >= 20)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                _buffer.CharPut(_offset + 16 + (index * 1), value);
            }

    public const string SecurityDescCharacterEncoding = "UTF-8";

            public int GetSecurityDesc(byte[] dst, int dstOffset)
            {
                const int length = 20;
                if (dstOffset < 0 || dstOffset > (dst.Length - length))
                {
                    throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
                }

                _buffer.GetBytes(_offset + 16, dst, dstOffset, length);
                return length;
            }

            public void SetSecurityDesc(byte[] src, int srcOffset)
            {
                const int length = 20;
                if (srcOffset < 0 || srcOffset > (src.Length - length))
                {
                    throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
                }

                _buffer.SetBytes(_offset + 16, src, srcOffset, length);
            }

            public const int SecurityTypeId = 167;

            public static string SecurityTypeMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "String";
                }

                return "";
            }

            public const byte SecurityTypeNullValue = (byte)0;

            public const byte SecurityTypeMinValue = (byte)32;

            public const byte SecurityTypeMaxValue = (byte)126;

            public const int SecurityTypeLength  = 3;

            public byte GetSecurityType(int index)
            {
                if (index < 0 || index >= 3)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                return _buffer.CharGet(_offset + 36 + (index * 1));
            }

            public void SetSecurityType(int index, byte value)
            {
                if (index < 0 || index >= 3)
                {
                    throw new IndexOutOfRangeException("index out of range: index=" + index);
                }

                _buffer.CharPut(_offset + 36 + (index * 1), value);
            }

    public const string SecurityTypeCharacterEncoding = "UTF-8";

            public int GetSecurityType(byte[] dst, int dstOffset)
            {
                const int length = 3;
                if (dstOffset < 0 || dstOffset > (dst.Length - length))
                {
                    throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
                }

                _buffer.GetBytes(_offset + 36, dst, dstOffset, length);
                return length;
            }

            public void SetSecurityType(byte[] src, int srcOffset)
            {
                const int length = 3;
                if (srcOffset < 0 || srcOffset > (src.Length - length))
                {
                    throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
                }

                _buffer.SetBytes(_offset + 36, src, srcOffset, length);
            }

            public const int SecurityIDId = 48;

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

            public const long SecurityIDNullValue = -9223372036854775808L;

            public const long SecurityIDMinValue = -9223372036854775807L;

            public const long SecurityIDMaxValue = 9223372036854775807L;

            public long SecurityID
            {
                get
                {
                    return _buffer.Int64GetLittleEndian(_offset + 39);
                }
                set
                {
                    _buffer.Int64PutLittleEndian(_offset + 39, value);
                }
            }


            public const int SecurityIDSourceId = 22;

            public static string SecurityIDSourceMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "";
                }

                return "";
            }

            public SecurityIDSource SecurityIDSource
            {
                get
                {
                    return (SecurityIDSource)_buffer.CharGet(_offset + 47);
                }
                set
                {
                    _buffer.CharPut(_offset + 47, (byte)value);
                }
            }


            public const int TransactTimeId = 60;

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

            public const ulong TransactTimeNullValue = 0xffffffffffffffffUL;

            public const ulong TransactTimeMinValue = 0x0UL;

            public const ulong TransactTimeMaxValue = 0xfffffffffffffffeUL;

            public ulong TransactTime
            {
                get
                {
                    return _buffer.Uint64GetLittleEndian(_offset + 48);
                }
                set
                {
                    _buffer.Uint64PutLittleEndian(_offset + 48, value);
                }
            }


            public const int BidPxId = 132;

            public static string BidPxMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "Price";
                }

                return "";
            }

            private readonly OptionalPrice _bidPx = new OptionalPrice();

            public OptionalPrice BidPx
            {
                get
                {
                    _bidPx.Wrap(_buffer, _offset + 56, _actingVersion);
                    return _bidPx;
                }
            }

            public const int BidSizeId = 134;

            public static string BidSizeMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "int";
                }

                return "";
            }

            public const long BidSizeNullValue = -9223372036854775808L;

            public const long BidSizeMinValue = -9223372036854775807L;

            public const long BidSizeMaxValue = 9223372036854775807L;

            public long BidSize
            {
                get
                {
                    return _buffer.Int64GetLittleEndian(_offset + 65);
                }
                set
                {
                    _buffer.Int64PutLittleEndian(_offset + 65, value);
                }
            }


            public const int OfferPxId = 133;

            public static string OfferPxMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "Price";
                }

                return "";
            }

            private readonly OptionalPrice _offerPx = new OptionalPrice();

            public OptionalPrice OfferPx
            {
                get
                {
                    _offerPx.Wrap(_buffer, _offset + 73, _actingVersion);
                    return _offerPx;
                }
            }

            public const int OfferSizeId = 135;

            public static string OfferSizeMetaAttribute(MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case MetaAttribute.Epoch: return "unix";
                    case MetaAttribute.TimeUnit: return "nanosecond";
                    case MetaAttribute.SemanticType: return "int";
                }

                return "";
            }

            public const long OfferSizeNullValue = -9223372036854775808L;

            public const long OfferSizeMinValue = -9223372036854775807L;

            public const long OfferSizeMaxValue = 9223372036854775807L;

            public long OfferSize
            {
                get
                {
                    return _buffer.Int64GetLittleEndian(_offset + 82);
                }
                set
                {
                    _buffer.Int64PutLittleEndian(_offset + 82, value);
                }
            }

        }
    }
    }
}
