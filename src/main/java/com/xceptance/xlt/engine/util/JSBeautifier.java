/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS Beautifier wrapper using Rhino.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class JSBeautifier
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JSBeautifier.class);

    /**
     * Content of file 'beautify.js'.
     */
    private final String js_beautify;

    /**
     * Static options map used by beautifier.
     */
    private static final Map<String, Object> OPTIONS = _createOptionMap();

    /**
     * Creates a new beautifier instance.
     * 
     * @throws IOException
     *             thrown when reading content of file 'beautify.js' has failed
     */
    private JSBeautifier()
    {
        String content = null;

        try (final InputStream is = getClass().getResourceAsStream("beautify.js"))
        {
            if (is != null)
            {
                content = IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        }
        catch (final IOException ioe)
        {
            LOG.error("Failed to read content of file 'beautify.js'", ioe);
        }

        js_beautify = content;
    }

    /**
     * Creates and returns the beautifier options map.
     * 
     * @return options map
     */
    private static Map<String, Object> _createOptionMap()
    {
        final HashMap<String, Object> opts = new HashMap<String, Object>();
        opts.put("indent_size", "2");
        opts.put("ident_char", " ");
        opts.put("preserve_newlines", true);
        opts.put("indent_level", 0);
        opts.put("space_after_anon_function", true);
        opts.put("brace_style", "expand");

        return opts;
    }

    /**
     * Beautifies the given JavaScript input.
     * 
     * @param inputJS
     *            the JavaScript code to beautify
     * @return beautified input
     */
    public static String beautify(final String inputJS)
    {
        return Singleton_Holder._INSTANCE.doBeautify(inputJS);
    }

    /**
     * Beautifies the given input and does the real work.
     * 
     * @param input
     *            the input to beautify
     * @return beautified input
     */
    private String doBeautify(final String input)
    {
        try
        {
            final Context ctx = Context.enter();
            // enforce null debugger to prevent generation of debug information
            ctx.setDebugger(null, null);
            // set JavaScript language version
            ctx.setLanguageVersion(Context.VERSION_1_6);

            // make sure that all js-native objects are defined (as Array, String etc.)
            final Scriptable scope = ctx.initStandardObjects();
            // create 'global namespace'
            scope.put("global", scope, ctx.newObject(scope));
            // ... and evaluate js-beautify code
            ctx.evaluateString(scope, js_beautify, "JSBeautify", 1, null);

            // build 'options' object
            final Scriptable options = ctx.newObject(scope);
            for (final String key : OPTIONS.keySet())
            {
                options.put(key, options, OPTIONS.get(key));
            }

            // put objects into scope that are referenced as parameter in method call below
            scope.put("opts", scope, options);
            scope.put("input", scope, input);

            // now, beautify the input by calling the function 'js_beautify'
            ctx.evaluateString(scope, "var result = global.js_beautify(input, opts);", "JSBeautify", 1, null);

            // get the result
            final Object result = scope.get("result", scope);
            // ... and return it as string
            return result.toString();
        }
        finally
        {
            Context.exit();
        }
    }

    /**
     * Singleton holder. Implementation based on the
     * <a href="http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder
     * idiom</a>.
     */
    private static final class Singleton_Holder
    {
        private static final JSBeautifier _INSTANCE = new JSBeautifier();
    }
}
