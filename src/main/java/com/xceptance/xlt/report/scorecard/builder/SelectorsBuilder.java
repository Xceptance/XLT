package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.SelectorDefinition;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for a list of selectors.
 */
public class SelectorsBuilder
{
    private final Configuration config;

    public SelectorsBuilder(Configuration config)
    {
        this.config = config;
    }

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
