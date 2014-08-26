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
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.util.Verify;

import java.util.*;

/**
 * Intermediate representation of SBE messages to be used for the generation of encoders and decoders
 * as stubs in various languages.
 */
public class Ir
{
    private final String packageName;
    private final String namespaceName;
    private final int id;
    private final int version;
    private final String semanticVersion;

    private final HeaderStructure headerStructure;
    private final Map<Long, List<Token>> messagesByIdMap = new HashMap<>();
    private final Map<String, List<Token>> typesByNameMap = new HashMap<>();

    /**
     * Create a new IR container taking a defensive copy of the headerStructure {@link Token}s passed.
     *
     * @param packageName     that should be applied to generated code.
     * @param namespaceName   that should be applied to generated code.
     * @param id              identifier for the schema.
     * @param version         of the schema
     * @param semanticVersion semantic version for mapping to the application domain.
     * @param headerTokens    representing the message headerStructure.
     */
    public Ir(
        final String packageName,
        final String namespaceName,
        final int id,
        final int version,
        final String semanticVersion,
        final List<Token> headerTokens)
    {
        Verify.notNull(packageName, "packageName");
        Verify.notNull(headerTokens, "headerTokens");

        this.packageName = packageName;
        this.namespaceName = namespaceName;
        this.id = id;
        this.version = version;
        this.semanticVersion = semanticVersion;
        this.headerStructure = new HeaderStructure(Collections.unmodifiableList(new ArrayList<>(headerTokens)));
    }

    /**
     * Return the {@link HeaderStructure} description for all messages.
     *
     * @return the {@link HeaderStructure} description for all messages.
     */
    public HeaderStructure headerStructure()
    {
        return headerStructure;
    }

    /**
     * Add a List of {@link Token}s for a given message id.
     *
     * @param messageId     to identify the list of tokens for the message.
     * @param messageTokens the List of {@link Token}s representing the message.
     */
    public void addMessage(final long messageId, final List<Token> messageTokens)
    {
        Verify.notNull(messageTokens, "messageTokens");

        captureTypes(messageTokens);

        messagesByIdMap.put(Long.valueOf(messageId), Collections.unmodifiableList(new ArrayList<>(messageTokens)));
    }

    /**
     * Get the getMessage for a given identifier.
     *
     * @param messageId to get.
     * @return the List of {@link Token}s representing the message or null if the id is not found.
     */
    public List<Token> getMessage(final long messageId)
    {
        return messagesByIdMap.get(Long.valueOf(messageId));
    }

    /**
     * Get the type representation for a given type name.
     *
     * @param name of type to get.
     * @return the List of {@link Token}s representing the type or null if the name is not found.
     */
    public List<Token> getType(final String name)
    {
        return typesByNameMap.get(name);
    }

    /**
     * Get the {@link Collection} of types in for this schema.
     *
     * @return the {@link Collection} of types in for this schema.
     */
    public Collection<List<Token>> types()
    {
        return typesByNameMap.values();
    }

    /**
     * The {@link Collection} of messages in this schema.
     *
     * @return the {@link Collection} of messages in this schema.
     */
    public Collection<List<Token>> messages()
    {
        return messagesByIdMap.values();
    }

    /**
     * Get the package name to be used for generated code.
     *
     * @return the package name to be used for generated code.
     */
    public String packageName()
    {
        return packageName;
    }

    /**
     * Get the namespaceName to be used for generated code.
     *
     * @return the namespaceName to be used for generated code.
     */
    public String namespaceName()
    {
        return namespaceName;
    }

    /**
     * Get the id number of the schema.
     *
     * @return id number of the schema.
     */
    public int id()
    {
        return id;
    }

    /**
     * Get the version of the schema.
     *
     * @return version number.
     */
    public int version()
    {
        return version;
    }

    /**
     * Get the semantic version of the schema.
     *
     * @return the semantic version of the schema as applicable to the layer 7 application.
     */
    public String semanticVersion()
    {
        return semanticVersion;
    }

    /**
     * Get the namespaceName to be used for generated code.
     *
     * If {@link #namespaceName} is null then {@link #packageName} is used.
     *
     * @return the namespaceName to be used for generated code.
     */
    public String applicableNamespace()
    {
        return namespaceName == null ? packageName : namespaceName;
    }

    private void captureTypes(final List<Token> tokens)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            switch (tokens.get(i).signal())
            {
                case BEGIN_COMPOSITE:
                    i = captureType(tokens, i, Signal.END_COMPOSITE);
                    break;

                case BEGIN_ENUM:
                    i = captureType(tokens, i, Signal.END_ENUM);
                    break;

                case BEGIN_SET:
                    i = captureType(tokens, i, Signal.END_SET);
                    break;
            }
        }
    }

    private int captureType(final List<Token> tokens, int index, final Signal endSignal)
    {
        final List<Token> typeTokens = new ArrayList<>();

        Token token = tokens.get(index);
        typeTokens.add(token);
        do
        {
            token = tokens.get(++index);
            typeTokens.add(token);
        }
        while (endSignal != token.signal());

        typesByNameMap.put(tokens.get(index).name(), Collections.unmodifiableList(typeTokens));

        return index;
    }
}
