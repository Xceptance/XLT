/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.common.xml;

import java.io.OutputStream;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.xceptance.xlt.api.util.XltLogger;

/**
 * Utility methods for the W3C DOM API classes.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class DomUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private DomUtils()
    {
    }

    /**
     * The default XML pretty printer.
     */
    private static final XmlDomPrinter xmlDomPrinter = new XmlDomPrinter(4);

    /**
     * Returns a pretty-printed tree for the given DOM node and its children.
     * 
     * @param node
     *            the node to print
     * @return the pretty-printed tree as a string
     */
    public static String prettyPrintNode(final Node node)
    {
        return xmlDomPrinter.printNode(node);
    }

    /**
     * Writes a pretty-printed tree for the given DOM node and its children to the specified output stream.
     * 
     * @param node
     *            the node to print
     * @param out
     *            the target output stream
     */
    public static void prettyPrintNode(final Node node, final OutputStream out)
    {
        xmlDomPrinter.printNode(node, out);
    }

    /**
     * Writes a pretty-printed tree for the given DOM node and its children to the specified writer.
     * 
     * @param node
     *            the node to print
     * @param out
     *            the target writer
     */
    public static void prettyPrintNode(final Node node, final Writer out)
    {
        xmlDomPrinter.printNode(node, out);
    }

    /**
     * Removes the named elements from the given document.
     * 
     * @param document
     *            the document to modify
     * @param tagName
     *            the name of the elements to be removed
     */
    public static void removeElementsByTagName(final Document document, final String tagName)
    {
        final NodeList nodes = document.getElementsByTagName(tagName);

        while (nodes.getLength() > 0)
        {
            final Node node = nodes.item(0);
            node.getParentNode().removeChild(node);
        }
    }

    /**
     * Returns a deep clone of the given {@link Node}. All child nodes will be cloned recursively.
     * 
     * @param node
     *            the node to clone
     * @param document
     *            the document the clone will belong to
     * @return the cloned {@link Node}
     */
    public static Node cloneNode(final Node node, final Document document)
    {
        if (node instanceof Element)
        {
            return cloneElementNode((Element) node, document);
        }

        if (node instanceof CDATASection)
        {
            return cloneCDATA((CDATASection) node, document);
        }

        if (node instanceof Text)
        {
            return cloneText((Text) node, document);
        }

        if (node instanceof Comment)
        {
            return cloneComment((Comment) node, document);
        }

        // fallthrough -> unknown type of node -> log warn message and return
        // null
        XltLogger.runTimeLogger.warn("Don't know how to clone this node: " + node.getClass());
        return null;
    }

    /**
     * Returns a clone of the given element node.
     * 
     * @param node
     *            the node to clone
     * @param document
     *            the document the given node resides in
     * @return a clone of given element node
     */
    private static Node cloneElementNode(final Element node, final Document document)
    {
        // create the clone
        final Element clone = document.createElement((node).getTagName());

        // clone the attributes
        final NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            final Attr attribute = (Attr) attributes.item(i);
            clone.setAttribute(attribute.getName(), attribute.getValue());
        }

        // clone the children
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            clone.appendChild(cloneNode(child, document));
        }

        return clone;
    }

    /**
     * Returns a clone of the given CDATA node.
     * 
     * @param node
     *            the node to clone
     * @param document
     *            the document the node resides in
     * @return a clone of given CDATA node
     */
    private static Node cloneCDATA(final CDATASection node, final Document document)
    {
        return document.createCDATASection(node.getData());

    }

    /**
     * Returns a clone of the given text node.
     * 
     * @param node
     *            the node to clone
     * @param document
     *            the document the node resides in
     * @return a clone of given text node
     */
    private static Node cloneText(final Text node, final Document document)
    {
        return document.createTextNode(node.getData());
    }

    /**
     * Returns a clone of the given comment node.
     * 
     * @param node
     *            the node to clone
     * @param document
     *            the document the node resides in
     * @return a clone of given comment node
     */
    private static Node cloneComment(final Comment node, final Document document)
    {
        return document.createComment(node.getData());
    }
}
