package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;
import com.xceptance.xlt.report.scorecard.Status;

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

    private String formatter;

    private Status manualStatus;

    private String manualValue;

    private String manualErrorMessage;

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

    public void formatter(String formatter)
    {
        this.formatter = formatter;
    }

    public void status(String status)
    {
        this.manualStatus = Status.valueOf(status.toUpperCase());
    }

    public void value(String value)
    {
        this.manualValue = value;
    }

    public void message(String message)
    {
        this.manualErrorMessage = message;
    }

    void index(int index)
    {
        this.index = index;
    }

    public Check build()
    {
        return new Check(index, selector, selectorId, condition, enabled, displayValue, formatter, manualStatus, manualValue,
                         manualErrorMessage);
    }
}
