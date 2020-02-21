package com.xceptance.xlt.engine.scripting;

/**
 * Represents an action read from a script file.
 */
public class Action extends ScriptElement
{
    /**
     * Constructor.
     * 
     * @param name
     *            the name
     */
    public Action(final String name, final int lineNumber)
    {
        this(name, false, lineNumber);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            the name
     * @param disabled
     *            whether or not this action is disabled
     */
    public Action(final String name, final boolean disabled, final int lineNumber)
    {
        super(name, disabled, lineNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("action %s", getName());
    }
}
