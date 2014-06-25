/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class OrderCancelReplaceRequest
    {
    public const ushort BlockLength = (ushort)204;
    public const ushort TemplateId = (ushort)71;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "G";

    private readonly OrderCancelReplaceRequest _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public OrderCancelReplaceRequest()
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


    public const int AccountId = 1;

    public static string AccountMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte AccountNullValue = (byte)0;

    public const byte AccountMinValue = (byte)32;

    public const byte AccountMaxValue = (byte)126;

    public const int AccountLength  = 12;

    public byte GetAccount(int index)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 0 + (index * 1));
    }

    public void SetAccount(int index, byte value)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 0 + (index * 1), value);
    }

    public const string AccountCharacterEncoding = "UTF-8";

    public int GetAccount(byte[] dst, int dstOffset)
    {
        const int length = 12;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 0, dst, dstOffset, length);
        return length;
    }

    public void SetAccount(byte[] src, int srcOffset)
    {
        const int length = 12;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 0, src, srcOffset, length);
    }

    public const int ClOrdIDId = 11;

    public static string ClOrdIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte ClOrdIDNullValue = (byte)0;

    public const byte ClOrdIDMinValue = (byte)32;

    public const byte ClOrdIDMaxValue = (byte)126;

    public const int ClOrdIDLength  = 20;

    public byte GetClOrdID(int index)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 12 + (index * 1));
    }

    public void SetClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 12 + (index * 1), value);
    }

    public const string ClOrdIDCharacterEncoding = "UTF-8";

    public int GetClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 12, dst, dstOffset, length);
        return length;
    }

    public void SetClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 12, src, srcOffset, length);
    }

    public const int OrderIDId = 37;

    public static string OrderIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const long OrderIDNullValue = -9223372036854775808L;

    public const long OrderIDMinValue = -9223372036854775807L;

    public const long OrderIDMaxValue = 9223372036854775807L;

    public long OrderID
    {
        get
        {
            return _buffer.Int64GetLittleEndian(_offset + 32);
        }
        set
        {
            _buffer.Int64PutLittleEndian(_offset + 32, value);
        }
    }


    public const int HandInstId = 21;

    public static string HandInstMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public HandInst HandInst
    {
        get
        {
            return (HandInst)_buffer.CharGet(_offset + 40);
        }
        set
        {
            _buffer.CharPut(_offset + 40, (byte)value);
        }
    }


    public const int OrderQtyId = 38;

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

    private readonly IntQty32 _orderQty = new IntQty32();

    public IntQty32 OrderQty
    {
        get
        {
            _orderQty.Wrap(_buffer, _offset + 41, _actingVersion);
            return _orderQty;
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
            return (CustOrderHandlingInst)_buffer.CharGet(_offset + 45);
        }
        set
        {
            _buffer.CharPut(_offset + 45, (byte)value);
        }
    }


    public const int OrdTypeId = 40;

    public static string OrdTypeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public OrdType OrdType
    {
        get
        {
            return (OrdType)_buffer.CharGet(_offset + 46);
        }
        set
        {
            _buffer.CharPut(_offset + 46, (byte)value);
        }
    }


    public const int OrigClOrdIDId = 41;

    public static string OrigClOrdIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte OrigClOrdIDNullValue = (byte)0;

    public const byte OrigClOrdIDMinValue = (byte)32;

    public const byte OrigClOrdIDMaxValue = (byte)126;

    public const int OrigClOrdIDLength  = 20;

    public byte GetOrigClOrdID(int index)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 47 + (index * 1));
    }

    public void SetOrigClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 47 + (index * 1), value);
    }

    public const string OrigClOrdIDCharacterEncoding = "UTF-8";

    public int GetOrigClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 47, dst, dstOffset, length);
        return length;
    }

    public void SetOrigClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 47, src, srcOffset, length);
    }

    public const int PriceId = 44;

    public static string PriceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly OptionalPrice _price = new OptionalPrice();

    public OptionalPrice Price
    {
        get
        {
            _price.Wrap(_buffer, _offset + 67, _actingVersion);
            return _price;
        }
    }

    public const int SideId = 54;

    public static string SideMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public Side Side
    {
        get
        {
            return (Side)_buffer.CharGet(_offset + 76);
        }
        set
        {
            _buffer.CharPut(_offset + 76, (byte)value);
        }
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

        return _buffer.CharGet(_offset + 77 + (index * 1));
    }

    public void SetSymbol(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 77 + (index * 1), value);
    }

    public const string SymbolCharacterEncoding = "UTF-8";

    public int GetSymbol(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 77, dst, dstOffset, length);
        return length;
    }

    public void SetSymbol(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 77, src, srcOffset, length);
    }

    public const int TestId = 58;

    public static string TestMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte TestNullValue = (byte)0;

    public const byte TestMinValue = (byte)32;

    public const byte TestMaxValue = (byte)126;

    public const int TestLength  = 18;

    public byte GetTest(int index)
    {
        if (index < 0 || index >= 18)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 83 + (index * 1));
    }

    public void SetTest(int index, byte value)
    {
        if (index < 0 || index >= 18)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 83 + (index * 1), value);
    }

    public const string TestCharacterEncoding = "UTF-8";

    public int GetTest(byte[] dst, int dstOffset)
    {
        const int length = 18;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 83, dst, dstOffset, length);
        return length;
    }

    public void SetTest(byte[] src, int srcOffset)
    {
        const int length = 18;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 83, src, srcOffset, length);
    }

    public const int TimeInForceId = 59;

    public static string TimeInForceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "char";
        }

        return "";
    }

    public TimeInForce TimeInForce
    {
        get
        {
            return (TimeInForce)_buffer.CharGet(_offset + 101);
        }
        set
        {
            _buffer.CharPut(_offset + 101, (byte)value);
        }
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
            return (BooleanType)_buffer.Uint8Get(_offset + 102);
        }
        set
        {
            _buffer.Uint8Put(_offset + 102, (byte)value);
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
            return _buffer.Uint64GetLittleEndian(_offset + 103);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 103, value);
        }
    }


    public const int NoAllocsId = 78;

    public static string NoAllocsMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public NoAllocs NoAllocs
    {
        get
        {
            return (NoAllocs)_buffer.CharGet(_offset + 111);
        }
        set
        {
            _buffer.CharPut(_offset + 111, (byte)value);
        }
    }


    public const int AllocAccountId = 79;

    public static string AllocAccountMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte AllocAccountNullValue = (byte)0;

    public const byte AllocAccountMinValue = (byte)32;

    public const byte AllocAccountMaxValue = (byte)126;

    public const int AllocAccountLength  = 10;

    public byte GetAllocAccount(int index)
    {
        if (index < 0 || index >= 10)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 112 + (index * 1));
    }

    public void SetAllocAccount(int index, byte value)
    {
        if (index < 0 || index >= 10)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 112 + (index * 1), value);
    }

    public const string AllocAccountCharacterEncoding = "UTF-8";

    public int GetAllocAccount(byte[] dst, int dstOffset)
    {
        const int length = 10;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 112, dst, dstOffset, length);
        return length;
    }

    public void SetAllocAccount(byte[] src, int srcOffset)
    {
        const int length = 10;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 112, src, srcOffset, length);
    }

    public const int StopPxId = 99;

    public static string StopPxMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Price";
        }

        return "";
    }

    private readonly OptionalPrice _stopPx = new OptionalPrice();

    public OptionalPrice StopPx
    {
        get
        {
            _stopPx.Wrap(_buffer, _offset + 122, _actingVersion);
            return _stopPx;
        }
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

        return _buffer.CharGet(_offset + 131 + (index * 1));
    }

    public void SetSecurityDesc(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 131 + (index * 1), value);
    }

    public const string SecurityDescCharacterEncoding = "UTF-8";

    public int GetSecurityDesc(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 131, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityDesc(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 131, src, srcOffset, length);
    }

    public const int MinQtyId = 110;

    public static string MinQtyMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    private readonly IntQty32 _minQty = new IntQty32();

    public IntQty32 MinQty
    {
        get
        {
            _minQty.Wrap(_buffer, _offset + 151, _actingVersion);
            return _minQty;
        }
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

        return _buffer.CharGet(_offset + 155 + (index * 1));
    }

    public void SetSecurityType(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 155 + (index * 1), value);
    }

    public const string SecurityTypeCharacterEncoding = "UTF-8";

    public int GetSecurityType(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 155, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityType(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 155, src, srcOffset, length);
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
            return (CustomerOrFirm)_buffer.Uint8Get(_offset + 158);
        }
        set
        {
            _buffer.Uint8Put(_offset + 158, (byte)value);
        }
    }


    public const int MaxShowId = 210;

    public static string MaxShowMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "Qty";
        }

        return "";
    }

    private readonly IntQty32 _maxShow = new IntQty32();

    public IntQty32 MaxShow
    {
        get
        {
            _maxShow.Wrap(_buffer, _offset + 159, _actingVersion);
            return _maxShow;
        }
    }

    public const int ExpireDateId = 432;

    public static string ExpireDateMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const ushort ExpireDateNullValue = (ushort)65535;

    public const ushort ExpireDateMinValue = (ushort)0;

    public const ushort ExpireDateMaxValue = (ushort)65534;

    public ushort ExpireDate
    {
        get
        {
            return _buffer.Uint16GetLittleEndian(_offset + 163);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 163, value);
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

        return _buffer.CharGet(_offset + 165 + (index * 1));
    }

    public void SetSelfMatchPreventionID(int index, byte value)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 165 + (index * 1), value);
    }

    public const string SelfMatchPreventionIDCharacterEncoding = "UTF-8";

    public int GetSelfMatchPreventionID(byte[] dst, int dstOffset)
    {
        const int length = 12;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 165, dst, dstOffset, length);
        return length;
    }

    public void SetSelfMatchPreventionID(byte[] src, int srcOffset)
    {
        const int length = 12;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 165, src, srcOffset, length);
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
            return (CtiCode)_buffer.CharGet(_offset + 177);
        }
        set
        {
            _buffer.CharPut(_offset + 177, (byte)value);
        }
    }


    public const int GiveUpFirmId = 9707;

    public static string GiveUpFirmMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte GiveUpFirmNullValue = (byte)0;

    public const byte GiveUpFirmMinValue = (byte)32;

    public const byte GiveUpFirmMaxValue = (byte)126;

    public const int GiveUpFirmLength  = 3;

    public byte GetGiveUpFirm(int index)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 178 + (index * 1));
    }

    public void SetGiveUpFirm(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 178 + (index * 1), value);
    }

    public const string GiveUpFirmCharacterEncoding = "UTF-8";

    public int GetGiveUpFirm(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 178, dst, dstOffset, length);
        return length;
    }

    public void SetGiveUpFirm(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 178, src, srcOffset, length);
    }

    public const int CmtaGiveupCDId = 9708;

    public static string CmtaGiveupCDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte CmtaGiveupCDNullValue = (byte)0;

    public const byte CmtaGiveupCDMinValue = (byte)32;

    public const byte CmtaGiveupCDMaxValue = (byte)126;

    public const int CmtaGiveupCDLength  = 2;

    public byte GetCmtaGiveupCD(int index)
    {
        if (index < 0 || index >= 2)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 181 + (index * 1));
    }

    public void SetCmtaGiveupCD(int index, byte value)
    {
        if (index < 0 || index >= 2)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 181 + (index * 1), value);
    }

    public const string CmtaGiveupCDCharacterEncoding = "UTF-8";

    public int GetCmtaGiveupCD(byte[] dst, int dstOffset)
    {
        const int length = 2;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 181, dst, dstOffset, length);
        return length;
    }

    public void SetCmtaGiveupCD(byte[] src, int srcOffset)
    {
        const int length = 2;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 181, src, srcOffset, length);
    }

    public const int CorrelationClOrdIDId = 9717;

    public static string CorrelationClOrdIDMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "String";
        }

        return "";
    }

    public const byte CorrelationClOrdIDNullValue = (byte)0;

    public const byte CorrelationClOrdIDMinValue = (byte)32;

    public const byte CorrelationClOrdIDMaxValue = (byte)126;

    public const int CorrelationClOrdIDLength  = 20;

    public byte GetCorrelationClOrdID(int index)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 183 + (index * 1));
    }

    public void SetCorrelationClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 183 + (index * 1), value);
    }

    public const string CorrelationClOrdIDCharacterEncoding = "UTF-8";

    public int GetCorrelationClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 183, dst, dstOffset, length);
        return length;
    }

    public void SetCorrelationClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 183, src, srcOffset, length);
    }

    public const int OFMOverrideId = 9768;

    public static string OFMOverrideMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public OFMOverride OFMOverride
    {
        get
        {
            return (OFMOverride)_buffer.CharGet(_offset + 203);
        }
        set
        {
            _buffer.CharPut(_offset + 203, (byte)value);
        }
    }

    }
}
