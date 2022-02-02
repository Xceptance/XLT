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
package com.xceptance.xlt.api.tests;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;

import util.lang.ClassAsByteArray;
import util.lang.ClassFromByteArrayLoader;
import util.lang.SimpleCompiler;
import util.xlt.properties.ReversibleChangePipeline;

/**
 * @author Sebastian Oerding
 */
public class AbstractTestCaseTest
{
    private ReversibleChangePipeline rcp;

    @Before
    public void before()
    {
        rcp = new ReversibleChangePipeline();
    }

    @After
    public void after()
    {
        rcp.reverseAll();
    }

    @Test
    public void testTestName()
    {
        // The test class, yes it's a valid complete class!
        final String sourceCode = "public class Test extends com.xceptance.xlt.api.tests.AbstractTestCase {}";
        // Compiling the class
        final ClassAsByteArray caba = SimpleCompiler.compile("Test", sourceCode);
        // Loading the class into a class loader
        final Class<?> theLoadedClass = ClassFromByteArrayLoader.getFreshlyLoadedClass(caba);
        // Create a new instance of this class, actually theLoadedClass is a sub type of AbstractTestCase
        final Object o = ReflectionUtils.getNewInstance(theLoadedClass);

        // Take the method
        final Method method = ReflectionUtils.getMethod(AbstractTestCase.class, "getSimpleName");
        // Get the value for o, should be simply "Test" as the class resides (due to its code) in the default package
        final String testName = ReflectionUtils.invokeMethod(o, method);
        // Verify test name
        Assert.assertEquals("Wrong name for compiled class", "Test", testName);
    }

    @Test
    public void testGetEffectiveKey()
    {
        final DummyTestCase testCase = new DummyTestCase();
        testCase.__setup();

        final String bareKey = "BLABLA_BLA";
        final String classNameKey = DummyTestCase.class.getName() + "." + bareKey;
        final String userNameKey = DummyTestCase.class.getSimpleName() + "." + bareKey;

        // no key present
        Assert.assertEquals("Wrong effective key!", bareKey, testCase.getEffectiveKey(bareKey));

        // bare key present
        rcp.addAndApply(bareKey, "");
        Assert.assertEquals("Wrong effective key!", bareKey, testCase.getEffectiveKey(bareKey));

        // classNameKey not present
        Assert.assertEquals("Wrong effective key!", bareKey, testCase.getEffectiveKey(bareKey));

        // classNameKey present
        rcp.addAndApply(classNameKey, "");
        Assert.assertEquals("Wrong effective key!", classNameKey, testCase.getEffectiveKey(bareKey));

        // userNameKey not present
        Assert.assertEquals("Wrong effective key!", classNameKey, testCase.getEffectiveKey(bareKey));

        // userNameKey present
        rcp.addAndApply(userNameKey, "");
        Assert.assertEquals("Wrong effective key!", userNameKey, testCase.getEffectiveKey(bareKey));
    }

    @Test
    public void testReconfigureStartUrl() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        String baseUrl = "http://de.selfhtml.org/intro/technologien/html.htm#auszeichnungssprache";
        String resultingUrl = dtc.reconfigureStartUrl(baseUrl);
        Assert.assertEquals("Wrong resulting URI, ", baseUrl, resultingUrl);

        baseUrl = "http://www.heise.de";
        resultingUrl = dtc.reconfigureStartUrl(baseUrl);
        Assert.assertEquals("Wrong resulting URI, ", baseUrl, resultingUrl);

        rcp.addAndApply("startUrl.protocol", "ftp");
        rcp.addAndApply("startUrl.userInfo", "xlt");
        rcp.addAndApply("startUrl.host", "www.heise.de");
        rcp.addAndApply("startUrl.port", "8080");
        rcp.addAndApply("startUrl.path", "/newsticker/meldung/1407195.html");
        rcp.addAndApply("startUrl.query", "url=mine");
        rcp.addAndApply("startUrl.fragment", "section");
        baseUrl = "ftp://xlt@www.heise.de:8080/newsticker/meldung/1407195.html?url=mine#section";
        Assert.assertEquals("Wrong resulting URI, ", baseUrl, dtc.reconfigureStartUrl(baseUrl));
    }

    @Test
    public void testGetProperty()
    {
        final String key = "mööh";
        final DummyTestCase dtc = new DummyTestCase();
        final String value = dtc.getProperty(key);
        Assert.assertEquals("Expected no value for key \"mööh\" to be in place but got ", null, value);

        final boolean val = dtc.getProperty(key, false);
        Assert.assertEquals("Expected no value \"true\" for key \"mööh\" to be in place!", false, val);
    }

    /**
     * test name for parameterless constructor
     * 
     * @throws Exception
     */
    @Test
    public void getTestName_initial_empty_constructor() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        Assert.assertEquals(DummyTestCase.class.getName(), dtc.getTestName());
    }

    /**
     * set test name <code>null</code>
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_null() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName(null);
        Assert.assertEquals(DummyTestCase.class.getName(), dtc.getTestName());
    }

    /**
     * set test name <code>empty</code>
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_empty() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("");
        Assert.assertEquals(DummyTestCase.class.getName(), dtc.getTestName());
    }

    /**
     * set test name to <code>spaces</code> only
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_spaces_only() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("  ");
        Assert.assertEquals(DummyTestCase.class.getName(), dtc.getTestName());
    }

    /**
     * set test name to certain <code>whitespaces</code> only
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_whitespace_only_2() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("\t \n");
        Assert.assertEquals(DummyTestCase.class.getName(), dtc.getTestName());
    }

    /**
     * test name has leading and trailing <code>whitespace</code>
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_whitespace_surrounded() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName(" foo ");
        Assert.assertEquals(" foo ", dtc.getTestName());
    }

    /**
     * test name has <code>whitespace</code> inside
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_whitespace_inside() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("foo bar");
        Assert.assertEquals("foo bar", dtc.getTestName());
    }

    /**
     * test name is simple name
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_valid() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("foo");
        Assert.assertEquals("foo", dtc.getTestName());
    }

    /**
     * test name consists of digits only
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_digits() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("123");
        Assert.assertEquals("123", dtc.getTestName());
    }

    /**
     * test name consists of special chars
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_specialChars() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("-_.,+$\\//()");
        Assert.assertEquals("-_.,+$\\//()", dtc.getTestName());
    }

    /**
     * overwrite test name
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_overwrite() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("foo");
        Assert.assertEquals("foo", dtc.getTestName());
        dtc.setTestName("bar");
        Assert.assertEquals("bar", dtc.getTestName());
    }

    /**
     * enrich test name
     * 
     * @throws Exception
     */
    @Test
    public void setTestName_enrich() throws Exception
    {
        final DummyTestCase dtc = new DummyTestCase();
        dtc.setTestName("foo");
        Assert.assertEquals("foo", dtc.getTestName());
        dtc.setTestName(dtc.getTestName() + "bar");
        Assert.assertEquals("foobar", dtc.getTestName());
    }

    private class DummyTestCase extends AbstractTestCase
    {
    }
}
