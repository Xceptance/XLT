package com.xceptance.xlt.agent;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.agentcontroller.TestUserStatus;
import com.xceptance.xlt.api.engine.DataManager;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.engine.DataManagerImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Load test runner.
 * <p>
 * Group of autonomous threads that execute one or more tests according to their configuration.
 * </p>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class LoadTestRunner extends Thread
{
    /**
     * Class logger instance.
     */
    private static final Log log = LogFactory.getLog(LoadTestRunner.class);

    /**
     * Configuration.
     */
    private final TestUserConfiguration config;

    /**
     * Status of runner.
     */
    private final TestUserStatus status;

    /**
     * Timer used for synchronization and test execution controlling.
     */
    private AbstractExecutionTimer timer;

    /**
     * Some information about this and other agents.
     */
    private final AgentInfo agentInfo;

    /**
     * Whether this runner has been aborted.
     */
    private volatile boolean aborted;

    /**
     * Creates a new LoadTestRunner object for the given load test configuration. Typically, multiple runners are
     * started for one test case configuration, so the number of the current runner is passed as well.
     * 
     * @param config
     *            the load test configuration
     * @param agentInfo
     *            load test agent information
     * @param timer
     *            the execution timer that controls this load test runner
     */
    public LoadTestRunner(final TestUserConfiguration config, final AgentInfo agentInfo, final AbstractExecutionTimer timer)
    {
        // create a new thread group for each LoadTestRunner as a means
        // to keep the main thread and any supporting threads together
        super(new ThreadGroup(config.getUserId()), config.getUserId());

        this.config = config;
        this.agentInfo = agentInfo;
        this.timer = timer;

        status = new TestUserStatus();
        status.setUserName(config.getUserId());
    }

    /**
     * Returns the current test case status.
     * 
     * @return the status
     */
    public TestUserStatus getTestUserStatus()
    {
        return status;
    }

    /**
     * Runs the test case as configured in the test case configuration.
     */
    @Override
    public void run()
    {
        try
        {
            final long now = GlobalClock.getInstance().getTime();

            // get and check the test case class
            Class<?> testCaseClass = null;
            try
            {
                // try to get the corresponding Java class
                testCaseClass = Class.forName(config.getTestCaseClassName());
                // class found -> validate it
                checkTestCaseClass(testCaseClass);
            }
            // no such Java class
            catch (final ClassNotFoundException cnf)
            {
                // failed to get resource URL -> indicate error
                throw new RuntimeException("Could not find java class '" + config.getTestCaseClassName() + "'.");
            }

            // get the other test case parameters
            final int iterations = config.getNumberOfIterations();
            final int warmUpPeriod = config.getWarmUpPeriod();
            final int measurementPeriod = config.getMeasurementPeriod();
            final int initialDelay = config.getInitialDelay();
            final int duration = warmUpPeriod + measurementPeriod;

            // initialize the master controller status
            status.setStartDate(now + initialDelay);
            status.setEndDate(now + initialDelay + duration);

            // initialize the session
            final SessionImpl session = (SessionImpl) Session.getCurrent();
            session.setUserCount(config.getNumberOfUsers());
            session.setUserName(config.getUserName());
            session.setUserNumber(config.getInstance());
            session.setAbsoluteUserNumber(config.getAbsoluteUserNumber());
            session.setTotalUserCount(config.getTotalUserCount());
            session.setLoadTest(true);
            session.setAgentID(agentInfo.getAgentID());
            session.setAgentNumber(agentInfo.getAgentNumber());
            session.setTotalAgentCount(agentInfo.getTotalAgentCount());

            // set the logging window
            final DataManager dataMgr = session.getDataManager();
            dataMgr.setStartOfLoggingPeriod(now + initialDelay + warmUpPeriod);
            dataMgr.setEndOfLoggingPeriod(now + initialDelay + warmUpPeriod + measurementPeriod);

            // run the test
            if (iterations != 0)
            {
                status.setMode(TestUserStatus.Mode.ITERATION);

                runIterations(testCaseClass, iterations);
            }
            else
            {
                status.setMode(TestUserStatus.Mode.TIME_PERIOD);

                if (duration != 0)
                {
                    runDuration(testCaseClass, duration);
                }
                else
                {
                    log.warn("Both number of iterations and computed duration are unspecified for test case: " +
                             config.getTestCaseClassName());
                }
            }

            // set the final state
            if (aborted)
            {
                status.setState(TestUserStatus.State.Aborted);
            }
            else
            {
                status.setPercentageComplete(100);
                status.setState(TestUserStatus.State.Finished);
            }
        }
        catch (final Exception ex)
        {
            log.error("Failed to run test as user: " + getName(), ex);

            status.setState(TestUserStatus.State.Failed);
            status.setException(ex);
        }
    }

    /**
     * Marks this load test runner as aborted.
     */
    public void setAborted()
    {
        aborted = true;
    }

    /**
     * Checks whether the test case class is acceptable, i.e., whether it has exactly one active test method. This
     * means, there can be only one method in the class that is - at the same time - annotated with "@Test" and not
     * annotated with "@Ignore". Furthermore, the class itself must not be annotated with "@Ignore"
     * 
     * @param testCaseClass
     *            the test case class
     * @throws RuntimeException
     *             if there is *not* exactly one active test method in this class
     */
    private void checkTestCaseClass(final Class<?> testCaseClass)
    {
        // check whether the test case class is annotated with @Ignore
        if (testCaseClass.isAnnotationPresent(Ignore.class))
        {
            throw new RuntimeException("Test class is annotated with @Ignore: " + testCaseClass.getName());
        }

        // check whether we have exactly one active test method
        int testMethodCount = 0;

        for (final Method method : testCaseClass.getMethods())
        {
            if (method.isAnnotationPresent(Test.class) && !method.isAnnotationPresent(Ignore.class))
            {
                testMethodCount++;
            }
        }

        if (testMethodCount != 1)
        {
            throw new RuntimeException("No or more than one active test method found in class: " + testCaseClass.getName());
        }
    }

    /**
     * Runs the passed test case repeatedly until the specified number of iterations has been reached. The test case
     * status is updated after each iteration.
     * 
     * @param testCaseClass
     *            the test case to execute
     * @param iterations
     *            the number of test iterations
     */
    private void runIterations(final Class<?> testCaseClass, final int iterations)
    {
        final String testClassName = (testCaseClass == null ? config.getTestCaseClassName() : testCaseClass.toString());
        log.info("Load test thread started (" + testClassName + " / " + iterations + " iterations)");

        for (int i = 0; i < iterations; i++)
        {
            try
            {
                status.setState(TestUserStatus.State.Waiting);
                timer.waitForNextExecution();
                status.setState(TestUserStatus.State.Running);
                runTestCase(testCaseClass, status);
            }
            catch (final InterruptedException ex)
            {
                break;
            }

            status.setPercentageComplete((i + 1) * 100 / iterations);
        }

        log.info("Load test thread finished.");
    }

    /**
     * Runs the passed test case repeatedly until the specified number of milliseconds has passed. The test case status
     * is updated after each iteration.
     * 
     * @param testCaseClass
     *            the test case to execute
     * @param duration
     *            the test period [ms]
     */
    private void runDuration(final Class<?> testCaseClass, final int duration)
    {
        final String testClassName = (testCaseClass == null ? config.getTestCaseClassName() : testCaseClass.toString());
        log.info("Load test thread started (" + testClassName + " / " + duration / 1000 + " s)");

        final long startTime = GlobalClock.getInstance().getTime();

        while (true)
        {
            try
            {
                status.setState(TestUserStatus.State.Waiting);
                timer.waitForNextExecution();
                status.setState(TestUserStatus.State.Running);
                runTestCase(testCaseClass, status);
            }
            catch (final InterruptedException ex)
            {
                break;
            }

            final long now = GlobalClock.getInstance().getTime();
            status.setPercentageComplete((int) ((now - startTime) * 100 / duration));
        }

        log.info("Load test thread finished.");
    }

    /**
     * Runs the passed test case *once*. Depending on the test result, the test case status is updated accordingly
     * (errors, iterations, runtime, etc.).
     * 
     * @param testCaseClass
     *            the test case to execute
     * @param status
     *            the test case status
     */
    protected void runTestCase(final Class<?> testCaseClass, final TestUserStatus status) throws InterruptedException
    {
        // check whether we have to quit before attempting a new iteration
        final SessionImpl session = SessionImpl.getCurrent();
        if (session.wasMarkedAsExpired())
        {
            throw new InterruptedException("User aborted as the load test is over");
        }

        // remember start time
        final long startTime = TimerUtils.getTime();

        // make sure transaction data recording is initiated even if XltTestRunner is not used by the test
        session.startTransaction();

        // execute the test via JUnit
        final Result result = new JUnitCore().run(testCaseClass);

        // check again whether we have to quit before dealing with the results
        if (session.wasMarkedAsExpired())
        {
            throw new InterruptedException("User aborted as the load test is over");
        }

        // reset the interrupted flag for a clean new transaction
        Thread.interrupted();

        // remember runtime
        final long runTime = TimerUtils.getTime() - startTime;

        final boolean failed = !result.wasSuccessful();
        Throwable failure = null;

        if (failed)
        {
            failure = result.getFailures().get(0).getException();

            if (log.isErrorEnabled())
            {
                log.error(String.format("Failure while executing test (user: '%s', output: '%s'):", session.getUserID(), session.getID()),
                          failure);
            }
        }

        // maintain the master controller status
        final long now = GlobalClock.getInstance().getTime();
        final DataManagerImpl dataManager = session.getDataManager();

        status.incrementIterations();
        status.addToTotalRuntime(runTime);
        status.setLastRuntime(runTime);
        status.setElapsedTime(now - status.getStartDate());
        status.setLastModifiedDate(now);
        status.setEvents(dataManager.getNumberOfEvents());

        if (failed)
        {
            status.incrementErrors();
        }

        // maintain the transaction statistics (if not already done)
        if (session.isTransactionPending())
        {
            /*
             * If transaction data recording is still in progress the test does not use XltTestRunner (which stops the
             * transaction automatically when test method execution has finished). In this case, we cannot rely on the
             * failure information tracked by the session itself but use the failure information reported by JUnit
             * instead.
             */

            // restore failure information at session...
            session.setFailed(failed);
            session.setFailReason(failure);
            // and stop the transaction
            session.stopTransaction();
        }

        // clean up
        session.clearFailedActionName();
    }
}
