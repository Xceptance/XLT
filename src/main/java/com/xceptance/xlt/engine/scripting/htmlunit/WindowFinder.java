/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.engine.scripting.util.TextMatchingUtils;

/**
 * Window finder.
 */
class WindowFinder
{

    private static final Strategy IMPLICIT_STRATEGY = new ImplicitStrategy();

    private static final Strategy NAME_STRATEGY = new NameStrategy();

    private static final Strategy TITLE_STRATEGY = new TitleStrategy();

    /**
     * Maps strategy names to strategies.
     */
    private final Map<String, Strategy> strategies = new HashMap<String, Strategy>();

    /**
     * Constructor.
     */
    WindowFinder()
    {
        strategies.put("name", NAME_STRATEGY);
        strategies.put("title", TITLE_STRATEGY);
        strategies.put("implicit", IMPLICIT_STRATEGY);
    }

    /**
     * Lookup an element on the given page using the given element locator.
     * 
     * @param webDriver
     *            the web driver
     * @param windowLocator
     *            the element locator
     * @return the first found element
     */
    WebWindow find(final WebClient webClient, final String windowLocator)
    {
        final String strategyName;
        final String value;

        final Matcher m = HtmlUnitFinder.STRATEGY_PATTERN.matcher(windowLocator);
        if (m.matches())
        {
            strategyName = m.group(1);
            value = m.group(2);
        }
        else
        {
            strategyName = "implicit";
            value = windowLocator;
        }

        final Strategy strategy = strategies.get(strategyName);
        if (strategy == null)
        {
            throw new IllegalLocatorException("Unknown window locator strategy: " + strategyName);
        }

        final WebWindow windowHandle = strategy.find(webClient, value);
        if (windowHandle == null)
        {
            throw new NoSuchWindowException("No window found for locator: " + windowLocator);
        }

        return windowHandle;
    }

    /**
     * Base class of all element lookup strategies.
     */
    private static abstract class Strategy
    {
        /**
         * Lookup an element on the given page using the given element locator.
         * 
         * @param webClient
         *            the web client
         * @param criterion
         *            the element locator
         * @return the first found element
         */
        protected abstract WebWindow find(final WebClient WebClient, final String criterion);
    }

    /**
     * Finds windows by name, then by title.
     */
    private static final class ImplicitStrategy extends Strategy
    {
        @Override
        protected WebWindow find(final WebClient webClient, final String nameOrTitle)
        {
            WebWindow w = NAME_STRATEGY.find(webClient, nameOrTitle);
            if (w == null)
            {
                w = TITLE_STRATEGY.find(webClient, nameOrTitle);
            }

            return w;
        }
    }

    /**
     * Finds windows by name.
     */
    private static final class NameStrategy extends Strategy
    {
        @Override
        protected WebWindow find(final WebClient webClient, final String windowName)
        {
            for (final WebWindow w : webClient.getTopLevelWindows())
            {
                if (TextMatchingUtils.isAMatch(w.getName(), windowName, true, true))
                {
                    return w;
                }
            }

            return null;
        }
    }

    /**
     * Finds windows by title.
     */
    private static final class TitleStrategy extends Strategy
    {
        @Override
        protected WebWindow find(final WebClient webClient, final String windowTitle)
        {
            for (final WebWindow w : webClient.getTopLevelWindows())
            {
                final Page page = w.getEnclosedPage();
                if (page instanceof HtmlPage)
                {
                    if (TextMatchingUtils.isAMatch(((HtmlPage) page).getTitleText(), windowTitle, true, true))
                    {
                        return w;
                    }
                }
            }

            return null;
        }
    }
}
