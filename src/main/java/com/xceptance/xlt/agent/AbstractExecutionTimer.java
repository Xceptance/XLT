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

import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import com.xceptance.xlt.engine.SessionImpl;

/**
 * An {@link AbstractExecutionTimer} delays the load test threads between two invocations of a test case by a certain
 * amount of time. It is up to the sub classes to determine the time when to commence the next execution.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractExecutionTimer
{
    /**
     * Whether or not the execution timer has been stopped.
     */
    private volatile boolean stopped;

    /**
     * The collection of threads that have currently registered for waiting with this execution timer.
     */
    private final Collection<Thread> waitingThreads;

    /**
     * The collection of threads that have ever registered for waiting with this execution timer.
     */
    private final Collection<Thread> knownThreads;

    /**
     * Creates a new {@link AbstractExecutionTimer} instance.
     * 
     * @param userTypeName
     *            the user name
     * @param initialDelay
     *            the initial delay [ms]
     * @param duration
     *            the duration [ms]
     * @param shutdownPeriod
     *            the shutdown period [ms]
     */
    protected AbstractExecutionTimer(final String userTypeName, final long initialDelay, final long duration, final int shutdownPeriod)
    {
        waitingThreads = new HashSet<Thread>();
        knownThreads = new HashSet<Thread>();

        if (duration > 0)
        {
            // start the auto-stop timer
            final Timer timer = new Timer("AbstractExecutionTimer-" + userTypeName, true);

            // add a task to soft-kill the waiting users only, not needed if there is no shutdown period
            if (shutdownPeriod > 0)
            {
                timer.schedule(new AutoStopTimerTask(), initialDelay + duration);
            }

            // add a task to soft-kill all remaining waiting/active users
            timer.schedule(new AutoStopRemainingTimerTask(), initialDelay + duration + shutdownPeriod);
        }
    }

    /**
     * Suspends the calling thread until the time for the next test case invocation has come.
     * 
     * @throws InterruptedException
     *             if the load test thread should immediately quit
     */
    protected abstract void executeWait() throws InterruptedException;

    /**
     * Returns the collection of threads that have registered with this execution timer so far.
     * 
     * @return the threads
     */
    public Collection<Thread> getThreads()
    {
        return knownThreads;
    }

    /**
     * Indicates whether this execution timer has been stopped.
     * 
     * @return whether or not it is stopped
     */
    public boolean isStopped()
    {
        return stopped;
    }

    /**
     * Registers the calling thread with this execution timer.
     */
    private void registerCurrentThread()
    {
        final Thread t = Thread.currentThread();
        waitingThreads.add(t);
        knownThreads.add(t);
    }

    /**
     * Unregisters the calling thread from this execution timer.
     */
    private void unregisterCurrentThread()
    {
        waitingThreads.remove(Thread.currentThread());
    }

    /**
     * Stops the execution timer. This method may be called multiple times, however, only the first invocation will have
     * an effect.
     */
    public synchronized void stop()
    {
        stopped = true;
        stopThreads(knownThreads);
    }

    /**
     * Suspends the calling thread until the time for the next test case invocation has come. If the timer has been
     * stopped already, the calling thread is interrupted immediately.
     * 
     * @throws InterruptedException
     *             if the load test thread should quit immediately
     */
    public final void waitForNextExecution() throws InterruptedException
    {
        synchronized (this)
        {
            if (stopped)
            {
                // interrupt myself
                throw new InterruptedException("User quits voluntarily as the measurement period is over");
            }
            else
            {
                registerCurrentThread();
            }
        }

        try
        {
            executeWait();
        }
        finally
        {
            synchronized (this)
            {
                unregisterCurrentThread();

                if (stopped)
                {
                    // interrupt myself
                    throw new InterruptedException("User quits voluntarily as the measurement period is over");
                }
            }
        }
    }

    /**
     * Stops all currently waiting threads known to this timer instance.
     */
    private synchronized void stopWaitingThreads()
    {
        stopped = true;
        stopThreads(waitingThreads);
    }

    /**
     * Tries to stop the given user threads by either interrupting them if they are currently waiting or marking them to
     * quit voluntarily if they are executing a test case.
     * 
     * @param threads
     *            the threads to stop
     */
    private void stopThreads(final Collection<Thread> threads)
    {
        for (final Thread thread : threads)
        {
            stopThread(thread);
        }
    }

    /**
     * Tries to stop the given user thread by either interrupting it if it is currently waiting or marking it to quit
     * voluntarily if it is executing a test case.
     * 
     * @param thread
     *            the thread to stop
     */
    synchronized void stopThread(final Thread thread)
    {
        if (thread.isAlive())
        {
            if (waitingThreads.contains(thread))
            {
                // the thread is currently waiting for its next turn -> can be interrupted safely
                thread.interrupt();
            }
            else
            {
                // the thread is currently executing a test case -> just mark its session as expired
                final SessionImpl sessionImpl = SessionImpl.getSessionForThread(thread);

                // check for a valid session -> if the thread died in between, there will be no session any longer
                if (sessionImpl != null)
                {
                    sessionImpl.markAsExpired();
                }
            }
        }
    }

    /**
     * This {@link TimerTask} implementation is called after the measurement period. It interrupts (terminates) all
     * waiting threads.
     */
    private class AutoStopTimerTask extends TimerTask
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            stopWaitingThreads();
        }
    }

    /**
     * This {@link TimerTask} implementation is called after the shutdown period. It interrupts (terminates) all waiting
     * threads and marks all other threads as expired.
     */
    private class AutoStopRemainingTimerTask extends TimerTask
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            stop();
        }
    }
}
