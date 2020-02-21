package com.xceptance.xlt.engine;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Script;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

/**
 * An {@link XltScript} wraps an existing {@link Script} instance and holds additional information.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltScript implements Script
{
    /**
     * The wrapped script.
     */
    private final Script script;

    /**
     * The source name associated with the wrapped script.
     */
    private final String sourceName;

    /**
     * Constructor.
     * 
     * @param script
     *            the wrapped script
     * @param sourceName
     *            the source name associated with the wrapped script
     */
    public XltScript(final Script script, final String sourceName)
    {
        this.script = script;
        this.sourceName = sourceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object exec(final Context context, final Scriptable scriptable)
    {
        return script.exec(context, scriptable);
    }

    /**
     * Returns the wrapped script.
     * 
     * @return the wrapped script
     */
    public Script getWrappedScript()
    {
        return script;
    }

    /**
     * Returns the source name associated with the wrapped script.
     * 
     * @return the source name
     */
    public String getSourceName()
    {
        return sourceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return script.toString();
    }
}
