/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.htmlunit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.htmlunit.ScriptResult;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.css.StyleAttributes.Definition;
import org.htmlunit.html.DisabledElement;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.DomText;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlOptionGroup;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPreformattedText;
import org.htmlunit.javascript.host.ClientRect;
import org.htmlunit.javascript.host.Element;
import org.htmlunit.javascript.host.Window;
import org.htmlunit.javascript.host.dom.Document;
import org.htmlunit.javascript.host.event.MouseEvent;
import org.htmlunit.javascript.host.html.HTMLElement;

/**
 * Some utilities for HtmlUnit's HTML elements.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class HtmlUnitElementUtils
{
    /**
     * Tag names of block-level elements.
     */
    private static final String[] BLOCK_LEVEL_TAG_NAMES =
        {
            // From the HTML spec (http://www.w3.org/TR/html401/sgml/dtd.html#block)
            // <!ENTITY % block
            // "P | %heading; | %list; | %preformatted; | DL | DIV | NOSCRIPT | BLOCKQUOTE | FORM | HR | TABLE | FIELDSET | ADDRESS">
            // <!ENTITY % heading "H1|H2|H3|H4|H5|H6">
            // <!ENTITY % list "UL | OL">
            // <!ENTITY % preformatted "PRE">

            // additionally: TH | TD since they are rendered as independent blocks (#944)

            "p", "h1", "h2", "h3", "h4", "h5", "h6", "dl", "div", "noscript", "blockquote", "form", "hr", "table", "fieldset", "address",
            "ul", "ol", "pre", "br", "td", "th"
        };

    /**
     * Non-breaking space.
     */
    private static final char[] NON_BREAKING_SPACES =
        {
            0x00A0, 0x2007, 0x202F
        };

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private HtmlUnitElementUtils()
    {
        // Empty
    }

    /**
     * Returns whether or not the given element is visible.
     * 
     * @param element
     *            the HTML element
     * @return <code>true</code> if the given element is visible, <code>false</code> otherwise.
     * @see DomNode#isDisplayed()
     */
    public static boolean isVisible(final DomElement element)
    {
        if (element instanceof HtmlOption || element instanceof HtmlOptionGroup)
        {
            final HtmlElement select = element.<HtmlElement>getFirstByXPath("./ancestor-or-self::select");
            return select != null && isVisible(select);
        }
        else if (element instanceof HtmlAnchor)
        {
            if (element.getChildElementCount() == 0 && StringUtils.isBlank(collapseWhitespace(element.getTextContent(), true)))
            {
                return false;
            }
        }

        try
        {
            Context.enter();
            if (!element.isDisplayed() || !consumesSpace(element))
            {
                return false;
            }

            return !isOverflowHidden(element);
        }
        finally
        {
            Context.exit();
        }
    }

    private static boolean consumesSpace(final DomElement element)
    {
        final ClientRect rectum = ((HTMLElement) element.getScriptableObject()).getBoundingClientRect();
        if (rectum.getWidth() > 0 && rectum.getHeight() > 0)
        {
            return true;
        }

        if (!"hidden".equals(getOverflow(element)))
        {
            final DomNodeList<DomNode> children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                final DomNode n = children.get(i);
                if (n.getNodeType() == DomNode.TEXT_NODE || n.getNodeType() == DomNode.ELEMENT_NODE && consumesSpace((HtmlElement) n))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOverflowHidden(final DomElement element)
    {
        if ("hidden".equals(getOverFlowState(element)))
        {
            final DomNodeList<DomNode> children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                final DomNode n = children.get(i);
                if (n.getNodeType() == DomNode.ELEMENT_NODE && !isOverflowHidden((HtmlElement) n))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param element
     * @return
     */
    private static String getOverFlowState(final DomElement element)
    {
        // final HTMLElement e = (HTMLElement) element.getScriptObject();
        // final ClientRect rectum = e.getBoundingClientRect();

        final HtmlPage page = (HtmlPage) element.getPage();
        final String overflow = page != null ? getOverflow(page.getDocumentElement()) : "visible";
        for (HtmlElement l = c(element); l != null; l = c(l))
        {
            final String[] q = _getVisibility(l, overflow);
            if (!"visible".equals(q[0]) || !"visible".equals(q[1]))
            {
                final ClientRect box = ((HTMLElement) l.getScriptableObject()).getBoundingClientRect();
                if (box.getWidth() == 0 || box.getHeight() == 0)
                {
                    return "hidden";
                }
                // TODO Reactivate code once HtmlUnit is able to interpret 'auto' CSS values correctly
                // boolean m = rectum.getRight() < box.getLeft(), s = rectum.getBottom() < box.getTop();
                // if (m && "hidden".equals(q[0]) || s && "hidden".equals(q[1]))
                // {
                // return "hidden";
                // }
                //
                // if (m && !"visible".equals(q[0]) || s && !"visible".equals(q[1]))
                // {
                // return "hidden".equals(getOverFlowState(l)) ? "hidden" : "scroll";
                // }
                // m = rectum.getLeft() >= (box.getLeft() + box.getWidth());
                // s = rectum.getTop() >= (box.getTop() + box.getHeight());
                // if (m && "hidden".equals(q[0]) || s && "hidden".equals(q[1]))
                // {
                // return "hidden";
                // }
                // if (m && !"visible".equals(q[0]) || s && !"visible".equals(q[1]))
                // {
                // return "hidden".equals(getOverFlowState(l)) ? "hidden" : "scroll";
                // }
            }
        }

        return "none";
    }

    /**
     * @param l
     * @return
     */
    private static String[] _getVisibility(HtmlElement l, final String overflow)
    {
        final HtmlPage page = (HtmlPage) l.getPage();
        final HtmlElement body = page != null ? page.getBody() : null;
        HtmlElement b = l;
        if ("visible".equals(overflow) && page != null)
        {
            if (l == page.getDocumentElement())
            {
                b = body;
            }
            else
            {
                if (l == body)
                {
                    return new String[]
                        {
                            "visible", "visible"
                        };
                }
            }
        }

        if ("hidden".equals(getOverflow(b)))
        {
            return new String[]
                {
                    "hidden", "hidden"
                };
        }

        String[] r = new String[]
            {
                getOverflowX(b), getOverflowY(b)
            };
        if (l == page.getDocumentElement())
        {
            if ("visible".equals(r[0]))
            {
                r[0] = "auto";
            }
            if ("visible".equals(r[1]))
            {
                r[1] = "auto";
            }
        }
        return r;
    }

    private static String getDisplay(final HtmlElement element)
    {
        final HTMLElement e = (HTMLElement) element.getScriptableObject();
        String value = e.getWindow().getComputedStyle(e, null).getDisplay();
        if ("inherit".equals(value))
        {
            value = getDisplay(getParentElement(element));
        }
        return value;
    }

    private static String getCssPosition(final DomElement element)
    {
        final HTMLElement e = (HTMLElement) element.getScriptableObject();
        String value = e.getWindow().getComputedStyle(e, null).getStyleAttribute(Definition.POSITION);
        if ("inherit".equals(value))
        {
            value = getCssPosition(getParentElement(element));
        }
        return value;
    }

    private static String getOverflow(final DomElement element)
    {
        final HTMLElement e = (HTMLElement) element.getScriptableObject();
        String value = e.getWindow().getComputedStyle(e, null).getStyleAttribute(Definition.OVERFLOW);
        if ("inherit".equals(value))
        {
            value = getCssPosition(getParentElement(element));
        }
        return value;
    }

    private static String getOverflowX(final DomElement element)
    {
        final HTMLElement e = (HTMLElement) element.getScriptableObject();
        String value = e.getWindow().getComputedStyle(e, null).getStyleAttribute(Definition.OVERFLOW_X);
        if ("inherit".equals(value))
        {
            value = getCssPosition(getParentElement(element));
        }
        return value;
    }

    private static String getOverflowY(final DomElement element)
    {
        final HTMLElement e = (HTMLElement) element.getScriptableObject();
        String value = e.getWindow().getComputedStyle(e, null).getStyleAttribute(Definition.OVERFLOW_Y);
        if ("inherit".equals(value))
        {
            value = getCssPosition(getParentElement(element));
        }
        return value;
    }

    private static HtmlElement c(final DomElement element)
    {
        final String pos = getCssPosition(element);
        final HtmlElement docElement = (HtmlElement) element.getPage().getDocumentElement();
        if ("fixed".equals(pos))
        {
            return element == docElement ? null : docElement;
        }

        final boolean posAbsolute = "absolute".equals(pos);
        HtmlElement e = getParentElement(element);
        while (e != null && !b(e, posAbsolute))
        {
            e = getParentElement(e);
        }
        return e;
    }

    /**
     * @param e
     * @return
     */
    private static boolean b(HtmlElement e, final boolean posIsAbsolute)
    {
        final HtmlPage page = (HtmlPage) e.getPage();
        final HtmlElement docElement = page != null ? page.getDocumentElement() : null;
        if (e == docElement)
        {
            return true;
        }
        final String display = getDisplay(e);
        if (display != null && display.startsWith("inline") || posIsAbsolute && "static".equals(getCssPosition(e)))
        {
            return false;
        }

        return true;
    }

    private static HtmlElement getParentElement(final DomElement element)
    {
        DomNode node = element.getParentNode();
        while (node != null && node.getNodeType() != 1)
            node = node.getParentNode();
        return node != null ? (HtmlElement) node : null;
    }

    /**
     * Computes the text for the given element. If the element is invisible the empty text is returned.
     * 
     * @param element
     *            the element whose text shall be computed
     * @return the text of the given element
     */
    public static final String computeText(final DomElement element)
    {
        return getTextFromNode(element, element instanceof HtmlPreformattedText).trim();
    }

    /**
     * Returns whether or not the given HTML element denotes a block-level element.
     * 
     * @param element
     *            the HTML element
     * @return <code>true</code> if the given HTML element denotes a block-level element, <code>false</code> otherwise
     */
    public static boolean isBlockLevelElement(final HtmlElement element)
    {
        final String tagName = element.getTagName().toLowerCase();

        for (final String blockLevelsTagName : BLOCK_LEVEL_TAG_NAMES)
        {
            if (blockLevelsTagName.equals(tagName))
            {
                return true;
            }
        }

        return false;

    }

    /**
     * Returns the given input string where all white-space has been collapsed.
     * 
     * @param text
     *            the input string
     * @return result of collapsing all white-space in the given input string
     */
    public static String collapseWhitespace(final String text)
    {
        return collapseWhitespace(text, false);
    }

    /**
     * Returns the given input string where all white-space has been collapsed.
     * 
     * @param text
     *            the input string
     * @param retainNonBreakingSpace
     *            whether or not non-breaking space characters should be retained
     * @return result of collapsing all white-space in the given input string
     */
    public static String collapseWhitespace(final String text, final boolean retainNonBreakingSpace)
    {
        String s = text;

        // remove any CR in the text
        s = s.replace("\r", "");

        // convert all non-breaking spaces to normal spaces
        if (!retainNonBreakingSpace)
        {
            s = replaceNonBreakingSpaces(s);
        }

        // collapse all regular whitespace characters
        s = s.replaceAll("\\p{javaWhitespace}+", " ");

        return s;

    }

    /**
     * Returns the value of the CSS property <code>text-transform</code> which is <code>none</code>,
     * <code>lowercase</code>, <code>uppercase</code> or <code>capitalize</code>.
     * 
     * @param window
     *            the window that contains the element's owner document
     * @param element
     *            the element
     * @return value of the CSS property <code>text-transform</code>
     */
    public static String getTextTransform(final Window window, final Element element)
    {
        String textTransform = "none";
        if (window != null && element != null)
        {
            textTransform = window.getComputedStyle(element, null).getStyleAttribute(Definition.TEXT_TRANSFORM);
            if ("inherit".equals(textTransform))
            {
                textTransform = getTextTransform(window, element.getParentElement());
            }
        }

        return textTransform;
    }

    /**
     * Returns the computed text of the given DOM node.
     * 
     * @param node
     *            the DOM node
     * @param preformatted
     *            whether or not the given DOM node contains pre-formatted text
     * @return computed text of the given DOM node
     */
    private static String getTextFromNode(final DomNode node, final boolean preformatted)
    {
        switch (node.getNodeType())
        {
            case 3:
                final String data = getTextFromTextNode((DomText) node);
                if (!preformatted)
                {
                    return collapseWhitespace(data);
                }
                return replaceNonBreakingSpaces(data);

            case 1:
                if (!node.isDisplayed())
                {
                    return "";
                }

                final StringBuilder text = new StringBuilder();
                if (isBlockLevelElement((HtmlElement) node))
                {
                    text.append('\n');
                }
                final boolean childrenPreformatted = preformatted || node instanceof HtmlPreformattedText;
                for (final DomNode child : node.getChildren())
                {
                    text.append(getTextFromNode(child, childrenPreformatted));
                }

                return text.toString();

            default:
                break;
        }

        return "";
    }

    /**
     * Returns the text of the given DOM text node while taking the value of the CSS property
     * <code>text-transform</code> into account.
     * 
     * @param textNode
     *            the DOM text node
     * @return text (as displayed) of the given DOM text node
     */
    private static String getTextFromTextNode(final DomText textNode)
    {
        String text = textNode.getNodeValue();
        final HtmlElement parent = (HtmlElement) textNode.getParentNode();
        final Window w = (Window) parent.getPage().getEnclosingWindow().getScriptableObject();
        final String textTransform = getTextTransform(w, (Element) parent.getScriptableObject());
        if ("lowercase".equals(textTransform))
        {
            text = text.toLowerCase();
        }
        else if ("uppercase".equals(textTransform))
        {
            text = text.toUpperCase();
        }
        else if ("capitalize".equals(textTransform))
        {
            text = WordUtils.capitalize(text);
        }
        return text;
    }

    /**
     * Replaces all non-breaking spaces within the given input string with space characters.
     * 
     * @param text
     *            the input string
     * @return the given input string where all non-breaking spaces have been replaced with space characters
     */
    private static String replaceNonBreakingSpaces(final String text)
    {
        String s = text;
        for (final char c : NON_BREAKING_SPACES)
        {
            s = s.replace(c, ' ');
        }
        return s;
    }

    /**
     * Returns the absolute position of the given element (scroll/page offset is NOT included).
     * 
     * @param element
     *            the element
     * @return position of the given element as two-dimensional array of integers
     */
    public static int[] getPosition(DomElement element)
    {
        final HTMLElement scriptable = (HTMLElement) element.getScriptableObject();
        final HtmlElement body = ((HtmlPage) element.getPage()).getBody();

        try
        {
            Context.enter();

            if (element.equals(body))
            {
                return new int[]
                    {
                        scriptable.getOffsetLeft(), scriptable.getOffsetTop()
                    };
            }

            final ClientRect rectum = scriptable.getBoundingClientRect();
            final Document doc = (Document) scriptable.getOwnerDocument();
            final HTMLElement docElement = (HTMLElement) doc.getDocumentElement();
            final HTMLElement bodyElement = (HTMLElement) body.getScriptableObject();

            int clientTop = docElement.getClientTop();
            if (clientTop == 0)
            {
                clientTop = bodyElement.getClientTop();
            }

            int clientLeft = docElement.getClientLeft();
            if (clientLeft == 0)
            {
                clientLeft = bodyElement.getClientLeft();
            }

            return new int[]
                {
                    rectum.getLeft() - clientLeft, rectum.getTop() - clientTop
                };
        }
        finally
        {
            Context.exit();
        }
    }

    /**
     * Fires a mouse event of the given type at the given target element.
     * 
     * @param element
     *            the target element
     * @param eventType
     *            the type of the mouse event (mousemove etc.)
     * @param isCtrlPressed
     *            is CTRL key pressed
     * @param isShiftPressed
     *            is SHIFT key pressed
     * @param isAltPressed
     *            is ALT key pressed
     * @param button
     *            mouse button
     * @param xOffset
     *            offset relative to X coordinate of given element
     * @param yOffset
     *            offset relative to Y coordinate of given element
     * @return resulting page
     */
    public static HtmlPage fireMouseEvent(final DomElement element, final String eventType, final boolean isCtrlPressed,
                                          final boolean isShiftPressed, final boolean isAltPressed, final int button, final int xOffset,
                                          final int yOffset)
    {
        HtmlPage page = (HtmlPage) element.getPage();
        if (element instanceof DisabledElement && ((DisabledElement) element).isDisabled())
        {
            return page;
        }

        final MouseEvent event = new MouseEvent(element, eventType, isShiftPressed, isCtrlPressed, isAltPressed, button);
        if (xOffset > -1 || yOffset > -1)
        {
            final int[] position = getPosition(element);
            if (xOffset > -1)
            {
                event.setClientX(position[0] + xOffset);
            }
            if (yOffset > -1)
            {
                event.setClientY(position[1] + yOffset);
            }
        }

        final ScriptResult result = element.fireEvent(event);
        if (result != null)
        {
            page = (HtmlPage) page.getWebClient().getCurrentWindow().getEnclosedPage();
        }
        return page;
    }

    /**
     * Fires a mouse event of the given type at the given target element.
     * 
     * @param element
     *            the target element
     * @param eventType
     *            the type of the mouse event (mousemove etc.)
     * @param isCtrlPressed
     *            is CTRL key pressed
     * @param isShiftPressed
     *            is SHIFT key pressed
     * @param isAltPressed
     *            is ALT key pressed
     * @param xOffset
     *            offset relative to X coordinate of given element
     * @param yOffset
     *            offset relative to Y coordinate of given element
     * @return resulting page
     */
    public static HtmlPage fireMouseEvent(final DomElement element, final String eventType, final int xOffset, final int yOffset)
    {
        return fireMouseEvent(element, eventType, false, false, false, MouseEvent.BUTTON_LEFT, xOffset, yOffset);
    }

    /**
     * Fires a mouse event of the given type at the given target element.
     * 
     * @param element
     *            the target element
     * @param eventType
     *            the type of the mouse event (mousemove etc.)
     * @param isCtrlPressed
     *            is CTRL key pressed
     * @param isShiftPressed
     *            is SHIFT key pressed
     * @param isAltPressed
     *            is ALT key pressed
     * @param xOffset
     *            offset relative to X coordinate of given element
     * @param yOffset
     *            offset relative to Y coordinate of given element
     * @param mouseButton
     *            the mouse button to use
     * @return resulting page
     */
    public static HtmlPage fireMouseEvent(final DomElement element, final String eventType, final int xOffset, final int yOffset,
                                          final int mouseButton)
    {
        return fireMouseEvent(element, eventType, false, false, false, mouseButton, xOffset, yOffset);
    }
}
