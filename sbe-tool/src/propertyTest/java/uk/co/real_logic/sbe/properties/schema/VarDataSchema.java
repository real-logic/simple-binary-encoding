/*
 * Copyright 2013-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.properties.schema;

import uk.co.real_logic.sbe.PrimitiveType;

public final class VarDataSchema
{
    private final Encoding dataEncoding;
    private final PrimitiveType lengthEncoding;
    private final short sinceVersion;

    public VarDataSchema(
        final Encoding dataEncoding,
        final PrimitiveType lengthEncoding,
        final short sinceVersion)
    {
        this.dataEncoding = dataEncoding;
        this.lengthEncoding = lengthEncoding;
        this.sinceVersion = sinceVersion;
    }

    public Encoding dataEncoding()
    {
        return dataEncoding;
    }

    public PrimitiveType lengthEncoding()
    {
        return lengthEncoding;
    }

    public short sinceVersion()
    {
        return sinceVersion;
    }

    public enum Encoding
    {
        ASCII,
        BYTES
    }
}
