package com.xceptance.xlt.agent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.xceptance.xlt.agentcontroller.TestUserConfiguration;

/**
 * The ExecutionTimerFactory creates different ExecutionTimer instances depending on the creation parameters. Note that
 * only one execution timer instance is created for one type of test case and this instance is shared between all load
 * test threads running this type of test.
 * 
 * @see PeriodicExecutionTimer
 * @see RandomExecutionTimer
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class ExecutionTimerFactory
{
    /**
     * Maps a test case case name to its corresponding execution timer.
     */
    private static final Map<String, AbstractExecutionTimer> timers = new HashMap<String, AbstractExecutionTimer>();

    /**
     * Returns all the executions timers that have been created so far.
     * 
     * @return the execution timers
     */
    public static Collection<AbstractExecutionTimer> getTimers()
    {
        return Collections.unmodifiableCollection(timers.values());
    }

    /**
     * Returns the one execution timer responsible for the given test case class. Depending on the specified
     * configuration parameters, a different class will be instantiated.
     * 
     * @param config
     *            the user configuration
     * @return the execution timer
     */
    public static synchronized AbstractExecutionTimer createTimer(final TestUserConfiguration config)
    {
        final String userTypeName = config.getUserName();
        AbstractExecutionTimer executionTimer = timers.get(userTypeName);

        // check whether we already have one
        if (executionTimer == null)
        {
            // no -> create one
            final int[][] arrivalRates = config.getArrivalRates();
            final int[][] users = config.getUsers();
            final int shutdownPeriod = config.getShutdownPeriod();
            final int agentIndex = config.getAgentIndex();

            final int warmUpPeriod = config.getWarmUpPeriod();
            final int measurementPeriod = config.getMeasurementPeriod();
            final int initialDelay = config.getInitialDelay();
            final int duration = warmUpPeriod + measurementPeriod;

            if (arrivalRates == null)
            {
                executionTimer = new RandomExecutionTimer(userTypeName, initialDelay, duration, shutdownPeriod, users, agentIndex);
            }
            else
            {
                executionTimer = new PeriodicExecutionTimer(userTypeName, initialDelay, duration, shutdownPeriod, arrivalRates, agentIndex,
                                                            config.getWeightFunction());
            }

            timers.put(userTypeName, executionTimer);
        }

        return executionTimer;
    }
}
