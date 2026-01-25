package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Main DSL entry point for {@link Configuration}.
 */
public class ScorecardBuilder
{
    private final Configuration config = new Configuration();

    private final SelectorsBuilder selectorsBuilder = new SelectorsBuilder(config);

    private final RulesBuilder rulesBuilder = new RulesBuilder(config);

    private final GroupsBuilder groupsBuilder = new GroupsBuilder(config);

    private final RatingsBuilder ratingsBuilder = new RatingsBuilder(config);

    public void version(int version)
    {
        // Just consume it, config handles version implicitly essentially or we can add it if Configuration supports it
    }

    public void selectors(@DelegatesTo(SelectorsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(selectorsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public void rules(@DelegatesTo(RulesBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(rulesBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public void groups(@DelegatesTo(GroupsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(groupsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public void ratings(@DelegatesTo(RatingsBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(ratingsBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public Configuration build()
    {
        return config;
    }
}
