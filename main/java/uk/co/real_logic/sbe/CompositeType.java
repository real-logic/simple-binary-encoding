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
package uk.co.real_logic.sbe;

import org.w3c.dom.Node;
import java.util.List;
import java.util.ArrayList;

/**
 * SBE compositeType
 *
 * decimal types can use mantissa and exponent portions as min/max/null. 
 *
 *              Length  Exponent     Min                Max              Null
 * decimal      9       -128 to 127  
 *  int64 mantissa                   -2^63 + 1          2^64(2^63 - 1)   -2^63
 *  int8 exponent                    10^127 to 10^-128  10^127
 * decimal64    8       -128 to 127                     
 *  int64 mantissa                   -2^63 + 1          2^64(2^63 - 1)   -2^63
 *  constant exponent                10^127 to 10^-128  10^127
 * decimal32    4       -128 to 127
 *  int32 mantissa                   -2^32 + 1          2^32(2^31 - 1)   -2^31
 *  constant exponent                10^-127            10^127
 */
public class CompositeType extends Type
{
    /** A composite is a sequence of encodedDataTypes, so we have a list of them */
    private List<EncodedDataType> composites;

    /**
     * Construct a new compositeType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public CompositeType(final Node node)
    {
        super(node); // set the common schema attributes

        composites = new ArrayList<EncodedDataType>();
        // TODO: iterate over children nodes to grab encoded data types
    }

    /**
     * The size (in octets) of the list of EncodedDataTypes
     *
     * @return size of the compositeType
     */
    public int size()
    {
        int sz = 0;

        for (EncodedDataType t : composites)
        {
            sz += t.size();
        }
        return sz;
    }

}
