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
package uk.co.real_logic.sbe.generation.java;

import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.IntHashSet;
import org.agrona.collections.IntObjConsumer;
import org.agrona.collections.MutableReference;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

// There is no abstraction for visiting fields, groups, and varData. Therefore, there is some "duplication".
// Lambdas without braces tend to conflict with checkstyle. Therefore, we allow braces when an expression is possible.
@SuppressWarnings({"DuplicatedCode", "CodeBlock2Expr"})
final class FieldOrderModel
{
    private static final boolean GENERATE_ACCESS_ORDER_CHECKS = Boolean.parseBoolean(
        System.getProperty("sbe.generate.access.order.checks", "true"));
    private final Int2ObjectHashMap<State> states = new Int2ObjectHashMap<>();
    private final Map<Token, TransitionGroup> transitions = new LinkedHashMap<>();
    private final Set<Token> topLevelBlockFields = new HashSet<>();
    private final Int2ObjectHashMap<State> versionWrappedStates = new Int2ObjectHashMap<>();
    private final Set<String> reservedNames = new HashSet<>();
    private final State notWrappedState = allocateState("NOT_WRAPPED");
    private State encoderWrappedState;

    public static boolean generateAccessOrderChecks()
    {
        return GENERATE_ACCESS_ORDER_CHECKS;
    }

    public static FieldOrderModel newInstance(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        final FieldOrderModel model = new FieldOrderModel();
        model.findTransitions(msgToken, fields, groups, varData);
        return model;
    }

    public State notWrappedState()
    {
        return notWrappedState;
    }

    public State latestVersionWrappedState()
    {
        return encoderWrappedState;
    }

