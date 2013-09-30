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
    private final NewOrderSingleFlyweight newOrderSingleFlyweight = new NewOrderSingleFlyweight();
    private final MassQuoteVisitor massQuoteVisitor = new MassQuoteVisitor();

    public void simpleEncode()
    {
        newOrderSingleFlyweight.resetForEncode(buffer);

        // If field is called out of order and a mandatory field is missed then an exception will be thrown
        newOrderSingleFlyweight.putClOrderId("123");
        newOrderSingleFlyweight.putSymbolId(567L);
        newOrderSingleFlyweight.putSide(Side.BUY);
        newOrderSingleFlyweight.putOrderQty(1);
        newOrderSingleFlyweight.putPrice(3.2);
        newOrderSingleFlyweight.putTransactTime(System.currentTimeMillis());

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        newOrderSingleFlyweight.resetForDecode(buffer);

        if (!newOrderSingleFlyweight.isValid()) // should validation just throw an exception?
        {
            throw new IllegalStateException("Message is screwed up");
        }

        String clientOrderId = newOrderSingleFlyweight.getClOrderId();
        long symbolId = newOrderSingleFlyweight.getSymbolId();
        Side side = newOrderSingleFlyweight.getSide();
        long orderQty = newOrderSingleFlyweight.getOrderQty();
        double price = newOrderSingleFlyweight.getPrice();
        long transactTime = newOrderSingleFlyweight.getTransactTime();
    }

    public void nestedGroupEncode()
    {
        massQuoteVisitor.resetForEncode(buffer);
        final long timestamp = System.currentTimeMillis();

        massQuoteVisitor.putQuoteId("1234");
        massQuoteVisitor.putCtiCode(CtiCode.HOUSE);

        massQuoteVisitor.getQuoteSetVisitor().addGroup(); // Create a new group in the message
        massQuoteVisitor.getQuoteSetVisitor().putUnderlyingSecurity("ESH0");

        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().addGroup();
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putId(1);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSymbol("ABC1");
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSecurityType(SecurityType.OPT);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putTransactTime(timestamp);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidPx(3.1);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidSize(10);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferPx(3.2);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferSize(10);

        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().addGroup();
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putId(2);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSymbol("ABC2");
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSecurityType(SecurityType.OPT);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putTransactTime(timestamp);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidPx(3.1);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidSize(10);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferPx(3.2);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferSize(10);

        massQuoteVisitor.getQuoteSetVisitor().addGroup(); // Create a new group in the message
        massQuoteVisitor.getQuoteSetVisitor().putUnderlyingSecurity("EAB0");

        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().addGroup();
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putId(3);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSymbol("ABC1");
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSecurityType(SecurityType.OPT);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putTransactTime(timestamp);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidPx(3.1);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidSize(10);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferPx(3.2);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferSize(10);

        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().addGroup();
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putId(4);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSymbol("ABC2");
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putSecurityType(SecurityType.OPT);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putTransactTime(timestamp);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidPx(3.1);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putBidSize(10);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferPx(3.2);
        massQuoteVisitor.getQuoteSetVisitor().getQuoteEntryVisitor().putOfferSize(10);

        buffer.flip();
        transport.send(buffer);
    }

    public void nestedGroupDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        massQuoteVisitor.resetForDecode(buffer);

        if (!massQuoteVisitor.isValid()) // should validation just throw an exception?
        {
            throw new IllegalStateException("Message is screwed up");
        }

        String quoteId = massQuoteVisitor.getQuoteId();
        CtiCode ctiCode = massQuoteVisitor.getCtiCode();

        QuoteSetVisitor quoteSetVisitor = massQuoteVisitor.getQuoteSetVisitor();
        while (quoteSetVisitor.next())
        {
            String underlyingSecurity = quoteSetVisitor.getUnderlyingSecurity();

            QuoteEntryVisitor quoteEntryVisitor = quoteSetVisitor.getQuoteEntryVisitor();
            while (quoteEntryVisitor.next())
            {
                long id = quoteEntryVisitor.getId();
                String symbol = quoteEntryVisitor.getSymbol();
                SecurityType securityType = quoteEntryVisitor.getSecurityType();
                long timestamp = quoteEntryVisitor.getTransactTime();
                double bidPrice = quoteEntryVisitor.getBidPx();
                long bidSize = quoteEntryVisitor.getBidSize();
                double offerPrice = quoteEntryVisitor.getOfferPrice();
                long offerSize = quoteEntryVisitor.getOfferSize();
            }
        }
    }
}
