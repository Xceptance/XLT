/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Dedicated unit tests for {@link GroovyPropertyEvaluator}. Tests the expression parser, evaluator, script cache, and
 * edge cases directly, without going through {@link PropertiesUtils}.
 *
 * @author Xceptance Software Technologies GmbH
 */
public class GroovyPropertyEvaluatorTest
{
    private Properties props;

    private Map<String, Object> ctx;

    @Before
    public void setUp()
    {
        props = new Properties();
        ctx = new ConcurrentHashMap<>();
        GroovyPropertyEvaluator.clearCache();
    }

    @After
    public void tearDown()
    {
        GroovyPropertyEvaluator.clearCache();
    }

    // =========================================================================
    // Input handling
    // =========================================================================

    /**
     * Null input should pass through unchanged.
     */
    @Test
    public void nullInput()
    {
        Assert.assertNull(GroovyPropertyEvaluator.evaluateGroovyExpressions(null, props, ctx));
    }

    /**
     * Empty string should pass through unchanged.
     */
    @Test
    public void emptyInput()
    {
        Assert.assertEquals("", GroovyPropertyEvaluator.evaluateGroovyExpressions("", props, ctx));
    }

    /**
     * String without any #{} markers should pass through unchanged.
     */
    @Test
    public void noMarkers()
    {
        Assert.assertEquals("plain text", GroovyPropertyEvaluator.evaluateGroovyExpressions("plain text", props, ctx));
    }

    /**
     * String containing just a hash but no opening brace should pass through.
     */
    @Test
    public void hashWithoutBrace()
    {
        Assert.assertEquals("# not groovy", GroovyPropertyEvaluator.evaluateGroovyExpressions("# not groovy", props, ctx));
    }

    // =========================================================================
    // Expression parsing edge cases
    // =========================================================================

