package com.xceptance.xlt.engine.scripting;

/**
 * The base class for the elements of a script file.
 */
public class ScriptElement
{
    /**
     * The name of the script element.
     */
    private final String name;

    /**
     * The name of the script element.
     */
    private final boolean disabled;

    private final int lineNumber;

    /**
     * Constructor.
     * 
     * @param name
     *            the element's name
     * @param disabled
     *            whether this script element is disabled (i.e. commented out)
     */
    public ScriptElement(final String name, final boolean disabled, final int lineNumber)
    {
        this.name = name;
        this.disabled = disabled;
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the value of the 'name' attribute.
     * 
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns whether this script element is disabled (commented out).
     * 
     * @return <code>true</code> if disabled, <code>false</code> otherwise
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Returns the element's line number.
     * 
     * @return the element's line number
     */
    public int getLineNumber()
    {
        return lineNumber;
    }
}
