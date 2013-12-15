package sbe

/**
 * Allocation free Option types. Based on the work described in https://github.com/scala/scala/pull/2848
 */
object Optional {

  final implicit class OptionalChar(val value: Byte) extends AnyVal {
    def isEmpty = value == 0
    def get = value
  }

  final implicit class OptionalByte(val value: Byte) extends AnyVal {
    def isEmpty = value == -128
    def get = value
  }

  final implicit class OptionalUnsignedByte(val value: Short) extends AnyVal {
    def isEmpty = value == 255
    def get = value
  }

  final implicit class OptionalShort(val value: Short) extends AnyVal {
    def isEmpty = value == -32768
    def get = value
  }

  final implicit class OptionalUnsignedShort(val value: Int) extends AnyVal {
    def isEmpty = value == 65535
    def get = value
  }

  final implicit class OptionalInt(val value: Int) extends AnyVal {
    def isEmpty = value == -2147483648
    def get = value
  }

  final implicit class OptionalUnsignedInt(val value: Long) extends AnyVal {
    def isEmpty = value == 4294967294L
    def get = value
  }

  final implicit class OptionalLong(val value: Long) extends AnyVal {
    def isEmpty = value == 0x8000000000000000L
    def get = value
  }

  final implicit class OptionalUnsignedLong(val value: Long) extends AnyVal {
    def isEmpty = value == 0L
    def get = value
  }

  final implicit class OptionalFloat(val value: Float) extends AnyVal {
    def isEmpty = value != value // java.lang.Float.isNaN check is v != v
    def get = value
  }

  final implicit class OptionalDouble(val value: Double) extends AnyVal {
    def isEmpty = value != value // java.lang.Double.isNaN check is v != v
    def get = value
  }
}
