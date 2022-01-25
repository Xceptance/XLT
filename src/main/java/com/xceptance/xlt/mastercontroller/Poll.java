/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.mastercontroller;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.util.FailedAgentControllerCollection;
import com.xceptance.xlt.util.ProgressBar;

public class Poll
{
    /** The log facility of this class. */
    private static final Logger LOG = LoggerFactory.getLogger(Poll.class);

    /** Interval between subsequent poll attempts. */
    private static final long POLL_INTERVAL = 1000;

    /**
     * A simple task to be performed by an agent controller. This task is repeatedly performed until it explicitly
     * returns <code>true</code> or throws an exception.
     */
    public static interface AgentControllerPollingTask
    {
        /**
         * Performs the polling task for the given agent controller.
         * 
         * @param agentController
         *            the agent controller
         * @return whether or not the polling task completed successfully and the given agent controller should not be
         *         polled again
         * @throws Exception
         *             thrown if querying the given agent controller has failed
         */
        public boolean call(final AgentController agentController) throws Exception;
    }

    /**
     * Poll agent controllers with a certain task. If the task was executed successfully the agent controller will not
     * get polled anymore.
     * 
     * @param executor
     *            thread pool executor
     * @param task
     *            polling task to run
     * @param controllers2Poll
     *            agent controllers to poll
     * @param failedAgentControllers
     *            failed agent controllers
     * @param progress
     *            progress bar
     * @progresscount ac
     * @return <code>true</code> if the task has finished successfully or <code>false</code> otherwise
     */
    public static boolean poll(final ThreadPoolExecutor executor, final AgentControllerPollingTask task,
                               final Collection<AgentController> controllers2Poll, final FailedAgentControllerCollection failedAgentControllers,
                               final ProgressBar progress)
    {

        final CountDownLatch latch = new CountDownLatch(controllers2Poll.size());

        // query the agent controllers
        for (final AgentController agentController : controllers2Poll)
        {
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    boolean finishAc = false;
                    try
                    {
                        finishAc = task.call(agentController);
                    }
                    catch (final Exception ex)
                    {
                        finishAc = true;

                        failedAgentControllers.add(agentController, ex);

                        LOG.error("Failed when polling " + agentController, ex);
                    }
                    finally
                    {
                        if (finishAc)
                        {
                            finishAgentControllerPolling(agentController, latch, progress);
                        }
                        else
                        {
                            // poll interval
                            try
                            {
                                Thread.sleep(POLL_INTERVAL);
                            }
                            catch (final InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            executor.execute(this);
                        }
                    }
                }
            });

        }

        // even if the loops above have finished we have to wait for the end of the executor jobs.
        boolean finished = true;
        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for polling threads coming to end failed", e);
            finished = false;
        }

        return finished;
    }

    /**
     * @progresscount 1
     */
    private static void finishAgentControllerPolling(final AgentController agentController, final CountDownLatch latch,
                                                     final ProgressBar progress)
    {
        // collect failed agent controllers
        progress.increaseCount();

        // count down latch on success or error only
        latch.countDown();
    }
}
