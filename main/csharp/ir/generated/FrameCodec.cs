/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public sealed partial class FrameCodec
    {
    public const ushort BlockLength = (ushort)12;
    public const ushort TemplateId = (ushort)1;
    public const ushort SchemaId = (ushort)0;
    public const ushort Schema_Version = (ushort)0;
    public const string SemanticType = "";

    private readonly FrameCodec _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public FrameCodec()
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


    public const int IrIdId = 1;

    public static string IrIdMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int IrIdNullValue = -2147483648;

    public const int IrIdMinValue = -2147483647;

    public const int IrIdMaxValue = 2147483647;

    public int IrId
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


    public const int IrVersionId = 2;

    public static string IrVersionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int IrVersionNullValue = -2147483648;

    public const int IrVersionMinValue = -2147483647;

    public const int IrVersionMaxValue = 2147483647;

    public int IrVersion
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


    public const int SchemaVersionId = 3;

    public static string SchemaVersionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int SchemaVersionNullValue = -2147483648;

    public const int SchemaVersionMinValue = -2147483647;

    public const int SchemaVersionMaxValue = 2147483647;

    public int SchemaVersion
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


    public const int PackageNameId = 4;

    public const string PackageNameCharacterEncoding = "UTF-8";


    public static string PackageNameMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int PackageNameHeaderSize = 1;

    public int GetPackageName(byte[] dst, int dstOffset, int length)
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

    public int SetPackageName(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int NamespaceNameId = 5;

    public const string NamespaceNameCharacterEncoding = "UTF-8";


    public static string NamespaceNameMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int NamespaceNameHeaderSize = 1;

    public int GetNamespaceName(byte[] dst, int dstOffset, int length)
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

    public int SetNamespaceName(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int limit = Limit;
        Limit = limit + sizeOfLengthField + length;
        _buffer.Uint8Put(limit, (byte)length);
        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public const int SemanticVersionId = 6;

    public const string SemanticVersionCharacterEncoding = "UTF-8";


    public static string SemanticVersionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int SemanticVersionHeaderSize = 1;

    public int GetSemanticVersion(byte[] dst, int dstOffset, int length)
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

    public int SetSemanticVersion(byte[] src, int srcOffset, int length)
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
