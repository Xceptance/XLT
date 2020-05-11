/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.actions;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.TestWrapper;
import com.xceptance.xlt.api.engine.Session;

/**
 * Test the {@link AbstractAction} implementation.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractActionTest extends AbstractXLTTestCase
{
    /**
     * Test instance of TestAction that fails on <code>preValidate()</code>.
     */
    private TestAction preFail;

    /**
     * Test instance of TestAction that fails on <code>postValidate()</code>.
     */
    private TestAction postFail;

    /**
     * Test instance of TestAction that fails on <code>execute()</code>.
     */
    private TestAction exeFail;

    /**
     * Initialize test instances.
     */
    @Before
    public void setUp()
    {
        preFail = new TestAction()
        {
            @Override
            public void preValidate() throws Exception
            {
                Assert.fail("prevalidate");
            }
        };
        postFail = new TestAction()
        {
            @Override
            public void postValidate() throws Exception
            {
                Assert.fail("postvalidate");
            }
        };
        exeFail = new TestAction()
        {
            @Override
            public void execute() throws Exception
            {
                Assert.fail("execute");
            }
        };

    }

    /**
     * Test {@link AbstractAction#preValidateSafe()}.
     */
    @Test
    public void testPrevalidateSafe()
    {
        Assert.assertFalse(preFail.preValidateSafe());
        Assert.assertTrue(new TestAction().preValidateSafe());

        final TestAction ta = new TestAction();
        final boolean previousResult = ta.preValidateSafe();
        Assert.assertTrue(previousResult == ta.preValidateSafe());

        final TestAction ta2 = new TestAction()
        {
            @Override
            public void preValidate() throws Exception
            {
                throw new Exception(";-)");
            }
        };
        ta2.preValidateSafe();
    }

    /**
     * Tests the implementation of {@link AbstractAction#run()} by letting preValidate() throw an exception.
     * 
     * @throws Throwable
     *             thrown by run() because preValidate() failed.
     */
    @Test(expected = AssertionError.class)
    public void testRun_PreValidateFailed() throws Throwable
    {
        preFail.run();
    }

    @Test
    public void testRunWithFailedEarlierPrevalidation() throws Throwable
    {
        new TestWrapper(
                        RunMethodStateException.class,
                        "Prevalidate() was already called in safe mode and failed. Check your test flow and do not call run() in case preValidateSafe() returned false.")
        {
            @Override
            protected void run() throws Throwable
            {
                preFail.preValidateSafe();
                preFail.run();
            }
        }.execute();
    }

    @Test
    public void testRunWithInterruptedException() throws Throwable
    {
        new TestWrapper(InterruptedException.class, "You interrupted me!")
        {
            @Override
            protected void run() throws Throwable
            {
                final TestAction ta = new TestAction()
                {
                    @Override
                    public void postValidate() throws Exception
                    {
                        throw new InterruptedException("You interrupted me!");
                    }
                };
                ta.run();
            }
        }.execute();
    }

    /**
     * Makes only a few simple calls to increase code coverage make you not to wonder whether code coverage is too low.
     */
    @Test
    public void testSimpleCalls()
    {
        final TestAction ta = new TestAction();
        ta.getThinkTime();
        ta.getThinkTimeDeviation();
        ta.setThinkTime(123L);
        ta.setThinkTimeDeviation(123L);
        ta.setThinkTime(123);
        ta.setThinkTimeDeviation(123);
        ta.setTimerName(null);
    }

    /**
     * Tests the implementation of {@link AbstractAction#run()} by letting execute() throw an exception.
     * 
     * @throws Throwable
     *             thrown by run() because execute() failed.
     */
    @Test(expected = AssertionError.class)
    public void testRun_ExecuteFailed() throws Throwable
    {
        exeFail.run();
    }

    /**
     * Tests the implementation of {@link AbstractAction#run()} by letting postValidate() throw an exception.
     * 
     * @throws Throwable
     *             thrown by run() because postValidate() failed.
     */
    @Test(expected = AssertionError.class)
    public void testRun_PostValidateFailed() throws Throwable
    {
        postFail.run();
    }

    /**
     * Test succeeding strategy of {@link AbstractAction#run()}.
     */
    @Test
    public void testRun_Succeeds()
    {
        try
        {
            new TestAction().run();
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link AbstractAction#getPreviousAction()} by calling it on an action that was
     * already run.
     */
    @Test(expected = RunMethodStateException.class)
    public void testGetPreviousAction_RunMethodStateException()
    {
        final TestAction action = new TestAction();
        try
        {
            action.run();
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        action.getPreviousAction();
    }

    /**
     * Tests the implementation of {@link AbstractAction#preValidateSafe()} by calling it on an action that was already
     * run.
     */
    @Test(expected = RunMethodStateException.class)
    public void testPrevalidateSafe_RunMethodStateException()
    {
        final TestAction action = new TestAction();
        try
        {
            action.run();
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        action.preValidateSafe();
    }

    /**
     * Tests the implementation of {@link AbstractAction#run()} by calling it on an action that was already run.
     */
    @Test(expected = RunMethodStateException.class)
    public void testRun_RunMethodStateException() throws Throwable
    {
        final TestAction action = new TestAction();
        try
        {
            action.run();
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        action.run();
    }

    /**
     * Tests the implementation of {@link AbstractAction} constructor, if the timer name is blank.
     */
    @Test
    public void testAbstractActionConstructorTimerNameIsBlank()
    {
        final TestAction action = new TestAction(StringUtils.EMPTY, null);
        Assert.assertEquals("TestAction", action.getTimerName());
    }

    /**
     * Tests the execution of the action think time. The first action should not execute any think time but all
     * successive should.
     * 
     * @throws Throwable
     */
    @Test
    public void testActionThinkTimeIsAppliedInBetween() throws Throwable
    {
        // Clear current session. Necessary since other tests might have been run before. 
        Session.getCurrent().clear();

        final ThinktimeAction action1 = new ThinktimeAction();
        action1.run();

        // No thinktime execution since this is the very first action
        Assert.assertFalse("Action think time executed", action1.thinktimeExecuted);

        final ThinktimeAction action2 = new ThinktimeAction();
        action2.run();

        // This is the 2nd action, thinktime should be executed
        Assert.assertTrue("Action think time not executed", action2.thinktimeExecuted);

        // Clear current session -> resets all session-scoped settings which also includes whether or not we already
        // executed any action
        Session.getCurrent().clear();

        // Although this is the 3rd action in declaration and execution order, clearing the current session causes this
        // action to be the very first action again
        final ThinktimeAction action3 = new ThinktimeAction();
        action3.run();
        // ... so thinktime should not be executed
        Assert.assertFalse("Action think time executed", action3.thinktimeExecuted);
    }

    /**
     * Dummy implementation of {@link AbstractAction}.
     * <p>
     * Used for anonymous classes of test objects inheriting from AbstractAction.
     * </p>
     * 
     * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
     */
    private class TestAction extends AbstractAction
    {
        public TestAction()
        {
            this(TestAction.class.getName(), null);
        }

        public TestAction(final String name, final AbstractAction action)
        {
            super(action, name);
        }

        @Override
        public void execute() throws Exception
        {
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        public void postValidate() throws Exception
        {
        }
    }

    private class ThinktimeAction extends TestAction
    {
        boolean thinktimeExecuted = false;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void executeThinkTime() throws InterruptedException
        {
            thinktimeExecuted = true;
            // TODO Auto-generated method stub
            super.executeThinkTime();
        }
    }
}
