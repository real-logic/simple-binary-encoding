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

import java.nio.ByteBuffer;

public class MassQuote
{
    public MassQuote quoteId(final String quoteId)
    {
        return this;
    }

    public MassQuote ctiCode(final CtiCode code)
    {
        return this;
    }

    public MassQuote setManualOrderIndicator(final boolean manualOrderIndicator)
    {
        return this;
    }

    public MassQuote quoteSets(final QuoteSet... quoteSet)
    {
        return this;
    }

    public MassQuote encode(final ByteBuffer buffer)
    {
        return this;
    }

    public MassQuote decode(final ByteBuffer buffer)
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

    public Iterable<? extends QuoteSet> quoteSets()
    {
        return null;
    }
}
