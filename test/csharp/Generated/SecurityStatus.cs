/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class SecurityStatus
    {
    public const ushort TemplateId = (ushort)24;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)29;
    public const string SematicType = "f";

    private readonly SecurityStatus _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public SecurityStatus()
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
            return _buffer.Uint16GetLittleEndian(_offset + 8);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 8, value);
        }
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

        return _buffer.CharGet(_offset + 10 + (index * 1));
    }

    public void SetSecurityGroup(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 10 + (index * 1), value);
    }

    public const string SecurityGroupCharacterEncoding = "UTF-8";

    public int GetSecurityGroup(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 10, dst, dstOffset, length);
        return length;
    }

    public void SetSecurityGroup(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 10, src, srcOffset, length);
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

        return _buffer.CharGet(_offset + 16 + (index * 1));
    }

    public void SetAsset(int index, byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 16 + (index * 1), value);
    }

    public const string AssetCharacterEncoding = "UTF-8";

    public int GetAsset(byte[] dst, int dstOffset)
    {
        const int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 16, dst, dstOffset, length);
        return length;
    }

    public void SetAsset(byte[] src, int srcOffset)
    {
        const int length = 6;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 16, src, srcOffset, length);
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

    public const int SecurityIDNullValue = 2147483647;

    public const int SecurityIDMinValue = -2147483647;

    public const int SecurityIDMaxValue = 2147483647;

    public int SecurityID
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 22);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 22, value);
        }
    }


    public const int SecurityTradingStatusSchemaId = 326;

    public static string SecurityTradingStatusMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public SecurityTradingStatus SecurityTradingStatus
    {
        get
        {
            return (SecurityTradingStatus)_buffer.Uint8Get(_offset + 26);
        }
        set
        {
            _buffer.Uint8Put(_offset + 26, (byte)value);
        }
    }


    public const int HaltReasonSchemaId = 327;

    public static string HaltReasonMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public HaltReason HaltReason
    {
        get
        {
            return (HaltReason)_buffer.Uint8Get(_offset + 27);
        }
        set
        {
            _buffer.Uint8Put(_offset + 27, (byte)value);
        }
    }


    public const int SecurityTradingEventSchemaId = 1174;

    public static string SecurityTradingEventMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public SecurityTradingEvent SecurityTradingEvent
    {
        get
        {
            return (SecurityTradingEvent)_buffer.Uint8Get(_offset + 28);
        }
        set
        {
            _buffer.Uint8Put(_offset + 28, (byte)value);
        }
    }

    }
}
