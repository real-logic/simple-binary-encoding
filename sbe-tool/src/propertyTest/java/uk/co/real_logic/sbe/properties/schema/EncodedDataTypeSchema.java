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
import uk.co.real_logic.sbe.ir.Encoding;

public final class EncodedDataTypeSchema implements TypeSchema
{
    private final PrimitiveType primitiveType;
    private final int length;
    private final Encoding.Presence presence;
    private final boolean isEmbedded;

    public EncodedDataTypeSchema(
        final PrimitiveType primitiveType,
        final int length,
        final Encoding.Presence presence,
        final boolean isEmbedded)
    {
        this.primitiveType = primitiveType;
        this.length = length;
        this.presence = presence;
        this.isEmbedded = isEmbedded;
    }

    public PrimitiveType primitiveType()
    {
        return primitiveType;
    }

    public int length()
    {
        return length;
    }

    public Encoding.Presence presence()
    {
        return presence;
    }

    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    public void accept(final TypeSchemaVisitor visitor)
    {
        visitor.onEncoded(this);
    }
}
