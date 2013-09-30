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

import java.nio.ByteBuffer;

public class NewOrderSingleFlyweight
{
    public void setBuffer(final ByteBuffer buffer)
    {
    }

    public void putClOrderId(final String value)
    {
    }

    public void putSymbolId(final long value)
    {
    }

    public void putSide(final Side side)
    {
    }

    public void putOrderQty(final int value)
    {
    }

    public void putPrice(final double value)
    {
    }

    public void putTransactTime(final long value)
    {
    }

    public void validate()
    {
    }

    public String getClOrderId()
    {
        return null;
    }

    public long getSymbolId()
    {
        return 0;
    }

    public Side getSide()
    {
        return null;
    }

    public long getOrderQty()
    {
        return 0;
    }

    public double getPrice()
    {
        return 0;
    }

    public long getTransactTime()
    {
        return 0;
    }
}
