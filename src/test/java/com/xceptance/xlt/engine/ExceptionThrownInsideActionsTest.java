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
package com.xceptance.xlt.engine;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.engine.util.XltTestRunner;

import util.xlt.MockDataManager;

/**
 * Tests the behavior of the XLT framework, esp. {@link XltTestRunner}, if an XLT test case throws an exception inside
 * an action. This is run for both inline-action-based test cases as well as for action-class-based test cases.
 */
@RunWith(Parameterized.class)
public class ExceptionThrownInsideActionsTest
{
    /** How the test case should behave and what results are expected */
    private static class TestConfig
    {
        final boolean failInBefore;

        final boolean failInTest;

        final boolean failInAfter1;

        final boolean failInAfter2;

        private final String[] failedActionNames;

        public TestConfig(final boolean failInBefore, final boolean failInTest, final boolean failInAfter1, final boolean failInAfter2,
                          final String... failedActionNames)
        {
            this.failInBefore = failInBefore;
            this.failInTest = failInTest;
            this.failInAfter1 = failInAfter1;
            this.failInAfter2 = failInAfter2;
            this.failedActionNames = failedActionNames;
        }
    }

    /** The test configurations. */
    @Parameters
    public static Object[] getTestConfigs()
    {
        return new Object[]
            {
                new TestConfig(false, false, false, false), new TestConfig(true, false, false, false, "Before"),
                new TestConfig(false, true, false, false, "Test"), new TestConfig(false, false, true, false, "After1"),
                new TestConfig(false, false, false, true, "After2"), new TestConfig(false, true, true, false, "Test", "After1"),
                new TestConfig(false, true, false, true, "Test", "After2"), new TestConfig(false, false, true, true, "After1", "After2"),
            };
    }

    /** The current test configuration. */
    @Parameter
    public TestConfig testConfig;

    /** The mock data manager that provides access to the logged data record. */
    private MockDataManager dataManager;

    /** Hack-ish way to make the current test configuration available to the actual test case. */
    private static TestConfig testConfiguration;

    @Before
    public void setUp()
    {
        // install the mock data manager
        final SessionImpl session = SessionImpl.getCurrent();
        dataManager = new MockDataManager(session);
        ReflectionUtils.writeInstanceField(session, "dataManagerImpl", dataManager);
    }

    @After
    public void cleanUp()
    {
        SessionImpl.removeCurrent();
    }

    /**
     * Runs the test configuration with an inline-action-based test case.
     */
    @Test
    public void inlineActionBasedTestCase() throws Throwable
    {
        runTest(InlineActionTestCase.class);
    }

    /**
     * Runs the test configuration with an action-class-based test case.
     */
    @Test
    public void actionClassBasedTestCase() throws Throwable
    {
        runTest(ActionClassTestCase.class);
    }

    private void runTest(final Class<?> testClass) throws Throwable
    {
        // run the test with the current configuration via JUnit
        testConfiguration = testConfig;
        final Result result = JUnitCore.runClasses(testClass);

        // validate
        validateJUnitResult(result, testConfig.failedActionNames);
        validateDataRecords(dataManager.dataRecords, testConfig.failedActionNames);
    }

    private void validateJUnitResult(final Result result, final String... failedActionNames)
    {
        final List<Failure> failures = result.getFailures();

        Assert.assertEquals(failedActionNames.length, failures.size());

        for (int i = 0; i < failures.size(); i++)
        {
            Assert.assertEquals(failedActionNames[i], failures.get(i).getException().getMessage());
        }
    }

    private void validateDataRecords(final List<Data> dataRecords, final String[] failedActionNames)
    {
        for (final Data record : dataRecords)
        {
            if (record instanceof TransactionData)
            {
                final TransactionData transactionData = (TransactionData) record;

                if (failedActionNames.length > 0)
                {
                    Assert.assertEquals(true, transactionData.hasFailed());
                    Assert.assertEquals(failedActionNames[0], transactionData.getFailedActionName());
                    Assert.assertTrue(StringUtils.isNotBlank(transactionData.getFailureStackTrace()));
                }
                else
                {
                    Assert.assertEquals(false, transactionData.hasFailed());
                    Assert.assertEquals(null, transactionData.getFailedActionName());
                    Assert.assertEquals(null, transactionData.getFailureStackTrace());
                }

                return;
            }
            else if (record instanceof ActionData)
            {
                final ActionData actionData = (ActionData) record;
                if (ArrayUtils.contains(failedActionNames, actionData.getName()))
                {
                    Assert.assertEquals(true, actionData.hasFailed());
                }
                // Commented out intentionally. Will be used later.
                // Assert.assertEquals(ArrayUtils.contains(actionNames, actionData.getName()), actionData.hasFailed());
            }
        }

        Assert.fail("Have not seen the final TransactionData record");
    }

    // ~~~ Dummy test case classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static abstract class BaseTestCase extends AbstractTestCase
    {
        protected abstract void executeAction(String string, boolean failInBefore) throws Throwable;

        @Before
        public void before() throws Throwable
        {
            executeAction("Before", testConfiguration.failInBefore);
        }

        @Test
        public void test() throws Throwable
        {
            executeAction("Test", testConfiguration.failInTest);
        }

        @After
        public void after1() throws Throwable
        {
            executeAction("After1", testConfiguration.failInAfter1);
        }

        @After
        public void after2() throws Throwable
        {
            executeAction("After2", testConfiguration.failInAfter2);
        }
    }

    public static class InlineActionTestCase extends BaseTestCase
    {
        @Override
        protected void executeAction(final String actionName, final boolean doFail) throws Throwable
        {
            Session.getCurrent().startAction(actionName);
            if (doFail)
            {
                throw new Exception(actionName);
            }
        }
    }

    public static class ActionClassTestCase extends BaseTestCase
    {
        @Override
        protected void executeAction(final String actionName, final boolean failInBefore) throws Throwable
        {
            new DummyAction(actionName, failInBefore).run();
        }
    }

    private static class DummyAction extends AbstractHtmlPageAction
    {
        private final boolean doFail;

        protected DummyAction(final String actionName, final boolean doFail)
        {
            super(actionName);
            this.doFail = doFail;
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
            if (doFail)
            {
                throw new Exception(getTimerName());
            }
        }

        @Override
        protected void postValidate() throws Exception
        {
        }
    }
}
