package com.xceptance.xlt.engine.scripting;

/**
 * Represents an inline comment read from a script file.
 */
public class CodeComment extends ScriptElement
{

    /**
     * Constructor.
     * 
     * @param comment
     *            the comment
     * @param lineNumber
     *            the line number
     */
    public CodeComment(final String comment, final int lineNumber)
    {
        super(comment, false, lineNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("comment %s", getName());
    }
}
