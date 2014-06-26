/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class AdminLogin
    {
    public const ushort TemplateId = (ushort)15;
    public const byte TemplateVersion = (byte)1;
    public const ushort BlockLength = (ushort)1;
    public const string SematicType = "A";

    private readonly AdminLogin _parentMessage;
    private DirectBuffer _buffer;
    private int _offset;
    private int _limit;
    private int _actingBlockLength;
    private int _actingVersion;

    public int Offset { get { return _offset; } }

    public AdminLogin()
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


    public const int HeartBtIntSchemaId = 108;

    public static string HeartBtIntMetaAttribute(MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute.Epoch: return "unix";
            case MetaAttribute.TimeUnit: return "nanosecond";
            case MetaAttribute.SemanticType: return "int";
        }

        return "";
    }

    public const sbyte HeartBtIntNullValue = (sbyte)-128;

    public const sbyte HeartBtIntMinValue = (sbyte)-127;

    public const sbyte HeartBtIntMaxValue = (sbyte)127;

    public sbyte HeartBtInt
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

    }
}
