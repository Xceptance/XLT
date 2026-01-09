package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.RuleDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for a list of rules.
 */
public class RulesBuilder
{
    private final Configuration config;

    public RulesBuilder(Configuration config)
    {
        this.config = config;
    }

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
