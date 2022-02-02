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
        final String testString = "The key testKey should be set to ${testKey}. " + "And this is key is empty: [${emptyKey}]. "
                                  + "Additionally, here we have a key containing a special character: ${$foo-bar}."
                                  + "Last but not least, an undefined key: #${undefined}#";
        // expected result string as indicated in documentation of
        // 'substituteVariables'
        final String replacedString = "The key testKey should be set to testValue. " + "And this is key is empty: []. "
                                      + "Additionally, here we have a key containing a special character: jesus."
                                      + "Last but not least, an undefined key: #${undefined}#";

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
     * Tests the implementation of {@link PropertiesUtils#loadProperties(File)} by passing a valid file and valid
     * properties as parameters.
     */
    @Test
    public void testLoadProperties_ValidFileValidProperties() throws Exception
    {
        PropertiesUtils.loadProperties(new File(TEST_PROPERTY_FILE), props);
        Assert.assertFalse(props.isEmpty());
        Assert.assertTrue(props.containsKey("testKey"));
        Assert.assertTrue(props.containsKey("emptyKey"));
    }
}
