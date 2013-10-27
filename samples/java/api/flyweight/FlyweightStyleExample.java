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

        // If field is called out of order and a mandatory field is missed then an exception will be thrown
        newOrderSingle.clOrderId("123");
        newOrderSingle.symbolId(567L);
        newOrderSingle.side(Side.BUY);
        newOrderSingle.orderQty(1);
        newOrderSingle.price(3.2);
        newOrderSingle.transactTime(System.currentTimeMillis());

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        newOrderSingle.reset(buffer);

        if (!newOrderSingle.valid()) // should validation just throw an exception?
        {
            throw new IllegalStateException("Message is screwed up");
        }

        String clientOrderId = newOrderSingle.clOrderId();
        long symbolId = newOrderSingle.symbolId();
        Side side = newOrderSingle.side();
        long orderQty = newOrderSingle.orderQty();
        double price = newOrderSingle.price();
        long transactTime = newOrderSingle.transactTime();
    }

    public void nestedGroupEncode()
    {
        massQuote.resetForEncode(buffer);
        final long timestamp = System.currentTimeMillis();

        massQuote.putQuoteId("1234");
        massQuote.putCtiCode(CtiCode.HOUSE);

        final MassQuote.QuoteSet quoteSet = massQuote.quoteSetSize(2);
        quoteSet.next();

        quoteSet.putUnderlyingSecurity("ESH0");

        MassQuote.QuoteSet.QuoteEntry quoteEntry = quoteSet.quoteEntrySize(2);
        quoteEntry.next();
        quoteEntry.id(1);
        quoteEntry.symbol("ABC1");
        quoteEntry.securityType(SecurityType.OPT);
        quoteEntry.transactTime(timestamp);
        quoteEntry.bidPx(3.1);
        quoteEntry.bidSize(10);
        quoteEntry.offerPx(3.2);
        quoteEntry.offerSize(10);

        quoteEntry.next();
        quoteEntry.id(2);
        quoteEntry.symbol("ABC2");
        quoteEntry.securityType(SecurityType.OPT);
        quoteEntry.transactTime(timestamp);
        quoteEntry.bidPx(3.1);
        quoteEntry.bidSize(10);
        quoteEntry.offerPx(3.2);
        quoteEntry.offerSize(10);

        quoteSet.next(); // Create next repeating set

        quoteSet.putUnderlyingSecurity("EAB0");

        quoteEntry = quoteSet.quoteEntrySize(2);
        quoteEntry.next();
        quoteEntry.id(3);
        quoteEntry.symbol("ABC1");
        quoteEntry.securityType(SecurityType.OPT);
        quoteEntry.transactTime(timestamp);
        quoteEntry.bidPx(3.1);
        quoteEntry.bidSize(10);
        quoteEntry.offerPx(3.2);
        quoteEntry.offerSize(10);

        quoteEntry.next();
        quoteEntry.id(4);
        quoteEntry.symbol("ABC2");
        quoteEntry.securityType(SecurityType.OPT);
        quoteEntry.transactTime(timestamp);
        quoteEntry.bidPx(3.1);
        quoteEntry.bidSize(10);
        quoteEntry.offerPx(3.2);
        quoteEntry.offerSize(10);

        buffer.flip();
        transport.send(buffer);
    }

    public void nestedGroupDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        massQuote.resetForDecode(buffer);

        String quoteId = massQuote.getQuoteId();
        CtiCode ctiCode = massQuote.getCtiCode();

        MassQuote.QuoteSet quoteSet = massQuote.quoteSet();
        while (quoteSet.next())
        {
            String underlyingSecurity = quoteSet.underlyingSecurity();

            MassQuote.QuoteSet.QuoteEntry quoteEntry = quoteSet.quoteEntry();
            while (quoteEntry.next())
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
