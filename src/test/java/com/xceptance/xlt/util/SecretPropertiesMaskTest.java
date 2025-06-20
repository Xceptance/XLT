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
