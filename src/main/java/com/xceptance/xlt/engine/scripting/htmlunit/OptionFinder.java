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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.xceptance.xlt.engine.scripting.util.TextMatchingUtils;

/**
 * HTML option locator.
 */
class OptionFinder
{

    /**
     * Map of strategy names to the appropriate strategies.
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
    HtmlOption findOption(final HtmlSelect select, final String optionLocator)
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
    List<HtmlOption> findOptions(final HtmlSelect select, final String optionLocator)
    {
        final String strategyName;
        final String value;

        final Matcher m = HtmlUnitFinder.STRATEGY_PATTERN.matcher(optionLocator);
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
            throw new IllegalLocatorException("Unsupported option locator strategy: " + strategyName);
        }

        final List<HtmlOption> options = strategy.find(select, value);
        if (options.size() == 0)
        {
            throw new NoSuchElementException("No option found for option locator: " + optionLocator);
        }

        return options;
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
         * @param optionLocator
         *            the option locator
         * @return list of all options of the given select element that match the given option locator
         */
        protected abstract List<HtmlOption> find(final HtmlSelect select, final String optionLocator);
    }

    /**
     * Finds an option by id attribute.
     */
    private static final class IdStrategy extends OptionLookupStrategy
    {
        @Override
        protected List<HtmlOption> find(final HtmlSelect select, final String optionLocator)
        {
            final ArrayList<HtmlOption> options = new ArrayList<HtmlOption>();
            for (final HtmlOption o : select.getOptions())
            {
                if (optionLocator.equals(o.getId()))
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
        protected List<HtmlOption> find(final HtmlSelect select, final String optionLocator)
        {
            final ArrayList<HtmlOption> options = new ArrayList<HtmlOption>();
            try
            {
                final int idx = Integer.parseInt(optionLocator);
                if (idx >= 0 && idx < select.getOptionSize())
                {
                    options.add(select.getOption(idx));
                }
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
        protected List<HtmlOption> find(final HtmlSelect select, final String optionLocator)
        {
            final ArrayList<HtmlOption> options = new ArrayList<HtmlOption>();
            for (final HtmlOption o : select.getOptions())
            {
                if (TextMatchingUtils.isAMatch(HtmlUnitElementUtils.computeText(o), optionLocator, true, true))
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
        protected List<HtmlOption> find(final HtmlSelect select, final String valuePattern)
        {
            final ArrayList<HtmlOption> options = new ArrayList<HtmlOption>();
            for (final HtmlOption o : select.getOptions())
            {
                if (TextMatchingUtils.isAMatch(o.getValueAttribute(), valuePattern, true, false))
                {
                    options.add(o);
                }
            }
            return options;
        }
    }

}
