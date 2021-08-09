/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.common.util.PropertiesUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Test the implementation of {@link XltProperties}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltPropertiesTest
{
    /**
     * XltProperties test instance.
     */
    protected XltProperties instance = null;

    /**
     * Name of test properties file.
     */
    protected static final String TEST_FILENAME = XltPropertiesTest.class.getSimpleName() + ".properties";

    protected static final String TEST_FILENAME_DIRECT_PROPERTIES = "directtest.properties";

    /**
     * System-dependent directory for temporary files.
     */
    protected static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Line separator.
     */
    protected static final String LINE_SEP = IOUtils.LINE_SEPARATOR;

    /**
     * Properties instance holding the test data.
     */
    protected static final Properties PROPS = setProperties();

    static
    {
        XltExecutionContext.getCurrent().setTestSuiteConfigDir(new File("samples/testsuite-posters/config"));
        XltExecutionContext.getCurrent().setTestSuiteHomeDir(new File(XltExecutionContext.getCurrent().getTestSuiteConfigDir().getName().getParent().getPath()));
    }

    /**
     * Sets up the test fixture statically.
     *
     * @throws Exception
     *             thrown when setup failed.
     */
    @BeforeClass
    public static void classIntro() throws Exception
    {
        Assert.assertNotNull(TEMP_DIR);
        Assert.assertTrue(TEMP_DIR.isDirectory() && TEMP_DIR.canWrite());

        final File f = new File(TEMP_DIR, TEST_FILENAME);

        final OutputStream out = new FileOutputStream(f);
        PROPS.store(out, f.getName());
        out.close();

        // write a special file with string data
        final File direct = new File(TEMP_DIR, TEST_FILENAME_DIRECT_PROPERTIES);

        final FileWriter fw = new FileWriter(direct);
        try
        {
            // K = Key V = Value
            // t = trimmed, nt = not trimmed
            fw.write("b_Kt_Vt=true\n");
            fw.write("b_Knt_Vnt =\ttrue \n");
            fw.write("b_Knt_Vt =true\n");
            fw.write("b_Kt_Vnt=\ttrue\t\n");

            fw.write("n_Kt_Vt=100\n");
            fw.write("n_Knt_Vnt = 100 \n");
            fw.write("n_Knt_Vt =100\n");
            fw.write("n_Kt_Vnt= 100 \n");

            fw.write("s_Kt_Vt=String\n");
            fw.write("s_Knt_Vnt = String \n");
            fw.write("s_Knt_Vt =String\n");
            fw.write("s_Kt_Vnt= String \n");
        }
        finally
        {
            fw.close();
        }
    }

    /**
     * Sets up the test fixture.
     *
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro()
    {
        XltPropertiesImpl.reset();
        instance = XltProperties.getInstance();
        Assert.assertNotNull("Failed to retrieve XltProperties singleton instance.", instance);
        final String testPropFileName = instance.getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);
        Assert.assertEquals("test.properties", testPropFileName);
    }

    /**
     * Tears down the test fixture statically.
     *
     * @throws Exception
     *             thrown when tear down has failed.
     */
    @AfterClass
    public static void classOutro() throws Exception
    {
        FileUtils.deleteFile(new File(TEMP_DIR, TEST_FILENAME));
        FileUtils.deleteFile(new File(TEMP_DIR, TEST_FILENAME_DIRECT_PROPERTIES));
    }

    /**
     * Tests the implementation of {@link XltProperties#getPropertiesForKey(String)} using an invalid domain key.
     */
    @Test
    public void testGetPropertiesForKey_KeyNullOrEmpty()
    {
        Map<String, String> map = instance.getPropertiesForKey(null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());

        map = instance.getPropertiesForKey("");
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }

    /**
     * Tests the implementation of {@link XltProperties#getPropertiesForKey(String)} using a valid domain key.
     */
    @Test
    public void testGetPropertiesForKey_ValidKey()
    {
        final Map<String, String> map = instance.getPropertiesForKey("java.");
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
    }

    /**
     * Tests the implementation of {@link XltProperties#getPropertiesForKey(String)} with respect to variable
     * substitution in property values.
     */
    @Test
    public void testGetPropertiesForKey_ValueWithPlaceholders()
    {
        instance.setProperties(PROPS);
        final Map<String, String> map = instance.getPropertiesForKey("test.");
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals("jeronimo rocks!", map.get("testKey2"));
    }

    /**
     * Tests the implementation of {@link XltProperties#setProperties(File)} .
     */
    @Test
    public void testSetPropertiesFile()
    {
        try
        {
            instance.setProperties(new File(TEMP_DIR, TEST_FILENAME));
        }
        catch (final IOException e)
        {
            Assert.fail("Failed to set property file '" + TEST_FILENAME + "'. Cause: " + e.getMessage());
        }

        final Map<String, String> map = instance.getPropertiesForKey("test");
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());
    }

    /**
     * Tests the implementation of {@link XltProperties#getProperty(String, int)},
     * {@link XltProperties#getProperty(String, long)} and {@link XltProperties#getProperty(String, boolean)}.
     */
    @Test
    public void testGetProperty_Primitives()
    {
        try
        {
            instance.setProperties(new File(TEMP_DIR, TEST_FILENAME));
        }
        catch (final IOException e)
        {
            Assert.fail("Failed to set property file '" + TEST_FILENAME + "'. Cause: " + e.getMessage());
        }

        Assert.assertEquals(5, instance.getProperty("prim.test.int", 0));
        // number format exception, so return the default value
        Assert.assertEquals(5, instance.getProperty("prim.test.bool", 5));
        Assert.assertEquals(-1, instance.getProperty("thereShouldBeNoPropertyWithThisName", -1));
        Assert.assertEquals(1L, instance.getProperty("prim.test.long", -1L));
        // number format exception, so return the default value
        Assert.assertEquals(5L, instance.getProperty("prim.test.bool", 5L));
        Assert.assertEquals(-1L, instance.getProperty("thereShouldBeNoPropertyWithThisName", -1L));
        Assert.assertEquals(false, instance.getProperty("prim.test.bool", true));

        Assert.assertEquals(123, instance.getProperty("ghostkey", 123));
        Assert.assertEquals(Long.MAX_VALUE, instance.getProperty("ghostkey", Long.MAX_VALUE));
        Assert.assertEquals(true, instance.getProperty("ghostkey", true));
    }

    /**
     * Tests the implementation of {@link XltProperties#getPropertyRandomValue(String, String)}.
     */
    @Test
    public void testGetRandomProperty()
    {
        try
        {
            instance.setProperties(new File(TEMP_DIR, TEST_FILENAME));
        }
        catch (final IOException e)
        {
            Assert.fail("Failed to set property file '" + TEST_FILENAME + "'. Cause: " + e.getMessage());
        }

        final String s = instance.getPropertyRandomValue("prim.test.multivalue", "Guiness Kilkenny Strongbow");
        Assert.assertTrue(s.equals("3") || s.equals("2") || s.equals("1"));
        final String s2 = instance.getPropertyRandomValue("thereShouldBeNoPropertyWithThisName", null);
        Assert.assertEquals("Expected no value for property with name \"thereShouldBeNoPropertyWithThisName\"!", XltConstants.EMPTYSTRING,
                            s2);
    }

    /**
     * Tests the implementation of {@link XltProperties#reset()}.
     */
    @Test
    public void testReset()
    {
        instance.setProperties(PROPS);
        Assert.assertEquals(3, instance.getPropertiesForKey("test").size());

        XltPropertiesImpl.reset();
        Assert.assertTrue(instance.getPropertiesForKey("test").isEmpty());

        final String testPropFileName = instance.getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);
        Assert.assertEquals("test.properties", testPropFileName);

    }

    @Test
    public void testSimpleCalls()
    {
        Assert.assertTrue("Date is to small!", instance.getStartTime() > 1326292183134L);
        instance.getVersion();
        instance.update();
    }

    @Test
    public void testPut()
    {
        final Class<?> theClass = ReflectionUtils.getNestedClass(XltPropertiesImpl.class, "VarSubstitutionSupportedProperties");
        final Object instanceOfTheClass = ReflectionUtils.getNewInstance(theClass);
        final Method putMethod = ReflectionUtils.getMethod(theClass, "put", Object.class, Object.class);
        final Object result0 = ReflectionUtils.invokeMethod(instanceOfTheClass, putMethod, null, null);
        Assert.assertEquals("Wrong result for method \"put\"", null, result0);
        final Object result1 = ReflectionUtils.invokeMethod(instanceOfTheClass, putMethod, null, "value");
        Assert.assertEquals("Wrong result for method \"put\"", null, result1);
        final Object result2 = ReflectionUtils.invokeMethod(instanceOfTheClass, putMethod, "key", null);
        Assert.assertEquals("Wrong result for method \"put\"", null, result2);
    }

    /**
     * Tests the implementation of {@link XltProperties#setProperties(Properties)}.
     */
    @Test
    public void testSetPropertiesProperties()
    {
        Assert.assertTrue(instance.getPropertiesForKey("test").isEmpty());

        instance.setProperties(PROPS);
        Assert.assertEquals(3, instance.getPropertiesForKey("test").size());

        for (final Enumeration<?> e = PROPS.propertyNames(); e.hasMoreElements();)
        {
            final String s = (String) e.nextElement();
            Assert.assertTrue(instance.containsKey(s));
            Assert.assertEquals(PropertiesUtils.substituteVariables(PROPS.getProperty(s), PROPS), instance.getProperty(s));
        }
    }

    /**
     * Tests the trimming functionality when setting a property.
     */
    @Test
    public void testSetPropertyUntrimmed()
    {
        final String key = "untrimmed.value";
        final String value = " untrimmed      ";

        instance.setProperty(key, value);
        Assert.assertEquals(value.trim(), instance.getProperty(key));
    }

    /**
     * Tests the trimming functionality when setting a set of properties.
     */
    @Test
    public void testSetPropertiesUntrimmed()
    {
        final String key = "untrimmed.value";
        final String value = " untrimmed      ";
        final Properties props = new Properties();
        props.setProperty(key, value);

        instance.setProperties(props);

        Assert.assertEquals(value.trim(), instance.getProperty(key));
    }

    /**
     * Plays with trimmed and untrimmed properties
     */
    @Test
    public void testTrimmingOfProperties()
    {
        try
        {
            instance.setProperties(new File(TEMP_DIR, TEST_FILENAME_DIRECT_PROPERTIES));
        }
        catch (final IOException e)
        {
            Assert.fail("Failed to set property file '" + TEST_FILENAME_DIRECT_PROPERTIES + "'. Cause: " + e.getMessage());
        }

        Assert.assertTrue(instance.getProperty("b_Kt_Vt", false));
        Assert.assertTrue(instance.getProperty("b_Knt_Vnt", false));
        Assert.assertTrue(instance.getProperty("b_Knt_Vt", false));
        Assert.assertTrue(instance.getProperty("b_Kt_Vnt", false));

        Assert.assertEquals(100, instance.getProperty("n_Kt_Vt", 101));
        Assert.assertEquals(100, instance.getProperty("n_Knt_Vnt", 101));
        Assert.assertEquals(100, instance.getProperty("n_Knt_Vt", 101));
        Assert.assertEquals(100, instance.getProperty("n_Kt_Vnt", 101));

        Assert.assertEquals("String", instance.getProperty("s_Kt_Vt", "foo"));
        Assert.assertEquals("String", instance.getProperty("s_Knt_Vnt", "foo"));
        Assert.assertEquals("String", instance.getProperty("s_Knt_Vt", "foo"));
        Assert.assertEquals("String", instance.getProperty("s_Kt_Vnt", "foo"));

    }

    /**
     * Tests the implementation of {@link XltProperties#getProperties()}, also with respect to variable substitution in
     * property values.
     */
    @Test
    public void testGetProperties()
    {
        instance.setProperties(PROPS);
        final Properties props = instance.getProperties();
        Assert.assertNotNull(props);
        Assert.assertFalse(props.isEmpty());

        // check variable substitution, also when using Map API
        Assert.assertEquals("jeronimo rocks!", props.getProperty("test.testKey2"));
        Assert.assertEquals("jeronimo rocks!", props.get("test.testKey2"));
    }

    /**
     * Creates a new Properties instance, fills it with test data and returns it afterwards.
     *
     * @return Properties instance holding test data.
     */
    private static Properties setProperties()
    {
        final Properties props = new Properties();

        props.setProperty("test.testKey1", "jeronimo");
        props.setProperty("test.testKey2", "${test.testKey1} rocks!");
        props.setProperty("test.${test.testKey1}.status", "king of the world");
        props.setProperty("prim.test.int", "5");
        props.setProperty("prim.test.long", "1");
        props.setProperty("prim.test.bool", "false");
        props.setProperty("prim.test.multivalue", "3 3 3 2 2 1");

        return props;
    }

    /**
     * Tests the multi-step lookup procedure in {@link XltProperties#getProperty(String)} that allows to override
     * general settings by qualifying them with the user name or the test class name.
     */
    @Test
    public void testGetPropertyWithFallback()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();
        final String originalTestClassName = session.getTestCaseClassName();

        try
        {
            // setup
            final String userName = getClass().getSimpleName();
            final String testClassName = getClass().getName();

            session.setUserName(userName);
            session.setTestCaseClassName(testClassName);

            instance.setProperty(userName + ".foo", "userValue");
            instance.setProperty(testClassName + ".foo", "classValue");
            instance.setProperty("foo", "bareValue");

            instance.setProperty(testClassName + ".bar", "classValue");
            instance.setProperty("bar", "bareValue");

            instance.setProperty("baz", "bareValue");

            // test
            Assert.assertEquals("userValue", instance.getProperty("foo"));
            Assert.assertEquals("classValue", instance.getProperty("bar"));
            Assert.assertEquals("bareValue", instance.getProperty("baz"));
        }
        finally
        {
            // restore session
            session.setUserName(originalUserName);
            session.setTestCaseClassName(originalTestClassName);
        }
    }

    /**
     * Ensure that the hierarchy of properties is intact, i.e. props take precedence in the following order:
     *
     * 1. user-specific
     * 2. test-class specific
     * 3. bare property key
     *
     * In each case the secret version of a property takes precedence over the public version
     */
    @Test
    public void testHierarchyOfPropertiesIsIntact()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();
        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty("prop", "Public");

        Assert.assertEquals("Public", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX, "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX +"prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(originalTestClassName + ".prop", "Test Class");

        Assert.assertEquals("Test Class", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX + originalTestClassName + ".prop", "Secret Test Class");

        Assert.assertEquals("Secret Test Class", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(originalUserName + ".prop", "User Name");

        Assert.assertEquals("User Name", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("User Name", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX + originalUserName + ".prop", "Secret User Name");

        Assert.assertEquals("Secret User Name", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));
    }

    /**
     * This tests, whether the secret properties are available with and without
     * the "secret." prefix
     */
    @Test
    public void testGetSecretPropertiesCompatible()
    {
        instance.setProperty(XltConstants.SECRET_PREFIX+"myProp", "Some very secret value");

        Assert.assertEquals("Some very secret value", instance.getProperty(XltConstants.SECRET_PREFIX+"myProp", "Secret not found"));
        Assert.assertEquals("Some very secret value", instance.getProperty("myProp", "Normal not found"));
    }

    /**
     * Check whether secret props always take precendence over normal properties
     */
    @Test
    public void testSecretPropOverwritesPublicProp()
    {
        instance.setProperty(XltConstants.SECRET_PREFIX+"prop", "Secret");
        instance.setProperty("prop", "Public");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }

    /**
     * Check whether secret properties can also be per test case
     */
    @Test
    public void testTestCaseSpecificSecretProperties()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty(XltConstants.SECRET_PREFIX + originalTestClassName +".prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX + originalTestClassName +".prop", "Full secret not found"));
        Assert.assertEquals("Secret", instance.getProperty(originalTestClassName +".prop", "Full not found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }
    /**
     * Check whether secret properties can also be per user
     */
    @Test
    public void testUserSpecificSecretProperties()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();

        instance.setProperty(XltConstants.SECRET_PREFIX + originalUserName +".prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX + originalUserName +".prop", "Full secret not found"));
        Assert.assertEquals("Secret", instance.getProperty(originalUserName +".prop", "Full not found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretProps()
    {
        instance.setProperty("prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretUserProps()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();

        instance.setProperty(originalUserName +".prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty("secret.prop", "Not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretTestCaseProps()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty(originalTestClassName +".prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty("secret.prop", "Not found"));
    }

    /**
     * Test whether {@link XltProperties#getProperties()} returns only the original secret property
     */
    @Test
    public void testConvertingToNormalPropertiesReturnsOriginalKey()
    {
        instance.setProperty("secret.prop", "Secret");

        final Properties properties = instance.getProperties();

        Assert.assertEquals("Secret", properties.getProperty("secret.prop"));
        Assert.assertNull(properties.getProperty("prop"));
    }

    @Test
    public void testContainsKeyLooksForSecretKeyAsWell()
    {
        instance.setProperty("secret.prop", "Some value");

        Assert.assertTrue(instance.containsKey("secret.prop"));
        Assert.assertTrue(instance.containsKey("prop"));
    }

    @Test
    public void testLoadingSecretPropertiesFromFiles()
    {
        Assert.assertTrue(instance.containsKey("secret.str"));
        Assert.assertTrue(instance.containsKey("str"));
        Assert.assertTrue(instance.containsKey("secret.value"));
        Assert.assertTrue(instance.containsKey("value"));
    }
}