    /**
     * Empty expression #{} should evaluate to empty string (Groovy returns null for empty script).
     */
    @Test
    public void emptyExpression()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ }", props, ctx);
        Assert.assertEquals("", result);
    }

    /**
     * Unclosed expression #{... without matching } should be treated as literal text (fail-soft behavior).
     */
    @Test
    public void unclosedExpression()
    {
        final String input = "prefix #{ 1 + 1";
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(input, props, ctx);
        Assert.assertEquals(input, result);
    }

    /**
     * Multiple separate expressions in one value should all be evaluated.
     */
    @Test
    public void multipleExpressions()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions("a=#{ 1 + 1 } b=#{ 2 + 2 }", props, ctx);
        Assert.assertEquals("a=2 b=4", result);
    }

    /**
     * Adjacent expressions without spacing between them.
     */
    @Test
    public void adjacentExpressions()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ 1 }#{ 2 }#{ 3 }", props, ctx);
        Assert.assertEquals("123", result);
    }

    /**
     * Expression result is the string representation of whatever Groovy returns.
     */
    @Test
    public void nonStringResult()
    {
        // List
        final String listResult = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ [1, 2, 3] }", props, ctx);
        Assert.assertEquals("[1, 2, 3]", listResult);

        // Map — Groovy map literal [a:1] creates a LinkedHashMap, whose toString() uses Java format {a=1}
        final String mapResult = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ [a:1] }", props, ctx);
        Assert.assertEquals("{a=1}", mapResult);
    }

    /**
     * Null result from Groovy should be replaced with empty string.
     */
    @Test
    public void nullResult()
    {
        Assert.assertEquals("", GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ null }", props, ctx));
    }

    // =========================================================================
    // Balanced brace parsing
    // =========================================================================

    /**
     * Nested braces inside closures should be handled correctly.
     */
    @Test
    public void nestedBracesInClosure()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ [1, 2, 3].collect { it * 2 }.join(',') }", props, ctx);
        Assert.assertEquals("2,4,6", result);
    }

    /**
     * Braces inside single-quoted strings should not affect brace counting.
     */
    @Test
    public void bracesInSingleQuotedString()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ '{not a brace}' }", props, ctx);
        Assert.assertEquals("{not a brace}", result);
    }

    /**
     * Braces inside double-quoted strings should not affect brace counting.
     */
    @Test
    public void bracesInDoubleQuotedString()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ \"{not a brace}\" }", props, ctx);
        Assert.assertEquals("{not a brace}", result);
    }

    /**
     * Escaped quotes inside strings should not break quote tracking.
     */
    @Test
    public void escapedQuotesInString()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ 'it\\'s fine' }", props, ctx);
        Assert.assertEquals("it's fine", result);
    }

    /**
     * Deeply nested braces (closure within closure).
     */
    @Test
    public void deeplyNestedBraces()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ [[1,2],[3,4]].collect { outer -> outer.collect { it * 10 } }.flatten().join(',') }", props, ctx);
        Assert.assertEquals("10,20,30,40", result);
    }

    // =========================================================================
    // Bindings (props and ctx)
    // =========================================================================

    /**
     * props binding provides read-only access to properties via bracket syntax.
     */
    @Test
    public void propsGetAt()
    {
        props.setProperty("myKey", "myValue");
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ props['myKey'] }", props, ctx);
        Assert.assertEquals("myValue", result);
    }

    /**
     * props.getProperty with default value.
     */
    @Test
    public void propsGetPropertyWithDefault()
    {
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ props.getProperty('missing', 'fallback') }", props, ctx);
        Assert.assertEquals("fallback", result);
    }

    /**
     * props.containsKey check.
     */
    @Test
    public void propsContainsKey()
    {
        props.setProperty("exists", "yes");
        Assert.assertEquals("true", GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ props.containsKey('exists') }", props, ctx));
        Assert.assertEquals("false", GroovyPropertyEvaluator.evaluateGroovyExpressions(
            "#{ props.containsKey('missing') }", props, ctx));
    }

    /**
     * ctx allows storing and retrieving values between evaluations.
     */
    @Test
    public void contextSharingBetweenEvaluations()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ ctx['shared'] = 42; '' }", props, ctx);
        final String result = GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ ctx['shared'] * 2 }", props, ctx);
        Assert.assertEquals("84", result);
    }

    // =========================================================================
    // Error handling
    // =========================================================================

    /**
     * Invalid Groovy syntax should throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidSyntaxThrows()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ this is not valid ++ }", props, ctx);
    }

    /**
     * The exception message should contain the original script for debugging.
     */
    @Test
    public void exceptionContainsScript()
    {
        try
        {
            GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ undefined_variable.method() }", props, ctx);
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            Assert.assertTrue("Exception should contain the script text",
                              e.getMessage().contains("undefined_variable.method()"));
        }
    }

    // =========================================================================
    // Script cache
    // =========================================================================

    /**
     * Evaluating the same expression twice should produce the same result (verifies caching doesn't break
     * re-evaluation).
     */
    @Test
    public void cacheProducesConsistentResults()
    {
        final String expr = "#{ 7 * 6 }";
        Assert.assertEquals("42", GroovyPropertyEvaluator.evaluateGroovyExpressions(expr, props, ctx));
        Assert.assertEquals("42", GroovyPropertyEvaluator.evaluateGroovyExpressions(expr, props, ctx));
    }

    /**
     * Clearing the cache should not affect the ability to re-evaluate expressions.
     */
    @Test
    public void clearCacheAllowsReEvaluation()
    {
        Assert.assertEquals("42", GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ 42 }", props, ctx));
        GroovyPropertyEvaluator.clearCache();
        Assert.assertEquals("42", GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ 42 }", props, ctx));
    }

    /**
     * Cached scripts should work correctly with different bindings (different props/ctx).
     */
    @Test
    public void cachedScriptWithDifferentBindings()
    {
        props.setProperty("x", "10");
        Assert.assertEquals("10", GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ props['x'] }", props, ctx));

        // Change the property value and re-evaluate — should see the new value
        props.setProperty("x", "20");
        Assert.assertEquals("20", GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ props['x'] }", props, ctx));
    }

    // =========================================================================
    // Security sandbox
    // =========================================================================

    /**
     * File system access via java.io.File should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksFileAccess()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new java.io.File('/etc/passwd') }", props, ctx);
    }

    /**
     * Network access via java.net.URL should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksUrlAccess()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new java.net.URL('http://evil.com') }", props, ctx);
    }

    /**
     * Network access via java.net.Socket should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksSocketAccess()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new java.net.Socket('localhost', 80) }", props, ctx);
    }

    /**
     * Runtime.exec should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksRuntimeExec()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ Runtime.getRuntime().exec('ls') }", props, ctx);
    }

    /**
     * System.exit should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksSystemExit()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ System.exit(0) }", props, ctx);
    }

    /**
     * System.setProperty should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksSystemSetProperty()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ System.setProperty('hack', 'true') }", props, ctx);
    }

    /**
     * ProcessBuilder should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksProcessBuilder()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new ProcessBuilder('ls').start() }", props, ctx);
    }

    /**
     * Thread creation should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksThreadCreation()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new Thread({ println 'hi' }).start() }", props, ctx);
    }

    /**
     * Groovy GDK String.execute() — the well-known sandbox escape. This calls Runtime.exec() internally via Groovy's
     * runtime category methods. The SecureASTCustomizer may or may not catch this at the AST level since it's a GDK
     * method on String, not a direct receiver call. This test documents the expected behavior.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksGdkStringExecute()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ 'ls'.execute() }", props, ctx);
    }

    /**
     * Class.forName reflection should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksClassForName()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ Class.forName('java.lang.Runtime') }", props, ctx);
    }

    /**
     * GroovyShell inception (creating a new GroovyShell inside a script) should be blocked.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksGroovyShellInception()
    {
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ new groovy.lang.GroovyShell().evaluate('1+1') }", props, ctx);
    }

    /**
     * Attempting to write to the props binding should fail — ReadOnlyProperties does not expose putAt or setProperty
     * with write semantics. Groovy's dynamic dispatch will throw a MissingMethodException or similar.
     */
    @Test(expected = IllegalArgumentException.class)
    public void securityBlocksPropsMutation()
    {
        props.setProperty("original", "safe");
        GroovyPropertyEvaluator.evaluateGroovyExpressions("#{ props['injected'] = 'hacked'; '' }", props, ctx);
    }
}
