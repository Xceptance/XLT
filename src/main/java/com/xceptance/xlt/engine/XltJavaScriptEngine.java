/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Session;

import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.NativeFunction;
import net.sourceforge.htmlunit.corejs.javascript.Script;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.debug.DebuggableScript;

/**
 * The {@link XltJavaScriptEngine} class is a specialization of HtmlUnit's {@link JavaScriptEngine}, which allows for
 * setting the optimization level to use when compiling JS snippets. Additionally, it measures the time taken to compile
 * and execute JS code.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltJavaScriptEngine extends JavaScriptEngine
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XltJavaScriptEngine.class);

    /**
     * The context factory to use.
     */
    private final HtmlUnitContextFactory contextFactory;

    /**
     * Whether or not to take measurements.
     */
    private final boolean takeMeasurements;

    static
    {
        BackgroundJavaScriptFactory.setFactory(new XltBackgroundJavaScriptFactory());
    }

    /**
     * Constructor.
     * 
     * @param webClient
     *            the web client
     * @param optimizationLevel
     *            the optimization level to use when compiling JS snippets
     * @param takeMeasurements
     *            whether or not to take measurements
     */
    public XltJavaScriptEngine(final WebClient webClient, final int optimizationLevel, final boolean takeMeasurements)
    {
        super(webClient);

        contextFactory = new XltContextFactory(webClient, optimizationLevel);
        this.takeMeasurements = takeMeasurements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object callFunction(final HtmlPage htmlPage, final Function function, final Scriptable scope, final Scriptable thisObject,
                               final Object[] args)
    {
        final Object result;

        if (takeMeasurements)
        {
            final String functionName = getFunctionName(function);
            final CustomData customData = new CustomData("Executing function " + functionName);
            final long start = System.nanoTime();

            try
            {
                result = super.callFunction(htmlPage, function, scope, thisObject, args);
            }
            finally
            {
                final long runTimeInNS = System.nanoTime() - start;

                customData.setRunTime(runTimeInNS / 1000000L);
                Session.getCurrent().getDataManager().logDataRecord(customData);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Execution of function '%s' took %.2f ms", functionName, runTimeInNS / 1000000.0));
                }
            }
        }
        else
        {
            result = super.callFunction(htmlPage, function, scope, thisObject, args);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Script compile(final HtmlPage htmlPage, final String sourceCode, final String sourceName, final int startLine)
    {
        Script script;

        final Cache cache = getWebClient().getCache();
        if (cache instanceof XltCache)
        {
            final XltCache xltCache = (XltCache) cache;

            script = xltCache.getCachedScript(sourceCode);
            if (script == null)
            {
                final String sourceFileName = getSourceFileName(sourceName, sourceCode);

                script = compileScript(htmlPage, sourceCode, sourceName, startLine, sourceFileName);

                // script can be null
                if (script != null)
                {
                    // wrap the script and attach more information
                    script = new XltScript(script, sourceFileName);

                    // finally put it in the cache
                    xltCache.cache(sourceCode, script);
                }
            }
        }
        else
        {
            // someone messed around with our XltCache so we have to compile the script each time
            final String sourceFileName = getSourceFileName(sourceName, sourceCode);
            script = compileScript(htmlPage, sourceCode, sourceName, startLine, sourceFileName);
        }

        return script;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(final HtmlPage htmlPage, final Script script)
    {
        final Object result;

        if (takeMeasurements)
        {
            final String sourceFileName = getSourceFileName(script);
            final CustomData customData = new CustomData("Executing " + sourceFileName);
            final long start = System.nanoTime();

            try
            {
                result = super.execute(htmlPage, script);
            }
            finally
            {
                final long runTimeInNS = System.nanoTime() - start;

                customData.setRunTime(runTimeInNS / 1000000L);
                Session.getCurrent().getDataManager().logDataRecord(customData);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Execution of script '%s' took %.2f ms", sourceFileName, runTimeInNS / 1000000.0));
                }
            }
        }
        else
        {
            result = super.execute(htmlPage, script);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlUnitContextFactory getContextFactory()
    {
        return contextFactory;
    }

    /**
     * Compiles the given source code to a script.
     * 
     * @param htmlPage
     *            the page that the code will execute within
     * @param sourceCode
     *            the JavaScript code to execute
     * @param sourceName
     *            the name that will be displayed on error conditions
     * @param startLine
     *            the line at which the script source starts
     * @param sourceFileName
     *            the source file name to use for logging
     * @return the generated script
     */
    private Script compileScript(final HtmlPage htmlPage, final String sourceCode, final String sourceName, final int startLine,
                                 final String sourceFileName)
    {
        final Script script;

        if (takeMeasurements)
        {
            final CustomData customData = new CustomData();
            final long start = System.nanoTime();

            try
            {
                script = super.compile(htmlPage, sourceCode, sourceName, startLine);
            }
            catch (final RuntimeException e)
            {
                customData.setFailed(true);
                throw e;
            }
            finally
            {
                final long runTimeInNS = System.nanoTime() - start;

                customData.setName("Compiling " + sourceFileName);
                customData.setRunTime(runTimeInNS / 1000000L);
                Session.getCurrent().getDataManager().logDataRecord(customData);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Compilation of script '%s' took %.2f ms", sourceFileName, runTimeInNS / 1000000.0));
                }
            }
        }
        else
        {
            script = super.compile(htmlPage, sourceCode, sourceName, startLine);
        }

        return script;
    }

    /**
     * Determines the name of the passed function.
     * 
     * @param function
     *            the function
     * @return the function name
     */
    private String getFunctionName(final Function function)
    {
        String functionName;

        if (function instanceof NativeFunction)
        {
            // will come to this point both in interpreted and compiled mode
            functionName = ((NativeFunction) function).getFunctionName();

            // handle anonymous functions
            if (functionName == null || functionName.isEmpty())
            {
                // try to tell one anonymous function from another
                final int hashCode;
                final DebuggableScript debuggableScript = ((NativeFunction) function).getDebuggableView();
                if (debuggableScript == null)
                {
                    // TODO: Is this safe for multiple JVMs?
                    // compiled mode -> get the hash from the name of the generated class
                    hashCode = function.getClass().getName().hashCode();
                }
                else
                {
                    // Using the hash of the source code Would be perfect but is expensive as the source will be
                    // generated each time.
                    // hashCode = function.toString().hashCode();

                    // TODO: Definitely not safe for multiple JVMs!
                    // interpreted mode -> get the hash from the debugging info
                    hashCode = debuggableScript.hashCode();
                }

                // qualify the function name with the hash
                functionName = "anonymous@" + hashCode;
            }
        }
        else
        {
            // last try
            functionName = "unknown@0";
        }

        return functionName;
    }

    /**
     * Determines the name of the JS source file from the passed script object.
     * 
     * @param script
     *            the script
     * @return the source file name
     */
    private String getSourceFileName(final Script script)
    {
        String sourceFileName;

        if (script instanceof XltScript)
        {
            // return the name as given during compilation
            sourceFileName = ((XltScript) script).getSourceName();
        }
        else
        {
            // various fall-backs
            if (script instanceof NativeFunction)
            {
                // will come to this point both in interpreted and compiled mode
                final DebuggableScript debuggableScript = ((NativeFunction) script).getDebuggableView();
                if (debuggableScript == null)
                {
                    // compiled mode -> get the source file name from the name of the generated class
                    sourceFileName = script.getClass().getSimpleName();
                    sourceFileName = StringUtils.substringBeforeLast(sourceFileName, "_");
                }
                else
                {
                    // interpreted mode -> get the source file name from the debugging info
                    sourceFileName = debuggableScript.getSourceName();
                    sourceFileName = StringUtils.substringAfterLast(sourceFileName, "/");
                    sourceFileName = StringUtils.substringBefore(sourceFileName, "?");
                }
            }
            else
            {
                // last try
                sourceFileName = "unknown_script@" + script.toString().hashCode();
            }
        }

        return sourceFileName;
    }

    /**
     * Determines the name of the JS source file from the passed script object.
     * 
     * @param sourceName
     *            the source name
     * @param sourceCode
     * @return the source file name
     */
    private String getSourceFileName(final String sourceName, final String sourceCode)
    {
        final String sourceFileName;

        if (sourceName.equals("injected script"))
        {
            // for injected scripts qualify the source file name with the source's hash
            sourceFileName = sourceName + "@" + sourceCode.hashCode();
        }
        else
        {
            sourceFileName = sourceName;
        }

        return sourceFileName;
    }
}