    public void forEachDecoderWrappedState(final IntObjConsumer<State> consumer)
    {
        final Int2ObjectHashMap<State>.EntryIterator iterator = versionWrappedStates.entrySet().iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            consumer.accept(iterator.getIntKey(), iterator.getValue());
        }
    }

    public void forEachStateOrderedByNumber(final Consumer<State> consumer)
    {
        states.values()
            .stream()
            .sorted(Comparator.comparingInt(s -> s.number))
            .forEach(consumer);
    }

    public boolean isTopLevelBlockField(final Token token)
    {
        return topLevelBlockFields.contains(token);
    }

    public String fieldPath(final Token token)
    {
        final StringBuilder sb = new StringBuilder();
        final TransitionGroup transitionGroup = transitions.get(token);
        if (null != transitionGroup)
        {
            transitionGroup.groupPath.forEach(groupToken ->
            {
                sb.append(groupToken.name()).append('.');
            });
        }
        sb.append(token.name());
        return sb.toString();
    }

    public List<Transition> getTransitions(final TransitionContext context, final Token token)
    {
        final TransitionGroup transitionGroup = transitions.get(token);
        if (transitionGroup == null)
        {
            return Collections.emptyList();
        }

        return transitionGroup.transitions.get(context);
    }

    public void generateGraph(
        final StringBuilder sb,
        final String indent)
    {
        sb.append(indent).append("digraph G {\n");
        transitions.values().forEach(transitionGroup ->
            transitionGroup.transitions.forEach((context, transitions1) ->
            {
                transitions1.forEach(transition ->
                {
                    transition.forEachStartState(startState ->
                    {
                        sb.append(indent).append("    ")
                            .append(startState.name)
                            .append(" -> ")
                            .append(transition.endState().name)
                            .append(" [label=\"  ").append(transition.description).append("  \"];\n");
                    });
                });
            })
        );
        sb.append(indent).append("}\n");
    }

    private static void findVersions(
        final IntHashSet versions,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        Generators.forEachField(fields, (token, ignored) -> versions.add(token.version()));

        for (int i = 0; i < groups.size(); i++)
        {
            final Token token = groups.get(i);
            if (token.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + token);
            }

            versions.add(token.version());

            ++i;
            final int groupHeaderTokenCount = groups.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final ArrayList<Token> groupFields = new ArrayList<>();
            i = collectFields(groups, i, groupFields);
            final ArrayList<Token> groupGroups = new ArrayList<>();
            i = collectGroups(groups, i, groupGroups);
            final ArrayList<Token> groupVarData = new ArrayList<>();
            i = collectVarData(groups, i, groupVarData);

            findVersions(versions, groupFields, groupGroups, groupVarData);
        }

        for (int i = 0; i < varData.size(); )
        {
            final Token token = varData.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }
            i += token.componentTokenCount();

            versions.add(token.version());
        }
    }

    private void findTransitions(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        final IntHashSet versions = new IntHashSet();
        versions.add(msgToken.version());
        findVersions(versions, fields, groups, varData);

        Generators.forEachField(fields, (fieldToken, ignored) -> topLevelBlockFields.add(fieldToken));

        versions.stream().sorted().forEach(version ->
        {
            final State versionWrappedState = allocateState("V" + version + "_BLOCK");

            versionWrappedStates.put(version, versionWrappedState);

            encoderWrappedState = versionWrappedState;

            allocateTransition(
                version,
                ".wrap(...)",
                Collections.emptyList(),
                null,
                Collections.singletonList(notWrappedState),
                versionWrappedState
            );

            findTransitions(
                Collections.singletonList(versionWrappedState),
                versionWrappedState,
                "V" + version + "_",
                new ArrayList<>(),
                fields,
                groups,
                varData,
                token -> token.version() <= version
            );
        });
    }

    @SuppressWarnings("checkstyle:MethodLength")
    private List<State> findTransitions(
        final List<State> entryStates,
        final State blockStateOrNull,
        final String prefix,
        final List<Token> groupPath,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Predicate<Token> filter)
    {
        final MutableReference<State> blockState = new MutableReference<>(blockStateOrNull);

        final List<State> fromStates = new ArrayList<>(entryStates);

        Generators.forEachField(fields, (token, ignored) ->
        {
            if (!filter.test(token))
            {
                return;
            }

            if (null == blockState.get())
            {
                blockState.set(allocateState(prefix + "BLOCK"));
                fromStates.add(blockState.get());
            }

            allocateTransition(
                TransitionContext.NONE,
                "." + token.name() + "(value)",
                groupPath,
                token,
                fromStates,
                blockState.get());
        });

        if (blockState.get() != null)
        {
            fromStates.clear();
            fromStates.add(blockState.get());
        }

        for (int i = 0; i < groups.size(); i++)
        {
            final Token token = groups.get(i);
            if (token.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + token);
            }

            ++i;
            final int groupHeaderTokenCount = groups.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final ArrayList<Token> groupFields = new ArrayList<>();
            i = collectFields(groups, i, groupFields);
            final ArrayList<Token> groupGroups = new ArrayList<>();
            i = collectGroups(groups, i, groupGroups);
            final ArrayList<Token> groupVarData = new ArrayList<>();
            i = collectVarData(groups, i, groupVarData);

            if (!filter.test(token))
            {
                continue;
            }

            final String groupName = token.name().toUpperCase();
            final String groupPrefix = prefix + groupName + "_";

            final List<State> beginGroupStates = new ArrayList<>(fromStates);
            final List<Token> newGroupPath = new ArrayList<>(groupPath);
            newGroupPath.add(token);

            final State nRemainingGroup = allocateState(groupPrefix + "N");
            final State nRemainingGroupElement = allocateState(groupPrefix + "N_BLOCK");
            final State oneRemainingGroupElement = allocateState(groupPrefix + "1_BLOCK");
            final State emptyGroup = allocateState(groupPrefix + "0");

            // fooCount(0)
            allocateTransition(
                TransitionContext.SELECT_EMPTY_GROUP,
                "." + token.name() + "Length(0)",
                groupPath,
                token,
                beginGroupStates,
                emptyGroup);

            // fooCount(N) where N > 0
            allocateTransition(
                TransitionContext.SELECT_MULTI_ELEMENT_GROUP,
                "." + token.name() + "Length(N) where N > 0",
                groupPath,
                token,
                beginGroupStates,
                nRemainingGroup);

            fromStates.clear();
            fromStates.add(nRemainingGroupElement);
            final List<State> nRemainingExitStates = findTransitions(
                fromStates,
                nRemainingGroupElement,
                groupPrefix + "N_",
                newGroupPath,
                groupFields,
                groupGroups,
                groupVarData,
                filter);

            fromStates.clear();
            fromStates.add(nRemainingGroup);
            fromStates.addAll(nRemainingExitStates);

            // where more than one element remains in the group
            allocateTransition(
                TransitionContext.NEXT_ELEMENT_IN_GROUP,
                token.name() + ".next()\\n&& count - index > 1",
                groupPath,
                token,
                fromStates,
                nRemainingGroupElement);

            fromStates.clear();
            fromStates.add(nRemainingGroup);
            fromStates.addAll(nRemainingExitStates);

            // where only one element remains in the group
            allocateTransition(
                TransitionContext.LAST_ELEMENT_IN_GROUP,
                token.name() + ".next()\\n&& count - index == 1",
                groupPath,
                token,
                fromStates,
                oneRemainingGroupElement);

            fromStates.clear();
            fromStates.add(oneRemainingGroupElement);

            final List<State> oneRemainingExitStates = findTransitions(
                fromStates,
                oneRemainingGroupElement,
                groupPrefix + "1_",
                newGroupPath,
                groupFields,
                groupGroups,
                groupVarData,
                filter);

            fromStates.clear();
            fromStates.add(emptyGroup);
            fromStates.addAll(oneRemainingExitStates);
        }

        for (int i = 0; i < varData.size(); )
        {
            final Token token = varData.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }
            i += token.componentTokenCount();

            if (!filter.test(token))
            {
                continue;
            }

            final State state = allocateState(prefix + token.name().toUpperCase() + "_DONE");
            allocateTransition(
                TransitionContext.NONE,
                "." + token.name() + "(value)",
                groupPath,
                token,
                fromStates,
                state);
            fromStates.clear();
            fromStates.add(state);
        }

        return fromStates;
    }

    private State allocateState(final String name)
    {
        if (!reservedNames.add(name))
        {
            throw new IllegalStateException("Name is already reserved: " + name);
        }

        final State state = new State(states.size(), name);
        states.put(state.number, state);
        return state;
    }

    private void allocateTransition(
        final Object firingContext,
        final String description,
        final List<Token> groupPath,
        final Token token,
        final List<State> from,
        final State to)
    {
        final TransitionGroup transitionGroup = transitions.computeIfAbsent(token,
            ignored -> new TransitionGroup(groupPath));
        final Transition transition = new Transition(description, from, to);
        transitionGroup.add(firingContext, transition);
    }

    static final class State
    {
        private final int number;
        private final String name;

        private State(final int number, final String name)
        {
            this.number = number;
            this.name = name;
        }

        public int number()
        {
            return number;
        }

        public String name()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return "State{" +
                "number=" + number +
                ", name='" + name + '\'' +
                '}';
        }
    }

    static final class Transition
    {
        private final String description;
        private final Set<State> from;
        private final State to;

        private Transition(final String description, final List<State> from, final State to)
        {
            this.description = description;
            this.from = new HashSet<>(from);
            this.to = to;
        }

        void forEachStartState(final Consumer<State> consumer)
        {
            from.forEach(consumer);
        }

        State endState()
        {
            return to;
        }

        @Override
        public String toString()
        {
            return "Transition{" +
                "description='" + description + '\'' +
                ", from=" + from +
                ", to=" + to +
                '}';
        }
    }

    enum TransitionContext
    {
        /**
         * For tokens with a set of transitions that does not depend on any context, e.g.,
         * when a block field is accessed. As opposed to a repeating group, where the
         * transitions depend both on the number of remaining elements in the group and
         * whether {@code next()} is called or {@code myGroupCount(int count)}.
         */
        NONE,

        /**
         * When a repeating group count is supplied as zero.
         */
        SELECT_EMPTY_GROUP,

        /**
         * When a repeating group count is supplied as greater than zero.
         */
        SELECT_MULTI_ELEMENT_GROUP,

        /**
         * When the next element in a repeating group is accessed,
         * and it is not the last element.
         */
        NEXT_ELEMENT_IN_GROUP,

        /**
         * When the next element in a repeating group is accessed,
         * and it is the last element.
         */
        LAST_ELEMENT_IN_GROUP
    }

    /**
     * The codec state transitions possible for a given block/group/data field.
     */
    private static final class TransitionGroup
    {
        private final Map<Object, List<Transition>> transitions = new LinkedHashMap<>();
        private final List<Token> groupPath;

        private TransitionGroup(final List<Token> groupPath)
        {
            this.groupPath = groupPath;
        }

        public void add(final Object context, final Transition transition)
        {
            final List<Transition> transitionsForContext =
                transitions.computeIfAbsent(context, ignored -> new ArrayList<>());

            final boolean duplicateEndState =
                transitionsForContext.stream().anyMatch(t -> t.to.number == transition.to.number);

            if (duplicateEndState)
            {
                throw new IllegalStateException("Duplicate end state: " + transition.to.name);
            }

            final Optional<Transition> conflictingTransition = transitionsForContext.stream()
                .filter(t -> t.from.stream().anyMatch(transition.from::contains))
                .findAny();

            if (conflictingTransition.isPresent())
            {
                throw new IllegalStateException(
                    "Conflicting transition: " + transition + " conflicts with " + conflictingTransition.get());
            }

            transitionsForContext.add(transition);
        }
    }
}
