/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package uk.co.real_logic.sbe.xml;

/**
 * fixUsage
 * <p/>
 * int
 * Length
 * TagNum
 * SeqNum
 * NumInGroup
 * DayOfMonth
 * float
 * Qty
 * Price
 * Amt
 * Percentage
 * char
 * Boolean
 * String
 * MultipleCharValue
 * MultipleStringValue
 * Country
 * Currency
 * Exchange
 * MonthYear
 * UTCTimestamp
 * UTCTimeOnly
 * UTCDateOnly
 * LocalMktDate
 * TZTimeOnly
 * TZTimestamp
 * data
 * Pattern
 * Reserved100Plus
 * Reserved1000Plus
 * Reserved4000Plus
 * XMLData
 * Language
 *
 * fixUsage must be specified in the <type> or <field> specification
 * - if in the type, then field inherits the specification
 * - if in the type and in the field, then field should override?
 * - if not in the type, then field must specify it
 */
public enum FixUsage
{
    INT("int"),
    LENGTH("Length"),
    TAGNUM("TagNum"),
    SEQNUM("SeqNum"),
    NUMINGROUP("NumInGroup"),
    DAYOFMONTH("DayOfMonth"),
    FLOAT("float"),
    QTY("Qty"),
    PRICE("Price"),
    AMT("Amt"),
    PERCENTAGE("Percentage"),
    CHAR("char"),
    BOOLEAN("Boolean"),
    STRING("String"),
    MULTIPLECHARVALUE("MultipleCharValue"),
    MULTIPLESTRINGVALUE("MultipleStringValue"),
    COUNTRY("Country"),
    CURRENCY("Currency"),
    EXCAHNGE("Exchange"),
    MONTHYEAR("MonthYear"),
    UTCTIMESTAMP("UTCTimestamp"),
    UTCTIMEONLY("UTCTimeOnly"),
    UTCDATEONLY("UTCDateOnly"),
    LOCALMKTDATE("LocalMktDate"),
    TZTIMEONLY("TZTimeOnly"),
    TZTIMESTAMP("TZTimestamp"),
    DATA("data"),
    PATTERN("Pattern"),
    RESERVED100PLUS("Reserved100Plus"),
    RESERVED1000PLUS("Reserved1000Plus"),
    RESERVED4000PLUS("Reserved4000Plus"),
    XMLDATA("XMLData"),
    LANGUAGE("Language");

    private final String name;

    FixUsage(final String name)
    {
        this.name = name;
    }

    /**
     * The name of the FIX semantic data type as a String
     *
     * @return the name as a String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Lookup name of fixUsage and return enum
     *
     * @param value of fixUsage to lookup or null for NOTSET
     * @return the {@link FixUsage} matching the name
     * @throws IllegalArgumentException if name not found
     */
    public static FixUsage lookup(final String value)
    {
	    if (value == null)
        {
            return null;
        }

        for (final FixUsage f : values())
        {
            if (value.equals(f.name))
            {
                return f;
            }
        }

        throw new IllegalArgumentException("No FixUsage for value: " + value);
    }
}
