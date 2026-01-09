package com.xceptance.xlt.report.scorecard.builder;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for a list of checks.
 */
public class ChecksBuilder
{
    private final List<Check> checks = new ArrayList<>();

    private int nextIndex = 0;

    public CheckBuilder check(@DelegatesTo(CheckBuilder.class) Closure<?> closure)
    {
        CheckBuilder builder = new CheckBuilder();
        builder.index(nextIndex++);
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        checks.add(builder.build());
        return builder;
    }

    public Check[] build()
    {
        return checks.toArray(new Check[0]);
    }
}
