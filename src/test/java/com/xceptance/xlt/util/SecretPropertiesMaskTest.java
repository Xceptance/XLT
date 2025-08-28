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
package com.xceptance.xlt.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;

public class SecretPropertiesMaskTest
{
    @Test
    public void testMaskSecretProperties_PrefixedPropsOnly() throws Exception
    {
        final String testProps = """
            test.key1 = foo
            test.key2 = bar

            ## Some Comment

            # test.key1 = old override
            test.key1 = override1
            test.key3 = baz

            secret.key1 = You shall \\
            not pass!!

            # Yet another override
            test.key2 = override2
            """;

        final StringWriter writer = new StringWriter();
        try (final SecretPropertiesMask mask = new SecretPropertiesMask(new StringReader(testProps), writer))
        {
            mask.maskProperties(false);
        }

        final String s = writer.toString();

        System.out.println("=== Original ===");
        System.out.println(testProps);
        System.out.println("\n=== Masked ===");
        System.out.println(s);

        final Properties props = new Properties();
        props.load(new StringReader(s));

        Assert.assertEquals("override1", props.getProperty("test.key1"));
        Assert.assertEquals("override2", props.getProperty("test.key2"));

        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, props.getProperty("secret.key1"));
    }

    @Test
    public void testMaskSecretProperties_AllProps() throws Exception
    {
        final String testProps = """
            test.key1 = foo
            test.key2 = bar

            ## Some Comment

            # test.key1 = old override
            test.key1 = override1
            test.key3 = baz

            secret.key1 = You shall \\
            not pass!!

            # Yet another override
            test.key2 = override2
            """;

        final StringWriter writer = new StringWriter();
        try (final SecretPropertiesMask mask = new SecretPropertiesMask(new StringReader(testProps), writer))
        {
            mask.maskProperties(true);
        }

        final String s = writer.toString();

        System.out.println("=== Original ===");
        System.out.println(testProps);
        System.out.println("\n=== Masked ===");
        System.out.println(s);

        final Properties props = new Properties();
        props.load(new StringReader(s));

        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, props.getProperty("test.key1"));
        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, props.getProperty("test.key2"));
        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, props.getProperty("test.key3"));

        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, props.getProperty("secret.key1"));
    }

}
