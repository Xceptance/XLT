/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import org.slf4j.Logger;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * AbstractAction is the base class for all test actions. A test action is one step in a sequence of steps that comprise
 * a test case.
 *
 * @see com.xceptance.xlt.api.tests.AbstractTestCase
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractAction
{
    /**
     * The name of the action thinktime property
     */
    private static final String THINKTIME_PROPERTY_NAME = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action";

    /**
     * The name of the action thinktime deviation property
     */
    private static final String THINKTIME_DEVIATION_PROPERTY_NAME = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action.deviation";

    /**
     * The action that preceded the current action.
     */
    private AbstractAction previousAction;

    /**
     * The name under which response time measurements for this action will be recorded.
     */
    private String timerName;

    /**
     * The think time.
     */
    private int thinkTime = 0;

    /**
     * The think time deviation.
     */
    private int thinkTimeDeviation = 0;

    /**
     * State of the execution, because run() can be called only once and after run() executes we will remove the
     * previousAction to free the chain
     */
    private boolean runAlreadyExecuted = false;

    /**
     * Indicate that the prevalidation failed. In case we come back and try again.
     */
    private boolean preValidationFailed = false;

    /**
     * We keep the state of the prevalidation to avoid double calls to prevalidate because of the use of
     * prevalidateSafe.
     */
    private boolean preValidateExecuted = false;

    /**
     * Creates a new AbstractAction object and gives it the passed timer name. This constructor is typically used for an
     * intermediate action in a sequence of actions, i.e. it has a previous action.
     *
     * @param previousAction
     *            the action that preceded the current action
     * @param timerName
     *            the name of the timer that is associated with this action
     */
    protected AbstractAction(final AbstractAction previousAction, final String timerName)
    {
        this.previousAction = previousAction;
        this.timerName = timerName;

        if (timerName == null)
        {
            this.timerName = getClass().getSimpleName();
        }
        else if (timerName.isBlank())
        {
            this.timerName = getClass().getSimpleName();
            XltLogger.runTimeLogger.warn("A timer name should not be blank or empty, used \"" + this.timerName + "\" instead!");
        }

        // get the default think time parameters from the configuration
        thinkTime = XltProperties.getInstance().getProperty(THINKTIME_PROPERTY_NAME, 0);
        thinkTimeDeviation = XltProperties.getInstance().getProperty(THINKTIME_DEVIATION_PROPERTY_NAME, 0);

        // // setup the current session if necessary
        // if (previousAction == null)
        // {
        // // make sure we have a fresh session
        // Session.getCurrent().clear();
        //
        // // get a unique id for this session
        // String id = String.valueOf(System.currentTimeMillis());
        //
        // // set unique identifier for this session
        // Session.getCurrent().setID(id);
        // }

        // mention the current action for simpler debugging
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("### " + this.timerName + " constructed...");
        }
    }

    /**
     * Returns the action that was passed as the previous action to the constructor. Allows to access data collected
     * during the previous action.
     *
     * @return the previous action (may be null)
     */
    public AbstractAction getPreviousAction()
    {
        // check if state is still valid
        if (runAlreadyExecuted)
        {
            throw new RunMethodStateException("run() was already called. Action state not valid any longer.");
        }

        return previousAction;
    }

    /**
     * Returns the name under which response time measures for this action will be recorded.
     *
     * @return the timer name
     */
    public String getTimerName()
    {
        return timerName;
    }

    /**
     * Verifies whether all pre-conditions to execute the current action are fulfilled. Prevalidation will only be
     * executed once to avoid double execution. In case of JavaScript usage or DOM changes during prevalidation, it
     * might change the result, therefore we limit it to one execution.
     *
     * @throws Exception
     *             if an error occurred while verifying the pre-conditions
     */
    public abstract void preValidate() throws Exception;

    /**
     * Executes the action. What is done here is determined by the sub classes.
     *
     * @throws Exception
     *             if an error occurred while executing the action
     */
    protected abstract void execute() throws Exception;

    /**
     * Verifies whether all post-conditions after executing the current action are fulfilled.
     *
     * @throws Exception
     *             if an error occurred while verifying the post-conditions
     */
    protected abstract void postValidate() throws Exception;

    /**
     * Executes preValidate and catches all exceptions. Will return false in case of exceptions, true otherwise. This
     * can be used to determine the state of the previous page without raising any errors, e.g. for checking if we could
     * continue browsing. Means that it is not an error when the pre-conditions are not met. In case of expected
     * exceptions (java.lang.AssertionError), no message will be issued. In case of any other exception type, the
     * message will be logged but still caught and false is returned. Can be called as long as run() was not called. The
     * prevalidation will only be executed once. The return value of the first execution is preserved and returned if
     * called again.
     *
     * @return true when we have no errors, false otherwise.
     */
    public boolean preValidateSafe()
    {
        // check if state is still valid
        if (runAlreadyExecuted)
        {
            throw new RunMethodStateException("Cannot execute preValidateSafe() because run() was already executed.");
        }

        // check if we already executed the prevalidation, use cached result in that case
        if (preValidateExecuted)
        {
            if (XltLogger.runTimeLogger.isDebugEnabled())
            {
                XltLogger.runTimeLogger.debug("# " + timerName + " - preValidateSafe() was already called");
            }

            return !preValidationFailed;
        }

        preValidationFailed = false;
        try
        {
            if (XltLogger.runTimeLogger.isDebugEnabled())
            {
                XltLogger.runTimeLogger.debug("# " + timerName + " - preValidateSafe()");
            }

            preValidateExecuted = true;

            preValidate();
        }
        catch (final AssertionError ae)
        {
            // this error type is expected because we did not find what we were looking for
            if (XltLogger.runTimeLogger.isDebugEnabled())
            {
                XltLogger.runTimeLogger.debug("# " + timerName + " - preValidate() failed:", ae);
            }

            preValidationFailed = true;
        }
        catch (final Throwable e)
        {
            // this is somehow unexpected, report it
            XltLogger.runTimeLogger.warn("Unexpected error during prevalidation step, continuing without reporting an error", e);
            preValidationFailed = true;
        }

        return !preValidationFailed;
    }

    /**
     * Runs the current action. This includes:
     * <ol>
     * <li>waiting the specified thinking time by calling executeThinkTime() if and only if this is not the 1st
     * action</li>
     * <li>checking the pre-conditions by calling preValidate()</li>
     * <li>executing the current action by calling execute()</li>
     * <li>checking the post-conditions by calling postValidate()</li>
     * </ol>
     * This method can only be called once.
     *
     * @throws Throwable
     *             if an error occurred while running the action
     */
    public void run() throws Throwable
    {
        // check if state is still valid
        if (runAlreadyExecuted)
        {
            throw new RunMethodStateException();
        }

        final SessionImpl session = SessionImpl.getCurrent();
        session.checkState();
        session.setCurrentActionName(timerName);

        boolean interrupted = false;
        Throwable t = null;

        final ActionData actionData = new ActionData(timerName);
        actionData.setTime(GlobalClock.millis());

        long start = 0;

        // only do think time if it is not the first action
        if (session.isExecuteThinkTime())
        {
            // do the think time, if everything was fine
            executeThinkTime();
        }
        else
        {
            session.setExecuteThinkTime(true);
        }

        try
        {
            try
            {
                // execute prevalidate only once
                if (!preValidateExecuted)
                {
                    try
                    {
                        preValidateExecuted = true;

                        if (XltLogger.runTimeLogger.isDebugEnabled())
                        {
                            XltLogger.runTimeLogger.debug("# " + timerName + " - preValidate()");
                        }

                        start = TimerUtils.get().getStartTime();

                        preValidate();
                    }
                    catch (final Throwable e)
                    {
                        logError("Prevalidation step failed", e);
                        preValidationFailed = true;
                        t = e;
                        throw e;
                    }
                    finally
                    {
                        if (XltLogger.runTimeLogger.isDebugEnabled())
                        {
                            XltLogger.runTimeLogger.debug(String.format("# %s - preValidate() finished after %d ms", timerName, TimerUtils.get().getElapsedTime(start)));
                        }
                    }
                }
                else
                {
                    if (XltLogger.runTimeLogger.isDebugEnabled())
                    {
                        XltLogger.runTimeLogger.debug("# " + timerName + " - preValidate() already called");
                    }

                    if (preValidationFailed)
                    {
                        // if the previous prevalidation failed, we cannot continue, because we will not see
                        // that error. So people might have called run() that accidentally.
                        throw new RunMethodStateException("Prevalidate() was already called in safe mode and failed. Check your test flow and do not call run() in case preValidateSafe() returned false.");
                    }
                }

                session.checkState();

                long runTime = 0;
                try
                {
                    if (XltLogger.runTimeLogger.isDebugEnabled())
                    {
                        XltLogger.runTimeLogger.debug("# " + timerName + " - execute()");
                    }

                    start = TimerUtils.get().getStartTime();

                    try
                    {
                        actionData.setTime(GlobalClock.millis());
                        execute();
                    }
                    finally
                    {
                        runTime = TimerUtils.get().getElapsedTime(start);
                        actionData.setRunTime((int) runTime);
                    }
                }
                catch (final Throwable e)
                {
                    logError("Execution step failed", e);
                    t = e;
                    throw e;
                }
                finally
                {
                    if (XltLogger.runTimeLogger.isDebugEnabled())
                    {
                        XltLogger.runTimeLogger.debug(String.format("# %s - execute() finished after %d ms", timerName, runTime));
                    }
                }
            }
            finally
            {
                // take the previous state off-line and therefore get rid off
                // the chained actions
                previousAction = null;
                runAlreadyExecuted = true;
            }

            session.checkState();

            if (XltLogger.runTimeLogger.isDebugEnabled())
            {
                XltLogger.runTimeLogger.debug("# " + timerName + " - postValidate()");
            }

            try
            {
                start = TimerUtils.get().getStartTime();
                postValidate();
            }
            catch (final Throwable e)
            {
                logError("Postvalidation step failed", e);
                t = e;
                throw e;
            }
            finally
            {
                if (XltLogger.runTimeLogger.isDebugEnabled())
                {
                    XltLogger.runTimeLogger.debug(String.format("# %s - postValidate() finished after %d ms", timerName,
                                                                TimerUtils.get().getElapsedTime(start)));
                }
            }
        }
        catch (final InterruptedException ie)
        {
            interrupted = true;
            t = ie;

            throw ie;
        }
        finally
        {
            final boolean failed = t != null;
            actionData.setFailed(failed);

            // only log data when thread was not interrupted to prevent inconsistencies
            if (!interrupted)
            {
                Session.getCurrent().getDataManager().logDataRecord(actionData);
                Session.getCurrent().setFailed(failed);
                ((SessionImpl) Session.getCurrent()).setFailReason(t);
            }
        }

        // clear the current action's name
        session.setCurrentActionName(null);
    }

    /**
     * Logs an error.
     *
     * @param msg
     *            the message text
     * @param e
     *            the error that was caught
     */
    private void logError(final String msg, final Throwable e)
    {
        final Session session = Session.getCurrent();
        final String logMessage = String.format("%s (user: '%s', output: '%s'): %s", msg, session.getUserID(), session.getID(),
                                                e.getMessage());

        XltLogger.runTimeLogger.error(logMessage, e);
    }

    /**
     * Waits for the defined thinking time.
     */
    protected void executeThinkTime() throws InterruptedException
    {
        final long resultingThinkTime = Math.max(0, XltRandom.nextIntWithDeviation(thinkTime, thinkTimeDeviation));

        // log only if active to safe string building
        final Logger logger = XltLogger.runTimeLogger;
        if (logger.isInfoEnabled())
        {
            logger.info("Executing action think time wait (" + resultingThinkTime + " ms)...");
        }

        if (resultingThinkTime > 0)
        {
            Thread.sleep(resultingThinkTime);
        }
    }

    /**
     * Returns the currently used think time.
     *
     * @return the think time
     */
    public long getThinkTime()
    {
        return thinkTime;
    }

    /**
     * Returns the currently used think time deviation.
     *
     * @return the think time deviation
     */
    public long getThinkTimeDeviation()
    {
        return thinkTimeDeviation;
    }

    /**
     * Sets the new think time. Uses only the least 32 bit of the argument value a new value!
     *
     * @param thinkTime
     *            the think time to set
     */
    public void setThinkTime(final long thinkTime)
    {
        this.thinkTime = (int) thinkTime;
    }

    /**
     * Sets the new think time deviation. Uses only the least 32 bit of the argument value a new value!
     *
     * @param thinkTimeDeviation
     *            the think time deviation to set
     */
    public void setThinkTimeDeviation(final long thinkTimeDeviation)
    {
        this.thinkTimeDeviation = (int) thinkTimeDeviation;
    }

    /**
     * Sets the new think time.
     *
     * @param thinkTime
     *            the think time to set
     */
    public void setThinkTime(final int thinkTime)
    {
        this.thinkTime = thinkTime;
    }

    /**
     * Sets the new think time deviation.
     *
     * @param thinkTimeDeviation
     *            the think time deviation to set
     */
    public void setThinkTimeDeviation(final int thinkTimeDeviation)
    {
        this.thinkTimeDeviation = thinkTimeDeviation;
    }

    /**
     * Sets the action's timer name.
     *
     * @param timerName
     *            the timer name
     */
    public void setTimerName(final String timerName)
    {
        this.timerName = timerName;
    }
}
