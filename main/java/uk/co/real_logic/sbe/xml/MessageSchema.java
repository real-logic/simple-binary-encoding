/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package uk.co.real_logic.sbe.xml;

import org.w3c.dom.Node;

import java.util.Map;
import java.nio.ByteOrder;

/**
 * Class to encapsulate the messageSchema attributes as well as messageHeader type reference for multiple Message objects
 */
public class MessageSchema
{
    private final String pkg;                         // package (optional?)
    private final String description;                 // description (optional)
    private final Long version;                       // version (optional - default is 0)
    private final String fixVersion;                  // fixVersion (optional)
    private final ByteOrder byteOrder;                // byteOrder (optional - default is littleEndian)
    private final Map<String, Type> typeMap;
    private final Map<Long, Message> messageMap;

    public MessageSchema(final Node node, Map<String, Type> typeMap, Map<Long, Message> messageMap)
    {
	this.pkg = XmlSchemaParser.getXmlAttributeValue(node, "package");
	this.description = XmlSchemaParser.getXmlAttributeValueNullable(node, "description");
	this.version = Long.parseLong(XmlSchemaParser.getXmlAttributeValue(node, "version", "0"));  // default version is 0
	this.fixVersion = XmlSchemaParser.getXmlAttributeValueNullable(node, "fixVersion");
	this.byteOrder = XmlSchemaParser.lookupByteOrder(XmlSchemaParser.getXmlAttributeValue(node, "byteOrder", "littleEndian"));
	this.typeMap = typeMap;
	this.messageMap = messageMap;
    }

    public Type getMessageHeader()
    {
	/* search types for defined messageHeader */
	return typeMap.get("messageHeader");
    }

    public Message getMessage(long id)
    {
	Long longId = new Long(id);
	return messageMap.get(longId);
    }

    public ByteOrder getByteOrder()
    {
	return byteOrder;
    }
}
