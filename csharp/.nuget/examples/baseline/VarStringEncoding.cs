/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Org.SbeTool.Sbe.Dll;

namespace Baseline
{
    public sealed partial class VarStringEncoding
    {
        private DirectBuffer _buffer;
        private int _offset;
        private int _actingVersion;

        public void Wrap(DirectBuffer buffer, int offset, int actingVersion)
        {
            _offset = offset;
            _actingVersion = actingVersion;
            _buffer = buffer;
        }

        public const int Size = -1;

        public const uint LengthNullValue = 4294967295U;
        public const uint LengthMinValue = 0U;
        public const uint LengthMaxValue = 1073741824U;

        public uint Length
        {
            get
            {
                return _buffer.Uint32GetLittleEndian(_offset + 0);
            }
            set
            {
                _buffer.Uint32PutLittleEndian(_offset + 0, value);
            }
        }


        public const byte VarDataNullValue = (byte)255;
        public const byte VarDataMinValue = (byte)0;
        public const byte VarDataMaxValue = (byte)254;
    }
}
