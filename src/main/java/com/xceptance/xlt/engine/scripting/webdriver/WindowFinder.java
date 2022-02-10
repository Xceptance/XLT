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
package com.xceptance.xlt.engine.scripting.webdriver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;

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
     * Finds a window using the given window locator and returns a handle to it.
     * 
     * @param webDriver
     *            the web driver
     * @param windowLocator
     *            the window locator
     * @param switchBack
     *            whether or not to switch back to the current window
     * @return the handle to the first window found
     */
    String find(final WebDriver webDriver, final String windowLocator, final boolean switchBack)
    {
        final String strategyName;
        final String value;

        final Matcher m = WebDriverFinder.STRATEGY_PATTERN.matcher(windowLocator);
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
            throw new InvalidSelectorException("Unknown window locator strategy: " + strategyName);
        }

        final String windowHandle = strategy.find(webDriver, value, switchBack);
        if (windowHandle == null)
        {
            throw new NoSuchWindowException("No window found for locator: " + windowLocator);
        }

        return windowHandle;
    }

    /**
     * Base class of all window lookup strategies.
     */
    private static abstract class Strategy
    {
        /**
         * Finds a window using the given window locator and returns a handle to it.
         * 
         * @param webDriver
         *            the web driver
         * @param criterion
         *            the criterion to identify the target window
         * @param switchBack
         *            whether or not to switch back to the current window
         * @return the handle to the first window found, or <code>null</code>
         */
        protected abstract String find(final WebDriver webDriver, final String criterion, final boolean switchBack);
    }

    /**
     * Finds windows by a certain criterion.
     */
    private static abstract class AbstractStrategy extends Strategy
    {
        @Override
        protected String find(final WebDriver webDriver, final String criterion, final boolean switchBack)
        {
            String currentWindow = null;

            if (switchBack)
            {
                // need to remember the current window -> may throw NoSuchWindowException
                currentWindow = webDriver.getWindowHandle();
            }

            try
            {
                for (final String windowHandle : webDriver.getWindowHandles())
                {
                    webDriver.switchTo().window(windowHandle);

                    if (checkCurrentWindow(webDriver, criterion))
                    {
                        // found it
                        return windowHandle;
                    }
                }

                return null;
            }
            finally
            {
                if (switchBack)
                {
                    // return to original window
                    webDriver.switchTo().window(currentWindow);
                }
            }
        }

        /**
         * Checks whether the current window matches the specified criterion.
         * 
         * @param webDriver
         *            the web driver
         * @param criterion
         *            the criterion to identify the target window
         * @return <code>true</code> if it is the target window, <code>false</code> otherwise
         */
        protected abstract boolean checkCurrentWindow(final WebDriver webDriver, final String criterion);
    }

    /**
     * Finds windows by name, then by title.
     */
    private static final class ImplicitStrategy extends Strategy
    {
        @Override
        protected String find(final WebDriver webDriver, final String nameOrTitle, final boolean switchBack)
        {
            String windowHandle = NAME_STRATEGY.find(webDriver, nameOrTitle, switchBack);
            if (windowHandle == null)
            {
                windowHandle = TITLE_STRATEGY.find(webDriver, nameOrTitle, switchBack);
            }

            return windowHandle;
        }
    }

    /**
     * Finds windows by name.
     */
    private static final class NameStrategy extends AbstractStrategy
    {
        @Override
        protected boolean checkCurrentWindow(final WebDriver webDriver, final String name)
        {
            final String windowName = WebDriverUtils.getCurrentWindowName(webDriver);
            return TextMatchingUtils.isAMatch(windowName, name, true, true);
        }
    }

    /**
     * Finds windows by title.
     */
    private static final class TitleStrategy extends AbstractStrategy
    {
        @Override
        protected boolean checkCurrentWindow(final WebDriver webDriver, final String title)
        {
            return TextMatchingUtils.isAMatch(webDriver.getTitle(), title, true, true);
        }
    }
}
