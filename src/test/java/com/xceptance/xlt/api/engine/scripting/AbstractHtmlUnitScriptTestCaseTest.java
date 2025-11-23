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
package com.xceptance.xlt.api.engine.scripting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.xceptance.common.collection.LRUHashMap;
import com.xceptance.xlt.api.engine.scripting.test.TestModule;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.TestDataUtils;

/**
 * Simple variable resolution test for tests that inherit from {@link AbstractHtmlUnitScriptTestCase}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractHtmlUnitScriptTestCaseTest
{
    @After
    public void cleanLoadedData() throws Exception
    {
        final Field pkgDataField = TestContext.class.getDeclaredField("LOADED_PKG_DATA");
        pkgDataField.setAccessible(true);
        ((LRUHashMap<?, ?>) pkgDataField.get(null)).clear();

        final Field scriptDataField = TestContext.class.getDeclaredField("LOADED_DATA");
        scriptDataField.setAccessible(true);
        ((LRUHashMap<?, ?>) scriptDataField.get(null)).clear();

        final Field globalDataField = TestContext.class.getDeclaredField("_globalData");
        globalDataField.setAccessible(true);
        globalDataField.set(TestContext.getCurrent(), null);
    }

    @Test
    public void testResolve() throws Throwable
    {
        final HashMap<String, String> tcData = new HashMap<String, String>();
        final HashMap<String, String> globalData = new HashMap<String, String>();

        globalData.put("foo", "bar");
        globalData.put("gtd", "default value");
        tcData.put("foo", "a barbar");

        try (MockedStatic<TestDataUtils> testDataUtilsMock = Mockito.mockStatic(TestDataUtils.class))
        {
            testDataUtilsMock.when(TestDataUtils::getGlobalTestData).thenReturn(globalData);
            testDataUtilsMock.when(() -> TestDataUtils.getTestData(TestInstance.class)).thenReturn(tcData);
            testDataUtilsMock.when(() -> TestDataUtils.getPackageTestData(any(Class.class), anyString(), anyString())).thenReturn(Collections.emptyMap());

            final TestInstance testInstance = new TestInstance();
            testInstance.__setUpAbstractHtmlUnitScriptTestCase();

            final String s = testInstance.doResolve("gtd is ${gtd}!");
            final String s2 = testInstance.doResolve("foo is ${foo}!");

            TestContext.getCurrent().storeValue("foo", "baz");
            final String s3 = testInstance.doResolve("foo is ${foo}!");

            testInstance.__cleanUpAbstractHtmlUnitScriptTestCase();

            Assert.assertEquals("gtd is default value!", s);
            Assert.assertEquals("foo is a barbar!", s2);
            Assert.assertEquals("foo is baz!", s3);
        }
    }

    /**
     * Tests correct resolution of variables using a mix of global data, package test data and script test data. Lookup
     * order sorted by priority in ascending order:
     * <ul>
     * <li>Global Data</li>
     * <li>Effective Module Package Data</li>
     * <li>Effective Module Data</li>
     * <li>Effective Test Case Package Data</li>
     * <li>Effective Test Case Data</li>
     * <li>Stored Variables</li>
     * </ul>
     */
    @Test
    public void testResolvePackageData() throws Throwable
    {
        final HashMap<String, String> tcPackageData = new HashMap<String, String>();
        final HashMap<String, String> tcData = new HashMap<String, String>();
        final HashMap<String, String> globalData = new HashMap<String, String>();
        final HashMap<String, String> modPackageData = new HashMap<String, String>();
        final HashMap<String, String> modData = new HashMap<String, String>();

        /*
         * Set up test data maps.
         */

        tcPackageData.put("foo", "from testcase package");
        tcPackageData.put("gtd1", "pkg-lvl-1 default");
        tcData.put("foo", "from testcase");
        globalData.put("foo", "default foo value");
        globalData.put("bar", "default bar value");
        globalData.put("gtd1", "1st default");
        globalData.put("gtd2", "2nd default");
        globalData.put("gtd3", "3rd default");

        modPackageData.put("foo", "from module package");
        modPackageData.put("gtd2", "pkg-lvl-2 default");
        modPackageData.put("bar", "from module package");

        modData.put("foo", "from module");
        modData.put("bar", "from module");

        final TestModule module = new TestModule();

        final String testPkgName = TestInstance.class.getPackage().getName();
        final String modPkgName = TestModule.class.getPackage().getName();

        try (MockedStatic<TestDataUtils> testDataUtilsMock = Mockito.mockStatic(TestDataUtils.class))
        {
            testDataUtilsMock.when(TestDataUtils::getGlobalTestData).thenReturn(globalData);
            testDataUtilsMock.when(() -> TestDataUtils.getTestData(TestInstance.class)).thenReturn(tcData);
            testDataUtilsMock.when(() -> TestDataUtils.getTestData(TestModule.class)).thenReturn(modData);
            testDataUtilsMock.when(() -> TestDataUtils.getPackageTestData(any(Class.class), anyString(), eq(testPkgName))).thenReturn(tcPackageData);
            testDataUtilsMock.when(() -> TestDataUtils.getPackageTestData(any(Class.class), anyString(), eq(modPkgName))).thenReturn(modPackageData);
            testDataUtilsMock.when(() -> TestDataUtils.getPackageTestData(any(Class.class), anyString(), Mockito.not(startsWith(testPkgName)))).thenReturn(Collections.emptyMap());

            final TestInstance testInstance = new TestInstance();
            testInstance.__setUpAbstractHtmlUnitScriptTestCase();

            final String s1 = testInstance.doResolve("foo is ${foo}");
            TestContext.getCurrent().pushScope(module);

            final String s2 = module.doResolve("foo is ${foo}");

            final String s9 = module.doResolve("bar is ${bar}");

            final String s3 = module.doResolve("gtd1 is ${gtd1}");
            final String s4 = module.doResolve("gtd2 is ${gtd2}");
            final String s5 = module.doResolve("gtd3 is ${gtd3}");

            TestContext.getCurrent().popScope();

            final String s6 = testInstance.doResolve("gtd1 is ${gtd1}");
            final String s7 = testInstance.doResolve("gtd3 is ${gtd3}");
            final String s8 = testInstance.doResolve("gtd2 is ${gtd2}");

            testInstance.__cleanUpAbstractHtmlUnitScriptTestCase();

            Assert.assertEquals("foo is from testcase", s1);
            Assert.assertEquals("foo is from testcase", s2);
            Assert.assertEquals("bar is from module", s9);
            Assert.assertEquals("gtd1 is pkg-lvl-1 default", s3);
            Assert.assertEquals("gtd2 is pkg-lvl-2 default", s4);
            Assert.assertEquals("gtd3 is 3rd default", s5);
            Assert.assertEquals("gtd1 is pkg-lvl-1 default", s6);
            Assert.assertEquals("gtd3 is 3rd default", s7);
            Assert.assertEquals("gtd2 is 2nd default", s8);
        }
    }

    private class TestInstance extends AbstractHtmlUnitScriptTestCase
    {
        public String doResolve(final String s)
        {
            return resolve(s);
        }
    }
}
