package com.xceptance.xlt.report.scorecard.builder;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link Check} definitions within a rule.
 * <p>
 * This container builder is used within a rule's {@code checks} section to define one or more validation checks. Each
 * {@code check { }} block creates a new check that is added to the collection.
 * </p>
 * <p>
 * Checks are automatically assigned sequential indices (0, 1, 2, ...) based on their order of definition. This index is
 * used for error reporting.
 * </p>
 *
 * @see Check
 * @see CheckBuilder
 * @see RuleBuilder
 */
public class ChecksBuilder
{
    /** Accumulated list of checks built from nested check {} blocks */
    private final List<Check> checks = new ArrayList<>();

    /** Counter for assigning sequential indices to checks */
    private int nextIndex = 0;

    /**
     * Defines a single check within this checks section.
     * <p>
     * Creates a new {@link CheckBuilder}, assigns it a sequential index, and applies the closure to configure the check
     * properties.
     * </p>
     *
     * @param closure
     *                    the closure defining check properties using {@link CheckBuilder}
     * @return the builder for method chaining (though typically not used in DSL)
     */
    public CheckBuilder check(@DelegatesTo(CheckBuilder.class) Closure<?> closure)
    {
        CheckBuilder builder = new CheckBuilder();
        // Assign index before delegating so the closure can access it if needed
        builder.index(nextIndex++);
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        checks.add(builder.build());
        return builder;
    }

    /**
     * Builds and returns all configured checks as an array.
     *
     * @return array of Check instances in definition order
     */
    public Check[] build()
    {
        return checks.toArray(new Check[0]);
    }
}
