package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.GroupDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for a list of groups.
 */
public class GroupsBuilder
{
    private final Configuration config;

    public GroupsBuilder(Configuration config)
    {
        this.config = config;
    }

    public GroupBuilder group(@DelegatesTo(GroupBuilder.class) Closure<?> closure)
    {
        GroupBuilder builder = new GroupBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        GroupDefinition group = builder.build();
        try
        {
            config.addGroup(group);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
