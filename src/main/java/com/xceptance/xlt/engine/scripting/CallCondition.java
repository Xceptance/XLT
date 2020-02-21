/*
 * File: CallCondition.java
 * Created on: Dec 15, 2015
 * 
 * Copyright 2015
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting;

/**
 * Condition for module calls.
 */
public class CallCondition
{
    private final boolean disabled;

    private final String conditionExpression;

    public CallCondition(final boolean disabled, final String conditionExpression)
    {
        this.disabled = disabled;
        this.conditionExpression = conditionExpression;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public String getConditionExpression()
    {
        return conditionExpression;
    }
}
