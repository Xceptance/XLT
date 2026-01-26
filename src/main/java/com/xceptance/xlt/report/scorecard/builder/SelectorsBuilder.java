package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.SelectorDefinition;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link SelectorDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code selectors} section to define reusable XPath selectors.
 * Each {@code selector { }} block creates a new selector that is immediately registered with the {@link Configuration}.
 * </p>
 *
 * @see SelectorDefinition
 * @see SelectorBuilder
 * @see ScorecardBuilder
 */
public class SelectorsBuilder
{
    /** Configuration to register selectors with */
    private final Configuration config;

    /**
     * Creates a new SelectorsBuilder that adds selectors to the given configuration.
     *
     * @param config
     *                   the configuration to populate with selectors
     */
    public SelectorsBuilder(Configuration config)
    {
        this.config = config;
    }

    /**
     * Defines a single selector within this selectors section.
     * <p>
     * Creates a new {@link SelectorBuilder}, applies the closure to configure the selector, builds it, and registers it
     * with the configuration.
     * </p>
     *
     * @param closure
     *                    the closure defining selector properties using {@link SelectorBuilder}
     * @return the builder for potential method chaining
     * @throws RuntimeException
     *                              if the selector cannot be added (e.g., duplicate ID)
     */
    public SelectorBuilder selector(@DelegatesTo(SelectorBuilder.class) Closure<?> closure)
    {
        SelectorBuilder builder = new SelectorBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        SelectorDefinition selector = builder.build();
        try
        {
            config.addSelector(selector);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
