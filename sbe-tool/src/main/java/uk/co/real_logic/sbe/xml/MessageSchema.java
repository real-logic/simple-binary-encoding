/*
 * Copyright 2013-2020 Real Logic Limited.
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
package uk.co.real_logic.sbe.xml;

import org.w3c.dom.Node;
import org.agrona.Verify;

import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/**
 * Message schema aggregate for schema attributes, messageHeader, and reference for multiple {@link Message} objects.
 */
public class MessageSchema
{
    /**
     * Default message header type name for the SBE spec.
     */
    public static final String HEADER_TYPE_DEFAULT = "messageHeader";

    private final String packageName;                 // package (required)
    private final String description;                 // description (optional)
    private final int id;                             // identifier for the schema (required)
    private final int version;                        // version (optional - default is 0)
    private final String semanticVersion;             // semanticVersion (optional)
    private final ByteOrder byteOrder;                // byteOrder (optional - default is littleEndian)
    private final String headerType;                  // headerType (optional - default to messageHeader)
    private final Map<String, Type> typeByNameMap;
    private final Map<Long, Message> messageByIdMap;

    public MessageSchema(
        final Node schemaNode, final Map<String, Type> typeByNameMap, final Map<Long, Message> messageByIdMap)
    {
        this.packageName = getAttributeValue(schemaNode, "package");
        this.description = getAttributeValueOrNull(schemaNode, "description");
        this.id = Integer.parseInt(getAttributeValue(schemaNode, "id"));
        this.version = Integer.parseInt(getAttributeValue(schemaNode, "version", "0"));
        this.semanticVersion = getAttributeValueOrNull(schemaNode, "semanticVersion");
        this.byteOrder = getByteOrder(getAttributeValue(schemaNode, "byteOrder", "littleEndian"));
        this.typeByNameMap = typeByNameMap;
        this.messageByIdMap = messageByIdMap;

        final String headerType = getAttributeValueOrNull(schemaNode, "headerType");
        this.headerType = null == headerType ? HEADER_TYPE_DEFAULT : headerType;
        Verify.present(typeByNameMap, this.headerType, "Message header");

        ((CompositeType)typeByNameMap.get(this.headerType)).checkForWellFormedMessageHeader(schemaNode);
    }

    /**
     * The Schema headerType for message headers. This should be a {@link CompositeType}.
     *
     * @return the Schema headerType for message headers
     */
    public CompositeType messageHeader()
    {
        return (CompositeType)typeByNameMap.get(headerType);
    }

    /**
     * The package name for the schema.
     *
     * @return he package name for the schema.
     */
    public String packageName()
    {
        return packageName;
    }

    /**
     * The description of the schema.
     *
     * @return the description of the schema.
     */
    public String description()
    {
        return description;
    }

    /**
     * The id number of the schema.
     *
     * @return the id number of the schema.
     */
    public int id()
    {
        return id;
    }

    /**
     * The version number of the schema.
     *
     * @return the version number of the schema.
     */
    public int version()
    {
        return version;
    }

    /**
     * The semantic version number of the schema. Typically use to reference a third party standard such as FIX.
     *
     * @return the semantic version number of the schema.
     */
    public String semanticVersion()
    {
        return semanticVersion;
    }

    /**
     * Return a given {@link Message} object with the given messageId.
     *
     * @param messageId of the message to return.
     * @return a given {@link Message} for the messageId.
     */
    public Message getMessage(final long messageId)
    {
        return messageByIdMap.get(messageId);
    }

    /**
     * Get the {@link Type} for a given name.
     *
     * @param typeName to lookup.
     * @return the type if found otherwise null.
     */
    public Type getType(final String typeName)
    {
        return typeByNameMap.get(typeName);
    }

    /**
     * Get the {@link Collection} of {@link Message}s for this Schema.
     *
     * @return the {@link Collection} of {@link Message}s for this Schema.
     */
    public Collection<Message> messages()
    {
        return messageByIdMap.values();
    }

    /**
     * Return the byte order specified by the messageSchema
     *
     * @return {@link ByteOrder} of the message encoding.
     */
    public ByteOrder byteOrder()
    {
        return byteOrder;
    }
}
