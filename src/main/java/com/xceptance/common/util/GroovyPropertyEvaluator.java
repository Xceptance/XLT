/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Utility class for evaluating Groovy expressions embedded in property values.
 * <p>
 * Supports the Spring-like syntax {@code #{...}} for embedding Groovy code that will be evaluated at property
 * resolution time. Multi-line scripts are supported.
 * </p>
 * <p>
 * Scripts have access to:
 * <ul>
 * <li>{@code props} - Read-only access to property values</li>
 * <li>{@code ctx} - Shared Map for storing data between script evaluations</li>
 * </ul>
 * </p>
 * Example usage in properties:
 * 
 * <pre>
 * totalUsers = 100
 * browse.users = #{ (props['totalUsers'] as int) * 0.4 }
 * 
 * # Multi-line with context sharing
 * setup = #{
 *     ctx['base'] = props['totalUsers'] as int
 *     'configured'
 * }
 * search.users = #{ ctx['base'] - 10 }
 * </pre>
 *
 * @author Xceptance Software Technologies GmbH
 * @since 8.0.0
 */
public class GroovyPropertyEvaluator
{
    /**
     * Pattern to match Groovy expressions in the format #{...} Uses DOTALL flag to support multi-line scripts.
     */
    private static final Pattern GROOVY_PATTERN = Pattern.compile("#\\{(.+?)\\}", Pattern.DOTALL);

    /**
     * Cache for compiled scripts to improve performance. Thread-safe via ConcurrentHashMap.
     */
    private static final Map<String, Script> SCRIPT_CACHE = new ConcurrentHashMap<>();

    /**
     * Shared GroovyShell with security customizations.
     */
    private static final GroovyShell SHELL;

    static
    {
        final CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(PropertyGroovySecurityUtils.createSecureCustomizer());
        SHELL = new GroovyShell(config);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private GroovyPropertyEvaluator()
    {
    }

    /**
     * Evaluates any Groovy expressions in the given value string.
     * <p>
     * Groovy expressions are marked with {@code #{...}} syntax and can span multiple lines. The result of each expression
     * replaces the entire {@code #{...}} block.
     * </p>
     *
     * @param value
     *                  the string potentially containing Groovy expressions
     * @param props
     *                  properties available to scripts via 'props' binding (read-only)
     * @param ctx
     *                  shared context map available to scripts via 'ctx' binding
     * @return the value with all Groovy expressions evaluated and replaced
     * @throws IllegalArgumentException
     *                                      if a Groovy expression cannot be evaluated
     */
    public static String evaluateGroovyExpressions(final String value, final Properties props, final Map<String, Object> ctx)
    {
        if (value == null || value.isEmpty() || !value.contains("#{"))
        {
            return value;
        }

        final Matcher matcher = GROOVY_PATTERN.matcher(value);
        final StringBuffer result = new StringBuffer();

        while (matcher.find())
        {
            final String script = matcher.group(1).trim();
            final String evaluated = evaluateSingleExpression(script, props, ctx);
            matcher.appendReplacement(result, Matcher.quoteReplacement(evaluated));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Evaluates a single Groovy script expression.
     *
     * @param script
     *                   the Groovy script to evaluate
     * @param props
     *                   properties available via 'props' binding
     * @param ctx
     *                   shared context map available via 'ctx' binding
     * @return the string representation of the script result
     * @throws IllegalArgumentException
     *                                      if the script cannot be evaluated
     */
    private static String evaluateSingleExpression(final String script, final Properties props, final Map<String, Object> ctx)
    {
        try
        {
            // Create bindings for this evaluation
            final Binding binding = new Binding();
            binding.setVariable("props", new ReadOnlyProperties(props));
            binding.setVariable("ctx", ctx);

            // Get or compile the script
            Script compiledScript = SCRIPT_CACHE.get(script);
            if (compiledScript == null)
            {
                synchronized (SHELL)
                {
                    compiledScript = SHELL.parse(script);
                    SCRIPT_CACHE.put(script, compiledScript);
                }
            }

            // Execute with current bindings
            final Script scriptInstance;
            synchronized (compiledScript)
            {
                // Clone the script for thread safety
                scriptInstance = compiledScript.getClass().getDeclaredConstructor().newInstance();
            }
            scriptInstance.setBinding(binding);

            final Object result = scriptInstance.run();
            return result == null ? "" : result.toString();
        }
        catch (final Exception e)
        {
            throw new IllegalArgumentException(String.format("Failed to evaluate Groovy expression: #{%s} - %s", script, e.getMessage()),
                                               e);
        }
    }

    /**
     * Clears the script cache. Useful for testing or when properties change significantly.
     */
    public static void clearCache()
    {
        SCRIPT_CACHE.clear();
    }

    /**
     * Read-only wrapper for Properties to prevent scripts from modifying properties.
     */
    private static class ReadOnlyProperties
    {
        private final Properties props;

        public ReadOnlyProperties(final Properties props)
        {
            this.props = props;
        }

        /**
         * Get a property value by key.
         * 
         * @param key
         *                the property key
         * @return the property value or null
         */
        public String getAt(final String key)
        {
            return props.getProperty(key);
        }

        /**
         * Get a property value by key (alternative method name).
         * 
         * @param key
         *                the property key
         * @return the property value or null
         */
        public String getProperty(final String key)
        {
            return props.getProperty(key);
        }

        /**
         * Get a property value with default.
         * 
         * @param key
         *                         the property key
         * @param defaultValue
         *                         the default value if key not found
         * @return the property value or default
         */
        public String getProperty(final String key, final String defaultValue)
        {
            return props.getProperty(key, defaultValue);
        }

        /**
         * Check if a property exists.
         * 
         * @param key
         *                the property key
         * @return true if the property exists
         */
        public boolean containsKey(final String key)
        {
            return props.containsKey(key);
        }
    }
}
