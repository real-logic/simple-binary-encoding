namespace Adaptive.SimpleBinaryEncoding
{
    /// <summary>
    /// Primitive types from which all other types are composed.
    /// 
    /// <p/>
    /// <table>
    ///     <thead>
    ///         <tr>
    ///             <th>PrimitiveType Type</th>
    ///             <th>Description</th>
    ///             <th>Length (octets)</th>
    ///         </tr>
    ///     </thead>
    ///     <tbody>
    ///         <tr>
    ///             <td>char</td>
    ///             <td>Character</td>
    ///             <td>1</td>
    ///         </tr>
    ///         <tr>
    ///             <td>int8</td>
    ///             <td>Signed byte</td>
    ///             <td>1</td>
    ///         </tr>
    ///         <tr>
    ///             <td>uint8</td>
    ///             <td>Unsigned Byte / single byte character</td>
    ///             <td>1</td>
    ///         </tr>
    ///         <tr>
    ///             <td>int16</td>
    ///             <td>Signed integer</td>
    ///             <td>2</td>
    ///         </tr>
    ///         <tr>
    ///             <td>uint16</td>
    ///             <td>Unsigned integer</td>
    ///             <td>2</td>
    ///         </tr>
    ///         <tr>
    ///             <td>int32</td>
    ///             <td>Signed integer</td>
    ///             <td>4</td>
    ///         </tr>
    ///         <tr>
    ///             <td>uint32</td>
    ///             <td>Unsigned integer</td>
    ///             <td>4</td>
    ///         </tr>
    ///         <tr>
    ///             <td>int64</td>
    ///             <td>Signed integer</td>
    ///             <td>8</td>
    ///         </tr>
    ///         <tr>
    ///             <td>uint64</td>
    ///             <td>Unsigned integer</td>
    ///             <td>8</td>
    ///         </tr>
    ///         <tr>
    ///             <td>float</td>
    ///             <td>Single precision floating point</td>
    ///             <td>4</td>
    ///         </tr>
    ///         <tr>
    ///             <td>double</td>
    ///             <td>Double precision floating point</td>
    ///             <td>8</td>
    ///         </tr>
    ///     </tbody>
    /// </table>
    /// </summary>
    public class PrimitiveType
    {
        public static readonly PrimitiveType SbeChar = new PrimitiveType("char", 1, PrimitiveValue.MinValueChar, PrimitiveValue.MaxValueChar, PrimitiveValue.NullValueChar, SbePrimitiveType.Char);
        public static readonly PrimitiveType SbeInt8 = new PrimitiveType("int8", 1, PrimitiveValue.MinValueInt8, PrimitiveValue.MaxValueInt8, PrimitiveValue.NullValueInt8, SbePrimitiveType.Int8);
        public static readonly PrimitiveType SbeInt16 = new PrimitiveType("int16", 2, PrimitiveValue.MinValueInt16, PrimitiveValue.MaxValueInt16, PrimitiveValue.NullValueInt16, SbePrimitiveType.Int16);
        public static readonly PrimitiveType SbeInt32 = new PrimitiveType("int32", 4, PrimitiveValue.MinValueInt32, PrimitiveValue.MaxValueInt32, PrimitiveValue.NullValueInt32, SbePrimitiveType.Int32);
        public static readonly PrimitiveType SbeInt64 = new PrimitiveType("int64", 8, PrimitiveValue.MinValueInt64, PrimitiveValue.MaxValueInt64, PrimitiveValue.NullValueInt64, SbePrimitiveType.Int64);
        public static readonly PrimitiveType SbeUInt8 = new PrimitiveType("uint8", 1, PrimitiveValue.MinValueUint8, PrimitiveValue.MaxValueUint8, PrimitiveValue.NullValueUint8, SbePrimitiveType.UInt8);
        public static readonly PrimitiveType SbeUInt16 = new PrimitiveType("uint16", 2, PrimitiveValue.MinValueUint16, PrimitiveValue.MaxValueUint16, PrimitiveValue.NullValueUint16, SbePrimitiveType.UInt16);
        public static readonly PrimitiveType SbeUInt32 = new PrimitiveType("uint32", 4, PrimitiveValue.MinValueUint32, PrimitiveValue.MaxValueUint32, PrimitiveValue.NullValueUint32, SbePrimitiveType.UInt32);
        public static readonly PrimitiveType SbeUInt64 = new PrimitiveType("uint64", 8, PrimitiveValue.MinValueUint64, PrimitiveValue.MaxValueUint64, PrimitiveValue.NullValueUint64, SbePrimitiveType.UInt64);
        public static readonly PrimitiveType SbeFloat = new PrimitiveType("float", 4, PrimitiveValue.MinValueFloat, PrimitiveValue.MaxValueFloat, PrimitiveValue.NullValueFloat, SbePrimitiveType.Float);
        public static readonly PrimitiveType SbeDouble = new PrimitiveType("double", 8, PrimitiveValue.MinValueDouble, PrimitiveValue.MaxValueDouble, PrimitiveValue.NullValueDouble, SbePrimitiveType.Double);

        private readonly PrimitiveValue _maxVal;
        private readonly PrimitiveValue _minVal;
        private readonly string _name;
        private readonly PrimitiveValue _nullVal;
        private readonly int _size;
        private readonly SbePrimitiveType _sbePrimitiveType;

        internal PrimitiveType(string name, int size, long minVal, long maxVal, long nullVal, SbePrimitiveType sbePrimitiveType)
        {
            _name = name;
            _size = size;
            _sbePrimitiveType = sbePrimitiveType;
            _minVal = new PrimitiveValue(minVal, size);
            _maxVal = new PrimitiveValue(maxVal, size);
            _nullVal = new PrimitiveValue(nullVal, size);
        }

        internal PrimitiveType(string name, int size, double minVal, double maxVal, double nullVal, SbePrimitiveType sbePrimitiveType)
        {
            _name = name;
            _size = size;
            _sbePrimitiveType = sbePrimitiveType;
            _minVal = new PrimitiveValue(minVal, size);
            _maxVal = new PrimitiveValue(maxVal, size);
            _nullVal = new PrimitiveValue(nullVal, size);
        }

        /// <summary>
        ///     The name of the primitive type as a String.
        /// </summary>
        /// <value>the name as a String</value>
        public string PrimitiveName
        {
            get { return _name; }
        }

        /// <summary>
        ///     The size of the primitive type in octets.
        /// </summary>
        /// <value>size (in octets) of the primitive type</value>
        public int Size
        {
            get { return _size; }
        }

        /// <summary>
        ///     The minVal of the primitive type.
        /// </summary>
        /// <value>default minVal of the primitive type</value>
        public PrimitiveValue MinVal
        {
            get { return _minVal; }
        }

        /// <summary>
        ///     The maxVal of the primitive type.
        /// </summary>
        /// <value>default maxVal of the primitive type</value>
        public PrimitiveValue MaxVal
        {
            get { return _maxVal; }
        }

        /// <summary>
        ///     The nullVal of the primitive type.
        /// </summary>
        /// <value>default nullVal of the primitive type</value>
        public PrimitiveValue NullVal
        {
            get { return _nullVal; }
        }

        public SbePrimitiveType Type
        {
            get { return _sbePrimitiveType; }
        }
    }
}