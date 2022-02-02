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
package com.xceptance.xlt.agent;

import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import com.xceptance.xlt.agent.unipro.CompositeFunction;
import com.xceptance.xlt.agent.unipro.Function;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * The {@link PeriodicExecutionTimer} delays all the load test threads on one agent such that they "arrive" with a
 * constant rate. If all threads are busy when a new period begins, the arrival event is not lost but stored, so that
 * the next free thread commences test execution immediately. However, the number of outstanding executions is limited
 * to the number of registered threads.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class PeriodicExecutionTimer extends AbstractExecutionTimer
{
    /**
     * The timer task that periodically recalculates the current arrival rate and controls the users accordingly.
     */
    private final ArrivalRateControllerTimerTask timerTask;

    /**
     * The timer that periodically triggers the recalculation of the arrival rate.
     */
    private final Timer timer;

    /**
     * Creates a new PeriodicExecutionTimer instance.
     * 
     * @param shutdownPeriod
     *            the shutdown period
     * @param agentIndex
     * @param agentCount
     * @param period
     *            the period between two executions
     */
    public PeriodicExecutionTimer(final String userTypeName, final long initialDelay, final long duration, final int shutdownPeriod,
                                  final int[][] arrivalRates, final int agentIndex, final double[] agentWeights)
    {
        super(userTypeName, initialDelay, duration, shutdownPeriod);

        // start the arrival rate recalculation task
        timerTask = new ArrivalRateControllerTimerTask(arrivalRates, agentIndex, agentWeights, this, initialDelay);

        timer = new Timer("PeriodicExecutionTimer-" + userTypeName, true);
        timer.scheduleAtFixedRate(timerTask, initialDelay, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeWait() throws InterruptedException
    {
        timerTask.semaphore.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        timer.cancel();
        super.stop();

        // debug code
        // int totalInvocations = 0;
        // for (final AgentEntry agentEntry : timerTask.agents)
        // {
        // System.err.printf("Invocations on Agent %d: %d\n", agentEntry.getAgentIndex(), agentEntry.getInvocations());
        // totalInvocations += agentEntry.getInvocations();
        // }
        // System.err.printf("Total invocations: %d\n", totalInvocations);
    }

    /**
     * This {@link TimerTask} implementation is called every second to recalculate the current arrival rate and to
     * control the users accordingly.
     */
    public static class ArrivalRateControllerTimerTask extends TimerTask
    {
        /**
         * The semaphore used to control the waiting threads.
         */
        private final Semaphore semaphore = new Semaphore(0, true);

        /**
         * The periodic execution timer that owns this timer task.
         */
        private final PeriodicExecutionTimer timer;

        /**
         * The index of the current agent.
         */
        private final int agentIndex;

        /**
         * The function that calculates the current arrival rate.
         */
        private final Function arrivalRateFunction;

        /**
         * A priority queue that sorts their agent entries by the number of invocations in ascending order.
         */
        private final PriorityQueue<AgentEntry> agents;

        /**
         * The time this timer task was created.
         */
        private final long startTimeMsec;

        /**
         * The time when a user was released last.
         */
        private double lastReleaseTime = 0;

        /**
         * The sampling points of the arrival rate load function.
         */
        private final int[] samplingPoints;

        /**
         * Start index used for special point lookup.
         */
        private int startIdx;

        /**
         * Constructor.
         * 
         * @param arrivalRates
         *            the arrival rates
         * @param agentIndex
         *            the agent index
         * @param agentWeights
         *            the agent weights
         * @param timer
         *            the period execution timer owning this timer task
         */
        public ArrivalRateControllerTimerTask(final int[][] arrivalRates, final int agentIndex, final double[] agentWeights,
                                              final PeriodicExecutionTimer timer, final long initialDelay)
        {
            this.agentIndex = agentIndex;
            arrivalRateFunction = new CompositeFunction(arrivalRates);
            samplingPoints = new int[arrivalRates.length];
            for (int i = 0; i < arrivalRates.length; i++)
            {
                samplingPoints[i] = arrivalRates[i][0];
            }
            agents = new PriorityQueue<AgentEntry>();
            for (int i = 0; i < agentWeights.length; i++)
            {
                agents.add(new AgentEntry(i, agentWeights[i]));
            }

            this.timer = timer;
            startTimeMsec = TimerUtils.getTime() + initialDelay;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            // calculate current time and round it to the next full second
            final double elapsedTimeSec = Math.round((TimerUtils.getTime() - startTimeMsec) / 1000.0);

            // compute number of users to release
            final int releases = computeReleases(elapsedTimeSec);

            // compute upper limit of permits for this second, but allow at least one permit per registered thread
            final int maxPermits = Math.max(releases, timer.getThreads().size());

            // release users one by one
            for (int i = 0; i < releases; i++)
            {
                // remove the agent with the fewest invocations from the queue
                final AgentEntry agentEntry = agents.poll();
                if (agentEntry.getAgentIndex() == agentIndex)
                {
                    // it is my turn!
                    // System.err.println("Releasing a user on this agent " + agentIndex);

                    // release one thread, but limit the outstanding executions (from this and the previous second) to
                    // the maximum number of permits for this second
                    if (semaphore.availablePermits() < maxPermits)
                    {
                        semaphore.release();
                    }
                }
                else
                {
                    // another agent will take care of this invocation
                    // System.err.println("Releasing a user on other agent " + agentEntry.getAgentIndex());
                }

                // update the invocations of the agent and add it to the queue again at the right position
                agentEntry.incrementInvocations((int) elapsedTimeSec);
                agents.add(agentEntry);
            }

            // System.err.printf(", lastReleased[new]: %f, releases: %d\n", lastReleaseTime, releases);
        }

        /**
         * Computes the number of users to release. The last release time is adjusted accordingly.
         * 
         * @param elapsedTimeSec
         *            the elapsed time in seconds
         * @return number of users to release
         */
        public int computeReleases(final double elapsedTimeSec)
        {
            final boolean isSpecialPoint = isSpecialPoint(elapsedTimeSec);

            final double arrivalRate = arrivalRateFunction.calculateY(elapsedTimeSec);

            int releases = 0;
            final double period = 3600 / arrivalRate;

            // System.err.printf("Elapsed: %.0fs, current arrival rate: %f, period: %f, isSpecialPoint: %b, lastReleased[old]: %f",
            // elapsedTimeSec, arrivalRate, period, isSpecialPoint, lastReleaseTime);

            /*
             * Determine the number of users to release.
             */

            if (elapsedTimeSec > 0)
            {
                final double prevArrivalRate = arrivalRateFunction.calculateY(elapsedTimeSec - 1);

                // prevArrivalRate == 0 was handled in last second)
                if (prevArrivalRate > 0)
                {
                    final double prevPeriod = 3600 / prevArrivalRate;

                    // use old period to fill remaining time as much as we can -> current time must not be reached
                    for (double nextRelease = lastReleaseTime + prevPeriod; nextRelease < elapsedTimeSec; lastReleaseTime = nextRelease, nextRelease += prevPeriod)
                    {
                        releases++;
                    }
                }

                // check for special point (sampling point of arrival rate function greater than zero)
                if (isSpecialPoint)
                {
                    // check if new period fits into the remaining time
                    if (lastReleaseTime + period <= elapsedTimeSec)
                    {
                        releases++;
                        lastReleaseTime = elapsedTimeSec;
                    }
                }
                // no special point and elapsed time is greater than zero
                else
                {
                    for (double nextRelease = lastReleaseTime + period; nextRelease <= elapsedTimeSec; lastReleaseTime = nextRelease, nextRelease += period)
                    {
                        releases++;
                    }
                }
            }
            // elapsed time is zero
            else
            {
                if (arrivalRate > 0)
                {
                    releases = 1;
                    lastReleaseTime = elapsedTimeSec;
                }
            }

            /*
             * If current arrival rate is 0, we have to check if it will increase in the next second and adjust the last
             * release time accordingly.
             */
            if (arrivalRate == 0)
            {
                final double nextSecond = elapsedTimeSec + 1.0d;
                final double nextArrivalRate = arrivalRateFunction.calculateY(nextSecond);
                if (nextArrivalRate > 0)
                {
                    final double nextPeriod = 3600 / nextArrivalRate;
                    // use maximum to prevent a rewind of the last release time
                    lastReleaseTime = Math.max(lastReleaseTime, nextSecond - nextPeriod - 0.000001);
                }
            }

            return releases;
        }

        /**
         * Returns whether or not the given time represents a special point - that is, a sampling point of the arrival
         * rate function greater than zero.
         * 
         * @param elapsedTimeSec
         *            the elapsed time in seconds
         * @return <code>true</code> if the given time represents a special point, <code>false</code> otherwise
         */
        private boolean isSpecialPoint(final double elapsedTimeSec)
        {
            boolean isSpecialPoint = false;
            for (int i = startIdx + 1; i < samplingPoints.length; i++)
            {
                if (samplingPoints[i] > elapsedTimeSec)
                {
                    break;
                }
                startIdx = i;
                if (samplingPoints[i] == elapsedTimeSec)
                {
                    isSpecialPoint = true;
                    break;
                }
            }
            return isSpecialPoint;
        }
    }

    /**
     * An entry in the agent priority queue representing one agent in the cluster.
     */
    public static class AgentEntry implements Comparable<AgentEntry>
    {
        private final int agentIndex;

        private final double agentWeight;

        private int invocations;

        private int lastInvocation;

        private double weightedInvocations;

        public AgentEntry(final int agentIndex, final double agentWeight)
        {
            this.agentIndex = agentIndex;
            this.agentWeight = agentWeight;
            lastInvocation = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final AgentEntry otherAgent)
        {
            int result = Double.compare(weightedInvocations, otherAgent.weightedInvocations);
            if (result == 0)
            {
                result = Double.compare(otherAgent.agentWeight, agentWeight);
                if (result == 0)
                {
                    result = new Integer(invocations).compareTo(otherAgent.invocations);
                    if (result == 0)
                    {
                        result = new Integer(lastInvocation).compareTo(otherAgent.lastInvocation);
                        if (result == 0)
                        {
                            result = new Integer(agentIndex).compareTo(otherAgent.agentIndex);
                        }
                    }
                }
            }
            return result;
        }

        public int getAgentIndex()
        {
            return agentIndex;
        }

        public double getAgentWeight()
        {
            return agentWeight;
        }

        public int getInvocations()
        {
            return invocations;
        }

        public double getWeightedInvocations()
        {
            return weightedInvocations;
        }

        public void incrementInvocations(final int invocationTime)
        {
            invocations++;
            weightedInvocations = invocations / agentWeight;
            lastInvocation = invocationTime;
        }
    }
}
