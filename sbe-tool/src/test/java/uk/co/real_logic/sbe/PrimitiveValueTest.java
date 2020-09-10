package uk.co.real_logic.sbe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.co.real_logic.sbe.PrimitiveValue.*;

class PrimitiveValueTest
{
    @Test
    void compareToThrowsNullPointerExceptionIfValueIsNull()
    {
        final PrimitiveValue value = new PrimitiveValue(5, 4);
        assertThrows(NullPointerException.class, () -> value.compareTo(null));
    }

    @Test
    void compareToDifferentRepresentationLongBeforeDouble()
    {
        final PrimitiveValue value = new PrimitiveValue(5, 4);
        final PrimitiveValue value2 = new PrimitiveValue(1.5, 4);
        assertEquals(-1, value.compareTo(value2));
    }

    @Test
    void compareToDifferentRepresentationDoubleBeforeByteArray()
    {
        final PrimitiveValue value = new PrimitiveValue(1.5, 4);
        final PrimitiveValue value2 = new PrimitiveValue(new byte[]{ 1, 0, 1 }, US_ASCII.name(), 3);
        assertEquals(-1, value.compareTo(value2));
    }

    @Test
    void compareToSmallerLengthBeforeBiggerLength()
    {
        final PrimitiveValue value = new PrimitiveValue(5, 4);
        final PrimitiveValue value2 = new PrimitiveValue(2, 8);
        assertEquals(-1, value.compareTo(value2));
    }

    @ParameterizedTest
    @MethodSource("longValues")
    void compareToLongValue(final long value, final long value2, final int expectedResult)
    {
        final PrimitiveValue primitiveValue = new PrimitiveValue(value, 8);
        final PrimitiveValue primitiveValue2 = new PrimitiveValue(value2, 8);
        assertEquals(expectedResult, primitiveValue.compareTo(primitiveValue2));
    }

    @ParameterizedTest
    @MethodSource("doubleValues")
    void compareToDoubleValue(final double value, final double value2, final int expectedResult)
    {
        final PrimitiveValue primitiveValue = new PrimitiveValue(value, 8);
        final PrimitiveValue primitiveValue2 = new PrimitiveValue(value2, 8);
        assertEquals(expectedResult, primitiveValue.compareTo(primitiveValue2));
    }

    @Test
    void compareToByteArrayDifferentLengthShortBeforeLong()
    {
        final PrimitiveValue primitiveValue = new PrimitiveValue(new byte[]{ 127, 127, 127 }, US_ASCII.name(), 5);
        final PrimitiveValue primitiveValue2 = new PrimitiveValue(new byte[]{ 1, 1, 1, 1 }, US_ASCII.name(), 5);
        assertEquals(-1, primitiveValue.compareTo(primitiveValue2));
    }

    @Test
    void compareToByteArray()
    {
        final PrimitiveValue primitiveValue = new PrimitiveValue(new byte[]{ 0, 1, 2, 3, 4, 5 }, US_ASCII.name(), 6);
        final PrimitiveValue primitiveValue2 =
            new PrimitiveValue(new byte[]{ 0, 1, 20, 30, 40, 5 }, US_ASCII.name(), 6);

        assertEquals(-1, primitiveValue.compareTo(primitiveValue2));
        assertEquals(1, primitiveValue2.compareTo(primitiveValue));
    }

    @Test
    void compareToByteArrayEqualValues()
    {
        final PrimitiveValue primitiveValue = new PrimitiveValue(new byte[]{ 0, 1, 2, 3, 4, 5 }, US_ASCII.name(), 10);
        final PrimitiveValue primitiveValue2 = new PrimitiveValue(new byte[]{ 0, 1, 2, 3, 4, 5 }, US_ASCII.name(), 10);

        assertEquals(0, primitiveValue.compareTo(primitiveValue2));
    }

    private static List<Arguments> longValues()
    {
        return asList(
            arguments(Long.MIN_VALUE, Long.MAX_VALUE, -1),
            arguments(-400, 0, -1),
            arguments(5, 5, 0),
            arguments(-5, -5, 0),
            arguments(-400, -1000, 1),
            arguments(222, 3, 1),
            arguments(6, -3, 1),
            arguments(MIN_VALUE_INT64, MAX_VALUE_INT64, -1),
            arguments(MIN_VALUE_INT64, NULL_VALUE_INT64, 1),
            arguments(MAX_VALUE_INT64, NULL_VALUE_INT64, 1)
        );
    }

    private static List<Arguments> doubleValues()
    {
        return asList(
            arguments(MIN_VALUE_DOUBLE, MAX_VALUE_DOUBLE, -1),
            arguments(MAX_VALUE_DOUBLE, MIN_VALUE_DOUBLE, 1),
            arguments(MAX_VALUE_DOUBLE, MAX_VALUE_DOUBLE, 0),
            arguments(MIN_VALUE_DOUBLE, MIN_VALUE_DOUBLE, 0),
            arguments(5.0, 5.0, 0),
            arguments(-5.1, -5.05, -1),
            arguments(NULL_VALUE_DOUBLE, NULL_VALUE_DOUBLE, 0),
            arguments(NULL_VALUE_DOUBLE, MIN_VALUE_DOUBLE, 1),
            arguments(MIN_VALUE_DOUBLE, NULL_VALUE_DOUBLE, -1)
        );
    }
}