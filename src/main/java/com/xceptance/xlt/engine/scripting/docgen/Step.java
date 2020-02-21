/*
 * File: Step.java
 * Created on: Nov 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting.docgen;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * Information about a test step.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("step")
public class Step
{
    final String name;

    private String description;

    @XStreamAsAttribute
    final boolean disabled;

    final boolean moduleCall;

    final Condition condition;

    Step(final String name, final String description, final boolean disabled, final boolean isModuleCall, final boolean conditionDisabled,
         final String conditionExpression)
    {
        this.name = name;
        this.description = description;
        this.disabled = disabled;
        this.moduleCall = isModuleCall;
        this.condition = conditionExpression == null ? null : new Condition(conditionDisabled, conditionExpression);
    }

    Step(final String name, final String description, final boolean disabled)
    {
        this(name, description, disabled, false, false, null);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    private transient String descriptionMarkup;

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the descriptionMarkup
     */
    public String getDescriptionMarkup()
    {
        if (descriptionMarkup == null)
        {
            descriptionMarkup = Marked.getInstance().markdownToHTML(getDescription());
        }
        return descriptionMarkup;

    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public boolean isModuleCall()
    {
        return moduleCall;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
        this.descriptionMarkup = null;
    }

    public Condition getCondition()
    {
        return condition;
    }

    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    public static class Condition
    {
        @XStreamAsAttribute
        final boolean disabled;

        final String expression;

        public Condition(final boolean isDisabled, final String cExpression)
        {
            disabled = isDisabled;
            expression = cExpression;
        }

        public boolean isDisabled()
        {
            return disabled;
        }

        /**
         * @return the expression
         */
        public String getExpression()
        {
            return expression;
        }
    }
}
