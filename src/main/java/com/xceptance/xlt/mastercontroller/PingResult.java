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

/**
 * Agent controller's ping result.
 */
public class PingResult
{
    /**
     * Agent controller's status.
     */
    private final Long pingTime;

    /**
     * Exception if occurred.
     */
    private final Exception exception;

    /**
     * @param status
     *            agent controller's status
     */
    public PingResult(final long pingTime)
    {
        this(pingTime, null);
    }

    /**
     * @param exception
     *            exception if occurred
     */
    public PingResult(final Exception exception)
    {
        this(null, exception);
    }

    private PingResult(final Long pingTime, final Exception exception)
    {
        this.pingTime = pingTime;
        this.exception = exception;
    }

    /**
     * Get the agent controller's last known status.
     * 
     * @return the agent controller's last known status
     */
    public long getPingTime()
    {
        return pingTime;
    }

    /**
     * Get the exception that occurred.
     * 
     * @return the exception that occurred
     */
    public Exception getException()
    {
        return exception;
    }
}
