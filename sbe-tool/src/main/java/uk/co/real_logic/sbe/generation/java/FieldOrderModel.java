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
import org.agrona.collections.MutableReference;

import java.util.*;
import java.util.function.Consumer;

import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

final class FieldOrderModel
{
    private static final boolean BLOCK_SKIP_CHECK_ENABLED = Boolean.getBoolean("sbe.block.skip.check.enabled");
    private final Int2ObjectHashMap<State> states = new Int2ObjectHashMap<>();
    private final Map<Token, TransitionGroup> transitions = new LinkedHashMap<>();
    private final Set<String> reservedNames = new HashSet<>();
    private final State notWrappedState = allocateState("NOT_WRAPPED");
    private final State wrappedState = allocateState("WRAPPED");
    private int transitionNumber;

    public State notWrappedState()
    {
        return notWrappedState;
    }

    public State wrappedState()
    {
        return wrappedState;
    }

    public void forEachState(final Consumer<State> consumer)
    {
        states.values()
            .stream()
            .sorted(Comparator.comparingInt(s -> s.number))
            .forEach(consumer);
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

    public void findTransitions(
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        findTransitions(
            Collections.singletonList(wrappedState),
            BLOCK_SKIP_CHECK_ENABLED ? null : wrappedState,
            "",
            fields,
            groups,
            varData
        );
    }

    @SuppressWarnings("checkstyle:MethodLength")
    private List<State> findTransitions(
        final List<State> entryStates,
        final State blockStateOrNull,
        final String prefix,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData
    )
    {
        final MutableReference<State> blockState = new MutableReference<>(blockStateOrNull);

        final List<State> fromStates = new ArrayList<>(entryStates);

        Generators.forEachField(fields, (token, ignored) ->
        {
            if (null == blockState.get())
            {
                blockState.set(allocateState(prefix.isEmpty() ? "BLOCK" : prefix + "_BLOCK"));
                fromStates.add(blockState.get());
            }

            allocateTransition(
                TransitionContext.NONE,
                "FILL_" + prefix + token.name().toUpperCase(),
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

            final String groupName = token.name().toUpperCase();
            final String groupPrefix = prefix.isEmpty() ? groupName : prefix + "_" + groupName;

            final List<State> beginGroupStates = new ArrayList<>(fromStates);

            final State nRemainingGroup = allocateState(groupPrefix + "_N");
            final State nRemainingGroupElement = allocateState(groupPrefix + "_N_ELEMENT");
            final State oneRemainingGroupElement = allocateState(groupPrefix + "_1_ELEMENT");
            final State emptyGroup = allocateState(groupPrefix + "_0");

            // fooCount(0)
            allocateTransition(
                TransitionContext.SELECT_EMPTY_GROUP,
                "ZERO_" + groupPrefix,
                token,
                beginGroupStates,
                emptyGroup);

            // fooCount(N) where N > 0
            allocateTransition(
                TransitionContext.SELECT_MULTI_ELEMENT_GROUP,
                "MANY_" + groupPrefix,
                token,
                beginGroupStates,
                nRemainingGroup);

            fromStates.clear();
            fromStates.add(nRemainingGroupElement);
            final List<State> nRemainingExitStates = findTransitions(
                fromStates,
                BLOCK_SKIP_CHECK_ENABLED ? null : nRemainingGroupElement,
                groupPrefix + "_N",
                groupFields,
                groupGroups,
                groupVarData);

            fromStates.clear();
            fromStates.add(nRemainingGroup);
            fromStates.addAll(nRemainingExitStates);

            // where more than one element remains in the group
            allocateTransition(
                TransitionContext.NEXT_ELEMENT_IN_GROUP,
                "NEXT_" + groupPrefix,
                token,
                fromStates,
                nRemainingGroupElement);

            fromStates.clear();
            fromStates.add(nRemainingGroup);
            fromStates.addAll(nRemainingExitStates);

            // where only one element remains in the group
            allocateTransition(
                TransitionContext.LAST_ELEMENT_IN_GROUP,
                "LAST_" + groupPrefix,
                token,
                fromStates,
                oneRemainingGroupElement);

            fromStates.clear();
            fromStates.add(oneRemainingGroupElement);

            final List<State> oneRemainingExitStates = findTransitions(
                fromStates,
                BLOCK_SKIP_CHECK_ENABLED ? null : oneRemainingGroupElement,
                groupPrefix + "_1",
                groupFields,
                groupGroups,
                groupVarData);

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

            final State state = allocateState("FILLED_" + prefix + "_" + token.name().toUpperCase());
            allocateTransition(
                TransitionContext.NONE,
                "FILL_" + prefix + "_" + token.name().toUpperCase(),
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

        final State state = new State(states.size() + 1, name);
        states.put(state.number, state);
        return state;
    }

    private void allocateTransition(
        final TransitionContext context,
        final String name,
        final Token token,
        final List<State> from,
        final State to)
    {
        if (!reservedNames.add(name))
        {
            throw new IllegalStateException("Name is already reserved: " + name);
        }

        final TransitionGroup transitionGroup = transitions.computeIfAbsent(token, ignored -> new TransitionGroup());
        final Transition transition = new Transition(nextTransitionNumber(), name, from, to);
        transitionGroup.add(context, transition);
    }

    private int nextTransitionNumber()
    {
        return transitionNumber++;
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
    }

    static final class Transition
    {
        private final int number;
        private final String name;
        private final Set<State> from;
        private final State to;

        private Transition(final int number, final String name, final List<State> from, final State to)
        {
            this.number = number;
            this.name = name;
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
    }

    enum TransitionContext
    {
        NONE,
        SELECT_EMPTY_GROUP,
        SELECT_MULTI_ELEMENT_GROUP,
        NEXT_ELEMENT_IN_GROUP,
        LAST_ELEMENT_IN_GROUP
    }

    private static final class TransitionGroup
    {
        private final Map<TransitionContext, List<Transition>> transitions = new LinkedHashMap<>();

        public void add(final TransitionContext context, final Transition transition)
        {
            final List<Transition> transitionsForContext =
                transitions.computeIfAbsent(context, ignored -> new ArrayList<>());

            final boolean duplicateEndState =
                transitionsForContext.stream().anyMatch(t -> t.to.number == transition.to.number);

            if (duplicateEndState)
            {
                throw new IllegalStateException("Duplicate end state: " + transition.to.name);
            }

            final boolean conflictingStartState =
                transitionsForContext.stream().anyMatch(t -> t.from.stream().anyMatch(transition.from::contains));

            if (conflictingStartState)
            {
                throw new IllegalStateException("Conflicting start states: " + transition.from);
            }

            transitionsForContext.add(transition);
        }
    }
}
