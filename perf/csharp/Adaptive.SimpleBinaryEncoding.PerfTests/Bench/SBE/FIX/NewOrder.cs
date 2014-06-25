/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class NewOrder
    {
    public const ushort BlockLength = (ushort)156;
    public const ushort TemplateId = (ushort)68;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "D";

    private readonly NewOrder _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public NewOrder()
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
            return (HandInst)_buffer.CharGet(_offset + 32);
        }
        set
        {
            _buffer.CharPut(_offset + 32, (byte)value);
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
            return (CustOrderHandlingInst)_buffer.CharGet(_offset + 33);
        }
        set
        {
            _buffer.CharPut(_offset + 33, (byte)value);
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
            _orderQty.Wrap(_buffer, _offset + 34, _actingVersion);
            return _orderQty;
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
            return (OrdType)_buffer.CharGet(_offset + 38);
        }
        set
        {
            _buffer.CharPut(_offset + 38, (byte)value);
        }
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
            _price.Wrap(_buffer, _offset + 39, _actingVersion);
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
            return (Side)_buffer.CharGet(_offset + 48);
        }
        set
        {
            _buffer.CharPut(_offset + 48, (byte)value);
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

        return _buffer.CharGet(_offset + 49 + (index * 1));
    }

    public void SetSymbol(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 49 + (index * 1), value);
    }

    public const string SymbolCharacterEncoding = "UTF-8";

    public int GetSymbol(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 49, dst, dstOffset, length);
        return length;
    }

    public void SetSymbol(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 49, src, srcOffset, length);
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
            return (TimeInForce)_buffer.CharGet(_offset + 55);
        }
        set
        {
            _buffer.CharPut(_offset + 55, (byte)value);
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
            return _buffer.Uint64GetLittleEndian(_offset + 56);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 56, value);
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
            return (BooleanType)_buffer.Uint8Get(_offset + 64);
        }
        set
        {
            _buffer.Uint8Put(_offset + 64, (byte)value);
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

        return _buffer.CharGet(_offset + 65 + (index * 1));
    }

    public void SetAllocAccount(int index, byte value)
    {
        if (index < 0 || index >= 10)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 65 + (index * 1), value);
    }

    public const string AllocAccountCharacterEncoding = "UTF-8";

    public int GetAllocAccount(byte[] dst, int dstOffset)
    {
        const int length = 10;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 65, dst, dstOffset, length);
        return length;
    }

    public void SetAllocAccount(byte[] src, int srcOffset)
    {
        const int length = 10;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 65, src, srcOffset, length);
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
            _stopPx.Wrap(_buffer, _offset + 75, _actingVersion);
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

        return _buffer.CharGet(_offset + 84 + (index * 1));
    }

    public void SetSecurityDesc(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 84 + (index * 1), value);
    }

    public const string SecurityDescCharacterEncoding = "UTF-8";

    public int GetSecurityDesc(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 84, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityDesc(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 84, src, srcOffset, length);
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
            _minQty.Wrap(_buffer, _offset + 104, _actingVersion);
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

        return _buffer.CharGet(_offset + 108 + (index * 1));
    }

    public void SetSecurityType(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 108 + (index * 1), value);
    }

    public const string SecurityTypeCharacterEncoding = "UTF-8";

    public int GetSecurityType(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 108, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityType(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 108, src, srcOffset, length);
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
            return (CustomerOrFirm)_buffer.Uint8Get(_offset + 111);
        }
        set
        {
            _buffer.Uint8Put(_offset + 111, (byte)value);
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
            _maxShow.Wrap(_buffer, _offset + 112, _actingVersion);
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
            return _buffer.Uint16GetLittleEndian(_offset + 116);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 116, value);
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

        return _buffer.CharGet(_offset + 118 + (index * 1));
    }

    public void SetSelfMatchPreventionID(int index, byte value)
    {
        if (index < 0 || index >= 12)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 118 + (index * 1), value);
    }

    public const string SelfMatchPreventionIDCharacterEncoding = "UTF-8";

    public int GetSelfMatchPreventionID(byte[] dst, int dstOffset)
    {
        const int length = 12;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 118, dst, dstOffset, length);
        return length;
    }

    public void SetSelfMatchPreventionID(byte[] src, int srcOffset)
    {
        const int length = 12;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 118, src, srcOffset, length);
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
            return (CtiCode)_buffer.CharGet(_offset + 130);
        }
        set
        {
            _buffer.CharPut(_offset + 130, (byte)value);
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

        return _buffer.CharGet(_offset + 131 + (index * 1));
    }

    public void SetGiveUpFirm(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 131 + (index * 1), value);
    }

    public const string GiveUpFirmCharacterEncoding = "UTF-8";

    public int GetGiveUpFirm(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 131, dst, dstOffset, length);
        return length;
    }

    public void SetGiveUpFirm(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 131, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 134 + (index * 1));
    }

    public void SetCmtaGiveupCD(int index, byte value)
    {
        if (index < 0 || index >= 2)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 134 + (index * 1), value);
    }

    public const string CmtaGiveupCDCharacterEncoding = "UTF-8";

    public int GetCmtaGiveupCD(byte[] dst, int dstOffset)
    {
        const int length = 2;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 134, dst, dstOffset, length);
        return length;
    }

    public void SetCmtaGiveupCD(byte[] src, int srcOffset)
    {
        const int length = 2;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 134, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 136 + (index * 1));
    }

    public void SetCorrelationClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 136 + (index * 1), value);
    }

    public const string CorrelationClOrdIDCharacterEncoding = "UTF-8";

    public int GetCorrelationClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 136, dst, dstOffset, length);
        return length;
    }

    public void SetCorrelationClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 136, src, srcOffset, length);
    }
    }
}
