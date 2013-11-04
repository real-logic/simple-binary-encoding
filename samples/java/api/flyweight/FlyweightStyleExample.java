/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package api.flyweight;

import api.SecurityType;
import api.Side;
import api.Transport;
import api.fluent.CtiCode;

import java.nio.ByteBuffer;

public class FlyweightStyleExample
{
    // Either used from single thread or kept thread local
    // We could also use this encode/decode mechanism from a byte[]
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

    private final Transport transport = new Transport();

    // Can be reused to avoid garbage
    private final NewOrderSingle newOrderSingle = new NewOrderSingle();
    private final MassQuote massQuote = new MassQuote();

    public void simpleEncode()
    {
        newOrderSingle.reset(buffer);

        newOrderSingle
            .clOrderId("123")
            .symbolId(567L)
            .side(Side.BUY)
            .orderQty(1)
            .price(3.2)
            .transactTime(System.currentTimeMillis());

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        newOrderSingle.reset(buffer);

        String clientOrderId = newOrderSingle.clOrderId();
        long symbolId = newOrderSingle.symbolId();
        Side side = newOrderSingle.side();
        long orderQty = newOrderSingle.orderQty();
        double price = newOrderSingle.price();
        long transactTime = newOrderSingle.transactTime();
    }

    public void nestedGroupEncode()
    {
        final long timestamp = System.currentTimeMillis();

        massQuote.reset(buffer)
            .quoteId("1234")
            .ctiCode(CtiCode.HOUSE);

        MassQuote.QuoteSet quoteSet = massQuote.quoteSetCount(2);

        quoteSet.next().underlyingSecurity("ESH0");

        quoteSet.quoteEntryCount(2)
            .next()
                .id(1)
                .symbol("ABC1")
                .securityType(SecurityType.OPT)
                .transactTime(timestamp)
                .bidPx(3.1)
                .bidSize(10)
                .offerPx(3.2)
                .offerSize(10)
            .next()
                .id(2)
                .symbol("ABC2")
                .securityType(SecurityType.OPT)
                .transactTime(timestamp)
                .bidPx(3.1)
                .bidSize(10)
                .offerPx(3.2)
                .offerSize(10);

        quoteSet.next().underlyingSecurity("EAB0");

        quoteSet.quoteEntryCount(2)
            .next()
                .id(3)
                .symbol("ABC1")
                .securityType(SecurityType.OPT)
                .transactTime(timestamp)
                .bidPx(3.1)
                .bidSize(10)
                .offerPx(3.2)
                .offerSize(10)
            .next()
                .id(4)
                .symbol("ABC2")
                .securityType(SecurityType.OPT)
                .transactTime(timestamp)
                .bidPx(3.1)
                .bidSize(10)
                .offerPx(3.2)
                .offerSize(10);

        buffer.flip();
        transport.send(buffer);
    }

    public void nestedGroupDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        massQuote.reset(buffer);

        String quoteId = massQuote.quoteId();
        CtiCode ctiCode = massQuote.ctiCode();

        for (MassQuote.QuoteSet quoteSet : massQuote.quoteSet())
        {
            String underlyingSecurity = quoteSet.underlyingSecurity();

            for (MassQuote.QuoteSet.QuoteEntry quoteEntry : quoteSet.quoteEntry())
            {
                long id = quoteEntry.id();
                String symbol = quoteEntry.symbol();
                SecurityType securityType = quoteEntry.securityType();
                long timestamp = quoteEntry.transactTime();
                double bidPrice = quoteEntry.bidPx();
                long bidSize = quoteEntry.bidSize();
                double offerPrice = quoteEntry.offerPrice();
                long offerSize = quoteEntry.offerSize();
            }
        }
    }
}
