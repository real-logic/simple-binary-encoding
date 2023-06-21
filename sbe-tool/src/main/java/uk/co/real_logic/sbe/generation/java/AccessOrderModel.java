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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

// Lambdas without braces tend to conflict with the indentation Checkstyle expects.
// Therefore, we allow lambdas with code blocks even when a lambda expression is possible.
@SuppressWarnings("CodeBlock2Expr")
final class AccessOrderModel
{
    private static final boolean GENERATE_ACCESS_ORDER_CHECKS = Boolean.parseBoolean(
        System.getProperty("sbe.generate.access.order.checks", "true"));
    private final Map<Token, String> groupPathsByField = new HashMap<>();
    private final Set<Token> topLevelBlockFields = new HashSet<>();
    private final CodecInteraction.HashConsingFactory interactionFactory =
        new CodecInteraction.HashConsingFactory(groupPathsByField, topLevelBlockFields);
    private final Map<CodecInteraction, List<TransitionGroup>> transitionsByInteraction = new LinkedHashMap<>();
    private final Map<State, List<TransitionGroup>> transitionsByState = new HashMap<>();
    private final Int2ObjectHashMap<State> versionWrappedStates = new Int2ObjectHashMap<>();
    private final State notWrappedState = allocateState("NOT_WRAPPED");
    private State encoderWrappedState;

    static boolean generateAccessOrderChecks()
    {
        return GENERATE_ACCESS_ORDER_CHECKS;
    }

    static AccessOrderModel newInstance(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Function<IntStream, IntStream> versionsSelector)
    {
        final AccessOrderModel model = new AccessOrderModel();
        model.findTransitions(msgToken, fields, groups, varData, versionsSelector);
        return model;
    }

    State notWrappedState()
    {
        return notWrappedState;
    }

    State latestVersionWrappedState()
    {
        return encoderWrappedState;
    }

