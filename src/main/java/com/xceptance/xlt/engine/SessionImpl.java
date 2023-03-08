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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.runners.model.MultipleFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.actions.AbstractAction;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.NetworkDataManager;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.SessionShutdownListener;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.metrics.Metrics;
import com.xceptance.xlt.engine.resultbrowser.ActionInfo;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * The {@link SessionImpl} class represents one run of a certain test case. Multiple threads running the same test case
 * will get different sessions. A session is the anchor that holds all the pages requested during that very test run and
 * all statistics recorded. A session may be re-used across different test runs only if the session is cleared between
 * two test runs.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class SessionImpl extends Session
{
    /**
     * The name of the transaction timeout property.
     */
    private static final String PROP_MAX_TRANSACTION_TIMEOUT = XltConstants.XLT_PACKAGE_PATH + ".maximumTransactionRunTime";

    /**
     * The default transaction timeout [ms], currently 15 minutes.
     */
    private static final int DEFAULT_TRANSACTION_TIMEOUT = 15 * 60 * 1000;

    /**
     * The log facility.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SessionImpl.class);

    /**
     * The result dir property key
     */
    private static final String RESULT_DIR_PROPERTY = XltConstants.XLT_PACKAGE_PATH + ".result-dir";

    /**
     * Constant for an unknown agent name.
     */
    private static final String UNKNOWN_AGENT_ID = "UnknownAgent";

    /**
     * Constant for an unknown user name.
     */
    private static final String UNKNOWN_USER_NAME = "UnknownUser";

    /**
     * The Session instances keyed by thread group.
     */
    private static final Map<ThreadGroup, SessionImpl> sessions = new ConcurrentHashMap<ThreadGroup, SessionImpl>(101);

    /**
     * Name of the removeUserInfoFromURL property.
     */

    /**
     * Returns the Session instance for the calling thread. If no such instance exists yet, it will be created.
     *
     * @return the Session instance for the calling thread
     */
    public static SessionImpl getCurrent()
    {
        return getSessionForThread(Thread.currentThread());
    }

    /**
     * Removes the Session instance for the calling thread. Typically, sessions are reused, so this method is especially
     * useful for testing purposes.
     *
     * @return the Session instance just removed
     */
    public static SessionImpl removeCurrent()
    {
        return sessions.remove(Thread.currentThread().getThreadGroup());
    }

    /**
     * Returns the Session instance for the given thread. If no such instance exists yet, it will be created.
     *
     * @return the Session instance for the given thread
     */
    public static SessionImpl getSessionForThread(final Thread thread)
    {
        final ThreadGroup threadGroup = thread.getThreadGroup();

        if (threadGroup == null)
        {
            // the thread died in between so there is no session
            return null;
        }
        else
        {
            SessionImpl s = sessions.get(threadGroup);

            if (s == null)
            {
                synchronized (threadGroup)
                {
                    // check again because two threads might have waited at the
                    // sync block and the first one created the session already
                    s = sessions.get(threadGroup);
                    if (s == null)
                    {
                        s = new SessionImpl(XltPropertiesImpl.getInstance());
                        sessions.put(threadGroup, s);
                    }
                }
            }

            return s;
        }
    }

    /**
     * Is the transactiontimer enabled?
     */
    public final boolean isTransactionExpirationTimerEnabled;

    /**
     * The absolute instance number of the current user.
     */
    private int absoluteUserNumber;

    /**
     * The ID of the current agent.
     */
    private String agentID;

    /**
     * The number (or index) of the current agent. This value ranges from 0...(n-1), where n denotes the total number of
     * configured agents.
     */
    private int agentNumber;

    /**
     * The session-specific request statistics.
     */
    private final DataManagerImpl dataManagerImpl;

    /**
     * Indicates whether or not there were errors during the session. This includes network problems as well as page
     * validation errors.
     */
    private boolean failed;

    /**
     * The fail reason (bound to {@link #failed}.
     */
    private Throwable t;

    /**
     * The session's ID.
     */
    private String id;

    /**
     * Whether we are in a load test or a functional test.
     */
    private boolean loadTest;

    /**
     * The session-specific request history.
     */
    private final RequestHistory requestHistory;

    /**
     * Network data manager.
     */
    private final NetworkDataManagerImpl networkDataManagerImpl;

    /**
     * The results directory for this session.
     */
    private Path resultDir;

    /**
     * The registered shutdown listeners.
     */
    private final List<SessionShutdownListener> shutdownListeners;

    /**
     * The total count of agents that take part in a load test.
     */
    private int totalAgentCount;

    /**
     * The total number of users, independent of the user type.
     */
    private int totalUserCount;

    /**
     * The number of users with the same type as the current user.
     */
    private int userCount;

    /**
     * The name of the current user.
     */
    private String userName;

    /**
     * The instance number of the current user.
     */
    private int userNumber;

    /**
     * The name of the current action, null if not in an action.
     */
    private String actionName;

    /**
     * The name of the action that failed.
     */
    private String failedActionName;

    /**
     * Maps the start time of a (WebDriver) action to the action name.
     */
    private final NavigableMap<Long, ActionInfo> webDriverActionStartTimes = new TreeMap<Long, ActionInfo>();

    /**
     * The timer task that interrupts the current transaction when the transaction timeout has expired.
     */
    private TimerTask transactionExpirationTimerTask;

    /**
     * Indicates whether or not the session was marked as expired. This happens only when the shutdown period is over.
     */
    private boolean sessionExpired;

    /**
     * Indicates whether or not the session's current transaction was marked as expired. This happens only when the
     * maximum permitted run time of a transaction is reached.
     */
    private boolean transactionExpired;

    /**
     * The fully qualified class name of the test case to which this session belongs.
     */
    private String testCaseClassName;

    /**
     * Used in {@link AbstractAction#run()} to check whether we have to wait the think time or not. Its primary use is
     * to avoid unnecessary think times.
     */
    private boolean executeThinkTime;

    /**
     * How long are transactions at max. This time us used by the transaction limiter.
     */
    private final long transactionTimeout;

    /**
     * Setup for testing purposes where most data points are just empty
     */
    protected SessionImpl()
    {
        this.isTransactionExpirationTimerEnabled = false;
        this.dataManagerImpl = null;
        this.requestHistory = null;
        this.networkDataManagerImpl = null;
        this.shutdownListeners = null;
        this.transactionTimeout = 0;
    }
    /**
     * Creates a new Session object.
     */
    public SessionImpl(final XltPropertiesImpl properties)
    {
        // set default values in case we run from Eclipse and the test is not
        // derived from AbstractTestCase
        id = String.valueOf(GlobalClock.get().millis());

        userCount = 1;
        userName = UNKNOWN_USER_NAME;
        userNumber = 0;
        absoluteUserNumber = 0;
        totalUserCount = 1;
        loadTest = false;
        executeThinkTime = false;
        // webDriverActionName = null; // this is Java default, so no reason to set

        agentID = UNKNOWN_AGENT_ID;
        agentNumber = 0;
        totalAgentCount = 1;

        // create the session-specific helper objects
        dataManagerImpl = new DataManagerImpl(this, Metrics.getInstance());
        shutdownListeners = new ArrayList<SessionShutdownListener>();
        networkDataManagerImpl = new NetworkDataManagerImpl();

        // create more session-specific helper objects
        requestHistory = new RequestHistory(this, properties);

        this.isTransactionExpirationTimerEnabled = properties.getProperty(this, XltConstants.XLT_PACKAGE_PATH + ".abortLongRunningTransactions")
            .map(Boolean::valueOf)
            .orElse(false);
        this.transactionTimeout = properties.getProperty(this, PROP_MAX_TRANSACTION_TIMEOUT)
            .flatMap(ParseNumbers::parseOptionalInt)
            .orElse(DEFAULT_TRANSACTION_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addShutdownListener(final SessionShutdownListener listener)
    {
        shutdownListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        // make sure the session is cleared correctly in any case
        try
        {
            if (actionDirector != null)
            {
                actionDirector.shutdown();
            }

            // call back any shutdown listeners
            for (final SessionShutdownListener listener : new ArrayList<SessionShutdownListener>(shutdownListeners))
            {
                listener.shutdown();
            }

            // dump the history if any
            if (requestHistory != null)
            {
                requestHistory.dumpToDisk();
            }
        }
        finally
        {
            // clear the session
            networkDataManagerImpl.clear();
            if (requestHistory != null)
            {
                requestHistory.clear();
            }
            shutdownListeners.clear();

            failed = false;
            t = null;
            resultDir = null;
            actionDirector = null;
            actionName = null;
            // failedActionName = null;
            // id = null;
            webDriverActionStartTimes.clear();
            executeThinkTime = false;
            testInstance = null;
            transactionTimer = null;  // just for safety's sake
            valueLog.clear();

            // we cannot reset the name, because the session is recycled over and over again but never fully inited by the load test framework again
            //userName = UNKNOWN_USER_NAME;

            dataManagerImpl.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAbsoluteUserNumber()
    {
        return absoluteUserNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentID()
    {
        return agentID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAgentNumber()
    {
        return agentNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataManagerImpl getDataManager()
    {
        return dataManagerImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getID()
    {
        return id;
    }

    /**
     * Returns the session's request history.
     *
     * @return the request history
     */
    public RequestHistory getRequestHistory()
    {
        return requestHistory;
    }

    /**
     * Returns the session's results directory.
     *
     * @return the result directory
     * @throws IOException
     * @throws
     */
    public Path getResultsDirectory()
    {
        // no result defined yet
        if (resultDir == null)
        {
            // get result-dir property value
            String resultDirName = XltConstants.RESULT_ROOT_DIR;
            if (!isLoadTest())
            {
                final String propVal = XltProperties.getInstance().getProperty(RESULT_DIR_PROPERTY, "");
                // invalid property value -> log message
                if (propVal.length() == 0)
                {
                    XltLogger.runTimeLogger.warn("No result dir defined. Will use default result directory 'results'.");
                }
                else
                {
                    resultDirName = propVal;
                }
            }

            // convert illegal characters potentially contained in user name
            final String cleanUserName = FileUtils.replaceIllegalCharsInFileName(userName);

            // create new file handle for result directory rooted at the
            // user name directory which itself is rooted at the configured
            // result dir
            //            resultDir = new File(new File(resultDirName, cleanUserName), String.valueOf(userNumber));
            resultDir = Path.of(resultDirName, cleanUserName, String.valueOf(userNumber));

            if (!Files.exists(resultDir))
            {
                // mkdirs() is not thread-safe
                synchronized (SessionImpl.class)
                {
                    try
                    {
                        Files.createDirectories(resultDir);
                        return resultDir;
                    }
                    catch (IOException e)
                    {
                        XltLogger.runTimeLogger.error("Cannot create file for output of timer: "
                            + resultDir.toString(), e);

                        return null;
                    }
                }
            }
        }

        return resultDir;
    }

    /**
     * Returns the fully qualified class name of the test case to which this session belongs.
     *
     * @return the test class name
     */
    @Override
    public String getTestCaseClassName()
    {
        return testCaseClassName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalAgentCount()
    {
        return totalAgentCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalUserCount()
    {
        return totalUserCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUserCount()
    {
        return userCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserID()
    {
        return userName + "-" + userNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName()
    {
        return userName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUserNumber()
    {
        return userNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public String getWebDriverActionName()
    {
        return getCurrentActionName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFailed()
    {
        return failed;
    }

    /**
     * Set the session's failure reason.
     */
    public void setFailReason(final Throwable t)
    {
        this.t = t;
    }

    /**
     * Returns the session's failure reason if any.
     *
     * @return the session's failure reason or <code>null</code>
     */
    public Throwable getFailReason()
    {
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadTest()
    {
        return loadTest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeShutdownListener(final SessionShutdownListener listener)
    {
        shutdownListeners.remove(listener);
    }

    /**
     * Sets the number of the currently running test user. This value ranges from 0...(n-1), where n denotes the total
     * number of configured test users, independent of their respective user type.
     *
     * @param number
     *            the number to set
     */
    public void setAbsoluteUserNumber(final int number)
    {
        absoluteUserNumber = number;
    }

    /**
     * Sets the new value of the 'agentID' attribute.
     *
     * @param agentID
     *            the new agentID value
     */
    public void setAgentID(final String agentID)
    {
        this.agentID = agentID;
    }

    /**
     * Sets the new value of the 'agentNumber' attribute.
     *
     * @param agentNumber
     *            the new agentNumber value
     */
    public void setAgentNumber(final int agentNumber)
    {
        this.agentNumber = agentNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFailed(final boolean value)
    {
        failed = value;
        if (failed)
        {
            setFailedActionName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setID(final String id)
    {
        this.id = id;
    }

    /**
     * Sets whether the current test session is executed in the context of a functional test or a load test.
     *
     * @param loadTest
     *            whether or not we are in a load test
     */
    public void setLoadTest(final boolean loadTest)
    {
        this.loadTest = loadTest;
    }

    /**
     * Sets the fully qualified class name of the test case to which this session belongs.
     *
     * @param className
     *            the class name
     */
    public void setTestCaseClassName(final String className)
    {
        testCaseClassName = className;
    }

    /**
     * Sets the new value of the 'totalAgentCount' attribute.
     *
     * @param totalAgentCount
     *            the new totalAgentCount value
     */
    public void setTotalAgentCount(final int totalAgentCount)
    {
        this.totalAgentCount = totalAgentCount;
    }

    /**
     * Sets the total count of test users running during a test. This includes all users of all types.
     *
     * @param count
     *            the count to set
     */
    public void setTotalUserCount(final int count)
    {
        totalUserCount = count;
    }

    /**
     * Sets the number of users which are of the same type as this user.
     *
     * @param userCount
     *            the userCount to set
     */
    public void setUserCount(final int userCount)
    {
        this.userCount = userCount;
    }

    /**
     * Sets the name of the user if it has changed or was not set before.
     *
     * @param userName
     *            the userName to set
     */
    public void setUserName(final String userName)
    {
        if (this.userName == null || !this.userName.equals(userName))
        {
            this.userName = userName;
            resultDir = null;
            dataManagerImpl.close();
        }
    }

    /**
     * Sets the user name if it has not been set so far.
     *
     * @param userName
     *            the userName to set
     */
    public void setUserNameIfNotSet(final String userName)
    {
        if (UNKNOWN_USER_NAME.equals(this.userName))
        {
            setUserName(userName);
        }
    }

    /**
     * Sets the user's instance number.
     *
     * @param userNumber
     *            the userNumber to set
     */
    public void setUserNumber(final int userNumber)
    {
        this.userNumber = userNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NetworkDataManager getNetworkDataManager()
    {
        return networkDataManagerImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void setWebDriverActionName(final String webDriverActionName)
    {
        startAction(webDriverActionName);
    }

    /**
     * WebDriver action director instance which is used in {@link #startAction(String)}.
     */
    private WebDriverActionDirector actionDirector;

    private ActionInfo actionInfo;

    /**
     * Sets the WebDriver action director which is used in {@link #startAction(String)}.
     *
     * @param director
     *            the WebDriver action director
     */
    public void setWebDriverActionDirector(final WebDriverActionDirector director)
    {
        actionDirector = director;
    }

    /**
     * Returns the WebDriver action director which is used in {@link #startAction(String)}.
     *
     * @return the WebDriver action director
     */
    public WebDriverActionDirector getWebDriverActionDirector()
    {
        return actionDirector;
    }

    /**
     * Performs any tasks necessary at the beginning of a transaction. To be called by the test framework when a new
     * transaction started.
     */
    public void transactionStarted()
    {
        // Add a timer task to mark this transaction as expired after the transaction timeout.
        if (isTransactionExpirationTimerEnabled)
        {
            // needs to create a new timer task each time as timer tasks cannot be reused
            transactionExpirationTimerTask = TransactionExpirationTimer.addTimerTask(getUserID(), transactionTimeout);
        }

        startTransaction();
    }

    /**
     * Performs any tasks necessary at the end of a transaction. To be called by the test framework when a transaction
     * finished.
     */
    public void transactionFinished()
    {
        // Cancel the timer task that interrupts this transaction after the transaction timeout.
        if (isTransactionExpirationTimerEnabled && transactionExpirationTimerTask != null)
        {
            transactionExpirationTimerTask.cancel();
        }

        stopTransaction();
    }

    /**
     * Returns the session's expired state.
     *
     * @return the expired state
     */
    public boolean wasMarkedAsExpired()
    {
        return sessionExpired;
    }

    /**
     * Mark the session as expired. Cannot be undone.
     */
    public void markAsExpired()
    {
        sessionExpired = true;
    }

    /**
     * Checks whether the current transaction should be aborted. This will be the case if the current transaction
     * exceeds the maximum permitted run time or if the load test has finished. In either case, a
     * {@link TransactionInterruptedException} will be thrown.
     *
     * @throws TransactionInterruptedException
     *             if the current transaction is to be aborted
     */
    public void checkState() throws TransactionInterruptedException
    {
        if (sessionExpired)
        {
            throw new TransactionInterruptedException("Load test has finished");
        }
        else if (transactionExpired)
        {
            throw new TransactionInterruptedException("Transaction exceeded the run time limit");
        }
    }

    /**
     * Returns a mapping from start times to action names for all created WebDriver actions.
     *
     * @return the mapping
     * @see #startAction(String)
     */
    public NavigableMap<Long, ActionInfo> getWebDriverActionStartTimes()
    {
        return webDriverActionStartTimes;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentActionName()
    {
        return actionName;
    }

    /**
     * Sets the name of the current action. Not necessary when using {@link #startAction(String)}, i.e. for WebDriver
     * actions.
     *
     * @param actionName
     *            the action name
     */
    public void setCurrentActionName(final String actionName)
    {
        this.actionName = actionName;
    }

    public ActionInfo getCurrentActionInfo()
    {
        return this.actionInfo;
    }

    /**
     * {@inheritDoc}
     */
    public void startAction(final String actionName)
    {
        // check whether the current session/transaction was marked as expired
        checkState();

        // parameter check
        ParameterCheckUtils.isNotNullOrEmpty(actionName, "actionName");

        // let action director start a new action
        if (actionDirector == null)
        {
            actionDirector = new WebDriverActionDirector();
        }

        actionDirector.startNewAction(actionName);

        // remember the action's name and start time
        this.actionName = actionName;

        actionInfo = new ActionInfo();
        actionInfo.name = actionName;

        webDriverActionStartTimes.put(GlobalClock.get().millis(), actionInfo);
    }

    /**
     * {@inheritDoc}
     */
    public void stopAction()
    {
        // check whether the current session/transaction was marked as expired
        checkState();

        // let action director finish the action
        if (actionDirector != null)
        {
            actionDirector.finishCurrentAction();
        }

        // clear the action name
        actionName = null;
    }

    /**
     * Returns the name of the action that caused the test case to fail.
     *
     * @return the action name, or <code>null</code> if there was no failed action
     */
    public String getFailedActionName()
    {
        return failedActionName;
    }

    /**
     * Sets the name of the current action as the action that caused the test case to fail. If no action is currently
     * open, sets the empty string. This method should be called only once.
     */
    private void setFailedActionName()
    {
        // set the failed action name, but only once
        if (failedActionName == null)
        {
            failedActionName = actionName == null ? "" : actionName;
        }
    }

    /**
     * Resets an internally set failed action name to <code>null</code>. This must be called explicitly as the failed
     * name is not cleared in {@link #clear()}. This way it survives the end of a session and can be reported
     * appropriately, but needs to be cleared before a new session starts.
     */
    public void clearFailedActionName()
    {
        this.failedActionName = null;
    }

    /**
     * @return boolean, indicating whether do the think time or not
     * @see AbstractAction#run()
     */
    public boolean isExecuteThinkTime()
    {
        return executeThinkTime;
    }

    /**
     * Sets the boolean that is used to decide whether do the think time or not
     *
     * @param executeThinkTime
     * @see AbstractAction#run()
     */
    public void setExecuteThinkTime(boolean executeThinkTime)
    {
        this.executeThinkTime = executeThinkTime;
    }

    /** The currently running test instance. */
    private Object testInstance;

    /**
     * Returns the currently running test instance.
     *
     * @return test instance
     */
    public Object getTestInstance()
    {
        return testInstance;
    }

    /**
     * Sets the currently running test instance.
     *
     * @param instance
     *            the test instance
     */
    public void setTestInstance(final Object instance)
    {
        testInstance = instance;
    }

    /**
     * The session's value log, a storage for session-specific test parameters and result data.
     */
    private final Map<String, Object> valueLog = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getValueLog()
    {
        return valueLog;
    }

    // stuff used for TransactionData recording

    /**
     * A {@link TransactionTimer} that holds the time at which {@linkplain #startTransaction() transaction data
     * recording was started} (or {@code null} if there is no transaction has been started yet)
     */
    private TransactionTimer transactionTimer = null;

    /**
     * Starts transaction data recording. This will clear any recorded failure information and start a new stop-watch
     * for the transaction.
     */
    public void startTransaction()
    {
        setFailed(false);
        setFailReason(null);
        clearFailedActionName();

        transactionTimer = new TransactionTimer();
    }

    /**
     * Tells whether transaction data recording is still in progress (i.e. has been {@linkplain #startTransaction()
     * started} but not {@linkplain #stopTransaction() stopped}, yet.
     *
     * @return {@code true} iff transaction data recording is still in progress
     */
    public boolean isTransactionPending()
    {
        return transactionTimer != null;
    }

    /**
     * Concludes transaction data recording.
     * <p>
     * This method may only be called while {@linkplain #isTransactionPending() transaction data recording is in
     * progress} (otherwise a {@link RuntimeException} will be thrown).
     */
    public void stopTransaction() throws RuntimeException
    {
        if (isTransactionPending())
        {
            final TransactionData transactionData = new TransactionData(getUserName());
            transactionData.setRunTime((int) transactionTimer.getRuntime());
            transactionData.setTime(transactionTimer.getStartTime());
            transactionData.setFailed(hasFailed());
            transactionData.setFailureStackTrace(extractFirstFailure(getFailReason()));
            transactionData.setFailedActionName(getFailedActionName());

            if (hasFailed())
            {
                transactionData.setTestUserNumber(String.valueOf(getUserNumber()));
                transactionData.setDirectoryName(getID());
            }

            transactionTimer = null;

            if (!wasMarkedAsExpired())
            {
                dataManagerImpl.logDataRecord(transactionData);
            }
        }
    }

    /**
     * For a {@link MultipleFailureException}, returns the first of the encapsulated failures. Otherwise just returns
     * the Throwable itself
     *
     * @param throwable
     * @return
     */
    private static Throwable extractFirstFailure(final Throwable throwable)
    {
        if (throwable instanceof MultipleFailureException)
        {
            return ((MultipleFailureException) throwable).getFailures().get(0);
        }
        else
        {
            return throwable;
        }
    }

    /**
     * The idea is that an instance of this remembers the time at which it is created and offers methods to retrieve
     * that time ({@link #getStartTime()}) and the time passed since it has been created ({@link #getRuntime()})
     */
    public static class TransactionTimer
    {
        private final long globalStartTime = GlobalClock.get().millis();

        private final long localStartTime = TimerUtils.get().getStartTime();

        public long getStartTime()
        {
            return globalStartTime;
        }

        public long getRuntime()
        {
            return TimerUtils.get().getElapsedTime(localStartTime);
        }
    }

    @Override
    public void setFailed()
    {
        setFailed(true);
    }

    @Override
    public void setNotFailed()
    {
        failed = false;
        clearFailedActionName();
    }
}
