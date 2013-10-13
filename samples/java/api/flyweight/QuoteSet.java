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

public class QuoteSet
{
    private QuoteEntry quoteEntry;

    public void addGroup()
    {
    }

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

    public QuoteEntry newQuoteEntry(final int length)
    {
        return null;
    }
}