    void forEachDecoderWrappedState(final IntObjConsumer<State> consumer)
    {
        final Int2ObjectHashMap<State>.EntryIterator iterator = versionWrappedStates.entrySet().iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            consumer.accept(iterator.getIntKey(), iterator.getValue());
        }
    }

    void forEachStateOrderedByStateNumber(final Consumer<State> consumer)
    {
        transitionsByState.keySet().stream()
            .sorted(Comparator.comparingInt(s -> s.number))
            .forEach(consumer);
    }

    CodecInteraction.HashConsingFactory interactionFactory()
    {
        return interactionFactory;
    }

    void getTransitions(
        final List<TransitionGroup> transitionsOut,
        final CodecInteraction interaction)
    {
        final List<TransitionGroup> transitionsForContext = transitionsByInteraction.get(interaction);
        if (null != transitionsForContext)
        {
            transitionsOut.addAll(transitionsForContext);
        }
    }

    void forEachTransitionFrom(final State state, final Consumer<TransitionGroup> consumer)
    {
        final List<TransitionGroup> transitionGroups = transitionsByState.get(state);
        if (null != transitionGroups)
        {
            transitionGroups.forEach(consumer);
        }
    }

    @SuppressWarnings("SameParameterValue")
    void generateGraph(final StringBuilder sb, final String indent)
    {
        sb.append(indent).append("digraph G {\n");
        transitionsByInteraction.values().forEach(transitionsForContext ->
        {
            transitionsForContext.forEach(transition ->
            {
                transition.forEachStartState(startState ->
                {
                    sb.append(indent).append("    ")
                        .append(startState.name)
                        .append(" -> ")
                        .append(transition.endState().name)
                        .append(" [label=\"  ").append(transition.interaction.exampleCode());

                    if (!transition.interaction.exampleConditions().isEmpty())
                    {
                        sb.append("\\n").append("  where ").append(transition.interaction.exampleConditions());
                    }

                    sb.append("  \"];\n");
                });
            });
        });
        sb.append(indent).append("}\n");
    }

    private void findTransitions(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Function<IntStream, IntStream> versionsSelector)
    {
        final IntHashSet versions = new IntHashSet();
        versions.add(msgToken.version());
        walkSchemaLevel(new VersionCollector(versions), fields, groups, varData);
        walkSchemaLevel(new PathCollector(topLevelBlockFields, groupPathsByField), fields, groups, varData);

        final IntStream selectedVersions = versionsSelector.apply(versions.stream().mapToInt(i -> i));
        selectedVersions.sorted().forEach(version ->
        {
            final State versionWrappedState = allocateState("V" + version + "_BLOCK");

            versionWrappedStates.put(version, versionWrappedState);

            encoderWrappedState = versionWrappedState;

            final CodecInteraction wrapInteraction = interactionFactory.wrap(version);

            allocateTransitions(
                wrapInteraction,
                Collections.singletonList(notWrappedState),
                versionWrappedState
            );

            final TransitionCollector transitionCollector = new TransitionCollector(
                "V" + version + "_",
                Collections.singleton(versionWrappedState),
                versionWrappedState,
                token -> token.version() <= version
            );

            walkSchemaLevel(transitionCollector, fields, groups, varData);
        });
    }

    private State allocateState(final String name)
    {
        final State state = new State(transitionsByState.size(), name);
        transitionsByState.put(state, new ArrayList<>());
        return state;
    }

    private void allocateTransitions(
        final CodecInteraction interaction,
        final Collection<State> from,
        final State to)
    {
        final TransitionGroup transitionGroup = new TransitionGroup(interaction, from, to);
        final List<TransitionGroup> transitionsForInteraction =
            transitionsByInteraction.computeIfAbsent(interaction, ignored -> new ArrayList<>());

        final boolean duplicateEndState =
            transitionsForInteraction.stream().anyMatch(t -> t.to.number == transitionGroup.to.number);

        if (duplicateEndState)
        {
            throw new IllegalStateException("Duplicate end state: " + transitionGroup.to.name);
        }

        final Optional<TransitionGroup> conflictingTransition = transitionsForInteraction.stream()
            .filter(t -> t.from.stream().anyMatch(transitionGroup.from::contains))
            .findAny();

        if (conflictingTransition.isPresent())
        {
            throw new IllegalStateException(
                "Conflicting transition: " + transitionGroup + " conflicts with " + conflictingTransition.get());
        }

        transitionsForInteraction.add(transitionGroup);

        from.forEach(fromState -> transitionsByState.get(fromState).add(transitionGroup));
    }

    private static void walkSchemaLevel(
        final SchemaConsumer consumer,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        Generators.forEachField(fields, (token, ignored) -> consumer.onBlockField(token));

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

            consumer.onEnterRepeatingGroup(token, groupFields, groupGroups, groupVarData);
        }

        for (int i = 0; i < varData.size(); )
        {
            final Token token = varData.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }
            i += token.componentTokenCount();

            consumer.onVarData(token);
        }
    }

    private interface SchemaConsumer
    {
        void onBlockField(Token token);

        void onEnterRepeatingGroup(
            Token token,
            List<Token> groupFields,
            List<Token> groupGroups,
            List<Token> groupVarData);

        void onVarData(Token token);
    }

    private static final class PathCollector implements SchemaConsumer
    {
        private final ArrayDeque<Token> groupPath = new ArrayDeque<>();
        private final Set<Token> topLevelBlockFields;
        private final Map<Token, String> groupPathsByField;

        private PathCollector(
            final Set<Token> topLevelBlockFields,
            final Map<Token, String> groupPathsByField)
        {
            this.topLevelBlockFields = topLevelBlockFields;
            this.groupPathsByField = groupPathsByField;
        }

        @Override
        public void onBlockField(final Token token)
        {
            if (groupPath.isEmpty())
            {
                topLevelBlockFields.add(token);
            }

            groupPathsByField.put(token, currentGroupPath());
        }

        @Override
        public void onEnterRepeatingGroup(
            final Token token,
            final List<Token> groupFields,
            final List<Token> groupGroups,
            final List<Token> groupVarData)
        {
            groupPathsByField.put(token, currentGroupPath());
            groupPath.addLast(token);
            walkSchemaLevel(this, groupFields, groupGroups, groupVarData);
            groupPath.removeLast();
        }

        @Override
        public void onVarData(final Token token)
        {
            groupPathsByField.put(token, currentGroupPath());
        }

        private String currentGroupPath()
        {
            final StringBuilder sb = new StringBuilder();
            groupPath.forEach(token ->
            {
                sb.append(token.name()).append('.');
            });
            return sb.toString();
        }
    }

    private static final class VersionCollector implements SchemaConsumer
    {
        private final IntHashSet versions;

        VersionCollector(final IntHashSet versions)
        {
            this.versions = versions;
        }

        @Override
        public void onBlockField(final Token token)
        {
            versions.add(token.version());
        }

        @Override
        public void onEnterRepeatingGroup(
            final Token token,
            final List<Token> groupFields,
            final List<Token> groupGroups,
            final List<Token> groupVarData)
        {
            versions.add(token.version());
            walkSchemaLevel(this, groupFields, groupGroups, groupVarData);
        }

        @Override
        public void onVarData(final Token token)
        {
            versions.add(token.version());
        }
    }

    private final class TransitionCollector implements SchemaConsumer
    {
        private final String statePrefix;
        private final HashSet<State> currentStates;
        private final State blockState;
        private final Predicate<Token> filter;

        private TransitionCollector(
            final String statePrefix,
            final Set<State> fromStates,
            final State blockState,
            final Predicate<Token> filter)
        {
            this.statePrefix = statePrefix;
            this.currentStates = new HashSet<>(fromStates);
            this.blockState = blockState;
            this.filter = filter;

            currentStates.add(blockState);
        }

        @Override
        public void onBlockField(final Token token)
        {
            if (filter.test(token))
            {
                final CodecInteraction codecInteraction = interactionFactory.accessField(token);
                allocateTransitions(codecInteraction, currentStates, blockState);
            }
        }

        @Override
        public void onEnterRepeatingGroup(
            final Token token,
            final List<Token> groupFields,
            final List<Token> groupGroups,
            final List<Token> groupVarData)
        {
            if (filter.test(token))
            {
                final String groupName = token.name().toUpperCase();
                final String groupPrefix = statePrefix + groupName + "_";
                final State nRemainingGroup = allocateState(groupPrefix + "N");
                final State nRemainingGroupElement = allocateState(groupPrefix + "N_BLOCK");
                final State oneRemainingGroupElement = allocateState(groupPrefix + "1_BLOCK");
                final State emptyGroup = allocateState(groupPrefix + "0");

                final Set<State> beginGroupStates = new HashSet<>(currentStates);

                // fooCount(0)
                final CodecInteraction emptyGroupInteraction = interactionFactory.determineGroupIsEmpty(token);
                allocateTransitions(emptyGroupInteraction, beginGroupStates, emptyGroup);

                // fooCount(N) where N > 0
                final CodecInteraction nonEmptyGroupInteraction = interactionFactory.determineGroupHasElements(token);
                allocateTransitions(nonEmptyGroupInteraction, beginGroupStates, nRemainingGroup);

                currentStates.clear();
                currentStates.add(nRemainingGroupElement);
                final TransitionCollector nRemainingCollector = new TransitionCollector(
                    groupPrefix + "N_",
                    currentStates,
                    nRemainingGroupElement,
                    filter
                );
                walkSchemaLevel(nRemainingCollector, groupFields, groupGroups, groupVarData);

                currentStates.clear();
                currentStates.add(nRemainingGroup);
                currentStates.addAll(nRemainingCollector.exitStates());

                // where more than one element remains in the group
                final CodecInteraction nextGroupElementInteraction = interactionFactory.moveToNextElement(token);
                allocateTransitions(nextGroupElementInteraction, currentStates, nRemainingGroupElement);

                currentStates.clear();
                currentStates.add(nRemainingGroup);
                currentStates.addAll(nRemainingCollector.exitStates());

                // where only one element remains in the group
                final CodecInteraction lastGroupElementInteraction = interactionFactory.moveToLastElement(token);
                allocateTransitions(lastGroupElementInteraction, currentStates, oneRemainingGroupElement);

                currentStates.clear();
                currentStates.add(oneRemainingGroupElement);

                final TransitionCollector oneRemainingCollector = new TransitionCollector(
                    groupPrefix + "1_",
                    currentStates,
                    oneRemainingGroupElement,
                    filter
                );
                walkSchemaLevel(oneRemainingCollector, groupFields, groupGroups, groupVarData);

                currentStates.clear();
                currentStates.add(emptyGroup);
                currentStates.addAll(oneRemainingCollector.exitStates());
            }
        }

        @Override
        public void onVarData(final Token token)
        {
            if (filter.test(token))
            {
                final CodecInteraction codecInteraction = interactionFactory.accessField(token);
                final State accessedState = allocateState(statePrefix + token.name().toUpperCase() + "_DONE");
                allocateTransitions(codecInteraction, currentStates, accessedState);
                currentStates.clear();
                currentStates.add(accessedState);
            }
        }

        private Set<State> exitStates()
        {
            return currentStates;
        }
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

        /**
         * In the scope of an {@code AccessOrderModel} instance, state numbers are contiguous
         * and start at 0. This numbering scheme allows easy generation of lookup tables.
         * @return the state number
         */
        int number()
        {
            return number;
        }

        String name()
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

    static final class TransitionGroup
    {
        private final CodecInteraction interaction;
        private final Set<State> from;
        private final State to;

        private TransitionGroup(
            final CodecInteraction interaction,
            final Collection<State> from,
            final State to)
        {
            this.interaction = interaction;
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

        String exampleCode()
        {
            return interaction.exampleCode();
        }

        @Override
        public String toString()
        {
            return "Transition{" +
                "interaction='" + exampleCode() + '\'' +
                ", from=" + from +
                ", to=" + to +
                '}';
        }
    }

    abstract static class CodecInteraction
    {
        abstract String groupQualifiedName();

        abstract String exampleCode();

        abstract String exampleConditions();

        final boolean isTopLevelBlockFieldAccess()
        {
            if (this instanceof AccessField)
            {
                final AccessField accessField = (AccessField)this;
                return accessField.isTopLevelBlockField();
            }

            return false;
        }

        /**
         * When an encoder or decoder is wrapped around a buffer.
         */
        private static final class Wrap extends CodecInteraction
        {
            private final int version;

            Wrap(final int version)
            {
                this.version = version;
            }

            @Override
            String groupQualifiedName()
            {
                return "wrap";
            }

            @Override
            String exampleCode()
            {
                return "wrap(version=" + version + ")";
            }

            @Override
            String exampleConditions()
            {
                return "";
            }
        }

        /**
         * When a block or variable-length field is accessed.
         */
        private static final class AccessField extends CodecInteraction
        {
            private final String groupPath;
            private final Token token;
            private final boolean isTopLevelBlockField;

            private AccessField(final String groupPath, final Token token, final boolean isTopLevelBlockField)
            {
                this.isTopLevelBlockField = isTopLevelBlockField;
                assert groupPath != null;
                assert token.signal() == Signal.BEGIN_FIELD || token.signal() == Signal.BEGIN_VAR_DATA;
                this.groupPath = groupPath;
                this.token = token;
            }

            boolean isTopLevelBlockField()
            {
                return isTopLevelBlockField;
            }

            @Override
            String groupQualifiedName()
            {
                return groupPath + token.name();
            }

            @Override
            String exampleCode()
            {
                return groupPath + token.name() + "(?)";
            }

            @Override
            String exampleConditions()
            {
                return "";
            }
        }

        /**
         * When a repeating group count is supplied as zero.
         */
        private static final class DetermineGroupIsEmpty extends CodecInteraction
        {
            private final String groupPath;
            private final Token token;

            private DetermineGroupIsEmpty(final String groupPath, final Token token)
            {
                assert groupPath != null;
                assert token.signal() == Signal.BEGIN_GROUP;
                this.groupPath = groupPath;
                this.token = token;
            }

            @Override
            String groupQualifiedName()
            {
                return groupPath + token.name();
            }

            @Override
            String exampleCode()
            {
                return groupPath + token.name() + "Count(0)";
            }

            @Override
            String exampleConditions()
            {
                return "";
            }
        }

        /**
         * When a repeating group count is supplied as greater than zero.
         */
        private static final class DetermineGroupHasElements extends CodecInteraction
        {
            private final String groupPath;
            private final Token token;

            private DetermineGroupHasElements(final String groupPath, final Token token)
            {
                assert groupPath != null;
                assert token.signal() == Signal.BEGIN_GROUP;
                this.groupPath = groupPath;
                this.token = token;
            }

            @Override
            String groupQualifiedName()
            {
                return groupPath + token.name();
            }

            @Override
            String exampleCode()
            {
                return groupPath + token.name() + "Count(>0)";
            }

            @Override
            String exampleConditions()
            {
                return "";
            }
        }

        /**
         * When the next element in a repeating group is accessed,
         * and it is not the last element.
         */
        private static final class MoveToNextElement extends CodecInteraction
        {
            private final String groupPath;
            private final Token token;

            private MoveToNextElement(final String groupPath, final Token token)
            {
                assert groupPath != null;
                assert token.signal() == Signal.BEGIN_GROUP;
                this.groupPath = groupPath;
                this.token = token;
            }

            @Override
            String groupQualifiedName()
            {
                return groupPath + token.name();
            }

            @Override
            String exampleCode()
            {
                return groupPath + token.name() + ".next()";
            }

            @Override
            String exampleConditions()
            {
                return "count - newIndex > 1";
            }
        }

        /**
         * When the next element in a repeating group is accessed,
         * and it is the last element.
         */
        private static final class MoveToLastElement extends CodecInteraction
        {
            private final String groupPath;
            private final Token token;

            private MoveToLastElement(final String groupPath, final Token token)
            {
                assert groupPath != null;
                assert token.signal() == Signal.BEGIN_GROUP;
                this.groupPath = groupPath;
                this.token = token;
            }

            @Override
            String groupQualifiedName()
            {
                return groupPath + token.name();
            }

            @Override
            String exampleCode()
            {
                return groupPath + token.name() + ".next()";
            }

            @Override
            String exampleConditions()
            {
                return "count - newIndex == 1";
            }
        }

        static final class HashConsingFactory
        {
            private final Int2ObjectHashMap<CodecInteraction> wrapInteractions = new Int2ObjectHashMap<>();
            private final Map<Token, CodecInteraction> accessFieldInteractions = new HashMap<>();
            private final Map<Token, CodecInteraction> determineGroupIsEmptyInteractions = new HashMap<>();
            private final Map<Token, CodecInteraction> determineGroupHasElementsInteractions = new HashMap<>();
            private final Map<Token, CodecInteraction> moveToNextElementInteractions = new HashMap<>();
            private final Map<Token, CodecInteraction> moveToLastElementInteractions = new HashMap<>();
            private final Map<Token, String> groupPathsByField;
            private final Set<Token> topLevelBlockFields;

            HashConsingFactory(
                final Map<Token, String> groupPathsByField,
                final Set<Token> topLevelBlockFields)
            {

                this.groupPathsByField = groupPathsByField;
                this.topLevelBlockFields = topLevelBlockFields;
            }

            CodecInteraction wrap(final int version)
            {
                return wrapInteractions.computeIfAbsent(version, Wrap::new);
            }

            CodecInteraction accessField(final Token token)
            {
                return accessFieldInteractions.computeIfAbsent(token,
                    t -> new AccessField(groupPathsByField.get(t), t, topLevelBlockFields.contains(t)));
            }

            CodecInteraction determineGroupIsEmpty(final Token token)
            {
                return determineGroupIsEmptyInteractions.computeIfAbsent(token,
                    t -> new DetermineGroupIsEmpty(groupPathsByField.get(t), t));
            }

            CodecInteraction determineGroupHasElements(final Token token)
            {
                return determineGroupHasElementsInteractions.computeIfAbsent(token,
                    t -> new DetermineGroupHasElements(groupPathsByField.get(t), t));
            }

            CodecInteraction moveToNextElement(final Token token)
            {
                return moveToNextElementInteractions.computeIfAbsent(token,
                    t -> new MoveToNextElement(groupPathsByField.get(t), t));
            }

            CodecInteraction moveToLastElement(final Token token)
            {
                return moveToLastElementInteractions.computeIfAbsent(token,
                    t -> new MoveToLastElement(groupPathsByField.get(t), t));
            }
        }
    }
}
