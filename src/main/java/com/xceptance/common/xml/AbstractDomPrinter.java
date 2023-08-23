/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.xceptance.xlt.common.XltConstants;

/**
 * A utility class to print a (sub) tree of DOM elements. The printer can be configured to print the nodes either
 * exactly the same way as they appear in the DOM tree, or pretty-printed with whitespace stripped and nodes properly
 * indented.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractDomPrinter
{
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractDomPrinter.class);

    /**
     * The indentation string.
     */
    protected final String defaultIndentation;

    /**
     * Whether or not the printer is pretty-printing.
     */
    protected final boolean prettyPrinting;

    /**
     * Creates a new printer object with pretty-printing disabled.
     */
    public AbstractDomPrinter()
    {
        this(-1);
    }

    /**
     * Creates a new printer object. Whether pretty-printing is enabled depends on the value of the "spaces" parameter.
     * If the value is negative, then pretty-printing is disabled. Otherwise the elements are indented with the number
     * of spaces given.
     * 
     * @param spaces
     *            the number of spaces of one indentation level
     */
    public AbstractDomPrinter(final int spaces)
    {
        defaultIndentation = StringUtils.repeat(" ", spaces);
        prettyPrinting = spaces >= 0;
    }

    /**
     * Returns a tree for the given DOM node and its children.
     * 
     * @param node
     *            the node to print
     * @return the node tree as a string
     */
    public final String printNode(final Node node)
    {
        final StringWriter out = new StringWriter();

        printNode(node, out);

        return out.toString();
    }

    /**
     * Writes a tree for the given DOM node and its children to the specified output stream.
     * 
     * @param node
     *            the node to print
     * @param out
     *            the target output stream
     */
    public final void printNode(final Node node, final OutputStream out)
    {
        try
        {
            printNode(node, new OutputStreamWriter(out, XltConstants.UTF8_ENCODING));
        }
        catch (final UnsupportedEncodingException uee)
        {
            log.warn("Failed to print node using '" + XltConstants.UTF8_ENCODING + "' encoding!");
        }
    }

    /**
     * Writes a tree for the given DOM node and its children to the specified writer.
     * 
     * @param node
     *            the node to print
     * @param out
     *            the target writer
     */
    public void printNode(final Node node, final Writer out)
    {
        final PrintWriter printWriter = new PrintWriter(out);

        printNode(node, "", printWriter);

        printWriter.flush();
    }

    /**
     * Prints the attributes of the given element to the specified writer.
     * 
     * @param element
     *            the element with the attributes
     * @param printWriter
     *            the target writer
     */
    protected void printAttributes(final Element element, final PrintWriter printWriter)
    {
        final NamedNodeMap attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            final Attr attribute = (Attr) attributes.item(i);

            printWriter.print(" " + StringEscapeUtils.escapeXml10(attribute.getName()) + "=\"" +
                              StringEscapeUtils.escapeXml10(attribute.getValue()) + "\"");
        }
    }

    /**
     * Prints the given CDATA section node to the specified writer.
     * 
     * @param cdataSection
     *            the CDATA section node to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printCDataSection(final CDATASection cdataSection, final String indentation, final PrintWriter printWriter)
    {
        printWriter.print(indentation + "<![CDATA[" + cdataSection.getData() + "]]>");

        if (prettyPrinting)
        {
            printWriter.println();
        }
    }

    /**
     * Prints the given comment node to the specified writer.
     * 
     * @param comment
     *            the comment node to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printComment(final Comment comment, final String indentation, final PrintWriter printWriter)
    {
        printWriter.print(indentation + "<!--" + comment.getData() + "-->");

        if (prettyPrinting)
        {
            printWriter.println();
        }
    }

    /**
     * Prints the given document node to the specified writer.
     * 
     * @param document
     *            the document node to print
     * @param printWriter
     *            the target writer
     */
    protected void printDocument(final Document document, final PrintWriter printWriter)
    {
        for (Node child = document.getFirstChild(); child != null; child = child.getNextSibling())
        {
            printNode(child, "", printWriter);
        }
    }

    /**
     * Prints the given document fragment node to the specified writer.
     * 
     * @param documentFragment
     *            the document node fragment to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printDocumentFragment(final DocumentFragment documentFragment, final String indentation, final PrintWriter printWriter)
    {
        for (Node child = documentFragment.getFirstChild(); child != null; child = child.getNextSibling())
        {
            printNode(child, indentation, printWriter);
        }
    }

    /**
     * Prints the given document type node to the specified writer.
     * 
     * @param documentType
     *            the document type node to print
     * @param printWriter
     *            the target writer
     */
    protected void printDocumentType(final DocumentType documentType, final PrintWriter printWriter)
    {
        printWriter.printf("<!DOCTYPE %s PUBLIC \"%s\" \"%s\">\n", documentType.getName(), documentType.getPublicId(),
                           documentType.getSystemId());
    }

    /**
     * Prints the given element node to the specified writer.
     * 
     * @param element
     *            the element node to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printElement(final Element element, final String indentation, final PrintWriter printWriter)
    {
        final String tagName = element.getTagName();

        printWriter.print(indentation + "<" + tagName);
        printAttributes(element, printWriter);

        if (element.hasChildNodes())
        {
            printWriter.print(">");

            if (prettyPrinting)
            {
                printWriter.println();
            }

            for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
            {
                printNode(child, indentation + defaultIndentation, printWriter);
            }

            printWriter.print(indentation + "</" + tagName + ">");
        }
        else
        {
            printEmptyElementClosing(element, printWriter);
        }

        if (prettyPrinting)
        {
            printWriter.println();
        }
    }

    /**
     * Prints the closing tag/angle bracket for an empty element node. This method must be implemented by sub classes to
     * meet the needs of the respective output format (for example XML vs. HTML).
     * 
     * @param element
     *            the element node
     * @param printWriter
     *            the target writer
     */
    protected abstract void printEmptyElementClosing(Element element, PrintWriter printWriter);

    /**
     * Prints the given node and recursively all child nodes to the specified writer indented by the passed indentation
     * string. Actually, this is the method to call when a node is to be printed. All other printing method are called
     * from this one.
     * 
     * @param node
     *            the node to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printNode(final Node node, final String indentation, final PrintWriter printWriter)
    {
        final int nodeType = node.getNodeType();

        switch (nodeType)
        {
            case Node.CDATA_SECTION_NODE:
                printCDataSection((CDATASection) node, indentation, printWriter);
                break;

            case Node.COMMENT_NODE:
                printComment((Comment) node, indentation, printWriter);
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
                printDocumentFragment((DocumentFragment) node, indentation, printWriter);
                break;

            case Node.DOCUMENT_NODE:
                printDocument((Document) node, printWriter);
                break;

            case Node.DOCUMENT_TYPE_NODE:
                printDocumentType((DocumentType) node, printWriter);
                break;

            case Node.ELEMENT_NODE:
                printElement((Element) node, indentation, printWriter);
                break;

            case Node.TEXT_NODE:
                printText((Text) node, indentation, printWriter);
                break;

            default:
                log.warn("Don't know how to print this node: " + node.getClass());
                break;
        }
    }

    /**
     * Prints the given text node to the specified writer.
     * 
     * @param text
     *            the text node to print
     * @param indentation
     *            the whitespace to indent the node
     * @param printWriter
     *            the target writer
     */
    protected void printText(final Text text, final String indentation, final PrintWriter printWriter)
    {
        String s = StringEscapeUtils.escapeXml10(text.getData());
        if (prettyPrinting)
        {
            // print non-blank text only
            s = s.trim();
            if (s.length() > 0)
            {
                printWriter.println(indentation + s);
            }
        }
        else
        {
            printWriter.print(s);
        }
    }
}
