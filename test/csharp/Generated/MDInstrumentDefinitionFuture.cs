/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class MDInstrumentDefinitionFuture
    {
    public const ushort TemplateId = (ushort)17;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)212;
    public const string SematicType = "d";

    private readonly MDInstrumentDefinitionFuture _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public MDInstrumentDefinitionFuture()
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

    public const uint TotNumReportsNullValue = 4294967295U;

    public const uint TotNumReportsMinValue = 0U;

    public const uint TotNumReportsMaxValue = 4294967293U;

    public uint TotNumReports
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


    public const int SecurityUpdateActionSchemaId = 980;

    public static string SecurityUpdateActionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public SecurityUpdateAction SecurityUpdateAction
    {
        get
        {
            return (SecurityUpdateAction)_buffer.CharGet(_offset + 4);
        }
        set
        {
            _buffer.CharPut(_offset + 4, (byte)value);
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
            return _buffer.Uint64GetLittleEndian(_offset + 5);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 5, value);
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
            return (SecurityTradingStatus)_buffer.Uint8Get(_offset + 13);
        }
        set
        {
            _buffer.Uint8Put(_offset + 13, (byte)value);
        }
    }


    public const int ApplIDSchemaId = 1180;

    public static string ApplIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const short ApplIDNullValue = (short)-32768;

    public const short ApplIDMinValue = (short)-32767;

    public const short ApplIDMaxValue = (short)32767;

    public short ApplID
    {
        get
        {
            return _buffer.Int16GetLittleEndian(_offset + 14);
        }
        set
        {
            _buffer.Int16PutLittleEndian(_offset + 14, value);
        }
    }


    public const int MarketSegmentIDSchemaId = 1300;

    public static string MarketSegmentIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const byte MarketSegmentIDNullValue = (byte)255;

    public const byte MarketSegmentIDMinValue = (byte)0;

    public const byte MarketSegmentIDMaxValue = (byte)254;

    public byte MarketSegmentID
    {
        get
        {
            return _buffer.Uint8Get(_offset + 16);
        }
        set
        {
            _buffer.Uint8Put(_offset + 16, value);
        }
    }


    public const int UnderlyingProductSchemaId = 462;

    public static string UnderlyingProductMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const byte UnderlyingProductNullValue = (byte)255;

    public const byte UnderlyingProductMinValue = (byte)0;

    public const byte UnderlyingProductMaxValue = (byte)254;

    public byte UnderlyingProduct
    {
        get
        {
            return _buffer.Uint8Get(_offset + 17);
        }
        set
        {
            _buffer.Uint8Put(_offset + 17, value);
        }
    }


    public const int SecurityExchangeSchemaId = 207;

    public static string SecurityExchangeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Exchange";
        }

        return "";
    }

    public const byte SecurityExchangeNullValue = (byte)0;

    public const byte SecurityExchangeMinValue = (byte)32;

    public const byte SecurityExchangeMaxValue = (byte)126;

    public const int SecurityExchangeLength  = 4;

    public byte GetSecurityExchange(int index)
    {
        if (index < 0 || index >= 4)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 18 + (index * 1));
    }

    public void SetSecurityExchange(int index, byte value)
    {
        if (index < 0 || index >= 4)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 18 + (index * 1), value);
    }

    public const string SecurityExchangeCharacterEncoding = "UTF-8";

    public int GetSecurityExchange(byte[] dst, int dstOffset)
    {
        const int length = 4;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 18, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityExchange(byte[] src, int srcOffset)
    {
        const int length = 4;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 18, src, srcOffset, length);
    }

    public const int SecurityGroupSchemaId = 1151;

    public static string SecurityGroupMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte SecurityGroupNullValue = (byte)0;

    public const byte SecurityGroupMinValue = (byte)32;

    public const byte SecurityGroupMaxValue = (byte)126;

    public const int SecurityGroupLength  = 6;

    public byte GetSecurityGroup(int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 22 + (index * 1));
    }

    public void SetSecurityGroup(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 22 + (index * 1), value);
    }

    public const string SecurityGroupCharacterEncoding = "UTF-8";

    public int GetSecurityGroup(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 22, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityGroup(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 22, src, srcOffset, length);
    }

    public const int AssetSchemaId = 6937;

    public static string AssetMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte AssetNullValue = (byte)0;

    public const byte AssetMinValue = (byte)32;

    public const byte AssetMaxValue = (byte)126;

    public const int AssetLength  = 6;

    public byte GetAsset(int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 28 + (index * 1));
    }

    public void SetAsset(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 28 + (index * 1), value);
    }

    public const string AssetCharacterEncoding = "UTF-8";

    public int GetAsset(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 28, dst, dstOffset, length);
        return length;
    }

    public void SetAsset(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 28, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 34 + (index * 1));
    }

    public void SetSymbol(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 34 + (index * 1), value);
    }

    public const string SymbolCharacterEncoding = "UTF-8";

    public int GetSymbol(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 34, dst, dstOffset, length);
        return length;
    }

    public void SetSymbol(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 34, src, srcOffset, length);
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
            return _buffer.Int32GetLittleEndian(_offset + 54);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 54, value);
        }
    }


    public const int SecurityIDSourceSchemaId = 22;

    public static string SecurityIDSourceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public const byte SecurityIDSourceNullValue = (byte)0;

    public const byte SecurityIDSourceMinValue = (byte)32;

    public const byte SecurityIDSourceMaxValue = (byte)126;

    private static readonly byte[] _SecurityIDSourceValue = {56};

    public const int SecurityIDSourceLength = 1;
    public byte SecurityIDSource(int index)
    {
        return _SecurityIDSourceValue[index];
    }

    public int GetSecurityIDSource(byte[] dst, int offset, int length)
    {
        int bytesCopied = Math.Min(length, 1);
        Array.Copy(_SecurityIDSourceValue, 0, dst, offset, bytesCopied);
        return bytesCopied;
    }

    public const int SecurityTypeSchemaId = 167;

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

    public const int SecurityTypeLength  = 6;

    public byte GetSecurityType(int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 58 + (index * 1));
    }

    public void SetSecurityType(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 58 + (index * 1), value);
    }

    public const string SecurityTypeCharacterEncoding = "UTF-8";

    public int GetSecurityType(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 58, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityType(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 58, src, srcOffset, length);
    }

    public const int CFICodeSchemaId = 461;

    public static string CFICodeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte CFICodeNullValue = (byte)0;

    public const byte CFICodeMinValue = (byte)32;

    public const byte CFICodeMaxValue = (byte)126;

    public const int CFICodeLength  = 6;

    public byte GetCFICode(int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 64 + (index * 1));
    }

    public void SetCFICode(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 64 + (index * 1), value);
    }

    public const string CFICodeCharacterEncoding = "UTF-8";

    public int GetCFICode(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 64, dst, dstOffset, length);
        return length;
    }

    public void SetCFICode(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 64, src, srcOffset, length);
    }

    public const int MaturityMonthYearSchemaId = 200;

    public static string MaturityMonthYearMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "MonthYear";
        }

        return "";
    }

    private readonly MaturityMonthYear _maturityMonthYear = new MaturityMonthYear();

    public MaturityMonthYear MaturityMonthYear
    {
        get
        {
            _maturityMonthYear.Wrap(_buffer, _offset + 70, _actingVersion);
            return _maturityMonthYear;
        }
    }

    public const int CurrencySchemaId = 15;

    public static string CurrencyMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Currency";
        }

        return "";
    }

    public const byte CurrencyNullValue = (byte)0;

    public const byte CurrencyMinValue = (byte)32;

    public const byte CurrencyMaxValue = (byte)126;

    public const int CurrencyLength  = 3;

    public byte GetCurrency(int index)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 75 + (index * 1));
    }

    public void SetCurrency(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 75 + (index * 1), value);
    }

    public const string CurrencyCharacterEncoding = "UTF-8";

    public int GetCurrency(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 75, dst, dstOffset, length);
        return length;
    }

    public void SetCurrency(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 75, src, srcOffset, length);
    }

    public const int SettlCurrencySchemaId = 120;

    public static string SettlCurrencyMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Currency";
        }

        return "";
    }

    public const byte SettlCurrencyNullValue = (byte)0;

    public const byte SettlCurrencyMinValue = (byte)32;

    public const byte SettlCurrencyMaxValue = (byte)126;

    public const int SettlCurrencyLength  = 3;

    public byte GetSettlCurrency(int index)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 78 + (index * 1));
    }

    public void SetSettlCurrency(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 78 + (index * 1), value);
    }

    public const string SettlCurrencyCharacterEncoding = "UTF-8";

    public int GetSettlCurrency(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 78, dst, dstOffset, length);
        return length;
    }

    public void SetSettlCurrency(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 78, src, srcOffset, length);
    }

    public const int MatchAlgorithmSchemaId = 1142;

    public static string MatchAlgorithmMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public const byte MatchAlgorithmNullValue = (byte)0;

    public const byte MatchAlgorithmMinValue = (byte)32;

    public const byte MatchAlgorithmMaxValue = (byte)126;

    public byte MatchAlgorithm
    {
        get
        {
            return _buffer.CharGet(_offset + 81);
        }
        set
        {
            _buffer.CharPut(_offset + 81, value);
        }
    }


    public const int MinTradeVolSchemaId = 562;

    public static string MinTradeVolMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    public const uint MinTradeVolNullValue = 4294967294U;

    public const uint MinTradeVolMinValue = 0U;

    public const uint MinTradeVolMaxValue = 4294967293U;

    public uint MinTradeVol
    {
        get
        {
            return _buffer.Uint32GetLittleEndian(_offset + 82);
        }
        set
        {
            _buffer.Uint32PutLittleEndian(_offset + 82, value);
        }
    }


    public const int MaxTradeVolSchemaId = 1140;

    public static string MaxTradeVolMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    public const uint MaxTradeVolNullValue = 4294967294U;

    public const uint MaxTradeVolMinValue = 0U;

    public const uint MaxTradeVolMaxValue = 4294967293U;

    public uint MaxTradeVol
    {
        get
        {
            return _buffer.Uint32GetLittleEndian(_offset + 86);
        }
        set
        {
            _buffer.Uint32PutLittleEndian(_offset + 86, value);
        }
    }


    public const int MinPriceIncrementSchemaId = 969;

    public static string MinPriceIncrementMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICE _minPriceIncrement = new PRICE();

    public PRICE MinPriceIncrement
    {
        get
        {
            _minPriceIncrement.Wrap(_buffer, _offset + 90, _actingVersion);
            return _minPriceIncrement;
        }
    }

    public const int DisplayFactorSchemaId = 9787;

    public static string DisplayFactorMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "float";
        }

        return "";
    }

    private readonly FLOAT _displayFactor = new FLOAT();

    public FLOAT DisplayFactor
    {
        get
        {
            _displayFactor.Wrap(_buffer, _offset + 98, _actingVersion);
            return _displayFactor;
        }
    }

    public const int MainFractionSchemaId = 37702;

    public static string MainFractionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const byte MainFractionNullValue = (byte)255;

    public const byte MainFractionMinValue = (byte)0;

    public const byte MainFractionMaxValue = (byte)254;

    public byte MainFraction
    {
        get
        {
            return _buffer.Uint8Get(_offset + 106);
        }
        set
        {
            _buffer.Uint8Put(_offset + 106, value);
        }
    }


    public const int SubFractionSchemaId = 37703;

    public static string SubFractionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const byte SubFractionNullValue = (byte)255;

    public const byte SubFractionMinValue = (byte)0;

    public const byte SubFractionMaxValue = (byte)254;

    public byte SubFraction
    {
        get
        {
            return _buffer.Uint8Get(_offset + 107);
        }
        set
        {
            _buffer.Uint8Put(_offset + 107, value);
        }
    }


    public const int PriceDisplayFormatSchemaId = 9800;

    public static string PriceDisplayFormatMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const byte PriceDisplayFormatNullValue = (byte)255;

    public const byte PriceDisplayFormatMinValue = (byte)0;

    public const byte PriceDisplayFormatMaxValue = (byte)254;

    public byte PriceDisplayFormat
    {
        get
        {
            return _buffer.Uint8Get(_offset + 108);
        }
        set
        {
            _buffer.Uint8Put(_offset + 108, value);
        }
    }


    public const int UnitOfMeasureSchemaId = 996;

    public static string UnitOfMeasureMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte UnitOfMeasureNullValue = (byte)0;

    public const byte UnitOfMeasureMinValue = (byte)32;

    public const byte UnitOfMeasureMaxValue = (byte)126;

    public const int UnitOfMeasureLength  = 30;

    public byte GetUnitOfMeasure(int index)
    {
        if (index < 0 || index >= 30)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 109 + (index * 1));
    }

    public void SetUnitOfMeasure(int index, byte value)
    {
        if (index < 0 || index >= 30)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 109 + (index * 1), value);
    }

    public const string UnitOfMeasureCharacterEncoding = "UTF-8";

    public int GetUnitOfMeasure(byte[] dst, int dstOffset)
    {
        const int length = 30;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 109, dst, dstOffset, length);
        return length;
    }

    public void SetUnitOfMeasure(byte[] src, int srcOffset)
    {
        const int length = 30;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 109, src, srcOffset, length);
    }

    public const int UnitOfMeasureQtySchemaId = 1147;

    public static string UnitOfMeasureQtyMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    private readonly PRICENULL _unitOfMeasureQty = new PRICENULL();

    public PRICENULL UnitOfMeasureQty
    {
        get
        {
            _unitOfMeasureQty.Wrap(_buffer, _offset + 139, _actingVersion);
            return _unitOfMeasureQty;
        }
    }

    public const int TradingReferencePriceSchemaId = 1150;

    public static string TradingReferencePriceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICENULL _tradingReferencePrice = new PRICENULL();

    public PRICENULL TradingReferencePrice
    {
        get
        {
            _tradingReferencePrice.Wrap(_buffer, _offset + 147, _actingVersion);
            return _tradingReferencePrice;
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
            return (SettlPriceType)_buffer.Uint8Get(_offset + 155);
        }
        set
        {
            _buffer.Uint8Put(_offset + 155, (byte)value);
        }
    }

    public const int OpenInterestQtySchemaId = 5792;

    public static string OpenInterestQtyMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const int OpenInterestQtyNullValue = 2147483647;

    public const int OpenInterestQtyMinValue = -2147483647;

    public const int OpenInterestQtyMaxValue = 2147483647;

    public int OpenInterestQty
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 156);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 156, value);
        }
    }


    public const int ClearedVolumeSchemaId = 5791;

    public static string ClearedVolumeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const int ClearedVolumeNullValue = 2147483647;

    public const int ClearedVolumeMinValue = -2147483647;

    public const int ClearedVolumeMaxValue = 2147483647;

    public int ClearedVolume
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 160);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 160, value);
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
            _highLimitPrice.Wrap(_buffer, _offset + 164, _actingVersion);
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
            _lowLimitPrice.Wrap(_buffer, _offset + 172, _actingVersion);
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
            _maxPriceVariation.Wrap(_buffer, _offset + 180, _actingVersion);
            return _maxPriceVariation;
        }
    }

    public const int DecayQuantitySchemaId = 5818;

    public static string DecayQuantityMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    public const int DecayQuantityNullValue = 2147483647;

    public const int DecayQuantityMinValue = -2147483647;

    public const int DecayQuantityMaxValue = 2147483647;

    public int DecayQuantity
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 188);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 188, value);
        }
    }


    public const int DecayStartDateSchemaId = 5819;

    public static string DecayStartDateMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "LocalMktDate";
        }

        return "";
    }

    public const ushort DecayStartDateNullValue = (ushort)65535;

    public const ushort DecayStartDateMinValue = (ushort)0;

    public const ushort DecayStartDateMaxValue = (ushort)65534;

    public ushort DecayStartDate
    {
        get
        {
            return _buffer.Uint16GetLittleEndian(_offset + 192);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 192, value);
        }
    }


    public const int OriginalContractSizeSchemaId = 5849;

    public static string OriginalContractSizeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    public const int OriginalContractSizeNullValue = 2147483647;

    public const int OriginalContractSizeMinValue = -2147483647;

    public const int OriginalContractSizeMaxValue = 2147483647;

    public int OriginalContractSize
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 194);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 194, value);
        }
    }


    public const int ContractMultiplierSchemaId = 231;

    public static string ContractMultiplierMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const int ContractMultiplierNullValue = 2147483647;

    public const int ContractMultiplierMinValue = -2147483647;

    public const int ContractMultiplierMaxValue = 2147483647;

    public int ContractMultiplier
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 198);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 198, value);
        }
    }


    public const int ContractMultiplierUnitSchemaId = 1435;

    public static string ContractMultiplierUnitMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const sbyte ContractMultiplierUnitNullValue = (sbyte)127;

    public const sbyte ContractMultiplierUnitMinValue = (sbyte)-127;

    public const sbyte ContractMultiplierUnitMaxValue = (sbyte)127;

    public sbyte ContractMultiplierUnit
    {
        get
        {
            return _buffer.Int8Get(_offset + 202);
        }
        set
        {
            _buffer.Int8Put(_offset + 202, value);
        }
    }


    public const int FlowScheduleTypeSchemaId = 1439;

    public static string FlowScheduleTypeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const sbyte FlowScheduleTypeNullValue = (sbyte)127;

    public const sbyte FlowScheduleTypeMinValue = (sbyte)-127;

    public const sbyte FlowScheduleTypeMaxValue = (sbyte)127;

    public sbyte FlowScheduleType
    {
        get
        {
            return _buffer.Int8Get(_offset + 203);
        }
        set
        {
            _buffer.Int8Put(_offset + 203, value);
        }
    }


    public const int MinPriceIncrementAmountSchemaId = 1146;

    public static string MinPriceIncrementAmountMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly PRICENULL _minPriceIncrementAmount = new PRICENULL();

    public PRICENULL MinPriceIncrementAmount
    {
        get
        {
            _minPriceIncrementAmount.Wrap(_buffer, _offset + 204, _actingVersion);
            return _minPriceIncrementAmount;
        }
    }

    private readonly NoEventsGroup _noEvents = new NoEventsGroup();

    public const long NoEventsSchemaId = 864;


    public NoEventsGroup NoEvents
    {
        get
        {
            _noEvents.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noEvents;
        }
    }

    public NoEventsGroup NoEventsCount(int count)
    {
        _noEvents.WrapForEncode(_parentMessage, _buffer, count);
        return _noEvents;
    }

    public class NoEventsGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private MDInstrumentDefinitionFuture _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)9;
            _index = -1;
            _count = count;
            _blockLength = 9;
            parentMessage.Limit = parentMessage.Limit + 3;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public NoEventsGroup Next()
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

        public const int EventTypeSchemaId = 865;

        public static string EventTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public EventType EventType
        {
            get
            {
                return (EventType)_buffer.Uint8Get(_offset + 0);
            }
            set
            {
                _buffer.Uint8Put(_offset + 0, (byte)value);
            }
        }


        public const int EventTimeSchemaId = 1145;

        public static string EventTimeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "UTCTimestamp";
            }

            return "";
        }

        public const ulong EventTimeNullValue = 0x8000000000000000UL;

        public const ulong EventTimeMinValue = 0x0UL;

        public const ulong EventTimeMaxValue = 0x7fffffffffffffffUL;

        public ulong EventTime
        {
            get
            {
                return _buffer.Uint64GetLittleEndian(_offset + 1);
            }
            set
            {
                _buffer.Uint64PutLittleEndian(_offset + 1, value);
            }
        }

    }

    private readonly NoMDFeedTypesGroup _noMDFeedTypes = new NoMDFeedTypesGroup();

    public const long NoMDFeedTypesSchemaId = 1141;


    public NoMDFeedTypesGroup NoMDFeedTypes
    {
        get
        {
            _noMDFeedTypes.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noMDFeedTypes;
        }
    }

    public NoMDFeedTypesGroup NoMDFeedTypesCount(int count)
    {
        _noMDFeedTypes.WrapForEncode(_parentMessage, _buffer, count);
        return _noMDFeedTypes;
    }

    public class NoMDFeedTypesGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private MDInstrumentDefinitionFuture _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)4;
            _index = -1;
            _count = count;
            _blockLength = 4;
            parentMessage.Limit = parentMessage.Limit + 3;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public NoMDFeedTypesGroup Next()
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

        public const int MDFeedTypeSchemaId = 1022;

        public static string MDFeedTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "String";
            }

            return "";
        }

        public const byte MDFeedTypeNullValue = (byte)0;

        public const byte MDFeedTypeMinValue = (byte)32;

        public const byte MDFeedTypeMaxValue = (byte)126;

        public const int MDFeedTypeLength  = 3;

        public byte GetMDFeedType(int index)
        {
            if (index < 0 || index >= 3)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            return _buffer.CharGet(_offset + 0 + (index * 1));
        }

        public void SetMDFeedType(int index, byte value)
        {
            if (index < 0 || index >= 3)
            {
                throw new IndexOutOfRangeException("index out of range: index=" + index);
            }

            _buffer.CharPut(_offset + 0 + (index * 1), value);
        }

    public const string MDFeedTypeCharacterEncoding = "UTF-8";

        public int GetMDFeedType(byte[] dst, int dstOffset)
        {
            const int length = 3;
            if (dstOffset < 0 || dstOffset > (dst.Length - length))
            {
                throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
            }

            _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
            return length;
        }

        public void SetMDFeedType(byte[] src, int srcOffset)
        {
            const int length = 3;
            if (srcOffset < 0 || srcOffset > (src.Length - length))
            {
                throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
            }

            _buffer.SetBytes(_offset + 0, src, srcOffset, length);
        }

        public const int MarketDepthSchemaId = 264;

        public static string MarketDepthMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte MarketDepthNullValue = (sbyte)-128;

        public const sbyte MarketDepthMinValue = (sbyte)-127;

        public const sbyte MarketDepthMaxValue = (sbyte)127;

        public sbyte MarketDepth
        {
            get
            {
                return _buffer.Int8Get(_offset + 3);
            }
            set
            {
                _buffer.Int8Put(_offset + 3, value);
            }
        }

    }

    private readonly NoInstAttribGroup _noInstAttrib = new NoInstAttribGroup();

    public const long NoInstAttribSchemaId = 870;


    public NoInstAttribGroup NoInstAttrib
    {
        get
        {
            _noInstAttrib.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noInstAttrib;
        }
    }

    public NoInstAttribGroup NoInstAttribCount(int count)
    {
        _noInstAttrib.WrapForEncode(_parentMessage, _buffer, count);
        return _noInstAttrib;
    }

    public class NoInstAttribGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private MDInstrumentDefinitionFuture _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)4;
            _index = -1;
            _count = count;
            _blockLength = 4;
            parentMessage.Limit = parentMessage.Limit + 3;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public NoInstAttribGroup Next()
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

        public const int InstAttribTypeSchemaId = 871;

        public static string InstAttribTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte InstAttribTypeNullValue = (sbyte)-128;

        public const sbyte InstAttribTypeMinValue = (sbyte)-127;

        public const sbyte InstAttribTypeMaxValue = (sbyte)127;

        public sbyte InstAttribType { get { return (sbyte)24; } }

        public const int InstAttribValueSchemaId = 872;

        public static string InstAttribValueMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "MultipleCharValue";
            }

            return "";
        }

        public InstAttribValue InstAttribValue
        {
            get
            {
                return (InstAttribValue)_buffer.Uint32GetLittleEndian(_offset + 0);
            }
            set
            {
                _buffer.Uint32PutLittleEndian(_offset + 0, (uint)value);
            }
        }
    }

    private readonly NoLotTypeRulesGroup _noLotTypeRules = new NoLotTypeRulesGroup();

    public const long NoLotTypeRulesSchemaId = 1234;


    public NoLotTypeRulesGroup NoLotTypeRules
    {
        get
        {
            _noLotTypeRules.WrapForDecode(_parentMessage, _buffer, _actingVersion);
            return _noLotTypeRules;
        }
    }

    public NoLotTypeRulesGroup NoLotTypeRulesCount(int count)
    {
        _noLotTypeRules.WrapForEncode(_parentMessage, _buffer, count);
        return _noLotTypeRules;
    }

    public class NoLotTypeRulesGroup
    {
        private readonly GroupSize _dimensions = new GroupSize();
        private MDInstrumentDefinitionFuture _parentMessage;
        private DirectBuffer _buffer;
        private int _blockLength;
        private int _actingVersion;
        private int _count;
        private int _index;
        private int _offset;

        public void WrapForDecode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int actingVersion)
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

        public void WrapForEncode(MDInstrumentDefinitionFuture parentMessage, DirectBuffer buffer, int count)
        {
            _parentMessage = parentMessage;
            _buffer = buffer;
            _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);
            _dimensions.NumInGroup = (byte)count;
            _dimensions.BlockLength = (ushort)5;
            _index = -1;
            _count = count;
            _blockLength = 5;
            parentMessage.Limit = parentMessage.Limit + 3;
        }

        public int Count { get { return _count; } }

        public bool HasNext { get { return _index + 1 < _count; } }

        public NoLotTypeRulesGroup Next()
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

        public const int LotTypeSchemaId = 1093;

        public static string LotTypeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "int";
            }

            return "";
        }

        public const sbyte LotTypeNullValue = (sbyte)-128;

        public const sbyte LotTypeMinValue = (sbyte)-127;

        public const sbyte LotTypeMaxValue = (sbyte)127;

        public sbyte LotType
        {
            get
            {
                return _buffer.Int8Get(_offset + 0);
            }
            set
            {
                _buffer.Int8Put(_offset + 0, value);
            }
        }


        public const int MinLotSizeSchemaId = 1231;

        public static string MinLotSizeMetaAttribute(MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case MetaAttribute.Epoch: return "unix";
                case MetaAttribute.TimeUnit: return "nanosecond";
                case MetaAttribute.SemanticType: return "Qty";
            }

            return "";
        }

        private readonly DecimalQty _minLotSize = new DecimalQty();

        public DecimalQty MinLotSize
        {
            get
            {
                _minLotSize.Wrap(_buffer, _offset + 1, _actingVersion);
                return _minLotSize;
            }
        }
    }
    }
}
