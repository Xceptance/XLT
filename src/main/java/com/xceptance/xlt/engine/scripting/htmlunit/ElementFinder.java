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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.xceptance.xlt.engine.scripting.ScriptException;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.ReplayUtils;
import com.xceptance.xlt.engine.scripting.util.TextMatchingUtils;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Element finder.
 */
public class ElementFinder
{

    private final Strategy CSS_STRATEGY = new CssStrategy();

    private final Strategy DOM_STRATEGY = new DomStrategy();

    private final Strategy ID_STRATEGY = new IdStrategy();

    private final Strategy IDENTIFIER_STRATEGY = new IdentifierStrategy();

    private final Strategy IMPLICIT_STRATEGY = new ImplicitStrategy();

    private final Strategy LINK_STRATEGY = new LinkStrategy();

    private final Strategy NAME_STRATEGY = new NameStrategy();

    private final Strategy XPATH_STRATEGY = new XPathStrategy();

    /**
     * Maps strategy names to strategies.
     */
    private final Map<String, Strategy> strategies = new HashMap<String, Strategy>();

    /**
     * Flag which indicates if element lookup is restricted to visible elements.
     */
    private final boolean visibleOnly;

    /**
     * Constructor.
     * 
     * @param visibleOnly
     *            visible flag
     */
    ElementFinder(final boolean visibleOnly)
    {
        this.visibleOnly = visibleOnly;

        strategies.put("css", CSS_STRATEGY);
        strategies.put("link", LINK_STRATEGY);
        strategies.put("dom", DOM_STRATEGY);
        strategies.put("xpath", XPATH_STRATEGY);
        strategies.put("name", NAME_STRATEGY);
        strategies.put("id", ID_STRATEGY);
        strategies.put("identifier", IDENTIFIER_STRATEGY);
        strategies.put("implicit", IMPLICIT_STRATEGY);
    }

    /**
     * Lookup an element on the given page using the given element locator.
     * 
     * @param page
     *            the HTML page to be searched on
     * @param locator
     *            the element locator
     * @return the first found element
     */
    public HtmlElement find(final HtmlPage page, final String locator)
    {
        final String strategyName;
        final String value;

        final Matcher m = HtmlUnitFinder.STRATEGY_PATTERN.matcher(locator);
        if (m.matches())
        {
            strategyName = m.group(1);
            value = m.group(2);
        }
        else
        {
            strategyName = "implicit";
            value = locator;
        }

        final Strategy strategy = strategies.get(strategyName);
        if (strategy == null)
        {
            throw new IllegalLocatorException("Unknown element locator strategy: " + strategyName);
        }

        final long max = TimerUtils.getTime() + TestContext.getCurrent().getImplicitTimeout();
        do
        {
            final HtmlElement e = strategy.find(page, value);
            if (e != null)
            {
                return e;
            }

            try
            {
                Thread.sleep(200);
            }
            catch (final InterruptedException ie)
            {
                throw new NoSuchElementException("Interrupted while implicitly waiting for an element", ie);
            }
        }
        while (TimerUtils.getTime() < max);

        throw new NoSuchElementException("No element found for locator: " + locator);
    }

    /**
     * Lookup all matching elements for the given element locator on the given page.
     * 
     * @param page
     *            the HTML page to be search on
     * @param locator
     *            the element locator
     * @return list of matched elements
     * @throws IllegalLocatorException
     *             thrown in case the given locator is invalid
     */
    List<HtmlElement> findAll(final HtmlPage page, final String locator)
    {
        final String strategyName;
        final String value;

        final Matcher m = HtmlUnitFinder.STRATEGY_PATTERN.matcher(locator);
        if (m.matches())
        {
            strategyName = m.group(1);
            value = m.group(2);
        }
        else
        {
            strategyName = "implicit";
            value = locator;
        }

        final Strategy strategy = strategies.get(strategyName);
        if (strategy == null)
        {
            throw new IllegalLocatorException("Unknown element locator strategy: " + strategyName);
        }

        return strategy.findAll(page, value);
    }

    /**
     * Base class of all element lookup strategies.
     */
    private abstract class Strategy
    {
        /**
         * Lookup an element on the given page using the given element locator.
         * 
         * @param page
         *            the HTML page to be searched on
         * @param locator
         *            the element locator
         * @return the first found element
         */
        protected abstract HtmlElement find(final HtmlPage page, final String locator);

        /**
         * Lookup an element on the given page using the given element locator.
         * 
         * @param page
         *            the HTML page to be searched on
         * @param locator
         *            the element locator
         * @return the first found element
         */
        protected boolean isAcceptable(final HtmlElement htmlElement)
        {
            if (visibleOnly)
            {
                return HtmlUnitElementUtils.isVisible(htmlElement);
            }
            else
            {
                return true;
            }
        }

