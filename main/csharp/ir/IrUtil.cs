using Adaptive.SimpleBinaryEncoding.Ir.Generated;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    internal class IrUtil
    {
        public static readonly byte[] EmptyBuffer = new byte[0];

        public static ByteOrderCodec MapByteOrder(ByteOrder byteOrder)
        {
            if (byteOrder == ByteOrder.BigEndian)
            {
                return ByteOrderCodec.SBE_BIG_ENDIAN;
            }

            return ByteOrderCodec.SBE_LITTLE_ENDIAN;
        }

        public static ByteOrder MapByteOrder(ByteOrderCodec byteOrder)
        {
            switch (byteOrder)
            {
                case ByteOrderCodec.SBE_LITTLE_ENDIAN:
                    return ByteOrder.LittleEndian;

                case ByteOrderCodec.SBE_BIG_ENDIAN:
                    return ByteOrder.BigEndian;
            }

            return ByteOrder.LittleEndian;
        }

        public static SignalCodec MapSignal(Signal signal)
        {
            switch (signal)
            {
                case Signal.BeginMessage:
                    return SignalCodec.BEGIN_MESSAGE;

                case Signal.EndMessage:
                    return SignalCodec.END_MESSAGE;

                case Signal.BeginField:
                    return SignalCodec.BEGIN_FIELD;

                case Signal.EndField:
                    return SignalCodec.END_FIELD;

                case Signal.BeginComposite:
                    return SignalCodec.BEGIN_COMPOSITE;

                case Signal.EndComposite:
                    return SignalCodec.END_COMPOSITE;

                case Signal.BeginEnum:
                    return SignalCodec.BEGIN_ENUM;

                case Signal.EndEnum:
                    return SignalCodec.END_ENUM;

                case Signal.BeginSet:
                    return SignalCodec.BEGIN_SET;

                case Signal.EndSet:
                    return SignalCodec.END_SET;

                case Signal.BeginGroup:
                    return SignalCodec.BEGIN_GROUP;

                case Signal.EndGroup:
                    return SignalCodec.END_GROUP;

                case Signal.BeginVarData:
                    return SignalCodec.BEGIN_VAR_DATA;

                case Signal.EndVarData:
                    return SignalCodec.END_VAR_DATA;

                case Signal.ValidValue:
                    return SignalCodec.VALID_VALUE;

                case Signal.Choice:
                    return SignalCodec.CHOICE;

                case Signal.Encoding:
                default:
                    return SignalCodec.ENCODING;
            }
        }

        public static Signal MapSignal(SignalCodec signal)
        {
            switch (signal)
            {
                case SignalCodec.BEGIN_MESSAGE:
                    return Signal.BeginMessage;

                case SignalCodec.END_MESSAGE:
                    return Signal.EndMessage;

                case SignalCodec.BEGIN_FIELD:
                    return Signal.BeginField;

                case SignalCodec.END_FIELD:
                    return Signal.EndField;

                case SignalCodec.BEGIN_COMPOSITE:
                    return Signal.BeginComposite;

                case SignalCodec.END_COMPOSITE:
                    return Signal.EndComposite;

                case SignalCodec.BEGIN_ENUM:
                    return Signal.BeginEnum;

                case SignalCodec.END_ENUM:
                    return Signal.EndEnum;

                case SignalCodec.BEGIN_SET:
                    return Signal.BeginSet;

                case SignalCodec.END_SET:
                    return Signal.EndSet;

                case SignalCodec.BEGIN_GROUP:
                    return Signal.BeginGroup;

                case SignalCodec.END_GROUP:
                    return Signal.EndGroup;

                case SignalCodec.BEGIN_VAR_DATA:
                    return Signal.BeginVarData;

                case SignalCodec.END_VAR_DATA:
                    return Signal.EndVarData;

                case SignalCodec.VALID_VALUE:
                    return Signal.ValidValue;

                case SignalCodec.CHOICE:
                    return Signal.Choice;

                case SignalCodec.ENCODING:
                default:
                    return Signal.Encoding;
            }
        }

        public static PrimitiveTypeCodec MapPrimitiveType(PrimitiveType type)
        {
            if (type == null)
            {
                return PrimitiveTypeCodec.NONE;
            }

            switch (type.Type)
            {
                case SbePrimitiveType.Int8:
                    return PrimitiveTypeCodec.INT8;

                case SbePrimitiveType.Int16:
                    return PrimitiveTypeCodec.INT16;

                case SbePrimitiveType.Int32:
                    return PrimitiveTypeCodec.INT32;

                case SbePrimitiveType.Int64:
                    return PrimitiveTypeCodec.INT64;

                case SbePrimitiveType.UInt8:
                    return PrimitiveTypeCodec.UINT8;

                case SbePrimitiveType.UInt16:
                    return PrimitiveTypeCodec.UINT16;

                case SbePrimitiveType.UInt32:
                    return PrimitiveTypeCodec.UINT32;

                case SbePrimitiveType.UInt64:
                    return PrimitiveTypeCodec.UINT64;

                case SbePrimitiveType.Float:
                    return PrimitiveTypeCodec.FLOAT;

                case SbePrimitiveType.Double:
                    return PrimitiveTypeCodec.DOUBLE;

                case SbePrimitiveType.Char:
                    return PrimitiveTypeCodec.CHAR;

                default:
                    return PrimitiveTypeCodec.NONE;
            }
        }

        public static PrimitiveType MapPrimitiveType(PrimitiveTypeCodec type)
        {
            switch (type)
            {
                case PrimitiveTypeCodec.INT8:
                    return PrimitiveType.SbeInt8;

                case PrimitiveTypeCodec.INT16:
                    return PrimitiveType.SbeInt16;

                case PrimitiveTypeCodec.INT32:
                    return PrimitiveType.SbeInt32;

                case PrimitiveTypeCodec.INT64:
                    return PrimitiveType.SbeInt64;

                case PrimitiveTypeCodec.UINT8:
                    return PrimitiveType.SbeUInt8;

                case PrimitiveTypeCodec.UINT16:
                    return PrimitiveType.SbeUInt16;

                case PrimitiveTypeCodec.UINT32:
                    return PrimitiveType.SbeUInt32;

                case PrimitiveTypeCodec.UINT64:
                    return PrimitiveType.SbeUInt64;

                case PrimitiveTypeCodec.FLOAT:
                    return PrimitiveType.SbeFloat;

                case PrimitiveTypeCodec.DOUBLE:
                    return PrimitiveType.SbeDouble;

                case PrimitiveTypeCodec.CHAR:
                    return PrimitiveType.SbeChar;

                case PrimitiveTypeCodec.NONE:
                default:
                    return null;
            }
        }

        public static int Put(DirectBuffer buffer, PrimitiveValue value, PrimitiveType type)
        {
            if (value == null)
            {
                return 0;
            }

            switch (type.Type)
            {
                case SbePrimitiveType.Char:
                    if (value.Size == 1)
                    {
                        buffer.CharPut(0, (byte)value.LongValue());
                        return 1;
                    }
                    else
                    {
                        var byteArrayValue = value.ByteArrayValue();
                        buffer.SetBytes(0, byteArrayValue, 0, byteArrayValue.Length);
                        return byteArrayValue.Length;
                    }

                case SbePrimitiveType.Int8:
                    buffer.Int8Put(0, (sbyte)value.LongValue());
                    return 1;

                case SbePrimitiveType.Int16:
                    buffer.Int16PutLittleEndian(0, (short)value.LongValue());
                    return 2;

                case SbePrimitiveType.Int32:
                    buffer.Int32PutLittleEndian(0, (int)value.LongValue());
                    return 4;

                case SbePrimitiveType.Int64:
                    buffer.Int64PutLittleEndian(0, value.LongValue());
                    return 8;

                case SbePrimitiveType.UInt8:
                    buffer.Uint8Put(0, (byte)value.LongValue());
                    return 1;

                case SbePrimitiveType.UInt16:
                    buffer.Uint16PutLittleEndian(0, (ushort)value.LongValue());
                    return 2;

                case SbePrimitiveType.UInt32:
                    buffer.Uint32PutLittleEndian(0, (uint) value.LongValue());
                    return 4;

                case SbePrimitiveType.UInt64:
                    buffer.Uint64PutLittleEndian(0, value.ULongValue());
                    return 8;

                case SbePrimitiveType.Float:
                    buffer.FloatPutLittleEndian(0, (float) value.DoubleValue());
                    return 4;

                case SbePrimitiveType.Double:
                    buffer.DoublePutLittleEndian(0, value.DoubleValue());
                    return 8;

                default:
                    return 0;
            }
        }

        public static PrimitiveValue Get(DirectBuffer buffer, PrimitiveType type, int length)
        {
            if (length == 0)
            {
                return null;
            }

            switch (type.Type)
            {
                case SbePrimitiveType.Char:
                    if (length == 1)
                    {
                        return new PrimitiveValue(buffer.CharGet(0), 1);
                    }
                    else
                    {
                        var array = new byte[length];
                        buffer.GetBytes(0, array, 0, array.Length);
                        return new PrimitiveValue(array, "UTF-8", array.Length);
                    }

                case SbePrimitiveType.Int8:
                    return new PrimitiveValue(buffer.Int8Get(0), 1);

                case SbePrimitiveType.Int16:
                    return new PrimitiveValue(buffer.Int16GetLittleEndian(0), 2);

                case SbePrimitiveType.Int32:
                    return new PrimitiveValue(buffer.Int32GetLittleEndian(0), 4);

                case SbePrimitiveType.Int64:
                    return new PrimitiveValue(buffer.Int64GetLittleEndian(0), 8);

                case SbePrimitiveType.UInt8:
                    return new PrimitiveValue(buffer.Uint8Get(0), 1);

                case SbePrimitiveType.UInt16:
                    return new PrimitiveValue(buffer.Uint16GetLittleEndian(0), 2);

                case SbePrimitiveType.UInt32:
                    return new PrimitiveValue(buffer.Uint32GetLittleEndian(0), 4);

                case SbePrimitiveType.UInt64:
                    return new PrimitiveValue(buffer.Uint64GetLittleEndian(0), 8);

                case SbePrimitiveType.Float:
                    return new PrimitiveValue(buffer.FloatGetLittleEndian(0), 4);

                case SbePrimitiveType.Double:
                    return new PrimitiveValue(buffer.DoubleGetLittleEndian(0), 8);

                default:
                    return null;
            }
        }

        public static byte[] GetBytes(string value, string characterEncoding)
        {
            if (null == value)
            {
                return EmptyBuffer;
            }

            return System.Text.Encoding.GetEncoding(characterEncoding).GetBytes(value);
        }

        public static Presence MapPresence(PresenceCodec presence)
        {
            switch (presence)
            {
                case PresenceCodec.SBE_OPTIONAL:
                    return Presence.Optional;

                case PresenceCodec.SBE_CONSTANT:
                    return Presence.Constant;
            }

            return Presence.Required;
        }

        public static PresenceCodec MapPresence(Presence presence)
        {
            switch (presence)
            {
                case Presence.Optional:
                    return PresenceCodec.SBE_OPTIONAL;

                case Presence.Constant:
                    return PresenceCodec.SBE_CONSTANT;
            }

            return PresenceCodec.SBE_REQUIRED;
        }
    }
}