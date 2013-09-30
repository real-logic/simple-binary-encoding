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

import api.fluent.CtiCode;

import java.nio.ByteBuffer;

public class MassQuoteVisitor
{
    private QuoteSetVisitor quoteSetVisitor = new QuoteSetVisitor();

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

    public QuoteSetVisitor getQuoteSetVisitor()
    {
        return quoteSetVisitor;
    }

    public boolean isValid()
    {
        return true;
    }

    public String getQuoteId()
    {
        return null;
    }

    public CtiCode getCtiCode()
    {
        return null;
    }
}