        /**
         * Lookup all matching elements on the given page using the given element locator.
         * 
         * @param page
         *            the HTML page to be searched on
         * @param locator
         *            the element locator
         * @return all matching elements
         */
        protected abstract List<HtmlElement> findAll(final HtmlPage page, final String locator);
    }

    /**
     * CSS strategy: <code>css=#id p.class</code>
     */
    private class CssStrategy extends Strategy
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected HtmlElement find(final HtmlPage page, final String cssSelector)
        {
            final DomNodeList<DomNode> found = page.querySelectorAll(cssSelector);

            for (final DomNode domNode : found)
            {
                final HtmlElement e = (HtmlElement) domNode;
                if (isAcceptable(e))
                {
                    return e;
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String cssLocator)
        {
            final ArrayList<HtmlElement> result = new ArrayList<HtmlElement>();
            final DomNodeList<DomNode> found = page.querySelectorAll(cssLocator);

            for (final DomNode domNode : found)
            {
                final HtmlElement e = (HtmlElement) domNode;
                if (isAcceptable(e))
                {
                    result.add(e);
                }
            }

            return result;
        }
    }

    /**
     * DOM lookup strategy <code>dom=&lt;domExp&gt;</code>.
     */
    private class DomStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String domExpression)
        {
            if (!page.getWebClient().getOptions().isJavaScriptEnabled())
            {
                throw new ScriptException("JavaScript needs to be enabled when using DOM locator strategy");
            }

            final ScriptResult r = page.executeJavaScript("(function(){return " + domExpression + "})();");
            if (!ScriptResult.isUndefined(r))
            {
                final Object o = r.getJavaScriptResult();
                if (o instanceof HTMLElement)
                {
                    final HtmlElement e = ((HTMLElement) o).getDomNodeOrDie();
                    if (isAcceptable(e))
                    {
                        return e;
                    }
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String domExpression)
        {
            final List<HtmlElement> result = new ArrayList<HtmlElement>();
            final HtmlElement e = find(page, domExpression);
            if (e != null)
            {
                result.add(e);
            }

            return result;
        }
    }

    /**
     * Identifier (ID or Name) lookup strategy.
     */
    private class IdentifierStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String idOrName)
        {
            HtmlElement e = ID_STRATEGY.find(page, idOrName);
            if (e == null)
            {
                // do not use NAME_STRATEGY, but search for the plain name only
                e = XPATH_STRATEGY.find(page, String.format("//*[@name='%s']", idOrName));
            }

