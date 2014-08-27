/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public sealed partial class TokenCodec
    {
    public const ushort BlockLength = (ushort)20;
    public const ushort TemplateId = (ushort)2;
    public const ushort SchemaId = (ushort)0;
    public const ushort Schema_Version = (ushort)0;
    public const string SemanticType = "";

    private readonly TokenCodec _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public TokenCodec()
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


    public const int TokenOffsetId = 11;

    public static string TokenOffsetMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int TokenOffsetNullValue = -2147483648;

    public const int TokenOffsetMinValue = -2147483647;

    public const int TokenOffsetMaxValue = 2147483647;

    public int TokenOffset
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


    public const int TokenSizeId = 12;

    public static string TokenSizeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int TokenSizeNullValue = -2147483648;

    public const int TokenSizeMinValue = -2147483647;

    public const int TokenSizeMaxValue = 2147483647;

    public int TokenSize
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


    public const int FieldIdId = 13;

    public static string FieldIdMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int FieldIdNullValue = -2147483648;

    public const int FieldIdMinValue = -2147483647;

    public const int FieldIdMaxValue = 2147483647;

    public int FieldId
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 8);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 8, value);
        }
    }


    public const int TokenVersionId = 14;

    public static string TokenVersionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int TokenVersionNullValue = -2147483648;

    public const int TokenVersionMinValue = -2147483647;

    public const int TokenVersionMaxValue = 2147483647;

    public int TokenVersion
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 12);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 12, value);
        }
    }


    public const int SignalId = 15;

    public static string SignalMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public SignalCodec Signal
    {
        get
        {
            return (SignalCodec)_buffer.Uint8Get(_offset + 16);
        }
        set
        {
            _buffer.Uint8Put(_offset + 16, (byte)value);
        }
    }


    public const int PrimitiveTypeId = 16;

    public static string PrimitiveTypeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public PrimitiveTypeCodec PrimitiveType
    {
        get
        {
            return (PrimitiveTypeCodec)_buffer.Uint8Get(_offset + 17);
        }
        set
        {
            _buffer.Uint8Put(_offset + 17, (byte)value);
        }
    }


    public const int ByteOrderId = 17;

    public static string ByteOrderMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public ByteOrderCodec ByteOrder
    {
        get
        {
            return (ByteOrderCodec)_buffer.Uint8Get(_offset + 18);
        }
        set
        {
            _buffer.Uint8Put(_offset + 18, (byte)value);
        }
    }


    public const int PresenceId = 18;

    public static string PresenceMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public PresenceCodec Presence
    {
        get
        {
            return (PresenceCodec)_buffer.Uint8Get(_offset + 19);
        }
        set
        {
            _buffer.Uint8Put(_offset + 19, (byte)value);
        }
    }


    public const int NameId = 19;

    public const string NameCharacterEncoding = "UTF-8";


    public static string NameMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int NameHeaderSize = 1;

    public int GetName(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetName(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int ConstValueId = 20;

    public const string ConstValueCharacterEncoding = "UTF-8";


    public static string ConstValueMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int ConstValueHeaderSize = 1;

    public int GetConstValue(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetConstValue(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int MinValueId = 21;

    public const string MinValueCharacterEncoding = "UTF-8";


    public static string MinValueMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int MinValueHeaderSize = 1;

    public int GetMinValue(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetMinValue(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int MaxValueId = 22;

    public const string MaxValueCharacterEncoding = "UTF-8";


    public static string MaxValueMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int MaxValueHeaderSize = 1;

    public int GetMaxValue(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetMaxValue(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int NullValueId = 23;

    public const string NullValueCharacterEncoding = "UTF-8";


    public static string NullValueMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int NullValueHeaderSize = 1;

    public int GetNullValue(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetNullValue(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int CharacterEncodingId = 24;

    public const string CharacterEncodingCharacterEncoding = "UTF-8";


    public static string CharacterEncodingMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int CharacterEncodingHeaderSize = 1;

    public int GetCharacterEncoding(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetCharacterEncoding(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int EpochId = 25;

    public const string EpochCharacterEncoding = "UTF-8";


    public static string EpochMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int EpochHeaderSize = 1;

    public int GetEpoch(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetEpoch(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int TimeUnitId = 26;

    public const string TimeUnitCharacterEncoding = "UTF-8";


    public static string TimeUnitMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int TimeUnitHeaderSize = 1;

    public int GetTimeUnit(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetTimeUnit(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int SemanticTypeId = 27;

    public const string SemanticTypeCharacterEncoding = "UTF-8";


    public static string SemanticTypeMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int SemanticTypeHeaderSize = 1;

    public int GetSemanticType(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        _buffer.CheckLimit(limit + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(limit);
        int bytesCopied = Math.Min(length, dataLength);
        Limit = limit + sizeOfLengthField + dataLength;
        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetSemanticType(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }
    }
}
