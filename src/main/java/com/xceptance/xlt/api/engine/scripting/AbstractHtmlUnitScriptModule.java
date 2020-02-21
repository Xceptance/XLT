package com.xceptance.xlt.api.engine.scripting;

import com.xceptance.xlt.engine.scripting.TestContext;

/**
 * Base class of command modules.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlUnitScriptModule
{

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the string to be resolved
     * @return the resolved string
     */
    protected String resolve(final String resolvable)
    {
        return TestContext.getCurrent().resolve(resolvable);
    }

    /**
     * Resolves the given test data key.
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    protected String resolveKey(final String key)
    {
        return TestContext.getCurrent().resolveKey(key);
    }

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    protected boolean evaluatesToTrue(final String jsExpression)
    {
        return TestContext.getCurrent().getAdapter().evaluatesToTrue(jsExpression);
    }

}
