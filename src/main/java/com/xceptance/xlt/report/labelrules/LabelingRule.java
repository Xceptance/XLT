/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.labelrules;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.util.rules.AbstractDataProcessingRule;
import com.xceptance.xlt.util.rules.AbstractFilter;
import com.xceptance.xlt.util.rules.InvalidDataProcessingRuleException;
import com.xceptance.xlt.util.rules.PlaceholderPosition;

/**
 * A {@link LabelingRule} governs the process of labeling data objects. It represents a bundle of criteria a data object
 * must meet to be labeled and defines how the new label will look like.
 * 
 * @see Data#setLabel(String)
 */
public class LabelingRule extends AbstractDataProcessingRule<Data>
{
    /**
     * Our return states to ensure correct communication of the result
     */
    public static enum ReturnState
    {
        STOP,
        CONTINUE
    };

    /**
     * The definition of the new label including placeholders.
     */
    private final String newLabel;

    /**
     * Whether or not to process the next rule if the current rule applied.
     */
    private final boolean stopOnMatch;

    /**
     * The list of configured request filters of this rule.
     */
    private final AbstractFilter<Data>[] filters;

    /**
     * The list of placeholders (with their position, etc.) in the new name.
     */
    private final PlaceholderPosition[] newLabelPlaceholders;

    /**
     * Constructor.
     *
     * @param newLabel
     * @param namePattern
     * @param labelPattern
     * @param typeCodePattern
     * @param stopOnMatch
     * @param nameExcludePattern
     * @param labelExcludePattern
     * @param typeCodeExcludePattern
     * @throws InvalidLabelingRuleException
     */
    public LabelingRule(final String newLabel, final String namePattern, final String labelPattern, String typeCodePattern,
                        final boolean stopOnMatch, final String nameExcludePattern, final String labelExcludePattern,
                        String typeCodeExcludePattern)
        throws InvalidDataProcessingRuleException
    {
        this.newLabel = newLabel;
        this.stopOnMatch = stopOnMatch;

        final ArrayList<AbstractFilter<Data>> requestFilters = new ArrayList<>(20);

        // parse the placeholder positions now (and only once)
        newLabelPlaceholders = parsePlaceholderPositions(newLabel);
        try
        {   // includes and source of data if not empty and configured
            addIfTypeCodeInNewName(requestFilters, new NameFilter(namePattern), namePattern, newLabelPlaceholders);
            addIfTypeCodeInNewName(requestFilters, new LabelFilter(labelPattern), labelPattern, newLabelPlaceholders);
            addIfTypeCodeInNewName(requestFilters, new TypeCodeFilter(typeCodePattern), typeCodePattern, newLabelPlaceholders);

            // excludes
            if (StringUtils.isNotBlank(nameExcludePattern))
            {
                requestFilters.add(new NameFilter(nameExcludePattern, true));
            }

            if (StringUtils.isNotBlank(labelExcludePattern))
            {
                requestFilters.add(new LabelFilter(labelExcludePattern, true));
            }

            if (StringUtils.isNotBlank(typeCodeExcludePattern))
            {
                requestFilters.add(new TypeCodeFilter(typeCodeExcludePattern, true));
            }
        }
        catch (final PatternSyntaxException pse)
        {
            throw new InvalidDataProcessingRuleException("Invalid regular expression: " + pse.getPattern());
        }

        this.filters = requestFilters.toArray(new AbstractFilter[requestFilters.size()]);

        // Validate the entire rule.
        validateRule(filters, newLabelPlaceholders);
    }

    /**
     * Processes this request merge rule. As a consequence, the passed request data object will (or will not) get a new
     * name. This is a multi-threaded routine aka in use by several threads at the same time.
     *
     * @param data
     *            the data object to process, will also be directly modified as result
     * @return true if we want to stop, false otherwise
     */
    public ReturnState process(final Data data)
    {
        // try each filter and remember its state for later processing
        final int requestFiltersSize = filters.length;
        // we can allocate that here because it is small and will live on the stack,
        // hence will not create GC pressure and will be hit in the cache
        final Object[] filterStates = new Object[requestFiltersSize];

        for (int i = 0; i < requestFiltersSize; i++)
        {
            final AbstractFilter<Data> filter = filters[i];
            final var state = filter.appliesTo(data);

            if (state == null)
            {
                // return early since one of the filters did *not* apply

                // continue request processing with an unmodified result
                return ReturnState.CONTINUE;
            }
            filterStates[i] = state;
        }

        // all filters applied so we can process the request, but check first what to do
        // anything to do?
        if (newLabelPlaceholders.length > 0)
        {
            // rename the request
            final StringBuilder result = new StringBuilder(newLabel);

            // search as long as there are placeholders in the name
            int displacement = 0;
            for (final PlaceholderPosition placeholder : newLabelPlaceholders)
            {
                // find the corresponding filter and filter state
                for (int i = 0; i < requestFiltersSize; i++)
                {
                    final AbstractFilter<Data> requestFilter = filters[i];

                    // check if this is our type code (compare it efficiently)
                    if (requestFilter.isSameTypeCode(placeholder.typeCode, placeholder.typeCodeHashCode))
                    {
                        final int capturingGroupIndex = placeholder.index;
                        final Object filterState = filterStates[i];

                        // get replacement
                        final CharSequence replacement = requestFilter.getReplacementText(data, capturingGroupIndex, filterState);

                        // replace the placeholder with the real values
                        result.delete(placeholder.start + displacement, placeholder.end + displacement);
                        result.insert(placeholder.start + displacement, replacement);

                        // adjust the displacement for the next replace
                        displacement += replacement.length() - placeholder.length;

                        break;
                    }
                }
            }

            // set the final name
            data.setLabel(result.toString());
        }
        else
        {
            // nothing to do, keep the newName
            data.setLabel(newLabel);
        }

        return stopOnMatch ? ReturnState.STOP : ReturnState.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Labeling rule: '").append(newLabel).append("', filters:  [");
        boolean appendComma = false;
        for (final AbstractFilter<Data> filter : filters)
        {
            final String typeCode = filter.getTypeCode();

            if (appendComma)
            {
                sb.append(", ");
            }

            // nl
            if ("n".equals(typeCode))
            {
                sb.append("name: ").append(filter.toString());
            }
            else if ("l".equals(typeCode))
            {
                sb.append("label: ").append(filter.toString());
            }
            else
            {
                sb.append("unknown");
            }

            appendComma = true;
        }
        sb.append("]");

        return sb.toString();
    }
}
