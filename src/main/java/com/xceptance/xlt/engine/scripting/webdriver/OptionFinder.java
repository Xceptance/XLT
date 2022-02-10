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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.xceptance.xlt.engine.scripting.util.TextMatchingUtils;

/**
 * HTML option finder.
 */
class OptionFinder
{

    /**
     * Maps strategy names to strategies.
     */
    private final Map<String, OptionLookupStrategy> strategies = new HashMap<String, OptionLookupStrategy>();

    /**
     * Constructor.
     */
    OptionFinder()
    {
        final OptionLookupStrategy labelStrategy = new LabelStrategy();

        strategies.put("implicit", labelStrategy);
        strategies.put("id", new IdStrategy());
        strategies.put("index", new IndexStrategy());
        strategies.put("label", labelStrategy);
        strategies.put("value", new ValueStrategy());
    }

    /**
     * Lookup an option of the given HTML select element that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return first found option of the given select element that match the given option locator
     */
    WebElement findOption(final WebElement select, final String optionLocator)
    {
        return findOptions(select, optionLocator).get(0);
    }

    /**
     * Lookup all options of the given HTML select element that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return list of all options of the given select element that match the given option locator
     */
    List<WebElement> findOptions(final WebElement select, final String optionLocator)
    {
        final String strategyName;
        final String value;

        final Matcher m = WebDriverFinder.STRATEGY_PATTERN.matcher(optionLocator);
        if (m.matches())
        {
            strategyName = m.group(1);
            value = m.group(2);
        }
        else
        {
            strategyName = "implicit";
            value = optionLocator;
        }

        final OptionLookupStrategy strategy = strategies.get(strategyName);
        if (strategy == null)
        {
            throw new InvalidSelectorException("Unsupported option locator strategy: " + strategyName);
        }

        final List<WebElement> options = strategy.find(select, value);
        if (options.size() == 0)
        {
            throw new NoSuchElementException("No option found for option locator: " + optionLocator);
        }

        return options;
    }

    /**
     * Finds an option by id attribute.
     */
    private static final class IdStrategy extends OptionLookupStrategy
    {
        @Override
        protected List<WebElement> find(final WebElement select, final String id)
        {
            final ArrayList<WebElement> options = new ArrayList<WebElement>();
            for (final WebElement o : getOptionElements(select))
            {
                if (id.equals(o.getAttribute("id")))
                {
                    options.add(o);
                }
            }
            return options;
        }
    }

    /**
     * Finds an option by index.
     */
    private static final class IndexStrategy extends OptionLookupStrategy
    {
        @Override
        protected List<WebElement> find(final WebElement select, final String index)
        {
            final ArrayList<WebElement> options = new ArrayList<WebElement>();
            try
            {
                final int idx = Integer.parseInt(index);
                final List<WebElement> optionElements = getOptionElements(select);
                options.add(optionElements.get(idx));
            }
            catch (final Exception e)
            {
                // ignore
            }
            return options;
        }
    }

    /**
     * Finds an option by label (element text).
     */
    private static final class LabelStrategy extends OptionLookupStrategy
    {
        @Override
        protected List<WebElement> find(final WebElement select, final String textPattern)
        {
            final ArrayList<WebElement> options = new ArrayList<WebElement>();
            for (final WebElement o : getOptionElements(select))
            {
                if (TextMatchingUtils.isAMatch(o.getText(), textPattern, true, true))
                {
                    options.add(o);
                }
            }
            return options;
        }
    }

    /**
     * Finds an option by value attribute.
     */
    private static final class ValueStrategy extends OptionLookupStrategy
    {
        @Override
        protected List<WebElement> find(final WebElement select, final String valuePattern)
        {
            final ArrayList<WebElement> options = new ArrayList<WebElement>();
            for (final WebElement o : getOptionElements(select))
            {
                final String value = StringUtils.defaultString(o.getAttribute("value"));

                if (TextMatchingUtils.isAMatch(value, valuePattern, true, false))
                {
                    options.add(o);
                }
            }
            return options;
        }
    }

    /**
     * Base class of HTML option lookup strategies.
     */
    private static abstract class OptionLookupStrategy
    {
        /**
         * Lookup all options of the given HTML select element that match the given option locator.
         * 
         * @param select
         *            the HTML select element
         * @param criterion
         *            the option locator
         * @return list of all options of the given select element that match the given option locator
         */
        protected abstract List<WebElement> find(final WebElement select, final String criterion);

        /**
         * Lookup all options of the given HTML select element that match the given option locator.
         * 
         * @param select
         *            the HTML select element
         * @param optionLocator
         *            the option locator
         * @return list of all options of the given select element that match the given option locator
         */
        protected List<WebElement> getOptionElements(final WebElement select)
        {
            return select.findElements(By.tagName("option"));
        }
    }

}
