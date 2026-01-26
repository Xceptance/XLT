package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.GroupDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link GroupDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code groups} section to define rule groups. Each
 * {@code group { }} block creates a new group that is immediately registered with the {@link Configuration}.
 * </p>
 * <p>
 * Groups determine how rules are organized, how points are calculated, and when the test should fail.
 * </p>
 *
 * @see GroupDefinition
 * @see GroupBuilder
 * @see ScorecardBuilder
 */
public class GroupsBuilder
{
    /** Configuration to register groups with */
    private final Configuration config;

    /**
     * Creates a new GroupsBuilder that adds groups to the given configuration.
     *
     * @param config
     *                   the configuration to populate with groups
     */
    public GroupsBuilder(Configuration config)
    {
        this.config = config;
    }

    /**
     * Defines a single group within this groups section.
     * <p>
     * Creates a new {@link GroupBuilder}, applies the closure to configure the group, builds it, and registers it with the
     * configuration.
     * </p>
     *
     * @param closure
     *                    the closure defining group properties using {@link GroupBuilder}
     * @return the builder for potential method chaining
     * @throws RuntimeException
     *                              if the group cannot be added (e.g., duplicate ID)
     */
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
