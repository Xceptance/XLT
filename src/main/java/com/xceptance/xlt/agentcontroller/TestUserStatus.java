/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.io.Serializable;

import com.xceptance.xlt.api.engine.GlobalClock;

/**
 * Status of a load test user.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TestUserStatus implements Serializable
{
    /**
     * Available running modes.
     */
    public enum Mode
    {
        ITERATION, TIME_PERIOD
    }

    /**
     * Available states.
     */
    public enum State
    {
        Aborted, Failed, Finished, Running, Waiting
    }

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * elapsed time
     */
    private long elapsedTime;

    /**
     * date when this user will stop
     */
    private long endDate;

    /**
     * number of errors
     */
    private int errors;

    /**
     * number of errors
     */
    private int events;

    /**
     * last known exception thrown by a test run
     */
    private Exception exception;

    /**
     * number of iterations
     */
    private int iterations;

    /**
     * last modified
     */
    private long lastModifiedDate;

    /**
     * runtime of last test run
     */
    private long lastRuntime;

    /**
     * running mode of test user
     */
    private Mode mode;

    /**
     * degree of completeness
     */
    private int percentageComplete;

    /**
     * date when this user has been started
     */
    private long startDate;

    /**
     * current state of user
     */
    private State state;

    /**
     * total runtime of all test runs
     */
    private long totalRuntime;

    /**
     * name of test user
     */
    private String userName;

    /**
     * Creates a new test user status.
     * <p>
     * Start date will be set to the current time and test user state will be set to <em>Waiting</em>.
     * </p>
     */
    public TestUserStatus()
    {
        startDate = GlobalClock.get().millis();
        state = State.Waiting;
    }

    public void addToTotalRuntime(final long millis)
    {
        totalRuntime += millis;
    }

    /**
     * Returns the average test case run time in seconds.
     * 
     * @return the average runtime
     */

    public long getAverageRuntime()
    {
        return (iterations == 0) ? 0 : totalRuntime / iterations;
    }

    /**
     * Returns the value of the 'elapsedTime' attribute.
     * 
     * @return the value of elapsedTime
     */
    public long getElapsedTime()
    {
        return elapsedTime;
    }

    /**
     * Returns the value of the 'endDate' attribute.
     * 
     * @return the value of endDate
     */
    public long getEndDate()
    {
        return endDate;
    }

    /**
     * Returns the value of the 'errors' attribute.
     * 
     * @return the value of errors
     */
    public int getErrors()
    {
        return errors;
    }

    /**
     * Returns the value of the 'events' attribute.
     * 
     * @return the value of errors
     */
    public int getEvents()
    {
        return events;
    }

    /**
     * Returns the value of the 'exception' attribute.
     * 
     * @return the value of exception
     */
    public Exception getException()
    {
        return exception;
    }

    /**
     * Returns the value of the 'iterations' attribute.
     * 
     * @return the value of iterations
     */
    public int getIterations()
    {
        return iterations;
    }

    /**
     * Returns the value of the 'lastModifiedDate' attribute.
     * 
     * @return the value of lastModifiedDate
     */
    public long getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    /**
     * Returns the value of the 'lastRuntime' attribute.
     * 
     * @return the value of lastRuntime
     */
    public long getLastRuntime()
    {
        return lastRuntime;
    }

    /**
     * Returns the value of the 'mode' attribute.
     * 
     * @return the value of mode
     */
    public Mode getMode()
    {
        return mode;
    }

    /**
     * Returns the value of the 'percentageComplete' attribute.
     * 
     * @return the value of percentageComplete
     */
    public int getPercentageComplete()
    {
        return percentageComplete;
    }

    /**
     * Returns the value of the 'startDate' attribute.
     * 
     * @return the value of startDate
     */
    public long getStartDate()
    {
        return startDate;
    }

    /**
     * Returns the value of the 'state' attribute.
     * 
     * @return the value of state
     */
    public State getState()
    {
        return state;
    }

    /**
     * Returns the value of the 'totalRuntime' attribute.
     * 
     * @return the value of totalRuntime
     */
    public long getTotalRuntime()
    {
        return totalRuntime;
    }

    /**
     * Returns the value of the 'userName' attribute.
     * 
     * @return the value of userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Increments the number of errors.
     */
    public void incrementErrors()
    {
        ++errors;
    }

    /**
     * Increments the number of events.
     */
    public void incrementEvents()
    {
        ++events;
    }

    /**
     * Increments the number of iterations.
     */
    public void incrementIterations()
    {
        ++iterations;
    }

    /**
     * @return the completed
     */

    public boolean isCompleted()
    {
        return percentageComplete == 100;
    }

    /**
     * Sets the new value of the 'elapsedTime' attribute.
     * 
     * @param elapsedTime
     *            the new elapsedTime value
     */
    public void setElapsedTime(final long elapsedTime)
    {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Sets the new value of the 'endDate' attribute.
     * 
     * @param endDate
     *            the new endDate value
     */
    public void setEndDate(final long endDate)
    {
        this.endDate = endDate;
    }

    /**
     * Sets the new value of the 'errors' attribute.
     * 
     * @param errors
     *            the new errors value
     */
    public void setErrors(final int errors)
    {
        this.errors = errors;
    }

    /**
     * Sets the new value of the 'events' attribute.
     * 
     * @param events
     *            the new events value
     */
    public void setEvents(final int events)
    {
        this.events = events;
    }

    /**
     * Sets the new value of the 'exception' attribute.
     * 
     * @param exception
     *            the new exception value
     */
    public void setException(final Exception exception)
    {
        this.exception = exception;
    }

    /**
     * Sets the new value of the 'iterations' attribute.
     * 
     * @param iterations
     *            the new iterations value
     */
    public void setIterations(final int iterations)
    {
        this.iterations = iterations;
    }

    /**
     * Sets the new value of the 'lastModifiedDate' attribute.
     * 
     * @param lastModifiedDate
     *            the new lastModifiedDate value
     */
    public void setLastModifiedDate(final long lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Sets the new value of the 'lastRuntime' attribute.
     * 
     * @param lastRuntime
     *            the new lastRuntime value
     */
    public void setLastRuntime(final long lastRuntime)
    {
        this.lastRuntime = lastRuntime;
    }

    /**
     * Sets the new value of the 'mode' attribute.
     * 
     * @param mode
     *            the new mode value
     */
    public void setMode(final Mode mode)
    {
        this.mode = mode;
    }

    /**
     * Sets the new value of the 'percentageComplete' attribute.
     * 
     * @param percentageComplete
     *            the new percentageComplete value
     */
    public void setPercentageComplete(final int percentageComplete)
    {
        this.percentageComplete = percentageComplete;
    }

    /**
     * Sets the new value of the 'startDate' attribute.
     * 
     * @param startDate
     *            the new startDate value
     */
    public void setStartDate(final long startDate)
    {
        this.startDate = startDate;
    }

    /**
     * Sets the new value of the 'state' attribute.
     * 
     * @param state
     *            the new state value
     */
    public void setState(final State state)
    {
        this.state = state;
    }

    /**
     * Sets the new value of the 'totalRuntime' attribute.
     * 
     * @param totalRuntime
     *            the new totalRuntime value
     */
    public void setTotalRuntime(final long totalRuntime)
    {
        this.totalRuntime = totalRuntime;
    }

    /**
     * Sets the new value of the 'userName' attribute.
     * 
     * @param userName
     *            the new userName value
     */
    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("[user=%s, iterations=%d, lastRuntime=%d ms, averageRuntime=%d ms, totalRuntime=%d ms, errors=%d, progress=%d%%]",
                             getUserName(), getIterations(), getLastRuntime(), getAverageRuntime(), getTotalRuntime(), getErrors(),
                             getPercentageComplete());
    }
}
