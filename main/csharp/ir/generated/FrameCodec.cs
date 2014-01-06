/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Uk.Co.Real_logic.Sbe.Ir.Generated
{
    public class FrameCodec
    {
        public enum MetaAttribute
        {
            Epoch,
            TimeUnit,
            SemanticType
        }

    public const ushort TemplateId = (ushort)1;
    public const byte TemplateVersion = (byte)0;
    public const ushort BlockLength = (ushort)8;

    private readonly FrameCodec _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _position;
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
        _actingVersion = TemplateVersion;
        Position = offset + _actingBlockLength;
    }

    public void WrapForDecode(DirectBuffer buffer, int offset,
                              int actingBlockLength, int actingVersion)
    {
        _buffer = buffer;
        _offset = offset;
        _actingBlockLength = actingBlockLength;
        _actingVersion = actingVersion;
        Position = offset + _actingBlockLength;
    }

    public int Size
    {
        get
        {
            return _position - _offset;
        }
    }

    public int Position
    {
        get
        {
            return _position;
        }
        set
        {
            _buffer.CheckPosition(_position);
            _position = value;
        }
    }


    public const int SbeIrVersionSchemaId = 1;

    public static string SbeIrVersionMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public const int SbeIrVersionNullVal = -2147483648;

    public const int SbeIrVersionMinVal = -2147483647;

    public const int SbeIrVersionMaxVal = 2147483647;

    public int SbeIrVersion
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


    public const int SchemaVersionSchemaId = 2;

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

    public const int SchemaVersionNullVal = -2147483648;

    public const int SchemaVersionMinVal = -2147483647;

    public const int SchemaVersionMaxVal = 2147483647;

    public int SchemaVersion
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


    public const int PackageValSchemaId = 4;

    public const string PackageValCharacterEncoding = "UTF-8";


    public static string PackageValMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "";
        }

        return "";
    }

    public int GetPackageVal(byte[] dst, int dstOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int position = Position;
        _buffer.CheckPosition(position + sizeOfLengthField);
        int dataLength = _buffer.Uint8Get(position);
        int bytesCopied = Math.Min(length, dataLength);
        Position = position + sizeOfLengthField + dataLength;
        _buffer.GetBytes(position + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int SetPackageVal(byte[] src, int srcOffset, int length)
    {
        const int sizeOfLengthField = 1;
        int position = Position;
        Position = position + sizeOfLengthField + length;
        _buffer.Uint8Put(position, (byte)length);
        _buffer.SetBytes(position + sizeOfLengthField, src, srcOffset, length);

        return length;
    }
    }
}
