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

import java.io.File;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test the implementation of {@link PropertiesUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class PropertiesUtilsTest extends AbstractXLTTestCase
{
    /**
     * Properties object for testing purposes.
     */
    private Properties props;

    /**
     * Test fixture setup.
     */
    @Before
    public void init()
    {
        props = new Properties();
        props.setProperty("testKey", "testValue");
        props.setProperty("emptyKey", "");
        props.setProperty("$foo-bar", "jesus");
    }

    /**
     * Test substitution of variables using all three handling cases in one test string.
     */
    @Test
    public void testSubstituteVars()
    {
        // string containing variables for substitution
        final String testString = "The key testKey should be set to ${testKey}. " + "And this is key is empty: [${emptyKey}]. " +
                                  "Additionally, here we have a key containing a special character: ${$foo-bar}." +
                                  "Last but not least, an undefined key: #${undefined}#";
        // expected result string as indicated in documentation of
        // 'substituteVariables'
        final String replacedString = "The key testKey should be set to testValue. " + "And this is key is empty: []. " +
                                      "Additionally, here we have a key containing a special character: jesus." +
                                      "Last but not least, an undefined key: #${undefined}#";

        // result of calling 'substituteVariables'
        final String resultString = PropertiesUtils.substituteVariables(testString, props);

        // validation
        Assert.assertEquals(replacedString, resultString);
    }

    @Test
    public void testSubstituteVars4NestedReferences() throws Throwable
    {
        props.setProperty("aaaa", "${bbb}");
        props.setProperty("bbb", "${cc}");
        props.setProperty("cc", "d");

        Assert.assertEquals("d", PropertiesUtils.substituteVariables("${aaaa}", props));

        // test variable substitution using cyclic lookup path
        props.setProperty("cc", "${aaaa}");

        Assert.assertEquals("${aaaa}", PropertiesUtils.substituteVariables("${aaaa}", props));
    }

    @Test
    public void testSubstituteVars4MultipleReferences() throws Throwable
    {
        props.setProperty("aaaa", "${bbb} ${cc}");
        props.setProperty("bbb", "${cc}");
        props.setProperty("cc", "d");

        Assert.assertEquals("d d", PropertiesUtils.substituteVariables("${aaaa}", props));
    }

    @Test
    public void testSubstituteVarsWithinLiteralText() throws Throwable
    {
        props.setProperty("aaaa", "---${bbb}---");
        props.setProperty("bbb", "${cc}");
        props.setProperty("cc", "d");

        Assert.assertEquals("---d---", PropertiesUtils.substituteVariables("${aaaa}", props));
    }

    /**
     * Tests the implementation of {@link PropertiesUtils#loadProperties(File)} by passing the null reference as file
     * parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadProperties_FileIsNull() throws Exception
    {
        PropertiesUtils.loadProperties((File) null);

    }

    /**
     * Tests the implementation of {@link PropertiesUtils#loadProperties(File)} by passing a valid file but invalid
     * properties as parameters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadProperties_PropertiesIsNull() throws Exception
    {
        PropertiesUtils.loadProperties(new File(TEST_PROPERTY_FILE), null);
    }

    /**
     * Tests the implementation of {@link PropertiesUtils#loadProperties(File)} by passing a valid file as parameter
     */
    @Test
    public void testLoadProperties_ValidFile() throws Exception
    {
        final Properties props = PropertiesUtils.loadProperties(new File(TEST_PROPERTY_FILE));
        Assert.assertNotNull(props);
        Assert.assertFalse(props.isEmpty());
    }

    /**
     * Tests the implementation of {@link PropertiesUtils#loadProperties(File)} by passing a valid file and valid properties
     * as parameters.
     */
    @Test
    public void testLoadProperties_ValidFileValidProperties() throws Exception
    {
        PropertiesUtils.loadProperties(new File(TEST_PROPERTY_FILE), props);
        Assert.assertFalse(props.isEmpty());
        Assert.assertTrue(props.containsKey("testKey"));
        Assert.assertTrue(props.containsKey("emptyKey"));
    }

    // =========================================================================
    // Groovy Expression Tests
    // =========================================================================

    /**
     * Test basic Groovy arithmetic expressions.
     */
    @Test
    public void testGroovyBasicArithmetic()
    {
        Assert.assertEquals("2", PropertiesUtils.substituteVariables("#{ 1 + 1 }", props));
        Assert.assertEquals("40.0", PropertiesUtils.substituteVariables("#{ 100 * 0.4 }", props));
        Assert.assertEquals("25", PropertiesUtils.substituteVariables("#{ 100 / 4 as int }", props));
    }

    /**
     * Test Groovy property access using props binding.
     */
    @Test
    public void testGroovyPropertyAccess()
    {
        props.setProperty("userCount", "100");

        // Using getAt syntax (props['key'])
        Assert.assertEquals("100", PropertiesUtils.substituteVariables("#{ props['userCount'] }", props));

        // Using getProperty method
        Assert.assertEquals("100", PropertiesUtils.substituteVariables("#{ props.getProperty('userCount') }", props));

        // Calculation with property
        Assert.assertEquals("40", PropertiesUtils.substituteVariables("#{ (props['userCount'] as int) * 0.4 as int }", props));
    }

    /**
     * Test Groovy context sharing between expressions.
     */
    @Test
    public void testGroovyContextSharing()
    {
        final java.util.Map<String, Object> ctx = new java.util.concurrent.ConcurrentHashMap<>();

        // Store value in context
        String result1 = PropertiesUtils.substituteVariables("#{ ctx['myKey'] = 42; 'stored' }", props, ctx);
        Assert.assertEquals("stored", result1);

        // Retrieve value from context
        String result2 = PropertiesUtils.substituteVariables("#{ ctx['myKey'] }", props, ctx);
        Assert.assertEquals("42", result2);

        // Calculate based on stored context
        String result3 = PropertiesUtils.substituteVariables("#{ ctx['myKey'] * 2 }", props, ctx);
        Assert.assertEquals("84", result3);
    }

    /**
     * Test multi-line Groovy scripts.
     */
    @Test
    public void testGroovyMultiLineScript()
    {
        props.setProperty("totalUsers", "100");

        final java.util.Map<String, Object> ctx = new java.util.concurrent.ConcurrentHashMap<>();

        String script = """
            #{
                def total = props['totalUsers'] as int
                ctx['base'] = total
                ctx['browse'] = (total * 0.4) as int
                'configured'
            }""";

        String result = PropertiesUtils.substituteVariables(script, props, ctx);
        Assert.assertEquals("configured", result);
        Assert.assertEquals(100, ctx.get("base"));
        Assert.assertEquals(40, ctx.get("browse"));

        // Now use the stored values
        String users = PropertiesUtils.substituteVariables("#{ ctx['base'] - ctx['browse'] }", props, ctx);
        Assert.assertEquals("60", users);
    }

    /**
     * Test mixed ${} and #{} expansion.
     */
    @Test
    public void testGroovyMixedExpansion()
    {
        props.setProperty("base", "100");

        // ${base} is resolved first, then #{} evaluates the result
        Assert.assertEquals("150", PropertiesUtils.substituteVariables("#{ ${base} + 50 }", props));
    }

    /**
     * Test Groovy expression embedded in text.
     */
    @Test
    public void testGroovyEmbeddedInText()
    {
        props.setProperty("count", "5");

        String result = PropertiesUtils.substituteVariables("Users: #{ props['count'] as int * 10 }", props);
        Assert.assertEquals("Users: 50", result);
    }

    /**
     * Test that no Groovy markers returns value unchanged.
     */
    @Test
    public void testGroovyNoMarkers()
    {
        Assert.assertEquals("plain text", PropertiesUtils.substituteVariables("plain text", props));
        Assert.assertEquals("${testKey}", PropertiesUtils.substituteVariables("${testKey}", new Properties()));
    }

    /**
     * Test that invalid Groovy syntax throws exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGroovyInvalidSyntax()
    {
        PropertiesUtils.substituteVariables("#{ this is not valid groovy ++ }", props);
    }

    /**
     * Test that security blocks dangerous operations.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGroovySecurityBlocksFileAccess()
    {
        PropertiesUtils.substituteVariables("#{ new java.io.File('/etc/passwd') }", props);
    }

    /**
     * Test null result from Groovy returns empty string.
     */
    @Test
    public void testGroovyNullResult()
    {
        Assert.assertEquals("", PropertiesUtils.substituteVariables("#{ null }", props));
    }
}
