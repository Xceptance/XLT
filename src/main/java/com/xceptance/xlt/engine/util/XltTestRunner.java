/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * A special JUnit4 runner implementation for XLT test cases. The sole purpose of this runner is to mark the current XLT
 * session as failed if the test method was aborted with an exception. An XLT test case class must be annotated to be
 * run with this runner. This is not necessary for test case classes that inherit from {@link AbstractTestCase}.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XltTestRunner extends BlockJUnit4ClassRunner
{
    /**
     * A wrapper around a JUnit {@link Statement} that marks the current session as failed when an exception/error has
     * been thrown during the execution of the wrapped statement.
     */
    private static class StatementWrapper extends Statement
    {
        /**
         * The wrapped statement.
         */
        private final Statement statement;

        /**
         * Constructor.
         *
         * @param statement
         *            the statement
         */
        public StatementWrapper(final Statement statement)
        {
            this.statement = statement;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void evaluate() throws Throwable
        {
            final SessionImpl session = SessionImpl.getCurrent();

            session.transactionStarted();

            try
            {
                statement.evaluate();

                // #3314: Inconsistencies in the results when catching exceptions
                // If we have come here, the test case finished without exception, so clear any error state (left behind
                // by a thrown but later caught exception) that might trigger a transaction error in the results.
                session.setFailed(false);
                session.setFailReason(null);
            }
            catch (final Throwable t)
            {
                // do not mark the session as failed if the session has simply expired (interrupted at the end of the
                // load test)
                final boolean sessionFailed = !session.wasMarkedAsExpired();
                session.setFailed(sessionFailed);

                // in case the session really failed, do some post-processing
                if (sessionFailed)
                {
                    // remember the throwable as the cause
                    session.setFailReason(t);

                    // finally rethrow
                    throw t;
                }
            }
            finally
            {
                session.transactionFinished();

                XltLogger.runTimeLogger.info("Cleaning up ...");

                session.clear();
            }
        }
    }

    /**
     * Constructor.
     *
     * @param klass
     *            test class
     * @throws InitializationError
     *             thrown on initialization failure
     */
    public XltTestRunner(final Class<?> klass) throws InitializationError
    {
        super(klass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement methodBlock(final FrameworkMethod method)
    {
        return new StatementWrapper(super.methodBlock(method));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test)
    {
        return new InvokeMethod(method, test);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement statement)
    {
        final List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
        return (befores == null || befores.isEmpty()) ? statement : new RunBefores(statement, befores, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement statement)
    {
        final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
        return (afters == null || afters.isEmpty()) ? statement : new RunAfters(statement, afters, target);
    }

    /**
     * Same as {@linkplain org.junit.internal.runners.statements.RunBefores} but ensures that any potential open action
     * is stopped after the execution of each <code>@Before</code> method.
     */
    private static class RunBefores extends Statement
    {
        private final Statement next;

        private final Object target;

        private final List<FrameworkMethod> befores;

        public RunBefores(final Statement next, final List<FrameworkMethod> befores, final Object target)
        {
            this.next = next;
            this.befores = befores;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable
        {
            for (final FrameworkMethod before : befores)
            {
                try
                {
                    before.invokeExplosively(target);
                }
                catch (final Throwable t)
                {
                    Session.getCurrent().setFailed(true);
                    throw t;
                }
                finally
                {
                    Session.getCurrent().stopAction();
                }
            }

            next.evaluate();
        }
    }

    /**
     * Same as {@linkplain org.junit.internal.runners.statements.InvokeMethod} but ensures that any potential open
     * action is stopped after the execution of the <code>@Test</code> method.
     */
    private static class InvokeMethod extends Statement
    {
        private final FrameworkMethod testMethod;

        private final Object target;

        private InvokeMethod(final FrameworkMethod testMethod, final Object target)
        {
            this.testMethod = testMethod;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable
        {
            try
            {
                testMethod.invokeExplosively(target);
            }
            catch (final Throwable t)
            {
                Session.getCurrent().setFailed(true);
                throw t;
            }
            finally
            {
                Session.getCurrent().stopAction();
            }
        }
    }

    /**
     * Same as {@linkplain org.junit.internal.runners.statements.RunAfters} but ensures that any potential open action
     * is stopped after the execution of each <code>@After</code> method.
     */
    private static class RunAfters extends Statement
    {
        private final Statement _next;

        private final List<FrameworkMethod> _afters;

        private final Object _target;

        private RunAfters(final Statement next, final List<FrameworkMethod> afters, final Object target)
        {
            _next = next;
            _afters = afters;
            _target = target;
        }

        @Override
        public void evaluate() throws Throwable
        {
            final List<Throwable> errors = new ArrayList<Throwable>();
            try
            {
                _next.evaluate();
            }
            catch (final Throwable e)
            {
                errors.add(e);
            }
            finally
            {
                for (final FrameworkMethod each : _afters)
                {
                    try
                    {
                        each.invokeExplosively(_target);
                    }
                    catch (final Throwable e)
                    {
                        Session.getCurrent().setFailed(true);
                        errors.add(e);
                    }
                    finally
                    {
                        Session.getCurrent().stopAction();
                    }
                }
            }
            MultipleFailureException.assertEmpty(errors);
        }
    }
}
