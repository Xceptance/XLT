/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.debug.Debugger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;

/**
 * The {@link XltContextFactory} allows for setting the optimization level to use when compiling JS snippets.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltContextFactory extends HtmlUnitContextFactory
{
    /**
     * The optimization level to use when compiling JS snippets.
     */
    private final int optimizationLevel;

    /**
     * Constructor.
     * 
     * @param webClient
     *            the web client
     * @param optimizationLevel
     *            the optimization level to use when compiling JS snippets
     */
    public XltContextFactory(final WebClient webClient, final int optimizationLevel)
    {
        super(webClient);

        this.optimizationLevel = optimizationLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Context makeContext()
    {
        final Context context = super.makeContext();

        context.setOptimizationLevel(optimizationLevel);

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDebugger(final Debugger debugger)
    {
        // debugger is supported in interpreted mode only
        if (optimizationLevel == -1)
        {
            super.setDebugger(debugger);
        }
    }
}
