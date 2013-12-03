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
public class IntermediateRepresentation
{
    private final String packageName;
    private final MessageHeader messageHeader;
    private final Map<Long, List<Token>> messagesByIdMap = new HashMap<>();
    private final Map<String, List<Token>> typesByNameMap = new HashMap<>();
    private final int version;

    /**
     * Create a new IR container taking a defensive copy of the header {@link Token}s passed.
     *
     * @param packageName that should be applied to generated code.
     * @param headerTokens representing the message header.
     */
    public IntermediateRepresentation(final String packageName, final List<Token> headerTokens, final int version)
    {
        Verify.notNull(packageName, "packageName");
        Verify.notNull(headerTokens, "headerTokens");

        this.packageName = packageName;
        this.messageHeader = new MessageHeader(Collections.unmodifiableList(new ArrayList<>(headerTokens)));
        this.version = version;
    }

    /**
     * Return the {@link MessageHeader} description for all messages.
     *
     * @return the {@link MessageHeader} description for all messages.
     */
    public MessageHeader messageHeader()
    {
        return messageHeader;
    }

    /**
     * Add a List of {@link Token}s for a given message id.
     *
     * @param messageId to identify the list of tokens for the message.
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
     * Get the namespace name to be used for generated code.
     *
     * @return the namespace name to be used for generated code.
     */
    public String namespaceName()
    {
        return packageName;
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
