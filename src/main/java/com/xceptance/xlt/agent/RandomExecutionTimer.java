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
package com.xceptance.xlt.agent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * The {@link RandomExecutionTimer} delays the load test threads by a random time. The range for the waiting time can be
 * controlled by configuration.
 * <p>
 * The count of runnable users is controlled by a load function and may vary over time. The current controller
 * implementation is unfair in the sense that users having a permit to run will keep it until the number of users
 * decreases.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class RandomExecutionTimer extends AbstractExecutionTimer
{
    /**
     * The timer task that periodically recalculates the current user count and controls the users accordingly.
     */
    private final UserCountControllerTimerTask timerTask;

    /**
     * The current number of permits to take out of the system.
     */
    private int permitsToSwallow;

    /**
     * The think time.
     */
    private final int thinkTime;

    /**
     * The think time deviation.
     */
    private final int thinkTimeDeviation;

    /**
     * The timer that periodically triggers the recalculation of the user count.
     */
    private final Timer timer;

    /**
     * Flag indicating whether the current thread calls {@link #executeWait()} the first time or not.
     */
    private final ThreadLocal<Boolean> waitsTheFirstTime = new ThreadLocal<Boolean>();

    /**
     * Creates a new {@link RandomExecutionTimer} instance.
     *
     * @param userTypeName
     * @param duration
     * @param shutdownPeriod
     * @param users
     * @param agentIndex
     * @param weightFunction
     */
    public RandomExecutionTimer(final String userTypeName, final long initialDelay, final long duration, final int shutdownPeriod,
                                final int[][] users, final int agentIndex)
    {
        super(userTypeName, initialDelay, duration, shutdownPeriod);

        // get the default think time parameters from the configuration
        thinkTime = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".thinktime.transaction", 0);
        thinkTimeDeviation = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".thinktime.transaction.deviation", 0);

        // start the user count recalculation task
        timer = new Timer("RandomExecutionTimer-" + userTypeName, true);
        timerTask = new UserCountControllerTimerTask(users, this, initialDelay);
        timer.scheduleAtFixedRate(timerTask, initialDelay, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeWait() throws InterruptedException
    {
        // simulate inter-transaction think time
        final long resultingThinkTime = Math.max(0, XltRandom.nextIntWithDeviation(thinkTime, thinkTimeDeviation));

        final Logger logger = XltLogger.runTimeLogger;
        if (logger.isInfoEnabled())
        {
            logger.info("Executing transaction think time wait (" + resultingThinkTime + " ms)...");
        }

        if (resultingThinkTime > 0)
        {
            Thread.sleep(resultingThinkTime);
        }

        // try to start next iteration
        boolean doAcquire = false;

        if (waitsTheFirstTime.get() == null)
        {
            // it is the first time for the current thread, need to acquire a permit
            waitsTheFirstTime.set(false);
            doAcquire = true;
        }
        // permits have been revoked, we need to re-apply for a free one
        else
        {
            synchronized (this)
            {
                if (permitsToSwallow > 0)
                {
                    permitsToSwallow--;
                    doAcquire = true;
                }
            }
        }

        if (doAcquire)
        {
            timerTask.semaphore.acquire();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        timer.cancel();
        super.stop();
    }

    private synchronized void addToPermits(final int permits)
    {
        permitsToSwallow += permits;
    }

    /**
     * This {@link TimerTask} implementation is called every second to calculate the number of allowed active users.
     */
    public static class UserCountControllerTimerTask extends TimerTask
    {
        /**
         * The execution timer that start this timer task.
         */
        private final RandomExecutionTimer executionTimer;

        /**
         * The semaphore used to control the waiting threads.
         */
        private final Semaphore semaphore;

        /**
         * The time this timer task was created.
         */
        private final long startTime;

        /**
         * Any initial delay configured.
         */
        private final long initialDelay;

        /**
         * The function to calculate the load.
         */
        private final int[][] users;

        /**
         * Last total users.
         */
        private int lastTotal;

        /**
         * The index to use for time lookup.
         */
        private int idx = 0;

        /**
         * Constructor.
         *
         * @param users
         *            the users
         */
        public UserCountControllerTimerTask(final int[][] users, final RandomExecutionTimer executionTimer, final long initialDelay)
        {
            this.users = users;
            semaphore = new Semaphore(0, true);
            this.executionTimer = executionTimer;
            this.initialDelay = initialDelay;
            this.startTime = TimerUtils.get().getStartTime();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            // calculate current time and round it to the next full second
            final long elapsedTimeSec = Math.round((TimerUtils.get().getElapsedTime(startTime) - initialDelay) / 1000.0);
            run(elapsedTimeSec);
        }

        public void run(final long elapsedTimeSec)
        {
            int totalUsers = lastTotal;
            while (idx < users.length && users[idx][0] <= elapsedTimeSec)
            {
                totalUsers = users[idx++][1];
            }
            // check if something to do
            if (totalUsers == lastTotal)
            {
                return;
            }

            // remember the difference
            final int usersToRelease = totalUsers - lastTotal;
            lastTotal = totalUsers;

            if (usersToRelease < 0)
            {
                executionTimer.addToPermits(-usersToRelease);
            }
            else
            {
                semaphore.release(usersToRelease);
            }
        }

        public int getLastTotal()
        {
            return lastTotal;
        }
    }
}
