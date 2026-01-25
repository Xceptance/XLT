package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.RatingDefinition;

/**
 * Builder for {@link RatingDefinition}.
 */
public class RatingBuilder
{
    private String id;

    private String name;

    private String description;

    private double value;

    private boolean enabled = true;

    private boolean failsTest = false;

    public void id(String id)
    {
        this.id = id;
    }

    public void name(String name)
    {
        this.name = name;
    }

    public void description(String description)
    {
        this.description = description;
    }

    public void value(double value)
    {
        this.value = value;
    }

    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void failsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    public RatingDefinition build()
    {
        return new RatingDefinition(id, name, description, value, enabled, failsTest);
    }
}
