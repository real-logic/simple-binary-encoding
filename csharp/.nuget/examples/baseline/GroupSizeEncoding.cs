/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Org.SbeTool.Sbe.Dll;

namespace Baseline
{
    public sealed partial class GroupSizeEncoding
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

        public const int Size = 4;

        public const ushort BlockLengthNullValue = (ushort)65535;
        public const ushort BlockLengthMinValue = (ushort)0;
        public const ushort BlockLengthMaxValue = (ushort)65534;

        public ushort BlockLength
        {
            get
            {
                return _buffer.Uint16GetLittleEndian(_offset + 0);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 0, value);
            }
        }


        public const ushort NumInGroupNullValue = (ushort)65535;
        public const ushort NumInGroupMinValue = (ushort)0;
        public const ushort NumInGroupMaxValue = (ushort)65534;

        public ushort NumInGroup
        {
            get
            {
                return _buffer.Uint16GetLittleEndian(_offset + 2);
            }
            set
            {
                _buffer.Uint16PutLittleEndian(_offset + 2, value);
            }
        }

    }
}
