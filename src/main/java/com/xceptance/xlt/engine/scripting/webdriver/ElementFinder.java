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
package com.xceptance.xlt.engine.scripting.webdriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xceptance.xlt.engine.scripting.util.ReplayUtils;
import com.xceptance.xlt.engine.scripting.util.TextMatchingUtils;

/**
 * Element finder.
 */
class ElementFinder
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
     * @param webDriver
     *            the web driver
     * @param locator
     *            the element locator
     * @return the first found element
     */
    WebElement find(final WebDriver webDriver, final String locator)
    {
        final String strategyName;
        final String value;

        final Matcher m = WebDriverFinder.STRATEGY_PATTERN.matcher(locator);
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
            throw new InvalidSelectorException("Unknown element locator strategy: " + strategyName);
        }

        final WebElement e = strategy.find(webDriver, value);
        if (e == null)
        {
            throw new NoSuchElementException("No element found for locator: " + locator);
        }

        return e;
    }

    List<WebElement> findAll(final WebDriver webDriver, final String locator)
    {
        final String strategyName;
        final String value;

        final Matcher m = WebDriverFinder.STRATEGY_PATTERN.matcher(locator);
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
            throw new InvalidSelectorException("Unknown element locator strategy: " + strategyName);
        }

        return strategy.findAll(webDriver, value);
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
        protected abstract WebElement find(final WebDriver webDriver, final String locator);

        protected abstract List<WebElement> findAll(final WebDriver webDriver, final String locator);

        /**
         * Lookup an element on the given page using the given element locator.
         * 
         * @param page
         *            the HTML page to be searched on
         * @param locator
         *            the element locator
         * @return the first found element
         */
        protected boolean isAcceptable(final WebElement webElement)
        {
            if (visibleOnly)
            {
                return webElement.isDisplayed();
            }
            else
            {
                return true;
            }
        }
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
        protected WebElement find(final WebDriver webDriver, final String cssSelector)
        {
            final List<WebElement> found = webDriver.findElements(By.cssSelector(cssSelector));

            for (final WebElement e : found)
            {
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
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final ArrayList<WebElement> result = new ArrayList<WebElement>();
            final List<WebElement> found = webDriver.findElements(By.cssSelector(locator));
            for (final WebElement e : found)
            {
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
        /**
         * {@inheritDoc}
         */
        @Override
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            final Object result = WebDriverUtils.executeJavaScript(webDriver, "return " + locator);

            if (result instanceof WebElement)
            {
                final WebElement element = (WebElement) result;
                if (isAcceptable(element))
                {
                    return element;
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final List<WebElement> result = new ArrayList<WebElement>();
            final WebElement e = find(webDriver, locator);
            if (e != null)
            {
                result.add(e);
            }
            return result;
        }
    }

    /**
     * Identifier (ID or name) lookup strategy.
     */
    private class IdentifierStrategy extends Strategy
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            WebElement e = ID_STRATEGY.find(webDriver, locator);

            if (e == null)
            {
                // do not use NAME_STRATEGY, but search for the plain name only
                e = XPATH_STRATEGY.find(webDriver, String.format("//*[@name='%s']", locator));
            }

            return e;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final ArrayList<WebElement> result = new ArrayList<WebElement>();
            result.addAll(ID_STRATEGY.findAll(webDriver, locator));
            result.addAll(XPATH_STRATEGY.findAll(webDriver, String.format("//*[@name='%s']", locator)));

            return result;
        }

    }

    /**
     * ID lookup strategy.
     */
    private class IdStrategy extends Strategy
    {
        @Override
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            return XPATH_STRATEGY.find(webDriver, toXPath(locator));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            return XPATH_STRATEGY.findAll(webDriver, toXPath(locator));
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
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            if (locator.startsWith("document."))
            {
                return DOM_STRATEGY.find(webDriver, locator);
            }
            else if (locator.startsWith("//"))
            {
                return XPATH_STRATEGY.find(webDriver, locator);
            }
            else
            {
                return IDENTIFIER_STRATEGY.find(webDriver, locator);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            if (locator.startsWith("document."))
            {
                return DOM_STRATEGY.findAll(webDriver, locator);
            }
            else if (locator.startsWith("//"))
            {
                return XPATH_STRATEGY.findAll(webDriver, locator);
            }
            else
            {
                return IDENTIFIER_STRATEGY.findAll(webDriver, locator);
            }
        }
    }

    /**
     * The "link=" strategy.
     */
    private class LinkStrategy extends Strategy
    {
        @Override
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            final List<WebElement> anchors = webDriver.findElements(By.tagName("a"));

            for (final WebElement a : anchors)
            {
                if (isAcceptable(a) && TextMatchingUtils.isAMatch(a.getText(), locator, true, true))
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
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final ArrayList<WebElement> result = new ArrayList<WebElement>();
            final List<WebElement> found = webDriver.findElements(By.tagName("a"));
            for (final WebElement e : found)
            {
                if (isAcceptable(e) && TextMatchingUtils.isAMatch(e.getText(), locator, true, true))
                {
                    result.add(e);
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
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            final Map<String, String> attributes = ReplayUtils.parseAttributes(locator);
            final List<WebElement> found = webDriver.findElements(By.xpath(String.format("//*[@name='%s']", attributes.get(NAME_KEY))));
            List<WebElement> elements = new ArrayList<WebElement>();
            for (final WebElement e : found)
            {
                if (isAcceptable(e))
                {
                    elements.add(e);
                }
            }

            if (attributes.containsKey(VALUE_KEY))
            {
                final String value = attributes.get(VALUE_KEY);
                final Iterator<WebElement> it = elements.iterator();
                while (it.hasNext())
                {
                    final WebElement e = it.next();
                    final String elementValue = e.getAttribute("value");
                    boolean keep = false;

                    // attribute is not defined
                    if (elementValue != null)
                    {
                        // wanted element has non empty value
                        if (value.length() > 0)
                        {
                            if (value.equals(elementValue))
                            {
                                keep = true;
                            }
                        }
                        // wanted element has empty value attribute
                        // or attribute is not defined (HtmlUnit)
                        else if (elementValue.length() == 0)
                        {
                            if (elementValue != DomElement.ATTRIBUTE_NOT_DEFINED && value.equals(elementValue))
                            {
                                keep = true;
                            }
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
                    elements = Collections.singletonList(elements.get(idx));
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
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final Map<String, String> attributes = ReplayUtils.parseAttributes(locator);
            final List<WebElement> found = webDriver.findElements(By.xpath(String.format("//*[@name='%s']", attributes.get(NAME_KEY))));
            List<WebElement> elements = new ArrayList<WebElement>();
            for (final WebElement e : found)
            {
                if (isAcceptable(e))
                {
                    elements.add(e);
                }
            }

            if (attributes.containsKey(VALUE_KEY))
            {
                final String value = attributes.get(VALUE_KEY);
                final Iterator<WebElement> it = elements.iterator();
                while (it.hasNext())
                {
                    final WebElement e = it.next();
                    final String elementValue = e.getAttribute("value");
                    boolean keep = false;

                    // attribute is not defined
                    if (elementValue != null)
                    {
                        // wanted element has non empty value
                        if (value.length() > 0)
                        {
                            if (value.equals(elementValue))
                            {
                                keep = true;
                            }
                        }
                        // wanted element has empty value attribute
                        // or attribute is not defined (HtmlUnit)
                        else if (elementValue.length() == 0)
                        {
                            if (elementValue != DomElement.ATTRIBUTE_NOT_DEFINED && value.equals(elementValue))
                            {
                                keep = true;
                            }
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
                    elements = Collections.singletonList(elements.get(idx));
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
        /**
         * {@inheritDoc}
         */
        @Override
        protected WebElement find(final WebDriver webDriver, final String locator)
        {
            final List<WebElement> found = webDriver.findElements(By.xpath(locator));

            for (final WebElement e : found)
            {
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
        protected List<WebElement> findAll(WebDriver webDriver, String locator)
        {
            final ArrayList<WebElement> result = new ArrayList<WebElement>();
            final List<WebElement> found = webDriver.findElements(By.xpath(locator));

            for (final WebElement e : found)
            {
                if (isAcceptable(e))
                {
                    result.add(e);
                }
            }
            return result;
        }
    }

}
