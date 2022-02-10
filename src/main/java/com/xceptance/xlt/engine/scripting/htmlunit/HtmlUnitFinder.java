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

import java.util.List;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

/**
 * Locator used to lookup HtmlUnit {@link HtmlElement}s.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class HtmlUnitFinder
{
    /**
     * Pattern used to parse lookup strategies.
     */
    static final Pattern STRATEGY_PATTERN = Pattern.compile("^(\\p{Alpha}+)=(.*)");

    /**
     * Element finder.
     */
    private final ElementFinder finder = new ElementFinder(false);

    /**
     * HTML option locator.
     */
    private final OptionFinder optionFinder = new OptionFinder();

    /**
     * Element finder restricted to visible elements.
     */
    private final ElementFinder visibleFinder = new ElementFinder(true);

    /**
     * Element finder restricted to visible elements.
     */
    private final WindowFinder windowFinder = new WindowFinder();

    /**
     * Lookup an element for the given locator on the given page.
     * 
     * @param page
     *            the HTML page to be searched on
     * @param locator
     *            the element locator
     * @return the first found element that is visible
     * @throws IllegalLocatorException
     *             if given locator is invalid
     * @throws NoSuchElementException
     *             if no such element was found
     */
    public HtmlElement findElement(final HtmlPage page, final String locator)
    {
        return findElement(page, locator, false);
    }

    /**
     * Lookup an element for the given locator on the given page.
     * 
     * @param page
     *            the HTML page to be searched on
     * @param locator
     *            the element locator
     * @param visibleElementsOnly
     *            flag which indicates if lookup should be restricted to visible elements
     * @return the first found element
     * @throws IllegalLocatorException
     *             if given locator is invalid
     * @throws NoSuchElementException
     *             if no such element was found
     */
    public HtmlElement findElement(final HtmlPage page, final String locator, final boolean visibleElementsOnly)
    {
        return getFinder(visibleElementsOnly).find(page, locator);
    }

    /**
     * Lookup all matching elements for the given locator on the given page.
     * 
     * @param page
     *            the HTML page to be searched on
     * @param locator
     *            the element locator
     * @param visibleElementsOnly
     *            flag which indicates if lookup should be restricted to visible elements
     * @return all found element that match the given locator
     * @throws IllegalLocatorException
     *             if given locator is invalid
     */
    public List<HtmlElement> findElements(final HtmlPage page, final String locator, final boolean visibleElementsOnly)
    {
        return getFinder(visibleElementsOnly).findAll(page, locator);
    }

    /**
     * Lookup all matching elements for the given locator on the given page.
     * 
     * @param page
     *            the HTML page to be searched on
     * @param locator
     *            the element locator
     * @param visibleElementsOnly
     *            flag which indicates if lookup should be restricted to visible elements
     * @return all found element that match the given locator
     * @throws IllegalLocatorException
     *             if given locator is invalid
     */
    public List<HtmlElement> findElements(final HtmlPage page, final String locator)
    {
        return findElements(page, locator, false);
    }

    /**
     * Lookup an option of the given HTML select element that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return the first found option matching the given option locator
     * @throws IllegalLocatorException
     *             if given locator is invalid
     * @throws NoSuchElementException
     *             if no such option was found
     */
    public HtmlOption findOption(final HtmlSelect select, final String optionLocator)
    {
        return optionFinder.findOption(select, optionLocator);
    }

    /**
     * Lookup all options of the given HTML select elements that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return list of matched options
     * @throws IllegalLocatorException
     *             if given locator is invalid
     * @throws NoSuchElementException
     *             if no such option was found
     */
    public List<HtmlOption> findOptions(final HtmlSelect select, final String optionLocator)
    {
        return optionFinder.findOptions(select, optionLocator);
    }

    /**
     * Lookup all options of the given HTML select elements that match the given window locator.
     * 
     * @param select
     *            the web client
     * @param windowLocator
     *            the window locator
     * @return the window
     * @throws IllegalLocatorException
     *             if given locator is invalid
     * @throws NoWindowElementException
     *             if no such window was found
     */
    public WebWindow findWindow(final WebClient webClient, final String windowLocator)
    {
        return windowFinder.find(webClient, windowLocator);
    }

    /**
     * Returns whether or not there exists at least one element identifiable via the given locator.
     * 
     * @param page
     *            the HTML page to be search on
     * @param locator
     *            the element locator
     * @return <code>true</code> if there exists at least one element for the given element locator, <code>false</code>
     *         otherwise
     */
    public boolean isElementPresent(final HtmlPage page, final String locator)
    {
        try
        {
            findElement(page, locator, false);
        }
        catch (final NoSuchElementException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Returns the element finder to be used.
     * 
     * @param visibleElementsOnly
     *            flag which indicates if element lookup should be restricted to visible elements
     * @return element finder
     */
    private ElementFinder getFinder(final boolean visibleElementsOnly)
    {
        return visibleElementsOnly ? visibleFinder : finder;
    }
}
