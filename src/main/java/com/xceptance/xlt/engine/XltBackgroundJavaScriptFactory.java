package com.xceptance.xlt.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptExecutor;

/**
 * Specialization of {@link BackgroundJavaScriptFactory} that uses our implementation of {@link JavaScriptExecutor}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltBackgroundJavaScriptFactory extends BackgroundJavaScriptFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public JavaScriptExecutor createJavaScriptExecutor(final WebClient webClient)
    {
        return new XltJavaScriptExecutor(webClient);
    };
}
