using Adaptive.SimpleBinaryEncoding.Util;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Optional encoding settings that can be associated with <seealso cref="Token"/>s.
    /// </summary>
    public class Encoding
    {
        private readonly PrimitiveType _primitiveType;
        private readonly Presence _presence;
        private readonly ByteOrder _byteOrder;
        private readonly PrimitiveValue _minVal;
        private readonly PrimitiveValue _maxVal;
        private readonly PrimitiveValue _nullVal;
        private readonly PrimitiveValue _constVal;
        private readonly string _characterEncoding;
        private readonly string _epoch;
        private readonly string _timeUnit;
        private readonly string _semanticType;

        public Encoding()
        {
            _primitiveType = null;
            _presence = Presence.Required;
            _byteOrder = ByteOrder.LittleEndian;
            _minVal = null;
            _maxVal = null;
            _nullVal = null;
            _constVal = null;
            _characterEncoding = null;
            _epoch = null;
            _timeUnit = null;
            _semanticType = null;
        }

        public Encoding(PrimitiveType primitiveType, Presence presence, ByteOrder byteOrder, PrimitiveValue minVal, PrimitiveValue maxVal, PrimitiveValue nullVal, PrimitiveValue constVal, string characterEncoding, string epoch, string timeUnit, string semanticType)
        {
            Verify.NotNull(presence, "presence");
            Verify.NotNull(byteOrder, "byteOrder");

            _primitiveType = primitiveType;
            _presence = presence;
            _byteOrder = byteOrder;
            _minVal = minVal;
            _maxVal = maxVal;
            _nullVal = nullVal;
            _constVal = constVal;
            _characterEncoding = characterEncoding;
            _epoch = epoch;
            _timeUnit = timeUnit;
            _semanticType = semanticType;
        }

        /// <summary>
        /// The <seealso cref="PrimitiveType"/> of this encoding.
        /// </summary>
        /// <value>the &lt;seealso cref=&quot;PrimitiveType&quot;/&gt; of this encoding.</value>
        public PrimitiveType PrimitiveType
        {
            get { return _primitiveType; }
        }

        /// <summary>
        /// The <seealso cref="ByteOrder"/> for this encoding.
        /// </summary>
        /// <value>the &lt;seealso cref=&quot;ByteOrder&quot;/&gt; for this encoding.</value>
        public ByteOrder ByteOrder
        {
            get { return _byteOrder; }
        }

        /// <summary>
        /// The min value for the token or null if not set.
        /// </summary>
        /// <value>the minVal for the token or null if not set.</value>
        public PrimitiveValue MinVal
        {
            get { return _minVal; }
        }


        /// <summary>
        /// The max value for the token or null if not set.
        /// </summary>
        /// <value>the maxVal for the token or null if not set.</value>
        public PrimitiveValue MaxVal
        {
            get { return _maxVal; }
        }

        /// <summary>
        /// The null value for the token or null if not set.
        /// </summary>
        /// <value>the nullVal for the token or null if not set.</value>
        public PrimitiveValue NullVal
        {
            get { return _nullVal; }
        }

        /// <summary>
        /// The constant value for the token or null if not set.
        /// </summary>
        /// <value>the constant value for the token or null if not set.</value>
        public PrimitiveValue ConstVal
        {
            get { return _constVal; }
        }

        /// <summary>
        /// Indicates the presence status of a field in a message.
        /// </summary>
        /// <value>indicates the presence status of a field in a message.</value>
        public Presence Presence
        {
            get { return _presence; }
        }

        /// <summary>
        /// The most applicable null value for the encoded type.
        /// </summary>
        /// <value>most applicable null value for the encoded type.</value>
        public PrimitiveValue ApplicableNullVal
        {
            get
            {
                if (null != _nullVal)
                {
                    return _nullVal;
                }

                return _primitiveType.NullVal;
            }
        }

        /// <summary>
        /// The most applicable min value for the encoded type.
        /// </summary>
        /// <value>most applicable min value for the encoded type.</value>
        public PrimitiveValue ApplicableMinVal
        {
            get
            {
                if (null != _minVal)
                {
                    return _minVal;
                }

                return _primitiveType.MinVal;
            }
        }


        /// <summary>
        /// The most applicable max value for the encoded type.
        /// </summary>
        /// <value>most applicable max value for the encoded type.</value>
        public PrimitiveValue ApplicableMaxVal
        {
            get
            {
                if (null != _maxVal)
                {
                    return _maxVal;
                }

                return _primitiveType.MaxVal;
            }
        }

        /// <summary>
        /// The character encoding for the token or null if not set.
        /// </summary>
        /// <value>the character encoding for the token or null if not set.</value>
        public string CharacterEncoding
        {
            get { return _characterEncoding; }
        }

        /// <summary>
        /// The epoch from which a timestamp is offset. The default is "unix".
        /// </summary>
        /// <value>the epoch from which a timestamp is offset.</value>
        public string Epoch
        {
            get { return _epoch; }
        }

        /// <summary>
        /// The time unit of the timestamp.
        /// </summary>
        /// <value>the time unit of the timestamp.</value>
        public string TimeUnit
        {
            get { return _timeUnit; }
        }

        /// <summary>
        /// The semantic type of an encoding which can have relevance to the application layer.
        /// </summary>
        /// <value>semantic type of an encoding which can have relevance to the application layer.</value>
        public string SemanticType
        {
            get { return _semanticType; }
        }

        public override string ToString()
        {
            return "Encoding{primitiveType=" + _primitiveType + ", presence=" + _presence + ", byteOrder=" + _byteOrder + ", minVal=" 
                + _minVal + ", maxVal=" + _maxVal + ", nullVal=" + _nullVal + ", constVal=" + _constVal + ", characterEncoding='" + _characterEncoding + '\'' + ", epoch='" + _epoch + '\'' 
                + ", timeUnit=" + _timeUnit + ", semanticType='" + _semanticType + '\'' + '}';
        }

        /// <summary>
        /// Builder to make <seealso cref="Encoding"/> easier to create.
        /// </summary>
        public class Builder
        {
            private PrimitiveType _primitiveType;
            private Presence _presence = ir.Presence.Required;
            private ByteOrder _byteOrder = SimpleBinaryEncoding.ByteOrder.LittleEndian;
            private PrimitiveValue _minVal;
            private PrimitiveValue _maxVal;
            private PrimitiveValue _nullVal;
            private PrimitiveValue _constVal;
            private string _characterEncoding;
            private string _epoch;
            private string _timeUnit;
            private string _semanticType;

            public Builder PrimitiveType(PrimitiveType primitiveType)
            {
                _primitiveType = primitiveType;
                return this;
            }

            public Builder Presence(Presence presence)
            {
                _presence = presence;
                return this;
            }

            public Builder ByteOrder(ByteOrder byteOrder)
            {
                _byteOrder = byteOrder;
                return this;
            }

            public Builder MinVal(PrimitiveValue minValue)
            {
                _minVal = minValue;
                return this;
            }

            public Builder MaxVal(PrimitiveValue maxValue)
            {
                _maxVal = maxValue;
                return this;
            }

            public Builder NullVal(PrimitiveValue nullValue)
            {
                _nullVal = nullValue;
                return this;
            }

            public Builder ConstVal(PrimitiveValue constValue)
            {
                _constVal = constValue;
                return this;
            }

            public Builder CharacterEncoding(string characterEncoding)
            {
                _characterEncoding = characterEncoding;
                return this;
            }

            public Builder Epoch(string epoch)
            {
                _epoch = epoch;
                return this;
            }

            public Builder TimeUnit(string timeUnit)
            {
                _timeUnit = timeUnit;
                return this;
            }

            public Builder SemanticType(string semanticType)
            {
                _semanticType = semanticType;
                return this;
            }

            public Encoding Build()
            {
                return new Encoding(_primitiveType, _presence, _byteOrder, _minVal, _maxVal, _nullVal, _constVal, _characterEncoding, _epoch, _timeUnit, _semanticType);
            }
        }
    }
}