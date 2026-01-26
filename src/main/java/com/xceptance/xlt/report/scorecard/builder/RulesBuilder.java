package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.RuleDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link RuleDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code rules} section to define validation rules. Each
 * {@code rule { }} block creates a new rule that is immediately registered with the {@link Configuration}.
 * </p>
 * <p>
 * Rules defined here can later be referenced by groups using their IDs.
 * </p>
 *
 * @see RuleDefinition
 * @see RuleBuilder
 * @see ScorecardBuilder
 */
public class RulesBuilder
{
    /** Configuration to register rules with */
    private final Configuration config;

    /**
     * Creates a new RulesBuilder that adds rules to the given configuration.
     *
     * @param config
     *                   the configuration to populate with rules
     */
    public RulesBuilder(Configuration config)
    {
        this.config = config;
    }

    /**
     * Defines a single rule within this rules section.
     * <p>
     * Creates a new {@link RuleBuilder}, applies the closure to configure the rule, builds it, and registers it with the
     * configuration.
     * </p>
     *
     * @param closure
     *                    the closure defining rule properties using {@link RuleBuilder}
     * @return the builder for potential method chaining
     * @throws RuntimeException
     *                              if the rule cannot be added (e.g., duplicate ID)
     */
    public RuleBuilder rule(@DelegatesTo(RuleBuilder.class) Closure<?> closure)
    {
        RuleBuilder builder = new RuleBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        RuleDefinition rule = builder.build();
        try
        {
            config.addRule(rule);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
