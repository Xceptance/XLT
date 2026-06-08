/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.scorecard.groovy.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.GroovyEvaluator;
import com.xceptance.xlt.report.scorecard.GroupDefinition;
import com.xceptance.xlt.report.scorecard.RatingDefinition;
import com.xceptance.xlt.report.scorecard.RuleDefinition;
import com.xceptance.xlt.report.scorecard.SelectorDefinition;
import com.xceptance.xlt.report.scorecard.ValidationException;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Root entry point for the Groovy DSL that builds a scorecard {@link Configuration}.
 * <p>
 * This builder provides the top-level structure for defining a complete scorecard configuration, including reusable
 * XPath selectors, validation rules, rule groups, and score-to-rating mappings.
 * </p>
 * <p>
 * Example usage in a Groovy config file:
 * </p>
 * 
 * <pre>{@code
 * scorecard {
 *     version 1
 *
 *     selectors {
 *         selector { id "errors"; expression "//summary/errors" }
 *     }
 *
 *     rules {
 *         rule { id "no-errors"; ... }
 *     }
 *
 *     groups {
 *         group { id "stability"; rules ["no-errors"] }
 *     }
 *
 *     ratings {
 *         rating { id "grade-a"; value 90.0 }
 *     }
 * }
 * }</pre>
 *
 * @see Configuration
 * @see GroovyEvaluator (parses and invokes this builder)
 */
public class ScorecardBuilder
{
    /** Builder for the selectors {} section */
    private final SelectorsBuilder selectorsBuilder = new SelectorsBuilder();

    /** Builder for the rules {} section */
    private final RulesBuilder rulesBuilder = new RulesBuilder();

    /** Builder for the groups {} section */
    private final GroupsBuilder groupsBuilder = new GroupsBuilder();

    /** Builder for the ratings {} section */
    private final RatingsBuilder ratingsBuilder = new RatingsBuilder();

    /**
     * Sets the configuration version number.
     * <p>
     * Currently this value is consumed but not stored, as the Configuration handles versioning implicitly. Future
     * versions may use this for backwards compatibility.
     * </p>
     *
     * @param version
     *            the configuration schema version
     */
    public void version(int version)
    {
        // Version is consumed for forward compatibility but not currently used.
        // Future schema changes may require version-specific parsing logic.
    }

    /**
     * Defines the selectors section containing reusable XPath expressions.
     * <p>
     * Selectors can be referenced by rules via their ID, avoiding repetition of complex XPath expressions throughout
     * the configuration.
     * </p>
     *
     * @param closure
     *            the closure defining selectors using {@link SelectorsBuilder}
     */
    public void selectors(@DelegatesTo(SelectorsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(selectorsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Defines the rules section containing individual validation rules.
     * <p>
     * Each rule specifies one or more checks against the test report XML document. Rules must be defined before they
     * can be referenced by groups.
     * </p>
     *
     * @param closure
     *            the closure defining rules using {@link RulesBuilder}
     */
    public void rules(@DelegatesTo(RulesBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(rulesBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Defines the groups section that organizes rules into logical categories.
     * <p>
     * Groups aggregate rules and define how points are calculated (e.g., all rules must pass, or first matching rule
     * wins). Groups also control the test failure behavior.
     * </p>
     *
     * @param closure
     *            the closure defining groups using {@link GroupsBuilder}
     */
    public void groups(@DelegatesTo(GroupsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(groupsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Defines the ratings section that maps score percentages to grade labels.
     * <p>
     * Ratings are evaluated in order; the first rating whose threshold is greater than or equal to the achieved
     * percentage is selected. Low ratings can be configured to fail the overall test.
     * </p>
     *
     * @param closure
     *            the closure defining ratings using {@link RatingsBuilder}
     */
    public void ratings(@DelegatesTo(RatingsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(ratingsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Builds and returns the fully populated {@link Configuration}.
     * <p>
     * This is called after all DSL sections have been processed to obtain the final configuration object for use by the
     * evaluator.
     * </p>
     *
     * @return the completed Configuration with all defined selectors, rules, groups, and ratings
     * @throws ValidationException
     *             in case of validation errors
     */
    public Configuration build() throws ValidationException
    {
        final Configuration config = new Configuration();

        final SelectorDefinition[] selectorArr = selectorsBuilder.build();
        if (selectorArr != null)
        {
            for (int i = 0; i < selectorArr.length; i++)
            {
                try
                {
                    config.addSelector(selectorArr[i]);
                }
                catch (final Exception e)
                {
                    throw new ValidationException(String.format("Selector #%d is invalid: %s", i, e.getMessage()));
                }
            }
        }

        final RuleDefinition[] ruleArr = rulesBuilder.build();
        if (ruleArr != null)
        {
            for (int i = 0; i < ruleArr.length; i++)
            {
                try
                {
                    config.addRule(ruleArr[i]);
                }
                catch (final Exception e)
                {
                    throw new ValidationException(String.format("Rule #%d is invalid: %s", i, e.getMessage()));
                }
            }
        }

        final GroupDefinition[] groupArr = groupsBuilder.build();
        for (int i = 0; i < groupArr.length; i++)
        {
            try
            {
                config.addGroup(groupArr[i]);
            }
            catch (final Exception e)
            {
                throw new ValidationException(String.format("Group #%d is invalid: %s", i, e.getMessage()));
            }
        }

        final RatingDefinition[] ratingArr = ratingsBuilder.build();
        if (ratingArr != null)
        {
            for (int i = 0; i < ratingArr.length; i++)
            {
                try
                {
                    config.addRating(ratingArr[i]);
                }
                catch (final Exception e)
                {
                    throw new ValidationException(String.format("Rating #%d is invalid: %s", i, e.getMessage()));
                }
            }
        }

        config.validate();

        return config;
    }
}
