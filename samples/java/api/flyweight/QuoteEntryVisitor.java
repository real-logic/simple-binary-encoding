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

public class QuoteEntryVisitor
{
    public void addGroup()
    {
    }

    public void putId(final int id)
    {
    }

    public void putSymbol(final String symbol)
    {
    }

    public void putSecurityType(final SecurityType securityType)
    {
    }

    public void putTransactTime(final long timestamp)
    {
    }

    public void putBidPx(final double value)
    {
    }

    public void putBidSize(final int value)
    {
    }

    public void putOfferPx(final double value)
    {
    }

    public void putOfferSize(final int value)
    {
    }

    public boolean next()
    {
        return false;
    }

    public long getId()
    {
        return 0;
    }

    public String getSymbol()
    {
        return null;
    }

    public SecurityType getSecurityType()
    {
        return null;
    }

    public long getTransactTime()
    {
        return 0;
    }

    public double getBidPx()
    {
        return 0;
    }

    public long getBidSize()
    {
        return 0;
    }

    public double getOfferPrice()
    {
        return 0;
    }

    public long getOfferSize()
    {
        return 0;
    }
}
