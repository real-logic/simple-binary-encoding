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
package uk.co.real_logic.sbe;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulate the XML Schema parsing for SBE so that other representations may be used to generate IR
 * 
 */
public class XMLSchemaParser
{
    /** XPath expression for accessing the type nodes under types */
    public static final String typeXPathExpr = "/messageSchema/types/type";
    /** XPath expression for accessing the composite nodes under types */
    public static final String compositeXPathExpr = "/messageSchema/types/composite";
    /** XPath expression for accessing the enum nodes under types */
    public static final String enumXPathExpr = "/messageSchema/types/enum";
    /** XPath expression for accessing the set nodes under types */
    public static final String setXPathExpr = "/messageSchema/types/set";

    /** XPath expression for accessing the message nodes under messageSchema */
    public static final String messageXPathExpr = "/messageSchema/message";

    /**
     * Take an input stream and parse it generating Intermediate Representation. 
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param stream to read schema from
     * @eturn list of Intermediate Representation nodes
     * @throws exceptions from XML parsing and processing
     */
    public static List<IRNode> parse(final InputStream stream) throws Exception
    {
	/** set up XML parsing */
	/**
	 * We could do the builder by pieces, but ... why?
	 * DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	 * DocumentBuilder builder = builderFactory.newDocumentBuilder();
	 * Document document = builder.parse(stream);
	 */
	Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
	XPath xPath =  XPathFactory.newInstance().newXPath();

	/** init types table/map for lookup by <field> elements */
	Map<String,Type> typesMap = new HashMap<String,Type>();

	// TODO: add primitiveTypes to typesMap?

	/** grab all "type" types (encodedDataType) and add to types table */
	addEncodedDataTypes(typesMap, (NodeList)xPath.compile(typeXPathExpr).evaluate(document, XPathConstants.NODESET));
	/** grab all "composite" types (compositeType) and add to types table */
	addCompositeTypes(typesMap, (NodeList)xPath.compile(compositeXPathExpr).evaluate(document, XPathConstants.NODESET));
	/** grab all "enum" types (enumType) and add to types table */
	addEnumTypes(typesMap, (NodeList)xPath.compile(enumXPathExpr).evaluate(document, XPathConstants.NODESET));
	/** grab all "set" types (setType) and add to types table */
	addSetTypes(typesMap, (NodeList)xPath.compile(setXPathExpr).evaluate(document, XPathConstants.NODESET));
	
	/** TODO: once all <types> handled, we can move to the actual encoding layout */

	/** TODO: grab all <message> elements and handle them - this is where IR is generated */
	return null;
    }

    /**
     * Add encodedDataType (if any) to Types Map
     *
     * @param map of types
     * @param list of Nodes
     */
    private static void addEncodedDataTypes(Map<String,Type> map, NodeList list)
    {
	for (int i = 0; i < list.getLength(); i++) {
	    // System.out.println(list.item(i).getFirstChild().getNodeValue()); 
	}
    }

    /**
     * Add compositeType (if any) to Types Map
     *
     * @param map of types
     * @param list of Nodes
     */
    private static void addCompositeTypes(Map<String,Type> map, NodeList list)
    {
	for (int i = 0; i < list.getLength(); i++) {
	    // System.out.println(list.item(i).getFirstChild().getNodeValue()); 
	}
    }
    
    /**
     * Add enumType (if any) to Types Map
     *
     * @param map of types
     * @param list of Nodes
     */
    private static void addEnumTypes(Map<String,Type> map, NodeList list)
    {
	for (int i = 0; i < list.getLength(); i++) {
	    // System.out.println(list.item(i).getFirstChild().getNodeValue()); 
	}
    }

    /**
     * Add setType (if any) to Types Map
     *
     * @param map of types
     * @param list of Nodes
     */
    private static void addSetTypes(Map<String,Type> map, NodeList list)
    {
	for (int i = 0; i < list.getLength(); i++) {
	    // System.out.println(list.item(i).getFirstChild().getNodeValue()); 
	}
    }

}
