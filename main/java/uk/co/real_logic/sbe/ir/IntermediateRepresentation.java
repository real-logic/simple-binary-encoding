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
    private final List<Token> headerTokens;
    private final Map<Long, List<Token>> messagesByIdMap = new HashMap<>();

    /**
     * Create a new IR container taking a defensive copy of the header {@link Token}s passed.
     * @param packageName that should be applied to generated code.
     * @param headerTokens representing the message header.
     */
    public IntermediateRepresentation(final String packageName, final List<Token> headerTokens)
    {
        Verify.notNull(packageName, "packageName");
        Verify.notNull(headerTokens, "headerTokens");

        this.packageName = packageName;
        this.headerTokens = Collections.unmodifiableList(new ArrayList<>(headerTokens));
    }

    /**
     * Return the {@link List} of {@link Token}s representing the message header.
     *
     * @return the {@link List} of {@link Token}s representing the message header.
     */
    public List<Token> getHeader()
    {
        return headerTokens;
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

        messagesByIdMap.put(Long.valueOf(messageId), Collections.unmodifiableList(new ArrayList<>(messageTokens)));
    }

    /**
     * Get the message for a given identifier.
     *
     * @param messageId to lookup.
     * @return the List of {@link Token}s representing the message or null if the id does not exist.
     */
    public List<Token> getMessage(final long messageId)
    {
        return messagesByIdMap.get(Long.valueOf(messageId));
    }

    /**
     * Get the package name to be used for generated code.
     *
     * @return the package name to be used for generated code.
     */
    public String getPackageName()
    {
        return packageName;
    }
}
