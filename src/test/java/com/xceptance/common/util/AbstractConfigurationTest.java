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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the implementation of {@link AbstractConfiguration}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractConfigurationTest
{
    /** Private instance of TestConfiguration.class */
    private TestConfiguration conf;

    @Before
    public void init()
    {
        conf = new TestConfiguration();

        final Properties props = new Properties();

        // string props
        props.setProperty("testclass", "AbstractConfigurationTest");
        props.setProperty("testclass.subclass", "TestConfiguration");
        props.setProperty("test.emptystring", "");

        // boolean props
        props.setProperty("product.worksfine", "true");
        props.setProperty("product.expensive", "false");

        // class prop
        props.setProperty("string.class", "java.lang.String");

        // int props
        props.setProperty("one.number", "1");
        props.setProperty("two.number", "2");

        // double props
        props.setProperty("one.double", "3.7");

        // file prop
        props.setProperty("test.file", "test");

        // URL prop
        props.setProperty("localhost.url", "http://localhost");

        // string props for key-prefix and key-fragment tests
        props.setProperty("one.two.three", "testValue1");
        props.setProperty("one.two.four", "testvalue2");
        props.setProperty("one.two.four.one", "testValue3");

        conf.addProperties(props);

    }

    /**
     * Test adding of properties. 'getStringProperty' is the only method which directly delegates to the underlying
     * Properties object.
     */
    @Test
    public void testAddProperties()
    {

        Assert.assertEquals("AbstractConfigurationTest", conf.getStringProperty("testclass"));
        Assert.assertEquals("TestConfiguration", conf.getStringProperty("testclass.subclass"));
        Assert.assertEquals("", conf.getStringProperty("test.emptystring"));

    }

    /**
     * Test exception raising of 'getRequiredProperty'.
     */
    @Test
    public void testGetRequiredProperty()
    {
        try
        {
            conf.getStringProperty("some.string.property");
            Assert.fail("Property doesn't exist. 'getStringProperty' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseClassName'.
     */
    @Test
    public void testParseClassName_RaiseException()
    {
        try
        {
            conf.getClassProperty("test.emptystring");
            Assert.fail("'parseClassName' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseFileName'.
     */
    @Test
    public void testParseFileName_RaiseException()
    {
        try
        {
            conf.getFileProperty("test.emptystring");
            Assert.fail("'parseFileName' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseUrl'.
     */
    @Test
    public void testParseUrl_RaiseException()
    {
        try
        {
            conf.getUrlProperty("test.emptystring");
            Assert.fail("'parseUrl' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseUri'.
     */
    @Test
    public void testParseUri_RaiseException()
    {
        try
        {
            conf.getUriProperty("test.emptystring");
            Assert.fail("'parseUri' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseInt'.
     */
    @Test
    public void testParseInt_RaiseException()
    {
        try
        {
            conf.getIntProperty("test.emptystring");
            Assert.fail("'parseInt' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test exception raising of 'parseDouble'.
     */
    @Test
    public void testParseDouble_RaiseException()
    {
        try
        {
            conf.getDoubleProperty("test.emptystring");
            Assert.fail("'parseDouble' should raise an exception!");
        }
        catch (final Throwable t)
        {
        }
    }

    /**
     * Test correct handling of 'getBooleanProperty(String)' and 'getBooleanProperty(String, boolean)'.
     */
    @Test
    public void testGetBooleanProperty()
    {
        Assert.assertTrue(conf.getBooleanProperty("product.worksfine"));
        Assert.assertFalse(conf.getBooleanProperty("product.expensive"));
        Assert.assertFalse(conf.getBooleanProperty("property.available", false));
    }

    /**
     * Test correct handling of 'getClassProperty(String)' and 'getClassProperty(String, Class)'.
     */
    @Test
    public void testGetClassProperty()
    {
        Assert.assertEquals(String.class, conf.getClassProperty("string.class"));
        Assert.assertEquals(Object.class, conf.getClassProperty("object.class", Object.class));
    }

    /**
     * Test correct handling of 'getIntProperty(String)' and 'getIntProperty(String, int)'.
     */
    @Test
    public void testGetIntProperty()
    {
        Assert.assertEquals(1, conf.getIntProperty("one.number"));
        Assert.assertEquals(2, conf.getIntProperty("two.number"));
        Assert.assertEquals(3, conf.getIntProperty("some.number", 3));
    }

    /**
     * Test correct handling of 'getDoubleProperty(String)' and 'getDoubleProperty(String, double)'.
     */
    @Test
    public void testGetDoubleProperty()
    {
        Assert.assertEquals(3.7, conf.getDoubleProperty("one.double"), 0.0);
        Assert.assertEquals(2.4, conf.getDoubleProperty("some.number", 2.4), 0.0);
    }

    /**
     * Test correct handling of 'getFileProperty(String)' and 'getFileProperty(String, File)'.
     */
    @Test
    public void testGetFileProperty()
    {
        Assert.assertEquals(new File("test"), conf.getFileProperty("test.file"));
        final File anyFile = new File("anyFile");
        Assert.assertEquals(anyFile, conf.getFileProperty("any.file", anyFile));
    }

    /**
     * Test correct handling of 'getURLProperty(String)' and 'getURLProperty(String, URL)'.
     */
    @Test
    public void testGetURLProperty()
    {
        try
        {
            Assert.assertEquals(new URL("http://localhost"), conf.getUrlProperty("localhost.url"));
            final URL tempURL = new URL("file:///tmp");
            Assert.assertEquals(tempURL, conf.getUrlProperty("temp.dir.url", tempURL));
        }
        catch (final MalformedURLException mue)
        {
            Assert.fail(mue.getMessage());
        }
    }

    /**
     * Test correct handling of 'getUriProperty(String)' and 'getUriProperty(String, URI)'.
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testGetUriProperty()
    {
        try
        {
            Assert.assertEquals(new URI("http://localhost"), conf.getUriProperty("localhost.url"));
            final URI tempURI = new URI("file:///tmp");
            Assert.assertEquals(tempURI, conf.getUriProperty("temp.dir.url", tempURI));
        }
        catch (final URISyntaxException mue)
        {
            Assert.fail(mue.getMessage());
        }
    }

    /**
     * Test property key fragment lookup.
     */
    @Test
    public void testGetPropertyKeyFragment()
    {
        Set<String> keys = conf.getPropertyKeyFragment("one.two.");
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("three"));
        Assert.assertTrue(keys.contains("four"));

        keys = conf.getPropertyKeyFragment("one.two.four.");
        Assert.assertEquals(1, keys.size());
        Assert.assertTrue(keys.contains("one"));
    }

    /**
     * Test property key lookup using common prefix.
     */
    @Test
    public void testGetPropertyKeysWithPrefix()
    {
        Set<String> keys = conf.getPropertyKeysWithPrefix("one.two");
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keys.contains("one.two.three"));
        Assert.assertTrue(keys.contains("one.two.four"));
        Assert.assertTrue(keys.contains("one.two.four.one"));

        keys = conf.getPropertyKeysWithPrefix("one.two.four");
        Assert.assertEquals(2, keys.size());
        Assert.assertFalse(keys.contains("one.two.three"));
        Assert.assertTrue(keys.contains("one.two.four"));
        Assert.assertTrue(keys.contains("one.two.four.one"));
    }

    /**
     * Private helper class that simply extends AbstractConfiguration to enable instantiation.
     * 
     * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
     */
    private static class TestConfiguration extends AbstractConfiguration
    {
    }
}