            return e;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String idOrName)
        {
            final ArrayList<HtmlElement> result = new ArrayList<HtmlElement>();
            result.addAll(ID_STRATEGY.findAll(page, idOrName));
            result.addAll(XPATH_STRATEGY.findAll(page, String.format("//*[@name='%s']", idOrName)));

            return result;
        }
    }

    /**
     * ID lookup strategy.
     */
    private class IdStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String id)
        {
            return XPATH_STRATEGY.find(page, toXPath(id));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, String id)
        {
            return XPATH_STRATEGY.findAll(page, toXPath(id));
        }
        
        private String toXPath(final String id)
        {
            return new StringBuilder("//*[@id='").append(id).append("']").toString();
        }
    }

    /**
     * The "implicit" strategy.
     */
    private class ImplicitStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String locator)
        {
            if (locator.startsWith("document."))
            {
                return DOM_STRATEGY.find(page, locator);
            }
            else if (locator.startsWith("//"))
            {
                return XPATH_STRATEGY.find(page, locator);
            }
            else
            {
                return IDENTIFIER_STRATEGY.find(page, locator);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(HtmlPage page, String locator)
        {
            if (locator.startsWith("document."))
            {
                return DOM_STRATEGY.findAll(page, locator);
            }
            else if (locator.startsWith("//"))
            {
                return XPATH_STRATEGY.findAll(page, locator);
            }
            else
            {
                return IDENTIFIER_STRATEGY.findAll(page, locator);
            }
        }
    }

    /**
     * The "link=" strategy.
     */
    private class LinkStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String linkText)
        {
            final List<HtmlAnchor> anchors = page.getAnchors();
            for (final HtmlAnchor a : anchors)
            {
                final String text = HtmlUnitElementUtils.computeText(a);
                if (isAcceptable(a) && TextMatchingUtils.isAMatch(text, linkText, true, true))
                {
                    return a;
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String linkText)
        {
            final ArrayList<HtmlElement> result = new ArrayList<HtmlElement>();
            final List<HtmlAnchor> anchors = page.getAnchors();
            for (final HtmlAnchor a : anchors)
            {
                final String text = HtmlUnitElementUtils.computeText(a);
                if (isAcceptable(a) && TextMatchingUtils.isAMatch(text, linkText, true, true))
                {
                    result.add(a);
                }
            }

            return result;
        }
    }

    /**
     * The "name=" strategy.
     */
    private class NameStrategy extends Strategy
    {
        private final String INDEX_KEY = "index";

        private final String NAME_KEY = "name";

        private final String VALUE_KEY = "value";

        @Override
        protected HtmlElement find(final HtmlPage page, final String name)
        {
            final Map<String, String> attributes = ReplayUtils.parseAttributes(name);
            final List<?> found = page.getByXPath(String.format("//*[@name='%s']", attributes.get(NAME_KEY)));
            List<HtmlElement> elements = new ArrayList<HtmlElement>();
            for (final Object o : found)
            {
                if (o instanceof HtmlElement)
                {
                    final HtmlElement e = (HtmlElement) o;
                    if (isAcceptable(e))
                    {
                        elements.add(e);
                    }
                }
            }

            if (attributes.containsKey(VALUE_KEY))
            {
                final String value = attributes.get(VALUE_KEY);
                final Iterator<HtmlElement> it = elements.iterator();
                while (it.hasNext())
                {
                    final HtmlElement e = it.next();
                    final String elementValue = e.getAttribute("value");
                    boolean keep = false;

                    // wanted element has non empty value
                    if (value.length() > 0)
                    {
                        if (value.equals(elementValue))
                        {
                            keep = true;
                        }
                    }
                    // wanted element has empty value attribute
                    else
                    {
                        if (elementValue.length() == 0 && elementValue != DomElement.ATTRIBUTE_NOT_DEFINED)
                        {
                            keep = true;
                        }
                    }
                    // keep the element?
                    if (!keep)
                    {
                        it.remove();
                    }
                }
            }

            if (attributes.containsKey(INDEX_KEY))
            {
                final String index = attributes.get(INDEX_KEY);
                try
                {
                    final int idx = Integer.parseInt(index);
                    if (idx >= 0 && idx < elements.size())
                    {
                        elements = Collections.singletonList(elements.get(idx));
                    }
                }
                catch (final Exception e)
                {
                    return null;
                }
            }

            if (elements.isEmpty())
            {
                return null;
            }
            else
            {
                return elements.get(0);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String name)
        {
            final Map<String, String> attributes = ReplayUtils.parseAttributes(name);
            final List<?> found = page.getByXPath(String.format("//*[@name='%s']", attributes.get(NAME_KEY)));
            List<HtmlElement> elements = new ArrayList<HtmlElement>();
            for (final Object o : found)
            {
                if (o instanceof HtmlElement)
                {
                    final HtmlElement e = (HtmlElement) o;
                    if (isAcceptable(e))
                    {
                        elements.add(e);
                    }
                }
            }

            if (attributes.containsKey(VALUE_KEY))
            {
                final String value = attributes.get(VALUE_KEY);
                final Iterator<HtmlElement> it = elements.iterator();
                while (it.hasNext())
                {
                    final HtmlElement e = it.next();
                    final String elementValue = e.getAttribute("value");
                    boolean keep = false;

                    // wanted element has non empty value
                    if (value.length() > 0)
                    {
                        if (value.equals(elementValue))
                        {
                            keep = true;
                        }
                    }
                    // wanted element has empty value attribute
                    else
                    {
                        if (elementValue.length() == 0 && elementValue != DomElement.ATTRIBUTE_NOT_DEFINED)
                        {
                            keep = true;
                        }
                    }
                    // keep the element?
                    if (!keep)
                    {
                        it.remove();
                    }
                }
            }

            if (attributes.containsKey(INDEX_KEY))
            {
                final String index = attributes.get(INDEX_KEY);
                try
                {
                    final int idx = Integer.parseInt(index);
                    if (idx >= 0 && idx < elements.size())
                    {
                        elements = Collections.singletonList(elements.get(idx));
                    }
                }
                catch (final Exception e)
                {
                    elements = Collections.emptyList();
                }
            }

            return elements;
        }
    }

    /**
     * XPath lookup strategy <code>xpath=&lt;xpathExp&gt;</code>.
     */
    private class XPathStrategy extends Strategy
    {
        @Override
        protected HtmlElement find(final HtmlPage page, final String xpath)
        {
            final List<?> found = page.getByXPath(xpath);
            for (final Object o : found)
            {
                if (o instanceof HtmlElement)
                {
                    final HtmlElement e = (HtmlElement) o;
                    if (isAcceptable(e))
                    {
                        return e;
                    }
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<HtmlElement> findAll(final HtmlPage page, final String xpath)
        {
            final ArrayList<HtmlElement> result = new ArrayList<HtmlElement>();
            final List<?> found = page.getByXPath(xpath);
            for (final Object o : found)
            {
                if (o instanceof HtmlElement)
                {
                    final HtmlElement e = (HtmlElement) o;
                    if (isAcceptable(e))
                    {
                        result.add(e);
                    }
                }
            }
            return result;
        }
    }
}
