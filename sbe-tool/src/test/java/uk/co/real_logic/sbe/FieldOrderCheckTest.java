/*
 * Copyright 2013-2023 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.real_logic.sbe;

import order.check.*;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class FieldOrderCheckTest
{
    private static final Class<IllegalStateException> INCORRECT_ORDER_EXCEPTION_CLASS = IllegalStateException.class;
    private static final int OFFSET = 0;
    private final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();

    @BeforeAll
    static void assumeDebugMode()
    {
        final boolean productionMode = Boolean.getBoolean("agrona.disable.bounds.checks");
        assumeFalse(productionMode);
    }

    @Test
    void allowsEncodingAndDecodingVariableLengthFieldsInSchemaDefinedOrder()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");

        final MultipleVarLengthDecoder decoder = new MultipleVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        assertThat(decoder.b(), equalTo("abc"));
        assertThat(decoder.c(), equalTo("def"));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelPrimitiveFields()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");
        encoder.a(43);

        final MultipleVarLengthDecoder decoder = new MultipleVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(43));
        assertThat(decoder.b(), equalTo("abc"));
        assertThat(decoder.c(), equalTo("def"));
    }

    @Test
    void disallowsSkippingEncodingOfVariableLengthField1()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.c("def"));
    }

    @Test
    void disallowsSkippingEncodingOfVariableLengthField2()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final CharSequence def = new StringBuilder("def");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.c(def));
    }

    @Test
    void disallowsSkippingEncodingOfVariableLengthField3()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final byte[] value = "def".getBytes();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.putC(value, 0, value.length));
    }

    @Test
    void disallowsSkippingEncodingOfVariableLengthField4()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final byte[] value = "def".getBytes();
        final UnsafeBuffer buffer = new UnsafeBuffer(value);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.putC(buffer, 0, buffer.capacity()));
    }

    @Test
    void disallowsReEncodingEarlierVariableLengthFields()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.b("ghi"));
    }

    @Test
    void disallowsReEncodingLatestVariableLengthField()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.c("ghi"));
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField1()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::c);
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField2()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> decoder.wrapC(new UnsafeBuffer()));
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField3()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> decoder.getC(new StringBuilder()));
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField4()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> decoder.getC(new byte[3], 0, 3));
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField5()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::cLength);
    }

    @Test
    void disallowsSkippingDecodingOfVariableLengthField6()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> decoder.getC(new ExpandableArrayBuffer(), 0, 3));
    }

    @Test
    void disallowsReDecodingEarlierVariableLengthField()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThat(decoder.b(), equalTo("abc"));
        assertThat(decoder.c(), equalTo("def"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::b);
    }

    @Test
    void disallowsReDecodingLatestVariableLengthField()
    {
        final MultipleVarLengthDecoder decoder = decodeUntilVarLengthFields();
        assertThat(decoder.b(), equalTo("abc"));
        assertThat(decoder.c(), equalTo("def"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::c);
    }

    private MultipleVarLengthDecoder decodeUntilVarLengthFields()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");

        final MultipleVarLengthDecoder decoder = new MultipleVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        return decoder;
    }

    @Test
    void allowsEncodingAndDecodingGroupAndVariableLengthFieldsInSchemaDefinedOrder()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.next().c(), equalTo(2));
        assertThat(decoder.d(), equalTo("abc"));
    }

    @Test
    void allowsEncodingAndDecodingEmptyGroupAndVariableLengthFieldsInSchemaDefinedOrder()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(0);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(0));
        assertThat(decoder.d(), equalTo("abc"));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingPrimitiveFieldInGroupElementAfterTopLevelVariableLengthField()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final GroupAndVarLengthEncoder.BEncoder bEncoder = encoder.bCount(2);
        bEncoder
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");
        bEncoder.c(3);

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.next().c(), equalTo(3));
        assertThat(decoder.d(), equalTo("abc"));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReWrappingGroupDecoderAfterAccessingLength()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final GroupAndVarLengthEncoder.BEncoder bEncoder = encoder.bCount(2);
        bEncoder
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        assertThat(decoder.b().count(), equalTo(2));
        final GroupAndVarLengthDecoder.BDecoder b = decoder.b();
        assertThat(b.next().c(), equalTo(1));
        assertThat(b.next().c(), equalTo(3));
        assertThat(decoder.d(), equalTo("abc"));
    }

    @Test
    void disallowsEncodingGroupElementBeforeCallingNext()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final GroupAndVarLengthEncoder.BEncoder bEncoder = encoder.bCount(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> bEncoder.c(1));
    }

    @Test
    void disallowsDecodingGroupElementBeforeCallingNext()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bs::c);
    }

    @Test
    void disallowsSkippingEncodingOfGroup()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.d("abc"));
    }

    @Test
    void disallowsReEncodingVariableLengthFieldAfterGroup()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.d("def"));
    }

    @Test
    void disallowsReEncodingGroupLength()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.bCount(1));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingGroupElementBlockFieldAfterTopLevelVariableLengthField()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final GroupAndVarLengthEncoder.BEncoder b = encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");
        b.c(3);

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.next().c(), equalTo(3));
        assertThat(decoder.d(), equalTo("abc"));
    }

    @Test
    void disallowsMissedDecodingOfGroupBeforeVariableLengthField()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::d);
    }

    @Test
    void disallowsReDecodingVariableLengthFieldAfterGroup()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.next().c(), equalTo(2));
        assertThat(decoder.d(), equalTo("abc"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::d);
    }

    @Test
    void disallowsReDecodingGroupBeforeVariableLengthField()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .next()
            .c(2);
        encoder.d("abc");

        final GroupAndVarLengthDecoder decoder = new GroupAndVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final GroupAndVarLengthDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.next().c(), equalTo(2));
        assertThat(decoder.d(), equalTo("abc"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::b);
    }

    @Test
    void allowsEncodingAndDecodingVariableLengthFieldInsideGroupInSchemaDefinedOrder()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .d("abc")
            .next()
            .c(2)
            .d("def");
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.d(), equalTo("abc"));
        assertThat(bs.next().c(), equalTo(2));
        assertThat(bs.d(), equalTo("def"));
        assertThat(decoder.e(), equalTo("ghi"));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingGroupElementPrimitiveFieldAfterElementVariableLengthField()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final VarLengthInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1);
        bEncoder
            .next()
            .c(1)
            .d("abc");
        bEncoder.c(2);
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(1));
        assertThat(bs.next().c(), equalTo(2));
        assertThat(bs.d(), equalTo("abc"));
        assertThat(decoder.e(), equalTo("ghi"));
    }

    @Test
    void disallowsMissedGroupElementVariableLengthFieldToEncodeAtTopLevel()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(1).next().c(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.e("abc"));
    }

    @Test
    void disallowsMissedGroupElementVariableLengthFieldToEncodeNextElement()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final VarLengthInsideGroupEncoder.BEncoder b = encoder.bCount(2)
            .next();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, b::next);
    }

    @Test
    void disallowsMissedGroupElementEncoding()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .d("abc");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.e("abc"));
    }

    @Test
    void disallowsReEncodingGroupElementVariableLengthField()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final VarLengthInsideGroupEncoder.BEncoder b = encoder.bCount(1)
            .next()
            .c(1)
            .d("abc");
        encoder.e("def");
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.d("ghi"));
    }

    @Test
    void disallowsReDecodingGroupElementVariableLengthField()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .d("abc")
            .next()
            .c(2)
            .d("def");
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThat(bs.d(), equalTo("abc"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bs::d);
    }

    @Test
    void disallowsMissedDecodingOfGroupElementVariableLengthFieldToNextElement()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .d("abc")
            .next()
            .c(2)
            .d("def");
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bs::next);
    }

    @Test
    void disallowsMissedDecodingOfGroupElementVariableLengthFieldToTopLevel()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(1)
            .next()
            .c(1)
            .d("abc");
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(1));
        assertThat(bs.next().c(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::e);
    }

    @Test
    void disallowsMissedDecodingOfGroupElement()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(2)
            .next()
            .c(1)
            .d("abc")
            .next()
            .c(2)
            .d("def");
        encoder.e("ghi");

        final VarLengthInsideGroupDecoder decoder = new VarLengthInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final VarLengthInsideGroupDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        assertThat(bs.next().c(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::e);
    }

    @Test
    void allowsEncodingNestedGroupsInSchemaDefinedOrder()
    {
        final NestedGroupsEncoder encoder = new NestedGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final NestedGroupsEncoder.BEncoder b = encoder.bCount(2)
            .next();
        b.c(1);
        b.dCount(2)
            .next()
            .e(2)
            .next()
            .e(3);
        b.fCount(1)
            .next()
            .g(4);
        b.next();
        b.c(5);
        b.dCount(1)
            .next()
            .e(6);
        b.fCount(1)
            .next()
            .g(7);
        encoder.hCount(1)
            .next()
            .i(8);

        final NestedGroupsDecoder decoder = new NestedGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final NestedGroupsDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(2));
        final NestedGroupsDecoder.BDecoder b0 = bs.next();
        assertThat(b0.c(), equalTo(1));
        final NestedGroupsDecoder.BDecoder.DDecoder b0ds = b0.d();
        assertThat(b0ds.count(), equalTo(2));
        assertThat(b0ds.next().e(), equalTo(2));
        assertThat(b0ds.next().e(), equalTo(3));
        final NestedGroupsDecoder.BDecoder.FDecoder b0fs = b0.f();
        assertThat(b0fs.count(), equalTo(1));
        assertThat(b0fs.next().g(), equalTo(4));
        final NestedGroupsDecoder.BDecoder b1 = bs.next();
        assertThat(b1.c(), equalTo(5));
        final NestedGroupsDecoder.BDecoder.DDecoder b1ds = b1.d();
        assertThat(b1ds.count(), equalTo(1));
        assertThat(b1ds.next().e(), equalTo(6));
        final NestedGroupsDecoder.BDecoder.FDecoder b1fs = b1.f();
        assertThat(b1fs.count(), equalTo(1));
        assertThat(b1fs.next().g(), equalTo(7));
        final NestedGroupsDecoder.HDecoder hs = decoder.h();
        assertThat(hs.count(), equalTo(1));
        assertThat(hs.next().i(), equalTo(8));
    }

    @Test
    void allowsEncodingEmptyNestedGroupsInSchemaDefinedOrder()
    {
        final NestedGroupsEncoder encoder = new NestedGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(0);
        encoder.hCount(0);

        final NestedGroupsDecoder decoder = new NestedGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        final NestedGroupsDecoder.BDecoder bs = decoder.b();
        assertThat(bs.count(), equalTo(0));
        final NestedGroupsDecoder.HDecoder hs = decoder.h();
        assertThat(hs.count(), equalTo(0));
    }

    @Test
    void disallowsMissedEncodingOfNestedGroup()
    {
        final NestedGroupsEncoder encoder = new NestedGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final NestedGroupsEncoder.BEncoder b = encoder.bCount(1)
            .next()
            .c(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.fCount(1));
    }

    @Test
    void allowsEncodingAndDecodingCompositeInsideGroupInSchemaDefinedOrder()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
    }

    @Test
    void disallowsEncodingCompositeInsideGroupBeforeCallingNext()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        final CompositeInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bEncoder::c);
    }

    @Test
    void disallowsDecodingCompositeInsideGroupBeforeCallingNext()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, b::c);
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelCompositeViaReWrap()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);
        encoder.a().x(5).y(6);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(5));
        assertThat(a.y(), equalTo(6));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
    }

    @Test
    void allowsReEncodingTopLevelCompositeViaEncoderReference()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        final PointEncoder aEncoder = encoder.a();
        aEncoder.x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);
        aEncoder.x(5).y(6);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(5));
        assertThat(a.y(), equalTo(6));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
    }

    @Test
    void allowsReEncodingGroupElementCompositeViaReWrap()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        final CompositeInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1).next();
        bEncoder.c().x(3).y(4);
        bEncoder.c().x(5).y(6);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(5));
        assertThat(c.y(), equalTo(6));
    }

    @Test
    void allowsReEncodingGroupElementCompositeViaEncoderReference()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        final CompositeInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1).next();
        final PointEncoder cEncoder = bEncoder.c();
        cEncoder.x(3).y(4);
        cEncoder.x(5).y(6);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(5));
        assertThat(c.y(), equalTo(6));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReDecodingTopLevelCompositeViaReWrap()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a1 = decoder.a();
        assertThat(a1.x(), equalTo(1));
        assertThat(a1.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
        final PointDecoder a2 = decoder.a();
        assertThat(a2.x(), equalTo(1));
        assertThat(a2.y(), equalTo(2));
    }

    @Test
    void allowsReDecodingTopLevelCompositeViaEncoderReference()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
    }

    @Test
    void allowsReDecodingGroupElementCompositeViaReWrap()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c1 = b.next().c();
        assertThat(c1.x(), equalTo(3));
        assertThat(c1.y(), equalTo(4));
        final PointDecoder c2 = b.c();
        assertThat(c2.x(), equalTo(3));
        assertThat(c2.y(), equalTo(4));
    }

    @Test
    void allowsReDecodingGroupElementCompositeViaEncoderReference()
    {
        final CompositeInsideGroupEncoder encoder = new CompositeInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().x(1).y(2);
        encoder.bCount(1)
            .next()
            .c().x(3).y(4);

        final CompositeInsideGroupDecoder decoder = new CompositeInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final PointDecoder a = decoder.a();
        assertThat(a.x(), equalTo(1));
        assertThat(a.y(), equalTo(2));
        final CompositeInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final PointDecoder c = b.next().c();
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
        assertThat(c.x(), equalTo(3));
        assertThat(c.y(), equalTo(4));
    }

    @Test
    void allowsNewDecoderToDecodeAddedPrimitiveField()
    {
        final AddPrimitiveV1Encoder encoder = new AddPrimitiveV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).b(2);

        final AddPrimitiveV1Decoder decoder = new AddPrimitiveV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.b(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingPrimitiveFieldAsNullValue()
    {
        final AddPrimitiveV0Encoder encoder = new AddPrimitiveV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1);

        modifyHeaderToLookLikeVersion0();

        final AddPrimitiveV1Decoder decoder = new AddPrimitiveV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.b(), equalTo(AddPrimitiveV1Decoder.bNullValue()));
    }

    @Test
    void allowsNewDecoderToDecodeAddedPrimitiveFieldBeforeGroup()
    {
        final AddPrimitiveBeforeGroupV1Encoder encoder = new AddPrimitiveBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(3).bCount(1).next().c(2);

        final AddPrimitiveBeforeGroupV1Decoder decoder = new AddPrimitiveBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), equalTo(3));
        final AddPrimitiveBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingPrimitiveFieldBeforeGroupAsNullValue()
    {
        final AddPrimitiveBeforeGroupV0Encoder encoder = new AddPrimitiveBeforeGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddPrimitiveBeforeGroupV1Decoder decoder = new AddPrimitiveBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), equalTo(AddPrimitiveBeforeGroupV1Decoder.dNullValue()));
        final AddPrimitiveBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedPrimitiveFieldBeforeGroup()
    {
        final AddPrimitiveBeforeGroupV1Encoder encoder = new AddPrimitiveBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(3).bCount(1).next().c(2);

        final AddPrimitiveBeforeGroupV1Decoder decoder = new AddPrimitiveBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsOldDecoderToSkipAddedPrimitiveFieldBeforeGroup()
    {
        final AddPrimitiveBeforeGroupV1Encoder encoder = new AddPrimitiveBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(3).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion1();

        final AddPrimitiveBeforeGroupV0Decoder decoder = new AddPrimitiveBeforeGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveBeforeGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeAddedPrimitiveFieldBeforeVarData()
    {
        final AddPrimitiveBeforeVarDataV1Encoder encoder = new AddPrimitiveBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).c(3).b("abc");

        final AddPrimitiveBeforeVarDataV1Decoder decoder = new AddPrimitiveBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.c(), equalTo(3));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsNewDecoderToDecodeMissingPrimitiveFieldBeforeVarDataAsNullValue()
    {
        final AddPrimitiveBeforeVarDataV0Encoder encoder = new AddPrimitiveBeforeVarDataV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).b("abc");

        modifyHeaderToLookLikeVersion0();

        final AddPrimitiveBeforeVarDataV1Decoder decoder = new AddPrimitiveBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.c(), equalTo(AddPrimitiveBeforeVarDataV1Decoder.cNullValue()));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedPrimitiveFieldBeforeVarData()
    {
        final AddPrimitiveBeforeVarDataV1Encoder encoder = new AddPrimitiveBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).c(3).b("abc");

        final AddPrimitiveBeforeVarDataV1Decoder decoder = new AddPrimitiveBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsOldDecoderToSkipAddedPrimitiveFieldBeforeVarData()
    {
        final AddPrimitiveBeforeVarDataV1Encoder encoder = new AddPrimitiveBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).c(3).b("abc");

        modifyHeaderToLookLikeVersion1();

        final AddPrimitiveBeforeVarDataV0Decoder decoder = new AddPrimitiveBeforeVarDataV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsNewDecoderToDecodeAddedPrimitiveFieldInsideGroup()
    {
        final AddPrimitiveInsideGroupV1Encoder encoder = new AddPrimitiveInsideGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2).d(3);

        final AddPrimitiveInsideGroupV1Decoder decoder = new AddPrimitiveInsideGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveInsideGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
        assertThat(b.d(), equalTo(3));
    }

    @Test
    void allowsNewDecoderToDecodeMissingPrimitiveFieldInsideGroupAsNullValue()
    {
        final AddPrimitiveInsideGroupV0Encoder encoder = new AddPrimitiveInsideGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddPrimitiveInsideGroupV1Decoder decoder = new AddPrimitiveInsideGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveInsideGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
        assertThat(b.d(), equalTo(AddPrimitiveInsideGroupV1Decoder.BDecoder.dNullValue()));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedPrimitiveFieldInsideGroup()
    {
        final AddPrimitiveInsideGroupV1Encoder encoder = new AddPrimitiveInsideGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(2).next().c(2).d(3).next().c(4).d(5);

        final AddPrimitiveInsideGroupV1Decoder decoder = new AddPrimitiveInsideGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveInsideGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(2));
        assertThat(b.next().c(), equalTo(2));
        assertThat(b.next().c(), equalTo(4));
    }

    @Test
    void allowsOldDecoderToSkipAddedPrimitiveFieldInsideGroup()
    {
        final AddPrimitiveInsideGroupV1Encoder encoder = new AddPrimitiveInsideGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(2).next().c(2).d(3).next().c(4).d(5);

        modifyHeaderToLookLikeVersion1();

        final AddPrimitiveInsideGroupV0Decoder decoder = new AddPrimitiveInsideGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddPrimitiveInsideGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(2));
        assertThat(b.next().c(), equalTo(2));
        assertThat(b.next().c(), equalTo(4));
    }

    @Test
    void allowsNewDecoderToDecodeAddedGroupBeforeVarData()
    {
        final AddGroupBeforeVarDataV1Encoder encoder = new AddGroupBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).cCount(1).next().d(2);
        encoder.b("abc");

        final AddGroupBeforeVarDataV1Decoder decoder = new AddGroupBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddGroupBeforeVarDataV1Decoder.CDecoder c = decoder.c();
        assertThat(c.count(), equalTo(1));
        assertThat(c.next().d(), equalTo(2));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsNewDecoderToDecodeMissingGroupBeforeVarDataAsNullValue()
    {
        final AddGroupBeforeVarDataV0Encoder encoder = new AddGroupBeforeVarDataV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).b("abc");

        modifyHeaderToLookLikeVersion0();

        final AddGroupBeforeVarDataV1Decoder decoder = new AddGroupBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddGroupBeforeVarDataV1Decoder.CDecoder c = decoder.c();
        assertThat(c.count(), equalTo(0));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void allowsNewDecoderToSkipMissingGroupBeforeVarData()
    {
        final AddGroupBeforeVarDataV0Encoder encoder = new AddGroupBeforeVarDataV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).b("abc");

        modifyHeaderToLookLikeVersion0();

        final AddGroupBeforeVarDataV1Decoder decoder = new AddGroupBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.b(), equalTo("abc"));
    }

    @Test
    void disallowsNewDecoderToSkipPresentButAddedGroupBeforeVarData()
    {
        final AddGroupBeforeVarDataV1Encoder encoder = new AddGroupBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).cCount(1).next().d(2);
        encoder.b("abc");

        final AddGroupBeforeVarDataV1Decoder decoder = new AddGroupBeforeVarDataV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::b);
    }

    @Test
    void allowsOldDecoderToSkipAddedGroupBeforeVarData()
    {
        final AddGroupBeforeVarDataV1Encoder encoder = new AddGroupBeforeVarDataV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        messageHeaderEncoder.numGroups(1);
        encoder.a(1).cCount(1).next().d(2);
        encoder.b("abc");

        modifyHeaderToLookLikeVersion1();

        final AddGroupBeforeVarDataV0Decoder decoder = new AddGroupBeforeVarDataV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));

        for (int i = 0; i < messageHeaderDecoder.numGroups(); i++)
        {
            skipGroup(decoder);
        }

        assertThat(decoder.b(), equalTo("abc"));
    }

    private void skipGroup(final AddGroupBeforeVarDataV0Decoder decoder)
    {
        final GroupSizeEncodingDecoder groupSizeEncodingDecoder = new GroupSizeEncodingDecoder()
            .wrap(buffer, decoder.limit());
        final int bytesToSkip = groupSizeEncodingDecoder.encodedLength() +
            groupSizeEncodingDecoder.blockLength() * groupSizeEncodingDecoder.numInGroup();
        decoder.limit(decoder.limit() + bytesToSkip);
    }

    @Test
    void allowsNewDecoderToDecodeAddedEnumFieldBeforeGroup()
    {
        final AddEnumBeforeGroupV1Encoder encoder = new AddEnumBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(Direction.BUY).bCount(1).next().c(2);

        final AddEnumBeforeGroupV1Decoder decoder = new AddEnumBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), equalTo(Direction.BUY));
        final AddEnumBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingEnumFieldBeforeGroupAsNullValue()
    {
        final AddEnumBeforeGroupV0Encoder encoder = new AddEnumBeforeGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddEnumBeforeGroupV1Decoder decoder = new AddEnumBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), equalTo(Direction.NULL_VAL));
        final AddEnumBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedEnumFieldBeforeGroup()
    {
        final AddEnumBeforeGroupV1Encoder encoder = new AddEnumBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(Direction.SELL).bCount(1).next().c(2);

        final AddEnumBeforeGroupV1Decoder decoder = new AddEnumBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddEnumBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsOldDecoderToSkipAddedEnumFieldBeforeGroup()
    {
        final AddEnumBeforeGroupV1Encoder encoder = new AddEnumBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d(Direction.BUY).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion1();

        final AddEnumBeforeGroupV0Decoder decoder = new AddEnumBeforeGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddEnumBeforeGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeAddedCompositeFieldBeforeGroup()
    {
        final AddCompositeBeforeGroupV1Encoder encoder = new AddCompositeBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().x(-1).y(-2);
        encoder.bCount(1).next().c(2);

        final AddCompositeBeforeGroupV1Decoder decoder = new AddCompositeBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final PointDecoder d = decoder.d();
        assertThat(d, notNullValue());
        assertThat(d.x(), equalTo(-1));
        assertThat(d.y(), equalTo(-2));
        final AddCompositeBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingCompositeFieldBeforeGroupAsNullValue()
    {
        final AddCompositeBeforeGroupV0Encoder encoder = new AddCompositeBeforeGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddCompositeBeforeGroupV1Decoder decoder = new AddCompositeBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), nullValue());
        final AddCompositeBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedCompositeFieldBeforeGroup()
    {
        final AddCompositeBeforeGroupV1Encoder encoder = new AddCompositeBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().x(-1).y(-2);
        encoder.bCount(1).next().c(2);

        final AddCompositeBeforeGroupV1Decoder decoder = new AddCompositeBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddCompositeBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsOldDecoderToSkipAddedCompositeFieldBeforeGroup()
    {
        final AddCompositeBeforeGroupV1Encoder encoder = new AddCompositeBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().x(-1).y(-2);
        encoder.bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion1();

        final AddCompositeBeforeGroupV0Decoder decoder = new AddCompositeBeforeGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddCompositeBeforeGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeAddedArrayFieldBeforeGroup()
    {
        final AddArrayBeforeGroupV1Encoder encoder = new AddArrayBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1)
            .putD((short)1, (short)2, (short)3, (short)4)
            .bCount(1)
            .next().c(2);

        final AddArrayBeforeGroupV1Decoder decoder = new AddArrayBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(0), equalTo((short)1));
        assertThat(decoder.d(1), equalTo((short)2));
        assertThat(decoder.d(2), equalTo((short)3));
        assertThat(decoder.d(3), equalTo((short)4));
        final AddArrayBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingArrayFieldBeforeGroupAsNullValue()
    {
        final AddArrayBeforeGroupV0Encoder encoder = new AddArrayBeforeGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddArrayBeforeGroupV1Decoder decoder = new AddArrayBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(0), equalTo(AddArrayBeforeGroupV1Decoder.dNullValue()));
        assertThat(decoder.d(1), equalTo(AddArrayBeforeGroupV1Decoder.dNullValue()));
        assertThat(decoder.d(2), equalTo(AddArrayBeforeGroupV1Decoder.dNullValue()));
        assertThat(decoder.d(3), equalTo(AddArrayBeforeGroupV1Decoder.dNullValue()));
        final AddArrayBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedArrayFieldBeforeGroup()
    {
        final AddArrayBeforeGroupV1Encoder encoder = new AddArrayBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1)
            .putD((short)1, (short)2, (short)3, (short)4)
            .bCount(1)
            .next().c(2);

        final AddArrayBeforeGroupV1Decoder decoder = new AddArrayBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddArrayBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsOldDecoderToSkipAddedArrayFieldBeforeGroup()
    {
        final AddArrayBeforeGroupV1Encoder encoder = new AddArrayBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1)
            .putD((short)1, (short)2, (short)3, (short)4)
            .bCount(1)
            .next().c(2);

        modifyHeaderToLookLikeVersion1();

        final AddArrayBeforeGroupV0Decoder decoder = new AddArrayBeforeGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddArrayBeforeGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeAddedBitSetFieldBeforeGroup()
    {
        final AddBitSetBeforeGroupV1Encoder encoder = new AddBitSetBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().guacamole(true).cheese(true);
        encoder.bCount(1).next().c(2);

        final AddBitSetBeforeGroupV1Decoder decoder = new AddBitSetBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final FlagsDecoder d = decoder.d();
        assertThat(d, notNullValue());
        assertThat(d.guacamole(), equalTo(true));
        assertThat(d.cheese(), equalTo(true));
        assertThat(d.sourCream(), equalTo(false));
        final AddBitSetBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToDecodeMissingBitSetFieldBeforeGroupAsNullValue()
    {
        final AddBitSetBeforeGroupV0Encoder encoder = new AddBitSetBeforeGroupV0Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion0();

        final AddBitSetBeforeGroupV1Decoder decoder = new AddBitSetBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        assertThat(decoder.d(), nullValue());
        final AddBitSetBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsNewDecoderToSkipPresentButAddedBitSetFieldBeforeGroup()
    {
        final AddBitSetBeforeGroupV1Encoder encoder = new AddBitSetBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().guacamole(true).cheese(true);
        encoder.bCount(1).next().c(2);

        final AddBitSetBeforeGroupV1Decoder decoder = new AddBitSetBeforeGroupV1Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddBitSetBeforeGroupV1Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsOldDecoderToSkipAddedBitSetFieldBeforeGroup()
    {
        final AddBitSetBeforeGroupV1Encoder encoder = new AddBitSetBeforeGroupV1Encoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(1).d().guacamole(true).cheese(true);
        encoder.bCount(1).next().c(2);

        modifyHeaderToLookLikeVersion1();

        final AddBitSetBeforeGroupV0Decoder decoder = new AddBitSetBeforeGroupV0Decoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(1));
        final AddBitSetBeforeGroupV0Decoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(2));
    }

    @Test
    void allowsEncodingAndDecodingEnumInsideGroupInSchemaDefinedOrder()
    {
        final EnumInsideGroupEncoder encoder = new EnumInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(Direction.BUY)
            .bCount(1)
            .next()
            .c(Direction.SELL);

        final EnumInsideGroupDecoder decoder = new EnumInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(Direction.BUY));
        final EnumInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(Direction.SELL));
    }

    @Test
    void disallowsEncodingEnumInsideGroupBeforeCallingNext()
    {
        final EnumInsideGroupEncoder encoder = new EnumInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(Direction.BUY);
        final EnumInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> bEncoder.c(Direction.SELL));
    }

    @Test
    void disallowsDecodingEnumInsideGroupBeforeCallingNext()
    {
        final EnumInsideGroupEncoder encoder = new EnumInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(Direction.BUY)
            .bCount(1)
            .next()
            .c(Direction.SELL);

        final EnumInsideGroupDecoder decoder = new EnumInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(Direction.BUY));
        final EnumInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, b::c);
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelEnum()
    {
        final EnumInsideGroupEncoder encoder = new EnumInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(Direction.BUY)
            .bCount(1)
            .next()
            .c(Direction.SELL);

        encoder.a(Direction.SELL);


        final EnumInsideGroupDecoder decoder = new EnumInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(Direction.SELL));
        final EnumInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(Direction.SELL));
    }

    @Test
    void allowsEncodingAndDecodingBitSetInsideGroupInSchemaDefinedOrder()
    {
        final BitSetInsideGroupEncoder encoder = new BitSetInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().cheese(true).guacamole(true);
        encoder.bCount(1)
            .next()
            .c().sourCream(true);

        final BitSetInsideGroupDecoder decoder = new BitSetInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final FlagsDecoder a = decoder.a();
        assertThat(a.guacamole(), equalTo(true));
        assertThat(a.cheese(), equalTo(true));
        assertThat(a.sourCream(), equalTo(false));
        final BitSetInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final FlagsDecoder c = b.next().c();
        assertThat(c.guacamole(), equalTo(false));
        assertThat(c.cheese(), equalTo(false));
        assertThat(c.sourCream(), equalTo(true));
    }

    @Test
    void disallowsEncodingBitSetInsideGroupBeforeCallingNext()
    {
        final BitSetInsideGroupEncoder encoder = new BitSetInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().cheese(true).guacamole(true);
        final BitSetInsideGroupEncoder.BEncoder bEncoder = encoder.bCount(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bEncoder::c);
    }

    @Test
    void disallowsDecodingBitSetInsideGroupBeforeCallingNext()
    {
        final BitSetInsideGroupEncoder encoder = new BitSetInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().cheese(true).guacamole(true);
        encoder.bCount(1)
            .next()
            .c().sourCream(true);

        final BitSetInsideGroupDecoder decoder = new BitSetInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final FlagsDecoder a = decoder.a();
        assertThat(a.guacamole(), equalTo(true));
        assertThat(a.cheese(), equalTo(true));
        assertThat(a.sourCream(), equalTo(false));
        final BitSetInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, b::c);
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelBitSetViaReWrap()
    {
        final BitSetInsideGroupEncoder encoder = new BitSetInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a().cheese(true).guacamole(true);
        encoder.bCount(1)
            .next()
            .c().sourCream(true);

        encoder.a().sourCream(true);

        final BitSetInsideGroupDecoder decoder = new BitSetInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        final FlagsDecoder a = decoder.a();
        assertThat(a.guacamole(), equalTo(true));
        assertThat(a.cheese(), equalTo(true));
        assertThat(a.sourCream(), equalTo(true));
        final BitSetInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        final FlagsDecoder c = b.next().c();
        assertThat(c.guacamole(), equalTo(false));
        assertThat(c.cheese(), equalTo(false));
        assertThat(c.sourCream(), equalTo(true));
    }

    @Test
    void allowsEncodingAndDecodingArrayInsideGroupInSchemaDefinedOrder()
    {
        final ArrayInsideGroupEncoder encoder = new ArrayInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.putA((short)1, (short)2, (short)3, (short)4);
        encoder.bCount(1)
            .next()
            .putC((short)5, (short)6, (short)7, (short)8);

        final ArrayInsideGroupDecoder decoder = new ArrayInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(0), equalTo((short)1));
        assertThat(decoder.a(1), equalTo((short)2));
        assertThat(decoder.a(2), equalTo((short)3));
        assertThat(decoder.a(3), equalTo((short)4));
        final ArrayInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        b.next();
        assertThat(b.c(0), equalTo((short)5));
        assertThat(b.c(1), equalTo((short)6));
        assertThat(b.c(2), equalTo((short)7));
        assertThat(b.c(3), equalTo((short)8));
    }

    @Test
    void disallowsEncodingArrayInsideGroupBeforeCallingNext1()
    {
        final ArrayInsideGroupEncoder.BEncoder bEncoder = encodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> bEncoder.putC((short)5, (short)6, (short)7, (short)8));
    }

    @Test
    void disallowsEncodingArrayInsideGroupBeforeCallingNext2()
    {
        final ArrayInsideGroupEncoder.BEncoder bEncoder = encodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> bEncoder.c(0, (short)5));
    }

    @Test
    void disallowsEncodingArrayInsideGroupBeforeCallingNext3()
    {
        final ArrayInsideGroupEncoder.BEncoder bEncoder = encodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () ->
            bEncoder.putC(new byte[] {5, 0, 6, 0, 7, 0, 8, 0}, 0, 8));
    }

    @Test
    void disallowsEncodingArrayInsideGroupBeforeCallingNext4()
    {
        final ArrayInsideGroupEncoder.BEncoder bEncoder = encodeUntilGroupWithArrayInside();
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[8]);
        buffer.putShort(0, (short)5);
        buffer.putShort(2, (short)6);
        buffer.putShort(4, (short)7);
        buffer.putShort(6, (short)8);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () ->
            bEncoder.putC(buffer, 0, 8));
    }

    private ArrayInsideGroupEncoder.BEncoder encodeUntilGroupWithArrayInside()
    {
        final ArrayInsideGroupEncoder encoder = new ArrayInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.putA((short)1, (short)2, (short)3, (short)4);
        return encoder.bCount(1);
    }

    @Test
    void disallowsDecodingArrayInsideGroupBeforeCallingNext1()
    {
        final ArrayInsideGroupDecoder.BDecoder b = decodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.c(0));
    }

    @Test
    void disallowsDecodingArrayInsideGroupBeforeCallingNext2()
    {
        final ArrayInsideGroupDecoder.BDecoder b = decodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.getC(new byte[8], 0, 8));
    }

    @Test
    void disallowsDecodingArrayInsideGroupBeforeCallingNext3()
    {
        final ArrayInsideGroupDecoder.BDecoder b = decodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.getC(new ExpandableArrayBuffer(), 0, 8));
    }

    @Test
    void disallowsDecodingArrayInsideGroupBeforeCallingNext4()
    {
        final ArrayInsideGroupDecoder.BDecoder b = decodeUntilGroupWithArrayInside();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> b.wrapC(new UnsafeBuffer()));
    }

    private ArrayInsideGroupDecoder.BDecoder decodeUntilGroupWithArrayInside()
    {
        final ArrayInsideGroupEncoder encoder = new ArrayInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.putA((short)1, (short)2, (short)3, (short)4);
        encoder.bCount(1)
            .next()
            .putC((short)5, (short)6, (short)7, (short)8);

        final ArrayInsideGroupDecoder decoder = new ArrayInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(0), equalTo((short)1));
        assertThat(decoder.a(1), equalTo((short)2));
        assertThat(decoder.a(2), equalTo((short)3));
        assertThat(decoder.a(3), equalTo((short)4));
        final ArrayInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        return b;
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelArrayViaReWrap()
    {
        final ArrayInsideGroupEncoder encoder = new ArrayInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.putA((short)1, (short)2, (short)3, (short)4);
        encoder.bCount(1)
            .next()
            .putC((short)5, (short)6, (short)7, (short)8);

        encoder.putA((short)9, (short)10, (short)11, (short)12);

        final ArrayInsideGroupDecoder decoder = new ArrayInsideGroupDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(0), equalTo((short)9));
        assertThat(decoder.a(1), equalTo((short)10));
        assertThat(decoder.a(2), equalTo((short)11));
        assertThat(decoder.a(3), equalTo((short)12));
        final ArrayInsideGroupDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        b.next();
        assertThat(b.c(0), equalTo((short)5));
        assertThat(b.c(1), equalTo((short)6));
        assertThat(b.c(2), equalTo((short)7));
        assertThat(b.c(3), equalTo((short)8));
    }

    @Test
    void allowsEncodingAndDecodingGroupFieldsInSchemaDefinedOrder1()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(0);
        encoder.dCount(1).next().e(43);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        assertThat(decoder.b().count(), equalTo(0));
        final MultipleGroupsDecoder.DDecoder d = decoder.d();
        assertThat(d.count(), equalTo(1));
        assertThat(d.next().e(), equalTo(43));
    }

    @Test
    void allowsEncodingAndDecodingGroupFieldsInSchemaDefinedOrder2()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(41));
        final MultipleGroupsDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(42));
        final MultipleGroupsDecoder.DDecoder d = decoder.d();
        assertThat(d.count(), equalTo(1));
        assertThat(d.next().e(), equalTo(43));
    }

    @Test
    @Disabled("Our access checks are too strict to allow the behaviour in this test.")
    void allowsReEncodingTopLevelPrimitiveFieldsAfterGroups()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);
        encoder.a(44);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(44));
        final MultipleGroupsDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(42));
        final MultipleGroupsDecoder.DDecoder d = decoder.d();
        assertThat(d.count(), equalTo(1));
        assertThat(d.next().e(), equalTo(43));
    }

    @Test
    void disallowsMissedEncodingOfGroupField()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.dCount(0));
    }

    @Test
    void disallowsReEncodingEarlierGroupFields()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.bCount(1));
    }

    @Test
    void disallowsReEncodingLatestGroupField()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.dCount(1));
    }

    @Test
    void disallowsMissedDecodingOfGroupField()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(41));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::d);
    }

    @Test
    void disallowsReDecodingEarlierGroupField()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(41));
        final MultipleGroupsDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(42));
        final MultipleGroupsDecoder.DDecoder d = decoder.d();
        assertThat(d.count(), equalTo(1));
        assertThat(d.next().e(), equalTo(43));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::b);
    }

    @Test
    void disallowsReDecodingLatestGroupField()
    {
        final MultipleGroupsEncoder encoder = new MultipleGroupsEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(41);
        encoder.bCount(1).next().c(42);
        encoder.dCount(1).next().e(43);

        final MultipleGroupsDecoder decoder = new MultipleGroupsDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(41));
        final MultipleGroupsDecoder.BDecoder b = decoder.b();
        assertThat(b.count(), equalTo(1));
        assertThat(b.next().c(), equalTo(42));
        final MultipleGroupsDecoder.DDecoder d = decoder.d();
        assertThat(d.count(), equalTo(1));
        assertThat(d.next().e(), equalTo(43));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::d);
    }

    private void modifyHeaderToLookLikeVersion0()
    {
        messageHeaderDecoder.wrap(buffer, OFFSET);
        final int v1TemplateId = messageHeaderDecoder.templateId() + 1_000;
        messageHeaderEncoder.wrap(buffer, OFFSET);
        messageHeaderEncoder.templateId(v1TemplateId).version(0);
    }

    private void modifyHeaderToLookLikeVersion1()
    {
        messageHeaderDecoder.wrap(buffer, OFFSET);
        assert messageHeaderDecoder.version() == 1;
        final int v0TemplateId = messageHeaderDecoder.templateId() - 1_000;
        messageHeaderEncoder.wrap(buffer, OFFSET);
        messageHeaderEncoder.templateId(v0TemplateId);
    }
    // TODO improve and test error message
}
