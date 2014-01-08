/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public class TokenCodec
    {
    public const ushort TemplateId = (ushort)2;
    public const byte TemplateVersion = (byte)0;
    public const ushort BlockLength = (ushort)20;

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
            _buffer.CheckLimit(_limit);
            _limit = value;
        }
    }


    public const int TokenOffsetSchemaId = 11;

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

    public const int TokenOffsetNullVal = -2147483648;

    public const int TokenOffsetMinVal = -2147483647;

    public const int TokenOffsetMaxVal = 2147483647;

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


    public const int TokenSizeSchemaId = 12;

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

    public const int TokenSizeNullVal = -2147483648;

    public const int TokenSizeMinVal = -2147483647;

    public const int TokenSizeMaxVal = 2147483647;

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


    public const int SchemaIdSchemaId = 13;

    public static string SchemaIdMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int SchemaIdNullVal = -2147483648;

    public const int SchemaIdMinVal = -2147483647;

    public const int SchemaIdMaxVal = 2147483647;

    public int SchemaId
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


    public const int TokenVersionSchemaId = 17;

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

    public const int TokenVersionNullVal = -2147483648;

    public const int TokenVersionMinVal = -2147483647;

    public const int TokenVersionMaxVal = 2147483647;

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


    public const int SignalSchemaId = 14;

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


    public const int PrimitiveTypeSchemaId = 15;

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


    public const int ByteOrderSchemaId = 16;

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


    public const int PresenceSchemaId = 17;

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


    public const int NameSchemaId = 18;

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

    public const int ConstValSchemaId = 19;

    public const string ConstValCharacterEncoding = "UTF-8";


    public static string ConstValMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public int GetConstVal(byte[] dst, int dstOffset, int length)
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

    public int SetConstVal(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int MinValSchemaId = 20;

    public const string MinValCharacterEncoding = "UTF-8";


    public static string MinValMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public int GetMinVal(byte[] dst, int dstOffset, int length)
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

    public int SetMinVal(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int MaxValSchemaId = 21;

    public const string MaxValCharacterEncoding = "UTF-8";


    public static string MaxValMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public int GetMaxVal(byte[] dst, int dstOffset, int length)
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

    public int SetMaxVal(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int NullValSchemaId = 22;

    public const string NullValCharacterEncoding = "UTF-8";


    public static string NullValMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public int GetNullVal(byte[] dst, int dstOffset, int length)
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

    public int SetNullVal(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int CharacterEncodingSchemaId = 23;

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

    public const int EpochSchemaId = 24;

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

    public const int TimeUnitSchemaId = 25;

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

    public const int SemanticTypeSchemaId = 26;

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
