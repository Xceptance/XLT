package com.xceptance.xlt.engine.scripting.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Base class of all conditions used in command adapters (e.g. element present, text matches etc).
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class Condition
{
    /**
     * Description of this condition.
     */
    private final String description;

    /**
     * Whether or not this condition has been satisfied.
     */
    private boolean _satisfied;

    /**
     * Reason why condition is (not) satisfied.
     */
    private String _reason;

    /**
     * Creates a new condition.
     * 
     * @param aDescription
     *            short description of this condition
     */
    public Condition(final String aDescription)
    {
        this.description = aDescription;
    }

    /**
     * Creates a new condition.
     * 
     * @param messageFormat
     *            format of the description
     * @param args
     *            arguments for description format
     */
    public Condition(final String messageFormat, final Object... args)
    {
        this(String.format(messageFormat, args));
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "Condition [".concat(description).concat("]");
    }

    /**
     * Returns the description of this condition.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the reason for the satisfaction state of this condition.
     * 
     * @return the reason
     */
    public String getReason()
    {
        return StringUtils.defaultString(_reason, this + (_satisfied ? "" : "not") + " satisfied");
    }

    /**
     * Evaluates the condition and returns whether or not it is satisfied. Any exception thrown while evaluating the
     * condition will cause it to be not satisfied with the exception's detail message as reason.
     * 
     * @return <code>true</code> if this condition is satisfied, <code>false</code> otherwise
     */
    public final boolean isTrue()
    {
        _satisfied = false;
        _reason = null;
        try
        {
            _satisfied = evaluate();
        }
        catch (final Throwable t)
        {
            _reason = t.getMessage();
            throw t;
        }

        return _satisfied;
    }

    /**
     * Evaluates the condition and returns the outcome.
     * 
     * @return outcome of evaluation
     */
    protected abstract boolean evaluate();

    /**
     * Sets the reason.
     * 
     * @param aReason
     *            the reason to set
     */
    protected void setReason(final String aReason)
    {
        _reason = aReason;
    }
}
