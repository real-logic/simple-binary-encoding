/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public enum MDUpdateAction : byte
    {
        New = 0,
        Change = 1,
        Delete = 2,
        DeleteThru = 3,
        DeleteFrom = 4,
        Overlay = 5,
        NULL_VALUE = 255
    }
}
