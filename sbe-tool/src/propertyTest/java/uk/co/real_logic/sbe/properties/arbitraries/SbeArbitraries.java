/*
 * Copyright 2013-2023 Real Logic Limited.
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

package uk.co.real_logic.sbe.properties.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.arbitraries.ListArbitrary;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.properties.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SbeArbitraries
{
    private static final int MAX_COMPOSITE_DEPTH = 3;
    private static final int MAX_GROUP_DEPTH = 3;

    private SbeArbitraries()
    {
    }

    /**
     * This combinator adds duplicates to an arbitrary list. We prefer this to JQwik's built-in functionality,
     * as that is inefficient and dominates test runs.
     * <p>
     * This method works by generating a list of integers, which represent, in an alternating manner,
     * the number of items to skip before <i>selecting</i> an item to duplicate
     * and the number of items to skip before <i>inserting</i> the duplicate.
     *
     * @param maxDuplicates the maximum number of duplicates to add
     * @param arbitrary     the arbitrary list to duplicate items in
     * @param <T>           the type of the list
     * @return an arbitrary list with duplicates
     */
    private static <T> Arbitrary<List<T>> withDuplicates(
        final int maxDuplicates,
        final ListArbitrary<T> arbitrary)
    {
        return Combinators.combine(
            Arbitraries.integers().list().ofMaxSize(2 * maxDuplicates),
            arbitrary
        ).as((positions, originalItems) ->
        {
            if (originalItems.isEmpty())
            {
                return originalItems;
            }

            final List<T> items = new ArrayList<>(originalItems);
            T itemToDupe = null;
            int j = 0;

            for (final int position : positions)
            {
                j += position;
                j %= items.size();
                j = Math.abs(j);
                if (itemToDupe == null)
                {
                    itemToDupe = items.get(j);
                }
                else
                {
                    items.add(j, itemToDupe);
                    itemToDupe = null;
                }
            }

            return items;
        });
    }

    private static Arbitrary<EncodedDataTypeSchema> encodedDataType()
    {
        return Combinators.combine(
            Arbitraries.of(PrimitiveType.values()),
            Arbitraries.of(true, false)
        ).as(EncodedDataTypeSchema::new);
    }

    private static Arbitrary<EnumTypeSchema> enumType()
    {
        return Arbitraries.oneOf(
            Arbitraries.chars().alpha()
                .map(Character::toUpperCase)
                .list()
                .ofMaxSize(10)
                .uniqueElements()
                .map(values -> new EnumTypeSchema(
                    "char",
                    values.stream().map(String::valueOf).collect(Collectors.toList())
                )),
            Arbitraries.integers()
                .between(1, 254)
                .list()
                .ofMaxSize(254)
                .uniqueElements()
                .map(values -> new EnumTypeSchema(
                    "uint8",
                    values.stream().map(String::valueOf).collect(Collectors.toList())
                ))
        );
    }

    private static Arbitrary<SetSchema> setType()
    {
        return Combinators.combine(
            Arbitraries.of(
                "uint8",
                "uint16",
                "uint32",
                "uint64"
            ),
            Arbitraries.integers().between(1, 4)
        ).as(SetSchema::new);
    }

    private static Arbitrary<TypeSchema> compositeType(final int depth)
    {
        return withDuplicates(2, type(depth - 1).list().ofMinSize(1).ofMaxSize(3))
            .map(CompositeTypeSchema::new);
    }

    private static Arbitrary<TypeSchema> type(final int depth)
    {
        if (depth == 1)
        {
            return Arbitraries.oneOf(
                encodedDataType(),
                enumType(),
                setType()
            );
        }
        else
        {
            return Arbitraries.oneOf(
                compositeType(depth),
                encodedDataType(),
                enumType(),
                setType()
            );
        }
    }

    private static Arbitrary<GroupSchema> group(final int depth)
    {
        final Arbitrary<List<GroupSchema>> subGroups = depth == 1 ?
            Arbitraries.of(0).map(ignored -> new ArrayList<>()) :
            group(depth - 1).list().ofMaxSize(3);

        return Combinators.combine(
            withDuplicates(2, type(MAX_COMPOSITE_DEPTH).list().ofMaxSize(5)),
            subGroups,
            varData().list().ofMaxSize(3)
        ).as(GroupSchema::new);
    }

    private static Arbitrary<VarDataSchema> varData()
    {
        return Arbitraries.of(VarDataSchema.Encoding.values())
            .map(VarDataSchema::new);
    }

    public static Arbitrary<MessageSchema> message()
    {
        return Combinators.combine(
            withDuplicates(3, type(MAX_COMPOSITE_DEPTH).list().ofMaxSize(10)),
            group(MAX_GROUP_DEPTH).list().ofMaxSize(3),
            varData().list().ofMaxSize(3)
        ).as(MessageSchema::new);
    }
}
