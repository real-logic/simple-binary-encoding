/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class OrderCancelRequest
    {
    public const ushort BlockLength = (ushort)119;
    public const ushort TemplateId = (ushort)70;
    public const ushort SchemaId = (ushort)2;
    public const ushort Schema_Version = (ushort)0;
    public const string SematicType = "F";

    private readonly OrderCancelRequest _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public OrderCancelRequest()
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

        return _buffer.CharGet(_offset + 40 + (index * 1));
    }

    public void SetOrigClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 40 + (index * 1), value);
    }

    public const string OrigClOrdIDCharacterEncoding = "UTF-8";

    public int GetOrigClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 40, dst, dstOffset, length);
        return length;
    }

    public void SetOrigClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 40, src, srcOffset, length);
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
            return (Side)_buffer.CharGet(_offset + 60);
        }
        set
        {
            _buffer.CharPut(_offset + 60, (byte)value);
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

        return _buffer.CharGet(_offset + 61 + (index * 1));
    }

    public void SetSymbol(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 61 + (index * 1), value);
    }

    public const string SymbolCharacterEncoding = "UTF-8";

    public int GetSymbol(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 61, dst, dstOffset, length);
        return length;
    }

    public void SetSymbol(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 61, src, srcOffset, length);
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
            return _buffer.Uint64GetLittleEndian(_offset + 67);
        }
        set
        {
            _buffer.Uint64PutLittleEndian(_offset + 67, value);
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
            return (BooleanType)_buffer.Uint8Get(_offset + 75);
        }
        set
        {
            _buffer.Uint8Put(_offset + 75, (byte)value);
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

        return _buffer.CharGet(_offset + 76 + (index * 1));
    }

    public void SetSecurityDesc(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 76 + (index * 1), value);
    }

    public const string SecurityDescCharacterEncoding = "UTF-8";

    public int GetSecurityDesc(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 76, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityDesc(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 76, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 96 + (index * 1));
    }

    public void SetSecurityType(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 96 + (index * 1), value);
    }

    public const string SecurityTypeCharacterEncoding = "UTF-8";

    public int GetSecurityType(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 96, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityType(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 96, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 99 + (index * 1));
    }

    public void SetCorrelationClOrdID(int index, byte value)
    {
        if (index < 0 || index >= 20)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 99 + (index * 1), value);
    }

    public const string CorrelationClOrdIDCharacterEncoding = "UTF-8";

    public int GetCorrelationClOrdID(byte[] dst, int dstOffset)
    {
        const int length = 20;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 99, dst, dstOffset, length);
        return length;
    }

    public void SetCorrelationClOrdID(byte[] src, int srcOffset)
    {
        const int length = 20;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 99, src, srcOffset, length);
    }
    }
}
