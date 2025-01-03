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

import uk.co.real_logic.sbe.ir.Encoding;

public final class FieldSchema
{
    private final TypeSchema type;
    private final Encoding.Presence presence;
    private final short sinceVersion;

    public FieldSchema(
        final TypeSchema type,
        final Encoding.Presence presence,
        final short sinceVersion)
    {
        assert sinceVersion == 0 || presence.equals(Encoding.Presence.OPTIONAL);
        this.type = type;
        this.presence = presence;
        this.sinceVersion = sinceVersion;
    }

    public TypeSchema type()
    {
        return type;
    }

    public Encoding.Presence presence()
    {
        return presence;
    }

    public short sinceVersion()
    {
        return sinceVersion;
    }
}
