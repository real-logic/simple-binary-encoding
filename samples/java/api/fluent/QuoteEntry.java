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

public class QuoteEntry
{
    public QuoteEntry id(final int id)
    {
        return this;
    }

    public QuoteEntry symbol(final String symbol)
    {
        return this;
    }

    public QuoteEntry securityType(final SecurityType securityType)
    {
        return this;
    }

    public QuoteEntry transactTime(final long timestamp)
    {
        return this;
    }

    public QuoteEntry bidPx(final double value)
    {
        return this;
    }

    public QuoteEntry bidSize(final long value)
    {
        return this;
    }

    public QuoteEntry offerPx(final double value)
    {
        return this;
    }

    public QuoteEntry offerSize(final long value)
    {
        return this;
    }

    public long id()
    {
        return 0;
    }

    public String symbol()
    {
        return null;
    }

    public SecurityType securityType()
    {
        return null;
    }

    public long transactTime()
    {
        return 0;
    }

    public double bidPx()
    {
        return 0;
    }

    public long bidSize()
    {
        return 0;
    }

    public double offerPrice()
    {
        return 0;
    }

    public long offerSize()
    {
        return 0;
    }
}
