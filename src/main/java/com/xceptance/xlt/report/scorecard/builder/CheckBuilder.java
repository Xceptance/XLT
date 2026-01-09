package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;

/**
 * Builder for {@link Check}.
 */
public class CheckBuilder
{
    private String selector;

    private String selectorId;

    private String condition;

    private boolean enabled = true;

    private boolean displayValue = true;

    private int index = 0;

    public void selector(String selector)
    {
        this.selector = selector;
    }

    public void selectorId(String selectorId)
    {
        this.selectorId = selectorId;
    }

    public void condition(String condition)
    {
        this.condition = condition;
    }

    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void displayValue(boolean displayValue)
    {
        this.displayValue = displayValue;
    }

    void index(int index)
    {
        this.index = index;
    }

    public Check build()
    {
        return new Check(index, selector, selectorId, condition, enabled, displayValue);
    }
}
