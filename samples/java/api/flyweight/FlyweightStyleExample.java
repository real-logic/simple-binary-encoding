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
    private final NewOrderSingleFlyweight newOrderSingle = new NewOrderSingleFlyweight();
    private final MassQuoteFlyweight massQuote = new MassQuoteFlyweight();

    public void simpleEncode()
    {
        newOrderSingle.resetForEncode(buffer);

        // If field is called out of order and a mandatory field is missed then an exception will be thrown
        newOrderSingle.putClOrderId("123");
        newOrderSingle.putSymbolId(567L);
        newOrderSingle.putSide(Side.BUY);
        newOrderSingle.putOrderQty(1);
        newOrderSingle.putPrice(3.2);
        newOrderSingle.putTransactTime(System.currentTimeMillis());

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        newOrderSingle.resetForDecode(buffer);

        if (!newOrderSingle.isValid()) // should validation just throw an exception?
        {
            throw new IllegalStateException("Message is screwed up");
        }

        String clientOrderId = newOrderSingle.getClOrderId();
        long symbolId = newOrderSingle.getSymbolId();
        Side side = newOrderSingle.getSide();
        long orderQty = newOrderSingle.getOrderQty();
        double price = newOrderSingle.getPrice();
        long transactTime = newOrderSingle.getTransactTime();
    }

    public void nestedGroupEncode()
    {
        massQuote.resetForEncode(buffer);
        final long timestamp = System.currentTimeMillis();

        massQuote.putQuoteId("1234");
        massQuote.putCtiCode(CtiCode.HOUSE);

        massQuote.getQuoteSet().addGroup(); // Create a new group in the message
        massQuote.getQuoteSet().putUnderlyingSecurity("ESH0");

        massQuote.getQuoteSet().getQuoteEntry().addGroup();
        massQuote.getQuoteSet().getQuoteEntry().putId(1);
        massQuote.getQuoteSet().getQuoteEntry().putSymbol("ABC1");
        massQuote.getQuoteSet().getQuoteEntry().putSecurityType(SecurityType.OPT);
        massQuote.getQuoteSet().getQuoteEntry().putTransactTime(timestamp);
        massQuote.getQuoteSet().getQuoteEntry().putBidPx(3.1);
        massQuote.getQuoteSet().getQuoteEntry().putBidSize(10);
        massQuote.getQuoteSet().getQuoteEntry().putOfferPx(3.2);
        massQuote.getQuoteSet().getQuoteEntry().putOfferSize(10);

        massQuote.getQuoteSet().getQuoteEntry().addGroup();
        massQuote.getQuoteSet().getQuoteEntry().putId(2);
        massQuote.getQuoteSet().getQuoteEntry().putSymbol("ABC2");
        massQuote.getQuoteSet().getQuoteEntry().putSecurityType(SecurityType.OPT);
        massQuote.getQuoteSet().getQuoteEntry().putTransactTime(timestamp);
        massQuote.getQuoteSet().getQuoteEntry().putBidPx(3.1);
        massQuote.getQuoteSet().getQuoteEntry().putBidSize(10);
        massQuote.getQuoteSet().getQuoteEntry().putOfferPx(3.2);
        massQuote.getQuoteSet().getQuoteEntry().putOfferSize(10);

        massQuote.getQuoteSet().addGroup(); // Create a new group in the message
        massQuote.getQuoteSet().putUnderlyingSecurity("EAB0");

        massQuote.getQuoteSet().getQuoteEntry().addGroup();
        massQuote.getQuoteSet().getQuoteEntry().putId(3);
        massQuote.getQuoteSet().getQuoteEntry().putSymbol("ABC1");
        massQuote.getQuoteSet().getQuoteEntry().putSecurityType(SecurityType.OPT);
        massQuote.getQuoteSet().getQuoteEntry().putTransactTime(timestamp);
        massQuote.getQuoteSet().getQuoteEntry().putBidPx(3.1);
        massQuote.getQuoteSet().getQuoteEntry().putBidSize(10);
        massQuote.getQuoteSet().getQuoteEntry().putOfferPx(3.2);
        massQuote.getQuoteSet().getQuoteEntry().putOfferSize(10);

        massQuote.getQuoteSet().getQuoteEntry().addGroup();
        massQuote.getQuoteSet().getQuoteEntry().putId(4);
        massQuote.getQuoteSet().getQuoteEntry().putSymbol("ABC2");
        massQuote.getQuoteSet().getQuoteEntry().putSecurityType(SecurityType.OPT);
        massQuote.getQuoteSet().getQuoteEntry().putTransactTime(timestamp);
        massQuote.getQuoteSet().getQuoteEntry().putBidPx(3.1);
        massQuote.getQuoteSet().getQuoteEntry().putBidSize(10);
        massQuote.getQuoteSet().getQuoteEntry().putOfferPx(3.2);
        massQuote.getQuoteSet().getQuoteEntry().putOfferSize(10);

        buffer.flip();
        transport.send(buffer);
    }

    public void nestedGroupDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        massQuote.resetForDecode(buffer);

        if (!massQuote.isValid()) // should validation just throw an exception?
        {
            throw new IllegalStateException("Message is screwed up");
        }

        String quoteId = massQuote.getQuoteId();
        CtiCode ctiCode = massQuote.getCtiCode();

        QuoteSetFlyweight quoteSet = massQuote.getQuoteSet();
        while (quoteSet.next())
        {
            String underlyingSecurity = quoteSet.getUnderlyingSecurity();

            QuoteEntryFlyweight quoteEntry = quoteSet.getQuoteEntry();
            while (quoteEntry.next())
            {
                long id = quoteEntry.getId();
                String symbol = quoteEntry.getSymbol();
                SecurityType securityType = quoteEntry.getSecurityType();
                long timestamp = quoteEntry.getTransactTime();
                double bidPrice = quoteEntry.getBidPx();
                long bidSize = quoteEntry.getBidSize();
                double offerPrice = quoteEntry.getOfferPrice();
                long offerSize = quoteEntry.getOfferSize();
            }
        }
    }
}
