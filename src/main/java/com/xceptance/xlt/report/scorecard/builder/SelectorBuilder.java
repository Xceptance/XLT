package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.SelectorDefinition;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for {@link SelectorDefinition}.
 */
public class SelectorBuilder
{
    private String id;

    private String expression;

    private String comment;

    public SelectorBuilder id(String id)
    {
        this.id = id;
        return this;
    }

    public SelectorBuilder expression(String expression)
    {
        this.expression = expression;
        return this;
    }

    public SelectorBuilder comment(String comment)
    {
        this.comment = comment;
        return this;
    }

    public SelectorDefinition build()
    {
        // SelectorDefinition constructor is currently package-private,
        // we might need to adjust it or place this builder in the same package temporarily if we want to avoid changing
        // visibility yet.
        // Ideally, we move builders to the same package or make constructor public.
        // For now, assuming we will fix visibility or move package.
        // Let's go with the separate package as planned and we will fix visibility in SelectorDefinition separately.
        return new SelectorDefinition(id, expression);
    }
}
