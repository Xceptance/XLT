/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.resultbrowser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.Page;
import org.htmlunit.html.BaseFrameElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Utility methods for the W3C DOM API classes.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
final class DomUtils
{

    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DomUtils.class);

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private DomUtils()
    {
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
    private static Node cloneNode(final Node node, final Document document)
    {
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
        if (node != null)
        {
            LOG.warn("Don't know how to clone this node: " + node.getClass());
        }
        return null;
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
        try
        {
            return document.createCDATASection(node.getData());
        }
        catch (final DOMException dex)
        {
            LOG.warn("Failed to create CDATA section", dex);
        }

        return null;
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
        try
        {
            return document.createTextNode(node.getData());
        }
        catch (final DOMException dex)
        {
            LOG.warn("Failed to create text node", dex);
        }

        return null;
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
        try
        {
            return document.createComment(node.getData());
        }
        catch (final DOMException dex)
        {
            LOG.warn("Failed to create comment node", dex);
        }

        return null;
    }

    /**
     * Creates a DOM clone of the given HTML page.
     *
     * @param page
     *            HTML page to be cloned
     * @return DOM clone of given HTML page
     */
    public static PageDOMClone clonePage(final HtmlPage page)
    {
        ParameterCheckUtils.isNotNull(page, "page");

        try
        {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // create a copy of the page's document type
            DocumentType docType = null;

            final DocumentType originalDocType = page.getDoctype();
            if (originalDocType != null)
            {
                final String name = originalDocType.getName();
                final String publicId = originalDocType.getPublicId();
                final String systemId = originalDocType.getSystemId();

                // only create a doc type if the name is not blank (#1558)
                if (StringUtils.isNotBlank(name))
                {
                    docType = builder.getDOMImplementation().createDocumentType(name, publicId, systemId);
                }
            }

            // create a new document with this document type -> the document will
            // contain the document type and a single root node <html>
            final Document doc = builder.getDOMImplementation().createDocument(null, "html", docType);
            // remove <html> node since the page to be cloned contains one
            doc.removeChild(doc.getElementsByTagName("html").item(0));

            final PageDOMClone clone = new PageDOMClone(page, doc);

            clonePage(page, clone);

            return clone;
        }
        catch (final Exception e)
        {
            LOG.error("Failed to clone page " + page, e);
        }

        return null;
    }

    /**
     * Helper method which creates a deep copy of the document element of the given HTML page and appends it to the
     * document node of the given DOM clone.
     *
     * @param page
     *            HTML page
     * @param clone
     *            DOM clone
     */
    private static void clonePage(final HtmlPage page, final PageDOMClone clone)
    {
        final Node pageClone = cloneNode(page.getDocumentElement(), clone);
        if (pageClone != null)
        {
            clone.getDocument().appendChild(pageClone);
        }
    }

    /**
     * Helper method which creates a deep copy of the given node contained in the given HTML page. The owning document
     * of the created node will be the DOM document of the given DOM clone object.
     *
     * @param node
     *            DOM node
     * @param clone
     *            DOM clone
     * @return deep copy of given DOM node
     */
    private static Node cloneNode(final Node node, final PageDOMClone clone)
    {
        if (node instanceof Element)
        {
            return cloneElementNode((Element) node, clone);
        }

        return cloneNode(node, clone.getDocument());
    }

    /**
     * Helper method which creates a deep copy of the given element node contained in the given HTML page. The owning
     * document of the created node will be the DOM document of the given DOM clone object. If the given element node
     * represents a frame element, the embedded HTML page of the corresponding frame window will be additionally cloned
     * and added to the DOM clone.
     *
     * @param node
     *            DOM element node
     * @param pageClone
     *            DOM clone
     * @return deep copy of given element node
     */
    private static Node cloneElementNode(final Element node, final PageDOMClone pageClone)
    {
        if (node == null)
        {
            return null;
        }

        // create the clone
        final Element clone;
        final String nodeNS = node.getNamespaceURI();
        try
        {
            // GH#88: Make sure to create the clone with same namespaceURI as the original.
            clone = pageClone.getDocument().createElementNS(nodeNS, node.getTagName());
        }
        catch (final DOMException dex)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn("Failed to clone element node " + node);
            }

            return null;
        }

        // clone the attributes
        final NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            final Attr attribute = (Attr) attributes.item(i);
            try
            {
                // GH#88: Use namespaceURI of attribute and fall back to namespaceURI of owner element node if not set.
                clone.setAttributeNS(ObjectUtils.defaultIfNull(attribute.getNamespaceURI(), nodeNS), attribute.getName(),
                                     attribute.getValue());
            }
            catch (final DOMException dex)
            {
                if (LOG.isWarnEnabled())
                {
                    LOG.warn(String.format("Failed to set attribute <%s> to value <%s>", attribute.getName(), attribute.getValue()));
                }
            }
        }

        // determine the children to clone
        final NodeList children;
        if (node instanceof HtmlTemplate)
        {
            children = ((HtmlTemplate) node).getContent().getChildNodes();
        }
        else
        {
            children = node.getChildNodes();
        }

        // clone the children
        for (int i = 0; i < children.getLength(); i++)
        {
            final Node child = children.item(i);
            final Node childClone = cloneNode(child, pageClone);
            if (childClone != null)
            {
                clone.appendChild(childClone);
            }
        }

        // special handling for frames
        if (node instanceof BaseFrameElement)
        {
            final Page p = ((BaseFrameElement) node).getEnclosedPage();
            if (p instanceof HtmlPage)
            {
                final PageDOMClone frameClone = clonePage((HtmlPage) p);
                if (frameClone != null)
                {
                    pageClone.addFrame(clone, frameClone);
                }
            }
        }

        return clone;
    }
}
