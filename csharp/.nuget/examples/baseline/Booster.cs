/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Org.SbeTool.Sbe.Dll;

namespace Baseline
{
    public sealed partial class Booster
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

        public const int Size = 2;

        public BoostType BoostType
        {
            get
            {
                return (BoostType)_buffer.CharGet(_offset + 0);
            }
            set
            {
                _buffer.CharPut(_offset + 0, (byte)value);
            }
        }


        public const byte HorsePowerNullValue = (byte)255;
        public const byte HorsePowerMinValue = (byte)0;
        public const byte HorsePowerMaxValue = (byte)254;

        public byte HorsePower
        {
            get
            {
                return _buffer.Uint8Get(_offset + 1);
            }
            set
            {
                _buffer.Uint8Put(_offset + 1, value);
            }
        }

    }
}
