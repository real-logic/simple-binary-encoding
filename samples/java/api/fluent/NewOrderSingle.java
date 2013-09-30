package api.fluent;/*
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

import api.Side;

import java.nio.ByteBuffer;

public class NewOrderSingle
{
    private String clientOrderId;

    public NewOrderSingle side(final Side side)
    {
        return this;
    }

    public NewOrderSingle transactTime(final long time)
    {
        return this;
    }

    public NewOrderSingle orderQty(final long amount)
    {
        return this;
    }

    public NewOrderSingle clOrderId(final String clOrderId)
    {
        return this;
    }

    public NewOrderSingle symbolId(final long symbolId)
    {
        return this;
    }

    public NewOrderSingle price(final double value)
    {
        return this;
    }

    public String clOrderId()
    {
        return null;
    }

    public long symbolId()
    {
        return 0;
    }

    public Side side()
    {
        return null;
    }

    public long orderQty()
    {
        return 0;
    }

    public double price()
    {
        return 0;
    }

    public long transactTime()
    {
        return 0;
    }

    public void decode(final ByteBuffer buffer)
    {
    }

    public void encode(final ByteBuffer buffer)
    {
    }
}
