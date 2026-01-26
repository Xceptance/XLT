package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;

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
    /** The configuration being built, populated by nested builders */
    private final Configuration config = new Configuration();

    /** Builder for the selectors {} section */
    private final SelectorsBuilder selectorsBuilder = new SelectorsBuilder(config);

    /** Builder for the rules {} section */
    private final RulesBuilder rulesBuilder = new RulesBuilder(config);

    /** Builder for the groups {} section */
    private final GroupsBuilder groupsBuilder = new GroupsBuilder(config);

    /** Builder for the ratings {} section */
    private final RatingsBuilder ratingsBuilder = new RatingsBuilder(config);

    /**
     * Sets the configuration version number.
     * <p>
     * Currently this value is consumed but not stored, as the Configuration handles versioning implicitly. Future versions
     * may use this for backwards compatibility.
     * </p>
     *
     * @param version
     *                    the configuration schema version
     */
    public void version(int version)
    {
        // Version is consumed for forward compatibility but not currently used.
        // Future schema changes may require version-specific parsing logic.
    }

    /**
     * Defines the selectors section containing reusable XPath expressions.
     * <p>
     * Selectors can be referenced by rules via their ID, avoiding repetition of complex XPath expressions throughout the
     * configuration.
     * </p>
     *
     * @param closure
     *                    the closure defining selectors using {@link SelectorsBuilder}
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
     * Each rule specifies one or more checks against the test report XML document. Rules must be defined before they can be
     * referenced by groups.
     * </p>
     *
     * @param closure
     *                    the closure defining rules using {@link RulesBuilder}
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
     * Groups aggregate rules and define how points are calculated (e.g., all rules must pass, or first matching rule wins).
     * Groups also control the test failure behavior.
     * </p>
     *
     * @param closure
     *                    the closure defining groups using {@link GroupsBuilder}
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
     * Ratings are evaluated in order; the first rating whose threshold is greater than or equal to the achieved percentage
     * is selected. Low ratings can be configured to fail the overall test.
     * </p>
     *
     * @param closure
     *                    the closure defining ratings using {@link RatingsBuilder}
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
     */
    public Configuration build()
    {
        return config;
    }
}
