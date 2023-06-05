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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
    void disallowsSkippingEncodingOfVariableLengthField()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.c("def"));
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
    @Disabled("Decoding checks not implemented yet.")
    void disallowsSkippingDecodingOfVariableLengthField()
    {
        final MultipleVarLengthEncoder encoder = new MultipleVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.b("abc");
        encoder.c("def");

        final MultipleVarLengthDecoder decoder = new MultipleVarLengthDecoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderDecoder);
        assertThat(decoder.a(), equalTo(42));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::c);
    }

    @Test
    @Disabled("Decoding checks not implemented yet.")
    void disallowsReDecodingEarlierVariableLengthField()
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
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::b);
    }

    @Test
    @Disabled("Decoding checks not implemented yet.")
    void disallowsReDecodingLatestVariableLengthField()
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
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, decoder::c);
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
    void disallowsEncodingGroupElementWithoutCallingNext()
    {
        final GroupAndVarLengthEncoder encoder = new GroupAndVarLengthEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.bCount(1).c(1));
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
    @Disabled("Decoding checks not implemented yet.")
    void disallowsSkippingDecodingOfGroupBeforeVariableLengthField()
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
    @Disabled("Decoding checks not implemented yet.")
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
    @Disabled("Decoding checks not implemented yet.")
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
    void disallowsSkippingGroupElementVariableLengthFieldToEncodeAtTopLevel()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        encoder.bCount(1).next().c(1);
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, () -> encoder.e("abc"));
    }

    @Test
    void disallowsSkippingGroupElementVariableLengthFieldToEncodeNextElement()
    {
        final VarLengthInsideGroupEncoder encoder = new VarLengthInsideGroupEncoder()
            .wrapAndApplyHeader(buffer, OFFSET, messageHeaderEncoder);
        encoder.a(42);
        final VarLengthInsideGroupEncoder.BEncoder b = encoder.bCount(2)
            .next();
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, b::next);
    }

    @Test
    void disallowsSkippingGroupElementEncoding()
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
    @Disabled("Decoding checks not implemented yet.")
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
        assertThat(bs.next().c(), equalTo("abc"));
        assertThrows(INCORRECT_ORDER_EXCEPTION_CLASS, bs::d);
    }

    @Test
    @Disabled("Decoding checks not implemented yet.")
    void disallowsSkippingDecodingOfGroupElementVariableLengthFieldToNextElement()
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
    @Disabled("Decoding checks not implemented yet.")
    void disallowsSkippingDecodingOfGroupElementVariableLengthFieldToTopLevel()
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
    @Disabled("Decoding checks not implemented yet.")
    void disallowsSkippingDecodingOfGroupElement()
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
        assertThat(bs.next().c(), equalTo("abc"));
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
    void disallowsSkippingEncodingOfNestedGroup()
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

    // TODO test more of SBE:
    //    - array setters
    //    - alternative varData setters
    //    - bitset setters
    //    - versioning
}
