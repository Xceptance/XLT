package com.xceptance.xlt.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.DefaultJavaScriptExecutor;
import com.xceptance.xlt.api.engine.Session;

/**
 * Specialization of {@link DefaultJavaScriptExecutor} that renames the executing thread to the session's user ID.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltJavaScriptExecutor extends DefaultJavaScriptExecutor
{
    /**
     * @param webClient
     */
    public XltJavaScriptExecutor(final WebClient webClient)
    {
        super(webClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getThreadName()
    {
        return Session.getCurrent().getUserID() + "-JS";
    }
}
