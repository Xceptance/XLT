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

import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * An {@link AbstractDomPrinter} implementation that generates HTML or XHTML output from a DOM. What format will be
 * generated depends on the document type of the DOM. If no document type is associated with the DOM, the output will be
 * HTML by default. The two output formats differ only in the way empty tags are closed. In HTML mode, empty tags are
 * closed with "&gt;", while in XHTML mode they are closed with " /&gt;". Tags which must have a closing tag according
 * to the HTML specification will get a closing tag in both modes, even if they are empty, for example
 * "&lt;div&gt;&lt;/div&gt;"
 * 
 * @see XmlDomPrinter
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class HtmlDomPrinter extends AbstractDomPrinter
{
    /**
     * The HTML tags which are empty. Not sure whether this list is complete/correct. All other tags will always get a
     * closing tag.
     */
    private static final String[] EMPTY_HTML_TAGS =
        {
            "base", "basefont", "br", "col", "frame", "hr", "img", "input", "isindex", "link", "meta", "param"
        };

    private static final String[] LITERAL_TEXT_ELEMENTS =
        {
            "style", "script", "xmp", "iframe", "noembed", "noframes", "noscript", "plaintext"
        };

    /**
     * Whether or not the output will be XHTML. Determined from the document's document type.
     */
    private boolean isXhtml;

    /**
     * Creates a new printer object with pretty-printing disabled.
     */
    public HtmlDomPrinter()
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
    public HtmlDomPrinter(final int spaces)
    {
        super(spaces);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printNode(final Node node, final Writer out)
    {
        // determine the mode before we start
        isXhtml = isXhtml(node);

        super.printNode(node, out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printEmptyElementClosing(final Element element, final PrintWriter printWriter)
    {
        final String tagName = element.getTagName();

        if (ArrayUtils.contains(EMPTY_HTML_TAGS, tagName))
        {
            if (isXhtml)
            {
                printWriter.print(" />");
            }
            else
            {
                printWriter.print(">");
            }
        }
        else
        {
            printWriter.print("></" + tagName + ">");
        }
    }

    /**
     * Determines whether or not the output format should be XHTML.
     * 
     * @param node
     *            the root node to print
     * @return <code>true</code> if the output format should be XHTML, <code>false</code> otherwise
     */
    protected boolean isXhtml(final Node node)
    {
        // get the document
        Document document;

        if (node.getNodeType() == Node.DOCUMENT_NODE)
        {
            // the node is the document
            document = (Document) node;
        }
        else
        {
            document = node.getOwnerDocument();
        }

        // determine mode
        if (document != null)
        {
            // get the mode from the document type
            final DocumentType documentType = document.getDoctype();
            final String publicId = (documentType != null ? documentType.getPublicId() : null);
            return publicId != null && publicId.toUpperCase().contains("XHTML");
        }
        else
        {
            // default mode
            return true;
        }
    }

    private boolean needEscapeText(final Node text)
    {
        Node node = text;
        while ((node = node.getParentNode()) != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE && ArrayUtils.contains(LITERAL_TEXT_ELEMENTS, ((Element) node).getTagName()))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printText(Text text, String indentation, PrintWriter printWriter)
    {
        String s = text.getData();

        if (needEscapeText(text))
        {
            s = StringEscapeUtils.escapeXml10(s);
        }

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printCDataSection(CDATASection cdataSection, String indentation, PrintWriter printWriter)
    {
        String s = cdataSection.getData();

        if (needEscapeText(cdataSection))
        {
            s = StringEscapeUtils.escapeXml10(s);
        }

        printWriter.print(indentation + "<![CDATA[" + s + "]]>");

        if (prettyPrinting)
        {
            printWriter.println();
        }

    }
}
