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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class GroupSchema
{
    private final List<FieldSchema> blockFields;
    private final List<GroupSchema> groups;
    private final List<VarDataSchema> varData;

    public GroupSchema(
        final List<FieldSchema> blockFields,
        final List<GroupSchema> groups,
        final List<VarDataSchema> varData)
    {
        this.blockFields = blockFields.stream()
            .sorted(Comparator.comparing(FieldSchema::sinceVersion))
            .collect(Collectors.toList());
        this.groups = groups;
        this.varData = varData.stream()
            .sorted(Comparator.comparing(VarDataSchema::sinceVersion))
            .collect(Collectors.toList());
    }

    public List<FieldSchema> blockFields()
    {
        return blockFields;
    }

    public List<GroupSchema> groups()
    {
        return groups;
    }

    public List<VarDataSchema> varData()
    {
        return varData;
    }
}
