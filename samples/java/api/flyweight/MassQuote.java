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

public class MassQuote
{
    private QuoteSet quoteSet = new QuoteSet();

    public void resetForEncode(final ByteBuffer buffer)
    {
    }

    public void resetForDecode(final ByteBuffer buffer)
    {
    }

    public void putQuoteId(final String quoteId)
    {
    }

    public void putCtiCode(final CtiCode code)
    {
    }

    public QuoteSet quoteSet()
    {
        return quoteSet;
    }

    public QuoteSet quoteSetCount(final int length)
    {
        return quoteSet;
    }

    public String getQuoteId()
    {
        return null;
    }

    public CtiCode getCtiCode()
    {
        return null;
    }

    public class QuoteSet
    {
        private QuoteEntry quoteEntry;

        public void putUnderlyingSecurity(final String security)
        {
        }

        public QuoteEntry quoteEntry()
        {
            return quoteEntry;
        }

        public int length()
        {
            return 0;
        }

        public void length(final int length)
        {
        }

        public String underlyingSecurity()
        {
            return "";
        }

        public boolean next()
        {
            return false;
        }

        public QuoteEntry quoteEntryCount(final int length)
        {
            return null;
        }

        public class QuoteEntry
        {
            public void length(final int length)
            {
            }

            public void id(final int id)
            {
            }

            public void symbol(final String symbol)
            {
            }

            public void securityType(final SecurityType securityType)
            {
            }

            public void transactTime(final long timestamp)
            {
            }

            public void bidPx(final double value)
            {
            }

            public void bidSize(final int value)
            {
            }

            public void offerPx(final double value)
            {
            }

            public void offerSize(final int value)
            {
            }

            public boolean next()
            {
                return false;
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

            public int length()
            {
                return 0;
            }
        }
    }
}
