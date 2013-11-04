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
import api.fluent.CtiCode;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class MassQuote
{
    private QuoteSet quoteSet = new QuoteSet();

    public MassQuote reset(final ByteBuffer buffer)
    {
        return this;
    }
    public MassQuote quoteId(final String quoteId)
    {
        return this;
    }

    public MassQuote ctiCode(final CtiCode code)
    {
        return this;
    }

    public String quoteId()
    {
        return null;
    }

    public CtiCode ctiCode()
    {
        return null;
    }

    public QuoteSet quoteSet()
    {
        return quoteSet;
    }

    public QuoteSet quoteSetCount(final int count)
    {
        return quoteSet;
    }

    public class QuoteSet implements Iterable<QuoteSet>, Iterator<QuoteSet>
    {
        private QuoteEntry quoteEntry;

        public QuoteSet underlyingSecurity(final String security)
        {
            return this;
        }

        public String underlyingSecurity()
        {
            return "";
        }

        public QuoteEntry quoteEntry()
        {
            return quoteEntry;
        }

        public int count()
        {
            return 0;
        }

        public boolean hasNext()
        {
            return true;
        }

        public QuoteSet next()
        {
            return this;
        }

        public void remove()
        {
        }

        public QuoteEntry quoteEntryCount(final int count)
        {
            return null;
        }

        public Iterator<QuoteSet> iterator()
        {
            return this;
        }

        public class QuoteEntry implements Iterable<QuoteEntry>, Iterator<QuoteEntry>
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

            public QuoteEntry bidSize(final int value)
            {
                return this;
            }

            public QuoteEntry offerPx(final double value)
            {
                return this;
            }

            public QuoteEntry offerSize(final int value)
            {
                return this;
            }

            public boolean hasNext()
            {
                return true;
            }

            public QuoteEntry next()
            {
                return this;
            }

            public void remove()
            {
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

            public Iterator<QuoteEntry> iterator()
            {
                return this;
            }
        }
    }
}
