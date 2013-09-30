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

import api.Side;
import api.Transport;

import java.nio.ByteBuffer;

public class FlyweightStyleExample
{
    // Either used from single thread or kept thread local
    // We could also use this encode/decode mechanism from a byte[]
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

    private final Transport transport = new Transport();

    public void simpleEncode()
    {
        // Can be reused to avoid garbage
        final NewOrderSingleFlyweight newOrderSingleFlyweight = new NewOrderSingleFlyweight();
        newOrderSingleFlyweight.setBuffer(buffer);

        newOrderSingleFlyweight.putClOrderId("123");
        newOrderSingleFlyweight.putSymbolId(567L);
        newOrderSingleFlyweight.putSide(Side.BUY);
        newOrderSingleFlyweight.putOrderQty(1);
        newOrderSingleFlyweight.putPrice(3.2);
        newOrderSingleFlyweight.putTransactTime(System.currentTimeMillis());

        newOrderSingleFlyweight.validate(); // throws exception if invalid

        buffer.flip();
        transport.send(buffer);
    }

    public void simpleDecode()
    {
        buffer.clear();
        transport.receive(buffer);

        buffer.flip();

        final NewOrderSingleFlyweight newOrderSingleFlyweight = new NewOrderSingleFlyweight();
        newOrderSingleFlyweight.setBuffer(buffer);

        newOrderSingleFlyweight.validate(); // throws exception if invalid

        String clientOrderId = newOrderSingleFlyweight.getClOrderId();
        long symbolId = newOrderSingleFlyweight.getSymbolId();
        Side side = newOrderSingleFlyweight.getSide();
        long orderQty = newOrderSingleFlyweight.getOrderQty();
        double price = newOrderSingleFlyweight.getPrice();
        long transactTime = newOrderSingleFlyweight.getTransactTime();
    }
}
