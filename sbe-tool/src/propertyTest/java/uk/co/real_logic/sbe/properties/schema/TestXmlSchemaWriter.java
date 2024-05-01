/*
 * Copyright 2013-2024 Real Logic Limited.
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

import uk.co.real_logic.sbe.ir.Encoding;
import org.agrona.collections.MutableInteger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static java.util.Objects.requireNonNull;

public final class TestXmlSchemaWriter
{
    private TestXmlSchemaWriter()
    {
    }

    public static String writeString(final MessageSchema schema)
    {
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        writeTo(schema, result);
        return writer.toString();
    }

    public static void writeFile(
        final MessageSchema schema,
        final File destination)
    {
        final StreamResult result = new StreamResult(destination);
        writeTo(schema, result);
    }

    private static void writeTo(
        final MessageSchema schema,
        final StreamResult destination)
    {
        try
        {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            final Element root = document.createElementNS("http://fixprotocol.io/2016/sbe", "sbe:messageSchema");
            root.setAttribute("id", Short.toString(schema.schemaId()));
            root.setAttribute("package", "uk.co.real_logic.sbe.properties");
            root.setAttribute("version", Short.toString(schema.version()));
            document.appendChild(root);

            final Element topLevelTypes = createTypesElement(document);
            root.appendChild(topLevelTypes);

            final HashMap<Object, String> typeToName = new HashMap<>();

            final TypeSchemaConverter typeSchemaConverter = new TypeSchemaConverter(
                document,
                topLevelTypes,
                typeToName
            );

            final Set<TypeSchema> visitedTypes = new HashSet<>();
            appendTypes(
                visitedTypes,
                topLevelTypes,
                typeSchemaConverter,
                schema.blockFields().stream().map(FieldSchema::type).collect(Collectors.toList()),
                schema.groups(),
                schema.varData());

            final Element message = document.createElement("sbe:message");
            message.setAttribute("name", "TestMessage");
            message.setAttribute("id", Short.toString(schema.templateId()));
            root.appendChild(message);
            final MutableInteger nextMemberId = new MutableInteger(0);
            appendMembers(
                document,
                typeToName,
                schema.blockFields(),
                schema.groups(),
                schema.varData(),
                nextMemberId,
                message);

            try
            {
                final Transformer transformer = TransformerFactory.newInstance().newTransformer();

                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                final DOMSource source = new DOMSource(document);

                transformer.transform(source, destination);
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void appendMembers(
        final Document document,
        final HashMap<Object, String> typeToName,
        final List<FieldSchema> blockFields,
        final List<GroupSchema> groups,
        final List<VarDataSchema> varData,
        final MutableInteger nextMemberId,
        final Element parent)
    {
        for (final FieldSchema field : blockFields)
        {
            final int id = nextMemberId.getAndIncrement();

            final boolean usePrimitiveName = field.type().isEmbedded() && field.type() instanceof EncodedDataTypeSchema;
            final String typeName = usePrimitiveName ?
                ((EncodedDataTypeSchema)field.type()).primitiveType().primitiveName() :
                requireNonNull(typeToName.get(field.type()));

            final Element element = document.createElement("field");
            element.setAttribute("id", Integer.toString(id));
            element.setAttribute("name", "member" + id);
            element.setAttribute("type", typeName);
            element.setAttribute("presence", field.presence().name().toLowerCase());
            element.setAttribute("sinceVersion", Short.toString(field.sinceVersion()));
            parent.appendChild(element);
        }

        for (final GroupSchema group : groups)
        {
            final int id = nextMemberId.getAndIncrement();

            final Element element = document.createElement("group");
            element.setAttribute("id", Integer.toString(id));
            element.setAttribute("name", "member" + id);
            appendMembers(
                document,
                typeToName,
                group.blockFields(),
                group.groups(),
                group.varData(),
                nextMemberId,
                element);
            parent.appendChild(element);
        }

        for (final VarDataSchema data : varData)
        {
            final int id = nextMemberId.getAndIncrement();
            final Element element = document.createElement("data");
            element.setAttribute("id", Integer.toString(id));
            element.setAttribute("name", "member" + id);
            element.setAttribute("type", requireNonNull(typeToName.get(data)));
            element.setAttribute("sinceVersion", Short.toString(data.sinceVersion()));
            parent.appendChild(element);
        }
    }

    private static Element createTypesElement(final Document document)
    {
        final Element types = document.createElement("types");

        types.appendChild(createCompositeElement(
            document,
            "messageHeader",
            createTypeElement(document, "blockLength", "uint16"),
            createTypeElement(document, "templateId", "uint16"),
            createTypeElement(document, "schemaId", "uint16"),
            createTypeElement(document, "version", "uint16")
        ));

        types.appendChild(createCompositeElement(
            document,
            "groupSizeEncoding",
            createTypeElement(document, "blockLength", "uint16"),
            createTypeElement(document, "numInGroup", "uint16")
        ));

        return types;
    }

    private static Element createSetElement(
        final Document document,
        final String name,
        final String encodingType,
        final Set<Integer> choices)
    {
        final Element enumElement = document.createElement("set");
        enumElement.setAttribute("name", name);
        enumElement.setAttribute("encodingType", encodingType);

        for (final Integer value : choices)
        {
            final Element choice = document.createElement("choice");
            choice.setAttribute("name", "option" + value);
            choice.setTextContent(value.toString());
            enumElement.appendChild(choice);
        }

        return enumElement;
    }

    private static Element createEnumElement(
        final Document document,
        final String name,
        final String encodingType,
        final List<String> validValues)
    {
        final Element enumElement = document.createElement("enum");
        enumElement.setAttribute("name", name);
        enumElement.setAttribute("encodingType", encodingType);

        int caseId = 0;
        for (final String value : validValues)
        {
            final Element validValue = document.createElement("validValue");
            validValue.setAttribute("name", "Case" + caseId++);
            validValue.setTextContent(value);
            enumElement.appendChild(validValue);
        }

        return enumElement;
    }

    private static Element createCompositeElement(
        final Document document,
        final String name,
        final Element... types
    )
    {
        final Element composite = document.createElement("composite");
        composite.setAttribute("name", name);

        for (final Element type : types)
        {
            composite.appendChild(type);
        }

        return composite;
    }

    private static Element createTypeElement(
        final Document document,
        final String name,
        final String primitiveType)
    {
        final Element blockLength = document.createElement("type");
        blockLength.setAttribute("name", name);
        blockLength.setAttribute("primitiveType", primitiveType);
        return blockLength;
    }

    private static Element createTypeElement(
        final Document document,
        final String name,
        final String primitiveType,
        final int length,
        final Encoding.Presence presence)
    {
        final Element typeElement = document.createElement("type");
        typeElement.setAttribute("name", name);
        typeElement.setAttribute("primitiveType", primitiveType);

        if (length > 1)
        {
            typeElement.setAttribute("length", Integer.toString(length));
        }

        switch (presence)
        {

            case REQUIRED:
                typeElement.setAttribute("presence", "required");
                break;
            case OPTIONAL:
                typeElement.setAttribute("presence", "optional");
                break;
            case CONSTANT:
                typeElement.setAttribute("presence", "constant");
                break;
            default:
                throw new IllegalArgumentException("Unknown presence: " + presence);
        }

        return typeElement;
    }

    private static Element createRefElement(
        final Document document,
        final String name,
        final String type)
    {
        final Element blockLength = document.createElement("ref");
        blockLength.setAttribute("name", name);
        blockLength.setAttribute("type", type);
        return blockLength;
    }

    private static void appendTypes(
        final Set<TypeSchema> visitedTypes,
        final Element topLevelTypes,
        final TypeSchemaConverter typeSchemaConverter,
        final List<TypeSchema> blockFields,
        final List<GroupSchema> groups,
        final List<VarDataSchema> varDataFields)
    {
        for (final TypeSchema field : blockFields)
        {
            if (!field.isEmbedded() && visitedTypes.add(field))
            {
                topLevelTypes.appendChild(typeSchemaConverter.convert(field));
            }
        }

        for (final GroupSchema group : groups)
        {
            appendTypes(
                visitedTypes,
                topLevelTypes,
                typeSchemaConverter,
                group.blockFields().stream().map(FieldSchema::type).collect(Collectors.toList()),
                group.groups(),
                group.varData());
        }

        for (final VarDataSchema varData : varDataFields)
        {
            topLevelTypes.appendChild(typeSchemaConverter.convert(varData));
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static final class TypeSchemaConverter implements TypeSchemaVisitor
    {
        private final Document document;
        private final Element topLevelTypes;
        private final Map<Object, String> typeToName;
        private final Function<Object, String> nextName;
        private Element result;

        private TypeSchemaConverter(
            final Document document,
            final Element topLevelTypes,
            final Map<Object, String> typeToName)
        {
            this.document = document;
            this.topLevelTypes = topLevelTypes;
            this.typeToName = typeToName;
            nextName = ignored -> "Type" + typeToName.size();
        }

        @Override
        public void onEncoded(final EncodedDataTypeSchema type)
        {
            result = createTypeElement(
                document,
                typeToName.computeIfAbsent(type, nextName),
                type.primitiveType().primitiveName(),
                type.length(),
                type.presence()
            );
        }

        @Override
        public void onComposite(final CompositeTypeSchema type)
        {
            final Element[] members = type.fields().stream()
                .map(this::embedOrReference)
                .toArray(Element[]::new);
            for (int i = 0; i < members.length; i++)
            {
                final Element member = members[i];
                member.setAttribute("name", "member" + i + "Of" + member.getAttribute("name"));
            }
            result = createCompositeElement(
                document,
                typeToName.computeIfAbsent(type, nextName),
                members
            );
        }

        @Override
        public void onEnum(final EnumTypeSchema type)
        {
            result = createEnumElement(
                document,
                typeToName.computeIfAbsent(type, nextName),
                type.encodingType(),
                type.validValues()
            );
        }

        @Override
        public void onSet(final SetSchema type)
        {
            result = createSetElement(
                document,
                typeToName.computeIfAbsent(type, nextName),
                type.encodingType(),
                type.choices()
            );
        }

        private Element embedOrReference(final TypeSchema type)
        {
            if (type.isEmbedded())
            {
                return convert(type);
            }
            else
            {
                final boolean hasWritten = typeToName.containsKey(type);
                if (!hasWritten)
                {
                    topLevelTypes.appendChild(convert(type));
                }

                final String typeName = requireNonNull(typeToName.get(type));
                return createRefElement(
                    document,
                    typeName,
                    typeName
                );
            }
        }

        public Element convert(final TypeSchema type)
        {
            result = null;
            type.accept(this);
            return requireNonNull(result);
        }

        public Node convert(final VarDataSchema varData)
        {
            final Element lengthElement = createTypeElement(document, "length",
                varData.lengthEncoding().primitiveName());

            if (varData.lengthEncoding().size() >= 4)
            {
                lengthElement.setAttribute("maxValue", Integer.toString(1_000_000));
            }

            final Element varDataElement = createTypeElement(document, "varData", "uint8");
            varDataElement.setAttribute("length", "0");

            if (varData.dataEncoding().equals(VarDataSchema.Encoding.ASCII))
            {
                varDataElement.setAttribute("characterEncoding", "US-ASCII");
            }

            return createCompositeElement(
                document,
                typeToName.computeIfAbsent(varData, nextName),
                lengthElement,
                varDataElement
            );
        }
    }
}
