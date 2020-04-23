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
package com.xceptance.xlt.engine.scripting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.RegExUtils;

/**
 * Scope for resolution of variables used by certain commands.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class VariableScope
{
    /** Pattern used to check variable names for validity. */
    private static final Pattern VAR_NAME_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    /** Pattern used to detect quoted dollar-signs. */
    private static final Pattern QUOTE_DOLLARSIGN_PATTERN = Pattern.compile("\\$\\$");

    /** Pattern used to resolve variable expressions. */
    private static final Pattern VAR_EXPR_PATTERN = Pattern.compile("(?<!\\$)\\$\\{[^\\s{}$]+\\}");

    /** Pattern used to resolve macro expressions. */
    private static final Pattern MACRO_EXPR_PATTERN = Pattern.compile("\\$\\$|\\$\\{[^\\s${}]+\\}");

    /**
     * Maximum number of variable/macro resolution rounds.
     */
    private static final int MAX_ITERATIONS = 1000;

    /**
     * The enclosing scope.
     */
    private final VariableScope _enclosingScope;

    /**
     * Test data.
     */
    private final Map<String, String> _testData;

    /**
     * Creates a new scope enclosed in the given one.
     * 
     * @param enlosingScope
     *            the enclosing scope (may be null)
     */
    VariableScope(final Map<String, String> testData, final VariableScope enlosingScope)
    {
        _enclosingScope = enlosingScope;
        _testData = testData != null ? testData : Collections.<String, String>emptyMap();
    }

    /**
     * Creates a new top-level scope.
     */
    VariableScope(final Map<String, String> testData)
    {
        this(testData, null);
    }

    /**
     * Returns the enclosing scope.
     */
    VariableScope getEnclosingScope()
    {
        return _enclosingScope;
    }

    /**
     * Returns whether or not this scope is enclosed by another scope.
     * 
     * @return <code>true</code> if this scope is enclosed by another scope, <code>false</code> otherwise
     */
    boolean hasEnclosingScope()
    {
        return _enclosingScope != null;
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the string to be resolved
     * @return the resolved string
     */
    String resolve(final String resolvable)
    {
        if (StringUtils.isBlank(resolvable))
        {
            return resolvable;
        }

        final String varResolved = resolveRecursively(resolvable, new HashSet<String>());
        return resolveMacros(varResolved);
    }

    /**
     * Resolves the given test data key
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    String resolveKey(final String key)
    {
        final String resolveKey;
        if (StringUtils.isBlank(key))
        {
            return key;
        }
        if (RegExUtils.isMatching(key, VAR_EXPR_PATTERN))
        {
            resolveKey = key;
        }
        else
        {
            resolveKey = "${" + key + "}";
        }
        String resolved = resolve(resolveKey);
        if (resolved.equals(resolveKey))
        {
            return null;
        }
        else
        {
            return resolved;
        }
    }

    /**
     * Inserts a new test data mapping where the given key is mapped to the given value.
     * 
     * @param key
     *            the test data variable
     * @param value
     *            the value of the variable
     */
    void storeValue(final String key, final String value)
    {
        if (!isValidVariable(key))
        {
            throw new ScriptException("Invalid variable name '" + key + "'");
        }
        if (value != null)
        {
            synchronized (this)
            {
                _testData.put(key, value);
            }
        }
    }

    /**
     * Resolves the given variable.
     * 
     * @param variable
     *            the test data variable to resolve
     * @return the value of the given test data variable or <code>null</code> if there is no such variable known
     */
    private String resolveVariable(final String variable)
    {
        String value = null;
        if (_enclosingScope != null)
        {
            value = _enclosingScope.resolveVariable(variable);
        }

        if (value == null)
        {
            value = _testData.get(variable);
        }

        return value;
    }

    /**
     * Resolves the given string recursively.
     * 
     * @param value
     *            the value to resolve
     * @param hashSet
     *            set of all variables already resolved
     * @return resolved string
     */
    private String resolveRecursively(final String value, final HashSet<String> hashSet)
    {
        String s = value;
        if (StringUtils.isNotBlank(s))
        {
            int iterations = 0;
            while (true)
            {
                if (iterations++ > MAX_ITERATIONS)
                {
                    throw new ScriptException("Failed to resolve '" + value + "'");
                }
                final String input = s;
                final MacroProcessor macroProc = MacroProcessor.getInstance();
                for (final String match : RegExUtils.getAllMatches(s, VAR_EXPR_PATTERN))
                {
                    final String exp = match.substring(2, match.length() - 1);
                    if (isValidVariable(exp) && !hashSet.contains(exp) && !macroProc.isMacro(exp))
                    {
                        hashSet.add(exp);

                        final String resolved = resolveVariable(exp);
                        if (resolved != null)
                        {
                            final String recResolved = resolveRecursively(resolved, new HashSet<String>(hashSet));
                            s = RegExUtils.replaceAll(s, "(?<!\\$)" + RegExUtils.escape(match), Matcher.quoteReplacement(recResolved));
                        }
                    }
                }

                if (s.equals(input))
                {
                    break;
                }
            }
        }

        return s;
    }

    private static String resolveMacros(final String value)
    {
        String s = value;
        if (!StringUtils.isEmpty(s))
        {
            final MacroProcessor macroProc = MacroProcessor.getInstance();
            int iterations = 0;
            while (true)
            {
                if (iterations++ > MAX_ITERATIONS)
                {
                    throw new ScriptException("Failed to resolve macros for '" + value + "'");
                }

                final String input = s;
                final StringBuilder result = new StringBuilder(s);

                final Matcher m = MACRO_EXPR_PATTERN.matcher(s);
                int offset = 0;

                while (m.find())
                {
                    final String match = m.group();
                    String replacemement = match;
                    if (match.length() == 2 && match.charAt(0) == match.charAt(1))
                    {
                        continue;
                    }
                    else
                    {
                        final String variable = match.substring(2, match.length() - 1);
                        if (macroProc.isMacro(variable))
                        {
                            replacemement = macroProc.executeMacro(variable);
                        }
                    }

                    result.replace(m.start() + offset, m.end() + offset, replacemement);
                    offset += replacemement.length() - match.length();
                }

                s = result.toString();
                if (s.equals(input))
                {
                    break;
                }
            }
        }

        return unescape(s);
    }

    private static boolean isValidVariable(final String variable)
    {
        return RegExUtils.isMatching(variable, VAR_NAME_PATTERN);
    }

    private static String unescape(final String value)
    {
        String s = value;
        if (!StringUtils.isEmpty(s))
        {
            final StringBuilder result = new StringBuilder(s);
            final Matcher m = QUOTE_DOLLARSIGN_PATTERN.matcher(s);
            int offset = 0;
            while (m.find())
            {
                result.replace(m.start() + offset, m.end() + offset, "$");
                --offset;
            }

            s = result.toString();
        }

        return s;
    }
}
