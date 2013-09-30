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
package api.fluent;

import api.SecurityType;
import api.Side;
import api.Transport;

import java.nio.ByteBuffer;

public class FluentDomStyleExample
{
    // Either used from single thread or kept thread local
    // We could also use this encode/decode mechanism from a byte[]
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

    private final Transport transport = new Transport();

    public void simpleEncode()
    {
        buffer.clear();
        NewOrderSingle newOrderSingle = new NewOrderSingle();

        newOrderSingle.clOrderId("123")
            .symbolId(567L)
            .side(Side.BUY)
            .orderQty(1)
            .price(7.4)
            .transactTime(System.currentTimeMillis())
            .encode(buffer); // will throw runtime exception if validation fails

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        buffer.flip();

        NewOrderSingle newOrderSingle = new NewOrderSingle();
        newOrderSingle.decode(buffer); // will throw runtime exception if validation fails

        String clientOrderId = newOrderSingle.clOrderId();
        long symbolId = newOrderSingle.symbolId();
        Side side = newOrderSingle.side();
        long orderQty = newOrderSingle.orderQty();
        double price = newOrderSingle.price();
        long transactTime = newOrderSingle.transactTime();
    }

    public void nestedGroupEncode()
    {
        buffer.clear();
        MassQuote massQuote = new MassQuote();
        long timestamp = System.currentTimeMillis();

        massQuote.quoteId("1234")
            .ctiCode(CtiCode.HOUSE)
            .quoteSets(
                new QuoteSet().underlyingSecurity("ESH0")
                    .quoteEntries(
                        new QuoteEntry().id(1)
                            .symbol("ABC1")
                            .securityType(SecurityType.OPT)
                            .transactTime(timestamp)
                            .bidPx(3.1)
                            .bidSize(10)
                            .offerPx(3.2)
                            .offerSize(10),
                        new QuoteEntry().id(2)
                            .symbol("ABC2")
                            .securityType(SecurityType.OPT)
                            .transactTime(timestamp)
                            .bidPx(3.1)
                            .bidSize(10)
                            .offerPx(3.2)
                            .offerSize(10)
                    ),
                new QuoteSet().underlyingSecurity("EAB0")
                    .quoteEntries(
                        new QuoteEntry().id(3)
                            .symbol("ABC1")
                            .securityType(SecurityType.OPT)
                            .transactTime(timestamp)
                            .bidPx(3.1)
                            .bidSize(10)
                            .offerPx(3.2)
                            .offerSize(10),
                        new QuoteEntry().id(4)
                            .symbol("ABC2")
                            .securityType(SecurityType.OPT)
                            .transactTime(timestamp)
                            .bidPx(3.1)
                            .bidSize(10)
                            .offerPx(3.2)
                            .offerSize(10)
                    )
            ).encode(buffer);

        buffer.flip();
        transport.send(buffer);
    }

    public void nestedGroupDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        buffer.flip();

        MassQuote massQuote = new MassQuote();
        massQuote.decode(buffer); // will throw runtime exception if validation fails

        String quoteId = massQuote.quoteId();
        CtiCode ctiCode = massQuote.ctiCode();

        for (final QuoteSet quoteSet : massQuote.quoteSets())
        {
            String underLyingSecurity = quoteSet.underlyingSecurity();

            for (final QuoteEntry quoteEntry : quoteSet.quoteEntries())
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
